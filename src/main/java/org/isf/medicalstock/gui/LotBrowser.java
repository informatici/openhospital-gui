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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.gui.LotBrowserEdit.LotListener;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

/**
 * Standalone browser to view and edit the lots of a selected medical.
 * <p>
 * The lot id is shown read-only (it is the primary key and is referenced by movements); the preparation date, expiring
 * (due) date and unit cost are editable through {@link LotBrowserEdit}. The current quantity (main store, wards and
 * overall) and the order of creation are shown read-only and computed live from the movements. The distribution of the
 * remaining quantity within the hospital is shown through {@link LotBrowserDistribution}.
 * <p>
 * The search field filters the pharmaceutical combo box by code or description; when the typed text is an existing lot
 * id, the lot's pharmaceutical is selected and the lot is highlighted in the table.
 */
public class LotBrowser extends ModalJFrame implements LotListener {

	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private List<Medical> medicals;
	private List<Lot> lotList = new ArrayList<>();
	private final String[] columns = {
			MessageBundle.getMessage("angal.medicalstock.creationorder.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.lotid").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.prepdate.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.duedate.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.cost.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.mainstorequantity.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.wardsquantity.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.overallquantity.col").toUpperCase()
	};
	private final int[] columnWidth = { 110, 140, 95, 95, 70, 120, 90, 100 };

	private JComboBox<Medical> medicalBox;
	private JTextField searchTextField;
	private JButton searchButton;
	private JTable jTable;
	private LotBrowserModel model;
	private JButton editButton;
	private JButton distributionButton;
	private JButton closeButton;
	private int selectedRow = -1;

	private final MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private final MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);
	private final JFrame myFrame;

	public LotBrowser() {
		super();
		myFrame = this;
		initialize();
		setVisible(true);
	}

	private void initialize() {
		setTitle(MessageBundle.getMessage("angal.medicalstock.lotbrowser.title"));
		setContentPane(getContentPanel());
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getContentPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(getSelectionPanel(), BorderLayout.NORTH);
		panel.add(new JScrollPane(getJTable()), BorderLayout.CENTER);
		panel.add(getButtonPanel(), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel getSelectionPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.pharmaceutical") + ':'));
		panel.add(getMedicalBox());
		panel.add(getSearchTextField());
		panel.add(getSearchButton());
		return panel;
	}

	private JTextField getSearchTextField() {
		if (searchTextField == null) {
			searchTextField = new JTextField(10);
			searchTextField.setToolTipText(MessageBundle.getMessage("angal.medicalstock.searchbypharmaceuticalorlotid.tooltip"));
			searchTextField.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						searchButton.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});
		}
		return searchTextField;
	}

	private JButton getSearchButton() {
		if (searchButton == null) {
			searchButton = new JButton();
			searchButton.setPreferredSize(new Dimension(20, 20));
			searchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			searchButton.addActionListener(actionEvent -> search());
		}
		return searchButton;
	}

	private void search() {
		String text = searchTextField.getText().trim();
		Lot lot = null;
		if (!text.isEmpty()) {
			try {
				lot = movStockInsertingManager.getLot(text);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		try {
			medicals = medicalBrowsingManager.getMedicalsSortedByName();
		} catch (OHServiceException e) {
			medicals = new ArrayList<>();
			OHServiceExceptionUtil.showMessages(e);
		}
		medicalBox.removeAllItems();
		if (lot != null) {
			for (Medical medical : medicals) {
				medicalBox.addItem(medical);
			}
			selectLot(lot);
			return;
		}
		for (Medical medical : getSearchMedicalsResults(text, medicals)) {
			medicalBox.addItem(medical);
		}
	}

	private void selectLot(Lot lot) {
		for (Medical medical : medicals) {
			if (medical.getCode().equals(lot.getMedical().getCode())) {
				medicalBox.setSelectedItem(medical); // triggers loadLots()
				break;
			}
		}
		for (int row = 0; row < lotList.size(); row++) {
			if (lotList.get(row).getCode().equals(lot.getCode())) {
				jTable.setRowSelectionInterval(row, row);
				jTable.scrollRectToVisible(jTable.getCellRect(row, 0, true));
				break;
			}
		}
	}

	private List<Medical> getSearchMedicalsResults(String s, List<Medical> medicalsList) {
		String query = s.trim();
		List<Medical> results = new ArrayList<>();
		for (Medical medoc : medicalsList) {
			if (!query.equals("")) {
				String[] patterns = query.split(" ");
				String code = medoc.getProdCode().toLowerCase();
				String description = medoc.getDescription().toLowerCase();
				boolean patternFound = false;
				for (String pattern : patterns) {
					if (code.contains(pattern.toLowerCase()) || description.contains(pattern.toLowerCase())) {
						patternFound = true;
						// It is sufficient that only one pattern matches the query
						break;
					}
				}
				if (patternFound) {
					results.add(medoc);
				}
			} else {
				results.add(medoc);
			}
		}
		return results;
	}

	private JComboBox<Medical> getMedicalBox() {
		if (medicalBox == null) {
			medicalBox = new JComboBox<>();
			try {
				medicals = medicalBrowsingManager.getMedicalsSortedByName();
			} catch (OHServiceException e) {
				medicals = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			for (Medical medical : medicals) {
				medicalBox.addItem(medical);
			}
			medicalBox.addActionListener(actionEvent -> loadLots());
		}
		return medicalBox;
	}

	private void loadLots() {
		Medical medical = (Medical) medicalBox.getSelectedItem();
		lotList = new ArrayList<>();
		if (medical != null) {
			try {
				lotList = movStockInsertingManager.getLotByMedical(medical, false);
			} catch (OHServiceException e) {
				lotList = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			lotList.sort(Comparator.comparing(Lot::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())));
		}
		selectedRow = -1;
		model.fireTableDataChanged();
		jTable.updateUI();
	}

	private JTable getJTable() {
		if (jTable == null) {
			model = new LotBrowserModel();
			jTable = new JTable(model);
			jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			int totalWidth = 0;
			for (int i = 0; i < columnWidth.length; i++) {
				jTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i]);
				totalWidth += columnWidth[i];
			}
			jTable.setPreferredScrollableViewportSize(new Dimension(totalWidth, 220));
			loadLots();
		}
		return jTable;
	}

	private JPanel getButtonPanel() {
		JPanel panel = new JPanel();
		panel.add(getEditButton());
		panel.add(getDistributionButton());
		panel.add(getCloseButton());
		return panel;
	}

	private JButton getEditButton() {
		if (editButton == null) {
			editButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			editButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			editButton.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
					return;
				}
				selectedRow = jTable.getSelectedRow();
				Lot lot = lotList.get(selectedRow);
				LotBrowserEdit editRecord = new LotBrowserEdit(myFrame, lot);
				editRecord.addLotListener(this);
				editRecord.setVisible(true);
			});
		}
		return editButton;
	}

	private JButton getDistributionButton() {
		if (distributionButton == null) {
			distributionButton = new JButton(MessageBundle.getMessage("angal.medicalstock.lotdistribution.btn"));
			distributionButton.setMnemonic(MessageBundle.getMnemonic("angal.medicalstock.lotdistribution.btn.key"));
			distributionButton.addActionListener(actionEvent -> {
				if (jTable.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
					return;
				}
				Lot lot = lotList.get(jTable.getSelectedRow());
				new LotBrowserDistribution(myFrame, lot).setVisible(true);
			});
		}
		return distributionButton;
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			closeButton.addActionListener(actionEvent -> dispose());
		}
		return closeButton;
	}

	@Override
	public void lotUpdated(AWTEvent e) {
		model.fireTableDataChanged();
		jTable.updateUI();
		if (selectedRow > -1 && selectedRow < jTable.getRowCount()) {
			jTable.setRowSelectionInterval(selectedRow, selectedRow);
		}
	}

	private String formatDate(LocalDateTime date) {
		return date != null ? date.format(DATE_FORMAT) : "";
	}

	class LotBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return lotList == null ? 0 : lotList.size();
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
			Lot lot = lotList.get(r);
			switch (c) {
				case 0:
					return r + 1;
				case 1:
					return lot.getCode();
				case 2:
					return formatDate(lot.getPreparationDate());
				case 3:
					return formatDate(lot.getDueDate());
				case 4:
					return lot.getCost();
				case 5:
					return lot.getMainStoreQuantity();
				case 6:
					return lot.getWardsTotalQuantity();
				case 7:
					return lot.getOverallQuantity();
				default:
					return null;
			}
		}

		@Override
		public boolean isCellEditable(int r, int c) {
			return false;
		}
	}
}
