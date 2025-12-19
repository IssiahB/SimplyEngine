package org.jarzarr.core;

import java.awt.Color;

/**
 * Mutable configuration object populated by the user's
 * {@link GameApp#configure(EngineConfig)}.
 *
 * <p>
 * This is intentionally "plain public fields" so configuring the engine is
 * quick and direct. The engine reads these values during init() and applies
 * them when constructing subsystems.
 * </p>
 */
public class EngineConfig {

	// ----------------------------
	// Window
	// ----------------------------

	/** Window title displayed in the title bar. */
	public String title = "Jarzarr";

	/** Initial preferred width (pixels). */
	public int width = 800;

	/** Initial preferred height (pixels). */
	public int height = 600;

	/** If true, allow the user to resize the window. */
	public boolean resizable = false;

	/** If true, start maximized (windowed fullscreen, not exclusive fullscreen). */
	public boolean fullscreen = false;

	// ----------------------------
	// Rendering
	// ----------------------------

	/** Number of buffers to use for the BufferStrategy (commonly 2 or 3). */
	public int bufferCount = 3;

	/** Clear color used to wipe the frame each render pass. */
	public Color clearColor = Color.BLACK;

	// ----------------------------
	// Loop
	// ----------------------------

	/** Fixed update rate (updates per second). */
	public double updatesPerSecond = 60.0;

	/**
	 * Catch-up update cap per frame to reduce spiral-of-death when frames are slow.
	 * Higher values reduce simulation slowdown but can increase CPU spikes.
	 */
	public int maxCatchUpUpdates = 8;

	/** Milliseconds to wait when stopping the loop thread (join timeout). */
	public int engineStopDelay = 100;

	// ----------------------------
	// Close behavior
	// ----------------------------

	/** If true, dispose the window on engine stop. */
	public boolean disposeWindowOnStop = true;

	// ----------------------------
	// Utilities
	// ----------------------------

	/** If true, enable loop debug output (UPS/FPS counters). */
	public boolean debug = false;
}
