package org.jarzarr.ui.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import org.jarzarr.ui.Insets;
import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

/**
 * Interactive slider for a normalized value in [0..1].
 *
 * <p>
 * Input:
 * </p>
 * <ul>
 * <li>Mouse down sets the value from the mouse x position</li>
 * <li>Mouse drag updates while pressed</li>
 * <li>Keyboard left/right adjusts when focused</li>
 * </ul>
 *
 * <p>
 * Fix: drag detection no longer hardcodes mouse button 1; it checks any mouse
 * button being held while this slider is pressed.
 * </p>
 */
public final class Slider extends UINode {

	private String label = "Slider";
	private float value = 0.5f; // 0..1
	private Font font;

	private int trackH = 8;
	private int knobR = 10;

	private Runnable onChanged;

	public Slider(String label, float initial01) {
		this.label = label;
		this.value = clamp01(initial01);

		this.prefW = 420;
		this.prefH = 64;
		this.padding = Insets.hv(10, 10);
	}

	public float value() {
		return value;
	}

	public Slider value(float v) {
		float nv = clamp01(v);
		if (nv != this.value) {
			this.value = nv;
			if (onChanged != null)
				onChanged.run();
		}
		return this;
	}

	public Slider label(String s) {
		this.label = s;
		return this;
	}

	public Slider font(Font f) {
		this.font = f;
		return this;
	}

	public Slider onChanged(Runnable r) {
		this.onChanged = r;
		return this;
	}

	@Override
	protected boolean isInteractive() {
		return true;
	}

	@Override
	protected boolean isFocusable() {
		return true;
	}

	@Override
	public boolean onMouseDown(UIContext ctx, int button) {
		setFromMouse(ctx);
		return true;
	}

	@Override
	public boolean onMouseMove(UIContext ctx, float mx, float my) {
		// Drag behavior: update while this node is pressed and any mouse button is
		// down.
		if (pressed && anyMouseDown(ctx)) {
			setFromMouse(ctx);
			return true;
		}
		return false;
	}

	@Override
	public boolean onMouseUp(UIContext ctx, int button) {
		return true;
	}

	@Override
	public boolean onKeyDown(UIContext ctx, int keyCode) {
		if (!focused || !enabled)
			return false;

		if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
			value(Math.max(0f, value - 0.05f));
			return true;
		}
		if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
			value(Math.min(1f, value + 0.05f));
			return true;
		}
		return false;
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
		if (hovered) {
			g.setColor(new Color(255, 255, 255, 14));
			g.fillRect((int) gx, (int) gy, (int) gw, (int) gh);
		}

		if (focused) {
			g.setColor(new Color(255, 255, 255, 90));
			g.drawRect((int) gx + 2, (int) gy + 2, (int) gw - 4, (int) gh - 4);
		}

		Font f = (font != null) ? font : (ctx.defaultFont != null ? ctx.defaultFont : ctx.theme.font);
		if (f != null)
			g.setFont(f);
		FontMetrics fm = g.getFontMetrics();

		int innerX = (int) (gx + padding.left);
		int innerY = (int) (gy + padding.top);
		int innerW = (int) (gw - padding.horizontal());
		int innerH = (int) (gh - padding.vertical());

		String txt = label != null ? label : "";
		int labelY = innerY + fm.getAscent();
		g.setColor(ctx.theme.text);
		g.drawString(txt, innerX, labelY);

		String pct = (int) (value * 100) + "%";
		int pctW = fm.stringWidth(pct);
		g.setColor(ctx.theme.mutedText);
		g.drawString(pct, innerX + innerW - pctW, labelY);

		int trackY = innerY + fm.getHeight() + 14;
		int trackTop = trackY - trackH / 2;

		g.setColor(new Color(255, 255, 255, 35));
		g.fillRect(innerX, trackTop, innerW, trackH);

		int fillW = (int) (innerW * value);
		g.setColor(new Color(255, 255, 255, 90));
		g.fillRect(innerX, trackTop, fillW, trackH);

		int knobX = innerX + fillW;
		int knobY = trackY;

		g.setColor(ctx.theme.caret);
		g.fillOval(knobX - knobR, knobY - knobR, knobR * 2, knobR * 2);

		g.setColor(new Color(0, 0, 0, 120));
		g.drawOval(knobX - knobR, knobY - knobR, knobR * 2, knobR * 2);
	}

	private void setFromMouse(UIContext ctx) {
		float innerX = gx + padding.left;
		float innerW = gw - padding.horizontal();
		if (innerW <= 1)
			return;

		float mx = ctx.mouseX;
		float v = (mx - innerX) / innerW;
		float nv = clamp01(v);

		if (nv != value) {
			value = nv;
			if (onChanged != null)
				onChanged.run();
		}
	}

	private boolean anyMouseDown(UIContext ctx) {
		// InputManager supports up to 8; button indices are AWT getButton() values
		// (1..3 typically).
		for (int b = 0; b < 8; b++) {
			if (ctx.input.isMouseDown(b))
				return true;
		}
		return false;
	}

	private static float clamp01(float v) {
		if (v < 0f)
			return 0f;
		if (v > 1f)
			return 1f;
		return v;
	}
}
