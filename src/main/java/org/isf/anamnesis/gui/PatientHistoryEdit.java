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
package org.isf.anamnesis.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.isf.anamnesis.manager.PatientHistoryManager;
import org.isf.anamnesis.model.PatientHistory;
import org.isf.anamnesis.model.PatientPatientHistory;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.model.Patient;
import org.isf.utils.jobjects.VoIntegerTextField;
import org.isf.utils.jobjects.VoLimitedTextArea;
import org.isf.utils.jobjects.VoLimitedTextField;

//VS4E -- DO NOT REMOVE THIS LINE!
public class PatientHistoryEdit extends JDialog {

	private static final long serialVersionUID = 1L;

	private PatientHistoryManager patientHistoryManager = Context.getApplicationContext().getBean(PatientHistoryManager.class);


	private JPanel jPanelPatient;
	private JPanel jPanelAnamnesis;
	private JPanel jPanelFamily;
	private JPanel jPanelPathologicalClosed;
	private JPanel jPanelPathologicalOpen;
	private JPanel jPanelPathologicalExtra;
	private JPanel jPanelPathological;
	private JPanel jPanelPhyHistoryUnisex;
	private JPanel jPanelButtons;
	private JLabel jLabelFirstName;
	private JLabel jLabelFirstNameText;
	private JLabel jLabelSecondName;
	private JLabel jLabelSecondNameText;
	private JLabel jLabelPatID;
	private JLabel jLabelPatIDText;

	// FAMILY
	private JCheckBox jCheckBoxFamilyNothing;
	private JCheckBox jCheckBoxFamilyHypertension;
	private JCheckBox jCheckBoxFamilyDrugsAddiction;
	private JCheckBox jCheckBoxFamilyCardio;
	private JCheckBox jCheckBoxFamilyInfective;
	private JCheckBox jCheckBoxFamilyEndo;
	private JCheckBox jCheckBoxFamilyRespiratory;
	private JCheckBox jCheckBoxFamilyCancer;
	private JCheckBox jCheckBoxFamilyOrto;
	private JCheckBox jCheckBoxFamilyOther;
	private JCheckBox jCheckBoxFamilyGyno;
	private JLabel jLabelFamilyDiseases;
	private VoLimitedTextArea jTextAreaFamilyNote;
	private JScrollPane jScrollPaneFamilyNote;

	// PATHOLOGICAL CLOSED
	private JCheckBox jCheckBoxPathClosedNothing;
	private JCheckBox jCheckBoxPathClosedHypertension;
	private JCheckBox jCheckBoxPathClosedDrugsAddiction;
	private JCheckBox jCheckBoxPathClosedCardio;
	private JCheckBox jCheckBoxPathClosedInfective;
	private JCheckBox jCheckBoxPathClosedEndo;
	private JCheckBox jCheckBoxPathClosedRespiratory;
	private JCheckBox jCheckBoxPathClosedCancer;
	private JCheckBox jCheckBoxPathClosedOrto;
	private JCheckBox jCheckBoxPathClosedGyno;
	private JCheckBox jCheckBoxPathClosedOther;
	private JLabel jLabelPathClosedDiseases;
	private VoLimitedTextArea jTextAreaPathClosedNote;
	private JScrollPane jScrollPanePathClosedNote;

	//	 PATHOLOGICAL OPEN
	private JCheckBox jCheckBoxPathOpenNothing;
	private JCheckBox jCheckBoxPathOpenHypertension;
	private JCheckBox jCheckBoxPathOpenDrugsAddiction;
	private JCheckBox jCheckBoxPathOpenCardio;
	private JCheckBox jCheckBoxPathOpenInfective;
	private JCheckBox jCheckBoxPathOpenEndo;
	private JCheckBox jCheckBoxPathOpenRespiratory;
	private JCheckBox jCheckBoxPathOpenCancer;
	private JCheckBox jCheckBoxPathOpenOrto;
	private JCheckBox jCheckBoxPathOpenGyno;
	private JCheckBox jCheckBoxPathOpenOther;
	private JLabel jLabelPathOpenDiseases;
	private VoLimitedTextArea jTextAreaPathOpenNote;
	private JScrollPane jScrollPanePathOpenNote;

	// PATHOLOGICAL EXTRA
	private VoLimitedTextArea jTextAreaPathExtraSurgery;
	private VoLimitedTextArea jTextAreaPathExtraAllergy;
	private VoLimitedTextArea jTextAreaPathExtraTherapy;
	private VoLimitedTextArea jTextAreaPathExtraUsualMedicines;
	private JScrollPane jScrollPanePathExtraSurgery;
	private JScrollPane jScrollPanePathExtraAllergy;
	private JScrollPane jScrollPanePathExtraTherapy;
	private JScrollPane jScrollPanePathExtraUsualMedicines;
	private VoLimitedTextArea jTextAreaPathExtraNote;
	private JScrollPane jScrollPanePathExtraNote;

	// PHYSIOLOGICAL UNISEX
	private JLabel jLabelPhyDiet;
	private JLabel jLabelPhyAlvo;
	private JLabel jLabelPhyDiuresis;
	private JLabel jLabelPhyAlcool;
	private JLabel jLabelPhySmoke;
	private JLabel jLabelPhyDrugs;
	private JCheckBox jCheckBoxPhyDietNormal;
	private JCheckBox jCheckBoxPhyDietAbnormal;
	private JCheckBox jCheckBoxPhyAlvoNormal;
	private JCheckBox jCheckBoxPhyDiuresisNormal;
	private JCheckBox jCheckBoxPhyAlcoolNo;
	private JCheckBox jCheckBoxPhySmokeNo;
	private JCheckBox jCheckBoxPhyDrugsNo;
	private JCheckBox jCheckBoxPhyAlvoAbnormal;
	private JCheckBox jCheckBoxPhyDiuresisAbnormal;
	private JCheckBox jCheckBoxPhyAlcoolYes;
	private JCheckBox jCheckBoxPhySmokeYes;
	private JCheckBox jCheckBoxPhyDrugsYes;
	private VoLimitedTextField jTextFieldPhyDietAbnormalText;
	private VoLimitedTextField jTextFieldPhyAlvoAbnormalText;
	private VoLimitedTextField jTextFieldPhyDiuresisAbnormalText;

	// PHYSIOLOGICAL FEMALE
	private JLabel jLabelPhyPeriod;
	private JLabel jLabelPhyMenopause;
	private JLabel jLabelPhyPregnancies;
	private JPanel jPanelPhysiologicalHistory;
	private JPanel jPanelPhyHistoryFemale;
	private JLabel jLabelPhyHRT;
	private JCheckBox jCheckBoxPhyPeriodNormal;
	private JCheckBox jCheckBoxPhyPeriodAbnormal;
	private VoLimitedTextField jTextFieldPhyPeriodAbnormalText;
	private JCheckBox jCheckBoxPhyMenopauseNo;
	private JCheckBox jCheckBoxPhyMenopauseYes;
	private VoIntegerTextField jTextFieldPhyMenopauseYesYears;
	private JCheckBox jCheckBoxPhyHRTNo;
	private JCheckBox jCheckBoxPhyHRTYes;
	private VoLimitedTextField jTextFieldPhyHRTYesText;
	private JPanel jPanelPhyPregnancyPanel;
	private JCheckBox jCheckBoxPhyPregnancyNo;
	private JCheckBox jCheckBoxPhyPregnancyYes;
	private JLabel jLabelPhyPregnancyNumber;
	private VoIntegerTextField jTextFieldPhyPregnancyNumber;
	private JLabel jLabelPhyPregnancyDeliveryNumber;
	private VoIntegerTextField jTextFieldPhyPregnancyDeliveryNumber;
	private JLabel jLabelPhyPregnancyAbortNumber;
	private VoIntegerTextField jTextFieldPhyPregnancyAbortNumber;
	private JPanel jPanelPhyMenopausePanel;
	private JLabel jLabelPhyMenopauseYears;

	// Buttons Groups
	private final ButtonGroup buttonGroupAlcool = new ButtonGroup();
	private final ButtonGroup buttonGroupSmoke = new ButtonGroup();
	private final ButtonGroup buttonGroupDrugs = new ButtonGroup();
	private final ButtonGroup buttonGroupDiet = new ButtonGroup();
	private final ButtonGroup buttonGroupAlvo = new ButtonGroup();
	private final ButtonGroup buttonGroupDiuresis = new ButtonGroup();
	private final ButtonGroup buttonGroupPeriod = new ButtonGroup();
	private final ButtonGroup buttonGroupMenopause = new ButtonGroup();
	private final ButtonGroup buttonGroupHRT = new ButtonGroup();
	private final ButtonGroup buttonGroupPregnancy = new ButtonGroup();

	// Buttons
	private JButton jButtonSave;
	private JButton jButtonCancel;

	// Actions in this form
	private Action actionExcludeFamilyNothing;
	private Action actionExcludePathClosedNothing;
	private Action actionExcludePathOpenNothing;
	private Action actionResetFamilyHistory;
	private Action actionResetPathClosed;
	private Action actionResetPathOpen;
	private Action actionSavePatientHistory;

	// Fonts
	private Font fontBoldTitleBorder = new Font("Tahoma", Font.BOLD, 11); //$NON-NLS-1$
	private Font fontDiseases = new Font("Tahoma", Font.BOLD, 12); //$NON-NLS-1$

	private boolean storeData;
	// Attributes
	private PatientHistory path;
	private Patient pat;
//	private String stp;
	private Action actionInsertExamination;

	public PatientHistoryEdit() {
		super();
		initComponents();
	}

	public PatientHistoryEdit(PatientPatientHistory path) {
		super();
		this.path = path.getPatientHistory();
		pat = path.getPatient();
		initComponents();
	}

	public PatientHistoryEdit(Frame parent, PatientPatientHistory path) {
		super(parent, true);
		this.path = path.getPatientHistory();
		pat = path.getPatient();
		initComponents();
	}
	
	public PatientHistoryEdit(Frame parent, PatientPatientHistory path, boolean storeData) {
		super(parent, true);
		this.path = path.getPatientHistory();
		pat = path.getPatient();
		this.storeData = storeData;
		initComponents();
	}
	
	public PatientHistoryEdit(Dialog parent, PatientPatientHistory path, boolean storeData) {
		super(parent, true);
		this.path = path.getPatientHistory();
		pat = path.getPatient();
		this.storeData = storeData;
		initComponents();
	}

	public PatientHistoryEdit(Dialog parent, PatientPatientHistory path) {
		super(parent, true);
		this.path = path.getPatientHistory();
		pat = path.getPatient();
		initComponents();
	}

	private void initComponents() {
		setTitle(MessageBundle.getMessage("angal.anamnesis.title.txt"));
		setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
		getContentPane().add(getJPanelPatient(), BorderLayout.NORTH);
		getContentPane().add(getJPanelAnamnesis(), BorderLayout.CENTER);
		getContentPane().add(getJPanelButtons(), BorderLayout.SOUTH);
		setSize(1063, 586); //TODO remove comment for design
		updateGUIPatient();
		updateGUIHistory();
	}


	private void updateGUIPatient() {
		jLabelPatIDText.setText(String.valueOf(path.getPatientId()));
		jLabelFirstNameText.setText(pat.getFirstName());
		jLabelSecondNameText.setText(pat.getSecondName());
//		jLabelSTPCodeText.setText(stp);
	}

	private void updateGUIHistory() {
		// Family
		jCheckBoxFamilyNothing.setSelected(path.isFamilyNothing());
		jCheckBoxFamilyHypertension.setSelected(path.isFamilyHypertension());
		jCheckBoxFamilyDrugsAddiction.setSelected(path.isFamilyDrugAddiction());
		jCheckBoxFamilyCardio.setSelected(path.isFamilyCardiovascular());
		jCheckBoxFamilyInfective.setSelected(path.isFamilyInfective());
		jCheckBoxFamilyEndo.setSelected(path.isFamilyEndocrinometabol());
		jCheckBoxFamilyRespiratory.setSelected(path.isFamilyRespiratory());
		jCheckBoxFamilyCancer.setSelected(path.isFamilyCancer());
		jCheckBoxFamilyOrto.setSelected(path.isFamilyOrto());
		jCheckBoxFamilyGyno.setSelected(path.isFamilyGyno());
		jCheckBoxFamilyOther.setSelected(path.isFamilyOther());
		jTextAreaFamilyNote.setText(path.getFamilyNote());
		// Closed
		jCheckBoxPathClosedNothing.setSelected(path.isPatClosedNothing());
		jCheckBoxPathClosedHypertension.setSelected(path.isPatClosedHypertension());
		jCheckBoxPathClosedDrugsAddiction.setSelected(path.isPatClosedDrugaddiction());
		jCheckBoxPathClosedCardio.setSelected(path.isPatClosedCardiovascular());
		jCheckBoxPathClosedInfective.setSelected(path.isPatClosedInfective());
		jCheckBoxPathClosedEndo.setSelected(path.isPatClosedEndocrinometabol());
		jCheckBoxPathClosedRespiratory.setSelected(path.isPatClosedRespiratory());
		jCheckBoxPathClosedCancer.setSelected(path.isPatClosedCancer());
		jCheckBoxPathClosedOrto.setSelected(path.isPatClosedOrto());
		jCheckBoxPathClosedGyno.setSelected(path.isPatClosedGyno());
		jCheckBoxPathClosedOther.setSelected(path.isPatClosedOther());
		jTextAreaPathClosedNote.setText(path.getPatClosedNote());
		// Open
		jCheckBoxPathOpenNothing.setSelected(path.isPatOpenNothing());
		jCheckBoxPathOpenHypertension.setSelected(path.isPatOpenHypertension());
		jCheckBoxPathOpenDrugsAddiction.setSelected(path.isPatOpenDrugaddiction());
		jCheckBoxPathOpenCardio.setSelected(path.isPatOpenCardiovascular());
		jCheckBoxPathOpenInfective.setSelected(path.isPatOpenInfective());
		jCheckBoxPathOpenEndo.setSelected(path.isPatOpenEndocrinometabol());
		jCheckBoxPathOpenRespiratory.setSelected(path.isPatOpenRespiratory());
		jCheckBoxPathOpenCancer.setSelected(path.isPatOpenCancer());
		jCheckBoxPathOpenOrto.setSelected(path.isPatOpenOrto());
		jCheckBoxPathOpenGyno.setSelected(path.isPatOpenGyno());
		jCheckBoxPathOpenOther.setSelected(path.isPatOpenOther());
		jTextAreaPathOpenNote.setText(path.getPatOpenNote());
		// Extra
		jTextAreaPathExtraSurgery.setText(path.getPatSurgery());
		jTextAreaPathExtraAllergy.setText(path.getPatAllergy());
		jTextAreaPathExtraTherapy.setText(path.getPatTherapy());
		jTextAreaPathExtraUsualMedicines.setText(path.getPatMedicine());
		jTextAreaPathExtraNote.setText(path.getPatNote());
		// Physiologic Unisex
		jCheckBoxPhyDietNormal.setSelected(path.isPhyNutritionNormal());
		jCheckBoxPhyDietAbnormal.setSelected(!path.isPhyNutritionNormal());
		jTextFieldPhyDietAbnormalText.setText(path.getPhyNutritionAbnormal());
		jCheckBoxPhyAlvoNormal.setSelected(path.isPhyAlvoNormal());
		jCheckBoxPhyAlvoAbnormal.setSelected(!path.isPhyAlvoNormal());
		jTextFieldPhyAlvoAbnormalText.setText(path.getPhyAlvoAbnormal());
		jCheckBoxPhyDiuresisNormal.setSelected(path.isPhyDiuresisNormal());
		jCheckBoxPhyDiuresisAbnormal.setSelected(!path.isPhyDiuresisNormal());
		jTextFieldPhyDiuresisAbnormalText.setText(path.getPhyDiuresisAbnormal());
		jCheckBoxPhyAlcoolNo.setSelected(!path.isPhyAlcool());
		jCheckBoxPhyAlcoolYes.setSelected(path.isPhyAlcool());
		jCheckBoxPhySmokeNo.setSelected(!path.isPhySmoke());
		jCheckBoxPhySmokeYes.setSelected(path.isPhySmoke());
		jCheckBoxPhyDrugsNo.setSelected(!path.isPhyDrug());
		jCheckBoxPhyDrugsYes.setSelected(path.isPhyDrug());
		// Physiologic Female
		jCheckBoxPhyPeriodNormal.setSelected(path.isPhyPeriodNormal());
		jCheckBoxPhyPeriodAbnormal.setSelected(!path.isPhyPeriodNormal());
		jTextFieldPhyPeriodAbnormalText.setText(path.getPhyPeriodAbnormal());
		jCheckBoxPhyMenopauseNo.setSelected(!path.isPhyMenopause());
		jCheckBoxPhyMenopauseYes.setSelected(path.isPhyMenopause());
		jTextFieldPhyMenopauseYesYears.setText(String.valueOf(path.getPhyMenopauseYears()));
		jCheckBoxPhyHRTNo.setSelected(path.isPhyHrtNormal());
		jCheckBoxPhyHRTYes.setSelected(!path.isPhyHrtNormal());
		jTextFieldPhyHRTYesText.setText(path.getPhyHrtAbnormal());
		jCheckBoxPhyPregnancyNo.setSelected(!path.isPhyPregnancy());
		jCheckBoxPhyPregnancyYes.setSelected(path.isPhyPregnancy());
		jTextFieldPhyPregnancyNumber.setText(String.valueOf(path.getPhyPregnancyNumber()));
		jTextFieldPhyPregnancyDeliveryNumber.setText(String.valueOf(path.getPhyPregnancyBirth()));
		jTextFieldPhyPregnancyAbortNumber.setText(String.valueOf(path.getPhyPregnancyAbort()));
	}

	private void updateModelFromGUI() {
		//Family
		path.setFamilyHypertension(jCheckBoxFamilyHypertension.isSelected());
		path.setFamilyDrugAddiction(jCheckBoxFamilyDrugsAddiction.isSelected());
		path.setFamilyCardiovascular(jCheckBoxFamilyCardio.isSelected());
		path.setFamilyInfective(jCheckBoxFamilyInfective.isSelected());
		path.setFamilyEndocrinometabol(jCheckBoxFamilyEndo.isSelected());
		path.setFamilyRespiratory(jCheckBoxFamilyRespiratory.isSelected());
		path.setFamilyCancer(jCheckBoxFamilyCancer.isSelected());
		path.setFamilyOrto(jCheckBoxFamilyOrto.isSelected());
		path.setFamilyGyno(jCheckBoxFamilyGyno.isSelected());
		path.setFamilyOther(jCheckBoxFamilyOther.isSelected());
		path.setFamilyNote(jTextAreaFamilyNote.getText());
		//Closed
		path.setPatClosedHypertension(jCheckBoxPathClosedHypertension.isSelected());
		path.setPatClosedDrugaddiction(jCheckBoxPathClosedDrugsAddiction.isSelected());
		path.setPatClosedCardiovascular(jCheckBoxPathClosedCardio.isSelected());
		path.setPatClosedInfective(jCheckBoxPathClosedInfective.isSelected());
		path.setPatClosedEndocrinometabol(jCheckBoxPathClosedEndo.isSelected());
		path.setPatClosedRespiratory(jCheckBoxPathClosedRespiratory.isSelected());
		path.setPatClosedCancer(jCheckBoxPathClosedCancer.isSelected());
		path.setPatClosedOrto(jCheckBoxPathClosedOrto.isSelected());
		path.setPatClosedGyno(jCheckBoxPathClosedGyno.isSelected());
		path.setPatClosedOther(jCheckBoxPathClosedOther.isSelected());
		path.setPatClosedNote(jTextAreaPathClosedNote.getText());
		//Open
		path.setPatOpenHypertension(jCheckBoxPathOpenHypertension.isSelected());
		path.setPatOpenDrugaddiction(jCheckBoxPathOpenDrugsAddiction.isSelected());
		path.setPatOpenCardiovascular(jCheckBoxPathOpenCardio.isSelected());
		path.setPatOpenInfective(jCheckBoxPathOpenInfective.isSelected());
		path.setPatOpenEndocrinometabol(jCheckBoxPathOpenEndo.isSelected());
		path.setPatOpenRespiratory(jCheckBoxPathOpenRespiratory.isSelected());
		path.setPatOpenCancer(jCheckBoxPathOpenCancer.isSelected());
		path.setPatOpenOrto(jCheckBoxPathOpenOrto.isSelected());
		path.setPatOpenGyno(jCheckBoxPathOpenGyno.isSelected());
		path.setPatOpenOther(jCheckBoxPathOpenOther.isSelected());
		path.setPatOpenNote(jTextAreaPathOpenNote.getText());
		// Extra
		path.setPatSurgery(jTextAreaPathExtraSurgery.getText());
		path.setPatAllergy(jTextAreaPathExtraAllergy.getText());
		path.setPatTherapy(jTextAreaPathExtraTherapy.getText());
		path.setPatMedicine(jTextAreaPathExtraUsualMedicines.getText());
		path.setPatNote(jTextAreaPathExtraNote.getText());
		// Physiologic Unisex
		path.setPhyNutritionNormal(jCheckBoxPhyDietNormal.isSelected());
		path.setPhyNutritionAbnormal(jTextFieldPhyDietAbnormalText.getText());
		path.setPhyAlvoNormal(jCheckBoxPhyAlvoNormal.isSelected());
		path.setPhyAlvoAbnormal(jTextFieldPhyAlvoAbnormalText.getText());
		path.setPhyDiuresisNormal(jCheckBoxPhyDiuresisNormal.isSelected());
		path.setPhyDiuresisAbnormal(jTextFieldPhyDiuresisAbnormalText.getText());
		path.setPhyAlcool(jCheckBoxPhyAlcoolYes.isSelected());
		path.setPhySmoke(jCheckBoxPhySmokeYes.isSelected());
		path.setPhyDrug(jCheckBoxPhyDrugsYes.isSelected());
		// Physiologic Female
		path.setPhyPeriodNormal(jCheckBoxPhyPeriodNormal.isSelected());
		path.setPhyPeriodAbnormal(jTextFieldPhyPeriodAbnormalText.getText());
		path.setPhyMenopause(jCheckBoxPhyMenopauseYes.isSelected());
		path.setPhyMenopauseYears(Integer.parseInt(jTextFieldPhyMenopauseYesYears.getText()));
		path.setPhyHrtNormal(jCheckBoxPhyHRTNo.isSelected());
		path.setPhyHrtAbnormal(jTextFieldPhyHRTYesText.getText());
		path.setPhyPregnancy(jCheckBoxPhyPregnancyYes.isSelected());
		path.setPhyPregnancyNumber(Integer.parseInt(jTextFieldPhyPregnancyNumber.getText()));
		path.setPhyPregnancyBirth(Integer.parseInt(jTextFieldPhyPregnancyDeliveryNumber.getText()));
		path.setPhyPregnancyAbort(Integer.parseInt(jTextFieldPhyPregnancyAbortNumber.getText()));
	}

	private VoLimitedTextArea getJTextAreaPathExtraNote() {
		if (jTextAreaPathExtraNote == null) {
			jTextAreaPathExtraNote = new VoLimitedTextArea(100, 2, 20);
			jTextAreaPathExtraNote.setWrapStyleWord(true);
			jTextAreaPathExtraNote.setLineWrap(true);
			jTextAreaPathExtraNote.setMargin(new Insets(0, 5, 0, 0));
			jTextAreaPathExtraNote.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					path.setPatNote(jTextAreaPathExtraNote.getText());
				}
			});
		}
		return jTextAreaPathExtraNote;
	}

	private JScrollPane getJScrollPanePathExtraNote() {
		if (jScrollPanePathExtraNote == null) {
			jScrollPanePathExtraNote = new JScrollPane();
			jScrollPanePathExtraNote.setAlignmentX(Component.LEFT_ALIGNMENT);
			jScrollPanePathExtraNote.setBorder(new TitledBorder(null, MessageBundle.getMessage("angal.anamnesis.extra.remarks.border"), TitledBorder.LEADING, TitledBorder.TOP, fontBoldTitleBorder, null)); //$NON-NLS-1$
			jScrollPanePathExtraNote.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPanePathExtraNote.setViewportView(getJTextAreaPathExtraNote());
		}
		return jScrollPanePathExtraNote;
	}

	private JScrollPane getJScrollPanePathExtraSurgery() {
		if (jScrollPanePathExtraSurgery == null) {
			jScrollPanePathExtraSurgery = new JScrollPane();
			jScrollPanePathExtraSurgery.setAlignmentX(Component.LEFT_ALIGNMENT);
			jScrollPanePathExtraSurgery.setBorder(new TitledBorder(null, MessageBundle.getMessage("angal.anamnesis.extra.surgery.border"), TitledBorder.LEADING, TitledBorder.TOP, fontBoldTitleBorder, null)); //$NON-NLS-1$
			jScrollPanePathExtraSurgery.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPanePathExtraSurgery.setViewportView(getJTextAreaPathExtraSurgery());
		}
		return jScrollPanePathExtraSurgery;
	}

	private JScrollPane getJScrollPanePathExtraAllergy() {
		if (jScrollPanePathExtraAllergy == null) {
			jScrollPanePathExtraAllergy = new JScrollPane();
			jScrollPanePathExtraAllergy.setAlignmentX(Component.LEFT_ALIGNMENT);
			jScrollPanePathExtraAllergy.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), MessageBundle.getMessage("angal.anamnesis.extra.allergy.border"), TitledBorder.LEADING, TitledBorder.TOP, fontBoldTitleBorder, null)); //$NON-NLS-1$ //$NON-NLS-2$
			jScrollPanePathExtraAllergy.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPanePathExtraAllergy.setViewportView(getJTextAreaPathExtraAllergy());
		}
		return jScrollPanePathExtraAllergy;
	}

	private JScrollPane getJScrollPanePathExtraTherapy() {
		if (jScrollPanePathExtraTherapy == null) {
			jScrollPanePathExtraTherapy = new JScrollPane();
			jScrollPanePathExtraTherapy.setAlignmentX(Component.LEFT_ALIGNMENT);
			jScrollPanePathExtraTherapy.setBorder(new TitledBorder(null, MessageBundle.getMessage("angal.anamnesis.extra.therapy.border"), TitledBorder.LEADING, TitledBorder.TOP, fontBoldTitleBorder, null)); //$NON-NLS-1$
			jScrollPanePathExtraTherapy.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPanePathExtraTherapy.setViewportView(getJTextAreaPathExtraTherapy());
		}
		return jScrollPanePathExtraTherapy;
	}

	private VoLimitedTextArea getJTextAreaPathExtraTherapy() {
		if (jTextAreaPathExtraTherapy == null) {
			jTextAreaPathExtraTherapy = new VoLimitedTextArea(200, 3, 25);
			jTextAreaPathExtraTherapy.setWrapStyleWord(true);
			jTextAreaPathExtraTherapy.setLineWrap(true);
			jTextAreaPathExtraTherapy.setMargin(new Insets(0, 5, 0, 0));
			jTextAreaPathExtraTherapy.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					path.setPatTherapy(jTextAreaPathExtraTherapy.getText());
				}
			});
		}
		return jTextAreaPathExtraTherapy;
	}

	private JScrollPane getJScrollPanePathExtraUsualMedicines() {
		if (jScrollPanePathExtraUsualMedicines == null) {
			jScrollPanePathExtraUsualMedicines = new JScrollPane();
			jScrollPanePathExtraUsualMedicines.setAlignmentX(Component.LEFT_ALIGNMENT);
			jScrollPanePathExtraUsualMedicines.setBorder(new TitledBorder(null, MessageBundle.getMessage("angal.anamnesis.extra.medicine.border"), TitledBorder.LEADING, TitledBorder.TOP, fontBoldTitleBorder, null)); //$NON-NLS-1$
			jScrollPanePathExtraUsualMedicines.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPanePathExtraUsualMedicines.setViewportView(getJTextAreaPathExtraUsualMedicines());
		}
		return jScrollPanePathExtraUsualMedicines;
	}

	private VoLimitedTextArea getJTextAreaPathExtraUsualMedicines() {
		if (jTextAreaPathExtraUsualMedicines == null) {
			jTextAreaPathExtraUsualMedicines = new VoLimitedTextArea(200, 3, 25);
			jTextAreaPathExtraUsualMedicines.setWrapStyleWord(true);
			jTextAreaPathExtraUsualMedicines.setLineWrap(true);
			jTextAreaPathExtraUsualMedicines.setMargin(new Insets(0, 5, 0, 0));
			jTextAreaPathExtraUsualMedicines.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					path.setPatMedicine(jTextAreaPathExtraUsualMedicines.getText());
				}
			});
		}
		return jTextAreaPathExtraUsualMedicines;
	}

	private VoLimitedTextArea getJTextAreaPathExtraAllergy() {
		if (jTextAreaPathExtraAllergy == null) {
			jTextAreaPathExtraAllergy = new VoLimitedTextArea(100, 2, 25);
			jTextAreaPathExtraAllergy.setWrapStyleWord(true);
			jTextAreaPathExtraAllergy.setLineWrap(true);
			jTextAreaPathExtraAllergy.setMargin(new Insets(0, 5, 0, 0));
			jTextAreaPathExtraAllergy.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					path.setPatAllergy(jTextAreaPathExtraAllergy.getText());
				}
			});
		}
		return jTextAreaPathExtraAllergy;
	}

	private VoLimitedTextArea getJTextAreaPathExtraSurgery() {
		if (jTextAreaPathExtraSurgery == null) {
			jTextAreaPathExtraSurgery = new VoLimitedTextArea(200, 3, 25);
			jTextAreaPathExtraSurgery.setWrapStyleWord(true);
			jTextAreaPathExtraSurgery.setLineWrap(true);
			jTextAreaPathExtraSurgery.setMargin(new Insets(0, 5, 0, 0));
			jTextAreaPathExtraSurgery.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					path.setPatSurgery(jTextAreaPathExtraSurgery.getText());
				}
			});
		}
		return jTextAreaPathExtraSurgery;
	}

	private VoLimitedTextArea getJTextAreaPathOpenNote() {
		if (jTextAreaPathOpenNote == null) {
			jTextAreaPathOpenNote = new VoLimitedTextArea(100, 2, 20);
			jTextAreaPathOpenNote.setLineWrap(true);
			jTextAreaPathOpenNote.setWrapStyleWord(true);
			jTextAreaPathOpenNote.setMargin(new Insets(0, 5, 0, 0));
			jTextAreaPathOpenNote.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					path.setPatOpenNote(jTextAreaPathOpenNote.getText());
				}
			});
		}
		return jTextAreaPathOpenNote;
	}

	private JScrollPane getJScrollPanePathOpenNote() {
		if (jScrollPanePathOpenNote == null) {
			jScrollPanePathOpenNote = new JScrollPane();
			jScrollPanePathOpenNote.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPanePathOpenNote.setAlignmentX(Component.LEFT_ALIGNMENT);
			jScrollPanePathOpenNote.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), MessageBundle.getMessage("angal.anamnesis.open.remarks.border"), TitledBorder.LEADING, TitledBorder.TOP, fontBoldTitleBorder, null)); //$NON-NLS-1$ //$NON-NLS-2$
			jScrollPanePathOpenNote.setViewportView(getJTextAreaPathOpenNote());
		}
		return jScrollPanePathOpenNote;
	}

	private JLabel getJLabelPathOpenDiseases() {
		if (jLabelPathOpenDiseases == null) {
			jLabelPathOpenDiseases = new JLabel(MessageBundle.getMessage("angal.anamnesis.open.diseases.txt")); //$NON-NLS-1$
			jLabelPathOpenDiseases.setFont(fontDiseases);
			jLabelPathOpenDiseases.setHorizontalAlignment(SwingConstants.LEFT);
		}
		return jLabelPathOpenDiseases;
	}

	private JCheckBox getJCheckBoxPathOpenOther() {
		if (jCheckBoxPathOpenOther == null) {
			jCheckBoxPathOpenOther = new JCheckBox();
			jCheckBoxPathOpenOther.setAction(getActionExcludePathOpenNothing());
			jCheckBoxPathOpenOther.setText(MessageBundle.getMessage("angal.anamnesis.open.other.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathOpenOther;
	}

	private JCheckBox getJCheckBoxPathOpenGyno() {
		if (jCheckBoxPathOpenGyno == null) {
			jCheckBoxPathOpenGyno = new JCheckBox();
			jCheckBoxPathOpenGyno.setAction(getActionExcludePathOpenNothing());
			jCheckBoxPathOpenGyno.setText(MessageBundle.getMessage("angal.anamnesis.open.gyno.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathOpenGyno;
	}

	private JCheckBox getJCheckBoxPathOpenOrto() {
		if (jCheckBoxPathOpenOrto == null) {
			jCheckBoxPathOpenOrto = new JCheckBox();
			jCheckBoxPathOpenOrto.setAction(getActionExcludePathOpenNothing());
			jCheckBoxPathOpenOrto.setText(MessageBundle.getMessage("angal.anamnesis.open.orto.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathOpenOrto;
	}

	private JCheckBox getJCheckBoxPathOpenCancer() {
		if (jCheckBoxPathOpenCancer == null) {
			jCheckBoxPathOpenCancer = new JCheckBox();
			jCheckBoxPathOpenCancer.setAction(getActionExcludePathOpenNothing());
			jCheckBoxPathOpenCancer.setText(MessageBundle.getMessage("angal.anamnesis.open.neoplastic.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathOpenCancer;
	}

	private JCheckBox getJCheckBoxPathOpenRespiratory() {
		if (jCheckBoxPathOpenRespiratory == null) {
			jCheckBoxPathOpenRespiratory = new JCheckBox();
			jCheckBoxPathOpenRespiratory.setAction(getActionExcludePathOpenNothing());
			jCheckBoxPathOpenRespiratory.setText(MessageBundle.getMessage("angal.anamnesis.open.respiratory.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathOpenRespiratory;
	}

	private JCheckBox getJCheckBoxPathOpenEndo() {
		if (jCheckBoxPathOpenEndo == null) {
			jCheckBoxPathOpenEndo = new JCheckBox();
			jCheckBoxPathOpenEndo.setAction(getActionExcludePathOpenNothing());
			jCheckBoxPathOpenEndo.setText(MessageBundle.getMessage("angal.anamnesis.open.endocrinometabol.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathOpenEndo;
	}

	private JCheckBox getJCheckBoxPathOpenInfective() {
		if (jCheckBoxPathOpenInfective == null) {
			jCheckBoxPathOpenInfective = new JCheckBox();
			jCheckBoxPathOpenInfective.setAction(getActionExcludePathOpenNothing());
			jCheckBoxPathOpenInfective.setText(MessageBundle.getMessage("angal.anamnesis.open.infective.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathOpenInfective;
	}

	private JCheckBox getJCheckBoxPathOpenCardio() {
		if (jCheckBoxPathOpenCardio == null) {
			jCheckBoxPathOpenCardio = new JCheckBox();
			jCheckBoxPathOpenCardio.setAction(getActionExcludePathOpenNothing());
			jCheckBoxPathOpenCardio.setText(MessageBundle.getMessage("angal.anamnesis.open.cardiovascolaris.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathOpenCardio;
	}

	private JCheckBox getJCheckBoxPathOpenDrugsAddiction() {
		if (jCheckBoxPathOpenDrugsAddiction == null) {
			jCheckBoxPathOpenDrugsAddiction = new JCheckBox();
			jCheckBoxPathOpenDrugsAddiction.setAction(getActionExcludePathOpenNothing());
			jCheckBoxPathOpenDrugsAddiction.setText(MessageBundle.getMessage("angal.anamnesis.open.drugsaddiction.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathOpenDrugsAddiction;
	}

	private JCheckBox getJCheckBoxPathOpenHypertension() {
		if (jCheckBoxPathOpenHypertension == null) {
			jCheckBoxPathOpenHypertension = new JCheckBox();
			jCheckBoxPathOpenHypertension.setAction(getActionExcludePathOpenNothing());
			jCheckBoxPathOpenHypertension.setText(MessageBundle.getMessage("angal.anamnesis.open.hypertension.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathOpenHypertension;
	}

	private JCheckBox getJCheckBoxPathOpenNothing() {
		if (jCheckBoxPathOpenNothing == null) {
			jCheckBoxPathOpenNothing = new JCheckBox();
			jCheckBoxPathOpenNothing.setAction(getActionResetPathOpen());
			jCheckBoxPathOpenNothing.setText(MessageBundle.getMessage("angal.anamnesis.open.nothingtodeclare.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathOpenNothing;
	}

	private VoLimitedTextArea getJTextAreaPathClosedNote() {
		if (jTextAreaPathClosedNote == null) {
			jTextAreaPathClosedNote = new VoLimitedTextArea(100, 2, 20);
			jTextAreaPathClosedNote.setWrapStyleWord(true);
			jTextAreaPathClosedNote.setLineWrap(true);
			jTextAreaPathClosedNote.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					path.setPatClosedNote(jTextAreaPathClosedNote.getText());
				}
			});
		}
		return jTextAreaPathClosedNote;
	}

	private JScrollPane getJScrollPanePathClosedNote() {
		if (jScrollPanePathClosedNote == null) {
			jScrollPanePathClosedNote = new JScrollPane();
			jScrollPanePathClosedNote.setAlignmentX(Component.LEFT_ALIGNMENT);
			jScrollPanePathClosedNote.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), MessageBundle.getMessage("angal.anamnesis.closed.remarks.border"), TitledBorder.LEADING, TitledBorder.TOP, fontBoldTitleBorder, null)); //$NON-NLS-1$ //$NON-NLS-2$
			jScrollPanePathClosedNote.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPanePathClosedNote.setViewportView(getJTextAreaPathClosedNote());
		}
		return jScrollPanePathClosedNote;
	}

	private JLabel getJLabelPathClosedDiseases() {
		if (jLabelPathClosedDiseases == null) {
			jLabelPathClosedDiseases = new JLabel(MessageBundle.getMessage("angal.anamnesis.closed.diseases.txt")); //$NON-NLS-1$
			jLabelPathClosedDiseases.setFont(fontDiseases);
			jLabelPathClosedDiseases.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return jLabelPathClosedDiseases;
	}

	private JCheckBox getJCheckBoxPathClosedOther() {
		if (jCheckBoxPathClosedOther == null) {
			jCheckBoxPathClosedOther = new JCheckBox();
			jCheckBoxPathClosedOther.setAction(getActionExcludePathClosedNothing());
			jCheckBoxPathClosedOther.setText(MessageBundle.getMessage("angal.anamnesis.closed.other.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathClosedOther;
	}

	private JCheckBox getJCheckBoxPathClosedGyno() {
		if (jCheckBoxPathClosedGyno == null) {
			jCheckBoxPathClosedGyno = new JCheckBox();
			jCheckBoxPathClosedGyno.setAction(getActionExcludePathClosedNothing());
			jCheckBoxPathClosedGyno.setText(MessageBundle.getMessage("angal.anamnesis.closed.gyno.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathClosedGyno;
	}

	private JCheckBox getJCheckBoxPathClosedOrto() {
		if (jCheckBoxPathClosedOrto == null) {
			jCheckBoxPathClosedOrto = new JCheckBox();
			jCheckBoxPathClosedOrto.setAction(getActionExcludePathClosedNothing());
			jCheckBoxPathClosedOrto.setText(MessageBundle.getMessage("angal.anamnesis.closed.orto.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathClosedOrto;
	}

	private JCheckBox getJCheckBoxPathClosedCancer() {
		if (jCheckBoxPathClosedCancer == null) {
			jCheckBoxPathClosedCancer = new JCheckBox();
			jCheckBoxPathClosedCancer.setAction(getActionExcludePathClosedNothing());
			jCheckBoxPathClosedCancer.setText(MessageBundle.getMessage("angal.anamnesis.closed.neoplastic.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathClosedCancer;
	}

	private JCheckBox getJCheckBoxPathClosedRespiratory() {
		if (jCheckBoxPathClosedRespiratory == null) {
			jCheckBoxPathClosedRespiratory = new JCheckBox();
			jCheckBoxPathClosedRespiratory.setAction(getActionExcludePathClosedNothing());
			jCheckBoxPathClosedRespiratory.setText(MessageBundle.getMessage("angal.anamnesis.closed.respiratory.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathClosedRespiratory;
	}

	private JCheckBox getJCheckBoxPathClosedEndo() {
		if (jCheckBoxPathClosedEndo == null) {
			jCheckBoxPathClosedEndo = new JCheckBox();
			jCheckBoxPathClosedEndo.setAction(getActionExcludePathClosedNothing());
			jCheckBoxPathClosedEndo.setText(MessageBundle.getMessage("angal.anamnesis.closed.endocrinometabol.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathClosedEndo;
	}

	private JCheckBox getJCheckBoxPathClosedInfective() {
		if (jCheckBoxPathClosedInfective == null) {
			jCheckBoxPathClosedInfective = new JCheckBox();
			jCheckBoxPathClosedInfective.setAction(getActionExcludePathClosedNothing());
			jCheckBoxPathClosedInfective.setText(MessageBundle.getMessage("angal.anamnesis.closed.infective.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathClosedInfective;
	}

	private JCheckBox getJCheckBoxPathClosedCardio() {
		if (jCheckBoxPathClosedCardio == null) {
			jCheckBoxPathClosedCardio = new JCheckBox();
			jCheckBoxPathClosedCardio.setAction(getActionExcludePathClosedNothing());
			jCheckBoxPathClosedCardio.setText(MessageBundle.getMessage("angal.anamnesis.closed.cardiovascolaris.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathClosedCardio;
	}

	private JCheckBox getJCheckBoxPathClosedDrugsAddiction() {
		if (jCheckBoxPathClosedDrugsAddiction == null) {
			jCheckBoxPathClosedDrugsAddiction = new JCheckBox();
			jCheckBoxPathClosedDrugsAddiction.setAction(getActionExcludePathClosedNothing());
			jCheckBoxPathClosedDrugsAddiction.setText(MessageBundle.getMessage("angal.anamnesis.closed.drugsaddiction.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathClosedDrugsAddiction;
	}

	private JCheckBox getJCheckBoxPathClosedHypertension() {
		if (jCheckBoxPathClosedHypertension == null) {
			jCheckBoxPathClosedHypertension = new JCheckBox();
			jCheckBoxPathClosedHypertension.setAction(getActionExcludePathClosedNothing());
			jCheckBoxPathClosedHypertension.setText(MessageBundle.getMessage("angal.anamnesis.closed.hypertension.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathClosedHypertension;
	}

	private JCheckBox getJCheckBoxPathClosedNothing() {
		if (jCheckBoxPathClosedNothing == null) {
			jCheckBoxPathClosedNothing = new JCheckBox();
			jCheckBoxPathClosedNothing.setAction(getActionResetPathClosed());
			jCheckBoxPathClosedNothing.setText(MessageBundle.getMessage("angal.anamnesis.closed.nothingtodeclare.txt")); //$NON-NLS-1$
		}
		return jCheckBoxPathClosedNothing;
	}

	private VoLimitedTextArea getJTextAreaFamilyNote() {
		if (jTextAreaFamilyNote == null) {
			jTextAreaFamilyNote = new VoLimitedTextArea(100, 2, 20);
			jTextAreaFamilyNote.setLineWrap(true);
			jTextAreaFamilyNote.setWrapStyleWord(true);
			jTextAreaFamilyNote.setMargin(new Insets(0, 5, 0, 0));
			jTextAreaFamilyNote.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					path.setFamilyNote(jTextAreaFamilyNote.getText());
				}
			});
		}
		return jTextAreaFamilyNote;
	}

	private JScrollPane getJScrollPaneFamilyNote() {
		if (jScrollPaneFamilyNote == null) {
			jScrollPaneFamilyNote = new JScrollPane();
			jScrollPaneFamilyNote.setAlignmentX(Component.LEFT_ALIGNMENT);
			jScrollPaneFamilyNote.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), MessageBundle.getMessage("angal.anamnesis.family.remarks.border"), TitledBorder.LEADING, TitledBorder.TOP, fontBoldTitleBorder, null)); //$NON-NLS-1$ //$NON-NLS-2$
			jScrollPaneFamilyNote.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPaneFamilyNote.setViewportView(getJTextAreaFamilyNote());
		}
		return jScrollPaneFamilyNote;
	}

	private JCheckBox getJCheckBoxFamilyOther() {
		if (jCheckBoxFamilyOther == null) {
			jCheckBoxFamilyOther = new JCheckBox();
			jCheckBoxFamilyOther.setAction(getActionExludeFamilyNothing());
			jCheckBoxFamilyOther.setText(MessageBundle.getMessage("angal.anamnesis.family.other.txt")); //$NON-NLS-1$
		}
		return jCheckBoxFamilyOther;
	}

	private JCheckBox getJCheckBoxFamilyGyno() {
		if (jCheckBoxFamilyGyno == null) {
			jCheckBoxFamilyGyno = new JCheckBox();
			jCheckBoxFamilyGyno.setAction(getActionExludeFamilyNothing());
			jCheckBoxFamilyGyno.setText(MessageBundle.getMessage("angal.anamnesis.family.gyno.txt")); //$NON-NLS-1$
		}
		return jCheckBoxFamilyGyno;
	}

	private JCheckBox getJCheckBoxFamilyOrto() {
		if (jCheckBoxFamilyOrto == null) {
			jCheckBoxFamilyOrto = new JCheckBox();
			jCheckBoxFamilyOrto.setAction(getActionExludeFamilyNothing());
			jCheckBoxFamilyOrto.setText(MessageBundle.getMessage("angal.anamnesis.family.orto.txt")); //$NON-NLS-1$
		}
		return jCheckBoxFamilyOrto;
	}

	private JCheckBox getJCheckBoxFamilyCancer() {
		if (jCheckBoxFamilyCancer == null) {
			jCheckBoxFamilyCancer = new JCheckBox();
			jCheckBoxFamilyCancer.setAction(getActionExludeFamilyNothing());
			jCheckBoxFamilyCancer.setText(MessageBundle.getMessage("angal.anamnesis.family.neoplastic.txt")); //$NON-NLS-1$
		}
		return jCheckBoxFamilyCancer;
	}

	private JCheckBox getJCheckBoxFamilyRespiratory() {
		if (jCheckBoxFamilyRespiratory == null) {
			jCheckBoxFamilyRespiratory = new JCheckBox();
			jCheckBoxFamilyRespiratory.setAction(getActionExludeFamilyNothing());
			jCheckBoxFamilyRespiratory.setText(MessageBundle.getMessage("angal.anamnesis.family.respiratory.txt")); //$NON-NLS-1$
		}
		return jCheckBoxFamilyRespiratory;
	}

	private JCheckBox getJCheckBoxFamilyEndo() {
		if (jCheckBoxFamilyEndo == null) {
			jCheckBoxFamilyEndo = new JCheckBox();
			jCheckBoxFamilyEndo.setAction(getActionExludeFamilyNothing());
			jCheckBoxFamilyEndo.setText(MessageBundle.getMessage("angal.anamnesis.family.endocrinometabol.txt")); //$NON-NLS-1$
		}
		return jCheckBoxFamilyEndo;
	}

	private JCheckBox getJCheckBoxFamilyInfective() {
		if (jCheckBoxFamilyInfective == null) {
			jCheckBoxFamilyInfective = new JCheckBox();
			jCheckBoxFamilyInfective.setAction(getActionExludeFamilyNothing());
			jCheckBoxFamilyInfective.setText(MessageBundle.getMessage("angal.anamnesis.family.infective.txt")); //$NON-NLS-1$
		}
		return jCheckBoxFamilyInfective;
	}

	private JCheckBox getJCheckBoxFamilyCardio() {
		if (jCheckBoxFamilyCardio == null) {
			jCheckBoxFamilyCardio = new JCheckBox();
			jCheckBoxFamilyCardio.setAction(getActionExludeFamilyNothing());
			jCheckBoxFamilyCardio.setText(MessageBundle.getMessage("angal.anamnesis.family.cardiovascolaris.txt")); //$NON-NLS-1$
		}
		return jCheckBoxFamilyCardio;
	}

	private JLabel getJLabelFamilyDiseases() {
		if (jLabelFamilyDiseases == null) {
			jLabelFamilyDiseases = new JLabel(MessageBundle.getMessage("angal.anamnesis.family.diseases.txt")); //$NON-NLS-1$
			jLabelFamilyDiseases.setFont(fontDiseases);
		}
		return jLabelFamilyDiseases;
	}

	private JCheckBox getJCheckBoxFamilyDrugsAddiction() {
		if (jCheckBoxFamilyDrugsAddiction == null) {
			jCheckBoxFamilyDrugsAddiction = new JCheckBox();
			jCheckBoxFamilyDrugsAddiction.setAction(getActionExludeFamilyNothing());
			jCheckBoxFamilyDrugsAddiction.setText(MessageBundle.getMessage("angal.anamnesis.family.drugsaddiction.txt")); //$NON-NLS-1$
		}
		return jCheckBoxFamilyDrugsAddiction;
	}

	private JCheckBox getJCheckBoxFamilyHypertension() {
		if (jCheckBoxFamilyHypertension == null) {
			jCheckBoxFamilyHypertension = new JCheckBox();
			jCheckBoxFamilyHypertension.setAction(getActionExludeFamilyNothing());
			jCheckBoxFamilyHypertension.setText(MessageBundle.getMessage("angal.anamnesis.family.hypertension.txt")); //$NON-NLS-1$
		}
		return jCheckBoxFamilyHypertension;
	}

	private JCheckBox getJCheckBoxFamilyNothing() {
		if (jCheckBoxFamilyNothing == null) {
			jCheckBoxFamilyNothing = new JCheckBox();
			jCheckBoxFamilyNothing.setAction(getActionResetFamilyHistory());
			jCheckBoxFamilyNothing.setText(MessageBundle.getMessage("angal.anamnesis.family.nothingtodeclare.txt")); //$NON-NLS-1$
		}
		return jCheckBoxFamilyNothing;
	}

	private JLabel getJLabelPatIDText() {
		if (jLabelPatIDText == null) {
			jLabelPatIDText = new JLabel();
			jLabelPatIDText.setFont(new Font("Tahoma", Font.BOLD, 14)); //$NON-NLS-1$
			jLabelPatIDText.setBackground(Color.WHITE);
			jLabelPatIDText.setOpaque(true);
			jLabelPatIDText.setText(""); //$NON-NLS-1$
		}
		return jLabelPatIDText;
	}

	private JLabel getJLabelPatID() {
		if (jLabelPatID == null) {
			jLabelPatID = new JLabel();
			jLabelPatID.setFont(new Font("Tahoma", Font.PLAIN, 14)); //$NON-NLS-1$
			jLabelPatID.setText(MessageBundle.getMessage("angal.anamnesis.patid.label")); //$NON-NLS-1$
		}
		return jLabelPatID;
	}

	private JLabel getJLabelSecondNameText() {
		if (jLabelSecondNameText == null) {
			jLabelSecondNameText = new JLabel();
			jLabelSecondNameText.setFont(new Font("Tahoma", Font.BOLD, 14)); //$NON-NLS-1$
			jLabelSecondNameText.setBackground(Color.WHITE);
			jLabelSecondNameText.setOpaque(true);
			jLabelSecondNameText.setText(""); //$NON-NLS-1$
		}
		return jLabelSecondNameText;
	}

	private JLabel getJLabelSecondName() {
		if (jLabelSecondName == null) {
			jLabelSecondName = new JLabel();
			jLabelSecondName.setFont(new Font("Tahoma", Font.PLAIN, 14)); //$NON-NLS-1$
			jLabelSecondName.setText(MessageBundle.getMessage("angal.anamnesis.secondname.label")); //$NON-NLS-1$
		}
		return jLabelSecondName;
	}

	private JLabel getJLabelFirstNameText() {
		if (jLabelFirstNameText == null) {
			jLabelFirstNameText = new JLabel();
			jLabelFirstNameText.setFont(new Font("Tahoma", Font.BOLD, 14)); //$NON-NLS-1$
			jLabelFirstNameText.setBackground(Color.WHITE);
			jLabelFirstNameText.setOpaque(true);
			jLabelFirstNameText.setText("from DB"); //$NON-NLS-1$
			jLabelFirstNameText.setMinimumSize(new Dimension(100, 40));
		}
		return jLabelFirstNameText;
	}

	private JLabel getJLabelFirstName() {
		if (jLabelFirstName == null) {
			jLabelFirstName = new JLabel();
			jLabelFirstName.setFont(new Font("Tahoma", Font.PLAIN, 14)); //$NON-NLS-1$
			jLabelFirstName.setText(MessageBundle.getMessage("angal.anamnesis.firstname.label")); //$NON-NLS-1$
		}
		return jLabelFirstName;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			jPanelButtons.add(getJButtonSave());
//			jPanelButtons.add(getJButtonExamination());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
	}

	private JPanel getJPanelPhyHistoryUnisex() {
		if (jPanelPhyHistoryUnisex == null) {
			jPanelPhyHistoryUnisex = new JPanel();
			jPanelPhyHistoryUnisex.setBorder(null);
			GridBagLayout gbl_jPanelPhyHistoryUnisex = new GridBagLayout();
			gbl_jPanelPhyHistoryUnisex.columnWidths = new int[] { 0, 0, 0, 0 };
			gbl_jPanelPhyHistoryUnisex.rowHeights = new int[] { 20, 20, 20, 20, 20, 20 };
			gbl_jPanelPhyHistoryUnisex.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0 };
			gbl_jPanelPhyHistoryUnisex.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
			jPanelPhyHistoryUnisex.setLayout(gbl_jPanelPhyHistoryUnisex);
			// COLONNA '0'
			GridBagConstraints gbc_jLabelPhyDiet = new GridBagConstraints();
			gbc_jLabelPhyDiet.anchor = GridBagConstraints.WEST;
			gbc_jLabelPhyDiet.insets = new Insets(0, 5, 0, 5);
			gbc_jLabelPhyDiet.gridx = 0;
			gbc_jLabelPhyDiet.gridy = 0;
			jPanelPhyHistoryUnisex.add(getJLabelPhyDiet(), gbc_jLabelPhyDiet);
			GridBagConstraints gbc_jLabelPhyAlvo = new GridBagConstraints();
			gbc_jLabelPhyAlvo.anchor = GridBagConstraints.WEST;
			gbc_jLabelPhyAlvo.insets = new Insets(0, 5, 0, 5);
			gbc_jLabelPhyAlvo.gridx = 0;
			gbc_jLabelPhyAlvo.gridy = 1;
			jPanelPhyHistoryUnisex.add(getJLabelPhyAlvo(), gbc_jLabelPhyAlvo);
			GridBagConstraints gbc_jLabelPhyDiuresis = new GridBagConstraints();
			gbc_jLabelPhyDiuresis.anchor = GridBagConstraints.WEST;
			gbc_jLabelPhyDiuresis.insets = new Insets(0, 5, 0, 5);
			gbc_jLabelPhyDiuresis.gridx = 0;
			gbc_jLabelPhyDiuresis.gridy = 2;
			jPanelPhyHistoryUnisex.add(getJLabelPhyDiuresis(), gbc_jLabelPhyDiuresis);
			GridBagConstraints gbc_jLabelPhyAlcool = new GridBagConstraints();
			gbc_jLabelPhyAlcool.anchor = GridBagConstraints.WEST;
			gbc_jLabelPhyAlcool.insets = new Insets(0, 5, 0, 5);
			gbc_jLabelPhyAlcool.gridx = 0;
			gbc_jLabelPhyAlcool.gridy = 3;
			jPanelPhyHistoryUnisex.add(getJLabelPhyAlcool(), gbc_jLabelPhyAlcool);
			GridBagConstraints gbc_jLabelPhySmoke = new GridBagConstraints();
			gbc_jLabelPhySmoke.anchor = GridBagConstraints.WEST;
			gbc_jLabelPhySmoke.insets = new Insets(0, 5, 0, 5);
			gbc_jLabelPhySmoke.gridx = 0;
			gbc_jLabelPhySmoke.gridy = 4;
			jPanelPhyHistoryUnisex.add(getJLabelPhySmoke(), gbc_jLabelPhySmoke);
			GridBagConstraints gbc_jLabelPhyDrugs = new GridBagConstraints();
			gbc_jLabelPhyDrugs.anchor = GridBagConstraints.WEST;
			gbc_jLabelPhyDrugs.insets = new Insets(0, 5, 0, 5);
			gbc_jLabelPhyDrugs.gridx = 0;
			gbc_jLabelPhyDrugs.gridy = 5;
			jPanelPhyHistoryUnisex.add(getJLabelPhyDrugs(), gbc_jLabelPhyDrugs);
			// COLONNA '1'
			GridBagConstraints gbc_jCheckBoxPhyDietNormal = new GridBagConstraints();
			gbc_jCheckBoxPhyDietNormal.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyDietNormal.gridx = 1;
			gbc_jCheckBoxPhyDietNormal.gridy = 0;
			jPanelPhyHistoryUnisex.add(getJCheckBoxPhyDietNormal(), gbc_jCheckBoxPhyDietNormal);
			GridBagConstraints gbc_jCheckBoxPhyAlvoNormal = new GridBagConstraints();
			gbc_jCheckBoxPhyAlvoNormal.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyAlvoNormal.gridx = 1;
			gbc_jCheckBoxPhyAlvoNormal.gridy = 1;
			jPanelPhyHistoryUnisex.add(getJCheckBoxPhyAlvoNormal(), gbc_jCheckBoxPhyAlvoNormal);
			GridBagConstraints gbc_jCheckBoxPhyDiuresisNormal = new GridBagConstraints();
			gbc_jCheckBoxPhyDiuresisNormal.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyDiuresisNormal.gridx = 1;
			gbc_jCheckBoxPhyDiuresisNormal.gridy = 2;
			jPanelPhyHistoryUnisex.add(getJCheckBoxPhyDiuresisNormal(), gbc_jCheckBoxPhyDiuresisNormal);
			GridBagConstraints gbc_jCheckBoxPhyAlcoolNo = new GridBagConstraints();
			gbc_jCheckBoxPhyAlcoolNo.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPhyAlcoolNo.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyAlcoolNo.gridx = 1;
			gbc_jCheckBoxPhyAlcoolNo.gridy = 3;
			jPanelPhyHistoryUnisex.add(getJCheckBoxPhyAlcoolNo(), gbc_jCheckBoxPhyAlcoolNo);
			GridBagConstraints gbc_jCheckBoxPhySmokeNo = new GridBagConstraints();
			gbc_jCheckBoxPhySmokeNo.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPhySmokeNo.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhySmokeNo.gridx = 1;
			gbc_jCheckBoxPhySmokeNo.gridy = 4;
			jPanelPhyHistoryUnisex.add(getJCheckBoxPhySmokeNo(), gbc_jCheckBoxPhySmokeNo);
			GridBagConstraints gbc_jCheckBoxPhyDrugsNo = new GridBagConstraints();
			gbc_jCheckBoxPhyDrugsNo.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPhyDrugsNo.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyDrugsNo.gridx = 1;
			gbc_jCheckBoxPhyDrugsNo.gridy = 5;
			jPanelPhyHistoryUnisex.add(getJCheckBoxPhyDrugsNo(), gbc_jCheckBoxPhyDrugsNo);
			// COLONNA '2'
			GridBagConstraints gbc_jCheckBoxPhyDietAbnormal = new GridBagConstraints();
			gbc_jCheckBoxPhyDietAbnormal.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyDietAbnormal.gridx = 2;
			gbc_jCheckBoxPhyDietAbnormal.gridy = 0;
			jPanelPhyHistoryUnisex.add(getJCheckBoxPhyDietAbnormal(), gbc_jCheckBoxPhyDietAbnormal);
			GridBagConstraints gbc_jCheckBoxPhyAlvoAbnormal = new GridBagConstraints();
			gbc_jCheckBoxPhyAlvoAbnormal.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyAlvoAbnormal.gridx = 2;
			gbc_jCheckBoxPhyAlvoAbnormal.gridy = 1;
			jPanelPhyHistoryUnisex.add(getJCheckBoxPhyAlvoAbnormal(), gbc_jCheckBoxPhyAlvoAbnormal);
			GridBagConstraints gbc_jCheckBoxPhyDiuresisAbnormal = new GridBagConstraints();
			gbc_jCheckBoxPhyDiuresisAbnormal.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyDiuresisAbnormal.gridx = 2;
			gbc_jCheckBoxPhyDiuresisAbnormal.gridy = 2;
			jPanelPhyHistoryUnisex.add(getJCheckBoxPhyDiuresisAbnormal(), gbc_jCheckBoxPhyDiuresisAbnormal);
			GridBagConstraints gbc_jCheckBoxPhyAlcoolYes = new GridBagConstraints();
			gbc_jCheckBoxPhyAlcoolYes.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPhyAlcoolYes.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyAlcoolYes.gridx = 2;
			gbc_jCheckBoxPhyAlcoolYes.gridy = 3;
			jPanelPhyHistoryUnisex.add(getJCheckBoxPhyAlcoolYes(), gbc_jCheckBoxPhyAlcoolYes);
			GridBagConstraints gbc_jCheckBoxPhySmokeYes = new GridBagConstraints();
			gbc_jCheckBoxPhySmokeYes.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPhySmokeYes.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhySmokeYes.gridx = 2;
			gbc_jCheckBoxPhySmokeYes.gridy = 4;
			jPanelPhyHistoryUnisex.add(getJCheckBoxPhySmokeYes(), gbc_jCheckBoxPhySmokeYes);
			GridBagConstraints gbc_jCheckBoxPhyDrugsYes = new GridBagConstraints();
			gbc_jCheckBoxPhyDrugsYes.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPhyDrugsYes.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyDrugsYes.gridx = 2;
			gbc_jCheckBoxPhyDrugsYes.gridy = 5;
			jPanelPhyHistoryUnisex.add(getJCheckBoxPhyDrugsYes(), gbc_jCheckBoxPhyDrugsYes);
			// COLONNA '3'
			GridBagConstraints gbc_jTextFieldPhyDietAbnormalText = new GridBagConstraints();
			gbc_jTextFieldPhyDietAbnormalText.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldPhyDietAbnormalText.insets = new Insets(0, 5, 0, 5);
			gbc_jTextFieldPhyDietAbnormalText.gridx = 3;
			gbc_jTextFieldPhyDietAbnormalText.gridy = 0;
			jPanelPhyHistoryUnisex.add(getJTextFieldPhyDietAbnormalText(), gbc_jTextFieldPhyDietAbnormalText);
			GridBagConstraints gbc_jTextFieldPhyAlvoAbnormalText = new GridBagConstraints();
			gbc_jTextFieldPhyAlvoAbnormalText.insets = new Insets(0, 5, 0, 5);
			gbc_jTextFieldPhyAlvoAbnormalText.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldPhyAlvoAbnormalText.gridx = 3;
			gbc_jTextFieldPhyAlvoAbnormalText.gridy = 1;
			jPanelPhyHistoryUnisex.add(getJTextFieldPhyAlvoAbnormalText(), gbc_jTextFieldPhyAlvoAbnormalText);
			GridBagConstraints gbc_jTextFieldPhyDiuresisAbnormalText = new GridBagConstraints();
			gbc_jTextFieldPhyDiuresisAbnormalText.insets = new Insets(0, 5, 0, 5);
			gbc_jTextFieldPhyDiuresisAbnormalText.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldPhyDiuresisAbnormalText.gridx = 3;
			gbc_jTextFieldPhyDiuresisAbnormalText.gridy = 2;
			jPanelPhyHistoryUnisex.add(getJTextFieldPhyDiuresisAbnormalText(), gbc_jTextFieldPhyDiuresisAbnormalText);
		}
		return jPanelPhyHistoryUnisex;
	}

	private JPanel getJPanelPathologicalExtra() {
		if (jPanelPathologicalExtra == null) {
			jPanelPathologicalExtra = new JPanel();
			jPanelPathologicalExtra.setBorder(null);
			jPanelPathologicalExtra.setLayout(new BoxLayout(jPanelPathologicalExtra, BoxLayout.Y_AXIS));
			jPanelPathologicalExtra.add(getJScrollPanePathExtraSurgery());
			jPanelPathologicalExtra.add(Box.createVerticalStrut(5));
			jPanelPathologicalExtra.add(getJScrollPanePathExtraAllergy());
			jPanelPathologicalExtra.add(Box.createVerticalStrut(5));
			jPanelPathologicalExtra.add(getJScrollPanePathExtraTherapy());
			jPanelPathologicalExtra.add(Box.createVerticalStrut(5));
			jPanelPathologicalExtra.add(getJScrollPanePathExtraUsualMedicines());
			jPanelPathologicalExtra.add(Box.createVerticalStrut(5));
			jPanelPathologicalExtra.add(getJScrollPanePathExtraNote());
		}
		return jPanelPathologicalExtra;
	}


	private JPanel getJPanelPathologicalOpen() {
		if (jPanelPathologicalOpen == null) {
			jPanelPathologicalOpen = new JPanel();
			jPanelPathologicalOpen.setBorder(new TitledBorder(null, MessageBundle.getMessage("angal.anamnesis.open.recentproblems.border"), TitledBorder.CENTER, TitledBorder.TOP, fontBoldTitleBorder, null)); //$NON-NLS-1$
			GridBagLayout gbl_jPanelPathologicalOpen = new GridBagLayout();
			gbl_jPanelPathologicalOpen.columnWidths = new int[] { 0 };
			gbl_jPanelPathologicalOpen.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			gbl_jPanelPathologicalOpen.columnWeights = new double[] { 1.0 };
			gbl_jPanelPathologicalOpen.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
			jPanelPathologicalOpen.setLayout(gbl_jPanelPathologicalOpen);
			GridBagConstraints gbc_jCheckBoxPathOpenNothing = new GridBagConstraints();
			gbc_jCheckBoxPathOpenNothing.fill = GridBagConstraints.BOTH;
			gbc_jCheckBoxPathOpenNothing.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathOpenNothing.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathOpenNothing.gridx = 0;
			gbc_jCheckBoxPathOpenNothing.gridy = 0;
			jPanelPathologicalOpen.add(getJCheckBoxPathOpenNothing(), gbc_jCheckBoxPathOpenNothing);
			GridBagConstraints gbc_jCheckBoxPathOpenHypertension = new GridBagConstraints();
			gbc_jCheckBoxPathOpenHypertension.fill = GridBagConstraints.HORIZONTAL;
			gbc_jCheckBoxPathOpenHypertension.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathOpenHypertension.gridx = 0;
			gbc_jCheckBoxPathOpenHypertension.gridy = 1;
			jPanelPathologicalOpen.add(getJCheckBoxPathOpenHypertension(), gbc_jCheckBoxPathOpenHypertension);
			GridBagConstraints gbc_jCheckBoxPathOpenDrugsAddiction = new GridBagConstraints();
			gbc_jCheckBoxPathOpenDrugsAddiction.fill = GridBagConstraints.HORIZONTAL;
			gbc_jCheckBoxPathOpenDrugsAddiction.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathOpenDrugsAddiction.gridx = 0;
			gbc_jCheckBoxPathOpenDrugsAddiction.gridy = 2;
			jPanelPathologicalOpen.add(getJCheckBoxPathOpenDrugsAddiction(), gbc_jCheckBoxPathOpenDrugsAddiction);
			GridBagConstraints gbc_jLabelPathOpenDiseases = new GridBagConstraints();
			gbc_jLabelPathOpenDiseases.insets = new Insets(5, 0, 0, 0);
			gbc_jLabelPathOpenDiseases.gridx = 0;
			gbc_jLabelPathOpenDiseases.gridy = 3;
			jPanelPathologicalOpen.add(getJLabelPathOpenDiseases(), gbc_jLabelPathOpenDiseases);
			GridBagConstraints gbc_jCheckBoxPathOpenCardio = new GridBagConstraints();
			gbc_jCheckBoxPathOpenCardio.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathOpenCardio.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathOpenCardio.gridx = 0;
			gbc_jCheckBoxPathOpenCardio.gridy = 4;
			jPanelPathologicalOpen.add(getJCheckBoxPathOpenCardio(), gbc_jCheckBoxPathOpenCardio);
			GridBagConstraints gbc_jCheckBoxPathOpenInfective = new GridBagConstraints();
			gbc_jCheckBoxPathOpenInfective.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathOpenInfective.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathOpenInfective.gridx = 0;
			gbc_jCheckBoxPathOpenInfective.gridy = 5;
			jPanelPathologicalOpen.add(getJCheckBoxPathOpenInfective(), gbc_jCheckBoxPathOpenInfective);
			GridBagConstraints gbc_jCheckBoxPathOpenEndo = new GridBagConstraints();
			gbc_jCheckBoxPathOpenEndo.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathOpenEndo.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathOpenEndo.gridx = 0;
			gbc_jCheckBoxPathOpenEndo.gridy = 6;
			jPanelPathologicalOpen.add(getJCheckBoxPathOpenEndo(), gbc_jCheckBoxPathOpenEndo);
			GridBagConstraints gbc_jCheckBoxPathOpenRespiratory = new GridBagConstraints();
			gbc_jCheckBoxPathOpenRespiratory.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathOpenRespiratory.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathOpenRespiratory.gridx = 0;
			gbc_jCheckBoxPathOpenRespiratory.gridy = 7;
			jPanelPathologicalOpen.add(getJCheckBoxPathOpenRespiratory(), gbc_jCheckBoxPathOpenRespiratory);
			GridBagConstraints gbc_jCheckBoxPathOpenCancer = new GridBagConstraints();
			gbc_jCheckBoxPathOpenCancer.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathOpenCancer.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathOpenCancer.gridx = 0;
			gbc_jCheckBoxPathOpenCancer.gridy = 8;
			jPanelPathologicalOpen.add(getJCheckBoxPathOpenCancer(), gbc_jCheckBoxPathOpenCancer);
			GridBagConstraints gbc_jCheckBoxPathOpenOrto = new GridBagConstraints();
			gbc_jCheckBoxPathOpenOrto.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathOpenOrto.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathOpenOrto.gridx = 0;
			gbc_jCheckBoxPathOpenOrto.gridy = 9;
			jPanelPathologicalOpen.add(getJCheckBoxPathOpenOrto(), gbc_jCheckBoxPathOpenOrto);
			GridBagConstraints gbc_jCheckBoxPathOpenGyno = new GridBagConstraints();
			gbc_jCheckBoxPathOpenGyno.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathOpenGyno.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathOpenGyno.gridx = 0;
			gbc_jCheckBoxPathOpenGyno.gridy = 10;
			jPanelPathologicalOpen.add(getJCheckBoxPathOpenGyno(), gbc_jCheckBoxPathOpenGyno);
			GridBagConstraints gbc_jCheckBoxPathOpenOther = new GridBagConstraints();
			gbc_jCheckBoxPathOpenOther.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathOpenOther.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathOpenOther.gridx = 0;
			gbc_jCheckBoxPathOpenOther.gridy = 11;
			jPanelPathologicalOpen.add(getJCheckBoxPathOpenOther(), gbc_jCheckBoxPathOpenOther);
			GridBagConstraints gbc_jScrollPanePathOpenNote = new GridBagConstraints();
			gbc_jScrollPanePathOpenNote.insets = new Insets(10, 0, 0, 0);
			gbc_jScrollPanePathOpenNote.fill = GridBagConstraints.BOTH;
			gbc_jScrollPanePathOpenNote.gridx = 0;
			gbc_jScrollPanePathOpenNote.gridy = 12;
			jPanelPathologicalOpen.add(getJScrollPanePathOpenNote(), gbc_jScrollPanePathOpenNote);
		}
		return jPanelPathologicalOpen;
	}

	private JPanel getJPanelPathologicalClosed() {
		if (jPanelPathologicalClosed == null) {
			jPanelPathologicalClosed = new JPanel();
			jPanelPathologicalClosed.setBorder(new TitledBorder(null, MessageBundle.getMessage("angal.anamnesis.closed.pastproblems.border"), TitledBorder.CENTER, TitledBorder.TOP, fontBoldTitleBorder, null)); //$NON-NLS-1$
			GridBagLayout gbl_jPanelPathologicalClosed = new GridBagLayout();
			gbl_jPanelPathologicalClosed.columnWidths = new int[] { 0 };
			gbl_jPanelPathologicalClosed.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			gbl_jPanelPathologicalClosed.columnWeights = new double[] { 1.0 };
			gbl_jPanelPathologicalClosed.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
			jPanelPathologicalClosed.setLayout(gbl_jPanelPathologicalClosed);
			GridBagConstraints gbc_jCheckBoxPathClosedNothing = new GridBagConstraints();
			gbc_jCheckBoxPathClosedNothing.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathClosedNothing.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathClosedNothing.gridx = 0;
			gbc_jCheckBoxPathClosedNothing.gridy = 0;
			jPanelPathologicalClosed.add(getJCheckBoxPathClosedNothing(), gbc_jCheckBoxPathClosedNothing);
			GridBagConstraints gbc_jCheckBoxPathClosedHypertension = new GridBagConstraints();
			gbc_jCheckBoxPathClosedHypertension.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathClosedHypertension.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathClosedHypertension.gridx = 0;
			gbc_jCheckBoxPathClosedHypertension.gridy = 1;
			jPanelPathologicalClosed.add(getJCheckBoxPathClosedHypertension(), gbc_jCheckBoxPathClosedHypertension);
			GridBagConstraints gbc_jCheckBoxPathClosedDrugsAddiction = new GridBagConstraints();
			gbc_jCheckBoxPathClosedDrugsAddiction.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathClosedDrugsAddiction.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathClosedDrugsAddiction.gridx = 0;
			gbc_jCheckBoxPathClosedDrugsAddiction.gridy = 2;
			jPanelPathologicalClosed.add(getJCheckBoxPathClosedDrugsAddiction(), gbc_jCheckBoxPathClosedDrugsAddiction);
			GridBagConstraints gbc_jLabelPathClosedDiseases = new GridBagConstraints();
			gbc_jLabelPathClosedDiseases.insets = new Insets(5, 0, 0, 0);
			gbc_jLabelPathClosedDiseases.gridx = 0;
			gbc_jLabelPathClosedDiseases.gridy = 3;
			jPanelPathologicalClosed.add(getJLabelPathClosedDiseases(), gbc_jLabelPathClosedDiseases);
			GridBagConstraints gbc_jCheckBoxPathClosedCardio = new GridBagConstraints();
			gbc_jCheckBoxPathClosedCardio.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathClosedCardio.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathClosedCardio.gridx = 0;
			gbc_jCheckBoxPathClosedCardio.gridy = 4;
			jPanelPathologicalClosed.add(getJCheckBoxPathClosedCardio(), gbc_jCheckBoxPathClosedCardio);
			GridBagConstraints gbc_jCheckBoxPathClosedInfective = new GridBagConstraints();
			gbc_jCheckBoxPathClosedInfective.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathClosedInfective.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathClosedInfective.gridx = 0;
			gbc_jCheckBoxPathClosedInfective.gridy = 5;
			jPanelPathologicalClosed.add(getJCheckBoxPathClosedInfective(), gbc_jCheckBoxPathClosedInfective);
			GridBagConstraints gbc_jCheckBoxPathClosedEndo = new GridBagConstraints();
			gbc_jCheckBoxPathClosedEndo.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathClosedEndo.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathClosedEndo.gridx = 0;
			gbc_jCheckBoxPathClosedEndo.gridy = 6;
			jPanelPathologicalClosed.add(getJCheckBoxPathClosedEndo(), gbc_jCheckBoxPathClosedEndo);
			GridBagConstraints gbc_jCheckBoxPathClosedRespiratory = new GridBagConstraints();
			gbc_jCheckBoxPathClosedRespiratory.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathClosedRespiratory.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathClosedRespiratory.gridx = 0;
			gbc_jCheckBoxPathClosedRespiratory.gridy = 7;
			jPanelPathologicalClosed.add(getJCheckBoxPathClosedRespiratory(), gbc_jCheckBoxPathClosedRespiratory);
			GridBagConstraints gbc_jCheckBoxPathClosedCancer = new GridBagConstraints();
			gbc_jCheckBoxPathClosedCancer.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathClosedCancer.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathClosedCancer.gridx = 0;
			gbc_jCheckBoxPathClosedCancer.gridy = 8;
			jPanelPathologicalClosed.add(getJCheckBoxPathClosedCancer(), gbc_jCheckBoxPathClosedCancer);
			GridBagConstraints gbc_jCheckBoxPathClosedOrto = new GridBagConstraints();
			gbc_jCheckBoxPathClosedOrto.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathClosedOrto.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathClosedOrto.gridx = 0;
			gbc_jCheckBoxPathClosedOrto.gridy = 9;
			jPanelPathologicalClosed.add(getJCheckBoxPathClosedOrto(), gbc_jCheckBoxPathClosedOrto);
			GridBagConstraints gbc_jCheckBoxPathClosedGyno = new GridBagConstraints();
			gbc_jCheckBoxPathClosedGyno.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathClosedGyno.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathClosedGyno.gridx = 0;
			gbc_jCheckBoxPathClosedGyno.gridy = 10;
			jPanelPathologicalClosed.add(getJCheckBoxPathClosedGyno(), gbc_jCheckBoxPathClosedGyno);
			GridBagConstraints gbc_jCheckBoxPathClosedOther = new GridBagConstraints();
			gbc_jCheckBoxPathClosedOther.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPathClosedOther.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxPathClosedOther.gridx = 0;
			gbc_jCheckBoxPathClosedOther.gridy = 11;
			jPanelPathologicalClosed.add(getJCheckBoxPathClosedOther(), gbc_jCheckBoxPathClosedOther);
			GridBagConstraints gbc_jScrollPanePathClosedNote = new GridBagConstraints();
			gbc_jScrollPanePathClosedNote.insets = new Insets(10, 0, 0, 0);
			gbc_jScrollPanePathClosedNote.fill = GridBagConstraints.BOTH;
			gbc_jScrollPanePathClosedNote.gridx = 0;
			gbc_jScrollPanePathClosedNote.gridy = 12;
			jPanelPathologicalClosed.add(getJScrollPanePathClosedNote(), gbc_jScrollPanePathClosedNote);
		}
		return jPanelPathologicalClosed;
	}

	private JPanel getJPanelFamily() {
		if (jPanelFamily == null) {
			jPanelFamily = new JPanel();
			jPanelFamily.setBorder(new TitledBorder(null, MessageBundle.getMessage("angal.anamnesis.family.familyhistory.border"), TitledBorder.CENTER, TitledBorder.TOP, fontBoldTitleBorder, null)); //$NON-NLS-1$
			GridBagLayout gbl_jPanelFamily = new GridBagLayout();
			gbl_jPanelFamily.columnWidths = new int[] { 0 };
			gbl_jPanelFamily.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			gbl_jPanelFamily.columnWeights = new double[] { 1.0 };
			gbl_jPanelFamily.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
			jPanelFamily.setLayout(gbl_jPanelFamily);
			GridBagConstraints gbc_jCheckBoxFamilyNothing = new GridBagConstraints();
			gbc_jCheckBoxFamilyNothing.fill = GridBagConstraints.HORIZONTAL;
			gbc_jCheckBoxFamilyNothing.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxFamilyNothing.gridx = 0;
			gbc_jCheckBoxFamilyNothing.gridy = 0;
			jPanelFamily.add(getJCheckBoxFamilyNothing(), gbc_jCheckBoxFamilyNothing);
			GridBagConstraints gbc_jCheckBoxFamilyHypertension = new GridBagConstraints();
			gbc_jCheckBoxFamilyHypertension.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxFamilyHypertension.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxFamilyHypertension.gridx = 0;
			gbc_jCheckBoxFamilyHypertension.gridy = 1;
			jPanelFamily.add(getJCheckBoxFamilyHypertension(), gbc_jCheckBoxFamilyHypertension);
			GridBagConstraints gbc_jCheckBoxFamilyDrugsAddiction = new GridBagConstraints();
			gbc_jCheckBoxFamilyDrugsAddiction.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxFamilyDrugsAddiction.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxFamilyDrugsAddiction.gridx = 0;
			gbc_jCheckBoxFamilyDrugsAddiction.gridy = 2;
			jPanelFamily.add(getJCheckBoxFamilyDrugsAddiction(), gbc_jCheckBoxFamilyDrugsAddiction);
			GridBagConstraints gbc_jLabelFamilyDiseases = new GridBagConstraints();
			gbc_jLabelFamilyDiseases.insets = new Insets(5, 0, 0, 0);
			gbc_jLabelFamilyDiseases.gridx = 0;
			gbc_jLabelFamilyDiseases.gridy = 3;
			jPanelFamily.add(getJLabelFamilyDiseases(), gbc_jLabelFamilyDiseases);
			GridBagConstraints gbc_jCheckBoxFamilyCardio = new GridBagConstraints();
			gbc_jCheckBoxFamilyCardio.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxFamilyCardio.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxFamilyCardio.gridx = 0;
			gbc_jCheckBoxFamilyCardio.gridy = 4;
			jPanelFamily.add(getJCheckBoxFamilyCardio(), gbc_jCheckBoxFamilyCardio);
			GridBagConstraints gbc_jCheckBoxFamilyInfective = new GridBagConstraints();
			gbc_jCheckBoxFamilyInfective.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxFamilyInfective.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxFamilyInfective.gridx = 0;
			gbc_jCheckBoxFamilyInfective.gridy = 5;
			jPanelFamily.add(getJCheckBoxFamilyInfective(), gbc_jCheckBoxFamilyInfective);
			GridBagConstraints gbc_jCheckBoxFamilyEndo = new GridBagConstraints();
			gbc_jCheckBoxFamilyEndo.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxFamilyEndo.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxFamilyEndo.gridx = 0;
			gbc_jCheckBoxFamilyEndo.gridy = 6;
			jPanelFamily.add(getJCheckBoxFamilyEndo(), gbc_jCheckBoxFamilyEndo);
			GridBagConstraints gbc_jCheckBoxFamilyRespiratory = new GridBagConstraints();
			gbc_jCheckBoxFamilyRespiratory.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxFamilyRespiratory.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxFamilyRespiratory.gridx = 0;
			gbc_jCheckBoxFamilyRespiratory.gridy = 7;
			jPanelFamily.add(getJCheckBoxFamilyRespiratory(), gbc_jCheckBoxFamilyRespiratory);
			GridBagConstraints gbc_jCheckBoxFamilyCancer = new GridBagConstraints();
			gbc_jCheckBoxFamilyCancer.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxFamilyCancer.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxFamilyCancer.gridx = 0;
			gbc_jCheckBoxFamilyCancer.gridy = 8;
			jPanelFamily.add(getJCheckBoxFamilyCancer(), gbc_jCheckBoxFamilyCancer);
			GridBagConstraints gbc_jCheckBoxFamilyOrto = new GridBagConstraints();
			gbc_jCheckBoxFamilyOrto.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxFamilyOrto.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxFamilyOrto.gridx = 0;
			gbc_jCheckBoxFamilyOrto.gridy = 9;
			jPanelFamily.add(getJCheckBoxFamilyOrto(), gbc_jCheckBoxFamilyOrto);
			GridBagConstraints gbc_jCheckBoxFamilyGyno = new GridBagConstraints();
			gbc_jCheckBoxFamilyGyno.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxFamilyGyno.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxFamilyGyno.gridx = 0;
			gbc_jCheckBoxFamilyGyno.gridy = 10;
			jPanelFamily.add(getJCheckBoxFamilyGyno(), gbc_jCheckBoxFamilyGyno);
			GridBagConstraints gbc_jCheckBoxFamilyOther = new GridBagConstraints();
			gbc_jCheckBoxFamilyOther.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxFamilyOther.insets = new Insets(0, 0, 0, 0);
			gbc_jCheckBoxFamilyOther.gridx = 0;
			gbc_jCheckBoxFamilyOther.gridy = 11;
			jPanelFamily.add(getJCheckBoxFamilyOther(), gbc_jCheckBoxFamilyOther);
			GridBagConstraints gbc_jScrollPaneFamilyNote = new GridBagConstraints();
			gbc_jScrollPaneFamilyNote.fill = GridBagConstraints.BOTH;
			gbc_jScrollPaneFamilyNote.insets = new Insets(10, 0, 0, 0);
			gbc_jScrollPaneFamilyNote.gridx = 0;
			gbc_jScrollPaneFamilyNote.gridy = 12;
			jPanelFamily.add(getJScrollPaneFamilyNote(), gbc_jScrollPaneFamilyNote);
		}
		return jPanelFamily;
	}

	private JPanel getJPanelAnamnesis() {
		if (jPanelAnamnesis == null) {
			jPanelAnamnesis = new JPanel();
			jPanelAnamnesis.setLayout(new BorderLayout());
			jPanelAnamnesis.add(getJPanelPathological(), BorderLayout.CENTER);
			jPanelAnamnesis.add(getJPanelPhysiologicalHistory(), BorderLayout.SOUTH);
		}
		return jPanelAnamnesis;
	}

	private JPanel getJPanelPatient() {
		if (jPanelPatient == null) {
			jPanelPatient = new JPanel();
			jPanelPatient.setBackground(Color.WHITE);
			jPanelPatient.setBorder(BorderFactory.createTitledBorder(null, MessageBundle.getMessage("angal.anamnesis.patient.txt"), TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, null)); //$NON-NLS-1$
			GridBagLayout gbl_jPanelPatient = new GridBagLayout();
			gbl_jPanelPatient.columnWidths = new int[] { 0, 100, 0, 150, 0, 150, 0, 150 };
			gbl_jPanelPatient.rowHeights = new int[] { 20 };
			gbl_jPanelPatient.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0 };
			gbl_jPanelPatient.rowWeights = new double[] { 0.0 };
			jPanelPatient.setLayout(gbl_jPanelPatient);
			GridBagConstraints gbc_jLabelPatID = new GridBagConstraints();
			gbc_jLabelPatID.fill = GridBagConstraints.BOTH;
			gbc_jLabelPatID.insets = new Insets(0, 5, 5, 5);
			gbc_jLabelPatID.gridx = 0;
			gbc_jLabelPatID.gridy = 0;
			jPanelPatient.add(getJLabelPatID(), gbc_jLabelPatID);
			GridBagConstraints gbc_jLabelPatIDText = new GridBagConstraints();
			gbc_jLabelPatIDText.fill = GridBagConstraints.BOTH;
			gbc_jLabelPatIDText.insets = new Insets(0, 0, 5, 5);
			gbc_jLabelPatIDText.gridx = 1;
			gbc_jLabelPatIDText.gridy = 0;
			jPanelPatient.add(getJLabelPatIDText(), gbc_jLabelPatIDText);
			GridBagConstraints gbc_jLabelFirstName = new GridBagConstraints();
			gbc_jLabelFirstName.fill = GridBagConstraints.BOTH;
			gbc_jLabelFirstName.insets = new Insets(0, 0, 5, 5);
			gbc_jLabelFirstName.gridx = 2;
			gbc_jLabelFirstName.gridy = 0;
			jPanelPatient.add(getJLabelFirstName(), gbc_jLabelFirstName);
			GridBagConstraints gbc_jLabelFirstNameText = new GridBagConstraints();
			gbc_jLabelFirstNameText.fill = GridBagConstraints.BOTH;
			gbc_jLabelFirstNameText.insets = new Insets(0, 0, 5, 5);
			gbc_jLabelFirstNameText.gridx = 3;
			gbc_jLabelFirstNameText.gridy = 0;
			jPanelPatient.add(getJLabelFirstNameText(), gbc_jLabelFirstNameText);
			GridBagConstraints gbc_jLabelSecondName = new GridBagConstraints();
			gbc_jLabelSecondName.fill = GridBagConstraints.BOTH;
			gbc_jLabelSecondName.insets = new Insets(0, 0, 5, 5);
			gbc_jLabelSecondName.gridx = 4;
			gbc_jLabelSecondName.gridy = 0;
			jPanelPatient.add(getJLabelSecondName(), gbc_jLabelSecondName);
			GridBagConstraints gbc_jLabelSecondNameText = new GridBagConstraints();
			gbc_jLabelSecondNameText.fill = GridBagConstraints.BOTH;
			gbc_jLabelSecondNameText.insets = new Insets(0, 0, 5, 5);
			gbc_jLabelSecondNameText.gridx = 5;
			gbc_jLabelSecondNameText.gridy = 0;
			jPanelPatient.add(getJLabelSecondNameText(), gbc_jLabelSecondNameText);
		}
		return jPanelPatient;
	}

	private JPanel getJPanelPathological() {
		if (jPanelPathological == null) {
			jPanelPathological = new JPanel();
			jPanelPathological.setBorder(null);
			jPanelPathological.setLayout(new BoxLayout(jPanelPathological, BoxLayout.X_AXIS));
			jPanelPathological.add(getJPanelFamily());
			jPanelPathological.add(getJPanelPathologicalClosed());
			jPanelPathological.add(getJPanelPathologicalOpen());
			jPanelPathological.add(getJPanelPathologicalExtra());
		}
		return jPanelPathological;
	}

	private JLabel getJLabelPhyDiet() {
		if (jLabelPhyDiet == null) {
			jLabelPhyDiet = new JLabel(MessageBundle.getMessage("angal.anamnesis.diet.txt")); //$NON-NLS-1$
		}
		return jLabelPhyDiet;
	}

	private JLabel getJLabelPhyAlvo() {
		if (jLabelPhyAlvo == null) {
			jLabelPhyAlvo = new JLabel(MessageBundle.getMessage("angal.anamnesis.alvo.txt")); //$NON-NLS-1$
		}
		return jLabelPhyAlvo;
	}

	private JLabel getJLabelPhyDiuresis() {
		if (jLabelPhyDiuresis == null) {
			jLabelPhyDiuresis = new JLabel(MessageBundle.getMessage("angal.anamnesis.diuresis.txt")); //$NON-NLS-1$
		}
		return jLabelPhyDiuresis;
	}

	private JLabel getJLabelPhyAlcool() {
		if (jLabelPhyAlcool == null) {
			jLabelPhyAlcool = new JLabel(MessageBundle.getMessage("angal.anamnesis.alcool.txt")); //$NON-NLS-1$
		}
		return jLabelPhyAlcool;
	}

	private JLabel getJLabelPhySmoke() {
		if (jLabelPhySmoke == null) {
			jLabelPhySmoke = new JLabel(MessageBundle.getMessage("angal.anamnesis.smoke.txt")); //$NON-NLS-1$
		}
		return jLabelPhySmoke;
	}

	private JLabel getJLabelPhyDrugs() {
		if (jLabelPhyDrugs == null) {
			jLabelPhyDrugs = new JLabel(MessageBundle.getMessage("angal.anamnesis.drugs.txt")); //$NON-NLS-1$
		}
		return jLabelPhyDrugs;
	}

	private JCheckBox getJCheckBoxPhyDietNormal() {
		if (jCheckBoxPhyDietNormal == null) {
			jCheckBoxPhyDietNormal = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.diet.normal.txt")); //$NON-NLS-1$
			buttonGroupDiet.add(jCheckBoxPhyDietNormal);
			jCheckBoxPhyDietNormal.addActionListener(e -> path.setPhyNutritionNormal(true));
		}
		return jCheckBoxPhyDietNormal;
	}

	private JCheckBox getJCheckBoxPhyDietAbnormal() {
		if (jCheckBoxPhyDietAbnormal == null) {
			jCheckBoxPhyDietAbnormal = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.diet.other.txt")); //$NON-NLS-1$
			buttonGroupDiet.add(jCheckBoxPhyDietAbnormal);
			jCheckBoxPhyDietAbnormal.addActionListener(e -> {
				path.setPhyNutritionNormal(false);
				jTextFieldPhyDietAbnormalText.requestFocus();
			});
		}
		return jCheckBoxPhyDietAbnormal;
	}

	private JCheckBox getJCheckBoxPhyAlvoNormal() {
		if (jCheckBoxPhyAlvoNormal == null) {
			jCheckBoxPhyAlvoNormal = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.alvo.normal.txt")); //$NON-NLS-1$
			buttonGroupAlvo.add(jCheckBoxPhyAlvoNormal);
			jCheckBoxPhyAlvoNormal.addActionListener(e -> path.setPhyAlvoNormal(true));
		}
		return jCheckBoxPhyAlvoNormal;
	}

	private JCheckBox getJCheckBoxPhyAlvoAbnormal() {
		if (jCheckBoxPhyAlvoAbnormal == null) {
			jCheckBoxPhyAlvoAbnormal = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.alvo.other.txt")); //$NON-NLS-1$
			buttonGroupAlvo.add(jCheckBoxPhyAlvoAbnormal);
			jCheckBoxPhyAlvoAbnormal.addActionListener(e -> {
				path.setPhyAlvoNormal(false);
				jTextFieldPhyAlvoAbnormalText.requestFocus();
			});
		}
		return jCheckBoxPhyAlvoAbnormal;
	}

	private JCheckBox getJCheckBoxPhyDiuresisNormal() {
		if (jCheckBoxPhyDiuresisNormal == null) {
			jCheckBoxPhyDiuresisNormal = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.diuresis.normal.txt")); //$NON-NLS-1$
			buttonGroupDiuresis.add(jCheckBoxPhyDiuresisNormal);
			jCheckBoxPhyDiuresisNormal.addActionListener(e -> path.setPhyDiuresisNormal(true));
		}
		return jCheckBoxPhyDiuresisNormal;
	}

	private JCheckBox getJCheckBoxPhyDiuresisAbnormal() {
		if (jCheckBoxPhyDiuresisAbnormal == null) {
			jCheckBoxPhyDiuresisAbnormal = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.diuresis.other.txt")); //$NON-NLS-1$
			buttonGroupDiuresis.add(jCheckBoxPhyDiuresisAbnormal);
			jCheckBoxPhyDiuresisAbnormal.addActionListener(e -> {
				path.setPhyDiuresisNormal(false);
				jTextFieldPhyDiuresisAbnormalText.requestFocus();
			});
		}
		return jCheckBoxPhyDiuresisAbnormal;
	}


	private JCheckBox getJCheckBoxPhyAlcoolNo() {
		if (jCheckBoxPhyAlcoolNo == null) {
			jCheckBoxPhyAlcoolNo = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.alcool.no.txt")); //$NON-NLS-1$
			buttonGroupAlcool.add(jCheckBoxPhyAlcoolNo);
			jCheckBoxPhyAlcoolNo.addActionListener(e -> path.setPhyAlcool(false));
		}
		return jCheckBoxPhyAlcoolNo;
	}

	private JCheckBox getJCheckBoxPhySmokeNo() {
		if (jCheckBoxPhySmokeNo == null) {
			jCheckBoxPhySmokeNo = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.smoke.no.txt")); //$NON-NLS-1$
			buttonGroupSmoke.add(jCheckBoxPhySmokeNo);
			jCheckBoxPhySmokeNo.addActionListener(e -> path.setPhySmoke(false));
		}
		return jCheckBoxPhySmokeNo;
	}

	private JCheckBox getJCheckBoxPhyDrugsNo() {
		if (jCheckBoxPhyDrugsNo == null) {
			jCheckBoxPhyDrugsNo = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.drugs.no.txt")); //$NON-NLS-1$
			buttonGroupDrugs.add(jCheckBoxPhyDrugsNo);
			jCheckBoxPhyDrugsNo.addActionListener(e -> path.setPhyDrug(false));
		}
		return jCheckBoxPhyDrugsNo;
	}



	private JCheckBox getJCheckBoxPhyAlcoolYes() {
		if (jCheckBoxPhyAlcoolYes == null) {
			jCheckBoxPhyAlcoolYes = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.alcool.yes.txt")); //$NON-NLS-1$
			buttonGroupAlcool.add(jCheckBoxPhyAlcoolYes);
			jCheckBoxPhyAlcoolYes.addActionListener(e -> path.setPhyAlcool(true));
		}
		return jCheckBoxPhyAlcoolYes;
	}

	private JCheckBox getJCheckBoxPhySmokeYes() {
		if (jCheckBoxPhySmokeYes == null) {
			jCheckBoxPhySmokeYes = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.alcool.yes.txt")); //$NON-NLS-1$
			buttonGroupSmoke.add(jCheckBoxPhySmokeYes);
			jCheckBoxPhySmokeYes.addActionListener(e -> path.setPhySmoke(true));
		}
		return jCheckBoxPhySmokeYes;
	}

	private JCheckBox getJCheckBoxPhyDrugsYes() {
		if (jCheckBoxPhyDrugsYes == null) {
			jCheckBoxPhyDrugsYes = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.alcool.yes.txt")); //$NON-NLS-1$
			buttonGroupDrugs.add(jCheckBoxPhyDrugsYes);
			jCheckBoxPhyDrugsYes.addActionListener(e -> path.setPhyDrug(true));
		}
		return jCheckBoxPhyDrugsYes;
	}

	private VoLimitedTextField getJTextFieldPhyDietAbnormalText() {
		if (jTextFieldPhyDietAbnormalText == null) {
			jTextFieldPhyDietAbnormalText = new VoLimitedTextField(30, 30);
			jTextFieldPhyDietAbnormalText.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					super.focusGained(e);
					path.setPhyNutritionNormal(false);
					updateGUIHistory();
				}

				@Override
				public void focusLost(FocusEvent e) {
					path.setPhyNutritionAbnormal(jTextFieldPhyDietAbnormalText.getText());
				}
			});
		}
		return jTextFieldPhyDietAbnormalText;
	}

	private VoLimitedTextField getJTextFieldPhyAlvoAbnormalText() {
		if (jTextFieldPhyAlvoAbnormalText == null) {
			jTextFieldPhyAlvoAbnormalText = new VoLimitedTextField(30, 30);
			jTextFieldPhyAlvoAbnormalText.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					super.focusGained(e);
					path.setPhyAlvoNormal(false);
					updateGUIHistory();
				}

				@Override
				public void focusLost(FocusEvent e) {
					path.setPhyAlvoAbnormal(jTextFieldPhyAlvoAbnormalText.getText());
				}
			});
		}
		return jTextFieldPhyAlvoAbnormalText;
	}

	private VoLimitedTextField getJTextFieldPhyDiuresisAbnormalText() {
		if (jTextFieldPhyDiuresisAbnormalText == null) {
			jTextFieldPhyDiuresisAbnormalText = new VoLimitedTextField(30, 30);
			jTextFieldPhyDiuresisAbnormalText.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					super.focusGained(e);
					path.setPhyDiuresisNormal(false);
					updateGUIHistory();
				}

				@Override
				public void focusLost(FocusEvent e) {
					path.setPhyDiuresisAbnormal(jTextFieldPhyDiuresisAbnormalText.getText());
				}
			});
		}
		return jTextFieldPhyDiuresisAbnormalText;
	}

	private JLabel getJLabelPhyPeriod() {
		if (jLabelPhyPeriod == null) {
			jLabelPhyPeriod = new JLabel(MessageBundle.getMessage("angal.anamnesis.period.txt")); //$NON-NLS-1$
		}
		return jLabelPhyPeriod;
	}

	private JLabel getJLabelPhyMenopause() {
		if (jLabelPhyMenopause == null) {
			jLabelPhyMenopause = new JLabel(MessageBundle.getMessage("angal.anamnesis.menopause.txt")); //$NON-NLS-1$
		}
		return jLabelPhyMenopause;
	}

	private JLabel getJLabelPhyPregnancies() {
		if (jLabelPhyPregnancies == null) {
			jLabelPhyPregnancies = new JLabel(MessageBundle.getMessage("angal.anamnesis.pregnancies.txt")); //$NON-NLS-1$
		}
		return jLabelPhyPregnancies;
	}

	private JPanel getJPanelPhysiologicalHistory() {
		if (jPanelPhysiologicalHistory == null) {
			jPanelPhysiologicalHistory = new JPanel();
			jPanelPhysiologicalHistory.setBorder(new TitledBorder(null, MessageBundle.getMessage("angal.anamnesis.physiologicalhistory.border"), TitledBorder.CENTER, TitledBorder.TOP, fontBoldTitleBorder, null)); //$NON-NLS-1$
			jPanelPhysiologicalHistory.setLayout(new BoxLayout(jPanelPhysiologicalHistory, BoxLayout.X_AXIS));
			jPanelPhysiologicalHistory.add(getJPanelPhyHistoryUnisex());
			jPanelPhysiologicalHistory.add(getJPanelPhyHistoryFemale());
		}
		return jPanelPhysiologicalHistory;
	}

	private JPanel getJPanelPhyHistoryFemale() {
		if (jPanelPhyHistoryFemale == null) {
			jPanelPhyHistoryFemale = new JPanel();
			jPanelPhyHistoryFemale.setBorder(null);
			GridBagLayout gbl_jPanelPhyHistoryFemale = new GridBagLayout();
			gbl_jPanelPhyHistoryFemale.columnWidths = new int[] { 0, 0, 0, 0 };
			gbl_jPanelPhyHistoryFemale.rowHeights = new int[] { 20, 20, 20, 20 };
			gbl_jPanelPhyHistoryFemale.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0 };
			gbl_jPanelPhyHistoryFemale.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
			jPanelPhyHistoryFemale.setLayout(gbl_jPanelPhyHistoryFemale);
			GridBagConstraints gbc_jLabelPhyPeriod = new GridBagConstraints();
			gbc_jLabelPhyPeriod.anchor = GridBagConstraints.WEST;
			gbc_jLabelPhyPeriod.insets = new Insets(0, 5, 0, 5);
			gbc_jLabelPhyPeriod.gridx = 0;
			gbc_jLabelPhyPeriod.gridy = 0;
			jPanelPhyHistoryFemale.add(getJLabelPhyPeriod(), gbc_jLabelPhyPeriod);
			GridBagConstraints gbc_chckbxNormal = new GridBagConstraints();
			gbc_chckbxNormal.anchor = GridBagConstraints.WEST;
			gbc_chckbxNormal.insets = new Insets(0, 5, 0, 5);
			gbc_chckbxNormal.gridx = 1;
			gbc_chckbxNormal.gridy = 0;
			jPanelPhyHistoryFemale.add(getJCheckBoxPeriodNormal(), gbc_chckbxNormal);
			GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
			gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
			gbc_chckbxNewCheckBox.insets = new Insets(0, 5, 0, 5);
			gbc_chckbxNewCheckBox.gridx = 2;
			gbc_chckbxNewCheckBox.gridy = 0;
			jPanelPhyHistoryFemale.add(getJCheckBoxPeriodAbnormal(), gbc_chckbxNewCheckBox);
			GridBagConstraints gbc_jTextFieldPhyPeriodAbnormalText = new GridBagConstraints();
			gbc_jTextFieldPhyPeriodAbnormalText.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldPhyPeriodAbnormalText.gridx = 3;
			gbc_jTextFieldPhyPeriodAbnormalText.gridy = 0;
			jPanelPhyHistoryFemale.add(getJTextFieldPhyPeriodAbnormalText(), gbc_jTextFieldPhyPeriodAbnormalText);
			GridBagConstraints gbc_jLabelPhyMenopause = new GridBagConstraints();
			gbc_jLabelPhyMenopause.anchor = GridBagConstraints.WEST;
			gbc_jLabelPhyMenopause.insets = new Insets(0, 5, 0, 5);
			gbc_jLabelPhyMenopause.gridx = 0;
			gbc_jLabelPhyMenopause.gridy = 1;
			jPanelPhyHistoryFemale.add(getJLabelPhyMenopause(), gbc_jLabelPhyMenopause);
			GridBagConstraints gbc_jCheckBoxPhyMenopauseNo = new GridBagConstraints();
			gbc_jCheckBoxPhyMenopauseNo.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPhyMenopauseNo.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyMenopauseNo.gridx = 1;
			gbc_jCheckBoxPhyMenopauseNo.gridy = 1;
			jPanelPhyHistoryFemale.add(getJCheckBoxPhyMenopauseNo(), gbc_jCheckBoxPhyMenopauseNo);
			GridBagConstraints gbc_jCheckBoxPhyMenopauseYes = new GridBagConstraints();
			gbc_jCheckBoxPhyMenopauseYes.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPhyMenopauseYes.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyMenopauseYes.gridx = 2;
			gbc_jCheckBoxPhyMenopauseYes.gridy = 1;
			jPanelPhyHistoryFemale.add(getJCheckBoxPhyMenopauseYes(), gbc_jCheckBoxPhyMenopauseYes);
			GridBagConstraints gbc_jPanelPhyMenopausePanel = new GridBagConstraints();
			gbc_jPanelPhyMenopausePanel.fill = GridBagConstraints.BOTH;
			gbc_jPanelPhyMenopausePanel.gridx = 3;
			gbc_jPanelPhyMenopausePanel.gridy = 1;
			jPanelPhyHistoryFemale.add(getJPanelPhyMenopausePanel(), gbc_jPanelPhyMenopausePanel);
			GridBagConstraints gbc_jLabelPhyHRT = new GridBagConstraints();
			gbc_jLabelPhyHRT.anchor = GridBagConstraints.WEST;
			gbc_jLabelPhyHRT.insets = new Insets(0, 5, 0, 5);
			gbc_jLabelPhyHRT.gridx = 0;
			gbc_jLabelPhyHRT.gridy = 2;
			jPanelPhyHistoryFemale.add(getJLabelPhyHRT(), gbc_jLabelPhyHRT);
			GridBagConstraints gbc_jCheckBoxPhyHRTNo = new GridBagConstraints();
			gbc_jCheckBoxPhyHRTNo.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPhyHRTNo.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyHRTNo.gridx = 1;
			gbc_jCheckBoxPhyHRTNo.gridy = 2;
			jPanelPhyHistoryFemale.add(getJCheckBoxPhyHRTNo(), gbc_jCheckBoxPhyHRTNo);
			GridBagConstraints gbc_jCheckBoxPhyHRTYes = new GridBagConstraints();
			gbc_jCheckBoxPhyHRTYes.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPhyHRTYes.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyHRTYes.gridx = 2;
			gbc_jCheckBoxPhyHRTYes.gridy = 2;
			jPanelPhyHistoryFemale.add(getJCheckBoxPhyHRTYes(), gbc_jCheckBoxPhyHRTYes);
			GridBagConstraints gbc_jTextFieldPhyHRTYesText = new GridBagConstraints();
			gbc_jTextFieldPhyHRTYesText.fill = GridBagConstraints.HORIZONTAL;
			gbc_jTextFieldPhyHRTYesText.gridx = 3;
			gbc_jTextFieldPhyHRTYesText.gridy = 2;
			jPanelPhyHistoryFemale.add(getJTextFieldPhyHRTYesText(), gbc_jTextFieldPhyHRTYesText);
			GridBagConstraints gbc_jLabelPhyPregnancies = new GridBagConstraints();
			gbc_jLabelPhyPregnancies.anchor = GridBagConstraints.WEST;
			gbc_jLabelPhyPregnancies.insets = new Insets(0, 5, 0, 5);
			gbc_jLabelPhyPregnancies.gridx = 0;
			gbc_jLabelPhyPregnancies.gridy = 3;
			jPanelPhyHistoryFemale.add(getJLabelPhyPregnancies(), gbc_jLabelPhyPregnancies);
			GridBagConstraints gbc_jCheckBoxPhyPregnancyNo = new GridBagConstraints();
			gbc_jCheckBoxPhyPregnancyNo.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPhyPregnancyNo.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyPregnancyNo.gridx = 1;
			gbc_jCheckBoxPhyPregnancyNo.gridy = 3;
			jPanelPhyHistoryFemale.add(getJCheckBoxPhyPregnancyNo(), gbc_jCheckBoxPhyPregnancyNo);
			GridBagConstraints gbc_jCheckBoxPhyPregnancyYes = new GridBagConstraints();
			gbc_jCheckBoxPhyPregnancyYes.anchor = GridBagConstraints.WEST;
			gbc_jCheckBoxPhyPregnancyYes.insets = new Insets(0, 5, 0, 5);
			gbc_jCheckBoxPhyPregnancyYes.gridx = 2;
			gbc_jCheckBoxPhyPregnancyYes.gridy = 3;
			jPanelPhyHistoryFemale.add(getJCheckBoxPhyPregnancyYes(), gbc_jCheckBoxPhyPregnancyYes);
			GridBagConstraints gbc_jPanelPhyPregnancyPanel = new GridBagConstraints();
			gbc_jPanelPhyPregnancyPanel.fill = GridBagConstraints.BOTH;
			gbc_jPanelPhyPregnancyPanel.gridx = 3;
			gbc_jPanelPhyPregnancyPanel.gridy = 3;
			jPanelPhyHistoryFemale.add(getJPanelPhyPregnancyPanel(), gbc_jPanelPhyPregnancyPanel);
		}
		return jPanelPhyHistoryFemale;
	}

	private JLabel getJLabelPhyHRT() {
		if (jLabelPhyHRT == null) {
			jLabelPhyHRT = new JLabel(MessageBundle.getMessage("angal.anamnesis.hrt.txt")); //$NON-NLS-1$
		}
		return jLabelPhyHRT;
	}

	private JCheckBox getJCheckBoxPeriodNormal() {
		if (jCheckBoxPhyPeriodNormal == null) {
			jCheckBoxPhyPeriodNormal = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.period.normal.txt")); //$NON-NLS-1$
			buttonGroupPeriod.add(jCheckBoxPhyPeriodNormal);
			jCheckBoxPhyPeriodNormal.addActionListener(e -> path.setPhyPeriodNormal(true));
		}
		return jCheckBoxPhyPeriodNormal;
	}

	private JCheckBox getJCheckBoxPeriodAbnormal() {
		if (jCheckBoxPhyPeriodAbnormal == null) {
			jCheckBoxPhyPeriodAbnormal = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.period.other.txt")); //$NON-NLS-1$
			buttonGroupPeriod.add(jCheckBoxPhyPeriodAbnormal);
			jCheckBoxPhyPeriodAbnormal.addActionListener(e -> {
				path.setPhyPeriodNormal(false);
				jTextFieldPhyPeriodAbnormalText.requestFocus();
			});
		}
		return jCheckBoxPhyPeriodAbnormal;
	}

	private VoLimitedTextField getJTextFieldPhyPeriodAbnormalText() {
		if (jTextFieldPhyPeriodAbnormalText == null) {
			jTextFieldPhyPeriodAbnormalText = new VoLimitedTextField(30, 30);
			jTextFieldPhyPeriodAbnormalText.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					super.focusGained(e);
					path.setPhyPeriodNormal(false);
					updateGUIHistory();
				}

				@Override
				public void focusLost(FocusEvent e) {
					path.setPhyPeriodAbnormal(jTextFieldPhyPeriodAbnormalText.getText());
				}
			});
		}
		return jTextFieldPhyPeriodAbnormalText;
	}

	private JCheckBox getJCheckBoxPhyMenopauseNo() {
		if (jCheckBoxPhyMenopauseNo == null) {
			jCheckBoxPhyMenopauseNo = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.menopause.no.txt")); //$NON-NLS-1$
			buttonGroupMenopause.add(jCheckBoxPhyMenopauseNo);
			jCheckBoxPhyMenopauseNo.addActionListener(e -> path.setPhyMenopause(false));
		}
		return jCheckBoxPhyMenopauseNo;
	}

	private JCheckBox getJCheckBoxPhyMenopauseYes() {
		if (jCheckBoxPhyMenopauseYes == null) {
			jCheckBoxPhyMenopauseYes = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.menopause.yes.txt")); //$NON-NLS-1$
			buttonGroupMenopause.add(jCheckBoxPhyMenopauseYes);
			jCheckBoxPhyMenopauseYes.addActionListener(e -> {
				path.setPhyMenopause(true);
				jTextFieldPhyMenopauseYesYears.requestFocus();
			});
		}
		return jCheckBoxPhyMenopauseYes;
	}

	private VoIntegerTextField getJTextFieldPhyMenopauseYesYears() {
		if (jTextFieldPhyMenopauseYesYears == null) {
			jTextFieldPhyMenopauseYesYears = new VoIntegerTextField(0, 5);
			jTextFieldPhyMenopauseYesYears.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					super.focusGained(e);
					path.setPhyMenopause(true);
					updateGUIHistory();
				}

				@Override
				public void focusLost(FocusEvent e) {
					path.setPhyMenopauseYears(Integer.parseInt(jTextFieldPhyMenopauseYesYears.getText()));
				}
			});
		}
		return jTextFieldPhyMenopauseYesYears;
	}

	private JCheckBox getJCheckBoxPhyHRTNo() {
		if (jCheckBoxPhyHRTNo == null) {
			jCheckBoxPhyHRTNo = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.hrt.no.txt")); //$NON-NLS-1$
			buttonGroupHRT.add(jCheckBoxPhyHRTNo);
			jCheckBoxPhyHRTNo.addActionListener(e -> path.setPhyHrtNormal(true));
		}
		return jCheckBoxPhyHRTNo;
	}

	private JCheckBox getJCheckBoxPhyHRTYes() {
		if (jCheckBoxPhyHRTYes == null) {
			jCheckBoxPhyHRTYes = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.hrt.yes.txt")); //$NON-NLS-1$
			buttonGroupHRT.add(jCheckBoxPhyHRTYes);
			jCheckBoxPhyHRTYes.addActionListener(e -> {
				path.setPhyHrtNormal(false);
				jTextFieldPhyHRTYesText.requestFocus();
			});
		}
		return jCheckBoxPhyHRTYes;
	}

	private VoLimitedTextField getJTextFieldPhyHRTYesText() {
		if (jTextFieldPhyHRTYesText == null) {
			jTextFieldPhyHRTYesText = new VoLimitedTextField(30, 30);
			jTextFieldPhyHRTYesText.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					super.focusGained(e);
					path.setPhyHrtNormal(false);
					updateGUIHistory();
				}

				@Override
				public void focusLost(FocusEvent e) {
					path.setPhyHrtAbnormal(jTextFieldPhyHRTYesText.getText());
				}
			});
		}
		return jTextFieldPhyHRTYesText;
	}

	private JPanel getJPanelPhyPregnancyPanel() {
		if (jPanelPhyPregnancyPanel == null) {
			jPanelPhyPregnancyPanel = new JPanel();
			jPanelPhyPregnancyPanel.setLayout(new GridLayout(0, 6, 5, 0));
			jPanelPhyPregnancyPanel.add(getJLabelPhyPregnancyNumber());
			jPanelPhyPregnancyPanel.add(getJTtextFieldPhyPregnancyNumber());
			jPanelPhyPregnancyPanel.add(getJLabelPhyPregnancyDeliveryNumber());
			jPanelPhyPregnancyPanel.add(getJTextFieldPhyPregnancyDeliveryNumber());
			jPanelPhyPregnancyPanel.add(getJLabelPhyPregnancyAbortNumber());
			jPanelPhyPregnancyPanel.add(getJTextFieldPhyPregnancyAbortNumber());
		}
		return jPanelPhyPregnancyPanel;
	}

	private JCheckBox getJCheckBoxPhyPregnancyNo() {
		if (jCheckBoxPhyPregnancyNo == null) {
			jCheckBoxPhyPregnancyNo = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.pregnancies.no.txt")); //$NON-NLS-1$
			buttonGroupPregnancy.add(jCheckBoxPhyPregnancyNo);
			jCheckBoxPhyPregnancyNo.addActionListener(e -> path.setPhyPregnancy(false));
		}
		return jCheckBoxPhyPregnancyNo;
	}

	private JCheckBox getJCheckBoxPhyPregnancyYes() {
		if (jCheckBoxPhyPregnancyYes == null) {
			jCheckBoxPhyPregnancyYes = new JCheckBox(MessageBundle.getMessage("angal.anamnesis.pregnancies.yes.txt")); //$NON-NLS-1$
			buttonGroupPregnancy.add(jCheckBoxPhyPregnancyYes);
			jCheckBoxPhyPregnancyYes.addActionListener(e -> {
				path.setPhyPregnancy(true);
				jTextFieldPhyPregnancyNumber.requestFocus();
			});
		}
		return jCheckBoxPhyPregnancyYes;
	}

	private JLabel getJLabelPhyPregnancyNumber() {
		if (jLabelPhyPregnancyNumber == null) {
			jLabelPhyPregnancyNumber = new JLabel(MessageBundle.getMessage("angal.anamnesis.pregnancies.nr.txt")); //$NON-NLS-1$
			jLabelPhyPregnancyNumber.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return jLabelPhyPregnancyNumber;
	}

	private VoIntegerTextField getJTtextFieldPhyPregnancyNumber() {
		if (jTextFieldPhyPregnancyNumber == null) {
			jTextFieldPhyPregnancyNumber = new VoIntegerTextField(0, 5);
			jTextFieldPhyPregnancyNumber.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					super.focusGained(e);
					path.setPhyPregnancy(true);
					updateGUIHistory();
				}

				@Override
				public void focusLost(FocusEvent e) {
					path.setPhyPregnancyNumber(Integer.parseInt(jTextFieldPhyPregnancyNumber.getText()));
				}
			});
		}
		return jTextFieldPhyPregnancyNumber;
	}

	private JLabel getJLabelPhyPregnancyDeliveryNumber() {
		if (jLabelPhyPregnancyDeliveryNumber == null) {
			jLabelPhyPregnancyDeliveryNumber = new JLabel(MessageBundle.getMessage("angal.anamnesis.pregnancies.delivery.txt")); //$NON-NLS-1$
			jLabelPhyPregnancyDeliveryNumber.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return jLabelPhyPregnancyDeliveryNumber;
	}

	private VoIntegerTextField getJTextFieldPhyPregnancyDeliveryNumber() {
		if (jTextFieldPhyPregnancyDeliveryNumber == null) {
			jTextFieldPhyPregnancyDeliveryNumber = new VoIntegerTextField(0, 5);
			jTextFieldPhyPregnancyDeliveryNumber.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					super.focusGained(e);
					path.setPhyPregnancy(true);
					updateGUIHistory();
				}

				@Override
				public void focusLost(FocusEvent e) {
					path.setPhyPregnancyBirth(Integer.parseInt(jTextFieldPhyPregnancyDeliveryNumber.getText()));
				}
			});
		}
		return jTextFieldPhyPregnancyDeliveryNumber;
	}

	private JLabel getJLabelPhyPregnancyAbortNumber() {
		if (jLabelPhyPregnancyAbortNumber == null) {
			jLabelPhyPregnancyAbortNumber = new JLabel(MessageBundle.getMessage("angal.anamnesis.pregnancies.abort.txt")); //$NON-NLS-1$
			jLabelPhyPregnancyAbortNumber.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return jLabelPhyPregnancyAbortNumber;
	}

	private VoIntegerTextField getJTextFieldPhyPregnancyAbortNumber() {
		if (jTextFieldPhyPregnancyAbortNumber == null) {
			jTextFieldPhyPregnancyAbortNumber = new VoIntegerTextField(0, 5);
			jTextFieldPhyPregnancyAbortNumber.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					super.focusGained(e);
					path.setPhyPregnancy(true);
					updateGUIHistory();
				}

				@Override
				public void focusLost(FocusEvent e) {
					path.setPhyPregnancyAbort(Integer.parseInt(jTextFieldPhyPregnancyAbortNumber.getText()));
				}
			});
		}
		return jTextFieldPhyPregnancyAbortNumber;
	}

	private class SwingActionExcludeFamilyNothing extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public SwingActionExcludeFamilyNothing() {
			putValue(NAME, "ExcludeFamilyNothing"); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.anamnesis.family.tooltip.excludenothingtodeclare.txt")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//Update model
			path.setFamilyNothing(false);
			updateModelFromGUI();
			//Update GUI
			updateGUIHistory();
		}
	}

	private Action getActionExludeFamilyNothing() {
		if (actionExcludeFamilyNothing == null) {
			actionExcludeFamilyNothing = new SwingActionExcludeFamilyNothing();
		}
		return actionExcludeFamilyNothing;
	}

	private class SwingActionExcludePathClosedNothing extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public SwingActionExcludePathClosedNothing() {
			putValue(NAME, "ExcludePathClosedNothing"); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.anamnesis.closed.tooltip.excludenothingtodeclare.txt")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//Update model
			path.setPatClosedNothing(false);
			updateModelFromGUI();
			//Update GUI
			updateGUIHistory();
		}
	}

	private Action getActionExcludePathClosedNothing() {
		if (actionExcludePathClosedNothing == null) {
			actionExcludePathClosedNothing = new SwingActionExcludePathClosedNothing();
		}
		return actionExcludePathClosedNothing;
	}

	private class SwingActionExcludePathOpenNothing extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public SwingActionExcludePathOpenNothing() {
			putValue(NAME, "ExcludePathOpenNothing"); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.anamnesis.open.tooltip.excludenothingtodeclare.txt")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//Update model
			path.setPatOpenNothing(false);
			updateModelFromGUI();
			//Update GUI
			updateGUIHistory();
		}
	}

	private Action getActionExcludePathOpenNothing() {
		if (actionExcludePathOpenNothing == null) {
			actionExcludePathOpenNothing = new SwingActionExcludePathOpenNothing();
		}
		return actionExcludePathOpenNothing;
	}

	private class SwingActionResetFamilyHistory extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public SwingActionResetFamilyHistory() {
			putValue(NAME, "ResetFamilyHistory"); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.anamnesis.family.tooltip.resetfamilyhistory.txt")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// Update model
			path.setFamilyNothing(true);
			path.setFamilyHypertension(false);
			path.setFamilyDrugAddiction(false);
			path.setFamilyCardiovascular(false);
			path.setFamilyInfective(false);
			path.setFamilyEndocrinometabol(false);
			path.setFamilyRespiratory(false);
			path.setFamilyCancer(false);
			path.setFamilyOrto(false);
			path.setFamilyGyno(false);
			path.setFamilyOther(false);
			// Update GUI
			updateGUIHistory();
		}
	}

	private Action getActionResetFamilyHistory() {
		if (actionResetFamilyHistory == null) {
			actionResetFamilyHistory = new SwingActionResetFamilyHistory();
		}
		return actionResetFamilyHistory;
	}

	private class SwingActionResetPathClosed extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public SwingActionResetPathClosed() {
			putValue(NAME, "ResetPathClosed"); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.anamnesis.closed.tooltip.resetpastproblems.txt")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// Update model
			path.setPatClosedNothing(true);
			path.setPatClosedHypertension(false);
			path.setPatClosedDrugaddiction(false);
			path.setPatClosedCardiovascular(false);
			path.setPatClosedInfective(false);
			path.setPatClosedEndocrinometabol(false);
			path.setPatClosedRespiratory(false);
			path.setPatClosedCancer(false);
			path.setPatClosedOrto(false);
			path.setPatClosedGyno(false);
			path.setPatClosedOther(false);
			// Update GUI
			updateGUIHistory();
		}
	}

	private Action getActionResetPathClosed() {
		if (actionResetPathClosed == null) {
			actionResetPathClosed = new SwingActionResetPathClosed();
		}
		return actionResetPathClosed;
	}

	private class SwingActionResetPathOpen extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public SwingActionResetPathOpen() {
			putValue(NAME, "ResetPathOpen"); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.anamnesis.open.tooltip.resetrecentproblems.txt")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			// Update model
			path.setPatOpenNothing(true);
			path.setPatOpenHypertension(false);
			path.setPatOpenDrugaddiction(false);
			path.setPatOpenCardiovascular(false);
			path.setPatOpenInfective(false);
			path.setPatOpenEndocrinometabol(false);
			path.setPatOpenRespiratory(false);
			path.setPatOpenCancer(false);
			path.setPatOpenOrto(false);
			path.setPatOpenGyno(false);
			path.setPatOpenOther(false);
			// Update GUI
			updateGUIHistory();
		}
	}

	private Action getActionResetPathOpen() {
		if (actionResetPathOpen == null) {
			actionResetPathOpen = new SwingActionResetPathOpen();
		}
		return actionResetPathOpen;
	}

	private JPanel getJPanelPhyMenopausePanel() {
		if (jPanelPhyMenopausePanel == null) {
			jPanelPhyMenopausePanel = new JPanel();
			jPanelPhyMenopausePanel.setLayout(new GridLayout(0, 6, 5, 0));
			jPanelPhyMenopausePanel.add(getJLabelPhyMenopauseYears());
			jPanelPhyMenopausePanel.add(getJTextFieldPhyMenopauseYesYears());
		}
		return jPanelPhyMenopausePanel;
	}

	private JLabel getJLabelPhyMenopauseYears() {
		if (jLabelPhyMenopauseYears == null) {
			jLabelPhyMenopauseYears = new JLabel(MessageBundle.getMessage("angal.anamnesis.menopause.years.txt")); //$NON-NLS-1$
			jLabelPhyMenopauseYears.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return jLabelPhyMenopauseYears;
	}

	private JButton getJButtonSave() {
		if (jButtonSave == null) {
			jButtonSave = new JButton();
			jButtonSave.setAction(getActionSavePatientHistory());
		}
		return jButtonSave;
	}

	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.anamnesis.cancel.txt")); //$NON-NLS-1$
			jButtonCancel.setMnemonic(KeyEvent.VK_C);
			jButtonCancel.addActionListener(e -> dispose());
		}
		return jButtonCancel;
	}

	class ActionSavePatientHistory extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public ActionSavePatientHistory() {

			putValue(NAME, MessageBundle.getMessage("angal.anamnesis.save.txt")); //$NON-NLS-1$
			putValue(MNEMONIC_KEY, KeyEvent.VK_S);
			putValue(SHORT_DESCRIPTION, MessageBundle.getMessage("angal.anamnesis.tooltip.savethepatienthistory.txt")); //$NON-NLS-1$
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			updateModelFromGUI();
			if (storeData) {
				patientHistoryManager.saveOrUpdate(path);
			}
			dispose();
		}
	}

	private Action getActionSavePatientHistory() {
		if (actionSavePatientHistory == null) {
			actionSavePatientHistory = new ActionSavePatientHistory();
		}
		return actionSavePatientHistory;
	}

}
