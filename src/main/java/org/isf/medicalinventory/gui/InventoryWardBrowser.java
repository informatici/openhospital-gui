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
package org.isf.medicalinventory.gui;

import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicalinventory.gui.InventoryWardEdit.InventoryListener;
import org.isf.medicalinventory.manager.MedicalInventoryManager;
import org.isf.medicalinventory.model.InventoryStatus;
import org.isf.medicalinventory.model.InventoryType;
import org.isf.medicalinventory.model.MedicalInventory;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.springframework.data.domain.Page;

public class InventoryWardBrowser extends ModalJFrame implements InventoryListener {

	private static final long serialVersionUID = 1L;

	private GoodDateChooser jCalendarTo;
	private GoodDateChooser jCalendarFrom;
	private LocalDateTime dateFrom = TimeTools.getDateToday0();
	private LocalDateTime dateTo = TimeTools.getDateToday24();
	private JLabel jLabelTo;
	private JLabel jLabelFrom;
	private JPanel panelHeader;
	private JPanel panelFooter;
	private JPanel panelContent;
	private JButton jButtonEdit;
	private JButton jButtonDelete;
	private JButton jButtonView;
	private JScrollPane scrollPaneInventory;
	private JTable jTableInventory;
	private String[] columsNames = {
			MessageBundle.getMessage("angal.inventory.referenceshow.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.ward.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.date.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventory.status.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.user.col").toUpperCase()
	};
	private int[] columwidth = { 150, 150, 100, 100, 150 };
	private Class[] columnClasses = { String.class, String.class, String.class, String.class, String.class };
	private boolean[] columnCentered = { false, true, true, true, true };
	private JComboBox<String> statusComboBox;
	private JLabel statusLabel;
	private JButton next;
	private JButton previous;
	private JComboBox<Integer> pagesComboBox = new JComboBox<>();
	private JLabel ofPagesLabel = new JLabel(MessageBundle.formatMessage("angal.common.pages.fmt.txt", 1));
	private static int PAGE_SIZE = 24;
	private int startIndex = 0;
	private int totalRows;
	private MedicalInventoryManager medicalInventoryManager = Context.getApplicationContext().getBean(MedicalInventoryManager.class);
	private WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private List<MedicalInventory> inventoryList;
	private Map<String, Ward> wardMap;

	public InventoryWardBrowser() {
		try {
			wardMap = wardBrowserManager.getWardMap();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		initComponents();
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(850, 550));
		setLocationRelativeTo(null); // center
		setTitle(MessageBundle.getMessage("angal.inventory.wardinventorybrowser.title"));

		panelHeader = getPanelHeader();
		getContentPane().add(panelHeader, BorderLayout.NORTH);

		panelContent = getPanelContent();
		getContentPane().add(panelContent, BorderLayout.CENTER);

		panelFooter = getPanelFooter();
		getContentPane().add(panelFooter, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
		pagesComboBox.setEditable(true);
		previous.setEnabled(false);
		next.setEnabled(false);
		next.addActionListener(actionEvent -> {
			if (!previous.isEnabled()) {
				previous.setEnabled(true);
			}
			startIndex += PAGE_SIZE;
			int page = startIndex / PAGE_SIZE + 1;
			jTableInventory.setModel(new InventoryBrowsingModel(page, PAGE_SIZE));
			if ((startIndex + PAGE_SIZE) > totalRows) {
				next.setEnabled(false);
			}
			pagesComboBox.setSelectedItem(page);
		});
		previous.addActionListener(actionEvent -> {
			if (!next.isEnabled()) {
				next.setEnabled(true);
			}
			startIndex -= PAGE_SIZE;
			int page = startIndex / PAGE_SIZE + 1;
			jTableInventory.setModel(new InventoryBrowsingModel(page, PAGE_SIZE));
			if (startIndex < PAGE_SIZE) {
				previous.setEnabled(false);
			}
			pagesComboBox.setSelectedItem(page);
		});
		pagesComboBox.addItemListener(itemEvent -> {
			int eventID = itemEvent.getStateChange();

			if (eventID == ItemEvent.SELECTED) {
				int pageNumber = (Integer) pagesComboBox.getSelectedItem();
				startIndex = (pageNumber - 1) * PAGE_SIZE;

				if ((startIndex + PAGE_SIZE) > totalRows) {
					next.setEnabled(false);
				} else {
					next.setEnabled(true);
				}
				if (pageNumber == 1) {
					previous.setEnabled(false);
				} else {
					previous.setEnabled(true);
				}
				pagesComboBox.setSelectedItem(pageNumber);
				jTableInventory.setModel(new InventoryBrowsingModel(pageNumber - 1, PAGE_SIZE));
				pagesComboBox.setEnabled(true);
			}
		});
	}

	private JPanel getPanelHeader() {
		if (panelHeader == null) {
			panelHeader = new JPanel();
			panelHeader.setBorder(new EmptyBorder(5, 0, 0, 5));
			GridBagLayout gbl_panelHeader = new GridBagLayout();
			gbl_panelHeader.columnWidths = new int[] { 65, 103, 69, 105, 77, 146, 0 };
			gbl_panelHeader.rowHeights = new int[] { 32, 0 };
			gbl_panelHeader.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
			gbl_panelHeader.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
			panelHeader.setLayout(gbl_panelHeader);
			GridBagConstraints gbc_jLabelFrom = new GridBagConstraints();
			gbc_jLabelFrom.fill = GridBagConstraints.HORIZONTAL;
			gbc_jLabelFrom.insets = new Insets(0, 0, 0, 5);
			gbc_jLabelFrom.gridx = 0;
			gbc_jLabelFrom.gridy = 0;
			panelHeader.add(getJLabelFrom(), gbc_jLabelFrom);
			GridBagConstraints gbc_jCalendarFrom = new GridBagConstraints();
			gbc_jCalendarFrom.fill = GridBagConstraints.HORIZONTAL;
			gbc_jCalendarFrom.insets = new Insets(0, 0, 0, 5);
			gbc_jCalendarFrom.gridx = 1;
			gbc_jCalendarFrom.gridy = 0;
			panelHeader.add(getJCalendarFrom(), gbc_jCalendarFrom);
			GridBagConstraints gbc_jLabelTo = new GridBagConstraints();
			gbc_jLabelTo.fill = GridBagConstraints.HORIZONTAL;
			gbc_jLabelTo.insets = new Insets(0, 0, 0, 5);
			gbc_jLabelTo.gridx = 2;
			gbc_jLabelTo.gridy = 0;
			panelHeader.add(getJLabelTo(), gbc_jLabelTo);
			GridBagConstraints gbc_jCalendarTo = new GridBagConstraints();
			gbc_jCalendarTo.fill = GridBagConstraints.HORIZONTAL;
			gbc_jCalendarTo.insets = new Insets(0, 0, 0, 5);
			gbc_jCalendarTo.gridx = 3;
			gbc_jCalendarTo.gridy = 0;
			panelHeader.add(getJCalendarTo(), gbc_jCalendarTo);
			GridBagConstraints gbc_statusLabel = new GridBagConstraints();
			gbc_statusLabel.fill = GridBagConstraints.HORIZONTAL;
			gbc_statusLabel.insets = new Insets(0, 0, 0, 5);
			gbc_statusLabel.gridx = 4;
			gbc_statusLabel.gridy = 0;
			panelHeader.add(getStatusLabel(), gbc_statusLabel);
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 5;
			gbc_comboBox.gridy = 0;
			panelHeader.add(getComboBox(), gbc_comboBox);
		}
		return panelHeader;
	}

	private JPanel getPanelContent() {
		if (panelContent == null) {
			panelContent = new JPanel();
			GridBagLayout gbl_panelContent = new GridBagLayout();
			gbl_panelContent.columnWidths = new int[] { 452, 0 };
			gbl_panelContent.rowHeights = new int[] { 402, 0 };
			gbl_panelContent.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
			gbl_panelContent.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
			panelContent.setLayout(gbl_panelContent);
			GridBagConstraints gbc_scrollPaneInventory = new GridBagConstraints();
			gbc_scrollPaneInventory.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneInventory.gridx = 0;
			gbc_scrollPaneInventory.gridy = 0;
			panelContent.add(getScrollPaneInventory(), gbc_scrollPaneInventory);
		}
		return panelContent;
	}

	private JPanel getPanelFooter() {
		if (panelFooter == null) {
			panelFooter = new JPanel();
			next = new JButton(MessageBundle.getMessage("angal.inventory.arrownext.btn"));
			next.setMnemonic(KeyEvent.VK_RIGHT);
			previous = new JButton(MessageBundle.getMessage("angal.inventory.arrowprevious.btn"));
			next.setMnemonic(KeyEvent.VK_LEFT);

			panelFooter.add(previous);
			panelFooter.add(pagesComboBox);
			panelFooter.add(ofPagesLabel);
			panelFooter.add(next);

			panelFooter.add(getNewButton());
			panelFooter.add(getViewButton());
			panelFooter.add(getEditButton());
			panelFooter.add(getDeleteButton());
			panelFooter.add(getCloseButton());
		}
		initializePagesCombo();
		return panelFooter;
	}

	private GoodDateChooser getJCalendarFrom() {
		if (jCalendarFrom == null) {
			jCalendarFrom = new GoodDateChooser(LocalDate.now(), false, false);
			jCalendarFrom.addDateChangeListener(event -> {
				dateFrom = jCalendarFrom.getDateStartOfDay();
				totalRows = medicalInventoryManager.getInventoryCount(InventoryType.ward.toString());
				startIndex = 0;
				previous.setEnabled(false);
				if (totalRows <= PAGE_SIZE) {
					next.setEnabled(false);
				} else {
					next.setEnabled(true);
				}
				jTableInventory.setModel(new InventoryBrowsingModel(startIndex, PAGE_SIZE));
				initializePagesCombo();
			});
		}
		return jCalendarFrom;
	}

	private GoodDateChooser getJCalendarTo() {
		if (jCalendarTo == null) {
			jCalendarTo = new GoodDateChooser(LocalDate.now(), false, false);
			jCalendarTo.addDateChangeListener(event -> {
				dateTo = jCalendarTo.getDateEndOfDay();
				totalRows = medicalInventoryManager.getInventoryCount(InventoryType.ward.toString());
				startIndex = 0;
				previous.setEnabled(false);
				if (totalRows <= PAGE_SIZE) {
					next.setEnabled(false);
				} else {
					next.setEnabled(true);
				}
				jTableInventory.setModel(new InventoryBrowsingModel(startIndex, PAGE_SIZE));
				initializePagesCombo();
			});
		}
		return jCalendarTo;
	}

	private JLabel getJLabelTo() {
		if (jLabelTo == null) {
			jLabelTo = new JLabel();
			jLabelTo.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelTo.setText(MessageBundle.getMessage("angal.common.dateto.label"));
		}
		return jLabelTo;
	}

	private JLabel getJLabelFrom() {
		if (jLabelFrom == null) {
			jLabelFrom = new JLabel();
			jLabelFrom.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelFrom.setText(MessageBundle.getMessage("angal.common.datefrom.label"));
		}
		return jLabelFrom;
	}

	private JButton getNewButton() {
		JButton jButtonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		jButtonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		jButtonNew.addActionListener(actionEvent -> {
			String draft = InventoryStatus.draft.toString();
			String validated = InventoryStatus.validated.toString();
			String inventoryType = InventoryType.ward.toString();
			List<MedicalInventory> draftMedicalInventories = new ArrayList<>();
			List<MedicalInventory> validatedMedicalInventories = new ArrayList<>();
			try {
				draftMedicalInventories = medicalInventoryManager.getMedicalInventoryByStatusAndInventoryType(draft, inventoryType);
				validatedMedicalInventories = medicalInventoryManager.getMedicalInventoryByStatusAndInventoryType(validated, inventoryType);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			Set<String> usedWardCodes = new HashSet<>();
			for (MedicalInventory inv : draftMedicalInventories) {
				usedWardCodes.add(inv.getWardCode());
			}
			for (MedicalInventory inv : validatedMedicalInventories) {
				usedWardCodes.add(inv.getWardCode());
			}
			boolean hasWardWithoutOpenInventory = wardMap.values().stream().anyMatch(w -> !usedWardCodes.contains(w.getCode()));
			if (hasWardWithoutOpenInventory) {
				InventoryWardEdit InventoryWardEdit = new InventoryWardEdit();
				InventoryWardEdit.addInventoryListener(this);
				InventoryWardEdit.showAsModal(this);
			} else {
				MessageDialog.error(null, "angal.inventory.allwardshaveaongoinginventory.msg");
				return;
			}
		});
		return jButtonNew;
	}

	private JButton getEditButton() {
		jButtonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		jButtonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		jButtonEdit.setEnabled(false);
		jButtonEdit.addActionListener(actionEvent -> {
			if (jTableInventory.getSelectedRowCount() > 1) {
				MessageDialog.error(this, "angal.inventory.pleaseselectonlyoneinventory.msg");
				return;
			}
			int selectedRow = jTableInventory.getSelectedRow();
			if (selectedRow == -1) {
				MessageDialog.error(this, "angal.inventory.pleaseselectinventory.msg");
				return;
			}
			MedicalInventory inventory = inventoryList.get(selectedRow);
			if (inventory.getStatus().equals(InventoryStatus.canceled.toString())) {
				MessageDialog.error(null, "angal.inventory.cancelednoteditable.msg");
				return;
			}

			if (inventory.getStatus().equals(InventoryStatus.done.toString())) {
				MessageDialog.error(null, "angal.inventory.donenoteditable.msg");
				return;
			}
			InventoryWardEdit InventoryWardEdit = new InventoryWardEdit(inventory, "update");
			InventoryWardEdit.addInventoryListener(this);
			InventoryWardEdit.showAsModal(this);
		});
		return jButtonEdit;
	}

	private JButton getViewButton() {
		jButtonView = new JButton(MessageBundle.getMessage("angal.inventory.view.btn"));
		jButtonView.setMnemonic(MessageBundle.getMnemonic("angal.inventory.view.btn.key"));
		jButtonView.setEnabled(false);
		jButtonView.addActionListener(actionEvent -> {
			if (jTableInventory.getSelectedRowCount() > 1) {
				MessageDialog.error(this, "angal.inventory.pleaseselectonlyoneinventory.msg");
				return;
			}
			int selectedRow = jTableInventory.getSelectedRow();
			if (selectedRow == -1) {
				MessageDialog.error(this, "angal.inventory.pleaseselectinventory.msg");
				return;
			}
			if (selectedRow > -1) {
				MedicalInventory inventory = inventoryList.get(selectedRow);
				InventoryWardEdit InventoryWardEdit = new InventoryWardEdit(inventory, "view");
				InventoryWardEdit.addInventoryListener(this);
				InventoryWardEdit.showAsModal(this);
			}
		});
		return jButtonView;
	}

	private JButton getDeleteButton() {
		jButtonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		jButtonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		jButtonDelete.setEnabled(false);

		jButtonDelete.addActionListener(actionEvent -> {
			if (jTableInventory.getSelectedRowCount() > 1) {
				MessageDialog.error(this, "angal.inventory.pleaseselectonlyoneinventory.msg");
				return;
			}
			int selectedRow = jTableInventory.getSelectedRow();
			if (selectedRow == -1) {
				MessageDialog.error(this, "angal.inventory.pleaseselectinventory.msg");
				return;
			}
			MedicalInventory inventory = inventoryList.get(selectedRow);
			String currentStatus = inventory.getStatus();
			if (currentStatus.equalsIgnoreCase(InventoryStatus.validated.toString()) || currentStatus.equalsIgnoreCase(InventoryStatus.draft.toString())) {
				int response = MessageDialog.yesNo(this, "angal.inventory.deletion.confirm.msg");
				if (response == JOptionPane.YES_OPTION) {
					try {
						medicalInventoryManager.deleteInventory(inventory);
						MessageDialog.info(this, "angal.inventory.deletion.success.msg");
						jTableInventory.setModel(new InventoryBrowsingModel(0, PAGE_SIZE));
					} catch (OHServiceException e) {
						MessageDialog.error(this, "angal.inventory.deletion.error.msg");
					}
				}
			} else {
				MessageDialog.error(this, "angal.inventory.deletion.error.msg");
			}
		});
		return jButtonDelete;
	}

	private JButton getCloseButton() {
		JButton jButtonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		jButtonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		jButtonClose.addActionListener(actionEvent -> dispose());
		return jButtonClose;
	}

	private JScrollPane getScrollPaneInventory() {
		if (scrollPaneInventory == null) {
			scrollPaneInventory = new JScrollPane();
			scrollPaneInventory.setViewportView(getJTableInventory());
		}
		return scrollPaneInventory;
	}

	private JTable getJTableInventory() {
		if (jTableInventory == null) {
			jTableInventory = new JTable();
			jTableInventory.setFillsViewportHeight(true);
			jTableInventory.setModel(new InventoryBrowsingModel(0, PAGE_SIZE));
			jTableInventory.setAutoCreateColumnsFromModel(false);
			for (int i = 0; i < columwidth.length; i++) {
				jTableInventory.getColumnModel().getColumn(i).setMinWidth(columwidth[i]);
				if (columnCentered[i]) {
					jTableInventory.getColumnModel().getColumn(i).setCellRenderer(new ColorCenterTableCellRenderer());
				} else {
					jTableInventory.getColumnModel().getColumn(i).setCellRenderer(new ColorTableCellRenderer());
				}
			}
			jTableInventory.getSelectionModel().addListSelectionListener(listSelectionEvent -> {

				if (listSelectionEvent.getValueIsAdjusting()) {
					int[] selectedRows = jTableInventory.getSelectedRows();
					if (selectedRows.length == 1) {
						int selectedRow = jTableInventory.getSelectedRow();
						MedicalInventory inventory = inventoryList.get(selectedRow);
						if (inventory.getStatus().equals(InventoryStatus.canceled.toString()) ||
							inventory.getStatus().equals(InventoryStatus.done.toString())) {
							jButtonEdit.setEnabled(false);
							jButtonDelete.setEnabled(false);
						} else {
							jButtonEdit.setEnabled(true);
							jButtonDelete.setEnabled(true);

						}
						jButtonView.setEnabled(true);
						jButtonDelete.setEnabled(true);
					} else {
						jButtonEdit.setEnabled(false);
						jButtonView.setEnabled(false);
						jButtonDelete.setEnabled(false);
					}
				}
			});
		}
		return jTableInventory;
	}

	class InventoryBrowsingModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public InventoryBrowsingModel(int page, int pageSize) {
			inventoryList = new ArrayList<>();
			String selectedStatus = statusComboBox.getSelectedIndex() > 0 ? statusComboBox.getSelectedItem().toString() : null;
			String status = medicalInventoryManager.getKeyByStatus(selectedStatus);
			String type = InventoryType.ward.toString();
			try {
				Page<MedicalInventory> medInventorypage = medicalInventoryManager.getMedicalInventoryByParamsPageable(dateFrom, dateTo, status, type, page,
					pageSize);
				inventoryList = medInventorypage.getContent();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public Class< ? > getColumnClass(int c) {
			return columnClasses[c];
		}

		@Override
		public int getRowCount() {
			if (inventoryList == null) {
				return 0;
			}
			return inventoryList.size();
		}

		@Override
		public String getColumnName(int c) {
			return columsNames[c];
		}

		@Override
		public int getColumnCount() {
			return columsNames.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			MedicalInventory medInvt = inventoryList.get(r);
			Ward ward = wardMap.get(medInvt.getWardCode());
			if (c == -1) {
				return medInvt;
			} else if (c == 0) {
				return medInvt.getInventoryReference();
			} else if (c == 1) {
				return ward;
			} else if (c == 2) {
				return medInvt.getInventoryDate().format(DATE_TIME_FORMATTER);
			} else if (c == 3) {
				return medInvt.getStatus();
			} else if (c == 4) {
				return medInvt.getUser();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}

	}

	private JComboBox<String> getComboBox() {
		if (statusComboBox == null) {
			statusComboBox = new JComboBox<>();
			statusComboBox.addItem(MessageBundle.getMessage("angal.common.all.txt"));
			for (InventoryStatus currentStatus : InventoryStatus.values()) {
				statusComboBox.addItem(MessageBundle.getMessage("angal.inventory.status." + currentStatus + ".txt"));
			}
			statusComboBox.addActionListener(actionEvent -> {
				totalRows = medicalInventoryManager.getInventoryCount(InventoryType.ward.toString());
				startIndex = 0;
				int page = 0;
				previous.setEnabled(false);
				if (totalRows <= PAGE_SIZE) {
					next.setEnabled(false);
				} else {
					next.setEnabled(true);
				}
				jTableInventory.setModel(new InventoryBrowsingModel(page, PAGE_SIZE));
				initializePagesCombo();
			});
		}
		return statusComboBox;
	}

	private JLabel getStatusLabel() {
		if (statusLabel == null) {
			statusLabel = new JLabel(MessageBundle.getMessage("angal.inventory.status.label"));
			statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return statusLabel;
	}

	public void initializePagesCombo() {
		// if totalRows = 0 we have at least 1 page
		int totalPages = Math.max(1, (int) Math.ceil((double) totalRows / PAGE_SIZE));
		for (int i = 1; i <= totalPages; i++) {
			pagesComboBox.addItem(i);
		}

		ofPagesLabel.setText(MessageBundle.formatMessage("angal.common.pages.fmt.txt", totalPages));
	}

	@Override
	public void inventoryCancelled(AWTEvent e) {
		jTableInventory.setModel(new InventoryBrowsingModel(0, PAGE_SIZE));
	}

	@Override
	public void inventoryInserted(AWTEvent e) {
		jTableInventory.setModel(new InventoryBrowsingModel(0, PAGE_SIZE));
	}

	@Override
	public void inventoryUpdated(AWTEvent e) {
		jTableInventory.setModel(new InventoryBrowsingModel(0, PAGE_SIZE));
	}

	class ColorTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			formatCellByInventoryStatus(table, row, cell);
			return cell;
		}
	}

	class ColorCenterTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(CENTER);
			formatCellByInventoryStatus(table, row, cell);
			return cell;
		}
	}

	private void formatCellByInventoryStatus(JTable table, int row, Component cell) {
		int statusColumn = table.getColumnModel().getColumnIndex(MessageBundle.getMessage("angal.common.status.txt").toUpperCase());
		String status = (String) table.getValueAt(row, statusColumn);
		if (status.equals(InventoryStatus.draft.toString()) || status.equals(InventoryStatus.validated.toString())) {
			cell.setForeground(Color.BLUE);
		} else {
			cell.setForeground(Color.BLACK);
		}
	}
}
