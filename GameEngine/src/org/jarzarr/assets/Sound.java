package org.jarzarr.assets;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public final class Sound {
    private final Clip clip;

    public Sound(Clip clip) { this.clip = clip; }

    public void play() {
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public void loop() {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        clip.stop();
        clip.setFramePosition(0);
    }

    // volume in decibels, e.g. -10f quieter, 0f normal
    public void setVolumeDb(float db) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            ((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(db);
        }
    }

    public void dispose() { clip.close(); }
}
