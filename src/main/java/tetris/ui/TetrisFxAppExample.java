package tetris.ui;

import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tetris.engine.GameAction;
import tetris.engine.GameEngine;
import tetris.model.Board;

import java.util.EnumSet;
import java.util.Set;

/**
 * Example JavaFX entry point showing how to wire GameEngine + BoardRenderer.
 */
public class TetrisFxAppExample extends Application {
    private static final double CELL_SIZE = 30.0;
    private static final long MOVE_INITIAL_DELAY_NS = 120_000_000L;
    private static final long MOVE_REPEAT_NS = 50_000_000L;
    private static final long SOFT_DROP_REPEAT_NS = 35_000_000L;

    private final Set<KeyCode> pressedKeys = EnumSet.noneOf(KeyCode.class);

    private GameEngine engine;
    private HoldPanelRenderer holdPanelRenderer;
    private AnimationTimer inputTimer;

    private long leftHeldNs;
    private long rightHeldNs;
    private long downHeldNs;
    private long leftRepeatNs;
    private long rightRepeatNs;
    private long downRepeatNs;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(Board.COLUMNS * CELL_SIZE, (Board.ROWS + 3) * CELL_SIZE);
        Canvas holdCanvas = new Canvas(6 * CELL_SIZE, 6 * CELL_SIZE);

        BoardRenderer renderer = new BoardRenderer(canvas, CELL_SIZE);
        holdPanelRenderer = new HoldPanelRenderer(holdCanvas, CELL_SIZE);
        engine = new GameEngine();
        engine.addHoldPieceListener(holdPanelRenderer);

        Scene scene = new Scene(new HBox(12, canvas, holdCanvas));
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            boolean firstPress = pressedKeys.add(code);

            // One-shot actions should happen once when key goes down.
            if (!firstPress) {
                return;
            }

            switch (code) {
                case ENTER:
                    engine.handleInput(GameAction.START);
                    break;
                case UP:
                case SPACE:
                    engine.handleInput(GameAction.ROTATE);
                    break;
                case P:
                    engine.handleInput(GameAction.PAUSE);
                    break;
                case C:
                    engine.handleInput(GameAction.HOLD);
                    break;
                case R:
                    engine.handleInput(GameAction.RESUME);
                    break;
                case ESCAPE:
                    engine.handleInput(GameAction.BACK_TO_MENU);
                    break;
                default:
                    break;
            }
        });
        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));

        stage.setTitle("Tetris - JavaFX Canvas");
        stage.setScene(scene);
        stage.show();

        engine.start(renderer);
        scene.getRoot().requestFocus();
        startInputLoop();
    }

    @Override
    public void stop() {
        if (inputTimer != null) {
            inputTimer.stop();
        }
        if (engine != null) {
            if (holdPanelRenderer != null) {
                engine.removeHoldPieceListener(holdPanelRenderer);
            }
            engine.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void startInputLoop() {
        inputTimer = new AnimationTimer() {
            private long previousNow;

            @Override
            public void handle(long now) {
                if (previousNow == 0L) {
                    previousNow = now;
                    return;
                }

                long deltaNs = now - previousNow;
                previousNow = now;

                processHorizontal(KeyCode.LEFT, deltaNs);
                processHorizontal(KeyCode.RIGHT, deltaNs);
                processSoftDrop(deltaNs);
            }
        };
        inputTimer.start();
    }

    private void processHorizontal(KeyCode key, long deltaNs) {
        boolean isLeft = key == KeyCode.LEFT;
        long heldNs = isLeft ? leftHeldNs : rightHeldNs;
        long repeatNs = isLeft ? leftRepeatNs : rightRepeatNs;

        if (pressedKeys.contains(key)) {
            heldNs += deltaNs;
            repeatNs += deltaNs;

            boolean shouldMoveNow = heldNs <= deltaNs
                    || (heldNs >= MOVE_INITIAL_DELAY_NS && repeatNs >= MOVE_REPEAT_NS);

            if (shouldMoveNow) {
                engine.handleInput(isLeft ? GameAction.LEFT : GameAction.RIGHT);
                if (heldNs >= MOVE_INITIAL_DELAY_NS) {
                    repeatNs = 0L;
                }
            }
        } else {
            heldNs = 0L;
            repeatNs = 0L;
        }

        if (isLeft) {
            leftHeldNs = heldNs;
            leftRepeatNs = repeatNs;
        } else {
            rightHeldNs = heldNs;
            rightRepeatNs = repeatNs;
        }
    }

    private void processSoftDrop(long deltaNs) {
        if (pressedKeys.contains(KeyCode.DOWN)) {
            downHeldNs += deltaNs;
            downRepeatNs += deltaNs;

            boolean shouldDropNow = downHeldNs <= deltaNs || downRepeatNs >= SOFT_DROP_REPEAT_NS;
            if (shouldDropNow) {
                engine.handleInput(GameAction.DOWN);
                downRepeatNs = 0L;
            }
        } else {
            downHeldNs = 0L;
            downRepeatNs = 0L;
        }
    }
}
