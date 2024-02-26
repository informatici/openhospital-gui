package org.isf.medicalinventory.gui;

import org.isf.utils.jobjects.InventoryState;
import org.isf.generaldata.MessageBundle;
import org.isf.utils.jobjects.ModalJFrame;

import com.toedter.calendar.JDateChooser;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import java.awt.Dimension;
import javax.swing.JFrame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class InventoryWardBrowser extends ModalJFrame {

    private static final long serialVersionUID = 1L;

    private JDateChooser jCalendarTo;
    private JDateChooser jCalendarFrom;
    private GregorianCalendar dateFrom = new GregorianCalendar();
    private GregorianCalendar dateTo = new GregorianCalendar();
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
    private String[] pColums = { MessageBundle.getMessage("angal.inventory.referenceshow"),
            MessageBundle.getMessage("angal.inventory.ward"), MessageBundle.getMessage("angal.common.date.txt"),
            MessageBundle.getMessage("angal.inventory.state"), MessageBundle.getMessage("angal.inventory.user") };
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

        ajustWidth();

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

    private JDateChooser getJCalendarFrom() {
        if (jCalendarFrom == null) {
            dateFrom.set(GregorianCalendar.HOUR_OF_DAY, 0);
            dateFrom.set(GregorianCalendar.MINUTE, 0);
            dateFrom.set(GregorianCalendar.SECOND, 0);

            jCalendarFrom = new JDateChooser(dateFrom.getTime()); // Calendar
            jCalendarFrom.setDateFormatString("dd/MM/yy"); //$NON-NLS-1$
            jCalendarFrom.addPropertyChangeListener("date", new PropertyChangeListener() { //$NON-NLS-1$

                public void propertyChange(PropertyChangeEvent evt) {
                    jCalendarFrom.setDate((Date) evt.getNewValue());
                    dateFrom.setTime((Date) evt.getNewValue());
                    dateFrom.set(GregorianCalendar.HOUR_OF_DAY, 0);
                    dateFrom.set(GregorianCalendar.MINUTE, 0);
                    dateFrom.set(GregorianCalendar.SECOND, 0);
                    jTableInventory.setModel(new InventoryBrowsingModel());
                }
            });
        }
        return jCalendarFrom;
    }

    private JDateChooser getJCalendarTo() {
        if (jCalendarTo == null) {
            dateTo.set(GregorianCalendar.HOUR_OF_DAY, 23);
            dateTo.set(GregorianCalendar.MINUTE, 59);
            dateTo.set(GregorianCalendar.SECOND, 59);
            jCalendarTo = new JDateChooser(dateTo.getTime()); // Calendar
            jCalendarTo.setDateFormatString("dd/MM/yy"); //$NON-NLS-1$
            jCalendarTo.addPropertyChangeListener("date", new PropertyChangeListener() { //$NON-NLS-1$

                public void propertyChange(PropertyChangeEvent evt) {
                    jCalendarTo.setDate((Date) evt.getNewValue());
                    dateTo.setTime((Date) evt.getNewValue());
                    dateTo.set(GregorianCalendar.HOUR_OF_DAY, 23);
                    dateTo.set(GregorianCalendar.MINUTE, 59);
                    dateTo.set(GregorianCalendar.SECOND, 59);
                    jTableInventory.setModel(new InventoryBrowsingModel());
                }
            });
        }
        return jCalendarTo;
    }

    private JLabel getJLabelTo() {
        if (jLabelTo == null) {
            jLabelTo = new JLabel();
            jLabelTo.setHorizontalAlignment(SwingConstants.RIGHT);
            jLabelTo.setText(MessageBundle.getMessage("angal.billbrowser.to")); //$NON-NLS-1$
        }
        return jLabelTo;
    }

    private JLabel getJLabelFrom() {
        if (jLabelFrom == null) {
            jLabelFrom = new JLabel();
            jLabelFrom.setHorizontalAlignment(SwingConstants.RIGHT);
            jLabelFrom.setText(MessageBundle.getMessage("angal.billbrowser.from")); //$NON-NLS-1$
        }
        return jLabelFrom;
    }

    private JButton getNewButton() {
        newButton = new JButton(MessageBundle.getMessage("angal.inventory.new"));
        newButton.setMnemonic(KeyEvent.VK_N);
        return newButton;
    }

    private JButton getViewButton() {
        viewButton = new JButton(MessageBundle.getMessage("angal.inventory.view"));
        viewButton.setMnemonic(KeyEvent.VK_V);
        return viewButton;
    }

    private JButton getUpdateButton() {
        updateButton = new JButton(MessageBundle.getMessage("angal.inventory.update"));
        updateButton.setMnemonic(KeyEvent.VK_M);
        return updateButton;
    }

    private JButton getDeleteButton() {
        deleteButton = new JButton(MessageBundle.getMessage("angal.inventory.delete"));
        deleteButton.setMnemonic(KeyEvent.VK_D);
        return deleteButton;
    }

    private JButton getCloseButton() {
        closeButton = new JButton(MessageBundle.getMessage("angal.inventory.close"));
        closeButton.setMnemonic(KeyEvent.VK_C);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        return closeButton;
    }

    /*
     * private JButton getInitialStocksButton() { initialStocksButton = new
     * JButton(MessageBundle.getMessage("angal.inventory.initialstocks"));
     * initialStocksButton.setMnemonic(KeyEvent.VK_N);
     * initialStocksButton.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent e) { WardInitialStocks wardInitialStocks = new
     * WardInitialStocks();
     * 
     * if (Param.bool("WITHMODALWINDOW")) {
     * wardInitialStocks.showAsModal(InventoryWardBrowser.this); } else {
     * wardInitialStocks.show(InventoryWardBrowser.this); } // } }); return
     * initialStocksButton; }
     */

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

    public String formatDateTime(GregorianCalendar time) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy"); //$NON-NLS-1$
        return format.format(time.getTime());
    }

    private void ajustWidth() {
        for (int i = 0; i < pColumwidth.length; i++) {
            jTableInventory.getColumnModel().getColumn(i).setMinWidth(pColumwidth[i]);
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
            stateLabel = new JLabel(MessageBundle.getMessage("angal.inventory.state"));
            stateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        }
        return stateLabel;
    }
}
