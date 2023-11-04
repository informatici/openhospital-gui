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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
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
import java.util.List;

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

import org.isf.generaldata.MessageBundle;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.hospital.model.Hospital;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.gui.SelectPatient.SelectionListener;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeVisitChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.TimeTools;
import org.isf.visits.manager.VisitManager;
import org.isf.visits.model.Visit;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

/**
 * @author Mwithi
 */
public class InsertVisit extends JDialog implements SelectionListener {

	private static final long serialVersionUID = 1L;

	/*
	 * Constants
	 */
	private static final int DEFAULT_DURATION = 30;
	private static final int PREFERRED_SPINNER_WIDTH = 100;
	private static final int ONE_LINE_COMPONENTS_HEIGHT = 30;
	private static final int SPINNER_MIN_QTY = 0;
	private static final int SPINNER_STEP_QTY = 1;
	
	/*
	 * Attributes
	 */
	private GoodDateTimeVisitChooser visitDateChooser;
	private JPanel buttonsPanel;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JPanel servicePanel;
	private JTextField serviceField;
	private JPanel durationPanel;
	private JPanel dateViPanel;
	private JButton jButtonPickPatient;
	private JTextField patientTextField;
	private Patient patientSelected;
	private JSpinner jSpinnerDur;

	/*
	 * Return Value
	 */
	private LocalDateTime visitDate;
	private JPanel wardPanel;
	private JComboBox<Ward> wardBox;
	private Ward ward;
	private Visit visit;
	private boolean insert;
	
	/*
	 * Managers
	 */
	private WardBrowserManager wardBrowserManager = Context.getApplicationContext().getBean(WardBrowserManager.class);
	private HospitalBrowsingManager hospitalBrowsingManager = Context.getApplicationContext().getBean(HospitalBrowsingManager.class);
	private VisitManager visitManager = Context.getApplicationContext().getBean(VisitManager.class);
	private List<Ward> wardList = new ArrayList<>();
	
	public InsertVisit(JFrame owner, Ward ward, Patient patient, boolean insert) {
		super(owner, true);
		this.patientSelected = patient;
		this.ward = ward;
		this.insert = insert;
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

		setTitle(MessageBundle.getMessage("angal.visit.addvisit.title"));
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

		GridBagConstraints gbcWard = new GridBagConstraints();
		gbcWard.fill = GridBagConstraints.VERTICAL;
		gbcWard.anchor = GridBagConstraints.WEST;

		gbcWard.gridy = 0;
		gbcWard.gridx = 0;
		patientParamsPanel.add(getWardPanel(), gbcWard);

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
				wardList = wardBrowserManager.getWards();
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
		JComboBox<Ward> newWardBox = new JComboBox<>();
		newWardBox.addItem(null);
		for (Ward aWard : wardList) {
			newWardBox.addItem(aWard);
			if (this.ward != null && this.ward.getCode().equalsIgnoreCase(aWard.getCode())) {
				newWardBox.setSelectedItem(aWard);
			}
		}
		newWardBox.addActionListener(actionEvent -> {
			ward = getSelectedWard();
			if (ward != null) {
				jSpinnerDur.setModel(new SpinnerNumberModel(getDuration(), SPINNER_MIN_QTY, null, SPINNER_STEP_QTY));
				dateViPanel.remove(visitDateChooser);
				visitDateChooser = new GoodDateTimeVisitChooser(visitDate, getDuration());
				dateViPanel.add(visitDateChooser);
			}
		});
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
		jSpinnerDur = new JSpinner(new SpinnerNumberModel(getDuration(), SPINNER_STEP_QTY, null, SPINNER_STEP_QTY));
		jSpinnerDur.setFont(new Font("Dialog", Font.BOLD, 14));
		jSpinnerDur.setAlignmentX(Component.LEFT_ALIGNMENT);
		jSpinnerDur.setPreferredSize(new Dimension(PREFERRED_SPINNER_WIDTH, ONE_LINE_COMPONENTS_HEIGHT));
		jSpinnerDur.setMaximumSize(new Dimension(Short.MAX_VALUE, ONE_LINE_COMPONENTS_HEIGHT));
		return jSpinnerDur;
	}

	private int getDuration() {
		if (ward != null) {
			for (Ward aWard : wardList) {
				if (aWard.getDescription().equals(ward.getDescription())) {
					return aWard.getVisitDuration();
				}
			}
		}
		Hospital hospital;
		try {
			hospital = hospitalBrowsingManager.getHospital();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
			return DEFAULT_DURATION;
		}
		return hospital.getVisitDuration();
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
				if (date == null) {
					MessageDialog.error(this, "angal.visit.pleasechooseavaliddateandtime.msg");
					return;
				}
				if (date.isBefore(TimeTools.getDateToday0())) {
					MessageDialog.error(this, "angal.visit.avisitcannotbescheduledforadatethatispast.msg");
					return;
				}
				Ward selectedWard = getSelectedWard();
				if (selectedWard == null) {
					MessageDialog.error(this, "angal.visit.pleasechooseaward.msg");
					return;
				}

				Visit thisVisit = new Visit();
				thisVisit.setPatient(patientSelected);
				thisVisit.setWard(selectedWard);
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
					OHServiceExceptionUtil.showMessages(e, this);
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
			visitDateChooser = new GoodDateTimeVisitChooser(visitDate, getDuration());
			dateViPanel.add(visitDateChooser);
		}
		return dateViPanel;
	}

	private JButton getJButtonPickPatient() {
		if (jButtonPickPatient == null) {
			jButtonPickPatient = new JButton(MessageBundle.getMessage("angal.visit.findpatient.btn"));
			jButtonPickPatient.setMnemonic(MessageBundle.getMnemonic("angal.visit.findpatient.btn.key"));
			jButtonPickPatient.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));
			jButtonPickPatient.addActionListener(actionEvent -> {

				SelectPatient sp = new SelectPatient(this, patientSelected);
				sp.addSelectionListener(this);
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
		patientTextField.setText(patientSelected != null ? patientSelected.getFirstName() + ' ' + patientSelected.getSecondName() : ""); //$NON-NLS-1$ //$NON-NLS-2$
		jButtonPickPatient.setText(MessageBundle.getMessage("angal.visit.changepatient")); //$NON-NLS-1$
		wardBox.setModel(getWardBox().getModel());
		pack();
	}

	public Ward getSelectedWard() {
		Object selectedItem = wardBox.getSelectedItem();
		if (selectedItem instanceof Ward) {
			return (Ward) wardBox.getSelectedItem();
		}
		return null;
	}

	public Visit getVisit() {
		return visit;
	}

}
