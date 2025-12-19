package org.jarzarr.input;

import org.jarzarr.InputManager;

/**
 * Abstraction for an input "trigger" that can drive an action.
 *
 * <p>
 * A trigger is a small adapter around raw input state (keyboard/mouse/etc.)
 * that can answer three questions for the current tick:
 * </p>
 * <ul>
 * <li>{@link #down(InputManager)}: currently held</li>
 * <li>{@link #pressed(InputManager)}: transitioned up -> down this tick</li>
 * <li>{@link #released(InputManager)}: transitioned down -> up this tick</li>
 * </ul>
 *
 * <p>
 * Triggers are consumed by {@link ActionMap} to implement user-defined action
 * bindings.
 * </p>
 */
public interface Trigger {

	/**
	 * Returns true while the trigger is currently held/active.
	 *
	 * @param input input manager to query
	 * @return true if held
	 */
	boolean down(InputManager input);

	/**
	 * Returns true only on the tick the trigger transitions from inactive ->
	 * active.
	 *
	 * @param input input manager to query
	 * @return true if pressed this tick
	 */
	boolean pressed(InputManager input);

	/**
	 * Returns true only on the tick the trigger transitions from active ->
	 * inactive.
	 *
	 * @param input input manager to query
	 * @return true if released this tick
	 */
	boolean released(InputManager input);
}
