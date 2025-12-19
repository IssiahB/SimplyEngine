package org.jarzarr.ui.layout;

import java.awt.Graphics2D;

import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

/**
 * Flow layout container that places children left-to-right and wraps to new
 * lines.
 *
 * <p>
 * Child sizes come from:
 * </p>
 * <ul>
 * <li>explicit width/height if > 0</li>
 * <li>else preferred size if > 0</li>
 * <li>else 0</li>
 * </ul>
 */
public final class WrapPanel extends UINode {

	/** Horizontal gap between children (pixels). */
	private float gapX = 10;

	/** Vertical gap between lines (pixels). */
	private float gapY = 10;

	public WrapPanel gaps(float gx, float gy) {
		this.gapX = gx;
		this.gapY = gy;
		markLayoutDirty();
		return this;
	}

	@Override
	protected void onLayout(UIContext ctx) {
		float innerX = padding.left;
		float innerY = padding.top;
		float innerW = Math.max(0, gw - padding.left - padding.right);

		float cursorX = innerX;
		float cursorY = innerY;
		float lineH = 0;

		for (UINode c : children) {
			if (c == null || !c.visible)
				continue;

			float cw = childW(c);
			float ch = childH(c);

			// Wrap to next line when current line would overflow.
			if (cursorX > innerX && (cursorX + cw) > (innerX + innerW)) {
				cursorX = innerX;
				cursorY += lineH + gapY;
				lineH = 0;
			}

			c.x = cursorX;
			c.y = cursorY;
			c.width = cw;
			c.height = ch;

			cursorX += cw + gapX;
			if (ch > lineH)
				lineH = ch;
		}
	}

	private float childW(UINode c) {
		if (c.width > 0)
			return c.width;
		if (c.prefW > 0)
			return c.prefW;
		return 0;
	}

	private float childH(UINode c) {
		if (c.height > 0)
			return c.height;
		if (c.prefH > 0)
			return c.prefH;
		return 0;
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
	}

	@Override
	protected boolean isInteractive() {
		return false;
	}
}
