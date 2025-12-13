package org.jarzarr.input;

import org.jarzarr.InputManager;

public interface Trigger {
	boolean down(InputManager input);

	boolean pressed(InputManager input);

	boolean released(InputManager input);
}
