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

	// Bee.png is 720x240 => 3 cols x 1 rows if 240x240
	private static final int TILE_W = 240;
	private static final int TILE_H = 240;
	private static final double ANIM_FPS = 5.0;

	private static final String BEE_PATH = "assets/images/Bee.png";
	private static final String WAV_PATH = "assets/audio/Vitory_Awaits.wav";

	public GameScene(EngineContext<Action> ctx) {
		this.ctx = ctx;
	}

	@Override
	public void onEnter() {
		BufferedImage sheetImg = ctx.assets().getImage(BEE_PATH);
		beeSheet = new SpriteSheet(sheetImg, TILE_W, TILE_H);

		// Build animation frames from row 0, columns 0..3
		int cols = sheetImg.getWidth() / TILE_W; // should be 3
		BufferedImage[] frames = new BufferedImage[cols];
		for (int i = 0; i < cols; i++) {
			frames[i] = beeSheet.spriteGrid(i, 0);
		}
		beeAnim = new Animation(ANIM_FPS, frames);

		// Load WAV as Sound
		music = ctx.assets().getSound(WAV_PATH);
		// optional: reduce volume a bit (0 is default)
		music.setVolumeDb(-8f);
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
		beeAnim.update(dt);

		// FIRE (left click) -> play sound
		if (ctx.actions().isPressed(Action.FIRE)) {
			music.play();
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
