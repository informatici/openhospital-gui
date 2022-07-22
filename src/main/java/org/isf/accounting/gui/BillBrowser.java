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
package org.isf.accounting.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY;
import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS;
import static org.isf.utils.Constants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS;
import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.accounting.gui.PatientBillEdit.PatientBillListener;
import org.isf.accounting.gui.totals.BalanceTotal;
import org.isf.accounting.gui.totals.PaymentsTotal;
import org.isf.accounting.gui.totals.UserTotal;
import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillPayments;
import org.isf.accounting.service.AccountingIoOperations;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.model.Patient;
import org.isf.stat.gui.report.GenericReportBill;
import org.isf.stat.gui.report.GenericReportFromDateToDate;
import org.isf.stat.gui.report.GenericReportPatient;
import org.isf.stat.gui.report.GenericReportUserInDate;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.JMonthChooser;
import org.isf.utils.jobjects.JYearChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lgooddatepicker.zinternaltools.WrapLayout;

/**
 * Browsing of table BILLS
 *
 * @author Mwithi
 */
public class BillBrowser extends ModalJFrame implements PatientBillListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(BillBrowser.class);

	@Override
	public void billInserted(AWTEvent event) {
		if (patientParent != null) {
			try {
				updateDataSet(dateFrom, dateTo, patientParent);
			} catch (OHServiceException ohServiceException) {
				LOGGER.error(ohServiceException.getMessage(), ohServiceException);
			}
		} else {
			updateDataSet(dateFrom, dateTo);
		}
		updateTables();
		updateTotals();
		if (event != null) {
			Bill billInserted = (Bill) event.getSource();
			if (billInserted != null) {
				int insertedId = billInserted.getId();
				IntStream.range(0, jTableBills.getRowCount()).forEach(i -> {
					Bill aBill = (Bill) jTableBills.getModel().getValueAt(i, -1);
					if (aBill.getId() == insertedId) {
						jTableBills.getSelectionModel().setSelectionInterval(i, i);
					}
				});
			}
			if (!isSingleUser && MainMenu.checkUserGrants("cashiersfilter")) {
				if (!users.contains(user)) {
					jComboUsers.addItem(user);
				}
				jComboUsers.setSelectedItem(user);
			}
		}
	}

	private static final long serialVersionUID = 1L;
	private JTabbedPane jTabbedPaneBills;
	private JTable jTableBills;
	private JScrollPane jScrollPaneBills;
	private JTable jTablePending;
	private JScrollPane jScrollPanePending;
	private JTable jTableClosed;
	private JScrollPane jScrollPaneClosed;
	private JTable jTableToday;
	private JTable jTablePeriod;
	private JTable jTableUser;
	private JPanel jPanelRange;
	private JPanel jPanelButtons;
	private JPanel jPanelSouth;
	private JPanel jPanelTotals;
	private JButton jButtonNew;
	private JButton jButtonEdit;
	private JButton jButtonPrintReceipt;
	private JButton jButtonDelete;
	private JButton jButtonClose;
	private Patient patientParent;
	private JTextField jAffiliatePersonJTextField = null;
	private JButton jButtonReport;
	private JComboBox<String> jComboUsers;
	private JTextField medicalJTextField = null;
	private JMonthChooser jComboBoxMonths;
	private JYearChooser jComboBoxYears;
	private JPanel panelSupRange;
	private GoodDateChooser jCalendarTo;
	private GoodDateChooser jCalendarFrom;
	private LocalDateTime dateFrom = LocalDateTime.now();
	private LocalDateTime dateTo = LocalDateTime.now();
	private LocalDateTime dateToday0 = LocalDate.now().atStartOfDay();
	private LocalDateTime dateToday24 = LocalDate.now().atTime(LocalTime.MAX);

	private JButton jButtonToday;

	private String[] columnNames = {
			MessageBundle.getMessage("angal.billbrowser.user.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.id.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.date.txt").toUpperCase(),
			MessageBundle.getMessage("angal.billbrowser.patientID.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.patient.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.amount.txt").toUpperCase(),
			MessageBundle.getMessage("angal.billbrowser.lastpayment.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.status.txt").toUpperCase(),
			MessageBundle.getMessage("angal.billbrowser.balance.col").toUpperCase()
	};
	private boolean isSingleUser = GeneralData.getGeneralData().getSINGLEUSER();
	private int[] columnsWidth = { 50, 50, 150, 50, 50, 100, 150, 50, 100 };
	private int[] maxWidth = { 50, 150, 150, 150, 200, 100, 150, 50, 100 };
	private boolean[] columnsResizable = { false, false, false, false, true, false, false, false, false };
	private Class<?>[] columnsClasses = { String.class, Integer.class, String.class, String.class, String.class, Double.class, String.class, String.class,
			Double.class };
	private boolean[] alignCenter = { false, true, true, true, false, false, true, true, false };
	private boolean[] boldCenter = { false, true, false, false, false, false, false, false, false };

	//Totals
	private BigDecimal totalToday;
	private BigDecimal balanceToday;
	private BigDecimal totalPeriod;
	private BigDecimal balancePeriod;
	private BigDecimal userToday;
	private BigDecimal userPeriod;
	private int month;
	private int year;

	//Bills & Payments
	private BillBrowserManager billManager = new BillBrowserManager(Context.getApplicationContext().getBean(AccountingIoOperations.class));
	private List<Bill> billPeriod;
	private List<BillPayments> paymentsPeriod;
	private List<Bill> billFromPayments;

	private String currencyCod;

	//Users
	private String user = UserBrowsingManager.getCurrentUser();
	private List<String> users;

	public BillBrowser() {
		try {
			this.currencyCod = Context.getApplicationContext().getBean(HospitalBrowsingManager.class).getHospitalCurrencyCod();
		} catch (OHServiceException ohServiceException) {
			this.currencyCod = null;
			MessageDialog.showExceptions(ohServiceException);
		}

		try {
			users = billManager.getUsers();
		} catch (OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
		updateDataSet();
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void initComponents() {
		add(getJPanelRange(), BorderLayout.NORTH);
		add(getJTabbedPaneBills(), BorderLayout.CENTER);
		add(getJPanelSouth(), BorderLayout.SOUTH);
		setTitle(MessageBundle.getMessage("angal.billbrowser.patientbillmanagment.title"));
		setMinimumSize(new Dimension(1150, 600));
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				//to free memory
				billPeriod.clear();
				users.clear();
				dispose();
			}
		});
		pack();
	}

	private JPanel getJPanelSouth() {
		if (jPanelSouth == null) {
			jPanelSouth = new JPanel();
			jPanelSouth.setLayout(new BoxLayout(jPanelSouth, BoxLayout.X_AXIS));
			jPanelSouth.add(getJPanelTotals());
			jPanelSouth.add(getJPanelButtons());
		}
		return jPanelSouth;
	}

	private JPanel getJPanelTotals() {
		if (jPanelTotals == null) {
			jPanelTotals = new JPanel();
			jPanelTotals.setLayout(new BoxLayout(jPanelTotals, BoxLayout.Y_AXIS));
			jPanelTotals.add(getJTableToday());
			jPanelTotals.add(getJTablePeriod());
			if (!isSingleUser) {
				jPanelTotals.add(getJTableUser());
			}
			updateTotals();
		}
		return jPanelTotals;
	}

    private GoodDateChooser getJCalendarFrom() {
        if (jCalendarFrom == null) {
            jCalendarFrom = new GoodDateChooser(LocalDate.now());
	        jCalendarFrom.addDateChangeListener(event -> {
		        LocalDate newDate = event.getNewDate();
		        if (newDate != null) {
					dateFrom = newDate.atStartOfDay();
					jButtonToday.setEnabled(true);
					billInserted(null);
		        }
	        });
        }
        return jCalendarFrom;
    }

	private GoodDateChooser getJCalendarTo() {
		if (jCalendarTo == null) {
			jCalendarTo = new GoodDateChooser(LocalDate.now());
			jCalendarTo.addDateChangeListener(event -> {
				LocalDate newDate = event.getNewDate();
				if (newDate != null) {
					dateTo = newDate.atTime(LocalTime.MAX);
					jButtonToday.setEnabled(true);
					billInserted(null);
				}
			});
		}
		return jCalendarTo;
	}

	private JButton getJButtonReport() {
		if (jButtonReport == null) {
			jButtonReport = new JButton(MessageBundle.getMessage("angal.billbrowser.report.btn"));
			jButtonReport.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.report.btn.key"));
			jButtonReport.addActionListener(actionEvent -> {
				List<String> options = new ArrayList<>();
				if (patientParent != null) {
					options.add(MessageBundle.getMessage("angal.billbrowser.patientstatement.txt"));
				}
				options.add(MessageBundle.getMessage("angal.billbrowser.todayclosure.txt"));
				options.add(MessageBundle.getMessage("angal.billbrowser.today.txt"));
				options.add(MessageBundle.getMessage("angal.billbrowser.period.txt"));
				options.add(MessageBundle.getMessage("angal.billbrowser.thismonth.txt"));
				options.add(MessageBundle.getMessage("angal.billbrowser.pickmonth.txt"));
				if (patientParent == null) {
					options.add(MessageBundle.getMessage("angal.billbrowser.patientstatement.txt"));
				}
				Icon icon = new ImageIcon("rsc/icons/calendar_dialog.png");
				String option = (String) MessageDialog.inputDialog(BillBrowser.this,
						icon,
						options.toArray(),
						options.get(0),
						"angal.billbrowser.pleaseselectareport.msg");
				if (option == null) {
					return;
				}

				String from = null;
				String to = null;

				int i = 0;

				if (patientParent != null && options.indexOf(option) == i) {
					new GenericReportPatient(patientParent.getCode(), GeneralData.PATIENTBILLSTATEMENT);
					return;
				}
				if (options.indexOf(option) == i) {

					from = TimeTools.formatDateTime(dateToday0, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
					to = TimeTools.formatDateTime(dateToday24, DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
					String user;
					if (isSingleUser) {
						user = "admin";
					} else {
						user = UserBrowsingManager.getCurrentUser();
					}
					new GenericReportUserInDate(from, to, user, "BillsReportUserInDate");
					return;
				}
				if (options.indexOf(option) == ++i) {
					from = TimeTools.formatDateTime(dateToday0, DATE_FORMAT_DD_MM_YYYY);
					to = TimeTools.formatDateTime(dateToday24, DATE_FORMAT_DD_MM_YYYY);
				}
				if (options.indexOf(option) == ++i) {
					from = TimeTools.formatDateTime(dateFrom, DATE_FORMAT_DD_MM_YYYY);
					to = TimeTools.formatDateTime(dateTo, DATE_FORMAT_DD_MM_YYYY);
				}
				if (options.indexOf(option) == ++i) {
					month = jComboBoxMonths.getMonth() + 1;
					LocalDateTime thisMonthFrom = dateFrom.toLocalDate()
							.withMonth(month)
							.withDayOfMonth(1)
							.atStartOfDay();
					LocalDateTime thisMonthTo = dateTo.toLocalDate()
							.withMonth(month)
							.withDayOfMonth(YearMonth.of(dateFrom.getYear(), month).lengthOfMonth())
							.atStartOfDay()
							.toLocalDate()
							.atTime(LocalTime.MAX);
					from = TimeTools.formatDateTime(thisMonthFrom, DATE_FORMAT_DD_MM_YYYY);
					to = TimeTools.formatDateTime(thisMonthTo, DATE_FORMAT_DD_MM_YYYY);
				}
				if (options.indexOf(option) == ++i) {
					icon = new ImageIcon("rsc/icons/calendar_dialog.png");
					int month;
					JMonthChooser monthChooser = new JMonthChooser();

					int r = JOptionPane.showConfirmDialog(BillBrowser.this,
							monthChooser,
							MessageBundle.getMessage("angal.billbrowser.month.txt"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE,
							icon);

					if (r == JOptionPane.OK_OPTION) {
						month = monthChooser.getMonth() + 1;
					} else {
						return;
					}

					LocalDateTime thisMonthFrom = dateFrom.toLocalDate()
							.withMonth(month)
							.withDayOfMonth(1)
							.atStartOfDay();
					LocalDateTime thisMonthTo = dateTo.toLocalDate()
							.withMonth(month)
							.withDayOfMonth(YearMonth.of(dateFrom.getYear(), month).lengthOfMonth())
							.atStartOfDay()
							.toLocalDate()
							.atTime(LocalTime.MAX);
					from = TimeTools.formatDateTime(thisMonthFrom, DATE_FORMAT_DD_MM_YYYY);
					to = TimeTools.formatDateTime(thisMonthTo, DATE_FORMAT_DD_MM_YYYY);
				}
				if (patientParent == null && options.indexOf(option) == ++i) {
					MessageDialog.error(BillBrowser.this, "angal.common.pleaseselectapatient.msg");
					return;
				}

				options = new ArrayList<>();
				options.add(MessageBundle.getMessage("angal.billbrowser.shortreportonlybaddebt.txt"));
				options.add(MessageBundle.getMessage("angal.billbrowser.fullreportallbills.txt"));

				icon = new ImageIcon("rsc/icons/list_dialog.png");
				option = (String) MessageDialog.inputDialog(BillBrowser.this,
						icon,
						options.toArray(),
						options.get(0),
						"angal.billbrowser.pleaseselectareport.msg");
				if (option == null) {
					return;
				}

				if (options.indexOf(option) == 0) {
					new GenericReportFromDateToDate(from, to, GeneralData.BILLSREPORTPENDING,
							MessageBundle.getMessage("angal.billbrowser.shortreportonlybaddebt.txt"), false);
				}
				if (options.indexOf(option) == 1) {
					new GenericReportFromDateToDate(from, to, GeneralData.BILLSREPORT,
							MessageBundle.getMessage("angal.billbrowser.fullreportallbills.txt"), false);
				}
			});
		}
		return jButtonReport;
	}

	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jButtonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jButtonClose.addActionListener(actionEvent -> {
				//to free memory
				billPeriod.clear();
				users.clear();
				dispose();
			});
		}
		return jButtonClose;
	}

	private boolean isOnlyOneSelected(JTable table) {
		int rowsSelected = table.getSelectedRowCount();
		if (rowsSelected > 1) {
			MessageDialog.error(BillBrowser.this, "angal.billbrowser.pleaseselectonlyonebill.msg");
			return false;
		}
		if (rowsSelected == 0) {
			MessageDialog.error(BillBrowser.this, "angal.billbrowser.pleaseselectabill.msg");
			return false;
		}
		return true;
	}

	private JButton getJButtonEdit() {
		if (jButtonEdit == null) {
			jButtonEdit = new JButton(MessageBundle.getMessage("angal.billbrowser.editbill.btn"));
			jButtonEdit.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.editbill.btn.key"));
			jButtonEdit.addActionListener(actionEvent -> {
				if (jScrollPaneBills.isShowing()) {
					if (!isOnlyOneSelected(jTableBills)) {
						return;
					}
					int rowSelected = jTableBills.getSelectedRow();
					Bill editBill = (Bill) jTableBills.getValueAt(rowSelected, -1);
					if (MainMenu.checkUserGrants("editclosedbills") || editBill.getStatus().equals("O")) { //$NON-NLS-1$
						PatientBillEdit pbe = new PatientBillEdit(BillBrowser.this, editBill, false);
						pbe.addPatientBillListener(BillBrowser.this);
						pbe.setVisible(true);
					} else {
						MessageDialog.error(BillBrowser.this, "angal.billbrowser.youcannoteditaclosedbill.msg");
						return;
					}
				}
				if (jScrollPanePending.isShowing()) {
					if (!isOnlyOneSelected(jTablePending)) {
						return;
					}
					int rowSelected = jTablePending.getSelectedRow();
					Bill editBill = (Bill) jTablePending.getValueAt(rowSelected, -1);
					PatientBillEdit pbe = new PatientBillEdit(BillBrowser.this, editBill, false);
					pbe.addPatientBillListener(BillBrowser.this);
					pbe.setVisible(true);
				}
				if (jScrollPaneClosed.isShowing()) {
					if (!isOnlyOneSelected(jTableClosed)) {
						return;
					}
					int rowSelected = jTableClosed.getSelectedRow();
					Bill editBill = (Bill) jTableClosed.getValueAt(rowSelected, -1);
					if (user.equals("admin")) { //$NON-NLS-1$
						PatientBillEdit pbe = new PatientBillEdit(BillBrowser.this, editBill, false);
						pbe.addPatientBillListener(BillBrowser.this);
						pbe.setVisible(true);
					} else {
						MessageDialog.error(BillBrowser.this, "angal.billbrowser.youcannoteditaclosedbill.msg");
					}
				}
			});
		}
		return jButtonEdit;
	}

	private JButton getJButtonPrintReceipt() {
		if (jButtonPrintReceipt == null) {
			jButtonPrintReceipt = new JButton(MessageBundle.getMessage("angal.billbrowser.receipt.btn"));
			jButtonPrintReceipt.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.receipt.btn.key"));
			jButtonPrintReceipt.addActionListener(actionEvent -> {
				try {
					if (jScrollPaneBills.isShowing()) {
						int rowsSelected = jTableBills.getSelectedRowCount();
						if (rowsSelected == 1) {
							int rowSelected = jTableBills.getSelectedRow();
							Bill editBill = (Bill) jTableBills.getValueAt(rowSelected, -1);
							if (editBill.getStatus().equals("C")) { //$NON-NLS-1$
								new GenericReportBill(editBill.getId(), GeneralData.PATIENTBILL, true, true);
							} else if (editBill.getStatus().equals("D")) {
								MessageDialog.error(BillBrowser.this, "angal.billbrowser.thebilldeleted.msg");
								return;
							} else if (editBill.getStatus().equals("O") && GeneralData.ALLOWPRINTOPENEDBILL) {
								new GenericReportBill(editBill.getId(), GeneralData.PATIENTBILL, true, true);
							} else {
								MessageDialog.error(BillBrowser.this, "angal.billbrowser.thebillisstillopen.msg");
								return;
							}
						} else if (rowsSelected > 1) {
							if (patientParent == null) {
								MessageDialog.error(BillBrowser.this, "angal.billbrowser.pleaseselectonlyonebill.msg");
								return;
							}
							Bill billTemp;
							int[] billIdIndex = jTableBills.getSelectedRows();
							List<Integer> billsIdList = new ArrayList<>();

							for (int idIndex : billIdIndex) {
								billTemp = (Bill) jTableBills.getValueAt(idIndex, -1);
								if (!billTemp.getStatus().equals("D")) {
									billsIdList.add(billTemp.getId());
								}
							}
							String fromDate = dateFrom.format(DATE_TIME_FORMATTER);
							String toDate = dateTo.format(DATE_TIME_FORMATTER);
							new GenericReportBill(billsIdList.get(0), GeneralData.PATIENTBILLGROUPED, patientParent, billsIdList, fromDate, toDate, true, true);
						} else {
							throw new Exception();
						}
					}
					if (jScrollPanePending.isShowing()) {
						int rowsSelected = jTablePending.getSelectedRowCount();
						if (rowsSelected == 1) {
							int rowSelected = jTablePending.getSelectedRow();
							Bill editBill = (Bill) jTablePending.getValueAt(rowSelected, -1);
							if (editBill.getStatus().equals("O") && GeneralData.ALLOWPRINTOPENEDBILL) {
								new GenericReportBill(editBill.getId(), GeneralData.PATIENTBILL, true, true);
							} else {
								PatientBillEdit pbe = new PatientBillEdit(BillBrowser.this, editBill, false);
								pbe.addPatientBillListener(BillBrowser.this);
								pbe.setVisible(true);
							}
						} else if (rowsSelected > 1) {
							if (patientParent == null) {
								MessageDialog.error(BillBrowser.this, "angal.billbrowser.pleaseselectonlyonebill.msg");
								return;
							} else if (GeneralData.ALLOWPRINTOPENEDBILL) {
								Bill billTemp;
								int[] billIdIndex = jTablePending.getSelectedRows();
								List<Integer> billsIdList = new ArrayList<>();

								for (int idIndex : billIdIndex) {
									billTemp = (Bill) jTablePending.getValueAt(idIndex, -1);
									billsIdList.add(billTemp.getId());
								}
								String fromDate = dateFrom.format(DATE_TIME_FORMATTER);
								String toDate = dateTo.format(DATE_TIME_FORMATTER);
								new GenericReportBill(billsIdList.get(0), GeneralData.PATIENTBILLGROUPED, patientParent, billsIdList, fromDate, toDate, true,
										true);
							} else {
								MessageDialog.error(BillBrowser.this, "angal.billbrowser.thebillisstillopen.msg");
								return;
							}
						} else {
							throw new Exception();
						}
					}
					if (jScrollPaneClosed.isShowing()) {
						int rowsSelected = jTableClosed.getSelectedRowCount();
						if (rowsSelected == 1) {
							int rowSelected = jTableClosed.getSelectedRow();
							Bill editBill = (Bill) jTableClosed.getValueAt(rowSelected, -1);
							new GenericReportBill(editBill.getId(), GeneralData.PATIENTBILL);
						} else if (rowsSelected > 1) {
							MessageDialog.error(BillBrowser.this, "angal.billbrowser.pleaseselectonlyonebill.msg");
						} else {
							throw new Exception();
						}
					}
				} catch (Exception ex) {
					MessageDialog.error(BillBrowser.this, "angal.billbrowser.pleaseselectabill.msg");
				}
			});
		}
		return jButtonPrintReceipt;
	}

	private void updateDataSet(LocalDateTime dateFrom, LocalDateTime dateTo, Patient patient) throws OHServiceException {
		/*
		 * Bills in the period
		 */
		billPeriod = billManager.getBills(dateFrom, dateTo, patient);

		/*
		 * Payments in the period
		 */
		paymentsPeriod = billManager.getPayments(dateFrom, dateTo, patient);

		/*
		 * Bills not in the period but with payments in the period
		 */
		billFromPayments = billManager.getBills(paymentsPeriod);
	}

	private JButton getJButtonNew() {
		if (jButtonNew == null) {
			jButtonNew = new JButton(MessageBundle.getMessage("angal.billbrowser.newbill.btn"));
			jButtonNew.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.newbill.btn.key"));
			jButtonNew.addActionListener(actionEvent -> {
				PatientBillEdit newBill = new PatientBillEdit(BillBrowser.this, new Bill(), true);
				newBill.addPatientBillListener(BillBrowser.this);
				newBill.setVisible(true);
			});
		}
		return jButtonNew;
	}

	private JButton getJButtonDelete() {
		if (jButtonDelete == null) {
			jButtonDelete = new JButton(MessageBundle.getMessage("angal.billbrowser.deletebill.btn"));
			jButtonDelete.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.deletebill.btn.key"));
			jButtonDelete.addActionListener(actionEvent -> {
				Bill deleteBill = null;
				int ok = JOptionPane.NO_OPTION;
				if (jScrollPaneBills.isShowing()) {
					if (!isOnlyOneSelected(jTableBills)) {
						return;
					}
					int rowSelected = jTableBills.getSelectedRow();
					deleteBill = (Bill) jTableBills.getValueAt(rowSelected, -1);
					ok = MessageDialog.yesNo(null, "angal.billbrowser.deletetheselectedbill.msg");
				}
				if (jScrollPanePending != null && jScrollPanePending.isShowing()) {
					if (!isOnlyOneSelected(jTablePending)) {
						return;
					}
					int rowSelected = jTablePending.getSelectedRow();
					deleteBill = (Bill) jTablePending.getValueAt(rowSelected, -1);
					ok = MessageDialog.yesNo(null, "angal.billbrowser.deletetheselectedbill.msg");
				}
				if (jScrollPaneClosed != null && jScrollPaneClosed.isShowing()) {
					if (!isOnlyOneSelected(jTableClosed)) {
						return;
					}
					int rowSelected = jTableClosed.getSelectedRow();
					deleteBill = (Bill) jTableClosed.getValueAt(rowSelected, -1);
					ok = MessageDialog.yesNo(null, "angal.billbrowser.deletetheselectedbill.msg");
				}
				if (ok == JOptionPane.YES_OPTION) {
					try {
						billManager.deleteBill(deleteBill);
					} catch (OHServiceException ohServiceException) {
						MessageDialog.showExceptions(ohServiceException);
					}
				}
				billInserted(null);
			});
		}
		return jButtonDelete;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel(new WrapLayout());
			if (MainMenu.checkUserGrants("btnbillnew")) {
				jPanelButtons.add(getJButtonNew());
			}
			if (MainMenu.checkUserGrants("btnbilledit")) {
				jPanelButtons.add(getJButtonEdit());
			}
			if (MainMenu.checkUserGrants("btnbilldelete")) {
				jPanelButtons.add(getJButtonDelete());
			}
			if (MainMenu.checkUserGrants("btnbillreceipt") && GeneralData.RECEIPTPRINTER) {
				jPanelButtons.add(getJButtonPrintReceipt());
			}
			if (MainMenu.checkUserGrants("btnbillreport")) {
				jPanelButtons.add(getJButtonReport());
			}
			jPanelButtons.add(getJButtonClose());
		}
		return jPanelButtons;
	}

	private JPanel getJPanelRange() {
		if (jPanelRange == null) {
			jPanelRange = new JPanel();
			jPanelRange.setLayout(new BorderLayout(0, 0));
			jPanelRange.add(getPanelSupRange(), BorderLayout.NORTH);
		}
		return jPanelRange;
	}

	private JPanel getPanelSupRange() {
		if (panelSupRange == null) {
			panelSupRange = new JPanel();
			if (!isSingleUser && MainMenu.checkUserGrants("cashiersfilter")) {
				panelSupRange.add(getJComboUsers());
			}
			panelSupRange.add(getJButtonToday());
			panelSupRange.add(new JLabel(MessageBundle.getMessage("angal.common.from.txt")));
			panelSupRange.add(getJCalendarFrom());
			panelSupRange.add(new JLabel(MessageBundle.getMessage("angal.common.to.txt")));
			panelSupRange.add(getJCalendarTo());
			panelSupRange.add(getJComboMonths());
			panelSupRange.add(getJComboYears());
			panelSupRange.add(getPanelChoosePatient());
		}
		return panelSupRange;
	}

	private JPanel getPanelChoosePatient() {
		JPanel priceListLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton jAffiliatePersonJButtonAdd = new JButton();
		jAffiliatePersonJButtonAdd.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
		jAffiliatePersonJButtonAdd.setToolTipText(MessageBundle.getMessage("angal.billbrowser.selectapatient.tooltip"));

		JButton jAffiliatePersonJButtonSupp = new JButton();
		jAffiliatePersonJButtonSupp.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
		jAffiliatePersonJButtonSupp.setToolTipText(MessageBundle.getMessage("angal.billbrowser.removeapatient.tooltip"));

		jAffiliatePersonJTextField = new JTextField(14);
		jAffiliatePersonJTextField.setEnabled(false);
		priceListLabelPanel.add(jAffiliatePersonJTextField);
		priceListLabelPanel.add(jAffiliatePersonJButtonAdd);
		priceListLabelPanel.add(jAffiliatePersonJButtonSupp);

		jAffiliatePersonJButtonAdd.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				SelectPatient selectPatient = new SelectPatient(BillBrowser.this, false, true);
				selectPatient.addSelectionListener(BillBrowser.this);
				selectPatient.setVisible(true);
				Patient pat = selectPatient.getPatient();

				try {
					patientSelected(pat);
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			}
		});

		jAffiliatePersonJButtonSupp.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				patientParent = null;
				jAffiliatePersonJTextField.setText("");
				billInserted(null);
			}
		});

		return priceListLabelPanel;
	}

	public void patientSelected(Patient patient) throws OHServiceException {
		patientParent = patient;
		jAffiliatePersonJTextField.setText(patientParent != null ? patientParent.getName() : "");

		if (patientParent != null) {
			if (medicalJTextField != null) {
				medicalJTextField.setText("");
			}
			updateDataSet(dateFrom, dateTo, patientParent);
			updateTables();
			updateTotals();
		}
	}

	public Patient getPatientParent() {
		return patientParent;
	}

	public void setPatientParent(Patient patientParent) {
		this.patientParent = patientParent;
	}

	private JComboBox<String> getJComboUsers() {
		if (jComboUsers == null) {
			jComboUsers = new JComboBox<>();

			for (String user : users) {
				jComboUsers.addItem(user);
			}

			if (users.contains(user)) {
				jComboUsers.setSelectedItem(user);
			}

			jComboUsers.addActionListener(actionEvent -> {
				user = (String) jComboUsers.getSelectedItem();
				jTableUser.setValueAt("<html><b>" + user + " " + MessageBundle.getMessage("angal.billbrowser.todaycolon.txt") + "</b></html>", 0, 0);
				jTableUser.setValueAt("<html><b>" + user + " " + MessageBundle.getMessage("angal.billbrowser.periodcolon.txt") + "</b></html>", 0, 2);
				updateTotals();
			});
		}
		return jComboUsers;
	}

	private JButton getJButtonToday() {
		if (jButtonToday == null) {
			jButtonToday = new JButton(MessageBundle.getMessage("angal.billbrowser.today.btn"));
			jButtonToday.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.today.btn.key"));
			jButtonToday.addActionListener(actionEvent -> {
				dateFrom = dateToday0;
				dateTo = dateToday24;
				jCalendarFrom.setDate(dateFrom.toLocalDate());
				jCalendarTo.setDate(dateTo.toLocalDate());
				jButtonToday.setEnabled(false);
			});
			jButtonToday.setEnabled(false);
		}
		return jButtonToday;
	}

	private JMonthChooser getJComboMonths() {
		if (jComboBoxMonths == null) {
			jComboBoxMonths = new JMonthChooser();
			jComboBoxMonths.addPropertyChangeListener("month", propertyChangeEvent -> {
				month = jComboBoxMonths.getMonth() + 1;
				dateFrom = dateFrom.toLocalDate()
						.withMonth(month)
						.withDayOfMonth(1)
						.atStartOfDay();
				dateTo = dateTo.toLocalDate()
						.withMonth(month)
						.withDayOfMonth(YearMonth.of(dateFrom.getYear(), month).lengthOfMonth())
						.atStartOfDay()
						.toLocalDate()
						.atTime(LocalTime.MAX);
				jCalendarFrom.setDate(dateFrom.toLocalDate());
				jCalendarTo.setDate(dateTo.toLocalDate());
			});
		}
		return jComboBoxMonths;
	}

	private JYearChooser getJComboYears() {
		if (jComboBoxYears == null) {
			jComboBoxYears = new JYearChooser();
			jComboBoxYears.getModel().addChangeListener(e -> {
				year = jComboBoxYears.getYear();
				dateFrom = LocalDate.now()
						.withYear(year)
						.withMonth(1)
						.withDayOfMonth(1)
						.atStartOfDay();
				dateTo = LocalDate.now()
						.withYear(year)
						.withMonth(12)
						.withDayOfMonth(YearMonth.of(year, 12).lengthOfMonth())
						.atStartOfDay()
						.toLocalDate()
						.atTime(LocalTime.MAX);
				jCalendarFrom.setDate(dateFrom.toLocalDate());
				jCalendarTo.setDate(dateTo.toLocalDate());
			});
		}
		return jComboBoxYears;
	}

	private JScrollPane getJScrollPaneClosed() {
		if (jScrollPaneClosed == null) {
			jScrollPaneClosed = new JScrollPane();
			jScrollPaneClosed.setViewportView(getJTableClosed());
		}
		return jScrollPaneClosed;
	}

	private JTable getJTableClosed() {
		if (jTableClosed == null) {
			jTableClosed = new JTable();
			jTableClosed.setModel(new BillTableModel("C")); //$NON-NLS-1$
			decorateTable(jTableClosed);
			jTableClosed.setAutoCreateColumnsFromModel(false);
			jTableClosed.setDefaultRenderer(String.class, new StringTableCellRenderer());
			jTableClosed.setDefaultRenderer(Integer.class, new IntegerTableCellRenderer());
			jTableClosed.setDefaultRenderer(Double.class, new DoubleTableCellRenderer());
		}
		return jTableClosed;
	}

	private JScrollPane getJScrollPanePending() {
		if (jScrollPanePending == null) {
			jScrollPanePending = new JScrollPane();
			jScrollPanePending.setViewportView(getJTablePending());
		}
		return jScrollPanePending;
	}

	private JTable getJTablePending() {
		if (jTablePending == null) {
			jTablePending = new JTable();
			jTablePending.setModel(new BillTableModel("O")); //$NON-NLS-1$
			decorateTable(jTablePending);
			jTablePending.setAutoCreateColumnsFromModel(false);
			jTablePending.setDefaultRenderer(String.class, new StringTableCellRenderer());
			jTablePending.setDefaultRenderer(Integer.class, new IntegerTableCellRenderer());
			jTablePending.setDefaultRenderer(Double.class, new DoubleTableCellRenderer());
		}
		return jTablePending;
	}

	private JScrollPane getJScrollPaneBills() {
		if (jScrollPaneBills == null) {
			jScrollPaneBills = new JScrollPane();
			jScrollPaneBills.setViewportView(getJTableBills());
		}
		return jScrollPaneBills;
	}

	private JTable getJTableBills() {
		if (jTableBills == null) {
			jTableBills = new JTable();
			jTableBills.setModel(new BillTableModel("ALL")); //$NON-NLS-1$
			decorateTable(jTableBills);
			jTableBills.setAutoCreateColumnsFromModel(false);
			jTableBills.setDefaultRenderer(String.class, new StringTableCellRenderer());
			jTableBills.setDefaultRenderer(Integer.class, new IntegerTableCellRenderer());
			jTableBills.setDefaultRenderer(Double.class, new DoubleTableCellRenderer());
		}
		return jTableBills;
	}

	private void decorateTable(JTable table) {
		IntStream.range(0, columnsWidth.length).forEach(idx -> {
			table.getColumnModel().getColumn(idx).setMinWidth(columnsWidth[idx]);
			if (!columnsResizable[idx]) {
				table.getColumnModel().getColumn(idx).setMaxWidth(maxWidth[idx]);
			}
			if (alignCenter[idx]) {
				table.getColumnModel().getColumn(idx).setCellRenderer(new StringCenterTableCellRenderer());
				if (boldCenter[idx]) {
					table.getColumnModel().getColumn(idx).setCellRenderer(new CenterBoldTableCellRenderer());
				}
			}
		});
	}

	private JTabbedPane getJTabbedPaneBills() {
		if (jTabbedPaneBills == null) {
			jTabbedPaneBills = new JTabbedPane();
			jTabbedPaneBills.addTab(MessageBundle.getMessage("angal.billbrowser.bills.title"), getJScrollPaneBills());
			jTabbedPaneBills.addTab(MessageBundle.getMessage("angal.billbrowser.pending.title"), getJScrollPanePending());
			jTabbedPaneBills.addTab(MessageBundle.getMessage("angal.billbrowser.closed.title"), getJScrollPaneClosed());
		}
		return jTabbedPaneBills;
	}

	private JTable getJTableToday() {
		if (jTableToday == null) {
			jTableToday = new JTable();
			jTableToday.setModel(
					new DefaultTableModel(new Object[][] {
							{
									"<html><b>" + MessageBundle.getMessage("angal.billbrowser.paidtodaycolon.txt") + "</b></html>",
									currencyCod,
									totalToday,
									"<html><b>" + MessageBundle.getMessage("angal.billbrowser.notpaidcolon.txt") + "</b></html>",
									currencyCod,
									balanceToday
							}
					},
							new String[] { "", "", "", "", "", "" }) {

						private static final long serialVersionUID = 1L;
						Class<?>[] types = new Class<?>[] { JLabel.class, JLabel.class, Double.class, JLabel.class, JLabel.class, Double.class };

						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return types[columnIndex];
						}

						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}
					});
			jTableToday.getColumnModel().getColumn(1).setMinWidth(3);
			jTableToday.getColumnModel().getColumn(4).setMinWidth(3);
			jTableToday.setRowSelectionAllowed(false);
			jTableToday.setGridColor(Color.WHITE);
		}
		return jTableToday;
	}

	private JTable getJTablePeriod() {
		if (jTablePeriod == null) {
			jTablePeriod = new JTable();
			jTablePeriod.setModel(new DefaultTableModel(
					new Object[][] {
							{
									"<html><b>" + MessageBundle.getMessage("angal.billbrowser.paidperiodcolon.txt") + "</b></html>",
									currencyCod,
									totalPeriod,
									"<html><b>" + MessageBundle.getMessage("angal.billbrowser.notpaidcolon.txt") + "</b></html>",
									currencyCod,
									balancePeriod }
					},
					new String[] { "", "", "", "", "", "" }) {

				private static final long serialVersionUID = 1L;
				Class<?>[] types = new Class<?>[] { JLabel.class, JLabel.class, Double.class, JLabel.class, JLabel.class, Double.class };

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
			jTablePeriod.getColumnModel().getColumn(1).setMinWidth(3);
			jTablePeriod.getColumnModel().getColumn(4).setMinWidth(3);
			jTablePeriod.setRowSelectionAllowed(false);
			jTablePeriod.setGridColor(Color.WHITE);

		}
		return jTablePeriod;
	}

	private JTable getJTableUser() {
		if (jTableUser == null) {
			jTableUser = new JTable();
			jTableUser.setModel(
					new DefaultTableModel(new Object[][]
						{ {
								"<html><b>" + user + " " + MessageBundle.getMessage("angal.billbrowser.todaycolon.txt") + "</b></html>",
								userToday,
								"<html><b>" + user + " " + MessageBundle.getMessage("angal.billbrowser.periodcolon.txt") + "</b></html>",
								userPeriod
						} },
							new String[] { "", "", "", "" }) {

						private static final long serialVersionUID = 1L;
						Class<?>[] types = new Class<?>[] { JLabel.class, Double.class, JLabel.class, Double.class };

						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return types[columnIndex];
						}

						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}
					});
			jTableUser.setRowSelectionAllowed(false);
			jTableUser.setGridColor(Color.WHITE);
		}
		return jTableUser;
	}

	private void updateTables() {
		jTableBills.setModel(new BillTableModel("ALL")); //$NON-NLS-1$
		jTablePending.setModel(new BillTableModel("O")); //$NON-NLS-1$
		jTableClosed.setModel(new BillTableModel("C")); //$NON-NLS-1$
	}

	private void updateDataSet() {
		updateDataSet(LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());
	}

	private void updateDataSet(LocalDateTime dateFrom, LocalDateTime dateTo) {
		try {
			/*
			 * Bills in the period
			 */
			billPeriod = billManager.getBills(dateFrom, dateTo);
		} catch (OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}

		try {
			/*
			 * Payments in the period
			 */
			paymentsPeriod = billManager.getPayments(dateFrom, dateTo);
		} catch (OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}

		try {
			/*
			 * Bills not in the period but with payments in the period
			 */
			billFromPayments = billManager.getBills(paymentsPeriod);
		} catch (OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
	}

	private void updateTotals() {
		List<Bill> billToday = null;
		List<BillPayments> paymentsToday = null;
		if (UserBrowsingManager.getCurrentUser().equals("admin")) {
			try {
				billToday = billManager.getBills(dateToday0, dateToday24);
				paymentsToday = billManager.getPayments(dateToday0, dateToday24);
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		} else {
			billToday = billPeriod;
			paymentsToday = paymentsPeriod;
		}

		totalPeriod = new BigDecimal(0);
		balancePeriod = new BigDecimal(0);
		totalToday = new BigDecimal(0);
		balanceToday = new BigDecimal(0);
		userToday = new BigDecimal(0);
		userPeriod = new BigDecimal(0);

		List<Integer> notDeletedBills = billPeriod.stream()
				.filter(bill -> !bill.getStatus().equals("D"))
				.map(Bill::getId)
				.collect(Collectors.toList());

		// Bills in range contribute for Not Paid (balance)
		balancePeriod = new BalanceTotal(billPeriod).getValue();

		// Bills in today contribute for Not Paid Today (balance)
		balanceToday = new BalanceTotal(billToday).getValue();

		// Payments in range contribute for Paid Period (total)
		userPeriod = new UserTotal(notDeletedBills, paymentsPeriod, user).getValue();
		totalPeriod = new PaymentsTotal(notDeletedBills, paymentsPeriod).getValue();

		// Payments in today contribute for Paid Today (total)
		userToday = new UserTotal(notDeletedBills, paymentsToday, user).getValue();
		totalToday = new PaymentsTotal(notDeletedBills, paymentsToday).getValue();

		jTableToday.setValueAt(totalToday, 0, 2);
		jTableToday.setValueAt(balanceToday, 0, 5);
		jTablePeriod.setValueAt(totalPeriod, 0, 2);
		jTablePeriod.setValueAt(balancePeriod, 0, 5);
		if (jTableUser != null) {
			jTableUser.setValueAt(userToday, 0, 1);
			jTableUser.setValueAt(userPeriod, 0, 3);
		}
	}

	public class BillTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private List<Bill> tableArray = new ArrayList<>();

		/*
		 * All Bills
		 */
		public BillTableModel(String status) {
			loadData(status);
		}

		private void loadData(String status) {
			try {
				tableArray = new BillDataLoader(billPeriod, billFromPayments, patientParent, billManager).loadBills(status);
			} catch (OHServiceException ohServiceException) {
				LOGGER.error("BillDataLoader error: ", ohServiceException);
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnsClasses[columnIndex];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override
		public int getRowCount() {
			if (tableArray == null) {
				return 0;
			}
			return tableArray.size();
		}

		@Override
		public Object getValueAt(int r, int c) {
			int index = -1;
			Bill thisBill = tableArray.get(r);
			if (c == index) {
				return thisBill;
			}
			if (c == ++index) {
				return thisBill.getUser();
			}
			if (c == ++index) {
				return thisBill.getId();
			}
			if (c == ++index) {
				return TimeTools.formatDateTime(thisBill.getDate(), DATE_FORMAT_DD_MM_YYYY_HH_MM_SS);
			}
			if (c == ++index) {
				int patID = thisBill.getBillPatient().getCode();
				return patID == 0 ? "" : String.valueOf(patID);
			}
			if (c == ++index) {
				return thisBill.getPatName();
			}
			if (c == ++index) {
				return thisBill.getAmount();
			}
			if (c == ++index) {
				return TimeTools.formatDateTime(thisBill.getUpdate(), DATE_FORMAT_DD_MM_YYYY_HH_MM_SS);
			}
			if (c == ++index) {
				return thisBill.getStatus();
			}
			if (c == ++index) {
				return thisBill.getBalance();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

	}

	private void formatCellByBillStatus(JTable table, int row, Component cell) {
		int statusColumn = table.getColumnModel().getColumnIndex(MessageBundle.getMessage("angal.common.status.txt").toUpperCase());
		if ((table.getValueAt(row, statusColumn)).equals("C")) { //$NON-NLS-1$
			cell.setForeground(Color.GRAY);
		}
		if ((table.getValueAt(row, statusColumn)).equals("D")) { //$NON-NLS-1$
			cell.setForeground(Color.RED);
		}
	}

	class StringTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			formatCellByBillStatus(table, row, cell);
			return cell;
		}
	}

	class StringCenterTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			formatCellByBillStatus(table, row, cell);
			return cell;
		}
	}

	class IntegerTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			cell.setFont(new Font(null, Font.BOLD, 12));
			setHorizontalAlignment(CENTER);
			formatCellByBillStatus(table, row, cell);
			return cell;
		}
	}

	class DoubleTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(RIGHT);
			formatCellByBillStatus(table, row, cell);
			return cell;
		}
	}

	class CenterBoldTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			cell.setFont(new Font(null, Font.BOLD, 12));
			formatCellByBillStatus(table, row, cell);
			return cell;
		}
	}

}
