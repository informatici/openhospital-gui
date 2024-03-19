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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EventListener;
import java.util.GregorianCalendar;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.TextPrompt;
import org.isf.utils.jobjects.TextPrompt.Show;
import org.isf.utils.time.TimeTools;

public class InventoryWardEdit extends ModalJFrame {

    private static final long serialVersionUID = 1L;

    private static EventListenerList InventoryListeners = new EventListenerList();

    public interface InventoryListener extends EventListener {

    }

    public static void addInventoryListener(InventoryListener l) {
        InventoryListeners.add(InventoryListener.class, l);
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
    private JScrollPane scrollPaneInventory;
    private JTable jTableInventoryRow;
    private String[] pColums = { MessageBundle.getMessage("angal.common.code.txt"),
            MessageBundle.getMessage("angal.inventory.medical.col"),
            MessageBundle.getMessage("angal.inventory.theorticalqty.col"),
            MessageBundle.getMessage("angal.inventory.realqty.col")
    };
    private boolean[] columnEditable = { false, false, false, true };
    private JRadioButton specificRadio;
    private JRadioButton allRadio;
    private JTextField searchTextField;
    private JLabel dateInventoryLabel;
    private JTextField codeTextField;
    private JLabel referenceLabel;
    private JTextField referenceTextField;
    private JTextField jTetFieldEditor;
    private JLabel wardLabel;
    private JComboBox wardComboBox;
    private JLabel loaderLabel;

    public InventoryWardEdit() {
        initComponents();
        cancelButton.setVisible(false);
        disabledSomeComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new DimensionUIResource(750, 580));
        setLocationRelativeTo(null); // center
        setTitle(MessageBundle.getMessage("angal.inventory.edit.title"));

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
            panelHeader.setBorder(new EmptyBorder(0, 0, 5, 0));
            GridBagLayout gbl_panelHeader = new GridBagLayout();
            gbl_panelHeader.columnWidths = new int[] { 123, 206, 187, 195, 0, 0 };
            gbl_panelHeader.rowHeights = new int[] { 34, 36, 0, 0 };
            gbl_panelHeader.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
            gbl_panelHeader.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
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
            GridBagConstraints gbc_specificRadio = new GridBagConstraints();
            gbc_specificRadio.anchor = GridBagConstraints.EAST;
            gbc_specificRadio.insets = new Insets(0, 0, 0, 5);
            gbc_specificRadio.gridx = 0;
            gbc_specificRadio.gridy = 2;
            panelHeader.add(getSpecificRadio(), gbc_specificRadio);
            GridBagConstraints gbc_codeTextField = new GridBagConstraints();
            gbc_codeTextField.insets = new Insets(0, 0, 0, 5);
            gbc_codeTextField.fill = GridBagConstraints.HORIZONTAL;
            gbc_codeTextField.gridx = 1;
            gbc_codeTextField.gridy = 2;
            panelHeader.add(getCodeTextField(), gbc_codeTextField);
            GridBagConstraints gbc_allRadio = new GridBagConstraints();
            gbc_allRadio.anchor = GridBagConstraints.EAST;
            gbc_allRadio.insets = new Insets(0, 0, 0, 5);
            gbc_allRadio.gridx = 2;
            gbc_allRadio.gridy = 2;
            panelHeader.add(getAllRadio(), gbc_allRadio);
            GridBagConstraints gbc_searchTextField = new GridBagConstraints();
            gbc_searchTextField.insets = new Insets(0, 0, 0, 5);
            gbc_searchTextField.fill = GridBagConstraints.HORIZONTAL;
            gbc_searchTextField.gridx = 3;
            gbc_searchTextField.gridy = 2;
            panelHeader.add(getSearchTextField(), gbc_searchTextField);
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
            panelFooter.add(getValidateButton());
            panelFooter.add(getCancelButton());
            panelFooter.add(getPrintButton());
            panelFooter.add(getCloseButton());
        }
        return panelFooter;
    }

    private GoodDateChooser getJCalendarFrom() {
        if (jCalendarInventory == null) {

            jCalendarInventory = new GoodDateChooser(dateInventory.toLocalDate());
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
        return cancelButton;
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
            scrollPaneInventory.setViewportView(getJTableInventoryRow());
        }
        return scrollPaneInventory;
    }

    private JTable getJTableInventoryRow() {
        if (jTableInventoryRow == null) {
            jTableInventoryRow = new JTable();
            jTetFieldEditor = new JTextField();
            jTableInventoryRow.setFillsViewportHeight(true);
            jTableInventoryRow.setModel(new InventoryRowModel());
            jTableInventoryRow.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {

                    if (!e.getValueIsAdjusting()) {
                        jTableInventoryRow.editCellAt(jTableInventoryRow.getSelectedRow(), 3);
                        jTetFieldEditor.selectAll();
                    }

                }
            });
            DefaultCellEditor cellEditor = new DefaultCellEditor(jTetFieldEditor);
            jTableInventoryRow.setDefaultEditor(Double.class, cellEditor);
        }
        return jTableInventoryRow;
    }

    class InventoryRowModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        public Class<?> getColumnClass(int c) {
            if (c == 0) {
                return String.class;
            } else if (c == 1) {
                return String.class;
            } else if (c == 2) {
                return Double.class;
            } else if (c == 3) {
                return Double.class;
            }
            return null;
        }

        public int getRowCount() {
            return 0;
        }

        public String getColumnName(int c) {
            return pColums[c];
        }

        public int getColumnCount() {
            return pColums.length;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnEditable[columnIndex];
        }
    }

    public String formatDateTime(GregorianCalendar time) {
        if (time == null)
            return "";
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy"); //$NON-NLS-1$
        return format.format(time.getTime());
    }

    class DecimalFormatRenderer extends DefaultTableCellRenderer {
        /**
         * 
         */
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
        }
        return specificRadio;
    }

    private JRadioButton getAllRadio() {
        if (allRadio == null) {
            allRadio = new JRadioButton(MessageBundle.getMessage("angal.inventory.allproduct.radio"));
        }
        return allRadio;
    }

    private JTextField getSearchTextField() {
        if (searchTextField == null) {
            searchTextField = new JTextField();
            searchTextField.setColumns(10);
            TextPrompt suggestion = new TextPrompt(
                    MessageBundle
                            .getMessage("angal.inventory.searchproduct.txt"),
                    searchTextField, Show.FOCUS_LOST);
            {
                suggestion.setForeground(Color.GRAY);
                suggestion.setHorizontalAlignment(JLabel.CENTER);
                suggestion.changeAlpha(0.5f);
                suggestion.changeStyle(Font.BOLD + Font.ITALIC);
            }
            searchTextField.setEnabled(false);
        }
        return searchTextField;
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
            codeTextField.setEnabled(false);
            codeTextField.setColumns(10);
            TextPrompt suggestion = new TextPrompt(
                    MessageBundle
                            .getMessage("angal.inventory.productcode.text"),
                    codeTextField, Show.FOCUS_LOST);
            {
                suggestion.setForeground(Color.GRAY);
                suggestion.setHorizontalAlignment(JLabel.CENTER);
                suggestion.changeAlpha(0.5f);
                suggestion.changeStyle(Font.BOLD + Font.ITALIC);
            }
        }
        return codeTextField;
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

            referenceTextField.setEnabled(false);
        }
        return referenceTextField;
    }

    private JLabel getWardLabel() {
        if (wardLabel == null) {
            wardLabel = new JLabel(MessageBundle.getMessage("angal.inventory.selectward.label"));
        }
        return wardLabel;
    }

    private JComboBox getWardComboBox() {
        wardComboBox = new JComboBox<>();
        return wardComboBox;
    }

    private void disabledSomeComponents() {
        jCalendarInventory.setEnabled(false);
        searchTextField.setEnabled(false);
        specificRadio.setEnabled(false);
        codeTextField.setEnabled(false);
        allRadio.setEnabled(false);
        referenceTextField.setEnabled(false);
        jTableInventoryRow.setEnabled(false);
        saveButton.setEnabled(false);
        validateButton.setEnabled(false);
        printButton.setEnabled(false);
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
