package org.jarzarr.ui;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.jarzarr.core.EngineContext;
import org.jarzarr.scene.Scene;

/**
 * Scene implementation that owns a UI tree and renders in screen space.
 */
public abstract class UIScene<A extends Enum<A>> implements Scene {
	protected final EngineContext<A> ctx;
	protected final UIRoot ui = new UIRoot();
	protected final UIContext uictx;

	private final AffineTransform identity = new AffineTransform();

	protected UIScene(EngineContext<A> ctx) {
		this.ctx = ctx;
		this.uictx = new UIContext(ctx.input(), ctx.assets());
	}

	/**
	 * Build your UI tree here (called from onEnter).
	 */
	protected abstract void buildUI(UIRoot root);

	@Override
	public void onEnter() {
		ui.children().clear();
		buildUI(ui);
		ui.markLayoutDirty();
		
		// Force a correct first layout immediately
	    int w = ctx.window().getWidth();
	    int h = ctx.window().getHeight();
	    uictx.screenW = w;
	    uictx.screenH = h;
	    ui.setSize(w, h);
	    ui.layoutIfNeeded(uictx);
	}

	@Override
	public void onExit() {
	}

	@Override
	public void onResize(int width, int height) {
		ui.setSize(width, height);
	}

	@Override
	public void update(double dt) {
		uictx.dt = dt;
		uictx.screenW = ctx.window().getWidth();
		uictx.screenH = ctx.window().getHeight();

		ui.setSize(uictx.screenW, uictx.screenH);
		ui.layoutIfNeeded(uictx);

		// Let UI process raw inputs (hover, click, etc.)
		ui.handleInput(uictx);

		// Update UI tree
		ui.update(uictx);
	}

	@Override
	public void render(Graphics2D g) {
		// Always render UI in screen coordinates (no camera transform)
		AffineTransform old = g.getTransform();
		g.setTransform(identity);

		ui.render(uictx, g);

		g.setTransform(old);
	}

	@Override
	public boolean isOverlay() {
		return true;
	}
}
