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
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.isf.generaldata.GeneralData;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.hospital.model.Hospital;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;

import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import com.github.lgooddatepicker.optionalusertools.DateTimeChangeListener;
import com.github.lgooddatepicker.optionalusertools.PickerUtilities;
import com.github.lgooddatepicker.optionalusertools.TimeVetoPolicy;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;

public class GoodDateTimeChooser extends Panel {

	private static final String TIME_FORMAT = "H:mm";

	private DateTimePicker dateTimePicker;
	private DatePickerSettings dateSettings;
	private TimePickerSettings timeSettings;

	private Time startTime;
	private Time endTime;
	private int increment;

	public GoodDateTimeChooser(LocalDateTime dateTime) {
		this(dateTime, true);
	}

	public GoodDateTimeChooser(LocalDateTime dateTime, boolean useSpinner) {
		this(dateTime, useSpinner, false);
	}

	public GoodDateTimeChooser(LocalDateTime dateTime, boolean useSpinner, boolean useVisitTimeRange) {
		this(dateTime, useSpinner, useVisitTimeRange, false, false);
	}

	public GoodDateTimeChooser(LocalDateTime dateTime, boolean useSpinner, boolean useVisitTimeRange, boolean useCustomIncrement) {
		this(dateTime, useSpinner, useVisitTimeRange, useCustomIncrement, true);
	}

	public GoodDateTimeChooser(LocalDateTime dateTime, boolean useSpinner, boolean useVisitTimeRange,
			boolean useCustomIncrement, boolean useDefaultTimeChangeListener) {
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

		if (useVisitTimeRange || useCustomIncrement) {
			HospitalBrowsingManager manager = Context.getApplicationContext().getBean(HospitalBrowsingManager.class);
			Hospital hospital = null;
			try {
				hospital = manager.getHospital();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			startTime = hospital.getVisitStartTime();
			endTime = hospital.getVisitEndTime();
			increment = hospital.getVisitIncrement();
		}

		if (useSpinner) {
			timeSettings.setDisplayToggleTimeMenuButton(false);
			timeSettings.setDisplaySpinnerButtons(true);
		} else {
			timeSettings.setDisplayToggleTimeMenuButton(true);
			timeSettings.setDisplaySpinnerButtons(false);
		}

		if (useCustomIncrement) {
			ArrayList<LocalTime> potentialMenuTimes = new ArrayList<>();
			LocalTime entry = LocalTime.MIDNIGHT;
			boolean continueLoop = true;
			LocalTime start = startTime.toLocalTime();
			LocalTime end = endTime.toLocalTime();
			while (continueLoop) {
				if (PickerUtilities.isLocalTimeInRange(entry, start, end, true)) {
					potentialMenuTimes.add(entry);
				}
				entry = entry.plusMinutes(increment);
				if (entry.isAfter(end)) {
					continueLoop = false;
				}
			}
			timeSettings.generatePotentialMenuTimes(potentialMenuTimes);
		}

		dateTimePicker = new DateTimePicker(dateSettings, timeSettings);
		// This helps the manual editing of the year field not to reset to some *very* old year value
		dateSettings.setDateRangeLimits(LocalDate.of(999, 12, 31), null);
		if (dateTime != null) {
			dateTimePicker.datePicker.setDate(dateTime.toLocalDate());
			dateTimePicker.timePicker.setTime(dateTime.toLocalTime());
		}

		ImageIcon calendarIcon = new ImageIcon("rsc/icons/calendar_button.png");
		JButton datePickerButton = dateTimePicker.datePicker.getComponentToggleCalendarButton();
		datePickerButton.setText("");
		datePickerButton.setIcon(calendarIcon);

		if (useVisitTimeRange) {
			timeSettings.setVetoPolicy(new VisitTimeVetoPolicy());
		}

		if (useDefaultTimeChangeListener) {
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

		add(dateTimePicker);
	}

	public LocalDateTime getLocalDateTime() {
		return dateTimePicker.getDateTimeStrict();
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

	private class VisitTimeVetoPolicy implements TimeVetoPolicy {

		@Override
		public boolean isTimeAllowed(LocalTime time) {
			// Only allow visit times as defined by the hospital, inclusive.
			return PickerUtilities.isLocalTimeInRange(
					time, startTime.toLocalTime(), endTime.toLocalTime(), true);
		}
	}

}
