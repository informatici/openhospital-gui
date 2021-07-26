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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

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
import org.isf.utils.jobjects.CustomJDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.time.TimeTools;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toedter.calendar.JMonthChooser;
import com.toedter.calendar.JYearChooser;

/**
 * Browsing of table BILLS
 *
 * @author Mwithi
 */
public class BillBrowser extends ModalJFrame implements PatientBillListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(BillBrowser.class);

	@Override
	public void billInserted(AWTEvent event){
		if (patientParent!=null) {
			try {
				updateDataSet(dateFrom, dateTo, patientParent);
			} catch (OHServiceException ohServiceException) {
				LOGGER.error(ohServiceException.getMessage(), ohServiceException);
			}
		} else{
			updateDataSet(dateFrom, dateTo);
		}
		updateTables();
		updateTotals();
		if (event != null) {
			Bill billInserted = (Bill) event.getSource();
			if (billInserted != null) {
				int insertedId = billInserted.getId();
				for (int i = 0; i < jTableBills.getRowCount(); i++) {
					Bill aBill = (Bill) jTableBills.getModel().getValueAt(i, -1);
					if (aBill.getId() == insertedId)
							jTableBills.getSelectionModel().setSelectionInterval(i, i);
				}
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
	private JButton jAffiliatePersonJButtonAdd  = null;
	private JButton jAffiliatePersonJButtonSupp  = null;
	private JTextField jAffiliatePersonJTextField  = null;
	private JButton jButtonReport;
	private JComboBox<String> jComboUsers;
	private JTextField medicalJTextField  = null;
	private JMonthChooser jComboBoxMonths;
	private JYearChooser jComboBoxYears;
	private JPanel panelSupRange;
	private JLabel jLabelTo;
	private JLabel jLabelFrom;
	private CustomJDateChooser jCalendarTo;
	private CustomJDateChooser jCalendarFrom;
	private GregorianCalendar dateFrom = new GregorianCalendar();
	private GregorianCalendar dateTo = new GregorianCalendar();
	private GregorianCalendar dateToday0 = TimeTools.getDateToday0();
	private GregorianCalendar dateToday24 = TimeTools.getDateToday24();

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
	private boolean[] columnShow = {MainMenu.checkUserGrants("cashiersfilter"), true, true, true, true, true, true, true, true};
	private int[] columnWidths = {50, 50, 150, 50, 50, 100, 150, 50, 100};
	private int[] maxWidth = {50, 150, 150, 150, 200, 100, 150, 50, 100};
	private boolean[] columnsResizable = {false, false, false, false, true, false, false, false, false};
	private Class<?>[] columnsClasses = {String.class, Integer.class, String.class, String.class, String.class, Double.class, String.class, String.class, Double.class};
	private boolean[] alignCenter = {false, true, true, true, false, false, true, true, false};
	private boolean[] boldCenter = {false, true, false, false, false, false, false, false, false};
	
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
	private ArrayList<Bill> billPeriod;
	private HashMap<Integer, Bill> mapBill = new HashMap<>();
	private ArrayList<BillPayments> paymentsPeriod;
	private ArrayList<Bill> billFromPayments;
	
	private String currencyCod;
	
	//Users
	private String user = UserBrowsingManager.getCurrentUser();
	private ArrayList<String> users;
	private boolean isSingleUser = GeneralData.getGeneralData().getSINGLEUSER();
	
	
	public BillBrowser() {
		try {
			this.currencyCod = Context.getApplicationContext().getBean(HospitalBrowsingManager.class).getHospitalCurrencyCod();
		} catch (OHServiceException ohServiceException) {
			this.currencyCod = null;
			MessageDialog.showExceptions(ohServiceException);
		}
		
		try {
			users = billManager.getUsers();
		} catch(OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
		updateDataSet();
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		//setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void initComponents() {
		add(getJPanelRange(), BorderLayout.NORTH);
		add(getJTabbedPaneBills(), BorderLayout.CENTER);
		add(getJPanelSouth(), BorderLayout.SOUTH);
		setTitle(MessageBundle.getMessage("angal.billbrowser.patientbillmanagment.title"));
		setMinimumSize(new Dimension(900, 600));
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				//to free memory
				billPeriod.clear();
				mapBill.clear();
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

	private JLabel getJLabelTo() {
		if (jLabelTo == null) {
			jLabelTo = new JLabel(MessageBundle.getMessage("angal.common.to.txt"));
		}
		return jLabelTo;
	}

	private CustomJDateChooser getJCalendarFrom() {
		if (jCalendarFrom == null) {
			jCalendarFrom = new CustomJDateChooser(dateToday0.getTime()); // Calendar
			jCalendarFrom.setLocale(new Locale(GeneralData.LANGUAGE));
			jCalendarFrom.setDateFormatString("dd/MM/yy"); //$NON-NLS-1$
			jCalendarFrom.getCalendarButton().setMnemonic(0);
			//$NON-NLS-1$
			jCalendarFrom.addPropertyChangeListener("date", evt -> {
				jCalendarFrom.setDate((Date) evt.getNewValue());
				dateFrom.setTime((Date) evt.getNewValue());
				dateFrom.set(Calendar.HOUR_OF_DAY, 0);
				dateFrom.set(Calendar.MINUTE, 0);
				dateFrom.set(Calendar.SECOND, 0);
				//dateToday0.setTime(dateFrom.getTime());
				jButtonToday.setEnabled(true);
				//billFilter();
				billInserted(null);
			});
		}			
		return jCalendarFrom;
	}

	private CustomJDateChooser getJCalendarTo() {
		if (jCalendarTo == null) {
			jCalendarTo = new CustomJDateChooser(dateToday24.getTime()); // Calendar
			jCalendarTo.setLocale(new Locale(GeneralData.LANGUAGE));
			jCalendarTo.setDateFormatString("dd/MM/yy"); //$NON-NLS-1$
			jCalendarTo.getCalendarButton().setMnemonic(0);
			jCalendarTo.addPropertyChangeListener("date", evt -> {
				jCalendarTo.setDate((Date) evt.getNewValue());
				dateTo.setTime((Date) evt.getNewValue());
				dateTo.set(Calendar.HOUR_OF_DAY, 23);
				dateTo.set(Calendar.MINUTE, 59);
				dateTo.set(Calendar.SECOND, 59);
				//dateToday24.setTime(dateTo.getTime());
				jButtonToday.setEnabled(true);
				billInserted(null);
			});
		}
		return jCalendarTo;
	}
	
	private JLabel getJLabelFrom() {
		if (jLabelFrom == null) {
			jLabelFrom = new JLabel(MessageBundle.getMessage("angal.common.from.txt"));
		}
		return jLabelFrom;
	}

	private JButton getJButtonReport() {
		if (jButtonReport == null) {
			jButtonReport = new JButton(MessageBundle.getMessage("angal.billbrowser.report.btn"));
			jButtonReport.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.report.btn.key"));
			jButtonReport.addActionListener(e -> {
				ArrayList<String> options = new ArrayList<>();
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

				GregorianCalendar from = null;
				GregorianCalendar to = null;

				int i = 0;

				if (patientParent != null && options.indexOf(option) == i) {
					new GenericReportPatient(patientParent.getCode(), GeneralData.PATIENTBILLSTATEMENT);
					return;
				}
				if (options.indexOf(option) == i) {

					String fromString = TimeTools.formatDateTimeReport(dateFrom);
					String toString = TimeTools.formatDateTimeReport(dateTo);
					String user;
					if (isSingleUser) {
						user = "admin";
					} else {
						user = UserBrowsingManager.getCurrentUser();
					}
					new GenericReportUserInDate(fromString, toString, user, "BillsReportUserInDate");
					return;
				}
				if (options.indexOf(option) == ++i) {

					from = TimeTools.getDateToday0();
					to = TimeTools.getDateToday0();
				}
				if (options.indexOf(option) == ++i) {

					from = dateFrom;
					to = dateTo;
				}
				if (options.indexOf(option) == ++i) {

					month = jComboBoxMonths.getMonth();
					from = dateFrom;
					to = dateTo;
					from.set(Calendar.MONTH, month);
					from.set(Calendar.DAY_OF_MONTH, 1);
					to.set(Calendar.MONTH, month);
					to.set(Calendar.DAY_OF_MONTH, dateFrom.getActualMaximum(Calendar.DAY_OF_MONTH));
				}
				if (options.indexOf(option) == ++i) {

					icon = new ImageIcon("rsc/icons/calendar_dialog.png");

					int month;
					JMonthChooser monthChooser = new JMonthChooser();
					monthChooser.setLocale(new Locale(GeneralData.LANGUAGE));

					int r = JOptionPane.showConfirmDialog(BillBrowser.this,
							monthChooser,
							MessageBundle.getMessage("angal.billbrowser.month.txt"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE,
							icon);

					if (r == JOptionPane.OK_OPTION) {
						month = monthChooser.getMonth();
					} else {
						return;
					}

					from = dateFrom;
					to = dateTo;
					from.set(Calendar.MONTH, month);
					from.set(Calendar.DAY_OF_MONTH, 1);
					to.set(Calendar.MONTH, month);
					to.set(Calendar.DAY_OF_MONTH, dateFrom.getActualMaximum(Calendar.DAY_OF_MONTH));
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
				if (option == null)
					return;

				if (options.indexOf(option) == 0) {
					new GenericReportFromDateToDate(
							TimeTools.formatDateTime(from, "dd/MM/yyyy"),
							TimeTools.formatDateTime(to, "dd/MM/yyyy"),
							GeneralData.BILLSREPORTPENDING,
							MessageBundle.getMessage("angal.billbrowser.shortreportonlybaddebt.txt"), false);
				}
				if (options.indexOf(option) == 1) {
					new GenericReportFromDateToDate(
							TimeTools.formatDateTime(from, "dd/MM/yyyy"),
							TimeTools.formatDateTime(to, "dd/MM/yyyy"),
							GeneralData.BILLSREPORT,
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
			jButtonClose.addActionListener(e -> {
				//to free memory
				billPeriod.clear();
				mapBill.clear();
				users.clear();
				dispose();
			});
		}
		return jButtonClose;
	}
	
	private boolean isOnlyOneSelected(JTable table) {
		int rowsSelected = table.getSelectedRowCount();
		if (rowsSelected > 1) {
			MessageDialog.error(BillBrowser.this,"angal.billbrowser.pleaseselectonlyonebill.msg");
			return false;
		} else if (rowsSelected == 0) {
			MessageDialog.error(BillBrowser.this, "angal.billbrowser.pleaseselectabill.msg");
			return false;
		}
		return true;
	}

	private JButton getJButtonEdit() {
		if (jButtonEdit == null) {
			jButtonEdit = new JButton(MessageBundle.getMessage("angal.billbrowser.editbill.btn"));
			jButtonEdit.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.editbill.btn.key"));
			jButtonEdit.addActionListener(e -> {
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
			jButtonPrintReceipt.addActionListener(e -> {
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
							ArrayList<Integer> billsIdList = new ArrayList<>();

							for (int idIndex : billIdIndex) {
								billTemp = (Bill) jTableBills.getValueAt(idIndex, -1);
								if (!billTemp.getStatus().equals("D")) {
									billsIdList.add(billTemp.getId());
								}
							}
							java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
							String fromDate = sdf.format(dateFrom.getTime());
							String toDate = sdf.format(dateTo.getTime());
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
								ArrayList<Integer> billsIdList = new ArrayList<>();

								for (int idIndex : billIdIndex) {
									billTemp = (Bill) jTablePending.getValueAt(idIndex, -1);
									//if (!billTemp.getStatus().equals("D")){
									billsIdList.add(billTemp.getId());
									//}
								}
								java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
								String fromDate = sdf.format(dateFrom.getTime());
								String toDate = sdf.format(dateTo.getTime());
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

	private void updateDataSet(GregorianCalendar dateFrom, GregorianCalendar dateTo, Patient patient) throws OHServiceException {
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
			jButtonNew.addActionListener(e -> {
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
			jButtonDelete.addActionListener(e -> {
				Bill deleteBill = null;
				int ok = JOptionPane.NO_OPTION;
				if (jScrollPaneBills.isShowing()) {
					if (!isOnlyOneSelected(jTableBills)) return;
					int rowSelected = jTableBills.getSelectedRow();
					deleteBill = (Bill)jTableBills.getValueAt(rowSelected, -1);
					ok = MessageDialog.yesNo(null, "angal.billbrowser.deletetheselectedbill.msg");
				}
				if (jScrollPanePending != null && jScrollPanePending.isShowing()) {
					if (!isOnlyOneSelected(jTablePending)) return;
					int rowSelected = jTablePending.getSelectedRow();
					deleteBill = (Bill)jTablePending.getValueAt(rowSelected, -1);
					ok = MessageDialog.yesNo(null, "angal.billbrowser.deletetheselectedbill.msg");
				}
				if (jScrollPaneClosed != null && jScrollPaneClosed.isShowing()) {
					if (!isOnlyOneSelected(jTableClosed)) return;
					int rowSelected = jTableClosed.getSelectedRow();
					deleteBill = (Bill)jTableClosed.getValueAt(rowSelected, -1);
					ok = MessageDialog.yesNo(null, "angal.billbrowser.deletetheselectedbill.msg");
				}
				if (ok == JOptionPane.YES_OPTION) {
					try {
						billManager.deleteBill(deleteBill);
					} catch(OHServiceException ohServiceException) {
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
			jPanelButtons = new JPanel();
			if (MainMenu.checkUserGrants("btnbillnew")) jPanelButtons.add(getJButtonNew());
			if (MainMenu.checkUserGrants("btnbilledit")) jPanelButtons.add(getJButtonEdit());
			if (MainMenu.checkUserGrants("btnbilldelete")) jPanelButtons.add(getJButtonDelete());
			if (MainMenu.checkUserGrants("btnbillreceipt") && GeneralData.RECEIPTPRINTER) jPanelButtons.add(getJButtonPrintReceipt());
			if (MainMenu.checkUserGrants("btnbillreport")) jPanelButtons.add(getJButtonReport());
			jPanelButtons.add(getJButtonClose());
		}
		return jPanelButtons;
	}

	private JPanel getJPanelRange() {
		if (jPanelRange == null) {
			jPanelRange = new JPanel();
				jPanelRange.setLayout(new BorderLayout(0, 0));
				jPanelRange.add(getPanelSupRange(), BorderLayout.NORTH);
				//if ( Param.bool("ALLOWFILTERBILLBYMEDICAL")){
				//	jPanelRange.add(getPanelChooseMedical(), BorderLayout.SOUTH);
				//}
		}
		return jPanelRange;
	}

	private JPanel getPanelSupRange() {
		if (panelSupRange == null) {
			panelSupRange = new JPanel();
			if (MainMenu.checkUserGrants("cashiersfilter")) {
				panelSupRange.add(getJComboUsers());
			}
			panelSupRange.add(getJButtonToday());
			panelSupRange.add(getJLabelFrom());
			panelSupRange.add(getJCalendarFrom());
			panelSupRange.add(getJLabelTo());
			panelSupRange.add(getJCalendarTo());
			panelSupRange.add(getJComboMonths());
			panelSupRange.add(getJComboYears());
			panelSupRange.add(getPanelChoosePatient());
		}
		return panelSupRange;
	}

	private JPanel getPanelChoosePatient() {
		JPanel priceListLabelPanel = new JPanel();
		//panelSupRange.add(priceListLabelPanel);
		priceListLabelPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		jAffiliatePersonJButtonAdd = new JButton();
		jAffiliatePersonJButtonAdd.addActionListener(e -> {
		});
		jAffiliatePersonJButtonAdd.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));

		jAffiliatePersonJButtonSupp = new JButton();
		jAffiliatePersonJButtonSupp.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));

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
				//garantUserChoose = null;
				//comboGaranti.setSelectedItem(null);
				jAffiliatePersonJTextField.setText("");
				billInserted(null);
			}
		});

		return priceListLabelPanel;
	}
	
	public void patientSelected(Patient patient) throws OHServiceException {
		patientParent = patient;
		jAffiliatePersonJTextField.setText(patientParent != null 
				? patientParent.getName() : "");
		
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
			for (String user : users) 
				jComboUsers.addItem(user);
			
			jComboUsers.addActionListener(arg0 -> {
				user = (String) jComboUsers.getSelectedItem();
				jTableUser.setValueAt("<html><b>"+user+"</b></html>", 0, 0);
				updateTotals();
			});
		}
		return jComboUsers;
	}
	
	private JButton getJButtonToday() {
		if (jButtonToday == null) {
			jButtonToday = new JButton(MessageBundle.getMessage("angal.billbrowser.today.btn"));
			jButtonToday.setMnemonic(MessageBundle.getMnemonic("angal.billbrowser.today.btn.key"));
			jButtonToday.addActionListener(e -> {
				dateFrom.setTime(dateToday0.getTime());
				dateTo.setTime(dateToday24.getTime());
				jCalendarFrom.setDate(dateFrom.getTime());
				jCalendarTo.setDate(dateTo.getTime());

				jButtonToday.setEnabled(false);
			});
			jButtonToday.setEnabled(false);
		}
		return jButtonToday;
	}

	private JMonthChooser getJComboMonths() {
		if (jComboBoxMonths == null) {
			jComboBoxMonths = new JMonthChooser();
			jComboBoxMonths.setLocale(new Locale(GeneralData.LANGUAGE));
			jComboBoxMonths.addPropertyChangeListener("month", evt -> {
				month = jComboBoxMonths.getMonth();
				dateFrom.set(Calendar.MONTH, month);
				dateFrom.set(Calendar.DAY_OF_MONTH, 1);
				dateTo.set(Calendar.MONTH, month);
				dateTo.set(Calendar.DAY_OF_MONTH, dateFrom.getActualMaximum(Calendar.DAY_OF_MONTH));

				jCalendarFrom.setDate(dateFrom.getTime());
				jCalendarTo.setDate(dateTo.getTime());
			});
		}
		return jComboBoxMonths;
	}

	private JYearChooser getJComboYears() {
		if (jComboBoxYears == null) {
			jComboBoxYears = new JYearChooser();
			jComboBoxYears.setLocale(new Locale(GeneralData.LANGUAGE));
			jComboBoxYears.addPropertyChangeListener("year", evt -> {
				year = jComboBoxYears.getYear();
				dateFrom.set(Calendar.YEAR, year);
				dateFrom.set(Calendar.MONTH, 1);
				dateFrom.set(Calendar.DAY_OF_YEAR, 1);
				dateTo.set(Calendar.YEAR, year);
				dateTo.set(Calendar.MONTH, 12);
				dateTo.set(Calendar.DAY_OF_YEAR, dateFrom.getActualMaximum(Calendar.DAY_OF_YEAR));
				jCalendarFrom.setDate(dateFrom.getTime());
				jCalendarTo.setDate(dateTo.getTime());
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
			for (int i = 0; i < columnWidths.length; i++) {
				jTableClosed.getColumnModel().getColumn(i).setMinWidth(columnWidths[i]);
				if (!columnsResizable[i]) {
					jTableClosed.getColumnModel().getColumn(i).setMaxWidth(maxWidth[i]);
				}
				if (alignCenter[i]) {
					jTableClosed.getColumnModel().getColumn(i).setCellRenderer(new StringCenterTableCellRenderer());
					if (boldCenter[i]) {
						jTableClosed.getColumnModel().getColumn(i).setCellRenderer(new CenterBoldTableCellRenderer());
					}
				}
				if (!columnShow[i]) {
					jTableClosed.getColumnModel().getColumn(i).setWidth(0);
					jTableClosed.getColumnModel().getColumn(i).setMinWidth(0);
					jTableClosed.getColumnModel().getColumn(i).setMaxWidth(0);
				}
			}
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
			for (int i = 0; i < columnWidths.length; i++) {
				jTablePending.getColumnModel().getColumn(i).setMinWidth(columnWidths[i]);
				if (!columnsResizable[i]) {
					jTablePending.getColumnModel().getColumn(i).setMaxWidth(maxWidth[i]);
				}
				if (alignCenter[i]) {
					jTablePending.getColumnModel().getColumn(i).setCellRenderer(new StringCenterTableCellRenderer());
					if (boldCenter[i]) {
						jTablePending.getColumnModel().getColumn(i).setCellRenderer(new CenterBoldTableCellRenderer());
					}
				}
				if (!columnShow[i]) {
					jTablePending.getColumnModel().getColumn(i).setWidth(0);
					jTablePending.getColumnModel().getColumn(i).setMinWidth(0);
					jTablePending.getColumnModel().getColumn(i).setMaxWidth(0);
				}
			}
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
			for (int i = 0; i < columnWidths.length; i++) {
				jTableBills.getColumnModel().getColumn(i).setMinWidth(columnWidths[i]);
				if (!columnsResizable[i]) {
					jTableBills.getColumnModel().getColumn(i).setMaxWidth(maxWidth[i]);
				}
				if (alignCenter[i]) {
					jTableBills.getColumnModel().getColumn(i).setCellRenderer(new StringCenterTableCellRenderer());
					if (boldCenter[i]) {
						jTableBills.getColumnModel().getColumn(i).setCellRenderer(new CenterBoldTableCellRenderer());
					}
				}
				if (!columnShow[i]) {
					jTableBills.getColumnModel().getColumn(i).setWidth(0);
					jTableBills.getColumnModel().getColumn(i).setMinWidth(0);
					jTableBills.getColumnModel().getColumn(i).setMaxWidth(0);
				}
			}
			jTableBills.setAutoCreateColumnsFromModel(false);
			jTableBills.setDefaultRenderer(String.class, new StringTableCellRenderer());
			jTableBills.setDefaultRenderer(Integer.class, new IntegerTableCellRenderer());
			jTableBills.setDefaultRenderer(Double.class, new DoubleTableCellRenderer());
		}
		return jTableBills;
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
									"<html><b>" + MessageBundle.getMessage("angal.billbrowser.todaycolon.txt").toUpperCase() + "</b></html>",
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
								"<html><b>"+MessageBundle.getMessage("angal.billbrowser.periodcolon.txt").toUpperCase()+"</b></html>",
								currencyCod,
								totalPeriod, 
								"<html><b>"+MessageBundle.getMessage("angal.billbrowser.notpaidcolon.txt")+"</b></html>",
								currencyCod,
								balancePeriod}
							}, 
							new String[] {"","","","","",""}) {
				private static final long serialVersionUID = 1L;
				Class<?>[] types = new Class<?>[] { JLabel.class, JLabel.class, Double.class, JLabel.class, JLabel.class, Double.class};
	
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
			jTableUser.setModel(new DefaultTableModel(new Object[][] {{"<html><b>"+MessageBundle.getMessage("angal.billbrowser.user.txt")+"</b></html>", userToday,
					"<html><b>"+MessageBundle.getMessage("angal.billbrowser.period.txt")+"</b></html>", userPeriod}},
					new String[] {"","","",""}) {
				private static final long serialVersionUID = 1L;
				Class<?>[] types = new Class<?>[] { JLabel.class, Double.class, JLabel.class, Double.class};
	
				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}

				@Override
				public boolean isCellEditable(int row, int column){
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
		updateDataSet(new DateTime().toDateMidnight().toGregorianCalendar(), new DateTime().toDateMidnight().plusDays(1).toGregorianCalendar());
	}
	
	private void updateDataSet(GregorianCalendar dateFrom, GregorianCalendar dateTo){
		try {
			/*
			 * Bills in the period
			 */
			billPeriod = billManager.getBills(dateFrom, dateTo);
		} catch(OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}

		try {
			/*
			 * Payments in the period
			 */
			paymentsPeriod = billManager.getPayments(dateFrom, dateTo);
		} catch(OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}

		try {
			/*
			 * Bills not in the period but with payments in the period
			 */
			billFromPayments = billManager.getBills(paymentsPeriod);
		} catch(OHServiceException ohServiceException) {
			MessageDialog.showExceptions(ohServiceException);
		}
	}
	
	private void updateTotals() {
		ArrayList<Bill> billToday = null;
		ArrayList<BillPayments> paymentsToday = null;
		if (UserBrowsingManager.getCurrentUser().equals("admin")) {
			try {
				billToday = billManager.getBills(dateToday0, dateToday24);
				paymentsToday = billManager.getPayments(dateToday0, dateToday24);
			} catch(OHServiceException ohServiceException) {
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
		
		ArrayList<Integer> notDeletedBills = new ArrayList<>();
				
		//Bills in range contribute for Not Paid (balance)
		for (Bill bill : billPeriod) {
			if (!bill.getStatus().equals("D")) {
				notDeletedBills.add(bill.getId());
				BigDecimal balance = new BigDecimal(Double.toString(bill.getBalance()));
				balancePeriod = balancePeriod.add(balance);
			}
		}
		
		//Payments in range contribute for Paid Period (total)
		for (BillPayments payment : paymentsPeriod) {
			if (notDeletedBills.contains(payment.getBill().getId())) {
				BigDecimal payAmount = new BigDecimal(Double.toString(payment.getAmount()));
				String payUser = payment.getUser();
				
				totalPeriod = totalPeriod.add(payAmount);
					
				if (!isSingleUser && payUser.equals(user))
					userPeriod = userPeriod.add(payAmount);
			}
		}
		
		//Bills in today contribute for Not Paid Today (balance)
		if (billToday != null){
			for (Bill bill : billToday) {
				if (!bill.getStatus().equals("D")) {
					BigDecimal balance = new BigDecimal(Double.toString(bill.getBalance()));
					balanceToday = balanceToday.add(balance);
				}
			}
		}
		
		//Payments in today contribute for Paid Today (total)
		if (paymentsToday != null){
			for (BillPayments payment : paymentsToday) {
				if (notDeletedBills.contains(payment.getBill().getId())) {
					BigDecimal payAmount = new BigDecimal(Double.toString(payment.getAmount()));
					String payUser = payment.getUser();
					totalToday = totalToday.add(payAmount);
					if (!isSingleUser && payUser.equals(user))
						userToday = userToday.add(payAmount);
				}
			}
		}
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
		private ArrayList<Bill> tableArray = new ArrayList<>();
		
		/*
		 * All Bills
		 */
		private ArrayList<Bill> billAll = new ArrayList<>();
		
		public BillTableModel(String status) {
			loadData(status);
		}
		
		private void loadData(String status) {
			
			tableArray.clear();
			mapBill.clear();
			mapping(status);
		}
		
		private void mapping(String status) {
			
			/*
			 * Mappings Bills in the period 
			 */
			for (Bill bill : billPeriod) {
				//mapBill.clear();
				mapBill.put(bill.getId(), bill);
			}
			
			/*
			 * Merging the two bills lists
			 */
			billAll.addAll(billPeriod);
			for (Bill bill : billFromPayments) {
				if (mapBill.get(bill.getId()) == null)
					billAll.add(bill);
			}
			
			if (status.equals("O")) {
				if (patientParent != null) {
					try {
						tableArray = billManager.getPendingBillsAffiliate(patientParent.getCode());
					} catch (OHServiceException ohServiceException) {
						MessageDialog.showExceptions(ohServiceException);
					}
				} else {
					for (Bill bill : billPeriod) {
						if (bill.getStatus().equals(status))
							tableArray.add(bill);
					}
				}
			}
			else if (status.equals("ALL")) {
				Collections.sort(billAll);
				tableArray = billAll;
			}
			else if (status.equals("C")) {
				for (Bill bill : billPeriod) {
					if (bill.getStatus().equals(status))
						tableArray.add(bill);
				}
			}
			tableArray.sort(Collections.reverseOrder());
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
				return TimeTools.formatDateTime(thisBill.getDate(), "dd/MM/yy - HH:mm:ss");
			}
			if (c == ++index) {
				int patID = thisBill.getBillPatient().getCode();
				return patID == 0 ? "" : String.valueOf(patID);
				//return thisBill.getId();
			}
			if (c == ++index) {
				return thisBill.getPatName();
			}
			if (c == ++index) {
				return thisBill.getAmount();
			}
			if (c == ++index) {
				return TimeTools.formatDateTime(thisBill.getUpdate(), "dd/MM/yy - HH:mm:ss");
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
	
	public boolean isSameDay(GregorianCalendar aDate, GregorianCalendar today) {
		return (aDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)) &&
			   (aDate.get(Calendar.MONTH) == today.get(Calendar.MONTH)) &&
			   (aDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH));
	}
	
	private void formatCellByBillStatus(JTable table, int row, Component cell) {
		int status_column = !GeneralData.getGeneralData().getSINGLEUSER() ? 7 : 6;
		if (((String)table.getValueAt(row, status_column)).equals("C")) { //$NON-NLS-1$
			cell.setForeground(Color.GRAY);
		}
		if (((String)table.getValueAt(row, status_column)).equals("D")) { //$NON-NLS-1$
			cell.setForeground(Color.RED);
		}
	}

	class StringTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {  
		   
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			cell.setForeground(Color.BLACK);
			formatCellByBillStatus(table, row, cell);
			return cell;
	   }
	}
	
	class StringCenterTableCellRenderer extends DefaultTableCellRenderer {  

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {  
		   
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			formatCellByBillStatus(table, row, cell);
			return cell;
	   }
	}
	
	class IntegerTableCellRenderer extends DefaultTableCellRenderer {  

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {  
		   
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
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
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {  
		   
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(RIGHT);
			formatCellByBillStatus(table, row, cell);
			return cell;
	   }
	}
	
	class CenterBoldTableCellRenderer extends DefaultTableCellRenderer {  

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {  
		    
			Component cell=super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			cell.setForeground(Color.BLACK);
			setHorizontalAlignment(CENTER);
			cell.setFont(new Font(null, Font.BOLD, 12));
			formatCellByBillStatus(table, row, cell);
			return cell;
	   }
	}
}
