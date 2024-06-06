/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicalinventory.manager.MedicalInventoryManager;
import org.isf.medicalinventory.manager.MedicalInventoryRowManager;
import org.isf.medicalinventory.model.MedicalInventory;
import org.isf.medicalinventory.model.MedicalInventoryRow;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.medstockmovtype.manager.MedicalDsrStockMovementTypeBrowserManager;
import org.isf.medstockmovtype.model.MovementType;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.supplier.manager.SupplierBrowserManager;
import org.isf.supplier.model.Supplier;
import org.isf.utils.db.NormalizeString;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.InventoryStatus;
import org.isf.utils.jobjects.InventoryType;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.RequestFocusListener;
import org.isf.utils.jobjects.TextPrompt;
import org.isf.utils.jobjects.TextPrompt.Show;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

public class InventoryEdit extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private static EventListenerList InventoryListeners = new EventListenerList();

	public interface InventoryListener extends EventListener {

		public void InventoryInserted(AWTEvent e);

		public void InventoryUpdated(AWTEvent e);

		public void InventoryCancelled(AWTEvent e);
	}

	public static void addInventoryListener(InventoryListener l) {
		InventoryListeners.add(InventoryListener.class, l);
	}

	public static void removeInventoryListener(InventoryListener listener) {
		InventoryListeners.remove(InventoryListener.class, listener);
	}

	private GoodDateChooser jCalendarInventory;
	private LocalDateTime dateInventory = TimeTools.getNow();
	private JPanel panelHeader;
	private JPanel panelFooter;
	private JPanel panelContent;
	private JButton closeButton;
	private JButton deleteButton;
	private JButton saveButton;
	private JButton resetButton;
	private JScrollPane scrollPaneInventory;
	private JTable jTableInventoryRow;
	private List<MedicalInventoryRow> inventoryRowList;
	private List<MedicalInventoryRow> inventoryRowSearchList;
	private String[] pColums = { MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.inventory.product.col").toUpperCase(),
			MessageBundle.getMessage("angal.wardpharmacy.lotnumber.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.duedate.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventoryrow.theorticqty.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventoryrow.realqty.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventoryrow.unitprice.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventory.totalprice").toUpperCase() };
	private int[] pColumwidth = { 100, 200, 100, 100, 100, 80, 80, 80 };
	private boolean[] columnEditable = { false, false, true, true, false, true, true, false };
	private boolean[] columnEditableView = { false, false, false, false, false, false, false, false };
	private boolean[] pColumnVisible = { true, true, !GeneralData.AUTOMATICLOT_IN, true, true, true, GeneralData.LOTWITHCOST, true };
	private MedicalInventory inventory = null;
	private JRadioButton specificRadio;
	private JRadioButton allRadio;
	private JLabel dateInventoryLabel;
	private JTextField codeTextField;
	private String code = null;
	private String mode = null;
	private JLabel referenceLabel;
	private JLabel chargeTypeLabel;
	private JLabel dischargeTypeLabel;
	private JLabel supplierLabel;
	private JLabel destinationLabel;
	private JTextField referenceTextField;
	private JTextField jTetFieldEditor;
	private JComboBox<MovementType> chargeCombo;
	private JComboBox<MovementType> dischargeCombo;
	private JComboBox<Supplier> supplierCombo;
	private JComboBox<Ward> destinationCombo;
	private MedicalInventoryManager medicalInventoryManager = Context.getApplicationContext().getBean(MedicalInventoryManager.class);
	private MedicalInventoryRowManager medicalInventoryRowManager = Context.getApplicationContext().getBean(MedicalInventoryRowManager.class);
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);
	private MedicalDsrStockMovementTypeBrowserManager movTypeManager = Context.getApplicationContext().getBean(MedicalDsrStockMovementTypeBrowserManager.class);
	private SupplierBrowserManager supplierManager = Context.getApplicationContext().getBean(SupplierBrowserManager.class);
	private WardBrowserManager wardManager = Context.getApplicationContext().getBean(WardBrowserManager.class);

	
	public InventoryEdit() {
		initComponents();
		mode = "new";
	}
	
	private boolean isAutomaticLotIn() {
		return GeneralData.AUTOMATICLOT_IN;
	}
	
	public InventoryEdit(MedicalInventory inventory, String modee) {
		this.inventory = inventory;
		mode = modee;
		initComponents();
		if (mode.equals("view")) {
			saveButton.setVisible(false);
			deleteButton.setVisible(false);
			columnEditable = columnEditableView;
			codeTextField.setEditable(false);
		}
	}

	private void initComponents() {
		inventoryRowList = new ArrayList<>();
		inventoryRowSearchList = new ArrayList<>();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(950, 580));
		setLocationRelativeTo(null);
		if (this.inventory == null) {
			setTitle(MessageBundle.getMessage("angal.inventory.newinventory.title"));
		} else {
			setTitle(MessageBundle.getMessage("angal.inventory.editinventory.title"));
		}
		getContentPane().setLayout(new BorderLayout());

		panelHeader = getPanelHeader();

		getContentPane().add(panelHeader, BorderLayout.NORTH);

		panelContent = getPanelContent();
		getContentPane().add(panelContent, BorderLayout.CENTER);

		panelFooter = getPanelFooter();
		getContentPane().add(panelFooter, BorderLayout.SOUTH);

		ajustWidth();

		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				if (inventoryRowList != null) {
					inventoryRowList.clear();
				}
				if (inventoryRowSearchList != null) {
					inventoryRowSearchList.clear();
				}
				dispose();
			}
		});
	}

	private JPanel getPanelHeader() {
		if (panelHeader == null) {
			panelHeader = new JPanel();
			panelHeader.setBorder(new EmptyBorder(5, 0, 5, 0));
			GridBagLayout gbl_panelHeader = new GridBagLayout();
			gbl_panelHeader.columnWidths = new int[] { 159, 191, 192, 218, 51, 0 };
			gbl_panelHeader.rowHeights = new int[] { 30, 30, 0 };
			gbl_panelHeader.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			gbl_panelHeader.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panelHeader.setLayout(gbl_panelHeader);
			GridBagConstraints gbc_dateInventoryLabel = new GridBagConstraints();
			gbc_dateInventoryLabel.insets = new Insets(0, 0, 5, 5);
			gbc_dateInventoryLabel.gridx = 0;
			gbc_dateInventoryLabel.gridy = 0;
			panelHeader.add(getDateInventoryLabel(), gbc_dateInventoryLabel);

			GridBagConstraints gbc_jCalendarInventory = new GridBagConstraints();
			gbc_jCalendarInventory.fill = GridBagConstraints.HORIZONTAL;
			gbc_jCalendarInventory.insets = new Insets(0, 0, 5, 5);
			gbc_jCalendarInventory.gridx = 1;
			gbc_jCalendarInventory.gridy = 0;
			panelHeader.add(getJCalendarFrom(), gbc_jCalendarInventory);
			GridBagConstraints gbc_referenceLabel = new GridBagConstraints();
			gbc_referenceLabel.anchor = GridBagConstraints.EAST;
			gbc_referenceLabel.insets = new Insets(0, 0, 5, 5);
			gbc_referenceLabel.gridx = 2;
			gbc_referenceLabel.gridy = 0;
			panelHeader.add(getReferenceLabel(), gbc_referenceLabel);
			GridBagConstraints gbc_referenceTextField = new GridBagConstraints();
			gbc_referenceTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_referenceTextField.insets = new Insets(0, 0, 5, 5);
			gbc_referenceTextField.gridx = 3;
			gbc_referenceTextField.gridy = 0;
			panelHeader.add(getReferenceTextField(), gbc_referenceTextField);
			GridBagConstraints gbc_chargeLabel = new GridBagConstraints();
			gbc_chargeLabel.insets = new Insets(0, 0, 5, 5);
			gbc_chargeLabel.gridx = 0;
			gbc_chargeLabel.gridy = 1;
			panelHeader.add(getChargeLabel(), gbc_chargeLabel);
			GridBagConstraints gbc_jComboCharge = new GridBagConstraints();
			gbc_jComboCharge.fill = GridBagConstraints.HORIZONTAL;
			gbc_jComboCharge.insets = new Insets(0, 0, 5, 5);
			gbc_jComboCharge.gridx = 1;
			gbc_jComboCharge.gridy = 1;
			panelHeader.add(getJComboCharge(), gbc_jComboCharge);
			GridBagConstraints gbc_supplierLabel = new GridBagConstraints();
			gbc_supplierLabel.anchor = GridBagConstraints.EAST;
			gbc_supplierLabel.insets = new Insets(0, 0, 5, 5);
			gbc_supplierLabel.gridx = 2;
			gbc_supplierLabel.gridy = 1;
			panelHeader.add(getSupplierLabel(), gbc_supplierLabel);
			GridBagConstraints gbc_supplierCombo = new GridBagConstraints();
			gbc_supplierCombo.fill = GridBagConstraints.HORIZONTAL;
			gbc_supplierCombo.insets = new Insets(0, 0, 5, 5);
			gbc_supplierCombo.gridx = 3;
			gbc_supplierCombo.gridy = 1;
			panelHeader.add(getJComboSupplier(), gbc_supplierCombo);
			GridBagConstraints gbc_dischargeLabel = new GridBagConstraints();
			gbc_dischargeLabel.insets = new Insets(0, 0, 5, 5);
			gbc_dischargeLabel.gridx = 0;
			gbc_dischargeLabel.gridy = 2;
			panelHeader.add(getDischargeLabel(), gbc_dischargeLabel);
			GridBagConstraints gbc_dichargeCombo = new GridBagConstraints();
			gbc_dichargeCombo.fill = GridBagConstraints.HORIZONTAL;
			gbc_dichargeCombo.insets = new Insets(0, 0, 5, 5);
			gbc_dichargeCombo.gridx = 1;
			gbc_dichargeCombo.gridy = 2;
			panelHeader.add(getJComboDischarge(), gbc_dichargeCombo);
			GridBagConstraints gbc_destinationLabel = new GridBagConstraints();
			gbc_destinationLabel.anchor = GridBagConstraints.EAST;
			gbc_destinationLabel.insets = new Insets(0, 0, 5, 5);
			gbc_destinationLabel.gridx = 2;
			gbc_destinationLabel.gridy = 2;
			panelHeader.add(getDestinationLabel(), gbc_destinationLabel);
			GridBagConstraints gbc_destinationCombo = new GridBagConstraints();
			gbc_destinationCombo.fill = GridBagConstraints.HORIZONTAL;
			gbc_destinationCombo.insets = new Insets(0, 0, 5, 5);
			gbc_destinationCombo.gridx = 3;
			gbc_destinationCombo.gridy = 2;
			panelHeader.add(getJComboDestination(), gbc_destinationCombo);
			GridBagConstraints gbc_specificRadio = new GridBagConstraints();
			gbc_specificRadio.anchor = GridBagConstraints.EAST;
			gbc_specificRadio.insets = new Insets(0, 0, 0, 5);
			gbc_specificRadio.gridx = 0;
			gbc_specificRadio.gridy = 3;
			panelHeader.add(getSpecificRadio(), gbc_specificRadio);
			GridBagConstraints gbc_codeTextField = new GridBagConstraints();
			gbc_codeTextField.insets = new Insets(0, 0, 0, 5);
			gbc_codeTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_codeTextField.gridx = 1;
			gbc_codeTextField.gridy = 3;
			panelHeader.add(getCodeTextField(), gbc_codeTextField);
			GridBagConstraints gbc_allRadio = new GridBagConstraints();
			gbc_allRadio.anchor = GridBagConstraints.EAST;
			gbc_allRadio.insets = new Insets(0, 0, 0, 5);
			gbc_allRadio.gridx = 2;
			gbc_allRadio.gridy = 3;
			panelHeader.add(getAllRadio(), gbc_allRadio);
			ButtonGroup group = new ButtonGroup();
			group.add(specificRadio);
			group.add(allRadio);
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
			panelFooter.add(getNewButton());
			panelFooter.add(getDeleteButton());
			panelFooter.add(getCloseButton());
			panelFooter.add(getCleanTableButton());
		}
		return panelFooter;
	}

	private GoodDateChooser getJCalendarFrom() {
		if (jCalendarInventory == null) {
			jCalendarInventory = new GoodDateChooser(LocalDate.now());
			if (inventory != null) {
				jCalendarInventory.setDate(inventory.getInventoryDate().toLocalDate());
			}
			jCalendarInventory.addDateChangeListener(event -> {
				dateInventory = jCalendarInventory.getDateStartOfDay();
			});
		}
		return jCalendarInventory;
	}

	@SuppressWarnings("unused")
	private JButton getNewButton() {
		saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
		saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
		saveButton.addActionListener(actionEvent -> {
			String State = InventoryStatus.draft.toString();
			String user = UserBrowsingManager.getCurrentUser();
			int checkResults = 0;
			if (inventoryRowSearchList == null || inventoryRowSearchList.size() < 1) {
				MessageDialog.error(null, "angal.inventory.noproduct.msg");
				return;
			}
			LocalDateTime now = LocalDateTime.now();
			if (dateInventory.isAfter(now)) {
				MessageDialog.error(null, "angal.inventory.notdateinfuture.msg");
				return;
			}
			List<MedicalInventoryRow> invRowWithSameLots = checkDuplicateLotForSameMedical();
			if (invRowWithSameLots.size() > 0) {
				String message = "";
				for (MedicalInventoryRow invR: invRowWithSameLots) {
					message = message.concat(invR.getMedical().getDescription()+",\n ");
				}
				MessageDialog.error(null, "angal.inventory.thosemedicalhavethesamelot.fmt.msg", message);
				return ;
			}
			if ((inventory == null) && (mode.equals("new"))) {
				String reference = referenceTextField.getText().trim();
				if (reference.equals("")) {
					MessageDialog.error(null, "angal.inventory.mustenterareference.msg");
					return;
				}
				if (medicalInventoryManager.referenceExists(reference)) {
					MessageDialog.error(null, "angal.inventory.referencealreadyused.msg");
					return;
				}
				inventory = new MedicalInventory();
				inventory.setInventoryReference(reference);
				inventory.setInventoryDate(dateInventory);
				inventory.setStatus(State);
				inventory.setUser(user);
				inventory.setInventoryType(InventoryType.main.toString());
				MedicalInventory meInventory;
				try {
					meInventory = medicalInventoryManager.newMedicalInventory(inventory);
					if (meInventory != null) {
						MedicalInventoryRow currentInventoryRow;
						for (Iterator<MedicalInventoryRow> iterator = inventoryRowSearchList.iterator(); iterator.hasNext();) {
							MedicalInventoryRow medicalInventoryRow = (MedicalInventoryRow) iterator.next();
							medicalInventoryRow.setInventory(meInventory);
							Lot lot = medicalInventoryRow.getLot();
							String lotCode = lot.getCode();
							Medical medical = medicalInventoryRow.getMedical();
							if (lot != null) {
								lotCode = lot.getCode();
								Lot lotExist = movStockInsertingManager.getLot(lotCode);
								if (lotExist != null) {
									Lot lotStore = null;
									lotStore = movStockInsertingManager.updateLot(lot);
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
							currentInventoryRow = medicalInventoryRowManager.newMedicalInventoryRow(medicalInventoryRow);
							if (currentInventoryRow == null) {
								checkResults++;
							}
						}
						if (checkResults == 0) {
							// enable validation
							mode = "update";
							MessageDialog.info(this, "angal.inventory.savesuccess.msg");
							closeButton.doClick();
						} else {
							MessageDialog.error(null, "angal.inventory.error.msg");
						}
					} else {
						MessageDialog.error(null, "angal.inventory.error.msg");
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			} else if ((inventory != null) && (mode.equals("update"))) {
				checkResults = 0;
				boolean toUpdate = false;
				if (!inventory.getInventoryDate().equals(dateInventory)) {
					inventory.setInventoryDate(dateInventory);
					toUpdate = true;
				}
				if (!inventory.getUser().equals(user)) {
					inventory.setUser(user);
					toUpdate = true;
				}
				if (toUpdate) {
					try {
						inventory = medicalInventoryManager.updateMedicalInventory(inventory);
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
						return;
					}
				}
				try {
					for (Iterator<MedicalInventoryRow> iterator = inventoryRowSearchList.iterator(); iterator.hasNext();) {
						MedicalInventoryRow medicalInventoryRow = iterator.next();
						MedicalInventoryRow updateMedicalInvRow;
						Medical medical = medicalInventoryRow.getMedical();
						Lot lot = medicalInventoryRow.getLot();
						String lotCode ;
						medicalInventoryRow.setInventory(inventory);
						if (medicalInventoryRow.getId() == 0) {
							if (lot != null) {
								lotCode = lot.getCode();
								boolean isExist = false;
								try {
									Lot lotExist = movStockInsertingManager.getLot(lotCode);
									if (lotExist != null) {
										isExist = true;
									}
								} catch (OHServiceException e) {
									OHServiceExceptionUtil.showMessages(e);
									return ;
								}
								if (!isExist) {
									if (lot.getDueDate() != null) {
										Lot lotStore = movStockInsertingManager.storeLot(lotCode, lot, medical);
										medicalInventoryRow.setLot(lotStore);
										medicalInventoryRow.setNewLot(true);
									} else {
										medicalInventoryRow.setLot(null);
									}
								} else {
									Lot lotStore = movStockInsertingManager.updateLot(lot);
									medicalInventoryRow.setLot(lotStore);
								}
							}
							updateMedicalInvRow = medicalInventoryRowManager.newMedicalInventoryRow(medicalInventoryRow);
						} else {
							lot = medicalInventoryRow.getLot();
							if (lot != null) {
								lotCode = lot.getCode();
								Lot lotExist = movStockInsertingManager.getLot(lotCode);
								if (lotExist != null) {
									Lot lotStore;
									lotExist.setDueDate(lot.getDueDate());
									lotExist.setPreparationDate(lot.getPreparationDate());
									lotExist.setCost(lot.getCost());
									lotStore = movStockInsertingManager.updateLot(lotExist);
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
							updateMedicalInvRow = medicalInventoryRowManager.updateMedicalInventoryRow(medicalInventoryRow);
						}
						if (updateMedicalInvRow == null) {
							checkResults++;
						}
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
					return ;
				}
				if (checkResults == 0) {
					MessageDialog.info(null, "angal.inventory.update.success.msg");
					closeButton.doClick();
				} else {
					MessageDialog.error(null, "angal.inventory.update.error.msg");
				}
			}
		});
		return saveButton;
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
				if (inventory == null) {
					for (int i = selectedRows.length - 1; i >= 0; i--) {
						MedicalInventoryRow selectedInventoryRow = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRows[i], -1);
						inventoryRowSearchList.remove(selectedInventoryRow);
	                }
				} else {
					List<MedicalInventoryRow> inventoryRowsToDelete = new ArrayList<>();
					for (int i = selectedRows.length - 1; i >= 0; i--) {
						MedicalInventoryRow inventoryRow = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRows[i], -1);
						inventoryRowsToDelete.add(inventoryRow);
					}
					try {
						medicalInventoryRowManager.deleteMedicalInventoryRows(inventoryRowsToDelete);
						inventoryRowSearchList.removeAll(inventoryRowsToDelete);
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
						return ;
					}
				}
			} else {
				return;
			}
			jTableInventoryRow.updateUI();
		});
		return deleteButton;
	}
	private JButton getCloseButton() {
		closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		closeButton.addActionListener(actionEvent -> {
			dispose();
		});
		return closeButton;
	}
	
	private JButton getCleanTableButton() {
		resetButton = new JButton(MessageBundle.getMessage("angal.inventory.clean.btn"));
		resetButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.clean.btn.key"));
		resetButton.addActionListener(actionEvent -> {
			int reset = MessageDialog.yesNo(null, "angal.inventoryrow.doyoureallywanttocleanthistable.msg");
			if (reset == JOptionPane.YES_OPTION) {
				if (inventory == null) {
					if (inventoryRowList != null) {
						inventoryRowList.clear();
					}
					if (inventoryRowSearchList != null) {
						inventoryRowSearchList.clear();
					}
				} else {
					List<MedicalInventoryRow> inventoryRowsToDelete = new ArrayList<>();
					for (MedicalInventoryRow invRoww : inventoryRowSearchList) {
						inventoryRowsToDelete.add(invRoww);
					}
					try {
						medicalInventoryRowManager.deleteMedicalInventoryRows(inventoryRowsToDelete);
						inventoryRowSearchList.clear();
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
						return ;
					}
				}
				jTableInventoryRow.updateUI();
				ajustWidth();
			}
		});
		return resetButton;
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
			jTetFieldEditor = new JTextField();
			jTableInventoryRow.setFillsViewportHeight(true);
			jTableInventoryRow.setModel(new InventoryRowModel());
			for (int i = 0; i < pColumnVisible.length; i++) {
				jTableInventoryRow.getColumnModel().getColumn(i).setCellRenderer(new EnabledTableCellRenderer());
				jTableInventoryRow.getColumnModel().getColumn(i).setPreferredWidth(pColumwidth[i]);
				if (!pColumnVisible[i]) {
					jTableInventoryRow.getColumnModel().getColumn(i).setMinWidth(0);
					jTableInventoryRow.getColumnModel().getColumn(i).setMaxWidth(0);
					jTableInventoryRow.getColumnModel().getColumn(i).setWidth(0);
				}
			}
			jTableInventoryRow.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting()) {
						jTableInventoryRow.editCellAt(jTableInventoryRow.getSelectedRow(), jTableInventoryRow.getSelectedColumn());
						jTetFieldEditor.selectAll();
					}

				}
			});
			DefaultCellEditor cellEditor = new DefaultCellEditor(jTetFieldEditor);
			jTableInventoryRow.setDefaultEditor(Integer.class, cellEditor);
		}
		return jTableInventoryRow;
	}

	class EnabledTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			return cell;
		}
	}
	class InventoryRowModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public InventoryRowModel() throws OHServiceException {
			
			if (inventory == null) {
				if (allRadio.isSelected()) { // insert
					inventoryRowList = loadNewInventoryTable(null);
				} else if (specificRadio.isSelected() && code != null && !code.trim().equals("")) {
					inventoryRowList = loadNewInventoryTable(code);
				}
			} else { // updating
				if (allRadio.isSelected()) {
					inventoryRowList = medicalInventoryRowManager.getMedicalInventoryRowByInventoryId(inventory.getId());
				} else if (specificRadio.isSelected() && code != null && !code.trim().equals("")) {
					inventoryRowList = medicalInventoryRowManager.getMedicalInventoryRowByInventoryId(inventory.getId()).stream().filter(medRow -> medRow.getMedical().getProdCode().equals(code)).toList();
				}
			}
			if(inventoryRowList != null) {
				inventoryRowSearchList = new ArrayList<MedicalInventoryRow>();
				inventoryRowSearchList.addAll(inventoryRowList);
				inventoryRowSearchList.sort((p1, p2) -> p1.getMedical().getDescription().compareTo(p2.getMedical().getDescription()));
			}
				
		}

		public Class< ? > getColumnClass(int c) {
			if (c == 0) {
				return String.class;
			} else if (c == 1) {
				return String.class;
			} else if (c == 2) {
				return String.class;
			} else if (c == 3) {
				return String.class;
			} else if (c == 4) {
				return Integer.class;
			} else if (c == 5) {
				return Integer.class;
			} else if (c == 6) {
				return Integer.class;
			} else if (c == 7) {
				return Integer.class;
			}
			return null;
		}

		public int getRowCount() {
			if (inventoryRowSearchList == null)
				return 0;
			return inventoryRowSearchList.size();
		}

		public String getColumnName(int c) {
			return pColums[c];
		}

		public int getColumnCount() {
			return pColums.length;
		}

		public Object getValueAt(int r, int c) {
			MedicalInventoryRow medInvtRow = inventoryRowSearchList.get(r);
			if (c == -1) {
				return medInvtRow;
			} else if (c == 0) {
				return medInvtRow.getMedical() == null ? "" : medInvtRow.getMedical().getProdCode();
			} else if (c == 1) {
				return medInvtRow.getMedical() == null ? "" : medInvtRow.getMedical().getDescription();
			} else if (c == 2) {
				if (medInvtRow.getLot() == null) {
					return "";
				}
				return medInvtRow.getLot().getCode();
			} else if (c == 3) {
				if (medInvtRow.getLot() != null) {
					if (medInvtRow.getLot().getDueDate() != null) {
						return medInvtRow.getLot().getDueDate().format(DATE_TIME_FORMATTER);
					}
				}
				return "";
			} else if (c == 4) {
				Double dblVal = medInvtRow.getTheoreticQty();
				return dblVal.intValue();
			} else if (c == 5) {
				Double dblValue = medInvtRow.getRealQty();
				return dblValue.intValue();
			} else if (c == 6) {
				if (medInvtRow.getLot() != null) {
					if (medInvtRow.getLot().getCost() != null) {
						return medInvtRow.getLot().getCost();
					}
				}
				return 0.0;
			} else if (c == 7) {
				if (medInvtRow.getLot() != null) {
					if (medInvtRow.getLot().getCost() != null) {
						return medInvtRow.getRealQty() * medInvtRow.getLot().getCost().doubleValue();
					}
				}
				return 0.0;
			}
			return null;
		}

		@Override
		public void setValueAt(Object value, int r, int c) {
			if (r < inventoryRowSearchList.size()) {
				MedicalInventoryRow invRow = inventoryRowSearchList.get(r);
				Medical medical= invRow.getMedical();
				if (c == 2) {
					Lot lot = getLot(value.toString());
					if(lot == null) {
						return ;
					}
					lot.setMedical(medical);
					invRow.setLot(lot);
					inventoryRowSearchList.set(r, invRow);
				}
				if (c == 3) {
					Lot lot = invRow.getLot();
					if(lot == null) {
						Lot lotToStore = getLot(value.toString());
						if(lotToStore == null) {
							return ;
						}
						lotToStore.setMedical(medical);
						invRow.setLot(lotToStore);
					} else {
						if (lot.getCode().equals("")) {
							lot = getLot("");
							lot.setMedical(medical);
						} else {
							SimpleDateFormat dateFormatString = new SimpleDateFormat("dd/MM/yyyy hh:mm");
							Date dueDate = new Date();
							try {
								dueDate = dateFormatString.parse(value.toString());
							} catch (ParseException e) {
								e.printStackTrace();
								return ;
							}
							LocalDateTime date = dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
							lot.setDueDate(date);
						}
						invRow.setLot(lot);
					}
					inventoryRowSearchList.set(r, invRow);
				}
				if (c == 5) {
					Integer intValue = 0;
					try {
						intValue = Integer.parseInt(value.toString());
					} catch (NumberFormatException e) {
						intValue = 0;
						return ;
					}

					invRow.setRealqty(intValue);
					inventoryRowSearchList.set(r, invRow);
				}
				if (c == 6) {
					Double doubleValue = 0.0;
					try {
						doubleValue = Double.parseDouble(value.toString());
					} catch (NumberFormatException e) {
						doubleValue = 0.0;
						return ;
					}
					Lot lot = invRow.getLot();
					if (!isAutomaticLotIn()) {
						if (lot == null || lot.getCode().equals("")) {
							MessageDialog.error(null, "angal.inventoryrow.cannotchangethepriceofproductwithoutlot.msg");
							doubleValue = 0.0;
							return ;
						}
					}
					lot.setCost(new BigDecimal(doubleValue));
					invRow.setLot(lot);
					inventoryRowSearchList.set(r, invRow);
				}
				fireTableCellUpdated(r, c);
				jTableInventoryRow.updateUI();
			}

		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnEditable[columnIndex];
		}
	}
	
	private Lot getLot(String lotCode) {
		Lot lot = null;
		if (isAutomaticLotIn()) {
			LocalDateTime preparationDate = TimeTools.getNow().truncatedTo(ChronoUnit.MINUTES);
			LocalDateTime expiringDate = askExpiringDate();
			lot = new Lot("", preparationDate, expiringDate);
			// Cost
			BigDecimal cost = new BigDecimal(0);
			if (GeneralData.LOTWITHCOST) {
				cost = askCost(2);
				if (cost.compareTo(new BigDecimal(0)) == 0) {
					return null;
				}
			}
			lot.setCost(cost);
		} else {
			lot = askLot(lotCode);
		}
		return lot;
	}
	
	private Lot askLot(String lotCode) {
		LocalDateTime preparationDate;
		LocalDateTime expiringDate;
		Lot lot = null;

		JTextField lotNameTextField = new JTextField(15);
		lotNameTextField.addAncestorListener(new RequestFocusListener());
		TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotid"), lotNameTextField);
		suggestion.setFont(new Font("Tahoma", Font.PLAIN, 14)); //$NON-NLS-1$
		suggestion.setForeground(Color.GRAY);
		suggestion.setHorizontalAlignment(SwingConstants.CENTER);
		suggestion.changeAlpha(0.5f);
		suggestion.changeStyle(Font.BOLD + Font.ITALIC);
		if (lotCode.trim().length() != 0) {
			lotNameTextField.setText(lotCode);
		}
		LocalDate now = LocalDate.now();
		GoodDateChooser preparationDateChooser = new GoodDateChooser(now);
		GoodDateChooser expireDateChooser = new GoodDateChooser(now);
		JPanel panel = new JPanel(new GridLayout(3, 2));
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotnumberabb"))); //$NON-NLS-1$
		panel.add(lotNameTextField);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.preparationdate"))); //$NON-NLS-1$
		panel.add(preparationDateChooser);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate"))); //$NON-NLS-1$
		panel.add(expireDateChooser);
		do {
			int ok = JOptionPane.showConfirmDialog(
							this,
							panel,
							MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotinformations"), //$NON-NLS-1$
							JOptionPane.OK_CANCEL_OPTION);

			if (ok == JOptionPane.OK_OPTION) {
				String lotName = lotNameTextField.getText();
				if (expireDateChooser.getDate().isBefore(preparationDateChooser.getDate())) {
					MessageDialog.error(this, "angal.medicalstock.multiplecharging.expirydatebeforepreparationdate");
				} else {
					expiringDate = expireDateChooser.getDateEndOfDay();
					preparationDate = preparationDateChooser.getDateStartOfDay();
					lot = new Lot(lotName, preparationDate, expiringDate);
					if (GeneralData.LOTWITHCOST) {
						BigDecimal cost = askCost(2);
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
	private BigDecimal askCost(int qty) {
		double cost = 0.;
		do {
			String input = JOptionPane.showInputDialog(this,
							MessageBundle.getMessage("angal.medicalstock.multiplecharging.unitcost"), //$NON-NLS-1$
							0.);
			if (input != null) {
				try {
					cost = Double.parseDouble(input);
					if (cost < 0) {
						throw new NumberFormatException();
					} else if (cost == 0.) {
						double total = askTotalCost();
						// if (total == 0.) return;
						cost = total / qty;
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
	
	protected LocalDateTime askExpiringDate() {
		LocalDateTime date = TimeTools.getNow();
		GoodDateTimeSpinnerChooser expireDateChooser = new GoodDateTimeSpinnerChooser(date);
		JPanel panel = new JPanel(new GridLayout(1, 2));
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate"))); //$NON-NLS-1$
		panel.add(expireDateChooser);

		int ok = JOptionPane.showConfirmDialog(this, panel,
						MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate"), //$NON-NLS-1$
						JOptionPane.OK_CANCEL_OPTION);

		if (ok == JOptionPane.OK_OPTION) {
			date = expireDateChooser.getLocalDateTime();
		}
		return date;
	}
	
	protected double askTotalCost() {
		String input = JOptionPane.showInputDialog(this,
						MessageBundle.getMessage("angal.medicalstock.multiplecharging.totalcost"), //$NON-NLS-1$
						0.);
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

	public MedicalInventory getInventory() {
		return inventory;
	}

	public void setInventory(MedicalInventory inventory) {
		this.inventory = inventory;
	}

	private JRadioButton getSpecificRadio() {
		if (specificRadio == null) {
			specificRadio = new JRadioButton(MessageBundle.getMessage("angal.inventory.specificproduct.txt"));
			if (inventory != null) {
				specificRadio.setSelected(false);
			} else {
				specificRadio.setSelected(true);
			}
			specificRadio.addActionListener(actionEvent -> {
				if (specificRadio.isSelected()) {
					codeTextField.setEnabled(true);
					codeTextField.setText("");				}
			});
		}
		return specificRadio;
	}

	private JRadioButton getAllRadio() {
		if (allRadio == null) {
			allRadio = new JRadioButton(MessageBundle.getMessage("angal.inventory.allproduct.txt"));
			if (inventory != null) {
				allRadio.setSelected(true);
			} else {
				allRadio.setSelected(false);
			}
			allRadio.addActionListener(actionEvent -> {
				if (allRadio.isSelected()) {
					codeTextField.setEnabled(false);
					codeTextField.setText("");
					try {
						jTableInventoryRow.setModel(new InventoryRowModel());
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
					jTableInventoryRow.updateUI();
					code = null;
					ajustWidth();
				}
			});
		}
		return allRadio;
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
			if (inventory != null) {
				codeTextField.setEnabled(false);
			} else {
				codeTextField.setEnabled(true);
			}
			codeTextField.setColumns(10);
			TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.common.code.txt"), codeTextField, Show.FOCUS_LOST);
			suggestion.setFont(new Font("Tahoma", Font.PLAIN, 12));
			suggestion.setForeground(Color.GRAY);
			suggestion.setHorizontalAlignment(JLabel.CENTER);
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
						codeTextField.setText("");
					}
				}
			});
		}
		return codeTextField;
	}

	private List<MedicalInventoryRow> loadNewInventoryTable(String code) throws OHServiceException {	
		List<MedicalInventoryRow> inventoryRowsList = new ArrayList<>();
		List<Medical> medicalList = new ArrayList<>();
		List<Lot> lots = null;
		Medical medical = null;
		MedicalInventoryRow inventoryRowTemp = null;
		if (code != null) {
			medical = medicalBrowsingManager.getMedicalByMedicalCode(code);
			if (medical != null) {
				medicalList.add(medical);
			} else {
				MessageDialog.error(null, MessageBundle.getMessage("angal.inventory.noproductfound"));
			}
		} else {
			medicalList = medicalBrowsingManager.getMedicals();
		}
		for (Iterator<Medical> iterator = medicalList.iterator(); iterator.hasNext();) {
			medical = iterator.next();
			lots = movStockInsertingManager.getLotByMedical(medical);
			if ((lots.size() == 0)) {
				inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, medical, new Lot("", null, null));
				inventoryRowsList.add(inventoryRowTemp);
			}
			for (Iterator<Lot> iterator2 = lots.iterator(); iterator2.hasNext();) {
				Lot lot = iterator2.next();
				inventoryRowTemp = new MedicalInventoryRow(0, lot.getOverallQuantity(), lot.getOverallQuantity(),null, medical, lot);
				inventoryRowsList.add(inventoryRowTemp);
			}
		}		
		return inventoryRowsList;
	}
	private void addInventoryRow(String code) throws OHServiceException {
		List<MedicalInventoryRow> inventoryRowsList = new ArrayList<MedicalInventoryRow>();
		List<Medical> medicalList = new ArrayList<Medical>();
		List<Lot> lots = null;
		Medical medical = null;
		MedicalInventoryRow inventoryRowTemp = null;
		if (code != null) {
			medical = medicalBrowsingManager.getMedicalByMedicalCode(code);
			if (medical != null) {
				if (inventoryRowSearchList != null) {
					medicalList.add(medical);
				}
			} else {
				medical = chooseMedical(code);
				if (medical != null) {
					medicalList.add(medical);
				}
			}
		} else {
			medicalList = medicalBrowsingManager.getMedicals();
		}
		for (Iterator<Medical> iterator = medicalList.iterator(); iterator.hasNext();) {
			Medical med = (Medical) iterator.next();
			List<MedicalInventoryRow> medRow = inventoryRowSearchList.stream().filter(invR -> invR.getMedical().getCode().equals(med.getCode())).collect(Collectors.toList());
			if (medRow.size() > 0) {
				inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, med, new Lot("", null, null));
				inventoryRowsList.add(inventoryRowTemp);
			} else {
				lots = movStockInsertingManager.getLotByMedical(med);
				if (lots.size() == 0) {
					inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, med, new Lot("", null, null));
					inventoryRowsList.add(inventoryRowTemp);
				} else {
					for (Iterator<Lot> iterator2 = lots.iterator(); iterator2.hasNext();) {
						Lot lot = (Lot) iterator2.next();
						inventoryRowTemp = new MedicalInventoryRow(0, lot.getOverallQuantity(), lot.getOverallQuantity(), null, med, lot);
						inventoryRowsList.add(inventoryRowTemp);
					}
				}
			}
		}
		for (MedicalInventoryRow inventoryRow : inventoryRowsList) {
			int position = getPosition(inventoryRow);
			if (position == -1) {
				inventoryRowSearchList.add(inventoryRow);
			} else {
				inventoryRowSearchList.add(position + 1, inventoryRow);
			}
		}
		jTableInventoryRow.updateUI();
	}

	private Medical chooseMedical(String text) throws OHServiceException {
		Map<String, Medical> medicalMap;
		List<Medical> medicals = medicalBrowsingManager.getMedicals();
		medicalMap = new HashMap<String, Medical>();
		for (Medical med : medicals) {
			String key = med.getProdCode().toLowerCase();
			key = med.getCode().toString().toLowerCase();
			medicalMap.put(key, med);
		}
		ArrayList<Medical> medList = new ArrayList<Medical>();
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
			dialog.setTitle(MessageBundle.getMessage("angal.medicalstock.multiplecharging.selectmedical.title"));
			framas.setParentFrame(dialog);
			dialog.setContentPane(framas);
			dialog.setVisible(true);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			med = framas.getSelectedMedical();
			return med;
		}
		return null;
	}

	private void ajustWidth() {
		for (int i = 0; i < pColumwidth.length; i++) {
			jTableInventoryRow.getColumnModel().getColumn(i).setMinWidth(pColumwidth[i]);
		}
	}

	public EventListenerList getInventoryListeners() {
		return InventoryListeners;
	}

	public void setInventoryListeners(EventListenerList inventoryListeners) {
		InventoryListeners = inventoryListeners;
	}

	private JLabel getReferenceLabel() {
		if (referenceLabel == null) {
			referenceLabel = new JLabel(MessageBundle.getMessage("angal.common.reference.label"));
		}
		return referenceLabel;
	}

	private JLabel getChargeLabel() {
		if (chargeTypeLabel == null) {
			chargeTypeLabel = new JLabel(MessageBundle.getMessage("angal.inventory.chargetype.label"));
		}
		return chargeTypeLabel;
	}
	
	private JLabel getSupplierLabel() {
		if (supplierLabel == null) {
			supplierLabel = new JLabel(MessageBundle.getMessage("angal.inventory.supplier.label"));
		}
		return supplierLabel;
	}
	
	private JLabel getDischargeLabel() {
		if (dischargeTypeLabel == null) {
			dischargeTypeLabel = new JLabel(MessageBundle.getMessage("angal.inventory.dischargetype.label"));
		}
		return dischargeTypeLabel;
	}
	
	private JLabel getDestinationLabel() {
		if (destinationLabel == null) {
			destinationLabel = new JLabel(MessageBundle.getMessage("angal.inventory.destination.label"));
		}
		return destinationLabel;
	}
	
	private JComboBox<MovementType> getJComboCharge() {
		if (chargeCombo == null) {
			chargeCombo = new JComboBox<MovementType>();
			try {
				List<MovementType> movementTypes = movTypeManager.getMedicalDsrStockMovementType();
				for(MovementType movType: movementTypes) {
					if(movType.getType().equals("+")) {
						chargeCombo.addItem(movType);
					}
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		return chargeCombo;
	}
	
	private JComboBox<MovementType> getJComboDischarge() {
		if (dischargeCombo == null) {
			dischargeCombo = new JComboBox<MovementType>();
			try {
				List<MovementType> movementTypes = movTypeManager.getMedicalDsrStockMovementType();
				for(MovementType movType: movementTypes) {
					if(movType.getType().equals("-")) {
						dischargeCombo.addItem(movType);
					}	
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		return dischargeCombo;
	}
	
	private JComboBox<Supplier> getJComboSupplier() {
		if (supplierCombo == null) {
			supplierCombo = new JComboBox<Supplier>();
			try {
				List<Supplier> suppliers = supplierManager.getList();
				for(Supplier supplier: suppliers) {
					supplierCombo.addItem(supplier);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		return supplierCombo;
	}
	
	private JComboBox<Ward> getJComboDestination() {
		if (destinationCombo == null) {
			destinationCombo = new JComboBox<Ward>();
			try {
				List<Ward> wards = wardManager.getWards();
				for(Ward ward: wards) {
					destinationCombo.addItem(ward);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		return destinationCombo;
	}
	
	private JTextField getReferenceTextField() {
		if (referenceTextField == null) {
			referenceTextField = new JTextField();
			referenceTextField.setColumns(10);
			if (inventory != null && !mode.equals("new")) {
				referenceTextField.setText(inventory.getInventoryReference());
			}
		}
		return referenceTextField;
	}
	
	private int getPosition (MedicalInventoryRow inventoryRow) {
		int position = -1;
		int i = 0;
		for (MedicalInventoryRow invR: inventoryRowSearchList) {
			if (invR.getMedical().getCode().equals(inventoryRow.getMedical().getCode())) {
				position = i;
			}
			i = i + 1;
		}
		return position;
	}
	private List<MedicalInventoryRow> checkDuplicateLotForSameMedical() {
		 Map<MedicalInventoryRow, Integer> frequencyMap = new HashMap<>();
	     List<MedicalInventoryRow> duplicates = new ArrayList<>();
	     for (MedicalInventoryRow invRow : inventoryRowSearchList) {
	         frequencyMap.put(invRow, frequencyMap.getOrDefault(invRow, 0) + 1);
	     }
	     for (Map.Entry<MedicalInventoryRow, Integer> entry : frequencyMap.entrySet()) {
	    	 if (entry.getValue() > 1) {
	    		 duplicates.add(entry.getKey());
	    	 }
	     }
	     return duplicates;
	}
}
