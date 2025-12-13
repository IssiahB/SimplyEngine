package org.jarzarr;

public abstract class Loop implements Runnable {
	
	private boolean debug = false;
	private double targetUPS = 60.0;
	private int maxCatchUpUpdates = 8;
	private int stopDelay = 100;

	private Thread thread = null;
	private volatile boolean running = false;
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public void setTargetUPS(double ups) {
        if (ups <= 0) throw new IllegalArgumentException("UPS must be > 0");
        this.targetUPS = ups;
    }

    public void setMaxCatchUpUpdates(int max) {
        if (max < 1) max = 1;
        this.maxCatchUpUpdates = max;
    }
    
    public void setStopDelay(int delay) {
    	if (delay <= 0) throw new IllegalArgumentException("delay must be > 0");
    	this.stopDelay = delay;
    }


	public synchronized void start() {
		if (!running) {
			this.thread = new Thread(this);
			this.running = true;
			this.thread.start();
		}
	}

	public synchronized void stop() {
		if (running) {
			this.running = false;
			try {
				this.thread.join(stopDelay);
			} catch (InterruptedException e) {
				System.err.println("Main loop thread has already been interrupted!");
				e.printStackTrace();
			}
		}
	}

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

			// Fixed timestep updates
			int updateCapCount = 0;
			while (accumulator >= NS_PER_UPDATE && updateCapCount < this.maxCatchUpUpdates) {
				update(1.0 / TARGET_UPS);
				accumulator -= NS_PER_UPDATE;
				updates++;
				updateCapCount++;
			}

			render();
			frames++;

			// FPS / UPS counter (once per second)
			if (this.debug)
				if (System.currentTimeMillis() - timer >= 1000) {
					System.out.println("UPS: " + updates + " | FPS: " + frames);
					updates = 0;
					frames = 0;
					timer += 1000;
				}

			// Prevent CPU from pegging
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		cleanup();
	}
	
	

	protected abstract void init();

	protected abstract void update(double dt);

	protected abstract void render();

	protected abstract void cleanup();

}
