package org.jarzarr.scene;

import java.awt.Graphics2D;

/**
 * Scene wrapper that plays a {@link SceneTransition} between two scenes.
 *
 * <p>
 * Supports three use-cases:
 * </p>
 * <ul>
 * <li>SET: replace the entire stack with the "to" scene after the
 * transition.</li>
 * <li>PUSH: push an overlay scene with a transition, then replace this wrapper
 * with the overlay.</li>
 * <li>POP: transition out the current top scene back to the scene below it,
 * then pop this wrapper.</li>
 * </ul>
 *
 * <p>
 * This wrapper is intentionally simple: it forwards update to both scenes
 * (optional) and delegates rendering to the transition.
 * </p>
 */
public final class TransitionScene implements Scene {

	public enum Mode {
		/** After transition completes: SceneManager.set(to) */
		SET,
		/** After transition completes: SceneManager.replaceTop(to) */
		PUSH,
		/** After transition completes: SceneManager.pop() (removes this wrapper) */
		POP
	}

	private final SceneManager scenes;
	private final Scene from;
	private final Scene to;
	private final SceneTransition transition;
	private final Mode mode;

	private double t = 0.0;
	private boolean completed = false;

	public TransitionScene(SceneManager scenes, Scene from, Scene to, SceneTransition transition, Mode mode) {
		this.scenes = scenes;
		this.from = from;
		this.to = to;
		this.transition = transition;
		this.mode = mode;
	}

	@Override
	public void onEnter() {
		// Ensure both scenes are "entered" at least once in the contexts where it
		// matters.
		// - SET: 'to' should be entered so it can load assets/layout before being
		// shown.
		// - PUSH: 'to' is an overlay and should be entered before being faded in.
		// - POP: 'to' is already on stack under us; do NOT call onEnter again.
		if (mode == Mode.SET || mode == Mode.PUSH) {
			if (to != null)
				to.onEnter();
		}
	}

	@Override
	public void onExit() {
		// Important: we do NOT call onExit for from/to here because:
		// - SET: SceneManager.set() will onExit everything properly.
		// - PUSH: SceneManager.replaceTop(to) will onExit this wrapper; 'to' is already
		// entered.
		// - POP: SceneManager.pop() will onExit this wrapper; the old top was already
		// replaced by us.
	}

	@Override
	public void update(double dt) {
		if (completed)
			return;

		// Optional: allow both scenes to tick so their internal animations remain
		// "alive" during fade.
		if (from != null)
			from.update(dt);
		if (to != null)
			to.update(dt);

		t += dt;
		double dur = transition.durationSeconds();
		if (t >= dur) {
			completed = true;
			finish();
		}
	}

	@Override
	public void render(Graphics2D g) {
		float a = (float) (transition.durationSeconds() <= 0.0 ? 1.0 : (t / transition.durationSeconds()));
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
		// PUSH and POP should render above the base scene.
		// SET can be treated as non-overlay (it replaces everything anyway).
		return mode == Mode.PUSH || mode == Mode.POP;
	}

	private void finish() {
		switch (mode) {
		case SET -> scenes.set(to);
		case PUSH -> scenes.replaceTop(to);
		case POP -> scenes.pop(); // pops this wrapper, revealing the scene below
		}
	}
}
