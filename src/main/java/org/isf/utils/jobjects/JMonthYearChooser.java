/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2021 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.JPanel;

import org.isf.generaldata.GeneralData;
import org.isf.utils.time.Converters;

import com.toedter.calendar.JMonthChooser;
import com.toedter.calendar.JYearChooser;

/**
 * MonthYearChooser.java - 14/dic/2012
 *
 * @author Mwithi
 */
public class JMonthYearChooser extends JPanel {

	private static final long serialVersionUID = 1L;

	private GregorianCalendar gc = new GregorianCalendar();

	/**
	 * Create the dialog.
	 */
	public JMonthYearChooser() {

		JMonthChooser month = new JMonthChooser();
		month.setLocale(new Locale(GeneralData.LANGUAGE));
		month.addPropertyChangeListener("month", propertyChangeEvent -> {
			JMonthChooser theChooser = (JMonthChooser) propertyChangeEvent.getSource();
			gc.set(Calendar.MONTH, theChooser.getMonth());
		});

		JYearChooser year = new JYearChooser();
		year.setLocale(new Locale(GeneralData.LANGUAGE));
		year.addPropertyChangeListener("year", propertyChangeEvent -> {
			JYearChooser theChooser = (JYearChooser) propertyChangeEvent.getSource();
			gc.set(Calendar.YEAR, theChooser.getYear());
		});

		JPanel datePanel = new JPanel();
		datePanel.add(month);
		datePanel.add(year);
		this.add(datePanel);
	}

	public GregorianCalendar getDate() {
		return gc;
	}

	public LocalDate getLocalDate() {
		return Converters.convertToLocalDateTime(gc).toLocalDate();
	}

}
