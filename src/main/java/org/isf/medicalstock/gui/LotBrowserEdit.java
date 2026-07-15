/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2025 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.medicalstock.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EventListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;

/**
 * Modal dialog to edit the editable fields of a {@link Lot}: preparation date, expiring (due) date and unit cost. The
 * lot id, the related medical and the current quantities are shown read-only. The update is delegated to
 * {@link MovStockInsertingManager#updateLot(Lot)}, which enforces the validation (dates and non-negative cost).
 */
public class LotBrowserEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	public interface LotListener extends EventListener {

		void lotUpdated(AWTEvent e);
	}

	private final EventListenerList lotListeners = new EventListenerList();

	public void addLotListener(LotListener listener) {
		lotListeners.add(LotListener.class, listener);
	}

	public void removeLotListener(LotListener listener) {
		lotListeners.remove(LotListener.class, listener);
	}

	private void fireLotUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};
		EventListener[] listeners = lotListeners.getListeners(LotListener.class);
		for (EventListener listener : listeners) {
			((LotListener) listener).lotUpdated(event);
		}
	}

	private final Lot lot;
	private GoodDateChooser preparationDateChooser;
	private GoodDateChooser dueDateChooser;
	private JTextField costTextField;

	private final MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);

	public LotBrowserEdit(JFrame owner, Lot lot) {
		super(owner, true);
		this.lot = lot;
		initialize();
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.medicalstock.editlot.title"));
		setContentPane(getContentPane0());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getContentPane0() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(getDataPanel(), BorderLayout.CENTER);
		panel.add(getButtonPanel(), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel getDataPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		LocalDate preparationDate = lot.getPreparationDate() != null ? lot.getPreparationDate().toLocalDate() : null;
		LocalDate dueDate = lot.getDueDate() != null ? lot.getDueDate().toLocalDate() : null;
		preparationDateChooser = new GoodDateChooser(preparationDate);
		dueDateChooser = new GoodDateChooser(dueDate, true, false);
		costTextField = new JTextField(10);
		if (lot.getCost() != null) {
			costTextField.setText(lot.getCost().toPlainString());
		}

		addLotHeaderRows(panel, lot);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.prepdate.col") + ':'));
		panel.add(preparationDateChooser);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.duedate.col") + ':'));
		panel.add(dueDateChooser);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.cost.col") + ':'));
		panel.add(costTextField);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.overallquantity.col") + ':'));
		panel.add(new JLabel(String.valueOf(lot.getOverallQuantity())));
		return panel;
	}

	/**
	 * Adds the pharmaceutical and lot id header rows shared by the lot dialogs.
	 */
	static void addLotHeaderRows(JPanel panel, Lot lot) {
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.pharmaceutical") + ':'));
		panel.add(new JLabel(lot.getMedical() != null ? lot.getMedical().getDescription() : ""));
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.lotid") + ':'));
		panel.add(new JLabel(lot.getCode()));
	}

	private JPanel getButtonPanel() {
		JPanel panel = new JPanel();
		panel.add(getOkButton());
		panel.add(getCancelButton());
		return panel;
	}

	private JButton getOkButton() {
		JButton okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
		okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
		okButton.addActionListener(actionEvent -> {
			if (preparationDateChooser.getDate() == null) {
				MessageDialog.error(this, "angal.medicalstock.insertavalidpreparationdate.msg");
				return;
			}
			if (dueDateChooser.getDate() == null) {
				MessageDialog.error(this, "angal.medicalstock.insertavalidduedate.msg");
				return;
			}
			BigDecimal cost = null;
			String costText = costTextField.getText().trim();
			if (!costText.isEmpty()) {
				try {
					cost = new BigDecimal(costText);
				} catch (NumberFormatException numberFormatException) {
					MessageDialog.error(this, "angal.medicalstock.multiplecharging.pleaseinsertavalidvalue");
					return;
				}
			}
			lot.setPreparationDate(preparationDateChooser.getDateStartOfDay());
			lot.setDueDate(dueDateChooser.getDateEndOfDay());
			lot.setCost(cost);
			try {
				movStockInsertingManager.updateLot(lot);
				fireLotUpdated();
				dispose();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		});
		return okButton;
	}

	private JButton getCancelButton() {
		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		cancelButton.addActionListener(actionEvent -> dispose());
		return cancelButton;
	}
}
