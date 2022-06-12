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
package org.isf.opd.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY_HH_MM;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.EventListenerList;

import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.distype.manager.DiseaseTypeBrowserManager;
import org.isf.distype.model.DiseaseType;
import org.isf.examination.gui.PatientExaminationEdit;
import org.isf.examination.manager.ExaminationBrowserManager;
import org.isf.examination.model.GenderPatientExamination;
import org.isf.examination.model.PatientExamination;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.opd.manager.OpdBrowserManager;
import org.isf.opd.model.Opd;
import org.isf.operation.gui.OperationRowOpd;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.RememberDates;
import org.isf.utils.time.TimeTools;
import org.isf.visits.manager.VisitManager;
import org.isf.visits.model.Visit;

/**
 * ------------------------------------------
 * OpdEditExtended - add/edit an OPD registration
 * -----------------------------------------
 * modification history
 * 11/12/2005 - Vero, Rick  - first beta version
 * 07/11/2006 - ross - renamed from Surgery
 *                   - added visit date, disease 2, disease 3
 *                   - disease is not mandatory if re-attendance
 * 			         - version is now 1.0
 * 28/05/2008 - ross - added referral to / referral from check boxes
 * 12/06/2008 - ross - added patient data
 * 					 - fixed error on checking "male"/"female" option: should check after translation
 * 					 - version is not a resource into the bundle, is locale to the form
 *                   - form rearranged in x,y coordinates
 * 			         - version is now 1.1
 * 26/08/2008 - teo  - added patient chooser
 * 01/09/2008 - alex - added constructor for call from Admission
 * 					 - set Patient oriented OPD
 * 					 - history management for the patients
 * 					 - version now is 1.2
 * 01/01/2009 - Fabrizio - modified age fields back to Integer type
 * 13/02/2009 - Alex - added possibility to edit patient through EditButton
 * 					   added Edit.png icon
 * 					   fixed a bug on the first element in the comboBox
 * 13/02/2009 - Alex - added trash button for resetting searchfield
 * 03/13/2009 - Alex - lastOpdVisit appears at the bottom
 * 					   added control on duplicated diseases
 * 					   added re-attendance checkbox for a clear view
 * 					   new/re-attendance managed freely
 * 07/13/2009 - Alex - note field for the visit recall last visit note when start OPD from
 *  				   Admission and added Note even in Last OPD Visit
 *	  				   Extended patient search to patient code
 * ------------------------------------------
 * */
public class OpdEditExtended extends ModalJFrame implements PatientInsertExtended.PatientListener, PatientInsert.PatientListener, ActionListener {

	private static final long serialVersionUID = 1L;

	@Override
	public void patientInserted(AWTEvent e) {
		opdPatient = (Patient) e.getSource();
		setPatient(opdPatient);
		jComboPatResult.addItem(opdPatient);
		jComboPatResult.setSelectedItem(opdPatient);
		jPatientEditButton.setEnabled(true);
	}

	@Override
	public void patientUpdated(AWTEvent e) {
		setPatient(opdPatient);
	}

	private EventListenerList surgeryListeners = new EventListenerList();
	
	public interface SurgeryListener extends EventListener {
		void surgeryUpdated(AWTEvent e, Opd opd);
		void surgeryInserted(AWTEvent e, Opd opd);
	}
	
	public void addSurgeryListener(SurgeryListener l) {
		surgeryListeners.add(SurgeryListener.class, l);
	}
	
	public void removeSurgeryListener(SurgeryListener listener) {
		surgeryListeners.remove(SurgeryListener.class, listener);
	}

	private void fireSurgeryInserted(Opd opd) {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = surgeryListeners.getListeners(SurgeryListener.class);
		for (EventListener listener : listeners) {
			((SurgeryListener) listener).surgeryInserted(event, opd);
		}
	}

	private void fireSurgeryUpdated(Opd opd) {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = surgeryListeners.getListeners(SurgeryListener.class);
		for (EventListener listener : listeners) {
			((SurgeryListener) listener).surgeryUpdated(event, opd);
		}
	}

	private static final String LAST_OPD_LABEL = "<html><i>" + MessageBundle.getMessage("angal.opd.lastopdvisitm.txt") + "</i></html>:";
	private static final String LAST_NOTE_LABEL = "<html><i>" + MessageBundle.getMessage("angal.opd.lastopdnote.txt") + "</i></html>:";

	private JPanel jPanelMain = null;
	private JPanel jPanelNorth;
	private JPanel jPanelCentral;
	private JPanel jPanelData = null;
	private JPanel jPanelButtons = null;
	private JLabel jLabelDate = null;
	private JLabel jLabelDiseaseType1 = null;
	private JLabel jLabelDisease1 = null;
	private JLabel jLabelDis2 = null;
	private JLabel jLabelDis3 = null;

	private JComboBox diseaseTypeBox = null;
	private JComboBox diseaseBox1 = null;
	private JComboBox diseaseBox2 = null;
	private JComboBox diseaseBox3 = null;
	private JLabel jLabelAge = null;
	private JLabel jLabelSex = null;
	private LocalDateTime visitDateOpd = null;
	private DateTimeFormatter currentDateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY_HH_MM, new Locale(GeneralData.LANGUAGE));
	private GoodDateTimeChooser opdDateFieldCal = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private JButton jButtonExamination = null;
	private JCheckBox rePatientCheckBox = null;
	private JCheckBox newPatientCheckBox = null;
	private JCheckBox referralToCheckBox = null;
	private JCheckBox referralFromCheckBox = null;
	private JPanel jPanelSex = null;
	private ButtonGroup group = null;

	private JLabel jLabelFirstName = null;
	private JLabel jLabelSecondName = null;
	private JLabel jLabelAddress = null;
	private JLabel jLabelCity = null;
	private JLabel jLabelNextKin = null;

	private JPanel jPanelPatient = null;

	private VoLimitedTextField jFieldFirstName = null;
	private VoLimitedTextField jFieldSecondName = null;
	private VoLimitedTextField jFieldAddress = null;
	private VoLimitedTextField jFieldCity = null;
	private VoLimitedTextField jFieldNextKin = null;
	private VoLimitedTextField jFieldAge = null;

	private Opd opd;
	private boolean insert;
	private DiseaseType allType = new DiseaseType(MessageBundle.getMessage("angal.common.alltypes.txt"), MessageBundle.getMessage("angal.common.alltypes.txt"));

	private VoLimitedTextField jTextPatientSrc;
	private JComboBox jComboPatResult;
	private JLabel jSearchLabel = null;
	private JRadioButton radiof;
	private JRadioButton radiom;
	private JButton jPatientEditButton = null;
	private JButton jSearchButton = null;
	private JLabel jLabelLastOpdVisit = null;
	private JLabel jFieldLastOpdVisit = null;
	private JLabel jLabelLastOpdNote = null;
	private JLabel jFieldLastOpdNote = null;

	private Patient opdPatient = null;
	private JPanel jNotePanel = null;
	private JScrollPane jNoteScrollPane = null;
	private JTextArea jNoteTextArea = null;
	private JPanel jPatientNotePanel = null;
	private JScrollPane jPatientScrollNote = null;
	private JTextArea jPatientNote = null;
	private JPanel jOpdNumberPanel = null;
	private JTextField jOpdNumField = null;

	/*
	 * Managers and Arrays
	 */
	private DiseaseTypeBrowserManager diseaseTypeManager = Context.getApplicationContext().getBean(DiseaseTypeBrowserManager.class);
	private DiseaseBrowserManager diseaseManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
	private OpdBrowserManager opdManager = Context.getApplicationContext().getBean(OpdBrowserManager.class);
	private PatientBrowserManager patManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private VisitManager visitManager = Context.getApplicationContext().getBean(VisitManager.class);
	private List<DiseaseType> types;
	private List<Disease> diseasesOPD;
	private List<Disease> diseasesAll;
	private List<Patient> pat = new ArrayList<>();

	private Disease lastOPDDisease1;
	private JLabel JlabelOpd;

	/*
	 * Adds: Textfields and buttons to enable search in diagnosis
	 */
	private JTextField searchDiseaseTextField;
	private JTextField searchDiseaseTextField2;
	private JTextField searchDiseaseTextField3;
	private JButton searchDiseaseButton;
	private JButton searchDiseaseButton2;
	private JButton searchDiseaseButton3;

	private OperationRowOpd operationop;
	private JTabbedPane jTabbedPaneOpd;
	private JPanel jPanelOperation;

	/**
	 * Opd next visit fields
	 */
	private JLabel nextVisitLabel;
	private GoodDateTimeChooser opdNextVisitDate;
	private LocalDateTime nextDateBackup; //TODO: Workaround for update, a better solution must be found here

	/**
	 * This method initializes
	 *
	 * @wbp.parser.constructor
	 */
	public OpdEditExtended(JFrame owner, Opd old, boolean inserting) {
		super();
		opd = old;
		insert = inserting;
		try {
			types = diseaseTypeManager.getDiseaseType();
			diseasesOPD = diseaseManager.getDiseaseOpd();
			diseasesAll = diseaseManager.getDiseaseAll();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		try {
			if (!insert) {
				opdPatient = opd.getPatient();
				if (opdPatient != null && opd.getPatient().getCode() != 0) {
					PatientBrowserManager patBrowser = Context.getApplicationContext().getBean(PatientBrowserManager.class);
					opdPatient = patBrowser.getPatientAll(opd.getPatient().getCode());
				} else { //old OPD has no PAT_ID => Create Patient from OPD
					opdPatient = new Patient(opd);
					opdPatient.setCode(0);
				}
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		initialize();
	}
	
	public OpdEditExtended(JFrame owner, Opd opd, Patient patient, boolean inserting) {
		super();
		this.opd = opd;
		opdPatient = patient;
		insert = inserting;
		try {
			types = diseaseTypeManager.getDiseaseType();
			diseasesOPD = diseaseManager.getDiseaseOpd();
			diseasesAll = diseaseManager.getDiseaseAll();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		try {
			if (!insert) {
				opdPatient = opd.getPatient();
				if (opdPatient != null && opd.getPatient().getCode() != 0) { 
					PatientBrowserManager patBrowser = Context.getApplicationContext().getBean(PatientBrowserManager.class);
					opdPatient = patBrowser.getPatientAll(opd.getPatient().getCode());
				} else { //old OPD has no PAT_ID => Create Patient from OPD
					opdPatient = new Patient(opd);
					opdPatient.setCode(0);
				}
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		initialize();
	}
	
	private void setPatient(Patient p) {
			jFieldAge.setText(TimeTools.getFormattedAge(p.getBirthDate()));
			jFieldFirstName.setText(p.getFirstName());
			jFieldAddress.setText(p.getAddress());
			jFieldCity.setText(p.getCity());
			jFieldSecondName.setText(p.getSecondName());
			jFieldNextKin.setText(p.getNextKin());
			jPatientNote.setText(opdPatient.getNote());
			setMyMatteBorder(jPanelPatient, MessageBundle.formatMessage("angal.opd.patientcode.fmt.msg", opdPatient.getCode()));
			if (p.getSex() == 'M') {
				radiom.setSelected(true);				
			} else if (p.getSex() == 'F') {
				radiof.setSelected(true);			
			}
			if (insert) {
				getLastOpd(p.getCode());
			}
			opdNextVisitDate.setEnabled(true);
	}
	
	private void resetPatient() {
		jFieldAge.setText("");
		jFieldFirstName.setText("");
		jFieldAddress.setText("");
		jFieldCity.setText("");
		jFieldSecondName.setText("");
		jFieldNextKin.setText("");
		jPatientNote.setText("");
		setMyMatteBorder(jPanelPatient, MessageBundle.getMessage("angal.common.patient.txt"));
		radiom.setSelected(true);
		opdPatient = null;
		opdNextVisitDate.setEnabled(false);
	}
	
	//Alex: Resetting history from the last OPD visit for the patient
	private boolean getLastOpd(int code)
	{
		Opd lastOpd = null;
		try {
			lastOpd = opdManager.getLastOpd(code);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		
		if (lastOpd == null) {
			newPatientCheckBox.setSelected(true);
			rePatientCheckBox.setSelected(false);
			jLabelLastOpdVisit.setText("");
			jFieldLastOpdVisit.setText("");
			jLabelLastOpdNote.setText("");
			jFieldLastOpdNote.setText("");
			jNoteTextArea.setText("");
			return false;
		}
		
		lastOPDDisease1 = null;
		Disease lastOPDDisease2 = null;
		Disease lastOPDDisease3 = null;
		
		for (Disease disease : diseasesOPD) {
			
			if (lastOpd.getDisease() != null && disease.getCode().compareTo(lastOpd.getDisease().getCode()) == 0) {
					lastOPDDisease1 = disease;
			}
			if (lastOpd.getDisease2() != null && disease.getCode().compareTo(lastOpd.getDisease2().getCode()) == 0) {
				lastOPDDisease2 = disease;
			}
			if (lastOpd.getDisease3() != null && disease.getCode().compareTo(lastOpd.getDisease3().getCode()) == 0) {
				lastOPDDisease3 = disease;
			}
		}

		// TODO: this should be a formatted message in the bundle and not "appended" together
		StringBuilder lastOPDDisease = new StringBuilder();
		lastOPDDisease.append(MessageBundle.getMessage("angal.opd.on.txt")).append(" ").append(currentDateFormat.format(lastOpd.getDate())).append(" - ");
		if (lastOPDDisease1 != null) {
			setAttendance();
			lastOPDDisease.append(lastOPDDisease1.getDescription());
		} 
		if (lastOPDDisease2 != null) {
			lastOPDDisease.append(", ").append(lastOPDDisease2.getDescription());
		}
		if (lastOPDDisease3 != null) {
			lastOPDDisease.append(", ").append(lastOPDDisease3.getDescription());
		}
		jLabelLastOpdVisit.setText(LAST_OPD_LABEL);
		jFieldLastOpdVisit.setText(lastOPDDisease.toString());
		jLabelLastOpdNote.setText(LAST_NOTE_LABEL);
		String note = lastOpd.getNote();
		jFieldLastOpdNote.setText(note.equals("") ? MessageBundle.getMessage("angal.opd.none.txt") : note);
		jNoteTextArea.setText(lastOpd.getNote());
		
		return true;		
	}
	
	private void setAttendance() {
		if (!insert) {
			return;
		}
		Object selectedObject = diseaseBox1.getSelectedItem();
		if (selectedObject instanceof Disease) {
			Disease disease = (Disease) selectedObject;
			if (lastOPDDisease1 != null && disease.getCode().equals(lastOPDDisease1.getCode())) {
				rePatientCheckBox.setSelected(true);
				newPatientCheckBox.setSelected(false);
			} else {
				rePatientCheckBox.setSelected(false);
				newPatientCheckBox.setSelected(true);
			}
		}
	}

	/**
	 * @return the jPanelNorth
	 */
	private JPanel getjPanelNorth() {
		if (jPanelNorth == null) {
			String referralTo;
			String referralFrom;
			jPanelNorth = new JPanel(new FlowLayout());
			rePatientCheckBox = new JCheckBox(MessageBundle.getMessage("angal.opd.reattendance.txt"));
			newPatientCheckBox = new JCheckBox(MessageBundle.getMessage("angal.opd.newattendance.txt"));
			newPatientCheckBox.addActionListener(actionEvent -> {
				if (newPatientCheckBox.isSelected()) {
					newPatientCheckBox.setSelected(true);
					rePatientCheckBox.setSelected(false);
				} else {
					newPatientCheckBox.setSelected(false);
					rePatientCheckBox.setSelected(true);
				}
			});
			rePatientCheckBox.addActionListener(actionEvent -> {
				if (rePatientCheckBox.isSelected()) {
					rePatientCheckBox.setSelected(true);
					newPatientCheckBox.setSelected(false);
				} else {
					newPatientCheckBox.setSelected(true);
					rePatientCheckBox.setSelected(false);
				}
			});
			jPanelNorth.add(rePatientCheckBox);
			jPanelNorth.add(newPatientCheckBox);
			if (!insert) {
				if (opd.getNewPatient() == 'N') {
					newPatientCheckBox.setSelected(true);
				} else {
					rePatientCheckBox.setSelected(true);
				}
			}
			referralFromCheckBox = new JCheckBox(MessageBundle.getMessage("angal.opd.referral.txt"));
			jPanelNorth.add(referralFromCheckBox);
			if (!insert) {
				referralFrom = opd.getReferralFrom();
				if (referralFrom == null) {
					referralFrom = "";
				}
				if (referralFrom.equals("R")) {
					referralFromCheckBox.setSelected(true);
				}
			}
			referralToCheckBox = new JCheckBox(MessageBundle.getMessage("angal.opd.referralto.txt"));
			jPanelNorth.add(referralToCheckBox);
			if (!insert) {
				referralTo = opd.getReferralTo();
				if (referralTo == null) {
					referralTo = "";
				}
				if (referralTo.equals("R")) {
					referralToCheckBox.setSelected(true);
				}
			}
		}
		return jPanelNorth;
	}

	/**
	 * @return the jPanelCentral
	 */
	private JPanel getjPanelCentral() {
		if (jPanelCentral == null) {
			jPanelCentral = new JPanel();
			jPanelCentral.setLayout(new BoxLayout(jPanelCentral, BoxLayout.Y_AXIS));
			jPanelCentral.add(getDataPanel());
			jPanelCentral.add(Box.createVerticalStrut(10));
			jPanelCentral.add(getJTabbedPaneOpd());
			
		}
		return jPanelCentral;
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setContentPane(getMainPanel());
		pack();
		setMinimumSize(this.getSize());
		setLocationRelativeTo(null);

		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.opd.newopdregistration.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.opd.editopdregistration.title"));
		}
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		if (insert) {
			jTextPatientSrc.requestFocusInWindow();
		} else {
			jNoteTextArea.requestFocusInWindow();
		}
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				//to free memory
				pat.clear();
				diseasesAll.clear();
				diseasesOPD.clear();
				types.clear();
				jComboPatResult.removeAllItems();
				diseaseTypeBox.removeAllItems();
				diseaseBox1.removeAllItems();
				diseaseBox2.removeAllItems();
				diseaseBox3.removeAllItems();
				dispose();
			}
		});
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMainPanel() {
		if (jPanelMain == null) {
			jPanelMain = new JPanel();
			jPanelMain.setLayout(new BorderLayout());
			jPanelMain.add(getjPanelNorth(), BorderLayout.NORTH);
			jPanelMain.add(getJNotePanel(), BorderLayout.EAST);
			jPanelMain.add(getjPanelCentral(), BorderLayout.CENTER);
			jPanelMain.add(getJButtonPanel(), BorderLayout.SOUTH);
		}
		return jPanelMain;
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDataPanel() {
		if (jPanelData == null) {
			jPanelData = new JPanel();
			GridBagLayout gblPanelData = new GridBagLayout();
			gblPanelData.columnWidths = new int[] { 80, 40, 20, 80, 20 };
			gblPanelData.rowHeights = new int[] { 20, 20, 20, 20, 20, 20, 20, 20 };
			gblPanelData.columnWeights = new double[] { 0.0, 0.1, 0.0, 1.0, 0.0 };
			gblPanelData.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
			jPanelData.setLayout(gblPanelData);

			jLabelDate = new JLabel(MessageBundle.getMessage("angal.opd.attendancedate.txt"));
			GridBagConstraints gbcLabelDate = new GridBagConstraints();
			gbcLabelDate.fill = GridBagConstraints.VERTICAL;
			gbcLabelDate.anchor = GridBagConstraints.WEST;
			gbcLabelDate.insets = new Insets(5, 5, 5, 5);
			gbcLabelDate.gridy = 0;
			gbcLabelDate.gridx = 0;
			jPanelData.add(jLabelDate, gbcLabelDate);
			GridBagConstraints gbcDateFieldCal = new GridBagConstraints();
			gbcDateFieldCal.weightx = 0.5;
			gbcDateFieldCal.fill = GridBagConstraints.HORIZONTAL;
			gbcDateFieldCal.insets = new Insets(5, 5, 5, 5);
			gbcDateFieldCal.gridy = 0;
			gbcDateFieldCal.gridx = 1;
			jPanelData.add(getOpdDateFieldCal(), gbcDateFieldCal);
			GridBagConstraints gbcOpdNumberPanel = new GridBagConstraints();
			gbcOpdNumberPanel.weightx = 0.5;
			gbcOpdNumberPanel.gridwidth = 1;
			gbcOpdNumberPanel.fill = GridBagConstraints.BOTH;
			gbcOpdNumberPanel.insets = new Insets(5, 5, 5, 5);
			gbcOpdNumberPanel.gridy = 0;
			gbcOpdNumberPanel.gridx = 3;
			jPanelData.add(getJOpdNumberPanel(), gbcOpdNumberPanel);
			GridBagConstraints gbcLabelOpd = new GridBagConstraints();
			gbcLabelOpd.insets = new Insets(0, 0, 5, 0);
			gbcLabelOpd.gridx = 4;
			gbcLabelOpd.gridy = 0;
			jPanelData.add(getJlabelOpd(), gbcLabelOpd);
			jSearchLabel = new JLabel(MessageBundle.getMessage("angal.common.search.txt"));
			GridBagConstraints gbcSearchLabel = new GridBagConstraints();
			gbcSearchLabel.fill = GridBagConstraints.VERTICAL;
			gbcSearchLabel.anchor = GridBagConstraints.WEST;
			gbcSearchLabel.insets = new Insets(5, 5, 5, 5);
			gbcSearchLabel.gridy = 1;
			gbcSearchLabel.gridx = 0;
			jPanelData.add(jSearchLabel, gbcSearchLabel);
			GridBagConstraints gbcTextPatientSrc = new GridBagConstraints();
			gbcTextPatientSrc.weightx = 0.5;
			gbcTextPatientSrc.fill = GridBagConstraints.HORIZONTAL;
			gbcTextPatientSrc.insets = new Insets(5, 5, 5, 5);
			gbcTextPatientSrc.gridy = 1;
			gbcTextPatientSrc.gridx = 1;
			jPanelData.add(getJTextPatientSrc(), gbcTextPatientSrc);
			GridBagConstraints gbcSearchButton = new GridBagConstraints();
			gbcSearchButton.insets = new Insets(5, 5, 5, 5);
			gbcSearchButton.gridy = 1;
			gbcSearchButton.gridx = 2;
			jPanelData.add(getJSearchButton(), gbcSearchButton);
			GridBagConstraints gbcSearchBox = new GridBagConstraints();
			gbcSearchBox.weightx = 0.5;
			gbcSearchBox.fill = GridBagConstraints.HORIZONTAL;
			gbcSearchBox.insets = new Insets(5, 5, 5, 5);
			gbcSearchBox.gridy = 1;
			gbcSearchBox.gridx = 3;
			jPanelData.add(getSearchBox(), gbcSearchBox);
			GridBagConstraints gbcPatientEditButton = new GridBagConstraints();
			gbcPatientEditButton.insets = new Insets(5, 5, 5, 0);
			gbcPatientEditButton.gridy = 1;
			gbcPatientEditButton.gridx = 4;
			jPanelData.add(getJPatientEditButton(), gbcPatientEditButton);

			jLabelDiseaseType1 = new JLabel(MessageBundle.getMessage("angal.opd.diseasetype.txt"));
			GridBagConstraints gbcLabelDiseaseType1 = new GridBagConstraints();
			gbcLabelDiseaseType1.fill = GridBagConstraints.VERTICAL;
			gbcLabelDiseaseType1.insets = new Insets(5, 5, 5, 5);
			gbcLabelDiseaseType1.anchor = GridBagConstraints.WEST;
			gbcLabelDiseaseType1.gridy = 2;
			gbcLabelDiseaseType1.gridx = 0;
			jPanelData.add(jLabelDiseaseType1, gbcLabelDiseaseType1);
			GridBagConstraints gbcDiseaseTypeBox = new GridBagConstraints();
			gbcDiseaseTypeBox.insets = new Insets(5, 5, 5, 0);
			gbcDiseaseTypeBox.fill = GridBagConstraints.HORIZONTAL;
			gbcDiseaseTypeBox.gridwidth = 3;
			gbcDiseaseTypeBox.gridy = 2;
			gbcDiseaseTypeBox.gridx = 1;
			jPanelData.add(getDiseaseTypeBox(), gbcDiseaseTypeBox);

			jLabelDisease1 = new JLabel(MessageBundle.getMessage("angal.opd.diagnosis.txt"));
			GridBagConstraints gbcLabelDisease1 = new GridBagConstraints();
			gbcLabelDisease1.fill = GridBagConstraints.VERTICAL;
			gbcLabelDisease1.insets = new Insets(5, 5, 5, 5);
			gbcLabelDisease1.anchor = GridBagConstraints.WEST;
			gbcLabelDisease1.gridy = 3;
			gbcLabelDisease1.gridx = 0;
			jPanelData.add(jLabelDisease1, gbcLabelDisease1);
			/////////////Search text field/////////////
			GridBagConstraints gbcSearchDiseaseTextField = new GridBagConstraints();
			gbcSearchDiseaseTextField.weightx = 0.5;
			gbcSearchDiseaseTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcSearchDiseaseTextField.insets = new Insets(5, 5, 5, 5);
			gbcSearchDiseaseTextField.gridy = 3;
			gbcSearchDiseaseTextField.gridx = 1;
			searchDiseaseTextField = new JTextField(10);
			searchDiseaseTextField.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						searchDiseaseButton.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});
			jPanelData.add(searchDiseaseTextField, gbcSearchDiseaseTextField);
			/////////////Search text button/////////////
			GridBagConstraints gbcSearchDiseaseButton = new GridBagConstraints();
			gbcSearchDiseaseButton.insets = new Insets(5, 5, 5, 5);
			gbcSearchDiseaseButton.gridy = 3;
			gbcSearchDiseaseButton.gridx = 2;
			searchDiseaseButton = new JButton();
			searchDiseaseButton.setPreferredSize(new Dimension(20, 20));
			searchDiseaseButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			searchDiseaseButton.addActionListener(this);
			jPanelData.add(searchDiseaseButton, gbcSearchDiseaseButton);
			/////////////Diseases combo/////////////
			GridBagConstraints gbcDiseaseBox1 = new GridBagConstraints();
			gbcDiseaseBox1.insets = new Insets(5, 5, 5, 5);
			gbcDiseaseBox1.fill = GridBagConstraints.HORIZONTAL;
			gbcDiseaseBox1.weightx = 0.5;
			gbcDiseaseBox1.gridy = 3;
			gbcDiseaseBox1.gridx = 3;
			jPanelData.add(getDiseaseBox1(), gbcDiseaseBox1);

			jLabelDis2 = new JLabel(MessageBundle.getMessage("angal.opd.diagnosisnfulllist2.txt"));
			GridBagConstraints gbcLabelDis2 = new GridBagConstraints();
			gbcLabelDis2.fill = GridBagConstraints.VERTICAL;
			gbcLabelDis2.insets = new Insets(5, 5, 5, 5);
			gbcLabelDis2.anchor = GridBagConstraints.WEST;
			gbcLabelDis2.gridy = 4;
			gbcLabelDis2.gridx = 0;
			jPanelData.add(jLabelDis2, gbcLabelDis2);
			/////////////Search text field/////////////
			GridBagConstraints gbcSearchDiseaseTextField2 = new GridBagConstraints();
			gbcSearchDiseaseTextField2.weightx = 0.5;
			gbcSearchDiseaseTextField2.fill = GridBagConstraints.HORIZONTAL;
			gbcSearchDiseaseTextField2.insets = new Insets(5, 5, 5, 5);
			gbcSearchDiseaseTextField2.gridy = 4;
			gbcSearchDiseaseTextField2.gridx = 1;
			searchDiseaseTextField2 = new JTextField(10);
			searchDiseaseTextField2.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						searchDiseaseButton2.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});
			jPanelData.add(searchDiseaseTextField2, gbcSearchDiseaseTextField2);
			/////////////Search text button/////////////
			GridBagConstraints gbcSearchDiseaseButton2 = new GridBagConstraints();
			gbcSearchDiseaseButton2.insets = new Insets(5, 5, 5, 5);
			gbcSearchDiseaseButton2.gridy = 4;
			gbcSearchDiseaseButton2.gridx = 2;
			searchDiseaseButton2 = new JButton();
			searchDiseaseButton2.setPreferredSize(new Dimension(20, 20));
			searchDiseaseButton2.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			searchDiseaseButton2.addActionListener(this);
			jPanelData.add(searchDiseaseButton2, gbcSearchDiseaseButton2);
			/////////////Diseases combo/////////////
			GridBagConstraints gbcDiseaseBox2 = new GridBagConstraints();
			gbcDiseaseBox2.insets = new Insets(5, 5, 5, 5);
			gbcDiseaseBox2.fill = GridBagConstraints.HORIZONTAL;
			gbcDiseaseBox2.weightx = 0.5;
			gbcDiseaseBox2.gridy = 4;
			gbcDiseaseBox2.gridx = 3;
			jPanelData.add(getDiseaseBox2(), gbcDiseaseBox2);

			jLabelDis3 = new JLabel(MessageBundle.getMessage("angal.opd.diagnosisnfulllist3.txt"));
			GridBagConstraints gbcLabelDisBox3 = new GridBagConstraints();
			gbcLabelDisBox3.fill = GridBagConstraints.VERTICAL;
			gbcLabelDisBox3.insets = new Insets(5, 5, 5, 5);
			gbcLabelDisBox3.anchor = GridBagConstraints.WEST;
			gbcLabelDisBox3.gridy = 5;
			gbcLabelDisBox3.gridx = 0;
			jPanelData.add(jLabelDis3, gbcLabelDisBox3);
			/////////////Search text field/////////////
			GridBagConstraints gbcSearchDiseaseTextField3 = new GridBagConstraints();
			gbcSearchDiseaseTextField3.weightx = 0.5;
			gbcSearchDiseaseTextField3.fill = GridBagConstraints.HORIZONTAL;
			gbcSearchDiseaseTextField3.insets = new Insets(5, 5, 5, 5);
			gbcSearchDiseaseTextField3.gridy = 5;
			gbcSearchDiseaseTextField3.gridx = 1;
			searchDiseaseTextField3 = new JTextField(10);
			searchDiseaseTextField3.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						searchDiseaseButton3.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});
			jPanelData.add(searchDiseaseTextField3, gbcSearchDiseaseTextField3);
			/////////////Search text button/////////////
			GridBagConstraints gbcSearchDiseaseButton3 = new GridBagConstraints();
			gbcSearchDiseaseButton3.insets = new Insets(5, 5, 5, 5);
			gbcSearchDiseaseButton3.gridy = 5;
			gbcSearchDiseaseButton3.gridx = 2;
			searchDiseaseButton3 = new JButton();
			searchDiseaseButton3.setPreferredSize(new Dimension(20, 20));
			searchDiseaseButton3.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			jPanelData.add(searchDiseaseButton3, gbcSearchDiseaseButton3);
			searchDiseaseButton3.addActionListener(this);
			/////////////Diseases combo/////////////
			GridBagConstraints gbcDiseaseBox3 = new GridBagConstraints();
			gbcDiseaseBox3.insets = new Insets(5, 5, 5, 5);
			gbcDiseaseBox3.fill = GridBagConstraints.HORIZONTAL;
			gbcDiseaseBox3.weightx = 0.5;
			gbcDiseaseBox3.gridy = 5;
			gbcDiseaseBox3.gridx = 3;
			jPanelData.add(getDiseaseBox3(), gbcDiseaseBox3);

			jLabelLastOpdVisit = new JLabel(" ");
			jLabelLastOpdVisit.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelLastOpdVisit.setForeground(Color.RED);
			GridBagConstraints gbcLabelLastOpdVisit = new GridBagConstraints();
			gbcLabelLastOpdVisit.fill = GridBagConstraints.HORIZONTAL;
			gbcLabelLastOpdVisit.insets = new Insets(5, 5, 5, 5);
			gbcLabelLastOpdVisit.anchor = GridBagConstraints.EAST;
			gbcLabelLastOpdVisit.gridy = 6;
			gbcLabelLastOpdVisit.gridx = 0;
			jPanelData.add(jLabelLastOpdVisit, gbcLabelLastOpdVisit);
			jFieldLastOpdVisit = new JLabel(" ");
			jFieldLastOpdVisit.setFocusable(false);
			GridBagConstraints gbcFieldLastOpdVisit = new GridBagConstraints();
			gbcFieldLastOpdVisit.insets = new Insets(5, 5, 5, 0);
			gbcFieldLastOpdVisit.fill = GridBagConstraints.HORIZONTAL;
			gbcFieldLastOpdVisit.gridwidth = 4;
			gbcFieldLastOpdVisit.gridy = 6;
			gbcFieldLastOpdVisit.gridx = 1;
			jPanelData.add(jFieldLastOpdVisit, gbcFieldLastOpdVisit);

			jLabelLastOpdNote = new JLabel(" ");
			jLabelLastOpdNote.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelLastOpdNote.setForeground(Color.RED);
			GridBagConstraints gbcLabelLastOpdNote = new GridBagConstraints();
			gbcLabelLastOpdNote.fill = GridBagConstraints.HORIZONTAL;
			gbcLabelLastOpdNote.insets = new Insets(5, 5, 0, 5);
			gbcLabelLastOpdNote.anchor = GridBagConstraints.EAST;
			gbcLabelLastOpdNote.gridy = 7;
			gbcLabelLastOpdNote.gridx = 0;
			jPanelData.add(jLabelLastOpdNote, gbcLabelLastOpdNote);
			jFieldLastOpdNote = new JLabel(" ");
			jFieldLastOpdNote.setPreferredSize(new Dimension(500, 30));
			jFieldLastOpdNote.setFocusable(false);
			GridBagConstraints gbcFieldLastOpdNote = new GridBagConstraints();
			gbcFieldLastOpdNote.anchor = GridBagConstraints.WEST;
			gbcFieldLastOpdNote.insets = new Insets(5, 5, 0, 0);
			gbcFieldLastOpdNote.gridwidth = 4;
			gbcFieldLastOpdNote.gridy = 7;
			gbcFieldLastOpdNote.gridx = 1;
			jPanelData.add(jFieldLastOpdNote, gbcFieldLastOpdNote);

			GridBagConstraints gbcNextVisitLabel = new GridBagConstraints();
			gbcNextVisitLabel.insets = new Insets(0, 0, 0, 5);
			gbcNextVisitLabel.gridx = 0;
			gbcNextVisitLabel.gridy = 8;
			jPanelData.add(getNextVisitLabel(), gbcNextVisitLabel);
			GridBagConstraints gbcOpdNextVisitDate = new GridBagConstraints();
			gbcOpdNextVisitDate.insets = new Insets(0, 0, 0, 5);
			gbcOpdNextVisitDate.fill = GridBagConstraints.BOTH;
			gbcOpdNextVisitDate.gridx = 1;
			gbcOpdNextVisitDate.gridy = 8;
			jPanelData.add(getOpdNextVisitDate(), gbcOpdNextVisitDate);
		}
		return jPanelData;
	}

	private GoodDateTimeChooser getOpdDateFieldCal() {
		if (opdDateFieldCal == null) {
			if (insert) {
				if (RememberDates.getLastOpdVisitDate() == null) {
					visitDateOpd = LocalDateTime.now();
				} else {
					visitDateOpd = RememberDates.getLastOpdVisitDate();
				}
			} else {
				visitDateOpd  = opd.getDate();
			}
			opdDateFieldCal = new GoodDateTimeChooser(visitDateOpd);
			opdDateFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
		}
		return opdDateFieldCal;
	}
	
	private JPanel getJOpdNumberPanel() {
		if (jOpdNumberPanel == null) {
			jOpdNumberPanel = new JPanel();

			jOpdNumField = new JTextField(10);
			
			jOpdNumField.setFocusable(true);
			if (insert) {
				jOpdNumField.setText(String.valueOf(getOpdProgYear(RememberDates.getLastOpdVisitDate())));
			} else {
				jOpdNumField.setText(String.valueOf(opd.getProgYear()));
			}

			jOpdNumberPanel.add(new JLabel(MessageBundle.getMessage("angal.opd.opdnumber.txt")));
			jOpdNumberPanel.add(jOpdNumField);
		}
		return jOpdNumberPanel;
	}

	private int getOpdProgYear(LocalDateTime date) {
		int opdNum = 0;
		if (date == null) {
			date = LocalDateTime.now();
		}
		try {
			opdNum = opdManager.getProgYear(date.getYear()) + 1;
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (insert) {
			jOpdNumField.setEditable(opdNum == 1);
		}
		return opdNum;
	}

	private JPanel getJNotePanel() {
		if (jNotePanel == null) {
			jNotePanel = new JPanel();
			jNotePanel = setMyBorder(jNotePanel, MessageBundle.getMessage("angal.opd.notessymptom.txt"));
			jNoteScrollPane = new JScrollPane(getJTextArea());
			jNoteScrollPane.setVerticalScrollBar(new JScrollBar());
			jNoteScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			jNoteScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jNoteScrollPane.validate();
			jNotePanel.setLayout(new BorderLayout(0, 0));
			jNotePanel.add(jNoteScrollPane);
		}
		return jNotePanel;
	}
	
	private JTextArea getJTextArea() {
		if (jNoteTextArea == null) {
			jNoteTextArea = new JTextArea(15, 20);
			jNoteTextArea.setAutoscrolls(true);
			if (!insert) {
				jNoteTextArea.setText(opd.getNote());
			}
			jNoteTextArea.setWrapStyleWord(true);
			jNoteTextArea.setLineWrap(true);
		}
		return jNoteTextArea;
	}

	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDiseaseTypeBox() {
		if (diseaseTypeBox == null) {
			diseaseTypeBox = new JComboBox();

			DiseaseType elem2 = null;
			diseaseTypeBox.setMaximumSize(new Dimension(400, 50));
			diseaseTypeBox.addItem(allType);
			for (DiseaseType elem : types) {
				if (!insert && opd.getDisease().getType() != null) {
					if (opd.getDisease().getType().getCode().equals(elem.getCode())) {
						elem2 = elem;
					}
				}
				diseaseTypeBox.addItem(elem);
			}
			if (elem2 != null) {
				diseaseTypeBox.setSelectedItem(elem2);
			} else {
				diseaseTypeBox.setSelectedIndex(0);
			}
			diseaseTypeBox.addActionListener(actionEvent -> {
				diseaseBox1.removeAllItems();
				getDiseaseBox1();
			});
		}
		return diseaseTypeBox;
	}
	
	/**
	 * This method initializes jComboBox1	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDiseaseBox1() {
		if (diseaseBox1 == null) {
			diseaseBox1 = new JComboBox();
			diseaseBox1.setMaximumSize(new Dimension(400, 50));
			diseaseBox1.addActionListener(actionEvent -> setAttendance());
		}
		Disease elem2 = null;
		diseaseBox1.addItem("");

		for (Disease elem : diseasesOPD) {
			if (diseaseTypeBox.getSelectedItem().equals(allType)) {
				diseaseBox1.addItem(elem);
			} else if (elem.getType().equals(diseaseTypeBox.getSelectedItem())) {
				diseaseBox1.addItem(elem);
			}
			if (!insert && opd.getDisease() != null) {
				if (opd.getDisease().getCode().equals(elem.getCode())) {
					elem2 = elem;
				}
			}
		}
		if (!insert) {
			if (elem2 != null) {
				diseaseBox1.setSelectedItem(elem2);
			} else { //try in the canceled diseases
				if (opd.getDisease() != null) {
					for (Disease elem : diseasesAll) {
						if (opd.getDisease().getCode().equals(elem.getCode())) {
							MessageDialog.warning(OpdEditExtended.this, "angal.opd.disease1mayhavebeencancelled.msg");
							diseaseBox1.addItem(elem);
							diseaseBox1.setSelectedItem(elem);
						}
					}
				}
			}
		}
		return diseaseBox1;
	}
	
	public JComboBox getDiseaseBox2() {
		if (diseaseBox2 == null) {
			diseaseBox2 = new JComboBox();
			diseaseBox2.setMaximumSize(new Dimension(400, 50));
		}
		Disease elem2=null;
		diseaseBox2.addItem("");

		for (Disease elem : diseasesOPD) {
			diseaseBox2.addItem(elem);
			if (!insert && opd.getDisease2() != null) {
				if (opd.getDisease2().getCode().equals(elem.getCode())) {
					elem2 = elem;
				}
			}
		}
		if (elem2 !=  null) {
			diseaseBox2.setSelectedItem(elem2);
		} else { //try in the canceled diseases
			if (opd.getDisease2()!=null) {
				for (Disease elem : diseasesAll) {
					
					if (opd.getDisease2().getCode().equals(elem.getCode())) {
						MessageDialog.warning(OpdEditExtended.this, "angal.opd.disease2mayhavebeencancelled.msg");
						diseaseBox2.addItem(elem);
						diseaseBox2.setSelectedItem(elem);
					}
				}
			}
		}
		return diseaseBox2;
	}

	private VoLimitedTextField getJTextPatientSrc() {
		if (jTextPatientSrc == null) {
			jTextPatientSrc = new VoLimitedTextField(16, 20);
			jTextPatientSrc.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						jSearchButton.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});
		}
		return jTextPatientSrc;
	}
	
	private JButton getJSearchButton() {
		if (jSearchButton == null) {
			jSearchButton = new JButton();
			jSearchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			jSearchButton.setBorderPainted(false);
			jSearchButton.setPreferredSize(new Dimension(20, 20));
			jSearchButton.addActionListener(actionEvent -> {
				jComboPatResult.removeAllItems();
				try {
					pat = patManager.getPatientsByOneOfFieldsLike(jTextPatientSrc.getText());
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
					pat = new ArrayList<>();
				}
				getSearchBox(jTextPatientSrc.getText());
			});
		}
		return jSearchButton;
	}

	private void getSearchBox(String key) {
		String[] s1;

		if (key == null || key.compareTo("") == 0) {
			jComboPatResult.addItem(MessageBundle.getMessage("angal.opd.selectapatient.txt"));
			jComboPatResult.addItem(MessageBundle.getMessage("angal.opd.enteranewpatient.txt"));
			jLabelLastOpdVisit.setText(" ");
			jFieldLastOpdVisit.setText(" ");
			jLabelLastOpdNote.setText(" ");
			jFieldLastOpdNote.setText(" ");
			if (jNoteTextArea != null) {
				jNoteTextArea.setText("");
			}
			if (jPanelPatient != null) {
				resetPatient();
			}
		}

		for (Patient elem : pat) {
			if (key != null) {
				s1 = key.split(" ");
				String name = elem.getSearchString();
				int a = 0;
				for (String value : s1) {
					if (name.contains(value.toLowerCase())) {
						a++;
					}
				}
				if (a == s1.length) {
					jComboPatResult.addItem(elem);
				}
			} else {
				jComboPatResult.addItem(elem);
			}
		}
		//ADDED: Workaround for no items
		if (jComboPatResult.getItemCount() == 0) {
			opdPatient = null;
			if (jPanelPatient != null) {
				resetPatient();
			}
			jPatientEditButton.setEnabled(true);
		}
		//ADDED: Workaround for one item only
		if (jComboPatResult.getItemCount() == 1) {
			opdPatient = (Patient) jComboPatResult.getSelectedItem();
			setPatient(opdPatient);
			jPatientEditButton.setEnabled(true);
		}
		//ADDED: Workaround for first item
		if (jComboPatResult.getItemCount() > 0) {

			if (jComboPatResult.getItemAt(0) instanceof Patient) {
				opdPatient = (Patient) jComboPatResult.getItemAt(0);
				setPatient(opdPatient);
				jPatientEditButton.setEnabled(true);
			}
		}
		jTextPatientSrc.requestFocus();
	}
	
	private JComboBox getSearchBox() {
		if (jComboPatResult == null) {
			jComboPatResult = new JComboBox();
			if (opdPatient != null) {
				jComboPatResult.addItem(opdPatient);
				jComboPatResult.setEnabled(false);
				jTextPatientSrc.setEnabled(false);
				jSearchButton.setEnabled(false);
				return jComboPatResult;
			} else {
				jComboPatResult.addItem(MessageBundle.getMessage("angal.opd.selectapatient.txt"));
				jComboPatResult.addItem(MessageBundle.getMessage("angal.opd.enteranewpatient.txt"));
			}

			jComboPatResult.addActionListener(actionEvent -> {

				if (jComboPatResult.getSelectedItem() != null) {
					if (jComboPatResult.getSelectedItem().toString().compareTo(MessageBundle.getMessage("angal.opd.enteranewpatient.txt")) == 0) {
						if (GeneralData.PATIENTEXTENDED) {
							PatientInsertExtended newrecord = new PatientInsertExtended(OpdEditExtended.this, new Patient(), true);
							newrecord.addPatientListener(OpdEditExtended.this);
							newrecord.setVisible(true);
						} else {
							PatientInsert newrecord = new PatientInsert(OpdEditExtended.this, new Patient(), true);
							newrecord.addPatientListener(OpdEditExtended.this);
							newrecord.setVisible(true);
						}

					} else if (jComboPatResult.getSelectedItem().toString().compareTo(MessageBundle.getMessage("angal.opd.selectapatient.txt")) == 0) {
						jPatientEditButton.setEnabled(false);

					} else {
						opdPatient = (Patient) jComboPatResult.getSelectedItem();
						setPatient(opdPatient);
						jPatientEditButton.setEnabled(true);
					}
				}
			});
		}
		return jComboPatResult;
	}
	
	//ADDED: Alex
	private JButton getJPatientEditButton() {
		if (jPatientEditButton == null) {
			jPatientEditButton = new JButton();
			jPatientEditButton.setIcon(new ImageIcon("rsc/icons/edit_button.png"));
			jPatientEditButton.setBorderPainted(false);
			jPatientEditButton.setPreferredSize(new Dimension(20, 20));
			jPatientEditButton.addActionListener(actionEvent -> {
				if (opdPatient != null) {
					if (GeneralData.PATIENTEXTENDED) {
						PatientInsertExtended editrecord = new PatientInsertExtended(OpdEditExtended.this, opdPatient, false);
						editrecord.addPatientListener(OpdEditExtended.this);
						editrecord.setVisible(true);
					} else {
						PatientInsert editrecord = new PatientInsert(OpdEditExtended.this, opdPatient, false);
						editrecord.addPatientListener(OpdEditExtended.this);
						editrecord.setVisible(true);
					}
				}
			});
			if (!insert) {
				jPatientEditButton.setEnabled(false);
			}
		}	
		return jPatientEditButton;
	}
	
	private JComboBox getDiseaseBox3() {
		if (diseaseBox3 == null) {
			diseaseBox3 = new JComboBox();
			diseaseBox3.setMaximumSize(new Dimension(400, 50));
		}
		Disease elem2=null;
		diseaseBox3.addItem("");

		for (Disease elem : diseasesOPD) {
			diseaseBox3.addItem(elem);
			if (!insert && opd.getDisease3() != null) {
				if (opd.getDisease3().getCode().equals(elem.getCode())) {
					elem2 = elem;
				}
			}
		}
		if (elem2!= null) {
			diseaseBox3.setSelectedItem(elem2);
		} else { //try in the canceled diseases
			if (opd.getDisease3()!=null) {	
				for (Disease elem : diseasesAll) {
					if (opd.getDisease3().getCode().equals(elem.getCode())) {
						MessageDialog.warning(OpdEditExtended.this, "angal.opd.disease3mayhavebeencancelled.msg");
						diseaseBox3.addItem(elem);
						diseaseBox3.setSelectedItem(elem);
					}
				}
			}
		}
		return diseaseBox3;
	}

	private JTabbedPane getJTabbedPaneOpd() {
		if (jTabbedPaneOpd == null) {
			jTabbedPaneOpd = new JTabbedPane();
			jTabbedPaneOpd.addTab(MessageBundle.getMessage("angal.common.patient.txt"), getJPanelPatient());
			if ((insert && MainMenu.checkUserGrants("btnopdnewoperation"))
					|| (!insert && MainMenu.checkUserGrants("btnopdeditoperation"))) {
				jTabbedPaneOpd.addTab(MessageBundle.getMessage("angal.admission.operation"), getMultiOperationTab());
			}
			jTabbedPaneOpd.setPreferredSize(new Dimension(200, 400));
		}
		return jTabbedPaneOpd;
	}
	
	private JPanel getMultiOperationTab() {
		if (jPanelOperation == null) {
			jPanelOperation = new JPanel();
			jPanelOperation.setLayout(new BorderLayout(0, 0));
			operationop = new OperationRowOpd(opd);
			addSurgeryListener(operationop);
			jPanelOperation.add(operationop);
		}
		return jPanelOperation;
	}

	private JPanel getJPanelPatient() {
		if (jPanelPatient == null) {

			jPanelPatient = new JPanel();
			GridBagLayout gblPanelPatient = new GridBagLayout();
			gblPanelPatient.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
			gblPanelPatient.columnWeights = new double[] { 0.0, 1.0, 1.0 };
			gblPanelPatient.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
			jPanelPatient.setLayout(gblPanelPatient);
			setMyMatteBorder(jPanelPatient, MessageBundle.getMessage("angal.common.patient.txt"));

			jLabelFirstName = new JLabel(MessageBundle.getMessage("angal.opd.firstname.txt") + "\t");
			GridBagConstraints gbcLabelFirstName = new GridBagConstraints();
			gbcLabelFirstName.fill = GridBagConstraints.BOTH;
			gbcLabelFirstName.insets = new Insets(5, 5, 5, 5);
			gbcLabelFirstName.gridx = 0;
			gbcLabelFirstName.gridy = 0;
			jPanelPatient.add(jLabelFirstName, gbcLabelFirstName);
			jFieldFirstName = new VoLimitedTextField(50, 20);
			jFieldFirstName.setEditable(false);
			jFieldFirstName.setFocusable(false);
			GridBagConstraints gbcFieldFirstName = new GridBagConstraints();
			gbcFieldFirstName.insets = new Insets(5, 5, 5, 5);
			gbcFieldFirstName.fill = GridBagConstraints.BOTH;
			gbcFieldFirstName.gridx = 1;
			gbcFieldFirstName.gridy = 0;
			jPanelPatient.add(jFieldFirstName, gbcFieldFirstName);
			jLabelSecondName = new JLabel(MessageBundle.getMessage("angal.opd.secondname.txt") + "\t");
			GridBagConstraints gbcLabelSecondName = new GridBagConstraints();
			gbcLabelSecondName.insets = new Insets(5, 5, 5, 5);
			gbcLabelSecondName.fill = GridBagConstraints.BOTH;
			gbcLabelSecondName.gridx = 0;
			gbcLabelSecondName.gridy = 1;
			jPanelPatient.add(jLabelSecondName, gbcLabelSecondName);
			jFieldSecondName = new VoLimitedTextField(50, 20);
			jFieldSecondName.setEditable(false);
			jFieldSecondName.setFocusable(false);
			GridBagConstraints gbcFieldSecondName = new GridBagConstraints();
			gbcFieldSecondName.fill = GridBagConstraints.BOTH;
			gbcFieldSecondName.insets = new Insets(5, 5, 5, 5);
			gbcFieldSecondName.gridx = 1;
			gbcFieldSecondName.gridy = 1;
			jPanelPatient.add(jFieldSecondName, gbcFieldSecondName);
			jLabelAddress = new JLabel(MessageBundle.getMessage("angal.common.address.txt"));
			GridBagConstraints gbcLabelAddress = new GridBagConstraints();
			gbcLabelAddress.fill = GridBagConstraints.BOTH;
			gbcLabelAddress.insets = new Insets(5, 5, 5, 5);
			gbcLabelAddress.gridx = 0;
			gbcLabelAddress.gridy = 2;
			jPanelPatient.add(jLabelAddress, gbcLabelAddress);
			jFieldAddress = new VoLimitedTextField(50, 20);
			jFieldAddress.setEditable(false);
			jFieldAddress.setFocusable(false);
			GridBagConstraints gbcFieldAddress = new GridBagConstraints();
			gbcFieldAddress.fill = GridBagConstraints.BOTH;
			gbcFieldAddress.insets = new Insets(5, 5, 5, 5);
			gbcFieldAddress.gridx = 1;
			gbcFieldAddress.gridy = 2;
			jPanelPatient.add(jFieldAddress, gbcFieldAddress);
			jLabelCity = new JLabel(MessageBundle.getMessage("angal.common.city.txt"));
			GridBagConstraints gbcLabelCity = new GridBagConstraints();
			gbcLabelCity.fill = GridBagConstraints.BOTH;
			gbcLabelCity.insets = new Insets(5, 5, 5, 5);
			gbcLabelCity.gridx = 0;
			gbcLabelCity.gridy = 3;
			jPanelPatient.add(jLabelCity, gbcLabelCity);
			jFieldCity = new VoLimitedTextField(50, 20);
			jFieldCity.setEditable(false);
			jFieldCity.setFocusable(false);
			GridBagConstraints gbcFieldCity = new GridBagConstraints();
			gbcFieldCity.fill = GridBagConstraints.BOTH;
			gbcFieldCity.insets = new Insets(5, 5, 5, 5);
			gbcFieldCity.gridx = 1;
			gbcFieldCity.gridy = 3;
			jPanelPatient.add(jFieldCity, gbcFieldCity);
			jLabelNextKin = new JLabel(MessageBundle.getMessage("angal.opd.nextofkin.txt"));
			GridBagConstraints gbcLabelNextKin = new GridBagConstraints();
			gbcLabelNextKin.fill = GridBagConstraints.BOTH;
			gbcLabelNextKin.insets = new Insets(5, 5, 5, 5);
			gbcLabelNextKin.gridx = 0;
			gbcLabelNextKin.gridy = 4;
			jPanelPatient.add(jLabelNextKin, gbcLabelNextKin);
			jFieldNextKin = new VoLimitedTextField(50, 20);
			jFieldNextKin.setEditable(false);
			jFieldNextKin.setFocusable(false);
			GridBagConstraints gbcFieldNextKin = new GridBagConstraints();
			gbcFieldNextKin.fill = GridBagConstraints.BOTH;
			gbcFieldNextKin.insets = new Insets(5, 5, 5, 5);
			gbcFieldNextKin.gridx = 1;
			gbcFieldNextKin.gridy = 4;
			jPanelPatient.add(jFieldNextKin, gbcFieldNextKin);
			jLabelAge = new JLabel(MessageBundle.getMessage("angal.common.age.txt"));
			GridBagConstraints gbcLabelAge = new GridBagConstraints();
			gbcLabelAge.fill = GridBagConstraints.BOTH;
			gbcLabelAge.insets = new Insets(5, 5, 5, 5);
			gbcLabelAge.gridx = 0;
			gbcLabelAge.gridy = 5;
			jPanelPatient.add(jLabelAge, gbcLabelAge);
			jFieldAge = new VoLimitedTextField(50, 20);
			jFieldAge.setEditable(false);
			jFieldAge.setFocusable(false);
			GridBagConstraints gbcFieldAge = new GridBagConstraints();
			gbcFieldAge.fill = GridBagConstraints.BOTH;
			gbcFieldAge.insets = new Insets(5, 5, 5, 5);
			gbcFieldAge.gridx = 1;
			gbcFieldAge.gridy = 5;
			jPanelPatient.add(jFieldAge, gbcFieldAge);
			jLabelSex = new JLabel(MessageBundle.getMessage("angal.common.sex.txt"));
			GridBagConstraints gbcLabelSex = new GridBagConstraints();
			gbcLabelSex.fill = GridBagConstraints.HORIZONTAL;
			gbcLabelSex.insets = new Insets(5, 5, 5, 5);
			gbcLabelSex.gridx = 0;
			gbcLabelSex.gridy = 6;
			jPanelPatient.add(jLabelSex, gbcLabelSex);
			radiom = new JRadioButton(MessageBundle.getMessage("angal.common.male.btn"));
			radiof = new JRadioButton(MessageBundle.getMessage("angal.common.female.btn"));
			jPanelSex = new JPanel();
			jPanelSex.add(radiom);
			jPanelSex.add(radiof);
			GridBagConstraints gbcPanelSex = new GridBagConstraints();
			gbcPanelSex.insets = new Insets(5, 5, 5, 5);
			gbcPanelSex.fill = GridBagConstraints.HORIZONTAL;
			gbcPanelSex.gridx = 1;
			gbcPanelSex.gridy = 6;
			jPanelPatient.add(jPanelSex, gbcPanelSex);
			GridBagConstraints gbcPatientNote = new GridBagConstraints();
			gbcPatientNote.fill = GridBagConstraints.BOTH;
			gbcPatientNote.insets = new Insets(5, 5, 5, 5);
			gbcPatientNote.gridx = 2;
			gbcPatientNote.gridy = 0;
			gbcPatientNote.gridheight = 7;
			jPanelPatient.add(getJPatientNote(), gbcPatientNote);

			group = new ButtonGroup();
			group.add(radiom);
			group.add(radiof);
			radiom.setSelected(true);
			radiom.setEnabled(false);
			radiof.setEnabled(false);
			radiom.setFocusable(false);
			radiof.setFocusable(false);

			if (opdPatient != null) {
				setPatient(opdPatient);
			}
		}
		return jPanelPatient;
	}
	
	private JPanel getJPatientNote() {
		if (jPatientNotePanel == null) {
			jPatientNotePanel = new JPanel(new BorderLayout());
			jPatientScrollNote = new JScrollPane(getJPatientNoteArea());
			jPatientScrollNote.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			jPatientScrollNote.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jPatientScrollNote.setAutoscrolls(false);
			jPatientScrollNote.validate();
			jPatientNotePanel.add(jPatientScrollNote, BorderLayout.CENTER);
		}
		return jPatientNotePanel;
	}
	
	private JTextArea getJPatientNoteArea() {
		if (jPatientNote == null) {
			jPatientNote = new JTextArea(15, 15);
			if (!insert) {
				jPatientNote.setText(opdPatient.getNote());
			}
			jPatientNote.setLineWrap(true);
			jPatientNote.setEditable(false);
			jPatientNote.setFocusable(false);
		}
		return jPatientNote;
	}

	/**
	 * This method initializes jPanelButtons	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJButtonPanel() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getOkButton(), null);
			if (insert && MainMenu.checkUserGrants("btnopdnewexamination") || 
					!insert && MainMenu.checkUserGrants("btnopdeditexamination")) {
				jPanelButtons.add(getJButtonExamination(), null);
			}
			jPanelButtons.add(getCancelButton(), null);
		}
		return jPanelButtons;
	}
	
	private JButton getJButtonExamination() {
		if (jButtonExamination == null) {
			jButtonExamination = new JButton(MessageBundle.getMessage("angal.opd.examination.btn"));
			jButtonExamination.setMnemonic(MessageBundle.getMnemonic("angal.opd.examination.btn.key"));
			
			jButtonExamination.addActionListener(actionEvent -> {
				if (opdPatient == null) {
					MessageDialog.error(OpdEditExtended.this,"angal.common.pleaseselectapatient.msg");
					return;
				}

				ExaminationBrowserManager examManager = Context.getApplicationContext().getBean(ExaminationBrowserManager.class);
				PatientExamination patex;
				PatientExamination lastPatex = null;
				try {
					lastPatex = examManager.getLastByPatID(opdPatient.getCode());
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
				if (lastPatex != null) {
					patex = examManager.getFromLastPatientExamination(lastPatex);
				} else {
					patex = examManager.getDefaultPatientExamination(opdPatient);
				}

				GenderPatientExamination gpatex = new GenderPatientExamination(patex, opdPatient.getSex() == 'M');

				PatientExaminationEdit dialog = new PatientExaminationEdit(OpdEditExtended.this, gpatex);
				dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.showAsModal(OpdEditExtended.this);
			});
		}
		return jButtonExamination;
	}
	
	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(actionEvent -> {
				
				if (opdDateFieldCal.getLocalDateTime() != null) {
					visitDateOpd = LocalDateTime.now();
					opd.setDate(visitDateOpd);
				} else {
					opd.setDate(null);
				}
				int opdProgYear = 0;
				
				if (jOpdNumField.isEditable()) {
					try {
						opdProgYear = Integer.parseInt(jOpdNumField.getText());
						if (opdManager.isExistOpdNum(opdProgYear, visitDateOpd.getYear())) {
							MessageDialog.error(OpdEditExtended.this, "angal.opd.opdnumberalreadyexist.msg");
							if (insert) {
								jOpdNumField.setText(String.valueOf(getOpdProgYear(visitDateOpd)));
							}
							jOpdNumField.requestFocusInWindow();
							return;
						}
					} catch (NumberFormatException e) {
						MessageDialog.error(OpdEditExtended.this, "angal.opd.opdnumbermustbeanumber.msg");
						jOpdNumField.requestFocusInWindow();
						return;
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				} else {
					opdProgYear = getOpdProgYear(visitDateOpd);
				}
				
				char newPatient;
				String referralTo;
				String referralFrom;
				Disease disease = null;
				Disease disease2 = null;
				Disease disease3 = null;

				if (newPatientCheckBox.isSelected()) {
					newPatient = 'N';
				} else {
					newPatient = 'R';
				}
				if (referralToCheckBox.isSelected()) {
					referralTo = "R";
				} else {
					referralTo = "";
				}
				if (referralFromCheckBox.isSelected()) {
					referralFrom = "R";
				} else {
					referralFrom = "";
				}
				// disease
				if (diseaseBox1.getSelectedIndex() > 0) {
					disease = ((Disease) diseaseBox1.getSelectedItem());
				}
				// disease2
				if (diseaseBox2.getSelectedIndex() > 0) {
					disease2 = ((Disease) diseaseBox2.getSelectedItem());
				}
				// disease3
				if (diseaseBox3.getSelectedIndex() > 0) {
					disease3 = ((Disease) diseaseBox3.getSelectedItem());
				}

				boolean scheduleVisit = false;
				LocalDateTime nextVisit = opdNextVisitDate.getLocalDateTime(); // FIXME: despite the presentation dd/MM/yy the object has time when insert = true
				if (nextVisit != null) {
					if (nextVisit.compareTo(opdDateFieldCal.getLocalDateTime()) < 0) {
						MessageDialog.error(OpdEditExtended.this, "angal.opd.cannotsetadateinthepastfornextvisit.msg");
						return;
					}
					opd.setNextVisitDate(nextVisit);
					scheduleVisit = true;
				} else {
					opd.setNextVisitDate(null);
				}

				opd.setNote(jNoteTextArea.getText());
				opd.setPatient(opdPatient);
				opd.setNewPatient(newPatient);
				opd.setReferralFrom(referralFrom);
				opd.setReferralTo(referralTo);
				opd.setDisease(disease);
				opd.setDisease2(disease2);
				opd.setDisease3(disease3);
				opd.setUserID(UserBrowsingManager.getCurrentUser());

				try {
					if (insert) { // Insert
						opd.setProgYear(opdProgYear);
						// remember for later use
						RememberDates.setLastOpdVisitDate(visitDateOpd);
						boolean result = opdManager.newOpd(opd);
						if (result) {
							if (scheduleVisit) {
								Visit visit = new Visit();
								visit.setDate(opd.getNextVisitDate());
								visit.setPatient(opd.getPatient());
								visit.setWard(null);
								visitManager.newVisit(visit);
							}

							fireSurgeryInserted(opd);
							dispose();
						}
						if (!result) {
							MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
						}
					} else { // Update
						Opd updatedOpd = opdManager.updateOpd(opd);
						if (updatedOpd != null) {
							
							//TODO: move the whole logic to manager
							Visit visit = new Visit();
							if (scheduleVisit) {
								visit.setDate(opd.getNextVisitDate());
								visit.setPatient(opd.getPatient());
							
								if (nextDateBackup != null && !TimeTools.isSameDay(opd.getNextVisitDate(), nextDateBackup)) {
									Iterator<Visit> visits = visitManager.getVisitsOPD(opd.getPatient().getCode()).iterator();
	
									boolean found = false;
									while (!found && visits.hasNext()) {
										visit = visits.next();
										found = TimeTools.isSameDay(visit.getDate(), nextDateBackup);
									}
									visit.setDate(opd.getNextVisitDate());
									visit.setPatient(opd.getPatient());
								}
								
								visitManager.newVisit(visit);
								
							} else {
								
								if (nextDateBackup != null) {
									Iterator<Visit> visits = visitManager.getVisitsOPD(opd.getPatient().getCode()).iterator();
	
									boolean found = false;
									while (!found && visits.hasNext()) {
										visit = visits.next();
										found = TimeTools.isSameDay(visit.getDate(), nextDateBackup);
									}
									if (found) {
										visitManager.deleteVisit(visit);
									}
								}
							}

							fireSurgeryUpdated(updatedOpd);
							dispose();
						}
						if (updatedOpd == null) {
							MessageDialog.error(OpdEditExtended.this, "angal.common.datacouldnotbesaved.msg");
						}
					}
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
			});
		}
		return okButton;
	}
	
	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> {
				//to free Memory
				pat.clear();
				diseasesAll.clear();
				diseasesOPD.clear();
				types.clear();
				jComboPatResult.removeAllItems();
				diseaseTypeBox.removeAllItems();
				diseaseBox1.removeAllItems();
				diseaseBox2.removeAllItems();
				diseaseBox3.removeAllItems();
				dispose();
			});
		}
		return cancelButton;
	}
	
	/*
	 * Set a specific border+title to a panel
	 */
	private JPanel setMyBorder(JPanel c, String title) {
		javax.swing.border.Border b2 = BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title), BorderFactory
						.createEmptyBorder(0, 0, 0, 0));
		c.setBorder(b2);
		return c;
	}
	
	/*
	 * Set a specific border+title+matte to a panel
	 */
	private JPanel setMyMatteBorder(JPanel c, String title) {
		c.setBorder(new TitledBorder(new MatteBorder(1, 20, 1, 1, new Color(153, 180, 209)), title, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		return c;
	}

	private JLabel getJlabelOpd() {
		if (JlabelOpd == null) {
			JlabelOpd = new JLabel("");
		}
		return JlabelOpd;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		JButton source = (JButton) actionEvent.getSource();
		if (source == searchDiseaseButton) {
			diseaseBox1.removeAllItems();
			diseaseBox1.addItem("");
			for (Disease disease : getSearchDiagnosisResults(searchDiseaseTextField.getText(), diseasesOPD == null ? diseasesAll : diseasesOPD)) {
				diseaseBox1.addItem(disease);
			}
			if (diseaseBox1.getItemCount() >= 2) {
				diseaseBox1.setSelectedIndex(1);
			}
			diseaseBox1.requestFocus();
			if (diseaseBox1.getItemCount() > 2) {
				diseaseBox1.showPopup();
			}
		} else if (source == searchDiseaseButton2) {
			diseaseBox2.removeAllItems();
			diseaseBox2.addItem("");
			for (Disease disease : getSearchDiagnosisResults(searchDiseaseTextField2.getText(), diseasesOPD == null ? diseasesAll : diseasesOPD)) {
				diseaseBox2.addItem(disease);
			}
			if (diseaseBox2.getItemCount() >= 2) {
				diseaseBox2.setSelectedIndex(1);
			}
			diseaseBox2.requestFocus();
			if (diseaseBox2.getItemCount() > 2) {
				diseaseBox2.showPopup();
			}
		} else if (source == searchDiseaseButton3) {
			diseaseBox3.removeAllItems();
			diseaseBox3.addItem("");
			for (Disease disease : getSearchDiagnosisResults(searchDiseaseTextField3.getText(), diseasesOPD == null ? diseasesAll : diseasesOPD)) {
				diseaseBox3.addItem(disease);
			}
			if (diseaseBox3.getItemCount() >= 2) {
				diseaseBox3.setSelectedIndex(1);
			}
			diseaseBox3.requestFocus();
			if (diseaseBox3.getItemCount() > 2) {
				diseaseBox3.showPopup();
			}
		}
	}

	private List<Disease> getSearchDiagnosisResults(String s, List<Disease> diseaseList) {
		String query = s.trim();
		List<Disease> results = new ArrayList<>();
		for (Disease disease : diseaseList) {
			if (!query.equals("")) {
				String[] patterns = query.split(" ");
				String name = disease.getDescription().toLowerCase();
				boolean patternFound = false;
				for (String pattern : patterns) {
					if (name.contains(pattern.toLowerCase())) {
						patternFound = true;
						//It is sufficient that only one pattern matches the query
						break;
					}
				}
				if (patternFound) {
					results.add(disease);
				}
			} else {
				results.add(disease);
			}
		}
		return results;
	}

	private JLabel getNextVisitLabel() {
		if (nextVisitLabel == null) {
			nextVisitLabel = new JLabel(MessageBundle.getMessage("angal.opd.nextvisitdate.txt"));
		}
		return nextVisitLabel;
	}
	
	private GoodDateTimeChooser getOpdNextVisitDate() {
		if (opdNextVisitDate == null) {

			LocalDateTime nextDate = null;
			if (!insert) {
				nextDate = opd.getNextVisitDate();
			}
			if (nextDate != null) {
				nextDateBackup = nextDate; // in case of changing the date during this update
			}

			opdNextVisitDate = new GoodDateTimeChooser(nextDate, false, true, true,true);

			if (opd.getPatient() == null) {
				opdNextVisitDate.setEnabled(false);
			}
		}
		return opdNextVisitDate;
	}

}
