package org.jarzarr.core;

import org.jarzarr.InputManager;
import org.jarzarr.Window;
import org.jarzarr.assets.AssetManager;
import org.jarzarr.input.ActionMap;
import org.jarzarr.render.Camera2D;
import org.jarzarr.scene.SceneManager;

/**
 * Immutable container for engine-owned systems.
 *
 * <p>
 * This is passed to the user {@link GameApp} and to scenes so gameplay code can
 * access core services (window, input, actions, scenes, camera, assets) without
 * global singletons.
 * </p>
 *
 * <p>
 * The only mutable value stored here is {@code lastDt}, which is updated once
 * per engine update tick.
 * </p>
 *
 * @param <A> enum type representing high-level input actions
 */
public final class EngineContext<A extends Enum<A>> {

	/** Window wrapper owning JFrame/Canvas/BufferStrategy. */
	private final Window window;

	/** Raw input device state (keyboard/mouse). */
	private final InputManager input;

	/** High-level action mapping (enum actions driven by input). */
	private final ActionMap<A> actions;

	/**
	 * Scene manager controlling the current scene and routing update/render calls.
	 */
	private final SceneManager scenes;

	/** 2D camera used for world-to-screen transforms. */
	private final Camera2D camera;

	/** Asset manager used to load/cache/dispose engine resources. */
	private final AssetManager assets;

	/** Last fixed delta-time used by the engine update tick (seconds). */
	private double lastDt;

	/**
	 * Creates a new engine context. All references are stored as-is and are
	 * expected to remain valid for the lifetime of the engine.
	 *
	 * @param window  window system
	 * @param input   input manager
	 * @param actions action map
	 * @param scenes  scene manager
	 * @param camera  camera
	 * @param assets  asset manager
	 */
	public EngineContext(Window window, InputManager input, ActionMap<A> actions, SceneManager scenes, Camera2D camera,
			AssetManager assets) {
		this.window = window;
		this.input = input;
		this.actions = actions;
		this.scenes = scenes;
		this.camera = camera;
		this.assets = assets;
	}

	/**
	 * @return engine window wrapper (JFrame/Canvas/BufferStrategy)
	 */
	public Window window() {
		return window;
	}

	/**
	 * @return raw input state (keyboard/mouse)
	 */
	public InputManager input() {
		return input;
	}

	/**
	 * @return high-level action mapper
	 */
	public ActionMap<A> actions() {
		return actions;
	}

	/**
	 * @return scene manager controlling active scene(s)
	 */
	public SceneManager scenes() {
		return scenes;
	}

	/**
	 * @return 2D camera instance
	 */
	public Camera2D camera() {
		return camera;
	}

	/**
	 * @return asset manager for loading/caching/disposal
	 */
	public AssetManager assets() {
		return assets;
	}

	/**
	 * Returns the dt used for the most recent engine update tick.
	 *
	 * @return last fixed delta time (seconds)
	 */
	public double lastDt() {
		return lastDt;
	}

	/**
	 * Sets the dt used for the current/most recent engine update tick. Called by
	 * the engine once per update() step.
	 *
	 * @param dt delta time in seconds
	 */
	public void setLastDt(double dt) {
		this.lastDt = dt;
	}
}
