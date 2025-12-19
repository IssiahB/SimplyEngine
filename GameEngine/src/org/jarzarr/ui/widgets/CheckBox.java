package org.jarzarr.ui.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import org.jarzarr.ui.Insets;
import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;
import org.jarzarr.ui.UIRoot;

/**
 * Clickable checkbox with label.
 *
 * <p>
 * Supports toggling via mouse click and keyboard (Space/Enter) when focused.
 * </p>
 */
public final class CheckBox extends UINode implements UIRoot.Clickable {

	private String label;
	private boolean checked;

	private Font font;

	private int boxSize = 22;
	private int gap = 10;

	private Color boxBg = new Color(0, 0, 0, 0);
	private Color boxBorder;
	private Color checkColor;
	private Color textColor;

	private Runnable onChanged;

	public CheckBox(String label, boolean initial) {
		this.label = label;
		this.checked = initial;

		this.prefH = 40;
		this.prefW = 320;
		this.padding = Insets.hv(10, 8);
	}

	public CheckBox label(String s) {
		this.label = s;
		markLayoutDirty();
		return this;
	}

	public CheckBox checked(boolean v) {
		this.checked = v;
		return this;
	}

	public boolean isChecked() {
		return checked;
	}

	public CheckBox font(Font f) {
		this.font = f;
		return this;
	}

	public CheckBox onChanged(Runnable r) {
		this.onChanged = r;
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
		checked = !checked;
		if (onChanged != null)
			onChanged.run();
	}

	@Override
	public boolean onKeyDown(UIContext ctx, int keyCode) {
		if (!focused || !enabled)
			return false;

		if (keyCode == java.awt.event.KeyEvent.VK_SPACE || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
			checked = !checked;
			if (onChanged != null)
				onChanged.run();
			return true;
		}
		return false;
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
		Color border = (boxBorder != null) ? boxBorder : ctx.theme.fieldBorder;
		Color tick = (checkColor != null) ? checkColor : ctx.theme.caret;
		Color textC = (textColor != null) ? textColor : ctx.theme.text;

		if (hovered) {
			g.setColor(new Color(255, 255, 255, 14));
			g.fillRect((int) gx, (int) gy, (int) gw, (int) gh);
		}

		if (focused) {
			g.setColor(new Color(255, 255, 255, 90));
			g.drawRect((int) gx + 2, (int) gy + 2, (int) gw - 4, (int) gh - 4);
		}

		int innerX = (int) (gx + padding.left);
		int innerY = (int) (gy + padding.top);
		int innerH = (int) (gh - padding.vertical());

		int boxX = innerX;
		int boxY = innerY + (innerH - boxSize) / 2;

		g.setColor(boxBg);
		g.fillRect(boxX, boxY, boxSize, boxSize);

		g.setColor(border);
		g.drawRect(boxX, boxY, Math.max(0, boxSize - 1), Math.max(0, boxSize - 1));

		if (checked) {
			g.setColor(tick);
			int x1 = boxX + 5;
			int y1 = boxY + boxSize / 2;
			int x2 = boxX + 10;
			int y2 = boxY + boxSize - 6;
			int x3 = boxX + boxSize - 5;
			int y3 = boxY + 6;

			g.drawLine(x1, y1, x2, y2);
			g.drawLine(x2, y2, x3, y3);
		}

		Font f = (font != null) ? font : (ctx.defaultFont != null ? ctx.defaultFont : ctx.theme.font);
		if (f != null)
			g.setFont(f);
		FontMetrics fm = g.getFontMetrics();

		int textX = boxX + boxSize + gap;
		int textY = innerY + (innerH - fm.getHeight()) / 2 + fm.getAscent();

		g.setColor(textC);
		g.drawString(label != null ? label : "", textX, textY);
	}
}
