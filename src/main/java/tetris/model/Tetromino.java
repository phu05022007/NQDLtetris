package tetris.model;

/**
 * Base abstraction for all Tetris blocks.
 */
public abstract class Tetromino {
    private int x;
    private int y;
    protected int[][] shape;
    private final int id;

    protected Tetromino(int x, int y, int[][] shape, int id) {
        this.x = x;
        this.y = y;
        this.shape = copyMatrix(shape);
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getId() {
        return id;
    }

    public int[][] getShape() {
        return copyMatrix(shape);
    }

    /**
     * Return a direct reference to the internal shape matrix for read-only
     * use by internal systems that must avoid allocations (rendering,
     * collision checks). Callers MUST NOT mutate the returned matrix.
     */
    public int[][] getShapeRef() {
        return shape;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void rotateClockwise() {
        shape = rotateClockwise(shape);
    }

    public void rotateCounterClockwise() {
        int rows = shape.length;
        int cols = shape[0].length;
        int[][] rotated = new int[cols][rows];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                rotated[cols - 1 - col][row] = shape[row][col];
            }
        }

        shape = rotated;
    }

    protected static int[][] copyMatrix(int[][] matrix) {
        int[][] copy = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = matrix[i].clone();
        }
        return copy;
    }

    /**
     * Rotates a matrix 90 degrees clockwise using:
     * transpose + reverse each row.
     */
    protected static int[][] rotateClockwise(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[][] rotated = new int[cols][rows];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                rotated[col][rows - 1 - row] = matrix[row][col];
            }
        }

        return rotated;
    }
}
