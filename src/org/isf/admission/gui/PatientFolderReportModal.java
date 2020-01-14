package org.isf.admission.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.isf.generaldata.ExaminationParameters;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.stat.gui.report.GenericReportPatientVersion2;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.Converters;

import com.toedter.calendar.JDateChooser;

public class PatientFolderReportModal extends JDialog{
	private Integer patId;
	private JPanel jPanelChos;
	private JDateChooser jDateChooserDateFrom;
	private JDateChooser jDateChooserDateTo;
	private JPanel choosePanel;
	private JLabel chooselabel;
	private JComboBox chooseField;
	private JButton launchReportButton;
	private JButton closeButton;
	
	public PatientFolderReportModal(Integer code) {
		this.patId=code;
		initialize();
	}
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getJContentPane(), BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}
	private JPanel getJContentPane() {

		if (jPanelChos == null) {
			jPanelChos = new JPanel();
			
			GridBagLayout gbl_jPanelExamination = new GridBagLayout();
			gbl_jPanelExamination.columnWidths = new int[] { 0, 0, 0, 0 };
			gbl_jPanelExamination.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
			gbl_jPanelExamination.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0 };
			gbl_jPanelExamination.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
			jPanelChos.setLayout(gbl_jPanelExamination);

			JLabel jLabelDate = new JLabel("DateFrom"); //$NON-NLS-1$
			GridBagConstraints gbc_jLabelDate = new GridBagConstraints();
			gbc_jLabelDate.anchor = GridBagConstraints.WEST;
			gbc_jLabelDate.insets = new Insets(10, 5, 5, 5);
			gbc_jLabelDate.gridx = 0;
			gbc_jLabelDate.gridy = 0;
			jPanelChos.add(jLabelDate, gbc_jLabelDate);
			
			GridBagConstraints gbc_jDateChooserDate = new GridBagConstraints();
			gbc_jDateChooserDate.anchor = GridBagConstraints.WEST;
			gbc_jDateChooserDate.insets = new Insets(10, 5, 5, 5);
			gbc_jDateChooserDate.gridx = 1;
			gbc_jDateChooserDate.gridy = 0;
			jPanelChos.add(getJDateChooserDateFrom(), gbc_jDateChooserDate);
			
			JLabel jLabelDateto = new JLabel("Date to"); //$NON-NLS-1$
			GridBagConstraints gbc_jLabelDateto = new GridBagConstraints();
			gbc_jLabelDateto.anchor = GridBagConstraints.WEST;
			gbc_jLabelDateto.insets = new Insets(10, 5, 5, 5);
			gbc_jLabelDateto.gridx = 0;
			gbc_jLabelDateto.gridy = 1;
			jPanelChos.add(jLabelDateto, gbc_jLabelDateto);
			
			GridBagConstraints gbc_jDateChooserDateto = new GridBagConstraints();
			gbc_jDateChooserDateto.anchor = GridBagConstraints.WEST;
			gbc_jDateChooserDateto.insets = new Insets(10, 5, 5, 5);
			gbc_jDateChooserDateto.gridx = 1;
			gbc_jDateChooserDateto.gridy = 1;
			jPanelChos.add(getJDateChooserDateTo(), gbc_jDateChooserDateto);
			
			GridBagConstraints gbc_jSliderHeight = new GridBagConstraints();
			gbc_jSliderHeight.insets = new Insets(5, 5, 5, 5);
			gbc_jSliderHeight.fill = GridBagConstraints.HORIZONTAL;
			gbc_jSliderHeight.gridx = 0;
			gbc_jSliderHeight.gridy = 2;
			
			jPanelChos.add(getValue(), gbc_jSliderHeight);
			
			GridBagConstraints gbc_jCancelButtont = new GridBagConstraints();
			gbc_jCancelButtont.insets = new Insets(5, 5, 5, 5);
			gbc_jCancelButtont.fill = GridBagConstraints.HORIZONTAL;
			gbc_jCancelButtont.gridx = 0;
			gbc_jCancelButtont.gridy = 3;
			
			jPanelChos.add(getCloseButton(), gbc_jCancelButtont);


			GridBagConstraints gbc_jPrintButtont = new GridBagConstraints();
			gbc_jPrintButtont.insets = new Insets(5, 5, 5, 5);
			gbc_jPrintButtont.fill = GridBagConstraints.HORIZONTAL;
			gbc_jPrintButtont.gridx = 1;
			gbc_jPrintButtont.gridy = 3;
	
			jPanelChos.add(getPrintButton(), gbc_jPrintButtont);
		
		

	
			



		}
		return jPanelChos;
	
	}
	
	private JButton getPrintButton() {
		if (launchReportButton == null) {
			launchReportButton = new JButton();
			launchReportButton.setMnemonic(KeyEvent.VK_R);
			launchReportButton.setText(MessageBundle.getMessage("angal.admission.patientfolder.launchreport")); //$NON-NLS-1$
			launchReportButton.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// GenericReportMY rpt3 = new GenericReportMY(new Integer(6), new Integer(2008), "hmis108_adm_by_diagnosis_in");
					new GenericReportPatientVersion2(patId, getTypeField(), getDateFromValue(), getDateToValue(), GeneralData.PATIENTSHEET);
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
	private JPanel getValue() {

		if (choosePanel == null) {

			choosePanel = new JPanel();
			chooselabel = new JLabel();
			chooselabel.setText("Report For:");

			chooseField = new JComboBox();
			chooseField.addItem("All");
			chooseField.addItem("Admission");
			chooseField.addItem("Opd");
			chooseField.addItem("Drugs");
			chooseField.addItem("Examination");
			
			
		
			choosePanel.add(chooselabel);
			choosePanel.add(chooseField);
		}

		return choosePanel;
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
			//jDateChooserDate.setLocale(new Locale(GeneralData.LANGUAGE));
			jDateChooserDateFrom.setLocale(new Locale("en")); //$NON-NLS-1$
			jDateChooserDateFrom.setDateFormatString("dd/MM/yyyy"); //$NON-NLS-1$
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
			//jDateChooserDate.setLocale(new Locale(GeneralData.LANGUAGE));
			jDateChooserDateTo.setLocale(new Locale("en")); //$NON-NLS-1$
			jDateChooserDateTo.setDateFormatString("dd/MM/yyyy"); //$NON-NLS-1$
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
		return jDateChooserDateTo.getDate();
	}
	public Date getDateFromValue() {
		return jDateChooserDateFrom.getDate();
	}
}
