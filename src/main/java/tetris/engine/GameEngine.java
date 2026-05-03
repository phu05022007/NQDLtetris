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
import java.util.Random;
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
    private final List<NextPieceListener> nextPieceListeners;

    private AnimationTimer gameLoopTimer;
    private GameRenderer renderer;
    private long previousNow;
    private long updateAccumulatorNs;
    private long renderAccumulatorNs;

    // Line clear animation state
    private boolean lineClearAnimating = false;
    private int[] linesToClear = null;
    private long lineClearElapsedNs = 0L;
    private int lineClearColorIndex = 0; // 0=yellow,1=red
    private static final long LINE_CLEAR_ANIMATION_NS = 600_000_000L; // 600ms
    private static final long LINE_CLEAR_FLASH_PERIOD_NS = 150_000_000L; // 150ms
    private static final long FAST_LINE_CLEAR_ANIMATION_NS = 300_000_000L; // 300ms for hard-drop
    private static final long FAST_LINE_CLEAR_FLASH_PERIOD_NS = 75_000_000L; // 75ms flash for hard-drop
    private long currentLineClearAnimationNs = LINE_CLEAR_ANIMATION_NS;
    private long currentLineClearFlashPeriodNs = LINE_CLEAR_FLASH_PERIOD_NS;
    private final Random random = new Random();

    private TetrominoFactory.TetrominoType nextTetrominoType;

    // Language for UI/localized text. Null until set by UI; default to EN when accessed.
    public enum Language {
        EN,
        VI
    }

    private Language language = null;

    public void setLanguage(Language language) {
        this.language = language == null ? Language.EN : language;
    }

    public Language getLanguage() {
        return this.language == null ? Language.EN : this.language;
    }

    /**
     * Minimal localized string lookup used by state renderers and UI.
     */
    public String getText(String key) {
        Language lang = getLanguage();
        switch (lang) {
            case VI:
                switch (key) {
                    case "menu.title": return "TETRIS";
                    case "menu.start": return "Nhấn ENTER để bắt đầu";
                    case "paused.title": return "Tạm dừng";
                    case "paused.instructions": return "Nhấn R để tiếp tục, Esc để về menu";
                    case "gameover.title": return "KẾT THÚC";
                    case "gameover.instructions": return "Nhấn R để chơi lại, Esc để về menu";
                    case "label.score": return "Điểm: ";
                    case "label.lines": return "Hàng: ";
                    case "help.title": return "Hướng dẫn chơi";
                    case "help.move": return "Di chuyển: ← →";
                    case "help.rotate": return "Xoay: ↑";
                    case "help.softdrop": return "Rơi mềm: ↓";
                    case "help.harddrop": return "Rơi nhanh: Space";
                    case "help.hold": return "Giữ khối: C";
                    case "help.controls": return "Bắt đầu: Enter | Tạm dừng: P | Tiếp tục: R";
                    case "help.menu": return "Quay về menu: Esc | Hiện/ẩn hướng dẫn: H";
                    case "help.goal": return "Mục tiêu: Hoàn thành hàng để ghi điểm: 1=100,2=300,3=500,4=800";
                    default: return key;
                }
            default:
                switch (key) {
                    case "menu.title": return "TETRIS";
                    case "menu.start": return "Press ENTER to start";
                    case "paused.title": return "PAUSED";
                    case "paused.instructions": return "Press R to resume, Esc to return to menu";
                    case "gameover.title": return "GAME OVER";
                    case "gameover.instructions": return "Press R to restart, Esc to return to menu";
                    case "label.score": return "Score: ";
                    case "label.lines": return "Lines: ";
                    case "help.title": return "How to play";
                    case "help.move": return "Move: ← →";
                    case "help.rotate": return "Rotate: ↑";
                    case "help.softdrop": return "Soft drop: ↓";
                    case "help.harddrop": return "Hard drop: Space";
                    case "help.hold": return "Hold: C";
                    case "help.controls": return "Start: Enter | Pause: P | Resume: R";
                    case "help.menu": return "Back to menu: Esc | Toggle help: H";
                    case "help.goal": return "Goal: clear lines to score: 1=100,2=300,3=500,4=800";
                    default: return key;
                }
        }
    }

    public GameEngine() {
        this.board = new Board();
        this.tetrominoFactory = new TetrominoFactory();

        this.menuState = new MenuState();
        this.playingState = new PlayingState();
        this.pausedState = new PausedState();
        this.gameOverState = new GameOverState();
        this.holdPieceListeners = new CopyOnWriteArrayList<HoldPieceListener>();
        this.nextPieceListeners = new CopyOnWriteArrayList<NextPieceListener>();
        // initialize next piece
        this.nextTetrominoType = tetrominoFactory.randomType();
        this.heldTetrominoType = null;
        this.holdUsedThisTurn = false;

        this.currentState = menuState;
        this.currentState.enter(this);
    }

    public TetrominoFactory.TetrominoType getNextTetrominoType() {
        return this.nextTetrominoType;
    }

    public void addNextPieceListener(NextPieceListener listener) {
        if (listener != null) {
            nextPieceListeners.add(listener);
            listener.onNextPieceChanged(nextTetrominoType);
        }
    }

    public void removeNextPieceListener(NextPieceListener listener) {
        nextPieceListeners.remove(listener);
    }

    private void notifyNextPieceChanged(TetrominoFactory.TetrominoType nextType) {
        for (NextPieceListener l : nextPieceListeners) {
            l.onNextPieceChanged(nextType);
        }
    }

    public void changeState(GameState nextState) {
        currentState.exit(this);
        currentState = nextState;
        currentState.enter(this);
        // Hide overlay when switching to playing state so gameplay is visible.
        if (renderer != null && nextState == playingState) {
            renderer.hideOverlay();
        }
    }

    public void handleInput(GameAction action) {
        if (lineClearAnimating) {
            // Ignore input while clearing animation plays
            return;
        }

        currentState.handleInput(this, action);
    }

    public void update() {
        if (lineClearAnimating) {
            // Pause game updates while animation plays
            return;
        }

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

                // Advance line-clear animation timer if active
                if (lineClearAnimating) {
                    lineClearElapsedNs += deltaTimeNs;
                }

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

                // Finalize line clear after animation duration
                if (lineClearAnimating && lineClearElapsedNs >= currentLineClearAnimationNs) {
                    finalizeLineClearAnimation();
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

            // Detect full lines first and play animation if any.
            int[] fullLines = board.getFullLines();
            if (fullLines != null && fullLines.length > 0) {
                // Start line clear animation and postpone removal/spawn until it's done.
                startLineClearAnimation(fullLines);
                // Clear current tetromino reference since it's locked to the board now.
                currentTetromino = null;
                return false;
            }

            int cleared = board.clearFullLines();
            totalClearedLines += cleared;
            score += calculateScore(cleared);
            return spawnRandomTetromino(true);
        }

        currentTetromino.setPosition(currentTetromino.getX(), nextY);
        return false;
    }

    /**
     * Perform a hard drop: move current piece to the lowest valid Y, lock it,
     * clear lines, update score, and spawn the next piece. Returns true if
     * spawning the next piece collides (game over condition).
     */
    public boolean hardDrop() {
        if (currentTetromino == null) {
            return spawnRandomTetromino(true);
        }

        int ghostY = getGhostY();
        currentTetromino.setPosition(currentTetromino.getX(), ghostY);
        board.lock(currentTetromino);
        int[] fullLines = board.getFullLines();
        if (fullLines != null && fullLines.length > 0) {
            // Faster, snappier animation when lines are cleared by hard drop
            startLineClearAnimation(fullLines, FAST_LINE_CLEAR_ANIMATION_NS, FAST_LINE_CLEAR_FLASH_PERIOD_NS);
            // Current tetromino is locked on board; clear reference and postpone spawn until animation completes.
            currentTetromino = null;
            return false;
        }

        return spawnRandomTetromino(true);
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
        TetrominoFactory.TetrominoType toSpawn = (nextTetrominoType != null) ? nextTetrominoType : tetrominoFactory.randomType();
        currentTetromino = tetrominoFactory.create(toSpawn);
        // prepare next piece and notify listeners
        nextTetrominoType = tetrominoFactory.randomType();
        notifyNextPieceChanged(nextTetrominoType);
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

    private void startLineClearAnimation(int[] rows) {
        startLineClearAnimation(rows, LINE_CLEAR_ANIMATION_NS, LINE_CLEAR_FLASH_PERIOD_NS);
    }

    private void startLineClearAnimation(int[] rows, long animationNs, long flashPeriodNs) {
        if (rows == null || rows.length == 0) {
            return;
        }
        this.linesToClear = rows.clone();
        this.lineClearColorIndex = random.nextInt(2); // 0 or 1
        this.lineClearElapsedNs = 0L;
        this.lineClearAnimating = true;
        this.currentLineClearAnimationNs = animationNs > 0 ? animationNs : LINE_CLEAR_ANIMATION_NS;
        this.currentLineClearFlashPeriodNs = flashPeriodNs > 0 ? flashPeriodNs : LINE_CLEAR_FLASH_PERIOD_NS;
        board.setPendingClearRows(rows, lineClearColorIndex, currentLineClearFlashPeriodNs);
    }

    private void finalizeLineClearAnimation() {
        if (linesToClear == null || linesToClear.length == 0) {
            lineClearAnimating = false;
            lineClearElapsedNs = 0L;
            return;
        }

        int cleared = linesToClear.length;

        // Remove the rows from the board and clear pending markers
        board.removeLines(linesToClear);
        board.clearPendingClearRows();

        // Reset animation state
        lineClearAnimating = false;
        lineClearElapsedNs = 0L;

        // Update score and counters
        totalClearedLines += cleared;
        score += calculateScore(cleared);

        // Spawn next piece and handle game over
        boolean gameOver = spawnRandomTetromino(true);
        if (gameOver) {
            changeState(gameOverState);
        }
    }
}
