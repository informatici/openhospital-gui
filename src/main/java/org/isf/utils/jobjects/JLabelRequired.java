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
 * JLabelRequired.java - 28/gen/2014
 * @author Mwithi
 */

import javax.swing.Icon;
import javax.swing.JLabel;

public class JLabelRequired extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1519907071350395237L;
	
	private static String MARK = " *";

	/**
	 * 
	 */
	public JLabelRequired() {
	}

	/**
	 * @param text
	 */
	public JLabelRequired(String text) {
		super(text);
	}

	/**
	 * @param image
	 */
	public JLabelRequired(Icon image) {
		super(image);
	}

	/**
	 * @param text
	 * @param horizontalAlignment
	 */
	public JLabelRequired(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	/**
	 * @param image
	 * @param horizontalAlignment
	 */
	public JLabelRequired(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
	}

	/**
	 * @param text
	 * @param icon
	 * @param horizontalAlignment
	 */
	public JLabelRequired(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JLabel#setText(java.lang.String)
	 */
	@Override
	public void setText(String text) {
		super.setText(text + MARK);
	}
}
