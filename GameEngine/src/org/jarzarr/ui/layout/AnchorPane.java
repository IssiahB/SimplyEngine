package org.jarzarr.ui.layout;

import java.awt.Graphics2D;

import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

/**
 * Container that positions children using {@link Anchors}.
 *
 * <p>
 * Children with {@code anchors == null} are left as-is (their x/y/width/height
 * are interpreted normally relative to this container's content area).
 * </p>
 *
 * <p>
 * Fix: child width/height is clamped to >= 0 when stretching to avoid negative
 * sizes.
 * </p>
 */
public final class AnchorPane extends UINode {

	@Override
	protected void onLayout(UIContext ctx) {
		float contentW = Math.max(0, gw - padding.left - padding.right);
		float contentH = Math.max(0, gh - padding.top - padding.bottom);

		for (UINode c : children) {
			if (c == null || !c.visible)
				continue;

			Anchors a = c.anchors;
			if (a == null)
				continue; // use child.x/y/width/height as-is

			// Horizontal
			if (a.left != null && a.right != null) {
				c.x = padding.left + a.left;
				c.width = Math.max(0, contentW - a.left - a.right);
			} else if (a.left != null) {
				c.x = padding.left + a.left;
				// keep explicit width or pref width
			} else if (a.right != null) {
				float w = (c.width > 0) ? c.width : ((c.prefW > 0) ? c.prefW : 0);
				c.width = w;
				c.x = padding.left + (contentW - a.right - w);
			}

			// Vertical
			if (a.top != null && a.bottom != null) {
				c.y = padding.top + a.top;
				c.height = Math.max(0, contentH - a.top - a.bottom);
			} else if (a.top != null) {
				c.y = padding.top + a.top;
			} else if (a.bottom != null) {
				float h = (c.height > 0) ? c.height : ((c.prefH > 0) ? c.prefH : 0);
				c.height = h;
				c.y = padding.top + (contentH - a.bottom - h);
			}
		}
	}

	/** AnchorPane does not render anything by default. */
	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
	}

	/** AnchorPane is a layout-only container. */
	@Override
	protected boolean isInteractive() {
		return false;
	}
}
