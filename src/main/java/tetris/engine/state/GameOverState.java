package tetris.engine.state;

import tetris.engine.GameAction;
import tetris.engine.GameEngine;
import tetris.engine.GameRenderer;

public class GameOverState implements GameState {
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
        if (action == GameAction.RESTART) {
            engine.resetGame();
            engine.changeState(engine.getPlayingState());
        } else if (action == GameAction.BACK_TO_MENU) {
            engine.changeState(engine.getMenuState());
        }
    }

    @Override
    public void update(GameEngine engine) {
        // No gameplay update after game over.
    }

    @Override
    public void render(GameEngine engine, GameRenderer renderer) {
        renderer.clear();
        renderer.drawBoard(engine.getBoard(), engine.getCurrentTetromino(), engine.getGhostY());
        renderer.drawText(engine.getText("gameover.title"), 3, 10);
        renderer.drawText(engine.getText("label.score") + engine.getScore(), 3, 12);
        renderer.drawText(engine.getText("gameover.instructions"), 1, 14);
        renderer.present();
    }

    @Override
    public String name() {
        return "GAME_OVER";
    }
}
