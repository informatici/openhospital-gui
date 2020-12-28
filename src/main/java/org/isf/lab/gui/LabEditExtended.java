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
package org.isf.lab.gui;

/*------------------------------------------
 * LabEdit - Add/edit a laboratory exam
 * -----------------------------------------
 * modification history
 * 
 *------------------------------------------*/

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.EventListenerList;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.manager.ExamRowBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.exa.model.ExamRow;
import org.isf.generaldata.GeneralData;
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
import org.isf.utils.jobjects.CustomJDateChooser;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.Converters;
import org.isf.utils.time.RememberDates;

public class LabEditExtended extends ModalJFrame {
	/**
	 * 
	 */
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

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;};
		
		EventListener[] listeners = labEditExtendedListener.getListeners(LabEditExtendedListener.class);
		Arrays.stream(listeners)
				.forEach(listener -> ((LabEditExtendedListener)listener).labUpdated());
	}
	//---------------------------------------------------------------------------
	
	//private static final String VERSION="v1.2";
	private static final String VERSION="2.0";
	
	private boolean insert = false;

	private Laboratory lab = null;
	private JPanel jContentPane = null;
	private JPanel buttonPanel = null;
	private JPanel dataPanel = null;
	private JPanel resultPanel = null;
	private JLabel examLabel = null;
	private JLabel noteLabel = null;
	private JLabel patientLabel = null;
	private JCheckBox inPatientCheckBox = null;
	private JLabel nameLabel = null;
	private JLabel ageLabel = null;
	private JLabel sexLabel = null;
	private JLabel examDateLabel = null;
	private JLabel matLabel = null;
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

	//ADDED: Alex
	private JPanel dataPatient = null;
	private VoLimitedTextField jTextPatientSrc;
	private Patient labPat = null;
	private String lastKey;
	private String s;
	private ArrayList<Patient> pat = null;
	//private JButton jSearchTrashButton = null;
	
	//private VoDateTextField examDateField = null;
	private CustomJDateChooser examDateFieldCal = null;
	private LocalDateTime dateIn = null;

	
	private static final Integer panelWidth=500; 
	private static final Integer labelWidth=50; 
	private static final Integer dataPanelHeight=90;
	private static final Integer dataPatientHeight=100;
	private static final Integer resultPanelHeight=350; 
	private static final Integer buttonPanelHeight=40; 

	
	private ArrayList<ExamRow> eRows = null;
	
	//private LabManager labManager = new LabManager(Context.getApplicationContext().getBean(LabIoOperations.class));
	private LabManager labManager = Context.getApplicationContext().getBean(LabManager.class);
	private PrintManager printManager = Context.getApplicationContext().getBean(PrintManager.class);
	private LabRowManager lRowManager = Context.getApplicationContext().getBean(LabRowManager.class);
	private AdmissionBrowserManager admMan = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
	private ExamRowBrowsingManager rowManager = Context.getApplicationContext().getBean(ExamRowBrowsingManager.class);

	private JTextField examTextField;

	private boolean examChanged;
	
	public LabEditExtended(JFrame owner, Laboratory laboratory, boolean inserting) {
		//super(owner, true);
		insert = inserting;
		lab = laboratory;
		initialize();
	}

	private void initialize() {

		this.setBounds(30,30,panelWidth+20,dataPanelHeight+dataPatientHeight+resultPanelHeight+buttonPanelHeight+30);
		this.setContentPane(getJContentPane());
		this.setResizable(false);
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.lab.newlaboratoryexam")+"("+VERSION+")");
		} else {
			this.setTitle(MessageBundle.getMessage("angal.lab.editlaboratoryexam")+"("+VERSION+")");
		}
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
			resultPanel.setBounds(0, dataPanelHeight+dataPatientHeight, panelWidth, resultPanelHeight);
			if (!insert) {
				examSelected = lab.getExam();
				if (examSelected.getProcedure() == 1)
					resultPanel = getFirstPanel();
				else if (examSelected.getProcedure() == 2)
					resultPanel = getSecondPanel();
				else if (examSelected.getProcedure() == 3)
					resultPanel = getThirdPanel();
			}
			resultPanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.GRAY), MessageBundle.getMessage("angal.lab.result")));
			jContentPane.add(resultPanel);
			jContentPane.add(getButtonPanel()); // Generated
		}
		return jContentPane;
	}

	private JPanel getDataPanel() {
		if (dataPanel == null) {
			//initialize data panel
			dataPanel = new JPanel();
			dataPanel.setLayout(null);
			dataPanel.setBounds(0, 0, panelWidth, dataPanelHeight);
			//exam date
			examDateLabel = new JLabel(MessageBundle.getMessage("angal.common.date"));
			examDateLabel.setBounds(5, 10, labelWidth, 20);
			//examDateField=getExamDateField();
			//examDateField.setBounds(labelWidth+5, 10, 70, 20);
			examDateFieldCal = getExamDateFieldCal();
			examDateFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
			examDateFieldCal.setDateFormatString("dd/MM/yy");
			examDateFieldCal.setBounds(labelWidth+5, 10, 90, 20);
			//material
			matLabel = new JLabel(MessageBundle.getMessage("angal.lab.material"));
			matLabel.setBounds(155, 10, labelWidth, 20);
			matComboBox= getMatComboBox();
			matComboBox.setBounds(215, 10, 280, 20);
			//exam combo
			examLabel = new JLabel(MessageBundle.getMessage("angal.lab.exam"));
			examLabel.setBounds(5, 35, labelWidth, 20);
			examComboBox=getExamComboBox();
			examComboBox.setBounds(labelWidth+5, 35, 440, 20);

			//patient (in or out) data
			patientLabel = new JLabel(MessageBundle.getMessage("angal.lab.patientcode"));
			patientLabel.setBounds(labelWidth+5, 60, 110 , 20);
			
			//ADDED: Alex
			inPatientCheckBox = getInPatientCheckBox();
			inPatientCheckBox.setBounds(5, 60, labelWidth, 20);
			jTextPatientSrc = new VoLimitedTextField(200,20);
			jTextPatientSrc.setBounds(labelWidth+50,60,100,20);
			
			jTextPatientSrc.addKeyListener(new KeyListener() {
				public void keyTyped(KeyEvent e) 
				{
					lastKey = "";
					String s = "" + e.getKeyChar();
					if (Character.isLetterOrDigit(e.getKeyChar())) {
						lastKey = s;
					}
					s = jTextPatientSrc.getText() + lastKey;
					s = s.trim();
					
					filterPatient(s);
				}

				//@Override
				public void keyPressed(KeyEvent e) {}

				//@Override
				public void keyReleased(KeyEvent e) {}
			});
			patientComboBox = getPatientComboBox(s);
			patientComboBox.setBounds(labelWidth+160, 60, 285, 20);

			//add all to the data panel
			dataPanel.add(examDateLabel, null);
			dataPanel.add(examDateFieldCal, null);
			dataPanel.add(matLabel, null);
			dataPanel.add(matComboBox, null);
			dataPanel.add(examLabel, null);
			dataPanel.add(examComboBox, null);
			dataPanel.add(patientLabel, null);
			dataPanel.add(inPatientCheckBox,null);
			//ADDED: Alex
			dataPanel.add(jTextPatientSrc,null);
			dataPanel.add(patientComboBox,null);
			
			dataPanel.setPreferredSize(new Dimension(150,200));
						
		}
		return dataPanel;
	}

	
// REPLACED BY CALENDAR	
//	private VoDateTextField getExamDateField() {
//		String d = "";
//		java.util.Date myDate = null;
//		if (insert)
//			dateIn=RememberDates.getLastLabExamDateGregorian();
//		 else 
//			dateIn  = lab.getExamDate();
//
//		if (dateIn!=null) {
//			myDate = dateIn.getTime();
//			d = currentDateFormat.format(myDate);
//		}
//		return (new VoDateTextField("dd/mm/yy", d, 15));
//	}

	private JPanel getDataPatient() {
		if (dataPatient == null) {
			dataPatient = new JPanel();
			dataPatient.setLayout(null);
			dataPatient.setBounds(0, dataPanelHeight, panelWidth, dataPatientHeight);
			dataPatient.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.GRAY), MessageBundle.getMessage("angal.lab.datapatient")));
			
			nameLabel = new JLabel(MessageBundle.getMessage("angal.lab.name"));
			nameLabel.setBounds(10, 20, labelWidth, 20);
			patTextField=getPatientTextField();
			patTextField.setBounds(labelWidth+5, 20, 180, 20);
			ageLabel = new JLabel(MessageBundle.getMessage("angal.lab.age"));
			ageLabel.setBounds(255, 20, 35, 20);
			ageTextField=getAgeTextField();
			ageTextField.setBounds(295, 20, 50, 20);
			sexLabel = new JLabel(MessageBundle.getMessage("angal.lab.sexmf"));
			sexLabel.setBounds(370, 20, 80, 20);
			sexTextField=getSexTextField();
			sexTextField.setBounds(440, 20, 50, 20);
			//note			
			noteLabel = new JLabel(MessageBundle.getMessage("angal.lab.note"));
			noteLabel.setBounds(10, 50, labelWidth, 20);
			noteTextArea = getNoteTextArea();
			noteTextArea.setBounds(labelWidth+5, 50, 440, 35);
			noteTextArea.setEditable(true);
			noteTextArea.setWrapStyleWord(true);
			noteTextArea.setAutoscrolls(true);
			
			/*
			 * Teo : Adding scroll capabilities at note textArea
			 */
			if(noteScrollPane == null)
			{
				noteScrollPane = new JScrollPane(noteTextArea);
				noteScrollPane.setBounds(labelWidth+5, 50, 440, 35);
				noteScrollPane.createVerticalScrollBar();
				noteScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				noteScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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

	private CustomJDateChooser getExamDateFieldCal() {
		if (insert) {
			dateIn = Converters.convertToLocalDateTime(RememberDates.getLastLabExamDateGregorian());
		} else { 
			dateIn = lab.getExamDate().atStartOfDay();
		}
		return (new CustomJDateChooser(dateIn, "dd/MM/yy"));
	}
	
	private JCheckBox getInPatientCheckBox() {
		if (inPatientCheckBox == null) {
			inPatientCheckBox = new JCheckBox(MessageBundle.getMessage("angal.lab.in"));
			if (!insert)
				inPatientCheckBox.setSelected(lab.getInOutPatient().equalsIgnoreCase("I"));
			lab.setInOutPatient((inPatientCheckBox.isSelected()?"I":"R"));
		}
		return inPatientCheckBox;
	}

	/*
	 * TODO: Patient Selection like in LabNew
	 */
	private PatientComboBox getPatientComboBox(String s) {

		//String key = s;
		PatientBrowserManager patBrowser = Context.getApplicationContext().getBean(PatientBrowserManager.class);
		try {
			if (insert){
				pat = patBrowser.getPatient();
			} else  {
				pat = patBrowser.getPatient();
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (patientComboBox == null) {
			patientComboBox = new PatientComboBox();
			patientComboBox.addItem(MessageBundle.getMessage("angal.lab.selectapatient"));

			if (!insert && lab.getPatient() != null) {
				try{
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

			patientComboBox.addActionListener(arg0 -> {
				if (patientComboBox.getSelectedIndex()>0) {
					labPat=(Patient)patientComboBox.getSelectedItem();
					setPatient(labPat);
					Admission admission = null;
					try {
						admission = admMan.getCurrentAdmission(labPat);
					}catch(OHServiceException e){
						OHServiceExceptionUtil.showMessages(e);
					}
					inPatientCheckBox.setSelected(admission != null ? true : false);
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

		if (patientComboBox.getItemCount()> 0) {
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
			buttonPanel.setBounds(0, dataPanelHeight+dataPatientHeight+resultPanelHeight, panelWidth, buttonPanelHeight);
			buttonPanel.add(getOkButton(), null);
			buttonPanel.add(getCancelButton(), null);
			buttonPanel.add(getPrintButton(),null);
		}
		return buttonPanel;
	}

	private ExamComboBox getExamComboBox() {
		if (examComboBox == null) {

			ExamBrowsingManager manager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
			ArrayList<Exam> exams;
			try {
				exams = manager.getExams();
			} catch (OHServiceException e) {
				exams = null;
				OHServiceExceptionUtil.showMessages(e);
			}
			examComboBox.addItem(MessageBundle.getMessage("angal.lab.selectanexam"));
			examComboBox = ExamComboBox.withExamsAndExamFromLaboratorySelected(exams, lab, insert);

			examComboBox.addActionListener(arg0 -> {
				if (!(examComboBox.getSelectedItem() instanceof String)) {
					examSelected = (Exam) examComboBox
							.getSelectedItem();

					if (examSelected.getProcedure() == 1)
						resultPanel = getFirstPanel();
					else if (examSelected.getProcedure() == 2)
						resultPanel = getSecondPanel();
					else if (examSelected.getProcedure() == 3)
						resultPanel = getThirdPanel();

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
					ArrayList<String> materialList = labMan.getMaterialList();
					return MatComboBox.withMaterialsAndMaterialFromLabSelected(materialList, lab, insert, labManager::getMaterialTranslated);
		});
	}

	
	private JTextArea getNoteTextArea() {
		if (noteTextArea == null) {
			noteTextArea = new JTextArea(10,30);
			if (!insert){
				noteTextArea.setText(lab.getNote());
			}
			noteTextArea.setLineWrap(true);
			noteTextArea.setPreferredSize(new Dimension(10,30));
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
			printButton = new JButton(MessageBundle.getMessage("angal.lab.print"));
			printButton.setMnemonic(KeyEvent.VK_P);
			printButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {					
					try {
						ArrayList<LaboratoryForPrint> labs = new ArrayList<>();
						
						labs.add(new LaboratoryForPrint(
								lab.getCode(),
								lab.getExam(),
								lab.getDate(),
								lab.getResult()
							)
						);
						if (!labs.isEmpty()) {
							
							printManager.print("Laboratory",labs,0);
						}
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
					
				}

			});
		}
		return printButton;
	}
	
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(MessageBundle.getMessage("angal.common.cancel"));
			cancelButton.setMnemonic(KeyEvent.VK_C);
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		return cancelButton;
	}

	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText(MessageBundle.getMessage("angal.common.ok"));
			okButton.setMnemonic(KeyEvent.VK_O);
			okButton.addActionListener(e -> {
				if (examComboBox.getSelectedIndex() == 0) {
					JOptionPane.showMessageDialog(LabEditExtended.this,
							MessageBundle.getMessage("angal.lab.pleaseselectanexam"));
					return;
				}
				String matSelected=(String)matComboBox.getSelectedItem();
				examSelected=(Exam)examComboBox.getSelectedItem();
				try {
					labPat=(Patient)patientComboBox.getSelectedItem();
				} catch (ClassCastException e2) {
					JOptionPane.showMessageDialog(LabEditExtended.this,
							MessageBundle.getMessage("angal.lab.pleaseselectapatient"));
					return;
				}
				LocalDateTime examDate = LocalDateTime.now();
				try {
					examDate = examDateFieldCal.getLocalDateTime();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(LabEditExtended.this,
							MessageBundle.getMessage("angal.lab.pleaseinsertavalidexamdate"));
					return;
				}
				if (examSelected.getProcedure() == 3 && examTextField.getText().isEmpty()) {

					JOptionPane.showMessageDialog(LabEditExtended.this, MessageBundle.getMessage("angal.labnew.pleaseinsertavalidvalue"));
						return;
				}
				ArrayList<String> labRow = new ArrayList<String>();
				lab.setDate(LocalDateTime.now());
				lab.setExamDate(examDate.toLocalDate());
				RememberDates.setLastLabExamDate(Converters.toCalendar(examDate));
				lab.setMaterial(labManager.getMaterialKey(matSelected));
				lab.setExam(examSelected);
				lab.setNote(noteTextArea.getText());
				lab.setInOutPatient((inPatientCheckBox.isSelected()?"I":"O"));
				lab.setPatient(labPat);
				lab.setPatName(labPat.getName());
				lab.setSex(labPat.getSex()+"");

				if (examSelected.getProcedure() == 1)
					lab.setResult(examRowComboBox.getSelectedItem()
							.toString());
				else if (examSelected.getProcedure() == 2) {
					lab.setResult(MessageBundle.getMessage("angal.lab.multipleresults"));
					for (int i = 0; i < resultPanel.getComponentCount(); i++) {
						if (((ExamRowSubPanel) resultPanel.getComponent(i))
								.getSelectedResult().equalsIgnoreCase("P")) {
							labRow.add(eRows.get(i).getDescription());
						}
					}
				}
				else if (examSelected.getProcedure() == 3) {
					lab.setResult(examTextField.getText());
				}
				boolean result = false;
				if (insert) {
					lab.setAge(labPat.getAge());
					try {
						result = labManager.newLaboratory(lab,	labRow);
					} catch (OHServiceException e1) {
						result = false;
						OHServiceExceptionUtil.showMessages(e1);
						return;
					}
				}
				else {
					try {
						result = labManager.updateLaboratory(lab, labRow);
					} catch (OHServiceException e1) {
						result = false;
						OHServiceExceptionUtil.showMessages(e1);
						return;
					}
				}
				if (!result)
					JOptionPane.showMessageDialog(null,
							MessageBundle.getMessage("angal.sql.thedatacouldnotbesaved"));
				else {
					fireLabUpdated();
					dispose();
				}
			});
		}
		return okButton;
	}

	private JPanel getFirstPanel() {
		resultPanel.removeAll();
		String result="";
		examRowComboBox = new ExamRowComboBox();
		examRowComboBox.setMaximumSize(new Dimension(200, 25));
		examRowComboBox.setMinimumSize(new Dimension(200, 25));
		examRowComboBox.setPreferredSize(new Dimension(200, 25));
		if (insert) {
			result=examSelected.getDefaultResult();
		} else {
			result=lab.getResult();
		}
		examRowComboBox.addItem(result);

		ArrayList<ExamRow> rows;
		try {
			rows = rowManager.getExamRowByExamCode(examSelected.getCode());
		} catch (OHServiceException e) {
			rows = null;
			OHServiceExceptionUtil.showMessages(e);
		}

		examRowComboBox.populateWithExamRowsWithDescriptionNotEqualToLabResult(rows, result);
		if (examRowComboBox.getItemCount() > 0) resultPanel.add(examRowComboBox);

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
			ArrayList<LaboratoryRow> lRows;
			try {
				lRows = lRowManager.getLabRowByLabId(lab.getCode());
			} catch (OHServiceException e) {
				lRows = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			ArrayList<LaboratoryRow> finalLRows = lRows;
			Optional.ofNullable(eRows).ifPresent(examRows ->
				examRows.forEach(r -> resultPanel.add(ExamRowSubPanel.forExamRowAndLaboratoryRows(r, finalLRows))));
		}
		return resultPanel;
	}
	
	private JPanel getThirdPanel() {
		resultPanel.removeAll();
		String result="";
		examTextField = new JTextField();
		examTextField.setMaximumSize(new Dimension(200, 25));
		examTextField.setMinimumSize(new Dimension(200, 25));
		examTextField.setPreferredSize(new Dimension(200, 25));
		if (insert || examChanged) {
			result=examSelected.getDefaultResult();
			examChanged = false;
		} else {
			result=lab.getResult();
		}
		examTextField.setText(result);

		resultPanel.add(examTextField);

		return resultPanel;
	}

}
