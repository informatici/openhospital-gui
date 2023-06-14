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

import javax.swing.BoxLayout;

import com.github.lgooddatepicker.components.DateTimePicker;

public class GoodDateTimeSpinnerChooser extends GoodDateTimeChooserBase {

	public GoodDateTimeSpinnerChooser(LocalDateTime dateTime) {
		this(dateTime, true);
	}

	public GoodDateTimeSpinnerChooser(LocalDateTime dateTime, boolean useDefaultTimeChangeListener) {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);

		setBaseSettings();

		timeSettings.setDisplayToggleTimeMenuButton(false);
		timeSettings.setDisplaySpinnerButtons(true);

		dateTimePicker = new DateTimePicker(dateSettings, timeSettings);
		// This helps the manual editing of the year field not to reset to some *very* old year value
		dateSettings.setDateRangeLimits(LocalDate.of(999, 12, 31), null);
		if (dateTime != null) {
			dateTimePicker.datePicker.setDate(dateTime.toLocalDate());
			dateTimePicker.timePicker.setTime(dateTime.toLocalTime());
		}

		setIcon();

		if (useDefaultTimeChangeListener) {
			setDefaultListener();
		}

		add(dateTimePicker);
	}

}
