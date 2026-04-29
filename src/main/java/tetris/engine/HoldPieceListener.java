package tetris.engine;

import tetris.model.TetrominoFactory;

/**
 * Observer for hold piece changes.
 * UI panels can subscribe without coupling model/engine to UI classes.
 */
public interface HoldPieceListener {
    void onHoldPieceChanged(TetrominoFactory.TetrominoType holdType);
}
