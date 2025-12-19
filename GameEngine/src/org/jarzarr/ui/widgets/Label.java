package org.jarzarr.ui.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import org.jarzarr.ui.Insets;
import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

/**
 * Non-interactive text label with optional autosizing and alignment.
 *
 * <p>
 * Autosizing:
 * </p>
 * <ul>
 * <li>If {@code autoSize=true}, the label measures text and sets
 * {@code prefW/prefH} (unless the user explicitly provided width/height/pref
 * sizes).</li>
 * </ul>
 *
 * <p>
 * Alignment is applied within the label's inner content rect (after padding).
 * </p>
 */
public final class Label extends UINode {

	public enum AlignH {
		LEFT, CENTER, RIGHT
	}

	public enum AlignV {
		TOP, CENTER, BOTTOM
	}

	private String text;
	private Font font;
	private Color color;

	private AlignH alignH = AlignH.LEFT;
	private AlignV alignV = AlignV.TOP;

	private boolean autoSize = true;

	// Cached measurements to avoid re-measuring every layout.
	private String lastText = null;
	private Font lastFont = null;
	private float measuredW = 0;
	private float measuredH = 0;

	// Shared render context for string bounds (no AA, no fractional metrics)
	private static final FontRenderContext FRC = new FontRenderContext(null, false, false);

	public Label(String text) {
		this.text = text;
		this.padding = Insets.ZERO;
	}

	public Label text(String t) {
		this.text = t;
		markLayoutDirty();
		return this;
	}

	public Label font(Font f) {
		this.font = f;
		markLayoutDirty();
		return this;
	}

	public Label color(Color c) {
		this.color = c;
		return this;
	}

	public Label align(AlignH h, AlignV v) {
		this.alignH = h;
		this.alignV = v;
		return this;
	}

	public Label autoSize(boolean v) {
		this.autoSize = v;
		markLayoutDirty();
		return this;
	}

	@Override
	protected void onMeasure(UIContext ctx) {
		if (!autoSize)
			return;

		Font f = (font != null) ? font : (ctx.defaultFont != null ? ctx.defaultFont : ctx.theme.font);
		if (f == null)
			return;

		String t = (text != null) ? text : "";

		// Cache hit: no need to re-measure.
		if (t.equals(lastText) && f.equals(lastFont))
			return;

		Rectangle2D bounds = f.getStringBounds(t, FRC);
		measuredW = (float) Math.ceil(bounds.getWidth());
		measuredH = (float) Math.ceil(bounds.getHeight());

		lastText = t;
		lastFont = f;

		// Only set pref sizes if user hasn't explicitly set width/height/pref.
		if (this.width <= 0 && this.prefW <= 0)
			this.prefW = measuredW + padding.horizontal();
		if (this.height <= 0 && this.prefH <= 0)
			this.prefH = measuredH + padding.vertical();
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
		if (text == null || text.isEmpty())
			return;

		Font f = (font != null) ? font : (ctx.defaultFont != null ? ctx.defaultFont : ctx.theme.font);
		if (f != null)
			g.setFont(f);

		Color c = (color != null) ? color : ctx.theme.text;
		g.setColor(c);

		var fm = g.getFontMetrics();
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
