package org.jarzarr.ui;

import java.awt.Font;

import org.jarzarr.InputManager;
import org.jarzarr.assets.AssetManager;

/**
 * Per-frame UI context passed to UI nodes.
 *
 * <p>
 * Contains shared engine services (input/assets) plus frame state like screen
 * size, mouse position, and dt.
 * </p>
 */
public final class UIContext {

	/** Engine input manager (keyboard/mouse state). */
	public final InputManager input;

	/** Engine asset manager (images/fonts/sounds). */
	public final AssetManager assets;

	/** Current screen width in pixels (usually window canvas width). */
	public int screenW;

	/** Current screen height in pixels (usually window canvas height). */
	public int screenH;

	/** Mouse X in screen space (cached each tick by UIRoot). */
	public float mouseX;

	/** Mouse Y in screen space (cached each tick by UIRoot). */
	public float mouseY;

	/** Delta time for this tick (seconds). */
	public double dt;

	/** Optional default font override for widgets that want it. */
	public Font defaultFont;

	/** Active theme (colors/fonts). */
	public UITheme theme = UITheme.defaults();

	/**
	 * Creates a UIContext.
	 *
	 * @param input  engine input manager
	 * @param assets engine asset manager
	 */
	public UIContext(InputManager input, AssetManager assets) {
		this.input = input;
		this.assets = assets;
	}
}
