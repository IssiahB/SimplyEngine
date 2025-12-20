package org.jarzarr.test;

import java.awt.Graphics2D;

import org.jarzarr.scene.Scene;
import org.jarzarr.scene.SceneManager;
import org.jarzarr.scene.SceneTransition;

/**
 * Scene wrapper that plays a SceneTransition and then swaps to the target
 * scene.
 *
 * This sits on top of your existing Scene system; no changes needed to Scene.
 */
public final class TransitionScene implements Scene {

	private final SceneManager manager;
	private final Scene from;
	private final Scene to;
	private final SceneTransition transition;

	private double t = 0.0;

	public TransitionScene(SceneManager manager, Scene from, Scene to, SceneTransition transition) {
		this.manager = manager;
		this.from = from;
		this.to = to;
		this.transition = transition;
	}

	@Override
	public void onEnter() {
		// Do NOT call from.onEnter()/onExit() here — SceneManager already controls
		// lifecycle.
		// We do call to.onEnter() now so it can initialize before rendering begins.
		if (to != null)
			to.onEnter();
	}

	@Override
	public void onExit() {
		// Nothing. SceneManager will exit us and then set() the real scene.
	}

	@Override
	public void update(double dt) {
		double dur = transition.durationSeconds();
		t += dt;

		// Update both scenes during transition (keeps animations alive)
		if (from != null)
			from.update(dt);
		if (to != null)
			to.update(dt);

		if (t >= dur) {
			// Transition done: replace stack with TO scene.
			// We must ensure FROM exits (SceneManager.set does this).
			manager.set(to);
		}
	}

	@Override
	public void render(Graphics2D g) {
		double dur = transition.durationSeconds();
		float a = (dur <= 0.0) ? 1f : (float) (t / dur);
		if (a < 0f)
			a = 0f;
		if (a > 1f)
			a = 1f;
		transition.render(g, from, to, a);
	}

	@Override
	public void onResize(int width, int height) {
		if (from != null)
			from.onResize(width, height);
		if (to != null)
			to.onResize(width, height);
	}

	@Override
	public boolean isOverlay() {
		return false;
	}
}
