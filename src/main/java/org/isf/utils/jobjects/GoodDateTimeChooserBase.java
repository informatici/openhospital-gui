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

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;

import java.awt.Panel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.isf.generaldata.GeneralData;
import org.isf.utils.time.TimeTools;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import com.github.lgooddatepicker.optionalusertools.DateTimeChangeListener;
import com.github.lgooddatepicker.optionalusertools.PickerUtilities;
import com.github.lgooddatepicker.optionalusertools.TimeVetoPolicy;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;

public abstract class GoodDateTimeChooserBase extends Panel {

	protected static final String TIME_FORMAT = "H:mm";

	protected DateTimePicker dateTimePicker;
	protected DatePickerSettings dateSettings;
	protected TimePickerSettings timeSettings;

	protected LocalTime startTime;
	protected LocalTime endTime;
	protected int increment;

	public void setBaseSettings() {
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
	}

	public void setIcon() {
		ImageIcon calendarIcon = new ImageIcon("rsc/icons/calendar_button.png");
		JButton datePickerButton = dateTimePicker.datePicker.getComponentToggleCalendarButton();
		datePickerButton.setText("");
		datePickerButton.setIcon(calendarIcon);
	}

	public void setDefaultListener() {
		addDateTimeChangeListener(event -> {
			DateChangeEvent dateChangeEvent = event.getDateChangeEvent();
			if (dateChangeEvent != null) {
				// if the time is blank set it to the current time; otherwise leave it alone
				TimePicker timePicker = event.getTimePicker();
				if (timePicker.getTime() == null) {
					timePicker.setTime(LocalTime.now());
				}
			}
		});
		}

	public LocalDateTime getLocalDateTime() {
		return TimeTools.truncateToSeconds(dateTimePicker.getDateTimeStrict());
	}

	// time can be blank/empty
	public LocalDateTime getLocalDateTimePermissive() {
		return TimeTools.truncateToSeconds(dateTimePicker.getDateTimePermissive());
	}

	public void setDateTime(LocalDateTime dateTime) {
		if (dateTime != null) {
			dateTimePicker.datePicker.setDate(dateTime.toLocalDate());
			dateTimePicker.timePicker.setTime(dateTime.toLocalTime());
		} else {
			dateTimePicker.datePicker.clear();
			dateTimePicker.timePicker.clear();
		}
	}

	public void setDate(LocalDate date) {
		dateTimePicker.datePicker.setDate(date);
	}

	public void addDateTimeChangeListener(DateTimeChangeListener listener) {
		dateTimePicker.addDateTimeChangeListener(listener);
	}

	protected class VisitTimeVetoPolicy implements TimeVetoPolicy {

		@Override
		public boolean isTimeAllowed(LocalTime time) {
			// Only allow visit times as defined by the hospital, inclusive.
			return PickerUtilities.isLocalTimeInRange(
					time, startTime, endTime, true);
		}
	}

}
