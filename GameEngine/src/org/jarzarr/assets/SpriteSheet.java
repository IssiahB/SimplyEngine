package org.jarzarr.assets;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class SpriteSheet {

	private final BufferedImage sheet;
	private final int tileW;
	private final int tileH;

	// Cache for extracted sprites: key = "x,y,w,h"
	private final Map<String, BufferedImage> cache = new HashMap<>();

	public SpriteSheet(BufferedImage sheet, int tileW, int tileH) {
		this.sheet = sheet;
		this.tileW = tileW;
		this.tileH = tileH;
	}

	public BufferedImage spritePx(int x, int y, int w, int h) {
		String key = x + "," + y + "," + w + "," + h;
		BufferedImage img = cache.get(key);
		if (img != null)
			return img;

		img = sheet.getSubimage(x, y, w, h);
		cache.put(key, img);
		return img;
	}

	public BufferedImage spriteGrid(int col, int row) {
		int x = col * tileW;
		int y = row * tileH;
		return spritePx(x, y, tileW, tileH);
	}

	public BufferedImage spriteIndex(int index, int columns) {
		int col = index % columns;
		int row = index / columns;
		return spriteGrid(col, row);
	}

	public int getSheetWidth() {
		return sheet.getWidth();
	}

	public int getSheetHeight() {
		return sheet.getHeight();
	}

	public int getTileW() {
		return tileW;
	}

	public int getTileH() {
		return tileH;
	}
}
