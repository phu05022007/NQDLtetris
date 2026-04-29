package tetris.model;

import java.util.Random;

/**
 * Factory Method implementation for creating random Tetromino instances.
 */
public class TetrominoFactory {
    private static final int SPAWN_X = 3;
    private static final int SPAWN_Y = 0;

    private final Random random;

    public TetrominoFactory() {
        this(new Random());
    }

    public TetrominoFactory(Random random) {
        this.random = random;
    }

    public Tetromino createRandom() {
        TetrominoType[] types = TetrominoType.values();
        TetrominoType randomType = types[random.nextInt(types.length)];
        return create(randomType);
    }

    public Tetromino create(TetrominoType type) {
        switch (type) {
            case I:
                return new ITetromino(SPAWN_X, SPAWN_Y);
            case J:
                return new JTetromino(SPAWN_X, SPAWN_Y);
            case L:
                return new LTetromino(SPAWN_X, SPAWN_Y);
            case O:
                return new OTetromino(SPAWN_X, SPAWN_Y);
            case S:
                return new STetromino(SPAWN_X, SPAWN_Y);
            case T:
                return new TTetromino(SPAWN_X, SPAWN_Y);
            case Z:
                return new ZTetromino(SPAWN_X, SPAWN_Y);
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    public TetrominoType typeFromId(int id) {
        switch (id) {
            case 1:
                return TetrominoType.I;
            case 2:
                return TetrominoType.J;
            case 3:
                return TetrominoType.L;
            case 4:
                return TetrominoType.O;
            case 5:
                return TetrominoType.S;
            case 6:
                return TetrominoType.T;
            case 7:
                return TetrominoType.Z;
            default:
                throw new IllegalArgumentException("Unknown tetromino id: " + id);
        }
    }

    public enum TetrominoType {
        I, J, L, O, S, T, Z
    }

    private static final class ITetromino extends Tetromino {
        private ITetromino(int x, int y) {
            super(x, y, new int[][]{
                    {1, 1, 1, 1}
            }, 1);
        }
    }

    private static final class JTetromino extends Tetromino {
        private JTetromino(int x, int y) {
            super(x, y, new int[][]{
                    {2, 0, 0},
                    {2, 2, 2}
            }, 2);
        }
    }

    private static final class LTetromino extends Tetromino {
        private LTetromino(int x, int y) {
            super(x, y, new int[][]{
                    {0, 0, 3},
                    {3, 3, 3}
            }, 3);
        }
    }

    private static final class OTetromino extends Tetromino {
        private OTetromino(int x, int y) {
            super(x, y, new int[][]{
                    {4, 4},
                    {4, 4}
            }, 4);
        }
    }

    private static final class STetromino extends Tetromino {
        private STetromino(int x, int y) {
            super(x, y, new int[][]{
                    {0, 5, 5},
                    {5, 5, 0}
            }, 5);
        }
    }

    private static final class TTetromino extends Tetromino {
        private TTetromino(int x, int y) {
            super(x, y, new int[][]{
                    {0, 6, 0},
                    {6, 6, 6}
            }, 6);
        }
    }

    private static final class ZTetromino extends Tetromino {
        private ZTetromino(int x, int y) {
            super(x, y, new int[][]{
                    {7, 7, 0},
                    {0, 7, 7}
            }, 7);
        }
    }
}
