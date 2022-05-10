/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2021 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.medicalstock.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.medicalstock.model.Movement;
import org.isf.medstockmovtype.manager.MedicaldsrstockmovTypeBrowserManager;
import org.isf.medstockmovtype.model.MovementType;
import org.isf.menu.manager.Context;
import org.isf.supplier.manager.SupplierBrowserManager;
import org.isf.supplier.model.Supplier;
import org.isf.utils.db.NormalizeString;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.GoodDateTimeChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.RequestFocusListener;
import org.isf.utils.jobjects.TextPrompt;
import org.isf.utils.jobjects.TextPrompt.Show;
import org.isf.utils.time.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovStockMultipleCharging extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MovStockMultipleCharging.class);

	private static final int CODE_COLUMN_WIDTH = 100;
	private static final int UNITS = 0;
	private static final int PACKETS = 1;

	private JPanel mainPanel;
	private JTextField jTextFieldReference;
	private JTextField jTextFieldSearch;
	private JComboBox jComboBoxChargeType;
	private GoodDateChooser jDateChooser;
	private JComboBox jComboBoxSupplier;
	private JTable jTableMovements;
	private final String[] columnNames = {
      	MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.multiplecharging.qtypacket").toUpperCase(),
		MessageBundle.getMessage("angal.common.qty.txt").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.multiplecharging.unitpack").toUpperCase(),
		MessageBundle.getMessage("angal.common.total.txt").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotnumberabb").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.multiplecharging.cost").toUpperCase(),
		MessageBundle.getMessage("angal.common.total.txt").toUpperCase()
	};
	private final Class[] columnClasses = { String.class, String.class, Integer.class, Integer.class, String.class, Integer.class, String.class, String.class, BigDecimal.class, BigDecimal.class };
	private boolean[] columnEditable = { true, false, false, true, true, false, !GeneralData.AUTOMATICLOT_IN, true, true, false };
 	private int[] columnWidth = { 50, 100, 70, 50, 70, 50, 50, 80, 50, 80 };
	private boolean[] columnResizable = { false, true, false, false, false, false, false, false, false, false };
	private boolean[] columnVisible = { true, true, true, true, true, true, !GeneralData.AUTOMATICLOT_IN, true, GeneralData.LOTWITHCOST, GeneralData.LOTWITHCOST };
 	private int[] columnAlignment = { SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER,
			SwingConstants.CENTER, SwingConstants.RIGHT, SwingConstants.RIGHT };
	private boolean[] columnBold = { false, false, false, false, false, true, false, false, false, true };
	private Map<String, Medical> medicalMap;
	private List<Integer> units;
	private JTableModel model;
	private String[] qtyOption = new String[] {
			MessageBundle.getMessage("angal.medicalstock.multiplecharging.units"), //$NON-NLS-2$
			MessageBundle.getMessage("angal.medicalstock.multiplecharging.packets") //$NON-NLS-2$
	};
	private JComboBox comboBoxUnits = new JComboBox(qtyOption);
	private int optionSelected = UNITS;
	
	private MovStockInsertingManager movManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private MedicaldsrstockmovTypeBrowserManager medicaldsrstockmovTypeBrowserManager = Context.getApplicationContext().getBean(MedicaldsrstockmovTypeBrowserManager.class);
	private SupplierBrowserManager supplierBrowserManager = Context.getApplicationContext().getBean(SupplierBrowserManager.class);

	private boolean isAutomaticLot() {
		return GeneralData.AUTOMATICLOT_IN;
	}

	/**
	 * Create the dialog.
	 */
	public MovStockMultipleCharging(JFrame owner) {
		super(owner, true);
		initialize();
		initcomponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private void initialize() {
		List<Medical> medicals;
		try {
			medicals = medicalBrowsingManager.getMedicals();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
			medicals = null;
		}

		medicalMap = new HashMap<>();
		if (null != medicals) {
			for (Medical med : medicals) {
				String key = med.getProdCode();
				if (key == null || key.equals("")) {
					key = med.getType().getCode() + med.getDescription();
				}
				medicalMap.put(key, med);
			}
		}
		units = new ArrayList<>();
	}
	
	private void initcomponents() {
		setTitle(MessageBundle.getMessage("angal.medicalstock.stockmovement.title"));
		add(getJPanelHeader(), BorderLayout.NORTH);
		add(getJMainPanel(), BorderLayout.CENTER);
		add(getJButtonPane(), BorderLayout.SOUTH);
		setPreferredSize(new Dimension(800, 600));
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJPanelHeader() {
		JPanel headerPanel = new JPanel();
		getContentPane().add(headerPanel, BorderLayout.NORTH);
		GridBagLayout gblHeaderPanel = new GridBagLayout();
		gblHeaderPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gblHeaderPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gblHeaderPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gblHeaderPanel.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		headerPanel.setLayout(gblHeaderPanel);
		{
			JLabel jLabelDate = new JLabel(MessageBundle.getMessage("angal.common.date.txt")+":");
			GridBagConstraints gbcLabelDate = new GridBagConstraints();
			gbcLabelDate.anchor = GridBagConstraints.WEST;
			gbcLabelDate.insets = new Insets(5, 5, 5, 5);
			gbcLabelDate.gridx = 0;
			gbcLabelDate.gridy = 0;
			headerPanel.add(jLabelDate, gbcLabelDate);
		}
		{
			GridBagConstraints gbcDateChooser = new GridBagConstraints();
			gbcDateChooser.anchor = GridBagConstraints.WEST;
			gbcDateChooser.insets = new Insets(5, 0, 5, 5);
			gbcDateChooser.fill = GridBagConstraints.VERTICAL;
			gbcDateChooser.gridx = 1;
			gbcDateChooser.gridy = 0;
			headerPanel.add(getJDateChooser(), gbcDateChooser);
		}
		{
			JLabel jLabelReferenceNo = new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.referencenumberabb")+":"); //$NON-NLS-1$
			GridBagConstraints gbcLabelReferenceNo = new GridBagConstraints();
			gbcLabelReferenceNo.anchor = GridBagConstraints.EAST;
			gbcLabelReferenceNo.insets = new Insets(5, 0, 5, 5);
			gbcLabelReferenceNo.gridx = 2;
			gbcLabelReferenceNo.gridy = 0;
			headerPanel.add(jLabelReferenceNo, gbcLabelReferenceNo);
		}
		{
			jTextFieldReference = new JTextField();
			GridBagConstraints gbcTextFieldReference = new GridBagConstraints();
			gbcTextFieldReference.insets = new Insets(5, 0, 5, 0);
			gbcTextFieldReference.fill = GridBagConstraints.HORIZONTAL;
			gbcTextFieldReference.gridx = 3;
			gbcTextFieldReference.gridy = 0;
			headerPanel.add(jTextFieldReference, gbcTextFieldReference);
			jTextFieldReference.setColumns(10);
		}
		{
			JLabel jLabelChargeType = new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.chargetype")+":"); //$NON-NLS-1$
			GridBagConstraints gbcLabelChargeType = new GridBagConstraints();
			gbcLabelChargeType.anchor = GridBagConstraints.EAST;
			gbcLabelChargeType.insets = new Insets(0, 5, 5, 5);
			gbcLabelChargeType.gridx = 0;
			gbcLabelChargeType.gridy = 1;
			headerPanel.add(jLabelChargeType, gbcLabelChargeType);
		}
		{
			GridBagConstraints gbcComboBoxChargeType = new GridBagConstraints();
			gbcComboBoxChargeType.anchor = GridBagConstraints.WEST;
			gbcComboBoxChargeType.insets = new Insets(0, 0, 5, 5);
			gbcComboBoxChargeType.gridx = 1;
			gbcComboBoxChargeType.gridy = 1;
			headerPanel.add(getJComboBoxChargeType(), gbcComboBoxChargeType);
		}
		{
			JLabel jLabelSupplier = new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.supplier")+":"); //$NON-NLS-1$
			GridBagConstraints gbcLabelSupplier = new GridBagConstraints();
			gbcLabelSupplier.anchor = GridBagConstraints.WEST;
			gbcLabelSupplier.insets = new Insets(0, 5, 0, 5);
			gbcLabelSupplier.gridx = 0;
			gbcLabelSupplier.gridy = 3;
			headerPanel.add(jLabelSupplier, gbcLabelSupplier);
		}
		{
			GridBagConstraints gbcComboBoxSupplier = new GridBagConstraints();
			gbcComboBoxSupplier.anchor = GridBagConstraints.WEST;
			gbcComboBoxSupplier.insets = new Insets(0, 0, 0, 5);
			gbcComboBoxSupplier.gridx = 1;
			gbcComboBoxSupplier.gridy = 3;
			headerPanel.add(getJComboBoxSupplier(), gbcComboBoxSupplier);
		}
		return headerPanel;
	}

	private JPanel getJButtonPane() {
		JPanel buttonPane = new JPanel();
		{
			JButton deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			deleteButton.addActionListener(actionEvent -> {
				int row = jTableMovements.getSelectedRow();
				if (row > -1) {
					model.removeItem(row);
				}
			});
			buttonPane.add(deleteButton);
		}
		{
			JButton saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
			saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
			saveButton.addActionListener(actionEvent -> {
				if (!checkAndPrepareMovements()) {
					return;
				}
				if (!save()) {
					return;
				}
				dispose();
			});
			buttonPane.add(saveButton);
		}
		{
			JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
			buttonPane.add(cancelButton);
		}
		return buttonPane;
	}

	private JPanel getJMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel(new BorderLayout());
			mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			mainPanel.add(getJTextFieldSearch(), BorderLayout.NORTH);
			mainPanel.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return mainPanel;
	}

	private JScrollPane getJScrollPane() {
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(getJTable());
		scrollPane.setPreferredSize(new Dimension(400, 450));
		return scrollPane;
	}

	private JTable getJTable() {
		if (jTableMovements == null) {
			model = new JTableModel();
			jTableMovements = new JTable(model);
			jTableMovements.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTableMovements.setRowHeight(24);
			jTableMovements.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_DELETE) {
						int row = jTableMovements.getSelectedRow();
						model.removeItem(row);
					}
				}
			});

			for (int i = 0; i < columnNames.length; i++) {
				jTableMovements.getColumnModel().getColumn(i).setCellRenderer(new EnabledTableCellRenderer());
				jTableMovements.getColumnModel().getColumn(i).setMinWidth(columnWidth[i]);
				if (!columnResizable[i]) {
					jTableMovements.getColumnModel().getColumn(i).setResizable(columnResizable[i]);
					jTableMovements.getColumnModel().getColumn(i).setMaxWidth(columnWidth[i]);
				}
				if (!columnVisible[i]) {
					jTableMovements.getColumnModel().getColumn(i).setMinWidth(0);
					jTableMovements.getColumnModel().getColumn(i).setMaxWidth(0);
					jTableMovements.getColumnModel().getColumn(i).setWidth(0);
				}
			}

			TableColumn qtyOptionColumn = jTableMovements.getColumnModel().getColumn(4);
			qtyOptionColumn.setCellEditor(new DefaultCellEditor(comboBoxUnits));
			
			TableColumn costColumn = jTableMovements.getColumnModel().getColumn(8);
			costColumn.setCellRenderer(new DecimalFormatRenderer());
			
			TableColumn totalColumn = jTableMovements.getColumnModel().getColumn(9);
			totalColumn.setCellRenderer(new DecimalFormatRenderer());
			
			comboBoxUnits.setSelectedIndex(optionSelected);
		}
		return jTableMovements;
	}

	private JTextField getJTextFieldSearch() {
		if (jTextFieldSearch == null) {
			jTextFieldSearch = new JTextField();
			jTextFieldSearch.setPreferredSize(new Dimension(300, 30));
			jTextFieldSearch.setHorizontalAlignment(SwingConstants.LEFT);
			
			jTextFieldSearch.setColumns(10);
			TextPrompt suggestion = new TextPrompt(
					MessageBundle.getMessage("angal.medicalstock.typeacodeoradescriptionandpressenter"), //$NON-NLS-1$ 
					jTextFieldSearch, 
					Show.FOCUS_LOST); 
			{
				suggestion.setFont(new Font("Tahoma", Font.PLAIN, 14)); //$NON-NLS-1$
				suggestion.setForeground(Color.GRAY);
				suggestion.setHorizontalAlignment(SwingConstants.CENTER);
				suggestion.changeAlpha(0.5f);
				suggestion.changeStyle(Font.BOLD + Font.ITALIC);
			}
			jTextFieldSearch.addActionListener(actionEvent -> {
				String text = jTextFieldSearch.getText();
				Medical med;
				if (medicalMap.containsKey(text)) {
					// Medical found
					med = medicalMap.get(text);
				} else {

					med = chooseMedical(text.toLowerCase());
				}

				if (med != null) {

					// Quantity
					int qty = askQuantity(med);
					if (qty == 0) {
						return;
					}

					// Lot (PreparationDate && ExpiringDate)
					Lot lot;
					boolean isNewLot = false;
					if (isAutomaticLot()) {
						LocalDateTime preparationDate = LocalDateTime.now();
						LocalDateTime expiringDate = askExpiringDate();
						lot = new Lot("", preparationDate, expiringDate); //$NON-NLS-1$
						// Cost
						BigDecimal cost = new BigDecimal(0);
						if (GeneralData.LOTWITHCOST) {
							cost = askCost(qty);
							if (cost.compareTo(new BigDecimal(0)) == 0) {
								return;
							}
						}
						isNewLot = true;
						lot.setCost(cost);
					} else {
						do {
							lot = chooseLot(med);
							if (lot == null) {
								lot = askLot();
								if (lot == null) {
									return;
								}
								// Lot Cost
								BigDecimal cost = new BigDecimal(0);
								if (GeneralData.LOTWITHCOST) {
									cost = askCost(qty);
									if (cost.compareTo(new BigDecimal(0)) == 0) {
										return;
									}
								}
								isNewLot = true;
								lot.setCost(cost);
							}
						} while (lot == null);
					}

					// Date
					LocalDateTime date = jDateChooser.getDateStartOfDay();

					// RefNo
					String refNo = jTextFieldReference.getText().trim();

					Movement movement = new Movement(med, (MovementType) jComboBoxChargeType.getSelectedItem(), null, lot, date, qty, new Supplier(), refNo);
					model.addItem(movement, isNewLot);

					units.add(PACKETS);

					jTextFieldSearch.setText(""); //$NON-NLS-1$
					jTextFieldSearch.requestFocus();
				}
			});
		}
		return jTextFieldSearch;
	}

	private GoodDateChooser getJDateChooser() {
		if (jDateChooser == null) {
			jDateChooser = new GoodDateChooser(LocalDate.now());
		}
		return jDateChooser;
	}

	private JComboBox getJComboBoxChargeType() {
		if (jComboBoxChargeType == null) {
			jComboBoxChargeType = new JComboBox();
			List<MovementType> movTypes;
			try {
				movTypes = medicaldsrstockmovTypeBrowserManager.getMedicaldsrstockmovType();
			} catch (OHServiceException e) {
				movTypes = null;
				OHServiceExceptionUtil.showMessages(e);
			}
			if (null != movTypes) {
				for (MovementType movType : movTypes) {
					if (movType.getType().contains("+")) {
						jComboBoxChargeType.addItem(movType);
					}
				}
			}
		}
		return jComboBoxChargeType;
	}

	protected BigDecimal askCost(int qty) {
		double cost = 0.;
		do {
			String input = JOptionPane.showInputDialog(MovStockMultipleCharging.this, 
					MessageBundle.getMessage("angal.medicalstock.multiplecharging.unitcost"),  //$NON-NLS-1$
					0.);
			if (input != null) {
				try {
					cost = Double.parseDouble(input);
					if (cost < 0) {
						throw new NumberFormatException();
					} else if (cost == 0.) {
						double total = askTotalCost();
						//if (total == 0.) return;
						cost = total / qty;
					}
				} catch (NumberFormatException nfe) {
					MessageDialog.error(MovStockMultipleCharging.this, "angal.medicalstock.multiplecharging.pleaseinsertavalidvalue");
				}
			} else {
				return new BigDecimal(cost);
			}
		} while (cost == 0.);
		return new BigDecimal(cost);
	}
	
	protected double askTotalCost() {
		String input = JOptionPane.showInputDialog(MovStockMultipleCharging.this, 
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
				MessageDialog.error(MovStockMultipleCharging.this, "angal.medicalstock.multiplecharging.pleaseinsertavalidvalue");
			}
		}
		return total;
	}

	protected Lot askLot() {
		LocalDateTime preparationDate = LocalDateTime.now();
		LocalDateTime expiringDate = LocalDateTime.now();
		Lot lot = null;

		JTextField lotNameTextField = new JTextField(15);
		lotNameTextField.addAncestorListener(new RequestFocusListener());
		if (isAutomaticLot()) {
			lotNameTextField.setEnabled(false);
		}
		TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotid"), lotNameTextField);
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
					MovStockMultipleCharging.this, 
					panel, 
					MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotinformations"), //$NON-NLS-1$
					JOptionPane.OK_CANCEL_OPTION);
			
			if (ok == JOptionPane.OK_OPTION) {
				String lotName = lotNameTextField.getText();
				
				if (expireDateChooser.getDate().isBefore(preparationDateChooser.getDate())) {
					MessageDialog.error(MovStockMultipleCharging.this, "angal.medicalstock.multiplecharging.expirydatebeforepreparationdate");
				} 
				else if (expireDateChooser.getDate().isBefore(jDateChooser.getDate())) {
					MessageDialog.error(MovStockMultipleCharging.this, "angal.medicalstock.multiplecharging.expiringdateinthepastnotallowed");
				} else {
					expiringDate = expireDateChooser.getDateEndOfDay();
					preparationDate = preparationDateChooser.getDateStartOfDay();
					lot = new Lot(lotName, preparationDate, expiringDate);
				}
			} else {
				return null;
			}
		}
		while (lot == null);
		return lot;
	}
	
	protected Medical chooseMedical(String text) {
		List<Medical> medList = new ArrayList<>();
		for (Medical aMed : medicalMap.values()) {
			if (NormalizeString.normalizeContains(aMed.getDescription().toLowerCase(), text.toLowerCase())) {
				medList.add(aMed);
			}
		}
		Collections.sort(medList);
		Medical med = null;
		
		if (!medList.isEmpty()) {
			JTable medTable = new JTable(new StockMedModel(medList));
			medTable.getColumnModel().getColumn(0).setMaxWidth(CODE_COLUMN_WIDTH);
			medTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JPanel panel = new JPanel();
			panel.add(new JScrollPane(medTable));
			
			int ok = JOptionPane.showConfirmDialog(
					MovStockMultipleCharging.this, 
					panel, 
					MessageBundle.getMessage("angal.medicalstock.multiplecharging.chooseamedical"), //$NON-NLS-1$ 
					JOptionPane.YES_NO_OPTION);
			
			if (ok == JOptionPane.OK_OPTION) {
				int row = medTable.getSelectedRow();
				med = medList.get(row);
			}
			return med;
		}
		return null;
	}

	protected Lot chooseLot(Medical med) {
		List<Lot> lots;
		try {
			lots = movManager.getLotByMedical(med);
		} catch (OHServiceException e) {
			lots = new ArrayList<>();
			OHServiceExceptionUtil.showMessages(e);
		}
		Lot lot = null;
		if (!lots.isEmpty()) {
			JTable lotTable = new JTable(new StockMovModel(lots));
			lotTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.useanexistinglot")), BorderLayout.NORTH); //$NON-NLS-1$
			panel.add(new JScrollPane(lotTable), BorderLayout.CENTER);
			Object[] options = {
					MessageBundle.getMessage("angal.medicalstock.multiplecharging.selectedlot"), //$NON-NLS-1$
					MessageBundle.getMessage("angal.medicalstock.multiplecharging.newlot")}; //$NON-NLS-1$
						
			int row;
			do {
				int ok = JOptionPane.showOptionDialog(MovStockMultipleCharging.this, 
						panel, 
						MessageBundle.getMessage("angal.medicalstock.multiplecharging.existinglot"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION, 
						JOptionPane.QUESTION_MESSAGE, 
						null, 
						options, 
						options[0]);

				if (ok == JOptionPane.YES_OPTION) {
					row = lotTable.getSelectedRow();
					if (row != -1) {
						lot = lots.get(row);
					} else {
						MessageDialog.error(MovStockMultipleCharging.this, "angal.common.pleaseselectarow.msg");
					}
				} else {
					row = 0;
				}
				
			} while (row == -1);
		}
		return lot;
	}

	protected LocalDateTime askExpiringDate() {
		LocalDateTime date = LocalDateTime.now();
		GoodDateTimeChooser expireDateChooser = new GoodDateTimeChooser(date);
		JPanel panel = new JPanel(new GridLayout(1, 2));
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate"))); //$NON-NLS-1$
		panel.add(expireDateChooser);

		int ok = JOptionPane.showConfirmDialog(MovStockMultipleCharging.this, panel, 
				MessageBundle.getMessage("angal.medicalstock.multiplecharging.expiringdate"), //$NON-NLS-1$
				JOptionPane.OK_CANCEL_OPTION); 

		if (ok == JOptionPane.OK_OPTION) {
			date = expireDateChooser.getLocalDateTime();
		}
		return date;
	}

	protected int askQuantity(Medical med) {
		StringBuilder title = new StringBuilder(MessageBundle.getMessage("angal.common.quantity.txt"));
		StringBuilder message = new StringBuilder(med.toString());
		String prodCode = med.getProdCode();
		if (prodCode != null && !prodCode.equals("")) {
			title.append(" ").append(MessageBundle.getMessage("angal.common.code.txt"));
			title.append(": ").append(prodCode);
		} else { 
			title.append(": ");
		}
		int qty = 0;
		do {
			String quantity = JOptionPane.showInputDialog(MovStockMultipleCharging.this, 
					message.toString(), 
					title.toString(),
					JOptionPane.QUESTION_MESSAGE);
			if (quantity != null) {
				try {
					qty = Integer.parseInt(quantity);
					if (qty == 0) {
						return 0;
					}
					if (qty < 0) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException nfe) {
					MessageDialog.error(MovStockMultipleCharging.this, "angal.medicalstock.multiplecharging.pleaseinsertavalidvalue");
					qty = 0;
				}
			} else {
				return qty;
			}
		} while (qty == 0);
		return qty;
	}

	private JComboBox getJComboBoxSupplier() {
		if (jComboBoxSupplier == null) {
			jComboBoxSupplier = new JComboBox();
			jComboBoxSupplier.addItem(""); //$NON-NLS-1$
			List<Supplier> suppliers = null;
			try {
				suppliers = (List<Supplier>) supplierBrowserManager.getList();
            } catch (OHServiceException e) {
                OHServiceExceptionUtil.showMessages(e);
            }
            if (suppliers != null) {
                for (Supplier sup : suppliers) {
                    jComboBoxSupplier.addItem(sup);
                }
            }
		}
		return jComboBoxSupplier;
	}

	public class JTableModel extends AbstractTableModel {

		private List<Movement> movements;
		private List<Boolean> newLots;

		private static final long serialVersionUID = 1L;

		public JTableModel() {
			movements = new ArrayList<>();
			newLots = new ArrayList<>();
		}
		
		public List<Movement> getMovements() {
			return movements;
		}

		public void removeItem(int row) {
			movements.remove(row);
			units.remove(row);
			newLots.remove(row);
			fireTableDataChanged();
		}

		public void addItem(Movement movement, Boolean isNewLot) {
			movements.add(movement);
			newLots.add(isNewLot);
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return movements.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnClasses[columnIndex];
		}

		@Override
		public boolean isCellEditable(int r, int c) {
			if (c == 6 || c == 7 || c == 8) {
				return newLots.get(r);
			}
			return columnEditable[c];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int r, int c) {
			Movement movement = movements.get(r);
			Medical medical = movement.getMedical();
			Lot lot = movement.getLot();
			String lotName = lot.getCode();
			int qty = movement.getQuantity();
			int ppp = medical.getPcsperpck() == 0 ? 1 : medical.getPcsperpck();
			int option = units.get(r);
			int total = option == UNITS ? qty : ppp * qty;
			BigDecimal cost = lot.getCost();
			if (c == -1) {
				return movement;
			} else if (c == 0) {
				return medical.getProdCode();
			} else if (c == 1) {
				return medical.getDescription();
			} else if (c == 2) {
				return ppp;
			} else if (c == 3) {
				return qty;
			} else if (c == 4) {
				return qtyOption[option];
			} else if (c == 5) {
				return total;
			} else if (c == 6) {
				return lotName.equals("") ? "AUTO" : lotName; //$NON-NLS-1$ //$NON-NLS-2$
			} else if (c == 7) {
				return TimeTools.formatDateTime(lot.getDueDate(), DATE_FORMAT_DD_MM_YYYY);
			} else if (c == 8) {
				return cost;
			} else if (c == 9) {
				if (cost != null) {
					return cost.multiply(new BigDecimal(total));
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int,
		 * int)
		 */
		@Override
		public void setValueAt(Object value, int r, int c) {
			Movement movement = movements.get(r);
			Lot lot = movement.getLot();
			if (c == 0) {
				String key = String.valueOf(value);
				if (medicalMap.containsKey(key)) {
					movement.setMedical(medicalMap.get(key));
					movements.set(r, movement);
				}
			} else if (c == 3) {
				movement.setQuantity((Integer) value);
			} else if (c == 4) {
				int newOption = 0;
				if (value == qtyOption[1]) {
					newOption = 1;
				}
				units.set(r, newOption);
			} else if (c == 6) {
				lot.setCode((String) value);
			} else if (c == 7) {
				try {
					LocalDateTime date = TimeTools.parseDate((String) value, DATE_FORMAT_DD_MM_YYYY, true);
					lot.setDueDate(date);
				} catch (Exception exception) {
					LOGGER.error(exception.getMessage(), exception);
				}
			} else if (c == 8) {
				lot.setCost((BigDecimal) value);
			}
			movements.set(r, movement);
			fireTableDataChanged();
		}
	}
	
	private boolean checkAndPrepareMovements() {
		boolean ok = true;

		List<Movement> movements = model.getMovements();
		if (movements.isEmpty()) {
			MessageDialog.error(MovStockMultipleCharging.this, "angal.medicalstock.multiplecharging.noelementtosave");
			return false;
		}
		
		// Check supplier
		Object supplier = jComboBoxSupplier.getSelectedItem();
		if (supplier == null || supplier instanceof String) {
			MessageDialog.error(MovStockMultipleCharging.this, "angal.medicalstock.multiplecharging.pleaseselectasupplier.msg");
			return false;
		}

		LocalDate thisDate = jDateChooser.getDate();
		
		// Check and set all movements
		for (int i = 0; i < movements.size(); i++) {
			Movement mov = movements.get(i);
			int option = units.get(i);
			mov.setDate(thisDate.atStartOfDay());
			mov.setRefNo(jTextFieldReference.getText());
			mov.setQuantity(calcTotal(mov, option));
			mov.setType((MovementType) jComboBoxChargeType.getSelectedItem());
			mov.setSupplier(((Supplier) jComboBoxSupplier.getSelectedItem()));
		}
		return ok;
	}

	private int calcTotal(Movement mov, int option) {
		Medical medical = mov.getMedical();
		int qty = mov.getQuantity();
		int ppp = medical.getPcsperpck() == 0 ? 1 : medical.getPcsperpck();
		int total = option == UNITS ? qty : ppp * qty;

		return total;
	}
	
	private boolean save() {
		boolean ok = true;
		List<Movement> movements = model.getMovements();
		try {
			movManager.newMultipleChargingMovements(movements, movements.get(0).getRefNo());
		} catch (OHServiceException e) {
			ok = false;
			OHServiceExceptionUtil.showMessages(e);
		} 
		return ok;
	}
	
	class EnabledTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setHorizontalAlignment(columnAlignment[column]);
			if (!table.isCellEditable(row, column)) {
				cell.setBackground(Color.LIGHT_GRAY);
			} else {
				cell.setBackground(Color.WHITE);
			}
			if (columnBold[column]) { 
				cell.setFont(new Font(null, Font.BOLD, 12));
			}
			return cell;
		}
	}
	
	class DecimalFormatRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		private final DecimalFormat formatter = new DecimalFormat("#,##0.00"); //$NON-NLS-1$

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			// First format the cell value as required
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value != null) {
				value = formatter.format(value);
			}
			setHorizontalAlignment(columnAlignment[column]);
			if (!table.isCellEditable(row, column)) {
				cell.setBackground(Color.LIGHT_GRAY);
			} else {
				cell.setBackground(Color.WHITE);
			}
			if (columnBold[column]) { 
				cell.setFont(new Font(null, Font.BOLD, 12));
			}
			// And pass it on to parent class
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}
	
	class StockMovModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private List<Lot> lotList;

		public StockMovModel(List<Lot> lots) {
			lotList = lots;
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
			if (c == 0) {
				return MessageBundle.getMessage("angal.medicalstock.lotid").toUpperCase();
			}
			if (c == 1) {
				return MessageBundle.getMessage("angal.medicalstock.prepdate").toUpperCase();
			}
			if (c == 2) {
				return MessageBundle.getMessage("angal.medicalstock.duedate").toUpperCase();
			}
			if (c == 3) {
				return MessageBundle.getMessage("angal.common.quantity.txt").toUpperCase();
			}
			if (GeneralData.LOTWITHCOST) {
				if (c == 4) {
					return MessageBundle.getMessage("angal.medicalstock.multiplecharging.cost").toUpperCase();
				}
			}
			return ""; //$NON-NLS-1$
		}

		@Override
		public int getColumnCount() {
			if (GeneralData.LOTWITHCOST) {
				return 5;
			}
			return 4;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Lot lot = lotList.get(r);
			if (c == -1) {
				return lot;
			} else if (c == 0) {
				return lot.getCode();
			} else if (c == 1) {
				return TimeTools.formatDateTime(lot.getPreparationDate(), DATE_FORMAT_DD_MM_YYYY);
			} else if (c == 2) {
				return TimeTools.formatDateTime(lot.getDueDate(), DATE_FORMAT_DD_MM_YYYY);
			} else if (c == 3) {
				return lot.getMainStoreQuantity();
			} else if (c == 4) {
				return lot.getCost();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
	
	class StockMedModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private List<Medical> medList;

		public StockMedModel(List<Medical> meds) {
			medList = meds;
		}

		@Override
		public int getRowCount() {
			if (medList == null) {
				return 0;
			}
			return medList.size();
		}

		@Override
		public String getColumnName(int c) {
			if (c == 0) {
				return MessageBundle.getMessage("angal.common.code.txt").toUpperCase();
			}
			if (c == 1) {
				return MessageBundle.getMessage("angal.common.description.txt").toUpperCase();
			}
			return ""; //$NON-NLS-1$
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Medical med = medList.get(r);
			if (c == -1) {
				return med;
			} else if (c == 0) {
				return med.getProdCode();
			} else if (c == 1) {
				return med.getDescription();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

}
