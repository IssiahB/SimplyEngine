package org.jarzarr.assets;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Central asset cache/loader for the engine.
 *
 * <p>
 * Provides cached loading for:
 * </p>
 * <ul>
 * <li>Images (BufferedImage)</li>
 * <li>Fonts (base font + derived sizes)</li>
 * <li>Audio clips (Clip) and simple Sound wrappers</li>
 * </ul>
 *
 * <p>
 * Assets are loaded from the classpath using {@link Resources} and cached by
 * normalized path strings.
 * </p>
 *
 * <p>
 * Call {@link #dispose()} during engine shutdown to release audio resources and
 * clear caches.
 * </p>
 */
public class AssetManager {

	/** Cached images by normalized resource path. */
	private final Map<String, BufferedImage> images = new HashMap<>();

	/** Cached base fonts by normalized resource path (derive sizes from these). */
	private final Map<String, Font> baseFonts = new HashMap<>();

	/** Cached audio clips by normalized resource path. */
	private final Map<String, Clip> clips = new HashMap<>();

	/** Cached Sound wrappers by normalized resource path. */
	private final Map<String, Sound> sounds = new HashMap<>();

	// -------- Images --------

	/**
	 * Loads an image from the classpath and caches it.
	 *
	 * @param path classpath resource path
	 * @return loaded image
	 * @throws RuntimeException if loading fails
	 */
	public BufferedImage getImage(String path) {
		path = Resources.normalize(path);
		BufferedImage img = images.get(path);
		if (img != null)
			return img;

		try (InputStream in = Resources.stream(path)) {
			img = ImageIO.read(in);
			if (img == null)
				throw new IllegalArgumentException("Unsupported image format: " + path);
			images.put(path, img);
			return img;
		} catch (IOException e) {
			throw new RuntimeException("Failed to load image: " + path, e);
		}
	}

	// -------- Fonts --------

	/**
	 * Loads a base font (TTF/OTF) and caches it. Use {@link Font#deriveFont(float)}
	 * (or {@link #getFont(String, float)}) to get sized variants.
	 *
	 * @param path classpath font resource path
	 * @return cached base font
	 * @throws RuntimeException if loading fails
	 */
	public Font getBaseFont(String path) {
		path = Resources.normalize(path);
		Font f = baseFonts.get(path);
		if (f != null)
			return f;

		try (InputStream in = Resources.stream(path)) {
			f = Font.createFont(Font.TRUETYPE_FONT, in);
			baseFonts.put(path, f);
			return f;
		} catch (FontFormatException | IOException e) {
			throw new RuntimeException("Failed to load font: " + path, e);
		}
	}

	/**
	 * Loads a base font (cached) and returns a derived sized font.
	 *
	 * @param path classpath font resource path
	 * @param size font size in points
	 * @return derived font instance
	 */
	public Font getFont(String path, float size) {
		return getBaseFont(path).deriveFont(size);
	}

	// -------- Audio (Clip) --------

	/**
	 * Loads an audio clip and caches it.
	 *
	 * <p>
	 * This loader decodes audio into a common PCM_SIGNED format (16-bit), which
	 * improves compatibility across mixers.
	 * </p>
	 *
	 * <p>
	 * Best results with WAV PCM as a baseline.
	 * </p>
	 *
	 * @param path classpath audio resource path
	 * @return loaded clip (opened and ready)
	 * @throws RuntimeException if loading fails
	 */
	public Clip getClip(String path) {
		path = Resources.normalize(path);
		Clip clip = clips.get(path);
		if (clip != null)
			return clip;

		try (InputStream raw = Resources.stream(path); AudioInputStream ais = AudioSystem.getAudioInputStream(raw)) {

			AudioFormat baseFormat = ais.getFormat();

			// Convert to a common format many mixers support (PCM signed, 16-bit).
			AudioFormat decoded = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
					baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

			try (AudioInputStream dais = AudioSystem.getAudioInputStream(decoded, ais)) {
				clip = AudioSystem.getClip();
				clip.open(dais);
				clips.put(path, clip);
				return clip;
			}

		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			throw new RuntimeException("Failed to load audio clip: " + path, e);
		}
	}

	/**
	 * Returns a cached {@link Sound} wrapper for the given audio resource.
	 * Internally reuses {@link #getClip(String)} for clip caching.
	 *
	 * @param path classpath audio resource path
	 * @return cached Sound wrapper
	 */
	public Sound getSound(String path) {
		path = Resources.normalize(path);
		Sound s = sounds.get(path);
		if (s != null)
			return s;

		s = new Sound(getClip(path)); // reuse existing getClip()
		sounds.put(path, s);
		return s;
	}

	/**
	 * Disposes all cached assets and releases native resources.
	 *
	 * <p>
	 * Audio clips hold native lines; closing them is important to prevent leaks.
	 * Images/fonts are pure memory objects, but clearing caches helps GC.
	 * </p>
	 */
	public void dispose() {
		// Close clips so the audio lines are released.
		for (Clip c : clips.values()) {
			try {
				c.close();
			} catch (Exception ignored) {
			}
		}

		// Sound.dispose() also closes its clip, but keep it safe if Sound changes
		// later.
		for (Sound s : sounds.values()) {
			try {
				s.dispose();
			} catch (Exception ignored) {
			}
		}

		clips.clear();
		sounds.clear();
		images.clear();
		baseFonts.clear();
	}
}
