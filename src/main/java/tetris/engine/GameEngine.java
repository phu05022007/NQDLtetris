package tetris.engine;

import javafx.animation.AnimationTimer;
import tetris.engine.state.GameOverState;
import tetris.engine.state.GameState;
import tetris.engine.state.MenuState;
import tetris.engine.state.PausedState;
import tetris.engine.state.PlayingState;
import tetris.model.Board;
import tetris.model.Tetromino;
import tetris.model.TetrominoFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Context in State Pattern. Holds current state and delegates behavior to it.
 */
public class GameEngine {
    private static final long UPDATE_INTERVAL_NS = 500_000_000L;
    private static final long TARGET_FRAME_INTERVAL_NS = 16_666_667L;

    private final Board board;
    private final TetrominoFactory tetrominoFactory;

    private final GameState menuState;
    private final GameState playingState;
    private final GameState pausedState;
    private final GameState gameOverState;

    private GameState currentState;
    private Tetromino currentTetromino;
    private TetrominoFactory.TetrominoType heldTetrominoType;
    private boolean holdUsedThisTurn;
    private int score;
    private int totalClearedLines;
    private final List<HoldPieceListener> holdPieceListeners;

    private AnimationTimer gameLoopTimer;
    private GameRenderer renderer;
    private long previousNow;
    private long updateAccumulatorNs;
    private long renderAccumulatorNs;

    public GameEngine() {
        this.board = new Board();
        this.tetrominoFactory = new TetrominoFactory();

        this.menuState = new MenuState();
        this.playingState = new PlayingState();
        this.pausedState = new PausedState();
        this.gameOverState = new GameOverState();
        this.holdPieceListeners = new CopyOnWriteArrayList<HoldPieceListener>();
        this.heldTetrominoType = null;
        this.holdUsedThisTurn = false;

        this.currentState = menuState;
        this.currentState.enter(this);
    }

    public void changeState(GameState nextState) {
        currentState.exit(this);
        currentState = nextState;
        currentState.enter(this);
    }

    public void handleInput(GameAction action) {
        currentState.handleInput(this, action);
    }

    public void update() {
        currentState.update(this);
    }

    public void render(GameRenderer renderer) {
        currentState.render(this, renderer);
    }

    public void start(GameRenderer renderer) {
        this.renderer = renderer;
        this.previousNow = 0L;
        this.updateAccumulatorNs = 0L;
        this.renderAccumulatorNs = 0L;

        if (gameLoopTimer != null) {
            gameLoopTimer.stop();
        }

        gameLoopTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (previousNow == 0L) {
                    previousNow = now;
                    return;
                }

                long deltaTimeNs = now - previousNow;
                previousNow = now;

                updateAccumulatorNs += deltaTimeNs;
                renderAccumulatorNs += deltaTimeNs;

                while (updateAccumulatorNs >= UPDATE_INTERVAL_NS) {
                    GameEngine.this.update();
                    updateAccumulatorNs -= UPDATE_INTERVAL_NS;
                }

                while (renderAccumulatorNs >= TARGET_FRAME_INTERVAL_NS) {
                    GameEngine.this.render(GameEngine.this.renderer);
                    renderAccumulatorNs -= TARGET_FRAME_INTERVAL_NS;
                }
            }
        };

        gameLoopTimer.start();
    }

    public void stop() {
        if (gameLoopTimer != null) {
            gameLoopTimer.stop();
        }
    }

    public void resetGame() {
        board.clear();
        currentTetromino = null;
        holdUsedThisTurn = false;
        setHeldTetrominoType(null);
        score = 0;
        totalClearedLines = 0;
    }

    public boolean stepDown() {
        if (currentTetromino == null) {
            return spawnRandomTetromino(true);
        }

        int nextY = currentTetromino.getY() + 1;
        if (board.checkCollision(currentTetromino.getX(), nextY, currentTetromino.getShape())) {
            board.lock(currentTetromino);
            int cleared = board.clearFullLines();
            totalClearedLines += cleared;
            score += calculateScore(cleared);
            return spawnRandomTetromino(true);
        }

        currentTetromino.setPosition(currentTetromino.getX(), nextY);
        return false;
    }

    public void moveCurrent(int dx, int dy) {
        if (currentTetromino == null) {
            return;
        }

        int targetX = currentTetromino.getX() + dx;
        int targetY = currentTetromino.getY() + dy;
        if (!board.checkCollision(targetX, targetY, currentTetromino.getShape())) {
            currentTetromino.setPosition(targetX, targetY);
        }
    }

    public void rotateCurrentClockwise() {
        if (currentTetromino == null) {
            return;
        }

        currentTetromino.rotateClockwise();
        if (board.checkCollision(
                currentTetromino.getX(),
                currentTetromino.getY(),
                currentTetromino.getShape()
        )) {
            // Revert if rotation collides.
            currentTetromino.rotateCounterClockwise();
        }
    }

    public boolean holdCurrentPiece() {
        if (currentTetromino == null || holdUsedThisTurn) {
            return false;
        }

        TetrominoFactory.TetrominoType currentType = tetrominoFactory.typeFromId(currentTetromino.getId());

        if (heldTetrominoType == null) {
            setHeldTetrominoType(currentType);
            holdUsedThisTurn = true;
            return spawnRandomTetromino(false);
        }

        TetrominoFactory.TetrominoType nextType = heldTetrominoType;
        setHeldTetrominoType(currentType);
        holdUsedThisTurn = true;
        return spawnTetrominoOfType(nextType, false);
    }

    public GameState getMenuState() {
        return menuState;
    }

    public GameState getPlayingState() {
        return playingState;
    }

    public GameState getPausedState() {
        return pausedState;
    }

    public GameState getGameOverState() {
        return gameOverState;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public Board getBoard() {
        return board;
    }

    public Tetromino getCurrentTetromino() {
        return currentTetromino;
    }

    public TetrominoFactory.TetrominoType getHeldTetrominoType() {
        return heldTetrominoType;
    }

    public void addHoldPieceListener(HoldPieceListener listener) {
        if (listener != null) {
            holdPieceListeners.add(listener);
            listener.onHoldPieceChanged(heldTetrominoType);
        }
    }

    public void removeHoldPieceListener(HoldPieceListener listener) {
        holdPieceListeners.remove(listener);
    }

    /**
     * Simulates dropping current piece vertically until the next step collides.
     * Returns the deepest valid Y for rendering ghost piece.
     */
    public int getGhostY() {
        if (currentTetromino == null) {
            return -1;
        }

        int targetX = currentTetromino.getX();
        int ghostY = currentTetromino.getY();
        int[][] shape = currentTetromino.getShape();

        while (!board.checkCollision(targetX, ghostY + 1, shape)) {
            ghostY++;
        }

        return ghostY;
    }

    public int getScore() {
        return score;
    }

    public int getTotalClearedLines() {
        return totalClearedLines;
    }

    private boolean spawnRandomTetromino(boolean resetHoldForNewTurn) {
        currentTetromino = tetrominoFactory.createRandom();
        if (resetHoldForNewTurn) {
            holdUsedThisTurn = false;
        }
        return board.checkCollision(currentTetromino.getX(), currentTetromino.getY(), currentTetromino.getShape());
    }

    private boolean spawnTetrominoOfType(TetrominoFactory.TetrominoType type, boolean resetHoldForNewTurn) {
        currentTetromino = tetrominoFactory.create(type);
        if (resetHoldForNewTurn) {
            holdUsedThisTurn = false;
        }
        return board.checkCollision(currentTetromino.getX(), currentTetromino.getY(), currentTetromino.getShape());
    }

    private void setHeldTetrominoType(TetrominoFactory.TetrominoType type) {
        heldTetrominoType = type;
        notifyHoldPieceChanged();
    }

    private void notifyHoldPieceChanged() {
        for (HoldPieceListener listener : holdPieceListeners) {
            listener.onHoldPieceChanged(heldTetrominoType);
        }
    }

    private int calculateScore(int clearedLines) {
        switch (clearedLines) {
            case 1:
                return 100;
            case 2:
                return 300;
            case 3:
                return 500;
            case 4:
                return 800;
            default:
                return 0;
        }
    }
}
