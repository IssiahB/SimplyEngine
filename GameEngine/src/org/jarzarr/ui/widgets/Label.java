package org.jarzarr.ui.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

public final class Label extends UINode {
	public enum AlignH {
		LEFT, CENTER, RIGHT
	}

	public enum AlignV {
		TOP, CENTER, BOTTOM
	}

	private String text;
	private Font font;
	private Color color = Color.WHITE;
	private AlignH alignH = AlignH.LEFT;
	private AlignV alignV = AlignV.TOP;

	public Label(String text) {
		this.text = text;
		this.prefH = 24;
	}

	public Label text(String t) {
		text = t;
		return this;
	}

	public Label font(Font f) {
		font = f;
		return this;
	}

	public Label color(Color c) {
		color = c;
		return this;
	}

	public Label align(AlignH h, AlignV v) {
		alignH = h;
		alignV = v;
		return this;
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
		if (text == null || text.isEmpty())
			return;

		Font f = (font != null) ? font : ctx.defaultFont;
		if (f != null)
			g.setFont(f);

		g.setColor(color);

		FontMetrics fm = g.getFontMetrics();
		int tw = fm.stringWidth(text);
		int th = fm.getHeight();

		float px = gx + padding.left;
		float py = gy + padding.top;
		float pw = gw - padding.horizontal();
		float ph = gh - padding.vertical();

		float tx = px;
		float ty = py + fm.getAscent();

		if (alignH == AlignH.CENTER)
			tx = px + (pw - tw) * 0.5f;
		else if (alignH == AlignH.RIGHT)
			tx = px + (pw - tw);

		if (alignV == AlignV.CENTER)
			ty = py + (ph - th) * 0.5f + fm.getAscent();
		else if (alignV == AlignV.BOTTOM)
			ty = py + (ph - th) + fm.getAscent();

		g.drawString(text, (int) tx, (int) ty);
	}

	@Override
	protected boolean isInteractive() {
		return false;
	}
}
