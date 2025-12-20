package org.jarzarr.assets;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * SpriteAtlas maps string names to sub-rects of a single atlas image.
 *
 * Descriptor format (simple text): # comment name x y w h
 *
 * Example: bee_idle_0 0 0 20 20 bee_idle_1 20 0 20 20
 *
 * Coordinates are pixels in the atlas image.
 */
public final class SpriteAtlas {

	public static final class Region {
		public final String name;
		public final int x, y, w, h;

		Region(String name, int x, int y, int w, int h) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
	}

	private final BufferedImage atlasImage;
	private final Map<String, Region> regions = new HashMap<>();
	private final Map<String, BufferedImage> cache = new HashMap<>();

	public SpriteAtlas(BufferedImage atlasImage) {
		this.atlasImage = atlasImage;
	}

	public BufferedImage image() {
		return atlasImage;
	}

	public Region region(String name) {
		return regions.get(name);
	}

	public BufferedImage sprite(String name) {
		if (name == null)
			return null;
		BufferedImage cached = cache.get(name);
		if (cached != null)
			return cached;

		Region r = regions.get(name);
		if (r == null)
			throw new IllegalArgumentException("Atlas region not found: " + name);

		BufferedImage sub = atlasImage.getSubimage(r.x, r.y, r.w, r.h);
		cache.put(name, sub);
		return sub;
	}

	public void add(String name, int x, int y, int w, int h) {
		if (name == null || name.isBlank())
			return;
		regions.put(name, new Region(name.trim(), x, y, w, h));
	}

	public static SpriteAtlas load(BufferedImage atlasImage, String descriptorPath) {
		SpriteAtlas atlas = new SpriteAtlas(atlasImage);
		String path = Resources.normalize(descriptorPath);

		try (InputStream in = Resources.stream(path);
				BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

			String line;
			int ln = 0;
			while ((line = br.readLine()) != null) {
				ln++;
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#"))
					continue;

				String[] parts = line.split("\\s+");
				if (parts.length != 5)
					throw new IllegalArgumentException("Bad atlas line " + ln + ": " + line);

				String name = parts[0];
				int x = Integer.parseInt(parts[1]);
				int y = Integer.parseInt(parts[2]);
				int w = Integer.parseInt(parts[3]);
				int h = Integer.parseInt(parts[4]);

				atlas.add(name, x, y, w, h);
			}

		} catch (IOException e) {
			throw new RuntimeException("Failed to load atlas descriptor: " + path, e);
		}

		return atlas;
	}
}
