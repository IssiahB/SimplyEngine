package org.jarzarr.ui.layout;

import java.awt.Graphics2D;

import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

public final class Center extends UINode {
	private UINode child;

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
		if (child == null)
			return;

		float cw = gw - padding.left - padding.right;
		float ch = gh - padding.top - padding.bottom;

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
