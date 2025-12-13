package org.jarzarr.core;

import org.jarzarr.InputManager;
import org.jarzarr.Window;
import org.jarzarr.input.ActionMap;
import org.jarzarr.render.Camera2D;
import org.jarzarr.scene.SceneManager;

/**
 * Bundles engine-wide services for easy dependency passing.
 * Generic on action enum type so your library stays type-safe.
 */
public final class EngineContext<A extends Enum<A>> {

    private final Window window;
    private final InputManager input;
    private final ActionMap<A> actions;
    private final SceneManager scenes;
    private final Camera2D camera;

    // Optional: expose last dt if you want scenes to read it (you already pass dt into update)
    private double lastDt;

    public EngineContext(Window window, InputManager input, ActionMap<A> actions, SceneManager scenes, Camera2D camera) {
        this.window = window;
        this.input = input;
        this.actions = actions;
        this.scenes = scenes;
        this.camera = camera;
    }

    public Window window() { return window; }
    public InputManager input() { return input; }
    public ActionMap<A> actions() { return actions; }
    public SceneManager scenes() { return scenes; }
    public Camera2D camera() { return camera; }

    public double lastDt() { return lastDt; }
    public void setLastDt(double dt) { this.lastDt = dt; }
}
