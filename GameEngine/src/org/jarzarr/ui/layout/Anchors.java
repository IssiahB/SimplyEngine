package org.jarzarr.ui.layout;

/**
 * Anchor rules for children inside {@link AnchorPane}.
 *
 * <p>
 * Any field may be null (unused). The meaning:
 * </p>
 * <ul>
 * <li>left: distance from left edge of parent content</li>
 * <li>right: distance from right edge</li>
 * <li>top: distance from top edge</li>
 * <li>bottom: distance from bottom edge</li>
 * </ul>
 *
 * <p>
 * Combinations:
 * </p>
 * <ul>
 * <li>left only: pin to left, keep width as-is/pref</li>
 * <li>right only: pin to right, keep width as-is/pref</li>
 * <li>left + right: stretch horizontally between them</li>
 * <li>top/bottom similarly for vertical</li>
 * </ul>
 */
public final class Anchors {

	/** Optional anchor offsets (null means unused). */
	public final Float left, right, top, bottom;

	private Anchors(Float left, Float right, Float top, Float bottom) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}

	/** @return anchors that fill the parent content area (0 on all sides). */
	public static Anchors fill() {
		return new Anchors(0f, 0f, 0f, 0f);
	}

	/** Pin to top-left using left/top offsets. */
	public static Anchors topLeft(float left, float top) {
		return new Anchors(left, null, top, null);
	}

	/** Pin to top-right using right/top offsets. */
	public static Anchors topRight(float right, float top) {
		return new Anchors(null, right, top, null);
	}

	/** Pin to bottom-left using left/bottom offsets. */
	public static Anchors bottomLeft(float left, float bottom) {
		return new Anchors(left, null, null, bottom);
	}

	/** Pin to bottom-right using right/bottom offsets. */
	public static Anchors bottomRight(float right, float bottom) {
		return new Anchors(null, right, null, bottom);
	}

	/** Stretch horizontally between left/right, and pin to top. */
	public static Anchors stretchTop(float left, float right, float top) {
		return new Anchors(left, right, top, null);
	}

	/** Stretch horizontally between left/right, and pin to bottom. */
	public static Anchors stretchBottom(float left, float right, float bottom) {
		return new Anchors(left, right, null, bottom);
	}

	/**
	 * Stretch vertically between top/bottom, and pin to left.
	 *
	 * @param top    top offset
	 * @param bottom bottom offset
	 * @param left   left offset
	 */
	public static Anchors stretchVertical(float top, float bottom, float left) {
		return new Anchors(left, null, top, bottom);
	}

	/** Stretch both horizontally and vertically. */
	public static Anchors stretch(float left, float right, float top, float bottom) {
		return new Anchors(left, right, top, bottom);
	}
}
