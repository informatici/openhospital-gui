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
import java.time.LocalDate;
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
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.patvac.manager.PatVacManager;
import org.isf.patvac.model.PatientVaccine;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.RememberDates;
import org.isf.utils.time.TimeTools;
import org.isf.vaccine.manager.VaccineBrowserManager;
import org.isf.vaccine.model.Vaccine;
import org.isf.vactype.manager.VaccineTypeBrowserManager;
import org.isf.vactype.model.VaccineType;

/**
 * PatVacEdit - edit (new/update) a patient's vaccine
 */
public class PatVacEdit extends JDialog {

	private static final long serialVersionUID = -4271389493861772053L;
	private boolean insert;

	private PatientVaccine patVac;
	private JPanel jContentPane;
	private JPanel buttonPanel;
	private JPanel dataPanel;
	private JPanel dataPatient;

	private JButton okButton;
	private JButton cancelButton;
	private JButton jSearchButton;

	private JComboBox<Vaccine> vaccineComboBox;
	private JComboBox<Object> patientComboBox;
	private JComboBox<VaccineType> vaccineTypeComboBox;

	private VoLimitedTextField patTextField;
	private VoLimitedTextField ageTextField;
	private VoLimitedTextField sexTextField;
	private VoLimitedTextField progrTextField;

	private JTextField jTextPatientSrc;
	private Patient selectedPatient;
	private String lastKey;
	private String s;
	private List<Patient> patientList;
	private GoodDateChooser vaccineDateFieldCal;
	private int patNextYProg;

	private JPanel centerPanel;

	private VaccineBrowserManager vaccineBrowserManager = Context.getApplicationContext().getBean(VaccineBrowserManager.class);
	private PatVacManager patVacManager = Context.getApplicationContext().getBean(PatVacManager.class);
	private VaccineTypeBrowserManager vaccineTypeBrowserManager = Context.getApplicationContext().getBean(VaccineTypeBrowserManager.class);
	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);

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
			return patVacManager.getProgYear(0);
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
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
	 * This method initializes dataPanel. This panel contains all items (comboboxes, calendar) to define a vaccine
	 * 
	 * @return dataPanel (JPanel)
	 */
	private JPanel getDataPanel() {
		if (dataPanel == null) {
			// initialize data panel
			dataPanel = new JPanel();
			GridBagLayout gblDataPanel = new GridBagLayout();
			gblDataPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0};
			dataPanel.setLayout(gblDataPanel);

			//patient search fields
			JLabel patientLabel = new JLabel(MessageBundle.getMessage("angal.patvac.searchpatient"));
			GridBagConstraints gbcPatientLabel = new GridBagConstraints();
			gbcPatientLabel.anchor = GridBagConstraints.WEST;
			gbcPatientLabel.fill = GridBagConstraints.VERTICAL;
			gbcPatientLabel.insets = new Insets(5, 5, 5, 5);
			gbcPatientLabel.gridx = 0;
			gbcPatientLabel.gridy = 1;
			dataPanel.add(patientLabel, gbcPatientLabel);
			
			GridBagConstraints gbcTextFieldSearchPatient = new GridBagConstraints();
			gbcTextFieldSearchPatient.fill = GridBagConstraints.BOTH;
			gbcTextFieldSearchPatient.insets = new Insets(5, 5, 5, 5);
			gbcTextFieldSearchPatient.gridx = 1;
			gbcTextFieldSearchPatient.gridy = 1;
			dataPanel.add(getJTextFieldSearchPatient(), gbcTextFieldSearchPatient);
			
			if (GeneralData.ENHANCEDSEARCH) {
				
				GridBagConstraints gbcSearchButton = new GridBagConstraints();
				gbcSearchButton.insets = new Insets(5, 5, 5, 5);
				gbcSearchButton.anchor = GridBagConstraints.WEST;
				gbcSearchButton.gridx = 2;
				gbcSearchButton.gridy = 1;
				dataPanel.add(getJSearchButton(), gbcSearchButton);
			}
			
			GridBagConstraints gbcPatientComboBox = new GridBagConstraints();
			gbcPatientComboBox.gridwidth = 2;
			gbcPatientComboBox.fill = GridBagConstraints.HORIZONTAL;
			gbcPatientComboBox.insets = new Insets(5, 5, 5, 5);
			gbcPatientComboBox.gridx = 3;
			gbcPatientComboBox.gridy = 1;
			dataPanel.add(getPatientComboBox(s), gbcPatientComboBox);
			
			if (!insert) {
				patientComboBox.setEnabled(false);
				jTextPatientSrc.setEnabled(false);
			}

			// vaccine date
			JLabel vaccineDateLabel = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
			GridBagConstraints gbcVaccineDateLabel = new GridBagConstraints();
			gbcVaccineDateLabel.anchor = GridBagConstraints.NORTHWEST;
			gbcVaccineDateLabel.insets = new Insets(5, 5, 5, 5);
			gbcVaccineDateLabel.gridx = 0;
			gbcVaccineDateLabel.gridy = 0;
			dataPanel.add(vaccineDateLabel, gbcVaccineDateLabel);
			vaccineDateFieldCal = getVaccineDateFieldCal();
			GridBagConstraints gbcVaccineDateFieldCal = new GridBagConstraints();
			gbcVaccineDateFieldCal.anchor = GridBagConstraints.WEST;
			gbcVaccineDateFieldCal.insets = new Insets(5, 5, 5, 5);
			gbcVaccineDateFieldCal.gridx = 1;
			gbcVaccineDateFieldCal.gridy = 0;
			dataPanel.add(vaccineDateFieldCal, gbcVaccineDateFieldCal);

			// progressive
			JLabel progrLabel = new JLabel(MessageBundle.getMessage("angal.patvac.progressive"));
			GridBagConstraints gbcProgrLabel = new GridBagConstraints();
			gbcProgrLabel.anchor = GridBagConstraints.NORTHEAST;
			gbcProgrLabel.insets = new Insets(5, 5, 5, 5);
			gbcProgrLabel.gridx = 3;
			gbcProgrLabel.gridy = 0;
			dataPanel.add(progrLabel, gbcProgrLabel);
			GridBagConstraints gbcProgrTextField = new GridBagConstraints();
			gbcProgrTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcProgrTextField.anchor = GridBagConstraints.NORTHEAST;
			gbcProgrTextField.insets = new Insets(5, 5, 5, 5);
			gbcProgrTextField.gridx = 4;
			gbcProgrTextField.gridy = 0;
			dataPanel.add(getProgrTextField(), gbcProgrTextField);

			// vaccineType combo box
			JLabel vaccineTypeLabel = new JLabel(MessageBundle.getMessage("angal.patvac.vaccinetype"));
			GridBagConstraints gbcVaccineTypeLabel = new GridBagConstraints();
			gbcVaccineTypeLabel.anchor = GridBagConstraints.WEST;
			gbcVaccineTypeLabel.fill = GridBagConstraints.VERTICAL;
			gbcVaccineTypeLabel.insets = new Insets(5, 5, 5, 5);
			gbcVaccineTypeLabel.gridx = 0;
			gbcVaccineTypeLabel.gridy = 2;
			dataPanel.add(vaccineTypeLabel, gbcVaccineTypeLabel);
			GridBagConstraints gbcVaccineTypeComboBox = new GridBagConstraints();
			gbcVaccineTypeComboBox.fill = GridBagConstraints.BOTH;
			gbcVaccineTypeComboBox.insets = new Insets(5, 5, 5, 5);
			gbcVaccineTypeComboBox.gridwidth = 4;
			gbcVaccineTypeComboBox.gridx = 1;
			gbcVaccineTypeComboBox.gridy = 2;
			dataPanel.add(getVaccineTypeComboBox(), gbcVaccineTypeComboBox);

			// vaccine combo box
			JLabel vaccineLabel = new JLabel(MessageBundle.getMessage("angal.patvac.vaccine"));
			GridBagConstraints gbcVaccineLabel = new GridBagConstraints();
			gbcVaccineLabel.anchor = GridBagConstraints.WEST;
			gbcVaccineLabel.fill = GridBagConstraints.VERTICAL;
			gbcVaccineLabel.insets = new Insets(5, 5, 5, 5);
			gbcVaccineLabel.gridx = 0;
			gbcVaccineLabel.gridy = 3;
			dataPanel.add(vaccineLabel, gbcVaccineLabel);
			GridBagConstraints gbcVaccineComboBox = new GridBagConstraints();
			gbcVaccineComboBox.fill = GridBagConstraints.BOTH;
			gbcVaccineComboBox.insets = new Insets(5, 5, 5, 5);
			gbcVaccineComboBox.gridwidth = 4;
			gbcVaccineComboBox.gridx = 1;
			gbcVaccineComboBox.gridy = 3;
			dataPanel.add(getVaccineComboBox(), gbcVaccineComboBox);
		}
		return dataPanel;
	}

	private JTextField getJTextFieldSearchPatient() {
		jTextPatientSrc = new JTextField();
		if (GeneralData.ENHANCEDSEARCH) {
			jTextPatientSrc.addKeyListener(new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						jSearchButton.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});
		} else {
			jTextPatientSrc.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e) {
					lastKey = "";
					String s = String.valueOf(e.getKeyChar());
					if (Character.isLetterOrDigit(e.getKeyChar())) {
						lastKey = s;
					}
					s = jTextPatientSrc.getText() + lastKey;
					s = s.trim();
					filterPatient(s);
				}

				@Override
				public void keyPressed(KeyEvent e) {
				}

				@Override
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
			jSearchButton.addActionListener(actionEvent -> {
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
	 * @return GoodDateChooser
	 */
	private GoodDateChooser getVaccineDateFieldCal() {
		LocalDateTime dateIn;
		if (insert) {
			dateIn = RememberDates.getLastPatientVaccineDate();
		} else {
			dateIn = patVac.getVaccineDate();
		}
		if (dateIn == null) {
			dateIn = TimeTools.getNow();
		}
		return new GoodDateChooser(dateIn.toLocalDate());
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
			vaccineTypeComboBox = new JComboBox<>();
			vaccineTypeComboBox.setPreferredSize(new Dimension(200, 30));
			vaccineTypeComboBox.addItem(new VaccineType("", MessageBundle.getMessage("angal.patvac.allvaccinetype")));

			List<VaccineType> types = null;
			try {
				types = vaccineTypeBrowserManager.getVaccineType();
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
			VaccineType vaccineTypeSel = null;
			if (types != null) {
				for (VaccineType elem : types) {
					vaccineTypeComboBox.addItem(elem);
					if (!insert && elem.getCode() != null) {
						if (elem.getCode().equalsIgnoreCase(patVac.getVaccine().getVaccineType().getCode())) {
							vaccineTypeSel = elem;
						}
					}
				}
			}
			if (vaccineTypeSel != null) {
				vaccineTypeComboBox.setSelectedItem(vaccineTypeSel);
			}

			vaccineTypeComboBox.addActionListener(actionEvent -> {
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
			vaccineComboBox = new JComboBox<>();
			vaccineComboBox.setPreferredSize(new Dimension(200, 30));
		}
		vaccineComboBox.addItem(new Vaccine("", MessageBundle.getMessage("angal.patvac.allvaccine"), new VaccineType("", "")));
		List<Vaccine> allVac = null;
		try {
			if (((VaccineType) vaccineTypeComboBox.getSelectedItem()).getDescription().equals(MessageBundle.getMessage("angal.patvac.allvaccinetype"))) {
				allVac = vaccineBrowserManager.getVaccine();
			} else {
				allVac = vaccineBrowserManager.getVaccine(((VaccineType) vaccineTypeComboBox.getSelectedItem()).getCode());
			}
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		Vaccine vaccineSel = null;
		if (allVac != null) {
			for (Vaccine elem : allVac) {
				if (!insert && elem.getCode() != null) {
					if (elem.getCode().equalsIgnoreCase(patVac.getVaccine().getCode())) {
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
				sbName.append(elem.getSecondName());
				sbName.append(elem.getFirstName());
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
			} else {
				selectedPatient = null;
			}
		} else {
			selectedPatient = null;
		}
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
		ageTextField.setText(String.valueOf(selectedPatient.getAge()));
		sexTextField.setText(String.valueOf(selectedPatient.getSex()));
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
			patientComboBox = new JComboBox<>();
		}
		patientComboBox.addItem(MessageBundle.getMessage("angal.patvac.selectapatient"));
		Patient patSelected = null;

		if (GeneralData.ENHANCEDSEARCH) {
			try {
				patientList = patientBrowserManager.getPatientsByOneOfFieldsLike(regExp);
			} catch (OHServiceException ex) {
				OHServiceExceptionUtil.showMessages(ex);
				patientList = new ArrayList<>();
			}
		} else {
			try {
				patientList = patientBrowserManager.getPatient();
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
				} else {
					selectedPatient = null;
				}
			} else {
				selectedPatient = null;
			}
		}
		patientComboBox.addActionListener(actionEvent -> {
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
				sexTextField.setText(String.valueOf(patVac.getPatSex()));
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
			okButton.addActionListener(actionEvent -> {

				// check on patient
				if (selectedPatient == null) {
					MessageDialog.error(null, "angal.common.pleaseselectapatient.msg");
					return;
				}

				LocalDate vaccineDate = vaccineDateFieldCal.getDate();
				patVac.setProgr(Integer.parseInt(progrTextField.getText()));
				patVac.setVaccineDate(vaccineDate.atStartOfDay());
				patVac.setVaccine((Vaccine) vaccineComboBox.getSelectedItem());
				patVac.setPatient(selectedPatient);
				patVac.setLock(0);

				// handling db insert/update
				try {
					if (insert) {
						patVacManager.newPatientVaccine(patVac);
					} else {
						patVacManager.updatePatientVaccine(patVac);
					}
					dispose();
				} catch (OHServiceException e1) {
					MessageDialog.error(null, "angal.patvac.thedatacouldnobesaved");
					OHServiceExceptionUtil.showMessages(e1);
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
			cancelButton.addActionListener(actionEvent -> dispose());
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

}
