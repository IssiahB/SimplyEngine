package org.jarzarr.ui.widgets;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

/**
 * Simple rectangular panel that can draw a background fill and optional border.
 *
 * <p>
 * This is a non-interactive node typically used as a container visual (cards,
 * overlays, menu panels). It does not modify layout; it only renders.
 * </p>
 */
public class Panel extends UINode {

	/** Optional background color (null = no fill). */
	private Color bg;

	/** Optional border color (null = no border). */
	private Color border;

	/** Border width in pixels. (Currently rendered as a single stroke outline.) */
	private float borderWidth = 1f;

	/**
	 * Sets the background fill color.
	 *
	 * @param c background color (null disables fill)
	 * @return this
	 */
	public Panel background(Color c) {
		bg = c;
		return this;
	}

	/**
	 * Sets the border color and stroke width.
	 *
	 * <p>
	 * Note: AWT's default {@code Graphics2D} stroke is 1px unless you set a custom
	 * Stroke. This method stores the width for future expansion.
	 * </p>
	 *
	 * @param c border color (null disables border)
	 * @param w border width (<=0 disables border)
	 * @return this
	 */
	public Panel border(Color c, float w) {
		border = c;
		borderWidth = w;
		return this;
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
		int x = (int) gx;
		int y = (int) gy;
		int w = (int) gw;
		int h = (int) gh;

		if (w <= 0 || h <= 0)
			return;

		if (bg != null) {
			g.setColor(bg);
			g.fillRect(x, y, w, h);
		}

		// Fix: drawRect should generally use (w-1,h-1) so the border sits inside
		// bounds.
		if (border != null && borderWidth > 0) {
			g.setColor(border);
			g.drawRect(x, y, Math.max(0, w - 1), Math.max(0, h - 1));
		}
	}

	@Override
	protected boolean isInteractive() {
		return false;
	}
}
