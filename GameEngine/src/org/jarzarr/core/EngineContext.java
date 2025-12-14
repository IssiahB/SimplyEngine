package org.jarzarr.core;

import org.jarzarr.InputManager;
import org.jarzarr.Window;
import org.jarzarr.assets.AssetManager;
import org.jarzarr.input.ActionMap;
import org.jarzarr.render.Camera2D;
import org.jarzarr.scene.SceneManager;

public final class EngineContext<A extends Enum<A>> {

	private final Window window;
	private final InputManager input;
	private final ActionMap<A> actions;
	private final SceneManager scenes;
	private final Camera2D camera;
	private final AssetManager assets;

	private double lastDt;

	public EngineContext(Window window, InputManager input, ActionMap<A> actions, SceneManager scenes, Camera2D camera,
			AssetManager assets) {
		this.window = window;
		this.input = input;
		this.actions = actions;
		this.scenes = scenes;
		this.camera = camera;
		this.assets = assets;
	}

	public Window window() {
		return window;
	}

	public InputManager input() {
		return input;
	}

	public ActionMap<A> actions() {
		return actions;
	}

	public SceneManager scenes() {
		return scenes;
	}

	public Camera2D camera() {
		return camera;
	}

	public AssetManager assets() {
		return assets;
	}

	public double lastDt() {
		return lastDt;
	}

	public void setLastDt(double dt) {
		this.lastDt = dt;
	}
}
