package org.jarzarr.ui.layout;

import java.awt.Graphics2D;

import org.jarzarr.ui.Insets;
import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

/**
 * Simple linear layout container (vertical or horizontal stacking).
 *
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Spacing between children</li>
 * <li>Optional cross-axis fill (stretch width for vertical, height for
 * horizontal)</li>
 * <li>Uniform child margin (applied to every child)</li>
 * </ul>
 *
 * <p>
 * Fix: spacing is now applied only between children (not after the last child).
 * </p>
 */
public final class StackPanel extends UINode {

	public enum Direction {
		VERTICAL, HORIZONTAL
	}

	/** Stack direction. */
	private final Direction dir;

	/** Space between children (pixels). */
	private float spacing = 10f;

	/** If true, children are stretched along the cross axis. */
	private boolean fillCrossAxis = true;

	/** Margin applied around every child (in addition to this panel's padding). */
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
		childMargin = (m != null) ? m : Insets.ZERO;
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
		int placed = 0;

		for (UINode c : children) {
			if (c == null || !c.visible)
				continue;

			float w = (c.width > 0) ? c.width : ((c.prefW > 0) ? c.prefW : 0);
			float h = (c.height > 0) ? c.height : ((c.prefH > 0) ? c.prefH : 0);

			// Apply spacing only between visible children.
			if (placed > 0)
				cursor += spacing;

			if (dir == Direction.VERTICAL) {
				c.x = cx + childMargin.left;
				c.y = cy + cursor + childMargin.top;
				c.width = fillCrossAxis ? Math.max(0, cw - childMargin.horizontal()) : w;
				c.height = h;

				cursor += c.height + childMargin.vertical();
			} else {
				c.x = cx + cursor + childMargin.left;
				c.y = cy + childMargin.top;
				c.width = w;
				c.height = fillCrossAxis ? Math.max(0, ch - childMargin.vertical()) : h;

				cursor += c.width + childMargin.horizontal();
			}

			placed++;
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
