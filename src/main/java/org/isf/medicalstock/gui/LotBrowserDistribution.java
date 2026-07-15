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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.medicalstockward.manager.MovWardBrowserManager;
import org.isf.medicalstockward.model.MedicalWard;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

/**
 * Modal dialog to show how the remaining quantity of a {@link Lot} is distributed within the hospital: one row for the
 * main store, one row for each ward holding a non-zero quantity of the lot and a final row with the overall total. All
 * the values are shown read-only and computed live from the movements when the dialog is opened; if the quantities
 * cannot be loaded, the table is left empty rather than showing a partial distribution.
 */
public class LotBrowserDistribution extends JDialog {

	private static final long serialVersionUID = 1L;

	private final Lot lot;
	private final List<Object[]> distributionRows = new ArrayList<>();
	private final String[] columns = {
			MessageBundle.getMessage("angal.common.ward.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.quantity.txt").toUpperCase()
	};
	private final int[] columnWidth = { 250, 100 };

	private final MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);
	private final MovWardBrowserManager movWardBrowserManager = Context.getApplicationContext().getBean(MovWardBrowserManager.class);
	private final WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);

	public LotBrowserDistribution(JFrame owner, Lot lot) {
		super(owner, true);
		this.lot = lot;
		loadDistribution();
		initialize();
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.medicalstock.lotdistribution.title"));
		setContentPane(getContentPanel());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
	}

	private void loadDistribution() {
		if (lot.getMedical() == null) {
			return;
		}
		List<Object[]> rows = new ArrayList<>();
		try {
			Lot freshLot = getFreshLot();
			rows.add(new Object[] { MessageBundle.getMessage("angal.medicalstock.lotdistribution.mainstore.txt"), formatQuantity(freshLot.getMainStoreQuantity()) });
			for (Ward ward : wardBrowserManager.getWards()) {
				BigDecimal quantity = getQuantityInWard(ward);
				if (quantity.compareTo(BigDecimal.ZERO) != 0) {
					rows.add(new Object[] { ward.getDescription(), formatQuantity(quantity) });
				}
			}
			rows.add(new Object[] { MessageBundle.getMessage("angal.common.total.txt"), formatQuantity(freshLot.getOverallQuantity()) });
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
			return;
		}
		distributionRows.addAll(rows);
	}

	private Lot getFreshLot() throws OHServiceException {
		for (Lot medicalLot : movStockInsertingManager.getLotByMedical(lot.getMedical(), false)) {
			if (medicalLot.getCode().equals(lot.getCode())) {
				return medicalLot;
			}
		}
		return lot;
	}

	private BigDecimal getQuantityInWard(Ward ward) throws OHServiceException {
		for (MedicalWard medicalWard : movWardBrowserManager.getMedicalsWard(ward.getCode(), lot.getMedical().getCode(), false)) {
			if (medicalWard.getLot() != null && lot.getCode().equals(medicalWard.getLot().getCode())) {
				return medicalWard.getQty();
			}
		}
		return BigDecimal.ZERO;
	}

	private String formatQuantity(Number quantity) {
		double value = quantity.doubleValue();
		if (value == Math.rint(value)) {
			return String.valueOf((long) value);
		}
		return String.valueOf(value);
	}

	private JPanel getContentPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(getLotPanel(), BorderLayout.NORTH);
		panel.add(new JScrollPane(getJTable()), BorderLayout.CENTER);
		panel.add(getButtonPanel(), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel getLotPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		LotBrowserEdit.addLotHeaderRows(panel, lot);
		return panel;
	}

	private JTable getJTable() {
		JTable jTable = new JTable(new LotDistributionModel());
		jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		int totalWidth = 0;
		for (int i = 0; i < columnWidth.length; i++) {
			jTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i]);
			totalWidth += columnWidth[i];
		}
		jTable.setPreferredScrollableViewportSize(new Dimension(totalWidth, 150));
		return jTable;
	}

	private JPanel getButtonPanel() {
		JPanel panel = new JPanel();
		panel.add(getCloseButton());
		return panel;
	}

	private JButton getCloseButton() {
		JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		closeButton.addActionListener(actionEvent -> dispose());
		return closeButton;
	}

	class LotDistributionModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return distributionRows.size();
		}

		@Override
		public String getColumnName(int c) {
			return columns[c];
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			return distributionRows.get(r)[c];
		}

		@Override
		public boolean isCellEditable(int r, int c) {
			return false;
		}
	}
}
