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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
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

        public void InventoryInserted(AWTEvent e);

        public void InventoryUpdated(AWTEvent e);

        public void InventoryCancelled(AWTEvent e);
    }

    public static void addInventoryListener(InventoryListener l) {
        InventoryListeners.add(InventoryListener.class, l);
    }

    private void fireInventoryUpdated() {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
            private static final long serialVersionUID = 1L;
        };

        EventListener[] listeners = InventoryListeners.getListeners(InventoryListener.class);
        for (int i = 0; i < listeners.length; i++) {
            ((InventoryListener) listeners[i]).InventoryUpdated(event);
        }
    }

    private GoodDateChooser jCalendarInventory;
    private LocalDateTime dateInventory = TimeTools.getServerDateTime();
    private JPanel panelHeader;
    private JPanel panelFooter;
    private JPanel panelContent;
    private JButton closeButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton printButton;
    private JButton validateButton;
    private JButton deleteButton;
    private JScrollPane scrollPaneInventory;
    private JTable jTableInventoryRow;
    private List<MedicalInventoryRow> inventoryRowList;
    private List<MedicalInventoryRow> inventoryRowSearchList;
    private String[] pColums = { MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.medical.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.new.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.lotcode.col").toUpperCase(),
            MessageBundle.getMessage("angal.medicalstock.duedate.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.theorticalqty.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.realqty.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.unitprice.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.totalprice.col").toUpperCase()
    };
    private int[] pColumwidth = { 100, 200, 50, 100, 100, 100, 80, 80, 80 };
    private boolean[] columnEditable = { false, false, false, false, false, false, true, false, false };
    private boolean[] columnEditableView = { false, false, false, false, false, false, false, false, false };
    private boolean[] pColumnVisible = { true, true, true, !GeneralData.AUTOMATICLOT_IN, true, true, true,
            GeneralData.LOTWITHCOST, true };
    private MedicalInventory inventory = null;
    private JRadioButton specificRadio;
    private JRadioButton allRadio;
    private JLabel dateInventoryLabel;
    private JTextField codeTextField;
    private String code = null;
    private String mode = null;
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
        cancelButton.setVisible(false);
        disabledSomeComponents();
    }

    public InventoryWardEdit(MedicalInventory inventory, String modee) {
        this.inventory = inventory;
        mode = modee;
        initComponents();
        if (mode.equals("view")) {
            validateButton.setVisible(false);
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
            wardComboBox.setEnabled(false);
            columnEditable = columnEditableView;
        }
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
            panelFooter.add(getValidateButton());
            panelFooter.add(getPrintButton());
            panelFooter.add(getCloseButton());
        }
        return panelFooter;
    }

    private GoodDateChooser getJCalendarFrom() {
        if (jCalendarInventory == null) {

            jCalendarInventory = new GoodDateChooser(LocalDate.now());
            if (inventory != null) {
                jCalendarInventory.setDate(inventory.getInventoryDate().toLocalDate());
            }
            jCalendarInventory.addDateChangeListener(dateChangeEvent -> {
                LocalDate newDate = dateChangeEvent.getNewDate();
                if (newDate != null) {
                    dateInventory = newDate.atStartOfDay();
                    jCalendarInventory.setDate(newDate);
                }
            });
        }
        return jCalendarInventory;
    }

    private JButton getSaveButton() {
        saveButton = new JButton(MessageBundle.getMessage("angal.common.save"));
        saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
        saveButton.addActionListener(actionEvent -> {
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
                inventory.setStatus(InventoryStatus.draft.toString());
                inventory.setUser(user);
                inventory.setInventoryType(InventoryType.ward.toString());
                if (!wardComboBox.getSelectedItem().equals("") && wardComboBox.getSelectedItem() != null) {
                    Ward selectedWard = (Ward) wardComboBox.getSelectedItem();
                    inventory.setWard(selectedWard.getCode());
                } else {
                    MessageDialog.error(null, "angal.inventory.youmustselectaward.msg");
                    return;
                }
                MedicalInventory meInventory;
                try {
                    meInventory = medicalInventoryManager.newMedicalInventory(inventory);
                    if (meInventory != null) {
                        MedicalInventoryRow currentInventoryRow;
                        for (Iterator<MedicalInventoryRow> iterator = inventoryRowSearchList.iterator(); iterator
                                .hasNext();) {
                            MedicalInventoryRow medicalInventoryRow = (MedicalInventoryRow) iterator.next();
                            medicalInventoryRow.setInventory(meInventory);
                            Lot lot = medicalInventoryRow.getLot();
                            if (lot != null && lot.getCode().equals("")) {
                                medicalInventoryRow.setLot(null);
                            }
                            currentInventoryRow = medicalInventoryRowManager
                                    .newMedicalInventoryRow(medicalInventoryRow);
                            if (currentInventoryRow == null) {
                                checkResults++;
                            }
                        }
                        if (checkResults == 0) {
                            // enable validation
                            mode = "update";
                            MessageDialog.info(this, "angal.inventory.savesucces.msg");
                            fireInventoryUpdated();
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
            } else if (inventory != null && mode.equals("update")) {
                checkResults = 0;
                boolean toUpdate = false;
                MedicalInventory result = new MedicalInventory();
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
                        result = medicalInventoryManager.updateMedicalInventory(inventory);
                    } catch (OHServiceException e) {
                        OHServiceExceptionUtil.showMessages(e);
                    }
                }

                if (result != null) {
                    try {
                        for (Iterator<MedicalInventoryRow> iterator = inventoryRowSearchList.iterator(); iterator
                                .hasNext();) {
                            MedicalInventoryRow medicalInventoryRow = (MedicalInventoryRow) iterator.next();
                            if (medicalInventoryRowManager.updateMedicalInventoryRow(medicalInventoryRow) == null) {
                                checkResults++;
                            }
                            movStockInsertingManager.updateLot(medicalInventoryRow.getLot());
                        }
                    } catch (OHServiceException e) {
                        OHServiceExceptionUtil.showMessages(e);
                    }
                    if (checkResults == 0) {
                        MessageDialog.info(this, "angal.inventory.updatesucces.msg");
                        validateButton.setEnabled(true);
                        fireInventoryUpdated();
                    } else {
                        MessageDialog.error(null, "angal.inventory.updaterowerror.msg");
                    }
                } else {
                    MessageDialog.error(null, "angal.inventory.updateerror.msg");
                }

            }
        });
        return saveButton;
    }

    private JButton getValidateButton() {

        validateButton = new JButton(MessageBundle.getMessage("angal.inventory.validate.btn"));
        validateButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.validate.btn.key"));
        return validateButton;
    }

    private JButton getCancelButton() {
        cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
        cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
        cancelButton.addActionListener(actionEvent -> {
            if (jTableInventoryRow.getSelectedRowCount() > 1) {
                MessageDialog.error(this, "angal.inventoryrow.pleaseselectonlyoneinventoryrow.msg");
                return;
            }
            int selectedRow = jTableInventoryRow.getSelectedRow();
            if (selectedRow == -1) {
                MessageDialog.error(this, "angal.inventoryrow.pleaseselectonlyoneinventoryrow.msg");
                return;
            }
            MedicalInventoryRow selectedInventory = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRow,
                    -1);
            int delete = MessageDialog.yesNo(null, "angal.inventoryrow.doyoureallywanttodeletethisinventoryrow.msg");
            if (delete == JOptionPane.YES_OPTION) {
                if (selectedInventory.getInventory() == null) {
                    inventoryRowSearchList.remove(selectedRow);
                }
            } else {
                return;
            }
            jTableInventoryRow.updateUI();
        });
        return cancelButton;
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
                    List<MedicalInventoryRow> inventoryRowsToDelete = new ArrayList<>();
                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        MedicalInventoryRow inventoryRow = (MedicalInventoryRow) jTableInventoryRow
                                .getValueAt(selectedRows[i], -1);
                        inventoryRowsToDelete.add(inventoryRow);
                    }
                    try {
                        medicalInventoryRowManager.deleteMedicalInventoryRows(inventoryRowsToDelete);
                        inventoryRowSearchList.removeAll(inventoryRowsToDelete);
                    } catch (OHServiceException e) {
                        OHServiceExceptionUtil.showMessages(e);
                        return;
                    }
                }
            } else {
                return;
            }
            jTableInventoryRow.updateUI();
            ajustWidth();
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
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
            jTableInventoryRow.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        jTableInventoryRow.editCellAt(jTableInventoryRow.getSelectedRow(), 5);
                        jTetFieldEditor.selectAll();
                    }

                }
            });
            jTableInventoryRow.getModel().addTableModelListener(new TableModelListener() {

                @Override
                public void tableChanged(TableModelEvent e) {

                    if (e.getType() == TableModelEvent.UPDATE) {
                        int row = e.getFirstRow();
                        int column = e.getColumn();
                        TableModel model = (TableModel) e.getSource();
                        Object data = model.getValueAt(row, column);

                        if (column == 2) {
                            Object data2 = model.getValueAt(row, 3);
                            if (!data.toString().equals("") && data2.toString().equals("")) {
                                jTableInventoryRow.setSurrendersFocusOnKeystroke(true);
                                jTableInventoryRow.getEditorComponent().requestFocus();
                                return;
                            }
                        }

                        if (column == 3) {
                            Object data2 = model.getValueAt(row, 2);
                            if (!data.toString().equals("") && data2.toString().equals("")) {
                                jTableInventoryRow.setSurrendersFocusOnKeystroke(true);
                                jTableInventoryRow.getEditorComponent().requestFocus();
                                return;
                            }
                        }
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return cell;
        }
    }

    class InventoryRowModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        public InventoryRowModel() {
            inventoryRowList = new ArrayList<>();
            if (inventory == null) {
                if (allRadio.isSelected()) {
                    try {
                        inventoryRowList = loadNewInventoryTable(null);
                    } catch (OHServiceException e) {
                        OHServiceExceptionUtil.showMessages(e);
                    }
                } else if (specificRadio.isSelected() && code != null && !code.trim().equals("")) {
                    try {
                        inventoryRowList = loadNewInventoryTable(code.trim());
                    } catch (OHServiceException e) {
                        OHServiceExceptionUtil.showMessages(e);
                    }
                }
            } else if (inventory != null) {
                if (allRadio.isSelected()) {
                    try {
                        inventoryRowList = medicalInventoryRowManager
                                .getMedicalInventoryRowByInventoryId(inventory.getId());
                    } catch (OHServiceException e) {
                        OHServiceExceptionUtil.showMessages(e);
                    }
                } else if (specificRadio.isSelected() && code != null && !code.trim().equals("")) {
                    try {
                        inventoryRowList = medicalInventoryRowManager
                                .getMedicalInventoryRowByInventoryIdAndMedicalCode(inventory.getId(), code.trim());
                    } catch (OHServiceException e) {
                        OHServiceExceptionUtil.showMessages(e);
                    }
                }
            }

            inventoryRowSearchList = inventoryRowList;
        }

        public Class<?> getColumnClass(int c) {
            if (c == 0) {
                return String.class;
            } else if (c == 1) {
                return String.class;
            } else if (c == 2) {
                return String.class;
            } else if (c == 3) {
                return String.class;
            } else if (c == 4) {
                return String.class;
            } else if (c == 5) {
                return Double.class;
            } else if (c == 6) {
                return Integer.class;
            } else if (c == 7) {
                return Double.class;
            } else if (c == 8) {
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
                return medInvtRow.getMedical() == null ? "" : medInvtRow.getMedical().getProdCode();
            } else if (c == 1) {
                return medInvtRow.getMedical() == null ? "" : medInvtRow.getMedical().getDescription();
            } else if (c == 2) {
                if (medInvtRow.getLot() == null || medInvtRow.isNewLot()) {
                    return "N";
                }
                return "";
            } else if (c == 3) {
                return medInvtRow.getLot() == null ? "" : medInvtRow.getLot().getCode();
            } else if (c == 4) {
                if (medInvtRow.getLot() != null) {
                    if (medInvtRow.getLot().getDueDate() != null) {
                        return medInvtRow.getLot().getDueDate().format(DATE_TIME_FORMATTER);
                    }
                }
                return "";
            } else if (c == 5) {
                Double dblVal = medInvtRow.getTheoreticQty();
                return dblVal;
            } else if (c == 6) {
                Double dblValue = medInvtRow.getRealQty();
                return dblValue.intValue();
            } else if (c == 7) {
                if (medInvtRow.getLot() != null) {
                    if (medInvtRow.getLot().getCost() != null) {
                        return medInvtRow.getLot().getCost();
                    }
                }
                return 0.0;
            } else if (c == 8) {
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
                if (c == 6) {
                    Integer intValue = 0;
                    try {
                        intValue = Integer.parseInt(value.toString());
                    } catch (NumberFormatException e) {
                        intValue = 0;
                        return;
                    }
                    if (intValue < 0) {
                        MessageDialog.error(null, "angal.inventoryrow.invalidquantity.msg");
                        return;
                    }

                    invRow.setRealqty(intValue);
                    inventoryRowSearchList.set(r, invRow);
                }
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnEditable[columnIndex];
        }
    }

    private Lot getLot() {
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
            lot = askLot();
        }
        return lot;
    }

    private Lot askLot() {
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

    private List<MedicalInventoryRow> loadNewInventoryTable(String code) throws OHServiceException {
        List<MedicalInventoryRow> inventoryRowsList = new ArrayList<>();
        List<MedicalWard> medicalWardList = new ArrayList<>();
        Medical medical = null;
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
        medicalWardList.stream().forEach(medicalWard -> {
            inventoryRowsList.add(new MedicalInventoryRow(0, medicalWard.getQty(), medicalWard.getQty(), null,
                    medicalWard.getMedical(), medicalWard.getLot()));
        });
        return inventoryRowsList;
    }

    class DecimalFormatRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
        private final DecimalFormat formatter = new DecimalFormat("#,##0.00"); //$NON-NLS-1$

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            // First format the cell value as required
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            value = formatter.format((Number) value);
            // setHorizontalAlignment(columnAlignment[column]);
            if (!columnEditable[column]) {
                cell.setBackground(Color.LIGHT_GRAY);
            }

            // And pass it on to parent class
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    private JRadioButton getSpecificRadio() {
        if (specificRadio == null) {
            specificRadio = new JRadioButton(MessageBundle.getMessage("angal.inventory.specificproduct.radio"));
            if (inventory != null) {
                specificRadio.setSelected(false);
            } else {
                specificRadio.setSelected(true);
            }
            specificRadio.addActionListener(actionEvent -> {

                if (specificRadio.isSelected()) {
                    codeTextField.setEnabled(true);
                    codeTextField.setText("");
                    if (inventoryRowList != null) {
                        inventoryRowList.clear();
                    }
                    if (inventoryRowSearchList != null) {
                        inventoryRowSearchList.clear();
                    }
                    jTableInventoryRow.updateUI();
                    ajustWidth();
                }
            });
        }
        return specificRadio;
    }

    private JRadioButton getAllRadio() {
        if (allRadio == null) {
            allRadio = new JRadioButton(MessageBundle.getMessage("angal.inventory.allproduct.radio"));
            if (inventory != null) {
                allRadio.setSelected(true);
            } else {
                allRadio.setSelected(false);
            }
            allRadio.addActionListener(actionEvent -> {
                if (allRadio.isSelected()) {
                    codeTextField.setEnabled(false);
                    codeTextField.setText("");
                    if (inventoryRowList != null) {
                        inventoryRowList.clear();
                    }
                    if (inventoryRowSearchList != null) {
                        inventoryRowSearchList.clear();
                    }
                    jTableInventoryRow.setModel(new InventoryRowModel());
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
            dateInventoryLabel = new JLabel(MessageBundle.getMessage("angal.inventory.date.label"));
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
        List<MedicalInventoryRow> inventoryRowsList = new ArrayList<MedicalInventoryRow>();
        List<MedicalWard> medicalWardList = new ArrayList<MedicalWard>();
        Medical medical = null;
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
                        .filter(medRow -> medRow.getMedical().getProdCode().equals(medicalCode)).toList();
            }
        }
        if (inventoryRowSearchList == null) {
            inventoryRowSearchList = new ArrayList<>();
        }
        for (MedicalInventoryRow inventoryRow : inventoryRowsList) {
            inventoryRowSearchList.add(inventoryRow);
        }
        jTableInventoryRow.updateUI();
    }

    private Medical chooseMedical(String text) throws OHServiceException {
        Map<String, Medical> medicalMap;
        List<Medical> medicals = movWardBrowserManager.getMedicalsWard(wardId.charAt(0), false).stream()
                .map(medicalWard -> medicalWard.getMedical()).toList();
        if (mode.equals("update")) {
            medicals.clear();
            List<MedicalInventoryRow> inventoryRowListTemp = medicalInventoryRowManager
                    .getMedicalInventoryRowByInventoryId(inventory.getId());
            for (MedicalInventoryRow medicalInventoryRow : inventoryRowListTemp) {
                medicals.add(medicalInventoryRow.getMedical());
            }
        }
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
            wardComboBox = new JComboBox<Ward>();
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
                            return;
                        }
                    }
                }
            });
        }
        return wardComboBox;
    }

    private JComboBox<MovementType> getChargeComboBox() {
        if (chargeComboBox == null) {
            chargeComboBox = new JComboBox<MovementType>();
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
            }
        }
        return chargeComboBox;
    }

    private JComboBox<MovementType> getDischargeComboBox() {
        if (dischargeComboBox == null) {
            dischargeComboBox = new JComboBox<MovementType>();
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
            }
        }
        return dischargeComboBox;
    }

    private JComboBox<Supplier> getSupplierComboBox() {
        if (supplierComboBox == null) {
            supplierComboBox = new JComboBox<Supplier>();
            List<Supplier> suppliers;
            try {
                suppliers = supplierBrowserManager.getAll();
            } catch (OHServiceException e) {
                suppliers = new ArrayList<>();
                OHServiceExceptionUtil.showMessages(e);
            }
            for (Supplier supplier : suppliers) {
                supplierComboBox.addItem(supplier);
            }
            supplierComboBox.setSelectedIndex(-1);
        }
        return supplierComboBox;
    }

    private JComboBox<Ward> getDestinationComboBox() {
        if (destinationComboBox == null) {
            destinationComboBox = new JComboBox<Ward>();
            List<Ward> wardList;
            try {
                wardList = wardBrowserManager.getWards();
            } catch (OHServiceException e) {
                wardList = new ArrayList<>();
                OHServiceExceptionUtil.showMessages(e);
            }
            for (Ward ward : wardList) {
                destinationComboBox.addItem(ward);
            }
            destinationComboBox.setSelectedIndex(-1);
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
        validateButton.setEnabled(false);
        printButton.setEnabled(false);
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
}
