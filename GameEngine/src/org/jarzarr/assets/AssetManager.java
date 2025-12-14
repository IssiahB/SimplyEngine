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

public class AssetManager {

	private final Map<String, BufferedImage> images = new HashMap<>();
	private final Map<String, Font> baseFonts = new HashMap<>();
	private final Map<String, Clip> clips = new HashMap<>();
	private final Map<String, Sound> sounds = new HashMap<>();

	// -------- Images --------

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
	 * Loads a base font (TTF/OTF) and caches it. Use deriveFont(size) for sizes.
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

	public Font getFont(String path, float size) {
		return getBaseFont(path).deriveFont(size);
	}

	// -------- Audio (Clip) --------

	/**
	 * Loads a Clip and caches it. Best results with WAV PCM (common baseline).
	 */
	public Clip getClip(String path) {
		path = Resources.normalize(path);
		Clip clip = clips.get(path);
		if (clip != null)
			return clip;

		try (InputStream raw = Resources.stream(path); AudioInputStream ais = AudioSystem.getAudioInputStream(raw)) {

			AudioFormat baseFormat = ais.getFormat();

			// Convert to a common format many mixers support (PCM signed)
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

	public Sound getSound(String path) {
		path = Resources.normalize(path);
		Sound s = sounds.get(path);
		if (s != null)
			return s;
		s = new Sound(getClip(path)); // reuse existing getClip()
		sounds.put(path, s);
		return s;
	}

	public void dispose() {
		// Close clips so the audio lines are released
		for (Clip c : clips.values()) {
			try {
				c.close();
			} catch (Exception ignored) {
			}
		}
		
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
