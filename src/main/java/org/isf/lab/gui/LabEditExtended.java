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
package org.isf.lab.gui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.EventListenerList;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.manager.ExamRowBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.exa.model.ExamRow;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.gui.elements.ExamComboBox;
import org.isf.lab.gui.elements.ExamRowComboBox;
import org.isf.lab.gui.elements.ExamRowSubPanel;
import org.isf.lab.gui.elements.MatComboBox;
import org.isf.lab.gui.elements.PatientComboBox;
import org.isf.lab.manager.LabManager;
import org.isf.lab.manager.LabRowManager;
import org.isf.lab.model.Laboratory;
import org.isf.lab.model.LaboratoryForPrint;
import org.isf.lab.model.LaboratoryRow;
import org.isf.menu.manager.Context;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.serviceprinting.manager.PrintManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.RememberDates;

/** ------------------------------------------
 * LabEditExtended - Add/edit a laboratory exam
 * -----------------------------------------
 * modification history
 * ------------------------------------------
 */
public class LabEditExtended extends ModalJFrame {

	private static final long serialVersionUID = -6576310684918676344L;
	
	//LISTENER INTERFACE --------------------------------------------------------
	private EventListenerList labEditExtendedListener = new EventListenerList();
	
	public interface LabEditExtendedListener extends EventListener {
		void labUpdated();
	}
	
	public void addLabEditExtendedListener(LabEditExtendedListener l) {
		labEditExtendedListener.add(LabEditExtendedListener.class, l);
	}

	private void fireLabUpdated() {
		new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = labEditExtendedListener.getListeners(LabEditExtendedListener.class);
		for (EventListener listener : listeners) {
			((LabEditExtendedListener) listener).labUpdated();
		}
	}

	//---------------------------------------------------------------------------
	
	private boolean insert;

	private Laboratory lab;
	private JPanel jContentPane = null;
	private JPanel buttonPanel = null;
	private JPanel dataPanel = null;
	private JPanel resultPanel = null;
	private JCheckBox inPatientCheckBox = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private JButton printButton = null;
	private JComboBox matComboBox = null;
	private ExamComboBox examComboBox = null;
	private ExamRowComboBox examRowComboBox = null;
	private PatientComboBox patientComboBox = null;
	private Exam examSelected = null;
	private JScrollPane noteScrollPane = null;

	private JTextArea noteTextArea = null;

	private VoLimitedTextField patTextField = null;
	private VoLimitedTextField ageTextField = null;
	private VoLimitedTextField sexTextField = null;

	private JPanel dataPatient = null;
	private VoLimitedTextField jTextPatientSrc;
	private Patient labPat = null;
	private String lastKey;
	private String s;
	private List<Patient> pat = null;

	private GoodDateTimeChooser examDateFieldCal = null;

	private static final int PANEL_WIDTH = 550;
	private static final int LABEL_WIDTH = 70;
	private static final int DATA_PANEL_HEIGHT = 90;
	private static final int DATA_PATIENT_HEIGHT = 100;
	private static final int RESULT_PANEL_HEIGHT = 350;
	private static final int BUTTON_PANEL_HEIGHT = 40;

	private List<ExamRow> eRows = null;
	
	private LabManager labManager = Context.getApplicationContext().getBean(LabManager.class);
	private PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);
	private LabRowManager lRowManager = Context.getApplicationContext().getBean(LabRowManager.class);
	private AdmissionBrowserManager admMan = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
	private ExamRowBrowsingManager rowManager = Context.getApplicationContext().getBean(ExamRowBrowsingManager.class);

	private JTextField examTextField;

	private boolean examChanged;
	
	public LabEditExtended(JFrame owner, Laboratory laboratory, boolean inserting) {
		insert = inserting;
		lab = laboratory;
		initialize();
		showAsModal(owner);
	}

	private void initialize() {
		this.setBounds(30, 30, PANEL_WIDTH + 20, DATA_PANEL_HEIGHT + DATA_PATIENT_HEIGHT + RESULT_PANEL_HEIGHT + BUTTON_PANEL_HEIGHT + 30);
		this.setContentPane(getJContentPane());
		this.setResizable(false);
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.lab.newlaboratoryexam.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.lab.editlaboratoryexam.title"));
		}
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setLocationRelativeTo(null);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			// data panel
			jContentPane.add(getDataPatient());
			jContentPane.add(getDataPanel());
			resultPanel = new JPanel();
			resultPanel.setBounds(0, DATA_PANEL_HEIGHT + DATA_PATIENT_HEIGHT, PANEL_WIDTH, RESULT_PANEL_HEIGHT);
			if (!insert) {
				examSelected = lab.getExam();
				if (examSelected.getProcedure() == 1) {
					resultPanel = getFirstPanel();
				} else if (examSelected.getProcedure() == 2) {
					resultPanel = getSecondPanel();
				} else if (examSelected.getProcedure() == 3) {
					resultPanel = getThirdPanel();
				}
			}
			resultPanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.GRAY), MessageBundle.getMessage("angal.common.result.txt")));
			jContentPane.add(resultPanel);
			jContentPane.add(getButtonPanel());
		}
		return jContentPane;
	}

	private JPanel getDataPanel() {
		if (dataPanel == null) {
			//initialize data panel
			dataPanel = new JPanel();
			dataPanel.setLayout(null);
			dataPanel.setBounds(0, 0, PANEL_WIDTH, DATA_PANEL_HEIGHT);
			//exam date
			JLabel examDateLabel = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
			examDateLabel.setBounds(5, 10, LABEL_WIDTH, 25);
			examDateFieldCal = getExamDateFieldCal();
			examDateFieldCal.setBounds(LABEL_WIDTH + 5, 10, 200, 25);
			//material
			JLabel materialLabel = new JLabel(MessageBundle.getMessage("angal.lab.material"));
			materialLabel.setBounds(290, 10, 140, 20);
			matComboBox = getMatComboBox();
			matComboBox.setBounds(360, 10, 185, 20);

			//exam combo
			JLabel examLabel = new JLabel(MessageBundle.getMessage("angal.lab.exam"));
			examLabel.setBounds(5, 40, LABEL_WIDTH, 20);
			examComboBox = getExamComboBox();
			examComboBox.setBounds(LABEL_WIDTH + 5, 40, 470, 20);

			//patient (in or out) data
			JLabel patientLabel = new JLabel(MessageBundle.getMessage("angal.lab.patientcode"));
			patientLabel.setBounds(LABEL_WIDTH + 5, 65, 120, 20);
			inPatientCheckBox = getInPatientCheckBox();
			inPatientCheckBox.setBounds(5, 65, LABEL_WIDTH, 20);
			jTextPatientSrc = new VoLimitedTextField(200, 20);
			jTextPatientSrc.setBounds(LABEL_WIDTH + 70, 65, 90, 20);

			jTextPatientSrc.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
					lastKey = "";
					String keyChar = "" + e.getKeyChar();
					if (Character.isLetterOrDigit(e.getKeyChar())) {
						lastKey = keyChar;
					}
					keyChar = jTextPatientSrc.getText() + lastKey;
					keyChar = keyChar.trim();

					filterPatient(keyChar);
				}

				//@Override
				@Override
				public void keyPressed(KeyEvent e) {
				}

				//@Override
				@Override
				public void keyReleased(KeyEvent e) {
				}
			});
			patientComboBox = getPatientComboBox(s);
			patientComboBox.setBounds(LABEL_WIDTH + 170, 65, 305, 20);

			//add all to the data panel
			dataPanel.add(examDateLabel, null);
			dataPanel.add(examDateFieldCal, null);
			dataPanel.add(materialLabel, null);
			dataPanel.add(matComboBox, null);
			dataPanel.add(examLabel, null);
			dataPanel.add(examComboBox, null);
			dataPanel.add(patientLabel, null);
			dataPanel.add(inPatientCheckBox, null);
			//ADDED: Alex
			dataPanel.add(jTextPatientSrc, null);
			dataPanel.add(patientComboBox, null);

			dataPanel.setPreferredSize(new Dimension(150, 200));
		}
		return dataPanel;
	}

	private JPanel getDataPatient() {
		if (dataPatient == null) {
			dataPatient = new JPanel();
			dataPatient.setLayout(null);
			dataPatient.setBounds(0, DATA_PANEL_HEIGHT, PANEL_WIDTH, DATA_PATIENT_HEIGHT);
			dataPatient.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.GRAY), MessageBundle.getMessage("angal.lab.datapatient")));

			JLabel nameLabel = new JLabel(MessageBundle.getMessage("angal.common.name.txt"));
			nameLabel.setBounds(10, 20, LABEL_WIDTH, 20);
			patTextField = getPatientTextField();
			patTextField.setBounds(LABEL_WIDTH +5, 20, 200, 20);
			JLabel ageLabel = new JLabel(MessageBundle.getMessage("angal.common.age.txt"));
			ageLabel.setBounds(300, 20, 35, 20);
			ageTextField = getAgeTextField();
			ageTextField.setBounds(340, 20, 50, 20);
			JLabel sexLabel = new JLabel(MessageBundle.getMessage("angal.lab.sexmf"));
			sexLabel.setBounds(405, 20, 80, 20);
			sexTextField = getSexTextField();
			sexTextField.setBounds(480, 20, 50, 20);
			//note			
			JLabel noteLabel = new JLabel(MessageBundle.getMessage("angal.lab.note"));
			noteLabel.setBounds(10, 50, LABEL_WIDTH, 20);
			noteTextArea = getNoteTextArea();
			noteTextArea.setEditable(true);
			noteTextArea.setWrapStyleWord(true);
			noteTextArea.setAutoscrolls(true);
			
			/*
			 * Teo : Adding scroll capabilities at note textArea
			 */
			if (noteScrollPane == null) {
				noteScrollPane = new JScrollPane(noteTextArea);
				noteScrollPane.setBounds(LABEL_WIDTH +5, 50, 460, 35);
				noteScrollPane.createVerticalScrollBar();
				noteScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				noteScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				noteScrollPane.setAutoscrolls(true);
				dataPatient.add(noteScrollPane);
			}
			
			dataPatient.add(nameLabel, null);
			dataPatient.add(patTextField);
			dataPatient.add(ageLabel, null);
			dataPatient.add(ageTextField);
			dataPatient.add(sexLabel, null);
			dataPatient.add(sexTextField);
			dataPatient.add(noteLabel, null);
			
			patTextField.setEditable(false);
			ageTextField.setEditable(false);
			sexTextField.setEditable(false);
			noteTextArea.setEditable(true);
			
		}
		return dataPatient;
	}

	private GoodDateTimeChooser getExamDateFieldCal() {
		LocalDateTime dateIn;
		if (insert) {
			dateIn = RememberDates.getLastLabExamDate();
			if (dateIn == null) {
				dateIn = LocalDateTime.now();
			}
		} else {
			dateIn = lab.getDate();
		}
		return new GoodDateTimeChooser(dateIn);
	}
	
	private JCheckBox getInPatientCheckBox() {
		if (inPatientCheckBox == null) {
			inPatientCheckBox = new JCheckBox(MessageBundle.getMessage("angal.lab.in"));
			if (!insert) {
				inPatientCheckBox.setSelected(lab.getInOutPatient().equalsIgnoreCase("I"));
			}
			lab.setInOutPatient((inPatientCheckBox.isSelected()?"I":"R"));
		}
		return inPatientCheckBox;
	}

	/*
	 * TODO: Patient Selection like in LabNew
	 */
	private PatientComboBox getPatientComboBox(String s) {
		
		PatientBrowserManager patBrowser = Context.getApplicationContext().getBean(PatientBrowserManager.class);
		try {
			if (insert) {
				// TODO: Investigate whether this should be deprecated in favor of getPatient(int page, int size)
				pat = patBrowser.getPatient();
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (patientComboBox == null) {
			patientComboBox = new PatientComboBox();
			patientComboBox.addItem(MessageBundle.getMessage("angal.lab.selectapatient"));

			if (!insert && lab.getPatient() != null) {
				try {
					labPat = patBrowser.getPatientAll(lab.getPatient().getCode());
					patientComboBox.addItem(labPat);
					patientComboBox.setSelectedItem(labPat);
					patientComboBox.setEnabled(false);
					jTextPatientSrc.setText(String.valueOf(labPat.getCode()));
					jTextPatientSrc.setEnabled(false);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				return patientComboBox;
			}

			Optional.ofNullable(pat)
					.ifPresent(patients -> patients.stream().forEach(patientComboBox::addItem));

			patientComboBox.addActionListener(actionEvent -> {
				if (patientComboBox.getSelectedIndex() > 0) {
					labPat = (Patient) patientComboBox.getSelectedItem();
					setPatient(labPat);
					Admission admission = null;
					try {
						admission = admMan.getCurrentAdmission(labPat);
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
					inPatientCheckBox.setSelected(admission != null);
				}
			});
		}
		return patientComboBox;
	}

	private void filterPatient(String key) {
		patientComboBox.removeAllItems();

		if (key == null || key.compareTo("") == 0) {
			patientComboBox.addItem(MessageBundle.getMessage("angal.lab.selectapatient"));
			resetLabPat();
		}

		patientComboBox.addPatientsFilteredByKey(pat, key);

		if (patientComboBox.getItemCount() > 0) {
			patientComboBox.getSelectedPatient().ifPresent(patient -> {
				labPat = patient;
				setPatient(labPat);
			});
		}
	}

	private void resetLabPat() {
		patTextField.setText("");
		ageTextField.setText("");
		sexTextField.setText("");
		noteTextArea.setText("");
		labPat = null;
	}

	private void setPatient(Patient labPat) {
		patTextField.setText(labPat.getName());
		ageTextField.setText(labPat.getAge()+"");
		sexTextField.setText(labPat.getSex()+"");
		noteTextArea.setText(labPat.getNote());		
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setBounds(0, DATA_PANEL_HEIGHT + DATA_PATIENT_HEIGHT + RESULT_PANEL_HEIGHT, PANEL_WIDTH, BUTTON_PANEL_HEIGHT);
			buttonPanel.add(getOkButton(), null);
			buttonPanel.add(getPrintButton(), null);
			buttonPanel.add(getCancelButton(), null);
		}
		return buttonPanel;
	}

	private ExamComboBox getExamComboBox() {
		if (examComboBox == null) {

			ExamBrowsingManager manager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
			List<Exam> exams;
			try {
				exams = manager.getExams();
			} catch (OHServiceException e) {
				exams = null;
				OHServiceExceptionUtil.showMessages(e);
			}
			examComboBox = ExamComboBox.withExamsAndExamFromLaboratorySelected(exams, lab, insert);
			examComboBox.addItem(MessageBundle.getMessage("angal.lab.selectanexam"));

			examComboBox.addActionListener(actionEvent -> {
				if (!(examComboBox.getSelectedItem() instanceof String)) {
					examSelected = (Exam) examComboBox.getSelectedItem();

					if (examSelected.getProcedure() == 1) {
						resultPanel = getFirstPanel();
					} else if (examSelected.getProcedure() == 2) {
						resultPanel = getSecondPanel();
					} else if (examSelected.getProcedure() == 3) {
						resultPanel = getThirdPanel();
					}

					validate();
					repaint();
				}
			});
			resultPanel = null;
		}
		return examComboBox;
	}

	private JComboBox getMatComboBox() {
		return Optional.ofNullable(matComboBox)
				.orElseGet(() -> {
					LabManager labMan = Context.getApplicationContext().getBean(LabManager.class);
					List<String> materialList = labMan.getMaterialList();
					return MatComboBox.withMaterialsAndMaterialFromLabSelected(materialList, lab, insert, labManager::getMaterialTranslated);
				});
	}

	private JTextArea getNoteTextArea() {
		if (noteTextArea == null) {
			noteTextArea = new JTextArea(10, 30);
			if (!insert) {
				noteTextArea.setText(lab.getNote());
			}
			noteTextArea.setLineWrap(true);
			noteTextArea.setPreferredSize(new Dimension(10, 30));
			noteTextArea.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		}
		return noteTextArea;
	}

	private VoLimitedTextField getPatientTextField() {
		if (patTextField == null) {
			patTextField = new VoLimitedTextField(100);
			if (!insert) {
				patTextField.setText(lab.getPatName());
			}
		}
		return patTextField;
	}

	
	private VoLimitedTextField getAgeTextField() {
		if (ageTextField == null) {
			ageTextField = new VoLimitedTextField(3);
			if (insert) {
				ageTextField.setText("");
				}
			else {
				try {	
					Integer intAge=lab.getAge();
					ageTextField.setText(intAge.toString());
					}
				catch (Exception e) {
					ageTextField.setText("");
					}
				}
			}
		return ageTextField;
	}
	
	private VoLimitedTextField getSexTextField() {
		if (sexTextField == null) {
			sexTextField = new VoLimitedTextField(1);
			if (!insert) {
				sexTextField.setText(lab.getSex());
			}
		}
		return sexTextField;
	}

	private JButton getPrintButton() {
		if (printButton == null) {
			printButton = new JButton(MessageBundle.getMessage("angal.common.print.btn"));
			printButton.setMnemonic(MessageBundle.getMnemonic("angal.common.print.btn.key"));
			printButton.addActionListener(actionEvent -> {
				try {
					List<LaboratoryForPrint> labs = new ArrayList<>();

					labs.add(new LaboratoryForPrint(
									lab.getCode(),
									lab.getExam(),
									lab.getDate(),
									lab.getResult()
							)
					);
					if (!labs.isEmpty()) {
						printManager.print(MessageBundle.getMessage("angal.common.laboratory.txt"), labs, 0);
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			});
		}
		return printButton;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
		}
		return cancelButton;
	}

	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(actionEvent -> {
				if (examComboBox.getSelectedIndex() == 0) {
					MessageDialog.error(LabEditExtended.this, "angal.lab.pleaseselectanexam.msg");
					return;
				}
				String matSelected = (String) matComboBox.getSelectedItem();
				examSelected = (Exam) examComboBox.getSelectedItem();
				try {
					labPat = (Patient) patientComboBox.getSelectedItem();
				} catch (ClassCastException e2) {
					MessageDialog.error(LabEditExtended.this, "angal.common.pleaseselectapatient.msg");
					return;
				}
				LocalDateTime examDate = LocalDateTime.now();
				try {
					examDate = examDateFieldCal.getLocalDateTime();
				} catch (Exception e1) {
					MessageDialog.error(LabEditExtended.this, "angal.lab.pleaseinsertavalidexamdate.msg");
					return;
				}
				if (examSelected.getProcedure() == 3 && examTextField.getText().isEmpty()) {
					MessageDialog.error(LabEditExtended.this, "angal.labnew.pleaseinsertavalidvalue");
					return;
				}
				List<String> labRow = new ArrayList<>();
				lab.setDate(LocalDateTime.now());
				lab.setExamDate(examDate.toLocalDate());
				RememberDates.setLastLabExamDate(examDate);
				lab.setMaterial(labManager.getMaterialKey(matSelected));
				lab.setExam(examSelected);
				lab.setNote(noteTextArea.getText());
				lab.setInOutPatient((inPatientCheckBox.isSelected() ? "I" : "O"));
				lab.setPatient(labPat);
				lab.setPatName(labPat.getName());
				lab.setSex(labPat.getSex() + "");

				if (examSelected.getProcedure() == 1) {
					lab.setResult(examRowComboBox.getSelectedItem().toString());
				} else if (examSelected.getProcedure() == 2) {
					lab.setResult(MessageBundle.getMessage("angal.lab.multipleresults.txt"));
					for (int i = 0; i < resultPanel.getComponentCount(); i++) {
						if (((ExamRowSubPanel) resultPanel.getComponent(i)).getSelectedResult().equalsIgnoreCase("P")) {
							labRow.add(eRows.get(i).getDescription());
						}
					}
				} else if (examSelected.getProcedure() == 3) {
					lab.setResult(examTextField.getText());
				}
				boolean result;
				if (insert) {
					lab.setAge(labPat.getAge());
					try {
						result = labManager.newLaboratory(lab, labRow);
					} catch (OHServiceException e1) {
						result = false;
						OHServiceExceptionUtil.showMessages(e1);
						return;
					}
				} else {
					try {
						result = labManager.updateLaboratory(lab, labRow);
					} catch (OHServiceException e1) {
						result = false;
						OHServiceExceptionUtil.showMessages(e1);
						return;
					}
				}
				if (!result) {
					MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
				} else {
					fireLabUpdated();
					dispose();
				}
			});
		}
		return okButton;
	}

	private JPanel getFirstPanel() {
		resultPanel.removeAll();
		String result;
		examRowComboBox = new ExamRowComboBox();
		examRowComboBox.setMaximumSize(new Dimension(200, 25));
		examRowComboBox.setMinimumSize(new Dimension(200, 25));
		examRowComboBox.setPreferredSize(new Dimension(200, 25));
		if (insert) {
			result = examSelected.getDefaultResult();
		} else {
			result = lab.getResult();
		}
		examRowComboBox.addItem(result);

		List<ExamRow> rows;
		try {
			rows = rowManager.getExamRowByExamCode(examSelected.getCode());
		} catch (OHServiceException e) {
			rows = null;
			OHServiceExceptionUtil.showMessages(e);
		}

		examRowComboBox.addExamRowsWithDescriptionNotEqualTo(rows, result);
		if (examRowComboBox.getItemCount() > 0) {
			resultPanel.add(examRowComboBox);
		}
		return resultPanel;
	}

	private JPanel getSecondPanel() {
		resultPanel.removeAll();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
		String examId = examSelected.getCode();
		eRows = null;

		try {
			eRows = rowManager.getExamRowByExamCode(examId);
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}
		if (insert) {
			Optional.ofNullable(eRows)
					.ifPresent(examRows -> eRows.forEach(ExamRowSubPanel::forExamRow));
		} else {
			List<LaboratoryRow> lRows;
			try {
				lRows = lRowManager.getLabRowByLabId(lab.getCode());
			} catch (OHServiceException e) {
				lRows = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			List<LaboratoryRow> finalLRows = lRows;
			Optional.ofNullable(eRows).ifPresent(examRows ->
					examRows.forEach(r -> resultPanel.add(ExamRowSubPanel.forExamRowAndLaboratoryRows(r, finalLRows))));
		}
		return resultPanel;
	}

	private JPanel getThirdPanel() {
		resultPanel.removeAll();
		String result;
		examTextField = new JTextField();
		examTextField.setMaximumSize(new Dimension(200, 25));
		examTextField.setMinimumSize(new Dimension(200, 25));
		examTextField.setPreferredSize(new Dimension(200, 25));
		if (insert || examChanged) {
			result = examSelected.getDefaultResult();
			examChanged = false;
		} else {
			result = lab.getResult();
		}
		examTextField.setText(result);
		resultPanel.add(examTextField);
		return resultPanel;
	}

}
