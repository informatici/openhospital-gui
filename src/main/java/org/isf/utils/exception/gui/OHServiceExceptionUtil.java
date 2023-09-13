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
package org.isf.utils.exception.gui;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.model.OHExceptionMessage;

public class OHServiceExceptionUtil {

	private OHServiceExceptionUtil() {
	}

	/**
	 * Iterate exception messages and show them in a JOptionPane with parentComponent = null
	 *
	 * @param e
	 */
	public static void showMessages(OHServiceException e) {
		showMessages(e, null);
	}

	/**
	 * Iterate exception messages and show them in a JOptionPane with parentComponent specified
	 *
	 * @param e
	 * @param parentComponent
	 */
	public static void showMessages(OHServiceException e, Component parentComponent) {
		if (null != e.getMessages()) {
			for (OHExceptionMessage msg : e.getMessages()) {
				String message = msg.getMessage();
				String title = msg.getTitle();
				if (null == title) {
					title = "";
				}

				int messageType = msg.getLevel().getSwingSeverity();
				JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
			}
		}
	}

}
