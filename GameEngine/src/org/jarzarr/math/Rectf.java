package org.jarzarr.math;

/**
 * Axis-aligned rectangle.
 */
public final class Rectf {
	public float x, y, w, h;

	public Rectf() {
		this(0, 0, 0, 0);
	}

	public Rectf(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public float left() {
		return x;
	}

	public float right() {
		return x + w;
	}

	public float top() {
		return y;
	}

	public float bottom() {
		return y + h;
	}

	public boolean contains(float px, float py) {
		return px >= x && py >= y && px <= (x + w) && py <= (y + h);
	}
}
