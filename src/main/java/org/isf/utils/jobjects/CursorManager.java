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
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.isf.utils.time.DelayTimer;

class CursorManager {

	private final DelayTimer waitTimer;
	private final Stack<DispatchedEvent> dispatchedEvents;
	private boolean needsCleanup;

	public CursorManager(DelayTimer waitTimer) {
		this.dispatchedEvents = new Stack<>();
		this.waitTimer = waitTimer;
	}

	private void cleanUp() {
		if (dispatchedEvents.peek().resetCursor()) {
			clearQueueOfInputEvents();
		}
	}

	private void clearQueueOfInputEvents() {
		final WaitCursorEventQueue waitCursorEventQueue = (WaitCursorEventQueue) Toolkit.getDefaultToolkit().getSystemEventQueue();
		final EventQueue parentQueue = waitCursorEventQueue.getParentQueue();
		synchronized (parentQueue) {
			synchronized (waitCursorEventQueue) {
				for (AWTEvent nonInputEvent : gatherNonInputEvents(waitCursorEventQueue)) {
					waitCursorEventQueue.postEvent(nonInputEvent);
				}
			}
		}
	}

	private List<AWTEvent> gatherNonInputEvents(EventQueue systemQueue) {
		List<AWTEvent> events = new ArrayList<>();
		while (systemQueue.peekEvent() != null) {
			try {
				AWTEvent nextEvent = systemQueue.getNextEvent();
				if (!(nextEvent instanceof InputEvent)) {
					events.add(nextEvent);
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		return events;
	}

	public void push(Object source) {
		if (needsCleanup) {
			waitTimer.stopTimer();
			cleanUp();
			//this corrects the state when a modal dialog
			//opened last time round
		}
		dispatchedEvents.push(new DispatchedEvent(source));
		needsCleanup = true;
	}

	public void pop() {
		cleanUp();
		dispatchedEvents.pop();
		if (!dispatchedEvents.isEmpty()) {
			//this will be stopped if getNextEvent() is called -
			//used to watch for modal dialogs closing
			waitTimer.startTimer();
		} else {
			needsCleanup = false;
		}
	}

	public void setCursor() {
		dispatchedEvents.peek().setCursor();
	}

}
