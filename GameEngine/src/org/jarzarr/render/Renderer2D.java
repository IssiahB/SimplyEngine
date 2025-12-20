package org.jarzarr.render;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Thin wrapper around Graphics2D to reduce boilerplate and standardize drawing.
 *
 * Usage:<code> public void render(Graphics2D g) { Renderer2D r = new Renderer2D(g);
 * r.text("Hello", 10, 20); } </code>
 *
 * Notes: - This does NOT replace Graphics2D; it wraps it. Your Scene API stays
 * the same. - Provides simple push/pop state so temporary
 * transforms/clips/composites are safe.
 */
public final class Renderer2D {

	private final Graphics2D g;
	private final Deque<State> stack = new ArrayDeque<>(16);

	private static final class State {
		final AffineTransform tx;
		final Shape clip;
		final Composite composite;
		final Paint paint;
		final Stroke stroke;
		final Font font;

		State(Graphics2D g) {
			this.tx = g.getTransform();
			this.clip = g.getClip();
			this.composite = g.getComposite();
			this.paint = g.getPaint();
			this.stroke = g.getStroke();
			this.font = g.getFont();
		}
	}

	public Renderer2D(Graphics2D g) {
		this.g = g;
	}

	public Graphics2D g() {
		return g;
	}

	// ---------- state ----------
	public void push() {
		stack.push(new State(g));
	}

	public void pop() {
		if (stack.isEmpty())
			return;
		State s = stack.pop();
		g.setTransform(s.tx);
		g.setClip(s.clip);
		g.setComposite(s.composite);
		g.setPaint(s.paint);
		g.setStroke(s.stroke);
		g.setFont(s.font);
	}

	// ---------- common setters ----------
	public Renderer2D color(Color c) {
		g.setColor(c);
		return this;
	}

	public Renderer2D font(Font f) {
		if (f != null)
			g.setFont(f);
		return this;
	}

	public Renderer2D stroke(float w) {
		g.setStroke(new BasicStroke(w));
		return this;
	}

	public Renderer2D alpha(float a01) {
		float a = clamp01(a01);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
		return this;
	}

	public Renderer2D clip(int x, int y, int w, int h) {
		g.setClip(x, y, Math.max(0, w), Math.max(0, h));
		return this;
	}

	public Renderer2D resetClip() {
		g.setClip(null);
		return this;
	}

	// ---------- primitives ----------
	public void rectFill(int x, int y, int w, int h) {
		g.fillRect(x, y, w, h);
	}

	public void rect(int x, int y, int w, int h) {
		g.drawRect(x, y, w, h);
	}

	public void ovalFill(int x, int y, int w, int h) {
		g.fillOval(x, y, w, h);
	}

	public void oval(int x, int y, int w, int h) {
		g.drawOval(x, y, w, h);
	}

	public void line(int x1, int y1, int x2, int y2) {
		g.drawLine(x1, y1, x2, y2);
	}

	// ---------- images ----------
	public void image(BufferedImage img, int x, int y) {
		if (img == null)
			return;
		g.drawImage(img, x, y, null);
	}

	public void image(BufferedImage img, int x, int y, int w, int h) {
		if (img == null)
			return;
		g.drawImage(img, x, y, w, h, null);
	}

	// ---------- text ----------
	public void text(String s, int x, int y) {
		if (s == null || s.isEmpty())
			return;
		g.drawString(s, x, y);
	}

	public int textWidth(String s) {
		if (s == null)
			return 0;
		return g.getFontMetrics().stringWidth(s);
	}

	public int fontHeight() {
		return g.getFontMetrics().getHeight();
	}

	private static float clamp01(float v) {
		if (v < 0f)
			return 0f;
		if (v > 1f)
			return 1f;
		return v;
	}
}
