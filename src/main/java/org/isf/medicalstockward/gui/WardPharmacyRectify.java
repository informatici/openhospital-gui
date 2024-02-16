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
package org.isf.medicalstockward.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.medicalstockward.manager.MovWardBrowserManager;
import org.isf.medicalstockward.model.MedicalWard;
import org.isf.medicalstockward.model.MovementWard;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.RequestFocusListener;
import org.isf.utils.jobjects.TextPrompt;
import org.isf.utils.time.TimeTools;
import org.isf.ward.model.Ward;

public class WardPharmacyRectify extends JDialog {

	private static final Font FONT_BOLD = new Font("Tahoma", Font.BOLD, 14);
	private static final Font FONT_PLAIN_14 = new Font("Tahoma", Font.PLAIN, 14);
	private static final Font FONT_PLAIN_28 = new Font("Tahoma", Font.PLAIN, 28);

	// LISTENER INTERFACE --------------------------------------------------------
	private EventListenerList movementWardListeners = new EventListenerList();

	public interface MovementWardListeners extends EventListener {

		void movementInserted(AWTEvent e);
	}

	public void addMovementWardListener(MovementWardListeners l) {
		movementWardListeners.add(MovementWardListeners.class, l);
	}

	public void removeMovementWardListener(MovementWardListeners listener) {
		movementWardListeners.remove(MovementWardListeners.class, listener);
	}

	private void fireMovementWardInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = movementWardListeners.getListeners(MovementWardListeners.class);
		for (EventListener listener : listeners) {
			((MovementWardListeners) listener).movementInserted(event);
		}
	}

	// ---------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	private Ward selectedWard;
	private JComboBox jComboBoxMedical;
	private JTextField jTextFieldStockQty;
	private JLabel jLabelLotQty;
	private JLabel jLabelInLot;
	private JSpinner jSpinnerNewQty;
	private SpinnerNumberModel spinnerNewQtyModel;

	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private MovWardBrowserManager movWardBrowserManager = Context.getApplicationContext().getBean(MovWardBrowserManager.class);
	private MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);

	private List<Medical> medicals; // list of all medicals available in the application
	private Map<Integer, Double> wardMap; // map quantities by their medical_id
	private JTextField jTextFieldLotNumber;
	private JButton jButtonChooseLot;
	private List<MedicalWard> wardDrugs; // list of drugs available in the selected ward

	private JButton jButtonNewLot;
	private Lot selectedLot;

	public WardPharmacyRectify() {
		initMedicals();
		initComponents();
	}

	private void initMedicals() {
		try {
			this.medicals = medicalBrowsingManager.getMedicals();
		} catch (OHServiceException e) {
			this.medicals = null;
			OHServiceExceptionUtil.showMessages(e);
		}
	}

	/**
	 * Create the dialog.
	 */
	public WardPharmacyRectify(JFrame owner, Ward ward) {
		super(owner, true);
		selectedWard = ward;
		try {
			wardDrugs = movWardBrowserManager.getMedicalsWard(selectedWard.getCode().charAt(0), false);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		wardMap = new HashMap<>();
		for (MedicalWard medWard : wardDrugs) {

			if (wardMap.containsKey(medWard.getMedical().getCode())) {
				Double quantity = wardMap.get(medWard.getMedical().getCode());
				wardMap.put(medWard.getMedical().getCode(), quantity + medWard.getQty());
			} else {
				wardMap.put(medWard.getMedical().getCode(), medWard.getQty());
			}
		}
		initMedicals();
		initComponents();
	}

	public WardPharmacyRectify(JFrame owner, Ward ward, Medical medical) {
		super(owner, true);
		selectedWard = ward;
		try {
			wardDrugs = movWardBrowserManager.getMedicalsWard(selectedWard.getCode().charAt(0), false);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		wardMap = new HashMap<>();
		for (MedicalWard medWard : wardDrugs) {

			if (wardMap.containsKey(medWard.getMedical().getCode())) {
				Double qu = wardMap.get(medWard.getMedical().getCode());
				wardMap.put(medWard.getMedical().getCode(), qu + medWard.getQty());
			} else {
				wardMap.put(medWard.getMedical().getCode(), medWard.getQty());
			}
		}

		initMedicals();
		initComponents();
		jComboBoxMedical.setSelectedItem(medical);
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gblContentPanel = new GridBagLayout();
		gblContentPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gblContentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gblContentPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gblContentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gblContentPanel);

		JLabel jLabelRectifyTitle = new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.title")); //$NON-NLS-1$
		jLabelRectifyTitle.setForeground(Color.RED);
		jLabelRectifyTitle.setFont(FONT_PLAIN_28);
		GridBagConstraints gbcLabelRectifyTitle = new GridBagConstraints();
		gbcLabelRectifyTitle.insets = new Insets(0, 0, 5, 5);
		gbcLabelRectifyTitle.gridx = 1;
		gbcLabelRectifyTitle.gridy = 0;
		contentPanel.add(jLabelRectifyTitle, gbcLabelRectifyTitle);

		JLabel jLabelStock = new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.instock")); //$NON-NLS-1$
		GridBagConstraints gbcLabelStock = new GridBagConstraints();
		gbcLabelStock.anchor = GridBagConstraints.SOUTH;
		gbcLabelStock.insets = new Insets(0, 0, 5, 0);
		gbcLabelStock.gridx = 4;
		gbcLabelStock.gridy = 1;
		contentPanel.add(jLabelStock, gbcLabelStock);

		JLabel jLabelMedical = new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.medical")); //$NON-NLS-1$
		jLabelMedical.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelMedical.setPreferredSize(new Dimension(100, 25));
		GridBagConstraints gbcLabelMedical = new GridBagConstraints();
		gbcLabelMedical.insets = new Insets(0, 0, 5, 5);
		gbcLabelMedical.anchor = GridBagConstraints.EAST;
		gbcLabelMedical.gridx = 0;
		gbcLabelMedical.gridy = 2;
		contentPanel.add(jLabelMedical, gbcLabelMedical);

		GridBagConstraints gbcComboBoxMedical = new GridBagConstraints();
		gbcComboBoxMedical.insets = new Insets(0, 0, 5, 5);
		gbcComboBoxMedical.fill = GridBagConstraints.HORIZONTAL;
		gbcComboBoxMedical.gridx = 1;
		gbcComboBoxMedical.gridy = 2;
		gbcComboBoxMedical.gridwidth = 2;
		contentPanel.add(getJComboBoxMedical(), gbcComboBoxMedical);

		GridBagConstraints gbcTextFieldStockQty = new GridBagConstraints();
		gbcTextFieldStockQty.insets = new Insets(0, 0, 5, 0);
		gbcTextFieldStockQty.gridx = 4;
		gbcTextFieldStockQty.gridy = 2;
		contentPanel.add(getJTextFieldStockQty(), gbcTextFieldStockQty);

		JLabel jLabelLot = new JLabel(MessageBundle.getMessage("angal.medicalstockward.lotnumberabb")); //$NON-NLS-1$
		jLabelLot.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelLot.setPreferredSize(new Dimension(100, 25));
		GridBagConstraints gbcLabelLot = new GridBagConstraints();
		gbcLabelLot.insets = new Insets(0, 0, 5, 5);
		gbcLabelLot.anchor = GridBagConstraints.EAST;
		gbcLabelLot.gridx = 0;
		gbcLabelLot.gridy = 3;
		contentPanel.add(jLabelLot, gbcLabelLot);

		GridBagConstraints gbcPanelLot = new GridBagConstraints();
		gbcPanelLot.insets = new Insets(0, 0, 0, 0);
		gbcPanelLot.fill = GridBagConstraints.HORIZONTAL;
		gbcPanelLot.gridx = 1;
		gbcPanelLot.gridy = 3;
		gbcPanelLot.gridwidth = 2;
		contentPanel.add(getJPanelLot(), gbcPanelLot);

		jLabelInLot = new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.inlot")); //$NON-NLS-1$
		jLabelInLot.setVisible(false);
		GridBagConstraints gbcLabelInLot = new GridBagConstraints();
		gbcLabelInLot.anchor = GridBagConstraints.SOUTH;
		gbcLabelInLot.insets = new Insets(0, 0, 5, 0);
		gbcLabelInLot.gridx = 4;
		gbcLabelInLot.gridy = 3;
		contentPanel.add(jLabelInLot, gbcLabelInLot);

		JLabel jLabelNewQuantity = new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.actualquantity")); //$NON-NLS-1$
		GridBagConstraints gbcLabelNewQuantity = new GridBagConstraints();
		gbcLabelNewQuantity.anchor = GridBagConstraints.EAST;
		gbcLabelNewQuantity.insets = new Insets(0, 0, 5, 5);
		gbcLabelNewQuantity.gridx = 0;
		gbcLabelNewQuantity.gridy = 4;
		contentPanel.add(jLabelNewQuantity, gbcLabelNewQuantity);

		GridBagConstraints gbcSpinnerNewQty = new GridBagConstraints();
		gbcSpinnerNewQty.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinnerNewQty.insets = new Insets(0, 0, 5, 5);
		gbcSpinnerNewQty.gridx = 1;
		gbcSpinnerNewQty.gridy = 4;
		gbcSpinnerNewQty.gridwidth = 2;
		contentPanel.add(getJSpinnerNewQty(), gbcSpinnerNewQty);

		GridBagConstraints gbcLabelLotQty = new GridBagConstraints();
		gbcLabelLotQty.insets = new Insets(0, 0, 5, 0);
		gbcLabelLotQty.gridx = 4;
		gbcLabelLotQty.gridy = 4;
		contentPanel.add(getJLabelLotQty(), gbcLabelLotQty);

		JLabel jLabelReason = new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.reason")); //$NON-NLS-1$
		GridBagConstraints gbcLabelReason = new GridBagConstraints();
		gbcLabelReason.anchor = GridBagConstraints.EAST;
		gbcLabelReason.insets = new Insets(0, 0, 0, 5);
		gbcLabelReason.gridx = 0;
		gbcLabelReason.gridy = 5;
		contentPanel.add(jLabelReason, gbcLabelReason);

		JTextField jTextFieldReason = new JTextField();
		GridBagConstraints gbcTextFieldReason = new GridBagConstraints();
		gbcTextFieldReason.insets = new Insets(0, 0, 0, 5);
		gbcTextFieldReason.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldReason.gridx = 1;
		gbcTextFieldReason.gridy = 5;
		gbcTextFieldReason.gridwidth = 2;
		contentPanel.add(jTextFieldReason, gbcTextFieldReason);

		/*
		 * TODO: to refactor all this part by extracting in separated method all this logic
		 */
		JPanel jButtonPanel = new JPanel();
		jButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		getContentPane().add(jButtonPanel, BorderLayout.SOUTH);

		JButton jButtonOk = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
		jButtonOk.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
		jButtonOk.addActionListener(actionEvent -> {
			Object item;
			Medical med;

			/*
			 * TODO: to refactor all this part by extracting in separated method all this logic
			 */
			try {
				item = jComboBoxMedical.getSelectedItem();
				if (item instanceof Medical) {
					med = (Medical) jComboBoxMedical.getSelectedItem();
				} else {
					MessageDialog.error(this, "angal.medicalstockward.rectify.pleaseselectadrug");
					return;
				}
			} catch (ClassCastException e1) {
				MessageDialog.error(this, "angal.medicalstockward.rectify.pleaseselectadrug");
				return;
			}

			/*
			 * To override MovWardBrowserManager.validateMovementWard() behavior
			 */
			if (selectedLot == null) {
				MessageDialog.error(this, "angal.medicalstockward.rectify.pleaseselectalot");
				return;
			}

			String reason = jTextFieldReason.getText().trim();
			if (reason.equals("")) {
				MessageDialog.error(this, "angal.medicalstockward.rectify.pleasespecifythereason");
				return;
			}

			double lotQty = 0;
			try {
				lotQty = movWardBrowserManager.getCurrentQuantityInWard(selectedWard, selectedLot);
			} catch (OHServiceException e2) {
				OHServiceExceptionUtil.showMessages(e2);
			}
			double newQty = spinnerNewQtyModel.getNumber().doubleValue();
			double movQuantity = lotQty - newQty;

			if (movQuantity == 0. || newQty < 0) {
				JOptionPane.showMessageDialog(this, MessageBundle.getMessage("angal.medicalstockward.rectify.pleaseinsertavalidvalue"));
				return;
			}
			if (newQty == 0.) {
				int ok = JOptionPane.showConfirmDialog(this, MessageBundle.getMessage("angal.medicalstockward.rectify.thiswillemptythelotproceed"));
				if (ok != JOptionPane.OK_OPTION) {
					return;
				}
			}

			try {
				movStockInsertingManager.storeLot(selectedLot.getCode(), selectedLot, med);
				movWardBrowserManager.newMovementWard(new MovementWard(selectedWard, TimeTools.getNow(), false, null, 0, 0, reason, med, movQuantity,
								MessageBundle.getMessage("angal.medicalstockward.rectify.pieces"), selectedLot));
				fireMovementWardInserted();
				dispose();
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
		});
		jButtonPanel.add(jButtonOk);

		JButton jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		jButtonCancel.addActionListener(actionEvent -> dispose());
		jButtonPanel.add(jButtonCancel);

		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJPanelLot() {
		JPanel lotPanel = new JPanel(new SpringLayout());
		GridBagLayout gblLotPanel = new GridBagLayout();
		gblLotPanel.columnWeights = new double[] { 1.0, 0.0, 0.0 };
		gblLotPanel.rowWeights = new double[] { 0.0, 0.0, 0.0 };
		lotPanel.setLayout(gblLotPanel);

		jTextFieldLotNumber = new JTextField();
		jTextFieldLotNumber.setEditable(false);
		GridBagConstraints gbcTextFieldLotNumberReason = new GridBagConstraints();
		gbcTextFieldLotNumberReason.insets = new Insets(0, 0, 0, 5);
		gbcTextFieldLotNumberReason.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldLotNumberReason.gridx = 1;
		gbcTextFieldLotNumberReason.gridy = 0;
		lotPanel.add(jTextFieldLotNumber, gbcTextFieldLotNumberReason);
		jTextFieldLotNumber.setColumns(20);

		GridBagConstraints gbcButtonChooseLot = new GridBagConstraints();
		gbcButtonChooseLot.gridx = 2;
		gbcButtonChooseLot.gridy = 0;
		lotPanel.add(getJButtonChooseLot(), gbcButtonChooseLot);

		GridBagConstraints gbcButtonNewLot = new GridBagConstraints();
		gbcButtonNewLot.insets = new Insets(0, 5, 0, 5);
		gbcButtonNewLot.gridx = 3;
		gbcButtonNewLot.gridy = 0;
		lotPanel.add(getJButtonNewLot(), gbcButtonNewLot);

		return lotPanel;
	}

	private JButton getJButtonNewLot() {
		if (jButtonNewLot == null) {
			jButtonNewLot = new JButton(MessageBundle.getMessage("angal.medicalstockward.newlot.btn"));
			jButtonNewLot.setMnemonic(MessageBundle.getMnemonic("angal.medicalstockward.newlot.btn.key"));
			jButtonNewLot.addActionListener(actionEvent -> {
				Medical medical;
				try {
					medical = (Medical) jComboBoxMedical.getSelectedItem();
				} catch (ClassCastException e1) {
					MessageDialog.error(this, "angal.medicalstockward.rectify.pleaseselectadrug");
					return;
				}
				chooseLot(medical, true);
			});
		}
		return jButtonNewLot;
	}
	private JButton getJButtonChooseLot() {
		if (jButtonChooseLot == null) {
			jButtonChooseLot = new JButton(MessageBundle.getMessage("angal.medicalstockward.chooselot.btn"));
			jButtonChooseLot.setMnemonic(MessageBundle.getMnemonic("angal.medicalstockward.chooselot.btn.key"));
			jButtonChooseLot.addActionListener(actionEvent -> {
				Medical medical;
				try {
					medical = (Medical) jComboBoxMedical.getSelectedItem();
				} catch (ClassCastException e1) {
					MessageDialog.error(this, "angal.medicalstockward.rectify.pleaseselectadrug");
					return;
				}
				chooseLot(medical, false);
			});
		}
		return jButtonChooseLot;
	}

	private MedicalWard chooseLot(Medical medical, boolean newLot) {
		MedicalWard medWard = null;

		List<MedicalWard> drugChooseList = new ArrayList<>();

		for (MedicalWard elem : wardDrugs) {
			if (elem.getMedical().getDescription().equals(medical.getDescription())) {
				drugChooseList.add(elem);
			}
		}
		if (newLot) {
			Lot addLot = askLot();
			if (addLot == null) {
				return null;
			}

			jTextFieldLotNumber.setText(addLot.getCode());
			jSpinnerNewQty.setValue(0);
			jLabelLotQty.setText("0");
			jLabelInLot.setVisible(true);
			if (GeneralData.LOTWITHCOST) {
				BigDecimal cost = askCost();
				addLot.setCost(cost);
			}
			selectedLot = addLot;

		} else {

			JTable lotTable = new JTable(new StockMovModel(drugChooseList));
			lotTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.selectalot")), BorderLayout.NORTH); //$NON-NLS-1$
			panel.add(new JScrollPane(lotTable), BorderLayout.CENTER);

			int ok = JOptionPane.showConfirmDialog(this,
							panel,
							MessageBundle.getMessage("angal.medicalstockward.rectify.lotinformations"), //$NON-NLS-1$
							JOptionPane.OK_CANCEL_OPTION);

			if (ok == JOptionPane.OK_OPTION) {
				int row = lotTable.getSelectedRow();
				if (row != -1) {
					medWard = drugChooseList.get(row);
				} else {
					return null;
				}
				jTextFieldLotNumber.setText(medWard.getLot().getCode());
				jSpinnerNewQty.setValue(medWard.getQty());
				jLabelLotQty.setText(medWard.getQty().toString());
				jLabelInLot.setVisible(true);
				selectedLot = medWard.getLot();
			}
		}
		return medWard;
	}
	private boolean isAutomaticLot() {
		return GeneralData.AUTOMATICLOT_IN;
	}

	protected Lot askLot() {
		Lot lot = null;
		JTextField lotNameTextField = new JTextField(15);

		TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.medicalstock.multiplecharging.lotid"), lotNameTextField);
		suggestion.setFont(FONT_PLAIN_14);
		suggestion.setForeground(Color.GRAY);
		suggestion.setHorizontalAlignment(SwingConstants.CENTER);
		suggestion.changeAlpha(0.5f);
		suggestion.changeStyle(Font.BOLD + Font.ITALIC);

		lotNameTextField.addAncestorListener(new RequestFocusListener());
		if (isAutomaticLot()) {
			lotNameTextField.setEnabled(false);
		}
		GoodDateChooser preparationDateChooser = new GoodDateChooser(null, true, false);
		GoodDateChooser expireDateChooser = new GoodDateChooser(null, true, false);

		JPanel panel = new JPanel(new GridLayout(3, 2));
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.lotnumberabb"))); //$NON-NLS-1$
		panel.add(lotNameTextField);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.preparationdate"))); //$NON-NLS-1$
		panel.add(preparationDateChooser);
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstockward.rectify.expiringdate"))); //$NON-NLS-1$
		panel.add(expireDateChooser);

		do {
			int ok = JOptionPane.showConfirmDialog(this, panel,
							MessageBundle.getMessage("angal.medicalstockward.rectify.lotinformations"), //$NON-NLS-1$
							JOptionPane.OK_CANCEL_OPTION);
			if (ok == JOptionPane.OK_OPTION) {
				String lotName = lotNameTextField.getText();
				if (lotName.isEmpty()) {
					MessageDialog.error(this, "angal.medicalstockward.rectify.lotnumberSelect");
					return null;
				}
				if (expireDateChooser.getDate().isBefore(preparationDateChooser.getDate())) {
					MessageDialog.error(this, "angal.medicalstockward.rectify.expirydatebeforepreparationdate");
				} else {
					LocalDateTime expiringDate = expireDateChooser.getDateEndOfDay();
					LocalDateTime preparationDate = preparationDateChooser.getDateStartOfDay();
					lot = new Lot(lotName, preparationDate, expiringDate);
				}
			} else {
				return null;
			}
		} while (lot == null);
		return lot;
	}

	protected BigDecimal askCost() {
		double cost = 0.;
		do {
			String input = JOptionPane.showInputDialog(this,
							MessageBundle.getMessage("angal.medicalstockward.rectify.unitcost"), //$NON-NLS-1$
							0.);
			if (input != null) {
				try {
					cost = Double.parseDouble(input);
					if (cost < 0) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException nfe) {
					MessageDialog.error(this, "angal.medicalstockward.rectify.pleaseinsertavalidvalue");
				}
			} else {
				return BigDecimal.valueOf(cost);
			}
		} while (cost == 0.);
		return BigDecimal.valueOf(cost);
	}

	protected int askQuantity(Medical med) {
		StringBuilder title = new StringBuilder(MessageBundle.getMessage("angal.common.quantity.txt"));
		StringBuilder message = new StringBuilder(med.toString());
		String prodCode = med.getProdCode();
		if (prodCode != null && !prodCode.equals("")) {
			title.append(' ').append(MessageBundle.getMessage("angal.common.code.txt")); //$NON-NLS-1$ //$NON-NLS-2$
			title.append(": ").append(prodCode); //$NON-NLS-1$
		} else {
			title.append(": "); //$NON-NLS-1$
		}
		int qty = 0;
		do {
			String quantity = JOptionPane.showInputDialog(this,
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
					MessageDialog.error(this, "angal.medicalstockward.invalidquantitypleasetryagain");
					qty = 0;
				}
			} else {
				return qty;
			}
		} while (qty == 0);
		return qty;
	}

	/**
	 * @return
	 */
	private JSpinner getJSpinnerNewQty() {
		if (jSpinnerNewQty == null) {
			spinnerNewQtyModel = new SpinnerNumberModel(0.0d, 0.0d, null, 0.5d);
			jSpinnerNewQty = new JSpinner(spinnerNewQtyModel);
			jSpinnerNewQty.setFont(FONT_BOLD);
			jSpinnerNewQty.addChangeListener(changeEvent -> {
				double stock = Double.parseDouble(jTextFieldStockQty.getText());
				double newQty = spinnerNewQtyModel.getNumber().doubleValue();
				if (stock > 0) {
					jButtonChooseLot.setEnabled(true);
				}
				if (newQty > stock) {
					jButtonNewLot.setEnabled(true);
				} else if (newQty < stock) {
					jButtonNewLot.setEnabled(false);
				}
			});
		}
		return jSpinnerNewQty;
	}

	/**
	 * @return
	 */
	private JTextField getJTextFieldStockQty() {
		if (jTextFieldStockQty == null) {
			jTextFieldStockQty = new JTextField(""); //$NON-NLS-1$
			jTextFieldStockQty.setHorizontalAlignment(SwingConstants.CENTER);
			jTextFieldStockQty.setPreferredSize(new Dimension(100, 25));
			jTextFieldStockQty.setFont(FONT_BOLD);
			jTextFieldStockQty.setForeground(Color.BLACK);
			jTextFieldStockQty.setEditable(false);
			jTextFieldStockQty.setBorder(BorderFactory.createEmptyBorder());
		}
		return jTextFieldStockQty;
	}

	/**
	 * @return
	 */
	private JLabel getJLabelLotQty() {
		if (jLabelLotQty == null) {
			jLabelLotQty = new JLabel(""); //$NON-NLS-1$
			jLabelLotQty.setHorizontalAlignment(SwingConstants.CENTER);
			jLabelLotQty.setPreferredSize(new Dimension(100, 25));
			jLabelLotQty.setFont(FONT_BOLD);
			jLabelLotQty.setForeground(Color.BLUE);
		}
		return jLabelLotQty;
	}

	/**
	 * @return
	 */
	private JComboBox getJComboBoxMedical() {
		if (jComboBoxMedical == null) {
			jComboBoxMedical = new JComboBox();
			jComboBoxMedical.addItem(""); //$NON-NLS-1$
			for (Medical med : medicals) {
				jComboBoxMedical.addItem(med);
			}
			jComboBoxMedical.addActionListener(actionEvent -> {
				try {
					Medical med = (Medical) jComboBoxMedical.getSelectedItem();
					jButtonChooseLot.setEnabled(false);
					jButtonNewLot.setEnabled(true);
					for (MedicalWard medWard : wardDrugs) {
						if (med.getDescription().equals(medWard.getMedical().getDescription())) {
							jButtonChooseLot.setEnabled(true);
							jButtonNewLot.setEnabled(false);
						}
					}
					Integer code = med.getCode();
					Double qty = wardMap.get(code);
					if (qty == null) {
						qty = 0.0D;
					}
					jTextFieldStockQty.setText(qty.toString());
					jSpinnerNewQty.setValue(qty);
				} catch (ClassCastException ex) {
					jTextFieldStockQty.setText(""); //$NON-NLS-1$
					jSpinnerNewQty.setValue(0.0D);
				}
			});
		}
		return jComboBoxMedical;
	}

	class StockMovModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private List<MedicalWard> druglist;

		public StockMovModel(List<MedicalWard> drugList) {
			druglist = drugList;
		}

		@Override
		public int getRowCount() {
			if (druglist == null) {
				return 0;
			}
			return druglist.size();
		}

		@Override
		public String getColumnName(int c) {
			if (c == 0) {
				return MessageBundle.getMessage("angal.medicalstock.lotid").toUpperCase();
			}
			if (c == 1) {
				return MessageBundle.getMessage("angal.medicalstock.duedate").toUpperCase();
			}
			if (c == 2) {
				return MessageBundle.getMessage("angal.common.quantity.txt").toUpperCase();
			}
			return ""; //$NON-NLS-1$
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Object getValueAt(int r, int c) {
			MedicalWard drug = druglist.get(r);
			if (c == -1) {
				return drug;
			} else if (c == 0) {
				return drug.getId().getLot().getCode();
			} else if (c == 1) {

				return TimeTools.formatDateTime(drug.getId().getLot().getDueDate(), DATE_FORMAT_DD_MM_YYYY);
			} else if (c == 2) {
				return drug.getQty();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
}
