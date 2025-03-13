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

import static org.isf.utils.Constants.DATE_FORMATTER;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicalinventory.manager.MedicalInventoryManager;
import org.isf.medicalinventory.manager.MedicalInventoryRowManager;
import org.isf.medicalinventory.model.InventoryStatus;
import org.isf.medicalinventory.model.InventoryType;
import org.isf.medicalinventory.model.MedicalInventory;
import org.isf.medicalinventory.model.MedicalInventoryRow;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.medicalstockward.manager.MovWardBrowserManager;
import org.isf.medicalstockward.model.MedicalWard;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.stat.gui.report.GenericReportPharmaceuticalInventory;
import org.isf.utils.db.NormalizeString;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.RequestFocusListener;
import org.isf.utils.jobjects.TextPrompt;
import org.isf.utils.jobjects.TextPrompt.Show;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

public class InventoryWardEdit extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private static EventListenerList InventoryListeners = new EventListenerList();

	public interface InventoryListener extends EventListener {

		void InventoryInserted(AWTEvent e);

		void InventoryUpdated(AWTEvent e);

		void InventoryCancelled(AWTEvent e);
	}

	public static void addInventoryListener(InventoryListener listener) {
		InventoryListeners.add(InventoryListener.class, listener);
	}

	public static void removeInventoryListener(InventoryListener listener) {
		InventoryListeners.remove(InventoryListener.class, listener);
	}

	private void fireInventoryUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = InventoryListeners.getListeners(InventoryListener.class);
		for (EventListener listener : listeners) {
			((InventoryListener) listener).InventoryUpdated(event);
		}
		jTableInventoryRow.updateUI();
	}

	private void fireInventoryInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = InventoryListeners.getListeners(InventoryListener.class);
		for (EventListener listener : listeners) {
			((InventoryListener) listener).InventoryInserted(event);
		}
		jTableInventoryRow.updateUI();
	}

	private GoodDateChooser jCalendarInventory;
	private LocalDateTime dateInventory = TimeTools.getNow();
	private JPanel panelHeader;
	private JPanel panelFooter;
	private JPanel panelContent;
	private JButton closeButton;
	private JButton printButton;
	private JButton deleteButton;
	private JButton saveButton;
	private JButton resetButton;
	private JButton lotButton;
	private JButton validateButton;
	private JButton confirmButton;
	private JScrollPane scrollPaneInventory;
	private JTable jTableInventoryRow;
	private List<MedicalInventoryRow> inventoryRowList = new ArrayList<>();
	private List<MedicalInventoryRow> inventoryRowSearchList = new ArrayList<>();
	private List<MedicalInventoryRow> inventoryRowsToDelete = new ArrayList<>();
	private List<MedicalInventoryRow> inventoryRowListAdded = new ArrayList<>();
	private List<Lot> lotsSaved = new ArrayList<>();
	private HashMap<Integer, Lot> lotsDeleted = new HashMap<>();
	private String[] columsNames = { MessageBundle.getMessage("angal.inventory.id.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.inventory.medical.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventory.newlot.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventory.lotcode.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.duedate.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventory.theoreticalqty.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventory.realqty.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventory.unitcost.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventory.totalcost.col").toUpperCase()
	};
	private int[] columwidth = { 50, 50, 200, 100, 100, 100, 100, 80, 80, 80 };
	private boolean[] columnEditable = { false, false, false, false, false, false, false, true, false, false };
	private boolean[] columnEditableView = { false, false, false, false, false, false, false, false, false, false };
	private boolean[] columnVisible = { false, true, true, true, !GeneralData.AUTOMATICLOT_IN, true, true, true, GeneralData.LOTWITHCOST,
			GeneralData.LOTWITHCOST };
	private boolean[] columnCentered = { false, false, false, true, true, true, true, true, true, true };
	private boolean[] columnDecimalNumber = { false, false, false, false, false, false, false, false, true, true };
	private Class< ? >[] columnsClasses = { String.class, Integer.class, String.class, String.class, String.class, LocalDate.class, Integer.class,
			Integer.class, BigDecimal.class, BigDecimal.class };
	private final String[] lotSelectionColumnNames = {
			MessageBundle.getMessage("angal.medicalstock.lotid").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.prepdate.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.duedate").toUpperCase()
	};
	private final Class[] lotSelectionColumnClasses = { String.class, String.class, String.class };
	private MedicalInventory inventory;
	private JLabel specificProduct;
	private JButton selectButton;
	private JLabel dateInventoryLabel;
	private JTextField codeTextField;
	private String code;
	private String mode;
	private JLabel statusLabel;
	private String wardId = "";
	private JLabel referenceLabel;
	private JTextField referenceTextField;
	private JTextField jTextFieldEditor;
	private JLabel wardLabel;
	private JComboBox<Ward> wardComboBox;
	private Ward wardSelected;
	private boolean selectAll;
	private String newReference;
	private WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private MedicalInventoryManager medicalInventoryManager = Context.getApplicationContext()
		.getBean(MedicalInventoryManager.class);
	private MedicalInventoryRowManager medicalInventoryRowManager = Context.getApplicationContext()
		.getBean(MedicalInventoryRowManager.class);
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext()
		.getBean(MedicalBrowsingManager.class);
	private MovWardBrowserManager movWardBrowserManager = Context.getApplicationContext()
		.getBean(MovWardBrowserManager.class);
	private MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);

	public InventoryWardEdit() {
		mode = "new";
		initComponents();
		disabledSomeComponents();
	}

	public InventoryWardEdit(MedicalInventory inventory, String mod) {
		this.inventory = inventory;
		wardId = this.inventory.getWard();
		mode = mod;
		initComponents();
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setMinimumSize(new DimensionUIResource(950, 580));
		setLocationRelativeTo(null);
		if (mode.equals("new")) {
			setTitle(MessageBundle.getMessage("angal.inventory.newinventory.title"));
		}
		if (mode.equals("view")) {
			setTitle(MessageBundle.getMessage("angal.inventory.viewinventory.title"));
		}
		if (mode.equals("update")) {
			setTitle(MessageBundle.getMessage("angal.inventory.editinventory.title"));
		}
		getContentPane().setLayout(new BorderLayout());
		panelHeader = getPanelHeader();
		getContentPane().add(panelHeader, BorderLayout.NORTH);
		panelContent = getPanelContent();
		getContentPane().add(panelContent, BorderLayout.CENTER);
		panelFooter = getPanelFooter();
		getContentPane().add(panelFooter, BorderLayout.SOUTH);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				closeButton.doClick();
			}
		});
		if (mode.equals("view")) {
			saveButton.setVisible(false);
			validateButton.setVisible(false);
			confirmButton.setVisible(false);
			deleteButton.setVisible(false);
			columnEditable = columnEditableView;
			resetButton.setVisible(false);
			referenceTextField.setVisible(false);
			jCalendarInventory.setEnabled(false);
			selectButton.setEnabled(false);
			wardComboBox.setEnabled(false);
			printButton.setVisible(true);
			lotButton.setVisible(false);
			codeTextField.setEditable(false);

		} else {
			saveButton.setVisible(true);
			validateButton.setVisible(true);
			deleteButton.setVisible(true);
			codeTextField.setEditable(true);
			resetButton.setVisible(true);
			referenceTextField.setEditable(true);
			jCalendarInventory.setEnabled(true);
			selectButton.setEnabled(true);
			wardComboBox.setEnabled(true);
			lotButton.setVisible(true);
			if (inventory != null && inventory.getStatus().equals(InventoryStatus.validated.toString())) {
				confirmButton.setEnabled(true);
			} else {
				confirmButton.setEnabled(false);
			}
			printButton.setVisible(false);
		}
	}

	private JPanel getPanelHeader() {
		if (panelHeader == null) {
			panelHeader = new JPanel();
			panelHeader.setBorder(new EmptyBorder(5, 0, 5, 0));
			GridBagLayout gbl_panelHeader = new GridBagLayout();
			gbl_panelHeader.columnWidths = new int[] { 159, 191, 192, 218, 218, 0 };
			gbl_panelHeader.rowHeights = new int[] { 30, 30, 0 };
			gbl_panelHeader.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			gbl_panelHeader.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panelHeader.setLayout(gbl_panelHeader);
			GridBagConstraints gbc_wardLabel = new GridBagConstraints();
			gbc_wardLabel.anchor = GridBagConstraints.EAST;
			gbc_wardLabel.insets = new Insets(0, 0, 5, 5);
			gbc_wardLabel.gridx = 0;
			gbc_wardLabel.gridy = 0;
			panelHeader.add(getWardLabel(), gbc_wardLabel);
			GridBagConstraints gbc_wardComboBox = new GridBagConstraints();
			gbc_wardComboBox.insets = new Insets(0, 0, 5, 5);
			gbc_wardComboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_wardComboBox.gridx = 1;
			gbc_wardComboBox.gridy = 0;
			panelHeader.add(getWardComboBox(), gbc_wardComboBox);
			GridBagConstraints gbc_dateInventoryLabel = new GridBagConstraints();
			gbc_dateInventoryLabel.anchor = GridBagConstraints.EAST;
			gbc_dateInventoryLabel.insets = new Insets(0, 0, 5, 5);
			gbc_dateInventoryLabel.gridx = 0;
			gbc_dateInventoryLabel.gridy = 1;
			panelHeader.add(getDateInventoryLabel(), gbc_dateInventoryLabel);

			GridBagConstraints gbc_jCalendarInventory = new GridBagConstraints();
			gbc_jCalendarInventory.fill = GridBagConstraints.HORIZONTAL;
			gbc_jCalendarInventory.insets = new Insets(0, 0, 5, 5);
			gbc_jCalendarInventory.gridx = 1;
			gbc_jCalendarInventory.gridy = 1;
			panelHeader.add(getJCalendarFrom(), gbc_jCalendarInventory);
			GridBagConstraints gbc_referenceLabel = new GridBagConstraints();
			gbc_referenceLabel.anchor = GridBagConstraints.EAST;
			gbc_referenceLabel.insets = new Insets(0, 0, 5, 5);
			gbc_referenceLabel.gridx = 2;
			gbc_referenceLabel.gridy = 1;
			panelHeader.add(getReferenceLabel(), gbc_referenceLabel);
			GridBagConstraints gbc_referenceTextField = new GridBagConstraints();
			gbc_referenceTextField.insets = new Insets(0, 0, 5, 5);
			gbc_referenceTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_referenceTextField.gridx = 3;
			gbc_referenceTextField.gridy = 1;
			panelHeader.add(getReferenceTextField(), gbc_referenceTextField);
			GridBagConstraints gbc_statusLabel = new GridBagConstraints();
			gbc_statusLabel.anchor = GridBagConstraints.CENTER;
			gbc_statusLabel.insets = new Insets(0, 0, 5, 5);
			gbc_statusLabel.gridx = 4;
			gbc_statusLabel.gridy = 1;
			gbc_statusLabel.gridheight = 3;
			panelHeader.add(getStatusLabel(), gbc_statusLabel);

			GridBagConstraints gbc_specificRadio = new GridBagConstraints();
			gbc_specificRadio.anchor = GridBagConstraints.EAST;
			gbc_specificRadio.insets = new Insets(0, 0, 0, 5);
			gbc_specificRadio.gridx = 0;
			gbc_specificRadio.gridy = 4;
			panelHeader.add(getSpecificProductLabel(), gbc_specificRadio);
			GridBagConstraints gbc_codeTextField = new GridBagConstraints();
			gbc_codeTextField.insets = new Insets(0, 0, 0, 5);
			gbc_codeTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_codeTextField.gridx = 1;
			gbc_codeTextField.gridy = 4;
			panelHeader.add(getCodeTextField(), gbc_codeTextField);
			GridBagConstraints gbc_selectButton = new GridBagConstraints();
			gbc_selectButton.anchor = GridBagConstraints.EAST;
			gbc_selectButton.insets = new Insets(0, 0, 0, 5);
			gbc_selectButton.gridx = 2;
			gbc_selectButton.gridy = 4;
			panelHeader.add(getSelectButton(), gbc_selectButton);
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
			panelFooter.add(getSaveButton());
			panelFooter.add(getLotButton());
			panelFooter.add(getDeleteButton());
			panelFooter.add(getCleanTableButton());
			panelFooter.add(getValidateButton());
			panelFooter.add(getConfirmButton());
			panelFooter.add(getPrintButton());
			panelFooter.add(getCloseButton());
		}
		return panelFooter;
	}

	private GoodDateChooser getJCalendarFrom() {
		if (jCalendarInventory == null) {

			jCalendarInventory = new GoodDateChooser(LocalDate.now(), false, false);
			if (inventory != null) {
				jCalendarInventory.setDate(inventory.getInventoryDate().toLocalDate());
				dateInventory = inventory.getInventoryDate();
			}
			jCalendarInventory.addDateChangeListener(event -> {
				dateInventory = jCalendarInventory.getDate().atStartOfDay();
			});
		}
		return jCalendarInventory;
	}

	private JButton getSelectButton() {
		if (selectButton == null) {
			selectButton = new JButton(MessageBundle.getMessage("angal.inventory.allproduct.btn"));
			selectButton.setSelected(inventory != null);
			selectButton.addActionListener(actionEvent -> {
				if (!selectAll) {
					if (!inventoryRowSearchList.isEmpty()) {
						int info = MessageDialog.yesNo(null, "angal.inventory.doyouwanttoaddallnotyetlistedproducts.msg");
						if (info != JOptionPane.YES_OPTION) {
							return;
						}
					}
					try {
						jTableInventoryRow.setModel(new InventoryRowModel(true));
						if (inventory != null) {
							inventory.setStatus(InventoryStatus.draft.toString());
						}
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
					fireInventoryUpdated();
					code = null;
				} else {
					MessageDialog.info(null, "angal.inventory.youhavealreadyaddedallproduct.msg");
				}
			});
		}
		return selectButton;
	}

	private JButton getSaveButton() {
		saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
		saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
		saveButton.addActionListener(actionEvent -> {
			String status = InventoryStatus.draft.toString();
			String user = UserBrowsingManager.getCurrentUser();
			if (inventoryRowSearchList == null || inventoryRowSearchList.isEmpty()) {
				MessageDialog.error(null, "angal.inventory.cannotsaveinventorywithoutproducts.msg");
				return;
			}
			try {
				if (!lotsDeleted.isEmpty() || !inventoryRowsToDelete.isEmpty()) {
					for (Map.Entry<Integer, Lot> entry : lotsDeleted.entrySet()) {
						MedicalInventoryRow invRow = medicalInventoryRowManager.getMedicalInventoryRowById(entry.getKey());
						if (invRow != null) {
							invRow.setLot(null);
							medicalInventoryRowManager.updateMedicalInventoryRow(invRow);
							movStockInsertingManager.deleteLot(entry.getValue());
						}
					}
					medicalInventoryRowManager.deleteMedicalInventoryRows(inventoryRowsToDelete);
					if (inventory.getStatus().equals(InventoryStatus.validated.toString())) {
						inventory.setStatus(InventoryStatus.draft.toString());
						inventory = medicalInventoryManager.updateMedicalInventory(inventory, true);
					}
				}
				List<MedicalInventoryRow> newMedicalInventoryRows = new ArrayList<>();
				if (mode.equals("new")) {
					newReference = referenceTextField.getText().trim();
					wardSelected = (Ward) wardComboBox.getSelectedItem();
					boolean refExist = medicalInventoryManager.referenceExists(newReference);
					if (refExist) {
						MessageDialog.error(null, "angal.inventory.referencealreadyused.msg");
						return;
					}
					inventory = new MedicalInventory();
					inventory.setInventoryReference(newReference);
					inventory.setInventoryDate(dateInventory);
					inventory.setStatus(status);
					inventory.setUser(user);
					inventory.setInventoryType(InventoryType.ward.toString());
					inventory.setWard(wardSelected != null ? wardSelected.getCode() : null);
					for (MedicalInventoryRow medicalInventoryRow : inventoryRowSearchList) {
						medicalInventoryRow.setInventory(inventory);
						Lot lot = medicalInventoryRow.getLot();
						String lotCode;
						Medical medical = medicalInventoryRow.getMedical();
						if (lot != null) {
							lotCode = lot.getCode();
							Lot lotExist = movStockInsertingManager.getLot(lotCode);
							if (lotExist != null) {
								Lot lotStore = movStockInsertingManager.updateLot(lot);
								medicalInventoryRow.setLot(lotStore);
							} else {
								if (lot.getDueDate() != null) {
									Lot lotStore = movStockInsertingManager.storeLot(lotCode, lot, medical);
									medicalInventoryRow.setLot(lotStore);
									medicalInventoryRow.setNewLot(true);
								} else {
									medicalInventoryRow.setLot(null);
								}
							}
						} else {
							medicalInventoryRow.setLot(null);
						}
						newMedicalInventoryRows.add(medicalInventoryRow);
					}
					inventory = medicalInventoryManager.newMedicalInventory(inventory, newMedicalInventoryRows);
					mode = "update";
					validateButton.setEnabled(true);
					MessageDialog.info(this, "angal.inventory.savesuccess.msg");
					fireInventoryInserted();
					resetVariable();
				} else if (mode.equals("update") && MessageDialog.yesNo(null, "angal.inventory.doyouwanttoupdatethisinventory.msg") == JOptionPane.YES_OPTION) {
					String lastReference = inventory.getInventoryReference();
					LocalDateTime lastDateInventory = inventory.getInventoryDate();
					newReference = referenceTextField.getText().trim();
					MedicalInventory existingInventory = medicalInventoryManager.getInventoryByReference(newReference);
					if (existingInventory != null && !Objects.equals(existingInventory.getId(), inventory.getId())) {
						MessageDialog.error(null, "angal.inventory.referencealreadyused.msg");
						return;
					}
					if (!inventory.getInventoryDate().equals(dateInventory)) {
						inventory.setInventoryDate(dateInventory);
					}
					if (!inventory.getUser().equals(user)) {
						inventory.setUser(user);
					}
					if (!newReference.equals(lastReference)) {
						inventory.setInventoryReference(newReference);
					}
					if (inventory.getStatus().equals(InventoryStatus.validated.toString())) {
						inventory.setStatus(InventoryStatus.draft.toString());
					}
					if (inventoryRowListAdded.isEmpty() && lotsSaved.isEmpty() && lotsDeleted.isEmpty()) {
						if (checkParameters(lastReference, lastDateInventory)) {
							inventory = medicalInventoryManager.updateMedicalInventory(inventory, true);
							if (inventory != null) {
								MessageDialog.info(null, "angal.inventory.update.success.msg");
								statusLabel.setText(InventoryStatus.draft.toString().toUpperCase());
								statusLabel.setForeground(Color.GRAY);
								resetVariable();
								fireInventoryUpdated();
							} else {
								MessageDialog.error(null, "angal.inventory.update.error.msg");
								return;
							}
						} else {
							if (!inventoryRowsToDelete.isEmpty()) {
								MessageDialog.info(null, "angal.inventory.update.success.msg");
								statusLabel.setText(InventoryStatus.draft.toString().toUpperCase());
								statusLabel.setForeground(Color.GRAY);
								resetVariable();
								fireInventoryUpdated();
							} else {
								MessageDialog.info(null, "angal.inventory.inventoryisalreadysaved.msg");
								return;
							}
						}
						return;
					}

					inventory = medicalInventoryManager.updateMedicalInventory(inventory, true);

					for (MedicalInventoryRow medicalInventoryRow : inventoryRowSearchList) {
						Medical medical = medicalInventoryRow.getMedical();
						Lot lot = medicalInventoryRow.getLot();
						if (lot != null) {
							Lot lotExist = movStockInsertingManager.getLot(lot.getCode());
							if (lotExist != null) {
								lot.setMedical(medical);
								lot = movStockInsertingManager.updateLot(lot);
								medicalInventoryRow.setLot(lot);
							} else {
								MedicalInventoryRow inventoryRow = medicalInventoryRowManager.getMedicalInventoryRowById(medicalInventoryRow.getId());
								if (inventoryRow != null && inventoryRow.getLock() != medicalInventoryRow.getLock()) {
									Lot newLot = movStockInsertingManager.storeLot(lot.getCode(), lot, medical);
									inventoryRow.setLot(newLot);
									inventoryRow.setNewLot(true);
									inventoryRow.setRealqty(medicalInventoryRow.getRealQty());
									medicalInventoryRow = inventoryRow;
								} else {
									Lot newLot = movStockInsertingManager.storeLot(lot.getCode(), lot, medical);
									medicalInventoryRow.setLot(newLot);
									medicalInventoryRow.setNewLot(true);
								}
							}
						}

						medicalInventoryRow.setInventory(inventory);
						medicalInventoryRow = medicalInventoryRow.getId() == 0 ? medicalInventoryRowManager.newMedicalInventoryRow(medicalInventoryRow)
							: medicalInventoryRowManager.updateMedicalInventoryRow(medicalInventoryRow);

						newMedicalInventoryRows.add(medicalInventoryRow);
					}
					MessageDialog.info(null, "angal.inventory.update.success.msg");
					statusLabel.setText(InventoryStatus.draft.toString().toUpperCase());
					statusLabel.setForeground(Color.GRAY);
					resetVariable();
					fireInventoryUpdated();
				}
				if (!newMedicalInventoryRows.isEmpty()) {
					inventoryRowSearchList = newMedicalInventoryRows;
					jTableInventoryRow.updateUI();
				}
				if (confirmButton.isEnabled()) {
					confirmButton.setEnabled(false);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		});
		return saveButton;
	}

	private JButton getValidateButton() {
		validateButton = new JButton(MessageBundle.getMessage("angal.inventory.validate.btn"));
		validateButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.validate.btn.key"));
		validateButton.setEnabled(inventory != null);
		validateButton.addActionListener(actionEvent -> {
			if (inventory == null) {
				MessageDialog.error(null, "angal.inventory.inventorymustsavebeforevalidation.msg");
				return;
			}
			int reset = MessageDialog.yesNo(null, "angal.inventory.doyoureallywanttovalidatethisinventory.msg");
			if (reset == JOptionPane.YES_OPTION) {
				newReference = referenceTextField.getText().trim();
				String lastReference = inventory.getInventoryReference();
				LocalDateTime lastDate = inventory.getInventoryDate();
				if (checkParameters(lastReference, lastDate)) {
					MessageDialog.error(null, "angal.inventory.pleasesaveinventorybeforevalidateit.msg");
					return;
				}
				if (!inventoryRowSearchList.stream().filter(i -> i.getLot() == null).toList().isEmpty()) {
					MessageDialog.error(null, "angal.inventory.youcannotvalidatethisinventory.msg");
					return;
				}
				if (!inventoryRowSearchList.stream().filter(i -> i.isNewLot() && i.getRealQty() == 0).toList().isEmpty()) {
					MessageDialog.error(null, "angal.inventory.youcannotvalidatetheinventorylotrealqtyzero.msg");
					return;
				}
				// validate inventory
				String status = InventoryStatus.validated.toString();
				try {
					medicalInventoryManager.validateMedicalWardInventoryRow(inventory, inventoryRowSearchList);
					inventory.setStatus(status);
					inventory = medicalInventoryManager.updateMedicalInventory(inventory, true);
					MessageDialog.info(null, "angal.inventory.validate.success.msg");
					statusLabel.setText(status.toUpperCase());
					statusLabel.setForeground(Color.BLUE);
					confirmButton.setEnabled(true);
					fireInventoryUpdated();
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
					int answer = MessageDialog.yesNo(null, "angal.inventory.doyouwanttoactualizetheinventory.msg");
					if (answer == JOptionPane.YES_OPTION) {
						try {
							inventory.setStatus(status);
							inventory = medicalInventoryManager.actualizeMedicalWardInventoryRow(inventory);
							statusLabel.setText(status.toUpperCase());
							statusLabel.setForeground(Color.BLUE);
							confirmButton.setEnabled(true);
							jTableInventoryRow.setModel(new InventoryRowModel());
							fireInventoryUpdated();
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1);
						}
					} else {
						try {
							inventory.setStatus(InventoryStatus.draft.toString());
							statusLabel.setText(InventoryStatus.draft.toString().toUpperCase());
							statusLabel.setForeground(Color.GRAY);
							inventory = medicalInventoryManager.updateMedicalInventory(inventory, true);
							fireInventoryUpdated();
						} catch (OHServiceException ex) {
							OHServiceExceptionUtil.showMessages(ex);
						}
					}
				}
			}
		});
		return validateButton;
	}

	private JButton getConfirmButton() {
		confirmButton = new JButton(MessageBundle.getMessage("angal.inventory.confirm.btn"));
		confirmButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.confirm.btn.key"));
		if (inventory == null) {
			confirmButton.setEnabled(false);
		}
		confirmButton.addActionListener(actionEvent -> {
			if (inventory == null) {
				MessageDialog.error(null, "angal.inventory.inventorymustsavebeforevalidation.msg");
				return;
			}
			int confirm = MessageDialog.yesNo(null, "angal.inventory.doyoureallywanttoconfirmthisinventory.msg");
			if (confirm == JOptionPane.YES_OPTION) {
				newReference = referenceTextField.getText().trim();
				String lastReference = inventory.getInventoryReference();
				LocalDateTime lastDate = inventory.getInventoryDate();
				if (checkParameters(lastReference, lastDate)) {
					MessageDialog.error(null, "angal.inventory.pleasesaveinventorybeforeconfirmation.msg");
					return;
				}
				if (!inventoryRowSearchList.stream().filter(i -> i.getLot() == null).toList().isEmpty()) {
					MessageDialog.error(null, "angal.inventory.youcannotconfirmthisinventory.msg");
					return;
				}
				if (!inventoryRowSearchList.stream().filter(i -> i.isNewLot() && i.getRealQty() == 0).toList().isEmpty()) {
					MessageDialog.error(null, "angal.inventory.youcannotconfirmtheinventorylotrealqtyzero.msg");
					return;
				}
				// confirm inventory
				try {
					medicalInventoryManager.confirmMedicalWardInventoryRow(inventory, inventoryRowSearchList);
					MessageDialog.info(null, "angal.inventory.confirm.success.msg");
					fireInventoryUpdated();
					closeButton.doClick();
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
					MessageDialog.info(null, "angal.inventory.pleasevalidateinventoryagainsbeforeconfirmation.msg");
					confirmButton.setEnabled(false);
					return;
				}
			}
		});
		return confirmButton;
	}

	private JButton getCleanTableButton() {
		resetButton = new JButton(MessageBundle.getMessage("angal.inventory.clean.btn"));
		resetButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.clean.btn.key"));
		resetButton.addActionListener(actionEvent -> {
			int reset = MessageDialog.yesNo(null, "angal.inventory.doyoureallywanttocleanthistable.msg");
			if (reset == JOptionPane.YES_OPTION) {
				if (inventory != null) {
					inventoryRowsToDelete.addAll(inventoryRowSearchList);
				}
				selectAll = false;
				codeTextField.setEnabled(true);
				inventoryRowSearchList.clear();
				DefaultTableModel model = (DefaultTableModel) jTableInventoryRow.getModel();
				model.setRowCount(0);
				model.setColumnCount(0);
				jTableInventoryRow.updateUI();
			}
		});
		return resetButton;
	}

	private JButton getDeleteButton() {
		deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		deleteButton.addActionListener(actionEvent -> {
			int[] selectedRows = jTableInventoryRow.getSelectedRows();
			if (selectedRows.length == 0) {
				MessageDialog.error(this, "angal.inventoryrow.pleaseselectatleastoneinventoryrow.msg");
				return;
			}
			int delete = MessageDialog.yesNo(null, "angal.inventoryrow.doyoureallywanttodeletethisinventoryrow.msg");
			if (delete == JOptionPane.YES_OPTION) {
				if (selectedRows.length == inventoryRowSearchList.size()) {
					resetButton.doClick();
					return;
				}
				DefaultTableModel model = (DefaultTableModel) jTableInventoryRow.getModel();
				if (inventory == null) {
					for (int i = selectedRows.length - 1; i >= 0; i--) {
						MedicalInventoryRow selectedInventoryRow = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRows[i], -1);
						inventoryRowSearchList.remove(selectedInventoryRow);
						model.fireTableDataChanged();
						jTableInventoryRow.setModel(model);
					}
				} else {
					for (int i = selectedRows.length - 1; i >= 0; i--) {
						MedicalInventoryRow inventoryRow = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRows[i], -1);
						inventoryRowSearchList.remove(inventoryRow);
						model.fireTableDataChanged();
						jTableInventoryRow.setModel(model);
						if (inventoryRow.getId() != 0) {
							inventoryRowsToDelete.add(inventoryRow);
						}
					}
				}
				jTableInventoryRow.clearSelection();
				selectAll = false;
			} else {
				return;
			}
		});
		return deleteButton;
	}

	private JButton getLotButton() {
		lotButton = new JButton(MessageBundle.getMessage("angal.inventory.lot.btn"));
		lotButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.lot.btn.key"));
		lotButton.addActionListener(actionEvent -> {
			int selectedRow = jTableInventoryRow.getSelectedRow();
			if (selectedRow == -1) {
				MessageDialog.error(this, "angal.inventoryrow.pleaseselectonlyoneinventoryrow.msg");
				return;
			}
			MedicalInventoryRow selectedInventoryRow = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRow, -1);
			Lot lotToUpdate = selectedInventoryRow.getLot();
			Lot lot = new Lot();
			try {
				if (lotToUpdate != null && !selectedInventoryRow.isNewLot()) {
					BigDecimal cost = BigDecimal.ZERO;
					if (isLotWithCost()) {
						cost = askCost(cost);
						if (cost.compareTo(BigDecimal.ZERO) == 0) {
							return;
						}
					}
					lotToUpdate.setCost(cost);
					lot = lotToUpdate;
				} else {
					lot = this.getLot(lotToUpdate);
					String lotCode = lotToUpdate != null ? lotToUpdate.getCode() : "";
					if (lot != null && !lot.getCode().equals(lotCode)) {
						Lot lotDelete = movStockInsertingManager.getLot(lotCode);
						if (lotDelete != null) {
							lotsDeleted.put(selectedInventoryRow.getId(), lotDelete);
						}
					}
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
				return;
			}
			if (lot != null) {
				code = lot.getCode();
				if (selectedInventoryRow.getLot() == null) {
					List<MedicalInventoryRow> invRows = inventoryRowSearchList.stream()
						.filter(inv -> inv.getLot() != null && inv.getLot().getCode().equals(code)).toList();
					if (invRows.isEmpty() || code.isEmpty()) {
						selectedInventoryRow.setNewLot(true);
						selectedInventoryRow.setLot(lot);
						lotsSaved.add(lot);
					} else {
						if (lotToUpdate != null && code.equals(lotToUpdate.getCode())) {
							selectedInventoryRow.setLot(lot);
							lotsSaved.add(lot);
						} else {
							MessageDialog.error(this, "angal.inventoryrow.thislotcodealreadyexists.msg");
							lotButton.doClick();
						}
					}
				} else {
					List<MedicalInventoryRow> invRows = inventoryRowSearchList.stream()
						.filter(inv -> inv.getMedical().getCode().equals(selectedInventoryRow.getMedical().getCode())).collect(Collectors.toList());
					invRows = invRows.stream().filter(inv -> inv.getLot() != null && inv.getLot().getCode().equals(code)).collect(Collectors.toList());
					if (invRows.isEmpty() || code.equals("")) {
						selectedInventoryRow.setNewLot(true);
						selectedInventoryRow.setLot(lot);
						lotsSaved.add(lot);
					} else {
						if (lotToUpdate != null && code.equals(lotToUpdate.getCode())) {
							selectedInventoryRow.setLot(lot);
							lotsSaved.add(lot);
						} else {
							MessageDialog.error(this, "angal.inventoryrow.thislotcodealreadyexists.msg");
							lotButton.doClick();
						}
					}
				}
				inventoryRowSearchList.set(selectedRow, selectedInventoryRow);
				jTableInventoryRow.updateUI();
			}
		});
		return lotButton;
	}
	private BigDecimal askCost(BigDecimal lastCost) {
		double cost = 0.;
		do {
			String input = JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.medicalstock.multiplecharging.unitcost"),
				lastCost);
			if (input != null) {
				try {
					cost = Double.parseDouble(input);
					if (cost < 0) {
						throw new NumberFormatException();
					} else if (cost == 0.) {
						double total = askTotalCost();
						cost = total / 2;
					}
				} catch (NumberFormatException nfe) {
					MessageDialog.error(this, "angal.medicalstock.multiplecharging.pleaseinsertavalidvalue");
				}
			} else {
				return BigDecimal.valueOf(cost);
			}
		} while (cost == 0.);
		return BigDecimal.valueOf(cost);
	}

	private JButton getPrintButton() {
		printButton = new JButton(MessageBundle.getMessage("angal.common.print.btn"));
		printButton.setMnemonic(MessageBundle.getMnemonic("angal.common.print.btn.key"));
		printButton.setEnabled(true);

		printButton.addActionListener(e -> {
			int printRealQty = 0;
			int response = MessageDialog.yesNo(this, "angal.inventory.askforrealquantityempty.msg");
			if (response == JOptionPane.YES_OPTION) {
				printRealQty = 1;
			}
			new GenericReportPharmaceuticalInventory(this.inventory, "InventoryWard", printRealQty);
		});
		return printButton;
	}

	private JButton getCloseButton() {
		closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		closeButton.addActionListener(actionEvent -> {
			String lastReference = "";
			newReference = referenceTextField.getText().trim();
			LocalDateTime lastDate = dateInventory;
			if (inventory != null) {
				lastReference = inventory.getInventoryReference();
				lastDate = inventory.getInventoryDate();
			}

			if (checkParameters(lastReference, lastDate)) {
				int reset = MessageDialog.yesNoCancel(null, "angal.inventory.doyouwanttosavethechanges.msg");
				if (reset == JOptionPane.YES_OPTION) {
					this.saveButton.doClick();
					dispose();
				}
				if (reset == JOptionPane.NO_OPTION) {
					resetVariable();
					dispose();
				} else {
					return;
				}
			} else {
				resetVariable();
				dispose();
			}
		});
		return closeButton;
	}

	private JScrollPane getScrollPaneInventory() {
		if (scrollPaneInventory == null) {
			scrollPaneInventory = new JScrollPane();
			try {
				scrollPaneInventory.setViewportView(getJTableInventoryRow());
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		return scrollPaneInventory;
	}

	private JTable getJTableInventoryRow() throws OHServiceException {
		if (jTableInventoryRow == null) {
			jTableInventoryRow = new JTable();
			jTextFieldEditor = new JTextField();
			jTableInventoryRow.setFillsViewportHeight(true);
			jTableInventoryRow.setModel(new InventoryRowModel());
			jTableInventoryRow.setAutoCreateColumnsFromModel(false);
			for (int i = 0; i < columnVisible.length; i++) {
				jTableInventoryRow.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer());
				jTableInventoryRow.getColumnModel().getColumn(i).setPreferredWidth(columwidth[i]);
				if (i == 0 || !columnVisible[i]) {
					jTableInventoryRow.getColumnModel().getColumn(i).setMinWidth(0);
					jTableInventoryRow.getColumnModel().getColumn(i).setMaxWidth(0);
					jTableInventoryRow.getColumnModel().getColumn(i).setPreferredWidth(0);
				}
				if (columnCentered[i]) {
					jTableInventoryRow.getColumnModel().getColumn(i).setCellRenderer(new CenterTableCellRenderer());
				}
				if (columnDecimalNumber[i]) {
					jTableInventoryRow.getColumnModel().getColumn(i).setCellRenderer(new DecimalNumberTableCellRenderer());
				}
			}
			DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
			centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
			jTableInventoryRow.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
			jTableInventoryRow.getSelectionModel().addListSelectionListener(listSelectionEvent -> {

				if (listSelectionEvent.getValueIsAdjusting()) {
					jTableInventoryRow.editCellAt(jTableInventoryRow.getSelectedRow(), jTableInventoryRow.getSelectedColumn());
				}
			});

			DefaultCellEditor cellEditor = new DefaultCellEditor(jTextFieldEditor);
			jTableInventoryRow.setDefaultEditor(Integer.class, cellEditor);
		}
		return jTableInventoryRow;
	}

	class InventoryRowModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public InventoryRowModel(boolean add) throws OHServiceException {
			inventoryRowList = loadNewInventoryTable(null, inventory, add);
			if (!inventoryRowList.isEmpty()) {
				for (MedicalInventoryRow inventoryRow : inventoryRowList) {
					addMedInRowInInventorySearchList(inventoryRow);
				}
				selectAll = true;
				MessageDialog.info(null, "angal.inventory.allmedicaladdedsuccessfully.msg");
			} else {
				MessageDialog.info(null, "angal.inventory.youhavealreadyaddedallproduct.msg");
			}
		}

		public InventoryRowModel() throws OHServiceException {
			if (!inventoryRowSearchList.isEmpty()) {
				inventoryRowSearchList.clear();
			}
			if (inventory != null) {
				inventoryRowList = medicalInventoryRowManager.getMedicalInventoryRowByInventoryId(inventory.getId());
			} else {
				if (selectButton.isSelected()) {
					inventoryRowList = loadNewInventoryTable(null, inventory, false);
				}
			}
			if (inventoryRowList != null && !inventoryRowList.isEmpty()) {
				for (MedicalInventoryRow inventoryRow : inventoryRowList) {
					addMedInRowInInventorySearchList(inventoryRow);
					if (inventoryRow.getId() == 0) {
						inventoryRowListAdded.add(inventoryRow);
					}
				}
			}
		}

		@Override
		public Class< ? > getColumnClass(int c) {
			return columnsClasses[c];
		}

		@Override
		public int getRowCount() {
			if (inventoryRowSearchList == null) {
				return 0;
			}
			return inventoryRowSearchList.size();
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
			if (r < inventoryRowSearchList.size()) {
				MedicalInventoryRow medInvtRow = inventoryRowSearchList.get(r);
				if (c == -1) {
					return medInvtRow;
				} else if (c == 0) {
					return medInvtRow.getId();
				} else if (c == 1) {
					return medInvtRow.getMedical() == null ? "" : medInvtRow.getMedical().getProdCode();
				} else if (c == 2) {
					return medInvtRow.getMedical() == null ? "" : medInvtRow.getMedical().getDescription();
				} else if (c == 3) {
					return medInvtRow.getLot() == null || medInvtRow.isNewLot() ? "N" : "";
				} else if (c == 4) {
					return medInvtRow.getLot() == null ? "" : (medInvtRow.getLot().getCode().equals("") ? "AUTO" : medInvtRow.getLot().getCode());
				} else if (c == 5) {
					if (medInvtRow.getLot() != null && medInvtRow.getLot().getDueDate() != null) {
						return medInvtRow.getLot().getDueDate().format(DATE_FORMATTER);
					}
					return "";
				} else if (c == 6) {
					return medInvtRow.getTheoreticQty();
				} else if (c == 7) {
					double dblValue = medInvtRow.getRealQty();
					return (int) dblValue;
				} else if (c == 8) {
					if (medInvtRow.getLot() != null && medInvtRow.getLot().getCost() != null) {
						medInvtRow.setTotal(medInvtRow.getLot().getCost().multiply(BigDecimal.valueOf(medInvtRow.getRealQty())));
						return medInvtRow.getLot().getCost();
					}
					return BigDecimal.ZERO;
				} else if (c == 9) {
					if (medInvtRow.getLot() != null && medInvtRow.getLot().getCost() != null) {
						return medInvtRow.getTotal();
					}
					return BigDecimal.ZERO;
				}
			}
			return null;
		}

		@Override
		public void setValueAt(Object value, int r, int c) {
			if (r < inventoryRowSearchList.size()) {
				MedicalInventoryRow invRow = inventoryRowSearchList.get(r);
				if (c == 7) {
					double doubleValue = 0.0;
					if (value != null) {
						try {
							doubleValue = Double.parseDouble(value.toString());
						} catch (NumberFormatException e) {
							return;
						}
					}
					if (doubleValue < 0) {
						MessageDialog.error(null, "angal.inventoryrow.invalidquantity.msg");
						return;
					}
					invRow.setRealqty(doubleValue);
					if (invRow.getLot() != null && invRow.getLot().getCost() != null) {
						BigDecimal total = invRow.getLot().getCost().multiply(BigDecimal.valueOf(invRow.getRealQty()));
						invRow.setTotal(total);
					}
					inventoryRowListAdded.add(invRow);
					inventoryRowSearchList.set(r, invRow);
					SwingUtilities.invokeLater(() -> jTableInventoryRow.updateUI());
				}
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnEditable[columnIndex];
		}
	}

	private List<MedicalInventoryRow> loadNewInventoryTable(String code, MedicalInventory inventory, boolean add) throws OHServiceException {
		List<MedicalInventoryRow> inventoryRowsList;
		if (inventory != null) {
			int id = inventory.getId();
			inventoryRowsList = medicalInventoryRowManager.getMedicalInventoryRowByInventoryId(id);
			if (add) {
				inventoryRowsList = getMedicalInventoryRows(code);
			}
		} else {
			inventoryRowsList = getMedicalInventoryRows(code);
		}
		return inventoryRowsList;
	}

	private List<MedicalInventoryRow> getMedicalInventoryRows(String code) throws OHServiceException {
		List<MedicalInventoryRow> inventoryRowsList = new ArrayList<>();
		List<MedicalWard> medicalWardList = new ArrayList<>();
		Medical medical;
		MedicalInventoryRow inventoryRowTemp;
		if (code != null) {
			medical = medicalBrowsingManager.getMedicalByMedicalCode(code);
			if (medical != null) {
				medicalWardList = movWardBrowserManager.getMedicalsWard(wardId, medical.getCode(), false);
			} else {
				MessageDialog.error(null, MessageBundle.getMessage("angal.inventory.noproductfound.msg"));
			}
		} else {
			medicalWardList = movWardBrowserManager.getMedicalsWard(wardId, false);
		}
		for (MedicalWard medicalWard : medicalWardList) {
			inventoryRowTemp = new MedicalInventoryRow(0, medicalWard.getQty(), medicalWard.getQty(), null,
				medicalWard.getMedical(), medicalWard.getLot());
			if (!existInInventorySearchList(inventoryRowTemp)) {
				inventoryRowsList.add(inventoryRowTemp);
			}
		}
		return inventoryRowsList;
	}

	private boolean existInInventorySearchList(MedicalInventoryRow inventoryRow) {
		boolean found = false;
		List<MedicalInventoryRow> invRows = inventoryRowSearchList.stream()
			.filter(inventoryRow1 -> inventoryRow1.getMedical().getCode().equals(inventoryRow.getMedical().getCode())).toList();
		if (!invRows.isEmpty()) {
			for (MedicalInventoryRow invR : invRows) {
				if (inventoryRow.getLot() != null && invR.getLot() != null) {
					if (inventoryRow.getLot().getCode().equals(invR.getLot().getCode())) {
						found = true;
						break;
					}
				} else {
					if (invR.getLot() == null && inventoryRow.getLot() == null) {
						found = true;
						break;
					}
				}
			}
		}
		return found;
	}

	private JLabel getSpecificProductLabel() {
		if (specificProduct == null) {
			specificProduct = new JLabel(MessageBundle.getMessage("angal.inventory.specificproduct.btn"));
		}
		return specificProduct;
	}

	private JLabel getDateInventoryLabel() {
		if (dateInventoryLabel == null) {
			dateInventoryLabel = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
		}
		return dateInventoryLabel;
	}

	private JTextField getCodeTextField() {
		if (codeTextField == null) {
			codeTextField = new JTextField();
			codeTextField.setEnabled(inventory == null);
			codeTextField.setColumns(10);
			TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.common.code.txt"), codeTextField, Show.FOCUS_LOST);
			suggestion.setFont(new Font("Tahoma", Font.PLAIN, 12));
			suggestion.setForeground(Color.GRAY);
			suggestion.setHorizontalAlignment(SwingConstants.CENTER);
			suggestion.changeAlpha(0.5f);
			suggestion.changeStyle(Font.BOLD + Font.ITALIC);
			codeTextField.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						code = codeTextField.getText().trim();
						code = code.toLowerCase();
						try {
							addInventoryRow(code);
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1);
						}
						if (inventory != null && !inventory.getStatus().equals(InventoryStatus.draft.toString())) {
							inventory.setStatus(InventoryStatus.draft.toString());
						}
						codeTextField.setText("");
					}
				}
			});
		}
		return codeTextField;
	}

	private void addInventoryRow(String code) throws OHServiceException {
		List<MedicalInventoryRow> inventoryRowsList = new ArrayList<>();
		List<MedicalWard> medicalWardList;
		List<Lot> lots;
		Medical medical = null;
		MedicalInventoryRow inventoryRowTemp;
		if (wardId.isEmpty()) {
			wardId = ((Ward) Objects.requireNonNull(wardComboBox.getSelectedItem())).getCode();
		}
		if (code != null) {
			medical = medicalBrowsingManager.getMedicalByMedicalCode(code);
			if (medical == null) {
				medical = chooseMedical(code);
			}
		}
		if (medical == null) {
			return;
		}
		medicalWardList = movWardBrowserManager.getMedicalsWard(wardId, medical.getCode(), false);
		if (!medicalWardList.isEmpty()) {
			boolean found = false;
			for (MedicalInventoryRow row : inventoryRowSearchList) {
				if (row.getMedical().getCode().equals(medical.getCode())) {
					found = true;
					break;
				}
			}
			if (!found) {
				inventoryRowsList = medicalWardList.stream().map(medWard -> new MedicalInventoryRow(0, medWard.getQty(),
					medWard.getQty(), null, medWard.getMedical(), medWard.getLot())).toList();
			} else {
				Lot lot = chooseLot(medical);
				if (lot != null) {
					if (lot.getCode().equals("A")) {
						inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, medical, null);
						inventoryRowsList.add(inventoryRowTemp);
					} else {
						List<MedicalInventoryRow> medIvRowsWithSameLot = inventoryRowSearchList.stream()
							.filter(invR -> invR.getLot() != null && invR.getLot().getCode().equals(lot.getCode())).toList();
						if (medIvRowsWithSameLot.isEmpty()) {
							inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, medical, lot);
							inventoryRowsList.add(inventoryRowTemp);
						} else {
							int info = MessageDialog.yesNo(null, "angal.inventory.productalreadyexistwithlot.fmt.msg", medical.getDescription(), lot.getCode());
							if (info == JOptionPane.YES_OPTION) {
								addInventoryRow(code);
							}
						}
					}
				}
			}

		} else {
			lots = movStockInsertingManager.getLotByMedical(medical, false);
			if (lots.isEmpty()) {
				inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, medical, null);
				if (!existInInventorySearchList(inventoryRowTemp)) {
					inventoryRowsList.add(inventoryRowTemp);
				} else {
					int info = MessageDialog.yesNo(null, "angal.inventory.productalreadyexist.fmt.msg", medical.getDescription());
					if (info == JOptionPane.YES_OPTION) {
						inventoryRowsList.add(inventoryRowTemp);
					}
				}
			} else {
				Lot lot = chooseLot(medical);
				if (lot != null) {
					if (lot.getCode().equals("A")) {
						inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, medical, null);
						inventoryRowsList.add(inventoryRowTemp);
					} else {
						List<MedicalInventoryRow> medIvRowsWithSameLot = inventoryRowSearchList.stream()
							.filter(invR -> invR.getLot() != null && invR.getLot().getCode().equals(lot.getCode())).toList();
						if (medIvRowsWithSameLot.isEmpty()) {
							inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, medical, lot);
							inventoryRowsList.add(inventoryRowTemp);
						} else {
							int info = MessageDialog.yesNo(null, "angal.inventory.productalreadyexistwithlot.fmt.msg", medical.getDescription(), lot.getCode());
							if (info == JOptionPane.YES_OPTION) {
								addInventoryRow(code);
							}
						}
					}
				}
			}
		}
		if (inventoryRowSearchList == null) {
			inventoryRowSearchList = new ArrayList<>();
		}
		inventoryRowsList.forEach(this::addMedInRowInInventorySearchList);
		jTableInventoryRow.updateUI();
	}

	private Medical chooseMedical(String text) throws OHServiceException {
		Map<String, Medical> medicalMap = new HashMap<>();
		List<Medical> medicals = medicalBrowsingManager.getMedicals();
		for (Medical med : medicals) {
			String key = med.getCode().toString().toLowerCase();
			medicalMap.put(key, med);
		}
		List<Medical> medList = new ArrayList<>();
		for (Medical aMed : medicalMap.values()) {
			if (NormalizeString.normalizeContains(aMed.getDescription().toLowerCase(), text)) {
				medList.add(aMed);
			}
		}
		Collections.sort(medList);
		Medical med = null;
		if (!medList.isEmpty()) {
			MedicalPicker framas = new MedicalPicker(new StockMedModel(medList), medList);
			framas.setSize(300, 400);
			JDialog dialog = new JDialog();
			dialog.setLocationRelativeTo(null);
			dialog.setSize(600, 350);
			dialog.setLocationRelativeTo(null);
			dialog.setModal(true);
			dialog.setTitle(MessageBundle.getMessage("angal.medicalstock.multiplecharging.chooseamedical"));
			framas.setParentFrame(dialog);
			dialog.setContentPane(framas);
			dialog.setVisible(true);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			med = framas.getSelectedMedical();
		}
		return med;
	}

	private JLabel getReferenceLabel() {
		if (referenceLabel == null) {
			referenceLabel = new JLabel(MessageBundle.getMessage("angal.inventory.reference.label"));
		}
		return referenceLabel;
	}

	private JTextField getReferenceTextField() {
		if (referenceTextField == null) {
			// limit to 40 similarly to MainInventory so to have a common validation method
			referenceTextField = new VoLimitedTextField(40, 10);
			referenceTextField.setColumns(10);
			if (inventory != null && !mode.equals("new")) {
				referenceTextField.setText(inventory.getInventoryReference());
			}
		}
		return referenceTextField;
	}

	private JLabel getStatusLabel() {
		if (statusLabel == null) {
			if (inventory == null) {
				String currentStatus = InventoryStatus.draft.toString().toUpperCase();
				statusLabel = new JLabel(currentStatus);
				statusLabel.setForeground(Color.GRAY);
			} else {
				String currentStatus = inventory.getStatus().toUpperCase();
				statusLabel = new JLabel(currentStatus);
				if (currentStatus.equalsIgnoreCase(InventoryStatus.draft.toString())) {
					statusLabel.setForeground(Color.GRAY);
				}
				if (currentStatus.equalsIgnoreCase(InventoryStatus.validated.toString())) {
					statusLabel.setForeground(Color.BLUE);
				}
				if (currentStatus.equalsIgnoreCase(InventoryStatus.canceled.toString())) {
					statusLabel.setForeground(Color.RED);
				}
				if (currentStatus.equalsIgnoreCase(InventoryStatus.done.toString())) {
					statusLabel.setForeground(Color.GREEN);
				}
			}
			statusLabel.setFont(new Font(statusLabel.getFont().getName(), Font.BOLD, statusLabel.getFont().getSize() + 8));
		}
		return statusLabel;
	}

	private JLabel getWardLabel() {
		if (wardLabel == null) {
			wardLabel = new JLabel(MessageBundle.getMessage("angal.inventory.selectward.label"));
		}
		return wardLabel;
	}

	private JComboBox<Ward> getWardComboBox() {
		if (wardComboBox == null) {
			wardComboBox = new JComboBox<>();
			List<Ward> wardList;
			try {
				wardList = wardBrowserManager.getWards();
			} catch (OHServiceException e) {
				wardList = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			if (!mode.equals("new")) {
				String wardId = inventory.getWard();
				for (Ward ward : wardList) {
					if (ward.getCode().equals(wardId)) {
						wardComboBox.addItem(ward);
						wardSelected = ward;
					}
				}
				wardComboBox.setEnabled(false);
			} else {
				for (Ward elem : wardList) {
					wardComboBox.addItem(elem);
				}
				wardComboBox.setSelectedIndex(-1);
			}

			wardComboBox.addItemListener(itemEvent -> {

				if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
					Object item = itemEvent.getItem();
					if (item instanceof Ward wardSelected) {
						wardId = wardSelected.getCode();
						List<MedicalInventory> medicalWardInventoryDraft;
						List<MedicalInventory> medicalWardInventoryValidated;
						try {
							medicalWardInventoryDraft = medicalInventoryManager
								.getMedicalInventoryByStatusAndWard(InventoryStatus.draft.toString(), wardId);
							medicalWardInventoryValidated = medicalInventoryManager
								.getMedicalInventoryByStatusAndWard(InventoryStatus.validated.toString(), wardId);
						} catch (OHServiceException e) {
							medicalWardInventoryDraft = new ArrayList<>();
							medicalWardInventoryValidated = new ArrayList<>();
							OHServiceExceptionUtil.showMessages(e);
						}

						if (medicalWardInventoryDraft.isEmpty() && medicalWardInventoryValidated.isEmpty()) {
							activateSomeComponents();
						} else {
							MessageDialog.error(this,
								"angal.inventory.cannotcreateanotherinventorywithotherinprogressinthisward.msg");
						}
					}
				}
			});
		}
		return wardComboBox;
	}

	private void disabledSomeComponents() {
		jCalendarInventory.setEnabled(false);
		specificProduct.setEnabled(false);
		codeTextField.setEnabled(false);
		selectButton.setEnabled(false);
		referenceTextField.setEnabled(false);
		jTableInventoryRow.setEnabled(false);
		saveButton.setEnabled(false);
		deleteButton.setEnabled(false);
		resetButton.setEnabled(false);
		lotButton.setEnabled(false);
	}

	private void activateSomeComponents() {
		jCalendarInventory.setEnabled(true);
		specificProduct.setEnabled(true);
		codeTextField.setEnabled(true);
		selectButton.setEnabled(true);
		referenceTextField.setEnabled(true);
		jTableInventoryRow.setEnabled(true);
		wardComboBox.setEnabled(false);
		saveButton.setEnabled(true);
		deleteButton.setEnabled(true);
		resetButton.setEnabled(true);
		lotButton.setEnabled(true);
	}

	private void addMedInRowInInventorySearchList(MedicalInventoryRow inventoryRow) {
		int position = getPosition(inventoryRow);
		if (position == -1) {
			position = inventoryRowSearchList.size();
			inventoryRowSearchList.add(position, inventoryRow);
		} else {
			inventoryRowSearchList.add(position + 1, inventoryRow);
		}
		if (inventoryRow.getId() == 0) {
			inventoryRowListAdded.add(inventoryRow);
		}
	}

	private int getPosition(MedicalInventoryRow inventoryRow) {
		int position = -1;
		int i = 0;
		for (MedicalInventoryRow inventoryRow1 : inventoryRowSearchList) {
			if (inventoryRow1.getMedical().getCode().equals(inventoryRow.getMedical().getCode())) {
				position = i;
			}
			i++;
		}
		return position;
	}

	private void resetVariable() {
		inventoryRowsToDelete.clear();
		lotsDeleted.clear();
		inventoryRowListAdded.clear();
		lotsSaved.clear();
	}

	private boolean isLotWithCost() {
		return GeneralData.LOTWITHCOST;
	}

	private boolean isAutomaticLotIn() {
		return GeneralData.AUTOMATICLOT_IN;
	}

	protected double askTotalCost() {
		String input = JOptionPane.showInputDialog(this, MessageBundle.getMessage("angal.medicalstock.multiplecharging.totalcost"), 0.);
		double total = 0.;
		if (input != null) {
			try {
				total = Double.parseDouble(input);
				if (total < 0) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException nfe) {
				MessageDialog.error(this, "angal.medicalstock.multiplecharging.pleaseinsertavalidvalue");
			}
		}
		return total;
	}

	private Lot getLot(Lot lotToUpdate) throws OHServiceException {
		Lot lot;
		if (isAutomaticLotIn()) {
			LocalDateTime preparationDate = TimeTools.getNow().truncatedTo(ChronoUnit.MINUTES);
			LocalDateTime expiringDate = askExpiringDate();
			lot = new Lot(null, "", preparationDate, expiringDate);
			// Cost
			BigDecimal cost = new BigDecimal(0);
			if (isLotWithCost()) {
				cost = askCost(cost);
				if (cost.compareTo(new BigDecimal(0)) == 0) {
					return null;
				}
			}
			lot.setCost(cost);
		} else {
			lot = askLot(lotToUpdate);
		}
		return lot;
	}

	protected LocalDateTime askExpiringDate() {
		LocalDateTime date = TimeTools.getNow();
		GoodDateTimeSpinnerChooser expireDateChooser = new GoodDateTimeSpinnerChooser(date);
		JPanel panel = new JPanel(new GridLayout(1, 2));
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate")));
		panel.add(expireDateChooser);

		int ok = JOptionPane.showConfirmDialog(this, panel, MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate"),
			JOptionPane.OK_CANCEL_OPTION);

		if (ok == JOptionPane.OK_OPTION) {
			date = expireDateChooser.getLocalDateTime();
		}
		return date;
	}

	private Lot askLot(Lot lotToUpdate) {
		LocalDateTime preparationDate;
		LocalDateTime expiringDate;
		Lot lot = null;
		Medical medical = null;

		JTextField lotNameTextField = new JTextField(15);
		lotNameTextField.addAncestorListener(new RequestFocusListener());
		TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotid"), lotNameTextField);
		suggestion.setFont(new Font("Tahoma", Font.PLAIN, 14));
		suggestion.setForeground(Color.GRAY);
		suggestion.setHorizontalAlignment(SwingConstants.CENTER);
		suggestion.changeAlpha(0.5f);
		suggestion.changeStyle(Font.BOLD + Font.ITALIC);
		LocalDate now = LocalDate.now();
		GoodDateChooser preparationDateChooser = new GoodDateChooser(now);
		GoodDateChooser expireDateChooser = new GoodDateChooser(now);
		if (lotToUpdate != null) {
			medical = lotToUpdate.getMedical();
			lotNameTextField.setText(lotToUpdate.getCode());
			preparationDateChooser = new GoodDateChooser(lotToUpdate.getPreparationDate().toLocalDate());
			expireDateChooser = new GoodDateChooser(lotToUpdate.getDueDate().toLocalDate());
		}
		JPanel panel = new JPanel(new GridLayout(3, 2));
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotnumberabb")));
		panel.add(lotNameTextField);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.preparationdate")));
		panel.add(preparationDateChooser);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate")));
		panel.add(expireDateChooser);
		do {
			int ok = JOptionPane.showConfirmDialog(this, panel,
				MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotinformations"), JOptionPane.OK_CANCEL_OPTION);
			if (ok == JOptionPane.OK_OPTION) {
				String lotName = lotNameTextField.getText();
				if (expireDateChooser.getDate().isBefore(preparationDateChooser.getDate())) {
					MessageDialog.error(this, "angal.medicalstock.multiplecharging.expirydatebeforepreparationdate");
				} else {
					expiringDate = expireDateChooser.getDateEndOfDay();
					preparationDate = preparationDateChooser.getDateStartOfDay();
					lot = new Lot(medical, lotName, preparationDate, expiringDate);
					BigDecimal cost = new BigDecimal(0);
					if (isLotWithCost()) {
						if (lotToUpdate != null) {
							cost = askCost(lotToUpdate.getCost());
						} else {
							cost = askCost(cost);
						}

						if (cost.compareTo(new BigDecimal(0)) == 0) {
							return null;
						} else {
							lot.setCost(cost);
						}
					}
				}
			} else {
				return null;
			}
		} while (lot == null);
		return lot;
	}

	private boolean checkParameters(String reference, LocalDateTime date) {
		return !lotsSaved.isEmpty() || !inventoryRowListAdded.isEmpty() || !lotsDeleted.isEmpty() || !inventoryRowsToDelete.isEmpty()
			|| (reference != null && !reference.equals(newReference)) || !date.toLocalDate().equals(dateInventory.toLocalDate());
	}

	class StockLotModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private List<Lot> lotList;

		@Override
		public Class< ? > getColumnClass(int columnIndex) {
			return lotSelectionColumnClasses[columnIndex];
		}

		public StockLotModel(List<Lot> lots) {
			this.lotList = new ArrayList<>(lots);
		}

		@Override
		public int getRowCount() {
			if (lotList == null) {
				return 0;
			}
			return lotList.size();
		}

		@Override
		public String getColumnName(int c) {
			return lotSelectionColumnNames[c];
		}

		@Override
		public int getColumnCount() {
			return lotSelectionColumnNames.length;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Lot lot = lotList.get(r);
			int i = -1;
			if (c == i) {
				return lot;
			} else if (c == ++i) {
				return lot.getCode();
			} else if (c == ++i) {
				return lot.getPreparationDate().format(DATE_FORMATTER);
			} else if (c == ++i) {
				return lot.getDueDate().format(DATE_FORMATTER);
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	protected Lot chooseLot(Medical med) {
		List<Lot> lots;
		try {
			lots = movStockInsertingManager.getLotByMedical(med, false); // get also empty lots
		} catch (OHServiceException e) {
			lots = new ArrayList<>();
			OHServiceExceptionUtil.showMessages(e);
		}
		if (lots.isEmpty()) {
			return null;
		}

		StockLotModel lotModel = new StockLotModel(lots);
		JTable lotTable = new JTable(lotModel);
		lotTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.useanexistinglot")), BorderLayout.NORTH);
		panel.add(new JScrollPane(lotTable), BorderLayout.CENTER);

		Lot selectedLot = null;
		Object[] options = {
				MessageBundle.getMessage("angal.medicalstock.multiplecharging.selectedlot"),
				MessageBundle.getMessage("angal.medicalstock.multiplecharging.newlot"),
				MessageBundle.getMessage("angal.common.cancel.btn")
		};

		int row;
		do {
			int ok = JOptionPane.showOptionDialog(this, panel,
				MessageBundle.getMessage("angal.medicalstock.multiplecharging.existinglot"),
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			row = lotTable.getSelectedRow();
			if (ok == JOptionPane.YES_OPTION) {
				if (row != -1) {
					selectedLot = lots.get(row);
				} else {
					MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
				}
			}
			if (ok == JOptionPane.NO_OPTION) {
				if (row == -1) {
					row = 0;
					selectedLot = new Lot("A");
				} else {
					MessageDialog.error(this, "angal.inventory.selectarowerror.msg");
					chooseLot(med);
				}
			} else {
				row = 0;
			}
		} while (row == -1);

		return selectedLot;
	}

	class CenterTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(CENTER);
			return cell;
		}
	}

	public class DecimalNumberTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof BigDecimal) {
				lbl.setText(String.format("%.02f", value));
			}
			lbl.setOpaque(true);
			lbl.setBackground(Color.WHITE);
			setHorizontalAlignment(CENTER);
			return lbl;
		}
	}
}
