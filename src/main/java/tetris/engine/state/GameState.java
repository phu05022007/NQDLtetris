package tetris.engine.state;

import tetris.engine.GameAction;
import tetris.engine.GameEngine;
import tetris.engine.GameRenderer;

public interface GameState {
    void enter(GameEngine engine);

    void exit(GameEngine engine);

    void handleInput(GameEngine engine, GameAction action);

    void update(GameEngine engine);

    void render(GameEngine engine, GameRenderer renderer);

    String name();
}
