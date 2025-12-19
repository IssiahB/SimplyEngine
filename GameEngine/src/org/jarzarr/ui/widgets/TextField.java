package org.jarzarr.ui.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;

import org.jarzarr.ui.Insets;
import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UIRoot;
import org.jarzarr.ui.UINode;

/**
 * Single-line text input field.
 *
 * <p>
 * Supports:
 * </p>
 * <ul>
 * <li>Typing via {@link #onTextInput(UIContext, String)} (fed by InputManager
 * keyTyped buffer)</li>
 * <li>Navigation keys (Left/Right/Home/End)</li>
 * <li>Editing keys (Backspace/Delete)</li>
 * <li>Click to place caret</li>
 * <li>Horizontal scrolling so caret remains visible</li>
 * </ul>
 *
 * <p>
 * Notes:
 * </p>
 * <ul>
 * <li>This implementation assumes monoline text; no selection yet.</li>
 * <li>FontMetrics during update() uses Toolkit metrics as a fallback (can be
 * slightly off compared to the actual Graphics2D metrics used in render).</li>
 * </ul>
 */
public final class TextField extends UINode implements UIRoot.Clickable {

	private final StringBuilder value = new StringBuilder();
	private Font font;

	/** Caret index in [0..value.length()]. */
	private int caret = 0;

	/**
	 * Horizontal scroll offset in pixels (text is drawn shifted left by this
	 * amount).
	 */
	private int scrollX = 0;

	// Caret blink
	private double blink = 0;
	private boolean caretOn = false;

	/** Optional placeholder (shown when empty and not focused). */
	private String placeholder = "";

	public TextField() {
		this.prefW = 320;
		this.prefH = 44;
		this.padding = Insets.hv(10, 10);
	}

	public TextField font(Font f) {
		this.font = f;
		return this;
	}

	public TextField placeholder(String p) {
		this.placeholder = (p != null ? p : "");
		return this;
	}

	public String getText() {
		return value.toString();
	}

	public void setText(String s) {
		value.setLength(0);
		if (s != null)
			value.append(s);
		caret = Math.min(caret, value.length());
		scrollX = 0;
		markLayoutDirty();
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
	public void onClick(UIContext ctx, int button) {
		// Focus is handled by UIRoot; no-op here.
	}

	@Override
	public boolean onMouseDown(UIContext ctx, int button) {
		placeCaretFromMouse(ctx);
		ensureCaretVisible(ctx);
		blink = 0;
		caretOn = true;
		return true;
	}

	@Override
	public boolean onKeyDown(UIContext ctx, int keyCode) {
		if (!focused)
			return false;

		switch (keyCode) {
		case KeyEvent.VK_BACK_SPACE -> {
			if (caret > 0 && value.length() > 0) {
				value.deleteCharAt(caret - 1);
				caret--;
				ensureCaretVisible(ctx);
			}
			return true;
		}
		case KeyEvent.VK_DELETE -> {
			if (caret < value.length()) {
				value.deleteCharAt(caret);
				ensureCaretVisible(ctx);
			}
			return true;
		}
		case KeyEvent.VK_LEFT -> {
			caret = Math.max(0, caret - 1);
			ensureCaretVisible(ctx);
			return true;
		}
		case KeyEvent.VK_RIGHT -> {
			caret = Math.min(value.length(), caret + 1);
			ensureCaretVisible(ctx);
			return true;
		}
		case KeyEvent.VK_HOME -> {
			caret = 0;
			ensureCaretVisible(ctx);
			return true;
		}
		case KeyEvent.VK_END -> {
			caret = value.length();
			ensureCaretVisible(ctx);
			return true;
		}
		}
		return false;
	}

	@Override
	public boolean onTextInput(UIContext ctx, String text) {
		if (!focused || text == null || text.isEmpty())
			return false;

		value.insert(caret, text);
		caret += text.length();
		ensureCaretVisible(ctx);
		return true;
	}

	@Override
	public void update(UIContext ctx) {
		super.update(ctx);

		if (focused) {
			blink += ctx.dt;
			if (blink >= 0.5) { // toggle 2 Hz
				blink = 0;
				caretOn = !caretOn;
			}
			ensureCaretVisible(ctx);
		} else {
			caretOn = false;
			blink = 0;
			// Keep scroll clamped if resized while unfocused.
			clampScroll(ctx);
		}
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
		Color bg = ctx.theme.fieldBg;
		Color border = ctx.theme.fieldBorder;
		Color textC = ctx.theme.text;
		Color caretC = ctx.theme.caret;

		int x = (int) gx, y = (int) gy, w = (int) gw, h = (int) gh;
		if (w <= 0 || h <= 0)
			return;

		// Background + border
		g.setColor(bg);
		g.fillRect(x, y, w, h);

		g.setColor(border);
		g.drawRect(x, y, Math.max(0, w - 1), Math.max(0, h - 1));

		// Focus outline
		if (focused) {
			g.setColor(new Color(255, 255, 255, 90));
			g.drawRect(x + 2, y + 2, Math.max(0, w - 5), Math.max(0, h - 5));
		}

		// Font metrics
		Font f = (font != null) ? font : (ctx.defaultFont != null ? ctx.defaultFont : ctx.theme.font);
		if (f != null)
			g.setFont(f);
		FontMetrics fm = g.getFontMetrics();

		// Inner content rect
		int innerX = (int) (gx + padding.left);
		int innerY = (int) (gy + padding.top);
		int innerW = (int) (gw - padding.horizontal());
		int innerH = (int) (gh - padding.vertical());

		// Clip to inner rect so text never draws outside.
		Shape oldClip = g.getClip();
		g.setClip(innerX, innerY, Math.max(0, innerW), Math.max(0, innerH));

		// Baseline centered vertically in inner rect.
		int textBaseY = innerY + (innerH - fm.getHeight()) / 2 + fm.getAscent();

		int textX = innerX - scrollX;

		String draw = value.toString();
		boolean showPlaceholder = draw.isEmpty() && !focused && placeholder != null && !placeholder.isEmpty();

		if (showPlaceholder) {
			g.setColor(ctx.theme.mutedText);
			g.drawString(placeholder, textX, textBaseY);
		} else {
			g.setColor(textC);
			g.drawString(draw, textX, textBaseY);
		}

		// Caret
		if (focused && caretOn && !showPlaceholder) {
			int safeCaret = Math.min(caret, draw.length());
			int caretPx = fm.stringWidth(draw.substring(0, safeCaret));
			int cx = textX + caretPx;

			int cy1 = innerY + 2;
			int cy2 = innerY + innerH - 2;

			g.setColor(caretC);
			g.drawLine(cx, cy1, cx, cy2);
		}

		g.setClip(oldClip);
	}

	// -----------------------------
	// Scrolling + caret placement
	// -----------------------------

	/**
	 * Adjusts {@link #scrollX} so the caret stays within the visible inner width.
	 */
	private void ensureCaretVisible(UIContext ctx) {
		Font f = (font != null) ? font : (ctx.defaultFont != null ? ctx.defaultFont : ctx.theme.font);
		if (f == null)
			return;

		int innerW = (int) (gw - padding.horizontal());
		if (innerW <= 0) {
			scrollX = 0;
			return;
		}

		if (value.length() == 0) {
			scrollX = 0;
			return;
		}

		FontMetrics fm = getFontMetricsFor(f);

		String s = value.toString();
		int safeCaret = Math.min(caret, s.length());
		int caretPx = fm.stringWidth(s.substring(0, safeCaret));
		int textWidth = fm.stringWidth(s);

		int leftVisible = scrollX;
		int rightVisible = scrollX + innerW;

		int margin = 6;

		if (caretPx < leftVisible + margin) {
			scrollX = Math.max(0, caretPx - margin);
		} else if (caretPx > rightVisible - margin) {
			scrollX = caretPx - innerW + margin;
		}

		int maxScroll = Math.max(0, textWidth - innerW);
		if (scrollX > maxScroll)
			scrollX = maxScroll;
		if (scrollX < 0)
			scrollX = 0;
	}

	/**
	 * Clamps scroll to a valid range without trying to follow the caret. Useful
	 * when unfocused but resized.
	 */
	private void clampScroll(UIContext ctx) {
		Font f = (font != null) ? font : (ctx.defaultFont != null ? ctx.defaultFont : ctx.theme.font);
		if (f == null) {
			scrollX = 0;
			return;
		}

		int innerW = (int) (gw - padding.horizontal());
		if (innerW <= 0) {
			scrollX = 0;
			return;
		}

		FontMetrics fm = getFontMetricsFor(f);
		int textWidth = fm.stringWidth(value.toString());
		int maxScroll = Math.max(0, textWidth - innerW);

		if (scrollX > maxScroll)
			scrollX = maxScroll;
		if (scrollX < 0)
			scrollX = 0;
	}

	/**
	 * Computes caret index by converting mouse x position into "text space"
	 * (accounts for horizontal scroll).
	 */
	private void placeCaretFromMouse(UIContext ctx) {
		Font f = (font != null) ? font : (ctx.defaultFont != null ? ctx.defaultFont : ctx.theme.font);
		if (f == null)
			return;

		if (value.length() == 0) {
			caret = 0;
			return;
		}

		FontMetrics fm = getFontMetricsFor(f);

		float innerX = gx + padding.left;

		float mx = ctx.mouseX;
		int localX = (int) Math.floor((mx - innerX) + scrollX);

		String s = value.toString();

		int acc = 0;
		for (int i = 0; i < s.length(); i++) {
			int w = fm.charWidth(s.charAt(i));
			int mid = acc + (w / 2);
			if (localX < mid) {
				caret = i;
				return;
			}
			acc += w;
		}

		caret = s.length();
	}

	/**
	 * Fallback FontMetrics provider used outside render().
	 *
	 * <p>
	 * Limitation: Toolkit metrics may differ slightly from the actual Graphics2D
	 * metrics (AA, rendering hints, transforms). If you want perfect caret
	 * placement at all times, pass a Graphics2D (or FontRenderContext) through
	 * UIContext each frame.
	 * </p>
	 */
	private static FontMetrics getFontMetricsFor(Font f) {
		return java.awt.Toolkit.getDefaultToolkit().getFontMetrics(f);
	}
}
