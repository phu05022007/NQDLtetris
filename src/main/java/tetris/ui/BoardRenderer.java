package tetris.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import tetris.engine.GameRenderer;
import tetris.model.Board;
import tetris.model.Tetromino;

/**
 * JavaFX Canvas renderer for Tetris board and active tetromino.
 * Uses immediate mode drawing to minimize per-frame object allocations.
 */
public class BoardRenderer implements GameRenderer {
    private static final double STROKE_WIDTH = 1.0;
    private static final double GHOST_OPACITY = 0.25;

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final double cellSize;

    private final Color backgroundColor;
    private final Color gridLineColor;
    private final Color[] paletteById;

    public BoardRenderer(Canvas canvas, double cellSize) {
        this(canvas, cellSize, Color.web("#111827"), Color.web("#1F2937"));
    }

    public BoardRenderer(Canvas canvas, double cellSize, Color backgroundColor, Color gridLineColor) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.cellSize = cellSize;
        this.backgroundColor = backgroundColor;
        this.gridLineColor = gridLineColor;
        this.paletteById = createPalette();
    }

    @Override
    public void clear() {
        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @Override
    public void drawBoard(Board board, Tetromino activeTetromino, int ghostY) {
        int[][] grid = board.getGridView();

        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLUMNS; col++) {
                drawCell(col, row, grid[row][col]);
            }
        }

        if (activeTetromino != null) {
            if (ghostY > activeTetromino.getY()) {
                drawTetrominoAt(activeTetromino, activeTetromino.getX(), ghostY, GHOST_OPACITY);
            }
            drawTetrominoAt(activeTetromino, activeTetromino.getX(), activeTetromino.getY(), 1.0);
        }
    }

    @Override
    public void drawText(String text, int x, int y) {
        gc.setFill(Color.WHITE);
        gc.fillText(text, x * cellSize, y * cellSize);
    }

    @Override
    public void present() {
        // Canvas is drawn immediately; no back buffer swap needed.
    }

    private void drawTetrominoAt(Tetromino tetromino, int baseX, int baseY, double opacity) {
        gc.save();
        gc.setGlobalAlpha(opacity);

        int[][] shape = tetromino.getShape();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                int value = shape[row][col];
                if (value == 0) {
                    continue;
                }

                int boardX = baseX + col;
                int boardY = baseY + row;
                if (boardY < 0 || boardX < 0 || boardX >= Board.COLUMNS || boardY >= Board.ROWS) {
                    continue;
                }
                drawCell(boardX, boardY, value);
            }
        }

        gc.restore();
    }

    private void drawCell(int boardX, int boardY, int value) {
        double px = boardX * cellSize;
        double py = boardY * cellSize;

        if (value == 0) {
            gc.setFill(backgroundColor);
            gc.fillRect(px, py, cellSize, cellSize);
            gc.setStroke(gridLineColor);
            gc.setLineWidth(STROKE_WIDTH);
            gc.strokeRect(px, py, cellSize, cellSize);
            return;
        }

        gc.setFill(colorOf(value));
        gc.fillRect(px, py, cellSize, cellSize);
        gc.setStroke(Color.color(0, 0, 0, 0.30));
        gc.setLineWidth(STROKE_WIDTH);
        gc.strokeRect(px, py, cellSize, cellSize);
    }

    private Color colorOf(int id) {
        if (id <= 0 || id >= paletteById.length) {
            return Color.LIGHTGRAY;
        }
        return paletteById[id];
    }

    private Color[] createPalette() {
        return new Color[]{
                Color.TRANSPARENT,     // 0 empty
                Color.web("#22D3EE"),  // 1 I
                Color.web("#3B82F6"),  // 2 J
                Color.web("#F97316"),  // 3 L
                Color.web("#FACC15"),  // 4 O
                Color.web("#4ADE80"),  // 5 S
                Color.web("#A855F7"),  // 6 T
                Color.web("#EF4444")   // 7 Z
        };
    }
}
