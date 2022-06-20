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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

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
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
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
import org.isf.generaldata.MessageBundle;
import org.isf.lab.manager.LabManager;
import org.isf.lab.model.Laboratory;
import org.isf.lab.model.LaboratoryRow;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.priceslist.model.Price;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.OhTableModelExam;
import org.isf.utils.time.RememberDates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabNew extends ModalJFrame implements SelectionListener {

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

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = labListener.getListeners(LabListener.class);
		for (EventListener listener : listeners) {
			((LabListener) listener).labInserted();
		}
	}

	//---------------------------------------------------------------------------
	
    @Override
	public void patientSelected(Patient patient) {
		patientSelected = patient;
		//INTERFACE
		jTextFieldPatient.setText(patientSelected.getName());
		jTextFieldPatient.setEditable(false);
		jButtonPickPatient.setText(MessageBundle.getMessage("angal.labnew.changepatient"));
		jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.changethepatientassociatedwiththisexams")); //$NON-NLS-1$
		jButtonTrashPatient.setEnabled(true);
		String inOut = getIsAdmitted();
		if (inOut.equalsIgnoreCase("O")) {
			jRadioButtonOPD.setSelected(true);
		} else {
			jRadioButtonIPD.setSelected(true);
		}
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
	private GoodDateTimeChooser jCalendarDate;
	private JPanel jPanelMaterial;
	private JComboBox<String> jComboBoxMaterial;
	private JPanel jPanelResults;
	private JPanel jPanelNote;
	private JPanel jPanelButtons;
	private JButton jButtonOK;
	private JButton jButtonCancel;
	private JTextArea jTextAreaNote;
	private JScrollPane jScrollPaneNote;
	private JRadioButton jRadioButtonOPD;
	private JRadioButton jRadioButtonIPD;
	private JPanel jOpdIpdPanel;

	private static final Dimension PATIENT_DIMENSION = new Dimension(200, 20);
	private static final Dimension LABEL_DIMENSION = new Dimension(75, 20);
	private static final int EAST_WIDTH = 200;
	private static final int COMPONENT_HEIGHT = 20;
	private static final int RESULT_HEIGHT = 200;

	private Object[] examClasses = { Exam.class, String.class };
	private String[] examColumnNames = {
			MessageBundle.getMessage("angal.common.exam.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.result.txt").toUpperCase()
	};
	private int[] examColumnWidth = { 200, 150 };
	private boolean[] examResizable = { true, false };
	
	private Patient patientSelected = null;
	private Laboratory selectedLab = null;

	//Admission
	private AdmissionBrowserManager admissionManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
	
	//Materials
	private LabManager labManager = Context.getApplicationContext().getBean(LabManager.class);
	private List<String> matList = labManager.getMaterialList();
	
	//Exams (ALL)
	private ExamBrowsingManager exaManager = Context.getApplicationContext().getBean(ExamBrowsingManager.class);
	private List<Exam> exaArray;
	
	//Results (ALL)
	private ExamRowBrowsingManager examRowManager = Context.getApplicationContext().getBean(ExamRowBrowsingManager.class);

	//Arrays for this Patient
	private List<List<LaboratoryRow>> examResults = new ArrayList<>();
	private List<Laboratory> examItems = new ArrayList<>();
	private ExamTableModel jTableModel;
	private JTextField jTextFieldExamResult;
                
	public LabNew(JFrame owner) {
		try {
			exaArray = exaManager.getExams();
		} catch (OHServiceException e) {
			exaArray = null;
			OHServiceExceptionUtil.showMessages(e);
		}
		
		initComponents();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(MessageBundle.getMessage("angal.labnew.title"));
		showAsModal(owner);
	}

	public LabNew(JFrame owner, Patient patient) {
        patientSelected = patient;
        
		try {
			exaArray = exaManager.getExams();
		} catch (OHServiceException e) {
			exaArray = null;
			OHServiceExceptionUtil.showMessages(e);
		}
		
		initComponents();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(MessageBundle.getMessage("angal.labnew.title"));

		if (patientSelected != null) {
			patientSelected(patientSelected);
		}
		showAsModal(owner);
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
			jTextAreaNote = new JTextArea(3, 50);
			jTextAreaNote.setText("");
			jTextAreaNote.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
					selectedLab.setNote(jTextAreaNote.getText().trim());
					examItems.get(jTableExams.getSelectedRow()).setNote(jTextAreaNote.getText().trim());
				}

				@Override
				public void keyPressed(KeyEvent e) {
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}
			});
		}
		return jTextAreaNote;
	}

	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jButtonCancel.addActionListener(actionEvent -> dispose());
		}
		return jButtonCancel;
	}

	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			jButtonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			jButtonOK.addActionListener(actionEvent -> {

				LocalDateTime newDate = LocalDateTime.now();
				try {
					newDate = jCalendarDate.getLocalDateTime();
				} catch (Exception e1) {
					MessageDialog.error(LabNew.this, "angal.lab.pleaseinsertavalidexamdate.msg");
					return;
				}
				RememberDates.setLastLabExamDate(newDate);
				String inOut = jRadioButtonOPD.isSelected() ? "O" : "I";

				for (Laboratory lab : examItems) {
					lab.setDate(newDate);
					lab.setExamDate(newDate.toLocalDate());
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
			});
		}
		return jButtonOK;
	}
	
	private String getIsAdmitted() {
		Admission adm = new Admission();
		try {
			adm = admissionManager.getCurrentAdmission(patientSelected);
		} catch(OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		return (adm==null?"O":"I");					
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonOK());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
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
			jPanelResults.setPreferredSize(new Dimension(EAST_WIDTH, RESULT_HEIGHT));
			jPanelResults.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.LIGHT_GRAY), MessageBundle.getMessage("angal.common.result.txt")));
		} else {
			jPanelResults.removeAll();
			int selectedRow = jTableExams.getSelectedRow();
			Exam selectedExam = selectedLab.getExam();

			if (selectedExam.getProcedure() == 1) {
				JTextField txtResultValue = new JTextField();
				JComboBox<String> jComboBoxExamResults = new JComboBox<>();
				jComboBoxExamResults.setMaximumSize(new Dimension(EAST_WIDTH, COMPONENT_HEIGHT));
				jComboBoxExamResults.setMinimumSize(new Dimension(EAST_WIDTH, COMPONENT_HEIGHT));
				jComboBoxExamResults.setPreferredSize(new Dimension(EAST_WIDTH, COMPONENT_HEIGHT));
				txtResultValue.setMaximumSize(new Dimension(EAST_WIDTH, COMPONENT_HEIGHT));
				txtResultValue.setMinimumSize(new Dimension(EAST_WIDTH, COMPONENT_HEIGHT));
				txtResultValue.setPreferredSize(new Dimension(EAST_WIDTH, COMPONENT_HEIGHT));
				List<ExamRow> exaRowArray;
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
				jComboBoxExamResults.addActionListener(actionEvent -> {
					selectedLab.setResult(jComboBoxExamResults.getSelectedItem().toString());
					examItems.set(selectedRow, selectedLab);
					jTableExams.updateUI();
				});
				if (jComboBoxExamResults.getItemCount() > 0) {
					jPanelResults.add(jComboBoxExamResults);
				} else {
					jPanelResults.add(new JLabel(selectedExam.getDefaultResult()));
				}

			} else if (selectedExam.getProcedure() == 2) {

				jPanelResults.removeAll();
				jPanelResults.setLayout(new BoxLayout(jPanelResults, BoxLayout.Y_AXIS));

				List<LaboratoryRow> checking = examResults.get(jTableExams.getSelectedRow());
				boolean checked;
				JPanel resultsContainer = new JPanel();
				resultsContainer.setLayout(new GridLayout(0, 1));
				JScrollPane resultsContainerScroll = new JScrollPane(resultsContainer);
				resultsContainerScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				resultsContainerScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				resultsContainerScroll.setBounds(0, 0, EAST_WIDTH, RESULT_HEIGHT);
				jPanelResults.add(resultsContainerScroll);
				List<ExamRow> exaRowArray;
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
							if (checking.contains(labRow)) {
								checked = true;
							}

							resultsContainer.add(new CheckBox(exaRow, checked));
						}
					}
				}
			} else if (selectedExam.getProcedure() == 3) {
				jTextFieldExamResult = new JTextField();
				jTextFieldExamResult.setMaximumSize(new Dimension(EAST_WIDTH, COMPONENT_HEIGHT));
				jTextFieldExamResult.setMinimumSize(new Dimension(EAST_WIDTH, COMPONENT_HEIGHT));
				jTextFieldExamResult.setPreferredSize(new Dimension(EAST_WIDTH, COMPONENT_HEIGHT));

				jTextFieldExamResult.setText(selectedLab.getResult());

				jTextFieldExamResult.getDocument().addDocumentListener(new DocumentListener() {

					@Override
					public void removeUpdate(DocumentEvent e) {
						selectedLab.setResult(jTextFieldExamResult.getText());
						jTableExams.updateUI();
					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						selectedLab.setResult(jTextFieldExamResult.getText());
						jTableExams.updateUI();
					}

					@Override
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

		private static final long serialVersionUID = 1L;
		private JCheckBox check = this;
		
		public CheckBox(ExamRow exaRow, boolean checked) {
			this.setText(exaRow.getDescription());
			this.setSelected(checked);
			this.addActionListener(actionEvent -> {
				if (check.isSelected()) {
					LaboratoryRow laboratoryRow = new LaboratoryRow();
					laboratoryRow.setDescription(actionEvent.getActionCommand());
					examResults.get(jTableExams.getSelectedRow()).add(laboratoryRow);
				} else {
					LaboratoryRow laboratoryRow = new LaboratoryRow();
					laboratoryRow.setDescription(actionEvent.getActionCommand());
					examResults.get(jTableExams.getSelectedRow()).remove(laboratoryRow);
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
			jComboBoxMaterial.addActionListener(actionEvent -> {
				selectedLab.setMaterial(labManager.getMaterialKey((String) jComboBoxMaterial.getSelectedItem()));
				examItems.get(jTableExams.getSelectedRow()).setMaterial(selectedLab.getMaterial());
			});
			jComboBoxMaterial.setPreferredSize(new Dimension(EAST_WIDTH, COMPONENT_HEIGHT));
			jComboBoxMaterial.setMaximumSize(new Dimension(EAST_WIDTH, COMPONENT_HEIGHT));
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
			jLabelDate = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
			jLabelDate.setPreferredSize(LABEL_DIMENSION);
		}
		return jLabelDate;
	}
	
	private JPanel getJOpdIpdPanel() {
		if (jOpdIpdPanel == null) {
			jOpdIpdPanel = new JPanel();
			
			jRadioButtonOPD = new JRadioButton(MessageBundle.getMessage("angal.labnew.opd.btn"));
			jRadioButtonIPD = new JRadioButton(MessageBundle.getMessage("angal.labnew.ip.btn"));

			ButtonGroup radioGroup = new ButtonGroup();
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
			jButtonTrashPatient.setPreferredSize(new Dimension(25, 25));
			jButtonTrashPatient.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png")); //$NON-NLS-1$
			jButtonTrashPatient.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.removepatientassociationwiththisexam")); //$NON-NLS-1$
			jButtonTrashPatient.addActionListener(actionEvent -> {

				patientSelected = null;
				//INTERFACE
				jTextFieldPatient.setText("");
				jTextFieldPatient.setEditable(false);
				jButtonPickPatient.setText(MessageBundle.getMessage("angal.labnew.findpatient.btn"));
				jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.labnew.tooltip.associateapatientwiththisexam")); //$NON-NLS-1$
				jButtonTrashPatient.setEnabled(false);
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
			jButtonPickPatient.addActionListener(actionEvent -> {
				SelectPatient sp = new SelectPatient(LabNew.this, patientSelected);
				sp.addSelectionListener(LabNew.this);
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
			jTextFieldPatient.setPreferredSize(PATIENT_DIMENSION);
			jTextFieldPatient.setEditable(false);
		}
		return jTextFieldPatient;
	}

	private JLabel getJLabelPatient() {
		if (jLabelPatient == null) {
			jLabelPatient = new JLabel(MessageBundle.getMessage("angal.common.patient.txt"));
			jLabelPatient.setPreferredSize(LABEL_DIMENSION);
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

	private GoodDateTimeChooser getJCalendarDate() {
		if (jCalendarDate == null) {
			LocalDateTime labDate = RememberDates.getLastLabExamDate();
			if (labDate == null) {
				labDate = LocalDateTime.now();
			}
			jCalendarDate = new GoodDateTimeChooser(labDate);
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
				if (!examResizable[i]) {
					jTableExams.getColumnModel().getColumn(i).setMaxWidth(examColumnWidth[i]);
				}
			}
			
			jTableExams.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ListSelectionModel listSelectionModel = jTableExams.getSelectionModel();
			listSelectionModel.addListSelectionListener(selectionEvent -> {
			// Check that mouse has been released.
			if (!selectionEvent.getValueIsAdjusting()) {
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

	private JPanel getJPanelExamButtons() {
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
			jButtonAddExam.addActionListener(actionEvent -> {
				String mat = "";

				OhTableModelExam<Price> modelOh = new OhTableModelExam<>(exaArray);

				ExamPicker examPicker = new ExamPicker(modelOh);

				examPicker.setSize(300, 400);

				JDialog dialog = new JDialog();
				dialog.setTitle(MessageBundle.getMessage("angal.stat.examslist")); // TODO: use more correct key
				dialog.setLocationRelativeTo(null);
				dialog.setSize(600, 350);
				dialog.setLocationRelativeTo(null);
				dialog.setModal(true);

				examPicker.setParentFrame(dialog);
				dialog.setContentPane(examPicker);
				dialog.setVisible(true);
				dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				List<Exam> exams = examPicker.getAllSelectedObject();

				Exam exa;
				Laboratory lab;
				boolean alreadyIn;

				if (exams.isEmpty()) {
					return;
				}

				for (Exam exam : exams) {
					alreadyIn = false;
					lab = new Laboratory();
					exa = exam;

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
			jButtonRemoveItem.addActionListener(actionEvent -> {

				int selectedRow = jTableExams.getSelectedRow();
				if (selectedRow < 0) {
					MessageDialog.error(LabNew.this,"angal.lab.pleaseselectanexam.msg");
				} else {
					examItems.remove(selectedRow);
					jPanelResults.removeAll();
					jTableExams.clearSelection();
					jTableModel.fireTableDataChanged();
					jTableExams.updateUI();
					jComboBoxMaterial.setEnabled(false);
					repaint();
				}
			});
		}
		return jButtonRemoveItem;
	}
	
	public class ExamTableModel extends DefaultTableModel {
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return examClasses[columnIndex].getClass();
		}

		@Override
		public int getColumnCount() {
			return examColumnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return examColumnNames[columnIndex];
		}

		@Override
		public int getRowCount() {
			if (examItems == null) {
				return 0;
			}
			return examItems.size();
		}

		@Override
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

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
		}

	}
}
