package org.jarzarr;

import java.awt.Canvas;
import java.awt.event.*;

public class InputManager implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	private static final int MAX_KEYS = 512;
	private static final int MAX_MOUSE_BUTTONS = 8;

	private final boolean[] keysDown = new boolean[MAX_KEYS];
	private final boolean[] keysPressed = new boolean[MAX_KEYS];
	private final boolean[] keysReleased = new boolean[MAX_KEYS];

	private final boolean[] mouseDown = new boolean[MAX_MOUSE_BUTTONS];
	private final boolean[] mousePressed = new boolean[MAX_MOUSE_BUTTONS];
	private final boolean[] mouseReleased = new boolean[MAX_MOUSE_BUTTONS];

	private int mouseX, mouseY;
	private int mouseDX, mouseDY;
	private int scroll;

	public InputManager(Canvas canvas) {
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
	}

	/**
	 * Call once per update() tick. Resets transitional states.
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
	}

	// --------------------
	// Keyboard
	// --------------------

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

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if (key >= 0 && key < MAX_KEYS) {
			keysDown[key] = false;
			keysReleased[key] = true;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Not used for games
	}

	// --------------------
	// Mouse Buttons
	// --------------------

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

	@Override
	public void mouseReleased(MouseEvent e) {
		int btn = e.getButton();
		if (btn >= 0 && btn < MAX_MOUSE_BUTTONS) {
			mouseDown[btn] = false;
			mouseReleased[btn] = true;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	// --------------------
	// Mouse Movement
	// --------------------

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseDX += e.getX() - mouseX; // Value positive = mouse move right | negative = mouse move left
		mouseDY += e.getY() - mouseY; // Value positive = mouse move down | negative = mouse move up
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	// --------------------
	// Mouse Wheel
	// --------------------

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		scroll += e.getWheelRotation(); // Value 1 = scroll down | -1 = scroll up
	}

	// --------------------
	// Query API
	// --------------------

	public boolean isKeyDown(int key) {
		return key >= 0 && key < MAX_KEYS && keysDown[key];
	}

	public boolean isKeyPressed(int key) {
		return key >= 0 && key < MAX_KEYS && keysPressed[key];
	}

	public boolean isKeyReleased(int key) {
		return key >= 0 && key < MAX_KEYS && keysReleased[key];
	}

	public boolean isMouseDown(int button) {
		return button >= 0 && button < MAX_MOUSE_BUTTONS && mouseDown[button];
	}

	public boolean isMousePressed(int button) {
		return button >= 0 && button < MAX_MOUSE_BUTTONS && mousePressed[button];
	}

	public boolean isMouseReleased(int button) {
		return button >= 0 && button < MAX_MOUSE_BUTTONS && mouseReleased[button];
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}

	public int getMouseDX() {
		return mouseDX;
	}

	public int getMouseDY() {
		return mouseDY;
	}

	public int getScroll() {
		return scroll;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
