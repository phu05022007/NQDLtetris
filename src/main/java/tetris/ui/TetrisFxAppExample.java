package tetris.ui;

import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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

    private Label scoreLabel;
    private Label linesLabel;
    private StackPane canvasStack;
    private VBox helpOverlay;
    private VBox languageOverlay;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(Board.COLUMNS * CELL_SIZE, (Board.ROWS + 3) * CELL_SIZE);
        Canvas holdCanvas = new Canvas(6 * CELL_SIZE, 6 * CELL_SIZE);

        BoardRenderer renderer = new BoardRenderer(canvas, CELL_SIZE);
        holdPanelRenderer = new HoldPanelRenderer(holdCanvas, CELL_SIZE);
        engine = new GameEngine();
        engine.addHoldPieceListener(holdPanelRenderer);
        // HUD labels
        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(new Font(18));
        scoreLabel.getStyleClass().add("hud-label");
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setEffect(new DropShadow(6, Color.web("#4B0082", 0.85)));

        linesLabel = new Label("Lines: 0");
        linesLabel.setFont(new Font(14));
        linesLabel.getStyleClass().add("hud-label");
        linesLabel.setTextFill(Color.WHITE);
        linesLabel.setEffect(new DropShadow(6, Color.web("#4B0082", 0.85)));

        // Canvas stack for overlay and styling
        canvasStack = new StackPane(canvas);
        canvasStack.getStyleClass().add("canvas-stack");
        DropShadow ds = new DropShadow();
        ds.setRadius(8);
        ds.setOffsetY(2);
        ds.setColor(Color.color(0,0,0,0.7));
        canvas.setEffect(ds);
        holdCanvas.setEffect(ds);

        // Language selection overlay (shown at startup)
        languageOverlay = new VBox(12);
        languageOverlay.setAlignment(Pos.CENTER);
        languageOverlay.setPadding(new Insets(16));
        languageOverlay.getStyleClass().add("language-overlay");
        Label langPrompt = new Label("Choose language / Chọn ngôn ngữ");
        langPrompt.setFont(new Font(18));
        langPrompt.setTextFill(Color.WHITE);
        langPrompt.getStyleClass().add("language-prompt");
        langPrompt.setEffect(new DropShadow(6, Color.web("#4B0082", 0.85)));
        Button enButton = new Button("English");
        Button viButton = new Button("Tiếng Việt");
        HBox langButtons = new HBox(8, enButton, viButton);
        langButtons.setAlignment(Pos.CENTER);
        languageOverlay.getChildren().addAll(langPrompt, langButtons);
        languageOverlay.setMaxWidth(canvas.getWidth() * 0.8);
        languageOverlay.setMaxHeight(canvas.getHeight() * 0.6);
        canvasStack.getChildren().add(languageOverlay);

        // Help overlay will be built after language selection
        helpOverlay = null;

        VBox rightBox = new VBox(12, holdCanvas, scoreLabel, linesLabel);
        rightBox.setAlignment(Pos.TOP_CENTER);
        rightBox.setPadding(new Insets(8));

        HBox root = new HBox(12, canvasStack, rightBox);
        root.getStyleClass().add("root-background");
        Scene scene = new Scene(root);
        // Load centralized stylesheet for theme and component styles.
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            boolean firstPress = pressedKeys.add(code);

            // Ignore input until language is selected
            if (languageOverlay != null && languageOverlay.isVisible()) {
                return;
            }

            // One-shot actions should happen once when key goes down.
            if (!firstPress) {
                return;
            }

            switch (code) {
                case ENTER:
                    engine.handleInput(GameAction.START);
                    break;
                    case UP:
                        engine.handleInput(GameAction.ROTATE);
                        break;
                    case SPACE:
                        engine.handleInput(GameAction.HARD_DROP);
                        break;
                case P:
                    engine.handleInput(GameAction.PAUSE);
                    break;
                case C:
                    engine.handleInput(GameAction.HOLD);
                    break;
                case H:
                    if (helpOverlay != null) {
                        helpOverlay.setVisible(!helpOverlay.isVisible());
                    }
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

        // Language selection button handlers
        enButton.setOnAction(e -> {
            engine.setLanguage(tetris.engine.GameEngine.Language.EN);
            buildHelpOverlay();
            canvasStack.getChildren().remove(languageOverlay);
            languageOverlay.setVisible(false);
            canvasStack.getChildren().add(helpOverlay);
            scoreLabel.setText(engine.getText("label.score") + engine.getScore());
            linesLabel.setText(engine.getText("label.lines") + engine.getTotalClearedLines());
            engine.start(renderer);
            scene.getRoot().requestFocus();
            startInputLoop();
        });

        viButton.setOnAction(e -> {
            engine.setLanguage(tetris.engine.GameEngine.Language.VI);
            buildHelpOverlay();
            canvasStack.getChildren().remove(languageOverlay);
            languageOverlay.setVisible(false);
            canvasStack.getChildren().add(helpOverlay);
            scoreLabel.setText(engine.getText("label.score") + engine.getScore());
            linesLabel.setText(engine.getText("label.lines") + engine.getTotalClearedLines());
            engine.start(renderer);
            scene.getRoot().requestFocus();
            startInputLoop();
        });

        stage.setTitle("Tetris - JavaFX Canvas");
        stage.setScene(scene);
        stage.show();

        // Do not start engine or input loop until language is selected.
        // engine.start(renderer) and startInputLoop() are invoked when user
        // clicks the language button handlers.
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

                // Update HUD labels (localized after language selection)
                if (scoreLabel != null && engine != null) {
                    scoreLabel.setText(engine.getText("label.score") + engine.getScore());
                }
                if (linesLabel != null && engine != null) {
                    linesLabel.setText(engine.getText("label.lines") + engine.getTotalClearedLines());
                }
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

    private void buildHelpOverlay() {
        helpOverlay = new VBox(8);
        helpOverlay.setAlignment(Pos.TOP_LEFT);
        helpOverlay.setPadding(new Insets(12));
        helpOverlay.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 0% 100%, rgba(0,0,0,0.85), rgba(43,11,68,0.65)); -fx-background-radius: 8;");
        Label helpTitle = new Label(engine.getText("help.title"));
        helpTitle.setFont(new Font(16));
        helpTitle.setTextFill(Color.WHITE);
        helpTitle.setStyle("-fx-background-color: rgba(0,0,0,0.45); -fx-padding: 6 8; -fx-background-radius: 6; -fx-border-color: #4B0082; -fx-border-width: 1; -fx-border-radius: 6;");
        helpTitle.setEffect(new DropShadow(6, Color.web("#4B0082", 0.85)));
        helpOverlay.getChildren().add(helpTitle);

        String[] helpLines = new String[] {
            engine.getText("help.move"),
            engine.getText("help.rotate"),
            engine.getText("help.softdrop"),
            engine.getText("help.hold"),
            engine.getText("help.harddrop"),
            engine.getText("help.controls"),
            engine.getText("help.menu"),
            engine.getText("help.goal")
        };

        for (String line : helpLines) {
            Label l = new Label(line);
            l.setTextFill(Color.WHITE);
            l.setStyle("-fx-background-color: rgba(0,0,0,0.35); -fx-padding: 4 6; -fx-background-radius: 6; -fx-border-color: #4B0082; -fx-border-width: 1; -fx-border-radius: 6;");
            l.setEffect(new DropShadow(4, Color.web("#4B0082", 0.8)));
            helpOverlay.getChildren().add(l);
        }
        helpOverlay.setVisible(false);
    }
}
