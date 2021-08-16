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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;

/**
 * @author Mwithi
 */
public class JDateAndTimeChooserDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	/*
	 * Constants
	 */
	private static final String DATE_TIME_FORMAT = "EEEE, d MMMM yyyy - HH:mm";
	private static final int TEXT_SIZE = 30;

	/*
	 * Attributes
	 */
	private JDateAndTimeChooser dateAndTimeChooser;
	private JPanel buttonsPanel;
	private JButton buttonOK;
	private JButton buttonCancel;

	/*
	 * Return Value
	 */
	private Date date = null;

	public JDateAndTimeChooserDialog(JDialog owner) {
		super(owner, true);
		initComponents();
	}

	public JDateAndTimeChooserDialog(JDialog owner, Date date) {
		super(owner, true);
		this.date = date;
		initComponents();
	}

	private void initComponents() {
		setSize(new Dimension(740, 400));
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(getDateAndTimeChooser());
		getContentPane().add(getButtonsPanel(), BorderLayout.SOUTH);
		setResizable(false);
		setLocationRelativeTo(null);

	}

	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new JPanel();
			buttonsPanel.add(getButtonOK());
			buttonsPanel.add(getButtonCancel());
		}
		return buttonsPanel;
	}

	private JButton getButtonCancel() {
		if (buttonCancel == null) {
			buttonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			buttonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			buttonCancel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
		}
		return buttonCancel;
	}

	private JButton getButtonOK() {
		if (buttonOK == null) {
			buttonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			buttonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			buttonOK.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					date = dateAndTimeChooser.getDateTime();
					dispose();
				}
			});
		}
		return buttonOK;
	}

	public JDateAndTimeChooser getDateAndTimeChooser() {
		if (dateAndTimeChooser == null) {
			dateAndTimeChooser = new JDateAndTimeChooser();
			dateAndTimeChooser.getDateChooser().setLocale(new Locale(GeneralData.LANGUAGE));
			dateAndTimeChooser.getDateChooser().setDateFormatString(DATE_TIME_FORMAT); //$NON-NLS-1$
			dateAndTimeChooser.getDateChooser().setFont(new Font("Arial", Font.BOLD, TEXT_SIZE), false);
			if (date != null) dateAndTimeChooser.getDateChooser().setDate(date);
		}
		return dateAndTimeChooser;
	}

	public Date getDate() {
		return date;
	}

}
