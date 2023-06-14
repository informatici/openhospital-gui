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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

import javax.swing.BoxLayout;

import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.hospital.model.Hospital;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;

import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.optionalusertools.PickerUtilities;

public class GoodDateTimeVisitChooser extends GoodDateTimeChooserBase {

	public GoodDateTimeVisitChooser(LocalDateTime dateTime, int increment) {
		this(dateTime, increment, true);
	}

	public GoodDateTimeVisitChooser(LocalDateTime dateTime, int increment, boolean useDefaultTimeChangeListener) {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);

		setBaseSettings();

		HospitalBrowsingManager hospitalBrowsingManager = Context.getApplicationContext().getBean(HospitalBrowsingManager.class);
		Hospital hospital = null;
		try {
			hospital = hospitalBrowsingManager.getHospital();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (hospital != null) {
			startTime = hospital.getVisitStartTime().toLocalTime();
			endTime = hospital.getVisitEndTime().toLocalTime();
		} else {
			startTime = LocalTime.MIN;
			endTime = LocalTime.MAX;
		}

		timeSettings.setDisplayToggleTimeMenuButton(true);
		timeSettings.setDisplaySpinnerButtons(false);

		ArrayList<LocalTime> potentialMenuTimes = new ArrayList<>();
		boolean continueLoop = true;
		LocalTime entry = startTime;
		while (continueLoop) {
			if (PickerUtilities.isLocalTimeInRange(entry, startTime, endTime, true)) {
				potentialMenuTimes.add(entry);
			}
			entry = entry.plusMinutes(increment);
			if (entry.isAfter(endTime)) {
				continueLoop = false;
			}
		}
		timeSettings.generatePotentialMenuTimes(potentialMenuTimes);

		dateTimePicker = new DateTimePicker(dateSettings, timeSettings);
		// This helps the manual editing of the year field not to reset to some *very* old year value
		dateSettings.setDateRangeLimits(LocalDate.of(999, 12, 31), null);
		if (dateTime != null) {
			dateTimePicker.datePicker.setDate(dateTime.toLocalDate());
			dateTimePicker.timePicker.setTime(dateTime.toLocalTime());
		}

		setIcon();

		timeSettings.setVetoPolicy(new VisitTimeVetoPolicy());

		if (useDefaultTimeChangeListener) {
			setDefaultListener();
		}

		add(dateTimePicker);
	}

}
