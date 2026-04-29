package tetris.engine;

import tetris.model.Board;
import tetris.model.Tetromino;

/**
 * Rendering abstraction to keep engine/state independent from UI library.
 */
public interface GameRenderer {
    void clear();

    void drawBoard(Board board, Tetromino activeTetromino, int ghostY);

    void drawText(String text, int x, int y);

    void present();
}
