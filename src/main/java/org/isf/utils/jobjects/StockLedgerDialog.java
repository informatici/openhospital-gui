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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.isf.generaldata.MessageBundle;

/**
 * @author Mwithi
 */
public class StockLedgerDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JFromDateToDateChooser dateRange;
	private JPanel buttonsPanel;
	private JButton buttonOK;
	private JButton buttonCancel;
	private Date dateFrom;
	private Date dateTo;
	private boolean cancel = false;

	public StockLedgerDialog(Frame owner) {
		super(owner, true);
		dateRange = new JFromDateToDateChooser();
		initAndShow();
	}

	public StockLedgerDialog(Frame owner, Date dateFrom, Date dateTo) {
		super(owner, true);
		dateRange = new JFromDateToDateChooser(dateFrom, dateTo);
		initAndShow();
	}

	private void initAndShow() {
		add(dateRange, BorderLayout.CENTER);
		add(getButtonsPanel(), BorderLayout.SOUTH);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle(MessageBundle.getMessage("angal.messagedialog.question.title"));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
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
					cancel = true;
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
					int n = JOptionPane.showConfirmDialog(StockLedgerDialog.this,
							MessageBundle.getMessage("angal.common.thiscouldretrievealargeamountofdataproceed.msg"),
							MessageBundle.getMessage("angal.messagedialog.question.title"),
							JOptionPane.OK_CANCEL_OPTION);
					if (n != JOptionPane.OK_OPTION) {
						cancel = true;
					}
					dateFrom = dateRange.getDateFrom();
					dateTo = dateRange.getDateTo();
					dispose();
				}
			});
		}
		return buttonOK;
	}

	/**
	 * @return the dateFrom
	 */
	public Date getDateFrom() {
		return dateFrom;
	}

	/**
	 * @return the dateTo
	 */
	public Date getDateTo() {
		return dateTo;
	}

	/**
	 * @return the cancel
	 */
	public boolean isCancel() {
		return cancel;
	}
}
