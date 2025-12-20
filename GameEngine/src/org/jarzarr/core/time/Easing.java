package org.jarzarr.core.time;

/**
 * Easing functions for tweens.
 */
public final class Easing {
	private Easing() {
	}

	public interface Ease {
		float apply(float t01);
	}

	public static final Ease LINEAR = t -> t;

	public static final Ease EASE_IN_OUT = t -> {
		// smoothstep
		return t * t * (3f - 2f * t);
	};

	public static final Ease EASE_OUT = t -> {
		// quad out
		float u = 1f - t;
		return 1f - (u * u);
	};

	public static final Ease EASE_IN = t -> t * t;
}
