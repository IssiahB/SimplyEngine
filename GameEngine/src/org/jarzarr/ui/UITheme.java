package org.jarzarr.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Simple theme container for UI rendering.
 *
 * <p>
 * Stores fonts and common colors used by UI widgets.
 * </p>
 *
 * <p>
 * This is intentionally a plain data object so games can mutate the theme at
 * runtime without boilerplate.
 * </p>
 */
public final class UITheme {

	// Fonts
	/** Default font for normal UI text. */
	public Font font;

	/** Smaller font for secondary text. */
	public Font fontSmall;

	/** Larger font for headers. */
	public Font fontLarge;

	// Common colors
	/** Primary text color. */
	public Color text = Color.WHITE;

	/** Muted/secondary text color. */
	public Color mutedText = new Color(220, 220, 220, 200);

	// Panel / overlay
	/** Screen overlay tint (used for pause/menu background dim). */
	public Color overlay = new Color(0, 0, 0, 170);

	/** Panel background fill color. */
	public Color panelBg = new Color(20, 20, 20, 220);

	/** Panel border color. */
	public Color panelBorder = new Color(255, 255, 255, 40);

	// Button
	public Color buttonNormal = new Color(40, 40, 40, 220);
	public Color buttonHover = new Color(60, 60, 60, 220);
	public Color buttonPressed = new Color(25, 25, 25, 220);
	public Color buttonDisabled = new Color(30, 30, 30, 160);
	public Color buttonBorder = new Color(255, 255, 255, 40);

	// TextField
	public Color fieldBg = new Color(30, 30, 30, 220);
	public Color fieldBorder = new Color(255, 255, 255, 50);
	public Color caret = new Color(255, 255, 255, 220);

	/**
	 * @return a default theme instance
	 */
	public static UITheme defaults() {
		return new UITheme();
	}
}
