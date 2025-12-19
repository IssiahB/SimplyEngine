package org.jarzarr.scene;

import java.awt.Graphics2D;

/**
 * Core scene interface.
 *
 * <p>
 * A Scene represents a self-contained state of the application (gameplay, menu,
 * pause screen, dialog, etc.). The engine delegates update and render calls to
 * the active scene(s) through the {@link SceneManager}.
 * </p>
 *
 * <p>
 * Scenes are lifecycle-managed:
 * </p>
 * <ul>
 * <li>{@link #onEnter()} is called when the scene becomes active</li>
 * <li>{@link #onExit()} is called when the scene is removed</li>
 * </ul>
 *
 * <p>
 * Scenes may optionally act as overlays (menus, pause screens) that render on
 * top of scenes below them.
 * </p>
 */
public interface Scene {

	/**
	 * Called once when the scene becomes active. Use this to allocate resources,
	 * reset state, or register listeners.
	 */
	void onEnter();

	/**
	 * Called once when the scene is removed or replaced. Use this to clean up
	 * resources or unregister listeners.
	 */
	void onExit();

	/**
	 * Fixed-timestep update logic. Called from the engine's update loop.
	 *
	 * @param dt fixed delta time in seconds
	 */
	void update(double dt);

	/**
	 * Render hook called once per frame. All drawing should be performed using the
	 * provided Graphics2D.
	 *
	 * @param g graphics context for the current frame
	 */
	void render(Graphics2D g);

	/**
	 * Optional window resize callback. Called whenever the window's canvas size
	 * changes.
	 *
	 * @param width  new canvas width
	 * @param height new canvas height
	 */
	default void onResize(int width, int height) {
	}

	/**
	 * Indicates whether this scene is an overlay.
	 *
	 * <p>
	 * If true, scenes below this one in the stack will also render. Common examples
	 * include pause menus, HUDs, and dialogs.
	 * </p>
	 *
	 * <p>
	 * If false, this scene is treated as a base scene and will obscure scenes below
	 * it.
	 * </p>
	 *
	 * @return true if this scene should render on top of lower scenes
	 */
	default boolean isOverlay() {
		return false;
	}
}
