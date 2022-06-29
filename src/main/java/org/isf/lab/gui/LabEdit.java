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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.isf.lab.gui.elements.ExamRowSubPanel;
import org.isf.lab.gui.elements.MatComboBox;
import org.isf.lab.gui.elements.PatientComboBox;
import org.isf.lab.manager.LabManager;
import org.isf.lab.manager.LabRowManager;
import org.isf.lab.model.Laboratory;
import org.isf.lab.model.LaboratoryForPrint;
import org.isf.lab.model.LaboratoryRow;
import org.isf.lab.service.LabIoOperations;
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

/**
 * ------------------------------------------
 * LabEdit - Add/edit a laboratory exam
 * -----------------------------------------
 * modification history
 * 02/03/2006 - Davide - first beta version
 * 03/11/2006 - ross - changed title, enlarged window
 *                   - changed note from textfield to textarea
 * 			         - version is now 1.0
 * 08/11/2006 - ross - added age, sex, exam date, material
 *                   - added editing capability
 * 18/08/2008 - Teo  - Add scroll capabilities at note JTextArea
 * 13/02/2009 - Alex - add calendar
 * ------------------------------------------
 */
public class LabEdit extends ModalJFrame {

	private static final long serialVersionUID = 1055379190540460482L;

	//LISTENER INTERFACE --------------------------------------------------------
	private EventListenerList labEditListener = new EventListenerList();
	
	public interface LabEditListener extends EventListener {
		void labUpdated();
	}
	
	public void addLabEditListener(LabEditListener l) {
		labEditListener.add(LabEditListener.class, l);
	}

	private void fireLabUpdated() {
		new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = labEditListener.getListeners(LabEditListener.class);
		for (EventListener listener : listeners) {
			((LabEditListener) listener).labUpdated();
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
	private JComboBox examRowComboBox = null;
	private PatientComboBox patientComboBox = null;
	private Exam examSelected = null;
	private JScrollPane noteScrollPane = null;

	private JTextArea noteTextArea = null;

	private VoLimitedTextField patTextField = null;
	private VoLimitedTextField ageTextField = null;
	private VoLimitedTextField sexTextField = null;

	private GoodDateTimeChooser examDateFieldCal = null;

	private static final int PANEL_WIDTH = 550;
	private static final int LABEL_WIDTH = 70;
	private static final int DATA_PANEL_HEIGHT = 170;
	private static final int RESULT_PANEL_HEIGHT = 350;
	private static final int BUTTON_PANEL_HEIGHT = 40;

	private ExamRowBrowsingManager rowManager = Context.getApplicationContext().getBean(ExamRowBrowsingManager.class);
	private LabManager labManager = Context.getApplicationContext().getBean(LabManager.class, Context.getApplicationContext().getBean(LabIoOperations.class));
	private LabRowManager lRowManager = Context.getApplicationContext().getBean(LabRowManager.class);

	private List<ExamRow> eRows = null;
	private PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);
	private Patient patSelected;

	private JTextField examRowTextField;

	private boolean examChanged;

	public LabEdit(JFrame owner, Laboratory laboratory, boolean inserting) {
		insert = inserting;
		lab = laboratory;
		initialize();
		showAsModal(owner);
	}

	private void initialize() {
		this.setBounds(30, 30, PANEL_WIDTH + 20, DATA_PANEL_HEIGHT + RESULT_PANEL_HEIGHT + BUTTON_PANEL_HEIGHT + 30);
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
			jContentPane.add(getDataPanel());
			resultPanel = new JPanel();
			resultPanel.setBounds(0, DATA_PANEL_HEIGHT, PANEL_WIDTH, RESULT_PANEL_HEIGHT);
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
			JLabel patientLabel = new JLabel(MessageBundle.getMessage("angal.lab.patient"));
			patientLabel.setBounds(5, 65, LABEL_WIDTH, 20);
			inPatientCheckBox = getInPatientCheckBox();
			inPatientCheckBox.setBounds(LABEL_WIDTH + 5, 65, LABEL_WIDTH, 20);
			patientComboBox = getPatientComboBox();
			patientComboBox.setBounds((LABEL_WIDTH + 5) * 2, 65, 395, 20);

			JLabel nameLabel = new JLabel(MessageBundle.getMessage("angal.common.name.txt"));
			nameLabel.setBounds(5, 90, LABEL_WIDTH, 20);
			patTextField = getPatientTextField();
			patTextField.setBounds(LABEL_WIDTH + 5, 90, 200, 20);
			JLabel ageLabel = new JLabel(MessageBundle.getMessage("angal.common.age.txt"));
			ageLabel.setBounds(300, 90, 35, 20);
			ageTextField = getAgeTextField();
			ageTextField.setBounds(340, 90, 50, 20);
			JLabel sexLabel = new JLabel(MessageBundle.getMessage("angal.lab.sexmf"));
			sexLabel.setBounds(405, 90, 80, 20);
			sexTextField = getSexTextField();
			sexTextField.setBounds(480, 90, 50, 20);
			//note
			JLabel noteLabel = new JLabel(MessageBundle.getMessage("angal.lab.note"));
			noteLabel.setBounds(5, 120, LABEL_WIDTH, 20);
			noteTextArea = getNoteTextArea();
			noteTextArea.setEditable(true);
			noteTextArea.setWrapStyleWord(true);
			noteTextArea.setAutoscrolls(true);

			//add all to the data panel
			dataPanel.add(examDateLabel, null);
			dataPanel.add(examDateFieldCal, null);
			dataPanel.add(materialLabel, null);
			dataPanel.add(matComboBox, null);
			dataPanel.add(examLabel, null);
			dataPanel.add(examComboBox, null);
			dataPanel.add(patientLabel, null);
			dataPanel.add(inPatientCheckBox, null);
			dataPanel.add(patientComboBox, null);
			dataPanel.add(nameLabel, null);
			dataPanel.add(patTextField);
			dataPanel.add(ageLabel, null);
			dataPanel.add(ageTextField);
			dataPanel.add(sexLabel, null);
			dataPanel.add(sexTextField);
			dataPanel.add(noteLabel, null);

			dataPanel.setPreferredSize(new Dimension(150, 200));

			/*
			 * Teo : Adding scroll capabilities at note textArea
			 */
			if (noteScrollPane == null) {
				noteScrollPane = new JScrollPane(noteTextArea);
				noteScrollPane.setBounds(LABEL_WIDTH +5, 120, 470, 40);
				noteScrollPane.createVerticalScrollBar();
				noteScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				noteScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				noteScrollPane.setAutoscrolls(true);
				dataPanel.add(noteScrollPane);
			}
		}
		return dataPanel;
	}

	private GoodDateTimeChooser getExamDateFieldCal() {
		LocalDateTime dateIn;
		if (insert) {
			dateIn = RememberDates.getLastLabExamDate();
		} else { 
			dateIn = lab.getDate();
		}
		if (dateIn == null) {
			dateIn = LocalDateTime.now();
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
	 * with the difference that here will be optional
	 * If no patient is chosen only Name, Age and Sex will be saved
	 * in LABORATORY table (Name can be empty)
	 */
	private PatientComboBox getPatientComboBox() {
		if (patientComboBox == null) {
			patientComboBox = new PatientComboBox();
			patSelected=null;
			PatientBrowserManager patBrowser = Context.getApplicationContext().getBean(PatientBrowserManager.class);
			List<Patient> pat = null;
			try {
				pat = patBrowser.getPatient();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}

			patientComboBox = PatientComboBox.withPatientsAndPatientFromLaboratorySelected(pat, lab, insert);
			patSelected = patientComboBox.getSelectedPatient().orElse(null);

			patientComboBox.addActionListener(actionEvent -> {
				if (patientComboBox.getSelectedIndex() > 0) {
					AdmissionBrowserManager admMan = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
					patSelected = (Patient) patientComboBox.getSelectedItem();
					patTextField.setText(patSelected.getName());
					ageTextField.setText(patSelected.getAge() + "");
					sexTextField.setText(patSelected.getSex() + "");
					Admission admission = null;
					try {
						admission = admMan.getCurrentAdmission(patSelected);
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
					inPatientCheckBox.setSelected(admission != null);
				}
			});
		}
		return patientComboBox;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setBounds(0, DATA_PANEL_HEIGHT + RESULT_PANEL_HEIGHT, PANEL_WIDTH, BUTTON_PANEL_HEIGHT);
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
			examSelected = examComboBox.getSelectedExam().orElse(null);

			examComboBox.addActionListener(actionEvent -> examComboBox.getSelectedExam().ifPresent(exam -> {
				examSelected = exam;

				if (examSelected.getProcedure() == 1) {
					resultPanel = getFirstPanel();
				} else if (examSelected.getProcedure() == 2) {
					resultPanel = getSecondPanel();
				} else if (examSelected.getProcedure() == 3) {
					resultPanel = getThirdPanel();
				}

				validate();
				repaint();
			}));
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
		return Optional.ofNullable(noteTextArea)
				.orElseGet(() -> {
					noteTextArea = new JTextArea(10, 35);
					if (!insert) {
						noteTextArea.setText(lab.getNote());
					}
					noteTextArea.setLineWrap(true);
					noteTextArea.setPreferredSize(new Dimension(10, 35));
					noteTextArea.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
					return noteTextArea;
				});
	}

	private VoLimitedTextField getPatientTextField() {
		return Optional.ofNullable(patTextField)
				.orElseGet(() -> {
					patTextField = new VoLimitedTextField(100);
					if (!insert) {
						patTextField.setText(lab.getPatName());
					}
					return patTextField;
				});
	}

	private VoLimitedTextField getAgeTextField() {
		if (ageTextField == null) {
			ageTextField = new VoLimitedTextField(3);
			if (insert) {
				ageTextField.setText("");
			} else {
				try {
					Integer intAge = lab.getAge();
					ageTextField.setText(intAge.toString());
				} catch (Exception e) {
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
					MessageDialog.error(null, "angal.lab.pleaseselectanexam.msg");
					return;
				}
				String matSelected = (String) matComboBox.getSelectedItem();
				examSelected = (Exam) examComboBox.getSelectedItem();

				Integer patId = -1;
				if (patientComboBox.getSelectedIndex() > 0) {
					patId = ((Patient) (patientComboBox.getSelectedItem())).getCode();
				}
				String sex = sexTextField.getText().toUpperCase();
				if (!(sex.equals("M") || sex.equals("F"))) {
					MessageDialog.error(null, "angal.lab.pleaseinsertmformaleorfforfemale.msg");
					return;
				}

				if (examSelected.getProcedure() == 3 && examRowTextField.getText().isEmpty()) {
					MessageDialog.error(null, "angal.labnew.pleaseinsertavalidvalue");
					return;
				}
				// exam date
				LocalDateTime examDate = LocalDateTime.now();
				List<String> labRow = new ArrayList<>();
				RememberDates.setLastLabExamDate(examDate);

				lab.setDate(examDate);
				lab.setExamDate(LocalDate.now());
				lab.setMaterial(labManager.getMaterialKey(matSelected));
				lab.setExam(examSelected);
				lab.setNote(noteTextArea.getText());
				lab.setInOutPatient((inPatientCheckBox.isSelected() ? "I" : "O"));
				lab.setPatient(patSelected);
				lab.setPatName(patTextField.getText());
				int tmpAge = 0;
				try {
					tmpAge = Integer.parseInt(ageTextField.getText());
				} catch (Exception ex) {
					MessageDialog.error(LabEdit.this, "angal.lab.insertvalidage.msg");
				}
				lab.setSex(sexTextField.getText().toUpperCase());

				if (examSelected.getProcedure() == 1) {
					lab.setResult(examRowComboBox.getSelectedItem().toString());
				} else if (examSelected.getProcedure() == 2) {
					lab.setResult(MessageBundle.getMessage("angal.lab.multipleresults.txt"));
					labRow = IntStream.range(0, resultPanel.getComponentCount())
							.filter(i -> ((ExamRowSubPanel) resultPanel.getComponent(i)).getSelectedResult().equalsIgnoreCase("P"))
							.mapToObj(i -> eRows.get(i).getDescription())
							.collect(Collectors.toCollection(ArrayList::new));
				} else if (examSelected.getProcedure() == 3) {
					lab.setResult(examRowTextField.getText());
				}
				boolean result;
				if (insert) {
					lab.setAge(tmpAge);
					try {
						result = labManager.newLaboratory(lab, labRow);
					} catch (OHServiceException e1) {
						result = false;
						OHServiceExceptionUtil.showMessages(e1);
					}
				} else {
					try {
						result = labManager.updateLaboratory(lab, labRow);
					} catch (OHServiceException e1) {
						result = false;
						OHServiceExceptionUtil.showMessages(e1);
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
		examRowComboBox = new JComboBox();
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
		if (null != rows) {
			for (ExamRow r : rows) {
				if (!r.getDescription().equals(result)) {
					examRowComboBox.addItem(r.getDescription());
				}
			}
		}
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
			if (null != eRows) {
				eRows.forEach(r -> resultPanel.add(ExamRowSubPanel.forExamRow(r)));
			}
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
		examRowTextField = new JTextField();
		examRowTextField.setMaximumSize(new Dimension(200, 25));
		examRowTextField.setMinimumSize(new Dimension(200, 25));
		examRowTextField.setPreferredSize(new Dimension(200, 25));
		if (insert || examChanged) {
			result = examSelected.getDefaultResult();
			examChanged = false;
		} else {
			result = lab.getResult();
		}
		examRowTextField.setText(result);
		resultPanel.add(examRowTextField);
		return resultPanel;
	}

}
