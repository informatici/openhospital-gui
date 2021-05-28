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
package org.isf.patient.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.PatientInsert.PatientListener;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class PatientBrowser extends ModalJFrame implements PatientListener{
	
	private static final long serialVersionUID = 1L;
	private String[] pColumns = { MessageBundle.getMessage("angal.patient.namem"),
			MessageBundle.getMessage("angal.patient.agem"),
			MessageBundle.getMessage("angal.patient.sexm"),
			MessageBundle.getMessage("angal.patient.addressm"),
			MessageBundle.getMessage("angal.patient.citym"),
			MessageBundle.getMessage("angal.patient.telephonem") };
	private JPanel jButtonPanel = null;
	private JPanel jContainPanel = null;
	private JButton jNewButton = null;
	private JButton jEditButton = null;
	private JButton jCloseButton = null;
	private JButton jDeleteButton = null;
	private JTable jTable = null;
	private PatientBrowserModel model;
	private int[] pColumnWidth = { 200, 30, 25 ,100, 100, 50 };
	private int selectedrow;
	private Patient patient;
	private PatientBrowserManager manager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private ArrayList<Patient> pPat;
	
	
	public JTable getJTable() {
		if (jTable == null) {
			model = new PatientBrowserModel();
			jTable = new JTable(model);
			jTable.getColumnModel().getColumn(0).setMinWidth(pColumnWidth[0]);
			jTable.getColumnModel().getColumn(1).setMinWidth(pColumnWidth[1]);
			jTable.getColumnModel().getColumn(2).setMinWidth(pColumnWidth[2]);
			jTable.getColumnModel().getColumn(2).setMaxWidth(pColumnWidth[2]);
			jTable.getColumnModel().getColumn(3).setMinWidth(pColumnWidth[3]);
			jTable.getColumnModel().getColumn(3).setMaxWidth(pColumnWidth[3]);
			jTable.getColumnModel().getColumn(4).setMinWidth(pColumnWidth[4]);
			jTable.getColumnModel().getColumn(5).setMinWidth(pColumnWidth[5]);
		}return jTable;
	}
	
	/**
	 * This method initializes 
	 */
	public PatientBrowser() {
		super();
		initialize();
		setVisible(true);
	}
	
	/**
	 * This method initializes jButtonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJNewButton(), null);
			jButtonPanel.add(getJEditButton(), null);
			jButtonPanel.add(getJDeleteButton(), null);
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}
	
	
	/**
	 * This method initializes this
	 */
	private void initialize() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		final int pfrmBase = 10;
        final int pfrmWidth = 6;
        final int pfrmHeight = 5;
        this.setBounds((screensize.width - screensize.width * pfrmWidth / pfrmBase ) / 2, (screensize.height - screensize.height * pfrmHeight / pfrmBase)/2, 
                screensize.width * pfrmWidth / pfrmBase, screensize.height * pfrmHeight / pfrmBase);
		this.setTitle(MessageBundle.getMessage("angal.patient.patientbrowser.title"));
		this.setContentPane(getJContainPanel());
		//pack();	
	}
	
	/**
	 * This method initializes containPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
			jContainPanel.add(new JScrollPane(getJTable()),
					java.awt.BorderLayout.CENTER);
			validate();
		}
		return jContainPanel;
	}
	
	/**
	 * This method initializes jNewButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJNewButton() {
		if (jNewButton == null) {
			jNewButton = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jNewButton.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jNewButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent event) {
					patient = new Patient();
					PatientInsert newrecord = new PatientInsert(PatientBrowser.this, patient, true);
					newrecord.addPatientListener(PatientBrowser.this);
					newrecord.setVisible(true);
				}
			});
		}
		return jNewButton;
	}
	
	/**
	 * This method initializes jEditButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJEditButton() {
		if (jEditButton == null) {
			jEditButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jEditButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jEditButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent event) {
					if (jTable.getSelectedRow() < 0) {
						MessageDialog.error(PatientBrowser.this, "angal.common.pleaseselectarow.msg");
					} else {
						selectedrow = jTable.getSelectedRow();
						patient = (Patient) (model.getValueAt(selectedrow, -1));
						PatientInsert editrecord = new PatientInsert(PatientBrowser.this, patient, false);
						editrecord.addPatientListener(PatientBrowser.this);
						editrecord.setVisible(true);
					}
				}
			});
		}
		return jEditButton;
	}
	
	/**
	 * This method initializes jCloseButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
		}
		return jCloseButton;
	}
	
	/**
	 * This method initializes jDeleteButton
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJDeleteButton() {
		if (jDeleteButton == null) {
			jDeleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jDeleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jDeleteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if (jTable.getSelectedRow() < 0) {
						MessageDialog.error(PatientBrowser.this, "angal.common.pleaseselectarow.msg");
					} else {
						Patient pat = (Patient) (model.getValueAt(jTable.getSelectedRow(), -1));
						int n = JOptionPane.showConfirmDialog(null,
								MessageBundle.getMessage("angal.patient.deletepatient") + " \" "+pat.getName() + "\" ?",
								MessageBundle.getMessage("angal.hospital"), JOptionPane.YES_NO_OPTION);
						try {
							if ((n == JOptionPane.YES_OPTION) && (manager.deletePatient(pat))) {
								pPat.remove(pPat.size() - jTable.getSelectedRow() - 1);
								model.fireTableDataChanged();
								jTable.updateUI();
							}
						} catch(OHServiceException ohServiceException) {
							MessageDialog.showExceptions(ohServiceException);
						}
					}
				}
				
			});
		}
		return jDeleteButton;
	}
	
class PatientBrowserModel extends DefaultTableModel {
		
	private static final long serialVersionUID = 1L;

		public PatientBrowserModel() {
			PatientBrowserManager manager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
			try {
				pPat = manager.getPatient();
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}

		public int getRowCount() {
			if (pPat == null)
				return 0;
			return pPat.size();
		}
		
		public String getColumnName(int c) {
			return pColumns[c];
		}

		public int getColumnCount() {
			return pColumns.length;
		}

		
		//{ "NAME", "AGE","SEX","ADDRESS","CITY", "TELEPHONE"};
		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return pPat.get(r).getName();
			} else if (c == -1) {
				return pPat.get(r);
			} else if (c == 1) {
				return pPat.get(r).getAge();
			} else if (c == 2) {
				return pPat.get(r).getSex();
			} else if (c == 3) {
				return pPat.get(r).getAddress();
			} else if (c == 4) {
				return pPat.get(r).getCity();
			} else if (c == 5) {
				return pPat.get(r).getTelephone();
			}
			return null;
		}
		
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			//return super.isCellEditable(arg0, arg1);
			return false;
		}
	}



public void patientUpdated(AWTEvent e) {
	/*pPat.set(selectedrow, patient);
	System.out.println("riga->" + selectedrow);
	((PatientBrowserModel) jTable.getModel()).fireTableDataChanged();
	jTable.updateUI();*/
	model= new PatientBrowserModel();
	model.fireTableDataChanged();
	jTable.updateUI();
	if ((jTable.getRowCount() > 0) && selectedrow > -1)
		jTable.setRowSelectionInterval(selectedrow, selectedrow);
}

public void patientInserted(AWTEvent e) {
	
	pPat.add(0, patient);
	((PatientBrowserModel) jTable.getModel()).fireTableDataChanged();
	if (jTable.getRowCount() > 0)
		jTable.setRowSelectionInterval(0, 0);
}

}
