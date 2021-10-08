/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2021 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.utils.time;

import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

/**
 * 02-mar-2006
 *
 * @author Theo
 */
public class DateTextField extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField day;
	private JTextField month;
	private JTextField year;
	private GregorianCalendar date;

	/**
	 * This is the constructor of the DateTextField object
	 * It displays the Date of the parameter "time"
	 * This object consists in 3 textfields (day,month,year) editable by the user
	 */
	public DateTextField() {
		date = new GregorianCalendar();
		initialize();
		day.setText("");
		month.setText("");
		year.setText("");
	}

	public DateTextField(LocalDateTime localDateTime) {
		this(Converters.toCalendar(localDateTime));
	}

	/**
	 * This is the constructor of the DateTextField object
	 * It displays the Date of the parameter "time"
	 * This object consists in 3 textfields (day,month,year) editable by the user
	 * @param time (GregorianCalendar)
	 */
	public DateTextField(GregorianCalendar time) {
		date = time;
		initialize();
		if (String.valueOf(time.get(Calendar.DAY_OF_MONTH)).length() == 1) {
			day.setText("0" + time.get(Calendar.DAY_OF_MONTH));
		} else {
			day.setText(String.valueOf(time.get(Calendar.DAY_OF_MONTH)));
		}
		if (String.valueOf(time.get(Calendar.MONTH) + 1).length() == 1) {
			month.setText("0" + (time.get(Calendar.MONTH) + 1));
		} else {
			month.setText(String.valueOf(time.get(Calendar.MONTH) + 1));
		}
		year.setText(String.valueOf(time.get(Calendar.YEAR)));
	}

	public void initialize() {
		day = new JTextField(2);
		day.setDocument(new DocumentLimit(2));
		day.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				if (day.getText().length() != 0) {
					if (day.getText().length() == 1) {
						String typed = day.getText();
						day.setText("0" + typed);
					}
					if (!isValidDay(day.getText())) {
						day.setText("01");
					}
				}
				//else day.setText("01");
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		month = new JTextField(2);
		month.setDocument(new DocumentLimit(2));
		month.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (month.getText().length() != 0) {
					if (month.getText().length() == 1) {
						String typed = month.getText();
						month.setText("0" + typed);
					}
					if (!isValidMonth(month.getText())) {
						month.setText("01");
					}
				}
				//else month.setText("01");
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		year = new JTextField(4);
		year.setDocument(new DocumentLimit(4));
		year.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (year.getText().length() == 4) {
					if (!isValidYear(year.getText())) {
						year.setText("2006");
					}
				} 
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		setLayout(new FlowLayout(FlowLayout.CENTER,2,0));
		add(day);
		add(new JLabel("/"));
		add(month);
		add(new JLabel("/"));
		add(year);
	}

	/**
	 * This method returns the day displayed by the object
	 * @return int
	 */
	public int getDay() {
		return Integer.parseInt(day.getText());
	}

	/**
	 * This method returns the month displayed by the object
	 * @return int
	 */
	public int getMonth() {
		return Integer.parseInt(month.getText());
	}

	/**
	 * This method returns the year displayed by the object
	 * @return int
	 */
	public int getYear() {
		return Integer.parseInt(year.getText());
	}

	/**
	 * This method update the parameter toModify setting the date displayed by the object
	 * @param toModify (GregorianCalendar)
	 * @return toModify (GregorianCalendar)
	 */
	public GregorianCalendar getCompleteDate(GregorianCalendar toModify) {
		toModify.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day.getText()));
		toModify.set(Calendar.MONTH, Integer.parseInt(month.getText()));
		toModify.set(Calendar.YEAR, Integer.parseInt(year.getText()));
		return toModify;
	}

	public LocalDateTime getCompleteLocalDateTime() {
		return Converters.convertToLocalDateTime(getCompleteDate());
	}

	/**
	 * This method returns the date displayed by the object
	 * @return GregorianCalendar
	 */
	public GregorianCalendar getCompleteDate() {
		if ((day.getText().length() == 0) || (month.getText().length() == 0) || (year.getText().length() == 0)) {
			day.setText("");
			month.setText("");
			year.setText("");
			return null;
		}
		date.set(Calendar.DAY_OF_MONTH, getDay());
		date.set(Calendar.MONTH, getMonth() - 1);
		date.set(Calendar.YEAR, getYear());
		return date;
	}

	/**
	 * This is a basic control for the day field input
	 * @param day (String)
	 * @return boolean (true if valid, false otherwise)
	 */
	private boolean isValidDay(String day) {
		if (day.length()<2) {
			return false;
		}
		if (day.charAt(0) < '0' || day.charAt(0) > '9' || day.charAt(1) < '0' || day.charAt(1) > '9') {
			return false;
		}
		int num = Integer.parseInt(day);
		return num >= 1 && num <= 31;
	}

	/**
	 * This is a basic control for the month field input
	 * @param month (String)
	 * @return boolean (true if valid, false otherwise)
	 */
	private boolean isValidMonth(String month) {
		if (month.length()<2) {
			return false;
		}
		if (month.charAt(0) < '0' ||month.charAt(0) > '9' || month.charAt(1) < '0' || month.charAt(1) > '9') {
			return false;
		}
		int num = Integer.parseInt(month);
		return num >= 1 && num <= 12;
	}

	/**
	 * This is a basic control for the year field input
	 * @param year (String)
	 * @return boolean (true if valid, false otherwise)
	 */
	private boolean isValidYear(String year) {
		if (year.length()<4) {
			return false;
		}
		if (year.charAt(0) < '0' || year.charAt(0) > '9' || year.charAt(1) < '0' || year.charAt(1) > '9'
				|| year.charAt(2) < '0' || year.charAt(2) > '9' || year.charAt(3) < '0'|| year.charAt(3) > '9') {
			return false;
		}
		return true;
	}

	public boolean isDateValid() {
		if ((day.getText().equals("")) || (month.getText().equals("")) || year.getText().equals("")) {
			return false;
		}
		return isValidDay(day.getText()) && isValidMonth(month.getText()) && isValidYear(year.getText());
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			day.setEnabled(true);
			month.setEnabled(true);
			year.setEnabled(true);
		} else {
			day.setEnabled(false);
			month.setEnabled(false);
			year.setEnabled(false);
		}
	}

	/**
	 * This class extends DefaultStyledDocument and is needed to limit of each input field
	 * @author someone (found on the web)
	 */
	public class DocumentLimit extends DefaultStyledDocument {

		private static final long serialVersionUID = 1L;
		private int maximumNumberOfCharacters;

		public DocumentLimit(int maximumNumberOfCharacters) {
			this.maximumNumberOfCharacters = maximumNumberOfCharacters;
		}

		@Override
		public void insertString(int off, String text, AttributeSet att) throws BadLocationException {
			int numberOfCharactersInDocument = getLength();
			int newTextLength = text.length();
			if (numberOfCharactersInDocument + newTextLength > maximumNumberOfCharacters) {
				int numberOfCharactersToInsert = maximumNumberOfCharacters - numberOfCharactersInDocument;
				if (numberOfCharactersToInsert > 0) {
					String parteNuovoTesto = text.substring(0, numberOfCharactersToInsert);
					super.insertString(off, parteNuovoTesto, att);
				}
			} else {
				super.insertString(off, text, att);
			}
		}
	}

}
