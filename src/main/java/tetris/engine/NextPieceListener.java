package tetris.engine;

import tetris.model.TetrominoFactory;

/**
 * Observer for next piece changes.
 */
public interface NextPieceListener {
    void onNextPieceChanged(TetrominoFactory.TetrominoType nextType);
}
