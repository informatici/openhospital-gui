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
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.isf.generaldata.MessageBundle;

public class GoodFromDateToDateChooser extends JDialog {

	private GoodDateChooser fromDateChooser;
	private GoodDateChooser toDateChooser;
	private JPanel buttonsPanel;
	private JButton buttonOK;
	private JButton buttonExcel;
	private JButton buttonCancel;
	private boolean isCancel = false;
	private JPanel datesChooserPanel;
	private LocalDate dateFrom;
	private LocalDate dateTo;
	private boolean isExcel = false;

	public GoodFromDateToDateChooser(ModalJFrame owner) {
		super(owner, true);
		this.dateFrom = LocalDate.now();
		this.dateTo = LocalDate.now();
		initComponents();
	}

	public GoodFromDateToDateChooser(JDialog owner, LocalDate dateFrom, LocalDate dateTo) {
		super(owner, true);
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout(10, 10));
		getContentPane().add(getDatesChoosers(dateFrom, dateTo), BorderLayout.CENTER);
		getContentPane().add(getButtonsPanel(), BorderLayout.SOUTH);
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
	}

	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new JPanel();
			buttonsPanel.add(getButtonOK());
			buttonsPanel.add(getButtonExcel());
			buttonsPanel.add(getButtonCancel());
		}
		return buttonsPanel;
	}

	private JButton getButtonCancel() {
		if (buttonCancel == null) {
			buttonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			buttonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			buttonCancel.addActionListener(actionEvent -> {
				isCancel = true;
				dispose();
			});
		}
		return buttonCancel;
	}

	private JButton getButtonOK() {
		if (buttonOK == null) {
			buttonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			buttonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			buttonOK.addActionListener(actionEvent -> {
				dateFrom = fromDateChooser.getDate();
				dateTo = toDateChooser.getDate();
				dispose();
			});
		}
		return buttonOK;
	}

	private JButton getButtonExcel() {
		if (buttonExcel == null) {
			buttonExcel = new JButton(MessageBundle.getMessage("angal.common.excel.btn"));
			buttonExcel.setMnemonic(MessageBundle.getMnemonic("angal.common.excel.btn.key"));
			buttonExcel.addActionListener(actionEvent -> {
				dateFrom = fromDateChooser.getDate();
				dateTo = toDateChooser.getDate();
				isExcel = true;
				dispose();
			});
		}
		return buttonExcel;
	}

	private JPanel getDatesChoosers(LocalDate dateFrom, LocalDate dateTo) {
		if (datesChooserPanel == null) {
			datesChooserPanel = new JPanel();
			datesChooserPanel.add(new JLabel(MessageBundle.getMessage("angal.common.from.txt") + ":"));
			fromDateChooser = new GoodDateChooser(dateFrom);
			datesChooserPanel.add(fromDateChooser);
			datesChooserPanel.add(new JLabel(MessageBundle.getMessage("angal.common.to.txt") + ":"));
			toDateChooser = new GoodDateChooser(dateTo);
			datesChooserPanel.add(toDateChooser);
		}
		return datesChooserPanel;
	}

	public LocalDate getDateFrom() {
		return dateFrom;
	}

	public LocalDate getDateTo() {
		return dateTo;
	}

	public boolean isExcel() {
		return isExcel;
	}

	public boolean isCancel() {
		return isCancel;
	}

}
