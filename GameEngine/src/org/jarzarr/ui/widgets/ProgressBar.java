package org.jarzarr.ui.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import org.jarzarr.ui.Insets;
import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

/**
 * Non-interactive progress bar for a normalized value in [0..1].
 *
 * <p>
 * Renders a label + percentage text on the top line and a filled bar beneath.
 * </p>
 */
public final class ProgressBar extends UINode {

	private String label = "Progress";
	private float value = 0f; // 0..1

	private Font font;

	public ProgressBar(String label, float initial01) {
		this.label = label;
		this.value = clamp01(initial01);

		this.prefW = 420;
		this.prefH = 52;
		this.padding = Insets.hv(10, 10);
	}

	public ProgressBar label(String s) {
		this.label = s;
		return this;
	}

	public ProgressBar font(Font f) {
		this.font = f;
		return this;
	}

	public float value() {
		return value;
	}

	public ProgressBar value(float v) {
		this.value = clamp01(v);
		return this;
	}

	@Override
	protected boolean isInteractive() {
		return false;
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
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

		int barY = innerY + fm.getHeight() + 8;
		int barH = Math.max(10, innerH - (fm.getHeight() + 10));
		int barW = innerW;

		g.setColor(new Color(255, 255, 255, 30));
		g.fillRect(innerX, barY, barW, barH);

		int fillW = (int) (barW * value);
		g.setColor(new Color(255, 255, 255, 90));
		g.fillRect(innerX, barY, fillW, barH);

		g.setColor(new Color(255, 255, 255, 40));
		g.drawRect(innerX, barY, Math.max(0, barW - 1), Math.max(0, barH - 1));

		String pct = (int) (value * 100) + "%";
		int pctW = fm.stringWidth(pct);
		g.setColor(ctx.theme.mutedText);
		g.drawString(pct, innerX + innerW - pctW, labelY);
	}

	private static float clamp01(float v) {
		if (v < 0f)
			return 0f;
		if (v > 1f)
			return 1f;
		return v;
	}
}
