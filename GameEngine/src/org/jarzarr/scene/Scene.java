package org.jarzarr.scene;

import java.awt.Graphics2D;

public interface Scene {
	/** Called once when the scene becomes active. */
	void onEnter();

	/** Called once when the scene is removed/replaced. */
	void onExit();

	/** Update logic (called from your fixed-timestep loop). */
	void update(double dt);

	/** Render (called once per frame). */
	void render(Graphics2D g);

	/**
	 * Optional: window resize hook. You can leave it empty in scenes that don't
	 * care.
	 */
	default void onResize(int width, int height) {
	}

	/**
	 * If true, scenes below this one will also render. Pause menus often return
	 * true (so gameplay is visible behind).
	 */
	default boolean isOverlay() {
		return false;
	}
}
