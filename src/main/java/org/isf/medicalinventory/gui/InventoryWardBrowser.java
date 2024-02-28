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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.InventoryState;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;

public class InventoryWardBrowser extends ModalJFrame {

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
    private JButton closeButton;
    private JButton newButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton viewButton;
    private JScrollPane scrollPaneInventory;
    private JTable jTableInventory;
    private String[] pColums = {
            MessageBundle.getMessage("angal.inventory.referenceshow.col").toUpperCase(),
            MessageBundle.getMessage("angal.common.ward.col").toUpperCase(),
            MessageBundle.getMessage("angal.common.date.col").toUpperCase(),
            MessageBundle.getMessage("angal.inventory.state.col").toUpperCase(),
            MessageBundle.getMessage("angal.common.user.col").toUpperCase()
    };
    private int[] pColumwidth = { 150, 150, 100, 100, 150 };
    private JComboBox<Object> stateComboBox;
    private JLabel stateLabel;

    public InventoryWardBrowser() {
        initComponents();
    }

    private void initComponents() {

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null); // center
        setTitle(MessageBundle.getMessage("angal.inventory.managementwardtitle"));

        panelHeader = getPanelHeader();
        getContentPane().add(panelHeader, BorderLayout.NORTH);

        panelContent = getPanelContent();
        getContentPane().add(panelContent, BorderLayout.CENTER);

        panelFooter = getPanelFooter();
        getContentPane().add(panelFooter, BorderLayout.SOUTH);

        for (int i = 0; i < pColumwidth.length; i++) {
            jTableInventory.getColumnModel().getColumn(i).setMinWidth(pColumwidth[i]);
        }

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        // pack();
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
            GridBagConstraints gbc_stateLabel = new GridBagConstraints();
            gbc_stateLabel.fill = GridBagConstraints.HORIZONTAL;
            gbc_stateLabel.insets = new Insets(0, 0, 0, 5);
            gbc_stateLabel.gridx = 4;
            gbc_stateLabel.gridy = 0;
            panelHeader.add(getStateLabel(), gbc_stateLabel);
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
            panelFooter.add(getNewButton());
            panelFooter.add(getViewButton());
            panelFooter.add(getUpdateButton());
            panelFooter.add(getDeleteButton());
            panelFooter.add(getCloseButton());
        }
        return panelFooter;
    }

    private GoodDateChooser getJCalendarFrom() {
        if (jCalendarFrom == null) {
            jCalendarFrom = new GoodDateChooser(dateFrom.toLocalDate());
            jCalendarFrom.addDateChangeListener(dateChangeEvent -> {
                LocalDate newDate = dateChangeEvent.getNewDate();
                if (newDate != null) {
                    dateFrom = newDate.atStartOfDay();
                    InventoryBrowsingModel inventoryModel = new InventoryBrowsingModel();
                }
            });
            jCalendarFrom.setEnabled(false);
        }
        return jCalendarFrom;
    }

    private GoodDateChooser getJCalendarTo() {
        if (jCalendarTo == null) {
            jCalendarTo = new GoodDateChooser(dateTo.toLocalDate(), false);
            jCalendarTo.addDateChangeListener(dateChangeEvent -> {
                LocalDate newDate = dateChangeEvent.getNewDate();
                if (newDate != null) {
                    dateTo = newDate.atStartOfDay();
                    InventoryBrowsingModel inventoryModel = new InventoryBrowsingModel();
                }
            });
            jCalendarTo.setEnabled(false);
        }
        return jCalendarTo;
    }

    private JLabel getJLabelTo() {
        if (jLabelTo == null) {
            jLabelTo = new JLabel();
            jLabelTo.setHorizontalAlignment(SwingConstants.RIGHT);
            jLabelTo.setText(MessageBundle.getMessage("angal.common.dateto.label")); //$NON-NLS-1$
        }
        return jLabelTo;
    }

    private JLabel getJLabelFrom() {
        if (jLabelFrom == null) {
            jLabelFrom = new JLabel();
            jLabelFrom.setHorizontalAlignment(SwingConstants.RIGHT);
            jLabelFrom.setText(MessageBundle.getMessage("angal.common.datefrom.label")); //$NON-NLS-1$
        }
        return jLabelFrom;
    }

    private JButton getNewButton() {
        newButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
        newButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
        return newButton;
    }

    private JButton getViewButton() {
        viewButton = new JButton(MessageBundle.getMessage("angal.inventory.view.btn"));
        viewButton.setMnemonic(MessageBundle.getMnemonic("angal.inventory.view.btn.key"));
        return viewButton;
    }

    private JButton getUpdateButton() {
        updateButton = new JButton(MessageBundle.getMessage("angal.common.update.btn"));
        updateButton.setMnemonic(MessageBundle.getMnemonic("angal.common.update.btn.key"));
        return updateButton;
    }

    private JButton getDeleteButton() {
        deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
        deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
        return deleteButton;
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
            scrollPaneInventory.setViewportView(getJTableInventory());
        }
        return scrollPaneInventory;
    }

    private JTable getJTableInventory() {
        if (jTableInventory == null) {
            jTableInventory = new JTable();
            jTableInventory.setFillsViewportHeight(true);
            jTableInventory.setModel(new InventoryBrowsingModel());
        }
        return jTableInventory;
    }

    class InventoryBrowsingModel extends DefaultTableModel {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public InventoryBrowsingModel() {

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
            }
            return null;
        }

        public String getColumnName(int c) {
            return pColums[c];
        }

        public int getColumnCount() {
            return pColums.length;
        }

        @Override
        public boolean isCellEditable(int arg0, int arg1) {
            return false;
        }

    }

    private JComboBox<Object> getComboBox() {
        if (stateComboBox == null) {
            stateComboBox = new JComboBox<Object>();
            stateComboBox.addItem("");
            for (InventoryState.State currentState : InventoryState.State.values()) {
                stateComboBox.addItem(MessageBundle.getMessage(currentState.getLabel()));
            }
        }
        return stateComboBox;
    }

    private JLabel getStateLabel() {
        if (stateLabel == null) {
            stateLabel = new JLabel(MessageBundle.getMessage("angal.inventory.state.label"));
            stateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        return stateLabel;
    }
}