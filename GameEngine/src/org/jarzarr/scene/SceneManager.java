package org.jarzarr.scene;

import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Deque;

public class SceneManager {

	private final Deque<Scene> stack = new ArrayDeque<>();

	// Deferred operations so scenes can request changes safely during update()
	private Scene pendingSet = null;
	private Scene pendingPush = null;
	private boolean pendingPop = false;

	/** Replace everything with a new base scene. */
	public void set(Scene scene) {
		pendingSet = scene;
		pendingPush = null;
		pendingPop = false;
	}

	/** Push an overlay scene on top (pause/menu). */
	public void push(Scene scene) {
		pendingPush = scene;
	}

	/** Pop the top scene (close pause/menu). */
	public void pop() {
		pendingPop = true;
	}

	public Scene top() {
		return stack.peekLast();
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

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

		// Render from the first non-overlay base scene up through overlays
		// Example: [GameScene, PauseScene] renders both if PauseScene.isOverlay() ==
		// true
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

	private void applyPendingTransitions() {
		// Replace stack
		if (pendingSet != null) {
			while (!stack.isEmpty()) {
				Scene s = stack.removeLast();
				s.onExit();
			}
			stack.addLast(pendingSet);
			pendingSet.onEnter();
			pendingSet = null;
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
