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
package org.isf.medicalstockward.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.isf.examination.manager.ExaminationBrowserManager;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.model.Medical;
import org.isf.medicalstockward.manager.MovWardBrowserManager;
import org.isf.medicalstockward.model.MedicalWard;
import org.isf.medicalstockward.model.MovementWard;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WardPharmacyNew extends JDialog implements SelectionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(WardPharmacyNew.class);

//LISTENER INTERFACE --------------------------------------------------------
    private EventListenerList movementWardListeners = new EventListenerList();
	
	public interface MovementWardListeners extends EventListener {
		void movementUpdated(AWTEvent e);
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

	//---------------------------------------------------------------------------
	
	@Override
	public void patientSelected(Patient patient) {
		patientSelected = patient;
		jTextFieldPatient.setText(patientSelected.getName());
		jTextFieldPatient.setEditable(false);
		jButtonPickPatient.setText(MessageBundle.getMessage("angal.medicalstockwardedit.changepatient")); //$NON-NLS-1$
		jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.medicalstockwardedit.changethepatientassociatedwiththismovement")); //$NON-NLS-1$
		jButtonTrashPatient.setEnabled(true);
		
		try {
			Optional.ofNullable(examinationBrowserManager.getLastByPatID(patientSelected.getCode()))
			.map(lastExam -> lastExam.getPex_weight())
			.map(weight -> weight.floatValue())
			.ifPresent(w -> patientWeight = w);
			
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, this);
			MessageDialog.error(this, "angal.medicalstockwardedit.problemoccurredwhileretrievingweight");
		}
	}
	
	private static final long serialVersionUID = 1L;

	private	ExaminationBrowserManager examinationBrowserManager = Context.getApplicationContext().getBean(ExaminationBrowserManager.class);
	private WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private MovWardBrowserManager movWardBrowserManager = Context.getApplicationContext().getBean(MovWardBrowserManager.class);
	
	private JLabel jLabelPatient;
	private JTextField jTextFieldPatient;
	private JButton jButtonPickPatient;
	private JButton jButtonTrashPatient;
	private JPanel jPanelPatient;
	private JPanel jPanelMedicals;
	private JPanel jPanelButtons;
	private JPanel jPanelNorth;
	private JPanel jPanelUse;
	private JButton jButtonOK;
	private JButton jButtonCancel;
	private JRadioButton jRadioPatient;
	private JTable jTableMedicals;
	private JScrollPane jScrollPaneMedicals;
	private JPanel jPanelMedicalsButtons;
	private JButton jButtonAddMedical;
	private JButton jButtonRemoveMedical;
	private static final Dimension PatientDimension = new Dimension(300, 20);

	private Patient patientSelected;
	private float patientWeight;
	private Ward wardSelected;
	private Object[] medClasses = { Medical.class, Integer.class, String.class };
	private String[] medColumnNames = {
			MessageBundle.getMessage("angal.wardpharmacy.medical.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.quantity.txt").toUpperCase(),
			MessageBundle.getMessage("angal.wardpharmacy.lotnumber.col").toUpperCase()
	};
	private Integer[] medWidth = { 150, 150, 50 };
	private boolean[] medResizable = { true, false, false };

	private List<Medical> medArray = new ArrayList<>();
	private List<MedicalWard> wardDrugs;
	private List<MedicalWard> medItems = new ArrayList<>();
	private JRadioButton jRadioUse;
	private JTextField jTextFieldUse;
	private JLabel jLabelUse;

	private JRadioButton jRadioWard;
	private JComboBox wardBox;
	private JPanel panelWard;
	private JTextField searchTextField;
	private JButton searchButton;
	private JComboBox jComboBoxMedicals;

	public WardPharmacyNew(JFrame owner, Ward ward, List<MedicalWard> drugs) {
		super(owner, true);
		wardDrugs = drugs;
		for (MedicalWard elem : wardDrugs) {
			medArray.add(elem.getMedical());
		}
		wardSelected = ward;
		initComponents();
	}

	private void initComponents() {
		add(getJPanelButtons(), BorderLayout.SOUTH);
		add(getJPanelMedicals(), BorderLayout.CENTER);
		add(getJPanelNorth(), BorderLayout.NORTH);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(MessageBundle.getMessage("angal.medicalstockwardedit.title"));
		pack();
		setLocationRelativeTo(null);
	}
	
	private boolean isAutomaticLot() {
		return GeneralData.AUTOMATICLOTWARD_TOWARD;
	}

	private JPanel getJPanelNorth() {
		if (jPanelNorth == null) {
			jPanelNorth = new JPanel();
			jPanelNorth.setLayout(new BoxLayout(jPanelNorth, BoxLayout.Y_AXIS));
			jPanelNorth.add(getJPanelPatient());
			jPanelNorth.add(getJPanelUse());
			jPanelNorth.add(getPanelWard());
			ButtonGroup group = new ButtonGroup();
			group.add(jRadioPatient);
			group.add(jRadioUse);
                        group.add(jRadioWard);
		}
		return jPanelNorth;
	}

	private JPanel getJPanelUse() {
		if (jPanelUse == null) {
			jPanelUse = new JPanel(new FlowLayout(FlowLayout.LEFT));
			jPanelUse.add(getJRadioUse());
			jPanelUse.add(getJLabelUse());
			jPanelUse.add(getJTextFieldUse());
		}
		return jPanelUse;
	}

	private JLabel getJLabelUse() {
		if (jLabelUse == null) {
			jLabelUse = new JLabel(MessageBundle.getMessage("angal.medicalstockwardedit.internaluse"));
		}
		return jLabelUse;
	}

	private JTextField getJTextFieldUse() {
		if (jTextFieldUse == null) {
			jTextFieldUse = new JTextField(MessageBundle.getMessage("angal.medicalstockwardedit.internaluse").toUpperCase());
			jTextFieldUse.setPreferredSize(PatientDimension);
			jTextFieldUse.setEnabled(false);
		}
		return jTextFieldUse;
	}

	private JRadioButton getJRadioUse() {
		if (jRadioUse == null) {
			jRadioUse = new JRadioButton();
			jRadioUse.addActionListener(actionEvent -> {
				jTextFieldPatient.setEnabled(false);
				jButtonPickPatient.setEnabled(false);
				jButtonTrashPatient.setEnabled(false);
				jTextFieldUse.setEnabled(true);
				wardBox.setEnabled(false);
			});
		}
		return jRadioUse;
	}

	class StockMovModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private List<MedicalWard> druglist;

		public StockMovModel(List<MedicalWard> drug) {
			druglist = drug;
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
				return MessageBundle.getMessage("angal.common.quantity.txt").toUpperCase();
			}
			if (c == 2) {
				return MessageBundle.getMessage("angal.medicalstock.duedate").toUpperCase();
			}
			return ""; //$NON-NLS-1$
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Object getValueAt(int r, int c) {
			MedicalWard medicalWard = druglist.get(r);
			if (c == -1) {
				
				return medicalWard;
			} else if (c == 0) {
				return medicalWard.getId().getLot();
			} else if (c == 1) {	
				return medicalWard.getQty();
			}  else if (c == 2) {
				return TimeTools.formatDateTime(medicalWard.getLot().getDueDate(), DATE_FORMAT_DD_MM_YYYY);
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
	
	private boolean checkQuantityInLot(MedicalWard medWard, double qty) {
		double wardQty = medWard.getQty();
		if (qty > wardQty) {
			MessageDialog.error(this, "angal.medicalstock.movementquantityisgreaterthanthequantityof.msg");
			return false;
		} 
		return true;
	}
	
	private MedicalWard automaticChoose(List<MedicalWard> drug, String me, int quantity) {
		drug.sort((o1, o2) -> {
			if (o1.getLot().getDueDate() == null || o2.getLot().getDueDate() == null) {
				return 0;
			}
			return o1.getLot().getDueDate().compareTo(o2.getLot().getDueDate());
		});

		MedicalWard medWard = null;
		int q = quantity;
		for (MedicalWard elem : drug) {
			if (elem.getMedical().getDescription().equals(me)) {

				if (elem.getQty() != 0.0) {
					if (q != 0) {
						if (elem.getQty() <= q) {
							q = (int) (q - elem.getQty());
							int maxquantity = (int) (elem.getQty() - 0);
							medWard = elem;
							addItem(medWard, maxquantity);

						} else {
							medWard = elem;

							addItem(medWard, q);
							q = 0;
						}
					}

				}
			}
		}

		return medWard;
	}

	private MedicalWard chooseLot(List<MedicalWard> drug, String me, int quantity) {
		List<MedicalWard> dr = new ArrayList<>();
		MedicalWard medWard = null;
		for (MedicalWard elem : drug) {
			if (elem.getMedical().getDescription().equals(me)) {
				if (elem.getQty() != 0.0) {
					MedicalWard e = elem;
					dr.add(e);
				}
			}

		}

		JTable lotTable = new JTable(new StockMovModel(dr));
		lotTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(MessageBundle.getMessage("angal.medicalstock.multipledischarging.selectalot")), BorderLayout.NORTH); //$NON-NLS-1$
		panel.add(new JScrollPane(lotTable), BorderLayout.CENTER);

		do {
			int ok = JOptionPane.showConfirmDialog(this,
					panel,
					MessageBundle.getMessage("angal.medicalstock.multipledischarging.lotinformations"), //$NON-NLS-1$
					JOptionPane.OK_CANCEL_OPTION);

			if (ok == JOptionPane.OK_OPTION) {
				int row = lotTable.getSelectedRow();
				if (row != -1) {
					medWard = dr.get(row);
				} else {
					return null;
				}

				if (!checkQuantityInLot(medWard, quantity)) {
					medWard = null;
				}

				addItem(medWard, quantity);
			}

		} while (dr == null);

		return medWard;
	}

	protected int askQuantity(String med, List<MedicalWard> drug) {
		int qty = 0;
		double totalQty = 0;
		String prodCode = null;
		for (MedicalWard elem : drug) {

			if (med.equals(elem.getMedical().getDescription())) {
				totalQty += elem.getQty();
				prodCode = elem.getMedical().getProdCode();
			}

		}
		StringBuilder message = new StringBuilder();
		message.append(med).append('\n') //$NON-NLS-1$
				.append(MessageBundle.getMessage("angal.medicalstock.multipledischarging.lyinginstock")) //$NON-NLS-1$
				.append(totalQty); // $NON-NLS-1$
		StringBuilder title = new StringBuilder(MessageBundle.getMessage("angal.common.quantity.txt"));

		if (prodCode != null && !prodCode.equals("")) { //$NON-NLS-1$
			title.append(' ') //$NON-NLS-1$
					.append(MessageBundle.getMessage("angal.common.code.txt"))
					.append(": ") //$NON-NLS-1$
					.append(prodCode);
		} else {
			title.append(": "); //$NON-NLS-1$
		}

		do {
			String quantity = JOptionPane.showInputDialog(this, message.toString(), title.toString(),
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
					MessageDialog.error(this, "angal.medicalstock.multipledischarging.pleaseinsertavalidvalue");
					qty = 0;
				}
			} else {
				return qty;
			}
			if (checkQuantity(totalQty, qty)) {

				if (isAutomaticLot() || jRadioPatient.isSelected()) {
					MedicalWard medicalSelection = automaticChoose(wardDrugs, med, qty);
				} else {
					MedicalWard medicalSelection = chooseLot(wardDrugs, med, qty);
				}
			} else {
				askQuantity(med, wardDrugs);
			}
		} while (qty == 0);

		return qty;

	}
	
	private boolean checkQuantity( double totalQty, double qty) {
	
		if (qty > totalQty) {
			StringBuilder message = new StringBuilder();
			message.append(MessageBundle.getMessage("angal.medicalstock.multipledischarging.thequantityisnotavailable")) //$NON-NLS-1$
				.append('\n') //$NON-NLS-1$
				.append(MessageBundle.getMessage("angal.medicalstock.multipledischarging.lyinginstock")) //$NON-NLS-1$
				.append(totalQty);
			JOptionPane.showMessageDialog(this, message.toString());
			return false;
		}
		return true;
	}

	private JButton getJButtonAddMedical() {
		if (jButtonAddMedical == null) {
			jButtonAddMedical = new JButton(MessageBundle.getMessage("angal.medicalstockwardedit.medical.btn"));
			jButtonAddMedical.setMnemonic(MessageBundle.getMnemonic("angal.medicalstockwardedit.medical.btn.key"));
			jButtonAddMedical.setIcon(new ImageIcon("rsc/icons/plus_button.png")); //$NON-NLS-1$
			jButtonAddMedical.addActionListener(actionEvent -> {
				String medical = (String) jComboBoxMedicals.getSelectedItem();
				int quantity = askQuantity(medical, wardDrugs);
			});
		}
		return jButtonAddMedical;
	}
	
	public double round(double input, double step) {
		return Math.round(input / step) * step;
	}
	
	private JButton getJButtonRemoveMedical() {
		if (jButtonRemoveMedical == null) {
			jButtonRemoveMedical = new JButton(MessageBundle.getMessage("angal.medicalstockwardedit.removeitem.btn"));
			jButtonRemoveMedical.setMnemonic(MessageBundle.getMnemonic("angal.medicalstockwardedit.removeitem.btn.key"));
			jButtonRemoveMedical.setIcon(new ImageIcon("rsc/icons/delete_button.png")); //$NON-NLS-1$
			jButtonRemoveMedical.addActionListener(actionEvent -> {
				if (jTableMedicals.getSelectedRow() < 0) {
					MessageDialog.error(this, "angal.medicalstockwardedit.pleaseselectanitem");
				} else {
					removeItem(jTableMedicals.getSelectedRow());
				}
			});
		}
		return jButtonRemoveMedical;
	}

	private void addItem(MedicalWard ward, int quantity) {
		if (ward != null) {
		
			MedicalWard item = new MedicalWard(ward.getMedical(), quantity, ward.getId().getLot());
			medItems.add(item);
			
//			medArray.add(med);
			jTableMedicals.updateUI();
		}
	}
	
	private void removeItem(int row) {
		if (row != -1) {
			medItems.remove(row);
			medArray.remove(row);
			jTableMedicals.updateUI();
		}
		
	}

	private JPanel getJPanelMedicalsButtons() {
		if (jPanelMedicalsButtons == null) {
			jPanelMedicalsButtons = new JPanel();
			jPanelMedicalsButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
			jPanelMedicalsButtons.add(getJButtonRemoveMedical());
		}
		return jPanelMedicalsButtons;
	}

	private JScrollPane getJScrollPaneMedicals() {
		if (jScrollPaneMedicals == null) {
			jScrollPaneMedicals = new JScrollPane();
			jScrollPaneMedicals.setViewportView(getJTableMedicals());
		}
		return jScrollPaneMedicals;
	}

	private JTable getJTableMedicals() {
		if (jTableMedicals == null) {
			jTableMedicals = new JTable();
			jTableMedicals.setModel(new MedicalTableModel());
			for (int i = 0; i < medWidth.length; i++) {
				jTableMedicals.getColumnModel().getColumn(i).setMinWidth(medWidth[i]);
				if (!medResizable[i]) {
					jTableMedicals.getColumnModel().getColumn(i).setMaxWidth(medWidth[i]);
				}
			}
		}
		return jTableMedicals;
	}

	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			jButtonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			jButtonOK.addActionListener(actionEvent -> {

				boolean isPatient;
				String description;
				int age = 0;
				LocalDateTime newDate = TimeTools.getNow();
				Ward wardTo = null; //
				if (jRadioPatient.isSelected()) {
					isPatient = true;
					if (patientSelected != null) {
						description = patientSelected.getName();
						age = patientSelected.getAge();
					} else {
						MessageDialog.error(null, "angal.medicalstock.multipledischarging.pleaseselectpatient");
						return;
					}
				} else if (jRadioWard.isSelected()) {
					Object selectedObj = wardBox.getSelectedItem();
					if (selectedObj instanceof Ward) {
						wardTo = (Ward) selectedObj;
					} else {
						MessageDialog.error(null, "angal.medicalstock.multipledischarging.pleaseselectaward.msg");
						return;
					}
					description = wardTo.getDescription();
					isPatient = false;
				} else {
					isPatient = false;
					description = jTextFieldUse.getText();
				}

				List<MovementWard> manyMovementWard = new ArrayList<>();
				try {
					for (MedicalWard medItem : medItems) {
						manyMovementWard.add(new MovementWard(wardSelected, newDate, isPatient, patientSelected,
								age, patientWeight, description, medItem.getMedical(), medItem.getQty(),
								MessageBundle.getMessage("angal.medicalstockwardedit.pieces"), wardTo, null, medItem.getLot()));
					}

					movWardBrowserManager.newMovementWard(manyMovementWard);
					fireMovementWardInserted();
					dispose();
				} catch (OHServiceException ex) {
					MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
				}
			});
		}
		return jButtonOK;
	}
	
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jButtonCancel.addActionListener(actionEvent -> dispose());
		}
		return jButtonCancel;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonOK());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
	}

	private JPanel getJPanelMedicals() {
		if (jPanelMedicals == null) {
			jPanelMedicals = new JPanel();
			jPanelMedicals.setLayout(new BoxLayout(jPanelMedicals, BoxLayout.Y_AXIS));
                        jPanelMedicals.add(getJPanelMedicalsSearch());
			jPanelMedicals.add(getJScrollPaneMedicals());
			jPanelMedicals.add(getJPanelMedicalsButtons());
		}
		return jPanelMedicals;
	}

	private JPanel getJPanelPatient() {
		if (jPanelPatient == null) {
			jPanelPatient = new JPanel(new FlowLayout(FlowLayout.LEFT));
			jPanelPatient.add(getJRadioPatient());
			jPanelPatient.add(getJLabelPatient());
			jPanelPatient.add(getJTextFieldPatient());
			jPanelPatient.add(getJButtonPickPatient());
			jPanelPatient.add(getJButtonTrashPatient());
		}
		return jPanelPatient;
	}

	private JRadioButton getJRadioPatient() {
		if (jRadioPatient == null) {
			jRadioPatient = new JRadioButton();
			jRadioPatient.setSelected(true);
			jRadioPatient.addActionListener(actionEvent -> {
				jTextFieldUse.setEnabled(false);
				jTextFieldPatient.setEnabled(true);
				jButtonPickPatient.setEnabled(true);
				wardBox.setEnabled(false);
				if (patientSelected != null) {
					jButtonTrashPatient.setEnabled(true);
				}
			});
		}
		return jRadioPatient;
	}

	private JButton getJButtonTrashPatient() {
		if (jButtonTrashPatient == null) {
			jButtonTrashPatient = new JButton();
			jButtonTrashPatient.setPreferredSize(new Dimension(25, 25));
			jButtonTrashPatient.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
			jButtonTrashPatient.setToolTipText(MessageBundle.getMessage("angal.medicalstockwardedit.tooltip.removepatientassociationwiththismovement"));
			jButtonTrashPatient.addActionListener(actionEvent -> {

				patientSelected = null;
				jTextFieldPatient.setText(""); //$NON-NLS-1$
				jTextFieldPatient.setEditable(true);
				jButtonPickPatient.setText(MessageBundle.getMessage("angal.medicalstockwardedit.selectpatient"));
				jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.medicalstockwardedit.tooltip.associateapatientwiththismovement"));
				jButtonTrashPatient.setEnabled(false);
			});
			jButtonTrashPatient.setEnabled(false);
		}
		return jButtonTrashPatient;
	}

	private JButton getJButtonPickPatient() {
		if (jButtonPickPatient == null) {
			jButtonPickPatient = new JButton(MessageBundle.getMessage("angal.medicalstockwardedit.selectpatient.btn"));
			jButtonPickPatient.setMnemonic(MessageBundle.getMnemonic("angal.medicalstockwardedit.selectpatient.btn.key"));
			jButtonPickPatient.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png")); //$NON-NLS-1$
			jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.medicalstockwardedit.tooltip.associateapatientwiththismovement"));
			jButtonPickPatient.addActionListener(actionEvent -> {
				SelectPatient sp = new SelectPatient(this, patientSelected);
				sp.addSelectionListener(this);
				sp.pack();
				sp.setVisible(true);
			});
		}
		return jButtonPickPatient;
	}

	private JTextField getJTextFieldPatient() {
		if (jTextFieldPatient == null) {
			jTextFieldPatient = new JTextField();
			jTextFieldPatient.setText(""); //$NON-NLS-1$
			jTextFieldPatient.setPreferredSize(PatientDimension);
		}
		return jTextFieldPatient;
	}

	private JLabel getJLabelPatient() {
		if (jLabelPatient == null) {
			jLabelPatient = new JLabel(MessageBundle.getMessage("angal.medicalstockwardedit.patient"));
		}
		return jLabelPatient;
	}
	
	public class MedicalTableModel implements TableModel {
		
		public MedicalTableModel() {
			
		}
		
		@Override
		public Class<?> getColumnClass(int i) {
			return medClasses[i].getClass();
		}

		@Override
		public int getColumnCount() {
			return medClasses.length;
		}
		
		@Override
		public int getRowCount() {
			if (medItems == null) {
				return 0;
			}
			return medItems.size();
		}
		
		@Override
		public Object getValueAt(int r, int c) {
			MedicalWard medWard = medItems.get(r);
			if (c == -1) {
				return medWard;
			}
			if (c == 0) {
				return medWard.getMedical().getDescription();
			}
			if (c == 1) {
				return medWard.getQty(); 
			}
			if (c == 2) {
				return medWard.getLot(); 
			}
			return null;
		}
		
		@Override
		public boolean isCellEditable(int r, int c) {
			return c == 1;
		}
		
		@Override
		public void setValueAt(Object item, int r, int c) {
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
		}

		@Override
		public String getColumnName(int columnIndex) {
			return medColumnNames[columnIndex];
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
		}
	}

	private JPanel getPanelWard() {
		if (panelWard == null) {
			panelWard = new JPanel();
			panelWard.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
			panelWard.add(getJRadioWard());
			//panelWard.add(getJLabelSelectWard());
			panelWard.add(getWardBox());
		}
		return panelWard;
	}

	private JRadioButton getJRadioWard() {
		if (jRadioWard == null) {
			jRadioWard = new JRadioButton(MessageBundle.getMessage("angal.wardpharmacynew.ward.btn"));
			jRadioWard.setSelected(false);
			jRadioWard.addActionListener(actionEvent -> {
				jTextFieldUse.setEnabled(false);
				jTextFieldPatient.setEnabled(false);
				jButtonPickPatient.setEnabled(false);
				wardBox.setEnabled(true);
			});
			jRadioWard.setMinimumSize(new Dimension(55, 22));
			jRadioWard.setMaximumSize(new Dimension(55, 22));
		}
		return jRadioWard;
	}

	private JComboBox getWardBox() {
		if (wardBox == null) {
			wardBox = new JComboBox();
			wardBox.setPreferredSize(new Dimension(300, 30));
			List<Ward> wardList = null;
			try {
				wardList = wardBrowserManager.getWards();
			} catch (OHServiceException ex) {
				LOGGER.error(ex.getMessage(), ex);
			}
			wardBox.addItem("");
			if (wardList != null) {
				for (Ward elem : wardList) {
					if (!wardSelected.getCode().equals(elem.getCode())) {
						wardBox.addItem(elem);
					}
				}
			}
			wardBox.setEnabled(false);
		}
		return wardBox;
	}

	private JPanel getJPanelMedicalsSearch() {
		searchButton = new JButton();
		searchButton.setPreferredSize(new Dimension(20, 20));
		searchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
		searchButton.addActionListener(actionEvent -> {
			jComboBoxMedicals.removeAllItems();
			List<Medical> results = getSearchMedicalsResults(searchTextField.getText(), medArray);
			for (Medical aMedical : results) {
				jComboBoxMedicals.addItem(aMedical.getDescription());
			}
		});

		searchTextField = new JTextField(10);
		searchTextField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					searchButton.doClick();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});

		/*
		 * Adds to facilitate the selection of products
		 */
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		searchPanel.add(searchTextField);
		searchPanel.add(searchButton);
		searchPanel.add(getJComboBoxMedicals());
		searchPanel.add(getJButtonAddMedical());
		return searchPanel;
	}

	private JComboBox getJComboBoxMedicals() {
		if (jComboBoxMedicals == null) {
			jComboBoxMedicals = new JComboBox();
			jComboBoxMedicals.setMaximumSize(new Dimension(300, 24));
			jComboBoxMedicals.setPreferredSize(new Dimension(300, 24));
		}
		List<Object> med = new ArrayList<>();
		for (Medical aMedical : medArray) {
			if (!med.contains(aMedical.getDescription())) {
				med.add(aMedical.getDescription());
				jComboBoxMedicals.addItem(aMedical.getDescription());
			}
		}
		return jComboBoxMedicals;
	}

	private List<Medical> getSearchMedicalsResults(String s, List<Medical> medicalsList) {
		String query = s.trim();
		List<Medical> results = new ArrayList<>();
		for (Medical medoc : medicalsList) {
			if (!query.equals("")) {
				String[] patterns = query.split(" ");
				String code = medoc.getProdCode().toLowerCase();
				String description = medoc.getDescription().toLowerCase();
				boolean patternFound = false;
				for (String pattern : patterns) {
					if (code.contains(pattern.toLowerCase()) || description.contains(pattern.toLowerCase())) {
						patternFound = true;
						//It is sufficient that only one pattern matches the query
						break;
					}
				}
				if (patternFound) {
					results.add(medoc);
				}
			} else {
				results.add(medoc);
			}
		}
		return results;
	}

}
