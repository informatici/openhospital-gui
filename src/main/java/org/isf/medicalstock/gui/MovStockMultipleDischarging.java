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
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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
import org.isf.utils.db.NormalizeString;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.TextPrompt;
import org.isf.utils.jobjects.TextPrompt.Show;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.isf.xmpp.gui.CommunicationFrame;
import org.isf.xmpp.manager.Interaction;

public class MovStockMultipleDischarging extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final int CODE_COLUMN_WIDTH = 100;
	private static final int UNITS = 0;
	private static final int PACKETS = 1;

	private JPanel mainPanel;
	private JTextField jTextFieldReference;
	private JTextField jTextFieldSearch;
	private JComboBox jComboBoxDischargeType;
	private GoodDateTimeChooser jDateChooser;
	private JComboBox jComboBoxDestination;
	private JTable jTableMovements;
	private final String[] columnNames = {
		MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
		MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.multipledischarging.unitpack").toUpperCase(),
		MessageBundle.getMessage("angal.common.qty.txt").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.multipledischarging.unitpack").toUpperCase(),
		MessageBundle.getMessage("angal.common.total.txt").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.multipledischarging.lotnumberabb").toUpperCase(),
		MessageBundle.getMessage("angal.medicalstock.multipledischarging.expiringdate").toUpperCase()
	};
	private final Class[] columnClasses = { String.class, String.class, Integer.class, Integer.class, String.class, Integer.class, String.class, String.class};
	private boolean[] columnEditable = { false, false, false, false, true, false, false, false};
	private int[] columnWidth = { 50, 100, 70, 50, 70, 50, 100, 80};
	private boolean[] columnResizable = { false, true, false, false, false, false, false, false};
	private boolean[] columnVisible = { true, true, true, true, true, true, !GeneralData.AUTOMATICLOT_OUT, !GeneralData.AUTOMATICLOT_OUT };
 	private int[] columnAlignment = { SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER,
			SwingConstants.CENTER};
	private boolean[] columnBold = { false, false, false, false, false, true, false, false};
	private Map<String, Medical> medicalMap;
	private List<Integer> units;
	private List<Integer> quantities;
	private JTableModel model;
	private String[] qtyOption = new String[] {
			MessageBundle.getMessage("angal.medicalstock.multipledischarging.units"), //$NON-NLS-1$
			MessageBundle.getMessage("angal.medicalstock.multipledischarging.packets") //$NON-NLS-1$
	}; 
	private int optionSelected = UNITS;
	private JComboBox comboBoxUnits = new JComboBox(qtyOption);
	private JComboBox shareWith = null;
	private Interaction share;
	private List<Medical> pool = new ArrayList<>();
	
	private MovStockInsertingManager movManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private MedicaldsrstockmovTypeBrowserManager medicaldsrstockmovTypeBrowserManager = Context.getApplicationContext().getBean(MedicaldsrstockmovTypeBrowserManager.class);

	private boolean isAutomaticLot() {
		return GeneralData.AUTOMATICLOT_OUT;
	}

	private boolean isXmpp() {
		return GeneralData.XMPPMODULEENABLED;
	}
	
	/**
	 * Create the dialog.
	 */
	public MovStockMultipleDischarging(JFrame owner) {
		super(owner, true);
		initialize();
		initcomponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
		setLocationRelativeTo(null);
	}

	private void initialize() {

		List<Medical> medicals;
		try {
			medicals = medicalBrowsingManager.getMedicals();
		} catch (OHServiceException e) {
			medicals = null;
			OHServiceExceptionUtil.showMessages(e);
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
		add(getJButtonPanel(), BorderLayout.SOUTH);
		setPreferredSize(new Dimension(800, 600));
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJButtonPanel() {

		JPanel buttonPanel = new JPanel();
		{
			JButton deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			deleteButton.addActionListener(actionEvent -> {
				int row = jTableMovements.getSelectedRow();
				if (row > -1) {
					model.removeItem(row);
				}
			});
			buttonPanel.add(deleteButton);
		}
		{
			JButton saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
			saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
			saveButton.addActionListener(actionEvent -> {
				if (!checkAndPrepareMovements()) {
					return;
				}
				if (!save()) {
					rollBackMovements();
					return;
				}
				dispose();
			});
			buttonPanel.add(saveButton);
		}
		{
			JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
			buttonPanel.add(cancelButton);
		}
		{
			if (isXmpp()) {
				shareWith = getShareUser();
				shareWith.setEnabled(false);
				buttonPanel.add(shareWith);
			}
		}

		return buttonPanel;
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

					med = chooseMedical(text);
				}

				if (med != null) {

					if (isAutomaticLot() && isMedicalPresent(med)) {
						return;
					}

					if (!isAvailable(med)) {
						return;
					}

					// Quantity
					int qty = askQuantity(med);
					if (qty == 0) {
						return;
					}

					// Lot (PreparationDate && ExpiringDate)
					List<Lot> lots;
					try {
						lots = movManager.getLotByMedical(med);
					} catch (OHServiceException e1) {
						lots = null;
						OHServiceExceptionUtil.showMessages(e1);
					}
					Lot lot;
					if (!isAutomaticLot()) {
						lot = chooseLot(lots, qty);
						if (lot == null) {
							return;
						}
					} else {
						lot = new Lot("", null, null); //$NON-NLS-1$
					}

					// Date
					LocalDateTime date = jDateChooser.getLocalDateTime();

					// RefNo
					String refNo = jTextFieldReference.getText();

					Movement movement = new Movement(med, (MovementType) jComboBoxDischargeType.getSelectedItem(), null, lot, date, qty, null, refNo);
					if (med.getPcsperpck() > 1) {
						model.addItem(movement, PACKETS);
					} else {
						model.addItem(movement, UNITS);
					}

					jTextFieldSearch.setText(""); //$NON-NLS-1$
					jTextFieldSearch.requestFocus();
				}
			});
		}
		return jTextFieldSearch;
	}

	protected boolean isAvailable(Medical med) {
		if (med.getTotalQuantity() == 0) {
			StringBuilder message = new StringBuilder()
				.append(MessageBundle.getMessage("angal.medicalstock.multipledischarging.outofstock")) //$NON-NLS-1$
				.append("\n") //$NON-NLS-1$
				.append(med.getDescription()); //$NON-NLS-1$
			JOptionPane.showMessageDialog(MovStockMultipleDischarging.this, message.toString());
			return false;
		}
		return true;
	}

	private boolean isMedicalPresent(Medical med) {
		List<Movement> movements = model.getMovements();
		for (Movement mov : movements) {
			if (mov.getMedical() == med) {
				StringBuilder message = new StringBuilder()
					.append(MessageBundle.getMessage("angal.medicalstock.multipledischarging.alreadyinthisform")) //$NON-NLS-1$
					.append("\n") //$NON-NLS-1$
					.append(med.getDescription()); //$NON-NLS-1$
				JOptionPane.showMessageDialog(MovStockMultipleDischarging.this, message.toString());
				return true;
			}
		}
		return false;
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
			comboBoxUnits.setSelectedIndex(optionSelected);
		}
		return jTableMovements;
	}

	private JPanel getJPanelHeader() {
		JPanel headerPanel = new JPanel();
		GridBagLayout gblHeaderPanel = new GridBagLayout();
		gblHeaderPanel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gblHeaderPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gblHeaderPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gblHeaderPanel.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		headerPanel.setLayout(gblHeaderPanel);
		{
			JLabel jLabelDate = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
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
			JLabel jLabelReferenceNo = new JLabel(MessageBundle.getMessage("angal.medicalstock.multipledischarging.referencenumber")); //$NON-NLS-1$
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
			JLabel jLabelChargeType = new JLabel(MessageBundle.getMessage("angal.medicalstock.multipledischarging.dischargetype")); //$NON-NLS-1$
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
			JLabel jLabelSupplier = new JLabel(MessageBundle.getMessage("angal.medicalstock.multipledischarging.destination")); //$NON-NLS-1$
			GridBagConstraints gbcLabelSupplier = new GridBagConstraints();
			gbcLabelSupplier.anchor = GridBagConstraints.WEST;
			gbcLabelSupplier.insets = new Insets(0, 5, 0, 5);
			gbcLabelSupplier.gridx = 0;
			gbcLabelSupplier.gridy = 3;
			headerPanel.add(jLabelSupplier, gbcLabelSupplier);
		}
		{
			GridBagConstraints gbcComboBoxDestination = new GridBagConstraints();
			gbcComboBoxDestination.anchor = GridBagConstraints.WEST;
			gbcComboBoxDestination.insets = new Insets(0, 0, 0, 5);
			gbcComboBoxDestination.gridx = 1;
			gbcComboBoxDestination.gridy = 3;
			headerPanel.add(getJComboBoxDestination(), gbcComboBoxDestination);
		}
		return headerPanel;
	}

	private JComboBox getShareUser() {
		share = new Interaction();
		Collection<String> contacts = share.getContactOnline();
		contacts.add(MessageBundle.getMessage("angal.medicalstock.multipledischarging.sharealertwithnobody")); //$NON-NLS-1$
		shareWith = new JComboBox(contacts.toArray());
		shareWith.setSelectedItem(MessageBundle.getMessage("angal.medicalstock.multipledischarging.sharealertwithnobody")); //$NON-NLS-1$

		return shareWith;
	}
	
	private GoodDateTimeChooser getJDateChooser() {
		if (jDateChooser == null) {
			jDateChooser = new GoodDateTimeChooser(LocalDateTime.now());
		}
		return jDateChooser;
	}

	private JComboBox getJComboBoxChargeType() {
		if (jComboBoxDischargeType == null) {
			jComboBoxDischargeType = new JComboBox();
			List<MovementType> movTypes;
			try {
				movTypes = medicaldsrstockmovTypeBrowserManager.getMedicaldsrstockmovType();
			} catch (OHServiceException e) {
				movTypes = null;
				OHServiceExceptionUtil.showMessages(e);
			}
			if (null != movTypes) {
				for (MovementType movType : movTypes) {
					if (movType.getType().contains("-")) {
						jComboBoxDischargeType.addItem(movType);
					}
				}
			}
		}
		return jComboBoxDischargeType;
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
			
			int ok = JOptionPane.showConfirmDialog(MovStockMultipleDischarging.this, 
					panel, 
					MessageBundle.getMessage("angal.medicalstock.multipledischarging.chooseamedical"), //$NON-NLS-1$ 
					JOptionPane.YES_NO_OPTION); 
			
			if (ok == JOptionPane.OK_OPTION) {
				int row = medTable.getSelectedRow();
				if (row < 0) {
					return null;
				}
				med = medList.get(row);
			}
			return med;
		}
		return null;
	}

	protected Lot chooseLot(List<Lot> lots, double qty) {
		Lot lot = null;
		if (!lots.isEmpty()) {
			stripeLots(lots);
			
			JTable lotTable = new JTable(new StockMovModel(lots));
			lotTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multipledischarging.selectalot")), BorderLayout.NORTH); //$NON-NLS-1$
			panel.add(new JScrollPane(lotTable), BorderLayout.CENTER);
			
			do {
				int ok = JOptionPane.showConfirmDialog(MovStockMultipleDischarging.this, 
						panel, 
						MessageBundle.getMessage("angal.medicalstock.multipledischarging.lotinformations"), //$NON-NLS-1$ 
						JOptionPane.OK_CANCEL_OPTION);
	
				if (ok == JOptionPane.OK_OPTION) {
					int row = lotTable.getSelectedRow();
					if (row != -1) {
						lot = lots.get(row);
					} else {
						return null;
					}
				} else {
					return null;
				}
				
				if (!checkQuantityInLot(lot, qty)) {
					lot = null;
				} else {
					return lot;
				}
			} while (lot == null);
		} 
		return lot;
	}
	
	private boolean checkQuantityInLot(Lot lot, double qty) {
		double lotQty = lot.getMainStoreQuantity();
		if (qty > lotQty) {
			MessageDialog.error(MovStockMultipleDischarging.this, "angal.medicalstock.movementquantityisgreaterthanthequantityof.msg");
			return false;
		} 
		return true;
	}

	private void stripeLots(List<Lot> lots) {
		if (!lots.isEmpty()) {
			List<Movement> movements = model.getMovements();
			ListIterator<Lot> lotIterator = lots.listIterator();
			while (lotIterator.hasNext()) {
				Lot aLot = (Lot) lotIterator.next();
				for (Movement mov : movements) {
					if (aLot.getCode().equals(mov.getLot().getCode())) {
						int aLotQty = aLot.getMainStoreQuantity();
						int newQty = aLotQty - mov.getQuantity();
						if (newQty == 0) {
							lotIterator.remove();
						} else {
							aLot.setMainStoreQuantity(newQty);
							lotIterator.set(aLot);
						}
					}
				}
			}
		}
	}

	private boolean checkQuantityAllMovements(Medical med, double qty) {
		double totalQty = med.getTotalQuantity();
		double newTotalQty;
		
		// update remaining quantity with already inserted movements
		List<Movement> movements = model.getMovements();
		double usedQty = 0;
		for (int i = 0; i < movements.size(); i++) {
			Movement mov = movements.get(i);
			if (mov.getMedical() == med) {
				usedQty += calcTotal(mov, units.get(i));
			}
		}
		
		newTotalQty = totalQty - usedQty;
		return checkQuantity(med, newTotalQty, qty);
	}
	
	private boolean checkQuantityInMovement(Movement movement, double qty) {
		Medical med = movement.getMedical();
		double totalQty = med.getTotalQuantity();
		double newTotalQty;
		
		// update remaining quantity with already inserted movements 
		// but not the current one
		List<Movement> movements = model.getMovements();
		double usedQty = 0;
		for (int i = 0; i < movements.size(); i++) {
			Movement mov = movements.get(i);
			if (mov.getCode() == movement.getCode()) {
				continue;
			}
			if (mov.getMedical() == med) {
				usedQty += calcTotal(mov, units.get(i));
			}
		}
		
		newTotalQty = totalQty - usedQty;
		if (!isAutomaticLot() && !checkQuantityInLot(movement.getLot(), qty)) {
			return false;
		}
		return checkQuantity(med, newTotalQty, qty);
	}
	
	private boolean checkQuantity(Medical med, double totalQty, double qty) {
		double criticalLevel = med.getMinqty();
		if (qty > totalQty) {
			StringBuilder message = new StringBuilder();
			message.append(MessageBundle.getMessage("angal.medicalstock.multipledischarging.thequantityisnotavailable")) //$NON-NLS-1$
				.append("\n") //$NON-NLS-1$
				.append(MessageBundle.getMessage("angal.medicalstock.multipledischarging.lyinginstock")) //$NON-NLS-1$
				.append(totalQty);
			JOptionPane.showMessageDialog(MovStockMultipleDischarging.this, message.toString());
			return false;
		}
		
		if (totalQty - qty < criticalLevel) {
			StringBuilder message = new StringBuilder();
			message.append(MessageBundle.getMessage("angal.medicalstock.multipledischarging.youaregoingundercriticalevel")) //$NON-NLS-1$
				.append(" (") //$NON-NLS-1$
				.append(criticalLevel)
				.append(") ") //$NON-NLS-1$
				.append(MessageBundle.getMessage("angal.medicalstock.multipledischarging.proceed")); //$NON-NLS-1$
			int ok = JOptionPane.showConfirmDialog(MovStockMultipleDischarging.this, message.toString());
			
			if (ok != JOptionPane.OK_OPTION) {
				return false;
			} else {
				if (isXmpp()) {
					shareWith.setEnabled(true);
					pool.add(med);
				}
				return true;
			}
		}
		return true;
	}
	
	protected int askQuantity(Medical med) {
		double totalQty = med.getTotalQuantity();
		
		// update remaining quantity with already inserted movements
		List<Movement> movements = model.getMovements();
		double usedQty = 0;
		for (Movement mov : movements) {
			if (mov.getMedical() == med) {
				usedQty+=mov.getQuantity();
			}
		}
		totalQty = totalQty - usedQty;
		
		StringBuilder message = new StringBuilder();
		message.append(med)
			.append("\n") //$NON-NLS-1$
			.append(MessageBundle.getMessage("angal.medicalstock.multipledischarging.lyinginstock")) //$NON-NLS-1$
			.append(totalQty); //$NON-NLS-1$
		
		StringBuilder title = new StringBuilder(MessageBundle.getMessage("angal.common.quantity.txt"));
		String prodCode = med.getProdCode();
		if (prodCode != null && !prodCode.equals("")) { //$NON-NLS-1$
			title.append(" ") //$NON-NLS-1$
			.append(MessageBundle.getMessage("angal.common.code.txt"))
			.append(": ") //$NON-NLS-1$
			.append(prodCode);
		} else { 
			title.append(": "); //$NON-NLS-1$
		}
		int qty = 0;
		do {
			String quantity = JOptionPane.showInputDialog(MovStockMultipleDischarging.this, 
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
					if (!checkQuantityAllMovements(med, qty)) {
						qty = 0;
					}
				} catch (NumberFormatException nfe) {
					MessageDialog.error(MovStockMultipleDischarging.this, "angal.medicalstock.multipledischarging.pleaseinsertavalidvalue");
					qty = 0;
				}
			} else {
				return qty;
			}
		} while (qty == 0);
		
		return qty;
	}

	private JComboBox getJComboBoxDestination() {
		if (jComboBoxDestination == null) {
			jComboBoxDestination = new JComboBox();
			jComboBoxDestination.addItem(""); //$NON-NLS-1$
			WardBrowserManager wardMan = Context.getApplicationContext().getBean(WardBrowserManager.class);
			List<Ward> wards;
			try {
				wards = wardMan.getWards();
			} catch (OHServiceException e) {
				wards = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			for (Ward ward : wards) {
				if (GeneralData.INTERNALPHARMACIES) {
					if (ward.isPharmacy()) {
						jComboBoxDestination.addItem(ward);
					}
				} else {
					jComboBoxDestination.addItem(ward);
				}
			}
		}
		return jComboBoxDestination;
	}

	private int calcTotal(Movement mov, int option) {
		Medical medical = mov.getMedical();
		int qty = mov.getQuantity();
		int ppp = medical.getPcsperpck() == 0 ? 1 : medical.getPcsperpck();
		int total = option == UNITS ? qty : ppp * qty;

		return total;
	}

	public class JTableModel extends AbstractTableModel {

		private List<Movement> movements;

		private static final long serialVersionUID = 1L;

		public JTableModel() {
			movements = new ArrayList<>();
		}
		
		public List<Movement> getMovements() {
			return movements;
		}

		public void removeItem(int row) {
			pool.remove(movements.get(row).getMedical());
			movements.remove(row);
			units.remove(row);
			fireTableDataChanged();
		}

		public void addItem(Movement movement, Integer unit) {
			movements.add(movement);
			units.add(unit);
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
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnEditable[columnIndex];
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
			int ppp = medical.getPcsperpck();
			int option = units.get(r);
			int total = calcTotal(movement, option);
			if (c == -1) {
				return movement;
			} else if (c == 0) {
				return medical.getProdCode();
			} else if (c == 1) {
				return medical.getDescription();
			} else if (c == 2) {
				return ppp == 0 ? 1 : ppp;
			} else if (c == 3) {
				return qty;
			} else if (c == 4) {
				return qtyOption[option];
			} else if (c == 5) {
				return total;
			} else if (c == 6) {
				return lotName.equals("") ? "AUTO" : lotName; //$NON-NLS-1$ //$NON-NLS-2$
			} else if (c == 7) {
				if (lot.getDueDate() != null) {
					return TimeTools.formatDateTime(lot.getDueDate(), DATE_FORMAT_DD_MM_YYYY);
				} else {
					return "AUTO"; //$NON-NLS-1$
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
		 */
		@Override
		public void setValueAt(Object value, int r, int c) {
			Movement movement = movements.get(r);
			if (c == 4) {
				int newOption = 0;
				if (qtyOption[1].equals(value)) {
					newOption = 1;
				}
				int total = calcTotal(movement, newOption);
				if (checkQuantityInMovement(movement, total)) {
					units.set(r, newOption);
				}
			}
			movements.set(r, movement);
			fireTableDataChanged();
		}
	}
	
	private boolean checkAndPrepareMovements() {
		boolean ok = true;
		quantities = new ArrayList<>();
		
		List<Movement> movements = model.getMovements();
		if (movements.isEmpty()) {
			MessageDialog.error(MovStockMultipleDischarging.this, "angal.medicalstock.multipledischarging.noelementtosave");
			return false;
		}
		
		// Check destination
		Object ward = jComboBoxDestination.getSelectedItem();
		if (ward instanceof String) {
			MessageDialog.error(MovStockMultipleDischarging.this, "angal.medicalstock.multipledischarging.pleaseselectaward.msg");
			return false;
		}
		
		// Check the Date
		LocalDateTime thisDate = jDateChooser.getLocalDateTime();
		
		// Check and set all movements
		for (int i = 0; i < movements.size(); i++) {
			Movement mov = movements.get(i);
			int option = units.get(i);
			mov.setWard((Ward) jComboBoxDestination.getSelectedItem());
			mov.setDate(thisDate);
			mov.setRefNo(jTextFieldReference.getText());
			quantities.add(mov.getQuantity());
			mov.setQuantity(calcTotal(mov, option));
			mov.setType((MovementType) jComboBoxDischargeType.getSelectedItem());
		}
		return ok;
	}
	
	private void rollBackMovements() {
		List<Movement> movements = model.getMovements();

		// Set back changed quantities
		for (int i = 0; i < movements.size(); i++) {
			Movement mov = movements.get(i);
			mov.setQuantity(quantities.get(i));
		}
	}
	
	private boolean save() {
		boolean ok = true;
		List<Movement> movements = model.getMovements();
		try {
			movManager.newMultipleDischargingMovements(movements, movements.get(0).getRefNo());

			if (isXmpp()) {
				if (shareWith.isEnabled() && (!(((String) shareWith.getSelectedItem())
						.equals(MessageBundle.getMessage("angal.medicalstock.multipledischarging.sharealertwithnobody"))))) { //$NON-NLS-1$
					CommunicationFrame frame = (CommunicationFrame) CommunicationFrame.getFrame();
					for (Medical med : pool) {
						frame.sendMessage(
								MessageBundle.getMessage("angal.medicalstock.multipledischarging.alert") + //$NON-NLS-1$ 
										med.getDescription() +
										MessageBundle.getMessage("angal.medicalstock.multipledischarging.isabouttoend"), //$NON-NLS-1$
								(String) shareWith.getSelectedItem(),
								false);
					}
				}
			}

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
			if (!columnEditable[column]) {
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
			return ""; //$NON-NLS-1$
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Object getValueAt(int r, int c) {
			if (c == -1) {
				return lotList.get(r);
			} else if (c == 0) {
				return lotList.get(r).getCode();
			} else if (c == 1) {
				return TimeTools.formatDateTime(lotList.get(r).getPreparationDate(), DATE_FORMAT_DD_MM_YYYY);
			} else if (c == 2) {
				return TimeTools.formatDateTime(lotList.get(r).getDueDate(), DATE_FORMAT_DD_MM_YYYY);
			} else if (c == 3) {
				return lotList.get(r).getMainStoreQuantity();
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
