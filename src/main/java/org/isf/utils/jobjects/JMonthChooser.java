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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.isf.generaldata.GeneralData;

public class JMonthChooser extends JPanel implements ItemListener, ChangeListener {

	private int month;
	private int oldSpinnerValue = 0;
	private JComboBox<String> comboBox;
	private JSpinner spinner;
	private boolean initialized;

	public JMonthChooser() {
		super();
		setLocale(new Locale(GeneralData.LANGUAGE));

		setLayout(new BorderLayout());
		comboBox = new JComboBox<>();
		comboBox.addItemListener(this);
		initNames();

		spinner = new JSpinner() {

			private static final long serialVersionUID = 1L;
			private JTextField textField = new JTextField();

			@Override
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				return new Dimension(size.width, textField.getPreferredSize().height);
			}
		};
		spinner.addChangeListener(this);
		spinner.setEditor(comboBox);
		comboBox.setBorder(new EmptyBorder(0, 0, 0, 0));
		updateUI();

		add(spinner, BorderLayout.WEST);

		initialized = true;
		setMonth(Calendar.getInstance().get(Calendar.MONTH));
	}

	/**
	 * Initializes the locale specific month names.
	 */
	private void initNames() {
		DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(getLocale());
		String[] monthNames = dateFormatSymbols.getMonths();

		if (comboBox.getItemCount() == 12) {
			comboBox.removeAllItems();
		}

		for (int idx = 0; idx < 12; idx++) {
			comboBox.addItem(monthNames[idx]);
		}
		comboBox.setSelectedIndex(month);
	}

	/**
	 * Is invoked if the state of the spinner changes.
	 *
	 * @param e the change event.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		SpinnerNumberModel model = (SpinnerNumberModel) ((JSpinner) e.getSource()).getModel();
		int value = model.getNumber().intValue();
		boolean increase = value > oldSpinnerValue;
		oldSpinnerValue = value;

		int newMonth = getMonth();

		if (increase) {
			newMonth += 1;
			if (newMonth == 12) {
				newMonth = 0;
			}
		} else {
			newMonth -= 1;
			if (newMonth == -1) {
				newMonth = 11;
			}
		}
		setMonth(newMonth);
	}

	/**
	 * The ItemListener for the months.
	 *
	 * @param itemEvent the item event
	 */
	@Override
	public void itemStateChanged(ItemEvent itemEvent) {
		if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
			int index = comboBox.getSelectedIndex();

			if ((index >= 0) && (index != month)) {
				setMonth(index, false);
			}
		}
	}

	/**
	 * Sets the month attribute of the JMonthChooser object. Fires a property change "month".
	 *
	 * @param newMonth the new month value
	 * @param select true, if the month should be selcted in the combo box.
	 */
	private void setMonth(int newMonth, boolean select) {
		if (!initialized) {
			return;
		}
		int oldMonth = month;
		month = newMonth;
		if (select) {
			comboBox.setSelectedIndex(month);
		}
		firePropertyChange("month", oldMonth, month);
	}

	/**
	 * Sets the month. This is a bound property. Valuse are valid between 0 (January) and 11 (December). A value < 0 will be treated as 0, a value >
	 * 11 will be treated as 11.
	 *
	 * @param newMonth the new month value
	 * @see #getMonth
	 */
	public void setMonth(int newMonth) {
		if (newMonth < 0) {
			setMonth(0, true);
		} else {
			setMonth(Math.min(newMonth, 11), true);
		}
	}

	/**
	 * Returns the month.
	 *
	 * @return the month value
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * Updates the UI.
	 *
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		final JSpinner testSpinner = new JSpinner();
		if (spinner != null) {
			if ("Windows".equals(UIManager.getLookAndFeel().getID())) {
				spinner.setBorder(testSpinner.getBorder());
			} else {
				spinner.setBorder(new EmptyBorder(0, 0, 0, 0));
			}
		}
	}

}
