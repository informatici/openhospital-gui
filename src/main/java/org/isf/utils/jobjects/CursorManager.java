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

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

class CursorManager {

	private final DelayTimer waitTimer;
	private final Stack<DispatchedEvent> dispatchedEvents;
	private boolean needsCleanup;

	public CursorManager(DelayTimer waitTimer) {
		this.dispatchedEvents = new Stack<DispatchedEvent>();
		this.waitTimer = waitTimer;
	}
	private void cleanUp() {
		if (((DispatchedEvent) dispatchedEvents.peek()).resetCursor()) {
			clearQueueOfInputEvents();
		}
	}
	private void clearQueueOfInputEvents() {
		EventQueue q = Toolkit.getDefaultToolkit().getSystemEventQueue();
		try {
			ArrayList<AWTEvent> nonInputEvents = gatherNonInputEvents(q);
			for (Iterator<AWTEvent> it = nonInputEvents.iterator(); it.hasNext();) {
				q.postEvent((AWTEvent) it.next());
			}

		} finally {
		}
	}
	private ArrayList<AWTEvent> gatherNonInputEvents(EventQueue systemQueue) {
		ArrayList<AWTEvent> events = new ArrayList<AWTEvent>();
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
			// this corrects the state when a modal dialog
			// opened last time round
		}
		dispatchedEvents.push(new DispatchedEvent(source));
		needsCleanup = true;
	}
	public void pop() {
		cleanUp();
		dispatchedEvents.pop();
		if (!dispatchedEvents.isEmpty()) {
			// this will be stopped if getNextEvent() is called -
			// used to watch for modal dialogs closing
			waitTimer.startTimer();
		} else {
			needsCleanup = false;
		}
	}
	public void setCursor() {
		((DispatchedEvent) dispatchedEvents.peek()).setCursor();
	}
}
