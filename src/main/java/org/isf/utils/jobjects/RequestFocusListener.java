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

import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Convenience class to request focus on a component.
 * <p>
 * When the component is added to a realized Window then component will
 * request focus immediately, since the ancestorAdded event is fired
 * immediately.
 * <p>
 * When the component is added to a non realized Window, then the focus
 * request will be made once the window is realized, since the
 * ancestorAdded event will not be fired until then.
 * <p>
 * Using the default constructor will cause the listener to be removed
 * from the component once the AncestorEvent is generated. A second constructor
 * allows you to specify a boolean value of false to prevent the
 * AncestorListener from being removed when the event is generated. This will
 * allow you to reuse the listener each time the event is generated.
 */
public class RequestFocusListener implements AncestorListener {

	private boolean removeListener;

	/**
	 * Convenience constructor. The listener is only used once and then it is
	 * removed from the component.
	 */
	public RequestFocusListener() {
		this(true);
	}

	/**
	 * Constructor that controls whether this listen can be used once or
	 * multiple times.
	 *
	 * @param removeListener when true this listener is only invoked once
	 * otherwise it can be invoked multiple times.
	 */
	public RequestFocusListener(boolean removeListener) {
		this.removeListener = removeListener;
	}

	@Override
	public void ancestorAdded(AncestorEvent e) {
		JComponent component = e.getComponent();
		component.requestFocusInWindow();
		if (removeListener) {
			component.removeAncestorListener(this);
		}
	}

	@Override
	public void ancestorMoved(AncestorEvent e) {
	}

	@Override
	public void ancestorRemoved(AncestorEvent e) {
	}
}