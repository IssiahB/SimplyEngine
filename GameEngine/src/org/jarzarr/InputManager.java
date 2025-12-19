package org.jarzarr;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Central input collector for keyboard + mouse.
 *
 * <p>
 * Tracks three states for keys/buttons:
 * </p>
 * <ul>
 * <li>Down: currently held</li>
 * <li>Pressed: transitioned up -> down this tick</li>
 * <li>Released: transitioned down -> up this tick</li>
 * </ul>
 *
 * <p>
 * Also tracks mouse position, delta movement (since last update tick), scroll
 * wheel delta, and typed text (for UI text fields).
 * </p>
 *
 * <p>
 * Important: Call {@link #update()} once per engine update tick to reset
 * transitional states (pressed/released/deltas/typedChars).
 * </p>
 */
public class InputManager implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	/** Max key codes tracked (indexed by KeyEvent keycode). */
	private static final int MAX_KEYS = 512;

	/** Max mouse buttons tracked (indexed by MouseEvent button). */
	private static final int MAX_MOUSE_BUTTONS = 8;

	/** True while the key is held. */
	private final boolean[] keysDown = new boolean[MAX_KEYS];

	/** True only on the tick the key transitions from up -> down. */
	private final boolean[] keysPressed = new boolean[MAX_KEYS];

	/** True only on the tick the key transitions from down -> up. */
	private final boolean[] keysReleased = new boolean[MAX_KEYS];

	/** True while the mouse button is held. */
	private final boolean[] mouseDown = new boolean[MAX_MOUSE_BUTTONS];

	/** True only on the tick the mouse button transitions from up -> down. */
	private final boolean[] mousePressed = new boolean[MAX_MOUSE_BUTTONS];

	/** True only on the tick the mouse button transitions from down -> up. */
	private final boolean[] mouseReleased = new boolean[MAX_MOUSE_BUTTONS];

	/** Current mouse X position in canvas coordinates. */
	private int mouseX, mouseY;

	/** Accumulated mouse delta since last update tick. */
	private int mouseDX, mouseDY;

	/** Accumulated scroll wheel delta since last update tick. */
	private int scroll;

	/**
	 * Typed characters since last update tick (for UI TextField input). Cleared in
	 * {@link #update()}.
	 */
	private final List<Character> typedChars = new ArrayList<>(64);

	/**
	 * Creates an input manager and registers listeners on the provided canvas.
	 *
	 * @param canvas engine render/input canvas
	 */
	public InputManager(Canvas canvas) {
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
	}

	/**
	 * Call once per update tick.
	 *
	 * <p>
	 * Resets transitional states (pressed/released), per-tick deltas
	 * (mouseDX/mouseDY/scroll), and clears typed text.
	 * </p>
	 */
	public void update() {
		for (int i = 0; i < MAX_KEYS; i++) {
			keysPressed[i] = false;
			keysReleased[i] = false;
		}
		for (int i = 0; i < MAX_MOUSE_BUTTONS; i++) {
			mousePressed[i] = false;
			mouseReleased[i] = false;
		}
		mouseDX = 0;
		mouseDY = 0;
		scroll = 0;

		// Clear typed characters once per tick.
		typedChars.clear();
	}

	// --------------------
	// Keyboard
	// --------------------

	/**
	 * Key down event. Sets keysDown and (if it was previously up) keysPressed.
	 *
	 * @param e key event
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key >= 0 && key < MAX_KEYS) {
			if (!keysDown[key]) {
				keysPressed[key] = true;
			}
			keysDown[key] = true;
		}
	}

	/**
	 * Key up event. Clears keysDown and sets keysReleased for this tick.
	 *
	 * @param e key event
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if (key >= 0 && key < MAX_KEYS) {
			keysDown[key] = false;
			keysReleased[key] = true;
		}
	}

	/**
	 * Typed character event. Collects printable characters for text input. Control
	 * characters and undefined chars are ignored.
	 *
	 * @param e key typed event
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		char c = e.getKeyChar();

		// Filter out undefined / control chars (keep basic printable chars).
		if (c == KeyEvent.CHAR_UNDEFINED)
			return;
		if (Character.isISOControl(c))
			return;

		typedChars.add(c);
	}

	// --------------------
	// Mouse Buttons
	// --------------------

	/**
	 * Mouse down event. Sets mouseDown and (if it was previously up) mousePressed.
	 *
	 * @param e mouse event
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		int btn = e.getButton();
		if (btn >= 0 && btn < MAX_MOUSE_BUTTONS) {
			if (!mouseDown[btn]) {
				mousePressed[btn] = true;
			}
			mouseDown[btn] = true;
		}
	}

	/**
	 * Mouse up event. Clears mouseDown and sets mouseReleased for this tick.
	 *
	 * @param e mouse event
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		int btn = e.getButton();
		if (btn >= 0 && btn < MAX_MOUSE_BUTTONS) {
			mouseDown[btn] = false;
			mouseReleased[btn] = true;
		}
	}

	/** Mouse click event (unused; pressed/released are tracked instead). */
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	// --------------------
	// Mouse Movement
	// --------------------

	/**
	 * Mouse move event. Updates current position and accumulates delta since last
	 * tick.
	 *
	 * @param e mouse event
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		mouseDX += e.getX() - mouseX;
		mouseDY += e.getY() - mouseY;
		mouseX = e.getX();
		mouseY = e.getY();
	}

	/**
	 * Mouse drag event. Treated the same as mouseMoved for delta tracking.
	 *
	 * @param e mouse event
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	// --------------------
	// Mouse Wheel
	// --------------------

	/**
	 * Mouse wheel event. Accumulates scroll rotation (commonly -1/1 per notch).
	 *
	 * @param e mouse wheel event
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		scroll += e.getWheelRotation();
	}

	// --------------------
	// Query API
	// --------------------

	/**
	 * Returns whether the key is currently held down.
	 *
	 * @param key KeyEvent keycode
	 * @return true if held
	 */
	public boolean isKeyDown(int key) {
		return key >= 0 && key < MAX_KEYS && keysDown[key];
	}

	/**
	 * Returns whether the key was pressed this tick (up -> down transition).
	 *
	 * @param key KeyEvent keycode
	 * @return true if pressed this tick
	 */
	public boolean isKeyPressed(int key) {
		return key >= 0 && key < MAX_KEYS && keysPressed[key];
	}

	/**
	 * Returns whether the key was released this tick (down -> up transition).
	 *
	 * @param key KeyEvent keycode
	 * @return true if released this tick
	 */
	public boolean isKeyReleased(int key) {
		return key >= 0 && key < MAX_KEYS && keysReleased[key];
	}

	/**
	 * Returns whether the mouse button is currently held down.
	 *
	 * @param button MouseEvent button id
	 * @return true if held
	 */
	public boolean isMouseDown(int button) {
		return button >= 0 && button < MAX_MOUSE_BUTTONS && mouseDown[button];
	}

	/**
	 * Returns whether the mouse button was pressed this tick (up -> down
	 * transition).
	 *
	 * @param button MouseEvent button id
	 * @return true if pressed this tick
	 */
	public boolean isMousePressed(int button) {
		return button >= 0 && button < MAX_MOUSE_BUTTONS && mousePressed[button];
	}

	/**
	 * Returns whether the mouse button was released this tick (down -> up
	 * transition).
	 *
	 * @param button MouseEvent button id
	 * @return true if released this tick
	 */
	public boolean isMouseReleased(int button) {
		return button >= 0 && button < MAX_MOUSE_BUTTONS && mouseReleased[button];
	}

	/** @return current mouse X position. */
	public int getMouseX() {
		return mouseX;
	}

	/** @return current mouse Y position. */
	public int getMouseY() {
		return mouseY;
	}

	/** @return accumulated mouse delta X since last update tick. */
	public int getMouseDX() {
		return mouseDX;
	}

	/** @return accumulated mouse delta Y since last update tick. */
	public int getMouseDY() {
		return mouseDY;
	}

	/** @return accumulated scroll delta since last update tick. */
	public int getScroll() {
		return scroll;
	}

	/**
	 * Returns the typed characters for this tick as a String. Cleared automatically
	 * next {@link #update()}.
	 *
	 * @return typed text for this tick (may be empty)
	 */
	public String getTypedText() {
		if (typedChars.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder(typedChars.size());
		for (char c : typedChars)
			sb.append(c);
		return sb.toString();
	}

	/** Mouse entered event (unused). */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/** Mouse exited event (unused). */
	@Override
	public void mouseExited(MouseEvent e) {
	}
}
