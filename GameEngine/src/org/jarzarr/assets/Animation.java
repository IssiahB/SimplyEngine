package org.jarzarr.assets;

import java.awt.image.BufferedImage;

/**
 * Time-based sprite animation.
 *
 * <p>
 * Animation advances frames based on elapsed time (dt) and a fixed
 * frames-per-second value. Supports optional looping.
 * </p>
 */
public class Animation {

	/** Ordered animation frames. */
	private final BufferedImage[] frames;

	/** Duration of each frame in seconds (computed from FPS). */
	private final double secondsPerFrame;

	/** Current frame index into frames[]. */
	private int frameIndex = 0;

	/** Accumulated time since last frame advance (seconds). */
	private double timer = 0.0;

	/** If true, animation loops when it reaches the end. */
	private boolean loop = true;

	/**
	 * Creates an animation with the given FPS and frames.
	 *
	 * @param fps    frames per second (must be > 0 for meaningful behavior)
	 * @param frames frame images in playback order
	 */
	public Animation(double fps, BufferedImage... frames) {
		this.frames = frames;
		this.secondsPerFrame = 1.0 / fps;
	}

	/**
	 * Sets whether the animation loops when it reaches the final frame.
	 *
	 * @param loop true to loop, false to clamp to last frame
	 */
	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	/**
	 * Resets playback to the first frame and clears internal timing.
	 */
	public void reset() {
		frameIndex = 0;
		timer = 0.0;
	}

	/**
	 * Advances animation time and updates the current frame if enough time has
	 * passed.
	 *
	 * @param dt delta time in seconds
	 */
	public void update(double dt) {
		if (frames.length <= 1)
			return;

		timer += dt;
		while (timer >= secondsPerFrame) {
			timer -= secondsPerFrame;
			frameIndex++;
			if (frameIndex >= frames.length) {
				frameIndex = loop ? 0 : frames.length - 1;
			}
		}
	}

	/**
	 * Convenience builder for a row-based animation from a {@link SpriteSheet}.
	 * Reads tiles (0..cols-1) from a single row and returns an animation.
	 *
	 * @param sheet sprite sheet
	 * @param row   row index (0-based)
	 * @param cols  number of columns/frames to read from that row
	 * @param fps   frames per second
	 * @return constructed row animation
	 */
	public static Animation buildRowAnim(SpriteSheet sheet, int row, int cols, double fps) {
		BufferedImage[] frames = new BufferedImage[cols];
		for (int c = 0; c < cols; c++) {
			frames[c] = sheet.spriteGrid(c, row);
		}
		return new Animation(fps, frames);
	}

	/**
	 * Returns the current frame image (no copy).
	 *
	 * @return current frame
	 */
	public BufferedImage frame() {
		return frames[frameIndex];
	}
}
