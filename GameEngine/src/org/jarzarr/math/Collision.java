package org.jarzarr.math;

/**
 * Lightweight collision helpers for AABB.
 */
public final class Collision {
	private Collision() {
	}

	/** AABB overlap test. */
	public static boolean overlaps(Rectf a, Rectf b) {
		return a.x < b.x + b.w && a.x + a.w > b.x && a.y < b.y + b.h && a.y + a.h > b.y;
	}

	/**
	 * Compute minimum translation vector (MTV) to separate two overlapping AABBs.
	 * Returns true if overlap exists and outMTV is set.
	 */
	public static boolean mtv(Rectf a, Rectf b, Vec2 outMTV) {
		if (!overlaps(a, b))
			return false;

		float axCenter = a.x + a.w * 0.5f;
		float ayCenter = a.y + a.h * 0.5f;
		float bxCenter = b.x + b.w * 0.5f;
		float byCenter = b.y + b.h * 0.5f;

		float dx = bxCenter - axCenter;
		float dy = byCenter - ayCenter;

		float px = (b.w * 0.5f + a.w * 0.5f) - Math.abs(dx);
		float py = (b.h * 0.5f + a.h * 0.5f) - Math.abs(dy);

		if (px < py) {
			outMTV.x = (dx < 0) ? px : -px;
			outMTV.y = 0;
		} else {
			outMTV.x = 0;
			outMTV.y = (dy < 0) ? py : -py;
		}
		return true;
	}

	public static float clamp(float v, float lo, float hi) {
		if (v < lo)
			return lo;
		if (v > hi)
			return hi;
		return v;
	}

	public static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}
}
