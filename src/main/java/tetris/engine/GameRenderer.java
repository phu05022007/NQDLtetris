package tetris.engine;

import tetris.model.Board;
import tetris.model.Tetromino;

/**
 * Rendering abstraction to keep engine/state independent from UI library.
 */
public interface GameRenderer {
    void clear();

    void drawBoard(Board board, Tetromino activeTetromino, int ghostY, Tetromino swapFlashTetromino, boolean swapFlashVisible);

    void drawText(String text, int x, int y);

    /**
     * Draw a centered overlay panel with a title and optional lines beneath it
     * (used for menu / paused / game over screens).
     */
    void drawOverlay(String title, String[] lines);

    /** Hide any overlay panels. */
    void hideOverlay();

    void present();
}
