package org.jarzarr.input;

import org.jarzarr.InputManager;

/**
 * Keyboard-based trigger.
 *
 * <p>
 * Wraps a single KeyEvent keycode and exposes the Trigger interface so it can
 * be bound to a high-level action in {@link ActionMap}.
 * </p>
 */
public final class KeyTrigger implements Trigger {

	/** KeyEvent keycode to query from InputManager. */
	private final int keyCode;

	/**
	 * Creates a trigger for a specific keyboard key.
	 *
	 * @param keyCode KeyEvent keycode (ex: KeyEvent.VK_A)
	 */
	public KeyTrigger(int keyCode) {
		this.keyCode = keyCode;
	}

	/**
	 * @return true while the key is held down
	 */
	@Override
	public boolean down(InputManager input) {
		return input.isKeyDown(keyCode);
	}

	/**
	 * @return true only on the tick the key was pressed (up -> down)
	 */
	@Override
	public boolean pressed(InputManager input) {
		return input.isKeyPressed(keyCode);
	}

	/**
	 * @return true only on the tick the key was released (down -> up)
	 */
	@Override
	public boolean released(InputManager input) {
		return input.isKeyReleased(keyCode);
	}
}
