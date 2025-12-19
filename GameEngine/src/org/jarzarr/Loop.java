package org.jarzarr;

/**
 * Simple fixed-timestep game loop runner.
 *
 * <p>
 * This class owns the loop thread, controls start/stop, and calls:
 * </p>
 * <ul>
 * <li>{@link #init()} once before the loop begins</li>
 * <li>{@link #update(double)} zero or more times per frame using a fixed
 * dt</li>
 * <li>{@link #render()} once per iteration (frame)</li>
 * <li>{@link #cleanup()} once after the loop ends</li>
 * </ul>
 *
 * <p>
 * Timing model:
 * </p>
 * <ul>
 * <li>Updates run at target UPS (updates per second) using a fixed
 * timestep.</li>
 * <li>Rendering runs as fast as the CPU allows, with a short sleep to reduce
 * pegging.</li>
 * <li>Catch-up updates are capped to prevent spiral-of-death on slow
 * frames.</li>
 * </ul>
 */
public abstract class Loop implements Runnable {

	/** When true, prints per-second UPS/FPS counters to stdout. */
	private boolean debug = false;

	/** Target updates per second for fixed-timestep updates. Must be > 0. */
	private double targetUPS = 60.0;

	/** Max number of catch-up updates allowed per frame to prevent runaway lag. */
	private int maxCatchUpUpdates = 8;

	/**
	 * Milliseconds to wait when joining the loop thread during stop(). Must be > 0.
	 */
	private int stopDelay = 100;

	/** The backing thread that runs the loop. */
	private Thread thread = null;

	/**
	 * Loop running flag. Volatile so other threads can request stop safely. Only
	 * the loop thread should execute init/update/render/cleanup.
	 */
	private volatile boolean running = false;

	/**
	 * Enables/disables debug counters printed once per second.
	 *
	 * @param debug true to print UPS/FPS counters
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Sets the target fixed update rate (UPS).
	 *
	 * @param ups updates per second (must be > 0)
	 * @throws IllegalArgumentException if ups <= 0
	 */
	public void setTargetUPS(double ups) {
		if (ups <= 0)
			throw new IllegalArgumentException("UPS must be > 0");
		this.targetUPS = ups;
	}

	/**
	 * Sets the maximum number of catch-up updates per frame. Values < 1 are clamped
	 * to 1.
	 *
	 * @param max maximum catch-up updates per render iteration
	 */
	public void setMaxCatchUpUpdates(int max) {
		if (max < 1)
			max = 1;
		this.maxCatchUpUpdates = max;
	}

	/**
	 * Sets how long stop() waits for the loop thread to join.
	 *
	 * @param delay milliseconds (must be > 0)
	 * @throws IllegalArgumentException if delay <= 0
	 */
	public void setStopDelay(int delay) {
		if (delay <= 0)
			throw new IllegalArgumentException("delay must be > 0");
		this.stopDelay = delay;
	}

	/**
	 * Starts the loop on a new thread if it is not already running. This method is
	 * synchronized to prevent double-start races.
	 */
	public synchronized void start() {
		if (!running) {
			this.thread = new Thread(this);
			this.running = true;
			this.thread.start();
		}
	}

	/**
	 * Requests the loop to stop and joins the loop thread for up to stopDelay ms.
	 * This method is synchronized to prevent stop/start races.
	 */
	public synchronized void stop() {
		if (running) {
			this.running = false;
			try {
				this.thread.join(stopDelay);
			} catch (InterruptedException e) {
				System.err.println("Main loop thread has already been interrupted!");
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Loop thread entrypoint. Runs init(), then fixed-timestep updates + rendering
	 * until stop() is called, then runs cleanup().
	 */
	@Override
	public void run() {
		init();

		final double TARGET_UPS = this.targetUPS;
		final double NS_PER_UPDATE = 1_000_000_000.0 / TARGET_UPS;

		long lastTime = System.nanoTime();
		double accumulator = 0.0;

		int updates = 0;
		int frames = 0;

		long timer = System.currentTimeMillis();

		while (running) {
			long now = System.nanoTime();
			double elapsed = now - lastTime;
			lastTime = now;

			accumulator += elapsed;

			// Fixed timestep updates.
			int updateCapCount = 0;
			while (accumulator >= NS_PER_UPDATE && updateCapCount < this.maxCatchUpUpdates) {
				update(1.0 / TARGET_UPS);
				accumulator -= NS_PER_UPDATE;
				updates++;
				updateCapCount++;
			}

			// One render per loop iteration.
			render();
			frames++;

			// FPS / UPS counter (once per second).
			if (this.debug) {
				if (System.currentTimeMillis() - timer >= 1000) {
					System.out.println("UPS: " + updates + " | FPS: " + frames);
					updates = 0;
					frames = 0;
					timer += 1000;
				}
			}

			// Prevent CPU from pegging.
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		cleanup();
	}

	/**
	 * Called once before the loop starts (resource creation, scene setup, etc.).
	 */
	protected abstract void init();

	/**
	 * Fixed timestep update.
	 *
	 * @param dt fixed delta time in seconds (typically 1/targetUPS)
	 */
	protected abstract void update(double dt);

	/** Called once per frame to draw to the render target. */
	protected abstract void render();

	/**
	 * Called once after the loop ends (resource disposal, shutdown hooks, etc.).
	 */
	protected abstract void cleanup();
}
