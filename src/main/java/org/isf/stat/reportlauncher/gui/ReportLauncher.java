/*
 * Open Hospital (www.open-hospital.org) Copyright © 2006-2023 Informatici Senza
 * Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data
 * management.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.stat.reportlauncher.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.stat.dto.ReportLauncherDto;
import org.isf.stat.gui.report.GenericReportFromDateToDate;
import org.isf.stat.gui.report.GenericReportMY;
import org.isf.stat.manager.JasperReportsManager;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.isf.xmpp.gui.CommunicationFrame;
import org.isf.xmpp.manager.Interaction;

/**
 * ReportLauncher - lets the user pick and launch a stat report. The list of available reports (and the parameters each one prompts for, e.g. year/month or
 * from-date/to-date) is provided by the CORE {@link JasperReportsManager#getReportsList()}; this class only builds the Swing selection panel around it.
 */
public class ReportLauncher extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private JPanel jPanel;
	private JPanel jButtonPanel;
	private JButton jCloseButton;
	private JPanel jContentPanel;
	private JButton jLaunchReport;
	private JButton jCSVButton;
	private JPanel jMonthPanel;
	private JLabel jMonthLabel;
	private JComboBox<String> jMonthComboBox;
	private JLabel jYearLabel;
	private JComboBox<String> jYearComboBox;
	private JLabel jFromDateLabel;
	private JLabel jToDateLabel;
	private GoodDateChooser jToDateField;
	private GoodDateChooser jFromDateField;

	private JComboBox<String> jRptComboBox;

	private Map<String, ReportLauncherDto> reportMap;

	private JComboBox<String> shareWith;
	Interaction userOh;

	private final JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

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
				jButtonPanel.add(getComboShareReport(), null);
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
			rep1 = setMyBorder(rep1, MessageBundle.getMessage("angal.stat.parametersselectionframe") + ' ');
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
			reportMap = new HashMap<>();
			// the discovery of the available reports (folder scan, localized titles, prompt parameters) now lives in the CORE manager
			for (ReportLauncherDto report : jasperReportsManager.getReportsList()) {
				reportMap.put(report.getTitle(), report);
				jRptComboBox.addItem(report.getTitle());
			}

			jRptComboBox.addActionListener(actionEvent -> {
				if (actionEvent.getActionCommand() != null && actionEvent.getActionCommand().equalsIgnoreCase("comboBoxChanged")) {
					selectAction();
				}
			});

			// TODO: fix how the layout of the last two fields are done; adding
			// spaces is a hack
			jMonthLabel = new JLabel("               " + MessageBundle.getMessage("angal.stat.month"));

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

			jMonthComboBox.setSelectedIndex(month - 1);

			// TODO: fix how the layout of the last two fields are done; adding
			// spaces is a hack
			jYearLabel = new JLabel("                    " + MessageBundle.getMessage("angal.stat.year"));
			jYearComboBox = new JComboBox<>();

			for (int i = 0; i < 20; i++) {
				jYearComboBox.addItem(String.valueOf(year - i));
			}

			jFromDateLabel = new JLabel(MessageBundle.getMessage("angal.common.datefrom.label"));
			LocalDate defaultDate = LocalDate.now().minusDays(8);
			jFromDateField = new GoodDateChooser(defaultDate);
			jToDateLabel = new JLabel(MessageBundle.getMessage("angal.common.dateto.label"));
			defaultDate = defaultDate.plusDays(7);
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
		Object selectedReport = jRptComboBox.getSelectedItem();
		if (selectedReport == null) {
			return;
		}
		ReportLauncherDto report = reportMap.get(selectedReport.toString());
		if (report == null) {
			return;
		}
		List<String> userInputParamNames = report.getUserInputParameters();
		if (userInputParamNames.contains("fromdate") || userInputParamNames.contains("todate")) {
			jMonthComboBox.setVisible(false);
			jMonthLabel.setVisible(false);
			jYearComboBox.setVisible(false);
			jYearLabel.setVisible(false);
			jFromDateLabel.setVisible(true);
			jFromDateField.setVisible(true);
			jToDateLabel.setVisible(true);
			jToDateField.setVisible(true);
		} else if (userInputParamNames.contains("month") || userInputParamNames.contains("year")) {
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

	public static Date convertToDateUsingInstant(LocalDate date) {
		return Date.from(date.atStartOfDay()
						.atZone(ZoneId.systemDefault())
						.toInstant());
	}

	protected void generateReport(boolean toExcel) {
		Object selectedReport = jRptComboBox.getSelectedItem();
		if (selectedReport == null) {
			return;
		}
		ReportLauncherDto report = reportMap.get(selectedReport.toString());
		if (report == null) {
			return;
		}
		String reportTitle = selectedReport.toString();
		List<String> userInputParamNames = report.getUserInputParameters();
		if (userInputParamNames.contains("fromdate") || userInputParamNames.contains("todate")) {
			new GenericReportFromDateToDate(jFromDateField.getDate(), jToDateField.getDate(),
							report.getFolder(), report.getFileName(), reportTitle, toExcel);
			if (GeneralData.XMPPMODULEENABLED) {
				String user = (String) shareWith.getSelectedItem();
				CommunicationFrame frame = (CommunicationFrame) CommunicationFrame.getFrame();
				frame.sendMessage("011100100110010101110000011011110111001001110100 " +
								TimeTools.formatDateTime(jFromDateField.getDate().atStartOfDay(), DATE_FORMAT_DD_MM_YYYY) + ' ' +
								TimeTools.formatDateTime(jToDateField.getDate().atTime(LocalTime.MAX), DATE_FORMAT_DD_MM_YYYY) + ' ' +
								reportTitle, user, false);
			}
		} else {
			int month = jMonthComboBox.getSelectedIndex() + 1;
			int year = Integer.parseInt((String) jYearComboBox.getSelectedItem());

			new GenericReportMY(month, year, report.getFolder(), report.getFileName(), reportTitle, toExcel);
			if (GeneralData.XMPPMODULEENABLED) {
				String user = (String) shareWith.getSelectedItem();
				CommunicationFrame frame = (CommunicationFrame) CommunicationFrame.getFrame();
				frame.sendMessage("011100100110010101110000011011110111001001110100 " + month + ' ' + year + ' ' +
								reportTitle, user, false);
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
