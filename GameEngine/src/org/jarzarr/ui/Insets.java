package org.jarzarr.ui;

/**
 * Immutable padding/margin-like values used by UI nodes.
 *
 * <p>
 * Values are in pixels (float). Insets are commonly used as padding: content
 * area = node bounds minus insets.
 * </p>
 */
public final class Insets {

	/** Left, top, right, bottom inset values in pixels. */
	public final float left, top, right, bottom;

	/** Convenience constant for zero padding. */
	public static final Insets ZERO = new Insets(0, 0, 0, 0);

	/**
	 * Creates an Insets object.
	 *
	 * @param left   left inset
	 * @param top    top inset
	 * @param right  right inset
	 * @param bottom bottom inset
	 */
	public Insets(float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	/** @return left + right */
	public float horizontal() {
		return left + right;
	}

	/** @return top + bottom */
	public float vertical() {
		return top + bottom;
	}

	/**
	 * Creates Insets with all sides set to the same value.
	 *
	 * @param v inset for all sides
	 * @return Insets(v, v, v, v)
	 */
	public static Insets all(float v) {
		return new Insets(v, v, v, v);
	}

	/**
	 * Creates Insets with horizontal and vertical values.
	 *
	 * @param h left/right inset
	 * @param v top/bottom inset
	 * @return Insets(h, v, h, v)
	 */
	public static Insets hv(float h, float v) {
		return new Insets(h, v, h, v);
	}
}
