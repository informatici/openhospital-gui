/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import javax.swing.event.TableModelEvent;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

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
import org.isf.medicalstockward.manager.MovWardBrowserManager;
import org.isf.medicalstockward.model.MedicalWard;
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

public class InventoryWardEdit extends ModalJFrame {

    private static final long serialVersionUID = 1L;

    private static EventListenerList InventoryListeners = new EventListenerList();

    public interface InventoryListener extends EventListener {

        void InventoryInserted(AWTEvent e);

        void InventoryUpdated(AWTEvent e);

        void InventoryCancelled(AWTEvent e);
    }

    public static void addInventoryListener(InventoryListener l) {
        InventoryListeners.add(InventoryListener.class, l);
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
    private LocalDateTime dateInventory = TimeTools.getServerDateTime();
    private JPanel panelHeader;
    private JPanel panelFooter;
    private JPanel panelContent;
    private JButton closeButton;
    private JButton saveButton;
    private JButton resetButton;
    private JButton printButton;
    private JButton validateButton;
    private JButton deleteButton;
    private JButton lotButton;
    private JScrollPane scrollPaneInventory;
    private JTable jTableInventoryRow;
    private List<MedicalInventoryRow> inventoryRowList = new ArrayList<>();
    private List<MedicalInventoryRow> inventoryRowSearchList = new ArrayList<>();
    private List<Lot> lotsSaved = new ArrayList<>();
    private HashMap<Integer, Lot> lotsDeleted = new HashMap<>();
    private List<MedicalInventoryRow> inventoryRowsToDelete = new ArrayList<>();
    private List<MedicalInventoryRow> inventoryRowListAdded = new ArrayList<>();
    private String[] pColums = { MessageBundle.getMessage("angal.inventory.id.col").toUpperCase(),
		    MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.medical.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.new.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.lotcode.col").toUpperCase(),
            MessageBundle.getMessage("angal.medicalstock.duedate.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.theorticalqty.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.realqty.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.unitprice.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.totalprice.col").toUpperCase()
    };
	private int[] pColumwidth = { 50, 50, 200, 100, 100, 100, 100, 80, 80, 80 };
	private boolean[] columnEditable = { false, false, false, false, false, false, false, true, false, false };
	private boolean[] columnEditableView = { false, false, false, false, false, false, false, false, false, false };
	private boolean[] pColumnVisible = { false, true, true, false, !GeneralData.AUTOMATICLOT_IN, true, true, true, GeneralData.LOTWITHCOST,
			GeneralData.LOTWITHCOST };
    private MedicalInventory inventory = null;
    private JRadioButton specificRadio;
    private JRadioButton allRadio;
    private JLabel dateInventoryLabel;
    private JTextField codeTextField;
    private String code = null;
    private String mode;
    private String wardId = "";
    private JLabel referenceLabel;
    private JTextField referenceTextField;
    private JTextField jTetFieldEditor;
    private JLabel wardLabel;
    private JLabel chargeLabel;
    private JLabel dischargeLabel;
    private JLabel supplierLabel;
    private JLabel destinationLabel;
    private JComboBox<Ward> wardComboBox;
    private JComboBox<MovementType> chargeComboBox;
    private JComboBox<MovementType> dischargeComboBox;
    private JComboBox<Supplier> supplierComboBox;
    private JComboBox<Ward> destinationComboBox;
    private Ward wardSelected;
    private JLabel loaderLabel;
    private boolean selectAll = false;
    private String newReference = null;
    private MovementType chargeType = null;
    private MovementType dischargeType = null;
    private Supplier supplier = null;
    private Ward destination = null;
    private WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
    private MedicalInventoryManager medicalInventoryManager = Context.getApplicationContext()
            .getBean(MedicalInventoryManager.class);
    private MedicalInventoryRowManager medicalInventoryRowManager = Context.getApplicationContext()
            .getBean(MedicalInventoryRowManager.class);
    private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext()
            .getBean(MedicalBrowsingManager.class);
    private MovWardBrowserManager movWardBrowserManager = Context.getApplicationContext()
            .getBean(MovWardBrowserManager.class);
    private MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext()
            .getBean(MovStockInsertingManager.class);
    private MedicalDsrStockMovementTypeBrowserManager medStockMovTypeManager = Context.getApplicationContext()
            .getBean(MedicalDsrStockMovementTypeBrowserManager.class);
    private SupplierBrowserManager supplierBrowserManager = Context.getApplicationContext()
            .getBean(SupplierBrowserManager.class);

    public boolean isAutomaticLotIn() {
        return GeneralData.AUTOMATICLOT_IN;
    }

    public InventoryWardEdit() {
        mode = "new";
        initComponents();
        resetButton.setVisible(false);
        disabledSomeComponents();
    }

    public InventoryWardEdit(MedicalInventory inventory, String modee) {
        this.inventory = inventory;
        mode = modee;
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new DimensionUIResource(950, 580));
        setLocationRelativeTo(null); // center
        if (mode.equals("update")) {
            setTitle(MessageBundle.getMessage("angal.inventory.edit.title"));
        } else if (mode.equals("view")) {
            setTitle(MessageBundle.getMessage("angal.inventory.view.title"));
        } else {
            setTitle(MessageBundle.getMessage("angal.inventory.new.title"));
        }

        panelHeader = getPanelHeader();
        getContentPane().add(panelHeader, BorderLayout.NORTH);

        panelContent = getPanelContent();
        getContentPane().add(panelContent, BorderLayout.CENTER);

        panelFooter = getPanelFooter();
        getContentPane().add(panelFooter, BorderLayout.SOUTH);

		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeButton.doClick();
			}
		});

		if (mode.equals("view")) {
			saveButton.setVisible(false);
			deleteButton.setVisible(false);
			columnEditable = columnEditableView;
			codeTextField.setEditable(false);
            resetButton.setVisible(false);
            referenceTextField.setVisible(false);
            jCalendarInventory.setEnabled(false);
            specificRadio.setEnabled(false);
            allRadio.setEnabled(false);
            chargeComboBox.setEnabled(false);
            dischargeComboBox.setEnabled(false);
            supplierComboBox.setEnabled(false);
            destinationComboBox.setEnabled(false);
            wardComboBox.setEnabled(false);
            lotButton.setVisible(false);
		} else {
            saveButton.setVisible(true);
            deleteButton.setVisible(true);
            codeTextField.setEditable(true);
            resetButton.setVisible(true);
            referenceTextField.setEditable(true);
            jCalendarInventory.setEnabled(true);
            specificRadio.setEnabled(true);
            allRadio.setEnabled(true);
            chargeComboBox.setEnabled(true);
            dischargeComboBox.setEnabled(true);
            supplierComboBox.setEnabled(true);
            destinationComboBox.setEnabled(true);
            lotButton.setVisible(false);
        }
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
            GridBagConstraints gbc_loaderLabel = new GridBagConstraints();
            gbc_loaderLabel.insets = new Insets(0, 0, 5, 5);
            gbc_loaderLabel.gridx = 2;
            gbc_loaderLabel.gridy = 0;
            panelHeader.add(getLoaderLabel(), gbc_loaderLabel);
            GridBagConstraints gbc_dateInventoryLabel = new GridBagConstraints();
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

            GridBagConstraints gbc_chargeLabel = new GridBagConstraints();
            gbc_chargeLabel.insets = new Insets(0, 0, 3, 3);
            gbc_chargeLabel.gridx = 0;
            gbc_chargeLabel.gridy = 2;
            panelHeader.add(getChargeLabel(), gbc_chargeLabel);
            GridBagConstraints gbc_chargeComboBox = new GridBagConstraints();
            gbc_chargeComboBox.fill = GridBagConstraints.HORIZONTAL;
            gbc_chargeComboBox.insets = new Insets(0, 0, 3, 3);
            gbc_chargeComboBox.gridx = 1;
            gbc_chargeComboBox.gridy = 2;
            panelHeader.add(getChargeComboBox(), gbc_chargeComboBox);
            GridBagConstraints gbc_supplierLabel = new GridBagConstraints();
            gbc_supplierLabel.anchor = GridBagConstraints.EAST;
            gbc_supplierLabel.insets = new Insets(0, 0, 3, 3);
            gbc_supplierLabel.gridx = 2;
            gbc_supplierLabel.gridy = 2;
            panelHeader.add(getSupplierLabel(), gbc_supplierLabel);
            GridBagConstraints gbc_supplierComboBox = new GridBagConstraints();
            gbc_supplierComboBox.fill = GridBagConstraints.HORIZONTAL;
            gbc_supplierComboBox.insets = new Insets(0, 0, 3, 3);
            gbc_supplierComboBox.gridx = 3;
            gbc_supplierComboBox.gridy = 2;
            panelHeader.add(getSupplierComboBox(), gbc_supplierComboBox);

            GridBagConstraints gbc_dischargeLabel = new GridBagConstraints();
            gbc_dischargeLabel.insets = new Insets(0, 0, 3, 3);
            gbc_dischargeLabel.gridx = 0;
            gbc_dischargeLabel.gridy = 3;
            panelHeader.add(getDischargeLabel(), gbc_dischargeLabel);
            GridBagConstraints gbc_dischargeComboBox = new GridBagConstraints();
            gbc_dischargeComboBox.fill = GridBagConstraints.HORIZONTAL;
            gbc_dischargeComboBox.insets = new Insets(0, 0, 3, 3);
            gbc_dischargeComboBox.gridx = 1;
            gbc_dischargeComboBox.gridy = 3;
            panelHeader.add(getDischargeComboBox(), gbc_dischargeComboBox);
            GridBagConstraints gbc_destinationLabel = new GridBagConstraints();
            gbc_destinationLabel.anchor = GridBagConstraints.EAST;
            gbc_destinationLabel.insets = new Insets(0, 0, 3, 3);
            gbc_destinationLabel.gridx = 2;
            gbc_destinationLabel.gridy = 3;
            panelHeader.add(getDestinationLabel(), gbc_destinationLabel);
            GridBagConstraints gbc_destinationComboBox = new GridBagConstraints();
            gbc_destinationComboBox.fill = GridBagConstraints.HORIZONTAL;
            gbc_destinationComboBox.insets = new Insets(0, 0, 3, 3);
            gbc_destinationComboBox.gridx = 3;
            gbc_destinationComboBox.gridy = 3;
            panelHeader.add(getDestinationComboBox(), gbc_destinationComboBox);

            GridBagConstraints gbc_specificRadio = new GridBagConstraints();
            gbc_specificRadio.anchor = GridBagConstraints.EAST;
            gbc_specificRadio.insets = new Insets(0, 0, 0, 5);
            gbc_specificRadio.gridx = 0;
            gbc_specificRadio.gridy = 4;
            panelHeader.add(getSpecificRadio(), gbc_specificRadio);
            GridBagConstraints gbc_codeTextField = new GridBagConstraints();
            gbc_codeTextField.insets = new Insets(0, 0, 0, 5);
            gbc_codeTextField.fill = GridBagConstraints.HORIZONTAL;
            gbc_codeTextField.gridx = 1;
            gbc_codeTextField.gridy = 4;
            panelHeader.add(getCodeTextField(), gbc_codeTextField);
            GridBagConstraints gbc_allRadio = new GridBagConstraints();
            gbc_allRadio.anchor = GridBagConstraints.EAST;
            gbc_allRadio.insets = new Insets(0, 0, 0, 5);
            gbc_allRadio.gridx = 2;
            gbc_allRadio.gridy = 4;
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
            panelFooter.add(getSaveButton());
            panelFooter.add(getDeleteButton());
            panelFooter.add(getLotButton());
            panelFooter.add(getResetButton());
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
            jCalendarInventory.addDateChangeListener(event -> dateInventory = jCalendarInventory.getDate().atStartOfDay());
        }
        return jCalendarInventory;
    }

    private JButton getSaveButton() {
        saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
        saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
        saveButton.addActionListener(actionEvent -> {
            String state = InventoryStatus.draft.toString();
            String user = UserBrowsingManager.getCurrentUser();
            if (inventoryRowSearchList == null || inventoryRowSearchList.isEmpty()) {
                MessageDialog.error(null, "angal.inventory.cannotsaveinventorywithoutproducts.msg");
                return;
            }
            try {
                if (!lotsDeleted.isEmpty() || !inventoryRowsToDelete.isEmpty()) {
                    for (Map.Entry<Integer, Lot> entry: lotsDeleted.entrySet()) {
                        MedicalInventoryRow invRow;
                        invRow = medicalInventoryRowManager.getMedicalInventoryRowById(entry.getKey());
                        if (invRow != null) {
                            invRow.setLot(null);
                            medicalInventoryRowManager.updateMedicalInventoryRow(invRow);
                            movStockInsertingManager.deleteLot(entry.getValue());
                        }
                    }
                    medicalInventoryRowManager.deleteMedicalInventoryRows(inventoryRowsToDelete);
                }
                if ((inventory == null) && (mode.equals("new"))) {
                    newReference = referenceTextField.getText().trim();
                    boolean refExist;
                    refExist = medicalInventoryManager.referenceExists(newReference);
                    if (refExist) {
                        MessageDialog.error(null, "angal.inventory.referencealreadyused.msg");
                        return;
                    }
                    inventory = new MedicalInventory();
                    inventory.setInventoryReference(newReference);
                    inventory.setInventoryDate(dateInventory);
                    inventory.setStatus(state);
                    inventory.setUser(user);
                    inventory.setInventoryType(InventoryType.ward.toString());
                    inventory.setChargeType(chargeType != null ? chargeType.getCode() : null);
                    inventory.setDischargeType(dischargeType != null ? dischargeType.getCode() : null);
                    inventory.setSupplier(supplier != null ? supplier.getSupId() : null);
                    inventory.setDestination(destination != null ? destination.getCode() : null);
                    inventory.setWard(wardSelected != null ? wardSelected.getCode() : null);
                    inventory = medicalInventoryManager.newMedicalInventory(inventory);
                    for (MedicalInventoryRow medicalInventoryRow : inventoryRowSearchList) {
                        medicalInventoryRow.setInventory(inventory);
                        Lot lot = medicalInventoryRow.getLot();
                        String lotCode;
                        Medical medical = medicalInventoryRow.getMedical();
                        if (lot != null) {
                            lotCode = lot.getCode();
                            Lot lotExist;
                            lotExist = movStockInsertingManager.getLot(lotCode);
                            if (lotExist != null) {
                                Lot lotStore;
                                lotStore = movStockInsertingManager.updateLot(lot);
                                medicalInventoryRow.setLot(lotStore);
                            } else {
                                if (lot.getDueDate() != null) {
                                    Lot lotStore;
                                    lotStore = movStockInsertingManager.storeLot(lotCode, lot, medical);
                                    medicalInventoryRow.setLot(lotStore);
                                    medicalInventoryRow.setNewLot(true);
                                } else {
                                    medicalInventoryRow.setLot(null);
                                }
                            }
                        } else {
                            medicalInventoryRow.setLot(null);
                        }
                        medicalInventoryRowManager.newMedicalInventoryRow(medicalInventoryRow);
                    }
                    mode = "update";
                    MessageDialog.info(this, "angal.inventory.savesuccess.msg");
                    fireInventoryInserted();
                    resetVariable();
                    int info = MessageDialog.yesNo(null, "angal.inventoryrow.doyouwanttocontinueediting.msg");
                    if (info != JOptionPane.YES_OPTION) {
                        dispose();
                    }
                } else if ((inventory != null) && (mode.equals("update"))) {
                    String lastCharge = inventory.getChargeType();
                    String lastDischarge = inventory.getDischargeType();
                    Integer lastSupplier = inventory.getSupplier();
                    String lastDestination = inventory.getDestination();
                    String lastReference = inventory.getInventoryReference();
                    newReference = referenceTextField.getText().trim();
                    MedicalInventory existingInventory =  medicalInventoryManager.getInventoryByReference(newReference);
                    if (existingInventory != null && !Objects.equals(existingInventory.getId(), inventory.getId())) {
                        MessageDialog.error(null, "angal.inventory.referencealreadyused.msg");
                        return;
                    }
                    if (inventoryRowListAdded.isEmpty() && lotsSaved.isEmpty() && lotsDeleted.isEmpty()) {
                        if ((destination != null && !destination.getCode().equals(lastDestination))
                                || (chargeType != null && !chargeType.getCode().equals(lastCharge))
                                || (dischargeType != null && !dischargeType.getCode().equals(lastDischarge))
                                || (supplier != null && !supplier.getSupId().equals(lastSupplier)) || (destination == null && lastDestination != null)
                                || (chargeType == null && lastCharge != null) || (dischargeType == null && lastDischarge != null)
                                || (supplier == null && lastSupplier != null) || !lastReference.equals(newReference)
                        ) {
                            if (!inventory.getInventoryDate().equals(dateInventory)) {
                                inventory.setInventoryDate(dateInventory);
                            }
                            if (!inventory.getUser().equals(user)) {
                                inventory.setUser(user);
                            }
                            if (!lastReference.equals(newReference)) {
                                inventory.setInventoryReference(newReference);
                            }
                            MovementType charge = (MovementType) chargeComboBox.getSelectedItem();
                            inventory.setChargeType(charge != null ? charge.getCode() : null);

                            MovementType discharge = (MovementType) dischargeComboBox.getSelectedItem();
                            inventory.setDischargeType(discharge != null ? discharge.getCode() : null);

                            Supplier supplier1 = (Supplier) supplierComboBox.getSelectedItem();
                            inventory.setSupplier(supplier1 != null ? supplier1.getSupId() : null);

                            Ward destination = (Ward) destinationComboBox.getSelectedItem();
                            inventory.setDestination(destination != null ? destination.getCode() : null);

                            inventory = medicalInventoryManager.updateMedicalInventory(inventory);
                            if (inventory != null) {
                                MessageDialog.info(null, "angal.inventory.update.success.msg");
                                resetVariable();
                                fireInventoryUpdated();
                                int info = MessageDialog.yesNo(null, "angal.inventoryrow.doyouwanttocontinueediting.msg");
                                if (info != JOptionPane.YES_OPTION) {
                                    dispose();
                                }
                            } else {
                                MessageDialog.error(null, "angal.inventory.update.error.msg");
                                return;
                            }

                        } else {
                            if (!inventoryRowsToDelete.isEmpty()) {
                                MessageDialog.info(null, "angal.inventory.update.success.msg");
                                resetVariable();
                                fireInventoryUpdated();
                                int info = MessageDialog.yesNo(null, "angal.inventoryrow.doyouwanttocontinueediting.msg");
                                if (info != JOptionPane.YES_OPTION) {
                                    dispose();
                                }
                            } else {
                                MessageDialog.info(null, "angal.inventory.inventoryisalreadysaved.msg");
                                return;
                            }
                        }
                        return;
                    }
                    if (!inventory.getInventoryDate().equals(dateInventory)) {
                        inventory.setInventoryDate(dateInventory);
                    }
                    if (!inventory.getUser().equals(user)) {
                        inventory.setUser(user);
                    }
                    if (!lastReference.equals(newReference)) {
                        inventory.setInventoryReference(newReference);
                    }

                    MovementType charge = (MovementType) chargeComboBox.getSelectedItem();
                    inventory.setChargeType(charge != null ? charge.getCode() : null);

                    MovementType discharge = (MovementType) dischargeComboBox.getSelectedItem();
                    inventory.setDischargeType(discharge != null ? discharge.getCode() : null);

                    Supplier supplier1 = (Supplier) supplierComboBox.getSelectedItem();
                    inventory.setSupplier(supplier1 != null ? supplier1.getSupId() : null);

                    Ward destination = (Ward) destinationComboBox.getSelectedItem();
                    inventory.setDestination(destination != null ? destination.getCode() : null);

                    inventory = medicalInventoryManager.updateMedicalInventory(inventory);

	                for (MedicalInventoryRow medicalInventoryRow : inventoryRowSearchList) {
		                Medical medical = medicalInventoryRow.getMedical();
		                Lot lot = medicalInventoryRow.getLot();
                        String lotCode;
                        medicalInventoryRow.setInventory(inventory);
                        int id = medicalInventoryRow.getId();
                        if (id == 0) {
                            if (lot != null) {
                                lotCode = lot.getCode();
                                boolean isExist;
                                Lot lotExist = movStockInsertingManager.getLot(lotCode);
                                isExist = (lotExist != null);
                                if (!isExist) {
                                    if (lot.getDueDate() != null) {
                                        Lot lotStore = movStockInsertingManager.storeLot(lotCode, lot, medical);
                                        medicalInventoryRow.setLot(lotStore);
                                        medicalInventoryRow.setNewLot(true);
                                    } else {
                                        Lot lotStore = movStockInsertingManager.updateLot(lot);
                                        medicalInventoryRow.setLot(lotStore);
                                    }
                                } else {
                                    Lot lotStore = movStockInsertingManager.updateLot(lot);
                                    medicalInventoryRow.setLot(lotStore);
                                }
                            }
                            medicalInventoryRowManager.newMedicalInventoryRow(medicalInventoryRow);
                        } else {
                            lot = medicalInventoryRow.getLot();
                            double reatQty = medicalInventoryRow.getRealQty();
                            medicalInventoryRow = medicalInventoryRowManager.getMedicalInventoryRowById(id);
                            medicalInventoryRow.setRealqty(reatQty);

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
                            if (medicalInventoryRow.getId() == 0) {
                                medicalInventoryRowManager.newMedicalInventoryRow(medicalInventoryRow);
                            } else {
                                medicalInventoryRowManager.updateMedicalInventoryRow(medicalInventoryRow);
                            }
                        }
	                }
                    MessageDialog.info(null, "angal.inventory.update.success.msg");
                    resetVariable();
                    fireInventoryUpdated();
                    int info = MessageDialog.yesNo(null, "angal.inventoryrow.doyouwanttocontinueediting.msg");
                    if (info != JOptionPane.YES_OPTION) {
                        dispose();
                    }
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
        validateButton.setEnabled(false);
        return validateButton;
    }

    private JButton getLotButton() {
        lotButton = new JButton(MessageBundle.getMessage("angal.inventory.lot.btn"));
        lotButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.lot.btn.key"));
        lotButton.addActionListener(actionEvent -> {
            int selectedRow = jTableInventoryRow.getSelectedRow();
            if (selectedRow == -1) {
                MessageDialog.error(this, "angal.inventoryrow.pleaseselectoneinventoryrow.msg");
                return;
            }
            MedicalInventoryRow selectedInventoryRow = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRow, -1);
            Lot lotToUpdate = selectedInventoryRow.getLot() != null ? selectedInventoryRow.getLot() : null;
            Lot lot;
            try {
                lot = this.getLot(lotToUpdate);
                String lotCode = lotToUpdate != null ? lotToUpdate.getCode() : "";
                if (lot != null && !lot.getCode().equals(lotCode)) {
                    Lot lotDelete = movStockInsertingManager.getLot(lotCode);
                    if (lotDelete != null) {
                        lotsDeleted.put(selectedInventoryRow.getId(), lotDelete);
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
                        MessageDialog.error(this, "angal.inventoryrow.thislotcodealreadyexists.msg");
                        lotButton.doClick();
                    }
                } else {
                    List<MedicalInventoryRow> invRows = inventoryRowSearchList.stream()
                            .filter(inv -> inv.getMedical().getCode().equals(selectedInventoryRow.getMedical().getCode())).toList();
                    if (invRows.isEmpty() || code.isEmpty()) {
                        selectedInventoryRow.setNewLot(true);
                        selectedInventoryRow.setLot(lot);
                        lotsSaved.add(lot);
                    } else {
                        MessageDialog.error(this, "angal.inventoryrow.thislotcodealreadyexists.msg");
                        lotButton.doClick();
                    }
                }
                inventoryRowSearchList.set(selectedRow, selectedInventoryRow);
                jTableInventoryRow.updateUI();
            }
        });
        lotButton.setEnabled(false);
        return lotButton;
    }

    private JButton getResetButton() {
        resetButton = new JButton(MessageBundle.getMessage("angal.inventory.clean.btn"));
        resetButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.clean.btn.key"));
        resetButton.addActionListener(actionEvent -> {
            int reset = MessageDialog.yesNo(null, "angal.inventoryrow.doyoureallywanttocleanthistable.msg");
            if (reset == JOptionPane.YES_OPTION) {
                if (inventory != null) {
                    for (MedicalInventoryRow invRow : inventoryRowSearchList) {
                        if (invRow.getId() != 0) {
                            inventoryRowsToDelete.add(invRow);
                        }
                    }
                }
                selectAll = false;
                specificRadio.setSelected(true);
                codeTextField.setEnabled(true);
                inventoryRowSearchList.clear();
                fireInventoryUpdated();
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
                if (inventory == null) {
                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        MedicalInventoryRow selectedInventoryRow = (MedicalInventoryRow) jTableInventoryRow
                                .getValueAt(selectedRows[i], -1);
                        inventoryRowSearchList.remove(selectedInventoryRow);
                    }
                } else {
                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        MedicalInventoryRow inventoryRow = (MedicalInventoryRow) jTableInventoryRow
                                .getValueAt(selectedRows[i], -1);
                        inventoryRowSearchList.remove(inventoryRow);
                        if (inventoryRow.getId() != 0) {
                            inventoryRowsToDelete.add(inventoryRow);
                        }
                    }
                }
                jTableInventoryRow.clearSelection();
            } else {
                return;
            }
            fireInventoryUpdated();
        });
        return deleteButton;
    }

    private JButton getPrintButton() {
        printButton = new JButton(MessageBundle.getMessage("angal.common.print.btn"));
        printButton.setMnemonic(MessageBundle.getMnemonic("angal.common.print.btn.key"));
        return printButton;
    }

    private JButton getCloseButton() {
        closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
        closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
        closeButton.addActionListener(actionEvent -> {
            String lastCharge = null;
            String lastDischarge = null;
            Integer lastSupplier = null;
            String lastDestination = null;
            String lastReference = null;
            newReference = referenceTextField.getText().trim();
            LocalDateTime lastDate = dateInventory;
            if (inventory != null) {
                lastCharge = inventory.getChargeType();
                lastDischarge = inventory.getDischargeType();
                lastSupplier = inventory.getSupplier();
                lastDestination = inventory.getDestination();
                lastReference = inventory.getInventoryReference();
                lastDate = inventory.getInventoryDate();
            }

            if (!lotsSaved.isEmpty() || !inventoryRowListAdded.isEmpty() || !lotsDeleted.isEmpty() || !inventoryRowsToDelete.isEmpty()
                    || (destination != null && !destination.getCode().equals(lastDestination))
                    || (chargeType != null && !chargeType.getCode().equals(lastCharge))
                    || (dischargeType != null && !dischargeType.getCode().equals(lastDischarge))
                    || (supplier != null && !supplier.getSupId().equals(lastSupplier))
                    || (destination == null && lastDestination != null) || (chargeType == null && lastCharge != null)
                    || (dischargeType == null && lastDischarge != null) || (supplier == null && lastSupplier != null)
                    || !Objects.equals(lastReference, newReference) || !lastDate.toLocalDate().equals(dateInventory.toLocalDate())
            ) {
                int reset = MessageDialog.yesNoCancel(null, "angal.inventoryrow.doyouwanttosavethechanges.msg");
                if (reset == JOptionPane.YES_OPTION) {
                    this.saveButton.doClick();
                }
                if (reset == JOptionPane.NO_OPTION) {
                    resetVariable();
                    dispose();
                } else {
                    resetVariable();
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
            jTetFieldEditor = new JTextField();
            jTableInventoryRow.setFillsViewportHeight(true);
            jTableInventoryRow.setModel(new InventoryRowModel());
            for (int i = 0; i < pColums.length; i++) {
                jTableInventoryRow.getColumnModel().getColumn(i).setCellRenderer(new EnabledTableCellRenderer());
                jTableInventoryRow.getColumnModel().getColumn(i).setPreferredWidth(pColumwidth[i]);
                if (!pColumnVisible[i]) {
                    jTableInventoryRow.getColumnModel().getColumn(i).setMinWidth(0);
                    jTableInventoryRow.getColumnModel().getColumn(i).setMaxWidth(0);
                    jTableInventoryRow.getColumnModel().getColumn(i).setWidth(0);
                }
            }
            jTableInventoryRow.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    jTableInventoryRow.editCellAt(jTableInventoryRow.getSelectedRow(), 5);
                    jTetFieldEditor.selectAll();
                }

            });
            jTableInventoryRow.getModel().addTableModelListener(e -> {

                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    TableModel model = (TableModel) e.getSource();
                    Object data = model.getValueAt(row, column);

                    if (column == 3) {
                        Object data2 = model.getValueAt(row, 3);
                        if (!data.toString().isEmpty() && data2.toString().isEmpty()) {
                            jTableInventoryRow.setSurrendersFocusOnKeystroke(true);
                            jTableInventoryRow.getEditorComponent().requestFocus();
                            return;
                        }
                    }

                    if (column == 4) {
                        Object data2 = model.getValueAt(row, 2);
                        if (!data.toString().isEmpty() && data2.toString().isEmpty()) {
                            jTableInventoryRow.setSurrendersFocusOnKeystroke(true);
                            jTableInventoryRow.getEditorComponent().requestFocus();
                        }
                    }
                }
            });
            DefaultCellEditor cellEditor = new DefaultCellEditor(jTetFieldEditor);
            jTableInventoryRow.setDefaultEditor(Integer.class, cellEditor);
        }
        return jTableInventoryRow;
    }

    static class EnabledTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
	        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
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
                MessageDialog.info(null, "angal.invetory.allmedicaladdedsuccessfully.msg");
            } else {
                MessageDialog.info(null, "angal.inventory.youhavealreadyaddedallproduct.msg");
            }
        }
        public InventoryRowModel() throws OHServiceException {
            if (inventory != null) {
                inventoryRowList = medicalInventoryRowManager.getMedicalInventoryRowByInventoryId(inventory.getId());
            } else {
                if (allRadio.isSelected()) {
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

        public Class<?> getColumnClass(int c) {
            if (c == 0) {
                return Integer.class;
            } else if (c == 1) {
                return String.class;
            } else if (c == 2) {
                return String.class;
            } else if (c == 3) {
                return String.class;
            } else if (c == 4) {
                return String.class;
            } else if (c == 5) {
                return String.class;
            } else if (c == 6) {
                return Double.class;
            } else if (c == 7) {
                return Integer.class;
            } else if (c == 8) {
                return Double.class;
            } else if (c == 9) {
                return Double.class;
            }
            return null;
        }

        public int getRowCount() {
            if (inventoryRowSearchList == null) {
                return 0;
            }
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
               return medInvtRow.getId();
            } else if (c == 1) {
                return medInvtRow.getMedical() == null ? "" : medInvtRow.getMedical().getProdCode();
            } else if (c == 2) {
                return medInvtRow.getMedical() == null ? "" : medInvtRow.getMedical().getDescription();
            } else if (c == 3) {
                if (medInvtRow.getLot() == null || medInvtRow.isNewLot()) {
                    return "N";
                }
                return "";
            } else if (c == 4) {
                return medInvtRow.getLot() == null ? "" : medInvtRow.getLot().getCode();
            } else if (c == 5) {
                if (medInvtRow.getLot() != null) {
                    if (medInvtRow.getLot().getDueDate() != null) {
                        return medInvtRow.getLot().getDueDate().format(DATE_TIME_FORMATTER);
                    }
                }
                return "";
            } else if (c == 6) {
	            return medInvtRow.getTheoreticQty();
            } else if (c == 7) {
                double dblValue = medInvtRow.getRealQty();
                return (int) dblValue;
            } else if (c == 8) {
                if (medInvtRow.getLot() != null) {
                    if (medInvtRow.getLot().getCost() != null) {
                        return medInvtRow.getLot().getCost();
                    }
                }
                return 0.0;
            } else if (c == 9) {
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
                if (c == 7) {
                    int intValue = 0;
                    if (value != null) {
                        try {
                            intValue = Integer.parseInt(value.toString());
                        } catch (NumberFormatException e) {
                            return;
                        }
                    }
                    if (intValue < 0) {
                        MessageDialog.error(null, "angal.inventoryrow.invalidquantity.msg");
                        return;
                    }

                    invRow.setRealqty(intValue);
                    if (invRow.getLot() != null && invRow.getLot().getCost() != null) {
                        double total = invRow.getRealQty() * invRow.getLot().getCost().doubleValue();
                        invRow.setTotal(total);
                    }
                    inventoryRowListAdded.add(invRow);
                    inventoryRowSearchList.set(r, invRow);
                }
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnEditable[columnIndex];
        }
    }

    private Lot getLot(Lot lotToUpdate) throws OHServiceException {
        Lot lot;
        if (isAutomaticLotIn()) {
            LocalDateTime preparationDate = TimeTools.getNow().truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime expiringDate = askExpiringDate();
            lot = new Lot("", preparationDate, expiringDate);
            // Cost
            BigDecimal cost = new BigDecimal(0);
            if (GeneralData.LOTWITHCOST) {
                cost = askCost(2, cost);
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

    private Lot askLot(Lot lotToUpdate) {
        LocalDateTime preparationDate;
        LocalDateTime expiringDate;
        Lot lot = null;

        JTextField lotNameTextField = new JTextField(15);
        lotNameTextField.addAncestorListener(new RequestFocusListener());
        TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotid"),
                lotNameTextField);
        suggestion.setFont(new Font("Tahoma", Font.PLAIN, 14)); //$NON-NLS-1$
        suggestion.setForeground(Color.GRAY);
        suggestion.setHorizontalAlignment(SwingConstants.CENTER);
        suggestion.changeAlpha(0.5f);
        suggestion.changeStyle(Font.BOLD + Font.ITALIC);

        LocalDate now = LocalDate.now();
        GoodDateChooser preparationDateChooser = new GoodDateChooser(now);
        GoodDateChooser expireDateChooser = new GoodDateChooser(now);
        if (lotToUpdate != null) {
            lotNameTextField.setText(lotToUpdate.getCode());
            preparationDateChooser = new GoodDateChooser(lotToUpdate.getPreparationDate().toLocalDate());
            expireDateChooser = new GoodDateChooser(lotToUpdate.getDueDate().toLocalDate());
        }
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
                    BigDecimal cost = new BigDecimal(0);
                    if (GeneralData.LOTWITHCOST) {
                        if (lotToUpdate != null) {
                            cost = askCost(2, lotToUpdate.getCost());
                        } else {
                            cost = askCost(2, cost);
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

    private BigDecimal askCost(int qty, BigDecimal lastCost) {
        double cost = 0.;
        do {
            String input = JOptionPane.showInputDialog(this,
                    MessageBundle.getMessage("angal.medicalstock.multiplecharging.unitcost"), //$NON-NLS-1$
                    lastCost);
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

    private List<MedicalInventoryRow> loadNewInventoryTable(String code, MedicalInventory inventory, boolean add) throws OHServiceException {
        List<MedicalInventoryRow> inventoryRowsList;
        if (inventory != null) {
            int id = inventory.getId();
            wardId = inventory.getWard();
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
        List<Lot> lots;
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
            medicalWardList = movWardBrowserManager.getMedicalsWard(wardId.charAt(0), false);
        }
        medicalWardList.forEach(medicalWard -> inventoryRowsList.add(new MedicalInventoryRow(0, medicalWard.getQty(), medicalWard.getQty(), null,
                medicalWard.getMedical(), medicalWard.getLot())));
        for (MedicalWard medicalWard : medicalWardList) {
            lots = movStockInsertingManager.getLotByMedical(medicalWard.getMedical(), false);
            double actualQty = medicalWard.getMedical().getInitialqty() + medicalWard.getMedical().getInqty() - medicalWard.getMedical().getOutqty();
            if (lots.isEmpty()) {
                inventoryRowTemp = new MedicalInventoryRow(0, actualQty, actualQty, null, medicalWard.getMedical(), null);
                if (!existInInventorySearchList(inventoryRowTemp)) {
                    inventoryRowsList.add(inventoryRowTemp);
                }
            } else {
                for (Lot lot : lots) {
                    inventoryRowTemp = new MedicalInventoryRow(0, lot.getMainStoreQuantity(), lot.getMainStoreQuantity(), null, medicalWard.getMedical(), lot);
                    if (!existInInventorySearchList(inventoryRowTemp)) {
                        inventoryRowsList.add(inventoryRowTemp);
                    }
                }
            }
        }
        return inventoryRowsList;
    }

    private boolean existInInventorySearchList(MedicalInventoryRow inventoryRow) {
        boolean found = false;
        List<MedicalInventoryRow> invRows = inventoryRowSearchList.stream()
                .filter(inventoryRow1 -> inventoryRow1.getMedical().getCode().equals(inventoryRow.getMedical().getCode())).toList();
        if (!invRows.isEmpty()) {
            for (MedicalInventoryRow invR: invRows) {
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

    private JRadioButton getSpecificRadio() {
        if (specificRadio == null) {
            specificRadio = new JRadioButton(MessageBundle.getMessage("angal.inventory.specificproduct.radio"));
	        specificRadio.addActionListener(actionEvent -> {
                if (specificRadio.isSelected()) {
                    codeTextField.setEnabled(true);
                    codeTextField.setText("");
                    allRadio.setSelected(false);
                }
            });
        }
        return specificRadio;
    }

    private JRadioButton getAllRadio() {
        if (allRadio == null) {
            allRadio = new JRadioButton(MessageBundle.getMessage("angal.inventory.allproduct.radio"));
	        allRadio.setSelected(inventory != null);
            specificRadio.setSelected(inventory == null);
            allRadio.addActionListener(actionEvent -> {
                if (!selectAll) {
                    if (allRadio.isSelected()) {
                        codeTextField.setEnabled(false);
                        codeTextField.setText("");
                        if (!inventoryRowSearchList.isEmpty()) {
                            int info = MessageDialog.yesNo(null, "angal.inventoryrow.doyouwanttoaddallnotyetlistedproducts.msg");
                            if (info == JOptionPane.YES_OPTION) {
                                try {
                                    allRadio.setSelected(true);
                                    jTableInventoryRow.setModel(new InventoryRowModel(true));
                                } catch (OHServiceException e){
                                    OHServiceExceptionUtil.showMessages(e);
                                }
                            } else {
                                allRadio.setSelected(false);
                                specificRadio.setEnabled(true);
                                selectAll = false;
                            }
                        } else {
                            if (mode.equals("update")) {
                                try {
                                    allRadio.setEnabled(true);
                                    jTableInventoryRow.setModel(new InventoryRowModel(true));
                                } catch (OHServiceException e) {
                                    OHServiceExceptionUtil.showMessages(e);
                                }
                            } else {
                                try {
                                    jTableInventoryRow.setModel(new InventoryRowModel());
                                } catch (OHServiceException e) {
                                    OHServiceExceptionUtil.showMessages(e);
                                }
                            }
                        }
                        fireInventoryUpdated();
                        code = null;
                        ajustWidth();
                    }
                } else {
                    MessageDialog.info(null, "angal.inventory.youhavealreadyaddedallproduct.msg");
                }
            });
        }
        return allRadio;
    }

    private JLabel getDateInventoryLabel() {
        if (dateInventoryLabel == null) {
            dateInventoryLabel = new JLabel(MessageBundle.getMessage("angal.inventory.date.label"));
        }
        return dateInventoryLabel;
    }

    private JTextField getCodeTextField() {
        if (codeTextField == null) {
            codeTextField = new JTextField();
	        codeTextField.setEnabled(inventory == null);
            codeTextField.setColumns(10);
            TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.common.code.txt"), codeTextField,
                    Show.FOCUS_LOST);
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
                        ajustWidth();
                    }
                }
            });
        }
        return codeTextField;
    }

    private void addInventoryRow(String code) throws OHServiceException {
        List<MedicalInventoryRow> inventoryRowsList = new ArrayList<>();
        List<MedicalWard> medicalWardList = new ArrayList<>();
        Medical medical = null;
        if (wardId.isEmpty()) {
            wardId = ((Ward) wardComboBox.getSelectedItem()).getCode();
        }
        if (code != null) {
            medical = medicalBrowsingManager.getMedicalByMedicalCode(code);
            if (medical != null) {
                medicalWardList = movWardBrowserManager.getMedicalsWard(wardId, medical.getCode(), false);
            } else {
                medical = chooseMedical(code);
                if (medical != null) {
                    boolean found = false;
                    if (inventoryRowSearchList != null) {
                        for (MedicalInventoryRow row : inventoryRowSearchList) {
                            if (row.getMedical().getCode().equals(medical.getCode())) {
                                found = true;
                            }
                        }
                    }
                    if (!found) {
                        medicalWardList = movWardBrowserManager.getMedicalsWard(wardId, medical.getCode(), false);
                    }
                }
            }
        } else {
            medicalWardList = movWardBrowserManager.getMedicalsWard(wardId.charAt(0), false);
        }
        if (mode.equals("new")) {
            inventoryRowsList = medicalWardList.stream().map(medWard -> new MedicalInventoryRow(0, medWard.getQty(),
                    medWard.getQty(), null, medWard.getMedical(), medWard.getLot())).toList();
        } else if (mode.equals("update")) {
            if (medical != null) {
                String medicalCode = medical.getProdCode();
                inventoryRowsList = medicalInventoryRowManager.getMedicalInventoryRowByInventoryId(inventory.getId())
                        .stream()
                        .filter(medRow -> {
                            if (medRow.getMedical().getProdCode().equals(medicalCode)) {
                                medRow.setId(0);
                                return true;
                            }
	                        return false;
                        }).toList();
            }
        }
        if (inventoryRowSearchList == null) {
            inventoryRowSearchList = new ArrayList<>();
        }
        inventoryRowsList.forEach(this::addMedInRowInInventorySearchList);
        jTableInventoryRow.updateUI();
    }

    private Medical chooseMedical(String text) throws OHServiceException {
        Map<String, Medical> medicalMap;
        List<Medical> medicals = new ArrayList<>(movWardBrowserManager.getMedicalsWard(wardId.charAt(0), false).stream()
		        .map(MedicalWard::getMedical).toList());
        if (mode.equals("update")) {
            medicals.clear();
            List<MedicalInventoryRow> inventoryRowListTemp = medicalInventoryRowManager
                    .getMedicalInventoryRowByInventoryId(inventory.getId());
            for (MedicalInventoryRow medicalInventoryRow : inventoryRowListTemp) {
                medicals.add(medicalInventoryRow.getMedical());
            }
        }
        medicalMap = new HashMap<>();
        for (Medical med : medicals) {
            String key;
            key = med.getCode().toString().toLowerCase();
            medicalMap.put(key, med);
        }
        ArrayList<Medical> medList = new ArrayList<>();
        for (Medical aMed : medicalMap.values()) {
            if (NormalizeString.normalizeContains(aMed.getDescription().toLowerCase(), text)) {
                medList.add(aMed);
            }
        }
        Collections.sort(medList);
        Medical med;
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
            if (!pColumnVisible[i]) {
                jTableInventoryRow.getColumnModel().getColumn(i).setMinWidth(0);
                jTableInventoryRow.getColumnModel().getColumn(i).setMaxWidth(0);
                jTableInventoryRow.getColumnModel().getColumn(i).setWidth(0);
            }
        }
    }

    private JLabel getReferenceLabel() {
        if (referenceLabel == null) {
            referenceLabel = new JLabel(MessageBundle.getMessage("angal.inventory.reference.label"));
        }
        return referenceLabel;
    }

    private JTextField getReferenceTextField() {
        if (referenceTextField == null) {
            referenceTextField = new JTextField();
            referenceTextField.setColumns(10);
            if (inventory != null && !mode.equals("new")) {
                referenceTextField.setText(inventory.getInventoryReference());
                referenceTextField.setEnabled(false);
            }
        }
        return referenceTextField;
    }

    private JLabel getWardLabel() {
        if (wardLabel == null) {
            wardLabel = new JLabel(MessageBundle.getMessage("angal.inventory.selectward.label"));
        }
        return wardLabel;
    }

    private JLabel getChargeLabel() {
        if (chargeLabel == null) {
            chargeLabel = new JLabel(MessageBundle.getMessage("angal.inventory.chargetype.label"));
        }
        return chargeLabel;
    }

    private JLabel getDischargeLabel() {
        if (dischargeLabel == null) {
            dischargeLabel = new JLabel(MessageBundle.getMessage("angal.inventory.dischargetype.label"));
        }
        return dischargeLabel;
    }

    private JLabel getSupplierLabel() {
        if (supplierLabel == null) {
            supplierLabel = new JLabel(MessageBundle.getMessage("angal.inventory.supplier.label"));
        }
        return supplierLabel;
    }

    private JLabel getDestinationLabel() {
        if (destinationLabel == null) {
            destinationLabel = new JLabel(MessageBundle.getMessage("angal.inventory.destination.label"));
        }
        return destinationLabel;
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
                    if (item instanceof Ward) {
                        wardSelected = (Ward) item;
                        wardId = wardSelected.getCode();
                        List<MedicalInventory> medicalWardInventory;
                        try {
                            medicalWardInventory = medicalInventoryManager
                                    .getMedicalInventoryByStatusAndWard(InventoryType.ward.toString(), wardId);
                        } catch (OHServiceException e) {
                            medicalWardInventory = new ArrayList<>();
                            OHServiceExceptionUtil.showMessages(e);
                        }

                        if (medicalWardInventory != null) {
                            activedSomeComponents();
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

    private JComboBox<MovementType> getChargeComboBox() {
        if (chargeComboBox == null) {
            chargeComboBox = new JComboBox<>();
            List<MovementType> movTypeList;
            try {
                movTypeList = medStockMovTypeManager.getMedicalDsrStockMovementType();
            } catch (OHServiceException e) {
                movTypeList = new ArrayList<>();
                OHServiceExceptionUtil.showMessages(e);
            }

            for (MovementType movementType : movTypeList) {
                if (movementType.getType().equals("+")) {
                    chargeComboBox.addItem(movementType);
                }
                if ((inventory != null && !mode.equals("new")) && movementType.getCode().equals(inventory.getChargeType())) {
                    chargeComboBox.setSelectedItem(movementType);
                }
            }

        }
        return chargeComboBox;
    }

    private JComboBox<MovementType> getDischargeComboBox() {
        if (dischargeComboBox == null) {
            dischargeComboBox = new JComboBox<>();
            List<MovementType> movTypeList;
            try {
                movTypeList = medStockMovTypeManager.getMedicalDsrStockMovementType();
            } catch (OHServiceException e) {
                movTypeList = new ArrayList<>();
                OHServiceExceptionUtil.showMessages(e);
            }

            for (MovementType movementType : movTypeList) {
                if (movementType.getType().equals("-")) {
                    dischargeComboBox.addItem(movementType);
                }
                if ((inventory != null && !mode.equals("new")) && movementType.getCode().equals(inventory.getDischargeType())) {
                    dischargeComboBox.setSelectedItem(movementType);
                }
            }
        }
        return dischargeComboBox;
    }

    private JComboBox<Supplier> getSupplierComboBox() {
        if (supplierComboBox == null) {
            supplierComboBox = new JComboBox<>();
            List<Supplier> suppliers;
            try {
                suppliers = supplierBrowserManager.getAll();
            } catch (OHServiceException e) {
                suppliers = new ArrayList<>();
                OHServiceExceptionUtil.showMessages(e);
            }
            for (Supplier supplier : suppliers) {
                supplierComboBox.addItem(supplier);
                if ((inventory != null && !mode.equals("new")) && supplier.getSupId().equals(inventory.getSupplier())) {
                    supplierComboBox.setSelectedItem(supplier);
                }
            }
            if (mode.equals("new")) {
                supplierComboBox.setSelectedIndex(-1);
            }
        }
        return supplierComboBox;
    }

    private JComboBox<Ward> getDestinationComboBox() {
        if (destinationComboBox == null) {
            destinationComboBox = new JComboBox<>();
            List<Ward> wardList;
            try {
                wardList = wardBrowserManager.getWards();
            } catch (OHServiceException e) {
                wardList = new ArrayList<>();
                OHServiceExceptionUtil.showMessages(e);
            }
            for (Ward ward : wardList) {
                destinationComboBox.addItem(ward);
                if ((inventory != null && !mode.equals("new")) && ward.getCode().equals(inventory.getDestination())) {
                    destinationComboBox.setSelectedItem(ward);
                }
            }
            if (mode.equals("new")) {
                destinationComboBox.setSelectedIndex(-1);
            }
        }
        return destinationComboBox;
    }

    private void disabledSomeComponents() {
        jCalendarInventory.setEnabled(false);
        specificRadio.setEnabled(false);
        codeTextField.setEnabled(false);
        allRadio.setEnabled(false);
        referenceTextField.setEnabled(false);
        jTableInventoryRow.setEnabled(false);
        saveButton.setEnabled(false);
        chargeComboBox.setEnabled(false);
        dischargeComboBox.setEnabled(false);
        supplierComboBox.setEnabled(false);
        destinationComboBox.setEnabled(false);
    }

    private void activedSomeComponents() {
        jCalendarInventory.setEnabled(true);
        specificRadio.setEnabled(true);
        codeTextField.setEnabled(true);
        allRadio.setEnabled(true);
        referenceTextField.setEnabled(true);
        jTableInventoryRow.setEnabled(true);
        wardComboBox.setEnabled(false);
        saveButton.setEnabled(true);
        chargeComboBox.setEnabled(true);
        dischargeComboBox.setEnabled(true);
        supplierComboBox.setEnabled(true);
        destinationComboBox.setEnabled(true);
    }

    private JLabel getLoaderLabel() {
        if (loaderLabel == null) {
            Icon icon = new ImageIcon("rsc/icons/oh_loader.GIF");
            loaderLabel = new JLabel("");
            loaderLabel.setIcon(icon);
            loaderLabel.setVisible(false);
        }
        return loaderLabel;
    }

    private void addMedInRowInInventorySearchList(MedicalInventoryRow inventoryRow) {
        int position = getPosition(inventoryRow);
        MessageDialog.info(null, position + "");
        MessageDialog.info(null, inventoryRow.getMedical().getDescription());
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
        for (MedicalInventoryRow inventoryRow1: inventoryRowSearchList) {
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
}
