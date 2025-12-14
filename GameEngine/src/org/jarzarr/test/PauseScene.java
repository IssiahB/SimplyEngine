package org.jarzarr.test;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.jarzarr.core.EngineContext;
import org.jarzarr.scene.Scene;
import org.jarzarr.test.Main.Action;

public class PauseScene implements Scene {

	private final EngineContext<Action> ctx;

	public PauseScene(EngineContext<Action> ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean isOverlay() {
		return true;
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
			ctx.scenes().pop();
			ctx.actions().consumePressed(Action.PAUSE);
		}
	}

	@Override
	public void render(Graphics2D g) {
		// simple overlay text
		g.setColor(new Color(0, 0, 0, 160));
		g.fillRect(0, 0, 10_000, 10_000);

		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 24));
		g.drawString("PAUSED - Press ESC to resume", 30, 80);
	}
}
