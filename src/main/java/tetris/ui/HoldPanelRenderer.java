package tetris.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import tetris.engine.HoldPieceListener;
import tetris.model.TetrominoFactory;

/**
 * Side panel renderer that observes hold piece changes.
 */
public class HoldPanelRenderer implements HoldPieceListener, tetris.engine.NextPieceListener {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final double cellSize;
    private TetrominoFactory.TetrominoType holdType;
    private TetrominoFactory.TetrominoType nextType;

    public HoldPanelRenderer(Canvas canvas, double cellSize) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.cellSize = cellSize;
        this.holdType = null;
        this.nextType = null;
        draw();
    }

    @Override
    public void onHoldPieceChanged(TetrominoFactory.TetrominoType holdType) {
        this.holdType = holdType;
        draw();
    }

    @Override
    public void onNextPieceChanged(TetrominoFactory.TetrominoType nextType) {
        this.nextType = nextType;
        draw();
    }

    private void draw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.setFill(Color.web("#0F172A"));
        gc.fillRect(0, 0, w, h);

        double padding = 8;
        double sectionHeight = (h - padding * 3) / 2.0;
        double sectionWidth = w - padding * 2;

        // NEXT box
        double nextX = padding;
        double nextY = padding;
        drawBox(nextX, nextY, sectionWidth, sectionHeight, "NEXT", nextType);

        // HOLD box
        double holdX = padding;
        double holdY = padding + sectionHeight + padding;
        drawBox(holdX, holdY, sectionWidth, sectionHeight, "HOLD", holdType);
    }

    private void drawBox(double x, double y, double boxW, double boxH, String label, TetrominoFactory.TetrominoType type) {
        // background and border
        gc.setFill(Color.web("#081028"));
        gc.fillRoundRect(x, y, boxW, boxH, 6, 6);
        gc.setStroke(Color.web("#4B0082"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, boxW, boxH, 6, 6);

        // label
        gc.setFill(Color.WHITE);
        gc.fillText(label, x + 6, y + 16);

        if (type == null) {
            return;
        }

        int[][] shape = shapeOf(type);
        Color color = colorOf(type);

        int rows = shape.length;
        int cols = (rows > 0) ? shape[0].length : 0;

        double shapeW = cols * cellSize;
        double shapeH = rows * cellSize;

        double startX = x + (boxW - shapeW) / 2.0;
        double startY = y + (boxH - shapeH) / 2.0 + 6;

        gc.setFill(color);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (shape[row][col] == 0) continue;
                double bx = startX + col * cellSize;
                double by = startY + row * cellSize;
                gc.fillRect(bx, by, cellSize, cellSize);
                gc.setStroke(Color.color(0, 0, 0, 0.3));
                gc.strokeRect(bx, by, cellSize, cellSize);
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
