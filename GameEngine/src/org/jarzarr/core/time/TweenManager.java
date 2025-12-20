package org.jarzarr.core.time;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tween system for animating values over time.
 *
 * Use: ctx.tweens().toFloat( () -> x, v -> x = v, 0f, 100f, 0.25,
 * Easing.EASE_IN_OUT );
 */
public final class TweenManager {

	public interface FloatGetter {
		float get();
	}

	public interface FloatSetter {
		void set(float v);
	}

	private static final class Tween {
		final FloatGetter get;
		final FloatSetter set;
		final float from;
		final float to;
		final double duration;
		final Easing.Ease ease;
		final Runnable onComplete;

		double t = 0.0;
		boolean done = false;

		Tween(FloatGetter get, FloatSetter set, float from, float to, double duration, Easing.Ease ease,
				Runnable onComplete) {
			this.get = get;
			this.set = set;
			this.from = from;
			this.to = to;
			this.duration = Math.max(0.001, duration);
			this.ease = (ease != null) ? ease : Easing.LINEAR;
			this.onComplete = onComplete;
		}
	}

	private final List<Tween> tweens = new ArrayList<>(64);

	public void clear() {
		tweens.clear();
	}

	public void update(double dt) {
		if (tweens.isEmpty())
			return;

		Iterator<Tween> it = tweens.iterator();
		while (it.hasNext()) {
			Tween tw = it.next();
			if (tw.done) {
				it.remove();
				continue;
			}

			tw.t += dt;
			float p = (float) (tw.t / tw.duration);
			if (p >= 1f)
				p = 1f;

			float e = tw.ease.apply(p);
			float v = lerp(tw.from, tw.to, e);

			try {
				tw.set.set(v);
			} catch (Exception ex) {
				ex.printStackTrace();
				tw.done = true;
				it.remove();
				continue;
			}

			if (p >= 1f) {
				tw.done = true;
				it.remove();
				if (tw.onComplete != null) {
					try {
						tw.onComplete.run();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	public void toFloat(FloatGetter get, FloatSetter set, float from, float to, double seconds, Easing.Ease ease) {
		toFloat(get, set, from, to, seconds, ease, null);
	}

	public void toFloat(FloatGetter get, FloatSetter set, float from, float to, double seconds, Easing.Ease ease,
			Runnable onComplete) {
		tweens.add(new Tween(get, set, from, to, seconds, ease, onComplete));
	}

	public void toFloatFromCurrent(FloatGetter get, FloatSetter set, float to, double seconds, Easing.Ease ease) {
		float from = (get != null) ? get.get() : 0f;
		toFloat(get, set, from, to, seconds, ease, null);
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}
}
