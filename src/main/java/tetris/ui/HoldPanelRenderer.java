package tetris.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import tetris.engine.HoldPieceListener;
import tetris.model.TetrominoFactory;

/**
 * Side panel renderer that observes hold piece changes.
 */
public class HoldPanelRenderer implements HoldPieceListener {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final double cellSize;

    public HoldPanelRenderer(Canvas canvas, double cellSize) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.cellSize = cellSize;
        draw(null);
    }

    @Override
    public void onHoldPieceChanged(TetrominoFactory.TetrominoType holdType) {
        draw(holdType);
    }

    private void draw(TetrominoFactory.TetrominoType holdType) {
        gc.setFill(Color.web("#0F172A"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Color.WHITE);
        gc.fillText("HOLD", 10, 20);

        if (holdType == null) {
            return;
        }

        int[][] shape = shapeOf(holdType);
        Color color = colorOf(holdType);

        gc.setFill(color);
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 0) {
                    continue;
                }
                double x = 10 + col * cellSize;
                double y = 30 + row * cellSize;
                gc.fillRect(x, y, cellSize, cellSize);
                gc.setStroke(Color.color(0, 0, 0, 0.3));
                gc.strokeRect(x, y, cellSize, cellSize);
            }
        }
    }

    private int[][] shapeOf(TetrominoFactory.TetrominoType type) {
        switch (type) {
            case I:
                return new int[][]{{1, 1, 1, 1}};
            case J:
                return new int[][]{{1, 0, 0}, {1, 1, 1}};
            case L:
                return new int[][]{{0, 0, 1}, {1, 1, 1}};
            case O:
                return new int[][]{{1, 1}, {1, 1}};
            case S:
                return new int[][]{{0, 1, 1}, {1, 1, 0}};
            case T:
                return new int[][]{{0, 1, 0}, {1, 1, 1}};
            case Z:
                return new int[][]{{1, 1, 0}, {0, 1, 1}};
            default:
                return new int[0][0];
        }
    }

    private Color colorOf(TetrominoFactory.TetrominoType type) {
        switch (type) {
            case I:
                return Color.web("#22D3EE");
            case J:
                return Color.web("#3B82F6");
            case L:
                return Color.web("#F97316");
            case O:
                return Color.web("#FACC15");
            case S:
                return Color.web("#4ADE80");
            case T:
                return Color.web("#A855F7");
            case Z:
                return Color.web("#EF4444");
            default:
                return Color.LIGHTGRAY;
        }
    }
}
