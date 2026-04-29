package tetris.model;

import java.util.Arrays;

/**
 * Board model storing locked blocks in a 20x10 grid.
 */
public class Board {
    public static final int ROWS = 20;
    public static final int COLUMNS = 10;

    private final int[][] grid;

    public Board() {
        this.grid = new int[ROWS][COLUMNS];
    }

    public int[][] getGridSnapshot() {
        int[][] copy = new int[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++) {
            copy[row] = grid[row].clone();
        }
        return copy;
    }

    /**
     * Returns direct board storage for high-performance read-only rendering.
     * Do not mutate this matrix outside Board.
     */
    public int[][] getGridView() {
        return grid;
    }

    public void clear() {
        for (int[] row : grid) {
            Arrays.fill(row, 0);
        }
    }

    public boolean isInside(int x, int y) {
        return y >= 0 && y < ROWS && x >= 0 && x < COLUMNS;
    }

    public boolean isCellEmpty(int x, int y) {
        if (!isInside(x, y)) {
            return false;
        }
        return grid[y][x] == 0;
    }

    public boolean canPlace(Tetromino tetromino) {
        return !checkCollision(tetromino.getX(), tetromino.getY(), tetromino.getShape());
    }

    /**
     * Checks if placing a shape at target position causes any collision:
     * - left or right wall
     * - bottom boundary
     * - occupied cells already locked on board
     */
    public boolean checkCollision(int targetX, int targetY, int[][] targetShape) {
        for (int row = 0; row < targetShape.length; row++) {
            for (int col = 0; col < targetShape[row].length; col++) {
                if (targetShape[row][col] == 0) {
                    continue;
                }

                int boardX = targetX + col;
                int boardY = targetY + row;

                // Hit left / right wall, or bottom of board.
                if (boardX < 0 || boardX >= COLUMNS || boardY >= ROWS) {
                    return true;
                }

                // Cells above visible top are allowed while spawning.
                if (boardY < 0) {
                    continue;
                }

                // Hit locked block.
                if (grid[boardY][boardX] != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public void lock(Tetromino tetromino) {
        int[][] shape = tetromino.getShape();
        int baseX = tetromino.getX();
        int baseY = tetromino.getY();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                int value = shape[row][col];
                if (value == 0) {
                    continue;
                }

                int boardX = baseX + col;
                int boardY = baseY + row;
                if (!isInside(boardX, boardY)) {
                    throw new IllegalStateException("Tetromino is out of board bounds.");
                }

                grid[boardY][boardX] = value;
            }
        }
    }

    public int clearFullLines() {
        int linesCleared = 0;

        for (int row = ROWS - 1; row >= 0; row--) {
            if (!isLineFull(row)) {
                continue;
            }

            removeLine(row);
            linesCleared++;
            row++;
        }

        return linesCleared;
    }

    private boolean isLineFull(int row) {
        for (int value : grid[row]) {
            if (value == 0) {
                return false;
            }
        }
        return true;
    }

    private void removeLine(int row) {
        for (int current = row; current > 0; current--) {
            grid[current] = grid[current - 1].clone();
        }
        Arrays.fill(grid[0], 0);
    }
}
