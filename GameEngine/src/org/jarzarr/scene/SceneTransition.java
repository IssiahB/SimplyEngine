package org.jarzarr.scene;

import java.awt.Graphics2D;

/**
 * Strategy interface for transitions between two scenes. A transition is driven
 * by time (duration) and renders both scenes.
 */
public interface SceneTransition {

	/** Duration of the transition in seconds. Must be > 0. */
	double durationSeconds();

	/**
	 * Render the transition at progress t01 in [0..1]. Implementations can draw
	 * FROM then TO, using alpha/compositing/etc.
	 */
	void render(Graphics2D g, Scene from, Scene to, float t01);
}
