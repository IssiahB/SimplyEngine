package org.jarzarr.core.time;

/**
 * Handle returned by Scheduler to allow cancellation.
 */
public final class TimerHandle {
	boolean cancelled = false;

	public void cancel() {
		cancelled = true;
	}

	public boolean isCancelled() {
		return cancelled;
	}
}
