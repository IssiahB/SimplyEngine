package org.jarzarr.input;

import org.jarzarr.InputManager;

public final class KeyTrigger implements Trigger {
	private final int keyCode;

	public KeyTrigger(int keyCode) {
		this.keyCode = keyCode;
	}

	@Override
	public boolean down(InputManager input) {
		return input.isKeyDown(keyCode);
	}

	@Override
	public boolean pressed(InputManager input) {
		return input.isKeyPressed(keyCode);
	}

	@Override
	public boolean released(InputManager input) {
		return input.isKeyReleased(keyCode);
	}
}
