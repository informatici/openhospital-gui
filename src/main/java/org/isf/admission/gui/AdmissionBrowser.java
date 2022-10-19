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

import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.EventListenerList;

import org.isf.admission.gui.validation.OperationRowValidator;
import org.isf.admission.gui.ward.WardComboBoxInitializer;
import org.isf.admission.manager.AdmissionBrowserManager;
import org.isf.admission.model.Admission;
import org.isf.admission.model.AdmittedPatient;
import org.isf.admtype.model.AdmissionType;
import org.isf.disctype.model.DischargeType;
import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.dlvrrestype.manager.DeliveryResultTypeBrowserManager;
import org.isf.dlvrrestype.model.DeliveryResultType;
import org.isf.dlvrtype.manager.DeliveryTypeBrowserManager;
import org.isf.dlvrtype.model.DeliveryType;
import org.isf.examination.gui.PatientExaminationEdit;
import org.isf.examination.manager.ExaminationBrowserManager;
import org.isf.examination.model.GenderPatientExamination;
import org.isf.examination.model.PatientExamination;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.operation.gui.OperationRowAdm;
import org.isf.patient.gui.PatientSummary;
import org.isf.patient.model.Patient;
import org.isf.pregtreattype.manager.PregnantTreatmentTypeBrowserManager;
import org.isf.pregtreattype.model.PregnantTreatmentType;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.jobjects.GoodDateTimeSpinnerChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.ShadowBorder;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.RememberDates;
import org.isf.utils.time.TimeTools;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;
import org.isf.xmpp.gui.CommunicationFrame;
import org.isf.xmpp.manager.Interaction;

import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;

/**
 * This class shows essential patient data and allows to create an admission
 * record or modify an existing one.
 *
 * release 2.5 nov-10-06
 *
 * @author flavio
 * ----------------------------------------------------------
 * modification history
 * ====================
 * 23/10/06 - flavio - borders set to not resizable
 *                     changed Disease IN (/OUT) into Dignosis IN (/OUT)
 *
 * 10/11/06 - ross - added RememberDate for admission Date
 * 				   - only diseses with flag In Patient (IPD) are displayed
 *                 - on Insert. in edit all are displayed
 *                 - the correct way should be to display the IPD + the one aready registered
 * 18/08/08 - Alex/Andrea - Calendar added
 * 13/02/09 - Alex - Cosmetic changes to UI
 * 10/01/11 - Claudia - insert ward beds availability
 * 01/01/11 - Alex - GUI and code reengineering
 * 29/12/11 - Nicola - insert alert IN/OUT patient for communication module
 * -----------------------------------------------------------
 */
public class AdmissionBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private static final int PREFERRED_WIDTH_DATES = 225;
	private static final int PREFERRED_WIDTH_DIAGNOSIS = 550;
	private static final int PREFERRED_WIDTH_TYPES = 220;
	private static final int PREFERRED_HEIGHT_LINE = 24;

	private EventListenerList admissionListeners = new EventListenerList();

	public interface AdmissionListener extends EventListener {

		void admissionUpdated(AWTEvent e);

		void admissionInserted(AWTEvent e);
	}

	public void addAdmissionListener(AdmissionListener l) {
		admissionListeners.add(AdmissionListener.class, l);
	}

	public void removeAdmissionListener(AdmissionListener listener) {
		admissionListeners.remove(AdmissionListener.class, listener);
	}

	private void fireAdmissionInserted(Admission anAdmission) {
		AWTEvent event = new AWTEvent(anAdmission, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = admissionListeners.getListeners(AdmissionListener.class);
		for (EventListener listener : listeners) {
			((AdmissionListener) listener).admissionInserted(event);
		}
	}

	private void fireAdmissionUpdated(Admission anAdmission) {
		AWTEvent event = new AWTEvent(anAdmission, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = admissionListeners.getListeners(AdmissionListener.class);
		for (EventListener listener : listeners) {
			((AdmissionListener) listener).admissionUpdated(event);
		}
	}

	private PregnantTreatmentTypeBrowserManager pregnantTreatmentTypeBrowserManager = Context.getApplicationContext().getBean(PregnantTreatmentTypeBrowserManager.class);
	private DeliveryResultTypeBrowserManager deliveryResultTypeBrowserManager = Context.getApplicationContext().getBean(DeliveryResultTypeBrowserManager.class);
	private DeliveryTypeBrowserManager deliveryTypeBrowserManager = Context.getApplicationContext().getBean(DeliveryTypeBrowserManager.class);
	private DiseaseBrowserManager diseaseBrowserManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
	private WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private AdmissionBrowserManager admissionBrowserManager = Context.getApplicationContext().getBean(AdmissionBrowserManager.class);
	private ExaminationBrowserManager examinationBrowserManager = Context.getApplicationContext().getBean(ExaminationBrowserManager.class);

	private final OperationRowValidator operationRowValidator = new OperationRowValidator();
	private final DiseaseFinder diseaseFinder = new DiseaseFinder();

	private Patient patient;

	private boolean editing;

	private Admission admission = null;

	private PatientSummary ps;

	private JTextArea textArea = null;

	private JTabbedPane jTabbedPaneAdmission;

	private JPanel jPanelAdmission;

	private JPanel jPanelOperation;

	private JPanel jPanelDelivery;

	private int pregnancyTabIndex;

	private JPanel jContentPane = null;

	// enable is if patient is female
	private boolean enablePregnancy = false;

	// viewing is if you set ward to pregnancy
	private boolean viewingPregnancy = false;

	private LocalDateTime visitDate = null;

	private VoLimitedTextField weightField = null;

	private GoodDateTimeSpinnerChooser visitDateFieldCal = null;

	private JComboBox treatmTypeBox = null;

	private LocalDateTime deliveryDate = TimeTools.getNow();

	private GoodDateTimeSpinnerChooser deliveryDateFieldCal = null;

	private JComboBox deliveryTypeBox = null;

	private JComboBox deliveryResultTypeBox = null;

	private List<PregnantTreatmentType> treatmTypeList = null;

	private List<DeliveryType> deliveryTypeList = null;

	private List<DeliveryResultType> deliveryResultTypeList = null;

	private LocalDateTime ctrl1Date = null;

	private LocalDateTime ctrl2Date = null;

	private LocalDateTime abortDate = null;

	private GoodDateTimeSpinnerChooser ctrl1DateFieldCal = null;

	private GoodDateTimeSpinnerChooser ctrl2DateFieldCal = null;

	private GoodDateTimeSpinnerChooser abortDateFieldCal = null;

	private JComboBox<Ward> wardBox;

	// save value during a switch
	private Ward saveWard = null;

	private String saveYProg = null;

	private JTextField yProgTextField = null;

	private JTextField FHUTextField = null;

	private JPanel wardPanel;

	private JPanel fhuPanel;

	private JPanel yearProgPanel;

	private JComboBox diseaseInBox;

	private List<Disease> diseaseInList = null;

	private List<Disease> diseaseOutList = null;

	private List<Disease> diseaseAllList = null;

	private JCheckBox malnuCheck;

	private JPanel diseaseInPanel;

	private JPanel malnuPanel;

	private LocalDateTime dateIn;

	private GoodDateTimeSpinnerChooser dateInFieldCal = null;

	private JComboBox admTypeBox = null;

	private List<AdmissionType> admTypeList = null;

	private JPanel admissionDatePanel;

	private JPanel admissionTypePanel;

	private JComboBox diseaseOut1Box = null;

	private JComboBox diseaseOut2Box = null;

	private JComboBox diseaseOut3Box = null;

	private JPanel diseaseOutPanel;

	private JPanel diseaseOut1Panel;

	private JPanel diseaseOut2Panel;

	private JPanel diseaseOut3Panel;

	private LocalDateTime dateOut = null;

	private GoodDateTimeSpinnerChooser dateOutFieldCal = null;

	private JComboBox disTypeBox = null;

	private List<DischargeType> disTypeList = null;

	private JPanel dischargeDatePanel;

	private JPanel dischargeTypePanel;

	private JPanel bedDaysPanel;

	private JPanel buttonPanel = null;

	private JLabel labelRequiredFields;

	private JButton closeButton = null;

	private JButton saveButton = null;

	private JButton jButtonExamination = null;

	private JPanel visitDatePanel;

	private JPanel weightPanel;

	private JPanel treatmentPanel;

	private JPanel deliveryDatePanel;

	private JPanel deliveryTypePanel;

	private JPanel deliveryResultTypePanel;

	private JPanel control1DatePanel;

	private JPanel control2DatePanel;

	private JPanel abortDatePanel;

	private VoLimitedTextField bedDaysTextField;

	private OperationRowAdm operationad;

	private JButton searchButton;
	private JButton searchDiseaseOut1Button;
	private JButton searchDiseaseOut2Button;
	private JButton searchDiseaseOut3Button;

	/*
	 * from AdmittedPatientBrowser
	 */
	public AdmissionBrowser(JFrame parentFrame, AdmittedPatient admPatient, boolean editing) {
		super();
		setTitle(editing ? MessageBundle.getMessage("angal.admission.editadmissionrecord.title")
				: MessageBundle.getMessage("angal.admission.newadmission.title"));
		addAdmissionListener((AdmissionListener) parentFrame);
		this.editing = editing;
		patient = admPatient.getPatient();
		if (Character.toUpperCase(patient.getSex()) == 'F') {
			enablePregnancy = true;
		}
		ps = new PatientSummary(patient);

		try {
			diseaseOutList = diseaseBrowserManager.getDiseaseIpdOut();
			Admission admiss = admissionBrowserManager.getCurrentAdmission(patient);
			//TODO: remove this anti-pattern OperationRowAdm
			operationad = new OperationRowAdm(admiss);
			addAdmissionListener(operationad);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		try {
			diseaseInList = diseaseBrowserManager.getDiseaseIpdIn();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (editing) {
			admission = admissionBrowserManager.getCurrentAdmission(patient);
			if (admission != null && admission.getWard().getCode().equalsIgnoreCase("M")) {
				viewingPregnancy = true;
			}
		} else {
			admission = new Admission();
		}

		initialize(parentFrame);

		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				// to free memory
				if (diseaseInList != null) {
					diseaseInList.clear();
				}
				if (diseaseOutList != null) {
					diseaseOutList.clear();
				}
				if (diseaseAllList != null) {
					diseaseAllList.clear();
				}
				dispose();
			}
		});
	}

	/*
	 * from PatientDataBrowser
	 */
	public AdmissionBrowser(JFrame parentFrame, JFrame parentParentFrame, Patient aPatient, Admission anAdmission) {
		super();
		setTitle(MessageBundle.getMessage("angal.admission.editadmissionrecord.title"));
		addAdmissionListener((AdmissionListener) parentParentFrame);
		addAdmissionListener((AdmissionListener) parentFrame);
		this.editing = true;
		patient = aPatient;
		if (Character.toUpperCase(patient.getSex()) == 'F') {
			enablePregnancy = true;
		}
		ps = new PatientSummary(patient);
		//TODO: remove this anti-pattern OperationRowAdm
		operationad = new OperationRowAdm(anAdmission);
		addAdmissionListener(operationad);

		try {
			diseaseOutList = diseaseBrowserManager.getDiseaseIpdOut();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		try {
			diseaseInList = diseaseBrowserManager.getDiseaseIpdIn();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		try {
			admission = admissionBrowserManager.getAdmission(anAdmission.getId());
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (admission.getWard().getCode().equalsIgnoreCase("M")) {
			viewingPregnancy = true;
		}

		initialize(parentFrame);

		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				//to free memory
				if (diseaseInList != null) {
					diseaseInList.clear();
				}
				if (diseaseOutList != null) {
					diseaseOutList.clear();
				}
				if (diseaseAllList != null) {
					diseaseAllList.clear();
				}
				dispose();
			}
		});
	}

	private void initialize(JFrame parent) {
		this.add(getJContentPane(), BorderLayout.CENTER);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		showAsModal(parent);
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getDataPanel(), java.awt.BorderLayout.CENTER);
			jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	private JPanel getDataPanel() {
		JPanel data = new JPanel();
		data.setLayout(new BorderLayout());
		data.add(getPatientDataPanel(), java.awt.BorderLayout.WEST);
		data.add(getJTabbedPaneAdmission(), java.awt.BorderLayout.CENTER);
		return data;
	}

	private JPanel getPatientDataPanel() {
		JPanel data = new JPanel();
		data.add(ps.getPatientCompleteSummary());
		return data;
	}

	private JTabbedPane getJTabbedPaneAdmission() {
		if (jTabbedPaneAdmission == null) {
			jTabbedPaneAdmission = new JTabbedPane();
			jTabbedPaneAdmission.addTab(MessageBundle.getMessage("angal.admission.admissionanddischarge.title"), getAdmissionTab());
			jTabbedPaneAdmission.addTab(MessageBundle.getMessage("angal.admission.operation.title"), getMultiOperationTab());
			if (enablePregnancy) {
				jTabbedPaneAdmission.addTab(MessageBundle.getMessage("angal.admission.delivery.title"), getDeliveryTab());
				pregnancyTabIndex = jTabbedPaneAdmission.getTabCount() - 1;
				if (!viewingPregnancy) {
					jTabbedPaneAdmission.setEnabledAt(pregnancyTabIndex, false);
				}
			}
			jTabbedPaneAdmission.addTab(MessageBundle.getMessage("angal.common.note.title"), getJPanelNote());
		}
		return jTabbedPaneAdmission;
	}

	private JPanel getAdmissionTab() {
		if (jPanelAdmission == null) {
			jPanelAdmission = new JPanel();

			GroupLayout layout = new GroupLayout(jPanelAdmission);
			jPanelAdmission.setLayout(layout);

			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);

			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(getDiseaseInPanel())
							.addComponent(getDiseaseOutPanel())
							.addGroup(layout.createSequentialGroup()
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(getWardPanel())
											.addComponent(getAdmissionDatePanel())
											.addComponent(getDischargeDatePanel())
									)
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(getFHUPanel())
											.addComponent(getAdmissionTypePanel())
											.addComponent(getBedDaysPanel())
									)
									.addGroup(layout.createParallelGroup(LEADING)
											.addComponent(getProgYearPanel())
											.addComponent(getMalnutritionPanel())
											.addComponent(getDischargeTypePanel())
											.addComponent(getJLabelRequiredFields())
									)
							)
					)
			);

			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(getWardPanel())
							.addComponent(getFHUPanel())
							.addComponent(getProgYearPanel())
					)
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(getAdmissionDatePanel())
							.addComponent(getAdmissionTypePanel())
							.addComponent(getMalnutritionPanel())
					)
					.addComponent(getDiseaseInPanel())
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(getDischargeDatePanel())
							.addComponent(getBedDaysPanel())
							.addComponent(getDischargeTypePanel())
					)
					.addComponent(getDiseaseOutPanel())
					.addComponent(getJLabelRequiredFields())
			);
		}
		updateBedDays();
		return jPanelAdmission;
	}

	private JPanel getMultiOperationTab() {
		if (jPanelOperation == null) {
			jPanelOperation = new JPanel();
			jPanelOperation.setLayout(new BorderLayout(0, 0));
			jPanelOperation.add(operationad);
		}
		return jPanelOperation;
	}

	private JPanel getDeliveryTab() {
		if (jPanelDelivery == null) {
			jPanelDelivery = new JPanel();

			GroupLayout layout = new GroupLayout(jPanelDelivery);
			jPanelDelivery.setLayout(layout);

			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);

			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(getVisitDatePanel(), GroupLayout.PREFERRED_SIZE, PREFERRED_WIDTH_DATES, GroupLayout.PREFERRED_SIZE)
							.addComponent(getDeliveryDatePanel(), GroupLayout.PREFERRED_SIZE, PREFERRED_WIDTH_DATES, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(LEADING)
							.addComponent(getWeightPanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(getDeliveryTypePanel())
					)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
							.addComponent(getTreatmentPanel())
							.addComponent(getDeliveryResultTypePanel())
							.addComponent(getControl1DatePanel(), GroupLayout.PREFERRED_SIZE, PREFERRED_WIDTH_DATES, GroupLayout.PREFERRED_SIZE)
							.addComponent(getControl2DatePanel(), GroupLayout.PREFERRED_SIZE, PREFERRED_WIDTH_DATES, GroupLayout.PREFERRED_SIZE)
							.addComponent(getAbortDatePanel(), GroupLayout.PREFERRED_SIZE, PREFERRED_WIDTH_DATES, GroupLayout.PREFERRED_SIZE)
					)
			);

			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(getVisitDatePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(getWeightPanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(getTreatmentPanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup(BASELINE)
							.addComponent(getDeliveryDatePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(getDeliveryTypePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(getDeliveryResultTypePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup()
							.addComponent(getControl1DatePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup()
							.addComponent(getControl2DatePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(layout.createParallelGroup()
							.addComponent(getAbortDatePanel(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
			);

			layout.linkSize(SwingConstants.VERTICAL, getDeliveryDatePanel(), getDeliveryTypePanel(), getDeliveryResultTypePanel());
		}
		return jPanelDelivery;
	}

	private JScrollPane getJPanelNote() {

		JScrollPane scrollPane = new JScrollPane(getJTextAreaNote());
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 50, 0, 50), // external
				new ShadowBorder(5, Color.LIGHT_GRAY))); // internal
		scrollPane.addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

			@Override
			public void ancestorAdded(AncestorEvent event) {
				textArea.requestFocus();
			}
		});
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		scrollPane.setPreferredSize(new Dimension(screensize.width / 2, screensize.height / 2));
		return scrollPane;
	}

	private JTextArea getJTextAreaNote() {
		if (textArea == null) {
			textArea = new JTextArea();
			if (editing && admission.getNote() != null) {
				textArea.setText(admission.getNote());
			}
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setMargin(new Insets(10, 10, 10, 10));
		}
		return textArea;
	}

	private JPanel getTreatmentPanel() {
		if (treatmentPanel == null) {
			treatmentPanel = new JPanel();

			treatmTypeBox = new JComboBox();
			treatmTypeBox.addItem("");
			try {
				treatmTypeList = pregnantTreatmentTypeBrowserManager.getPregnantTreatmentType();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			if (treatmTypeList != null) {
				for (PregnantTreatmentType elem : treatmTypeList) {
					treatmTypeBox.addItem(elem);
					if (editing) {
						if (admission.getPregTreatmentType() != null && admission.getPregTreatmentType().getCode().equalsIgnoreCase(elem.getCode())) {
							treatmTypeBox.setSelectedItem(elem);
						}
					}
				}
			}

			treatmentPanel.add(treatmTypeBox);
			treatmentPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.treatmenttype.border")));
		}
		return treatmentPanel;
	}

	private JPanel getWeightPanel() {
		if (weightPanel == null) {
			weightPanel = new JPanel();

			weightField = new VoLimitedTextField(5, 5);
			if (editing && admission.getWeight() != null) {
				float weight = admission.getWeight();
				weightField.setText(String.valueOf(weight));
			}

			weightPanel.add(weightField);
			weightPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.weight.border")));
		}
		return weightPanel;
	}

	private JPanel getVisitDatePanel() {
		if (visitDatePanel == null) {
			visitDatePanel = new JPanel();

			if (editing && admission.getVisitDate() != null) {
				visitDate = admission.getVisitDate();
			} else {
				visitDate = TimeTools.getNow();
			}
			visitDateFieldCal = new GoodDateTimeSpinnerChooser(visitDate);

			visitDatePanel.add(visitDateFieldCal);
			visitDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.visitdate.border")));
		}
		return visitDatePanel;
	}

	private JPanel getDeliveryResultTypePanel() {
		if (deliveryResultTypePanel == null) {
			deliveryResultTypePanel = new JPanel();

			deliveryResultTypeBox = new JComboBox();
			deliveryResultTypeBox.addItem("");
			try {
				deliveryResultTypeList = deliveryResultTypeBrowserManager.getDeliveryResultType();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			if (deliveryResultTypeList != null) {
				for (DeliveryResultType elem : deliveryResultTypeList) {
					deliveryResultTypeBox.addItem(elem);
					if (editing) {
						if (admission.getDeliveryResult() != null && admission.getDeliveryResult().getCode().equalsIgnoreCase(elem.getCode())) {
							deliveryResultTypeBox.setSelectedItem(elem);
						}
					}
				}
			}
			deliveryResultTypePanel.add(deliveryResultTypeBox);
			deliveryResultTypePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.deliveryresultype.border")));
		}
		return deliveryResultTypePanel;
	}

	private JPanel getDeliveryTypePanel() {
		if (deliveryTypePanel == null) {
			deliveryTypePanel = new JPanel();

			deliveryTypeBox = new JComboBox();
			deliveryTypeBox.addItem("");
			try {
				deliveryTypeList = deliveryTypeBrowserManager.getDeliveryType();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			if (deliveryTypeList != null) {
				for (DeliveryType elem : deliveryTypeList) {
					deliveryTypeBox.addItem(elem);
					if (editing) {
						if (admission.getDeliveryType() != null && admission.getDeliveryType().getCode().equalsIgnoreCase(elem.getCode())) {
							deliveryTypeBox.setSelectedItem(elem);
						}
					}
				}
			}
			deliveryTypePanel.add(deliveryTypeBox);
			deliveryTypePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.deliverytype.border")));
		}
		return deliveryTypePanel;
	}

	private JPanel getDeliveryDatePanel() {
		if (deliveryDatePanel == null) {
			deliveryDatePanel = new JPanel();

			if (editing && admission.getDeliveryDate() != null) {
				deliveryDate = admission.getDeliveryDate();
			}
			deliveryDateFieldCal = new GoodDateTimeSpinnerChooser(deliveryDate);

			deliveryDatePanel.add(deliveryDateFieldCal);
			deliveryDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.deliverydate.border")));
		}
		return deliveryDatePanel;
	}

	private JPanel getAbortDatePanel() {
		if (abortDatePanel == null) {
			abortDatePanel = new JPanel();
			if (editing && admission.getAbortDate() != null) {
				abortDate = admission.getAbortDate();
			}
			abortDateFieldCal = new GoodDateTimeSpinnerChooser(abortDate);
			abortDatePanel.add(abortDateFieldCal);
			abortDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.abortdate.border")));
		}
		return abortDatePanel;
	}

	private JPanel getControl1DatePanel() {
		if (control1DatePanel == null) {
			control1DatePanel = new JPanel();

			if (editing && admission.getCtrlDate1() != null) {
				ctrl1Date = admission.getCtrlDate1();
			}
			ctrl1DateFieldCal = new GoodDateTimeSpinnerChooser(ctrl1Date);
			control1DatePanel.add(ctrl1DateFieldCal);
			control1DatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.controln1date.border")));
		}
		return control1DatePanel;
	}

	private JPanel getControl2DatePanel() {
		if (control2DatePanel == null) {
			control2DatePanel = new JPanel();

			if (editing && admission.getCtrlDate2() != null) {
				ctrl2Date = admission.getCtrlDate2();
			}
			ctrl2DateFieldCal = new GoodDateTimeSpinnerChooser(ctrl2Date);
			control2DatePanel.add(ctrl2DateFieldCal);
			control2DatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.controln2date.border")));
		}
		return control2DatePanel;
	}

	private JPanel getProgYearPanel() {
		if (yearProgPanel == null) {
			yearProgPanel = new JPanel();

			if (saveYProg != null) {
				yProgTextField = new JTextField(saveYProg);
			} else if (editing) {
				yProgTextField = new JTextField("" + admission.getYProg());
			} else {
				yProgTextField = new JTextField("");
			}
			yProgTextField.setColumns(11);

			yearProgPanel.add(yProgTextField);
			yearProgPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.progressiveinyear.border")));

		}
		return yearProgPanel;
	}

	private JPanel getFHUPanel() {
		if (fhuPanel == null) {
			fhuPanel = new JPanel();

			if (editing) {
				FHUTextField = new JTextField(admission.getFHU());
			} else {
				FHUTextField = new JTextField();
			}
			FHUTextField.setColumns(20);

			fhuPanel.add(FHUTextField);
			fhuPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.fromhealthunit.border")));

		}
		return fhuPanel;
	}

	private JPanel getWardPanel() {
		if (wardPanel == null) {
			wardPanel = new JPanel();

			wardBox = new JComboBox<>();

			new WardComboBoxInitializer(
					wardBox,
					wardBrowserManager,
					patient,
					saveWard,
					editing,
					admission
			).initialize();

			wardBox.addActionListener(actionEvent -> {
				// set yProg
				if (wardBox.getSelectedIndex() <= 0) {
					yProgTextField.setText("");
					// This fixes the problem of losing the top border on the date picker because of the combox
					javax.swing.SwingUtilities.invokeLater(() -> {
						validate();
						repaint();
					});
					return;
				} else {
					String wardId = ((Ward) wardBox.getSelectedItem()).getCode();
					if (editing && wardId.equalsIgnoreCase(admission.getWard().getCode())) {
						yProgTextField.setText("" + admission.getYProg());
					} else {
						int nextProg = 1;
						try {
							nextProg = admissionBrowserManager.getNextYProg(wardId);
						} catch (OHServiceException ex) {
							OHServiceExceptionUtil.showMessages(ex);
						}
						yProgTextField.setText("" + nextProg);

						// get default selected warn default beds number
						int nBeds = ((Ward) wardBox.getSelectedItem()).getBeds();
						int usedBeds = 0;
						try {
							usedBeds = admissionBrowserManager.getUsedWardBed(wardId);
						} catch (OHServiceException ex) {
							OHServiceExceptionUtil.showMessages(ex);
						}
						int freeBeds = nBeds - usedBeds;
						if (freeBeds <= 0) {
							MessageDialog.info(AdmissionBrowser.this, "angal.admission.wardwithnobedsavailable.msg");
						}
					}
				}

				// switch panel
				if (((Ward) wardBox.getSelectedItem()).getCode().equalsIgnoreCase("M")) {
					if (!viewingPregnancy) {
						saveWard = (Ward) wardBox.getSelectedItem();
						saveYProg = yProgTextField.getText();
						viewingPregnancy = true;
						jTabbedPaneAdmission.setEnabledAt(pregnancyTabIndex, true);
						validate();
						repaint();
					}
				} else {
					if (viewingPregnancy) {
						saveWard = (Ward) wardBox.getSelectedItem();
						saveYProg = yProgTextField.getText();
						viewingPregnancy = false;
						jTabbedPaneAdmission.setEnabledAt(pregnancyTabIndex, false);
						validate();
						repaint();
					}
				}
				// This fixes the problem of losing the top border on the date picker because of the combox
				javax.swing.SwingUtilities.invokeLater(() -> {
					validate();
					repaint();
				});
			});

			wardPanel.add(wardBox);
			wardPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.ward.border")));
		}
		return wardPanel;
	}

	private JPanel getDiseaseInPanel() {
		if (diseaseInPanel == null) {
			diseaseInPanel = new JPanel();
			diseaseInPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

			diseaseInPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.diagnosisinstar.border")));
			diseaseInPanel.add(Box.createHorizontalStrut(50));

			diseaseInBox = new JComboBox();
			diseaseInBox.setPreferredSize(new Dimension(PREFERRED_WIDTH_DIAGNOSIS, PREFERRED_HEIGHT_LINE));

			Disease diseaseIn = admission.getDiseaseIn();
			diseaseInBox.removeAllItems();
			diseaseInBox.addItem(null);
			Optional<Disease> found = diseaseFinder.findAndSelectDisease(diseaseIn, diseaseInList, diseaseInBox);

			if (editing && !found.isPresent() && diseaseIn != null) {

				// Not found: search among all diseases
				try {
					if (diseaseAllList == null) {
						diseaseAllList = diseaseBrowserManager.getDiseaseAll();
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				found = diseaseFinder.findAndSelectFromAllDiseases(diseaseIn, diseaseAllList, diseaseInBox);

				if (!found.isPresent()) {
					// Still not found
					diseaseInBox.addItem(
							MessageBundle.formatMessage("angal.admission.notfoundasinpatientdisease.fmt.msg", admission.getDiseaseIn().getDescription()));
					diseaseInBox.setSelectedIndex(diseaseInBox.getItemCount() - 1);
				}
			}

			JTextField searchDiseasetextField = new JTextField();
			diseaseInPanel.add(searchDiseasetextField);
			searchDiseasetextField.setColumns(10);
			searchDiseasetextField.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						searchButton.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});

			searchButton = new JButton("");
			diseaseInPanel.add(searchButton);
			searchButton.addActionListener(actionEvent -> {
				diseaseInBox.removeAllItems();
				diseaseInBox.addItem("");
				for (Disease disease : diseaseFinder.getSearchDiagnosisResults(searchDiseasetextField.getText(), diseaseInList)) {
					diseaseInBox.addItem(disease);
				}

				if (diseaseInBox.getItemCount() >= 2) {
					diseaseInBox.setSelectedIndex(1);
				}
				diseaseInBox.requestFocus();
				if (diseaseInBox.getItemCount() > 2) {
					diseaseInBox.showPopup();
				}
			});
			searchButton.setPreferredSize(new Dimension(20, 20));
			searchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));

			diseaseInPanel.add(diseaseInBox);
		}
		return diseaseInPanel;
	}

	/**
	 * @return
	 */
	private JPanel getMalnutritionPanel() {
		if (malnuPanel == null) {
			malnuPanel = new JPanel();

			malnuCheck = new JCheckBox();
			malnuCheck.setSelected(editing && admission.getType().equalsIgnoreCase("M"));

			malnuPanel.add(malnuCheck);
			malnuPanel.add(new JLabel(MessageBundle.getMessage("angal.admission.malnutrition.txt")), BorderLayout.CENTER);

		}
		return malnuPanel;
	}

	private JPanel getAdmissionTypePanel() {
		if (admissionTypePanel == null) {
			admissionTypePanel = new JPanel();

			admTypeBox = new JComboBox();
			admTypeBox.setPreferredSize(new Dimension(PREFERRED_WIDTH_TYPES, PREFERRED_HEIGHT_LINE));
			admTypeBox.addItem("");
			try {
				admTypeList = admissionBrowserManager.getAdmissionType();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			for (AdmissionType elem : admTypeList) {
				admTypeBox.addItem(elem);
				if (editing) {
					if (admission.getAdmType().getCode().equalsIgnoreCase(elem.getCode())) {
						admTypeBox.setSelectedItem(elem);
					}
				}
			}

			admissionTypePanel.add(admTypeBox);
			admissionTypePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.admissiontype.border")));

		}
		return admissionTypePanel;
	}

	/**
	 * @return
	 */
	private JPanel getAdmissionDatePanel() {
		if (admissionDatePanel == null) {
			admissionDatePanel = new JPanel();

			if (editing) {
				dateIn = admission.getAdmDate();
			} else {
				dateIn = RememberDates.getLastAdmInDate();
			}
			if (dateIn == null) {
				dateIn = TimeTools.getNow();
			}
			dateInFieldCal = new GoodDateTimeSpinnerChooser(dateIn, false);

			dateInFieldCal.addDateTimeChangeListener(event -> {
				DateChangeEvent dateChangeEvent = event.getDateChangeEvent();
				if (dateChangeEvent != null) {
					LocalDate newDate = dateChangeEvent.getNewDate();
					if (newDate != null && newDate.isBefore(patient.getBirthDate())) {
						Runnable doMessage = () -> MessageDialog.error(null, "angal.admission.thepatientwasnotyetbornatselecteddate.msg");
						SwingUtilities.invokeLater(doMessage);
						dateInFieldCal.setDate(dateChangeEvent.getOldDate());
						return;
					}
					updateBedDays();
					getDiseaseInPanel();
					getDiseaseOut1Panel();
					getDiseaseOut2Panel();
					getDiseaseOut3Panel();
				}
			});
			admissionDatePanel.add(dateInFieldCal);
			admissionDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.admissiondate.border")));
		}
		return admissionDatePanel;
	}

	private JPanel getDiseaseOutPanel() {
		if (diseaseOutPanel == null) {
			diseaseOutPanel = new JPanel();
			diseaseOutPanel.setLayout(new BoxLayout(diseaseOutPanel, BoxLayout.Y_AXIS));
			diseaseOutPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.diagnosisout.border")));
			diseaseOutPanel.add(getDiseaseOut1Panel());
			diseaseOutPanel.add(getDiseaseOut2Panel());
			diseaseOutPanel.add(getDiseaseOut3Panel());
		}
		return diseaseOutPanel;
	}

	/**
	 * @return
	 */
	private JPanel getDiseaseOut1Panel() {
		if (diseaseOut1Panel == null) {
			diseaseOut1Panel = new JPanel();
			diseaseOut1Panel.setLayout(new FlowLayout(FlowLayout.LEFT));

			JLabel label = new JLabel(MessageBundle.getMessage("angal.admission.number1.txt"), SwingConstants.RIGHT);
			label.setPreferredSize(new Dimension(50, 50));
			label.setHorizontalTextPosition(SwingConstants.RIGHT);

			diseaseOut1Panel.add(label);

			diseaseOut1Box = new JComboBox();
			diseaseOut1Box.setPreferredSize(new Dimension(PREFERRED_WIDTH_DIAGNOSIS, PREFERRED_HEIGHT_LINE));

			Disease diseaseOut1 = admission.getDiseaseOut1();
			diseaseOut1Box.removeAllItems();
			diseaseOut1Box.addItem("");
			Optional<Disease> found = diseaseFinder.findAndSelectDisease(diseaseOut1, diseaseOutList, diseaseOut1Box);

			if (editing && !found.isPresent() && diseaseOut1 != null) {
				// Not found: search among all diseases
				try {
					if (diseaseAllList == null) {
						diseaseAllList = diseaseBrowserManager.getDiseaseAll();
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				found = diseaseFinder.findAndSelectFromAllDiseases(diseaseOut1, diseaseAllList, diseaseInBox);

				if (!found.isPresent()) {
					// Still not found
					diseaseOut1Box.addItem(MessageBundle.formatMessage("angal.admission.notfoundasoutpatientdisease.fmt.msg",
							admission.getDiseaseOut1().getDescription(), 1));
					diseaseOut1Box.setSelectedIndex(diseaseOut1Box.getItemCount() - 1);
				}
			}

			/////////////
			JTextField searchDiseaseOut1textField = new JTextField();
			diseaseOut1Panel.add(searchDiseaseOut1textField);
			searchDiseaseOut1textField.setColumns(10);
			searchDiseaseOut1textField.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						searchDiseaseOut1Button.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});

			searchDiseaseOut1Button = new JButton("");
			diseaseOut1Panel.add(searchDiseaseOut1Button);
			searchDiseaseOut1Button.addActionListener(actionEvent -> {
				diseaseOut1Box.removeAllItems();
				diseaseOut1Box.addItem("");
				for (Disease disease : diseaseFinder.getSearchDiagnosisResults(searchDiseaseOut1textField.getText(), diseaseOutList)) {
					diseaseOut1Box.addItem(disease);
				}

				if (diseaseOut1Box.getItemCount() >= 2) {
					diseaseOut1Box.setSelectedIndex(1);
				}
				diseaseOut1Box.requestFocus();
				if (diseaseOut1Box.getItemCount() > 2) {
					diseaseOut1Box.showPopup();
				}
			});
			searchDiseaseOut1Button.setPreferredSize(new Dimension(20, 20));
			searchDiseaseOut1Button.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			/////////////

			diseaseOut1Panel.add(diseaseOut1Box);
		}
		return diseaseOut1Panel;
	}

	private JPanel getDiseaseOut2Panel() {

		if (diseaseOut2Panel == null) {
			diseaseOut2Panel = new JPanel();
			diseaseOut2Panel.setLayout(new FlowLayout(FlowLayout.LEFT));

			JLabel label = new JLabel(MessageBundle.getMessage("angal.admission.number2.txt"), SwingConstants.RIGHT);
			label.setPreferredSize(new Dimension(50, 50));
			label.setHorizontalTextPosition(SwingConstants.RIGHT);

			diseaseOut2Panel.add(label);

			diseaseOut2Box = new JComboBox();
			diseaseOut2Box.setPreferredSize(new Dimension(PREFERRED_WIDTH_DIAGNOSIS, PREFERRED_HEIGHT_LINE));

			Disease diseaseOut2 = admission.getDiseaseOut2();
			diseaseOut2Box.removeAllItems();
			diseaseOut2Box.addItem("");
			Optional<Disease> found = diseaseFinder.findAndSelectDisease(diseaseOut2, diseaseOutList, diseaseOut2Box);

			if (editing && !found.isPresent() && diseaseOut2 != null) {
				// Not found: search among all diseases
				try {
					if (diseaseAllList == null) {
						diseaseAllList = diseaseBrowserManager.getDiseaseAll();
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				found = diseaseFinder.findAndSelectFromAllDiseases(diseaseOut2, diseaseAllList, diseaseInBox);

				if (!found.isPresent()) {
					// Still not found
					diseaseOut2Box.addItem(MessageBundle.formatMessage("angal.admission.notfoundasoutpatientdisease.fmt.msg",
							admission.getDiseaseOut2().getDescription(), 2));
					diseaseOut2Box.setSelectedIndex(diseaseOut2Box.getItemCount() - 1);
				}
			}

			/////////////
			JTextField searchDiseaseOut2textField = new JTextField();
			diseaseOut2Panel.add(searchDiseaseOut2textField);
			searchDiseaseOut2textField.setColumns(10);
			searchDiseaseOut2textField.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						searchDiseaseOut2Button.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});

			searchDiseaseOut2Button = new JButton("");
			diseaseOut2Panel.add(searchDiseaseOut2Button);
			searchDiseaseOut2Button.addActionListener(actionEvent -> {
				diseaseOut2Box.removeAllItems();
				diseaseOut2Box.addItem("");
				for (Disease disease : diseaseFinder.getSearchDiagnosisResults(searchDiseaseOut2textField.getText(), diseaseOutList)) {
					diseaseOut2Box.addItem(disease);
				}

				if (diseaseOut2Box.getItemCount() >= 2) {
					diseaseOut2Box.setSelectedIndex(1);
				}
				diseaseOut2Box.requestFocus();
				if (diseaseOut2Box.getItemCount() > 2) {
					diseaseOut2Box.showPopup();
				}
			});
			searchDiseaseOut2Button.setPreferredSize(new Dimension(20, 20));
			searchDiseaseOut2Button.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			/////////////

			diseaseOut2Panel.add(diseaseOut2Box);
		}
		return diseaseOut2Panel;
	}

	private JPanel getDiseaseOut3Panel() {

		if (diseaseOut3Panel == null) {
			diseaseOut3Panel = new JPanel();
			diseaseOut3Panel.setLayout(new FlowLayout(FlowLayout.LEFT));

			JLabel label = new JLabel(MessageBundle.getMessage("angal.admission.number3.txt"), SwingConstants.RIGHT);
			label.setPreferredSize(new Dimension(50, 50));

			diseaseOut3Panel.add(label);

			diseaseOut3Box = new JComboBox();
			diseaseOut3Box.setPreferredSize(new Dimension(PREFERRED_WIDTH_DIAGNOSIS, PREFERRED_HEIGHT_LINE));

			Disease diseaseOut3 = admission.getDiseaseOut3();
			diseaseOut3Box.removeAllItems();
			diseaseOut3Box.addItem("");
			// TODO: populate diseaseList
			Optional<Disease> found = diseaseFinder.findAndSelectDisease(diseaseOut3, diseaseOutList, diseaseOut3Box);

			if (editing && !found.isPresent() && diseaseOut3 != null) {
				// Not found: search among all diseases
				List<Disease> diseaseAllList = null;
				try {
					diseaseAllList = diseaseBrowserManager.getDiseaseAll();
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				found = diseaseFinder.findAndSelectFromAllDiseases(diseaseOut3, diseaseAllList, diseaseInBox);

				if (!found.isPresent()) {
					// Still not found
					diseaseOut3Box.addItem(MessageBundle.formatMessage("angal.admission.notfoundasoutpatientdisease.fmt.msg",
							admission.getDiseaseOut2().getDescription(), 3));
					diseaseOut3Box.setSelectedIndex(diseaseOut3Box.getItemCount() - 1);
				}
			}

			/////////////
			JTextField searchDiseaseOut3textField = new JTextField();
			diseaseOut3Panel.add(searchDiseaseOut3textField);
			searchDiseaseOut3textField.setColumns(10);
			searchDiseaseOut3textField.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						searchDiseaseOut3Button.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});

			searchDiseaseOut3Button = new JButton("");
			diseaseOut3Panel.add(searchDiseaseOut3Button);
			searchDiseaseOut3Button.addActionListener(actionEvent -> {
				diseaseOut3Box.removeAllItems();
				diseaseOut3Box.addItem("");
				for (Disease disease : diseaseFinder.getSearchDiagnosisResults(searchDiseaseOut3textField.getText(), diseaseOutList)) {
					diseaseOut3Box.addItem(disease);
				}

				if (diseaseOut3Box.getItemCount() >= 2) {
					diseaseOut3Box.setSelectedIndex(1);
				}
				diseaseOut3Box.requestFocus();
				if (diseaseOut3Box.getItemCount() > 2) {
					diseaseOut3Box.showPopup();
				}
			});
			searchDiseaseOut3Button.setPreferredSize(new Dimension(20, 20));
			searchDiseaseOut3Button.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			/////////////

			diseaseOut3Panel.add(diseaseOut3Box);
		}
		return diseaseOut3Panel;
	}

	private JPanel getDischargeTypePanel() {
		if (dischargeTypePanel == null) {
			dischargeTypePanel = new JPanel();

			disTypeBox = new JComboBox();
			disTypeBox.setPreferredSize(new Dimension(PREFERRED_WIDTH_TYPES, PREFERRED_HEIGHT_LINE));
			disTypeBox.addItem("");
			try {
				disTypeList = admissionBrowserManager.getDischargeType();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			for (DischargeType elem : disTypeList) {
				disTypeBox.addItem(elem);
				if (editing) {
					if (admission.getDisType() != null && admission.getDisType().getCode().equalsIgnoreCase(elem.getCode())) {
						disTypeBox.setSelectedItem(elem);
					}
				}
			}

			dischargeTypePanel.add(disTypeBox);
			dischargeTypePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.dischargetype.border")));
		}
		return dischargeTypePanel;
	}

	private JPanel getBedDaysPanel() {
		if (bedDaysPanel == null) {
			bedDaysPanel = new JPanel();

			bedDaysTextField = new VoLimitedTextField(10, 10);
			bedDaysTextField.setEditable(false);

			bedDaysPanel.add(bedDaysTextField);
			bedDaysPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.beddays.border")));
		}
		return bedDaysPanel;
	}

	private void updateBedDays() {
		try {
			LocalDateTime admission = dateInFieldCal.getLocalDateTime();
			LocalDateTime discharge = dateOutFieldCal.getLocalDateTime();
			int bedDays = TimeTools.getDaysBetweenDates(admission, discharge, false);
			if (bedDays == 0) {
				bedDays++;
			}
			bedDaysTextField.setText(String.valueOf(bedDays));
		} catch (Exception e) {
			bedDaysTextField.setText("");
		}
	}

	/**
	 * @return
	 */
	private JPanel getDischargeDatePanel() {
		if (dischargeDatePanel == null) {
			dischargeDatePanel = new JPanel();

			if (editing && admission.getDisDate() != null) {
				dateOut = admission.getDisDate();
			}
			dateOutFieldCal = new GoodDateTimeSpinnerChooser(editing ? dateOut : null, false);
			dateOutFieldCal.addDateTimeChangeListener(event -> {
				DateChangeEvent dateChangeEvent = event.getDateChangeEvent();
				if (dateChangeEvent != null) {
					// if the time is blank set it to the current time; otherwise leave it alone
					TimePicker timePicker = event.getTimePicker();
					if (timePicker.getTime() == null) {
						timePicker.setTime(LocalTime.now());
					}
					updateBedDays();
				}
			});

			dischargeDatePanel.add(dateOutFieldCal);
			dischargeDatePanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.dischargedate.border")));
		}
		return dischargeDatePanel;
	}

	private JComboBox shareWith = null;

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getSaveButton());
			if (MainMenu.checkUserGrants("btnadmadmexamination")) {
				buttonPanel.add(getJButtonExamination());
			}
			buttonPanel.add(getCloseButton());

			if (GeneralData.XMPPMODULEENABLED) {
				Interaction share = new Interaction();
				Collection<String> contacts = share.getContactOnline();
				String note = MessageBundle.getMessage("angal.admission.sharealertwithnobody.txt");
				contacts.add(note);
				shareWith = new JComboBox(contacts.toArray());
				shareWith.setSelectedItem(note);
				buttonPanel.add(shareWith);
			}
		}
		return buttonPanel;
	}

	private JButton getJButtonExamination() {
		if (jButtonExamination == null) {
			jButtonExamination = new JButton(MessageBundle.getMessage("angal.admission.examination.btn"));
			jButtonExamination.setMnemonic(MessageBundle.getMnemonic("angal.admission.examination.btn.key"));

			jButtonExamination.addActionListener(actionEvent -> {

				PatientExamination patex;

				PatientExamination lastPatex = null;
				try {
					lastPatex = examinationBrowserManager.getLastByPatID(patient.getCode());
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
				if (lastPatex != null) {
					patex = examinationBrowserManager.getFromLastPatientExamination(lastPatex);
				} else {
					patex = examinationBrowserManager.getDefaultPatientExamination(patient);
				}

				GenderPatientExamination gpatex = new GenderPatientExamination(patex, patient.getSex() == 'M');

				PatientExaminationEdit dialog = new PatientExaminationEdit(AdmissionBrowser.this, gpatex);
				dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.showAsModal(AdmissionBrowser.this);
			});
		}
		return jButtonExamination;
	}

	/**
	 * @return
	 */
	private JLabel getJLabelRequiredFields() {
		if (labelRequiredFields == null) {
			labelRequiredFields = new JLabel(MessageBundle.getMessage("angal.admission.indicatesrequiredfields.txt"));
		}
		return labelRequiredFields;
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			closeButton.addActionListener(actionEvent -> dispose());
		}
		return closeButton;
	}

	private JButton getSaveButton() {

		if (saveButton == null) {
			saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
			saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
			saveButton.addActionListener(actionEvent -> {

				/*
				 * is it an admission update or a discharge? if we have a
				 * valid discharge date isDischarge will be true
				 */
				boolean isDischarge = false;

				/*
				 * set if ward pregnancy is selected
				 */
				boolean isPregnancy = false;

				// get ward id (not null)
				if (wardBox.getSelectedIndex() == 0 || wardBox.getSelectedItem() == null) {
					MessageDialog.error(AdmissionBrowser.this, "angal.admission.pleaseselectavalidward.msg");
					return;
				}
				admission.setWard((Ward) (wardBox.getSelectedItem()));
				if (admission.getWard().getCode().equalsIgnoreCase("M")) {
					isPregnancy = true;
				}

				// get disease in id ( it can be null)
				if (diseaseInBox.getSelectedIndex() == 0) {
					MessageDialog.error(AdmissionBrowser.this, "angal.admission.pleaseselectavaliddiseasein.msg");
					return;
				}
				try {
					Disease diseaseIn = (Disease) diseaseInBox.getSelectedItem();
					admission.setDiseaseIn(diseaseIn);
				} catch (IndexOutOfBoundsException e1) {
					/*
					 * Workaround in case a fake-disease is selected (ie
					 * when previous disease has been deleted)
					 */
					admission.setDiseaseIn(null);
				}

				// get disease out id ( it can be null)
				int disease1index = diseaseOut1Box.getSelectedIndex();
				if (disease1index == 0) {
					admission.setDiseaseOut1(null);
				} else {
					Disease diseaseOut1 = (Disease) diseaseOut1Box.getSelectedItem();
					admission.setDiseaseOut1(diseaseOut1);
				}

				// get disease out id 2 ( it can be null)
				int disease2index = diseaseOut2Box.getSelectedIndex();
				if (disease2index == 0) {
					admission.setDiseaseOut2(null);
				} else {
					Disease diseaseOut2 = (Disease) diseaseOut2Box.getSelectedItem();
					admission.setDiseaseOut2(diseaseOut2);
				}

				// get disease out id 3 ( it can be null)
				int disease3index = diseaseOut3Box.getSelectedIndex();
				if (disease3index == 0) {
					admission.setDiseaseOut3(null);
				} else {
					Disease diseaseOut3 = (Disease) diseaseOut3Box.getSelectedItem();
					admission.setDiseaseOut3(diseaseOut3);
				}

				// get year prog (not null)
				admission.setYProg(Integer.parseInt(yProgTextField.getText()));

				// get FHU (it can be null)
				String s = FHUTextField.getText();
				if (s.equals("")) {
					admission.setFHU(null);
				} else {
					admission.setFHU(FHUTextField.getText());
				}

				dateIn = dateInFieldCal.getLocalDateTime();
				if (dateIn == null) {
					MessageDialog.error(AdmissionBrowser.this, "angal.admission.pleaseinsertavalidadmissiondate.msg");
					return;
				}
				admission.setAdmDate(dateIn);
				RememberDates.setLastAdmInDate(dateIn);

				// get admission type (not null)
				if (admTypeBox.getSelectedIndex() == 0) {
					MessageDialog.error(AdmissionBrowser.this, "angal.admission.pleaseselectavalidadmissiontype.msg");
					return;
				}
				admission.setAdmType(admTypeList.get(admTypeBox.getSelectedIndex() - 1));

				// check and get date out (it can be null)
				// if set date out, isDischarge is set
				if (dateOutFieldCal.getLocalDateTime() != null) {
					dateOut = dateOutFieldCal.getLocalDateTime();
					admission.setDisDate(dateOut);
					isDischarge = true;
				} else {
					admission.setDisDate(null);
				}

				// get discharge type (it can be null)
				// if isDischarge, null value not allowed
				if (disTypeBox.getSelectedIndex() == 0) {
					if (isDischarge) {
						MessageDialog.error(AdmissionBrowser.this, "angal.admission.pleaseselectavaliddischargetype.msg");
						return;
					}
					admission.setDisType(null);
				} else {
					if (dateOut == null) {
						MessageDialog.error(AdmissionBrowser.this, "angal.admission.pleaseinsertadischargedate.msg");
						return;
					}
					if (isDischarge) {
						admission.setDisType(disTypeList.get(disTypeBox.getSelectedIndex() - 1));
					} else {
						admission.setDisType(null);
					}
				}

				// field notes
				if (textArea.getText().equals("")) {
					admission.setNote(null);
				} else {
					admission.setNote(textArea.getText());
				}

				// fields for pregnancy status
				if (isPregnancy) {

					// get weight (it can be null)
					try {
						if (weightField.getText().equals("")) {
							admission.setWeight(null);
						} else {
							float f = Float.parseFloat(weightField.getText());
							admission.setWeight(f);
						}
					} catch (Exception ex) {
						MessageDialog.error(AdmissionBrowser.this, "angal.admission.pleaseinsertavalidweightvalue.msg");
						return;
					}

					// get treatment type(may be null)
					if (treatmTypeBox.getSelectedIndex() == 0) {
						admission.setPregTreatmentType(null);
					} else {
						admission.setPregTreatmentType(treatmTypeList.get(treatmTypeBox.getSelectedIndex() - 1));

					}

					// get visit date
					if (visitDateFieldCal.getLocalDateTime() != null) {
						visitDate = visitDateFieldCal.getLocalDateTime();
						admission.setVisitDate(visitDate);
					} else {
						admission.setVisitDate(null);
					}

					// get delivery date
					if (deliveryDateFieldCal.getLocalDateTime() != null) {
						deliveryDate = deliveryDateFieldCal.getLocalDateTime();
						admission.setDeliveryDate(deliveryDate);
					} else {
						admission.setDeliveryDate(null);
					}

					// get delivery type
					if (deliveryTypeBox.getSelectedIndex() == 0) {
						admission.setDeliveryType(null);
					} else {
						admission.setDeliveryType(deliveryTypeList.get(deliveryTypeBox.getSelectedIndex() - 1));
					}

					// get delivery result type
					if (deliveryResultTypeBox.getSelectedIndex() == 0) {
						admission.setDeliveryResult(null);
					} else {
						admission.setDeliveryResult(deliveryResultTypeList.get(deliveryResultTypeBox.getSelectedIndex() - 1));
					}

					// get ctrl1 date
					if (ctrl1DateFieldCal.getLocalDateTime() != null) {
						ctrl1Date = ctrl1DateFieldCal.getLocalDateTime();
						admission.setCtrlDate1(ctrl1Date);
					} else {
						admission.setCtrlDate1(null);
					}

					// get ctrl2 date
					if (ctrl2DateFieldCal.getLocalDateTime() != null) {
						ctrl2Date = ctrl2DateFieldCal.getLocalDateTime();
						admission.setCtrlDate2(ctrl2Date);
					} else {
						admission.setCtrlDate2(null);
					}

					// get abort date
					if (abortDateFieldCal.getLocalDateTime() != null) {
						abortDate = abortDateFieldCal.getLocalDateTime();
						admission.setAbortDate(abortDate);
					} else {
						admission.setAbortDate(null);
					}
				} // isPregnancy

				// set not editable fields
				String user = UserBrowsingManager.getCurrentUser();
				admission.setUserID(user);
				admission.setPatient(patient);

				if (admission.getDisDate() == null) {
					admission.setAdmitted(1);
				} else {
					admission.setAdmitted(0);
				}

				if (malnuCheck.isSelected()) {
					admission.setType("M");
				} else {
					admission.setType("N");
				}

				// IOoperation result
				boolean result = false;

				// ready to save...
				if (!editing && !isDischarge) {
					List<OHExceptionMessage> errors = operationRowValidator.checkAllOperationRowDate(operationad.getOprowData(), admission);
					if (!errors.isEmpty()) {
						OHServiceExceptionUtil.showMessages(new OHServiceException(errors));
					} else {
						int newKey = -1;
						try {
							newKey = admissionBrowserManager.newAdmissionReturnKey(admission);
						} catch (OHServiceException exc) {
							OHServiceExceptionUtil.showMessages(exc);
						}
						if (newKey > 0) {
							result = true;
							admission.setId(newKey);
							fireAdmissionInserted(admission);
							if (GeneralData.XMPPMODULEENABLED) {
								CommunicationFrame frame = (CommunicationFrame) CommunicationFrame.getFrame();
								frame.sendMessage(MessageBundle.formatMessage("angal.admission.newpatientadmissionin.fmt.msg", patient.getName(),
												((Ward) wardBox.getSelectedItem()).getDescription()),
										(String) shareWith.getSelectedItem(), false);
							}
							dispose();
						}
					}

				} else if (!editing && isDischarge) {
					List<OHExceptionMessage> errors = operationRowValidator.checkAllOperationRowDate(operationad.getOprowData(), admission);
					if (!errors.isEmpty()) {
						OHServiceExceptionUtil.showMessages(new OHServiceException(errors));
					} else {
						try {
							result = admissionBrowserManager.newAdmission(admission);
						} catch (OHServiceException ex) {
							OHServiceExceptionUtil.showMessages(ex);
						}
						if (result) {
							fireAdmissionUpdated(admission);
							dispose();
						}
					}

				} else {
					List<OHExceptionMessage> errors = operationRowValidator.checkAllOperationRowDate(operationad.getOprowData(), admission);
					if (!errors.isEmpty()) {
						OHServiceExceptionUtil.showMessages(new OHServiceException(errors));
					} else {
						try {
							result = admissionBrowserManager.updateAdmission(admission);
						} catch (OHServiceException ex) {
							OHServiceExceptionUtil.showMessages(ex);
						}
						if (result) {
							fireAdmissionUpdated(admission);
							if (GeneralData.XMPPMODULEENABLED) {
								CommunicationFrame frame = (CommunicationFrame) CommunicationFrame.getFrame();
								frame.sendMessage(MessageBundle.formatMessage("angal.admisssion.discharedpatientfor.fmt.msg",
												patient.getName(), ((DischargeType) disTypeBox.getSelectedItem()).getDescription()),
										(String) shareWith.getSelectedItem(), false);
							}
							dispose();
						}
					}
				}

				if (!result) {
					MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
				} else {
					dispose();
				}
			});
		}
		return saveButton;
	}

}
