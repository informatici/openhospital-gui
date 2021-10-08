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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.stat.gui.report.GenericReportPatientVersion2;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.Converters;

import com.toedter.calendar.JDateChooser;

public class PatientFolderReportModal extends ModalJFrame {

	private static final String DATE_FORMAT = "dd/MM/yyyy";

	private JFrame parent;
	private Integer patId;
	private JPanel jPanelChooser;
	private JDateChooser jDateChooserDateFrom;
	private JDateChooser jDateChooserDateTo;
	private JPanel choosePanel;
	private JButton launchReportButton;
	private JButton closeButton;
	private JPanel admissionPanel;
	private JCheckBox admissionCheck;
	private JPanel opdPanel;
	private JCheckBox opdCheck;
	private JPanel drugsPanel;
	private JCheckBox drugsCheck;
	private JPanel examinationPanel;
	private JCheckBox examinationCheck;
	private JPanel laboratoryPanel;
	private JCheckBox laboratoryCheck;
	private JPanel operationsPanel;
	private JCheckBox operationCheck;
	private JPanel allPanel;
	private JCheckBox allCheck;
	private JPanel labelPanel;
	private LocalDate date;
	
	public PatientFolderReportModal(JFrame parent, Integer code, LocalDate olderDate) {
		this.parent = parent;
		this.patId = code;
		this.date = olderDate;
		initialize();
	}
	
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getJContentPane(), BorderLayout.CENTER);
		this.setTitle(MessageBundle.getMessage("angal.admission.report.title"));
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		showAsModal(parent);
	}
	
	private JPanel getJContentPane() {

		if (jPanelChooser == null) {
			jPanelChooser = new JPanel();
			
			GridBagLayout gblPanelChooser = new GridBagLayout();
			gblPanelChooser.columnWidths = new int[] { 0, 0, 0, 0 };
			gblPanelChooser.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
			gblPanelChooser.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
			gblPanelChooser.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
			jPanelChooser.setLayout(gblPanelChooser);

			JLabel jLabelDate = new JLabel(MessageBundle.getMessage("angal.common.datefrom.label"));
			GridBagConstraints gbcLabelDate = new GridBagConstraints();
			gbcLabelDate.anchor = GridBagConstraints.WEST;
			gbcLabelDate.insets = new Insets(10, 5, 5, 5);
			gbcLabelDate.gridx = 0;
			gbcLabelDate.gridy = 0;
			jPanelChooser.add(jLabelDate, gbcLabelDate);
			
			GridBagConstraints gbcDateChooserDate = new GridBagConstraints();
			gbcDateChooserDate.anchor = GridBagConstraints.WEST;
			gbcDateChooserDate.insets = new Insets(10, 5, 5, 5);
			gbcDateChooserDate.gridx = 1;
			gbcDateChooserDate.gridy = 0;
			jPanelChooser.add(getJDateChooserDateFrom(), gbcDateChooserDate);
			
			JLabel jLabelDateto = new JLabel(MessageBundle.getMessage("angal.common.dateto.label"));
			GridBagConstraints gbcLabelDateto = new GridBagConstraints();
			gbcLabelDateto.anchor = GridBagConstraints.WEST;
			gbcLabelDateto.insets = new Insets(10, 5, 5, 5);
			gbcLabelDateto.gridx = 0;
			gbcLabelDateto.gridy = 1;
			jPanelChooser.add(jLabelDateto, gbcLabelDateto);
			
			GridBagConstraints gbcDateChooserDateto = new GridBagConstraints();
			gbcDateChooserDateto.anchor = GridBagConstraints.WEST;
			gbcDateChooserDateto.insets = new Insets(10, 5, 5, 5);
			gbcDateChooserDateto.gridx = 1;
			gbcDateChooserDateto.gridy = 1;
			jPanelChooser.add(getJDateChooserDateTo(), gbcDateChooserDateto);
			
			GridBagConstraints gbcSliderHeight = new GridBagConstraints();
			gbcSliderHeight.insets = new Insets(5, 5, 5, 5);
			gbcSliderHeight.fill = GridBagConstraints.WEST;
			gbcSliderHeight.gridx = 0;
			gbcSliderHeight.gridy = 2;
			jPanelChooser.add(getValueReport(), gbcSliderHeight);

			GridBagConstraints gbcPrintButton = new GridBagConstraints();
			gbcPrintButton.insets = new Insets(5, 5, 5, 5);
			gbcPrintButton.fill = GridBagConstraints.HORIZONTAL;
			gbcPrintButton.gridx = 0;
			gbcPrintButton.gridy = 3;
			jPanelChooser.add(getPrintButton(), gbcPrintButton);

			GridBagConstraints gbcCloseButton = new GridBagConstraints();
			gbcCloseButton.insets = new Insets(5, 5, 5, 5);
			gbcCloseButton.fill = GridBagConstraints.HORIZONTAL;
			gbcCloseButton.gridx = 1;
			gbcCloseButton.gridy = 3;
			jPanelChooser.add(getCloseButton(), gbcCloseButton);
		}
		return jPanelChooser;
	}
	
	private JButton getPrintButton() {
		if (launchReportButton == null) {
			launchReportButton = new JButton(MessageBundle.getMessage("angal.common.launchreport.btn"));
			launchReportButton.setMnemonic(MessageBundle.getMnemonic("angal.common.launchreport.btn.key"));
			launchReportButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					new GenericReportPatientVersion2(patId, getParameterString(), Converters.convertToLocalDateTime(getDateFromValue()), Converters.convertToLocalDateTime(getDateToValue()), GeneralData.PATIENTSHEET);
				}
				
				protected String getParameterString() {
					StringBuilder parameterString = new StringBuilder();
					if (getAllValue()) {
						parameterString.append("All");
						return parameterString.toString();
					}
					if (getDrugsValue()) {
						parameterString.append("Drugs");
					}
					if (getExaminationValue()) {
						parameterString.append("Examination");
					}
					if (getAdmissionValue()) {
						parameterString.append("Admission");
					}
					if (getOpdValue()) {
						parameterString.append("Opd");
					}
					if (getLaboratoryValue()) {
						parameterString.append("Laboratory");
					}
					if (getOperationValue()) {
						parameterString.append("Operations");
					}
					
					return parameterString.toString();
				}
			});
		}
		return launchReportButton;
	}
	
	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			closeButton.addActionListener(actionEvent -> dispose());
		}
		return closeButton;
	}
	
	private JPanel getValueReport() {

		if (choosePanel == null) {

			choosePanel = new JPanel();
			choosePanel.setLayout(new javax.swing.BoxLayout(choosePanel, javax.swing.BoxLayout.Y_AXIS));
			
			choosePanel.add(getPanelLabel());
			choosePanel.add(getPanelAll());
			choosePanel.add(getPanelAdmission());
			choosePanel.add(getPanelOpd());
			choosePanel.add(getPanelLaboratory());
			choosePanel.add(getPanelOperations());
			choosePanel.add(getPanelDrugs());
			choosePanel.add(getPanelExamination());
		}
		return choosePanel;
	}
	
	private JPanel getPanelLabel() {
		if (labelPanel == null) {
			labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
			labelPanel.setAlignmentY(LEFT_ALIGNMENT);
			labelPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.reportfor.txt") + ':'), BorderLayout.CENTER);
		}
		return labelPanel;
	}

	private JPanel getPanelAll() {
		if (allPanel == null) {
			allPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
			allPanel.setAlignmentY(LEFT_ALIGNMENT);
			allCheck = new JCheckBox();

			allCheck.setSelected(true);
			allPanel.add(allCheck);
			allPanel.add(new JLabel(MessageBundle.getMessage("angal.common.all.txt").toUpperCase()), BorderLayout.CENTER);
			allCheck.addActionListener(actionEvent -> {
				examinationCheck.setSelected(false);
				admissionCheck.setSelected(false);
				drugsCheck.setSelected(false);
				opdCheck.setSelected(false);
				operationCheck.setSelected(false);
				laboratoryCheck.setSelected(false);
			});
		}
		return allPanel;
	}

	private JPanel getPanelExamination() {
		if (examinationPanel == null) {
			examinationPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			examinationCheck = new JCheckBox();

			examinationPanel.add(examinationCheck);
			examinationPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.examination.txt")), BorderLayout.CENTER);
			examinationCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
			
		}
		return examinationPanel;
	}
	
	private JPanel getPanelOperations() {
		if (operationsPanel == null) {
			operationsPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			operationCheck = new JCheckBox();

			operationsPanel.add(operationCheck);
			operationsPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.operation.txt")), BorderLayout.CENTER);
			operationCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return operationsPanel;
	}
	
	private JPanel getPanelLaboratory() {
		if (laboratoryPanel == null) {
			laboratoryPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			laboratoryCheck = new JCheckBox();

			laboratoryPanel.add(laboratoryCheck);
			laboratoryPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.laboratory.txt")), BorderLayout.CENTER);
			laboratoryCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return laboratoryPanel;
	}
	
	private JPanel getPanelDrugs() {
		if (drugsPanel == null) {
			drugsPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			drugsCheck = new JCheckBox();

			drugsPanel.add(drugsCheck);
			drugsPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.drugs.txt")), BorderLayout.CENTER);
			drugsCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return drugsPanel;
	}
	
	private JPanel getPanelOpd() {
		if (opdPanel == null) {
			opdPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			opdCheck = new JCheckBox();

			opdPanel.add(opdCheck);
			opdPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.opd.txt")), BorderLayout.CENTER);
			opdCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return opdPanel;
	}

	private JPanel getPanelAdmission() {
		if (admissionPanel == null) {
			admissionPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			admissionCheck = new JCheckBox();

			admissionPanel.add(admissionCheck);
			admissionPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.patientfolder.admission.txt")), BorderLayout.CENTER);
			admissionCheck.addActionListener(actionEvent -> allCheck.setSelected(false));
		}
		return admissionPanel;
	}
	
	public boolean getOperationValue() {
		return operationCheck.isSelected();
	}
	public boolean getLaboratoryValue() {
		return laboratoryCheck.isSelected();
	}
	public boolean getAdmissionValue() {
		return admissionCheck.isSelected();
	}
	public boolean getOpdValue() {
		return opdCheck.isSelected();
	}
	public boolean getAllValue() {
		return allCheck.isSelected();
	}
	public boolean getDrugsValue() {
		return drugsCheck.isSelected();
	}
	
	public boolean getExaminationValue() {
		return examinationCheck.isSelected();
	}

	private JDateChooser getJDateChooserDateFrom() {
		if (jDateChooserDateFrom == null) {
			jDateChooserDateFrom = new JDateChooser();
			jDateChooserDateFrom.setPreferredSize(new Dimension(200, 40));
			jDateChooserDateFrom.setLocale(new Locale(GeneralData.LANGUAGE));
			jDateChooserDateFrom.setDateFormatString(DATE_FORMAT);
			jDateChooserDateFrom.setDate(Converters.toDate(date.atStartOfDay()));
			jDateChooserDateFrom.addPropertyChangeListener("date", evt -> {
				Date date = (Date) evt.getNewValue();
				jDateChooserDateFrom.setDate(date);
			});
		}
		return jDateChooserDateFrom;
	}

	private JDateChooser getJDateChooserDateTo() {
		if (jDateChooserDateTo == null) {
			jDateChooserDateTo = new JDateChooser();
			jDateChooserDateTo.setPreferredSize(new Dimension(200, 40));
			jDateChooserDateTo.setLocale(new Locale(GeneralData.LANGUAGE));
			jDateChooserDateTo.setDateFormatString(DATE_FORMAT);
			jDateChooserDateTo.setDate(new Date());
			jDateChooserDateTo.addPropertyChangeListener("date", evt -> {
				Date date = (Date) evt.getNewValue();
				jDateChooserDateTo.setDate(date);
			});
		}
		return jDateChooserDateTo;
	}

	public Date getDateToValue() {

		Date date = jDateChooserDateTo.getDate();
		if (date == null) {
			jDateChooserDateTo.setDate(new Date());
		}
		return jDateChooserDateTo.getDate();
	}

	public Date getDateFromValue() {

		Date date = jDateChooserDateFrom.getDate();
		if (date == null) {
			jDateChooserDateFrom.setDate(new Date());
		}
		return jDateChooserDateFrom.getDate();
	}
}
