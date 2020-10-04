/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.gui.SelectPatient;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.JDateAndTimeChooserDialog;
import org.isf.visits.manager.VisitManager;
import org.isf.visits.model.Visit;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

import com.toedter.calendar.JDateChooser;

/**
 * @author Mwithi
 * 
 */
public class InsertVisit extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * Constants
	 */
	private final String dateTimeFormat = "dd/MM/yy HH:mm:ss";

	/*
	 * Attributes
	 */
	private JDateChooser visitDateChooser;
	private JPanel buttonsPanel;
	private JButton buttonOK;
	private JButton buttonCancel;

	/*
	 * Return Value
	 */
	private Date visitDate = null;
	private JPanel wardPanel;
	private JComboBox wardBox;
	private Ward ward;
	private Patient patient;
	/*
	 * Managers
	 */
	private WardBrowserManager wbm = Context.getApplicationContext().getBean(WardBrowserManager.class);

	public InsertVisit(JFrame owner, Ward ward, Patient patient) {
		super(owner, true);
		this.patient = patient;
		this.ward = ward;
		initComponents();
	}

	public InsertVisit(JDialog owner, Date date) {
		super(owner, true);
		this.visitDate = date;
		initComponents();
	}

	public InsertVisit(JFrame owner, Date date, Ward ward, Patient patient) {
		super(owner, true);
		this.patient = patient;
		this.visitDate = date;
		this.ward = ward;
		initComponents();
	}

	private void initComponents() {
		setSize(new Dimension(500, 250));
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(getpVisitInf());

		getContentPane().add(getButtonsPanel(), BorderLayout.SOUTH);

		setLocationRelativeTo(null);

	}

	private JPanel getpVisitInf() {

		JPanel patientParamsPanel = new JPanel(new SpringLayout());

		GridBagLayout gbl_jPanelData = new GridBagLayout();
		gbl_jPanelData.columnWidths = new int[] { 20, 20, 20, 0, 0, 00 };
		gbl_jPanelData.rowHeights = new int[] { 20, 20, 20, 0, 0, 0, 0, 0 };
		gbl_jPanelData.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_jPanelData.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		patientParamsPanel.setLayout(gbl_jPanelData);

		GridBagConstraints gbc_ward = new GridBagConstraints();
		gbc_ward.fill = GridBagConstraints.VERTICAL;
		gbc_ward.anchor = GridBagConstraints.WEST;

		gbc_ward.gridy = 0;
		gbc_ward.gridx = 0;
		patientParamsPanel.add(getWardPanel(), gbc_ward);

		GridBagConstraints gbc_Pat = new GridBagConstraints();
		gbc_Pat.fill = GridBagConstraints.VERTICAL;
		gbc_Pat.anchor = GridBagConstraints.WEST;

		gbc_Pat.gridy = 0;
		gbc_Pat.gridx = 1;
		gbc_Pat.gridwidth = 3;
		patientParamsPanel.add(getPanelChoosePatient(), gbc_Pat);

		GridBagConstraints gbc_Service = new GridBagConstraints();
		gbc_Service.fill = GridBagConstraints.VERTICAL;
		gbc_Service.anchor = GridBagConstraints.WEST;

		gbc_Service.gridy = 1;
		gbc_Service.gridx = 0;
		patientParamsPanel.add(getService(), gbc_Service);

		GridBagConstraints gbc_Duration = new GridBagConstraints();
		gbc_Duration.fill = GridBagConstraints.VERTICAL;
		gbc_Duration.anchor = GridBagConstraints.WEST;
		gbc_Duration.gridy = 2;
		gbc_Duration.gridx = 0;
		gbc_Duration.gridwidth = 2;
		patientParamsPanel.add(getDuration(), gbc_Duration);

		GridBagConstraints gbc_date = new GridBagConstraints();
		gbc_date.fill = GridBagConstraints.VERTICAL;
		gbc_date.anchor = GridBagConstraints.WEST;

		gbc_date.gridy = 3;
		gbc_date.gridx = 0;
		gbc_date.gridwidth = 2;
		patientParamsPanel.add(getVisitDateChooser(), gbc_date);

		return patientParamsPanel;
	}

	private Ward saveWard = null;

	private ArrayList<Ward> wardList = new ArrayList<Ward>();

	private JPanel ServicePanel;

	private JLabel Servicelabel;

	private JTextField ServiceField;

	private JPanel DurationPanel;

	private JLabel Durationlabel;

	private JTextField DurationField;

	private JPanel dateViPanel;

	private JLabel dateAdm;

	private JButton admButton;

	private JButton jAffiliatePersonJButtonAdd;

	private JButton jAffiliatePersonJButtonSupp;

	private JTextField jAffiliatePersonJTextField;

	private Patient patientParent;

	private Visit visit;

	private JSpinner jSpinnerDur;

	private JPanel getWardPanel() {
		if (wardPanel == null) {
			wardPanel = new JPanel();
			wardBox = new JComboBox();
			wardBox.addItem("");
			try {
				wardList = wbm.getWards();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			for (Ward ward : wardList) {

				wardBox.addItem(ward);
				if (this.ward != null) {
					if (this.ward.getCode().equalsIgnoreCase(ward.getCode())) {
						wardBox.setSelectedItem(ward);
					}
				}
			}
		}

		wardPanel.add(wardBox);
		wardPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.admission.ward")));

		return wardPanel;
	}

	private JPanel getService() {
		if (ServicePanel == null) {

			ServicePanel = new JPanel();

			Servicelabel = new JLabel();
			Servicelabel.setText("Service");

			ServiceField = new JTextField(10);
			ServiceField.setEditable(true);
			ServiceField.setFocusable(true);

			ServicePanel.add(Servicelabel);
			ServicePanel.add(ServiceField);

		}
		return ServicePanel;
	}

	private JPanel getDuration() {

		if (DurationPanel == null) {

			DurationPanel = new JPanel();

			Durationlabel = new JLabel();
			Durationlabel.setText("Duration (Min)");

			DurationField = new JTextField(10);
			DurationField.setEditable(true);
			DurationField.setFocusable(true);

			DurationPanel.add(Durationlabel);
			DurationPanel.add(getSpinnerQty());

		}
		return DurationPanel;
	}

	private final int preferredSpinnerWidth = 100;
	private final int oneLineComponentsHeight = 30;

	private JSpinner getSpinnerQty() {
		Double startQty = 0.;
		Double minQty = 0.;
		Double stepQty = 1.;
		Double maxQty = null;
		jSpinnerDur = new JSpinner(new SpinnerNumberModel(startQty, minQty, maxQty, stepQty));
		jSpinnerDur.setFont(new Font("Dialog", Font.BOLD, 14)); //$NON-NLS-1$
		jSpinnerDur.setAlignmentX(Component.LEFT_ALIGNMENT);
		jSpinnerDur.setPreferredSize(new Dimension(preferredSpinnerWidth, oneLineComponentsHeight));
		jSpinnerDur.setMaximumSize(new Dimension(Short.MAX_VALUE, oneLineComponentsHeight));
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
			buttonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel"));
			buttonCancel.setMnemonic(KeyEvent.VK_N);
			buttonCancel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
		}
		return buttonCancel;
	}

	private JButton getButtonOK() {
		if (buttonOK == null) {
			buttonOK = new JButton(MessageBundle.getMessage("angal.common.ok"));
			buttonOK.setMnemonic(KeyEvent.VK_O);
			buttonOK.addActionListener(new ActionListener() {
				private VisitManager visitManager = Context.getApplicationContext().getBean(VisitManager.class);

				public void actionPerformed(ActionEvent arg0) {
					if (visitDateChooser.getDate() == null) {
						JOptionPane.showMessageDialog(InsertVisit.this, MessageBundle.getMessage("angal.visit.date"), //$NON-NLS-1$
								MessageBundle.getMessage("angal.therapy.warning"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
						return;
					}
					GregorianCalendar date = new GregorianCalendar();
					date.setTime(visitDateChooser.getDate());
					int visitID = 0;
					String note = null;
					Object o = jSpinnerDur.getValue();
					Number n = (Number) o;
					int i = n.intValue();
					String duration = String.valueOf(i);
					String service = ServiceField.getText();
					Object ward = wardBox.getSelectedItem();
					if (ward instanceof Ward) {
						saveWard = getWard();

					} else {
						JOptionPane.showMessageDialog(InsertVisit.this, MessageBundle.getMessage("angal.visit.ward"), //$NON-NLS-1$
								MessageBundle.getMessage("angal.therapy.warning"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
						return;
					}

					boolean sms = false;
					if (patient == null) {
						JOptionPane.showMessageDialog(InsertVisit.this,
								MessageBundle.getMessage("angal.medicalstockwardedit.pleaseselectapatient"), //$NON-NLS-1$
								MessageBundle.getMessage("angal.therapy.warning"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
						return;
					}
					try {

						visit = visitManager.newVisit(visitID, date, patient, note, sms, saveWard, duration, service);
						visitID = visit.getVisitID();
					} catch (OHServiceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (visitID > 0) {
						visit.setVisitID(visitID);
					}
					dispose();
				}
			});
		}
		return buttonOK;
	}

	public JPanel getVisitDateChooser() {

		if (dateViPanel == null) {

			dateViPanel = new JPanel();

			dateAdm = new JLabel();
			dateAdm.setText(MessageBundle.getMessage("Date"));

			dateViPanel.add(dateAdm);
			dateViPanel.add(getVisitDateField());
			dateViPanel.add(getAdmButton());
		}
		return dateViPanel;
	}

	private JDateChooser getVisitDateField() {
		visitDateChooser = new JDateChooser();
		visitDateChooser.setLocale(new Locale(GeneralData.LANGUAGE));
		visitDateChooser.setDateFormatString(dateTimeFormat); // $NON-NLS-1$
		if (visitDate != null)
			visitDateChooser.setDate(visitDate);

		return visitDateChooser;
	}

	private JButton getAdmButton() {

		if (admButton == null) {
			admButton = new JButton(""); //$NON-NLS-1$
			admButton.setIcon(new ImageIcon("./rsc/icons/clock_button.png")); //$NON-NLS-1$
			admButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					JDateAndTimeChooserDialog schedDate = new JDateAndTimeChooserDialog(InsertVisit.this,
							visitDateChooser.getDate());
					schedDate.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					schedDate.setVisible(true);

					Date date = schedDate.getDate();

					if (date != null) {

						visitDateChooser.setDate(date);

					} else {
						return;
					}
				}
			});
		}
		return admButton;
	}

	private JPanel getPanelChoosePatient() {
		JPanel choosePatientPanel = new JPanel();
		// panelSupRange.add(priceListLabelPanel);
		choosePatientPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		choosePatientPanel.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.medicalstockwardedit.pleaseselectapatient")));

		jAffiliatePersonJButtonAdd = new JButton();
		jAffiliatePersonJButtonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		jAffiliatePersonJButtonAdd.setIcon(new ImageIcon("rsc/icons/pick_patient_button.png"));

		jAffiliatePersonJButtonSupp = new JButton();
		jAffiliatePersonJButtonSupp.setIcon(new ImageIcon("rsc/icons/remove_patient_button.png"));

		jAffiliatePersonJTextField = new JTextField(14);
		jAffiliatePersonJTextField.setEnabled(false);
		choosePatientPanel.add(jAffiliatePersonJTextField);
		choosePatientPanel.add(jAffiliatePersonJButtonAdd);
		choosePatientPanel.add(jAffiliatePersonJButtonSupp);
		
		if (patient == null) {
			jAffiliatePersonJButtonAdd.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					SelectPatient selectPatient = new SelectPatient(InsertVisit.this, false, true);
					selectPatient.addSelectionListener(InsertVisit.this);
					selectPatient.setVisible(true);
					patient = selectPatient.getPatient();
					// System.out.println("Patient...........+++++++++++++.............."+pat.getFirstName());
					try {
						patientSelected(patient);
					} catch (OHServiceException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}
			});
		} else {
			jAffiliatePersonJTextField.setText(patient.getName());
			choosePatientPanel.setEnabled(false);
		}
		return choosePatientPanel;
	}

	public void patientSelected(Patient patient) throws OHServiceException {
		patientParent = patient;
		jAffiliatePersonJTextField.setText(
				patientParent != null ? patientParent.getFirstName() + " " + patientParent.getFirstName() : "");

	}

	public Date getVisitDate() {
		return visitDate;
	}

	public Ward getWard() {
		Object ward = wardBox.getSelectedItem();
		if (ward instanceof Ward) {
			return (Ward) wardBox.getSelectedItem();

		} else {
			return null;
		}
	}

	public String getServ() {
		return ServiceField.getText();
	}

	public String getdur() {
		Object o = jSpinnerDur.getValue();
		Number n = (Number) o;
		int i = n.intValue();
		String qty = String.valueOf(i);
		return qty;
	}

	public Patient getPatient() {
		return patientParent;
	}

	public Visit getVisit() {
		return visit;
	}

	public void setVisit(Visit vsRow) {
		this.visit = vsRow;
	}
}