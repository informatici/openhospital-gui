/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.isf.generaldata.GeneralData;

public class JDateAndTimeChooser extends JPanel {

	private static final long serialVersionUID = 1L;
	private CustomJDateChooser date;
	private JTimeTable timeTable;
	private Date dateTime = new Date();

	/**
	 * Create the panel.
	 */
	public JDateAndTimeChooser() {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		this.setSize(new Dimension(740, 400));
		this.add(getCustomJDateChooser());
		this.add(getJTimeTable());
	}
	
	private CustomJDateChooser getCustomJDateChooser() {
		if (date == null) {
			date = new CustomJDateChooser(dateTime);
			date.addPropertyChangeListener("date", new PropertyChangeListener() {

				public void propertyChange(PropertyChangeEvent evt) {
					dateTime = date.getDate();
				}
			});
		}
		return date;
	}

	private JTimeTable getJTimeTable() {
		if (timeTable == null) {
			timeTable = new JTimeTable();
			timeTable.addPropertyChangeListener("hour", new PropertyChangeListener() {
				
				public void propertyChange(PropertyChangeEvent evt) {
					Calendar calendar = date.getCalendar();
					calendar.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt((String) evt.getNewValue()));
					calendar.set(GregorianCalendar.SECOND, 0);
					date.setCalendar(calendar);
					dateTime = date.getDate();
				}
			});
			
			timeTable.addPropertyChangeListener("minute", new PropertyChangeListener() {
				
				public void propertyChange(PropertyChangeEvent evt) {
					Calendar calendar = date.getCalendar();
					calendar.set(GregorianCalendar.MINUTE, Integer.parseInt((String) evt.getNewValue()));
					calendar.set(GregorianCalendar.SECOND, 0);
					date.setCalendar(calendar);
					dateTime = date.getDate();
				}
			});
		}
		return timeTable;
	}

	/**
	 * @return the date
	 */
	public CustomJDateChooser getDateChooser() {
		return date;
	}
	
	/**
	 * @return the timeTable
	 */
	public JTimeTable getTimeTable() {
		return timeTable;
	}

	public Date getDateTime() {
		return dateTime;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeneralData.initialize();
		Date date;
		JDateAndTimeChooser dateTimeChooser = new JDateAndTimeChooser();
		int r = JOptionPane.showConfirmDialog(null,
				dateTimeChooser,
                "JOptionPane Example: ",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
		
		if (r == JOptionPane.OK_OPTION) {
			date = dateTimeChooser.getDateTime();
			System.out.println(date);
        }
	}
}
