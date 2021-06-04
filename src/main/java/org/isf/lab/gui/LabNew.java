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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.exa.manager.ExamBrowsingManager;
import org.isf.exa.manager.ExamRowBrowsingManager;
import org.isf.exa.model.Exam;
import org.isf.exa.model.ExamRow;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.manager.LabManager;
import org.isf.lab.model.Laboratory;
import org.isf.lab.model.LaboratoryRow;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.priceslist.model.Price;
import org.isf.serviceprinting.manager.PrintLabels;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.CustomJDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.OhTableModelExam;
import org.isf.utils.time.RememberDates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabNew extends JDialog implements SelectionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(LabNew.class);

//LISTENER INTERFACE --------------------------------------------------------
	private EventListenerList labListener = new EventListenerList();
	
	public interface LabListener extends EventListener {
		void labInserted();
	}
	
	public void addLabListener(LabListener l) {
		labListener.add(LabListener.class, l);
		
	}
	
	private void fireLabInserted() {
		new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;};
		
		EventListener[] listeners = labListener.getListeners(LabListener.class);
		for (int i = 0; i < listeners.length; i++)
			((LabListener)listeners[i]).labInserted();
	}
//---------------------------------------------------------------------------
	
    @Override
	public void patientSelected(Patient patient) {
		patientSelected = patient;
		//INTERFACE
		jTextFieldPatient.setText(patientSelected.getName());
		jTextFieldPatient.setEditable(false);
		jButtonPickPatient.setText(MessageBundle.getMessage("angal.labnew.changepatient")); //$NON-NLS-1$
		jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.changethepatientassociatedwiththisexams")); //$NON-NLS-1$
		jButtonTrashPatient.setEnabled(true);
		inOut = getIsAdmitted();
		if (inOut.equalsIgnoreCase("O")) jRadioButtonOPD.setSelected(true);
		else jRadioButtonIPD.setSelected(true);
	}
	
	private static final long serialVersionUID = 1L;
	private JTable jTableExams;
	private JScrollPane jScrollPaneTable;
	private JPanel jPanelNorth;
	private JButton jButtonRemoveItem;
	private JButton jButtonAddExam;
	private JPanel jPanelExamButtons;
	private JPanel jPanelEast;
	private JPanel jPanelSouth;
	private JPanel jPanelDate;
	private JPanel jPanelPatient;
	private JLabel jLabelPatient;
	private JTextField jTextFieldPatient;
	private JButton jButtonPickPatient;
	private JButton jButtonTrashPatient;
	private JLabel jLabelDate;
	private CustomJDateChooser jCalendarDate;
	private JPanel jPanelMaterial;
	private JComboBox<String> jComboBoxMaterial;
	private JComboBox<String> jComboBoxExamResults;
	private JPanel jPanelResults;
	private JPanel jPanelNote;
	private JPanel jPanelButtons;
	private JButton jButtonOK;
	private JButton jButtonCancel;
	private JTextArea jTextAreaNote;
	private JScrollPane jScrollPaneNote;
	private JRadioButton jRadioButtonOPD;
	private JRadioButton jRadioButtonIPD;
	private ButtonGroup radioGroup;
	private JPanel jOpdIpdPanel;
	private String inOut;
	
	private static final Dimension PatientDimension = new Dimension(200,20);
	private static final Dimension LabelDimension = new Dimension(50,20);
	//private static final Dimension ResultDimensions = new Dimension(200,200);
	//private static final Dimension MaterialDimensions = new Dimension(150,20);
	//private static final Dimension TextAreaNoteDimension = new Dimension(500, 50);
	private static final int EastWidth = 200;
	private static final int ComponentHeight = 20;
	private static final int ResultHeight = 200;
	
	private Object[] examClasses = {Exam.class, String.class};
	private String[] examColumnNames = {
            MessageBundle.getMessage("angal.common.exam.txt").toUpperCase(),
            MessageBundle.getMessage("angal.common.result.txt").toUpperCase()
        };
	private int[] examColumnWidth = {200, 150};
	private boolean[] examResizable = {true, false};
	
	//TODO private boolean modified;
	private Patient patientSelected = null;
	private Laboratory selectedLab = null;
	private JTextField txtResultValue;
	
	//Admission
	private AdmissionBrowserManager admissionManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
	
	//Materials
	private LabManager labManager = Context.getApplicationContext().getBean(LabManager.class);
	private ArrayList<String> matList = labManager.getMaterialList();
	
	//Exams (ALL)
	private ExamBrowsingManager exaManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
	private ArrayList<Exam> exaArray;
	
	//Results (ALL)
	private ExamRowBrowsingManager examRowManager = Context.getApplicationContext().getBean(ExamRowBrowsingManager.class);

	//Arrays for this Patient
	private ArrayList<ArrayList<LaboratoryRow>> examResults = new ArrayList<>();
	private ArrayList<Laboratory> examItems = new ArrayList<>();
	private ExamTableModel jTableModel;
	private JButton printLabelButton;
	private JTextField jTextFieldExamResult;
                
	public LabNew(JFrame owner) {
		super(owner, true);
		
		try {
			exaArray = exaManager.getExams();
		} catch (OHServiceException e) {
			exaArray = null;
			OHServiceExceptionUtil.showMessages(e);
		}
		
		initComponents();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(LabNew.DISPOSE_ON_CLOSE);
		setTitle(MessageBundle.getMessage("angal.labnew.title"));
		//setVisible(true);
	}

                
	public LabNew(JFrame owner, Patient patient) {
        super(owner, true);
        patientSelected = patient;
        
		try {
			exaArray = exaManager.getExams();
		} catch (OHServiceException e) {
			exaArray = null;
			OHServiceExceptionUtil.showMessages(e);
		}
		
		initComponents();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(LabNew.DISPOSE_ON_CLOSE);
		setTitle(MessageBundle.getMessage("angal.labnew.title"));
		//setVisible(true);
	}

	private void initComponents() {
		add(getJPanelNorth(), BorderLayout.NORTH);
		add(getJScrollPaneTable(), BorderLayout.CENTER);
		add(getJPanelEast(), BorderLayout.EAST);
		add(getJPanelSouth(), BorderLayout.SOUTH);
		pack();
	}

	private JScrollPane getJScrollPaneNote() {
		if (jScrollPaneNote == null) {
			jScrollPaneNote = new JScrollPane();
			jScrollPaneNote.setViewportView(getJTextAreaNote());
		}
		return jScrollPaneNote;
	}

	private JTextArea getJTextAreaNote() {
		if (jTextAreaNote == null) {
			jTextAreaNote = new JTextArea(3,50);
			jTextAreaNote.setText("");
			jTextAreaNote.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {
					selectedLab.setNote(jTextAreaNote.getText().trim());
					examItems.get(jTableExams.getSelectedRow()).setNote(jTextAreaNote.getText().trim());
				}

				@Override
				public void keyPressed(KeyEvent e) {}

				@Override
				public void keyReleased(KeyEvent e) {}
			});
			//jTextAreaNote.setPreferredSize(TextAreaNoteDimension);
		}
		return jTextAreaNote;
	}

	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
				
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		return jButtonCancel;
	}

	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			jButtonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			jButtonOK.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					
					GregorianCalendar newDate = new GregorianCalendar();
					try {
						newDate.setTime(jCalendarDate.getDate());
					} catch (Exception e1) {
						MessageDialog.error(LabNew.this, "angal.lab.pleaseinsertavalidexamdate.msg");
						return;
					}
					RememberDates.setLastLabExamDate(newDate);
					String inOut = jRadioButtonOPD.isSelected() ? "O" : "I";
                                        
                    for (Laboratory lab : examItems) {
                        lab.setDate(newDate);
                        lab.setExamDate(newDate);
                        lab.setInOutPatient(inOut);
                        lab.setPatient(patientSelected);
                        if (lab.getExam().getProcedure() == 3 && lab.getResult().isEmpty()) {
	                        MessageDialog.error(LabNew.this, "angal.labnew.pleaseinsertavalidvalue");
    						//select the first exam with the missing value
    						jTableExams.setRowSelectionInterval(examItems.indexOf(lab), examItems.indexOf(lab));
    						return;
    					}
                    }
                    	
					try {
						labManager.newLaboratory2(examItems, examResults);
						fireLabInserted();
						dispose();
					} catch (OHServiceException e1) {
						OHServiceExceptionUtil.showMessages(e1);
					}
				}
			});
		}
		return jButtonOK;
	}
	
	private String getIsAdmitted() {
		Admission adm = new Admission();
		try {
			adm = admissionManager.getCurrentAdmission(patientSelected);
		}catch(OHServiceException e){
			OHServiceExceptionUtil.showMessages(e);
		}
		return (adm==null?"O":"I");					
	}

	private JPanel getJPanelButtons() {
            if (jPanelButtons == null) {
                jPanelButtons = new JPanel();
                jPanelButtons.add(getJButtonOK());
                jPanelButtons.add(getPrintLabelButton());
                jPanelButtons.add(getJButtonCancel());
            }
            return jPanelButtons;
	}
	private JButton getPrintLabelButton(){
		if (printLabelButton==null){
			printLabelButton = new JButton(MessageBundle.getMessage("angal.labnew.printlabel.btn"));
			printLabelButton.setMnemonic(MessageBundle.getMnemonic("angal.labnew.printlabel.btn.key"));
			printLabelButton.addActionListener(new ActionListener() {
			
				public void actionPerformed(ActionEvent arg0) {
					
					if (patientSelected == null) {
						MessageDialog.error(null, "angal.common.pleaseselectapatient.msg");
						return;
					} 
					
					try {
						new PrintLabels("LabelForSamples",patientSelected.getCode());
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
					
				}
			});
		}
		return printLabelButton;
	}
	private JPanel getJPanelNote() {
            if (jPanelNote == null) {
                jPanelNote = new JPanel();
                jPanelNote.setLayout(new BoxLayout(jPanelNote, BoxLayout.Y_AXIS));
                jPanelNote.setBorder(BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(Color.LIGHT_GRAY), MessageBundle.getMessage("angal.labnew.note")));
                jPanelNote.add(getJScrollPaneNote());
            }
            return jPanelNote;
	}

	private JPanel getJPanelResults() {
		if (jPanelResults == null) {
			jPanelResults = new JPanel();
			jPanelResults.setPreferredSize(new Dimension(EastWidth, ResultHeight));
			jPanelResults.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.LIGHT_GRAY), MessageBundle.getMessage("angal.labnew.result")));
		} else {
			jPanelResults.removeAll();
			int selectedRow = jTableExams.getSelectedRow();
			Exam selectedExam = selectedLab.getExam();
                       
			if (selectedExam.getProcedure() == 1) {
				txtResultValue = new JTextField();
				jComboBoxExamResults = new JComboBox<>();
				jComboBoxExamResults.setMaximumSize(new Dimension(EastWidth, ComponentHeight));
				jComboBoxExamResults.setMinimumSize(new Dimension(EastWidth, ComponentHeight));
				jComboBoxExamResults.setPreferredSize(new Dimension(EastWidth, ComponentHeight));
				txtResultValue.setMaximumSize(new Dimension(EastWidth, ComponentHeight));
				txtResultValue.setMinimumSize(new Dimension(EastWidth, ComponentHeight));
				txtResultValue.setPreferredSize(new Dimension(EastWidth, ComponentHeight));
				ArrayList<ExamRow> exaRowArray;
				try {
					exaRowArray = examRowManager.getExamRowByExamCode(selectedExam.getCode());
				} catch (OHServiceException ex) {
					exaRowArray = null;
					LOGGER.error(ex.getMessage(), ex);
				}
				if (exaRowArray != null) {
					for (ExamRow exaRow : exaRowArray) {
						if (selectedExam.getCode().compareTo(exaRow.getExamCode().getCode()) == 0) {
							jComboBoxExamResults.addItem(exaRow.getDescription());
						}
					}
				}
				jComboBoxExamResults.setSelectedItem(selectedLab.getResult());
				jComboBoxExamResults.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectedLab.setResult(jComboBoxExamResults.getSelectedItem().toString());
						examItems.set(selectedRow, selectedLab);
						jTableExams.updateUI();
					}
				});
				if (jComboBoxExamResults.getItemCount() > 0)
					jPanelResults.add(jComboBoxExamResults);
				else
					jPanelResults.add(new JLabel(selectedExam.getDefaultResult()));

			}  else if (selectedExam.getProcedure() == 2) {
				
				
				
				jPanelResults.removeAll();
                jPanelResults.setLayout(new BoxLayout(jPanelResults, BoxLayout.Y_AXIS));

                ArrayList<LaboratoryRow> checking = examResults.get(jTableExams.getSelectedRow());
                boolean checked;
                JPanel resultsContainer = new JPanel();
                resultsContainer.setLayout(new GridLayout(0,1));
                JScrollPane resultsContainerScroll = new JScrollPane(resultsContainer);
                resultsContainerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                resultsContainerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                resultsContainerScroll.setBounds(0, 0, EastWidth, ResultHeight);
                jPanelResults.add(resultsContainerScroll);
                ArrayList<ExamRow> exaRowArray;
                try {
					exaRowArray = examRowManager.getExamRowByExamCode(selectedExam.getCode());
				} catch (OHServiceException ex) {
					exaRowArray = null;
	                LOGGER.error(ex.getMessage(), ex);
				}
                if (exaRowArray != null) {
	                for (ExamRow exaRow : exaRowArray) {
						if (selectedExam.getCode().compareTo(exaRow.getExamCode().getCode()) == 0) {
							
							checked = false;
							LaboratoryRow labRow = new LaboratoryRow();
							labRow.setDescription(exaRow.getDescription());
							if (checking.contains(labRow))
								checked = true;
							
	                        resultsContainer.add(new CheckBox(exaRow, checked));
						}
					}
                }
			} else if (selectedExam.getProcedure() == 3) {
				jTextFieldExamResult = new JTextField();
				jTextFieldExamResult.setMaximumSize(new Dimension(EastWidth, ComponentHeight));
				jTextFieldExamResult.setMinimumSize(new Dimension(EastWidth, ComponentHeight));
				jTextFieldExamResult.setPreferredSize(new Dimension(EastWidth, ComponentHeight));
				
				jTextFieldExamResult.setText(selectedLab.getResult());
				
				jTextFieldExamResult.getDocument().addDocumentListener(new DocumentListener() {
					
					public void removeUpdate(DocumentEvent e) {
						selectedLab.setResult(jTextFieldExamResult.getText());
						jTableExams.updateUI();
					}
					
					public void insertUpdate(DocumentEvent e) {
						selectedLab.setResult(jTextFieldExamResult.getText());
						jTableExams.updateUI();
					}
					
					public void changedUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
					}
				});

				jPanelResults.add(jTextFieldExamResult);
			}
		}
		return jPanelResults;
	}
	
	public class CheckBox extends JCheckBox {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JCheckBox check = this;
		
		public CheckBox(ExamRow exaRow, boolean checked) {
			this.setText(exaRow.getDescription());
			this.setSelected(checked);
			this.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (check.isSelected()) {
						LaboratoryRow laboratoryRow = new LaboratoryRow();
						laboratoryRow.setDescription(e.getActionCommand());
						examResults.get(jTableExams.getSelectedRow()).add(laboratoryRow);
					} else {
						LaboratoryRow laboratoryRow = new LaboratoryRow();
						laboratoryRow.setDescription(e.getActionCommand());
						examResults.get(jTableExams.getSelectedRow()).remove(laboratoryRow); 
					}
				}
			});
		}
	}

	private JComboBox<String> getJComboBoxMaterial() {
		if (jComboBoxMaterial == null) {
			jComboBoxMaterial = new JComboBox<>();
			for (String elem : matList) {
				jComboBoxMaterial.addItem(elem);
			}
			jComboBoxMaterial.addActionListener(e -> {
				selectedLab.setMaterial(labManager.getMaterialKey((String) jComboBoxMaterial.getSelectedItem()));
				examItems.get(jTableExams.getSelectedRow()).setMaterial(selectedLab.getMaterial());
//					jTableExams.updateUI();
			});
			jComboBoxMaterial.setPreferredSize(new Dimension(EastWidth, ComponentHeight));
			jComboBoxMaterial.setMaximumSize(new Dimension(EastWidth, ComponentHeight));
			jComboBoxMaterial.setEnabled(false);
		}
		return jComboBoxMaterial;
	}

	private JPanel getJPanelMaterial() {
		if (jPanelMaterial == null) {
			jPanelMaterial = new JPanel();
			jPanelMaterial.setLayout(new BoxLayout(jPanelMaterial, BoxLayout.Y_AXIS));
			jPanelMaterial.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.LIGHT_GRAY), MessageBundle.getMessage("angal.labnew.material")));
			jPanelMaterial.add(getJComboBoxMaterial());
		}
		return jPanelMaterial;
	}

	private JLabel getJLabelDate() {
		if (jLabelDate == null) {
			jLabelDate = new JLabel();
			jLabelDate.setText("Date");
			jLabelDate.setPreferredSize(LabelDimension);
		}
		return jLabelDate;
	}
	
	private JPanel getJOpdIpdPanel() {
		if (jOpdIpdPanel == null) {
			jOpdIpdPanel = new JPanel();
			
			jRadioButtonOPD = new JRadioButton("OPD");
			jRadioButtonIPD = new JRadioButton("IP");
			
			radioGroup = new ButtonGroup();
			radioGroup.add(jRadioButtonOPD);
			radioGroup.add(jRadioButtonIPD);
			
			jOpdIpdPanel.add(jRadioButtonOPD);
			jOpdIpdPanel.add(jRadioButtonIPD);
			
			jRadioButtonOPD.setSelected(true);
		}
		return jOpdIpdPanel;
	}

	private JButton getJButtonTrashPatient() {
		if (jButtonTrashPatient == null) {
			jButtonTrashPatient = new JButton();
			jButtonTrashPatient.setMnemonic(KeyEvent.VK_R);
			jButtonTrashPatient.setPreferredSize(new Dimension(25,25));
			jButtonTrashPatient.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png")); //$NON-NLS-1$
			jButtonTrashPatient.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.removepatientassociationwiththisexam")); //$NON-NLS-1$
			jButtonTrashPatient.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					
					patientSelected = null;
					//INTERFACE
					jTextFieldPatient.setText(""); //$NON-NLS-1$
					jTextFieldPatient.setEditable(false);
					jButtonPickPatient.setText(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
					jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.associateapatientwiththisexam")); //$NON-NLS-1$
					jButtonTrashPatient.setEnabled(false);
				}
			});
		}
		return jButtonTrashPatient;
	}

	private JButton getJButtonPickPatient() {
		if (jButtonPickPatient == null) {
			jButtonPickPatient = new JButton(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
			jButtonPickPatient.setMnemonic(MessageBundle.getMnemonic("angal.labnew.findpatient.btn.key"));
			jButtonPickPatient.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png")); //$NON-NLS-1$
			jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.associateapatientwiththisexam"));  //$NON-NLS-1$
			jButtonPickPatient.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					SelectPatient sp = new SelectPatient(LabNew.this, patientSelected);
					sp.addSelectionListener(LabNew.this);
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
			jTextFieldPatient.setEditable(false);
		}
		return jTextFieldPatient;
	}

	private JLabel getJLabelPatient() {
		if (jLabelPatient == null) {
			jLabelPatient = new JLabel();
			jLabelPatient.setText("Patient");
			jLabelPatient.setPreferredSize(LabelDimension);
		}
		return jLabelPatient;
	}

	private JPanel getJPanelPatient() {
		if (jPanelPatient == null) {
			jPanelPatient = new JPanel(new FlowLayout(FlowLayout.LEFT));
			jPanelPatient.add(getJLabelPatient());
			jPanelPatient.add(getJTextFieldPatient());
			jPanelPatient.add(getJButtonPickPatient());
			jPanelPatient.add(getJButtonTrashPatient());
			jPanelPatient.add(getJOpdIpdPanel());
		}
		return jPanelPatient;
	}

	private JPanel getJPanelDate() {
		if (jPanelDate == null) {
			jPanelDate = new JPanel(new FlowLayout(FlowLayout.LEFT));
			jPanelDate.add(getJLabelDate());
			jPanelDate.add(getJCalendarDate());
		}
		return jPanelDate;
	}

	private CustomJDateChooser getJCalendarDate() {
		if (jCalendarDate == null) {
			jCalendarDate = new CustomJDateChooser(RememberDates.getLastLabExamDateGregorian().getTime()); //To remind last used
			jCalendarDate.setLocale(new Locale(GeneralData.LANGUAGE));
			jCalendarDate.setDateFormatString("dd/MM/yy (HH:mm:ss)"); //$NON-NLS-1$
		}
		return jCalendarDate;
	}
	
	private JPanel getJPanelSouth() {
		if (jPanelSouth == null) {
			jPanelSouth = new JPanel();
			jPanelSouth.setLayout(new BoxLayout(jPanelSouth, BoxLayout.Y_AXIS));
			jPanelSouth.add(getJPanelNote());
			jPanelSouth.add(getJPanelButtons());
		}
		return jPanelSouth;
	}

	private JPanel getJPanelEast() {
		if (jPanelEast == null) {
			jPanelEast = new JPanel();
			jPanelEast.setLayout(new BoxLayout(jPanelEast, BoxLayout.Y_AXIS));
			jPanelEast.add(getJPanelExamButtons());
			jPanelEast.add(getJPanelMaterial());
			jPanelEast.add(getJPanelResults());
		}
		return jPanelEast;
	}

	private JPanel getJPanelNorth() {
		if (jPanelNorth == null) {
			jPanelNorth = new JPanel();
			jPanelNorth.setLayout(new BoxLayout(jPanelNorth, BoxLayout.Y_AXIS));
			jPanelNorth.add(getJPanelDate());
			jPanelNorth.add(getJPanelPatient());
		}
		return jPanelNorth;
	}

	private JScrollPane getJScrollPaneTable() {
		if (jScrollPaneTable == null) {
			jScrollPaneTable = new JScrollPane();
			jScrollPaneTable.setViewportView(getJTableExams());
		}
		return jScrollPaneTable;
	}

	private JTable getJTableExams() {
		if (jTableExams == null) {
			jTableExams = new JTable();
			jTableModel = new ExamTableModel();
			jTableExams.setModel(jTableModel);
			for (int i = 0; i < examColumnWidth.length; i++) {
				
				jTableExams.getColumnModel().getColumn(i).setMinWidth(examColumnWidth[i]);
				if (!examResizable[i]) jTableExams.getColumnModel().getColumn(i).setMaxWidth(examColumnWidth[i]);
			}
			
			jTableExams.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ListSelectionModel listSelectionModel = jTableExams.getSelectionModel();
			listSelectionModel.addListSelectionListener(e -> {
			// Check that mouse has been released.
			if (!e.getValueIsAdjusting()) {
				int selectedRow = jTableExams.getSelectedRow();
				
				if (selectedRow > -1) {
					selectedLab = (Laboratory)jTableExams.getValueAt(selectedRow, -1);
					jComboBoxMaterial.setSelectedItem(labManager.getMaterialTranslated(selectedLab.getMaterial()));
					jTextAreaNote.setText(selectedLab.getNote());
					jPanelResults = getJPanelResults();
					jComboBoxMaterial.setEnabled(true);
					
					validate();
					repaint();
					}
				}
			});
		}
		return jTableExams;
	}
	
	public JPanel getJPanelExamButtons() {
		if (jPanelExamButtons == null) {
			jPanelExamButtons = new JPanel();
			jPanelExamButtons.setLayout(new BoxLayout(jPanelExamButtons, BoxLayout.X_AXIS));
			jPanelExamButtons.add(getJButtonAddExam());
			jPanelExamButtons.add(getJButtonRemoveItem());
		}
		return jPanelExamButtons;
	}
	
	private JButton getJButtonAddExam() {

		if (jButtonAddExam == null) {
			jButtonAddExam = new JButton(MessageBundle.getMessage("angal.labnew.exam.btn"));
			jButtonAddExam.setMnemonic(MessageBundle.getMnemonic("angal.labnew.exam.btn.key"));
			jButtonAddExam.setIcon(new ImageIcon("rsc/icons/plus_button.png")); //$NON-NLS-1$
			jButtonAddExam.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String mat = "";

					OhTableModelExam<Price> modelOh = new OhTableModelExam<>(exaArray);

					ExamPicker examPicker = new ExamPicker(modelOh);

					examPicker.setSize(300, 400);

					JDialog dialog = new JDialog();
					dialog.setLocationRelativeTo(null);
					dialog.setSize(600, 350);
					dialog.setLocationRelativeTo(null);
					dialog.setModal(true);

					examPicker.setParentFrame(dialog);
					dialog.setContentPane(examPicker);
					dialog.setVisible(true);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					ArrayList<Exam> exams = examPicker.getAllSelectedObject();

					Exam exa = null;
					Laboratory lab = null;
					boolean alreadyIn = false;

					if (exams.size() < 1) {
						return;
					}
					
					for (int i = 0; i < exams.size(); i++) {
						alreadyIn = false;
						lab = new Laboratory();
						exa = exams.get(i);

						for (Laboratory labItem : examItems) {
							if (labItem.getExam() == exa) {
								MessageDialog.error(LabNew.this, "angal.labnew.thisexamisalreadypresent");
								alreadyIn = true;
							}
						}
						if (alreadyIn) {
							continue;
						}

						if (exa.getProcedure() == 1 || exa.getProcedure() == 3) {
							lab.setResult(exa.getDefaultResult());
						} else { // exa.getProcedure() == 2
							lab.setResult(MessageBundle.getMessage("angal.labnew.multipleresults"));
						}
						lab.setExam(exa);
						lab.setMaterial(labManager.getMaterialKey(mat));
						addItem(lab);
					}
				}
			});
		}
		return jButtonAddExam;
	}

	private void addItem(Laboratory lab) {
		examItems.add(lab);
		examResults.add(new ArrayList<>());
		jTableExams.updateUI();
		int index = examItems.size()-1;
		jTableExams.setRowSelectionInterval(index, index);
		
	}

	private JButton getJButtonRemoveItem() {
		if (jButtonRemoveItem == null) {
			jButtonRemoveItem = new JButton(MessageBundle.getMessage("angal.labnew.remove.btn"));
			jButtonRemoveItem.setMnemonic(MessageBundle.getMnemonic("angal.labnew.remove.btn.key"));
			jButtonRemoveItem.setIcon(new ImageIcon("rsc/icons/delete_button.png")); //$NON-NLS-1$
			jButtonRemoveItem.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					
					int selectedRow = jTableExams.getSelectedRow();
					if (selectedRow < 0) {
						MessageDialog.error(LabNew.this,"angal.labnew.pleaseselectanexam");
					} else {
						examItems.remove(selectedRow);
						jPanelResults.removeAll();
						jTableExams.clearSelection();
						jTableModel.fireTableDataChanged();
						jTableExams.updateUI();
						jComboBoxMaterial.setEnabled(false);
						//validate();
						repaint();
					}
				}
			});
		}
		return jButtonRemoveItem;
	}
	
	public class ExamTableModel extends DefaultTableModel {
		
		public Class<?> getColumnClass(int columnIndex) {
			return examClasses[columnIndex].getClass();
		}

		public int getColumnCount() {
			return examColumnNames.length;
		}

		public String getColumnName(int columnIndex) {
			return examColumnNames[columnIndex];
		}

		public int getRowCount() {
			if (examItems == null)
				return 0;
			return examItems.size();
		}

		public Object getValueAt(int r, int c) {
			Laboratory laboratory = examItems.get(r);
			if (c == -1) {
			    return laboratory;
			}
			if (c == 0) {
                return laboratory.getExam().getDescription();
			}
			if (c == 1) {
                return laboratory.getResult();
			}
			return null;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		public void addTableModelListener(TableModelListener l) {
		}

		public void removeTableModelListener(TableModelListener l) {
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
		}

	}
}
