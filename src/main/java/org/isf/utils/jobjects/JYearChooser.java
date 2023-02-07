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

import java.awt.Dimension;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

public class JYearChooser extends JSpinner {

	public JYearChooser() {
		SpinnerDateModel spinnerDateModel = new SpinnerDateModel();
		spinnerDateModel.setCalendarField(Calendar.YEAR);
		setModel(spinnerDateModel);
		setEditor(new JSpinner.DateEditor(this, "yyyy"));
		setPreferredSize(new Dimension(60, getPreferredSize().height));
	}

	/**
	 * Returns the year.
	 *
	 * @return the year
	 */
	public int getYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime((Date) getValue());
		return calendar.get(Calendar.YEAR);
	}

}
