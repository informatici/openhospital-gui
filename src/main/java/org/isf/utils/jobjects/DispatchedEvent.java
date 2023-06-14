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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.MenuComponent;
import java.awt.MenuContainer;

import javax.swing.SwingUtilities;

class DispatchedEvent {

	private final Object mutex = new Object();
	private final Object source;
	private Component parent;
	private Cursor lastCursor;

	public DispatchedEvent(Object source) {
		this.source = source;
	}

	public void setCursor() {
		synchronized (mutex) {
			parent = findVisibleParent();
			if (parent != null) {
				lastCursor = (parent.isCursorSet() ? parent.getCursor() : null);
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
		}
	}

	public boolean resetCursor() {
		synchronized (mutex) {
			if (parent != null) {
				parent.setCursor(lastCursor);
				parent = null;
				return true;
			}
			return false;
		}
	}

	private Component findVisibleParent() {
		Component result = null;
		if (source instanceof Component) {
			result = SwingUtilities.getRoot((Component) source);
		} else if (source instanceof MenuComponent) {
			MenuContainer mParent = ((MenuComponent) source).getParent();
			if (mParent instanceof Component) {
				result = SwingUtilities.getRoot((Component) mParent);
			}
		}
		if ((result != null) && result.isVisible()) {
			return result;
		}
		return null;
	}
}