package org.jarzarr.core.time;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple scheduler updated by Engine each tick.
 *
 * - after(seconds, r): run once after delay - every(seconds, r): run repeatedly
 * at interval
 *
 * Notes: - Uses update(dt) where dt is seconds. - Safe to schedule/cancel
 * during update.
 */
public final class Scheduler {

	private static final class Task {
		final TimerHandle handle = new TimerHandle();
		final Runnable runnable;
		final double interval; // 0 => one-shot
		double remaining;

		Task(double delay, double interval, Runnable r) {
			this.remaining = Math.max(0.0, delay);
			this.interval = interval;
			this.runnable = r;
		}
	}

	private final List<Task> tasks = new ArrayList<>(64);

	public TimerHandle after(double seconds, Runnable r) {
		Task t = new Task(seconds, 0.0, r);
		tasks.add(t);
		return t.handle;
	}

	public TimerHandle every(double seconds, Runnable r) {
		double s = Math.max(0.001, seconds);
		Task t = new Task(s, s, r);
		tasks.add(t);
		return t.handle;
	}

	public void clear() {
		tasks.clear();
	}

	public void update(double dt) {
		if (tasks.isEmpty())
			return;

		Iterator<Task> it = tasks.iterator();
		while (it.hasNext()) {
			Task t = it.next();
			if (t.handle.cancelled) {
				it.remove();
				continue;
			}

			t.remaining -= dt;
			if (t.remaining <= 0.0) {
				try {
					if (t.runnable != null)
						t.runnable.run();
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				if (t.interval > 0.0) {
					// repeat: keep phase stable-ish even if dt is big
					t.remaining += t.interval;
				} else {
					it.remove();
				}
			}
		}
	}
}
