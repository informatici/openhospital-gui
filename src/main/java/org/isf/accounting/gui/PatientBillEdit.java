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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.accounting.gui;

import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.isf.accounting.manager.BillBrowserManager;
import org.isf.accounting.model.Bill;
import org.isf.accounting.model.BillItems;
import org.isf.accounting.model.BillPayments;
import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.generaldata.TxtPrinter;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.priceslist.manager.PriceListManager;
import org.isf.priceslist.model.Price;
import org.isf.priceslist.model.PriceList;
import org.isf.pricesothers.manager.PricesOthersManager;
import org.isf.pricesothers.model.PricesOthers;
import org.isf.stat.gui.report.GenericReportBill;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.GoodDateTimeToggleChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.RememberDates;
import org.isf.utils.time.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import com.github.lgooddatepicker.zinternaltools.TimeChangeEvent;

/**
 * Create a single Patient Bill
 * it affects tables BILLS, BILLITEMS and BILLPAYMENTS
 *
 * @author Mwithi
 */
public class PatientBillEdit extends JDialog implements SelectionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(PatientBillEdit.class);
	private static final ImageIcon ADMISSION_ICON = new ImageIcon("rsc/icons/bed_icon.png");
	private static final String OPD_TEXT = MessageBundle.getMessage("angal.common.opd.txt");

	//LISTENER INTERFACE --------------------------------------------------------
	private EventListenerList patientBillListener = new EventListenerList();

	public interface PatientBillListener extends EventListener {
		void billInserted(AWTEvent aEvent);
	}

	public void addPatientBillListener(PatientBillListener l) {
		patientBillListener.add(PatientBillListener.class, l);
	}

	private void fireBillInserted(Bill aBill) {
		AWTEvent event = new AWTEvent(aBill, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = patientBillListener.getListeners(PatientBillListener.class);
		for (EventListener listener : listeners) {
			((PatientBillListener) listener).billInserted(event);
		}
	}

	@Override
	public void patientSelected(Patient patient) {
		setPatientSelected(patient);
		List<Bill> patientPendingBills = new ArrayList<>();
		Admission patientAdmission = admissionBrowserManager.getCurrentAdmission(patient);
		if (patientAdmission != null) {
			jLabelWard.setText(patientAdmission.getWard().getDescription());
			jLabelWard.setIcon(ADMISSION_ICON);
		} else {
			jLabelWard.setText(OPD_TEXT);
			jLabelWard.setIcon(null);
		}
		try {
			patientPendingBills = billBrowserManager.getPendingBills(patient.getCode());
		} catch (OHServiceException ohServiceException) {
			LOGGER.error(ohServiceException.getMessage(), ohServiceException);
		}
		if (patientPendingBills.isEmpty()) {
			// BILL
			thisBill.setBillPatient(patientSelected);
			thisBill.setIsPatient(true);
			thisBill.setPatName(patientSelected.getName());
			thisBill.setAdmission(patientAdmission);
			modified = true;
		} else {
			if (patientPendingBills.size() == 1) {
				if (GeneralData.ALLOWMULTIPLEOPENEDBILL) {
					int response = MessageDialog.yesNo(PatientBillEdit.this,
							"angal.newbill.thispatienthasapendingbilldoyouwanttocreateanother.msg");
					if (response == JOptionPane.YES_OPTION) {
						this.insert = true;
						thisBill.setBillPatient(patientSelected);
						thisBill.setIsPatient(true);
						thisBill.setPatName(patientSelected.getName());
						thisBill.setAdmission(patientAdmission);
						modified = true;
					} else {
						this.insert = false;
						setBill(patientPendingBills.get(0));

						/* ****** Check if it is same month ************** */
						//checkIfSameMonth();
						/* *********************************************** */
					}
				} else {
					MessageDialog.error(null, "angal.newbill.thispatienthasapendingbill.msg");
					this.insert = false;
					setBill(patientPendingBills.get(0));

					/* ****** Check if it is same month ************** */
					//checkIfSameMonth();
					/* *********************************************** */
				}
			} else {
				if (GeneralData.ALLOWMULTIPLEOPENEDBILL) {
					int response = MessageDialog.yesNo(PatientBillEdit.this,
							"angal.newbill.thispatienthasmorethanonependingbilldoyouwanttocreateanother.msg");
					if (response == JOptionPane.YES_OPTION) {
						this.insert = true;
						//thisBill.setPatID(patientSelected.getCode());
						thisBill.setBillPatient(patientSelected);
						thisBill.setIsPatient(true);
						thisBill.setPatName(patientSelected.getName());
						thisBill.setAdmission(patientAdmission);
						modified = true;
					} else if (response == JOptionPane.NO_OPTION) {
						// something must be proposed
						int resp = MessageDialog.yesNo(PatientBillEdit.this,
								"angal.newbill.thispatienthasmorethanonependingbilldoyouwanttoopenthelastpendingbill.msg");
						if (resp == JOptionPane.YES_OPTION) {
							this.insert = false;
							setBill(patientPendingBills.get(0));
							/* ****** Check if it is same month ************** */
							//checkIfSameMonth();
							/* *********************************************** */
						} else {
							dispose();
						}
					} else {
						return;
					}
				} else {
					MessageDialog.yesNo(null, "angal.admission.thereismorethanonependingbillforthispatientcontinue.msg");
					// TODO: the response is not checked; something needs to be done here
					return;
				}
			}
		}
		updateUI();
	}

	private static final long serialVersionUID = 1L;
	private JTable jTableBill;
	private JScrollPane jScrollPaneBill;
	private JButton jButtonAddMedical;
	private JButton jButtonAddOperation;
	private JButton jButtonAddExam;
	private JButton jButtonAddOther;
	private JButton jButtonAddPayment;
	private JPanel jPanelButtons;
	private JPanel jPanelDate;
	private JPanel jPanelPatient;
	private JTable jTablePayment;
	private JScrollPane jScrollPanePayment;
	private JTextField jTextFieldPatient;
	private JComboBox<PriceList> jComboBoxPriceList;
	private JPanel jPanelData;
	private JTable jTableTotal;
	private JScrollPane jScrollPaneTotal;
	private JTable jTableBigTotal;
	private JScrollPane jScrollPaneBigTotal;
	private JTable jTableBalance;
	private JScrollPane jScrollPaneBalance;
	private JPanel jPanelTop;
	private GoodDateTimeToggleChooser jCalendarDate;
	private JLabel jLabelDate;
	private JLabel jLabelUser;
	private JLabel jLabelPatient;
	private JButton jButtonRemoveItem;
	private JLabel jLabelPriceList;
	private JLabel jLabelWard;
	private JButton jButtonRemovePayment;
	private JButton jButtonAddRefund;
	private JPanel jPanelButtonsPayment;
	private JPanel jPanelButtonsBill;
	private JPanel jPanelButtonsActions;
	private JButton jButtonClose;
	private JButton jButtonPaid;
	private JButton jButtonPrintPayment;
	private JButton jButtonSave;
	private JButton jButtonBalance;
	private JButton jButtonCustom;
	private JButton jButtonPickPatient;
	private JButton jButtonTrashPatient;

	private static final Dimension PatientDimension = new Dimension(300, 20);
	private static final Dimension LabelsDimension = new Dimension(60, 20);
	private static final Dimension UserDimension = new Dimension(220, 20);
	private static final Dimension WardDimension = new Dimension(195, 20);
	private static final int PANEL_WIDTH = 450;
	private static final int BUTTON_WIDTH = 190;
	private static final int BUTTON_WIDTH_BILL = 190;
	private static final int BUTTON_WIDTH_PAYMENT = 190;
	private static final int PRICE_WIDTH = 190;
	private static final int CURRENCY_CODE_WIDTH = 40;
	private static final int QUANTITY_WIDTH = 40;
	private static final int BILL_HEIGHT = 200;
	private static final int TOTAL_HEIGHT = 20;
	private static final int BIG_TOTAL_HEIGHT = 20;
	private static final int PAYMENT_HEIGHT = 150;
	private static final int BALANCE_HEIGHT = 20;
	private static final int BUTTON_HEIGHT = 25;

	private BigDecimal total = new BigDecimal(0);
	private BigDecimal bigTotal = new BigDecimal(0);
	private BigDecimal balance = new BigDecimal(0);
	private int billID;
	private PriceList listSelected;
	private boolean insert;
	private boolean modified = false;
	private boolean keepDate = true;
	private boolean paid = false;
	private Bill thisBill;
	private Patient patientSelected;
	private LocalDateTime billDate = TimeTools.getNow();
	private LocalDateTime today = TimeTools.getNow();
	private String patientSelectedWard = "";

	private Object[] billClasses = {Price.class, Integer.class, Double.class};
	private String[] billColumnNames = {
			MessageBundle.getMessage("angal.newbill.item.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.qty.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.amount.txt").toUpperCase()
	};
	private Object[] paymentClasses = {Date.class, Double.class};

	private String currencyCod;

	//Prices and Lists (ALL)
	private PriceListManager priceListManager = Context.getApplicationContext().getBean(PriceListManager.class);
	private List<Price> prcArray;
	private List<PriceList> lstArray;

	//PricesOthers (ALL)
	private PricesOthersManager pricesOthersManager = Context.getApplicationContext().getBean(PricesOthersManager.class);
	private List<PricesOthers> othPrices;

	//Items and Payments (ALL)
	private BillBrowserManager billBrowserManager = Context.getApplicationContext().getBean(BillBrowserManager.class);
	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private AdmissionBrowserManager admissionBrowserManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);

	//Prices, Items and Payments for the tables
	private List<BillItems> billItems = new ArrayList<>();
	private List<BillPayments> payItems = new ArrayList<>();
	private List<Price> prcListArray = new ArrayList<>();
	private int billItemsSaved;
	private int payItemsSaved;

	//User
	private String user = UserBrowsingManager.getCurrentUser();

	public PatientBillEdit() {
		initCurrencyCod();
		PatientBillEdit newBill = new PatientBillEdit(null, new Bill(), true);
		newBill.setVisible(true);
		try {
			prcArray = priceListManager.getPrices();
			lstArray = priceListManager.getLists();
			othPrices = pricesOthersManager.getOthers();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, PatientBillEdit.this);
		}
	}

	//TODO: to harmonize constructors
	public PatientBillEdit(JFrame owner, Patient patient) {
		initCurrencyCod();
		Bill bill = new Bill();
		bill.setIsPatient(true);
		bill.setBillPatient(patient);
		bill.setPatName(patient.getName());
		bill.setAdmission(admissionBrowserManager.getCurrentAdmission(patient));
		PatientBillEdit newBill = new PatientBillEdit(owner, bill, true);
		newBill.setPatientSelected(patient);
		newBill.setVisible(true);
	}

	public PatientBillEdit(JFrame owner, Bill bill, boolean inserting) {
		super(owner, true);
		initCurrencyCod();
		this.insert = inserting;
		try {
			prcArray = priceListManager.getPrices();
			lstArray = priceListManager.getLists();
			othPrices = pricesOthersManager.getOthers();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, PatientBillEdit.this);
		}
		setBill(bill);
		initComponents();
		updateTotals();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
	}

	private void initCurrencyCod() {
		try {
			this.currencyCod = Context.getApplicationContext().getBean(HospitalBrowsingManager.class).getHospitalCurrencyCod();
		} catch (OHServiceException e) {
			this.currencyCod = null;
			OHServiceExceptionUtil.showMessages(e, PatientBillEdit.this);
		}
	}

	private void setBill(Bill bill) {
		try {
			this.thisBill = (Bill) bill.clone();
			this.thisBill.setAdmission(bill.getAdmission());
			setAdmissionWard(bill.getAdmission());
			this.thisBill.setBillPatient(bill.getBillPatient());
			this.thisBill.setPriceList(bill.getPriceList());
		} catch (CloneNotSupportedException cnse) {
			LOGGER.debug("CloneNotSupportedException", cnse);
		}
		billDate = bill.getDate();
		try {
			billItems = billBrowserManager.getItems(thisBill.getId());
			payItems = billBrowserManager.getPayments(thisBill.getId());
			othPrices = pricesOthersManager.getOthers();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e, PatientBillEdit.this);
		}
		billItemsSaved = billItems.size();
		payItemsSaved = payItems.size();
		if (!this.insert) {
			checkBill();
		}
	}

	private void initComponents() {
		add(getJPanelTop(), BorderLayout.NORTH);
		add(getJPanelData(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.EAST);
		if (this.insert) {
			setTitle(MessageBundle.getMessage("angal.patientbill.newpatientbill.title"));
		} else {
			setTitle(MessageBundle.formatMessage("angal.patientbill.editpatientbill.fmt.title", thisBill.getId()));
		}
		pack();
	}

	//check if PriceList and Patient still exist
	private void checkBill() {
		if (thisBill.isList()) {
			Optional<PriceList> priceList = lstArray.stream()
					.filter(pl -> pl.getId() == thisBill.getPriceList().getId())
					.findFirst();
			priceList.ifPresent(pl -> listSelected = pl);

			if (!priceList.isPresent()) { //PriceList not found
				Icon icon = new ImageIcon("rsc/icons/list_dialog.png"); //$NON-NLS-1$
				PriceList list = (PriceList)JOptionPane.showInputDialog(
						PatientBillEdit.this,
						MessageBundle.getMessage("angal.newbill.thepricelistassociatedwiththisbillnolongerexists.msg"),
						MessageBundle.getMessage("angal.newbill.selectapricelist.title"),
						JOptionPane.OK_OPTION,
						icon,
						lstArray.toArray(),
						"");
				if (list == null) {
					MessageDialog.warning(PatientBillEdit.this, "angal.newbill.nopricelistselectedwillbeused.fmt.msg",
							lstArray.get(0).getName());
					list = lstArray.get(0);
				}
				thisBill.setPriceList(list);
				thisBill.setListName(list.getName());
				modified = true;
			} else { // PriceList is found

				// PriceList changes (rarely changed):
				//  * could be changed the PriceList name (rare, showed in the Bill and persisted in BLL_LST_NAME) → no prompt
				//  * PriceList description (rare, not persisted in BILL) → no prompt
				//  * PriceList currency (very rare, showed aside the amounts) → only prompt

				PriceList thisBillPriceList = thisBill.getPriceList();
				PriceList matchingPriceList = priceList.get();
				if (!thisBillPriceList.getName().equals(matchingPriceList.getName())) {
					thisBillPriceList.setName(matchingPriceList.getName());
					modified = true;
				}
				if (!thisBillPriceList.getDescription().equals(matchingPriceList.getDescription())) {
					thisBillPriceList.setDescription(matchingPriceList.getDescription());
					modified = true;
				}
				if (!thisBillPriceList.getCurrency().equals(matchingPriceList.getCurrency())) {
					int ok = MessageDialog.yesNo(null, "angal.newbill.thecurrencyinthepricelisthaschangeditwasbutisnowdoyouwantthenewvalue.fmt.msg",
							thisBillPriceList.getCurrency(), matchingPriceList.getCurrency());
					if (ok == JOptionPane.YES_OPTION) {
						thisBillPriceList.setCurrency(matchingPriceList.getCurrency());
						modified = true;
					}
				}
			}
		}

		if (thisBill.isPatient()) {
			Patient patient = null;
			try {
				patient = patientBrowserManager.getPatientById(thisBill.getBillPatient().getCode());
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
			if (patient != null) {
				setPatientSelected(patient);

				// Patient changes (rarely changed):
				//   * could be changed the Patient name (very rare, showed in the Bill and persisted in BLL_PAT_NAME) → no prompt

				if (!thisBill.getBillPatient().getFirstName().equals(patient.getFirstName())) {
					thisBill.getBillPatient().setFirstName(patient.getFirstName());
					modified = true;
				}
				if (!thisBill.getBillPatient().getSecondName().equals(patient.getSecondName())) {
					thisBill.getBillPatient().setSecondName(patient.getSecondName());
					modified = true;
				}

				// Admission changes (frequently changed):
				//   * could be changed the Ward (rare, showed in the Bill) → only prompt
				//   * the status of the admission (very possible, discharged, showed in the Bill) → prompt and ask
				//   * the Admission ID (rare, the patient is discharged and readmitted while editing the Bill) → prompt and ask

				Admission currentAdmission = admissionBrowserManager.getCurrentAdmission(patient);
				
				Icon icon = UIManager.getIcon("OptionPane.warningIcon");
				Admission thisBillAdmission = thisBill.getAdmission();
				if (thisBillAdmission  == null && currentAdmission != null) {
					int ok = MessageDialog.yesNo(PatientBillEdit.this, icon, "angal.newbill.thispatientisadmittednowdoyouwanttolinkthisbilltothecurrentadmission.msg");
					if (ok == JOptionPane.OK_OPTION) {
						thisBill.setAdmission(currentAdmission);
						modified = true;
					}
				}
				if (thisBill.getAdmission() != null && currentAdmission == null) {
					int ok = MessageDialog.yesNo(PatientBillEdit.this, icon, "angal.newbill.thispatientisnolongeradmitteddoyouwanttounlinkthisbillfromthepreviousadmission.msg");
					if (ok == JOptionPane.OK_OPTION) {
						thisBill.setAdmission(currentAdmission);
						modified = true;
					}
				}
				if (thisBill.getAdmission() != null && currentAdmission != null && thisBill.getAdmission().getId() != currentAdmission.getId()) {
					int ok = MessageDialog.yesNo(PatientBillEdit.this, icon, "angal.newbill.thisbillwaslinkedtoapreviousadmissiondoyouwanttolinkittothecurrentadmissioninstead.msg");
					if (ok == JOptionPane.OK_OPTION) {
						thisBill.setAdmission(currentAdmission);
						modified = true;
					}
				}
				
				setAdmissionWard(thisBill.getAdmission());
				
			} else {  //Patient not found
				MessageDialog.warning(PatientBillEdit.this, "angal.newbill.patientassociatedwiththisbillnolongerexists.msg");
				thisBill.setIsPatient(false);
				thisBill.getBillPatient().setCode(0);
				thisBill.setAdmission(null);
				modified = true;
			}
		}
	}

	private void setAdmissionWard(Admission admission) {
		if (admission != null) {
			patientSelectedWard = thisBill.getAdmission().getWard().getDescription();
		} else {
			patientSelectedWard = "";
		}
	}

	private JPanel getJPanelData() {
		if (jPanelData == null) {
			jPanelData = new JPanel();
			jPanelData.setLayout(new BoxLayout(jPanelData, BoxLayout.Y_AXIS));
			jPanelData.add(getJScrollPaneTotal());
			jPanelData.add(getJScrollPaneBill());
			jPanelData.add(getJScrollPaneBigTotal());
			jPanelData.add(getJScrollPanePayment());
			jPanelData.add(getJScrollPaneBalance());
		}
		return jPanelData;
	}

	private JPanel getJPanelPatient() {
		if (jPanelPatient == null) {
			jPanelPatient = new JPanel();
			jPanelPatient.setLayout(new FlowLayout(FlowLayout.LEFT));
			jPanelPatient.add(getJLabelPatient());
			jPanelPatient.add(getJTextFieldPatient());
			jPanelPatient.add(getJLabelPriceList());
			jPanelPatient.add(getJComboBoxPriceList());
			jPanelPatient.add(getJLabelWard());
		}
		return jPanelPatient;
	}

	private JLabel getJLabelPatient() {
		if (jLabelPatient == null) {
			jLabelPatient = new JLabel(MessageBundle.getMessage("angal.common.patient.txt"));
			jLabelPatient.setPreferredSize(LabelsDimension);
		}
		return jLabelPatient;
	}


	private JTextField getJTextFieldPatient() {
		if (jTextFieldPatient == null) {
			jTextFieldPatient = new JTextField();
			jTextFieldPatient.setText(""); //$NON-NLS-1$
			jTextFieldPatient.setPreferredSize(PatientDimension);
			if (thisBill.isPatient()) {
				jTextFieldPatient.setText(thisBill.getPatName());
			}
			jTextFieldPatient.setEditable(false);
		}
		return jTextFieldPatient;
	}

	private JLabel getJLabelPriceList() {
		if (jLabelPriceList == null) {
			jLabelPriceList = new JLabel(MessageBundle.getMessage("angal.newbill.list.txt"));
		}
		return jLabelPriceList;
	}
	
	private JComboBox<PriceList> getJComboBoxPriceList() {
		if (jComboBoxPriceList == null) {
			jComboBoxPriceList = new JComboBox<>();
			PriceList list = null;
			for (PriceList lst : lstArray) {

				jComboBoxPriceList.addItem(lst);
				if (!this.insert) {
					if (lst.getId() == thisBill.getPriceList().getId()) {
						list = lst;
					}
				}
			}
			if (list != null) {
				jComboBoxPriceList.setSelectedItem(list);
			}

			jComboBoxPriceList.addActionListener(actionEvent -> {

				listSelected = (PriceList)jComboBoxPriceList.getSelectedItem();
				jTableBill.setModel(new BillTableModel());
				updateTotals();
			});
		}
		return jComboBoxPriceList;
	}
	
	private JLabel getJLabelWard() {
		if (jLabelWard == null) {
			jLabelWard = new JLabel();
			jLabelWard.setPreferredSize(WardDimension); //TODO: improve Layout
			jLabelWard.setHorizontalAlignment(SwingConstants.RIGHT);
			if (!patientSelectedWard.isEmpty()) {
				jLabelWard.setText(patientSelectedWard);
				jLabelWard.setIcon(ADMISSION_ICON);
			} else {
				jLabelWard.setText(OPD_TEXT);
				jLabelWard.setIcon(null);
			}
		}
		return jLabelWard;
	}
	
	private GoodDateTimeToggleChooser getJCalendarDate() {
		if (jCalendarDate == null) {
			if (this.insert) {
				// To remind last used
				billDate = RememberDates.getLastBillDate();
				if (RememberDates.getLastBillDate() == null) {
					billDate = TimeTools.getNow();
				}
			} else {
				// get BillDate
				billDate = thisBill.getDate();
			}
			jCalendarDate = new GoodDateTimeToggleChooser(billDate, false);

			jCalendarDate.addDateTimeChangeListener(event -> {
				DateChangeEvent dateChangeEvent = event.getDateChangeEvent();
				TimeChangeEvent timeChangeEvent = event.getTimeChangeEvent();
				if (dateChangeEvent != null) {
					// if the time is blank set it to the current time; otherwise leave it alone
					TimePicker timePicker = event.getTimePicker();
					if (timePicker.getTime() == null) {
						timePicker.setTime(LocalTime.now());
					}
				}
				if (!this.insert) {
					boolean isUnchanged = true;
					if (dateChangeEvent != null) {
						isUnchanged = billDate.toLocalDate().isEqual(dateChangeEvent.getNewDate());
					}
					LocalTime billTime = LocalTime.of(billDate.getHour(), billDate.getMinute());
					if (timeChangeEvent != null) {
						isUnchanged = timeChangeEvent.getNewTime().equals(billTime);
					}
					if (keepDate && !isUnchanged) {
						int ok = MessageDialog.yesNo(PatientBillEdit.this, "angal.newbill.doyouwanttochangetheoriginaldate.msg");
						if (ok == JOptionPane.YES_OPTION) {
							keepDate = false;
							modified = true;
							billDate = jCalendarDate.getLocalDateTime();
						} else {
							Runnable resetDateTime = () -> jCalendarDate.setDateTime(billDate);
							SwingUtilities.invokeLater(resetDateTime);
						}
					}
				}
			});
		}
		return jCalendarDate;
	}

	private JLabel getJLabelDate() {
		if (jLabelDate == null) {
			jLabelDate = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
			jLabelDate.setPreferredSize(LabelsDimension);
		}
		return jLabelDate;
	}

	private JPanel getJPanelDate() {
		if (jPanelDate == null) {
			jPanelDate = new JPanel();
			jPanelDate.setLayout(new FlowLayout(FlowLayout.LEFT));
			jPanelDate.add(getJLabelDate());
			jPanelDate.add(getJCalendarDate());
			jPanelDate.add(getJButtonPickPatient());
			jPanelDate.add(getJButtonTrashPatient());
			if (!GeneralData.getGeneralData().getSINGLEUSER()) {
				jPanelDate.add(getJLabelUser());
			}
		}
		return jPanelDate;
	}

	private JLabel getJLabelUser() {
		if (jLabelUser == null) {
			jLabelUser = new JLabel(MainMenu.getUser().getUserName());
			jLabelUser.setPreferredSize(UserDimension);  //TODO: improve Layout
			jLabelUser.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelUser.setForeground(Color.BLUE);
			jLabelUser.setFont(new Font(jLabelUser.getFont().getName(), Font.BOLD, jLabelUser.getFont().getSize() + 2));
		}
		return jLabelUser;
	}

	private JButton getJButtonTrashPatient() {
		if (jButtonTrashPatient == null) {
			jButtonTrashPatient = new JButton();
			jButtonTrashPatient.setPreferredSize(new Dimension(25, 25));
			jButtonTrashPatient.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));
			jButtonTrashPatient.setToolTipText(MessageBundle.getMessage("angal.newbill.removethepatientassociatedwiththisbill.tooltip"));
			jButtonTrashPatient.addActionListener(actionEvent -> {
				patientSelected = null;
				// BILL
				thisBill.setIsPatient(false);
				thisBill.getBillPatient().setCode(0);
				thisBill.setPatName(""); //$NON-NLS-1$
				thisBill.setAdmission(null);
				// INTERFACE
				jTextFieldPatient.setText("");
				jTextFieldPatient.setEditable(false);
				jButtonPickPatient.setText(MessageBundle.getMessage("angal.newbill.findpatient.btn"));
				jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.newbill.associateapatientwiththisbill.tooltip"));
				jButtonTrashPatient.setEnabled(false);
			});
			if (!thisBill.isPatient()) {
				jButtonTrashPatient.setEnabled(false);
			}
		}
		return jButtonTrashPatient;
	}

	private JButton getJButtonPickPatient() {
		if (jButtonPickPatient == null) {
			jButtonPickPatient = new JButton(MessageBundle.getMessage("angal.newbill.findpatient.btn"));
			jButtonPickPatient.setPreferredSize(new Dimension(150, 25));
			jButtonPickPatient.setMnemonic(MessageBundle.getMnemonic("angal.newbill.findpatient.btn.key"));
			jButtonPickPatient.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
			jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.newbill.associateapatientwiththisbill.tooltip"));
			jButtonPickPatient.addActionListener(actionEvent -> {

				SelectPatient sp = new SelectPatient(PatientBillEdit.this, patientSelected);
				sp.addSelectionListener(PatientBillEdit.this);
				sp.pack();
				sp.setVisible(true);

			});
			if (thisBill.isPatient()) {
				jButtonPickPatient.setText(MessageBundle.getMessage("angal.newbill.changepatient.btn"));
				jButtonPickPatient.setMnemonic(MessageBundle.getMnemonic("angal.newbill.changepatient.btn.key"));
				jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.newbill.changethepatientassociatedwiththisbill.tooltip"));
			}
		}
		return jButtonPickPatient;
	}

	public void setPatientSelected(Patient patientSelected) {
		this.patientSelected = patientSelected;
	}

	private JPanel getJPanelTop() {
		if (jPanelTop == null) {
			jPanelTop = new JPanel();
			jPanelTop.setLayout(new BoxLayout(jPanelTop, BoxLayout.Y_AXIS));
			jPanelTop.add(getJPanelDate());
			jPanelTop.add(getJPanelPatient());
		}
		return jPanelTop;
	}

	private JScrollPane getJScrollPaneBill() {
		if (jScrollPaneBill == null) {
			jScrollPaneBill = new JScrollPane();
			jScrollPaneBill.setBorder(null);
			jScrollPaneBill.setViewportView(getJTableBill());
			jScrollPaneBill.setMaximumSize(new Dimension(PANEL_WIDTH, BILL_HEIGHT));
			jScrollPaneBill.setMinimumSize(new Dimension(PANEL_WIDTH, BILL_HEIGHT));
			jScrollPaneBill.setPreferredSize(new Dimension(PANEL_WIDTH, BILL_HEIGHT));

		}
		return jScrollPaneBill;
	}

	private JTable getJTableBill() {
		if (jTableBill == null) {
			jTableBill = new JTable();
			jTableBill.setModel(new BillTableModel());
			jTableBill.getColumnModel().getColumn(1).setMinWidth(QUANTITY_WIDTH);
			jTableBill.getColumnModel().getColumn(1).setMaxWidth(QUANTITY_WIDTH);
			jTableBill.getColumnModel().getColumn(2).setMinWidth(PRICE_WIDTH);
			jTableBill.getColumnModel().getColumn(2).setMaxWidth(PRICE_WIDTH);
			jTableBill.setAutoCreateColumnsFromModel(false);
		}
		return jTableBill;
	}

	private JScrollPane getJScrollPaneBigTotal() {
		if (jScrollPaneBigTotal == null) {
			jScrollPaneBigTotal = new JScrollPane();
			jScrollPaneBigTotal.setViewportView(getJTableBigTotal());
			jScrollPaneBigTotal.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			jScrollPaneBigTotal.setMaximumSize(new Dimension(PANEL_WIDTH, BIG_TOTAL_HEIGHT));
			jScrollPaneBigTotal.setMinimumSize(new Dimension(PANEL_WIDTH, BIG_TOTAL_HEIGHT));
			jScrollPaneBigTotal.setPreferredSize(new Dimension(PANEL_WIDTH, BIG_TOTAL_HEIGHT));
		}
		return jScrollPaneBigTotal;
	}

	private JScrollPane getJScrollPaneTotal() {
		if (jScrollPaneTotal == null) {
			jScrollPaneTotal = new JScrollPane();
			jScrollPaneTotal.setViewportView(getJTableTotal());
			jScrollPaneTotal.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			jScrollPaneTotal.setMaximumSize(new Dimension(PANEL_WIDTH, TOTAL_HEIGHT));
			jScrollPaneTotal.setMinimumSize(new Dimension(PANEL_WIDTH, TOTAL_HEIGHT));
			jScrollPaneTotal.setPreferredSize(new Dimension(PANEL_WIDTH, TOTAL_HEIGHT));
		}
		return jScrollPaneTotal;
	}

	private JTable getJTableBigTotal() {
		if (jTableBigTotal == null) {
			jTableBigTotal = new JTable();
			jTableBigTotal.setModel(new DefaultTableModel(new Object[][] {
					{
							"<html><b>" + MessageBundle.getMessage("angal.newbill.topay.txt") + "</b></html>",
							currencyCod,
							bigTotal}
			}, new String[] {"","", ""}) {
				private static final long serialVersionUID = 1L;
				Class<?>[] types = new Class<?>[] { JLabel.class, JLabel.class, Double.class, };

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
			jTableBigTotal.getColumnModel().getColumn(1).setMinWidth(CURRENCY_CODE_WIDTH);
			jTableBigTotal.getColumnModel().getColumn(1).setMaxWidth(CURRENCY_CODE_WIDTH);
			jTableBigTotal.getColumnModel().getColumn(2).setMinWidth(PRICE_WIDTH);
			jTableBigTotal.getColumnModel().getColumn(2).setMaxWidth(PRICE_WIDTH);
			jTableBigTotal.setMaximumSize(new Dimension(PANEL_WIDTH, BIG_TOTAL_HEIGHT));
			jTableBigTotal.setMinimumSize(new Dimension(PANEL_WIDTH, BIG_TOTAL_HEIGHT));
			jTableBigTotal.setPreferredSize(new Dimension(PANEL_WIDTH, BIG_TOTAL_HEIGHT));
		}
		return jTableBigTotal;
	}

	private JTable getJTableTotal() {
		if (jTableTotal == null) {
			jTableTotal = new JTable();
			jTableTotal.setModel(new DefaultTableModel(new Object[][] {
					{
							"<html><b>"+MessageBundle.getMessage("angal.common.total.txt").toUpperCase()+"</b></html>",
							currencyCod,
							total}
			},
					new String[] {"","", ""}) {
				private static final long serialVersionUID = 1L;
				Class<?>[] types = new Class<?>[] { JLabel.class, JLabel.class, Double.class, };

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
			jTableTotal.getColumnModel().getColumn(1).setMinWidth(CURRENCY_CODE_WIDTH);
			jTableTotal.getColumnModel().getColumn(1).setMaxWidth(CURRENCY_CODE_WIDTH);
			jTableTotal.getColumnModel().getColumn(2).setMinWidth(PRICE_WIDTH);
			jTableTotal.getColumnModel().getColumn(2).setMaxWidth(PRICE_WIDTH);
			jTableTotal.setMaximumSize(new Dimension(PANEL_WIDTH, TOTAL_HEIGHT));
			jTableTotal.setMinimumSize(new Dimension(PANEL_WIDTH, TOTAL_HEIGHT));
			jTableTotal.setPreferredSize(new Dimension(PANEL_WIDTH, TOTAL_HEIGHT));
		}
		return jTableTotal;
	}

	private JScrollPane getJScrollPanePayment() {
		if (jScrollPanePayment == null) {
			jScrollPanePayment = new JScrollPane();
			jScrollPanePayment.setBorder(null);
			jScrollPanePayment.setViewportView(getJTablePayment());
			jScrollPanePayment.setMaximumSize(new Dimension(PANEL_WIDTH, PAYMENT_HEIGHT));
			jScrollPanePayment.setMinimumSize(new Dimension(PANEL_WIDTH, PAYMENT_HEIGHT));
			jScrollPanePayment.setPreferredSize(new Dimension(PANEL_WIDTH, PAYMENT_HEIGHT));
		}
		return jScrollPanePayment;
	}

	private JTable getJTablePayment() {
		if (jTablePayment == null) {
			jTablePayment = new JTable();
			jTablePayment.setModel(new PaymentTableModel());
			jTablePayment.getColumnModel().getColumn(1).setMinWidth(PRICE_WIDTH);
			jTablePayment.getColumnModel().getColumn(1).setMaxWidth(PRICE_WIDTH);
		}
		return jTablePayment;
	}

	private JScrollPane getJScrollPaneBalance() {
		if (jScrollPaneBalance == null) {
			jScrollPaneBalance = new JScrollPane();
			jScrollPaneBalance.setViewportView(getJTableBalance());
			jScrollPaneBalance.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			jScrollPaneBalance.setMaximumSize(new Dimension(PANEL_WIDTH, BALANCE_HEIGHT));
			jScrollPaneBalance.setMinimumSize(new Dimension(PANEL_WIDTH, BALANCE_HEIGHT));
			jScrollPaneBalance.setPreferredSize(new Dimension(PANEL_WIDTH, BALANCE_HEIGHT));
		}
		return jScrollPaneBalance;
	}

	private JTable getJTableBalance() {
		if (jTableBalance == null) {
			jTableBalance = new JTable();
			jTableBalance.setModel(new DefaultTableModel(new Object[][] {
					{
							"<html><b>"+MessageBundle.getMessage("angal.newbill.balance.txt").toUpperCase()+"</b></html>",
							currencyCod,
							balance}
			},
					new String[] {"","",""}) {
				private static final long serialVersionUID = 1L;
				Class<?>[] types = new Class<?>[] { JLabel.class, JLabel.class, Double.class, };

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return types[columnIndex];
				}

				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			});
			jTableBalance.getColumnModel().getColumn(1).setMinWidth(CURRENCY_CODE_WIDTH);
			jTableBalance.getColumnModel().getColumn(1).setMaxWidth(CURRENCY_CODE_WIDTH);
			jTableBalance.getColumnModel().getColumn(2).setMinWidth(PRICE_WIDTH);
			jTableBalance.getColumnModel().getColumn(2).setMaxWidth(PRICE_WIDTH);
			jTableBalance.setMaximumSize(new Dimension(PANEL_WIDTH, BALANCE_HEIGHT));
			jTableBalance.setMinimumSize(new Dimension(PANEL_WIDTH, BALANCE_HEIGHT));
			jTableBalance.setPreferredSize(new Dimension(PANEL_WIDTH, BALANCE_HEIGHT));
		}
		return jTableBalance;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.setLayout(new BoxLayout(jPanelButtons, BoxLayout.Y_AXIS));
			jPanelButtons.add(getJPanelButtonsBill());
			jPanelButtons.add(getJPanelButtonsPayment());
			jPanelButtons.add(Box.createVerticalGlue());
			jPanelButtons.add(getJPanelButtonsActions());
		}
		return jPanelButtons;
	}

	private JPanel getJPanelButtonsBill() {
		if (jPanelButtonsBill == null) {
			jPanelButtonsBill = new JPanel();
			jPanelButtonsBill.setLayout(new BoxLayout(jPanelButtonsBill, BoxLayout.Y_AXIS));
			jPanelButtonsBill.add(getJButtonAddMedical());
			jPanelButtonsBill.add(getJButtonAddOperation());
			jPanelButtonsBill.add(getJButtonAddExam());
			jPanelButtonsBill.add(getJButtonAddOther());
			jPanelButtonsBill.add(getJButtonAddCustom());
			jPanelButtonsBill.add(getJButtonRemoveItem());
			jPanelButtonsBill.setMinimumSize(new Dimension(BUTTON_WIDTH, BILL_HEIGHT + TOTAL_HEIGHT));
			jPanelButtonsBill.setMaximumSize(new Dimension(BUTTON_WIDTH, BILL_HEIGHT + TOTAL_HEIGHT));
			jPanelButtonsBill.setPreferredSize(new Dimension(BUTTON_WIDTH, BILL_HEIGHT + TOTAL_HEIGHT));

		}
		return jPanelButtonsBill;
	}

	private JPanel getJPanelButtonsPayment() {
		if (jPanelButtonsPayment == null) {
			jPanelButtonsPayment = new JPanel();
			jPanelButtonsPayment.setLayout(new BoxLayout(jPanelButtonsPayment, BoxLayout.Y_AXIS));
			jPanelButtonsPayment.add(getJButtonAddPayment());
			jPanelButtonsPayment.add(getJButtonAddRefund());
			if (GeneralData.RECEIPTPRINTER) {
				jPanelButtonsPayment.add(getJButtonPrintPayment());
			}
			jPanelButtonsPayment.add(getJButtonRemovePayment());
			jPanelButtonsPayment.setMinimumSize(new Dimension(BUTTON_WIDTH, PAYMENT_HEIGHT));
			jPanelButtonsPayment.setMaximumSize(new Dimension(BUTTON_WIDTH, PAYMENT_HEIGHT));
		}
		return jPanelButtonsPayment;
	}

	private JPanel getJPanelButtonsActions() {
		if (jPanelButtonsActions == null) {
			jPanelButtonsActions = new JPanel();
			jPanelButtonsActions.setLayout(new BoxLayout(jPanelButtonsActions, BoxLayout.Y_AXIS));
			jPanelButtonsActions.add(getJButtonBalance());
			jPanelButtonsActions.add(getJButtonSave());
			jPanelButtonsActions.add(getJButtonPaid());
			jPanelButtonsActions.add(getJButtonClose());
		}
		return jPanelButtonsActions;
	}

	private JButton getJButtonBalance() {
		if (jButtonBalance == null) {
			jButtonBalance = new JButton(MessageBundle.getMessage("angal.newbill.givechange.btn"));
			jButtonBalance.setMnemonic(MessageBundle.getMnemonic("angal.newbill.givechange.btn.key"));
			jButtonBalance.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
			jButtonBalance.setIcon(new ImageIcon("rsc/icons/money_button.png"));
			jButtonBalance.setHorizontalAlignment(SwingConstants.LEFT);
			if (this.insert)  {
				jButtonBalance.setEnabled(false);
			}
			jButtonBalance.addActionListener(actionEvent -> {

				Icon icon = new ImageIcon("rsc/icons/money_dialog.png");
				BigDecimal amount = new BigDecimal(0);

				String quantity = (String) JOptionPane.showInputDialog(PatientBillEdit.this,
						MessageBundle.getMessage("angal.newbill.entercustomercash.txt"),
						MessageBundle.getMessage("angal.newbill.givechange.title"),
						JOptionPane.OK_CANCEL_OPTION,
						icon,
						null,
						amount);

				if (quantity != null) {
					try {
						amount = new BigDecimal(quantity);
						if (amount.equals(new BigDecimal(0)) || amount.compareTo(balance) < 0) {
							return;
						}
						JOptionPane.showMessageDialog(PatientBillEdit.this,
								MessageBundle.formatMessage("angal.newbill.givechange.fmt.msg", amount.subtract(balance)),
								MessageBundle.getMessage("angal.newbill.givechange.title"),
								JOptionPane.OK_OPTION,
								icon);
					} catch (Exception eee) {
						MessageDialog.error(PatientBillEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
					}
				}
			});
		}
		return jButtonBalance;
	}

	private JButton getJButtonSave() {
		if (jButtonSave == null) {
			jButtonSave = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
			jButtonSave.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
			jButtonSave.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
			jButtonSave.setIcon(new ImageIcon("rsc/icons/save_button.png"));
			jButtonSave.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonSave.addActionListener(actionEvent -> {

				if (listSelected == null) {
					listSelected = lstArray.get(0);
				}

				checkBill();

				if (this.insert) {
					RememberDates.setLastBillDate(billDate);             //to remember for next INSERT
					Bill newBill = new Bill(0,                        //Bill ID
							billDate,                                    //from calendar
							billDate,                                    //most recent payment
							true,                                   //is a List?
							listSelected,                                //List
							listSelected.getName(),                      //List name
							thisBill.isPatient(),                        //is a Patient?
							thisBill.isPatient() ?
									patientSelected : null,    //Patient ID
							thisBill.isPatient() ?
									patientSelected.getName() :
									jTextFieldPatient.getText(),         //Patient Name
							paid ? "C" : "O",                            //CLOSED or OPEN
							total.doubleValue(),                         //Total
							balance.doubleValue(),                       //Balance
							user,                                       //User
							thisBill.getAdmission());					//Admission
									
					try {
						billBrowserManager.newBill(newBill, billItems, payItems);
						thisBill.setId(newBill.getId());
					} catch(OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex, PatientBillEdit.this);
						return;
					}
					fireBillInserted(newBill);
					dispose();

				} else {
					Bill updateBill = new Bill(thisBill.getId(),         //Bill ID
							billDate,                                    //from calendar
							null,                                 //most recent payment
							true,                                  //is a List?
							listSelected,                                //List
							listSelected.getName(),                      //List name
							thisBill.isPatient(),                        //is a Patient?
							thisBill.isPatient() ?
									patientSelected : null,    //Patient ID
							thisBill.isPatient() ?
									thisBill.getPatName() :
									jTextFieldPatient.getText(),         //Patient Name
							paid ? "C" : "O",                            //CLOSED or OPEN
							total.doubleValue(),                         //Total
							balance.doubleValue(),                       //Balance
							user,                                       //User
							thisBill.getAdmission());					//Admission

					try {
						billBrowserManager.updateBill(updateBill, billItems, payItems);
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex, PatientBillEdit.this);
						return;
					}
					fireBillInserted(updateBill);
				}
				if (hasNewPayments()) {
					TxtPrinter.initialize();
					new GenericReportBill(thisBill.getId(), "PatientBillPayments", false, !TxtPrinter.PRINT_WITHOUT_ASK);
				}
				if (paid && GeneralData.RECEIPTPRINTER) {
					TxtPrinter.initialize();
					if (TxtPrinter.PRINT_AS_PAID) {
						new GenericReportBill(billID, GeneralData.PATIENTBILL, false, !TxtPrinter.PRINT_WITHOUT_ASK);
					}
				}
				dispose();
			});
		}
		return jButtonSave;
	}

	private boolean hasNewPayments() {
		return (this.insert && !payItems.isEmpty()) || (payItems.size() - payItemsSaved) > 0;
	}

	private JButton getJButtonPrintPayment() {
		if (jButtonPrintPayment == null) {
			jButtonPrintPayment = new JButton(MessageBundle.getMessage("angal.newbill.paymentreceipt.btn"));
			jButtonPrintPayment.setMnemonic(MessageBundle.getMnemonic("angal.newbill.paymentreceipt.btn.key"));
			jButtonPrintPayment.setMaximumSize(new Dimension(BUTTON_WIDTH_PAYMENT, BUTTON_HEIGHT));
			jButtonPrintPayment.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonPrintPayment.setIcon(new ImageIcon("rsc/icons/receipt_button.png"));
			jButtonPrintPayment.addActionListener(actionEvent -> {
				TxtPrinter.initialize();
				new GenericReportBill(thisBill.getId(), "PatientBillPayments", false, !TxtPrinter.PRINT_WITHOUT_ASK);
			});
		}
		if (this.insert) {
			jButtonPrintPayment.setEnabled(false);
		}
		return jButtonPrintPayment;
	}

	private JButton getJButtonPaid() {
		if (jButtonPaid == null) {
			jButtonPaid = new JButton(MessageBundle.getMessage("angal.newbill.paid.btn"));
			jButtonPaid.setMnemonic(MessageBundle.getMnemonic("angal.newbill.paid.btn.key"));
			jButtonPaid.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
			jButtonPaid.setIcon(new ImageIcon("rsc/icons/ok_button.png"));
			jButtonPaid.setHorizontalAlignment(SwingConstants.LEFT);
			if (this.insert) {
				jButtonPaid.setEnabled(false);
			}
			jButtonPaid.addActionListener(actionEvent -> {

				LocalDateTime datePay;

				Icon icon = new ImageIcon("rsc/icons/money_dialog.png"); //$NON-NLS-1$
				int ok = MessageDialog.yesNo(PatientBillEdit.this, icon,
						"angal.newbill.doyouwanttosetthecurrentbillaspaid.msg");
				if (ok == JOptionPane.NO_OPTION) {
					return;
				}

				if (balance.compareTo(new BigDecimal(0)) > 0) {
					if (billDate.isBefore(today)) { //if Bill is in the past the user will be asked for PAID date

						icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$

						GoodDateTimeSpinnerChooser datePayChooser = new GoodDateTimeSpinnerChooser(TimeTools.getNow());

						int r = JOptionPane.showConfirmDialog(PatientBillEdit.this,
								datePayChooser,
								MessageBundle.getMessage("angal.newbill.dateofpayment.title"),
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.PLAIN_MESSAGE,
								icon);

						if (r == JOptionPane.OK_OPTION) {
							datePay = datePayChooser.getLocalDateTime();
						} else {
							return;
						}

						if (isValidPaymentDate(datePay)) {
							addPayment(datePay, balance.doubleValue());
						} else {
							return;
						}
					} else {
						datePay = TimeTools.getNow();
						addPayment(datePay, balance.doubleValue());
					}
				}
				paid = true;
				updateBalance();
				jButtonSave.doClick();
			});
		}
		return jButtonPaid;
	}

	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jButtonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jButtonClose.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
			jButtonClose.setIcon(new ImageIcon("rsc/icons/close_button.png"));
			jButtonClose.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonClose.addActionListener(actionEvent -> {
				if (modified) {
					int ok = MessageDialog.yesNoCancel(PatientBillEdit.this, "angal.newbill.billhasbeenchangedwouldyouliketosavethechanges.msg");
					if (ok == JOptionPane.YES_OPTION) {
						jButtonSave.doClick();
					} else if (ok == JOptionPane.NO_OPTION) {
						dispose();
					}
				} else {
					dispose();
				}
			});
		}
		return jButtonClose;
	}

	private JButton getJButtonAddRefund() {
		if (jButtonAddRefund == null) {
			jButtonAddRefund = new JButton(MessageBundle.getMessage("angal.newbill.refund.btn"));
			jButtonAddRefund.setMnemonic(MessageBundle.getMnemonic("angal.newbill.refund.btn.key"));
			jButtonAddRefund.setMaximumSize(new Dimension(BUTTON_WIDTH_PAYMENT, BUTTON_HEIGHT));
			jButtonAddRefund.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddRefund.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddRefund.addActionListener(actionEvent -> {

				Icon icon = new ImageIcon("rsc/icons/money_dialog.png");
				BigDecimal amount = new BigDecimal(0);

				LocalDateTime datePay;

				String quantity = (String) JOptionPane.showInputDialog(
						PatientBillEdit.this,
						MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
						MessageBundle.getMessage("angal.common.quantity.txt"),
						JOptionPane.PLAIN_MESSAGE,
						icon,
						null,
						amount);
				if (quantity != null) {
					try {
						amount = new BigDecimal(quantity).negate();
						if (amount.equals(new BigDecimal(0))) {
							return;
						}
					} catch (Exception eee) {
						MessageDialog.error(PatientBillEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
						return;
					}
				} else {
					return;
				}

				if (billDate.isBefore(today)) { //if is a bill in the past the user will be asked for date of payment

					GoodDateTimeSpinnerChooser datePayChooser = new GoodDateTimeSpinnerChooser(TimeTools.getNow());
					int r = JOptionPane.showConfirmDialog(PatientBillEdit.this,
							datePayChooser,
							MessageBundle.getMessage("angal.newbill.dateofpayment.title"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE);

					if (r == JOptionPane.OK_OPTION) {
						datePay = datePayChooser.getLocalDateTime();
					} else {
						return;
					}

					if (isValidPaymentDate(datePay)) {
						addPayment(datePay, amount.doubleValue());
					}
				} else {
					datePay = TimeTools.getNow();
					addPayment(datePay, amount.doubleValue());
				}
			});
		}
		return jButtonAddRefund;
	}

	private boolean isValidPaymentDate(LocalDateTime datePay) {
		LocalDateTime now = TimeTools.getNow();
		LocalDateTime lastPay;
		if (!payItems.isEmpty()) {
			lastPay = payItems.get(payItems.size() - 1).getDate();
		} else {
			lastPay = billDate;
		}
		if (datePay.isBefore(billDate)) {
			MessageDialog.error(PatientBillEdit.this, "angal.newbill.paymentmadebeforebilldate.msg");
			return false;
		} else if (datePay.isBefore(lastPay)) {
			MessageDialog.error(PatientBillEdit.this, "angal.newbill.thedateisbeforethelastpayment.msg");
			return false;
		} else if (datePay.isAfter(now)) {
			MessageDialog.error(PatientBillEdit.this, "angal.newbill.payementsinthefuturearenotallowed.msg");
			return false;
		}
		return true;
	}

	private JButton getJButtonAddPayment() {
		if (jButtonAddPayment == null) {
			jButtonAddPayment = new JButton(MessageBundle.getMessage("angal.newbill.payment.btn"));
			jButtonAddPayment.setMnemonic(MessageBundle.getMnemonic("angal.newbill.payment.btn.key"));
			jButtonAddPayment.setMaximumSize(new Dimension(BUTTON_WIDTH_PAYMENT, BUTTON_HEIGHT));
			jButtonAddPayment.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddPayment.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddPayment.addActionListener(actionEvent -> {

				Icon icon = new ImageIcon("rsc/icons/money_dialog.png");
				BigDecimal amount = new BigDecimal(0);

				LocalDateTime datePay;

				String quantity = (String) JOptionPane.showInputDialog(
						PatientBillEdit.this,
						MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
						MessageBundle.getMessage("angal.common.quantity.txt"),
						JOptionPane.PLAIN_MESSAGE,
						icon,
						null,
						amount);
				if (quantity != null) {
					try {
						amount = new BigDecimal(quantity);
						if (amount.equals(new BigDecimal(0))) {
							return;
						}
					} catch (Exception eee) {
						MessageDialog.error(PatientBillEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
						return;
					}
				} else {
					return;
				}

				if (billDate.isBefore(today)) { //if is a bill in the past the user will be asked for date of payment

					GoodDateTimeSpinnerChooser datePayChooser = new GoodDateTimeSpinnerChooser(TimeTools.getNow());
					int r = JOptionPane.showConfirmDialog(PatientBillEdit.this,
							datePayChooser,
							MessageBundle.getMessage("angal.newbill.dateofpayment.title"),
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE);

					if (r == JOptionPane.OK_OPTION) {
						datePay = datePayChooser.getLocalDateTime();
					} else {
						return;
					}

					if (isValidPaymentDate(datePay)) {
						addPayment(datePay, amount.doubleValue());
					}
				} else {
					datePay = TimeTools.getNow();
					addPayment(datePay, amount.doubleValue());
				}
			});
		}
		return jButtonAddPayment;
	}

	private JButton getJButtonRemovePayment() {
		if (jButtonRemovePayment == null) {
			jButtonRemovePayment = new JButton(MessageBundle.getMessage("angal.newbill.removepayment.btn"));
			jButtonRemovePayment.setMnemonic(MessageBundle.getMnemonic("angal.newbill.removepayment.btn.key"));
			jButtonRemovePayment.setMaximumSize(new Dimension(BUTTON_WIDTH_PAYMENT, BUTTON_HEIGHT));
			jButtonRemovePayment.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonRemovePayment.setIcon(new ImageIcon("rsc/icons/delete_button.png"));
			jButtonRemovePayment.addActionListener(actionEvent -> {
				int row = jTablePayment.getSelectedRow();
				if (row > -1) {
					removePayment(row);
				}
			});
		}
		return jButtonRemovePayment;
	}

	private JButton getJButtonAddOther() {
		if (jButtonAddOther == null) {
			jButtonAddOther = new JButton(MessageBundle.getMessage("angal.newbill.other.btn"));
			jButtonAddOther.setMnemonic(MessageBundle.getMnemonic("angal.newbill.other.btn.key"));
			jButtonAddOther.setMaximumSize(new Dimension(BUTTON_WIDTH_BILL, BUTTON_HEIGHT));
			jButtonAddOther.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddOther.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddOther.addActionListener(actionEvent -> {

				boolean isPrice = true;

				Map<Integer, PricesOthers> othersHashMap = new HashMap<>();
				for (PricesOthers other : othPrices) {
					othersHashMap.put(other.getId(), other);
				}

				List<Price> othArray = new ArrayList<>();
				for (Price price : prcListArray) {
					if (price.getGroup().equals("OTH")) {
						othArray.add(price);
					}
				}

				Icon icon = new ImageIcon("rsc/icons/plus_dialog.png");
				Price oth = (Price) JOptionPane.showInputDialog(
						PatientBillEdit.this,
						MessageBundle.getMessage("angal.newbill.pleaseselectanitem.txt"),
						MessageBundle.getMessage("angal.newbill.item.title"),
						JOptionPane.PLAIN_MESSAGE,
						icon,
						othArray.toArray(),
						""); //$NON-NLS-1$

				if (oth != null) {
					if (othersHashMap.get(Integer.valueOf(oth.getItem())).isUndefined()) {
						icon = new ImageIcon("rsc/icons/money_dialog.png"); //$NON-NLS-1$
						String price = (String) JOptionPane.showInputDialog(
								PatientBillEdit.this,
								MessageBundle.getMessage("angal.newbill.howmuchisit.txt"),
								MessageBundle.getMessage("angal.common.undefined.txt"),
								JOptionPane.PLAIN_MESSAGE,
								icon,
								null,
								"0"); //$NON-NLS-1$
						try {
							if (price == null) {
								return;
							}
							double amount = Double.parseDouble(price);
							oth.setPrice(amount);
							isPrice = false;
						} catch (Exception eee) {
							MessageDialog.error(PatientBillEdit.this, "angal.newbill.invalidpricepleasetryagain.msg");
							return;
						}
					}
					if (othersHashMap.get(Integer.valueOf(oth.getItem())).isDischarge()) {
						double amount = oth.getPrice();
						oth.setPrice(-amount);
					}
					if (othersHashMap.get(Integer.valueOf(oth.getItem())).isDaily()) {
						int qty = 1;
						icon = new ImageIcon("rsc/icons/calendar_dialog.png"); //$NON-NLS-1$
						String quantity = (String) JOptionPane.showInputDialog(
								PatientBillEdit.this,
								MessageBundle.getMessage("angal.newbill.howmanydays.txt"),
								MessageBundle.getMessage("angal.newbill.days.title"),
								JOptionPane.PLAIN_MESSAGE,
								icon,
								null,
								qty);
						try {
							if (quantity == null || quantity.equals("")) {
								return;
							}
							qty = Integer.parseInt(quantity);
							addItem(oth, qty, isPrice);
						} catch (Exception eee) {
							MessageDialog.error(PatientBillEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
						}
					} else {
						addItem(oth, 1, isPrice);
					}
				}
			});
		}
		return jButtonAddOther;
	}

	private JButton getJButtonAddExam() {
		if (jButtonAddExam == null) {
			jButtonAddExam = new JButton(MessageBundle.getMessage("angal.newbill.exam.btn"));
			jButtonAddExam.setMnemonic(MessageBundle.getMnemonic("angal.newbill.exam.btn.key"));
			jButtonAddExam.setMaximumSize(new Dimension(BUTTON_WIDTH_BILL, BUTTON_HEIGHT));
			jButtonAddExam.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddExam.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddExam.addActionListener(actionEvent -> {

				List<Price> exaArray = new ArrayList<>();
				for (Price price : prcListArray) {

					if (price.getGroup().equals("EXA")) {
						exaArray.add(price);
					}
				}

				Icon icon = new ImageIcon("rsc/icons/exam_dialog.png"); //$NON-NLS-1$
				Price exa = (Price) JOptionPane.showInputDialog(
						PatientBillEdit.this,
						MessageBundle.getMessage("angal.newbill.selectanexam.txt"),
						MessageBundle.getMessage("angal.newbill.exam.title"),
						JOptionPane.PLAIN_MESSAGE,
						icon,
						exaArray.toArray(),
						""); //$NON-NLS-1$
				addItem(exa, 1, true);
			});
		}
		return jButtonAddExam;
	}

	private JButton getJButtonAddOperation() {
		if (jButtonAddOperation == null) {
			jButtonAddOperation = new JButton(MessageBundle.getMessage("angal.newbill.operation.btn"));
			jButtonAddOperation.setMnemonic(MessageBundle.getMnemonic("angal.newbill.operation.btn.key"));
			jButtonAddOperation.setMaximumSize(new Dimension(BUTTON_WIDTH_BILL, BUTTON_HEIGHT));
			jButtonAddOperation.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddOperation.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddOperation.addActionListener(actionEvent -> {

				List<Price> opeArray = new ArrayList<>();
				for (Price price : prcListArray) {

					if (price.getGroup().equals("OPE")) {
						opeArray.add(price);
					}
				}

				Icon icon = new ImageIcon("rsc/icons/operation_dialog.png"); //$NON-NLS-1$
				Price ope = (Price) JOptionPane.showInputDialog(
						PatientBillEdit.this,
						MessageBundle.getMessage("angal.newbill.selectanoperation.txt"),
						MessageBundle.getMessage("angal.newbill.operation.title"),
						JOptionPane.PLAIN_MESSAGE,
						icon,
						opeArray.toArray(),
						""); //$NON-NLS-1$
				addItem(ope, 1, true);
			});
		}
		return jButtonAddOperation;
	}

	private JButton getJButtonAddMedical() {
		if (jButtonAddMedical == null) {
			jButtonAddMedical = new JButton(MessageBundle.getMessage("angal.newbill.medical.btn"));
			jButtonAddMedical.setMnemonic(MessageBundle.getMnemonic("angal.newbill.medical.btn"));
			jButtonAddMedical.setMaximumSize(new Dimension(BUTTON_WIDTH_BILL, BUTTON_HEIGHT));
			jButtonAddMedical.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonAddMedical.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonAddMedical.addActionListener(actionEvent -> {

				List<Price> medArray = new ArrayList<>();
				for (Price price : prcListArray) {

					if (price.getGroup().equals("MED")) {
						medArray.add(price);
					}
				}

				Icon icon = new ImageIcon("rsc/icons/medical_dialog.png"); //$NON-NLS-1$
				Price med = (Price) JOptionPane.showInputDialog(
						PatientBillEdit.this,
						MessageBundle.getMessage("angal.newbill.selectamedical.txt"),
						MessageBundle.getMessage("angal.newbill.medical.title"),
						JOptionPane.PLAIN_MESSAGE,
						icon,
						medArray.toArray(),
						""); //$NON-NLS-1$
				if (med != null) {
					int qty = 1;
					String quantity = (String) JOptionPane.showInputDialog(
							PatientBillEdit.this,
							MessageBundle.getMessage("angal.newbill.insertquantity.txt"),
							MessageBundle.getMessage("angal.common.quantity.txt"),
							JOptionPane.PLAIN_MESSAGE,
							icon,
							null,
							qty);
					try {
						if (quantity == null || quantity.equals("")) {
							return;
						}
						qty = Integer.parseInt(quantity);
						addItem(med, qty, true);
					} catch (Exception eee) {
						MessageDialog.error(PatientBillEdit.this, "angal.newbill.invalidquantitypleasetryagain.msg");
					}
				}
			});
		}
		return jButtonAddMedical;
	}

	private JButton getJButtonAddCustom() {
		if (jButtonCustom == null) {
			jButtonCustom = new JButton(MessageBundle.getMessage("angal.newbill.custom.btn"));
			jButtonCustom.setMnemonic(MessageBundle.getMnemonic("angal.newbill.custom.btn.key"));
			jButtonCustom.setMaximumSize(new Dimension(BUTTON_WIDTH_BILL, BUTTON_HEIGHT));
			jButtonCustom.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonCustom.setIcon(new ImageIcon("rsc/icons/plus_button.png"));
			jButtonCustom.addActionListener(actionEvent -> {
				double amount;
				Icon icon = new ImageIcon("rsc/icons/custom_dialog.png"); //$NON-NLS-1$
				String desc = (String) JOptionPane.showInputDialog(
						PatientBillEdit.this,
						MessageBundle.getMessage("angal.newbill.chooseadescription.txt"),
						MessageBundle.getMessage("angal.newbill.customitem.title"),
						JOptionPane.PLAIN_MESSAGE,
						icon,
						null,
						MessageBundle.getMessage("angal.newbill.newdescription.txt"));
				if (desc == null || desc.equals("")) { //$NON-NLS-1$
					return;
				} else {
					icon = new ImageIcon("rsc/icons/money_dialog.png"); //$NON-NLS-1$
					String price = (String) JOptionPane.showInputDialog(
							PatientBillEdit.this,
							MessageBundle.getMessage("angal.newbill.howmuchisit.txt"),
							MessageBundle.getMessage("angal.newbill.customitem.title"),
							JOptionPane.PLAIN_MESSAGE,
							icon,
							null,
							"0"); //$NON-NLS-1$
					try {
						amount = Double.parseDouble(price);
					} catch (Exception eee) {
						MessageDialog.error(PatientBillEdit.this, "angal.newbill.invalidpricepleasetryagain.msg");
						return;
					}

				}

				try {
					BillItems newItem = new BillItems(0,
							billBrowserManager.getBill(billID),
							false,
							"", //$NON-NLS-1$
							desc,
							amount,
							1);
					addItem(newItem);
				} catch (OHServiceException ohServiceException) {
					MessageDialog.showExceptions(ohServiceException);
				}
			});
		}
		return jButtonCustom;
	}

	private JButton getJButtonRemoveItem() {
		if (jButtonRemoveItem == null) {
			jButtonRemoveItem = new JButton(MessageBundle.getMessage("angal.newbill.removeitem.btn"));
			jButtonRemoveItem.setMnemonic(MessageBundle.getMnemonic("angal.newbill.removeitem.btn.key"));
			jButtonRemoveItem.setMaximumSize(new Dimension(BUTTON_WIDTH_BILL, BUTTON_HEIGHT));
			jButtonRemoveItem.setHorizontalAlignment(SwingConstants.LEFT);
			jButtonRemoveItem.setIcon(new ImageIcon("rsc/icons/delete_button.png"));
			jButtonRemoveItem.addActionListener(actionEvent -> {
				int row = jTableBill.getSelectedRow();
				if (row > -1) {
					removeItem(row);
				}
			});
		}
		return jButtonRemoveItem;
	}

	private void updateTotal() { //only positive items make the bill's total
		total = new BigDecimal(0);
		for (BillItems item : billItems) {
			double amount = item.getItemAmount();
			if (amount > 0) {
				BigDecimal itemAmount = new BigDecimal(Double.toString(amount));
				total = total.add(itemAmount.multiply(new BigDecimal(item.getItemQuantity())));
			}
		}
	}

	private void updateBigTotal() { //the big total (to pay) is made by all items
		bigTotal = new BigDecimal(0);
		for (BillItems item : billItems) {
			BigDecimal itemAmount = new BigDecimal(Double.toString(item.getItemAmount()));
			bigTotal = bigTotal.add(itemAmount.multiply(new BigDecimal(item.getItemQuantity())));
		}
	}

	private void updateBalance() { //the balance is what remaining after payments
		balance = new BigDecimal(0);
		BigDecimal payments = new BigDecimal(0);
		for (BillPayments pay : payItems) {
			BigDecimal payAmount = new BigDecimal(Double.toString(pay.getAmount()));
			payments = payments.add(payAmount);
		}
		balance = bigTotal.subtract(payments);
		if (jButtonPaid != null) {
			jButtonPaid.setEnabled(balance.compareTo(new BigDecimal(0)) >= 0);
		}
		if (jButtonBalance != null) {
			jButtonBalance.setEnabled(balance.compareTo(new BigDecimal(0)) >= 0);
		}
	}

	private void addItem(Price prc, int qty, boolean isPrice) {
		if (prc != null) {
			double amount = prc.getPrice();
			try {
				BillItems item = new BillItems(0,
						billBrowserManager.getBill(billID),
						isPrice,
						prc.getGroup() + prc.getItem(),
						prc.getDesc(),
						amount,
						qty);
				billItems.add(item);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, PatientBillEdit.this);
			}
			modified = true;
			jTableBill.updateUI();
			updateTotals();
		}
	}

	private void updateUI() {

		jCalendarDate.setDateTime(thisBill.getDate());
		jTextFieldPatient.setText(patientSelected.getName());
		jTextFieldPatient.setEditable(false);
		jButtonPickPatient.setText(MessageBundle.getMessage("angal.newbill.changepatient.btn"));
		jButtonPickPatient.setToolTipText(MessageBundle.getMessage("angal.newbill.changethepatientassociatedwiththisbill.tooltip"));
		jButtonTrashPatient.setEnabled(true);
		jTableBill.updateUI();
		jTablePayment.updateUI();
		updateTotals();
	}

	private void updateTotals() {
		updateTotal();
		updateBigTotal();
		updateBalance();
		jTableTotal.getModel().setValueAt(total, 0, 2);
		jTableBigTotal.getModel().setValueAt(bigTotal, 0, 2);
		jTableBalance.getModel().setValueAt(balance, 0, 2);
	}

	private void addItem(BillItems item) {
		if (item != null) {
			billItems.add(item);
			modified = true;
			jTableBill.updateUI();
			updateTotals();
		}
	}

	private void addPayment(LocalDateTime datePay, double qty) {
		if (qty != 0) {
			try {
				BillPayments pay = new BillPayments(0,
						billBrowserManager.getBill(billID),
						datePay,
						qty,
						user);
				payItems.add(pay);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, PatientBillEdit.this);
			}
			modified = true;
			Collections.sort(payItems);
			jTablePayment.updateUI();
			updateBalance();
			jTableBalance.getModel().setValueAt(balance, 0, 2);
		}
	}

	private void removeItem(int row) {
		if (row != -1 && row >= billItemsSaved) {
			billItems.remove(row);
			jTableBill.updateUI();
			jTableBill.clearSelection();
			updateTotals();
		} else {
			MessageDialog.error(null, "angal.newbill.youcannotdeletealreadysaveditems.msg");
		}
	}

	private void removePayment(int row) {
		if (row != -1 && row >= payItemsSaved) {
			payItems.remove(row);
			jTablePayment.updateUI();
			jTablePayment.clearSelection();
			updateTotals();
		} else {
			MessageDialog.error(null, "angal.newbill.youcannotdeletepastpayments.msg");
		}
	}

	public class BillTableModel implements TableModel {

		public BillTableModel() {

			Map<String, Price> priceHashTable;
			prcListArray = new ArrayList<>();

			/*
			 * Select the prices of the selected list.
			 * If no price list is selected (new bill) the first one is taken.
			 */
			if (listSelected == null) {
				listSelected = lstArray.get(0);
			}
			prcListArray = prcArray.stream()
					.filter(price -> price.getList().getId() == listSelected.getId())
					.collect(Collectors.toList());
			/*
			 * Create a hashTable with the selected prices.
			 */
			priceHashTable = prcListArray.stream().collect(
					Collectors.toMap(price -> price.getList().getId() + price.getGroup() + price.getItem(), price -> price, (a, b) -> b, HashMap::new));

			/*
			 * Updates the items in the bill.
			 */
			for (BillItems item : billItems) {
				if (item.isPrice()) {
					Price p = priceHashTable.get(listSelected.getId() + item.getPriceID());
					item.setItemDescription(p.getDesc());
					item.setItemAmount(p.getPrice());
				}
			}

			/*
			 * Updates the totals.
			 */
			updateTotal();
			updateBigTotal();
			updateBalance();
		}

		@Override
		public Class<?> getColumnClass(int i) {
			return billClasses[i].getClass();
		}


		@Override
		public int getColumnCount() {
			return billClasses.length;
		}

		@Override
		public int getRowCount() {
			if (billItems == null) {
				return 0;
			}
			return billItems.size();
		}

		@Override
		public Object getValueAt(int r, int c) {
			BillItems item = billItems.get(r);
			if (c == -1) {
				return item;
			}
			if (c == 0) {
				return item.getItemDescription();
			}
			if (c == 1) {
				return item.getItemQuantity();
			}
			if (c == 2) {
				BigDecimal qty = new BigDecimal(item.getItemQuantity());
				BigDecimal amount = new BigDecimal(Double.toString(item.getItemAmount()));
				return amount.multiply(qty).doubleValue();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int r, int c) {
			return c == 1;
		}

		@Override
		public void setValueAt(Object item, int r, int c) {
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
		}

		@Override
		public String getColumnName(int columnIndex) {
			return billColumnNames[columnIndex];
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
		}

	}

	public class PaymentTableModel implements TableModel {

		public PaymentTableModel() {
			updateBalance();
		}

		@Override
		public void addTableModelListener(TableModelListener l) {

		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return paymentClasses[columnIndex].getClass();
		}

		@Override
		public int getColumnCount() {
			return paymentClasses.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return null;
		}

		@Override
		public int getRowCount() {
			return payItems.size();
		}

		@Override
		public Object getValueAt(int r, int c) {
			if (c == -1) {
				return payItems.get(r);
			}
			if (c == 0) {
				return formatDateTime(payItems.get(r).getDate());
			}
			if (c == 1) {
				return payItems.get(r).getAmount();
			}
			return null;
		}
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
		}
	}

	public String formatDateTime(LocalDateTime time) {
		return DATE_TIME_FORMATTER.format(time);
	}

}
