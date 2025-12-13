package org.jarzarr.test;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.jarzarr.core.EngineConfig;
import org.jarzarr.core.EngineContext;
import org.jarzarr.core.GameApp;
import org.jarzarr.input.KeyTrigger;
import org.jarzarr.input.MouseButtonTrigger;
import org.jarzarr.scene.Scene;
import org.jarzarr.scenes.GameScene;
import org.jarzarr.test.Main.Action;

public class Main implements GameApp<Action> {
	
	public enum Action {
	    MOVE_LEFT, MOVE_RIGHT, MOVE_UP, MOVE_DOWN,
	    PAUSE, FIRE
	}
	
	public static void main(String[] args) {
		new org.jarzarr.Engine<>(new Main(), Action.class).start();
	}

	@Override
	public void configure(EngineConfig config) {
		config.title = "My Game";
        config.width = 1000;
        config.height = 700;
        config.resizable = true;
        config.updatesPerSecond = 60.0;
        config.maxCatchUpUpdates = 8;
        config.debug = true;
	}

	@Override
	public void bindInputs(EngineContext<Action> ctx) {
		ctx.actions().bind(Action.MOVE_LEFT,  new KeyTrigger(KeyEvent.VK_A));
        ctx.actions().bind(Action.MOVE_RIGHT, new KeyTrigger(KeyEvent.VK_D));
        ctx.actions().bind(Action.MOVE_UP,    new KeyTrigger(KeyEvent.VK_W));
        ctx.actions().bind(Action.MOVE_DOWN,  new KeyTrigger(KeyEvent.VK_S));
        ctx.actions().bind(Action.PAUSE,      new KeyTrigger(KeyEvent.VK_ESCAPE));
        ctx.actions().bind(Action.FIRE, new MouseButtonTrigger(MouseEvent.BUTTON1));
	}

	@Override
	public Scene createInitialScene(EngineContext<Action> ctx) {
		return new GameScene(ctx);
	}

}
