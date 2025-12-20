package org.jarzarr.math;

/**
 * Minimal 2D vector (float-based for typical 2D games). Mutable for low
 * allocation.
 */
public final class Vec2 {
	public float x;
	public float y;

	public Vec2() {
		this(0, 0);
	}

	public Vec2(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vec2 set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vec2 set(Vec2 o) {
		this.x = o.x;
		this.y = o.y;
		return this;
	}

	public Vec2 add(float dx, float dy) {
		this.x += dx;
		this.y += dy;
		return this;
	}

	public Vec2 add(Vec2 o) {
		this.x += o.x;
		this.y += o.y;
		return this;
	}

	public Vec2 sub(float dx, float dy) {
		this.x -= dx;
		this.y -= dy;
		return this;
	}

	public Vec2 sub(Vec2 o) {
		this.x -= o.x;
		this.y -= o.y;
		return this;
	}

	public Vec2 mul(float s) {
		this.x *= s;
		this.y *= s;
		return this;
	}

	public float len2() {
		return x * x + y * y;
	}

	public float len() {
		return (float) Math.sqrt(len2());
	}

	public Vec2 normalize() {
		float l = len();
		if (l > 1e-6f) {
			x /= l;
			y /= l;
		}
		return this;
	}

	public static float dot(Vec2 a, Vec2 b) {
		return a.x * b.x + a.y * b.y;
	}
}
