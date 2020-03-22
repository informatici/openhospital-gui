package org.isf.utils.jobjects;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;


public class WaitCursorEventQueue extends EventQueue implements DelayTimerCallback {
	private final CursorManager cursorManager;
	private final DelayTimer waitTimer;
	private final EventQueue previousEventQueue;

	public WaitCursorEventQueue(final EventQueue previousEventQueue,
								final int delay) {
		this.waitTimer = new DelayTimer(this, delay);
		this.cursorManager = new CursorManager(waitTimer);
		this.previousEventQueue = previousEventQueue;
	}

	public void close() {
		waitTimer.quit();
		pop();
	}

	protected void dispatchEvent(AWTEvent event) {
		cursorManager.push(event.getSource());
		waitTimer.startTimer();
		try {
			super.dispatchEvent(event);
		} finally {
			waitTimer.stopTimer();
			cursorManager.pop();
		}
	}


	public AWTEvent getNextEvent() throws InterruptedException {
		waitTimer.stopTimer();
		return super.getNextEvent();
	}

	public java.util.List<AWTEvent> getNonInputEvents() {
		final java.util.List<AWTEvent> nonInputEvents = new ArrayList<AWTEvent>();
		synchronized (previousEventQueue) {
			synchronized (this) {
				while (peekEvent() != null) {
					try {
						final AWTEvent nextEvent = getNextEvent();
						if (!(nextEvent instanceof InputEvent)) {
							nonInputEvents.add(nextEvent);
						}
					} catch (final InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
		return nonInputEvents;
	}

	public void trigger() {
		cursorManager.setCursor();
	}
}