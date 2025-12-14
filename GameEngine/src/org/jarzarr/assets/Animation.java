package org.jarzarr.assets;

import java.awt.image.BufferedImage;

public class Animation {
	private final BufferedImage[] frames;
	private final double secondsPerFrame;
	private int frameIndex = 0;
	private double timer = 0.0;
	private boolean loop = true;

	public Animation(double fps, BufferedImage... frames) {
		this.frames = frames;
		this.secondsPerFrame = 1.0 / fps;
	}

	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	public void reset() {
		frameIndex = 0;
		timer = 0.0;
	}

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

	public BufferedImage frame() {
		return frames[frameIndex];
	}
}
