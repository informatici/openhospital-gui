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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.isf.generaldata.MessageBundle;

/**
 * @author Mwithi
 */
public class StockLedgerDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel buttonsPanel;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JPanel dateFromToPanel;
	private GoodDateChooser dateFrom;
	private GoodDateChooser dateTo;
	private boolean cancel = false;

	public StockLedgerDialog(Frame owner, LocalDateTime from, LocalDateTime to) {
		super(owner, true);
		dateFrom = new GoodDateChooser(from.toLocalDate());
		dateTo = new GoodDateChooser(to.toLocalDate());
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
				int n = JOptionPane.showConfirmDialog(StockLedgerDialog.this,
						MessageBundle.getMessage("angal.common.thiscouldretrievealargeamountofdataproceed.msg"),
						MessageBundle.getMessage("angal.messagedialog.question.title"),
						JOptionPane.OK_CANCEL_OPTION);
				if (n != JOptionPane.OK_OPTION) {
					cancel = true;
				}
				dispose();
			});
		}
		return buttonOK;
	}

	public LocalDateTime getLocalDateTimeFrom() {
		return dateFrom.getDateStartOfDay();
	}

	public LocalDateTime getLocalDateTimeTo() {
		return dateTo.getDateStartOfDay();
	}

	/**
	 * @return the cancel
	 */
	public boolean isCancel() {
		return cancel;
	}

}
