package org.jarzarr.ui.layout;

import java.awt.Graphics2D;

import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

/**
 * Container that centers a single child within its content area.
 *
 * <p>
 * The child size uses:
 * </p>
 * <ul>
 * <li>explicit width/height if > 0</li>
 * <li>else preferred size if > 0</li>
 * <li>else 0</li>
 * </ul>
 */
public final class Center extends UINode {

	/** The single centered child (also present in children list). */
	private UINode child;

	/**
	 * Sets the centered child (replaces any existing child).
	 *
	 * @param c child node (nullable)
	 * @return this container
	 */
	public Center set(UINode c) {
		this.children().clear();
		this.child = c;
		if (c != null)
			add(c);
		markLayoutDirty();
		return this;
	}

	@Override
	protected void onLayout(UIContext ctx) {
		if (child == null || !child.visible)
			return;

		float cw = Math.max(0, gw - padding.left - padding.right);
		float ch = Math.max(0, gh - padding.top - padding.bottom);

		float w = (child.width > 0) ? child.width : ((child.prefW > 0) ? child.prefW : 0);
		float h = (child.height > 0) ? child.height : ((child.prefH > 0) ? child.prefH : 0);

		child.width = w;
		child.height = h;
		child.x = padding.left + (cw - w) * 0.5f;
		child.y = padding.top + (ch - h) * 0.5f;
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
	}

	@Override
	protected boolean isInteractive() {
		return false;
	}
}
