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

import java.awt.Font;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import com.toedter.calendar.IDateEditor;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;
import org.isf.utils.time.Converters;

/**
 * @author Mwithi
 * 
 * JDateChooser override, needs JCalendar(r)
 * it overrides Font attribution
 *
 */
public class CustomJDateChooser extends JDateChooser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public CustomJDateChooser() {}

	/**
	 * @param dateEditor
	 */
	public CustomJDateChooser(IDateEditor dateEditor) {
		super(dateEditor);
		this.setMnemonic(0);
	}

	/**
	 * @param date
	 */
	public CustomJDateChooser(Date date) {
		super(date);
		this.setMnemonic(0);
	}

	/**
	 * @param date
	 * @param dateFormatString
	 */
	public CustomJDateChooser(Date date, String dateFormatString) {
		super(date, dateFormatString);
		this.setMnemonic(0);
	}

	public CustomJDateChooser(LocalDateTime date, String dateFormatString) {
		this(Converters.toDate(date), dateFormatString);
	}

	/**
	 * @param date
	 * @param dateFormatString
	 * @param dateEditor
	 */
	public CustomJDateChooser(Date date, String dateFormatString,
			IDateEditor dateEditor) {
		super(date, dateFormatString, dateEditor);
		this.setMnemonic(0);
	}
	
	/**
	 * @param datePattern
	 * @param maskPattern
	 * @param placeholder
	 */
	public CustomJDateChooser(String datePattern, String maskPattern,
			char placeholder) {
		super(datePattern, maskPattern, placeholder);
		this.setMnemonic(0);
	}

	/**
	 * @param jcal
	 * @param date
	 * @param dateFormatString
	 * @param dateEditor
	 */
	public CustomJDateChooser(JCalendar jcal, Date date,
			String dateFormatString, IDateEditor dateEditor) {
		super(jcal, date, dateFormatString, dateEditor);
		this.setMnemonic(0);
	}
	
	/**
	 * @author Mwithi
	 * 
	 * Override
	 * 
	 * @param font
	 * @param calendar - if true, set Font for the popup calendar also
	 */
	public void setFont(Font font, boolean calendar) {
		if (isInitialized) {
			dateEditor.getUiComponent().setFont(font);
			if (calendar) jcalendar.setFont(font);
		}
	}

	@Override
	public void setFont(Font font) {
		setFont(font, true);
	}
	
	public void setMnemonic(char keyChar) {
		this.calendarButton.setMnemonic(keyChar);
	}
	
	public void setMnemonic(int keyChar) {
		this.calendarButton.setMnemonic(keyChar);
	}

	public void setDateFromLocalDateTime(LocalDateTime localDate) {
		setDate(Converters.toDate(localDate));
	}

	public LocalDateTime getLocalDateTime() {
		return Optional.ofNullable(getDate())
				.map(Converters::convertToLocalDateTime)
				.orElse(null);
	}
}
