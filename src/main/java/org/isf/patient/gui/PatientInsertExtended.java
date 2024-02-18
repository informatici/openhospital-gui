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
package org.isf.patient.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.EventListenerList;

import org.isf.agetype.manager.AgeTypeBrowserManager;
import org.isf.agetype.model.AgeType;
import org.isf.anamnesis.gui.PatientHistoryEdit;
import org.isf.anamnesis.manager.PatientHistoryManager;
import org.isf.anamnesis.model.PatientHistory;
import org.isf.anamnesis.model.PatientPatientHistory;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.generaldata.SmsParameters;
import org.isf.menu.manager.Context;
import org.isf.patconsensus.manager.PatientConsensusBrowserManager;
import org.isf.patconsensus.model.PatientConsensus;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.patient.model.PatientProfilePhoto;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.image.ImageUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.video.gui.PatientPhotoPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class PatientInsertExtended extends JDialog {

	private static final long serialVersionUID = -827831581202765055L;

	private static final Logger LOGGER = LoggerFactory.getLogger(PatientInsertExtended.class);

	private PatientHistoryManager patientHistoryManager = Context.getApplicationContext().getBean(PatientHistoryManager.class);
	private PatientConsensusBrowserManager patientConsensusManager = Context.getApplicationContext().getBean(PatientConsensusBrowserManager.class);

	private EventListenerList patientListeners = new EventListenerList();

	private PatientHistory patientHistory;

	public interface PatientListener extends EventListener {

		void patientUpdated(AWTEvent e);

		void patientInserted(AWTEvent e);
	}

	public void addPatientListener(PatientListener l) {
		patientListeners.add(PatientListener.class, l);
	}

	public void removePatientListener(PatientListener listener) {
		patientListeners.remove(PatientListener.class, listener);
	}

	private void firePatientInserted(Patient aPatient) {
		AWTEvent event = new AWTEvent(aPatient, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = -6853617821516727564L;

		};

		EventListener[] listeners = patientListeners.getListeners(PatientListener.class);
		for (EventListener listener : listeners) {
			((PatientListener) listener).patientInserted(event);
		}
	}

	private void firePatientUpdated(Patient aPatient) {
		AWTEvent event = new AWTEvent(aPatient, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 7777830932867901993L;

		};

		EventListener[] listeners = patientListeners.getListeners(PatientListener.class);
		for (EventListener listener : listeners) {
			((PatientListener) listener).patientUpdated(event);
		}
	}

	// COMPONENTS: Main
	private JPanel jMainPanel;
	private boolean insert;
	private boolean justSave;
	private Patient patient;

	private PatientConsensus consensus;

	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private AgeTypeBrowserManager ageTypeBrowserManager = Context.getApplicationContext().getBean(AgeTypeBrowserManager.class);

	// COMPONENTS: Data
	private JPanel jDataPanel;

	// COMPONENTS: Anagraph
	private JPanel jDataContainPanel;
	private JPanel jAnagraphPanel;

	// First Name Components:
	private JPanel jFirstName;
	private JPanel jFirstNameLabelPanel;
	private JPanel jFirstNameFieldPanel;
	private JLabel jFirstNameLabel;
	private JTextField jFirstNameTextField;

	// Second Name Components:
	private JPanel jSecondName;
	private JPanel jSecondNameLabelPanel;
	private JPanel jSecondNameFieldPanel;
	private JLabel jSecondNameLabel;
	private JTextField jSecondNameTextField;

	// AgeTypeSelection:
	private JPanel jAgeType;
	private JPanel jAgeTypeButtonGroup;
	private JPanel jAgeTypeSelection;
	private JPanel jAgeTypeBirthDatePanel;
	private JRadioButton jAgeTypeAge;
	private JRadioButton jAgeTypeBirthDate;
	private JRadioButton jAgeTypeDescription;

	// Age Components:
	private JPanel jAge;
	private JTextField jAgeYears;
	private JTextField jAgeMonths;
	private JTextField jAgeDays;
	private int years;
	private int months;
	private int days;

	// BirthDate Components:
	private JPanel jBirthDate;
	private JPanel jBirthDateLabelPanel;
	private JLabel jBirthDateLabel;
	private JPanel jBirthDateGroupPanel;
	private LocalDate birthDate;
	private JLabel jBirthDateAge;

	// AgeDescription Components:
	private int ageType;
	private int ageTypeMonths;
	private JPanel jAgeDesc;
	private JPanel jAgeDescPanel;
	private JPanel jAgeMonthsPanel;
	private JComboBox jAgeDescComboBox;
	private JComboBox jAgeMonthsComboBox;
	private JLabel jAgeMonthsLabel;

	// Sex Components:
	private JPanel jSexPanel;
	private JPanel jSexLabelPanel;
	private JRadioButton radiof;
	private JRadioButton radiom;

	// Address Components:
	private JPanel jAddress;
	private JPanel jAddressLabelPanel;
	private JPanel jAddressFieldPanel;
	private JTextField jAddressTextField;

	// Address Components:
	private JPanel jTaxCodePanel;
	private JPanel jTaxCodeLabelPanel;
	private JPanel jTaxCodeFieldPanel;
	private JTextField jTaxCodeTextField;

	// City Components:
	private JPanel jCity;
	private JPanel jCityLabelPanel;
	private JPanel jCityFieldPanel;
	private JTextField jCityTextField;

	// NextKin Components:
	private JPanel jNextKin;
	private JPanel jNextKinLabelPanel;
	private JPanel jNextKinFieldPanel;
	private JTextField jNextKinTextField;

	// Telephone Components:
	private JPanel jTelephone;
	private JPanel jTelephoneLabelPanel;
	private JPanel jTelephoneFieldPanel;
	private JTextField jTelephoneTextField;

	// COMPONENTS: Extension
	private JPanel jExtensionContent;

	// BloodType Components:
	private JPanel jBloodTypePanel;
	private JComboBox jBloodTypeComboBox;

	// Father Components:
	private JPanel jFatherPanelOptions;
	private JPanel jFatherPanel;
	private JTextField jFatherNameTextField;
	private JPanel jFatherAlivePanel;
	private JRadioButton jFatherDead;
	private JRadioButton jFatherAlive;
	private JRadioButton jFatherUnknown;

	// Mother Components:
	private JPanel jMotherOptions;
	private JPanel jMotherPanel;
	private JTextField jMotherNameTextField;
	private JPanel jMotherAlivePanel;
	private JRadioButton jMotherDead;
	private JRadioButton jMotherAlive;
	private JRadioButton jMotherUnknown;

	// Profession Components:
	private JPanel jProfessionPanel;
	private JComboBox jProfessionComboBox;

	// ParentTogether Components:
	private JPanel jParentPanel;
	private JPanel jParentNoPanel;
	private JRadioButton jParentYes;
	private JRadioButton jParentNo;
	private JRadioButton jParentUnknown;

	// HasInsurance Components:
	private JPanel jInsurancePanel;
	private JPanel jInsuranceNoPanel;
	private JRadioButton jInsuranceYes;
	private JRadioButton jInsuranceNo;
	private JRadioButton jInsuranceUnknown;

	// MaritalStatus Components:
	private JPanel jMaritalPanel;
	private JComboBox jMaritalStatusComboBox;

	// COMPONENTS: Note
	private JPanel jRightPanel;
	private JScrollPane jNoteScrollPane;
	private JTextArea jNoteTextArea;

	// COMPONENTS: Buttons
	private JPanel jButtonPanel;
	private JButton jOkButton;
	private JButton jAnamnesisButton;
	private JButton jCancelButton;

	private JLabel labelRequiredFields;

	private PatientPhotoPanel photoPanel;

	/**
	 * This method initializes
	 *
	 * @param owner
	 */
	public PatientInsertExtended(JFrame owner, Patient old, boolean inserting) {
		super(owner, true);
		patient = old;
		insert = inserting;

		initialize();
	}

	public PatientInsertExtended(JDialog owner, Patient old, boolean inserting) {
		super(owner, true);
		patient = old;
		insert = inserting;

		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {

		this.setContentPane(getJContainPanel());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.patient.newpatient.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.patient.editpatient.title"));
		}
		this.setSize(new Dimension(604, 445));
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jContainPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContainPanel() {
		if (jMainPanel == null) {
			jMainPanel = new JPanel();
			jMainPanel.setLayout(new BorderLayout());
			jMainPanel.add(getJDataPanel(), BorderLayout.CENTER);
			jMainPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
		}
		return jMainPanel;
	}

	/**
	 * This method initializes jMainPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJDataPanel() {
		if (jDataPanel == null) {
			jDataPanel = new JPanel();
			jDataPanel.setLayout(new BoxLayout(jDataPanel, BoxLayout.X_AXIS));
			jDataPanel.add(getJDataContainPanel(), null);
			jDataPanel.add(getJRightPanel(), null);
			pack();
		}
		return jDataPanel;
	}

	/**
	 * This method initializes jButtonPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			jButtonPanel.add(getJOkButton(), null);
			jButtonPanel.add(getJAnamnesisButton(), null);
			jButtonPanel.add(getJCancelButton(), null);
		}
		return jButtonPanel;
	}

	private JButton getJAnamnesisButton() {
		if (jAnamnesisButton == null) {
			jAnamnesisButton = new JButton(MessageBundle.getMessage("angal.anamnesis.open.anamnesis.btn"));
			jAnamnesisButton.setMnemonic(MessageBundle.getMnemonic("angal.opd.anamnesis.btn.key"));
			jAnamnesisButton.addActionListener(actionEvent -> {
				patientHistory = new PatientHistory();
				if (patient.getCode() != null) {
					patientHistory = Optional.ofNullable(this.patientHistoryManager.getByPatientId(patient.getCode())).orElse(patientHistory);
				}
				PatientPatientHistory pph = new PatientPatientHistory(patientHistory, patient);
				PatientHistoryEdit dialog = new PatientHistoryEdit(this, pph, false);
				dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.setModal(insert);
				dialog.setVisible(true);
			});

		}
		return jAnamnesisButton;
	}

	/**
	 * This method initializes jOkButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJOkButton() {
		if (jOkButton == null) {
			jOkButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			jOkButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			jOkButton.addActionListener(actionEvent -> {
				boolean ok = true;
				String firstName = jFirstNameTextField.getText().trim();
				String secondName = jSecondNameTextField.getText().trim();

				if (firstName.isEmpty()) {
					MessageDialog.error(this, "angal.patient.insertfirstname.msg");
					return;
				}
				if (secondName.isEmpty()) {
					MessageDialog.error(this, "angal.patient.insertsecondname.msg");
					return;
				}
				if (!checkAge()) {
					MessageDialog.error(this, "angal.patient.insertage");
					return;
				}
				if (insert) {
					String name = firstName + ' ' + secondName;
					try {
						if (patientBrowserManager.isNamePresent(name)) {
							switch (MessageDialog.yesNo(null, "angal.patient.thepatientisalreadypresent.msg")) {
							case JOptionPane.OK_OPTION:
								ok = true;
								break;
							case JOptionPane.NO_OPTION:
								ok = false;
								break;
							}
						}
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}

					if (!consensus.isConsensusFlag()) {
						MessageDialog.error(null, "angal.patient.consensus.consensus.mandatory.msg");
						ok = false;
					}

					if (ok) {
						patient.setFirstName(firstName);
						patient.setSecondName(secondName);

						if (radiof.isSelected()) {
							patient.setSex('F');
						} else if (radiom.isSelected()) {
							patient.setSex('M');
						} else {
							MessageDialog.info(this, "angal.patient.pleaseselectasex.msg");
							return;
						}
						patient.setTaxCode(jTaxCodeTextField.getText().trim());
						patient.setAddress(jAddressTextField.getText().trim());
						patient.setCity(jCityTextField.getText().trim());
						patient.setNextKin(jNextKinTextField.getText().trim());
						patient.setTelephone(jTelephoneTextField.getText().replace(" ", ""));
						patient.setMotherName(jMotherNameTextField.getText().trim());
						if (jMotherAlive.isSelected()) {
							patient.setMother('A');
						} else {
							if (jMotherDead.isSelected()) {
								patient.setMother('D');
							} else {
								patient.setMother('U');
							}
						}
						patient.setFatherName(jFatherNameTextField.getText().trim());
						if (jFatherAlive.isSelected()) {
							patient.setFather('A');
						} else {
							if (jFatherDead.isSelected()) {
								patient.setFather('D');
							} else {
								patient.setFather('U');
							}
						}
						patient.setBloodType(jBloodTypeComboBox.getSelectedItem().toString());
						patient.setMaritalStatus(patientBrowserManager.getMaritalKey(jMaritalStatusComboBox.getSelectedItem().toString()));
						patient.setProfession(patientBrowserManager.getProfessionKey(jProfessionComboBox.getSelectedItem().toString()));
						if (jInsuranceYes.isSelected()) {
							patient.setHasInsurance('Y');
						} else {
							if (jInsuranceNo.isSelected()) {
								patient.setHasInsurance('N');
							} else {
								patient.setHasInsurance('U');
							}
						}

						if (jParentYes.isSelected()) {
							patient.setParentTogether('Y');
						} else {
							if (jParentNo.isSelected()) {
								patient.setParentTogether('N');
							} else {
								patient.setParentTogether('U');
							}
						}

						patient.setNote(jNoteTextArea.getText().trim());
						try {
							patient = patientBrowserManager.savePatient(patient);
							consensus.setPatient(patient);
							patientConsensusManager.updatePatientConsensus(consensus);
							if (patientHistory != null) {
								patientHistory.setPatientId(patient.getCode());
								patientHistoryManager.saveOrUpdate(patientHistory);
							}
							firePatientInserted(patient);
							if (justSave) {
								insert = false;
								justSave = false;
								this.requestFocus();
							} else {
								dispose();
							}
						} catch (OHServiceException ohServiceException) {
							OHServiceExceptionUtil.showMessages(ohServiceException);
							MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
							LOGGER.error(ohServiceException.getMessage(), ohServiceException);
						}
					}
				} else {// Update
					if (!consensus.isConsensusFlag()) {
						MessageDialog.error(null, "angal.patient.consensus.consensus.mandatory.msg");
						return;
					}
					patient.setFirstName(firstName);
					patient.setSecondName(secondName);
					if (radiof.isSelected()) {
						patient.setSex('F');
					} else if (radiom.isSelected()) {
						patient.setSex('M');
					} else {
						MessageDialog.info(this, "angal.patient.pleaseselectasex.msg");
						return;
					}
					patient.setTaxCode(jTaxCodeTextField.getText().trim());
					patient.setAddress(jAddressTextField.getText().trim());
					patient.setCity(jCityTextField.getText().trim());
					patient.setNextKin(jNextKinTextField.getText().trim());
					patient.setTelephone(jTelephoneTextField.getText().replace(" ", ""));
					patient.setMotherName(jMotherNameTextField.getText().trim());

					if (jMotherAlive.isSelected()) {
						patient.setMother('A');
					} else {
						if (jMotherDead.isSelected()) {
							patient.setMother('D');
						} else {
							patient.setMother('U');
						}
					}
					patient.setFatherName(jFatherNameTextField.getText().trim());
					if (jFatherAlive.isSelected()) {
						patient.setFather('A');
					} else {
						if (jFatherDead.isSelected()) {
							patient.setFather('D');
						} else {
							patient.setFather('U');
						}
					}
					patient.setBloodType(jBloodTypeComboBox.getSelectedItem().toString());
					patient.setMaritalStatus(patientBrowserManager.getMaritalKey(jMaritalStatusComboBox.getSelectedItem().toString()));
					patient.setProfession(patientBrowserManager.getProfessionKey(jProfessionComboBox.getSelectedItem().toString()));

					if (jInsuranceYes.isSelected()) {
						patient.setHasInsurance('Y');
					} else {
						if (jInsuranceNo.isSelected()) {
							patient.setHasInsurance('N');
						} else {
							patient.setHasInsurance('U');
						}
					}

					if (jParentYes.isSelected()) {
						patient.setParentTogether('Y');
					} else {
						if (jParentNo.isSelected()) {
							patient.setParentTogether('N');
						} else {
							patient.setParentTogether('U');
						}
					}
					patient.setNote(jNoteTextArea.getText().trim());
					try {
						patient = patientBrowserManager.savePatient(patient);
						consensus.setPatient(patient);
						patientConsensusManager.updatePatientConsensus(consensus);
						if (patientHistory != null) {
							patientHistory.setPatientId(patient.getCode());
							patientHistoryManager.saveOrUpdate(patientHistory);
						}
						firePatientUpdated(patient);
						dispose();
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
						MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
					}
				}
			});

		}
		return jOkButton;
	}

	/**
	 * This method checks Age insertion
	 *
	 * @return javax.swing.JButton
	 */
	private boolean checkAge() {
		if (jAgeTypeAge.isSelected()) {
			try {
				years = Integer.parseInt(jAgeYears.getText());
				if (years < 0 || years > 120) {
					return false;
				}
				if (years > 100) {
					if (MessageDialog.yesNo(null, "angal.patient.confirmage.msg") == 1) {
						return false;
					}
				}
				months = Integer.parseInt(jAgeMonths.getText());
				days = Integer.parseInt(jAgeDays.getText());
				if (years == 0 && months == 0 && days == 0) {
					throw new NumberFormatException();
				}
				birthDate = LocalDate.now().minusYears(years).minusMonths(months).minusDays(days);
			} catch (NumberFormatException ex1) {
				MessageDialog.error(this, "angal.patient.insertvalidage.msg");
				return false;
			}
		} else if (jAgeTypeBirthDate.isSelected()) {
			if (birthDate == null) {
				return false;
			}
			calcAge(birthDate);
		} else if (jAgeTypeDescription.isSelected()) {
			int index = jAgeDescComboBox.getSelectedIndex();
			AgeType ageType = null;

			if (index > 0) {
				try {
					ageType = ageTypeBrowserManager.getTypeByCode(index);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			} else {
				return false;
			}

			years = ageType.getFrom();
			if (index == 1) {
				months = jAgeMonthsComboBox.getSelectedIndex();
				patient.setAgetype(ageType.getCode() + '/' + months);
				birthDate = LocalDate.now().minusYears(years).minusMonths(months);
			} else {
				birthDate = LocalDate.now().minusYears(years);
			}
		}
		patient.setAge(years);
		patient.setBirthDate(birthDate);
		patient.setAgetype("");
		return true;
	}

	/**
	 * This method initializes jCancelButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJCancelButton() {
		if (jCancelButton == null) {
			jCancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jCancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jCancelButton.addActionListener(actionEvent -> dispose());
		}
		return jCancelButton;
	}

	/**
	 * This method initializes jBirthDate
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJBirthDate() {
		if (jBirthDate == null) {
			jBirthDate = new JPanel();
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[] { 0, 0 };
			gridBagLayout.rowHeights = new int[] { 0, 0 };
			gridBagLayout.columnWeights = new double[] { 0.0, 1.0 };
			gridBagLayout.rowWeights = new double[] { 0.0, 0.0 };
			jBirthDate.setLayout(gridBagLayout);
			GridBagConstraints gbcBirthDateLabelPanel = new GridBagConstraints();
			gbcBirthDateLabelPanel.anchor = GridBagConstraints.WEST;
			gbcBirthDateLabelPanel.gridx = 0;
			gbcBirthDateLabelPanel.gridy = 0;
			jBirthDate.add(getJBirthDateLabelPanel(), gbcBirthDateLabelPanel);
			GridBagConstraints gbcBirthDateGroupPanel = new GridBagConstraints();
			gbcBirthDateGroupPanel.fill = GridBagConstraints.HORIZONTAL;
			gbcBirthDateGroupPanel.anchor = GridBagConstraints.WEST;
			gbcBirthDateGroupPanel.gridx = 1;
			gbcBirthDateGroupPanel.gridy = 0;
			jBirthDate.add(getJBirthDateGroupPanel(), gbcBirthDateGroupPanel);
			GridBagConstraints gbcBirthDateAge = new GridBagConstraints();
			gbcBirthDateAge.anchor = GridBagConstraints.WEST;
			gbcBirthDateAge.gridx = 1;
			gbcBirthDateAge.gridy = 1;
			jBirthDate.add(getJBirthDateAge(), gbcBirthDateAge);
		}
		return jBirthDate;
	}

	private JLabel getJBirthDateAge() {
		if (jBirthDateAge == null) {
			jBirthDateAge = new JLabel(" ");
		}
		return jBirthDateAge;
	}

	private String formatYearsMonthsDays(int years, int months, int days) {
		return MessageBundle.formatMessage("angal.patient.ymd.fmt.msg", years, months, days);
	}

	/**
	 * This method initializes jBirthDateLabel
	 *
	 * @return javax.swing.JLabel
	 */
	private JLabel getJBirthDateLabel() {
		if (jBirthDateLabel == null) {
			jBirthDateLabel = new JLabel(MessageBundle.getMessage("angal.patient.birthdate"));
		}
		return jBirthDateLabel;
	}

	/**
	 * This method initializes jBirthDateGroupPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJBirthDateGroupPanel() {
		if (jBirthDateGroupPanel == null) {
			jBirthDateGroupPanel = new JPanel(new BorderLayout());

			if (!insert && patient.getBirthDate() != null) {
				birthDate = patient.getBirthDate();
			}

			GoodDateChooser jBirthDateChooser = new GoodDateChooser(birthDate, false);
			jBirthDateChooser.addDateChangeListener(event -> {
				LocalDate newDate = event.getNewDate();
				if (newDate != null) {
					calcAge(newDate);
					birthDate = newDate;
				} else {
					birthDate = null;
					getJBirthDateAge();
					jBirthDateAge.setText("");
				}
			});
			jBirthDateGroupPanel.add(jBirthDateChooser, BorderLayout.WEST);
		}
		return jBirthDateGroupPanel;
	}

	private void calcAge(LocalDate bdate) {
		Period p = Period.between(bdate, LocalDate.now());
		years = p.getYears();
		months = p.getMonths();
		days = p.getDays();
		getJBirthDateAge();
		jBirthDateAge.setText(formatYearsMonthsDays(years, months, days));
	}

	/**
	 * This method initializes jBirthDateLabelPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJBirthDateLabelPanel() {
		if (jBirthDateLabelPanel == null) {
			jBirthDateLabelPanel = new JPanel();
			jBirthDateLabelPanel.add(getJBirthDateLabel(), BorderLayout.EAST);
		}
		return jBirthDateLabelPanel;
	}

	/**
	 * This method initializes jFirstNameTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJFirstNameTextField() {
		if (jFirstNameTextField == null) {
			jFirstNameTextField = new JTextField(15);
			if (!insert) {
				jFirstNameTextField.setText(patient.getFirstName());
			}
		}
		return jFirstNameTextField;
	}

	/**
	 * This method initializes jSecondNamePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJSecondNamePanel() {
		if (jSecondNameLabelPanel == null) {
			jSecondNameLabelPanel = new JPanel();
			jSecondNameLabelPanel.add(getJSecondNameLabel(), BorderLayout.EAST);
		}
		return jSecondNameLabelPanel;
	}

	/**
	 * This method initializes jSecondNameTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJSecondNameTextField() {
		if (jSecondNameTextField == null) {
			jSecondNameTextField = new JTextField(15);
			if (!insert) {
				jSecondNameTextField.setText(patient.getSecondName());
			}

		}
		return jSecondNameTextField;
	}

	/**
	 * This method initializes jSexPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getSexPanel() {
		if (jSexPanel == null) {
			jSexPanel = new JPanel();
			ButtonGroup sexGroup = new ButtonGroup();
			radiom = new JRadioButton(MessageBundle.getMessage("angal.common.male.btn"));
			radiof = new JRadioButton(MessageBundle.getMessage("angal.common.female.btn"));
			jSexPanel.add(getJSexLabelPanel(), null);
			jSexPanel.add(radiom, radiom.getName());
			if (!insert) {
				if (patient.getSex() == 'F') {
					radiof.setSelected(true);
				} else {
					radiom.setSelected(true);
				}
			}
			sexGroup.add(radiom);
			sexGroup.add(radiof);
			jSexPanel.add(radiof);

		}
		return jSexPanel;
	}

	/**
	 * This method initializes jAddressPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAddressLabelPanel() {
		if (jAddressLabelPanel == null) {
			JLabel jAddressLabel = new JLabel(MessageBundle.getMessage("angal.common.address.txt"));
			jAddressLabelPanel = new JPanel();
			jAddressLabelPanel.add(jAddressLabel, BorderLayout.EAST);
		}
		return jAddressLabelPanel;
	}

	/**
	 * This method initializes jTaxCodeLabelPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJTaxCodeLabelPanel() {
		if (jTaxCodeLabelPanel == null) {
			JLabel jTaxCodeLabel = new JLabel(MessageBundle.getMessage("angal.patient.taxcode"));
			jTaxCodeLabelPanel = new JPanel();
			jTaxCodeLabelPanel.add(jTaxCodeLabel, BorderLayout.EAST);
		}
		return jTaxCodeLabelPanel;
	}

	/**
	 * This method initializes jAddressTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJAddressTextField() {
		if (jAddressTextField == null) {
			jAddressTextField = new JTextField(15);
			if (!insert) {
				jAddressTextField.setText(patient.getAddress());
			}
		}
		return jAddressTextField;
	}

	/**
	 * This method initializes jTaxCodeTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTaxCodeTextField() {
		if (jTaxCodeTextField == null) {
			jTaxCodeTextField = new JTextField(15);
			if (!insert) {
				jTaxCodeTextField.setText(patient.getTaxCode());
			}
		}
		return jTaxCodeTextField;
	}

	/**
	 * This method initializes jCityLabelPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJCityLabelPanel() {
		if (jCityLabelPanel == null) {
			JLabel jCityLabel = new JLabel(MessageBundle.getMessage("angal.common.city.txt"));
			jCityLabelPanel = new JPanel();
			jCityLabelPanel.add(jCityLabel, BorderLayout.EAST);
		}
		return jCityLabelPanel;
	}

	/**
	 * This method initializes jCityTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJCityTextField() {
		if (jCityTextField == null) {
			jCityTextField = new JTextField(15);
			if (!insert) {
				jCityTextField.setText(patient.getCity());
			}
		}
		return jCityTextField;
	}

	/**
	 * This method initializes jTelPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJTelPanel() {
		if (jTelephoneLabelPanel == null) {
			JLabel jTelephoneLabel = new JLabel(MessageBundle.getMessage("angal.common.telephone.txt"));
			jTelephoneLabelPanel = new JPanel();
			jTelephoneLabelPanel.add(jTelephoneLabel, BorderLayout.EAST);
		}
		return jTelephoneLabelPanel;
	}

	/**
	 * This method initializes jTelephoneTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTelephoneTextField() {
		SmsParameters.initialize();
		if (jTelephoneTextField == null) {
			jTelephoneTextField = new JTextField(15);
			jTelephoneTextField.setText(SmsParameters.ICC);
			if (!insert) {
				jTelephoneTextField.setText(patient.getTelephone());
			}
		}
		return jTelephoneTextField;
	}

	/**
	 * This method initializes jNextKinLabelPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJNextKinLabelPanel() {
		if (jNextKinLabelPanel == null) {
			JLabel jNextKinLabel = new JLabel(MessageBundle.getMessage("angal.patient.nextkin"));
			jNextKinLabelPanel = new JPanel();
			jNextKinLabelPanel.add(jNextKinLabel, BorderLayout.EAST);
		}
		return jNextKinLabelPanel;
	}

	/**
	 * This method initializes jNextKinTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJNextKinTextField() {
		if (jNextKinTextField == null) {
			jNextKinTextField = new JTextField(15);
			if (!insert) {
				jNextKinTextField.setText(patient.getNextKin());
			}
		}
		return jNextKinTextField;
	}

	/**
	 * This method initializes jBloodTypePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJBloodTypePanel() {
		if (jBloodTypePanel == null) {
			jBloodTypePanel = new JPanel();
			jBloodTypePanel = setMyBorder(jBloodTypePanel, MessageBundle.getMessage("angal.patient.bloodtype"));
			String[] bloodTypes = { MessageBundle.getMessage("angal.patient.bloodtype.unknown"), "0+", "A+", "B+", "AB+", "0-", "A-", "B-", "AB-" };
			jBloodTypeComboBox = new JComboBox(bloodTypes);
			jBloodTypePanel.add(jBloodTypeComboBox);

			if (!insert) {
				jBloodTypeComboBox.setSelectedItem(patient.getBloodType());
			}
		}
		return jBloodTypePanel;
	}

	/**
	 * This method initializes jMaritalPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJMaritalPanel() {
		if (jMaritalPanel == null) {
			jMaritalPanel = new JPanel();
			jMaritalPanel = setMyBorder(jMaritalPanel, MessageBundle.getMessage("angal.patient.maritalstatus"));
			jMaritalStatusComboBox = new JComboBox(patientBrowserManager.getMaritalList());
			jMaritalPanel.add(jMaritalStatusComboBox);

			if (!insert) {
				jMaritalStatusComboBox.setSelectedItem(patientBrowserManager.getMaritalTranslated(patient.getMaritalStatus()));
			}
		}
		return jMaritalPanel;
	}

	/**
	 * This method initializes jFirstNameLabel
	 *
	 * @return javax.swing.JLabel
	 */
	private JLabel getJFirstNameLabel() {
		if (jFirstNameLabel == null) {
			jFirstNameLabel = new JLabel(MessageBundle.getMessage("angal.patient.firstname"));
		}
		return jFirstNameLabel;
	}

	/**
	 * This method initializes jSecondNameLabel
	 *
	 * @return javax.swing.JLabel
	 */
	private JLabel getJSecondNameLabel() {
		if (jSecondNameLabel == null) {
			jSecondNameLabel = new JLabel(MessageBundle.getMessage("angal.patient.secondname"));
		}
		return jSecondNameLabel;
	}

	/**
	 * This method initializes jFirstNameLabelPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJFirstNamePanel() {
		if (jFirstNameLabelPanel == null) {
			jFirstNameLabelPanel = new JPanel();
			jFirstNameLabelPanel.add(getJFirstNameLabel(), BorderLayout.EAST);
		}
		return jFirstNameLabelPanel;
	}

	/**
	 * This method initializes jAnagraphPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAnagraphPanel() {
		if (jAnagraphPanel == null) {
			jAnagraphPanel = new JPanel();
			jAnagraphPanel.setLayout(new BoxLayout(jAnagraphPanel, BoxLayout.Y_AXIS));
			jAnagraphPanel = setMyBorder(jAnagraphPanel, "");
			jAnagraphPanel.add(getJFirstName(), null);
			jAnagraphPanel.add(getJSecondName(), null);
			jAnagraphPanel.add(getJTaxCodePanel(), null);
			jAnagraphPanel.add(getJAgeType(), null);
			jAnagraphPanel.add(getSexPanel(), null);
			jAnagraphPanel.add(getJAddressPanel(), null);
			jAnagraphPanel.add(getJCity(), null);
			jAnagraphPanel.add(getJNextKin(), null);
			jAnagraphPanel.add(getJTelephone(), null);
			jAnagraphPanel.add(getJLabelRequiredFields(), null);
		}
		return jAnagraphPanel;
	}

	private JLabel getJLabelRequiredFields() {
		if (labelRequiredFields == null) {
			labelRequiredFields = new JLabel(MessageBundle.getMessage("angal.patient.indicatesrequiredfields"));
			labelRequiredFields.setAlignmentX(CENTER_ALIGNMENT);
		}
		return labelRequiredFields;
	}

	/**
	 * This method initializes jSexLabelPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJSexLabelPanel() {
		if (jSexLabelPanel == null) {
			JLabel jSexLabel = new JLabel(MessageBundle.getMessage("angal.patient.sexstar"));
			jSexLabelPanel = new JPanel();
			jSexLabelPanel.add(jSexLabel, BorderLayout.EAST);
		}
		return jSexLabelPanel;
	}

	/**
	 * This method initializes jSecondNameFieldPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJSecondNamePanel1() {
		if (jSecondNameFieldPanel == null) {
			jSecondNameFieldPanel = new JPanel();
			jSecondNameFieldPanel.add(getJSecondNameTextField(), null);
		}
		return jSecondNameFieldPanel;
	}

	/**
	 * This method initializes jFirstName
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJFirstName() {
		if (jFirstName == null) {
			jFirstName = new JPanel();
			jFirstName.setLayout(new BorderLayout());
			jFirstName.add(getJFirstNamePanel(), BorderLayout.WEST);
			jFirstName.add(getJFirstNameFieldPanel(), BorderLayout.EAST);
		}
		return jFirstName;
	}

	/**
	 * This method initializes jFirstNameFieldPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJFirstNameFieldPanel() {
		if (jFirstNameFieldPanel == null) {
			jFirstNameFieldPanel = new JPanel();
			jFirstNameFieldPanel.add(getJFirstNameTextField(), null);
		}
		return jFirstNameFieldPanel;
	}

	/**
	 * This method initializes jSecondName
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJSecondName() {
		if (jSecondName == null) {
			jSecondName = new JPanel();
			jSecondName.setLayout(new BorderLayout());
			jSecondName.add(getJSecondNamePanel(), BorderLayout.WEST);
			jSecondName.add(getJSecondNamePanel1(), BorderLayout.EAST);
		}
		return jSecondName;
	}

	/**
	 * This method initializes jAgeType
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgeType() {
		if (jAgeType == null) {
			jAgeType = new JPanel();
			jAgeType = setMyBorder(jAgeType, MessageBundle.getMessage("angal.patient.agestar"));
			jAgeType.setLayout(new BorderLayout());
			jAgeType.add(getJAgeTypeButtonGroup(), BorderLayout.NORTH);
			jAgeType.add(getJAgeTypeSelection(), BorderLayout.CENTER);
			jAgeType.setPreferredSize(new Dimension(100, 100));
		}
		return jAgeType;
	}

	/**
	 * This method initializes jAgeTypeButtonGroup
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgeTypeButtonGroup() {
		if (jAgeTypeButtonGroup == null) {
			jAgeTypeButtonGroup = new JPanel();
			ButtonGroup ageTypeGroup = new ButtonGroup();
			ageTypeGroup.add(getJAgeTypeAge());
			ageTypeGroup.add(getJAgeTypeDescription());
			ageTypeGroup.add(getJAgeTypeBirthDate());
			jAgeTypeButtonGroup.setLayout(new BorderLayout());
			jAgeTypeButtonGroup.add(getJAgeTypeAge(), BorderLayout.WEST);
			jAgeTypeButtonGroup.add(getJAgeTypeDescription(), BorderLayout.EAST);
			jAgeTypeButtonGroup.add(getJAgeTypeBirthDatePanel(), BorderLayout.CENTER);

			ActionListener sliceActionListener = actionEvent -> {
				jAgeType.remove(jAgeTypeSelection);
				jAgeType.add(getJAgeTypeSelection());
				jAgeType.validate();
				jAgeType.repaint();
			};

			if (!insert) {
				if (patient.getBirthDate() != null) {
					jAgeTypeBirthDate.setSelected(true);
					calcAge(patient.getBirthDate());
				} else if (patient.getAgetype() != null && patient.getAgetype().compareTo("") != 0) {
					parseAgeType();
					jAgeTypeDescription.setSelected(true);
				} else {
					jAgeTypeAge.setSelected(true);
					years = patient.getAge();
				}
			} else {
				jAgeTypeAge.setSelected(true);
			}

			jAgeTypeAge.addActionListener(sliceActionListener);
			jAgeTypeDescription.addActionListener(sliceActionListener);
			jAgeTypeBirthDate.addActionListener(sliceActionListener);
		}
		return jAgeTypeButtonGroup;
	}

	/**
	 * This method initializes ageType & ageTypeMonths
	 */
	private void parseAgeType() {

		if (patient.getAgetype().compareTo("") != 0) {
			StringTokenizer token = new StringTokenizer(patient.getAgetype(), "/");
			String token1 = token.nextToken();
			String t1 = token1.substring(1, 2);
			ageType = Integer.parseInt(t1);

			if (token.hasMoreTokens()) {

				String token2 = token.nextToken();
				int t2 = Integer.parseInt(token2);
				ageTypeMonths = t2;
			} else {
				ageTypeMonths = 0;
			}
		} else {
			ageType = -1;
		}
	}

	/**
	 * This method initializes jAgeTypeSelection
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgeTypeSelection() {
		if (jAgeTypeAge.isSelected()) {
			jAgeTypeSelection = getJAge();
		} else if (jAgeTypeBirthDate.isSelected()) {
			jAgeTypeSelection = getJBirthDate();
		} else {
			jAgeTypeSelection = getJAgeDescription();
		}
		return jAgeTypeSelection;
	}

	/**
	 * This method initializes jAgeType_Age
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJAgeTypeAge() {
		if (jAgeTypeAge == null) {
			jAgeTypeAge = new JRadioButton(MessageBundle.getMessage("angal.patient.modeage.btn"));
			jAgeTypeAge.setFocusable(false);
		}
		return jAgeTypeAge;
	}

	/**
	 * This method initializes jAgeType_BirthDate
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJAgeTypeBirthDate() {
		if (jAgeTypeBirthDate == null) {
			jAgeTypeBirthDate = new JRadioButton(MessageBundle.getMessage("angal.patient.modebdate.btn"));
			jAgeTypeBirthDate.setFocusable(false);
		}
		return jAgeTypeBirthDate;
	}

	/**
	 * This method initializes jAgeType_Description
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJAgeTypeDescription() {
		if (jAgeTypeDescription == null) {
			jAgeTypeDescription = new JRadioButton(MessageBundle.getMessage("angal.patient.modedescription.btn"));
			jAgeTypeDescription.setFocusable(false);
		}
		return jAgeTypeDescription;
	}

	/**
	 * This method initializes jAgeType_BirthDatePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgeTypeBirthDatePanel() {
		if (jAgeTypeBirthDatePanel == null) {
			jAgeTypeBirthDatePanel = new JPanel();
			jAgeTypeBirthDatePanel.add(getJAgeTypeBirthDate(), null);
		}
		return jAgeTypeBirthDatePanel;
	}

	/**
	 * This method initializes jAgeDesc
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgeDescription() {
		if (jAgeDesc == null) {
			jAgeDesc = new JPanel();
			jAgeDesc.add(getJAgeDescPanel());
		}
		return jAgeDesc;
	}

	/**
	 * This method initializes jAgeMonthsPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgeMonthsPanel() {
		if (jAgeMonthsPanel == null) {
			jAgeMonthsPanel = new JPanel();
			jAgeMonthsLabel = new JLabel(MessageBundle.getMessage("angal.common.months.txt"));

			String[] months = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
					"23" };
			jAgeMonthsComboBox = new JComboBox(months);
		}

		jAgeMonthsPanel.add(jAgeMonthsComboBox);
		jAgeMonthsPanel.add(jAgeMonthsLabel);

		if (!insert && ageType == 1) {

			jAgeMonthsComboBox.setSelectedIndex(ageTypeMonths);

		}
		return jAgeMonthsPanel;
	}

	/**
	 * This method initializes jAgeDescPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAgeDescPanel() {
		if (jAgeDescPanel == null) {
			jAgeDescPanel = new JPanel();

			jAgeDescComboBox = new JComboBox();

			List<AgeType> ageList;
			try {
				ageList = ageTypeBrowserManager.getAgeType();
			} catch (OHServiceException e) {
				ageList = new ArrayList<>();
				OHServiceExceptionUtil.showMessages(e);
			}
			jAgeDescComboBox.addItem("");
			for (AgeType ag : ageList) {
				jAgeDescComboBox.addItem(MessageBundle.getMessage(ag.getDescription()));
			}

			jAgeDescPanel.add(jAgeDescComboBox);
			jAgeDescPanel.add(getJAgeMonthsPanel());
			jAgeMonthsComboBox.setEnabled(false);

			jAgeDescComboBox.addActionListener(actionEvent -> {
				if (jAgeDescComboBox.getSelectedItem().toString().compareTo(MessageBundle.getMessage("angal.agetype.newborn.txt")) == 0) {
					jAgeMonthsComboBox.setEnabled(true);
				} else {
					jAgeMonthsComboBox.setEnabled(false);
				}
			});

			if (!insert) {

				parseAgeType();
				jAgeDescComboBox.setSelectedIndex(ageType + 1);

				if (ageType == 0) {
					jAgeMonthsComboBox.setEnabled(true);
					jAgeMonthsComboBox.setSelectedIndex(ageTypeMonths);
				}
			}

		}
		return jAgeDescPanel;
	}

	/**
	 * This method initializes jAge
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAge() {
		if (jAge == null) {
			jAge = new JPanel();
			jAge.add(new JLabel(MessageBundle.getMessage("angal.common.years.txt")));
			jAge.add(getJAgeFieldYears());
			jAge.add(new JLabel(MessageBundle.getMessage("angal.common.months.txt")));
			jAge.add(getJAgeFieldMonths());
			jAge.add(new JLabel(MessageBundle.getMessage("angal.common.days.txt")));
			jAge.add(getJAgeFieldDays());
		}
		return jAge;
	}

	/**
	 * This method initializes jAddressFieldPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAddressFieldPanel() {
		if (jAddressFieldPanel == null) {
			jAddressFieldPanel = new JPanel();
			jAddressFieldPanel.add(getJAddressTextField(), null);
		}
		return jAddressFieldPanel;
	}

	private JTextField getJAgeFieldYears() {
		if (jAgeYears == null) {
			jAgeYears = new JTextField("0", 3);
			jAgeYears.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
				}

				@Override
				public void focusGained(FocusEvent e) {
					JTextField thisField = (JTextField) e.getSource();
					thisField.setSelectionStart(0);
					thisField.setSelectionEnd(thisField.getText().length());
				}
			});
			if (!insert) {
				jAgeYears.setText(String.valueOf(years));
			}
		}
		return jAgeYears;
	}

	private JTextField getJAgeFieldMonths() {
		if (jAgeMonths == null) {
			jAgeMonths = new JTextField("0", 3);
			jAgeMonths.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
				}

				@Override
				public void focusGained(FocusEvent e) {
					JTextField thisField = (JTextField) e.getSource();
					thisField.setSelectionStart(0);
					thisField.setSelectionEnd(thisField.getText().length());
				}
			});
			if (!insert) {
				jAgeMonths.setText(String.valueOf(months));
			}
		}
		return jAgeMonths;
	}

	private JTextField getJAgeFieldDays() {
		if (jAgeDays == null) {
			jAgeDays = new JTextField("0", 3);
			jAgeDays.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent e) {
				}

				@Override
				public void focusGained(FocusEvent e) {
					JTextField thisField = (JTextField) e.getSource();
					thisField.setSelectionStart(0);
					thisField.setSelectionEnd(thisField.getText().length());
				}
			});
			if (!insert) {
				jAgeDays.setText(String.valueOf(days));
			}
		}
		return jAgeDays;
	}

	/**
	 * This method initializes jTaxCodeFieldPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJTaxCodeFieldPanel() {
		if (jTaxCodeFieldPanel == null) {
			jTaxCodeFieldPanel = new JPanel();
			jTaxCodeFieldPanel.add(getJTaxCodeTextField(), null);
		}
		return jTaxCodeFieldPanel;
	}

	/**
	 * This method initializes jCityFieldPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJCityFieldPanel() {
		if (jCityFieldPanel == null) {
			jCityFieldPanel = new JPanel();
			jCityFieldPanel.add(getJCityTextField(), null);
		}
		return jCityFieldPanel;
	}

	/**
	 * This method initializes jNextKinFieldPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJNextKinFieldPanel() {
		if (jNextKinFieldPanel == null) {
			jNextKinFieldPanel = new JPanel();
			jNextKinFieldPanel.add(getJNextKinTextField(), null);
		}
		return jNextKinFieldPanel;
	}

	/**
	 * This method initializes jTelephoneFieldPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJTelephoneFieldPanel() {
		if (jTelephoneFieldPanel == null) {
			jTelephoneFieldPanel = new JPanel();
			jTelephoneFieldPanel.add(getJTelephoneTextField(), null);
		}
		return jTelephoneFieldPanel;
	}

	/**
	 * This method initializes jAddressPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAddressPanel() {
		if (jAddress == null) {
			jAddress = new JPanel();
			jAddress.setLayout(new BorderLayout());
			jAddress.add(getJAddressLabelPanel(), BorderLayout.WEST);
			jAddress.add(getJAddressFieldPanel(), BorderLayout.EAST);

		}
		return jAddress;
	}

	/**
	 * This method initializes jTaxCodePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJTaxCodePanel() {
		if (jTaxCodePanel == null) {
			jTaxCodePanel = new JPanel();
			jTaxCodePanel.setLayout(new BorderLayout());
			jTaxCodePanel.add(getJTaxCodeLabelPanel(), BorderLayout.WEST);
			jTaxCodePanel.add(getJTaxCodeFieldPanel(), BorderLayout.EAST);

		}
		return jTaxCodePanel;
	}

	/**
	 * This method initializes jCity
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJCity() {
		if (jCity == null) {
			jCity = new JPanel();
			jCity.setLayout(new BorderLayout());
			jCity.add(getJCityLabelPanel(), BorderLayout.WEST);
			jCity.add(getJCityFieldPanel(), BorderLayout.EAST);
		}
		return jCity;
	}

	/**
	 * This method initializes jNextKin
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJNextKin() {
		if (jNextKin == null) {
			jNextKin = new JPanel();
			jNextKin.setLayout(new BorderLayout());
			jNextKin.add(getJNextKinLabelPanel(), BorderLayout.WEST);
			jNextKin.add(getJNextKinFieldPanel(), BorderLayout.EAST);
		}
		return jNextKin;
	}

	/**
	 * This method initializes jTelephone
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJTelephone() {
		if (jTelephone == null) {
			jTelephone = new JPanel();
			jTelephone.setLayout(new BorderLayout());
			jTelephone.add(getJTelPanel(), BorderLayout.WEST);
			jTelephone.add(getJTelephoneFieldPanel(), BorderLayout.EAST);
		}
		return jTelephone;
	}

	/**
	 * This method initializes jDataContainPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJDataContainPanel() {
		if (jDataContainPanel == null) {
			jDataContainPanel = new JPanel();
			if (!insert) {
				StringBuilder title = new StringBuilder(patient.getName()).append(" (").append(MessageBundle.getMessage("angal.common.code.txt")).append(": ")
								.append(patient.getCode()).append(')');
				jDataContainPanel = setMyBorderCenter(jDataContainPanel, title.toString());
			} else {
				jDataContainPanel = setMyBorderCenter(jDataContainPanel, MessageBundle.getMessage("angal.patient.insertdataofnewpatient"));
			}
			jDataContainPanel.setLayout(new BorderLayout());
			jDataContainPanel.add(getJAnagraphPanel(), BorderLayout.CENTER);
			jDataContainPanel.add(getJExtensionContent(), BorderLayout.EAST);
		}
		return jDataContainPanel;
	}

	/**
	 * This method initializes jProfessionPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJProfessionPanel() {
		if (jProfessionPanel == null) {
			jProfessionPanel = new JPanel();
			jProfessionPanel = setMyBorder(jProfessionPanel, MessageBundle.getMessage("angal.patient.profession"));
			jProfessionComboBox = new JComboBox(patientBrowserManager.getProfessionList());
			jProfessionPanel.add(jProfessionComboBox);

			if (!insert) {
				jProfessionComboBox.setSelectedItem(patientBrowserManager.getProfessionTranslated(patient.getProfession()));
			}
		}
		return jProfessionPanel;
	}

	/**
	 * This method initializes jFatherPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJFatherPanel() {
		if (jFatherPanel == null) {
			jFatherPanel = new JPanel();
			jFatherPanel.setLayout(new BorderLayout());
			JPanel jFatherNamePanel = new JPanel();
			jFatherNamePanel.add(getJFatherNameTextField());
			ButtonGroup fatherGroup = new ButtonGroup();
			fatherGroup.add(getJFatherDead());
			fatherGroup.add(getJFatherAlive());
			fatherGroup.add(getJFatherUnknown());
			jFatherPanel = setMyBorder(jFatherPanel, MessageBundle.getMessage("angal.patient.fathername"));
			jFatherPanel.add(jFatherNamePanel, BorderLayout.NORTH);
			jFatherPanel.add(getJFatherOptions(), BorderLayout.CENTER);
			if (!insert) {
				switch (patient.getFather()) {
				case 'D':
					getJFatherDead().setSelected(true);
					break;
				case 'A':
					getJFatherAlive().setSelected(true);
					break;
				default:
					break;
				}
			}

		}
		return jFatherPanel;
	}

	private JPanel getJFatherOptions() {
		if (jFatherPanelOptions == null) {
			jFatherPanelOptions = new JPanel();
			jFatherPanelOptions.add(getJFatherDead());
			jFatherPanelOptions.add(getJFatherUnknown());
			jFatherPanelOptions.add(getJFatherAlivePanel());
		}
		return jFatherPanelOptions;
	}

	/**
	 * This method initializes jFatherDeadRadioButton
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJFatherDead() {
		if (jFatherDead == null) {
			jFatherDead = new JRadioButton(MessageBundle.getMessage("angal.patient.dead.btn"));
		}
		return jFatherDead;
	}

	/**
	 * This method initializes jFatherAliveRadioButton
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJFatherAlive() {
		if (jFatherAlive == null) {
			jFatherAlive = new JRadioButton(MessageBundle.getMessage("angal.patient.alive.btn"));
		}
		return jFatherAlive;
	}

	/**
	 * This method initializes jFather_Unknown radio button
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJFatherUnknown() {
		if (jFatherUnknown == null) {
			jFatherUnknown = new JRadioButton(MessageBundle.getMessage("angal.patient.unknown.btn"));
			jFatherUnknown.setSelected(true);
		}
		return jFatherUnknown;
	}

	/**
	 * This method initializes jMotherPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJMotherPanel() {
		if (jMotherPanel == null) {
			jMotherPanel = new JPanel();
			jMotherPanel = setMyBorder(jMotherPanel, MessageBundle.getMessage("angal.patient.mothername"));

			jMotherPanel.setLayout(new BorderLayout());
			JPanel jMotherNamePanel = new JPanel();
			jMotherNamePanel.add(getJMotherNameTextField());
			jMotherPanel.add(jMotherNamePanel, BorderLayout.NORTH);
			jMotherPanel.add(getJMotherOptions(), BorderLayout.CENTER);
			ButtonGroup motherGroup = new ButtonGroup();
			motherGroup.add(getJMotherDead());
			motherGroup.add(getJMotherAlive());
			motherGroup.add(getJMotherUnknown());
			if (!insert) {
				switch (patient.getMother()) {
				case 'D':
					getJMotherDead().setSelected(true);
					break;
				case 'A':
					getJMotherAlive().setSelected(true);
					break;
				default:
					break;
				}
			}
		}
		return jMotherPanel;
	}

	private JPanel getJMotherOptions() {
		if (jMotherOptions == null) {
			jMotherOptions = new JPanel();
			jMotherOptions.add(getJMotherDead(), BorderLayout.WEST);
			jMotherOptions.add(getJMotherUnknown(), BorderLayout.EAST);
			jMotherOptions.add(getJMotherAlivePanel(), BorderLayout.CENTER);
		}
		return jMotherOptions;
	}

	/**
	 * This method initializes jMotherDeadRadioButton
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJMotherDead() {
		if (jMotherDead == null) {
			jMotherDead = new JRadioButton(MessageBundle.getMessage("angal.patient.dead.btn"));
		}
		return jMotherDead;
	}

	/**
	 * This method initializes jMotherAliveRadioButton
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJMotherAlive() {
		if (jMotherAlive == null) {
			jMotherAlive = new JRadioButton(MessageBundle.getMessage("angal.patient.alive.btn"));
		}
		return jMotherAlive;
	}

	/**
	 * This method initializes jMotherUnknownRadioButton
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJMotherUnknown() {
		if (jMotherUnknown == null) {
			jMotherUnknown = new JRadioButton(MessageBundle.getMessage("angal.patient.unknown.btn"));
			jMotherUnknown.setSelected(true);
		}
		return jMotherUnknown;
	}

	/**
	 * This method initializes jInsurancePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJInsurancePanel() {
		if (jInsurancePanel == null) {
			jInsurancePanel = new JPanel(new BorderLayout());
			jInsurancePanel = setMyBorder(jInsurancePanel, MessageBundle.getMessage("angal.patient.hasinsurance"));

			JPanel groupPanel = new JPanel();
			groupPanel.add(getJInsuranceYes());
			groupPanel.add(getJInsuranceNoPanel());
			groupPanel.add(getJInsuranceUnknown());

			ButtonGroup insuranceGroup = new ButtonGroup();
			insuranceGroup.add(getJInsuranceYes());
			insuranceGroup.add(getJInsuranceNo());
			insuranceGroup.add(getJInsuranceUnknown());
			if (!insert) {
				switch (patient.getHasInsurance()) {
				case 'Y':
					getJInsuranceYes().setSelected(true);
					break;
				case 'N':
					getJInsuranceNo().setSelected(true);
					break;
				default:
					break;
				}
			}
			jInsurancePanel.add(groupPanel, BorderLayout.CENTER);
		}
		return jInsurancePanel;
	}

	/**
	 * This method initializes jInsuranceYesRadioButton
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJInsuranceYes() {
		if (jInsuranceYes == null) {
			jInsuranceYes = new JRadioButton(MessageBundle.getMessage("angal.patient.hasinsuranceyes.btn"));
		}
		return jInsuranceYes;
	}

	/**
	 * This method initializes jInsuranceNoRadioButton
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJInsuranceNo() {
		if (jInsuranceNo == null) {
			jInsuranceNo = new JRadioButton(MessageBundle.getMessage("angal.patient.hasinsuranceno.btn"));
		}
		return jInsuranceNo;
	}

	/**
	 * This method initializes jInsuranceUnknownRadioButton
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJInsuranceUnknown() {
		if (jInsuranceUnknown == null) {
			jInsuranceUnknown = new JRadioButton(MessageBundle.getMessage("angal.patient.unknown.btn"));
			jInsuranceUnknown.setSelected(true);
		}
		return jInsuranceUnknown;
	}

	/**
	 * This method initializes jParentPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJParentPanel() {
		if (jParentPanel == null) {
			jParentPanel = new JPanel();
			ButtonGroup parentGroup = new ButtonGroup();
			parentGroup.add(getJParentYes());
			parentGroup.add(getJParentNo());
			parentGroup.add(getJParentUnknown());
			jParentPanel = setMyBorder(jParentPanel, MessageBundle.getMessage("angal.patient.parenttogether"));
			jParentPanel.add(getJParentYes());
			jParentPanel.add(getJPanelNoPanel());
			jParentPanel.add(getJParentUnknown());
			if (!insert) {
				switch (patient.getParentTogether()) {
				case 'Y':
					getJParentYes().setSelected(true);
					break;
				case 'N':
					getJParentNo().setSelected(true);
					break;
				default:
					break;
				}
			}
		}
		return jParentPanel;
	}

	/**
	 * This method initializes jParentYesRadioButton
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJParentYes() {
		if (jParentYes == null) {
			jParentYes = new JRadioButton(MessageBundle.getMessage("angal.patient.yes.btn"));
		}
		return jParentYes;
	}

	/**
	 * This method initializes jParentNoRadioButton
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJParentNo() {
		if (jParentNo == null) {
			jParentNo = new JRadioButton(MessageBundle.getMessage("angal.patient.no.btn"));
		}
		return jParentNo;
	}

	/**
	 * This method initializes jParentUnknownRadioButton
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJParentUnknown() {
		if (jParentUnknown == null) {
			jParentUnknown = new JRadioButton(MessageBundle.getMessage("angal.patient.unknown.btn"));
			jParentUnknown.setSelected(true);
		}
		return jParentUnknown;
	}

	/**
	 * This method initializes jExtensionContent
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJExtensionContent() {
		if (jExtensionContent == null) {
			jExtensionContent = new JPanel();
			jExtensionContent.setLayout(new BoxLayout(getJExtensionContent(), BoxLayout.Y_AXIS));
			jExtensionContent.add(getJBloodTypePanel(), null);
			jExtensionContent.add(getJMaritalPanel(), null);
			jExtensionContent.add(getJProfessionPanel(), null);
			jExtensionContent.add(getJFatherPanel(), null);
			jExtensionContent.add(getJMotherPanel(), null);
			jExtensionContent.add(getJParentPanel(), null);
			jExtensionContent.add(getJInsurancePanel(), null);
		}
		return jExtensionContent;
	}

	/**
	 * Set a specific border+title to a panel
	 */
	private JPanel setMyBorder(JPanel c, String title) {
		Border b1 = BorderFactory.createLineBorder(Color.lightGray);
		Border b2 = BorderFactory.createTitledBorder(b1, title, TitledBorder.LEFT, TitledBorder.TOP);
		c.setBorder(b2);
		return c;
	}

	private JPanel setMyBorderCenter(JPanel c, String title) {
		Border b1 = BorderFactory.createLineBorder(Color.lightGray);
		Border b2 = BorderFactory.createTitledBorder(b1, title, TitledBorder.CENTER, TitledBorder.TOP);
		c.setBorder(b2);
		return c;
	}

	/**
	 * This method initializes jFatherAlivePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJFatherAlivePanel() {
		if (jFatherAlivePanel == null) {
			jFatherAlivePanel = new JPanel();
			jFatherAlivePanel.add(getJFatherAlive(), null);
		}
		return jFatherAlivePanel;
	}

	/**
	 * This method initializes jMotherAlivePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJMotherAlivePanel() {
		if (jMotherAlivePanel == null) {
			jMotherAlivePanel = new JPanel();
			jMotherAlivePanel.add(getJMotherAlive(), null);
		}
		return jMotherAlivePanel;
	}

	/**
	 * This method initializes jInsuranceNoPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJInsuranceNoPanel() {
		if (jInsuranceNoPanel == null) {
			jInsuranceNoPanel = new JPanel();
			jInsuranceNoPanel.add(getJInsuranceNo(), null);
		}
		return jInsuranceNoPanel;
	}

	/**
	 * This method initializes jParentNoPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelNoPanel() {
		if (jParentNoPanel == null) {
			jParentNoPanel = new JPanel();
			jParentNoPanel.add(getJParentNo(), null);
		}
		return jParentNoPanel;
	}

	/**
	 * This method initializes jNoteTextArea
	 *
	 * @return javax.swing.JPanel
	 */
	private JTextArea getJTextArea() {
		if (jNoteTextArea == null) {
			jNoteTextArea = new JTextArea();
			jNoteTextArea.setTabSize(4);
			jNoteTextArea.setAutoscrolls(true);
			jNoteTextArea.setLineWrap(true);
			if (!insert) {
				jNoteTextArea.setText(patient.getNote());
			}
		}
		return jNoteTextArea;
	}

	/**
	 * This method initializes jNotePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJRightPanel() {
		if (jRightPanel == null) {
			jRightPanel = new JPanel(new BorderLayout());

			try {
				PatientProfilePhoto photo = this.patientBrowserManager.retrievePatientProfilePhoto(patient);
				Image image = photo != null ? photo.getPhotoAsImage() : null;
				Image scaledImage = image != null ? ImageUtil.scaleImage(image, GeneralData.IMAGE_THUMBNAIL_MAX_WIDTH) : null;
				photoPanel = new PatientPhotoPanel(this, patient.getCode(), scaledImage);

			} catch (IOException ioException) {
				LOGGER.error(ioException.getMessage(), ioException);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			if (photoPanel != null) {
				jRightPanel.add(photoPanel, BorderLayout.NORTH);
			}
			jRightPanel.add(getJNoteScrollPane(), BorderLayout.CENTER);
			jRightPanel.add(getJPanelConsensus(), BorderLayout.SOUTH);

		}
		return jRightPanel;
	}

	private JPanel getJPanelConsensus() {
		try {
			if (patient != null && patient.getCode() != null) {
				consensus = this.patientConsensusManager.getPatientConsensusByUserId(patient.getCode()).get();
			} else {
				consensus = new PatientConsensus();
			}
		} catch (OHServiceException e) {
			consensus = new PatientConsensus();
			LOGGER.debug(e.getMessage());
		}
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JCheckBox checkboxConsensus = new JCheckBox("<html><body style='width: 150px; padding-left: 10px;'>" +
						MessageBundle.getMessage("angal.patient.consensus.consensus.txt") +
						"</body></html>");
		checkboxConsensus.addActionListener(e -> consensus.setConsensusFlag(!consensus.isConsensusFlag()));
		checkboxConsensus.setSelected(consensus.isConsensusFlag());
		panel.add(checkboxConsensus);

		JCheckBox checkboxService = new JCheckBox("<html><body style='width: 150px; padding-left: 10px;'>" +
						MessageBundle.getMessage("angal.patient.consensus.service.txt") +
						"</body></html>");
		checkboxService.addActionListener(e -> consensus.setServiceFlag(!consensus.isServiceFlag()));
		checkboxService.setSelected(consensus.isServiceFlag());
		panel.add(checkboxService);

		panel.setBorder(
						BorderFactory.createCompoundBorder(
										BorderFactory.createTitledBorder(
														MessageBundle.getMessage("angal.patient.consensus.border")),
										BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		return panel;
	}

	private JScrollPane getJNoteScrollPane() {
		if (jNoteScrollPane == null) {
			jNoteScrollPane = new JScrollPane(getJTextArea());
			jNoteScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			jNoteScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jNoteScrollPane.setPreferredSize(new Dimension(200, 200));
			jNoteScrollPane.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.patient.note")),
											BorderFactory.createEmptyBorder(5, 5, 5, 5)),
							jNoteScrollPane.getBorder()));
		}
		return jNoteScrollPane;
	}

	/**
	 * This method initializes jFatherNameTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJFatherNameTextField() {
		if (jFatherNameTextField == null) {
			jFatherNameTextField = new JTextField(15);
			if (!insert) {
				jFatherNameTextField.setText(patient.getFatherName());
			}
		}
		return jFatherNameTextField;
	}

	/**
	 * This method initializes jMotherNameTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJMotherNameTextField() {
		if (jMotherNameTextField == null) {
			jMotherNameTextField = new JTextField(15);
			if (!insert) {
				jMotherNameTextField.setText(patient.getMotherName());
			}
		}
		return jMotherNameTextField;
	}

	public void setPatientPhoto(BufferedImage photo) {
		if (photo != null) {
			PatientProfilePhoto patientProfilePhoto = new PatientProfilePhoto();
			patientProfilePhoto.setPhoto(ImageUtil.imageToByte(photo));
			patient.setPatientProfilePhoto(patientProfilePhoto);
		} else {
			patient.setPatientProfilePhoto(null);
		}
	}

}
