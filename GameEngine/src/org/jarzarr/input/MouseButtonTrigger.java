package org.jarzarr.input;

import org.jarzarr.InputManager;

/**
 * Mouse-button-based trigger.
 *
 * <p>
 * Wraps a single MouseEvent button value and exposes the Trigger interface so
 * it can be bound to a high-level action in {@link ActionMap}.
 * </p>
 */
public final class MouseButtonTrigger implements Trigger {

	/** MouseEvent button index to query from InputManager. */
	private final int button;

	/**
	 * Creates a trigger for a specific mouse button.
	 *
	 * @param button MouseEvent button (commonly MouseEvent.BUTTON1, BUTTON2,
	 *               BUTTON3)
	 */
	public MouseButtonTrigger(int button) {
		this.button = button;
	}

	/**
	 * @return true while the mouse button is held down
	 */
	@Override
	public boolean down(InputManager input) {
		return input.isMouseDown(button);
	}

	/**
	 * @return true only on the tick the mouse button was pressed (up -> down)
	 */
	@Override
	public boolean pressed(InputManager input) {
		return input.isMousePressed(button);
	}

	/**
	 * @return true only on the tick the mouse button was released (down -> up)
	 */
	@Override
	public boolean released(InputManager input) {
		return input.isMouseReleased(button);
	}
}
