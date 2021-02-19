/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.utils.jobjects;
/**
 * This class implements a delay timer that will call trigger() 
 * on the DelayTimerCallback delay milliseconds after 
 * startTimer() was called, if stopTimer() was not called first.  
 * The timer will only throw events after startTimer() is called.  
 * Until then, it does nothing.  It is safe to call stopTimer() 
 * and startTimer() repeatedly.
 *
 * Note that calls to trigger() will happen on the timer thread.
 *
 * This class is multiple-thread safe. 
 */
public class DelayTimer extends Thread {
	private final DelayTimerCallback callback;
	private final Object mutex = new Object();
	private final Object triggeredMutex = new Object();
	private final long delay;
	private boolean quit;
	private boolean triggered;
	private long waitTime;

	public DelayTimer(DelayTimerCallback callback, long delay) {
		this.callback = callback;
		this.delay = delay;
		setDaemon(true);
		start();
	}

	/**
	 * Calling this method twice will reset the timer.
	 */
	public void startTimer() {
		synchronized (mutex) {
			waitTime = delay;
			mutex.notify();
		}
	}

	public void stopTimer() {
		try {
			synchronized (mutex) {
				synchronized (triggeredMutex) {
					if (triggered) {
						triggeredMutex.wait();
					}
				}
				waitTime = 0;
				mutex.notify();
			}
		} catch (InterruptedException ie) {
			System.err.println("trigger failure");
			ie.printStackTrace(System.err);
		}
	}

	public void run() {
		try {
			while (!quit) {
				synchronized (mutex) {

					if (waitTime < 0) {
						triggered = true;
						waitTime = 0;
					} else {
						long saveWaitTime = waitTime;
						waitTime = -1;
						mutex.wait(saveWaitTime);
					}
				}
				try {
					if (triggered) {
						callback.trigger();
					}
				} catch (Exception e) {
					System.err.println("trigger() threw exception, continuing");
					e.printStackTrace(System.err);
				} finally {
					synchronized (triggeredMutex) {
						triggered = false;
						triggeredMutex.notify();
					}
				}
			}
		} catch (InterruptedException ie) {
			System.err.println("interrupted in run");
			ie.printStackTrace(System.err);
		}
	}

	public void quit() {
		synchronized (mutex) {
			this.quit = true;
			mutex.notify();
		}
	}
}