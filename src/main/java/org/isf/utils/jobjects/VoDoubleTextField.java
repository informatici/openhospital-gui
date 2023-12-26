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

import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * @author <a href="http://www.java2s.com/Code/Java/Swing-JFC/Textfieldonlyacceptsnumbers.htm">...</a>
 */
public class VoDoubleTextField extends JTextField {

	private static final long serialVersionUID = 1L;
	private static final Pattern ALPHA_NUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]*$");

	/**
	 * @param defval - default value
	 * @param columns - number of columns to show
	 */
	public VoDoubleTextField(double defval, int columns) {
		super(String.valueOf(defval), columns);
	}

	@Override
	protected Document createDefaultModel() {
		return new IntTextDocument();
	}

	public double getValue() {
		try {
			return Double.parseDouble(getText());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	class IntTextDocument extends PlainDocument {

		private static final long serialVersionUID = 1L;

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if (str == null) {
				return;
			}
			String oldString = getText(0, getLength());
			String newString = oldString.substring(0, offs) + str + oldString.substring(offs);
			try {
				Double.parseDouble(newString + '0');
				super.insertString(offs, str, a);
			} catch (NumberFormatException e) {
				if (!ALPHA_NUMERIC_PATTERN.matcher(str).matches()) {
					super.insertString(offs, ".", a);
				}
			}
		}
	}
}
