package org.jarzarr.test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.jarzarr.assets.Animation;
import org.jarzarr.assets.Sound;
import org.jarzarr.assets.SpriteSheet;
import org.jarzarr.core.EngineContext;
import org.jarzarr.scene.Scene;
import org.jarzarr.test.Main.Action;

public class GameScene implements Scene {

	private final EngineContext<Action> ctx;

	// World position
	private double x = 100;
	private double y = 100;
	private final double speed = 220;

	// Assets
	private SpriteSheet beeSheet;
	private Animation beeAnim;
	private Sound music;

	private static final int TILE_W = 20;
	private static final int TILE_H = 20;
	private static final double ANIM_FPS = 15.0;

	private static final String WALK_PATH = "assets/images/goerge.png";
	private static final String WAV_PATH = "assets/audio/Vitory_Awaits.wav";

	private Animation walkRight;
	private Animation walkLeft;

	private boolean musicPlaying = false;

	public GameScene(EngineContext<Action> ctx) {
		this.ctx = ctx;
	}

	@Override
	public void onEnter() {
		BufferedImage sheetImg = ctx.assets().getImage(WALK_PATH);
		beeSheet = new SpriteSheet(sheetImg, TILE_W, TILE_H);

		// Build animations from your atlas rectangles (x,y,w,h)
		// Walk Right: y=0, x = 0..140 step 20, 8 frames
		walkRight = buildAnimFromRects(beeSheet, ANIM_FPS, new int[][] { { 0, 0, 20, 20 }, // walk_r_0
				{ 20, 0, 20, 20 }, // walk_r_1
				{ 40, 0, 20, 20 }, // walk_r_2
				{ 60, 0, 20, 20 }, // walk_r_3
				{ 80, 0, 20, 20 }, // walk_r_4
				{ 100, 0, 20, 20 }, // walk_r_5
				{ 120, 0, 20, 20 }, // walk_r_6
				{ 140, 0, 20, 20 } // walk_r_7
		});

		// Walk Left: y=20, x = 0..140 step 20, 8 frames
		walkLeft = buildAnimFromRects(beeSheet, ANIM_FPS, new int[][] { { 0, 20, 20, 20 }, // walk_l_0
				{ 140, 20, 20, 20 }, // walk_l_7
				{ 120, 20, 20, 20 }, // walk_l_6
				{ 100, 20, 20, 20 }, // walk_l_5
				{ 80, 20, 20, 20 }, // walk_l_4
				{ 60, 20, 20, 20 }, // walk_l_3
				{ 40, 20, 20, 20 }, // walk_l_2
				{ 20, 20, 20, 20 }, // walk_l_1
		});

		// Default animation
		beeAnim = walkRight;

		// Load WAV as Sound
		music = ctx.assets().getSound(WAV_PATH);
		music.setVolumeDb(-8f);

		// Camera
		ctx.camera().setZoom(5.0);
	}

	@Override
	public void onExit() {
	}

	@Override
	public void update(double dt) {
		// Pause overlay
		if (ctx.actions().isPressed(Action.PAUSE)) {
			ctx.scenes().transitionTo(new PauseScene(ctx), new org.jarzarr.scene.FadeTransition(0.20));
			ctx.actions().consumePressed(Action.PAUSE);
			return;
		}

		// Movement (world space)
		if (ctx.actions().isDown(Action.MOVE_LEFT))
			x -= speed * dt;
		if (ctx.actions().isDown(Action.MOVE_RIGHT))
			x += speed * dt;
		if (ctx.actions().isDown(Action.MOVE_UP))
			y -= speed * dt;
		if (ctx.actions().isDown(Action.MOVE_DOWN))
			y += speed * dt;

		// Animate
		boolean movingLeft = ctx.actions().isDown(Action.MOVE_LEFT);
		boolean movingRight = ctx.actions().isDown(Action.MOVE_RIGHT);

		if (movingLeft && beeAnim != walkLeft) {
			beeAnim = walkLeft;
			beeAnim.reset();
		} else if (movingRight && beeAnim != walkRight) {
			beeAnim = walkRight;
			beeAnim.reset();
		}

		// Only advance frames when moving
		boolean moving = movingLeft || movingRight || ctx.actions().isDown(Action.MOVE_UP)
				|| ctx.actions().isDown(Action.MOVE_DOWN);

		if (moving) {
			beeAnim.update(dt);
		} else {
			// If you want a true "idle" later, swap to an idle anim here.
			beeAnim.reset();
		}

		// FIRE (left click) -> play sound
		if (ctx.actions().isPressed(Action.FIRE)) {
			if (musicPlaying) {
				music.stop();
				musicPlaying = false;
			} else {
				music.play();
				musicPlaying = true;
			}
			ctx.actions().consumePressed(Action.FIRE);
		}

		// Smooth camera follow (center of sprite)
		ctx.camera().followSmooth(x + TILE_W / 2.0, y + TILE_H / 2.0, dt);
	}

	@Override
	public void render(Graphics2D g) {
		var original = g.getTransform();

		// World render
		ctx.camera().setViewport(ctx.window().getWidth(), ctx.window().getHeight());
		ctx.camera().apply(g);

		g.drawImage(beeAnim.frame(), (int) x, (int) y, null);

		// Restore transform
		g.setTransform(original);
	}

	// -------------------------
	// Helpers
	// -------------------------

	/**
	 * Builds an Animation from an array of rectangles [x,y,w,h] on a SpriteSheet.
	 * This matches your "atlas" text where each named sprite maps to a rectangle.
	 */
	private static Animation buildAnimFromRects(SpriteSheet sheet, double fps, int[][] rects) {
		BufferedImage[] frames = new BufferedImage[rects.length];
		for (int i = 0; i < rects.length; i++) {
			int[] r = rects[i];
			frames[i] = sheet.spritePx(r[0], r[1], r[2], r[3]);
		}
		return new Animation(fps, frames);
	}
}
