package org.jarzarr.scene;

import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Stack-based scene controller.
 *
 * <p>
 * The SceneManager maintains a stack of {@link Scene}s and supports replacing
 * the stack, pushing overlay scenes, and popping the top scene.
 * </p>
 *
 * <p>
 * All transitions are deferred until safe points so scenes can request changes
 * during update() without causing concurrent modification or lifecycle
 * inconsistencies.
 * </p>
 *
 * <p>
 * Typical usage:
 * </p>
 * <ul>
 * <li>{@link #set(Scene)} — replace the entire stack (new base scene)</li>
 * <li>{@link #push(Scene)} — add an overlay (pause/menu)</li>
 * <li>{@link #pop()} — remove the top scene</li>
 * </ul>
 */
public class SceneManager {

	/** Stack of active scenes (base scene at bottom, overlays on top). */
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

	/**
	 * Replaces all existing scenes with a new base scene. Actual replacement occurs
	 * at a safe point during update().
	 *
	 * @param scene new base scene
	 */
	public void set(Scene scene) {
		pendingSet = scene;
		pendingPush = null;
		pendingPop = false;
	}

	/**
	 * Pushes an overlay scene on top of the stack. Useful for pause menus, dialogs,
	 * HUD layers, etc.
	 *
	 * @param scene overlay scene
	 */
	public void push(Scene scene) {
		pendingPush = scene;
	}

	/**
	 * Pops the top scene off the stack. Commonly used to close pause menus or
	 * dialogs.
	 */
	public void pop() {
		pendingPop = true;
	}

	/**
	 * Returns the top-most scene without modifying the stack.
	 *
	 * @return top scene, or null if stack is empty
	 */
	public Scene top() {
		return stack.peekLast();
	}

	/**
	 * @return true if no scenes are currently active
	 */
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	/**
	 * Called once per engine update tick. Applies pending transitions, updates the
	 * top scene, then applies transitions again in case the update requested
	 * changes.
	 *
	 * @param dt fixed delta time in seconds
	 */
	public void update(double dt) {
		applyPendingTransitions();

		Scene top = stack.peekLast();
		if (top != null) {
			top.update(dt);
		}

		applyPendingTransitions();
	}

	/**
	 * Called once per render frame.
	 *
	 * <p>
	 * Rendering starts at the first non-overlay scene and proceeds upward, allowing
	 * overlay scenes to render on top of gameplay.
	 * </p>
	 *
	 * @param g graphics context for the frame
	 */
	public void render(Graphics2D g) {
		if (stack.isEmpty())
			return;

		// Render from the first non-overlay base scene up through overlays.
		// Example: [GameScene, PauseScene] renders both if PauseScene.isOverlay() ==
		// true.
		Scene[] scenes = stack.toArray(new Scene[0]);

		int startIndex = scenes.length - 1;
		while (startIndex > 0 && scenes[startIndex].isOverlay()) {
			startIndex--;
		}

		for (int i = startIndex; i < scenes.length; i++) {
			scenes[i].render(g);
		}
	}

	/**
	 * Forwards window resize events to all active scenes.
	 *
	 * @param w new width
	 * @param h new height
	 */
	public void onResize(int w, int h) {
		for (Scene s : stack)
			s.onResize(w, h);
	}

	/**
	 * Applies deferred scene transitions in a controlled order.
	 *
	 * <p>
	 * This ensures scene lifecycle methods (onEnter/onExit) are called safely
	 * outside of iteration.
	 * </p>
	 */
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
		}

		// Push overlay scene
		if (pendingPush != null) {
			stack.addLast(pendingPush);
			pendingPush.onEnter();
			pendingPush = null;
		}

		// Pop top scene
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
