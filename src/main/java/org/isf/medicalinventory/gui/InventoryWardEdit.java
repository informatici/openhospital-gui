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
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
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
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
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
import org.isf.medicalstock.manager.MovBrowserManager;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.medicalstock.model.Movement;
import org.isf.medicalstockward.manager.MovWardBrowserManager;
import org.isf.medicalstockward.model.MovementWard;
import org.isf.medtype.manager.MedicalTypeBrowserManager;
import org.isf.medtype.model.MedicalType;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.stat.gui.report.GenericReportPharmaceuticalInventory;
import org.isf.utils.db.NormalizeString;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.GoodDateTimeToggleChooser;
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
		jCalendarInventoryDate.setDateTime(dateInventory);
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
		jCalendarInventoryDate.setDateTime(dateInventory);
		jTableInventoryRow.updateUI();
	}

	private GoodDateTimeToggleChooser jCalendarInventoryDate;
	private LocalDateTime dateInventory = TimeTools.getNow().truncatedTo(ChronoUnit.MINUTES);
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
	private DefaultTableModel model;
	private List<MedicalInventoryRow> inventoryRowList = new ArrayList<>();
	private List<MedicalInventoryRow> inventoryRowSearchList = new ArrayList<>();
	private List<MedicalInventoryRow> inventoryRowsToDelete = new ArrayList<>();
	private List<MedicalInventoryRow> inventoryRowListAdded = new ArrayList<>();
	private List<Lot> lotsSaved = new ArrayList<>();
	private List<Lot> lotsDeleted = new ArrayList<>();
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
	private MedicalInventory inventory;
	private JLabel addMedicalLabel;
	private JButton selectButton;
	private JLabel dateInventoryLabel;
	private JTextField medicalCodeTextField;
	private String code;
	private String mode;
	private JLabel statusLabel;
	private String wardId = "";
	private JLabel referenceLabel;
	private JLabel addMedicalsByLabel;
	private JTextField referenceTextField;
	private JTextField jTextFieldEditor;
	private JLabel wardLabel;
	private JComboBox<Ward> wardComboBox;
	private String newReference;
	private JFrame frame;
	private JPanel mainPanel;
	private JRadioButton radioButtonAll;
	private JRadioButton radioOnlyNonZero;
	private JRadioButton radioWithMovement;
	private JButton jButtonCancel;
	private JButton jButtonOk;
	private MedicalType medicalTypeSelected;
	private JComboBox<MedicalType> medicalTypeComboBox;
	private List<Medical> medicals = new ArrayList<>();
	private Map<AbstractButton, Runnable> actions = new HashMap<>();
	private MedicalInventoryManager medicalInventoryManager = Context.getApplicationContext().getBean(MedicalInventoryManager.class);
	private MedicalInventoryRowManager medicalInventoryRowManager = Context.getApplicationContext().getBean(MedicalInventoryRowManager.class);
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);
	private WardBrowserManager wardManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private MedicalTypeBrowserManager medicalTypeManager = Context.getApplicationContext().getBean(MedicalTypeBrowserManager.class);
	private MovBrowserManager movBrowserManager = Context.getApplicationContext().getBean(MovBrowserManager.class);
	private MovWardBrowserManager movWardBrowserManager = Context.getApplicationContext().getBean(MovWardBrowserManager.class);
	private boolean allMedicals;
	private boolean allMedicalsChosen;
	private Object[] allMedicalsOrList = {
			MessageBundle.getMessage("angal.inventory.yesallmedicals.btn"),
			MessageBundle.getMessage("angal.inventory.noonlytheonesinthelist.btn")
	};

	public InventoryWardEdit() {
		mode = "new";
		initComponents();
		disableSomeComponents();
	}

	private boolean isAutomaticLotIn() {
		return GeneralData.AUTOMATICLOT_IN;
	}

	private boolean isLotWithCost() {
		return GeneralData.LOTWITHCOST;
	}

	public InventoryWardEdit(MedicalInventory inventory, String mod) {
		this.inventory = inventory;
		wardId = this.inventory.getWard();
		mode = mod;
		initComponents();
	}

	private void initComponents() {
		inventoryRowList = new ArrayList<>();
		inventoryRowSearchList = new ArrayList<>();
		try {
			medicals = medicalBrowsingManager.getMedicals();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setMinimumSize(new Dimension(1000, 600));
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
			referenceTextField.setEditable(false);
			jCalendarInventoryDate.setEnabled(false);
			selectButton.setEnabled(false);
			wardComboBox.setEnabled(false);
			printButton.setVisible(true);
			lotButton.setVisible(false);
			medicalCodeTextField.setEditable(false);

		} else {
			saveButton.setVisible(true);
			validateButton.setVisible(true);
			deleteButton.setVisible(true);
			medicalCodeTextField.setEditable(true);
			resetButton.setVisible(true);
			referenceTextField.setEditable(true);
			jCalendarInventoryDate.setEnabled(true);
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
			panelHeader.add(getJCalendarInventoryDate(), gbc_jCalendarInventory);
			GridBagConstraints gbc_referenceLabel = new GridBagConstraints();
			gbc_referenceLabel.anchor = GridBagConstraints.EAST;
			gbc_referenceLabel.insets = new Insets(0, 0, 5, 5);
			gbc_referenceLabel.gridx = 2;
			gbc_referenceLabel.gridy = 1;
			panelHeader.add(getReferenceLabel(), gbc_referenceLabel);
			GridBagConstraints gbc_referenceTextField = new GridBagConstraints();
			gbc_referenceTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_referenceTextField.insets = new Insets(0, 0, 5, 5);
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
			GridBagConstraints gbc_addMedicalLabel = new GridBagConstraints();
			gbc_addMedicalLabel.anchor = GridBagConstraints.EAST;
			gbc_addMedicalLabel.insets = new Insets(0, 0, 0, 5);
			gbc_addMedicalLabel.gridx = 0;
			gbc_addMedicalLabel.gridy = 3;
			panelHeader.add(getAddMedicalLabel(), gbc_addMedicalLabel);
			GridBagConstraints gbc_medicalCodeTextField = new GridBagConstraints();
			gbc_medicalCodeTextField.insets = new Insets(0, 0, 0, 5);
			gbc_medicalCodeTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_medicalCodeTextField.gridx = 1;
			gbc_medicalCodeTextField.gridy = 3;
			panelHeader.add(getMedicalCodeTextField(), gbc_medicalCodeTextField);
			GridBagConstraints gbc_addMedicalsByLabel = new GridBagConstraints();
			gbc_addMedicalsByLabel.anchor = GridBagConstraints.EAST;
			gbc_addMedicalsByLabel.insets = new Insets(0, 0, 0, 5);
			gbc_addMedicalsByLabel.gridx = 2;
			gbc_addMedicalsByLabel.gridy = 3;
			panelHeader.add(getAddMedicalsByLabel(), gbc_addMedicalsByLabel);
			GridBagConstraints gbc_selectButton = new GridBagConstraints();
			gbc_selectButton.anchor = GridBagConstraints.WEST;
			gbc_selectButton.insets = new Insets(0, 0, 0, 5);
			gbc_selectButton.gridx = 3;
			gbc_selectButton.gridy = 3;
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

	private GoodDateTimeToggleChooser getJCalendarInventoryDate() {
		if (jCalendarInventoryDate == null) {

			jCalendarInventoryDate = new GoodDateTimeToggleChooser(TimeTools.getNow());
			if (inventory != null) {
				jCalendarInventoryDate.setDateTime(inventory.getInventoryDate());
				dateInventory = inventory.getInventoryDate();
			}
			jCalendarInventoryDate.addDateTimeChangeListener(event -> {
				dateInventory = jCalendarInventoryDate.getLocalDateTime();
			});
		}
		return jCalendarInventoryDate;
	}

	private JButton getSelectButton() {
		if (selectButton == null) {
			selectButton = new JButton(MessageBundle.getMessage("angal.inventory.select.btn"));
			selectButton.addActionListener(actionEvent -> {
				mainPanel = new JPanel();
				mainPanel.setLayout(new BorderLayout(10, 10));

				JPanel leftPanel = new JPanel();
				leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				leftPanel.setLayout(new GridLayout(3, 1));
				leftPanel.add(new JLabel(MessageBundle.getMessage("angal.inventory.medicaltype.txt")));
				leftPanel.add(getJComboMedicalType());

				JPanel rightPanel = new JPanel();
				rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				rightPanel.setLayout(new GridLayout(3, 1, 5, 5));
				ButtonGroup radioGroup = new ButtonGroup();
				radioGroup.add(getAllRadioButton());
				radioGroup.add(getMedicalWithNonZeroQuantityRadioButton());
				radioGroup.add(getMedicalWithMovementRadioButton());

				// Map actions to buttons
				initializeActions();

				// Convert HashMap to TreeMap to sort keys
				Map<AbstractButton, Runnable> sortedActionMap = new TreeMap<>(Comparator.comparing(AbstractButton::getText));
				sortedActionMap.putAll(actions);

				// Add ActionListener to each button
				sortedActionMap.forEach((key, value) -> {
					rightPanel.add(key);
				});

				JPanel bottomPanel = new JPanel();
				bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
				bottomPanel.add(getOkButton());
				bottomPanel.add(getCancelButton());

				mainPanel.add(leftPanel, BorderLayout.CENTER);
				mainPanel.add(rightPanel, BorderLayout.EAST);
				mainPanel.add(bottomPanel, BorderLayout.SOUTH);

				frame = new JFrame();
				frame.add(mainPanel);
				frame.setSize(450, 200);
				frame.setTitle(MessageBundle.getMessage("angal.inventory.lotinformation.title"));
				frame.setLocationRelativeTo(null);
				frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				frame.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosing(WindowEvent e) {
						int choice = MessageDialog.yesNo(frame, "angal.inventory.areyousureyouwantoclosethiswindow.msg");
						if (choice == JOptionPane.YES_OPTION) {
							frame.dispose();
						}
					}
				});
				frame.setVisible(true);
			});
		}
		return selectButton;
	}

	private JButton getSaveButton() {
		saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
		saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
		saveButton.addActionListener(actionEvent -> {
			try {
				if (inventoryRowSearchList == null || inventoryRowSearchList.isEmpty()) {
					MessageDialog.error(null, "angal.inventory.cannotsaveinventorywithoutproducts.msg");
					return;
				}
				String status = InventoryStatus.draft.toString();
				String user = UserBrowsingManager.getCurrentUser();

				if (mode.equals("new")) {
					inventory = new MedicalInventory();
					inventory.setWard(wardId);
					inventory.setInventoryReference(referenceTextField.getText().trim());
					inventory.setInventoryDate(dateInventory);
					inventory.setStatus(status);
					inventory.setUser(user);
					inventory.setInventoryType(InventoryType.ward.toString());

					List<MedicalInventoryRow> newMedicalInventoryRows = new ArrayList<>();
					ListIterator<MedicalInventoryRow> inventoryRowSearchListIterator = inventoryRowSearchList.listIterator();
					while (inventoryRowSearchListIterator.hasNext()) {
						MedicalInventoryRow medicalInventoryRow = inventoryRowSearchListIterator.next();
						medicalInventoryRow.setInventory(inventory);
						Lot lot = medicalInventoryRow.getLot();
						Medical medical = medicalInventoryRow.getMedical();

						if (lot != null) {
							String lotCode = lot.getCode();
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
						}
						newMedicalInventoryRows.add(medicalInventoryRow);
					}
					inventory = medicalInventoryManager.newMedicalInventory(inventory, newMedicalInventoryRows);
					mode = "update";
					newReference = inventory.getInventoryReference();
					fireInventoryInserted();
					MessageDialog.info(this, "angal.inventory.savesuccess.msg");
					validateButton.setEnabled(true);
					confirmButton.setEnabled(false);
					resetVariables();

				} else if (mode.equals("update")) {
					int response = MessageDialog.yesNo(null, "angal.inventory.doyouwanttoupdatethisinventory.msg");
					if (response == JOptionPane.YES_OPTION) {
						String oldReference = inventory.getInventoryReference();
						newReference = referenceTextField.getText().trim();
						inventory.setInventoryReference(newReference);
						inventory.setInventoryDate(dateInventory);
						inventory.setStatus(status);
						inventory.setUser(user);
						inventory.setInventoryType(InventoryType.ward.toString());
						inventory = medicalInventoryManager.updateMedicalInventory(inventory, !oldReference.equals(newReference));

						ListIterator<MedicalInventoryRow> inventoryRowSearchListIterator = inventoryRowSearchList.listIterator();
						while (inventoryRowSearchListIterator.hasNext()) {
							MedicalInventoryRow medicalInventoryRow = inventoryRowSearchListIterator.next();
							Medical medical = medicalInventoryRow.getMedical();
							Lot lot = medicalInventoryRow.getLot();
							if (lot != null) {
								String lotCode = lot.getCode();
								Lot lotExist = movStockInsertingManager.getLot(lotCode);
								if (lotExist != null) {
									lot.setMedical(medical);
									lot = movStockInsertingManager.updateLot(lot);
									medicalInventoryRow.setLot(lot);
								} else {
									int idInvRow = medicalInventoryRow.getId();
									MedicalInventoryRow invRow = medicalInventoryRowManager.getMedicalInventoryRowById(idInvRow);
									if (invRow != null && invRow.getLock() != medicalInventoryRow.getLock()) {
										Lot newLot = movStockInsertingManager.storeLot(lotCode, lot, medical);
										invRow.setLot(newLot);
										invRow.setNewLot(true);
										invRow.setRealqty(medicalInventoryRow.getRealQty());
										medicalInventoryRow = invRow;
									} else {
										Lot newLot = movStockInsertingManager.storeLot(lotCode, lot, medical);
										medicalInventoryRow.setLot(newLot);
										medicalInventoryRow.setNewLot(true);
									}
								}
							}

							medicalInventoryRow.setInventory(inventory);
							int id = medicalInventoryRow.getId();
							if (id == 0) {
								MedicalInventoryRow savedRow = medicalInventoryRowManager.newMedicalInventoryRow(medicalInventoryRow);
								inventoryRowSearchListIterator.set(savedRow);
							} else {
								MedicalInventoryRow updatedRow = medicalInventoryRowManager.updateMedicalInventoryRow(medicalInventoryRow);
								inventoryRowSearchListIterator.set(updatedRow);
							}
						}

						fireInventoryUpdated();
						if (!lotsDeleted.isEmpty() || !inventoryRowsToDelete.isEmpty()) {
							lotsDeleted.removeAll(
								inventoryRowsToDelete.stream()
									.map(MedicalInventoryRow::getLot)
									.filter(Objects::nonNull)
									.collect(Collectors.toSet()));

							if (!inventoryRowsToDelete.isEmpty()) {
								medicalInventoryRowManager.deleteMedicalInventoryRows(inventoryRowsToDelete);
							}
							for (Lot lot : lotsDeleted) {
								movStockInsertingManager.deleteLot(lot);
							}
						}

						MessageDialog.info(null, "angal.inventory.update.success.msg");

						statusLabel.setText(status.toUpperCase());
						statusLabel.setForeground(Color.GRAY);
						resetVariables();
						validateButton.setEnabled(true);
						confirmButton.setEnabled(false);
					}
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
				return;
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
				MessageDialog.error(null, "angal.inventory.inventorymustbesavedbeforevalidation.msg");
				return;
			}
			List<MedicalInventoryRow> invRowWithoutLot = inventoryRowSearchList.stream().filter(invRow -> invRow.getLot() == null).collect(Collectors.toList());
			if (!invRowWithoutLot.isEmpty()) {
				MessageDialog.error(null, "angal.inventory.allinventoryrowshouldhavelotbeforevalidation.msg");
				return;
			}
			if (inventoryRowSearchList == null || inventoryRowSearchList.isEmpty()) {
				MessageDialog.error(null, "angal.inventory.cannotvalidateinventorywithoutproducts.msg");
				return;
			}
			String lastReference = inventory.getInventoryReference();
			LocalDateTime lastInventoryDate = inventory.getInventoryDate();
			List<MedicalInventoryRow> invRowWithoutRealQty = inventoryRowSearchList.stream().filter(invRow -> invRow.getRealQty() == 0 && invRow.isNewLot())
				.collect(Collectors.toList());
			if (!invRowWithoutRealQty.isEmpty()) {
				MessageDialog.error(null, "angal.inventory.allinventoryrowswithnewlotshouldhaverealqtygreatterthanzero.msg");
				return;
			}
			if (checkParametersChanges(lastReference, lastInventoryDate)) {
				MessageDialog.error(null, "angal.inventory.pleasesaveinventorybeforevalidateit.msg");
				return;
			}
			// validate inventory
			String status = InventoryStatus.validated.toString();
			String option = askAllMedicalsOrList("angal.inventory.doyouwanttocheckforallmedicalsinthestockoronlytheonesinthelist.msg");
			if (option == null) {
				return;
			}
			allMedicals = option.equals(allMedicalsOrList[0]); // Yes (All medicals)
			allMedicalsChosen = true;
			try {
				medicalInventoryManager.validateMedicalWardInventoryRow(inventory, inventoryRowSearchList, allMedicals);
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
						inventory = medicalInventoryManager.actualizeMedicalWardInventoryRow(inventory, allMedicals);
						dateInventory = inventory.getInventoryDate();
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
		});
		return validateButton;
	}

	private String askAllMedicalsOrList(String question) {
		String option = (String) MessageDialog.inputDialog(null, null, allMedicalsOrList, null, question);
		return option;
	}

	private JButton getConfirmButton() {
		confirmButton = new JButton(MessageBundle.getMessage("angal.inventory.confirm.btn"));
		confirmButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.confirm.btn.key"));
		if (inventory == null) {
			confirmButton.setEnabled(false);
		}
		confirmButton.addActionListener(actionEvent -> {
			if (inventory == null) {
				MessageDialog.error(null, "angal.inventory.inventorymustbesavedbeforevalidation.msg");
				return;
			}
			List<MedicalInventoryRow> invRowWithoutLot = inventoryRowSearchList.stream().filter(invRow -> invRow.getLot() == null).collect(Collectors.toList());
			if (!invRowWithoutLot.isEmpty()) {
				MessageDialog.error(null, "angal.inventory.allinventoryrowshouldhavelotbeforevalidation.msg");
				return;
			}
			int confirm = MessageDialog.yesNo(null, "angal.inventory.doyoureallywanttoconfirmthisinventory.msg");
			if (confirm == JOptionPane.YES_OPTION) {
				if (inventoryRowSearchList == null || inventoryRowSearchList.isEmpty()) {
					MessageDialog.error(null, "angal.inventory.cannotconfirminventorywithoutproducts.msg");
					return;
				}
				String lastReference = inventory.getInventoryReference();
				LocalDateTime lastDate = inventory.getInventoryDate();
				List<MedicalInventoryRow> invRowWithoutRealQty = inventoryRowSearchList.stream().filter(invRow -> invRow.getRealQty() == 0 && invRow.isNewLot())
					.collect(Collectors.toList());
				if (!invRowWithoutRealQty.isEmpty()) {
					MessageDialog.error(null, "angal.inventory.allinventoryrowswithnewlotshouldhaverealqtygreatterthanzero.msg");
					return;
				}
				if (checkParametersChanges(lastReference, lastDate)) {
					MessageDialog.error(null, "angal.inventory.pleasesaveinventorybeforeconfirmation.msg");
					return;
				}
				// confirm inventory
				String option = null;
				if (!allMedicalsChosen) { // if not asked in the same session
					option = askAllMedicalsOrList("angal.inventory.doyouwanttocheckforallmedicalsinthestockoronlytheonesinthelist.msg");
					if (option == null) {
						return;
					}
					allMedicals = option.equals(allMedicalsOrList[0]); // Yes (All medicals)
				}
				try {
					medicalInventoryManager.confirmMedicalWardInventoryRow(inventory, inventoryRowSearchList, allMedicals);
					MessageDialog.info(null, "angal.inventory.confirm.success.msg");
					fireInventoryUpdated();
					closeButton.doClick();
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
					MessageDialog.info(null, "angal.inventory.pleasevalidateinventoryagainsbeforeconfirmation.msg");
					confirmButton.setEnabled(false);
					inventory.setStatus(InventoryStatus.draft.toString());
					statusLabel.setText(InventoryStatus.draft.toString().toUpperCase());
					statusLabel.setForeground(Color.GRAY);
					try {
						inventory = medicalInventoryManager.updateMedicalInventory(inventory, true);
						fireInventoryUpdated();
					} catch (OHServiceException e1) {
						OHServiceExceptionUtil.showMessages(e1);
					}
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
				MessageDialog.error(this, "angal.inventory.pleaseselectatleastoneinventoryrow.msg");
				return;
			}
			int delete = MessageDialog.yesNo(null, "angal.inventory.doyoureallywanttodeletethisinventoryrow.msg");
			if (delete == JOptionPane.YES_OPTION) {
				if (selectedRows.length == inventoryRowSearchList.size()) {
					resetButton.doClick();
					return;
				}
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
				MessageDialog.error(this, "angal.inventory.pleaseselectonlyoneinventoryrow.msg");
				return;
			}
			MedicalInventoryRow selectedInventoryRow = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRow, -1);
			Lot oldLot = selectedInventoryRow.getLot(); // current lot
			Lot newLot;
			try {
				if (selectedInventoryRow.isNewLot()) {
					newLot = getLot(oldLot, selectedInventoryRow.getMedical()); // create or update lot
				} else {
					BigDecimal cost = (oldLot != null && oldLot.getCost() != null)
						? oldLot.getCost()
						: BigDecimal.ZERO;

					if (isLotWithCost()) {
						BigDecimal newCost = askCost(2, cost);
						if (newCost.compareTo(BigDecimal.ZERO) == 0) {
							return;
						}

						oldLot.setCost(newCost);
						newLot = oldLot;
					} else {
						newLot = oldLot;
					}
				}

				if (newLot != null) {
					String oldLotCode = (oldLot != null) ? oldLot.getCode() : null;
					String newLotCode = newLot.getCode();

					if (oldLotCode != null && !oldLotCode.equals(newLotCode)) {
						if (!lotsDeleted.contains(oldLot)) {
							lotsDeleted.add(oldLot);
						}
					}
					if (!lotsSaved.contains(newLot)) {
						lotsSaved.add(newLot);
					}
					selectedInventoryRow.setLot(newLot);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
				return;
			}
			inventoryRowSearchList.set(selectedRow, selectedInventoryRow);
			jTableInventoryRow.updateUI();
		});
		return lotButton;
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
			new GenericReportPharmaceuticalInventory(inventory, "Inventory", printRealQty);
		});
		return printButton;
	}

	private JButton getCloseButton() {
		closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		closeButton.addActionListener(actionEvent -> {
			String lastReference = null;
			newReference = referenceTextField.getText().trim();
			LocalDateTime lastDate = dateInventory;
			if (inventory != null) {
				lastReference = inventory.getInventoryReference();
				lastDate = inventory.getInventoryDate();
			}
			if (checkParametersChanges(lastReference, lastDate)) {
				int reset = MessageDialog.yesNoCancel(null, "angal.inventory.doyouwanttosavethechanges.msg");
				if (reset == JOptionPane.YES_OPTION) {
					this.saveButton.doClick();
					dispose();
				}
				if (reset == JOptionPane.NO_OPTION) {
					resetVariables();
					dispose();
				} else {
					return;
				}
			} else {
				resetVariables();
				dispose();
			}
		});
		return closeButton;
	}

	private String checkParamsValues(String chargeCode, String dischargeCode, Integer supplierId, String wardCode) {
		if (chargeCode == null || chargeCode.isEmpty()) {
			return "angal.inventory.choosechargetypebeforevalidation.msg";
		}
		if (dischargeCode == null || dischargeCode.isEmpty()) {
			return "angal.inventory.choosedischargetypebeforevalidation.msg";
		}
		if (supplierId == null || supplierId == 0) {
			return "angal.inventory.choosesupplierbeforevalidation.msg";
		}
		if (wardCode == null || wardCode.isEmpty()) {
			return "angal.inventory.choosedestinationbeforevalidation.msg";
		}
		return null;
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
			model = new InventoryRowModel();
			jTableInventoryRow.setModel(model);
			jTableInventoryRow.setAutoCreateColumnsFromModel(false);
			for (int i = 0; i < columnVisible.length; i++) {
				jTableInventoryRow.getColumnModel().getColumn(i).setPreferredWidth(columwidth[i]);
				if (!columnVisible[i]) {
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
			jTableInventoryRow.getSelectionModel().addListSelectionListener(listSelectionEvent -> {

				if (listSelectionEvent.getValueIsAdjusting()) {
					int selectedRow = jTableInventoryRow.getSelectedRow();
					if (selectedRow == -1) {
						lotButton.setEnabled(false);
						return;
					}
					MedicalInventoryRow medInvRow = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRow, -1);
					Lot lot = medInvRow.getLot();
					if (lot == null) {
						lotButton.setEnabled(true);
						return;
					}
					BigDecimal cost = lot.getCost() != null ? lot.getCost() : BigDecimal.ZERO;
					lotButton.setEnabled(medInvRow.isNewLot() || (isLotWithCost() && cost.compareTo(BigDecimal.ZERO) == 0));
				}
			});
			DefaultCellEditor cellEditor = new DefaultCellEditor(jTextFieldEditor);
			jTableInventoryRow.setDefaultEditor(Integer.class, cellEditor);
		}
		return jTableInventoryRow;
	}

	class InventoryRowModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public InventoryRowModel() throws OHServiceException {
			this(null, false, false, false);
		}

		public InventoryRowModel(boolean add) throws OHServiceException {
			this(null, false, add, false);
		}

		public InventoryRowModel(MedicalType medType) throws OHServiceException {
			this(medType, false, false, false);
		}

		public InventoryRowModel(boolean withNoZeroQty, MedicalType medType) throws OHServiceException {
			this(medType, withNoZeroQty, false, false);
		}

		public InventoryRowModel(MedicalType medType, boolean withMovement) throws OHServiceException {
			this(medType, false, false, withMovement);
		}

		public InventoryRowModel(MedicalType medType, boolean withNoZeroQty, boolean includeAll, boolean withMovement) throws OHServiceException {
			if (includeAll) {
				if (medType == null) {
					inventoryRowList = loadNewInventoryTable(null, inventory, true);
				} else {
					inventoryRowList = loadNewInventoryTableByMedicalType(medType);
				}
			} else if (withMovement) {
				inventoryRowList = loadNewInventoryTable(medType);
			} else if (withNoZeroQty) {
				if (medType == null) {
					inventoryRowList = loadNewInventoryTable(true, null);
				} else {
					inventoryRowList = loadNewInventoryTable(true, medType);
				}
			} else {
				inventoryRowSearchList.clear();
				if (inventory != null) {
					inventoryRowList = medicalInventoryRowManager.getMedicalInventoryRowByInventoryId(inventory.getId());
				}
			}

			if (!inventoryRowList.isEmpty()) {
				for (MedicalInventoryRow invRow : inventoryRowList) {
					addMedInRowInInventorySearchList(invRow);
					if (!includeAll && invRow.getId() == 0) {
						inventoryRowListAdded.add(invRow);
					}
				}
			} else {
				if (!mode.equals("new")) {
					MessageDialog.info(null, "angal.inventory.notdataforthatfilter.msg");
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
				Medical medical = medInvtRow.getMedical();
				Lot lot = medInvtRow.getLot();
				if (c == -1) {
					return medInvtRow;
				} else if (c == 0) {
					return medInvtRow.getId();
				} else if (c == 1) {
					return medical.getProdCode();
				} else if (c == 2) {
					return medical.getDescription();
				} else if (c == 3) {
					return lot == null || medInvtRow.isNewLot() ? "N" : "";
				} else if (c == 4) {
					return lot == null ? "" : (lot.getCode().equals("") ? "AUTO" : lot.getCode());
				} else if (c == 5) {
					if (lot != null && lot.getDueDate() != null) {
						return lot.getDueDate().format(DATE_FORMATTER);
					}
					return "";
				} else if (c == 6) {
					return medInvtRow.getTheoreticQty();
				} else if (c == 7) {
					double dblValue = medInvtRow.getRealQty();
					return (int) dblValue;
				} else if (c == 8) {
					if (lot != null && lot.getCost() != null) {
						medInvtRow.setTotal(lot.getCost().multiply(BigDecimal.valueOf(medInvtRow.getRealQty())));
						return lot.getCost();
					}
					return BigDecimal.ZERO;
				} else if (c == 9) {
					if (lot != null && lot.getCost() != null) {
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
						MessageDialog.error(null, "angal.inventory.invalidquantity.msg");
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

	private Lot getLot(Lot lotToUpdate, Medical medical) throws OHServiceException {
		Lot lot;

		if (isAutomaticLotIn()) {
			LocalDateTime preparationDate = TimeTools.getNow().truncatedTo(ChronoUnit.MINUTES);
			LocalDateTime expiringDate = askExpiringDate();
			Medical assignedMedical = (lotToUpdate != null) ? lotToUpdate.getMedical() : medical;
			if (assignedMedical == null) {
				throw new IllegalArgumentException("Fatal: Medical is required for new lots."); // should not happen!
			}
			String lotCode = (lotToUpdate != null) ? lotToUpdate.getCode() : "";
			lot = new Lot(assignedMedical, lotCode, preparationDate, expiringDate);
			BigDecimal cost = (lotToUpdate != null && lotToUpdate.getCost() != null) ? lotToUpdate.getCost() : BigDecimal.ZERO;
			if (isLotWithCost()) {
				cost = askCost(2, cost);
				if (cost.compareTo(BigDecimal.ZERO) == 0) {
					return null;
				}
			}
			lot.setCost(cost);
		} else {
			lot = askLot(lotToUpdate, medical);
		}

		return lot;
	}

	private Lot askLot(Lot lotToUpdate, Medical medical) {
		LocalDateTime preparationDate;
		LocalDateTime expiringDate;
		Lot lot = null;
		Medical assignedMedical = medical;

		JTextField lotCodeTextField = new JTextField(15);
		lotCodeTextField.addAncestorListener(new RequestFocusListener());
		TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotid"), lotCodeTextField);
		suggestion.setFont(new Font("Tahoma", Font.PLAIN, 14));
		suggestion.setForeground(Color.GRAY);
		suggestion.setHorizontalAlignment(SwingConstants.CENTER);
		suggestion.changeAlpha(0.5f);
		suggestion.changeStyle(Font.BOLD + Font.ITALIC);
		LocalDate now = LocalDate.now();
		GoodDateChooser preparationDateChooser = new GoodDateChooser(now);
		GoodDateChooser expireDateChooser = new GoodDateChooser(now);
		if (lotToUpdate != null) {
			assignedMedical = lotToUpdate.getMedical();
			lotCodeTextField.setText(lotToUpdate.getCode());
			preparationDateChooser = new GoodDateChooser(lotToUpdate.getPreparationDate().toLocalDate());
			expireDateChooser = new GoodDateChooser(lotToUpdate.getDueDate().toLocalDate());
		}
		JPanel panel = new JPanel(new GridLayout(3, 2));
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotnumberabb")));
		panel.add(lotCodeTextField);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.preparationdate")));
		panel.add(preparationDateChooser);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate")));
		panel.add(expireDateChooser);
		do {
			int ok = JOptionPane.showConfirmDialog(
				this,
				panel,
				MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotinformations"),
				JOptionPane.OK_CANCEL_OPTION);

			if (ok == JOptionPane.OK_OPTION) {
				String lotCode = lotCodeTextField.getText();
				try {
					/*
					 * check for lotCode in persisted lots and new lots in the inventoryRowSearchList avoiding null objects or lots without name yet (will be
					 * shown as "AUTO" and the lotCode will be generated)
					 */
					if (movStockInsertingManager.lotExists(lotCode) || inventoryRowSearchList.stream()
						.map(MedicalInventoryRow::getLot)
						.filter(Objects::nonNull)
						.filter(l -> !l.getCode().isEmpty())
						.anyMatch(l -> l.getCode().equals(lotCode))) {
						MessageDialog.error(this, "angal.medicalstock.multiplecharging.theinsertedlotcodealreaedyexists.msg");
						continue;
					}
					if (expireDateChooser.getDate().isBefore(preparationDateChooser.getDate())) {
						MessageDialog.error(this, "angal.medicalstock.multiplecharging.expirydatebeforepreparationdate");
					} else {
						expiringDate = expireDateChooser.getDateEndOfDay();
						preparationDate = preparationDateChooser.getDateStartOfDay();
						lot = new Lot(assignedMedical, lotCode, preparationDate, expiringDate);
						BigDecimal cost = BigDecimal.ZERO;
						if (isLotWithCost()) {
							if (lotToUpdate != null) {
								cost = askCost(2, lotToUpdate.getCost());
							} else {
								cost = askCost(2, cost);
							}

							if (cost.compareTo(BigDecimal.ZERO) == 0) {
								return null;
							} else {
								lot.setCost(cost);
							}
						}
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			} else {
				return null;
			}
		} while (lot == null);
		return lot;
	}

	private BigDecimal askCost(int qty, BigDecimal lastCost) {
		double cost = 0.;
		do {
			String input = JOptionPane.showInputDialog(this,
				MessageBundle.getMessage("angal.medicalstock.multiplecharging.unitcost"),
				lastCost);
			if (input != null) {
				try {
					cost = Double.parseDouble(input);
					if (cost < 0) {
						throw new NumberFormatException();
					} else if (cost == 0.) {
						double total = askTotalCost();
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
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate")));
		panel.add(expireDateChooser);

		int ok = JOptionPane.showConfirmDialog(this, panel,
			MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate"),
			JOptionPane.OK_CANCEL_OPTION);

		if (ok == JOptionPane.OK_OPTION) {
			date = expireDateChooser.getLocalDateTime();
		}
		return date;
	}

	protected double askTotalCost() {
		String input = JOptionPane.showInputDialog(this,
			MessageBundle.getMessage("angal.medicalstock.multiplecharging.totalcost"),
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

	private JComboBox<Ward> getWardComboBox() {
		if (wardComboBox == null) {
			wardComboBox = new JComboBox<>();
			List<Ward> wardList;
			try {
				wardList = wardManager.getWards();
			} catch (OHServiceException e) {
				wardList = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			if (!mode.equals("new")) {
				String wardId = inventory.getWard();
				for (Ward ward : wardList) {
					if (ward.getCode().equals(wardId)) {
						wardComboBox.addItem(ward);
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

	private void disableSomeComponents() {
		jCalendarInventoryDate.setEnabled(false);
		addMedicalLabel.setEnabled(false);
		medicalCodeTextField.setEnabled(false);
		selectButton.setEnabled(false);
		referenceTextField.setEnabled(false);
		jTableInventoryRow.setEnabled(false);
		saveButton.setEnabled(false);
		deleteButton.setEnabled(false);
		resetButton.setEnabled(false);
		lotButton.setEnabled(false);
	}

	private void activateSomeComponents() {
		jCalendarInventoryDate.setEnabled(true);
		addMedicalLabel.setEnabled(true);
		medicalCodeTextField.setEnabled(true);
		selectButton.setEnabled(true);
		referenceTextField.setEnabled(true);
		jTableInventoryRow.setEnabled(true);
		wardComboBox.setEnabled(false);
		saveButton.setEnabled(true);
		deleteButton.setEnabled(true);
		resetButton.setEnabled(true);
		lotButton.setEnabled(true);
	}

	private JLabel getAddMedicalLabel() {
		if (addMedicalLabel == null) {
			addMedicalLabel = new JLabel(MessageBundle.getMessage("angal.inventory.addamedical.label"));
		}
		return addMedicalLabel;
	}

	private JLabel getDateInventoryLabel() {
		if (dateInventoryLabel == null) {
			dateInventoryLabel = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
		}
		return dateInventoryLabel;
	}

	private JTextField getMedicalCodeTextField() {
		if (medicalCodeTextField == null) {
			medicalCodeTextField = new JTextField();
			medicalCodeTextField.setColumns(10);
			TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.common.code.txt"), medicalCodeTextField, Show.FOCUS_LOST);
			suggestion.setFont(new Font("Tahoma", Font.PLAIN, 12));
			suggestion.setForeground(Color.GRAY);
			suggestion.setHorizontalAlignment(SwingConstants.CENTER);
			suggestion.changeAlpha(0.5f);
			suggestion.changeStyle(Font.BOLD + Font.ITALIC);
			medicalCodeTextField.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						code = medicalCodeTextField.getText().trim();
						code = code.toLowerCase();
						try {
							addInventoryRow(code);
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1);
						}
						if (inventory != null && !inventory.getStatus().equals(InventoryStatus.draft.toString())) {
							inventory.setStatus(InventoryStatus.draft.toString());
						}
						medicalCodeTextField.setText("");
					}
				}
			});
		}
		return medicalCodeTextField;
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

	private List<MedicalInventoryRow> loadNewInventoryTableByMedicalType(MedicalType medicalType) throws OHServiceException {
		return getMedicalInventoryRowsByMedicalType(medicalType);
	}

	private List<MedicalInventoryRow> getMedicalInventoryRows(String code) throws OHServiceException {
		List<MedicalInventoryRow> inventoryRowsList = new ArrayList<>();
		List<Medical> medicalList = new ArrayList<>();
		List<Lot> lots = null;
		Medical medical = null;
		MedicalInventoryRow inventoryRowTemp = null;
		wardId = ((Ward) Objects.requireNonNull(wardComboBox.getSelectedItem())).getCode();
		Ward ward = wardManager.findWard(wardId);
		if (code != null) {
			medical = medicalBrowsingManager.getMedicalByMedicalCode(code);
			if (medical != null) {
				medicalList.add(medical);
			} else {
				medical = chooseMedical(code);
				if (medical != null) {
					medicalList.add(medical);
				}
			}
		} else {
			medicalList = medicals;
		}
		ListIterator<Medical> medicalListIterator = medicalList.listIterator();
		while (medicalListIterator.hasNext()) {
			Medical med = medicalListIterator.next();
			lots = movStockInsertingManager.getLotByMedical(med, false);
			double actualQty = 0.;
			if (lots.isEmpty()) {
				inventoryRowTemp = new MedicalInventoryRow(0, actualQty, actualQty, null, med, null);
				inventoryRowTemp.setNewLot(true); // missing parameter in the above constructor
				if (!existInInventorySearchList(inventoryRowTemp)) {
					inventoryRowsList.add(inventoryRowTemp);
				}
			} else {
				ListIterator<Lot> lotListIterator = lots.listIterator();
				while (lotListIterator.hasNext()) {
					Lot lot = lotListIterator.next();
					int lotQuantityInWard = movWardBrowserManager.getCurrentQuantityInWard(ward, lot);
					inventoryRowTemp = new MedicalInventoryRow(0, lotQuantityInWard, lotQuantityInWard, null, med, lot);
					if (!existInInventorySearchList(inventoryRowTemp)) {
						inventoryRowsList.add(inventoryRowTemp);
					}
				}
			}
		}
		return inventoryRowsList;
	}

	private List<MedicalInventoryRow> getMedicalInventoryRowsByMedicalType(MedicalType medType) throws OHServiceException {
		List<MedicalInventoryRow> inventoryRowsList = new ArrayList<>();
		String medTypeDescription = medType.getDescription();
		List<Medical> medicalList = medicalBrowsingManager.getMedicals(medTypeDescription, false);
		List<Lot> lots = null;
		MedicalInventoryRow inventoryRowTemp = null;
		ListIterator<Medical> medicalListIterator = medicalList.listIterator();
		wardId = ((Ward) Objects.requireNonNull(wardComboBox.getSelectedItem())).getCode();
		Ward ward = wardManager.findWard(wardId);
		while (medicalListIterator.hasNext()) {
			Medical med = medicalListIterator.next();
			lots = movStockInsertingManager.getLotByMedical(med, false);
			double actualQty = 0.;
			if (lots.isEmpty()) {
				inventoryRowTemp = new MedicalInventoryRow(0, actualQty, actualQty, null, med, null);
				inventoryRowTemp.setNewLot(true); // missing parameter in the above constructor
				if (!existInInventorySearchList(inventoryRowTemp)) {
					inventoryRowsList.add(inventoryRowTemp);
				}
			} else {
				ListIterator<Lot> lotListIterator = lots.listIterator();
				while (lotListIterator.hasNext()) {
					Lot lot = lotListIterator.next();
					int lotQuantityInWard = movWardBrowserManager.getCurrentQuantityInWard(ward, lot);
					inventoryRowTemp = new MedicalInventoryRow(0, lotQuantityInWard, lotQuantityInWard, null, med, lot);
					if (!existInInventorySearchList(inventoryRowTemp)) {
						inventoryRowsList.add(inventoryRowTemp);
					}
				}
			}
		}
		return inventoryRowsList;
	}

	private List<MedicalInventoryRow> getMedicalInventoryRowsWithMovement() throws OHServiceException {
		List<MedicalInventoryRow> inventoryRowsList = new ArrayList<>();
		List<Medical> medicalListWithMovement = new ArrayList<>();
		List<Lot> lots = null;
		MedicalInventoryRow inventoryRowTemp = null;
		List<Medical> medicalList = medicals;
		ListIterator<Medical> medicalListIterator = medicalList.listIterator();
		wardId = ((Ward) Objects.requireNonNull(wardComboBox.getSelectedItem())).getCode();
		Ward ward = wardManager.findWard(wardId);
		while (medicalListIterator.hasNext()) {
			Medical med = medicalListIterator.next();
			Integer medicalCode = med.getCode();
			List<Movement> movementsFromMainStore = movBrowserManager.getMovements(medicalCode, null, wardId, null, null, null, null, null, null,
				null);
			if (!movementsFromMainStore.isEmpty()) {
				medicalListWithMovement.add(med);
			}
		}
		List<MovementWard> movementsWard = movWardBrowserManager.getMovementWard(wardId, null, null);
		medicalListWithMovement.addAll(movementsWard.stream()
			.filter(movement -> medicalList.contains(movement.getMedical())).map(MovementWard::getMedical)
			.distinct()
			.toList());

		// Remove duplicates by converting the list to a set
		Set<Medical> uniqueMedicals = new HashSet<>(medicalListWithMovement);
		// Convert the set back to a list
		List<Medical> uniqueMedicalsList = new ArrayList<>(uniqueMedicals);

		medicalListIterator = uniqueMedicalsList.listIterator();
		while (medicalListIterator.hasNext()) {
			Medical med = medicalListIterator.next();
			lots = movStockInsertingManager.getLotByMedical(med, false);
			double actualQty = 0.;
			if (lots.isEmpty()) {
				inventoryRowTemp = new MedicalInventoryRow(0, actualQty, actualQty, null, med, null);
				inventoryRowTemp.setNewLot(true); // missing parameter in the above constructor
				if (!existInInventorySearchList(inventoryRowTemp)) {
					inventoryRowsList.add(inventoryRowTemp);
				}
			} else {
				ListIterator<Lot> lotListIterator = lots.listIterator();
				while (lotListIterator.hasNext()) {
					Lot lot = lotListIterator.next();
					int lotQuantityInWard = movWardBrowserManager.getCurrentQuantityInWard(ward, lot);
					inventoryRowTemp = new MedicalInventoryRow(0, lotQuantityInWard, lotQuantityInWard, null, med, lot);
					if (!existInInventorySearchList(inventoryRowTemp)) {
						inventoryRowsList.add(inventoryRowTemp);
					}
				}
			}
		}
		return inventoryRowsList;
	}

	private void addInventoryRow(String code) throws OHServiceException {
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
				medical = chooseMedical(code);
				if (medical != null) {
					medicalList.add(medical);
				}
			}
		} else {
			medicalList = medicals;
		}
		int numberOfMedicalWithoutSameLotAdded = 0;
		Medical medicalWithLot = null;
		ListIterator<Medical> medicalListIterator = medicalList.listIterator();
		while (medicalListIterator.hasNext()) {
			Medical med = medicalListIterator.next();
			lots = movStockInsertingManager.getLotByMedical(med, false);
			if (lots.isEmpty()) {
				inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, med, null);
				inventoryRowTemp.setNewLot(true); // missing parameter in the above constructor
				if (!existInInventorySearchList(inventoryRowTemp)) {
					inventoryRowsList.add(inventoryRowTemp);
				} else {
					int info = MessageDialog.yesNo(null, "angal.inventory.productalreadyexist.fmt.msg", med.getDescription());
					if (info == JOptionPane.YES_OPTION) {
						inventoryRowsList.add(inventoryRowTemp);
					}
				}
			} else {
				medicalWithLot = med;
				ListIterator<Lot> lotListIterator = lots.listIterator();
				while (lotListIterator.hasNext()) {
					Lot lot = lotListIterator.next();
					inventoryRowTemp = new MedicalInventoryRow(0, lot.getMainStoreQuantity(), lot.getMainStoreQuantity(), null, med, lot);
					if (!existInInventorySearchList(inventoryRowTemp)) {
						inventoryRowsList.add(inventoryRowTemp);
						numberOfMedicalWithoutSameLotAdded = numberOfMedicalWithoutSameLotAdded + 1;
					}
				}
			}
		}
		if (medicalWithLot != null && numberOfMedicalWithoutSameLotAdded == 0) {
			int info = MessageDialog.yesNo(null, "angal.inventory.productalreadyexist.fmt.msg", medicalWithLot.getDescription());
			if (info == JOptionPane.YES_OPTION) {
				inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, medicalWithLot, null);
				inventoryRowTemp.setNewLot(true); // missing parameter in the above constructor
				inventoryRowsList.add(inventoryRowTemp);
			}
		}
		for (MedicalInventoryRow inventoryRow : inventoryRowsList) {
			addMedInRowInInventorySearchList(inventoryRow);
		}
		jTableInventoryRow.updateUI();
	}

	private Medical chooseMedical(String text) throws OHServiceException {
		Map<String, Medical> medicalMap = new HashMap<>();
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
			referenceTextField = new VoLimitedTextField(40, 10); // limit to 40 because of potential suffixes
			if (inventory != null && !mode.equals("new")) {
				referenceTextField.setText(inventory.getInventoryReference());
				newReference = inventory.getInventoryReference();
			}
		}
		return referenceTextField;
	}

	private JLabel getStatusLabel() {
		if (statusLabel == null) {
			if (inventory == null) {
				String currentStatus = InventoryStatus.draft.toString();
				String status = medicalInventoryManager.getStatusByKey(currentStatus);
				statusLabel = new JLabel(status.toUpperCase());
				statusLabel.setForeground(Color.GRAY);
			} else {
				String currentStatus = inventory.getStatus();
				String status = medicalInventoryManager.getStatusByKey(currentStatus);
				statusLabel = new JLabel(status.toUpperCase());
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

	private JLabel getAddMedicalsByLabel() {
		if (addMedicalsByLabel == null) {
			addMedicalsByLabel = new JLabel(MessageBundle.getMessage("angal.inventory.addmedicalsby.label"));
		}
		return addMedicalsByLabel;
	}

	private int getPosition(MedicalInventoryRow inventoryRow) {
		int position = -1;
		int i = 0;
		for (MedicalInventoryRow invR : inventoryRowSearchList) {
			if (invR.getMedical().getCode().equals(inventoryRow.getMedical().getCode())) {
				position = i;
			}
			i = i + 1;
		}
		return position;
	}

	private boolean existInInventorySearchList(MedicalInventoryRow inventoryRow) {
		boolean found = false;
		List<MedicalInventoryRow> invRows = inventoryRowSearchList.stream()
			.filter(inv -> inv.getMedical().getCode().equals(inventoryRow.getMedical().getCode())).collect(Collectors.toList());
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

	private void resetVariables() {
		inventoryRowsToDelete.clear();
		lotsDeleted.clear();
		inventoryRowListAdded.clear();
		lotsSaved.clear();
	}

	private boolean checkParametersChanges(String reference, LocalDateTime date) {
		return !lotsSaved.isEmpty() || !inventoryRowListAdded.isEmpty() || !lotsDeleted.isEmpty() || !inventoryRowsToDelete.isEmpty()
			|| (reference != null && !reference.equals(newReference)) || !isSameDateTime(date, dateInventory);
	}

	private boolean isSameDateTime(LocalDateTime date1, LocalDateTime date2) {
		return TimeTools.isSameDateTime(date1, date2);
	}

	private JComboBox<MedicalType> getJComboMedicalType() {
		if (medicalTypeComboBox == null) {
			medicalTypeComboBox = new JComboBox<>();
			try {
				List<MedicalType> medicalTypes = medicalTypeManager.getMedicalType();
				MedicalType medicalType = new MedicalType(MessageBundle.getMessage("angal.common.all.txt"), MessageBundle.getMessage("angal.common.all.txt"));
				medicalTypeComboBox.addItem(medicalType);
				for (MedicalType medType : medicalTypes) {
					medicalTypeComboBox.addItem(medType);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			medicalTypeSelected = (MedicalType) medicalTypeComboBox.getSelectedItem();
			medicalTypeComboBox.addActionListener(actionEvent -> {
				medicalTypeSelected = (MedicalType) medicalTypeComboBox.getSelectedItem();
			});
		}
		return medicalTypeComboBox;
	}

	private JRadioButton getAllRadioButton() {
		if (radioButtonAll == null) {
			radioButtonAll = new JRadioButton(MessageBundle.getMessage("angal.common.all.btn"));
			radioButtonAll.setSelected(true);
		}
		return radioButtonAll;
	}

	private JRadioButton getMedicalWithNonZeroQuantityRadioButton() {
		if (radioOnlyNonZero == null) {
			radioOnlyNonZero = new JRadioButton(MessageBundle.getMessage("angal.inventory.medicalwithonlynonzeroqty.btn"));
		}
		return radioOnlyNonZero;
	}

	private JRadioButton getMedicalWithMovementRadioButton() {
		if (radioWithMovement == null) {
			radioWithMovement = new JRadioButton(MessageBundle.getMessage("angal.inventory.medicalwithmovementonly.btn"));
		}
		return radioWithMovement;
	}

	private JButton getCancelButton() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		}
		jButtonCancel.addActionListener(actionEvent -> frame.dispose());
		return jButtonCancel;
	}

	private List<MedicalInventoryRow> loadNewInventoryTable(boolean withNonZeroQty, MedicalType medicalTypeSelected) throws OHServiceException {
		List<MedicalInventoryRow> inventoryRowsList = getMedicalInventoryRows(null);
		if (withNonZeroQty) {
			inventoryRowsList = inventoryRowsList.stream().filter(inv -> inv.getTheoreticQty() > 0).collect(Collectors.toList());
		}
		if (medicalTypeSelected != null) {
			inventoryRowsList = inventoryRowsList.stream()
				.filter(inv -> inv.getMedical().getType().getDescription().equals(medicalTypeSelected.getDescription()))
				.collect(Collectors.toList());
		}
		return inventoryRowsList;
	}

	private List<MedicalInventoryRow> loadNewInventoryTable(MedicalType medicalTypeSelected) throws OHServiceException {
		List<MedicalInventoryRow> inventoryRowsList = getMedicalInventoryRowsWithMovement();
		if (medicalTypeSelected != null) {
			inventoryRowsList = inventoryRowsList.stream()
				.filter(inv -> inv.getMedical().getType().getDescription().equals(medicalTypeSelected.getDescription()))
				.collect(Collectors.toList());
		}
		return inventoryRowsList;
	}

	private JButton getOkButton() {
		if (jButtonOk == null) {
			jButtonOk = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			jButtonOk.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			jButtonOk.addActionListener(actionEvent -> {
				actions.forEach((key, value) -> {
					if (key.isSelected()) {
						actions.get(key).run();
					}
				});
				jButtonCancel.doClick();
			});
		}
		return jButtonOk;
	}

	private boolean areAllMedicalsInInventory() throws OHServiceException {
		Set<Medical> inventorySet = new HashSet<>();
		for (MedicalInventoryRow row : inventoryRowSearchList) {
			inventorySet.add(row.getMedical());
		}
		return medicals.size() == inventorySet.size() ? true : false;
	}

	private void initializeActions() {
		actions.put(radioButtonAll, () -> handleInventoryUpdate(true, false, false));
		actions.put(radioOnlyNonZero, () -> handleInventoryUpdate(false, false, true));
		actions.put(radioWithMovement, () -> handleInventoryUpdate(false, true, false));
	}

	private void handleInventoryUpdate(boolean includeAll, boolean withMovement, boolean nonZero) {
		boolean isAllSelected = medicalTypeSelected.getDescription().equals(MessageBundle.getMessage("angal.common.all.txt"));
		MedicalType medType = isAllSelected ? null : (MedicalType) medicalTypeComboBox.getSelectedItem();

		try {
			if (!areAllMedicalsInInventory()) {
				int userChoice = (!inventoryRowSearchList.isEmpty())
					? MessageDialog.yesNo(null, "angal.inventory.doyouwanttoaddallnotyetlistedproducts.msg")
					: JOptionPane.YES_OPTION;

				if (userChoice == JOptionPane.YES_OPTION) {
					jTableInventoryRow.setModel(new InventoryRowModel(medType, nonZero, includeAll, withMovement));
					fireInventoryUpdated();
					showUpdateSuccessMessage();
				}
			} else {
				MessageDialog.info(null, "angal.inventory.youhavealreadyaddedallproduct.msg");
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	private void showUpdateSuccessMessage() {
		if (!inventoryRowList.isEmpty()) {
			MessageDialog.info(null, "angal.inventory.tablehasbeenupdated.msg");
		} else {
			MessageDialog.info(null, "angal.inventory.notdataforthatfilter.msg");
		}
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
