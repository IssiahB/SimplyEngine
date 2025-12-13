package org.jarzarr.scenes;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jarzarr.core.EngineContext;
import org.jarzarr.scene.Scene;
import org.jarzarr.test.Main.Action;

public class GameScene implements Scene {

	private final EngineContext<Action> ctx;

	private double x = 10;
	private double y = 10;
	private final double speed = 200;
	private Color blockColor = Color.green;

	public GameScene(EngineContext<Action> ctx) {
		this.ctx = ctx;
	}

	@Override
	public void onEnter() {
	}

	@Override
	public void onExit() {
	}

	@Override
	public void update(double dt) {
		if (ctx.actions().isPressed(Action.PAUSE)) {
			// Push pause overlay and consume so gameplay doesn't also react
			ctx.scenes().push(new PauseScene(ctx));
			ctx.actions().consumePressed(Action.PAUSE);
			return;
		}

		if (ctx.actions().isDown(Action.MOVE_RIGHT))
			x += speed * dt;
		if (ctx.actions().isDown(Action.MOVE_LEFT))
			x -= speed * dt;
		if (ctx.actions().isDown(Action.MOVE_UP))
			y -= speed * dt;
		if (ctx.actions().isDown(Action.MOVE_DOWN))
			y += speed * dt;

		if (ctx.actions().isPressed(Action.FIRE))
			if (blockColor == Color.green)
				blockColor = Color.red;
			else
				blockColor = Color.green;

		
		ctx.camera().followSmooth(x+25, y+25, dt);
	}

	@Override
	public void render(Graphics2D g) {
		var original = g.getTransform();

		ctx.camera().setViewport(ctx.window().getWidth(), ctx.window().getHeight());

		ctx.camera().apply(g);

		g.setColor(blockColor);
		g.fillRect((int) x, (int) y, 50, 50);

		g.setTransform(original);
	}

}
