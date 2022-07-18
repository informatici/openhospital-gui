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
package org.isf.stat.reportlauncher.gui;

import static org.isf.utils.Constants.DATE_FORMATTER;
import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.stat.gui.report.GenericReportFromDateToDate;
import org.isf.stat.gui.report.GenericReportMY;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.isf.xmpp.gui.CommunicationFrame;
import org.isf.xmpp.manager.Interaction;

/**
 * --------------------------------------------------------
 * ReportLauncher - launch all the reports that have as parameters year and month
 * 					the class expects the initialization through year, month, name of the report (without .jasper)
 * ---------------------------------------------------------
 * modification history
 * 01/01/2006 - rick - first version. launches HMIS1081 and HMIS1081
 * 11/11/2006 - ross - rendered generic (ad angal)
 * 16/11/2014 - eppesuig - show WAIT_CURSOR during generateReport()
 * -----------------------------------------------------------------
 */
public class ReportLauncher extends ModalJFrame{

	private static final long serialVersionUID = 1L;

	private static final int BUNDLE = 0;
	private static final int FILENAME = 1;
	private static final int TYPE = 2;

	private int pfrmExactWidth = 500;
	private int pfrmExactHeight = 165;
	private JPanel jPanel = null;
	private JPanel jButtonPanel = null;
	private JButton jCloseButton = null;
	private JPanel jContentPanel = null;
	private JButton jLaunchReport = null;
	private JButton jCSVButton = null;
	private JPanel jMonthPanel = null;
	private JLabel jMonthLabel = null;
	private JComboBox<String> jMonthComboBox = null;
	private JLabel jYearLabel = null;
	private JComboBox<String> jYearComboBox = null;
	private JLabel jFromDateLabel = null;
	private JLabel jToDateLabel = null;
	private GoodDateChooser jToDateField = null;
	private GoodDateChooser jFromDateField = null;

	private JComboBox<String> jRptComboBox = null;

	private String[][] reportMatrix = {
		{"angal.stat.registeredpatient", 				"OH001_RegisteredPatients", 										"twodates"},
		{"angal.stat.registeredpatientbyprovenance", 	"OH002_RegisteredPatientsByProvenance", 							"twodates"},
		{"angal.stat.registeredpatientbyageandsex", 	"OH003_RegisteredPatientsByAgeAndSex", 								"twodates"},
		{"angal.stat.incomesallbypricecodes", 			"OH004_IncomesAllByPriceCodes", 									"twodates"},
		{"angal.stat.outpatientcount", 					"OH005_opd_count_monthly_report", 									"monthyear"},
		{"angal.stat.outpatientdiagnoses", 				"OH006_opd_dis_monthly_report", 									"monthyear"},
		{"angal.stat.labmonthlybasic", 					"OH007_lab_monthly_report", 										"monthyear"},
		{"angal.stat.labmonthlyresult", 				"OH007_lab_result_report", 											"twodates"},
		{"angal.stat.labsummaryforopd", 				"OH008_lab_summary_for_opd", 										"monthyear"},
		{"angal.stat.inpatientreport", 					"OH009_InPatientReport", 											"twodates"},
		{"angal.stat.outpatientreport", 				"OH010_OutPatientReport", 											"twodates"},
		{"angal.stat.allIncomes",						"BillsReport",														"twodates"},
		{"angal.stat.allIncomespending",				"BillsReportPending",												"twodates"},
		{"angal.stat.allIncomesmonth",					"BillsReportMonthly",												"twodates"},
		{"angal.stat.pageonecensusinfo", 				"hmis108_cover", 													"twodatesfrommonthyear"},
		{"angal.stat.pageonereferrals", 				"hmis108_referrals", 												"monthyear"},
		{"angal.stat.pageoneoperations", 				"hmis108_operations", 												"monthyear"},
		{"angal.stat.inpatientdiagnosisin", 			"hmis108_adm_by_diagnosisIn", 										"monthyear"},
		{"angal.stat.inpatientdiagnosisout", 			"hmis108_adm_by_diagnosisOut", 										"monthyear"},
		{"angal.stat.opdattendance", 					"hmis105_opd_attendance", 											"monthyear"},
		{"angal.stat.opdreferrals", 					"hmis105_opd_referrals", 											"monthyear"},
		{"angal.stat.opdbydiagnosis", 					"hmis105_opd_by_diagnosis", 										"monthyear"},
		{"angal.stat.labmonthlyformatted", 				"hmis055b_lab_monthly_formatted", 									"monthyear"},
		{"angal.stat.weeklyepidemsurveil", 				"hmis033_weekly_epid_surv", 										"twodates"},
		{"angal.stat.weeklyepidemsurveilunder5", 		"hmis033_weekly_epid_surv_under_5", 								"twodates"},
		{"angal.stat.weeklyepidemsurveilover5", 		"hmis033_weekly_epid_surv_over_5", 									"twodates"},
		{"angal.stat.monthlyworkloadreportpage1", 		"MOH717_Monthly_Workload_Report_for_Hospitals_page1", 				"monthyear"},
		{"angal.stat.monthlyworkloadreportpage2", 		"MOH717_Monthly_Workload_Report_for_Hospitals_page2", 				"monthyear"},
		{"angal.stat.dailyopdmorbiditysummaryunder5", 	"MOH705A_Under_5_Years_Daily_Outpatient_Morbidity_Summary_Sheet", 	"monthyear"},
		{"angal.stat.dailyopdmorbiditysummaryover5", 	"MOH705B_Over_5_Years_Daily_Outpatient_Morbidity_Summary_Sheet", 	"monthyear"},
	};

	private JComboBox<String> shareWith = null;
	Interaction userOh = null;

	/**
	 * This is the default constructor
	 */
	public ReportLauncher() {
		super();
		this.setResizable(false);
		initialize();
		setVisible(true);
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.stat.reportlauncher.title"));
		this.setContentPane(getJPanel());
		selectAction();
		pack();
		setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel(new BorderLayout());
			jPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			jPanel.add(getJContentPanel(), BorderLayout.CENTER);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButtonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel(new FlowLayout());
			if (GeneralData.XMPPMODULEENABLED) {
				jButtonPanel.add(getComboShareReport(),null);
			}
			jButtonPanel.add(getJLaunchReportButton(), null);
			jButtonPanel.add(getJCSVButton(), null);
			jButtonPanel.add(getJCloseButton(), null);
		}
		return jButtonPanel;
	}

	private JComboBox<String> getComboShareReport() {
		userOh = new Interaction();
		Collection<String> contacts = userOh.getContactOnline();
		String shareReport = MessageBundle.getMessage("angal.stat.sharereportwithnobody.txt");
		contacts.add(shareReport);
		shareWith = new JComboBox(contacts.toArray());
		shareWith.setSelectedItem(shareReport);
		return shareWith;
	}

	/**
	 * This method initializes jCloseButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJCloseButton() {
		if (jCloseButton == null) {
			jCloseButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jCloseButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jCloseButton.addActionListener(actionEvent -> dispose());
		}
		return jCloseButton;
	}

	/**
	 * This method initializes jContentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPanel() {
		if (jContentPanel == null) {
			
			jContentPanel = new JPanel(new BorderLayout());
			
			JPanel rep1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			rep1 = setMyBorder(rep1, MessageBundle.getMessage("angal.stat.parametersselectionframe") + " ");
			rep1.add(getJParameterSelectionPanel());

			jContentPanel.add(rep1, BorderLayout.NORTH);
		}
		return jContentPanel;
	}

	private JPanel getJParameterSelectionPanel() {

		if (jMonthPanel == null) {

			jMonthPanel = new JPanel(new FlowLayout());

			LocalDate now = LocalDate.now();
			int month = now.getMonthValue();
			int year = now.getYear();

			JLabel jRptLabel = new JLabel(MessageBundle.getMessage("angal.stat.report"));

			jRptComboBox = new JComboBox<>();
			for (String[] matrix : reportMatrix) {
				jRptComboBox.addItem(MessageBundle.getMessage(matrix[BUNDLE]));
			}
			
			jRptComboBox.addActionListener(actionEvent -> {
				if (actionEvent.getActionCommand() != null && actionEvent.getActionCommand().equalsIgnoreCase("comboBoxChanged")) {
						selectAction();
				}
			});
			
			jMonthLabel = new JLabel("        " + MessageBundle.getMessage("angal.stat.month"));
			
			jMonthComboBox = new JComboBox<>();
			jMonthComboBox.addItem(MessageBundle.getMessage("angal.stat.january"));
			jMonthComboBox.addItem(MessageBundle.getMessage("angal.stat.february"));
			jMonthComboBox.addItem(MessageBundle.getMessage("angal.stat.march"));
			jMonthComboBox.addItem(MessageBundle.getMessage("angal.stat.april"));
			jMonthComboBox.addItem(MessageBundle.getMessage("angal.stat.may"));
			jMonthComboBox.addItem(MessageBundle.getMessage("angal.stat.june"));
			jMonthComboBox.addItem(MessageBundle.getMessage("angal.stat.july"));
			jMonthComboBox.addItem(MessageBundle.getMessage("angal.stat.august"));
			jMonthComboBox.addItem(MessageBundle.getMessage("angal.stat.september"));
			jMonthComboBox.addItem(MessageBundle.getMessage("angal.stat.october"));
			jMonthComboBox.addItem(MessageBundle.getMessage("angal.stat.november"));
			jMonthComboBox.addItem(MessageBundle.getMessage("angal.stat.december"));

			jMonthComboBox.setSelectedIndex(month);

			jYearLabel = new JLabel("        " + MessageBundle.getMessage("angal.stat.year"));
			jYearComboBox = new JComboBox<>();

			for (int i = 0; i < 20; i++) {
				jYearComboBox.addItem((year - i) + "");
			}
			
			jFromDateLabel = new JLabel(MessageBundle.getMessage("angal.stat.fromdate"));
			LocalDate defaultDate = LocalDate.now().minusMonths(8);
			jFromDateField = new GoodDateChooser(defaultDate);
			jToDateLabel = new JLabel(MessageBundle.getMessage("angal.stat.todate"));
			defaultDate = defaultDate.plusMonths(7);
			jToDateField = new GoodDateChooser(defaultDate);
			jToDateLabel.setVisible(false);
			jToDateField.setVisible(false);
			jFromDateLabel.setVisible(false);
			jFromDateField.setVisible(false);
			
			jMonthPanel.add(jRptLabel, null);
			jMonthPanel.add(jRptComboBox, null);
			jMonthPanel.add(jMonthLabel, null);
			jMonthPanel.add(jMonthComboBox, null);
			jMonthPanel.add(jYearLabel, null);
			jMonthPanel.add(jYearComboBox, null);
			jMonthPanel.add(jFromDateLabel, null);
			jMonthPanel.add(jFromDateField, null);
			jMonthPanel.add(jToDateLabel, null);
			jMonthPanel.add(jToDateField, null);
		}
		return jMonthPanel;
	}

	protected void selectAction() {
		int rptIndex = jRptComboBox.getSelectedIndex();
		String sParType = reportMatrix[rptIndex][TYPE];
		if (sParType.equalsIgnoreCase("twodates")) {
			jMonthComboBox.setVisible(false);
			jMonthLabel.setVisible(false);
			jYearComboBox.setVisible(false);
			jYearLabel.setVisible(false);
			jFromDateLabel.setVisible(true);
			jFromDateField.setVisible(true);
			jToDateLabel.setVisible(true);
			jToDateField.setVisible(true);
		}
		if (sParType.equalsIgnoreCase("twodatesfrommonthyear")) {
			jMonthComboBox.setVisible(true);
			jMonthLabel.setVisible(true);
			jYearComboBox.setVisible(true);
			jYearLabel.setVisible(true);
			jFromDateLabel.setVisible(false);
			jFromDateField.setVisible(false);
			jToDateLabel.setVisible(false);
			jToDateField.setVisible(false);
		}
		if (sParType.equalsIgnoreCase("monthyear")) {
			jMonthComboBox.setVisible(true);
			jMonthLabel.setVisible(true);
			jYearComboBox.setVisible(true);
			jYearLabel.setVisible(true);
			jFromDateLabel.setVisible(false);
			jFromDateField.setVisible(false);
			jToDateLabel.setVisible(false);
			jToDateField.setVisible(false);
		}
	}

	private JButton getJLaunchReportButton() {
		if (jLaunchReport == null) {
			jLaunchReport = new JButton(MessageBundle.getMessage("angal.common.launchreport.btn"));
			jLaunchReport.setMnemonic(MessageBundle.getMnemonic("angal.common.launchreport.btn.key"));
			jLaunchReport.setBounds(new Rectangle(15, 15, 91, 31));
			jLaunchReport.addActionListener(actionEvent -> generateReport(false));
		}
		return jLaunchReport;
	}
	
	private JButton getJCSVButton() {
		if (jCSVButton == null) {
			jCSVButton = new JButton(MessageBundle.getMessage("angal.common.excel.btn"));
			jCSVButton.setMnemonic(MessageBundle.getMnemonic("angal.common.excel.btn.key"));
			jCSVButton.setBounds(new Rectangle(15, 15, 91, 31));
			jCSVButton.addActionListener(actionEvent -> generateReport(true));
		}
		return jCSVButton;
	}
	
	protected void generateReport(boolean toExcel) {
		   
		int rptIndex = jRptComboBox.getSelectedIndex();
		int month = jMonthComboBox.getSelectedIndex()+1;
		int year = (Integer.parseInt((String)jYearComboBox.getSelectedItem()));
		String fromDate = TimeTools.formatDateTime(jFromDateField.getDate().atStartOfDay(), DATE_FORMAT_DD_MM_YYYY);
		String toDate = TimeTools.formatDateTime(jToDateField.getDate().atTime(LocalTime.MAX), DATE_FORMAT_DD_MM_YYYY);

		if (rptIndex >= 0) {
			String sParType = reportMatrix[rptIndex][TYPE];
			if (sParType.equalsIgnoreCase("twodates")) {
				new GenericReportFromDateToDate(fromDate, toDate, reportMatrix[rptIndex][FILENAME], MessageBundle.getMessage(reportMatrix[rptIndex][BUNDLE]),
						toExcel);
				if (GeneralData.XMPPMODULEENABLED) {
					String user = (String) shareWith.getSelectedItem();
					CommunicationFrame frame = (CommunicationFrame) CommunicationFrame.getFrame();
					frame.sendMessage("011100100110010101110000011011110111001001110100 " + fromDate + " " + toDate + " " + reportMatrix[rptIndex][FILENAME],
							user, false);
				}
			}
			if (sParType.equalsIgnoreCase("twodatesfrommonthyear")) {
				LocalDate now = LocalDate.now();
				now = now.withDayOfMonth(1);
				now = now.withMonth(month);
				now = now.withYear(year);
				fromDate = now.format(DATE_FORMATTER);

				now = now.withDayOfMonth(now.lengthOfMonth());
				toDate = now.format(DATE_FORMATTER);

				new GenericReportFromDateToDate(fromDate, toDate, reportMatrix[rptIndex][FILENAME], MessageBundle.getMessage(reportMatrix[rptIndex][BUNDLE]), toExcel);
				if (GeneralData.XMPPMODULEENABLED) {
					String user = (String) shareWith.getSelectedItem();
					CommunicationFrame frame = (CommunicationFrame) CommunicationFrame.getFrame();
					frame.sendMessage("011100100110010101110000011011110111001001110100 " + fromDate + " " + toDate + " " + reportMatrix[rptIndex][FILENAME],
							user, false);
				}
			}
			if (sParType.equalsIgnoreCase("monthyear")) {
				new GenericReportMY(month, year, reportMatrix[rptIndex][FILENAME], MessageBundle.getMessage(reportMatrix[rptIndex][BUNDLE]), toExcel);
				if (GeneralData.XMPPMODULEENABLED) {
					String user = (String) shareWith.getSelectedItem();
					CommunicationFrame frame = (CommunicationFrame) CommunicationFrame.getFrame();
					frame.sendMessage("011100100110010101110000011011110111001001110100 " + month + " " + year + " " + reportMatrix[rptIndex][FILENAME],
							user, false);
				}
			}
		}
	}

	/**
	 * Set a specific border+title to a panel
	 */
	private JPanel setMyBorder(JPanel panel, String title) {
		Border border = BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title), BorderFactory.createEmptyBorder(0, 0, 0, 0));
		panel.setBorder(border);
		return panel;
	}

}  
