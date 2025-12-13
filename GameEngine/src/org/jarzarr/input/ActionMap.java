package org.jarzarr.input;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import org.jarzarr.InputManager;

public class ActionMap<A extends Enum<A>> {

    private final InputManager input;
    private final EnumMap<A, List<Trigger>> bindings;

    // Per-tick consumption state
    private final EnumSet<A> consumedPressed;
    private final EnumSet<A> consumedReleased;

    public ActionMap(Class<A> actionEnumClass, InputManager input) {
        this.input = input;
        this.bindings = new EnumMap<>(actionEnumClass);
        this.consumedPressed = EnumSet.noneOf(actionEnumClass);
        this.consumedReleased = EnumSet.noneOf(actionEnumClass);
    }

    /** Call once per update() tick to clear consumption state. */
    public void update() {
        consumedPressed.clear();
        consumedReleased.clear();
    }

    public void bind(A action, Trigger trigger) {
        bindings.computeIfAbsent(action, k -> new ArrayList<>()).add(trigger);
    }

    public void unbindAll(A action) {
        bindings.remove(action);
        consumedPressed.remove(action);
        consumedReleased.remove(action);
    }

    // ----------------------------
    // Queries
    // ----------------------------

    public boolean isDown(A action) {
        List<Trigger> list = bindings.get(action);
        if (list == null) return false;
        for (Trigger t : list) if (t.down(input)) return true;
        return false;
    }

    public boolean isPressed(A action) {
        if (consumedPressed.contains(action)) return false;

        List<Trigger> list = bindings.get(action);
        if (list == null) return false;
        for (Trigger t : list) if (t.pressed(input)) return true;
        return false;
    }

    public boolean isReleased(A action) {
        if (consumedReleased.contains(action)) return false;

        List<Trigger> list = bindings.get(action);
        if (list == null) return false;
        for (Trigger t : list) if (t.released(input)) return true;
        return false;
    }

    // ----------------------------
    // Consumption
    // ----------------------------

    /** Prevent other systems from seeing this action as "pressed" for the rest of this tick. */
    public void consumePressed(A action) {
        consumedPressed.add(action);
    }

    /** Prevent other systems from seeing this action as "released" for the rest of this tick. */
    public void consumeReleased(A action) {
        consumedReleased.add(action);
    }

    /** Optional convenience: consume both pressed+released for this tick. */
    public void consume(A action) {
        consumePressed(action);
        consumeReleased(action);
    }

    public boolean isPressedConsumed(A action) {
        return consumedPressed.contains(action);
    }

    public boolean isReleasedConsumed(A action) {
        return consumedReleased.contains(action);
    }
}
