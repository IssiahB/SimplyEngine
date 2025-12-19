package org.jarzarr.input;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import org.jarzarr.InputManager;

/**
 * Maps high-level enum actions to one or more {@link Trigger}s.
 *
 * <p>
 * This lets gameplay/UI code ask questions like "is JUMP pressed?" without
 * caring which key/mouse button caused it.
 * </p>
 *
 * <p>
 * Also supports per-tick "consumption" so one system (ex: UI) can claim an
 * action press/release and prevent other systems (ex: gameplay) from responding
 * in the same tick.
 * </p>
 *
 * <p>
 * Usage pattern:
 * </p>
 * <ul>
 * <li>Bind triggers in {@code GameApp.bindInputs(...)}.</li>
 * <li>Call {@link #update()} once per engine update tick to clear
 * consumption.</li>
 * <li>Query with {@link #isDown(Enum)}, {@link #isPressed(Enum)},
 * {@link #isReleased(Enum)}.</li>
 * <li>Optionally consume with {@link #consumePressed(Enum)} /
 * {@link #consumeReleased(Enum)}.</li>
 * </ul>
 *
 * @param <A> enum type representing high-level actions
 */
public class ActionMap<A extends Enum<A>> {

	/** Raw input manager queried by triggers. */
	private final InputManager input;

	/**
	 * Action -> list of triggers bound to that action. Multiple triggers allow "OR"
	 * behavior (any trigger can activate the action).
	 */
	private final EnumMap<A, List<Trigger>> bindings;

	// ----------------------------
	// Per-tick consumption state
	// ----------------------------

	/** Actions whose pressed state has been consumed this tick. */
	private final EnumSet<A> consumedPressed;

	/** Actions whose released state has been consumed this tick. */
	private final EnumSet<A> consumedReleased;

	/**
	 * Creates an ActionMap for the given action enum type.
	 *
	 * @param actionEnumClass enum class token (used to initialize EnumMap/EnumSet)
	 * @param input           input manager queried by triggers
	 */
	public ActionMap(Class<A> actionEnumClass, InputManager input) {
		this.input = input;
		this.bindings = new EnumMap<>(actionEnumClass);
		this.consumedPressed = EnumSet.noneOf(actionEnumClass);
		this.consumedReleased = EnumSet.noneOf(actionEnumClass);
	}

	/**
	 * Call once per update tick to clear per-tick consumption. Must be called every
	 * tick or consumption will persist.
	 */
	public void update() {
		consumedPressed.clear();
		consumedReleased.clear();
	}

	/**
	 * Binds a trigger to an action. Multiple triggers per action are allowed.
	 *
	 * @param action  the action to bind
	 * @param trigger the trigger that can activate the action
	 */
	public void bind(A action, Trigger trigger) {
		bindings.computeIfAbsent(action, k -> new ArrayList<>()).add(trigger);
	}

	/**
	 * Removes all triggers bound to an action and clears any consumption flags for
	 * it.
	 *
	 * @param action action to unbind
	 */
	public void unbindAll(A action) {
		bindings.remove(action);
		consumedPressed.remove(action);
		consumedReleased.remove(action);
	}

	// ----------------------------
	// Queries
	// ----------------------------

	/**
	 * Returns true while any trigger bound to the action is currently held.
	 *
	 * @param action action to query
	 * @return true if down
	 */
	public boolean isDown(A action) {
		List<Trigger> list = bindings.get(action);
		if (list == null)
			return false;
		for (Trigger t : list)
			if (t.down(input))
				return true;
		return false;
	}

	/**
	 * Returns true only on the tick the action was pressed, unless consumed.
	 *
	 * @param action action to query
	 * @return true if pressed this tick and not consumed
	 */
	public boolean isPressed(A action) {
		if (consumedPressed.contains(action))
			return false;

		List<Trigger> list = bindings.get(action);
		if (list == null)
			return false;
		for (Trigger t : list)
			if (t.pressed(input))
				return true;
		return false;
	}

	/**
	 * Returns true only on the tick the action was released, unless consumed.
	 *
	 * @param action action to query
	 * @return true if released this tick and not consumed
	 */
	public boolean isReleased(A action) {
		if (consumedReleased.contains(action))
			return false;

		List<Trigger> list = bindings.get(action);
		if (list == null)
			return false;
		for (Trigger t : list)
			if (t.released(input))
				return true;
		return false;
	}

	// ----------------------------
	// Consumption
	// ----------------------------

	/**
	 * Prevents other systems from seeing this action as "pressed" for the rest of
	 * the tick. Typical use: UI consumes confirm/cancel so gameplay doesn't also
	 * react.
	 *
	 * @param action action to consume
	 */
	public void consumePressed(A action) {
		consumedPressed.add(action);
	}

	/**
	 * Prevents other systems from seeing this action as "released" for the rest of
	 * the tick.
	 *
	 * @param action action to consume
	 */
	public void consumeReleased(A action) {
		consumedReleased.add(action);
	}

	/**
	 * Convenience: consumes both pressed and released for this tick.
	 *
	 * @param action action to consume
	 */
	public void consume(A action) {
		consumePressed(action);
		consumeReleased(action);
	}

	/**
	 * @param action action to query
	 * @return true if pressed state has been consumed this tick
	 */
	public boolean isPressedConsumed(A action) {
		return consumedPressed.contains(action);
	}

	/**
	 * @param action action to query
	 * @return true if released state has been consumed this tick
	 */
	public boolean isReleasedConsumed(A action) {
		return consumedReleased.contains(action);
	}
}
