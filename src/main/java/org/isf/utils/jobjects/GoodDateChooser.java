/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2022 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;

import java.awt.Panel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.isf.generaldata.GeneralData;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;

public class GoodDateChooser extends Panel {

	private DatePicker datePicker;
	private DatePickerSettings dateSettings;

	public GoodDateChooser() {
		this(LocalDate.now());
	}

	public GoodDateChooser(LocalDate date) {
		this(date, true);
	}

	public GoodDateChooser(LocalDate date, boolean futureDates) {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		dateSettings = new DatePickerSettings();
		dateSettings.setLocale(new Locale(GeneralData.LANGUAGE));
		dateSettings.setFormatForDatesCommonEra(DATE_FORMAT_DD_MM_YYYY);
		dateSettings.setAllowEmptyDates(true);
		datePicker = new DatePicker(dateSettings);
		// This helps the manual editing of the year field not to reset to some *very* old year value
		if (futureDates) {
			dateSettings.setDateRangeLimits(LocalDate.of(999, 12, 31), null);
		} else {
			// This disallows dates in the future
			dateSettings.setDateRangeLimits(LocalDate.of(999, 12, 31), LocalDate.now().plusDays(1));
		}
		if (date != null) {
			datePicker.setDate(date);
		}
		ImageIcon calendarIcon = new ImageIcon("rsc/icons/calendar_button.png");
		JButton datePickerButton = datePicker.getComponentToggleCalendarButton();
		datePickerButton.setText("");
		datePickerButton.setIcon(calendarIcon);
		add(datePicker);
	}

	public LocalDate getDate() {
		return datePicker.getDate();
	}

	public void setDate(LocalDate localDate) {
		datePicker.setDate(localDate);
	}

	public LocalDateTime getDateStartOfDay() {
		LocalDate localDate = getDate();
		return localDate != null ? localDate.atStartOfDay() : null;
	}

	public LocalDateTime getDateEndOfDay() {
		LocalDate localDate = getDate();
		return localDate != null ? localDate.atTime(LocalTime.MAX) : null;
	}

	public void addDateChangeListener(DateChangeListener listener) {
		datePicker.addDateChangeListener(listener);
	}

}
