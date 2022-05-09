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
import java.time.LocalDateTime;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.isf.generaldata.GeneralData;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;

public class GoodDateTimeChooser extends Panel {

	private static final String TIME_FORMAT = "H:mm";

	private DateTimePicker dateTimePicker;
	private DatePickerSettings dateSettings;
	private TimePickerSettings timeSettings;

	public GoodDateTimeChooser(LocalDateTime dateTime) {
		this(dateTime, true);
	}

	public GoodDateTimeChooser(LocalDateTime dateTime, boolean useSpinner) {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		dateSettings = new DatePickerSettings();
		dateSettings.setLocale(new Locale(GeneralData.LANGUAGE));
		dateSettings.setFormatForDatesCommonEra(DATE_FORMAT_DD_MM_YYYY);
		dateSettings.setAllowEmptyDates(true);
		dateSettings.setAllowKeyboardEditing(true);
		timeSettings = new TimePickerSettings();
		timeSettings.setAllowEmptyTimes(true);
		timeSettings.setAllowKeyboardEditing(true);
		timeSettings.setFormatForDisplayTime(TIME_FORMAT);
		timeSettings.setFormatForMenuTimes(TIME_FORMAT);
		if (useSpinner) {
			timeSettings.setDisplayToggleTimeMenuButton(false);
			timeSettings.setDisplaySpinnerButtons(true);
		} else {
			timeSettings.setDisplayToggleTimeMenuButton(true);
			timeSettings.setDisplaySpinnerButtons(false);
		}
		if (dateTime == null) {
			dateTime = LocalDateTime.now();
		}
		dateTimePicker = new DateTimePicker(dateSettings, timeSettings);
		dateTimePicker.datePicker.setDate(dateTime.toLocalDate());
		dateTimePicker.timePicker.setTime(dateTime.toLocalTime());

		ImageIcon calendarIcon = new ImageIcon("rsc/icons/calendar_button.png");
		JButton datePickerButton = dateTimePicker.datePicker.getComponentToggleCalendarButton();
		datePickerButton.setText("");
		datePickerButton.setIcon(calendarIcon);

		add(dateTimePicker);
	}

	public LocalDateTime getLocalDateTime() {
		return dateTimePicker.getDateTimeStrict();
	}

}
