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
package org.isf.patvac.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.patvac.manager.PatVacManager;
import org.isf.patvac.model.PatientVaccine;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.CustomJDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.RememberDates;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;
import org.isf.vactype.manager.VaccineTypeBrowserManager;
import org.isf.vactype.model.VaccineType;

/**
 * ------------------------------------------
 * PatVacEdit - edit (new/update) a patient's vaccine
 * -----------------------------------------
 * modification history
 * 25/08/2011 - claudia - first beta version
 * 04/11/2011 - claudia modify vaccine date check on OK button
 * 14/11/2011 - claudia inserted search condition on patient based on ENHANCEDSEARCH property
 * ------------------------------------------
 */
public class PatVacEdit extends JDialog {

	private static final long serialVersionUID = -4271389493861772053L;
	private boolean insert = false;

	private PatientVaccine patVac = null;
	private JPanel jContentPane = null;
	private JPanel buttonPanel = null;
	private JPanel dataPanel = null;
	private JPanel dataPatient = null;

	private JButton okButton = null;
	private JButton cancelButton = null;
	private JButton jSearchButton = null;

	private JComboBox<Vaccine> vaccineComboBox = null;
	private JComboBox<Object> patientComboBox = null;
	private JComboBox<VaccineType> vaccineTypeComboBox = null;

	private VoLimitedTextField patTextField = null;
	private VoLimitedTextField ageTextField = null;
	private VoLimitedTextField sexTextField = null;
	private VoLimitedTextField progrTextField = null;

	private JTextField jTextPatientSrc;
	private Patient selectedPatient = null;
	private String lastKey;
	private String s;
	private ArrayList<Patient> patientList = null;
	private CustomJDateChooser vaccineDateFieldCal = null;
	private GregorianCalendar dateIn = null;
	private int patNextYProg;

	private JPanel centerPanel;
	private JPanel patientPanel;
	
	private VaccineBrowserManager vaccineManager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);
	private PatVacManager patientVaccineManager = Context.getApplicationContext().getBean(PatVacManager.class);
	private VaccineTypeBrowserManager vaccineTypeManager = Context.getApplicationContext().getBean(VaccineTypeBrowserManager.class);
	private PatientBrowserManager patientManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);

	public PatVacEdit(JFrame myFrameIn, PatientVaccine patientVaccineIn, boolean action) {
		super(myFrameIn, true);
		insert = action;
		patVac = patientVaccineIn;
		selectedPatient = patientVaccineIn.getPatient();
		patNextYProg = getPatientVaccineYMaxProg() + 1;
		initialize();
	}

	private int getPatientVaccineYMaxProg() {
		
		try {
			return patientVaccineManager.getProgYear(0);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
			return 0;
		}
	}

	/**
	 * This method initializes this Frame, sets the correct Dimensions
	 */
	private void initialize() {

		this.setContentPane(getJContentPane());
		this.setResizable(false);
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.patvac.newpatientvaccine.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.patvac.edipatientvaccine.title"));
		}
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	/**
	 * This method initializes jContentPane, adds the main parts of the frame
	 * 
	 * @return jContentPanel (JPanel)
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
			jContentPane.add(getCenterPanel(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes dataPanel. This panel contains all items (combo
	 * boxes,calendar) to define a vaccine
	 * 
	 * @return dataPanel (JPanel)
	 */
	private JPanel getDataPanel() {
		if (dataPanel == null) {
			// initialize data panel
			dataPanel = new JPanel();
			GridBagLayout gbl_dataPanel = new GridBagLayout();
			gbl_dataPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0};
			dataPanel.setLayout(gbl_dataPanel);
			
			
			//patient search fields
			JLabel patientLabel = new JLabel(MessageBundle.getMessage("angal.patvac.searchpatient"));
			GridBagConstraints gbc_jPatientLabel = new GridBagConstraints();
			gbc_jPatientLabel.anchor = GridBagConstraints.WEST;
			gbc_jPatientLabel.fill = GridBagConstraints.VERTICAL;
			gbc_jPatientLabel.insets = new Insets(5, 5, 5, 5);
			gbc_jPatientLabel.gridx = 0;
			gbc_jPatientLabel.gridy = 1;
			dataPanel.add(patientLabel, gbc_jPatientLabel);
			
			GridBagConstraints gbc_jPatientSearchField = new GridBagConstraints();
			gbc_jPatientSearchField.fill = GridBagConstraints.BOTH;
			gbc_jPatientSearchField.insets = new Insets(5, 5, 5, 5);
			gbc_jPatientSearchField.gridx = 1;
			gbc_jPatientSearchField.gridy = 1;
			dataPanel.add(getJTextFieldSearchPatient(), gbc_jPatientSearchField);
			
			if (GeneralData.ENHANCEDSEARCH) {
				
				GridBagConstraints gbc_jPatientSearchButton = new GridBagConstraints();
				gbc_jPatientSearchButton.insets = new Insets(5, 5, 5, 5);
				gbc_jPatientSearchButton.anchor = GridBagConstraints.WEST;
				gbc_jPatientSearchButton.gridx = 2;
				gbc_jPatientSearchButton.gridy = 1;
				dataPanel.add(getJSearchButton(), gbc_jPatientSearchButton);
			}
			
			GridBagConstraints gbc_jPatientComboBox = new GridBagConstraints();
			gbc_jPatientComboBox.gridwidth = 2;
			gbc_jPatientComboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_jPatientComboBox.insets = new Insets(5, 5, 5, 5);
			gbc_jPatientComboBox.gridx = 3;
			gbc_jPatientComboBox.gridy = 1;
			dataPanel.add(getPatientComboBox(s), gbc_jPatientComboBox);
			
			if (!insert) {
				patientComboBox.setEnabled(false);
				jTextPatientSrc.setEnabled(false);
			}

			// vaccine date
			JLabel vaccineDateLabel = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
			GridBagConstraints gbc_vaccineDateLabel = new GridBagConstraints();
			gbc_vaccineDateLabel.anchor = GridBagConstraints.NORTHWEST;
			gbc_vaccineDateLabel.insets = new Insets(5, 5, 5, 5);
			gbc_vaccineDateLabel.gridx = 0;
			gbc_vaccineDateLabel.gridy = 0;
			dataPanel.add(vaccineDateLabel, gbc_vaccineDateLabel);
			vaccineDateFieldCal = getVaccineDateFieldCal();
			vaccineDateFieldCal.setLocale(new Locale(GeneralData.LANGUAGE));
			vaccineDateFieldCal.setDateFormatString("dd/MM/yy");
			GridBagConstraints gbc_vaccineDateFieldCal = new GridBagConstraints();
			gbc_vaccineDateFieldCal.anchor = GridBagConstraints.WEST;
			gbc_vaccineDateFieldCal.insets = new Insets(5, 5, 5, 5);
			gbc_vaccineDateFieldCal.gridx = 1;
			gbc_vaccineDateFieldCal.gridy = 0;
			dataPanel.add(vaccineDateFieldCal, gbc_vaccineDateFieldCal);

			// progressive
			JLabel progrLabel = new JLabel(MessageBundle.getMessage("angal.patvac.progressive"));
			GridBagConstraints gbc_progrLabel = new GridBagConstraints();
			gbc_progrLabel.anchor = GridBagConstraints.NORTHEAST;
			gbc_progrLabel.insets = new Insets(5, 5, 5, 5);
			gbc_progrLabel.gridx = 3;
			gbc_progrLabel.gridy = 0;
			dataPanel.add(progrLabel, gbc_progrLabel);
			GridBagConstraints gbc_progrTextField = new GridBagConstraints();
			gbc_progrTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_progrTextField.anchor = GridBagConstraints.NORTHEAST;
			gbc_progrTextField.insets = new Insets(5, 5, 5, 5);
			gbc_progrTextField.gridx = 4;
			gbc_progrTextField.gridy = 0;
			dataPanel.add(getProgrTextField(), gbc_progrTextField);

			// vaccineType combo box
			JLabel vaccineTypeLabel = new JLabel(MessageBundle.getMessage("angal.patvac.vaccinetype"));
			GridBagConstraints gbc_vaccineTypeLabel = new GridBagConstraints();
			gbc_vaccineTypeLabel.anchor = GridBagConstraints.WEST;
			gbc_vaccineTypeLabel.fill = GridBagConstraints.VERTICAL;
			gbc_vaccineTypeLabel.insets = new Insets(5, 5, 5, 5);
			gbc_vaccineTypeLabel.gridx = 0;
			gbc_vaccineTypeLabel.gridy = 2;
			dataPanel.add(vaccineTypeLabel, gbc_vaccineTypeLabel);
			GridBagConstraints gbc_vaccineTypeComboBox = new GridBagConstraints();
			gbc_vaccineTypeComboBox.fill = GridBagConstraints.BOTH;
			gbc_vaccineTypeComboBox.insets = new Insets(5, 5, 5, 5);
			gbc_vaccineTypeComboBox.gridwidth = 4;
			gbc_vaccineTypeComboBox.gridx = 1;
			gbc_vaccineTypeComboBox.gridy = 2;
			dataPanel.add(getVaccineTypeComboBox(), gbc_vaccineTypeComboBox);

			// vaccine combo box
			JLabel vaccineLabel = new JLabel(MessageBundle.getMessage("angal.patvac.vaccine"));
			GridBagConstraints gbc_vaccineLabel = new GridBagConstraints();
			gbc_vaccineLabel.anchor = GridBagConstraints.WEST;
			gbc_vaccineLabel.fill = GridBagConstraints.VERTICAL;
			gbc_vaccineLabel.insets = new Insets(5, 5, 5, 5);
			gbc_vaccineLabel.gridx = 0;
			gbc_vaccineLabel.gridy = 3;
			dataPanel.add(vaccineLabel, gbc_vaccineLabel);
			GridBagConstraints gbc_vaccineComboBox = new GridBagConstraints();
			gbc_vaccineComboBox.fill = GridBagConstraints.BOTH;
			gbc_vaccineComboBox.insets = new Insets(5, 5, 5, 5);
			gbc_vaccineComboBox.gridwidth = 4;
			gbc_vaccineComboBox.gridx = 1;
			gbc_vaccineComboBox.gridy = 3;
			dataPanel.add(getVaccineComboBox(), gbc_vaccineComboBox);
			
		}
		return dataPanel;
	}

	private JTextField getJTextFieldSearchPatient() {
		jTextPatientSrc = new JTextField();
		if (GeneralData.ENHANCEDSEARCH) {
			jTextPatientSrc.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						jSearchButton.doClick();
					}
				}

				public void keyReleased(KeyEvent e) {
				}

				public void keyTyped(KeyEvent e) {
				}
			});
		} else {
			jTextPatientSrc.addKeyListener(new KeyListener() {
				public void keyTyped(KeyEvent e) {
					lastKey = "";
					String s = "" + e.getKeyChar();
					if (Character.isLetterOrDigit(e.getKeyChar())) {
						lastKey = s;
					}
					s = jTextPatientSrc.getText() + lastKey;
					s = s.trim();
					filterPatient(s);
				}

				public void keyPressed(KeyEvent e) {
				}

				public void keyReleased(KeyEvent e) {
				}
			});
		} // search condition field
		return jTextPatientSrc;
	}

	/**
	 * This method initializes getJSearchButton
	 * 
	 * @return JButton
	 */
	private JButton getJSearchButton() {
		if (jSearchButton == null) {
			jSearchButton = new JButton();
			jSearchButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			jSearchButton.setPreferredSize(new Dimension(20, 20));
			if (!insert) {
				jSearchButton.setEnabled(false);
			}
			jSearchButton.addActionListener(e -> {
					patientComboBox.removeAllItems();
					resetPatVacPat();
					getPatientComboBox(jTextPatientSrc.getText());
			});
		}
		return jSearchButton;
	}

	/**
	 * This method initializes getVaccineDateFieldCal
	 * 
	 * @return JDateChooser
	 */
	private CustomJDateChooser getVaccineDateFieldCal() {
		java.util.Date myDate = null;
		if (insert) {
			dateIn = RememberDates.getLastPatientVaccineDateGregorian();
		} else {
			dateIn = patVac.getVaccineDate();
		}
		if (dateIn != null) {
			myDate = dateIn.getTime();
		}
		return (new CustomJDateChooser(myDate, "dd/MM/yy"));
	}

	/**
	 * This method initializes getProgrTextField about progressive field
	 * 
	 * @return progrTextField (VoLimitedTextField)
	 */
	private VoLimitedTextField getProgrTextField() {
		if (progrTextField == null) {
			progrTextField = new VoLimitedTextField(4, 5);
			if (insert) {
				progrTextField.setText(String.valueOf(patNextYProg));
			} else {
				progrTextField.setText(String.valueOf(patVac.getProgr()));
			}
		}
		return progrTextField;
	}

	/**
	 * This method initializes vaccineTypeComboBox. It is used to display available
	 * vaccine types
	 * 
	 * @return vaccineTypeComboBox (JComboBox)
	 */
	private JComboBox<VaccineType> getVaccineTypeComboBox() {
		if (vaccineTypeComboBox == null) {
			vaccineTypeComboBox = new JComboBox<VaccineType>();
			vaccineTypeComboBox.setPreferredSize(new Dimension(200, 30));
			vaccineTypeComboBox.addItem(new VaccineType("", MessageBundle.getMessage("angal.patvac.allvaccinetype")));

			ArrayList<VaccineType> types = null;
			try {
				types = vaccineTypeManager.getVaccineType();
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
			VaccineType vaccineTypeSel = null;
			if (types != null) {
				for (VaccineType elem : types) {
					vaccineTypeComboBox.addItem(elem);
					if (!insert && elem.getCode() != null) {
						if (elem.getCode().equalsIgnoreCase((patVac.getVaccine().getVaccineType().getCode()))) {
							vaccineTypeSel = elem;
						}
					}
				}
			}
			if (vaccineTypeSel != null)
				vaccineTypeComboBox.setSelectedItem(vaccineTypeSel);

			vaccineTypeComboBox.addActionListener(e -> {
					vaccineComboBox.removeAllItems();
					getVaccineComboBox();
			});
		}
		return vaccineTypeComboBox;
	}

	/**
	 * This method initializes comboVaccine. It is used to display available
	 * vaccines
	 * 
	 * @return vaccineComboBox (JComboBox)
	 */
	private JComboBox<Vaccine> getVaccineComboBox() {
		if (vaccineComboBox == null) {
			vaccineComboBox = new JComboBox<Vaccine>();
			vaccineComboBox.setPreferredSize(new Dimension(200, 30));
		}
		vaccineComboBox.addItem(new Vaccine("", MessageBundle.getMessage("angal.patvac.allvaccine"), new VaccineType("", "")));
		ArrayList<Vaccine> allVac = null;
		try {
			if (((VaccineType) vaccineTypeComboBox.getSelectedItem()).getDescription().equals(MessageBundle.getMessage("angal.patvac.allvaccinetype"))) {
				allVac = vaccineManager.getVaccine();
			} else {
				allVac = vaccineManager.getVaccine(((VaccineType) vaccineTypeComboBox.getSelectedItem()).getCode());
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		Vaccine vaccineSel = null;
		if (allVac != null) {
			for (Vaccine elem : allVac) {
				if (!insert && elem.getCode() != null) {
					if (elem.getCode().equalsIgnoreCase((patVac.getVaccine().getCode()))) {
						vaccineSel = elem;
					}
				}
				vaccineComboBox.addItem(elem);
			}
		}
		if (vaccineSel != null) {
			vaccineComboBox.setSelectedItem(vaccineSel);
		}
		return vaccineComboBox;
	}

	/**
	 * This method filter patient based on search string
	 */
	private void filterPatient(String key) {
		patientComboBox.removeAllItems();

		if (key == null || key.compareTo("") == 0) {
			patientComboBox.addItem(MessageBundle.getMessage("angal.patvac.selectapatient"));
			resetPatVacPat();
		}

		for (Patient elem : patientList) {
			if (key != null) {
				// Search key extended to name and code
				StringBuilder sbName = new StringBuilder();
				sbName.append(elem.getSecondName().toUpperCase());
				sbName.append(elem.getFirstName().toUpperCase());
				sbName.append(elem.getCode());
				String name = sbName.toString();

				if (name.toLowerCase().contains(key.toLowerCase())) {
					patientComboBox.addItem(elem);
				}
			} else {
				patientComboBox.addItem(elem);
			}
		}

		if (patientComboBox.getItemCount() == 1) {
			selectedPatient = (Patient) patientComboBox.getSelectedItem();
			setPatient(selectedPatient);
		}

		if (patientComboBox.getItemCount() > 0) {
			if (patientComboBox.getItemAt(0) instanceof Patient) {
				selectedPatient = (Patient) patientComboBox.getItemAt(0);
				setPatient(selectedPatient);
			} else
				selectedPatient = null;
		} else
			selectedPatient = null;
	}

	/**
	 * This method reset patient's additional data
	 */
	private void resetPatVacPat() {
		patTextField.setText("");
		ageTextField.setText("");
		sexTextField.setText("");
		selectedPatient = null;
		dataPatient.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), 
						MessageBundle.getMessage("angal.patvac.datapatient")));
	}

	/**
	 * This method sets patient's additional data
	 */
	private void setPatient(Patient selectedPatient) {
		patTextField.setText(selectedPatient.getName());
		ageTextField.setText(selectedPatient.getAge() + "");
		sexTextField.setText(selectedPatient.getSex() + "");
		dataPatient.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), 
						MessageBundle.formatMessage("angal.patvac.patientcode.fmt.msg", selectedPatient.getCode())));
	}

	/**
	 * This method initializes patientComboBox. It is used to display available
	 * patients
	 * 
	 * @return patientComboBox (JComboBox)
	 */
	private JComboBox<Object> getPatientComboBox(String regExp) {
		if (patientComboBox == null) {
			patientComboBox = new JComboBox<Object>();
		}
		patientComboBox.addItem(MessageBundle.getMessage("angal.patvac.selectapatient"));
		Patient patSelected = null;

		if (GeneralData.ENHANCEDSEARCH) {
			try {
				patientList = patientManager.getPatientsByOneOfFieldsLike(regExp);
			} catch (OHServiceException ex) {
				OHServiceExceptionUtil.showMessages(ex);
				patientList = new ArrayList<>();
			}
		}else{
			try {
				patientList = patientManager.getPatient();
			} catch (OHServiceException e) {
                OHServiceExceptionUtil.showMessages(e);
			}
		}
		if (patientList != null) {
			for (Patient elem : patientList) {
				if (!insert) {
					if (elem.getCode().equals(patVac.getPatient().getCode())) {
						patSelected = elem;
					}
				}
				patientComboBox.addItem(elem);
			}
		}
		if (patSelected != null) {
			patientComboBox.setSelectedItem(patSelected);
			selectedPatient = (Patient) patientComboBox.getSelectedItem();
		} else {
			if (patientComboBox.getItemCount() > 0 && GeneralData.ENHANCEDSEARCH) {
				if (patientComboBox.getItemAt(0) instanceof Patient) {
					selectedPatient = (Patient) patientComboBox.getItemAt(0);
					setPatient(selectedPatient);
				} else
					selectedPatient = null;
			} else
				selectedPatient = null;
		}
		patientComboBox.addActionListener(e -> {
				if (patientComboBox.getSelectedIndex() > 0) {
					selectedPatient = (Patient) patientComboBox.getSelectedItem();
					setPatient(selectedPatient);
				} else {
					selectedPatient = null;
					resetPatVacPat();
				}
		});

		return patientComboBox;
	}

	/**
	 * This method initializes dataPatient. This panel contains patient's data
	 * 
	 * @return dataPatient (JPanel)
	 */
	private JPanel getDataPatient() {
		if (dataPatient == null) {
			dataPatient = new JPanel();
			dataPatient.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), 
							MessageBundle.getMessage("angal.patvac.datapatient")));
			
			JLabel nameLabel = new JLabel(MessageBundle.getMessage("angal.common.name.txt"));
			dataPatient.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			dataPatient.add(nameLabel);
			dataPatient.add(getPatientTextField());
			
			JLabel ageLabel = new JLabel(MessageBundle.getMessage("angal.common.age.txt"));
			dataPatient.add(ageLabel);
			dataPatient.add(getAgeTextField());
			
			JLabel sexLabel = new JLabel(MessageBundle.getMessage("angal.patvac.sex"));
			dataPatient.add(sexLabel);
			dataPatient.add(getSexTextField());
			
			patTextField.setEditable(false);
			ageTextField.setEditable(false);
			sexTextField.setEditable(false);
		}
		return dataPatient;
	}

	/**
	 * This method initializes getPatientTextField about patient name
	 * 
	 * @return patTextField (VoLimitedTextField)
	 */
	private VoLimitedTextField getPatientTextField() {
		if (patTextField == null) {
			patTextField = new VoLimitedTextField(100, 15);
			if (!insert) {
				patTextField.setText(patVac.getPatName());
			}
		}
		return patTextField;
	}

	/**
	 * This method initializes getAgeTextField about patient
	 * 
	 * @return ageTextField (VoLimitedTextField)
	 */
	private VoLimitedTextField getAgeTextField() {
		if (ageTextField == null) {
			ageTextField = new VoLimitedTextField(3, 3);
			if (insert) {
				ageTextField.setText("");
			} else {
				try {
					int intAge = patVac.getPatAge();
					ageTextField.setText(String.valueOf(intAge));
				} catch (Exception e) {
					ageTextField.setText("");
				}
			}
		}
		return ageTextField;
	}

	/**
	 * This method initializes getSexTextField about patient
	 * 
	 * @return sexTextField (VoLimitedTextField)
	 */
	private VoLimitedTextField getSexTextField() {
		if (sexTextField == null) {
			sexTextField = new VoLimitedTextField(1, 1);
			if (!insert) {
				sexTextField.setText("" + patVac.getPatSex());
			}
		}
		return sexTextField;
	}

	/**
	 * This method initializes buttonPanel, that contains the buttons of the
	 * frame (on the bottom)
	 * 
	 * @return buttonPanel (JPanel)
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getOkButton(), null);
			buttonPanel.add(getCancelButton(), null);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes okButton. It is used to update db data
	 * 
	 * @return okButton (JPanel)
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(e -> {

					GregorianCalendar gregDate = new GregorianCalendar();
					gregDate.setTime(vaccineDateFieldCal.getDate());
                    patVac.setProgr(Integer.parseInt(progrTextField.getText()));

					// check on patient
					if (selectedPatient == null) {
						MessageDialog.error(null, "angal.common.pleaseselectapatient.msg");
						return;
					}

					patVac.setVaccineDate(gregDate);
					patVac.setVaccine((Vaccine) vaccineComboBox.getSelectedItem());
					patVac.setPatient(selectedPatient);
					patVac.setLock(0);

					boolean result;
					// handling db insert/update
					if (insert) {
						try {
							result = patientVaccineManager.newPatientVaccine(patVac);
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1);
							return;
						}
					} else {
						try {
							result = patientVaccineManager.updatePatientVaccine(patVac);
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1);
							return;
						}
					}

					if (!result) {
						MessageDialog.error(null, "angal.patvac.thedatacouldnobesaved");
						return;
					} else {
						dispose();
					}
			});
		}
		return okButton;
	}

	/**
	 * This method initializes cancelButton.
	 * 
	 * @return cancelButton (JPanel)
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(e -> {
					dispose();
			});
		}
		return cancelButton;
	}
	private JPanel getCenterPanel() {
		if (centerPanel == null) {
			centerPanel = new JPanel();
			centerPanel.setLayout(new BorderLayout());
			centerPanel.add(getDataPanel(), BorderLayout.CENTER);
			centerPanel.add(getDataPatient(), BorderLayout.SOUTH);
		}
		return centerPanel;
	}
	private JPanel getPatientPanel() {
		if (patientPanel == null) {
			patientPanel = new JPanel();
			GridBagLayout gbl_patientPanel = new GridBagLayout();
			gbl_patientPanel.columnWidths = new int[]{0};
			gbl_patientPanel.rowHeights = new int[]{0};
			gbl_patientPanel.columnWeights = new double[]{Double.MIN_VALUE};
			gbl_patientPanel.rowWeights = new double[]{Double.MIN_VALUE};
			patientPanel.setLayout(gbl_patientPanel);
		}
		return patientPanel;
	}
}
