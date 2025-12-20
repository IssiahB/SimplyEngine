package org.jarzarr.scene;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

/**
 * Simple cross-fade: - renders FROM normally - renders TO with alpha = t
 */
public final class FadeTransition implements SceneTransition {

	private final double duration;

	public FadeTransition(double durationSeconds) {
		this.duration = Math.max(0.01, durationSeconds);
	}

	@Override
	public double durationSeconds() {
		return duration;
	}

	@Override
	public void render(Graphics2D g, Scene from, Scene to, float t01) {
		if (from != null)
			from.render(g);

		if (to != null) {
			Composite old = g.getComposite();
			float a = clamp01(t01);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
			to.render(g);
			g.setComposite(old);
		}
	}

	private static float clamp01(float v) {
		if (v < 0f)
			return 0f;
		if (v > 1f)
			return 1f;
		return v;
	}
}
