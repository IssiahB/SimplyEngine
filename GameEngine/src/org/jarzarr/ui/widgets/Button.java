package org.jarzarr.ui.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.jarzarr.ui.Insets;
import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;
import org.jarzarr.ui.UIRoot;

/**
 * Clickable button with hover/pressed/focus visuals.
 *
 * <p>
 * Events:
 * </p>
 * <ul>
 * <li>UIRoot sets hovered/pressed/focused states and calls Clickable.onClick
 * when mouse-up occurs on the same node.</li>
 * </ul>
 *
 * <p>
 * Note: This button returns true from mouse down/up to consume input.
 * </p>
 */
public final class Button extends UINode implements UIRoot.Clickable {

	private final String text;
	private Font font;
	private Runnable onClick;

	// Optional overrides (if null -> theme)
	private Color bgNormal, bgHover, bgPressed, bgDisabled;
	private Color border;
	private Color textColor;

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

	public Button colors(Color normal, Color hover, Color pressed, Color disabled) {
		this.bgNormal = normal;
		this.bgHover = hover;
		this.bgPressed = pressed;
		this.bgDisabled = disabled;
		return this;
	}

	public Button border(Color c) {
		this.border = c;
		return this;
	}

	public Button textColor(Color c) {
		this.textColor = c;
		return this;
	}

	@Override
	protected boolean isInteractive() {
		return true;
	}

	@Override
	protected boolean isFocusable() {
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
		Color normal = (bgNormal != null) ? bgNormal : ctx.theme.buttonNormal;
		Color hoverC = (bgHover != null) ? bgHover : ctx.theme.buttonHover;
		Color pressedC = (bgPressed != null) ? bgPressed : ctx.theme.buttonPressed;
		Color disabledC = (bgDisabled != null) ? bgDisabled : ctx.theme.buttonDisabled;
		Color borderC = (border != null) ? border : ctx.theme.buttonBorder;
		Color textC = (textColor != null) ? textColor : ctx.theme.text;

		Color bg;
		if (!enabled)
			bg = disabledC;
		else if (pressed)
			bg = pressedC;
		else if (hovered)
			bg = hoverC;
		else
			bg = normal;

		int x = (int) gx, y = (int) gy, w = (int) gw, h = (int) gh;

		g.setColor(bg);
		g.fillRect(x, y, w, h);

		// Fix: draw border inside bounds.
		g.setColor(borderC);
		g.drawRect(x, y, Math.max(0, w - 1), Math.max(0, h - 1));

		// Focus outline
		if (focused) {
			g.setColor(new Color(255, 255, 255, 90));
			g.drawRect(x + 2, y + 2, Math.max(0, w - 5), Math.max(0, h - 5));
		}

		Font f = (font != null) ? font : (ctx.defaultFont != null ? ctx.defaultFont : ctx.theme.font);
		if (f != null)
			g.setFont(f);

		var fm = g.getFontMetrics();
		int tw = fm.stringWidth(text);
		int tx = (int) (gx + (gw - tw) * 0.5f);
		int ty = (int) (gy + (gh - fm.getHeight()) * 0.5f + fm.getAscent());

		g.setColor(textC);
		g.drawString(text, tx, ty);
	}
}
