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

import java.awt.Panel;
import java.time.LocalDateTime;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;

public class GoodDateTimeChooser extends Panel {

	private static final String TIME_FORMAT = "H:mm";

	DateTimePicker dateTimePicker;
	DatePickerSettings dateSettings;
	TimePickerSettings timeSettings;

	public GoodDateTimeChooser(LocalDateTime dateTime, boolean setTimeRange) {
//		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
//		this.setLayout(layout);
//		dateSettings = new DatePickerSettings();
//		dateSettings.setLocale(new Locale(GeneralData.LANGUAGE));
//		dateSettings.setFormatForDatesCommonEra(DATE_FORMAT_DD_MM_YYYY);
//		dateSettings.setAllowEmptyDates(false);
//		timeSettings = new TimePickerSettings();
//		timeSettings.setAllowEmptyTimes(false);
//		timeSettings.setAllowKeyboardEditing(false);
//		timeSettings.setFormatForDisplayTime(TIME_FORMAT);
//		timeSettings.setFormatForMenuTimes(TIME_FORMAT);
//		timeSettings.generatePotentialMenuTimes(getTimeIncrement(GeneralData.VISITINCREMENT), null, null);
//		if (dateTime == null) {
//			dateTime = LocalDateTime.now();
//		}
//		dateTime = dateTime.withMinute(0);
//		dateTime = dateTime.withSecond(0);
//		dateTime = dateTime.withNano(0);
//		if (setTimeRange) {
//			// ensure the hour is within the range of specified hours
//			int hour = dateTime.getHour();
//			if (hour < GeneralData.VISITSTARTHOUR || hour > GeneralData.VISITENDHOUR) {
//				dateTime = dateTime.withHour(GeneralData.VISITSTARTHOUR);
//			}
//		}
//		dateTimePicker = new DateTimePicker(dateSettings, timeSettings);
//		dateTimePicker.getTimePicker().getSettings().setVetoPolicy(new OHVetoTimerPolicy());
//		dateTimePicker.datePicker.setDate(dateTime.toLocalDate());
//		dateTimePicker.timePicker.setTime(dateTime.toLocalTime());
//		add(dateTimePicker);
	}

	public LocalDateTime getLocalDateTime() {
		return dateTimePicker.getDateTimeStrict();
	}

//	/**
//	 * OHVetoTimerPolicy, A veto policy is a way to disallow certain times from being selected in
//	 * the time picker. A vetoed time cannot be added to the time drop down menu. A vetoed time
//	 * cannot be selected by using the keyboard or the mouse.
//	 */
//	private static class OHVetoTimerPolicy implements TimeVetoPolicy {
//
//		/**
//		 * isTimeAllowed, Return true if a time should be allowed, or false if a time should be vetoed.
//		 */
//		@Override
//		public boolean isTimeAllowed(LocalTime time) {
//			// Only allow times from 5a to 7p, inclusive.
//			return PickerUtilities.isLocalTimeInRange(
//					time, LocalTime.of(GeneralData.VISITSTARTHOUR, 00), LocalTime.of(GeneralData.VISITENDHOUR, 00), true);
//		}
//	}
//
//	private TimeIncrement getTimeIncrement(int minutes) {
//		switch (minutes) {
//			case 5:
//				return TimeIncrement.FiveMinutes;
//			case 10:
//				return TimeIncrement.TenMinutes;
//			case 15:
//				return TimeIncrement.FifteenMinutes;
//			case 20:
//				return TimeIncrement.TwentyMinutes;
//			case 30:
//				return TimeIncrement.ThirtyMinutes;
//			case 60:
//				return TimeIncrement.OneHour;
//			default:
//				return TimeIncrement.ThirtyMinutes;
//		}
//	}

}
