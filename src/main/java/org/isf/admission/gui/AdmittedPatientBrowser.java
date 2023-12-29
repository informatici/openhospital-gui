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
package org.isf.admission.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.accounting.gui.PatientBillEdit;
import org.isf.admission.gui.AdmissionBrowser.AdmissionListener;
import org.isf.admission.gui.PatientDataBrowser.DeleteAdmissionListener;
import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.admission.model.AdmittedPatient;
import org.isf.anamnesis.gui.PatientHistoryEdit;
import org.isf.anamnesis.manager.PatientHistoryManager;
import org.isf.anamnesis.model.PatientHistory;
import org.isf.anamnesis.model.PatientPatientHistory;
import org.isf.dicom.gui.DicomGui;
import org.isf.disease.model.Disease;
import org.isf.exa.model.Exam;
import org.isf.examination.gui.PatientExaminationEdit;
import org.isf.examination.manager.ExaminationBrowserManager;
import org.isf.examination.model.GenderPatientExamination;
import org.isf.examination.model.PatientExamination;
import org.isf.exatype.model.ExamType;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.gui.LabEdit;
import org.isf.lab.gui.LabEditExtended;
import org.isf.lab.gui.LabNew;
import org.isf.lab.model.Laboratory;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.opd.gui.OpdEditExtended;
import org.isf.opd.model.Opd;
import org.isf.patient.gui.PatientInsert;
import org.isf.patient.gui.PatientInsertExtended;
import org.isf.patient.gui.PatientInsertExtended.PatientListener;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.therapy.gui.TherapyEdit;
import org.isf.utils.db.NormalizeString;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

/**
 * This class shows a list of all known patients and for each if (and where) they are actually admitted,
 * you can:
 * filter patients by ward and admission status
 * search for patient with given name
 * add a new patient, edit or delete an existing patient record
 * view extended data of a selected patient
 * add an admission record (or modify existing admission record, or set a discharge) of a selected patient
 */
public class AdmittedPatientBrowser extends ModalJFrame implements PatientInsert.PatientListener, PatientListener,
				AdmissionListener, DeleteAdmissionListener {

	private static final long serialVersionUID = 1L;

	private static final int PANEL_WIDTH = 240;
	private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");

	private PatientHistoryManager patientHistoryManager = Context.getApplicationContext().getBean(PatientHistoryManager.class);

	private String[] patientClassItems = { MessageBundle.getMessage("angal.common.all.txt"), MessageBundle.getMessage("angal.admission.admitted.txt"),
			MessageBundle.getMessage("angal.admission.notadmitted.txt") };
	private JComboBox patientClassBox = new JComboBox(patientClassItems);
	private GoodDateChooser[] dateChoosers = new GoodDateChooser[4];
	private VoLimitedTextField patientAgeFromTextField;
	private VoLimitedTextField patientAgeToTextField;
	private String[] patientSexItems = { MessageBundle.getMessage("angal.common.all.txt"), MessageBundle.getMessage("angal.common.male.txt"),
			MessageBundle.getMessage("angal.common.female.txt") };
	private JComboBox patientSexBox = new JComboBox(patientSexItems);
	private JCheckBox[] wardCheck;
	private JTextField searchString;
	private JButton jSearchButton;
	private JButton jButtonExamination;
	private String lastKey = "";
	private List<Ward> wardList;
	private JLabel rowCounter;
	private List<AdmittedPatient> pPatient = new ArrayList<>();
	private String[] pColumns = { MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.name.txt").toUpperCase(), MessageBundle.getMessage("angal.common.age.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.sex.txt").toUpperCase(),
			MessageBundle.getMessage("angal.admission.cityaddresstelephonenote.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.ward.txt").toUpperCase() };
	private int[] pColumnWidth = { 100, 200, 80, 50, 150, 100 };
	private boolean[] pColumnResizable = { false, false, false, false, true, false };
	private AdmittedPatient patient;
	private JTable table;
	private AdmittedPatientBrowser myFrame;

	private WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private AdmissionBrowserManager admissionBrowserManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
	private ExaminationBrowserManager examinationBrowserManager = Context.getApplicationContext().getBean(ExaminationBrowserManager.class);

	protected boolean altKeyReleased = true;
	protected Timer ageTimer = new Timer(1000, e -> filterPatient(null));

	public void fireMyDeletedPatient(Patient p) {

		int cc = 0;
		boolean found = false;
		for (AdmittedPatient elem : pPatient) {
			if (elem.getPatient().getCode().equals(p.getCode())) {
				found = true;
				break;
			}
			cc++;
		}
		if (found) {
			pPatient.remove(cc);
			lastKey = "";
			filterPatient(searchString.getText());
		}
	}

	@Override
	public void deleteAdmissionUpdated(AWTEvent e) {
		Admission adm = (Admission) e.getSource();

		// remember selected row
		int row = table.getSelectedRow();

		for (AdmittedPatient elem : pPatient) {
			if (elem.getPatient().getCode().equals(adm.getPatient().getCode())) {
				// found same patient in the list
				Admission elemAdm = elem.getAdmission();
				if (elemAdm != null) {
					// the patient is admitted
					if (elemAdm.getId() == adm.getId()) {
						// same admission --> delete
						elem.setAdmission(null);
					}
				}
				break;
			}
		}
		lastKey = "";
		filterPatient(searchString.getText());
		try {
			if (table.getRowCount() > 0) {
				table.setRowSelectionInterval(row, row);
			}
		} catch (Exception e1) {
		}
	}

	/*
	 * manage AdmissionBrowser messages
	 */
	@Override
	public void admissionInserted(AWTEvent e) {
		Admission adm = (Admission) e.getSource();

		// remember selected row
		int row = table.getSelectedRow();
		int patId = adm.getPatient().getCode();

		for (AdmittedPatient elem : pPatient) {
			if (elem.getPatient().getCode() == patId) {
				// found same patient in the list
				elem.setAdmission(adm);
				break;
			}
		}
		lastKey = "";
		filterPatient(searchString.getText());
		try {
			if (table.getRowCount() > 0) {
				table.setRowSelectionInterval(row, row);
			}
		} catch (Exception e1) {
		}
	}

	/*
	 * param contains info about patient admission, ward can varying or patient may be discharged
	 */
	@Override
	public void admissionUpdated(AWTEvent e) {
		Admission adm = (Admission) e.getSource();

		// remember selected row
		int row = table.getSelectedRow();
		int admId = adm.getId();
		int patId = adm.getPatient().getCode();

		for (AdmittedPatient elem : pPatient) {
			if (elem.getPatient().getCode() == patId) {
				// found same patient in the list
				Admission elemAdm = elem.getAdmission();
				if (adm.getDisDate() != null) {
					// is a discharge
					if (elemAdm != null) {
						// the patient is not discharged
						if (elemAdm.getId() == admId) {
							// same admission --> discharge
							elem.setAdmission(null);
						}
					}
				} else {
					// is not a discharge --> patient admitted
					elem.setAdmission(adm);
				}
				break;
			}
		}
		lastKey = "";
		filterPatient(searchString.getText());
		try {
			if (table.getRowCount() > 0) {
				table.setRowSelectionInterval(row, row);
			}
		} catch (Exception e1) {
		}
	}

	/*
	 * manage PatientEdit messages
	 * 
	 * mind PatientEdit return a patient patientInserted create a new AdmittedPatient for table
	 */
	@Override
	public void patientInserted(AWTEvent e) {
		Patient u = (Patient) e.getSource();
		pPatient.add(0, new AdmittedPatient(u, null));
		lastKey = "";
		filterPatient(searchString.getText());
		try {
			if (table.getRowCount() > 0) {
				table.setRowSelectionInterval(0, 0);
			}
		} catch (Exception e1) {
		}
		searchString.requestFocus();
		rowCounter.setText(MessageBundle.formatMessage("angal.admission.count.fmt.txt", pPatient.size()));
	}

	@Override
	public void patientUpdated(AWTEvent e) {

		Patient u = (Patient) e.getSource();

		// remember selected row
		int row = table.getSelectedRow();

		for (int i = 0; i < pPatient.size(); i++) {
			if (pPatient.get(i).getPatient().getCode().equals(u.getCode())) {
				Admission admission = pPatient.get(i).getAdmission();
				pPatient.remove(i);
				pPatient.add(i, new AdmittedPatient(u, admission));
				break;
			}
		}
		lastKey = "";
		filterPatient(searchString.getText());
		try {
			table.setRowSelectionInterval(row, row);
		} catch (Exception e1) {
		}
		searchString.requestFocus();
		rowCounter.setText(MessageBundle.formatMessage("angal.admission.count.fmt.txt", pPatient.size()));
	}

	public AdmittedPatientBrowser() {

		setTitle(MessageBundle.getMessage("angal.admission.patientbrowser.title"));
		myFrame = this;

		if (!GeneralData.ENHANCEDSEARCH) {
			// Load the whole list of patients
			try {
				pPatient = admissionBrowserManager.getAdmittedPatients(null);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		initComponents();
		setMinimumSize(new Dimension(1270, 570));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);

		rowCounter.setText(MessageBundle.formatMessage("angal.admission.count.fmt.txt", pPatient.size()));
		searchString.requestFocus();

		myFrame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// to free memory
				if (pPatient != null) {
					pPatient.clear();
				}
				if (wardList != null) {
					wardList.clear();
				}
				dispose();
			}
		});
	}

	private void initComponents() {
		add(getDataAndControlPanel(), BorderLayout.CENTER);
		add(getButtonPanel(), BorderLayout.SOUTH);
	}

	private JPanel getDataAndControlPanel() {
		JPanel dataAndControlPanel = new JPanel(new BorderLayout());
		dataAndControlPanel.add(new JScrollPane(getControlPanel()), BorderLayout.WEST);
		dataAndControlPanel.add(getScrollPane(), BorderLayout.CENTER);
		return dataAndControlPanel;
	}

	/*
	 * Panel with filtering controls
	 */
	private JPanel getControlPanel() {
		ActionListener listener = actionEvent -> SwingUtilities.invokeLater(() -> {
			lastKey = "";
			filterPatient(null);
		});

		patientClassBox = new JComboBox(patientClassItems);
		if (!GeneralData.ENHANCEDSEARCH) {
			patientClassBox.addActionListener(listener);
		}

		JPanel classPanel = new JPanel();
		classPanel.setLayout(new BoxLayout(classPanel, BoxLayout.Y_AXIS));
		classPanel.add(patientClassBox);
		classPanel.setPreferredSize(new Dimension(PANEL_WIDTH, 20));
		classPanel = setMyBorder(classPanel, MessageBundle.getMessage("angal.admission.admissionstatus.border"));

		JPanel wardPanel = new JPanel();
		wardPanel.setLayout(new BoxLayout(wardPanel, BoxLayout.Y_AXIS));
		wardPanel.setPreferredSize(new Dimension(PANEL_WIDTH, 20));
		if (wardList == null) {
			List<Ward> wardWithBeds = wardBrowserManager.getIpdWards();

			wardList = new ArrayList<>();
			for (Ward elem : wardWithBeds) {

				if (elem.getBeds() > 0) {
					wardList.add(elem);
				}
			}
		}

		JPanel[] checkPanel = new JPanel[wardList.size()];
		wardCheck = new JCheckBox[wardList.size()];

		for (int i = 0; i < wardList.size(); i++) {
			checkPanel[i] = new JPanel(new BorderLayout());
			wardCheck[i] = new JCheckBox();
			wardCheck[i].setSelected(true);
			if (!GeneralData.ENHANCEDSEARCH) {
				wardCheck[i].addActionListener(listener);
			}
			checkPanel[i].add(wardCheck[i], BorderLayout.WEST);
			checkPanel[i].add(new JLabel(wardList.get(i).getDescription()), BorderLayout.CENTER);
			wardPanel.add(checkPanel[i], null);
		}

		wardPanel = setMyBorder(wardPanel, MessageBundle.getMessage("angal.admission.ward.border"));

		rowCounter = new JLabel(MessageBundle.formatMessage("angal.admission.count.fmt.txt", 0));
		rowCounter.setAlignmentX(Component.CENTER_ALIGNMENT);
		wardPanel.add(rowCounter);

		JPanel calendarPanel = getAdmissionFilterPanel();

		KeyListener ageKeyListener = new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				ageTimer.setRepeats(false);
				ageTimer.start();
			}
		};

		JLabel ageFrom = new JLabel(MessageBundle.getMessage("angal.common.from.txt") + ':');
		patientAgeFromTextField = new VoLimitedTextField(3, 3);
		if (!GeneralData.ENHANCEDSEARCH) {
			patientAgeFromTextField.addKeyListener(ageKeyListener);
		}

		JLabel ageTo = new JLabel(MessageBundle.getMessage("angal.common.to.txt") + ':');
		patientAgeToTextField = new VoLimitedTextField(3, 3);
		if (!GeneralData.ENHANCEDSEARCH) {
			patientAgeToTextField.addKeyListener(ageKeyListener);
		}

		JPanel agePanel = new JPanel();
		agePanel.setPreferredSize(new Dimension(PANEL_WIDTH, 20));
		agePanel.add(ageFrom);
		agePanel.add(patientAgeFromTextField);
		agePanel.add(ageTo);
		agePanel.add(patientAgeToTextField);
		agePanel = setMyBorder(agePanel, MessageBundle.getMessage("angal.admission.age.border"));

		patientSexBox = new JComboBox(patientSexItems);
		patientSexBox.setPreferredSize(new Dimension(PANEL_WIDTH, 20));
		if (!GeneralData.ENHANCEDSEARCH) {
			patientSexBox.addActionListener(listener);
		}

		JPanel sexPanel = new JPanel();
		sexPanel.setPreferredSize(new Dimension(PANEL_WIDTH, 20));
		sexPanel.setLayout(new BorderLayout());
		sexPanel.add(patientSexBox, BorderLayout.CENTER);
		sexPanel = setMyBorder(sexPanel, MessageBundle.getMessage("angal.admission.sex.border"));

		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.setPreferredSize(new Dimension(PANEL_WIDTH, 20));
		searchString = new JTextField();
		searchString.setColumns(15);
		if (GeneralData.ENHANCEDSEARCH) {
			searchString.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						jSearchButton.doClick();
					}
				}
			});
		} else {
			searchString.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
					if (altKeyReleased) {
						lastKey = "";
						String s = String.valueOf(e.getKeyChar());
						if (Character.isLetterOrDigit(e.getKeyChar())) {
							lastKey = s;
						}
						filterPatient(searchString.getText());
					}
				}

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ALT) {
						altKeyReleased = false;
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
					altKeyReleased = true;
				}
			});
		}
		searchPanel.add(searchString, BorderLayout.CENTER);
		if (GeneralData.ENHANCEDSEARCH) {
			searchPanel.add(getButtonSearch(), BorderLayout.EAST);
		}
		searchPanel = setMyBorder(searchPanel, MessageBundle.getMessage("angal.admission.searchkey.border"));

		JPanel mainPanel = new JPanel();
		GroupLayout layout = new GroupLayout(mainPanel);
		layout.setAutoCreateContainerGaps(true);
		int width = calendarPanel.getMinimumSize().width;
		layout.setHorizontalGroup(layout.createSequentialGroup() //
						.addGroup(layout.createParallelGroup() //
										.addComponent(classPanel, width, width, width) //
										.addComponent(wardPanel, width, width, width) //
										.addComponent(calendarPanel, width, width, width) //
										.addComponent(agePanel, width, width, width) //
										.addComponent(sexPanel, width, width, width) //
										.addComponent(searchPanel, width, width, width)));

		layout.setVerticalGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(classPanel, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)) //
						.addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.BASELINE) //
										.addComponent(wardPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)) //
						.addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.BASELINE) //
										.addComponent(calendarPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)) //
						.addPreferredGap(ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(Alignment.BASELINE) //
										.addComponent(agePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)) //
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(sexPanel, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)) //
						.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(layout.createParallelGroup(Alignment.BASELINE) //
										.addComponent(searchPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		mainPanel.setLayout(layout);
		return mainPanel;
	}

	private JPanel getAdmissionFilterPanel() {
		JPanel calendarPanel = new JPanel();
		calendarPanel.setLayout(new GridBagLayout());
		calendarPanel.setPreferredSize(new Dimension(PANEL_WIDTH, 20));
		calendarPanel = setMyBorder(calendarPanel, MessageBundle.getMessage("angal.admission.date.border"));

		JLabel admissionLabel = new JLabel(MessageBundle.getMessage("angal.admission.admissiondate.txt"));
		JLabel dischargeLabel = new JLabel(MessageBundle.getMessage("angal.admission.dischargedate.txt"));
		for (int i = 0; i <= dateChoosers.length - 1; i++) {
			GoodDateChooser chooser = new GoodDateChooser(null);
			dateChoosers[i] = chooser;
		}

		GridBagConstraints gbcAdmissionLabel = new GridBagConstraints();
		gbcAdmissionLabel.gridx = 0;
		gbcAdmissionLabel.gridwidth = 4;
		gbcAdmissionLabel.gridy = 0;
		gbcAdmissionLabel.insets = new Insets(0, 0, 5, 0);
		gbcAdmissionLabel.anchor = GridBagConstraints.CENTER;
		calendarPanel.add(admissionLabel, gbcAdmissionLabel);
		GridBagConstraints gbcDateLabel0 = new GridBagConstraints();
		gbcDateLabel0.gridx = 0;
		gbcDateLabel0.gridy = 1;
		gbcDateLabel0.insets = new Insets(0, 5, 0, 5);
		gbcDateLabel0.weightx = 0.0;
		calendarPanel.add(new JLabel(MessageBundle.getMessage("angal.common.from.txt") + ':'), gbcDateLabel0);
		GridBagConstraints gbcDateDateChooser0 = new GridBagConstraints();
		gbcDateDateChooser0.gridx = 1;
		gbcDateDateChooser0.gridy = 1;
		gbcDateDateChooser0.weightx = 1.0;
		calendarPanel.add(dateChoosers[0], gbcDateDateChooser0);
		GridBagConstraints gbcDateLabel1 = new GridBagConstraints();
		gbcDateLabel1.gridx = 2;
		gbcDateLabel1.gridy = 1;
		gbcDateLabel1.insets = new Insets(0, 5, 0, 5);
		gbcDateLabel1.weightx = 0.0;
		calendarPanel.add(new JLabel(MessageBundle.getMessage("angal.common.to.txt") + ':'), gbcDateLabel1);
		GridBagConstraints gbcDateDateChooser1 = new GridBagConstraints();
		gbcDateDateChooser1.gridx = 3;
		gbcDateDateChooser1.gridy = 1;
		gbcDateDateChooser1.weightx = 1.0;
		calendarPanel.add(dateChoosers[1], gbcDateDateChooser1);

		GridBagConstraints gbcDischargeLabel = new GridBagConstraints();
		gbcDischargeLabel.gridx = 0;
		gbcDischargeLabel.gridwidth = 4;
		gbcDischargeLabel.gridy = 2;
		gbcDischargeLabel.insets = new Insets(10, 0, 5, 0);
		gbcDischargeLabel.anchor = GridBagConstraints.CENTER;
		calendarPanel.add(dischargeLabel, gbcDischargeLabel);
		GridBagConstraints gbcDateLabel2 = new GridBagConstraints();
		gbcDateLabel2.gridx = 0;
		gbcDateLabel2.gridy = 3;
		gbcDateLabel2.insets = new Insets(0, 5, 0, 5);
		gbcDateLabel2.weightx = 0.0;
		calendarPanel.add(new JLabel(MessageBundle.getMessage("angal.common.from.txt") + ':'), gbcDateLabel2);
		GridBagConstraints gbcDateDateChooser2 = new GridBagConstraints();
		gbcDateDateChooser2.gridx = 1;
		gbcDateDateChooser2.gridy = 3;
		gbcDateDateChooser2.weightx = 1.0;
		calendarPanel.add(dateChoosers[2], gbcDateDateChooser2);
		GridBagConstraints gbcDateLabel3 = new GridBagConstraints();
		gbcDateLabel3.gridx = 2;
		gbcDateLabel3.gridy = 3;
		gbcDateLabel3.insets = new Insets(0, 5, 0, 5);
		gbcDateLabel3.weightx = 0.0;
		calendarPanel.add(new JLabel(MessageBundle.getMessage("angal.common.to.txt") + ':'), gbcDateLabel3);
		GridBagConstraints gbcDateDateChooser3 = new GridBagConstraints();
		gbcDateDateChooser3.gridx = 3;
		gbcDateDateChooser3.gridy = 3;
		gbcDateDateChooser3.weightx = 1.0;
		gbcDateDateChooser3.insets = new Insets(0, 0, 5, 0);
		calendarPanel.add(dateChoosers[3], gbcDateDateChooser3);

		calendarPanel.setVisible(GeneralData.ENHANCEDSEARCH);
		return calendarPanel;
	}

	private JScrollPane getScrollPane() {
		table = new JTable(new AdmittedPatientBrowserModel(null));
		table.setAutoCreateColumnsFromModel(false);

		for (int i = 0; i < pColumns.length; i++) {
			table.getColumnModel().getColumn(i).setMinWidth(pColumnWidth[i]);
			if (!pColumnResizable[i]) {
				table.getColumnModel().getColumn(i).setMaxWidth(pColumnWidth[i]);
			}
		}

		table.getColumnModel().getColumn(0).setCellRenderer(new CenterTableCellRenderer());
		table.getColumnModel().getColumn(2).setCellRenderer(new CenterTableCellRenderer());
		table.getColumnModel().getColumn(3).setCellRenderer(new CenterTableCellRenderer());

		int tableWidth = 0;
		for (int j : pColumnWidth) {
			tableWidth += j;
		}

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(tableWidth + 200, 200));
		return scrollPane;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel(new WrapLayout());
		if (MainMenu.checkUserGrants("btnadmnew")) {
			buttonPanel.add(getButtonNew());
		}
		if (MainMenu.checkUserGrants("btnadmedit")) {
			buttonPanel.add(getButtonEdit());
		}
		if (MainMenu.checkUserGrants("btnadmdel")) {
			buttonPanel.add(getButtonDelete());
		}
		if (MainMenu.checkUserGrants("btnadmadm")) {
			buttonPanel.add(getButtonAdmission());
		}
		if (MainMenu.checkUserGrants("btnadmedit")) {
			buttonPanel.add(getJAnamnesisButton());
		}
		if (MainMenu.checkUserGrants("btnadmexamination")) {
			buttonPanel.add(getButtonExamination());
		}

		if (GeneralData.OPDEXTENDED && MainMenu.checkUserGrants("btnadmopd")) {
			buttonPanel.add(getButtonOpd());
		}
		if (MainMenu.checkUserGrants("btnadmlab")) {
			buttonPanel.add(getButtonLab());
		}
		if (MainMenu.checkUserGrants("btnadmbill")) {
			buttonPanel.add(getButtonBill());
		}
		if (MainMenu.checkUserGrants("data")) {
			buttonPanel.add(getButtonData());
		}
		if (GeneralData.DICOMMODULEENABLED && MainMenu.checkUserGrants("btnadmdicom")) {
			buttonPanel.add(getDICOMButton());
		}
		if (MainMenu.checkUserGrants("btnadmpatientfolder")) {
			buttonPanel.add(getButtonPatientFolderBrowser());
		}
		if (MainMenu.checkUserGrants("btnadmtherapy")) {
			buttonPanel.add(getButtonTherapy());
		}
		if (GeneralData.MERGEFUNCTION && MainMenu.checkUserGrants("btnadmmer")) {
			buttonPanel.add(getButtonMerge());
		}
		buttonPanel.add(getButtonClose());
		return buttonPanel;
	}

	private JButton getJAnamnesisButton() {
		JButton jAnamnesisButton = new JButton(MessageBundle.getMessage("angal.anamnesis.open.anamnesis.btn"));
		jAnamnesisButton.setMnemonic(MessageBundle.getMnemonic("angal.opd.anamnesis.btn.key"));
		jAnamnesisButton.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(null, "angal.common.pleaseselectapatient.msg");
				return;
			}
			patient = (AdmittedPatient) table.getValueAt(table.getSelectedRow(), -1);
			PatientHistory ph = new PatientHistory();
			ph.setPatientId(patient.getPatient().getCode());
			PatientHistory patientHistory = Optional.ofNullable(patientHistoryManager.getByPatientId(patient.getPatient().getCode())).orElse(ph);
			PatientPatientHistory pph = new PatientPatientHistory(patientHistory, patient.getPatient());
			PatientHistoryEdit dialog = new PatientHistoryEdit(this, pph, true);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setModal(false);
			dialog.setVisible(true);
		});
		return jAnamnesisButton;
	}

	private JButton getButtonExamination() {
		if (jButtonExamination == null) {
			jButtonExamination = new JButton(MessageBundle.getMessage("angal.admission.examination.btn"));
			jButtonExamination.setMnemonic(MessageBundle.getMnemonic("angal.admission.examination.btn.key"));
			jButtonExamination.addActionListener(actionEvent -> {
				if (table.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.common.pleaseselectapatient.msg");
					return;
				}
				patient = (AdmittedPatient) table.getValueAt(table.getSelectedRow(), -1);
				Patient pat = patient.getPatient();

				PatientExamination patex;

				PatientExamination lastPatex = null;
				try {
					lastPatex = examinationBrowserManager.getLastByPatID(pat.getCode());
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
				if (lastPatex != null) {
					patex = examinationBrowserManager.getFromLastPatientExamination(lastPatex);
				} else {
					patex = examinationBrowserManager.getDefaultPatientExamination(pat);
				}

				GenderPatientExamination gpatex = new GenderPatientExamination(patex, pat.getSex() == 'M');

				PatientExaminationEdit dialog = new PatientExaminationEdit(this, gpatex);
				dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.showAsModal(this);
			});
		}
		return jButtonExamination;
	}

	private JButton getButtonNew() {
		JButton buttonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
		buttonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
		buttonNew.addActionListener(actionEvent -> {
			Patient newPatient = new Patient();
			if (GeneralData.PATIENTEXTENDED) {
				PatientInsertExtended newrecord = new PatientInsertExtended(this, newPatient, true);
				newrecord.addPatientListener(this);
				newrecord.setVisible(true);
			} else {
				PatientInsert newrecord = new PatientInsert(this, newPatient, true);
				newrecord.addPatientListener(this);
				newrecord.setVisible(true);
			}

		});
		return buttonNew;
	}

	private JButton getButtonEdit() {
		JButton buttonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
		buttonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
		buttonEdit.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
				return;
			}
			patient = reloadSelectedPatient(table.getSelectedRow());
			if (GeneralData.PATIENTEXTENDED) {

				PatientInsertExtended editrecord = new PatientInsertExtended(this, patient.getPatient(), false);
				editrecord.addPatientListener(this);
				editrecord.setVisible(true);
			} else {
				PatientInsert editrecord = new PatientInsert(this, patient.getPatient(), false);
				editrecord.addPatientListener(this);
				editrecord.setVisible(true);
			}
		});
		return buttonEdit;
	}

	private JButton getButtonDelete() {
		JButton buttonDel = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		buttonDel.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		buttonDel.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
				return;
			}
			patient = (AdmittedPatient) table.getValueAt(table.getSelectedRow(), -1);
			Patient pat = patient.getPatient();

			int n = MessageDialog.yesNo(this, "angal.admission.deletepatient.fmt.msg", pat.getName());
			if (n == JOptionPane.YES_OPTION) {
				try {
					patientBrowserManager.deletePatient(pat);
					List<Admission> patientAdmissions;
					try {
						patientAdmissions = admissionBrowserManager.getAdmissions(pat);
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
						patientAdmissions = new ArrayList<>();
					}

					for (Admission elem : patientAdmissions) {
						try {
							admissionBrowserManager.setDeleted(elem.getId());
						} catch (OHServiceException e) {
							OHServiceExceptionUtil.showMessages(e);
						}
					}
					fireMyDeletedPatient(pat);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			}
		});
		return buttonDel;
	}

	private JButton getButtonAdmission() {
		JButton buttonAdmission = new JButton(MessageBundle.getMessage("angal.admission.admission.btn"));
		buttonAdmission.setMnemonic(MessageBundle.getMnemonic("angal.admission.admission.btn.key"));
		buttonAdmission.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
				return;
			}
			patient = reloadSelectedPatient(table.getSelectedRow());
			if (patient.getAdmission() != null) {
				// edit previous admission or discharge
				new AdmissionBrowser(myFrame, patient, true);
			} else {
				// new admission
				new AdmissionBrowser(myFrame, patient, false);
			}
		});
		return buttonAdmission;
	}

	private AdmittedPatient reloadSelectedPatient(int selectedRow) {
		AdmittedPatient selectedPatient = (AdmittedPatient) table.getValueAt(selectedRow, -1);
		// Reloading patient, with profile initialised.
		return admissionBrowserManager.loadAdmittedPatients(selectedPatient.getPatient().getCode());
	}

	private JButton getButtonOpd() {
		JButton buttonOpd = new JButton(MessageBundle.getMessage("angal.admission.opd.btn"));
		buttonOpd.setMnemonic(MessageBundle.getMnemonic("angal.admission.opd.btn.key"));
		buttonOpd.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
				return;
			}
			patient = reloadSelectedPatient(table.getSelectedRow());

			if (patient != null) {
				Opd opd = new Opd(0, ' ', -1, new Disease());
				OpdEditExtended newrecord = new OpdEditExtended(myFrame, opd, patient.getPatient(), true);
				newrecord.setLocationRelativeTo(null);
				newrecord.showAsModal(myFrame);
			}
		});
		return buttonOpd;
	}

	private JButton getButtonLab() {
		JButton buttonLab = new JButton(MessageBundle.getMessage("angal.admission.lab.btn"));
		buttonLab.setMnemonic(MessageBundle.getMnemonic("angal.admission.lab.btn.key"));
		buttonLab.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
				return;
			}
			patient = reloadSelectedPatient(table.getSelectedRow());
			Laboratory laboratory = new Laboratory(0, new Exam("", "", new ExamType("", ""), 0, ""), TimeTools.getNow(), "P", "", new Patient(), "");
			if (GeneralData.LABEXTENDED) {
				if (GeneralData.LABMULTIPLEINSERT) {
					LabNew editrecord = new LabNew(myFrame, patient.getPatient());
					editrecord.setVisible(true);
				} else {
					LabEditExtended editrecord = new LabEditExtended(myFrame, laboratory, true);
					editrecord.setVisible(true);
				}
			} else {
				LabEdit editrecord = new LabEdit(myFrame, laboratory, true);
				editrecord.setVisible(true);
			}
		});
		return buttonLab;
	}

	private JButton getButtonBill() {
		JButton buttonBill = new JButton(MessageBundle.getMessage("angal.admission.bill.btn"));
		buttonBill.setMnemonic(MessageBundle.getMnemonic("angal.admission.bill.btn.key"));
		buttonBill.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
				return;
			}
			patient = reloadSelectedPatient(table.getSelectedRow());

			if (patient != null) {
				Patient pat = patient.getPatient();
				PatientBillEdit pbe = new PatientBillEdit(this, pat);
				pbe.setVisible(true);
			}
		});
		return buttonBill;
	}

	private JButton getButtonData() {
		JButton buttonData = new JButton(MessageBundle.getMessage("angal.admission.data.btn"));
		buttonData.setMnemonic(MessageBundle.getMnemonic("angal.admission.data.btn.key"));
		buttonData.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
				return;
			}
			patient = reloadSelectedPatient(table.getSelectedRow());

			PatientDataBrowser pdb = new PatientDataBrowser(myFrame, patient.getPatient());
			pdb.addDeleteAdmissionListener(myFrame);
			pdb.showAsModal(this);
		});
		return buttonData;
	}

	private JButton getDICOMButton() {
		JButton dicomButton = new JButton(MessageBundle.getMessage("angal.admission.patientfolder.dicom.btn"));
		dicomButton.setMnemonic(MessageBundle.getMnemonic("angal.admission.patientfolder.dicom.btn.key"));
		dicomButton.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
				return;
			}
			patient = reloadSelectedPatient(table.getSelectedRow());
			DicomGui dg = new DicomGui(patient.getPatient(), this);
			dg.showAsModal(this);
		});
		return dicomButton;
	}

	private JButton getButtonPatientFolderBrowser() {
		JButton buttonPatientFolderBrowser = new JButton(MessageBundle.getMessage("angal.admission.patientfolder.btn"));
		buttonPatientFolderBrowser.setMnemonic(MessageBundle.getMnemonic("angal.admission.patientfolder.btn.key"));
		buttonPatientFolderBrowser.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
				return;
			}
			patient = reloadSelectedPatient(table.getSelectedRow());

			new PatientFolderBrowser(myFrame, patient.getPatient()).showAsModal(this);
		});
		return buttonPatientFolderBrowser;
	}

	private JButton getButtonTherapy() {
		JButton buttonTherapy = new JButton(MessageBundle.getMessage("angal.admission.therapy.btn"));
		buttonTherapy.setMnemonic(MessageBundle.getMnemonic("angal.admission.therapy.btn.key"));
		buttonTherapy.addActionListener(actionEvent -> {
			if (table.getSelectedRow() < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectapatient.msg");
				return;
			}
			patient = reloadSelectedPatient(table.getSelectedRow());

			TherapyEdit therapy = new TherapyEdit(this, patient.getPatient(), patient.getAdmission() != null);
			therapy.setLocationRelativeTo(null);
			therapy.setVisible(true);

		});
		return buttonTherapy;
	}

	private JButton getButtonMerge() {
		JButton buttonMerge = new JButton(MessageBundle.getMessage("angal.admission.merge.btn"));
		buttonMerge.setMnemonic(MessageBundle.getMnemonic("angal.admission.merge.btn.key"));
		buttonMerge.addActionListener(actionEvent -> {
			if (table.getSelectedRowCount() != 2) {
				MessageDialog.error(null, "angal.admission.pleaseselecttwopatients.msg");
				return;
			}

			int[] indexes = table.getSelectedRows();

			Patient mergedPatient;
			Patient patient1 = ((AdmittedPatient) table.getValueAt(indexes[0], -1)).getPatient();
			Patient patient2 = ((AdmittedPatient) table.getValueAt(indexes[1], -1)).getPatient();

			// Select most recent patient
			if (patient1.getCode() > patient2.getCode()) {
				mergedPatient = patient1;
			} else {
				mergedPatient = patient2;
				patient2 = patient1;
			}

			// ASK CONFIRMATION
			int ok = MessageDialog.yesNo(this,
							"angal.admission.withthisoperationthepatientwillbedeletedandhisherhistorytransferedtothepatient.fmt.msg", patient2.getCode(),
							patient2.getName(), patient2.getAge(), patient2.getAddress(), mergedPatient.getCode(), mergedPatient.getName(),
							mergedPatient.getAge(), mergedPatient.getAddress());
			if (ok != JOptionPane.YES_OPTION) {
				return;
			}

			if (mergedPatient.getName().toUpperCase().compareTo(patient2.getName().toUpperCase()) != 0) {
				String[] names = { mergedPatient.getName(), patient2.getName() };
				String whichName = (String) JOptionPane.showInputDialog(null, MessageBundle.getMessage("angal.admission.pleaseselectthefinalname.msg"),
								MessageDialog.QUESTION, JOptionPane.QUESTION_MESSAGE, null, names, null);
				if (whichName == null) {
					return;
				}
				if (whichName.compareTo(names[1]) == 0) {
					// patient2 name selected
					mergedPatient.setFirstName(patient2.getFirstName());
					mergedPatient.setSecondName(patient2.getSecondName());
				}
			}

			try {
				patientBrowserManager.mergePatient(mergedPatient, patient2);
				fireMyDeletedPatient(patient2);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		});
		return buttonMerge;
	}

	private JButton getButtonClose() {
		JButton buttonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		buttonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		buttonClose.addActionListener(actionEvent -> {
			// to free Memory
			if (pPatient != null) {
				pPatient.clear();
			}
			if (wardList != null) {
				wardList.clear();
			}
			dispose();
		});
		return buttonClose;
	}

	private void filterPatient(String key) {
		table.setModel(new AdmittedPatientBrowserModel(key));
		rowCounter.setText(MessageBundle.formatMessage("angal.admission.count.fmt.txt", table.getRowCount()));
		searchString.requestFocus();
	}

	private void searchPatient() {
		boolean isFilteredList = patientClassBox.getSelectedIndex() > 0 || //
				dateChoosers[0].getDate() != null && dateChoosers[1].getDate() != null || //
				dateChoosers[2].getDate() != null && dateChoosers[3].getDate() != null || //
				!patientAgeFromTextField.getText().isEmpty() || //
				!patientAgeToTextField.getText().isEmpty() || //
				patientSexBox.getSelectedIndex() > 0 || //
				!searchString.getText().isEmpty();
		if (!isFilteredList) {
			int ok = MessageDialog.okCancel(this, "angal.common.thiscouldretrievealargeamountofdataproceed.msg");
			if (ok != JOptionPane.OK_OPTION) {
				return;
			}
		}

		LocalDateTime[] admissionRange = new LocalDateTime[2];
		LocalDateTime[] dischargeRange = new LocalDateTime[2];
		for (int i = 0; i <= dateChoosers.length - 1; i++) {
			switch (i) {
			case 0:
				admissionRange[0] = dateChoosers[i].getDateStartOfDay();
				break;
			case 1:
				admissionRange[1] = dateChoosers[i].getDateEndOfDay();
				break;
			case 2:
				dischargeRange[0] = dateChoosers[i].getDateStartOfDay();
				break;
			case 3:
				dischargeRange[1] = dateChoosers[i].getDateEndOfDay();
				break;
			}
		}

		try {
			pPatient = admissionBrowserManager.getAdmittedPatients(admissionRange, dischargeRange, searchString.getText());
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		filterPatient(null);
	}

	private JButton getButtonSearch() {
		if (jSearchButton == null) {
			jSearchButton = new JButton();
			jSearchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			jSearchButton.setPreferredSize(new Dimension(20, 20));
			jSearchButton.addActionListener(actionEvent -> searchPatient());
		}
		return jSearchButton;
	}

	private JPanel setMyBorder(JPanel c, String title) {
		Border b2 = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title), BorderFactory.createEmptyBorder(0, 0, 0, 0));
		c.setBorder(b2);
		return c;
	}

	class AdmittedPatientBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		List<AdmittedPatient> patientList = new ArrayList<>();

		public AdmittedPatientBrowserModel(String key) {
			for (AdmittedPatient ap : pPatient) {
				Admission adm = ap.getAdmission();
				// if not admitted stripes admitted
				if (patientClassBox.getSelectedItem().equals(patientClassItems[2])) {
					if (adm != null) {
						continue;
					}
				}
				// if admitted stripes not admitted
				else if (patientClassBox.getSelectedItem().equals(patientClassItems[1])) {
					if (adm == null) {
						continue;
					}
				}

				// if all or admitted filters not matching ward
				if (!patientClassBox.getSelectedItem().equals(patientClassItems[2])) {
					if (adm != null) {
						int cc = -1;
						for (int j = 0; j < wardList.size(); j++) {
							if (adm.getWard().getCode().equalsIgnoreCase(wardList.get(j).getCode())) {
								cc = j;
								break;
							}
						}
						if (!wardCheck[cc].isSelected()) {
							continue;
						}
					}
				}

				// lower age limit
				String ageLimit = patientAgeFromTextField.getText();
				if (DIGIT_PATTERN.matcher(ageLimit).matches()) {
					if (!(ap.getPatient().getAge() >= Integer.parseInt(ageLimit))) {
						continue;
					}
				}

				// upper age limit
				ageLimit = patientAgeToTextField.getText();
				if (DIGIT_PATTERN.matcher(ageLimit).matches()) {
					if (!(ap.getPatient().getAge() <= Integer.parseInt(ageLimit))) {
						continue;
					}
				}

				// sex patient type
				Character sex = null;
				switch (patientSexBox.getSelectedIndex()) {
				case 1:
					sex = 'M';
					break;
				case 2:
					sex = 'F';
					break;
				}

				if (sex != null && !sex.equals(ap.getPatient().getSex())) {
					continue;
				}

				if (key != null) {
					String s = key + lastKey;
					s = s.trim();
					String[] tokens = s.split(" ");

					if (!s.isEmpty()) {
						String name = ap.getPatient().getSearchString();
						int a = 0;
						for (String value : tokens) {
							String token = value.toLowerCase();
							if (NormalizeString.normalizeContains(name, token)) {
								a++;
							}
						}
						if (a == tokens.length) {
							patientList.add(ap);
						}
					} else {
						patientList.add(ap);
					}
				} else {
					patientList.add(ap);
				}
			}
		}

		@Override
		public int getRowCount() {
			if (patientList == null) {
				return 0;
			}
			return patientList.size();
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
		public Object getValueAt(int r, int c) {
			AdmittedPatient admPat = patientList.get(r);
			Patient patient = admPat.getPatient();
			Admission admission = admPat.getAdmission();
			if (c == -1) {
				return admPat;
			} else if (c == 0) {
				return patient.getCode();
			} else if (c == 1) {
				return patient.getName();
			} else if (c == 2) {
				return TimeTools.getFormattedAge(patient.getBirthDate());
			} else if (c == 3) {
				return patient.getSex();
			} else if (c == 4) {
				return patient.getInformations();
			} else if (c == 5) {
				if (admission == null) {
					return "";
				} else {
					for (Ward ward : wardList) {
						if (ward.getCode().equalsIgnoreCase(admission.getWard().getCode())) {
							return ward.getDescription();
						}
					}
					return "?";
				}
			}

			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	class CenterTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			return cell;
		}
	}

}
