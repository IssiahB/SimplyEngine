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

		int cols = sheetImg.getWidth() / TILE_W; // 4
		int rows = sheetImg.getHeight() / TILE_H; // 2 (sanity check)

		Animation topRowAnim = Animation.buildRowAnim(beeSheet, 0, cols, ANIM_FPS);

		// Pick one as default (standing still, etc.)
		beeAnim = topRowAnim;

		music = ctx.assets().getSound(WAV_PATH);
		music.setVolumeDb(-8f);

		// Store these if you want to switch at runtime:
		this.walkRight = topRowAnim;
		this.walkLeft = topRowAnim;

		// Load WAV as Sound
		music = ctx.assets().getSound(WAV_PATH);
		// optional: reduce volume a bit (0 is default)
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
			ctx.scenes().push(new PauseScene(ctx));
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

		// Only advance frames when actually moving (optional but feels better)
		boolean moving = movingLeft || movingRight || ctx.actions().isDown(Action.MOVE_UP)
				|| ctx.actions().isDown(Action.MOVE_DOWN);

		if (moving)
			beeAnim.update(dt);
		else
			beeAnim.reset(); // or keep last frame if you prefer

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

		// Restore for UI render later (if you add any)
		g.setTransform(original);
	}
}
