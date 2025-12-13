package org.jarzarr.input;

import org.jarzarr.InputManager;

public final class MouseButtonTrigger implements Trigger {
	private final int button;

	public MouseButtonTrigger(int button) {
		this.button = button;
	}

	@Override
	public boolean down(InputManager input) {
		return input.isMouseDown(button);
	}

	@Override
	public boolean pressed(InputManager input) {
		return input.isMousePressed(button);
	}

	@Override
	public boolean released(InputManager input) {
		return input.isMouseReleased(button);
	}
}
