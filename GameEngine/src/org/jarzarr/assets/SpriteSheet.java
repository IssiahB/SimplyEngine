package org.jarzarr.assets;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for slicing sprites out of a sprite sheet.
 *
 * <p>
 * Supports extracting sprites by pixel rectangle or by grid coordinates, with a
 * simple cache to avoid repeatedly calling
 * {@link BufferedImage#getSubimage(int, int, int, int)}.
 * </p>
 *
 * <p>
 * Note: Subimages share the underlying raster with the sheet in most cases, so
 * this is memory-efficient but the sheet must remain alive.
 * </p>
 */
public class SpriteSheet {

	/** Full sprite sheet image. */
	private final BufferedImage sheet;

	/** Tile width for grid-based slicing. */
	private final int tileW;

	/** Tile height for grid-based slicing. */
	private final int tileH;

	/**
	 * Cache for extracted sprites. Key format: "x,y,w,h" in pixels.
	 */
	private final Map<String, BufferedImage> cache = new HashMap<>();

	/**
	 * Creates a sprite sheet wrapper.
	 *
	 * @param sheet underlying sheet image
	 * @param tileW tile width for grid slicing
	 * @param tileH tile height for grid slicing
	 */
	public SpriteSheet(BufferedImage sheet, int tileW, int tileH) {
		this.sheet = sheet;
		this.tileW = tileW;
		this.tileH = tileH;
	}

	/**
	 * Extracts a sprite using pixel coordinates. Results are cached by rectangle to
	 * avoid repeated subimage calls.
	 *
	 * @param x left pixel
	 * @param y top pixel
	 * @param w width in pixels
	 * @param h height in pixels
	 * @return extracted sprite subimage
	 */
	public BufferedImage spritePx(int x, int y, int w, int h) {
		String key = x + "," + y + "," + w + "," + h;
		BufferedImage img = cache.get(key);
		if (img != null)
			return img;

		img = sheet.getSubimage(x, y, w, h);
		cache.put(key, img);
		return img;
	}

	/**
	 * Extracts a sprite using grid coordinates (col,row) and the configured tile
	 * size.
	 *
	 * @param col column index (0-based)
	 * @param row row index (0-based)
	 * @return extracted sprite subimage
	 */
	public BufferedImage spriteGrid(int col, int row) {
		int x = col * tileW;
		int y = row * tileH;
		return spritePx(x, y, tileW, tileH);
	}

	/**
	 * Extracts a sprite by linear index assuming a fixed number of columns.
	 *
	 * @param index   linear tile index (0-based)
	 * @param columns number of columns in the sheet grid
	 * @return extracted sprite subimage
	 */
	public BufferedImage spriteIndex(int index, int columns) {
		int col = index % columns;
		int row = index / columns;
		return spriteGrid(col, row);
	}

	/** @return sheet width in pixels */
	public int getSheetWidth() {
		return sheet.getWidth();
	}

	/** @return sheet height in pixels */
	public int getSheetHeight() {
		return sheet.getHeight();
	}

	/** @return tile width in pixels */
	public int getTileW() {
		return tileW;
	}

	/** @return tile height in pixels */
	public int getTileH() {
		return tileH;
	}
}
