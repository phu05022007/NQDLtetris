package tetris.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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
    private static final long FLASH_PERIOD_NS = 150_000_000L;

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final double cellSize;

    private final Color backgroundColor;
    private final Color gridLineColor;
    private final Color[] paletteById;

    public BoardRenderer(Canvas canvas, double cellSize) {
        this(canvas, cellSize, Color.BLACK, Color.web("#2b0b44"));
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

        // Draw flashing overlay for pending cleared rows (if any).
        if (board.hasPendingClear()) {
            int[] pending = board.getPendingClearRows();
            if (pending != null && pending.length > 0) {
                long now = System.nanoTime();
                boolean visible = ((now / FLASH_PERIOD_NS) % 2) == 0;
                if (visible) {
                    Color overlay = board.getPendingClearColorIndex() == 0
                            ? Color.web("#FFD700", 0.55)
                            : Color.web("#FF4B4B", 0.55);
                    gc.setFill(overlay);
                    double w = cellSize * Board.COLUMNS;
                    for (int r : pending) {
                        if (r < 0 || r >= Board.ROWS) continue;
                        double py = r * cellSize;
                        gc.fillRect(0, py, w, cellSize);
                    }
                }
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
        // Draw text with a dark rounded background and a subtle shadow
        Font font = Font.font("Fredoka One", Math.max(12, cellSize * 0.6));
        gc.setFont(font);

        double textX = x * cellSize + Math.max(4, cellSize * 0.08);
        double textBaseline = y * cellSize + Math.max(14, cellSize * 0.6);

        Text tmp = new Text(text);
        tmp.setFont(font);
        double textWidth = tmp.getLayoutBounds().getWidth();
        double textHeight = tmp.getLayoutBounds().getHeight();

        double paddingH = Math.max(6, cellSize * 0.12);
        double paddingV = Math.max(4, cellSize * 0.06);

        double bgX = textX - paddingH / 2.0;
        double bgY = textBaseline - textHeight - paddingV / 2.0;
        double bgW = textWidth + paddingH;
        double bgH = textHeight + paddingV;
        double arc = Math.max(4.0, cellSize * 0.12);

        Color indigo = Color.web("#4B0082");

        gc.setFill(Color.color(0, 0, 0, 0.6));
        gc.fillRoundRect(bgX, bgY, bgW, bgH, arc, arc);

        // indigo stroke around the background box
        gc.setStroke(Color.web("#4B0082", 0.45));
        gc.setLineWidth(Math.max(1.0, cellSize * 0.04));
        gc.strokeRoundRect(bgX, bgY, bgW, bgH, arc, arc);

        // draw shadowed text using indigo as the shadow color
        gc.setFill(Color.web("#4B0082", 0.85));
        gc.fillText(text, textX + 1, textBaseline + 1);

        gc.setFill(Color.WHITE);
        gc.fillText(text, textX, textBaseline);
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
            double arc = Math.max(2.0, cellSize * 0.15);
            gc.strokeRoundRect(px + STROKE_WIDTH/2, py + STROKE_WIDTH/2, cellSize - STROKE_WIDTH, cellSize - STROKE_WIDTH, arc, arc);
            return;
        }

        Color base = colorOf(value);
        double arc = Math.max(2.0, cellSize * 0.15);
        gc.setFill(base);
        gc.fillRoundRect(px, py, cellSize, cellSize, arc, arc);
        gc.setStroke(Color.color(0, 0, 0, 0.30));
        gc.setLineWidth(STROKE_WIDTH);
        gc.strokeRoundRect(px + STROKE_WIDTH/2, py + STROKE_WIDTH/2, cellSize - STROKE_WIDTH, cellSize - STROKE_WIDTH, arc, arc);

        // subtle highlight
        gc.setFill(Color.color(1, 1, 1, 0.12));
        gc.fillRoundRect(px + cellSize * 0.08, py + cellSize * 0.08, cellSize * 0.84, cellSize * 0.4, arc * 0.6, arc * 0.6);
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
