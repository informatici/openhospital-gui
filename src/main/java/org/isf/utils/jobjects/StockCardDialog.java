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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.isf.generaldata.MessageBundle;
import org.isf.medicals.model.Medical;

/**
 * @author Mwithi
 */
public class StockCardDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextFieldSearchModel textField;
	private JPanel dateFromToPanel;
	private JPanel buttonsPanel;
	private JButton buttonOK;
	private JButton buttonExcel;
	private JButton buttonCancel;
	private GoodDateChooser dateFrom;
	private GoodDateChooser dateTo;
	private Medical medical;
	private boolean excel;
	private boolean cancel = false;

	public StockCardDialog(Frame owner, Medical medical, LocalDateTime from, LocalDateTime to) {
		super(owner, true);
		if (medical != null) {
			textField = new JTextFieldSearchModel(this, medical);
		} else {
			textField = new JTextFieldSearchModel(this, Medical.class);
		}
		dateFrom = new GoodDateChooser(from.toLocalDate());
		dateTo = new GoodDateChooser(to.toLocalDate());
		initAndShow();
	}

	private void initAndShow() {
		add(textField, BorderLayout.NORTH);
		add(getDateFromToPanel(), BorderLayout.CENTER);
		add(getButtonsPanel(), BorderLayout.SOUTH);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(MessageBundle.getMessage("angal.messagedialog.question.title"));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private JPanel getDateFromToPanel() {
		if (dateFromToPanel == null) {
			dateFromToPanel = new JPanel();
			FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
			layout.setHgap(5);
			dateFromToPanel.setLayout(layout);
			dateFromToPanel.add(new JLabel(MessageBundle.getMessage("angal.common.from.txt") + ':'));
			dateFromToPanel.add(dateFrom);
			dateFromToPanel.add(new JLabel(MessageBundle.getMessage("angal.common.to.txt") + ':'));
			dateFromToPanel.add(dateTo);
		}
		return dateFromToPanel;
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
				cancel = true;
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
				medical = (Medical) textField.getSelectedObject();
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
				medical = (Medical) textField.getSelectedObject();
				excel = true;
				dispose();
			});
		}
		return buttonExcel;
	}

	/**
	 * @return the med
	 */
	public Medical getMedical() {
		return medical;
	}

	/**
	 * @return the excel
	 */
	public boolean isExcel() {
		return excel;
	}

	/**
	 * @return the cancel
	 */
	public boolean isCancel() {
		return cancel;
	}

	public LocalDateTime getLocalDateTimeFrom() {
		return dateFrom.getDateStartOfDay();
	}

	public LocalDateTime getLocalDateTimeTo() {
		return dateTo.getDateStartOfDay();
	}

}
