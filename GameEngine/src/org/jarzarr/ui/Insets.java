package org.jarzarr.ui;

public final class Insets {
	public final float left, top, right, bottom;

	public static final Insets ZERO = new Insets(0, 0, 0, 0);

	public Insets(float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public float horizontal() {
		return left + right;
	}

	public float vertical() {
		return top + bottom;
	}

	public static Insets all(float v) {
		return new Insets(v, v, v, v);
	}

	public static Insets hv(float h, float v) {
		return new Insets(h, v, h, v);
	}
}
