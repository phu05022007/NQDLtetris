package tetris.engine.state;

import tetris.engine.GameAction;
import tetris.engine.GameEngine;
import tetris.engine.GameRenderer;

public class PausedState implements GameState {
    @Override
    public void enter(GameEngine engine) {
        // No-op.
    }

    @Override
    public void exit(GameEngine engine) {
        // No-op.
    }

    @Override
    public void handleInput(GameEngine engine, GameAction action) {
        if (action == GameAction.RESUME) {
            engine.changeState(engine.getPlayingState());
        } else if (action == GameAction.BACK_TO_MENU) {
            engine.changeState(engine.getMenuState());
        }
    }

    @Override
    public void update(GameEngine engine) {
        // Freeze gameplay while paused.
    }

    @Override
    public void render(GameEngine engine, GameRenderer renderer) {
        renderer.clear();
        renderer.drawBoard(engine.getBoard(), engine.getCurrentTetromino(), engine.getGhostY());
        renderer.drawText(engine.getText("paused.title"), 4, 10);
        renderer.drawText(engine.getText("paused.instructions"), 1, 12);
        renderer.present();
    }

    @Override
    public String name() {
        return "PAUSED";
    }
}
