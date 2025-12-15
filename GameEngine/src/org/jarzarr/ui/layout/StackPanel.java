package org.jarzarr.ui.layout;

import java.awt.Graphics2D;

import org.jarzarr.ui.Insets;
import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

public final class StackPanel extends UINode {
	public enum Direction {
		VERTICAL, HORIZONTAL
	}

	private final Direction dir;
	private float spacing = 10f;
	private boolean fillCrossAxis = true;
	private Insets childMargin = Insets.ZERO;

	public StackPanel(Direction dir) {
		this.dir = dir;
	}

	public StackPanel spacing(float s) {
		spacing = s;
		markLayoutDirty();
		return this;
	}

	public StackPanel fillCrossAxis(boolean v) {
		fillCrossAxis = v;
		markLayoutDirty();
		return this;
	}

	public StackPanel childMargin(Insets m) {
		childMargin = m;
		markLayoutDirty();
		return this;
	}

	@Override
	protected void onLayout(UIContext ctx) {
		float cx = padding.left;
		float cy = padding.top;
		float cw = Math.max(0, gw - padding.horizontal());
		float ch = Math.max(0, gh - padding.vertical());

		float cursor = 0f;

		for (UINode c : children) {
			float w = (c.width > 0) ? c.width : ((c.prefW > 0) ? c.prefW : 0);
			float h = (c.height > 0) ? c.height : ((c.prefH > 0) ? c.prefH : 0);

			if (dir == Direction.VERTICAL) {
				c.x = cx + childMargin.left;
				c.y = cy + cursor + childMargin.top;
				c.width = fillCrossAxis ? (cw - childMargin.horizontal()) : w;
				c.height = h;

				cursor += c.height + spacing + childMargin.vertical();
			} else {
				c.x = cx + cursor + childMargin.left;
				c.y = cy + childMargin.top;
				c.width = w;
				c.height = fillCrossAxis ? (ch - childMargin.vertical()) : h;

				cursor += c.width + spacing + childMargin.horizontal();
			}
		}
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
	}

	@Override
	protected boolean isInteractive() {
		return false;
	}
}
