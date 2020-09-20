/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.medicalstockward.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstockward.manager.MovWardBrowserManager;
import org.isf.medicalstockward.model.MedicalWard;
import org.isf.medicalstockward.model.MovementWard;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

public class WardPharmacyNew extends JDialog implements SelectionListener {

//LISTENER INTERFACE --------------------------------------------------------
    private EventListenerList movementWardListeners = new EventListenerList();
	
	public interface MovementWardListeners extends EventListener {
		public void movementUpdated(AWTEvent e);
		public void movementInserted(AWTEvent e);
	}
	
	public void addMovementWardListener(MovementWardListeners l) {
		movementWardListeners.add(MovementWardListeners.class, l);
	}
	
	public void removeMovementWardListener(MovementWardListeners listener) {
		movementWardListeners.remove(MovementWardListeners.class, listener);
	}
	
	private void fireMovementWardInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;};
		
		EventListener[] listeners = movementWardListeners.getListeners(MovementWardListeners.class);
		for (int i = 0; i < listeners.length; i++)
			((MovementWardListeners)listeners[i]).movementInserted(event);
	}
	/*private void fireMovementWardUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			*//**
			 * 
			 *//*
			private static final long serialVersionUID = 1L;};
		
		EventListener[] listeners = movementWardListeners.getListeners(MovementWardListeners.class);
		for (int i = 0; i < listeners.length; i++)
			((MovementWardListeners)listeners[i]).movementUpdated(event);
	}*/
//---------------------------------------------------------------------------
	
	public void patientSelected(Patient patient) {
		patientSelected = patient;
		jTextFieldPatient.setText(patientSelected.getName());
		jTextFieldPatient.setEditable(false);
		jButtonPickPatient.setText(MessageBundle.getMessage("angal.medicalstockwardedit.changepatient")); //$NON-NLS-1$
		jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.medicalstockwardedit.changethepatientassociatedwiththismovement")); //$NON-NLS-1$
		jButtonTrashPatient.setEnabled(true);
//		if (patientSelected.getWeight() == 0) {
//			JOptionPane.showMessageDialog(WardPharmacyNew.this, MessageBundle.getMessage("angal.medicalstockwardedit.theselectedpatienthasnoweightdefined"));
//		}
	}
	
	private static final long serialVersionUID = 1L;
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
	private static final Dimension PatientDimension = new Dimension(300,20);
	private static final String DATE_FORMAT_DD_MM_YYYY = "dd/MM/yyyy"; //$NON-NLS-1$
	
	private Patient patientSelected = null;
	private Ward wardSelected;
	private Object[] medClasses = {Medical.class, Integer.class, String.class};
	private String[] medColumnNames = {MessageBundle.getMessage("angal.medicalstockward.medical"), 
									   MessageBundle.getMessage("angal.common.quantity"),
									   MessageBundle.getMessage("angal.medicalstockward.lotnumberabb") };
	private Integer[] medWidth = {150, 150, 50};
	private boolean[] medResizable = {true, false, false};
	
	//Medicals (ALL)
	//MedicalBrowsingManager medManager = new MedicalBrowsingManager();
	//ArrayList<Medical> medArray = medManager.getMedicals();

	//Medicals (in WARD)
	//ArrayList<MedItem> medItems = new ArrayList<MedItem>();
	private ArrayList<Medical> medArray = new ArrayList<Medical>();
	private ArrayList<Double> qtyArray = new ArrayList<Double>(); 
	private ArrayList<MedicalWard> wardDrugs = null;
	private ArrayList<MedicalWard> medItems = new ArrayList<MedicalWard>();
	private JRadioButton jRadioUse;
	private JTextField jTextFieldUse;
	private JLabel jLabelUse;

        private JRadioButton jRadioWard;
	private JComboBox wardBox;
	private JPanel panelWard;
        /*
         *Adds to facilitate the selection of products 
         */
        private JPanel searchPanel;
        private JTextField searchTextField;
        private JButton searchButton;
        private JComboBox jComboBoxMedicals;
        //private JLabel jLabelSelectWard;

		private MovWardBrowserManager wardManager = Context.getApplicationContext().getBean(MovWardBrowserManager.class);

	public WardPharmacyNew(JFrame owner, Ward ward, ArrayList<MedicalWard> drugs) {
		super(owner, true);
		wardDrugs = drugs;
		for (MedicalWard elem : wardDrugs) {
			medArray.add(elem.getMedical());
			qtyArray.add(elem.getQty());
		}
		wardSelected = ward;
		initComponents();
	}

	private void initComponents() {
		add(getJPanelButtons(), BorderLayout.SOUTH);
		add(getJPanelMedicals(), BorderLayout.CENTER);
		add(getJPanelNorth(), BorderLayout.NORTH);
		setDefaultCloseOperation(WardPharmacyNew.DISPOSE_ON_CLOSE);
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
			jLabelUse = new JLabel();
			jLabelUse.setText(MessageBundle.getMessage("angal.medicalstockwardedit.internaluse"));
		}
		return jLabelUse;
	}

	private JTextField getJTextFieldUse() {
		if (jTextFieldUse == null) {
			jTextFieldUse = new JTextField();
			jTextFieldUse.setText(MessageBundle.getMessage("angal.medicalstockwardedit.internaluse").toUpperCase()); //$NON-NLS-1$
			jTextFieldUse.setPreferredSize(PatientDimension);
			jTextFieldUse.setEnabled(false);
		}
		return jTextFieldUse;
	}

	private JRadioButton getJRadioUse() {
		if (jRadioUse == null) {
			jRadioUse = new JRadioButton();
			jRadioUse.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					jTextFieldPatient.setEnabled(false);
					jButtonPickPatient.setEnabled(false);
					jButtonTrashPatient.setEnabled(false);
					jTextFieldUse.setEnabled(true);
                                        wardBox.setEnabled(false);
				}
			});
		}
		return jRadioUse;
	}
	private MovStockInsertingManager movManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);
	
	class StockMovModel extends DefaultTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private ArrayList<MedicalWard> druglist;

		public StockMovModel(ArrayList<MedicalWard> drug) {
			druglist = drug;
		}

		public int getRowCount() {
			if (druglist == null)
				return 0;
			return druglist.size();
		}

		public String getColumnName(int c) {
			if (c == 0) {
				return MessageBundle.getMessage("angal.medicalstock.lotid"); //$NON-NLS-1$
			}
			
			if (c == 1) {
				return MessageBundle.getMessage("angal.medicalstock.duedate"); //$NON-NLS-1$
			}
			if (c == 2) {
				return MessageBundle.getMessage("angal.common.quantity"); //$NON-NLS-1$
			}
			return ""; //$NON-NLS-1$
		}

		public int getColumnCount() {
			return 3;
		}

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
			JOptionPane.showMessageDialog(WardPharmacyNew.this, 
					MessageBundle.getMessage("angal.medicalstock.movementquantityisgreaterthanthequantityof")); //$NON-NLS-1$
			return false;
		} 
		return true;
	}
	
	private MedicalWard automaticChoose(ArrayList<MedicalWard> drug, String me, int qanty) {
		ArrayList<MedicalWard> dr = new ArrayList<MedicalWard>();
		Collections.sort(drug, new Comparator<MedicalWard>() {
			@Override
			public int compare(MedicalWard o1, MedicalWard o2) {
				if (o1.getLot().getDueDate() == null || o2.getLot().getDueDate() == null)
					return 0;
				return o1.getLot().getDueDate().compareTo(o2.getLot().getDueDate());
			}
		});

		MedicalWard medWard = null;
		int q = qanty;
		for (MedicalWard elem : drug) {
			if (elem.getMedical().getDescription().equals(me)) {

				if (elem.getQty() != 0.0) {
					if (q != 0) {
						if (elem.getQty() <= q) {
							MedicalWard e = elem;
							dr.add(e);
							q = (int) (q - elem.getQty());
							int maxquantity = (int) (elem.getQty() - 0);
							medWard = elem;
							addItem(medWard, maxquantity);

						} else {
							MedicalWard e = elem;
							dr.add(e);
							int qu = (int) (elem.getQty() - q);
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
	
	private MedicalWard chooseLot(ArrayList<MedicalWard> drug, String me, int qanty) {
		ArrayList<MedicalWard> dr = new ArrayList<MedicalWard>();
		MedicalWard medWard =null;
		for (MedicalWard elem : drug) {
			if(elem.getMedical().getDescription().equals(me)) {
				if(elem.getQty() != 0.0) {
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
				int ok = JOptionPane.showConfirmDialog(WardPharmacyNew.this, 
						panel, 
						MessageBundle.getMessage("angal.medicalstock.multipledischarging.lotinformations"), //$NON-NLS-1$ 
						JOptionPane.OK_CANCEL_OPTION);
	
				if (ok == JOptionPane.OK_OPTION) {
					int row = lotTable.getSelectedRow();
					if (row != -1) medWard = dr.get(row);
						else return null;
					
					
					if (!checkQuantityInLot(medWard, qanty)) medWard = null;
					
					
					addItem(medWard, qanty);
					
				}
				
			} while (dr == null);
		 
		return medWard;
	}
	
	protected int askQuantity(String med, ArrayList<MedicalWard> drug) {
		int qty = 0;
		double totalQty = 0;
		String prodCode = null;
		for (MedicalWard elem : drug) {

			if (med.equals(elem.getMedical().getDescription())) {
				totalQty += elem.getQty();
				prodCode = elem.getMedical().getProd_code();

			}

		}
		double usedQty = 0;
		StringBuilder message = new StringBuilder();
		message.append(med.toString()).append("\n") //$NON-NLS-1$
				.append(MessageBundle.getMessage("angal.medicalstock.multipledischarging.lyinginstock")) //$NON-NLS-1$
				.append(totalQty); // $NON-NLS-1$
		StringBuilder title = new StringBuilder(MessageBundle.getMessage("angal.common.quantity")); //$NON-NLS-1$

		if (prodCode != null && !prodCode.equals("")) { //$NON-NLS-1$
			title.append(" ") //$NON-NLS-1$
					.append(MessageBundle.getMessage("angal.common.code")) //$NON-NLS-1$
					.append(": ") //$NON-NLS-1$
					.append(prodCode);
		} else {
			title.append(": "); //$NON-NLS-1$
		}

		do {
			String quantity = JOptionPane.showInputDialog(WardPharmacyNew.this, message.toString(), title.toString(),
					JOptionPane.QUESTION_MESSAGE);

			if (quantity != null) {
				try {
					qty = Integer.parseInt(quantity);
					if (qty == 0)
						return 0;
					if (qty < 0)
						throw new NumberFormatException();

				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(WardPharmacyNew.this,
							MessageBundle.getMessage("angal.medicalstock.multipledischarging.pleaseinsertavalidvalue")); //$NON-NLS-1$
					qty = 0;
				}
			} else
				return qty;
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
				.append("\n") //$NON-NLS-1$
				.append(MessageBundle.getMessage("angal.medicalstock.multipledischarging.lyinginstock")) //$NON-NLS-1$
				.append(totalQty);
			JOptionPane.showMessageDialog(WardPharmacyNew.this, message.toString());
			return false;
		}
		return true;
	}
	private JButton getJButtonAddMedical() {
		if (jButtonAddMedical == null) {
			jButtonAddMedical = new JButton();
			jButtonAddMedical.setText(MessageBundle.getMessage("angal.medicalstockwardedit.medical")); //$NON-NLS-1$
			jButtonAddMedical.setMnemonic(KeyEvent.VK_M);
			jButtonAddMedical.setIcon(new ImageIcon("rsc/icons/plus_button.png")); //$NON-NLS-1$
			jButtonAddMedical.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String medical=(String)jComboBoxMedicals.getSelectedItem();
					int qanty = askQuantity(medical,wardDrugs);
				}
			});
		}
		return jButtonAddMedical;
	}
	
	public double round(double input, double step) {
		return Math.round(input / step) * step;
	}
	
	private JButton getJButtonRemoveMedical() {
		if (jButtonRemoveMedical == null) {
			jButtonRemoveMedical = new JButton();
			jButtonRemoveMedical.setText(MessageBundle.getMessage("angal.medicalstockwardedit.removeitem")); //$NON-NLS-1$
			jButtonRemoveMedical.setIcon(new ImageIcon("rsc/icons/delete_button.png")); //$NON-NLS-1$
			jButtonRemoveMedical.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (jTableMedicals.getSelectedRow() < 0) { 
						JOptionPane.showMessageDialog(WardPharmacyNew.this,
								MessageBundle.getMessage("angal.medicalstockwardedit.pleaseselectanitem"), //$NON-NLS-1$
								"Error", //$NON-NLS-1$
								JOptionPane.WARNING_MESSAGE);
					} else {
						removeItem(jTableMedicals.getSelectedRow());
					}
				}
			});
		}
		return jButtonRemoveMedical;
	}

	private void addItem(MedicalWard ward, int qanty) {
		if (ward != null) {
		
			MedicalWard item = new MedicalWard(ward.getMedical(), (double) qanty, ward.getId().getLot());
			medItems.add(item);
			
//			medArray.add(med);
			qtyArray.add((double) qanty);
			jTableMedicals.updateUI();
		}
	}
	
	private void removeItem(int row) {
		if (row != -1) {
			medItems.remove(row);
			medArray.remove(row);
			qtyArray.remove(row);
			jTableMedicals.updateUI();
		}
		
	}
	
	private JPanel getJPanelMedicalsButtons() {
		if (jPanelMedicalsButtons == null) {
			jPanelMedicalsButtons = new JPanel();
                        jPanelMedicalsButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
			//jPanelMedicalsButtons.setLayout(new BoxLayout(jPanelMedicalsButtons, BoxLayout.Y_AXIS));
			//jPanelMedicalsButtons.add(getJButtonAddMedical());
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
				if (!medResizable[i]) jTableMedicals.getColumnModel().getColumn(i).setMaxWidth(medWidth[i]);
			}
		}
		return jTableMedicals;
	}

	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton();
			jButtonOK.setText(MessageBundle.getMessage("angal.common.ok")); //$NON-NLS-1$
			jButtonOK.setMnemonic(KeyEvent.VK_O);
			jButtonOK.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					boolean isPatient;
					String description = "";
					int age = 0;
					float weight = 0;
					GregorianCalendar newDate = new GregorianCalendar();
					Ward wardTo = null; //
					if (jRadioPatient.isSelected()) {
						isPatient = true;
						if (patientSelected != null) {
							description = patientSelected.getName();
							age = patientSelected.getAge();
							weight = patientSelected.getWeight();
						}else {
							JOptionPane.showMessageDialog(null,
									MessageBundle.getMessage("angal.medicalstock.multipledischarging.pleaseselectpatient"));
							return;
						}
					} 
                    else if (jRadioWard.isSelected()) {
						Object selectedObj = wardBox.getSelectedItem();
						if(selectedObj instanceof Ward){
							wardTo = (Ward) selectedObj;
						}
						else{
							JOptionPane.showMessageDialog(null,
									MessageBundle.getMessage("angal.medicalstock.multipledischarging.pleaseselectaward"));
							return;
						}
                        description = wardTo.getDescription();
						isPatient = false;
					} 
                    else {
						isPatient = false;
						description = jTextFieldUse.getText();
					}
					
//					ArrayList<MovementWard> manyMovementWard = new ArrayList<MovementWard>();
//					for (int i = 0; i < medItems.size(); i++) {
//						try {
//							manyMovementWard.add(new MovementWard(
//									wardSelected,
//									newDate,
//									isPatient,
//									patientSelected,
//									age,
//									weight,
//									description,
//									medItems.get(i).getMedical(),
//									medItems.get(i).getQty(),
//									MessageBundle.getMessage("angal.medicalstockwardedit.pieces"),
//                                                                        wardTo));
//						} catch (OHException e1) {
//							e1.printStackTrace();
//						}
//					}
//					MovWardBrowserManager wardManager = new MovWardBrowserManager();
//					boolean result;
//					try {
//						result = wardManager.newMovementWard(manyMovementWard);
//						if (result) {
//							fireMovementWardInserted();
//							dispose();
//						} else
//							JOptionPane.showMessageDialog(null, MessageBundle.getMessage("angal.sql.thedatacouldnotbesaved"));
//					} catch (OHServiceException e1) {
//						result = false;
//						OHServiceExceptionUtil.showMessages(e1);
//					}

                    // innit of the datas needed to store the movement
					//ArrayList<Movement> movements = new ArrayList<Movement>();
					//Lot aLot = new Lot("", newDate, newDate);
					//String refNo = "";
                                        
                    ArrayList<MovementWard> manyMovementWard = new ArrayList<MovementWard>();
                    //MovStockInsertingManager movManager = new MovStockInsertingManager();
                    boolean result;
					try {
						// MovementType typeCharge = new
						// MedicaldsrstockmovTypeBrowserManager().getMovementType("charge");
						for (int i = 0; i < medItems.size(); i++) {
							manyMovementWard.add(new MovementWard(wardSelected, newDate, isPatient, patientSelected,
									age, weight, description, medItems.get(i).getMedical(), medItems.get(i).getQty(),
									MessageBundle.getMessage("angal.medicalstockwardedit.pieces"), wardTo, null,medItems.get(i).getLot()));
						}

						result = wardManager.newMovementWard(manyMovementWard);
					} catch (OHServiceException ex) {
                        result = false;
                    }
					if (result) {
                        fireMovementWardInserted();
                        dispose();
					} else {
                        JOptionPane.showMessageDialog(null, MessageBundle.getMessage("angal.sql.thedatacouldnotbesaved"));
                    
                    }	
				}
			});
		}
		return jButtonOK;
	}
	
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton();
			jButtonCancel.setText(MessageBundle.getMessage("angal.common.cancel")); //$NON-NLS-1$
			jButtonCancel.setMnemonic(KeyEvent.VK_C);
			jButtonCancel.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent event) {
						dispose();
				}
			});
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
			jRadioPatient.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					jTextFieldUse.setEnabled(false);
					jTextFieldPatient.setEnabled(true);
					jButtonPickPatient.setEnabled(true);
                                        wardBox.setEnabled(false);
					if (patientSelected != null) jButtonTrashPatient.setEnabled(true);
					
				}
			});
		}
		return jRadioPatient;
	}

	private JButton getJButtonTrashPatient() {
		if (jButtonTrashPatient == null) {
			jButtonTrashPatient = new JButton();
			jButtonTrashPatient.setMnemonic(KeyEvent.VK_R);
			jButtonTrashPatient.setPreferredSize(new Dimension(25,25));
			jButtonTrashPatient.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png")); //$NON-NLS-1$
			jButtonTrashPatient.setToolTipText(MessageBundle.getMessage("angal.medicalstockwardedit.tooltip.removepatientassociationwiththismovement")); //$NON-NLS-1$
			jButtonTrashPatient.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					
					patientSelected = null;
					jTextFieldPatient.setText(""); //$NON-NLS-1$
					jTextFieldPatient.setEditable(true);
					jButtonPickPatient.setText(MessageBundle.getMessage("angal.medicalstockwardedit.pickpatient"));
					jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.medicalstockwardedit.tooltip.associateapatientwiththismovement")); //$NON-NLS-1$
					jButtonTrashPatient.setEnabled(false);
				}
			});
			jButtonTrashPatient.setEnabled(false);
		}
		return jButtonTrashPatient;
	}

	private JButton getJButtonPickPatient() {
		if (jButtonPickPatient == null) {
			jButtonPickPatient = new JButton();
			jButtonPickPatient.setText(MessageBundle.getMessage("angal.medicalstockwardedit.pickpatient"));
			jButtonPickPatient.setMnemonic(KeyEvent.VK_P);
			jButtonPickPatient.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png")); //$NON-NLS-1$
			jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.medicalstockwardedit.tooltip.associateapatientwiththismovement"));
			jButtonPickPatient.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					SelectPatient sp = new SelectPatient(WardPharmacyNew.this, patientSelected);
					sp.addSelectionListener(WardPharmacyNew.this);
					sp.pack();
					sp.setVisible(true);
				}
			});
		}
		return jButtonPickPatient;
	}

	private JTextField getJTextFieldPatient() {
		if (jTextFieldPatient == null) {
			jTextFieldPatient = new JTextField();
			jTextFieldPatient.setText(""); //$NON-NLS-1$
			jTextFieldPatient.setPreferredSize(PatientDimension);
			//Font patientFont=new Font(jTextFieldPatient.getFont().getName(), Font.BOLD, jTextFieldPatient.getFont().getSize() + 4);
			//jTextFieldPatient.setFont(patientFont);
		}
		return jTextFieldPatient;
	}

	private JLabel getJLabelPatient() {
		if (jLabelPatient == null) {
			jLabelPatient = new JLabel();
			jLabelPatient.setText(MessageBundle.getMessage("angal.medicalstockwardedit.patient"));
		}
		return jLabelPatient;
	}
	
	public class MedicalTableModel implements TableModel {
		
		public MedicalTableModel() {
			
		}
		
		public Class<?> getColumnClass(int i) {
			return medClasses[i].getClass();
		}

		
		public int getColumnCount() {
			return medClasses.length;
		}
		
		public int getRowCount() {
			if (medItems == null)
				return 0;
			return medItems.size();
		}
		
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
		
		public boolean isCellEditable(int r, int c) {
			if (c == 1) return true;
			return false;
		}
		
		public void setValueAt(Object item, int r, int c) {
			//if (c == 1) billItems.get(r).setItemQuantity((Integer)item);

		}

		public void addTableModelListener(TableModelListener l) {
			
		}

		public String getColumnName(int columnIndex) {
			return medColumnNames[columnIndex];
		}

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
			jRadioWard = new JRadioButton(MessageBundle.getMessage("angal.wardpharmacynew.ward"));
			jRadioWard.setSelected(false);
			jRadioWard.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jTextFieldUse.setEnabled(false);
					jTextFieldPatient.setEnabled(false);
					jButtonPickPatient.setEnabled(false);
					wardBox.setEnabled(true);
				}
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
			WardBrowserManager wbm = Context.getApplicationContext().getBean(WardBrowserManager.class);
			ArrayList<Ward> wardList = null;
                        try {
                            wardList = wbm.getWards();
                        } catch (OHServiceException ex) {
                            Logger.getLogger(WardPharmacyNew.class.getName()).log(Level.SEVERE, null, ex);
                        }
			wardBox.addItem("");
			if(wardList != null) {
                            for (org.isf.ward.model.Ward elem : wardList) {
				if (!wardSelected.getCode().equals(elem.getCode()))
					wardBox.addItem(elem);
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
            searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    jComboBoxMedicals.removeAllItems();
                    ArrayList<Medical> results = getSearchMedicalsResults(searchTextField.getText(), medArray);
                    for (Medical aMedical : results) {
                    	jComboBoxMedicals.addItem(aMedical.getDescription());
                    }
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
                public void keyReleased(KeyEvent e) {}
                @Override
                public void keyTyped(KeyEvent e) {}
            });
            
            searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
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
		ArrayList<Object> med = new ArrayList<Object>();
		for (Medical aMedical : medArray) {
			if (!med.contains(aMedical.getDescription())) { 
				med.add(aMedical.getDescription());
				jComboBoxMedicals.addItem(aMedical.getDescription());
            } 
		}
		return jComboBoxMedicals;
	}
        
        private ArrayList<Medical> getSearchMedicalsResults(String s, ArrayList<Medical> medicalsList) {
            String query = s.trim();
            ArrayList<Medical> results = new ArrayList<Medical>();
            for (Medical medoc : medicalsList) {
                if(!query.equals("")) {
                    String[] patterns = query.split(" ");
                    String code = medoc.getProd_code().toLowerCase();
                    String description = medoc.getDescription().toLowerCase();
                    boolean patternFound = false;
                    for (String pattern : patterns) {
                        if (code.contains(pattern.toLowerCase()) || description.contains(pattern.toLowerCase())) {
                            patternFound = true;
                            //It is sufficient that only one pattern matches the query
                            break;
                        }
                    }
                    if (patternFound){
                        results.add(medoc);
                    }
                } else {
                    results.add(medoc);
                }
            }		
            return results;
        }

//	private JLabel getJLabelSelectWard() {
//		if (jLabelSelectWard == null) {
//			jLabelSelectWard = new JLabel(MessageBundle.getMessage("angal.wardpharmacynew.selectward"));
//			jLabelSelectWard.setVisible(false);
//			jLabelSelectWard.setAlignmentX(JLabel.CENTER_ALIGNMENT);
//		}
//		return jLabelSelectWard;
//	}
}
