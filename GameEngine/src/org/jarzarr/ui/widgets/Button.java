package org.jarzarr.ui.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.jarzarr.ui.Insets;
import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UIRoot;
import org.jarzarr.ui.UINode;

public final class Button extends UINode implements UIRoot.Clickable {
	private final String text;
	private Font font;
	private Runnable onClick;

	private Color bgNormal = new Color(40, 40, 40, 220);
	private Color bgHover = new Color(60, 60, 60, 220);
	private Color bgPressed = new Color(25, 25, 25, 220);
	private Color bgDisabled = new Color(30, 30, 30, 160);

	private Color border = new Color(255, 255, 255, 40);
	private Color textColor = Color.WHITE;

	public Button(String text) {
		this.text = text;
		this.prefW = 260;
		this.prefH = 48;
		this.padding = Insets.hv(12, 10);
	}

	public Button font(Font f) {
		font = f;
		return this;
	}

	public Button onClick(Runnable r) {
		onClick = r;
		return this;
	}

	@Override
	protected boolean isInteractive() {
		return true;
	}

	@Override
	public boolean onMouseDown(UIContext ctx, int button) {
		return true;
	}

	@Override
	public boolean onMouseUp(UIContext ctx, int button) {
		return true;
	}

	@Override
	public void onClick(UIContext ctx, int button) {
		if (!enabled)
			return;
		if (onClick != null)
			onClick.run();
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
		Color bg;
		if (!enabled)
			bg = bgDisabled;
		else if (pressed)
			bg = bgPressed;
		else if (hovered)
			bg = bgHover;
		else
			bg = bgNormal;

		g.setColor(bg);
		g.fillRect((int) gx, (int) gy, (int) gw, (int) gh);

		g.setColor(border);
		g.drawRect((int) gx, (int) gy, (int) gw, (int) gh);

		Font f = (font != null) ? font : ctx.defaultFont;
		if (f != null)
			g.setFont(f);

		var fm = g.getFontMetrics();
		int tw = fm.stringWidth(text);
		int tx = (int) (gx + (gw - tw) * 0.5f);
		int ty = (int) (gy + (gh - fm.getHeight()) * 0.5f + fm.getAscent());

		g.setColor(textColor);
		g.drawString(text, tx, ty);
	}
}
