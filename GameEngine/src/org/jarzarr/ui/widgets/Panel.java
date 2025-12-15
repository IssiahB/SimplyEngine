package org.jarzarr.ui.widgets;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

public class Panel extends UINode {
	private Color bg;
	private Color border;
	private float borderWidth = 1f;

	public Panel background(Color c) {
		bg = c;
		return this;
	}

	public Panel border(Color c, float w) {
		border = c;
		borderWidth = w;
		return this;
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
		if (bg != null) {
			g.setColor(bg);
			g.fillRect((int) gx, (int) gy, (int) gw, (int) gh);
		}
		if (border != null && borderWidth > 0) {
			g.setColor(border);
			g.drawRect((int) gx, (int) gy, (int) gw, (int) gh);
		}
	}

	@Override
	protected boolean isInteractive() {
		return false;
	}
}
