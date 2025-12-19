package org.jarzarr.ui;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.jarzarr.core.EngineContext;
import org.jarzarr.scene.Scene;

/**
 * Scene implementation that owns a UI tree and renders it in screen space.
 *
 * <p>
 * UIScene is an overlay scene by default so the UI can be drawn over gameplay.
 * </p>
 *
 * <p>
 * This scene:
 * </p>
 * <ul>
 * <li>Maintains a {@link UIRoot} as the UI tree root</li>
 * <li>Maintains a {@link UIContext} that wraps input/assets and frame
 * state</li>
 * <li>Runs UI layout, input routing, update, and render</li>
 * </ul>
 */
public abstract class UIScene<A extends Enum<A>> implements Scene {

	/** Engine context for access to window/input/assets. */
	protected final EngineContext<A> ctx;

	/** Root of the UI node tree. */
	protected final UIRoot ui = new UIRoot();

	/** Per-frame UI context passed to nodes. */
	protected final UIContext uictx;

	/**
	 * Identity transform for screen-space rendering (ignores camera/world
	 * transforms).
	 */
	private final AffineTransform identity = new AffineTransform();

	/**
	 * Creates a UI scene using the engine's input and asset systems.
	 *
	 * @param ctx engine context
	 */
	protected UIScene(EngineContext<A> ctx) {
		this.ctx = ctx;
		this.uictx = new UIContext(ctx.input(), ctx.assets());
	}

	/**
	 * Build the UI tree here (called from onEnter).
	 *
	 * @param root root node to attach children to
	 */
	protected abstract void buildUI(UIRoot root);

	/**
	 * Clears and rebuilds the UI tree, then forces an initial layout pass using the
	 * current window size.
	 */
	@Override
	public void onEnter() {
		ui.children().clear();
		buildUI(ui);
		ui.markLayoutDirty();

		// Force a correct first layout immediately.
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

	/**
	 * Updates the UI root size on window resize. Layout will be recomputed on the
	 * next update tick (or immediately if update runs right after).
	 */
	@Override
	public void onResize(int width, int height) {
		uictx.screenW = width;
		uictx.screenH = height;
		ui.setSize(width, height);
	}

	/**
	 * Per-tick update:
	 * <ul>
	 * <li>refresh dt and screen size</li>
	 * <li>layout if dirty</li>
	 * <li>route input into UI</li>
	 * <li>update UI tree</li>
	 * </ul>
	 */
	@Override
	public void update(double dt) {
		uictx.dt = dt;
		uictx.screenW = ctx.window().getWidth();
		uictx.screenH = ctx.window().getHeight();

		ui.setSize(uictx.screenW, uictx.screenH);
		ui.layoutIfNeeded(uictx);

		// Let UI process raw inputs (hover, click, focus, text).
		ui.handleInput(uictx);

		// Update UI tree logic.
		ui.update(uictx);
	}

	/**
	 * Renders the UI in screen coordinates (identity transform).
	 *
	 * @param g graphics context
	 */
	@Override
	public void render(Graphics2D g) {
		AffineTransform old = g.getTransform();
		g.setTransform(identity);

		ui.render(uictx, g);

		g.setTransform(old);
	}

	/**
	 * UI scenes are overlays by default (draw on top of gameplay).
	 */
	@Override
	public boolean isOverlay() {
		return true;
	}
}
