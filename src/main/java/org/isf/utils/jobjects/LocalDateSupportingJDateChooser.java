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

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import org.isf.utils.time.Converters;

import com.toedter.calendar.IDateEditor;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;

public class LocalDateSupportingJDateChooser extends JDateChooser {

	public LocalDateSupportingJDateChooser(Date date, String dateFormatString) {
		super(date, dateFormatString);
	}

	public LocalDateSupportingJDateChooser(Date date) {
		super(date);
	}

	public LocalDateSupportingJDateChooser(IDateEditor dateEditor) {
		super(dateEditor);
	}

	public LocalDateSupportingJDateChooser() {
	}

	public LocalDateSupportingJDateChooser(Date date, String dateFormatString, IDateEditor dateEditor) {
	}

	public LocalDateSupportingJDateChooser(String datePattern, String maskPattern, char placeholder) {
	}

	public LocalDateSupportingJDateChooser(JCalendar jcal, Date date, String dateFormatString, IDateEditor dateEditor) {
	}

	public LocalDateSupportingJDateChooser(LocalDateTime date, String dateFormatString) {
		this(Converters.toDate(date), dateFormatString);
	}

	public LocalDateSupportingJDateChooser(LocalDateTime date) {
		this(Converters.toDate(date));
	}

	public void setDate(LocalDateTime localDate) {
		setDate(Converters.toDate(localDate));
	}

	public LocalDateTime getLocalDateTime() {
		return Optional.ofNullable(getDate())
				.map(Converters::convertToLocalDateTime)
				.orElse(null);
	}

}