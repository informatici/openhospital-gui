package org.isf.admission.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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

public class PatientFolderReportModal extends ModalJFrame{
	private Integer patId;
	private JPanel jPanelChos;
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
	private JPanel allPanel;
	private JCheckBox allCheck;
	private JPanel labelPanel;
	private String date;
	
	public PatientFolderReportModal(Integer code, String dat) {
		this.patId=code;
		this.date=dat;
		initialize();
	}
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getJContentPane(), BorderLayout.CENTER);
		this.setTitle(MessageBundle.getMessage("angal.medicals.report")); 
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
			gbc_jSliderHeight.fill = GridBagConstraints.WEST;
			gbc_jSliderHeight.gridx = 0;
			gbc_jSliderHeight.gridy = 2;
			
			jPanelChos.add(getValueReport(), gbc_jSliderHeight);
			
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
					new GenericReportPatientVersion2(patId, getAllValue(), getAdmissionValue(), getOpdValue(), getExaminationValue(), getDrugsValue(), getDateFromValue(), getDateToValue(), GeneralData.PATIENTSHEET);
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
	
	private JPanel getValueReport() {

		if (choosePanel == null) {

			choosePanel = new JPanel();
			choosePanel.setLayout(new javax.swing.BoxLayout(choosePanel, javax.swing.BoxLayout.Y_AXIS));
			
			choosePanel.add(getPanelLabel());
			choosePanel.add(getPanelAll());
			choosePanel.add(getPanelAdmission());
			choosePanel.add(getPanelOpd());
			choosePanel.add(getPanelDrugs());
			choosePanel.add(getPanelExamination());
			
		}

		return choosePanel;
	}
	private JPanel getPanelLabel() {
		if (labelPanel == null) {
			labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));
			labelPanel.setAlignmentY(LEFT_ALIGNMENT);
			
			
			labelPanel.add(new JLabel("Report For:"), BorderLayout.CENTER);
			
			
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
			allPanel.add(new JLabel(MessageBundle.getMessage("angal.patvac.all")), BorderLayout.CENTER);
			allCheck.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					examinationCheck.setSelected(false);
					admissionCheck.setSelected(false);
					drugsCheck.setSelected(false);
					opdCheck.setSelected(false);
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
			examinationPanel.add(new JLabel(MessageBundle.getMessage("angal.opd.examination")), BorderLayout.CENTER);
			examinationCheck.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					allCheck.setSelected(false);
					
				}
			});
			
		}
		return examinationPanel;
	}
	
	private JPanel getPanelDrugs() {
		if (drugsPanel == null) {
			drugsPanel = new JPanel((new FlowLayout(FlowLayout.LEFT, 1, 1)));
			
			drugsCheck = new JCheckBox();

			
			drugsPanel.add(drugsCheck);
			drugsPanel.add(new JLabel(MessageBundle.getMessage("angal.medicalstockward.drugs")), BorderLayout.CENTER);
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
			Date date2 = null;
			DateFormat format = new SimpleDateFormat("dd/MM/yy");
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String correctdate = null;
			try {
				Date dat = format.parse(date);
				 correctdate = df.format(dat);
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				
				date2 = new SimpleDateFormat("dd/MM/yyyy").parse(correctdate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jDateChooserDateFrom.setDate(date2);
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
			//jDateChooserDate.setLocale(new Locale(GeneralData.LANGUAGE));
			jDateChooserDateTo.setLocale(new Locale(GeneralData.LANGUAGE)); //$NON-NLS-1$
			jDateChooserDateTo.setDateFormatString("dd/MM/yyyy"); //$NON-NLS-1$
//			String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());;
//			Date date2 = null;
//			try {
//				date2 = new SimpleDateFormat("dd/MM/yyyy").parse(date);
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
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
			String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());;
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
