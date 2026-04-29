package tetris.engine.state;

import tetris.engine.GameAction;
import tetris.engine.GameEngine;
import tetris.engine.GameRenderer;

public class MenuState implements GameState {
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
        if (action == GameAction.START) {
            engine.resetGame();
            engine.changeState(engine.getPlayingState());
        }
    }

    @Override
    public void update(GameEngine engine) {
        // No gameplay update in menu.
    }

    @Override
    public void render(GameEngine engine, GameRenderer renderer) {
        renderer.clear();
        renderer.drawText("TETRIS", 4, 5);
        renderer.drawText("Press START to play", 2, 7);
        renderer.present();
    }

    @Override
    public String name() {
        return "MENU";
    }
}
