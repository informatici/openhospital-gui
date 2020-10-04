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
package org.isf.admission.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.stat.gui.report.GenericReportPatientVersion2;
import org.isf.utils.jobjects.ModalJFrame;

import com.toedter.calendar.JDateChooser;

public class PatientFolderReportModal extends ModalJFrame{
	private JFrame parent;
	private Integer patId;
	private JPanel jPanelChooser;
	private JDateChooser jDateChooserDateFrom;
	private JDateChooser jDateChooserDateTo;
	private JPanel choosePanel;
	private JLabel chooselabel;
	private JComboBox chooseField;
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
	private GregorianCalendar date;
	
	public PatientFolderReportModal(JFrame parent, Integer code, GregorianCalendar olderDate) {
		this.parent = parent;
		this.patId=code;
		this.date=olderDate;
		initialize();
	}
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getJContentPane(), BorderLayout.CENTER);
		this.setTitle(MessageBundle.getMessage("angal.medicals.report")); 
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		showAsModal(parent);
	}
	private JPanel getJContentPane() {

		if (jPanelChooser == null) {
			jPanelChooser = new JPanel();
			
			GridBagLayout gbl_jPanelExamination = new GridBagLayout();
			gbl_jPanelExamination.columnWidths = new int[] { 0, 0, 0, 0 };
			gbl_jPanelExamination.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
			gbl_jPanelExamination.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
			gbl_jPanelExamination.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
			jPanelChooser.setLayout(gbl_jPanelExamination);

			JLabel jLabelDate = new JLabel(MessageBundle.getMessage("angal.common.datefrom")); //$NON-NLS-1$
			GridBagConstraints gbc_jLabelDate = new GridBagConstraints();
			gbc_jLabelDate.anchor = GridBagConstraints.WEST;
			gbc_jLabelDate.insets = new Insets(10, 5, 5, 5);
			gbc_jLabelDate.gridx = 0;
			gbc_jLabelDate.gridy = 0;
			jPanelChooser.add(jLabelDate, gbc_jLabelDate);
			
			GridBagConstraints gbc_jDateChooserDate = new GridBagConstraints();
			gbc_jDateChooserDate.anchor = GridBagConstraints.WEST;
			gbc_jDateChooserDate.insets = new Insets(10, 5, 5, 5);
			gbc_jDateChooserDate.gridx = 1;
			gbc_jDateChooserDate.gridy = 0;
			jPanelChooser.add(getJDateChooserDateFrom(), gbc_jDateChooserDate);
			
			JLabel jLabelDateto = new JLabel(MessageBundle.getMessage("angal.common.dateto")); //$NON-NLS-1$
			GridBagConstraints gbc_jLabelDateto = new GridBagConstraints();
			gbc_jLabelDateto.anchor = GridBagConstraints.WEST;
			gbc_jLabelDateto.insets = new Insets(10, 5, 5, 5);
			gbc_jLabelDateto.gridx = 0;
			gbc_jLabelDateto.gridy = 1;
			jPanelChooser.add(jLabelDateto, gbc_jLabelDateto);
			
			GridBagConstraints gbc_jDateChooserDateto = new GridBagConstraints();
			gbc_jDateChooserDateto.anchor = GridBagConstraints.WEST;
			gbc_jDateChooserDateto.insets = new Insets(10, 5, 5, 5);
			gbc_jDateChooserDateto.gridx = 1;
			gbc_jDateChooserDateto.gridy = 1;
			jPanelChooser.add(getJDateChooserDateTo(), gbc_jDateChooserDateto);
			
			GridBagConstraints gbc_jSliderHeight = new GridBagConstraints();
			gbc_jSliderHeight.insets = new Insets(5, 5, 5, 5);
			gbc_jSliderHeight.fill = GridBagConstraints.WEST;
			gbc_jSliderHeight.gridx = 0;
			gbc_jSliderHeight.gridy = 2;
			jPanelChooser.add(getValueReport(), gbc_jSliderHeight);
			
			GridBagConstraints gbc_jCancelButtont = new GridBagConstraints();
			gbc_jCancelButtont.insets = new Insets(5, 5, 5, 5);
			gbc_jCancelButtont.fill = GridBagConstraints.HORIZONTAL;
			gbc_jCancelButtont.gridx = 0;
			gbc_jCancelButtont.gridy = 3;
			jPanelChooser.add(getCloseButton(), gbc_jCancelButtont);


			GridBagConstraints gbc_jPrintButtont = new GridBagConstraints();
			gbc_jPrintButtont.insets = new Insets(5, 5, 5, 5);
			gbc_jPrintButtont.fill = GridBagConstraints.HORIZONTAL;
			gbc_jPrintButtont.gridx = 1;
			gbc_jPrintButtont.gridy = 3;
			jPanelChooser.add(getPrintButton(), gbc_jPrintButtont);
		
		}
		return jPanelChooser;
	
	}
	
	private JButton getPrintButton() {
		if (launchReportButton == null) {
			launchReportButton = new JButton();
			launchReportButton.setMnemonic(KeyEvent.VK_R);
			launchReportButton.setText(MessageBundle.getMessage("angal.admission.patientfolder.launchreport")); //$NON-NLS-1$
			launchReportButton.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// GenericReportMY rpt3 = new GenericReportMY(new Integer(6), new Integer(2008), "hmis108_adm_by_diagnosis_in");
					new GenericReportPatientVersion2(patId, getParameterString(), getDateFromValue(), getDateToValue(), GeneralData.PATIENTSHEET);
				}
				
				protected String getParameterString() {
					StringBuilder parameterString = new StringBuilder();
					if (getAllValue()) {
						parameterString.append("All");
						return parameterString.toString();
					}
					if (getDrugsValue()) parameterString.append("Drugs");
					if (getExaminationValue()) parameterString.append("Examination");
					if (getAdmissionValue()) parameterString.append("Admission");
					if (getOpdValue()) parameterString.append("Opd");
					if (getLaboratoryValue()) parameterString.append("Laboratory");
					if (getOperationValue()) parameterString.append("Operations");
					
					return parameterString.toString();
				}
			});
		}
		return launchReportButton;
	}
	
	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton();
			closeButton.setMnemonic(KeyEvent.VK_C);
			closeButton.setText(MessageBundle.getMessage("angal.common.close"));   //$NON-NLS-1$
			closeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
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
			
			
			labelPanel.add(new JLabel(MessageBundle.getMessage("angal.patientfolder.reportfor") + ":"), BorderLayout.CENTER);
			
			
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
			allPanel.add(new JLabel(MessageBundle.getMessage("angal.menu.all")), BorderLayout.CENTER);
			allCheck.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					examinationCheck.setSelected(false);
					admissionCheck.setSelected(false);
					drugsCheck.setSelected(false);
					opdCheck.setSelected(false);
					operationCheck.setSelected(false);
					laboratoryCheck.setSelected(false);
				}
			});
			
		}
		return allPanel;
	}
	private JPanel getPanelExamination() {
		if (examinationPanel == null) {
			examinationPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			examinationCheck = new JCheckBox();

			
			examinationPanel.add(examinationCheck);
			examinationPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.examination")), BorderLayout.CENTER);
			examinationCheck.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					allCheck.setSelected(false);
					
				}
			});
			
		}
		return examinationPanel;
	}
	
	private JPanel getPanelOperations() {
		if (operationsPanel == null) {
			operationsPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			operationCheck = new JCheckBox();

			
			operationsPanel.add(operationCheck);
			operationsPanel.add(new JLabel(MessageBundle.getMessage("angal.menu.operation")), BorderLayout.CENTER);
			operationCheck.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					allCheck.setSelected(false);
					
				}
			});
		}
		return operationsPanel;
	}
	
	private JPanel getPanelLaboratory() {
		if (laboratoryPanel == null) {
			laboratoryPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			laboratoryCheck = new JCheckBox();

			
			laboratoryPanel.add(laboratoryCheck);
			laboratoryPanel.add(new JLabel(MessageBundle.getMessage("angal.menu.laboratory")), BorderLayout.CENTER);
			laboratoryCheck.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					allCheck.setSelected(false);
					
				}
			});
		}
		return laboratoryPanel;
	}
	
	private JPanel getPanelDrugs() {
		if (drugsPanel == null) {
			drugsPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			drugsCheck = new JCheckBox();

			
			drugsPanel.add(drugsCheck);
			drugsPanel.add(new JLabel(MessageBundle.getMessage("angal.patientfolder.tab.drugs")), BorderLayout.CENTER);
			drugsCheck.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					allCheck.setSelected(false);
					
				}
			});
		}
		return drugsPanel;
	}
	
	private JPanel getPanelOpd() {
		if (opdPanel == null) {
			opdPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			opdCheck = new JCheckBox();
			
			
			opdPanel.add(opdCheck);
			opdPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.opd")), BorderLayout.CENTER);
			opdCheck.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					allCheck.setSelected(false);
					
				}
			});
		}
		return opdPanel;
	}

	
	private JPanel getPanelAdmission() {
		if (admissionPanel == null) {
			admissionPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			admissionCheck = new JCheckBox();

			
			admissionPanel.add(admissionCheck);
			admissionPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.admission")), BorderLayout.CENTER);
			admissionCheck.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					allCheck.setSelected(false);
					
				}
			});
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
	public String getTypeField() {
		if(chooseField.getSelectedItem().equals("All")) {
			return "";
		} else {
		return (String) chooseField.getSelectedItem();
		}
	}
	
	
	private JDateChooser getJDateChooserDateFrom() {
		if (jDateChooserDateFrom == null) {
			jDateChooserDateFrom = new JDateChooser();
			jDateChooserDateFrom.setPreferredSize(new Dimension(200, 40));
			//jDateChooserDate.setLocale(new Locale(GeneralData.LANGUAGE));
			jDateChooserDateFrom.setLocale(new Locale(GeneralData.LANGUAGE)); //$NON-NLS-1$
			jDateChooserDateFrom.setDateFormatString("dd/MM/yyyy"); //$NON-NLS-1$
			jDateChooserDateFrom.setDate(date.getTime());
			jDateChooserDateFrom.addPropertyChangeListener("date", new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					Date date = (Date) evt.getNewValue();
					jDateChooserDateFrom.setDate(date);
				}
			});
		}
		return jDateChooserDateFrom;

}
	private JDateChooser getJDateChooserDateTo() {
		if (jDateChooserDateTo == null) {
			jDateChooserDateTo = new JDateChooser();
			jDateChooserDateTo.setPreferredSize(new Dimension(200, 40));
			jDateChooserDateTo.setLocale(new Locale(GeneralData.LANGUAGE)); //$NON-NLS-1$
			jDateChooserDateTo.setDateFormatString("dd/MM/yyyy"); //$NON-NLS-1$
			jDateChooserDateTo.setDate(new Date());
			
			jDateChooserDateTo.addPropertyChangeListener("date", new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					Date date = (Date) evt.getNewValue();
					jDateChooserDateTo.setDate(date);	
				}
			});
		}
		return jDateChooserDateTo;
	}
	public Date getDateToValue() {
		
		Date date3 = jDateChooserDateTo.getDate();
		if(date3!=null){
		 return jDateChooserDateTo.getDate();
		}else {	
			String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
			Date date2 = null;
			try {
				date2 = new SimpleDateFormat("dd/MM/yyyy").parse(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jDateChooserDateTo.setDate(date2);
			return jDateChooserDateTo.getDate();
		}
	}
		
	
	public Date getDateFromValue() {
		
		Date date3 = jDateChooserDateFrom.getDate();
		if(date3!=null){
		return jDateChooserDateFrom.getDate();
		}else {
			String date = "01/01/2000";
			Date date2 = null;
			try {
				date2 = new SimpleDateFormat("dd/MM/yyyy").parse(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jDateChooserDateFrom.setDate(date2);
			return jDateChooserDateFrom.getDate();
		}
	}
}