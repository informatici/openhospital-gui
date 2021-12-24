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
package org.isf.visits.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.JDateAndTimeChooserDialog;
import org.isf.utils.jobjects.LocalDateSupportingJDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.TimeTools;
import org.isf.visits.manager.VisitManager;
import org.isf.visits.model.Visit;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

import com.toedter.calendar.JDateChooser;

/**
 * @author Mwithi
 */
public class InsertVisit extends JDialog implements SelectionListener {

	private static final long serialVersionUID = 1L;

	/*
	 * Constants
	 */
	private static final String DATE_TIME_FORMAT = "dd/MM/yy HH:mm:ss";
	private static final Integer DEFAULT_DURATION = 30;
	private static final int PREFERRED_SPINNER_WIDTH = 100;
	private static final int ONE_LINE_COMPONENTS_HEIGHT = 30;
	
	/*
	 * Attributes
	 */
	private LocalDateSupportingJDateChooser visitDateChooser;
	private JPanel buttonsPanel;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JPanel servicePanel;
	private JTextField serviceField;
	private JPanel durationPanel;
	private JPanel dateViPanel;
	private JButton admButton;
	private JButton jButtonPickPatient;
	private JTextField patientTextField;
	private Patient patientSelected;
	private JSpinner jSpinnerDur;

	/*
	 * Return Value
	 */
	private LocalDateTime visitDate = null;
	private JPanel wardPanel;
	private JComboBox<Ward> wardBox;
	private Ward ward;
	private Visit visit;
	private boolean insert = false;
	
	/*
	 * Managers
	 */
	private WardBrowserManager wbm = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private VisitManager visitManager = Context.getApplicationContext().getBean(VisitManager.class);
	private List<Ward> wardList = new ArrayList<>();
	
	public InsertVisit(JFrame owner, Ward ward, Patient patient, boolean insert) {
		super(owner, true);
		setTitle(MessageBundle.getMessage("angal.visit.addvisit.title"));
		this.patientSelected = patient;
		this.ward = ward;
		this.insert = insert;
		initComponents();
	}

	public InsertVisit(JDialog owner, LocalDateTime date) {
		super(owner, true);
		this.visitDate = date;
		initComponents();
	}

	public InsertVisit(JFrame owner, LocalDateTime date, Ward ward, Patient patient, boolean insert) {
		super(owner, true);
		this.patientSelected = patient;
		this.visitDate = date;
		this.ward = ward;
		this.insert = insert;
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(getpVisitInf());
		getContentPane().add(getButtonsPanel(), BorderLayout.SOUTH);

		setResizable(false);
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getpVisitInf() {

		JPanel patientParamsPanel = new JPanel(new SpringLayout());

		GridBagLayout jPanelData = new GridBagLayout();
		jPanelData.columnWidths = new int[] { 20, 20, 20, 0, 0, 0 };
		jPanelData.rowHeights = new int[] { 20, 20, 20, 0, 0, 0, 0, 0 };
		jPanelData.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		jPanelData.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		patientParamsPanel.setLayout(jPanelData);

		GridBagConstraints ward = new GridBagConstraints();
		ward.fill = GridBagConstraints.VERTICAL;
		ward.anchor = GridBagConstraints.WEST;

		ward.gridy = 0;
		ward.gridx = 0;
		patientParamsPanel.add(getWardPanel(), ward);

		GridBagConstraints gbcPatientParams = new GridBagConstraints();
		gbcPatientParams.fill = GridBagConstraints.VERTICAL;
		gbcPatientParams.anchor = GridBagConstraints.WEST;

		gbcPatientParams.gridy = 0;
		gbcPatientParams.gridx = 1;
		gbcPatientParams.gridwidth = 3;
		patientParamsPanel.add(getPanelChoosePatient(), gbcPatientParams);

		GridBagConstraints gbcService = new GridBagConstraints();
		gbcService.fill = GridBagConstraints.VERTICAL;
		gbcService.anchor = GridBagConstraints.WEST;

		gbcService.gridy = 1;
		gbcService.gridx = 0;
		patientParamsPanel.add(getServicePanel(), gbcService);

		GridBagConstraints gbcDuration = new GridBagConstraints();
		gbcDuration.fill = GridBagConstraints.VERTICAL;
		gbcDuration.anchor = GridBagConstraints.WEST;
		gbcDuration.gridy = 2;
		gbcDuration.gridx = 0;
		gbcDuration.gridwidth = 2;
		patientParamsPanel.add(getDurationPanel(), gbcDuration);

		GridBagConstraints date = new GridBagConstraints();
		date.fill = GridBagConstraints.VERTICAL;
		date.anchor = GridBagConstraints.WEST;

		date.gridy = 3;
		date.gridx = 0;
		date.gridwidth = 2;
		patientParamsPanel.add(getVisitDateChooser(), date);

		return patientParamsPanel;
	}

	private JPanel getWardPanel() {
		if (wardPanel == null) {
			wardPanel = new JPanel();
			try {
				wardList = wbm.getWards();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			wardBox = getWardBox();
		}

		wardPanel.add(wardBox);
		wardPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.common.ward.txt")));

		return wardPanel;
	}

	private JComboBox<Ward> getWardBox() {
		JComboBox<Ward> newWardBox = new JComboBox<Ward>();
		newWardBox.addItem(null);
		for (Ward ward : wardList) {
			if (patientSelected == null) {
				if (ward.getBeds() > 0) {
					newWardBox.addItem(ward);
				}
			} else {
				// if patient is a male you don't see pregnancy case
				if (("" + patientSelected.getSex()).equalsIgnoreCase("F") && !ward.isFemale()) {
					continue;
				} else if (("" + patientSelected.getSex()).equalsIgnoreCase("M") && !ward.isMale()) {
					continue;
				} else {
					if (ward.getBeds() > 0) {
						newWardBox.addItem(ward);
					}
				}
			}
			if (this.ward != null) {
				if (this.ward.getCode().equalsIgnoreCase(ward.getCode())) {
					newWardBox.setSelectedItem(ward);
				}
			}
		}
		return newWardBox;
	}

	private JPanel getServicePanel() {
		if (servicePanel == null) {
			servicePanel = new JPanel();

			JLabel servicelabel = new JLabel(MessageBundle.getMessage("angal.visit.service"));

			serviceField = new JTextField(10);
			serviceField.setEditable(true);
			serviceField.setFocusable(true);

			servicePanel.add(servicelabel);
			servicePanel.add(serviceField);

		}
		return servicePanel;
	}

	private JPanel getDurationPanel() {
		if (durationPanel == null) {
			durationPanel = new JPanel();

			JLabel durationlabel = new JLabel(MessageBundle.getMessage("angal.visit.durationinminutes"));

			JTextField durationField = new JTextField(10);
			durationField.setEditable(true);
			durationField.setFocusable(true);

			durationPanel.add(durationlabel);
			durationPanel.add(getSpinnerQty());

		}
		return durationPanel;
	}

	private JSpinner getSpinnerQty() {
		Integer minQty = 0;
		Integer stepQty = 1;
		Integer maxQty = null;
		jSpinnerDur = new JSpinner(new SpinnerNumberModel(DEFAULT_DURATION, minQty, maxQty, stepQty));
		jSpinnerDur.setFont(new Font("Dialog", Font.BOLD, 14));
		jSpinnerDur.setAlignmentX(Component.LEFT_ALIGNMENT);
		jSpinnerDur.setPreferredSize(new Dimension(PREFERRED_SPINNER_WIDTH, ONE_LINE_COMPONENTS_HEIGHT));
		jSpinnerDur.setMaximumSize(new Dimension(Short.MAX_VALUE, ONE_LINE_COMPONENTS_HEIGHT));
		return jSpinnerDur;
	}

	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new JPanel();
			buttonsPanel.add(getButtonOK());
			buttonsPanel.add(getButtonCancel());
		}
		return buttonsPanel;
	}

	private JButton getButtonCancel() {
		if (buttonCancel == null) {
			buttonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			buttonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			buttonCancel.addActionListener(actionEvent -> dispose());
		}
		return buttonCancel;
	}

	private JButton getButtonOK() {
		if (buttonOK == null) {
			buttonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			buttonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			buttonOK.addActionListener(actionEvent -> {

				LocalDateTime date = visitDateChooser.getLocalDateTime();
				if (date.isBefore(TimeTools.getDateToday0())) {
					MessageDialog.error(InsertVisit.this, "angal.visit.avisitcannotbescheduledforadatethatispast.msg");
					return;
				}
				Ward ward = getSelectedWard();
				if (ward == null) {
					MessageDialog.error(InsertVisit.this, "angal.visit.pleasechooseaward.msg");
					return;
				}

				Visit thisVisit = new Visit();
				thisVisit.setPatient(patientSelected);
				thisVisit.setWard(ward);
				thisVisit.setDate(date);
				thisVisit.setDuration((Integer) jSpinnerDur.getValue());
				thisVisit.setService(serviceField.getText());
				try {
					if (insert) {
						visit = visitManager.newVisit(thisVisit);
					} else {
						visitManager.validateVisit(thisVisit);
						visit = thisVisit;
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e, InsertVisit.this);
					return;
				}
				dispose();
			});
		}
		return buttonOK;
	}

	public JPanel getVisitDateChooser() {
		if (dateViPanel == null) {
			dateViPanel = new JPanel();
			dateViPanel.add(new JLabel(MessageBundle.getMessage("angal.common.date.txt")));
			dateViPanel.add(getVisitDateField());
			dateViPanel.add(getAdmButton());
		}
		return dateViPanel;
	}

	private JDateChooser getVisitDateField() {
		visitDateChooser = new LocalDateSupportingJDateChooser();
		visitDateChooser.setLocale(new Locale(GeneralData.LANGUAGE));
		visitDateChooser.setDateFormatString(DATE_TIME_FORMAT);
		if (visitDate != null) {
			visitDateChooser.setDate(visitDate);
		}
		return visitDateChooser;
	}

	private JButton getAdmButton() {
		if (admButton == null) {
			admButton = new JButton("");
			admButton.setIcon(new ImageIcon("./rsc/icons/clock_button.png"));
			admButton.addActionListener(actionEvent -> {

				JDateAndTimeChooserDialog schedDate = new JDateAndTimeChooserDialog(InsertVisit.this, visitDateChooser.getDate());
				schedDate.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				schedDate.setVisible(true);

				Date date = schedDate.getDate();

				if (date != null) {
					visitDateChooser.setDate(date);
				}
			});
		}
		return admButton;
	}

	private JButton getJButtonPickPatient() {
		if (jButtonPickPatient == null) {
			jButtonPickPatient = new JButton(MessageBundle.getMessage("angal.visit.findpatient.btn"));
			jButtonPickPatient.setMnemonic(MessageBundle.getMnemonic("angal.visit.findpatient.btn.key"));
			jButtonPickPatient.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
			jButtonPickPatient.addActionListener(actionEvent -> {

				SelectPatient sp = new SelectPatient(InsertVisit.this, patientSelected);
				sp.addSelectionListener(InsertVisit.this);
				sp.pack();
				sp.setVisible(true);

			});
		}
		return jButtonPickPatient;
	}

	private JPanel getPanelChoosePatient() {
		JPanel choosePatientPanel = new JPanel();
		choosePatientPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		choosePatientPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.visit.pleaseselectapatient.title")));

		patientTextField = new JTextField(14);
		patientTextField.setEditable(false);
		choosePatientPanel.add(patientTextField);
		choosePatientPanel.add(getJButtonPickPatient());
		
		if (patientSelected != null) {
			patientSelected(patientSelected);
			jButtonPickPatient.setEnabled(false);
		}

		return choosePatientPanel;
	}

	@Override
	public void patientSelected(Patient patient) {
		patientSelected = patient;
		patientTextField.setText(patientSelected != null ? patientSelected.getFirstName() + " " + patientSelected.getSecondName() : ""); //$NON-NLS-2$
		jButtonPickPatient.setText(MessageBundle.getMessage("angal.visit.changepatient"));
		wardBox.setModel(getWardBox().getModel());
		pack();
	}

	public LocalDateTime getVisitDate() {
		return visitDate;
	}

	public Ward getSelectedWard() {
		Object ward = wardBox.getSelectedItem();
		if (ward instanceof Ward) {
			return (Ward) wardBox.getSelectedItem();
		} else {
			return null;
		}
	}

	public String getServ() {
		return serviceField.getText();
	}

	public String getdur() {
		Object o = jSpinnerDur.getValue();
		Number n = (Number) o;
		int i = n.intValue();
		return String.valueOf(i);
	}

	public Patient getPatient() {
		return patientSelected;
	}

	public Visit getVisit() {
		return visit;
	}

	public void setVisit(Visit vsRow) {
		this.visit = vsRow;
	}

}
