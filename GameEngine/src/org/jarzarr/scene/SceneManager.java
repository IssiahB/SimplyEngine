package org.jarzarr.scene;

import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Stack-based scene controller.
 *
 * <p>
 * Supports:
 * </p>
 * <ul>
 * <li>set(Scene) — replace entire stack</li>
 * <li>push(Scene) — push overlay</li>
 * <li>pop() — pop top</li>
 * <li>replaceTop(Scene) — replace only the top scene</li>
 * </ul>
 *
 * <p>
 * All mutations are deferred until safe points during update().
 * </p>
 */
public class SceneManager {

	/** Stack of active scenes (base at bottom, overlays on top). */
	private final Deque<Scene> stack = new ArrayDeque<>();

	// ----------------------------
	// Deferred transition requests
	// ----------------------------

	/** Pending full stack replacement. */
	private Scene pendingSet = null;

	/** Pending overlay push. */
	private Scene pendingPush = null;

	/** Pending pop request. */
	private boolean pendingPop = false;

	/** Pending top replacement (atomic: pop top + push new). */
	private Scene pendingReplaceTop = null;

	/** Replace everything with a new base scene (deferred). */
	public void set(Scene scene) {
		pendingSet = scene;
		pendingReplaceTop = null;
		pendingPush = null;
		pendingPop = false;
	}

	/** Push an overlay scene on top (deferred). */
	public void push(Scene scene) {
		pendingPush = scene;
	}

	/** Pop the top scene (deferred). */
	public void pop() {
		pendingPop = true;
	}

	/**
	 * Replace only the top scene (deferred). This is critical for transitions where
	 * you need to swap a wrapper scene into/out of the stack without destroying the
	 * base scene underneath.
	 */
	public void replaceTop(Scene scene) {
		pendingReplaceTop = scene;
		// Do NOT clear pendingSet by default; replaceTop is its own operation.
		// But if caller is deliberately replacing top, we should avoid an accidental
		// pop/push ordering issue.
		pendingPush = null;
		pendingPop = false;
	}

	public Scene top() {
		return stack.peekLast();
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

	// ----------------------------------------------------
	// Transitions
	// ----------------------------------------------------

	/**
	 * Replace the entire stack with a transition wrapper that fades from the
	 * current top to {@code next}, then ends with {@code next} as the only scene.
	 *
	 * Use this for: title -> game, game -> gameover, level changes, etc.
	 */
	public void transitionTo(Scene next, SceneTransition transition) {
		Scene from = stack.peekLast();
		Scene wrapper = new TransitionScene(this, from, next, transition, TransitionScene.Mode.SET);
		set(wrapper);
	}

	/**
	 * Push an overlay scene (pause/menu) with a transition. After the transition
	 * completes, the wrapper is replaced with the overlay (stack is preserved).
	 */
	public void transitionPush(Scene overlay, SceneTransition transition) {
		Scene from = stack.peekLast();
		Scene wrapper = new TransitionScene(this, from, overlay, transition, TransitionScene.Mode.PUSH);
		push(wrapper);
	}

	/**
	 * Pop the current top scene with a transition. This replaces the current top
	 * with a transition wrapper which fades back to the scene below, then pops the
	 * wrapper.
	 */
	public void transitionPop(SceneTransition transition) {
		if (stack.isEmpty())
			return;

		Scene from = stack.peekLast(); // current top
		Scene to = peekBelowTop(); // scene underneath (may be null)

		// For POP we usually want a "fade out" of FROM revealing TO.
		// Your FadeTransition is "fade in TO over FROM" (alpha=t).
		// We adapt it by swapping and inverting.
		SceneTransition adapted = new SceneTransition() {
			@Override
			public double durationSeconds() {
				return transition.durationSeconds();
			}

			@Override
			public void render(Graphics2D g, Scene ignoredFrom, Scene ignoredTo, float t01) {
				// Render underlying (to) normally, then overlay (from) with alpha (1 - t).
				// We can reuse the provided transition by swapping parameters and inverting t.
				// Equivalent for FadeTransition; also works for most cross-fade style
				// transitions.
				float inv = 1f - t01;
				transition.render(g, to, from, inv);
			}
		};

		Scene wrapper = new TransitionScene(this, from, to, adapted, TransitionScene.Mode.POP);

		// Replace the top scene (the one we're closing) with the wrapper.
		// This prevents input going to the old top while it is fading out.
		replaceTop(wrapper);
	}

	private Scene peekBelowTop() {
		if (stack.size() < 2)
			return null;
		// Deque has no direct "peekSecondLast"; iterate safely
		Scene below = null;
		for (Scene s : stack) {
			below = s;
		}
		// The loop ends at last element; we need second last, so do it differently:
		Scene last = stack.removeLast();
		below = stack.peekLast();
		stack.addLast(last);
		return below;
	}

	// ----------------------------------------------------
	// Loop hooks
	// ----------------------------------------------------

	/** Call once per update tick. */
	public void update(double dt) {
		applyPendingTransitions();

		Scene top = stack.peekLast();
		if (top != null) {
			top.update(dt);
		}

		applyPendingTransitions();
	}

	/** Call once per render frame. */
	public void render(Graphics2D g) {
		if (stack.isEmpty())
			return;

		Scene[] scenes = stack.toArray(new Scene[0]);

		int startIndex = scenes.length - 1;
		while (startIndex > 0 && scenes[startIndex].isOverlay()) {
			startIndex--;
		}

		for (int i = startIndex; i < scenes.length; i++) {
			scenes[i].render(g);
		}
	}

	public void onResize(int w, int h) {
		for (Scene s : stack)
			s.onResize(w, h);
	}

	// ----------------------------------------------------
	// Deferred apply
	// ----------------------------------------------------

	private void applyPendingTransitions() {
		// Replace entire stack
		if (pendingSet != null) {
			while (!stack.isEmpty()) {
				Scene s = stack.removeLast();
				s.onExit();
			}
			stack.addLast(pendingSet);
			pendingSet.onEnter();
			pendingSet = null;

			// If we set, ignore other pending ops that tick
			pendingReplaceTop = null;
			pendingPush = null;
			pendingPop = false;
			return;
		}

		// Replace only top
		if (pendingReplaceTop != null) {
			Scene oldTop = stack.peekLast();
			if (oldTop != null) {
				stack.removeLast();
				oldTop.onExit();
			}
			stack.addLast(pendingReplaceTop);
			pendingReplaceTop.onEnter();
			pendingReplaceTop = null;
		}

		// Push overlay
		if (pendingPush != null) {
			stack.addLast(pendingPush);
			pendingPush.onEnter();
			pendingPush = null;
		}

		// Pop top
		if (pendingPop) {
			Scene s = stack.peekLast();
			if (s != null) {
				stack.removeLast();
				s.onExit();
			}
			pendingPop = false;
		}
	}
}
