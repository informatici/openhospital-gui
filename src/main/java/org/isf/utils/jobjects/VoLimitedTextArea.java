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

import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

/**
 * Returns a JTextArea of the wanted length
 *
 * @author studente
 */
public class VoLimitedTextArea extends JTextArea {

	private static final long serialVersionUID = 1L;

	public class LimitedDimension extends DefaultStyledDocument {

		private static final long serialVersionUID = 1L;
		private final int maxChars;

		public LimitedDimension(int maxChars) {
			this.maxChars = maxChars;
		}

		@Override
		public void insertString(int off, String text, AttributeSet att)
				throws BadLocationException {
			int charsInDocument = getLength();
			int newLength = text.length();
			if (charsInDocument + newLength > maxChars) {
				int availableChars = maxChars - charsInDocument;
				if (availableChars > 0) {
					String newTextPart = text.substring(0, availableChars);
					super.insertString(off, newTextPart, att);
				}
			} else {
				super.insertString(off, text, att);
			}
		}
	}

	/**
	 * @param maxChars - the max number of chars
	 */
	public VoLimitedTextArea(int maxChars) {
		super();
		this.setDocument(new LimitedDimension(maxChars));
	}

	/**
	 * @param maxChars - the max number of chars
	 * @param text
	 * @param rows
	 * @param columns
	 */
	public VoLimitedTextArea(int maxChars, String text, int rows, int columns) {
		super(text, rows, columns);
		this.setDocument(new LimitedDimension(maxChars));
	}

	/**
	 * @param maxChars - the max number of chars
	 * @param rows
	 * @param columns
	 */
	public VoLimitedTextArea(int maxChars, int rows, int columns) {
		super(rows, columns);
		this.setDocument(new LimitedDimension(maxChars));
	}
}
