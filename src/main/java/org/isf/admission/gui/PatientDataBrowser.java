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
package org.isf.admission.gui;

import static org.isf.utils.Constants.DATE_FORMATTER;
import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YY;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.EventListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.malnutrition.gui.MalnutritionBrowser;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.opd.gui.OpdEdit;
import org.isf.opd.gui.OpdEditExtended;
import org.isf.opd.manager.OpdBrowserManager;
import org.isf.opd.model.Opd;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.gui.PatientSummary;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.table.TableSorter;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

/**
 * This class shows and allows to modify all patient data and all patient admissions.
 *
 * last release  oct-23-06
 * @author flavio
 * ----------------------------------------------------
 * (org.isf.admission.gui)PatientDataBrowser
 * ---------------------------------------------------
 * modification history
 * 08/09/2008 - alex - added OPD in the table
 * 					 - modified EDIT and DELETE methods to match the selection
 * 					 - fixed record elimination in the view port
 * 					 - modified some panels in GUI
 * ------------------------------------------
 */
public class PatientDataBrowser extends ModalJFrame implements 
				PatientInsert.PatientListener, PatientInsertExtended.PatientListener, AdmissionBrowser.AdmissionListener, OpdEditExtended.SurgeryListener {

	private static final long serialVersionUID = 1L;
	private EventListenerList deleteAdmissionListeners = new EventListenerList();

    public interface DeleteAdmissionListener extends EventListener {
        void deleteAdmissionUpdated(AWTEvent e);
    }

    public void addDeleteAdmissionListener(DeleteAdmissionListener l) {
        deleteAdmissionListeners.add(DeleteAdmissionListener.class, l);
    }

    public void removeDeleteAdmissionListener(DeleteAdmissionListener listener) {
        deleteAdmissionListeners.remove(DeleteAdmissionListener.class, listener);
    }

	private void fireDeleteAdmissionUpdated(Admission admission) {
		AWTEvent event = new AWTEvent(admission, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = deleteAdmissionListeners.getListeners(DeleteAdmissionListener.class);
		for (EventListener listener : listeners) {
			((DeleteAdmissionListener) listener).deleteAdmissionUpdated(event);
		}
	}
	
	//---------------------------------------------------------------------
	
	@Override
	public void patientInserted(AWTEvent e) {
	}

	@Override
	public void patientUpdated(AWTEvent e) {
		jContentPane = null;
		initialize();		
	}
	
	@Override
	public void surgeryInserted(AWTEvent e, Opd opd) {
	}
	
	@Override
	public void surgeryUpdated(AWTEvent e, Opd opd) {
		jContentPane = null;
		initialize();
	}
	
	@Override
	public void admissionInserted(AWTEvent e) {
	}

	@Override
	public void admissionUpdated(AWTEvent e) {
		jContentPane = null;
		initialize();		
	}

	private Patient patient;
	private JFrame admittedPatientWindow;
	
	private AdmissionBrowserManager admissionManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
	
	public PatientDataBrowser(AdmittedPatientBrowser parentWindow,  Patient myPatient) {
		super();
		patient = myPatient;
		admittedPatientWindow = parentWindow;
		initialize();
	}

	private void initialize() {

		this.setContentPane(getJContentPane());

		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		setTitle(MessageBundle.getMessage("angal.admission.patientdata.title"));
		
		pack();
		
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		
		Dimension mySize = getSize();
		
		setLocation((screenSize.width-mySize.width)/2,(screenSize.height-mySize.height)/2);
		setResizable(false);
		setVisible(true);
	}

	
	
	private JPanel jContentPane = null;

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getPatientDataPanel(), java.awt.BorderLayout.NORTH);
			jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
		}
		return jContentPane;
	}
	
	
	private JPanel patientData=null;
	private boolean isMalnutrition = false;
	
	private JPanel getPatientDataPanel() {
		patientData = new JPanel();
		patientData.setLayout(new BorderLayout());
		
		patientData.add(getTablesPanel(), BorderLayout.EAST);
		
		PatientSummary ps = new PatientSummary(patient);
		for (Admission elem : admList) {
			if (elem.getType().equalsIgnoreCase("M")) {
				isMalnutrition = true;
				break;
			}
		}
		patientData.add(ps.getPatientCompleteSummary(), BorderLayout.WEST);

		return patientData;
	}

	private List<Admission> admList;
	private List<Disease> disease;
	private List<Ward> ward;
	private List<Opd> opdList;

	private String[] pColumns = {
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.ward.txt").toUpperCase(),
			MessageBundle.getMessage("angal.admission.diagnosisin.col").toUpperCase(),
			MessageBundle.getMessage("angal.admission.diagnosisout.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.status.txt").toUpperCase()
	};
	private int[] pColumnWidth = {120, 150, 200, 200, 120};
	
	private DefaultTableModel admModel;

	private JTable admTable;
	private TableSorter sorter;
	
	private JScrollPane scrollPane;
	
	private JPanel tablesPanel=null;
	
	private JPanel getTablesPanel() {
		tablesPanel = new JPanel(new BorderLayout());
		
		admModel = new AdmissionBrowserModel();
		sorter = new TableSorter(admModel);
		admTable = new JTable(sorter);      
		//sorter.addMouseListenerToHeaderInTable(admTable); no needed
		sorter.sortByColumn(0, false); //sort by first column, descending
				
		for (int i = 0; i< pColumns.length; i++) {
			admTable.getColumnModel().getColumn(i).setPreferredWidth(pColumnWidth[i]);
			if (i == 0 || i == 4) {
				admTable.getColumnModel().getColumn(i).setCellRenderer(new DateCellRenderer());
			}
		}

		scrollPane = new JScrollPane(admTable);
		scrollPane.setPreferredSize(new Dimension(500, 440));
		tablesPanel.add(scrollPane, BorderLayout.CENTER);

		return tablesPanel;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel;
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		if (MainMenu.checkUserGrants("btndataedit")) {
			buttonPanel.add(getEditButton(), null);
		}
		if (MainMenu.checkUserGrants("btndatadel")) {
			buttonPanel.add(getDeleteButton(), null);
		}
		if (MainMenu.checkUserGrants("btndatamalnut")) {
			buttonPanel.add(getMalnutritionButton(), null);
		}
		buttonPanel.add(getCloseButton(), null);
		return buttonPanel;
	}

	
	private JButton closeButton=null;
	private JButton editButton=null;
	private JButton deleteButton=null;
	private JButton malnutritionButton=null;
	
	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			closeButton.addActionListener(actionEvent -> dispose());
		}
		return closeButton;
	}

	private JButton getEditButton() {
		if (editButton == null) {
			editButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			editButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			editButton.addActionListener(actionEvent -> {
				if (admTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					return;
				}

				int selectedRow = admTable.getSelectedRow();
				Object selectedObj = sorter.getValueAt(selectedRow, -1);

				if (selectedObj instanceof Admission) {
					Admission ad = (Admission) sorter.getValueAt(selectedRow, -1);
					new AdmissionBrowser(PatientDataBrowser.this, admittedPatientWindow, patient, ad);
				} else {

					Opd opd = (Opd) sorter.getValueAt(selectedRow, -1);
					if (GeneralData.OPDEXTENDED) {
						OpdEditExtended newrecord = new OpdEditExtended(PatientDataBrowser.this, opd, false);
						newrecord.showAsModal(PatientDataBrowser.this);
					} else {
						OpdEdit newrecord = new OpdEdit(PatientDataBrowser.this, opd, false);
						newrecord.setVisible(true);
					}
				}
			});
		}
		return editButton;
	}

	private JButton getDeleteButton() {
		if (deleteButton == null) {
			deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			deleteButton.addActionListener(actionEvent -> {
				if (admTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					return;
				}

				int selectedRow = admTable.getSelectedRow();
				Object selectedObj = sorter.getValueAt(selectedRow, -1);

				try {
					if (selectedObj instanceof Admission) {

						Admission adm = (Admission) sorter.getValueAt(selectedRow, -1);

						int n = MessageDialog.yesNo(null,"angal.admission.deleteselectedadmission.msg");
						if ((n == JOptionPane.YES_OPTION) && admissionManager.setDeleted(adm.getId())) {
							admList.remove(adm);
							admModel.fireTableDataChanged();
							admTable.updateUI();
							sorter.sortByColumn(0, false);
							if (adm.getAdmitted() == 1) {
								fireDeleteAdmissionUpdated(adm);
							}
							PatientDataBrowser.this.requestFocus();
						}
					} else {
						Opd opd = (Opd) sorter.getValueAt(selectedRow, -1);
						OpdBrowserManager delOpd = Context.getApplicationContext().getBean(OpdBrowserManager.class);

						int n = MessageDialog.yesNo(null,"angal.admission.deleteselectedopd.msg");
						if ((n == JOptionPane.YES_OPTION) && (delOpd.deleteOpd(opd))) {
							opdList.remove(opd);
							admModel.fireTableDataChanged();
							admTable.updateUI();
							sorter.sortByColumn(0, false);
						}
					}
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
			});
		}
		return deleteButton;
	}
	
	private JButton getMalnutritionButton() {
		if (malnutritionButton == null) {			
			malnutritionButton = new JButton(MessageBundle.getMessage("angal.admission.malnutritioncontrol.btn"));
			malnutritionButton.setMnemonic(MessageBundle.getMnemonic( "angal.admission.malnutritioncontrol.btn.key"));
			malnutritionButton.addActionListener(actionEvent -> {
				if (admTable.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectarow.msg");
					return;
				}
				int selectedRow = admTable.getSelectedRow();
				Object selectedObj = sorter.getValueAt(selectedRow, -1);

				if (selectedObj instanceof Admission) {
					Admission ad = (Admission) sorter.getValueAt(selectedRow, -1);
					if (ad.getType().equalsIgnoreCase("M")) {
						new MalnutritionBrowser(PatientDataBrowser.this, ad);
					}
					else {
						MessageDialog.info(null, "angal.admission.theselectedadmissionhasnoconcernwithmalnutrition.msg");
					}
				} else {
					MessageDialog.info(null,"angal.admission.opdhasnoconcernwithmalnutrition.msg");
				}
			});
		}
		return malnutritionButton;	
	}
	
class AdmissionBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = -453243229156512947L;
		private AdmissionBrowserManager admissionBrowserManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
		private DiseaseBrowserManager diseaseBrowserManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);

		public AdmissionBrowserModel() {
			WardBrowserManager wbm = Context.getApplicationContext().getBean(WardBrowserManager.class);
			OpdBrowserManager opd = Context.getApplicationContext().getBean(OpdBrowserManager.class);
			try {
				opdList = opd.getOpdList(patient.getCode());
			} catch(OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			try {
				admList = admissionBrowserManager.getAdmissions(patient);
			} catch(OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			try {
				ward = wbm.getWards();
			} catch(OHServiceException e) {
                OHServiceExceptionUtil.showMessages(e);
			}
			try {
				disease = diseaseBrowserManager.getDiseaseAll();
			} catch(OHServiceException e) {
                OHServiceExceptionUtil.showMessages(e);
			}
		}
		
		
		@Override
		public int getRowCount() {
			int count = 0;
			if (admList != null) {
				count += admList.size();
			}
			if (opdList != null) {
				count += opdList.size();
			}
			return count;
		}

		@Override
		public String getColumnName(int c) {
			return pColumns[c];
		}

		@Override
		public int getColumnCount() {
			return pColumns.length;
		}	
		
		
		@Override
		public Object getValueAt(int row, int column) {
			if (column == -1) {
				if (row < admList.size())	{
					return admList.get(row);
				} else {
					int z = row - admList.size();
					return opdList.get(z);
				}
			
			} else if (column == 0) {
				if (row < admList.size()) {

					LocalDateTime myDate = admList.get(row).getAdmDate();
					return myDate.format(DATE_FORMATTER);
					
				} else {
					
					int z = row - admList.size();
					LocalDateTime myDate = opdList.get(z).getDate();
					return myDate.format(DATE_FORMATTER);
				}
				
			} else if (column == 1) {				
				if (row < admList.size()) {
					String id = admList.get(row).getWard().getCode();
					for (Ward elem : ward) {
						if (elem.getCode().equalsIgnoreCase(id)) {
							return elem.getDescription();
						}
					}
				} else {
					return "OPD";
				}
			}
			else if (column == 2) {
				String id;
				if (row < admList.size()) {
					id = admList.get(row).getDiseaseIn().getCode();
				} else {
					int z = row - admList.size();
					id = opdList.get(z).getDisease().getCode();
				}
				if (id == null) {
					id = "";
				}
				for (Disease elem : disease) {
					if (elem.getCode().equalsIgnoreCase(id)) {
						return elem.getDescription();
					}
				}
				return MessageBundle.getMessage("angal.admission.nodisease.txt");

			} else if (column == 3) {
				String id;
				if (row < admList.size()) {
					id = admList.get(row).getDiseaseOut1() == null ? null :  admList.get(row).getDiseaseOut1().getCode();
					if (id == null) {
						id = "";
					}
				} else {
					int z = row - admList.size();
					Disease dis = opdList.get(z).getDisease3();
					if (dis == null) {
						dis = opdList.get(z).getDisease2();
						if (dis == null) {
							id = opdList.get(z).getDisease().getCode();
						} else {
							id = dis.getCode();
						}
					} else {
						id = dis.getCode();
					}
				}
				for (Disease elem : disease) {
					if (elem.getCode().equalsIgnoreCase(id)) {
						return elem.getDescription();
					}
				}				
				return MessageBundle.getMessage("angal.admission.nodisease.txt");
				
			}  else if (column == 4) {
				if (row < admList.size()) {
					if (admList.get(row).getDisDate() == null) {
						return MessageBundle.getMessage("angal.admission.present.txt");
					} else {
						return admList.get(row).getDisDate();
					}
				} else {
					int z = row - admList.size();
					String status = "" + opdList.get(z).getNewPatient();
					return (status.compareTo("R") == 0
							? MessageBundle.getMessage("angal.opd.reattendance.txt")
							: MessageBundle.getMessage("angal.opd.newattendance.txt"));
				}
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	public class DateCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
			
			if (value instanceof Date) {
				// Use SimpleDateFormat class to get a formatted String from Date object.
				String strDate = new SimpleDateFormat(DATE_FORMAT_DD_MM_YY).format((Date)value);
				
				// Sorting algorithm will work with model value. So you dont need to worry
				// about the renderer's display value. 
				this.setText(strDate);
			}
			return this;
		}
	}
	
}
