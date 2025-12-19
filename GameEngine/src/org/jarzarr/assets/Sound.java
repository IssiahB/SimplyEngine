package org.jarzarr.assets;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Thin wrapper around a {@link Clip} to provide simple sound playback control.
 *
 * <p>
 * Designed for short sound effects loaded fully into memory (WAV/PCM is ideal).
 * For long music tracks, a streaming approach would be more appropriate.
 * </p>
 */
public final class Sound {

	/** Backing audio clip (preloaded audio data). */
	private final Clip clip;

	/**
	 * Wraps an already-loaded clip.
	 *
	 * @param clip audio clip (must be opened/ready)
	 */
	public Sound(Clip clip) {
		this.clip = clip;
	}

	/**
	 * Plays the sound from the beginning. If already playing, it stops and restarts
	 * (common SFX behavior).
	 */
	public void play() {
		if (clip.isRunning())
			clip.stop();
		clip.setFramePosition(0);
		clip.start();
	}

	/**
	 * Loops the sound continuously. The clip will keep playing until
	 * {@link #stop()} is called.
	 */
	public void loop() {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	/**
	 * Stops playback and rewinds to the start.
	 */
	public void stop() {
		clip.stop();
		clip.setFramePosition(0);
	}

	/**
	 * Sets volume using decibels (MASTER_GAIN) if supported by the clip.
	 *
	 * <p>
	 * Typical values:
	 * </p>
	 * <ul>
	 * <li>0.0f = normal</li>
	 * <li>-10.0f = quieter</li>
	 * <li>-20.0f = much quieter</li>
	 * </ul>
	 *
	 * @param db gain in decibels
	 */
	public void setVolumeDb(float db) {
		if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(db);
		}
	}

	/**
	 * Releases native audio resources owned by the clip. After calling dispose, the
	 * Sound should not be used.
	 */
	public void dispose() {
		clip.close();
	}
}
