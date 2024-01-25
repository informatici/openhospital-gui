/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.utils.jobjects;

import java.awt.AWTEvent;
import java.awt.EventQueue;

import org.isf.utils.time.DelayTimer;
import org.isf.utils.time.DelayTimerCallback;

public class WaitCursorEventQueue extends EventQueue implements DelayTimerCallback {

	private final CursorManager cursorManager;
	private final DelayTimer waitTimer;
	private final EventQueue parentQueue;

	public WaitCursorEventQueue(int delay, EventQueue systemQueue) {
		this.waitTimer = new DelayTimer(this, delay);
		this.cursorManager = new CursorManager(waitTimer);
		this.parentQueue = systemQueue;
	}

	public void close() {
		waitTimer.quit();
		pop();
	}

	public EventQueue getParentQueue() {
		return parentQueue;
	}

	@Override
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

	@Override
	public AWTEvent getNextEvent() throws InterruptedException {
		waitTimer.stopTimer();
		return super.getNextEvent();
	}

	@Override
	public void trigger() {
		cursorManager.setCursor();
	}

}
