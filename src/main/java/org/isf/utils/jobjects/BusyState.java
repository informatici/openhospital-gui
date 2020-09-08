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

/*
 * OpenHospital
 *
 * $Id: BusyState.java,v 1.1 2014/11/16 11:32:09 eppesuig Exp $ $Date: 2014/11/16 11:32:09 $ $Revision: 1.1 $
 */

import java.awt.Container;

/**
 * Offer a method that enable or disable a component and all its children.
 * The cursor is changed to <code>WAIT_CURSOR</code> when disabling widgets, to
 * <code>DEFAULT_CURSOR</code> when enabling them.
 * 
 * The method is not recursive, so <i>grandchildren</i> aren't processed: it only
 * works on a component and its first level children.
 *
 * @author Giuseppe Sacco
 * @deprecated
 */
public class BusyState {
	static public void setBusyState(Container panel, boolean busy) {
		/*panel.setCursor(new Cursor( busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
		for (Component comp : panel.getComponents()) {
			comp.setEnabled(!busy);
		}*/
	}
}
