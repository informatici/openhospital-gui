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
import java.time.LocalTime;

import javax.swing.BoxLayout;

import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;

public class GoodTimeChooser extends Panel {

	private static final String TIME_FORMAT = "H:mm";

	private TimePicker timePicker;
	private TimePickerSettings timeSettings;

	public GoodTimeChooser(LocalTime time) {
		this(time, true);
	}

	public GoodTimeChooser(LocalTime time, boolean useSpinner) {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		timeSettings = new TimePickerSettings();
		timeSettings.setAllowEmptyTimes(false);
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
		timePicker = new TimePicker(timeSettings);
		if (time != null) {
			timePicker.setTime(time);
		}
		add(timePicker);
	}

	public LocalTime getLocalTime() {
		return timePicker.getTime();
	}

	public void setTime(LocalTime time) {
		if (time != null) {
			timePicker.setTime(time);
		} else {
			timePicker.clear();
		}
	}

	public void setEditable(boolean editable) {
		timePicker.setEnabled(editable);
	}

}
