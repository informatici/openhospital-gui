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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.time.LocalDate;
import java.util.EventListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
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
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.patient.manager.PatientBrowserManager;
import org.isf.patient.model.Patient;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;

public class PatientInsert extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private EventListenerList patientListeners = new EventListenerList();

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

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = patientListeners.getListeners(PatientListener.class);
		for (EventListener listener : listeners) {
			((PatientListener) listener).patientInserted(event);
		}
	}

	private void firePatientUpdated(Patient aPatient) {
		AWTEvent event = new AWTEvent(aPatient, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = patientListeners.getListeners(PatientListener.class);
		for (EventListener listener : listeners) {
			((PatientListener) listener).patientUpdated(event);
		}
	}

	private JPanel jContainPanel;
	private JPanel jDataPanel;
	private JPanel jButtonPanel;
	private JButton jOkButton;
	private JButton jCancelButton;
	private JTextField ageField;
	private Integer age = 0;
	private JPanel jAgePanel;
	private JTextField jFirstNameTextField;
	private JPanel jSecondNamePanel;
	private JTextField jSecondNameTextField;
	private JPanel sexPanel;
	private String sexSelect = " ";
	private char sex = 'M';
	private boolean insert;
	private Patient patient;
	private JPanel jAddressPanel;
	private JTextField jAddressTextField;
	private JPanel jCityPanel;
	private JTextField jCityTextField;
	private JPanel jTelPanel;
	private JTextField jTelephoneTextField;
	private JPanel jNextKinPanel;
	private JTextField jNextKinTextField;
	//	private int oldAge;
	private PatientBrowserManager patientBrowserManager = Context.getApplicationContext().getBean(PatientBrowserManager.class);
	private JLabel jLabel1;
	private JLabel jLabel;
	private JLabel jAgeLabel;
	private JPanel jFirstNamePanel;
	private JPanel jLabelPanel;
	private JPanel jSexLabelPanel;
	private JPanel jSecondNamePanel1;
	private JPanel jAgePanel1;
	private JPanel jFirstName;
	private JPanel jPanel1;
	private JPanel jSecondName;
	private JPanel jAge;
	private JPanel jPanel;
	private JPanel jPanel2;
	private JPanel jPanel3;
	private JPanel jPanel5;
	private JPanel jAddress;
	private JPanel jCity;
	private JPanel jNextKin;
	private JPanel jTelephone;
	private JPanel jDataContainPanel;
	private JTextArea jNoteTextArea;
	private JPanel jNotePanel;
	private JScrollPane jNoteScrollPane;

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		sexSelect = actionEvent.getActionCommand();
	}
	
	/**
	 * This method initializes 
	 */
	public PatientInsert(JDialog owner, Patient old, boolean inserting) {
		super(owner, true);
		patient = old;
		insert = inserting;
		initialize();
	}

	public PatientInsert(JFrame owner, Patient old, boolean inserting) {
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
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJDataPanel(), BorderLayout.NORTH);
			jContainPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
		}
		return jContainPanel;
	}
	
	/**
	 * This method initializes jDataPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJDataPanel() {
		if (jDataPanel == null) {
			jDataPanel = new JPanel();
			jDataPanel.setLayout(new BoxLayout(getJDataPanel(), BoxLayout.Y_AXIS));
			jDataPanel.add(getJDataContainPanel(), null);
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
			jButtonPanel.add(getJCancelButton(), null);
		}
		return jButtonPanel;
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
				LocalDate bdate = LocalDate.now();

				if (insert) {
					if (jFirstNameTextField.getText().isEmpty()) {
						MessageDialog.error(null, "angal.patient.insertfirstname.msg");
					} else {
						if (jSecondNameTextField.getText().isEmpty()) {
							MessageDialog.error(null, "angal.patient.insertsecondname.msg");
						} else {
							if (age == -1) {
								MessageDialog.error(null, "angal.patient.insertvalidage.msg");
							} else {
								bdate = bdate.minusYears(age);
								String name = jFirstNameTextField.getText() + ' ' + jSecondNameTextField.getText();
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
								if (ok) {
									patient.setFirstName(jFirstNameTextField.getText());
									patient.setSecondName(jSecondNameTextField.getText());
									patient.setAge(age);

									if (sexSelect.equals(MessageBundle.getMessage("angal.common.female.txt"))) {
										sex = 'F';
									} else {
										sex = 'M';
									}
									patient.setSex(sex);
									patient.setAddress(jAddressTextField.getText());
									patient.setCity(jCityTextField.getText());
									patient.setNextKin(jNextKinTextField.getText());
									patient.setTelephone(jTelephoneTextField.getText());
									patient.setNote(jNoteTextArea.getText());

									//PatientExtended Compatibility
									patient.setBirthDate(bdate);
									patient.setAgetype("");
									patient.setMotherName("");
									patient.setMother('U');
									patient.setFatherName("");
									patient.setFather('U');
									patient.setBloodType("");
									patient.setHasInsurance('U');
									patient.setParentTogether('U');

									try {
										patient = patientBrowserManager.savePatient(patient);
										firePatientInserted(patient);
										dispose();
									} catch (OHServiceException ex) {
										OHServiceExceptionUtil.showMessages(ex);
										MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
									}
								}
							}
						}
					}
				} else { //Update
					String name = jFirstNameTextField.getText() + ' ' + jSecondNameTextField.getText();
					if (!patient.getName().equals(name)) {
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
					} else {
						ok = true;
					}
					if (ok) {

						patient.setFirstName(jFirstNameTextField.getText());
						patient.setSecondName(jSecondNameTextField.getText());
						patient.setAge(age);
						if (sexSelect.equals(" ")) {
							sex = patient.getSex();
						} else if (sexSelect.equals(MessageBundle.getMessage("angal.common.female.txt"))) {
							sex = 'F';
						} else {
							sex = 'M';
						}
						patient.setSex(sex);
						patient.setAddress(jAddressTextField.getText());
						patient.setCity(jCityTextField.getText());
						patient.setNextKin(jNextKinTextField.getText());
						patient.setTelephone(jTelephoneTextField.getText());
						patient.setNote(jNoteTextArea.getText());

						try {
							patient = patientBrowserManager.savePatient(patient);
							firePatientUpdated(patient);
							dispose();
						} catch (OHServiceException ex) {
							OHServiceExceptionUtil.showMessages(ex);
							MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
						}
					}
				}
			});
		}
		return jOkButton;
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
	 * This method initializes ageField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getAgeField() {
		if (ageField == null) {
			ageField = new JTextField(15);
			ageField.setText("0");
			ageField.setMaximumSize(new Dimension(20, 50));
			if (insert) {
				age = -1;
				ageField.setText("");
			} else {
				ageField.setText(String.valueOf(patient.getAge()));
				age = patient.getAge();
			}
			ageField.setMinimumSize(new Dimension(100, 50));
		}
		ageField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				try {				
					age = Integer.parseInt(ageField.getText());
					if (age < 0 || age > 120) {
						ageField.setText("0");
						MessageDialog.error(null, "angal.patient.insertvalidage.msg");
					}
				} catch (NumberFormatException ex) {
					MessageDialog.error(null, "angal.patient.insertvalidage.msg");
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		return ageField;
	}
	
	/**
	 * This method initializes jAgePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJAgePanel() {
		if (jAgePanel == null) {
			jAgePanel = new JPanel();
			jAgePanel.add(getJAgeLabel(), BorderLayout.EAST);
		}
		return jAgePanel;
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
		if (jSecondNamePanel == null) {
			jSecondNamePanel = new JPanel();
			jSecondNamePanel.add(getJLabel(), BorderLayout.EAST);
		}
		return jSecondNamePanel;
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
		if (sexPanel == null) {			
			sexPanel = new JPanel();
			ButtonGroup sexGroup = new ButtonGroup();
			JRadioButton radiom= new JRadioButton(MessageBundle.getMessage("angal.common.male.btn"));
			JRadioButton radiof= new JRadioButton(MessageBundle.getMessage("angal.common.female.btn"));
			sexPanel.add(getJSexLabelPanel(), null);
			sexPanel.add(radiom, radiom.getName());
			if (insert) {
				radiom.setSelected(true);
			} else {
				if (patient.getSex() == 'F') {
					radiof.setSelected(true);
				} else {
					radiom.setSelected(true);
				}
			}			
			radiom.addActionListener(this);
			radiof.addActionListener(this);
			sexGroup.add(radiom);
			sexGroup.add(radiof);
			sexPanel.add(radiof);
		}
		return sexPanel;
	}
	
	/**
	 * This method initializes jAddressPanel
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJAddressPanel() {
		if (jAddressPanel == null) {
			JLabel jAddressLabel = new JLabel(MessageBundle.getMessage("angal.common.address.txt"));
			jAddressPanel = new JPanel();
			jAddressPanel.add(jAddressLabel, BorderLayout.EAST);
		}
		return jAddressPanel;
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
	 * This method initializes jCityPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJCityPanel() {
		if (jCityPanel == null) {
			JLabel jCityLabel = new JLabel(MessageBundle.getMessage("angal.common.city.txt"));
			jCityPanel = new JPanel();		
			jCityPanel.add(jCityLabel, BorderLayout.EAST);
		}
		return jCityPanel;
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
		if (jTelPanel == null) {
			JLabel jTelLabel = new JLabel(MessageBundle.getMessage("angal.common.telephone.txt"));
			jTelPanel = new JPanel();				
			jTelPanel.add(jTelLabel,  BorderLayout.EAST);
		}
		return jTelPanel;
	}
	
	/**
	 * This method initializes jTelephoneTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTelephoneTextField() {
		if (jTelephoneTextField == null) {
			jTelephoneTextField = new JTextField(15);
			if (!insert) {
				jTelephoneTextField.setText(patient.getTelephone());
			}
		}
		return jTelephoneTextField;
	}
	
	/**
	 * This method initializes jNextKidPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJNextKinPanel() {
		if (jNextKinPanel == null) {
			JLabel jNextKinLabel = new JLabel(MessageBundle.getMessage("angal.patient.nextkin"));
			jNextKinPanel = new JPanel();			
			jNextKinPanel.add(jNextKinLabel, BorderLayout.EAST);
		}
		return jNextKinPanel;
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
	 * This method initializes jLabel1	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getJLabel1() {
		if (jLabel1 == null) {
			jLabel1 = new JLabel(MessageBundle.getMessage("angal.patient.firstname"));
		}
		return jLabel1;
	}

	/**
	 * This method initializes jLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getJLabel() {
		if (jLabel == null) {
			jLabel = new JLabel(MessageBundle.getMessage("angal.patient.secondname"));
		}
		return jLabel;
	}

	/**
	 * This method initializes jAgeLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getJAgeLabel() {
		if (jAgeLabel == null) {
			jAgeLabel = new JLabel(MessageBundle.getMessage("angal.common.age.txt") + " *");
		}
		return jAgeLabel;
	}

	/**
	 * This method initializes jFirstNamePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJFirstNamePanel() {
		if (jFirstNamePanel == null) {
			jFirstNamePanel = new JPanel();
			jFirstNamePanel.add(getJLabel1(), BorderLayout.EAST);
		}
		return jFirstNamePanel;
	}

	/**
	 * This method initializes jLabelPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJLabelPanel() {
		if (jLabelPanel == null) {
			jLabelPanel = new JPanel();
			jLabelPanel.setLayout(new BoxLayout(getJLabelPanel(), BoxLayout.Y_AXIS));
			jLabelPanel = setMyBorder(jLabelPanel, "");
			jLabelPanel.add(getJFirstName(), null);
			jLabelPanel.add(getJSecondName(), null);
			jLabelPanel.add(getJAge(), null);
			jLabelPanel.add(getSexPanel(), null);
			jLabelPanel.add(getJAddress(), null);
			jLabelPanel.add(getJCity(), null);
			jLabelPanel.add(getJNextKin(), null);
			jLabelPanel.add(getJTelephone(), null);
		}
		return jLabelPanel;
	}

	/**
	 * This method initializes jSexLabelPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJSexLabelPanel() {
		if (jSexLabelPanel == null) {
			JLabel jLabel2 = new JLabel(MessageBundle.getMessage("angal.patient.sexstar"));
			jSexLabelPanel = new JPanel();
			jSexLabelPanel.setLayout(new BorderLayout());
			
			jSexLabelPanel.add(jLabel2, BorderLayout.EAST);
		}
		return jSexLabelPanel;
	}

	/**
	 * This method initializes jSecondNamePanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJSecondNamePanel1() {
		if (jSecondNamePanel1 == null) {
			jSecondNamePanel1 = new JPanel();
			jSecondNamePanel1.add(getJSecondNameTextField(), null);
		}
		return jSecondNamePanel1;
	}

	/**
	 * This method initializes jAgePanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJAgePanel1() {
		if (jAgePanel1 == null) {
			jAgePanel1 = new JPanel();
			jAgePanel1.add(getAgeField(), null);
		}
		return jAgePanel1;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJFirstName() {
		if (jFirstName == null) {
			jFirstName = new JPanel();
			jFirstName.setLayout(new BorderLayout());
			jFirstName.add(getJFirstNamePanel(), BorderLayout.WEST);
			jFirstName.add(getJPanel1(), BorderLayout.EAST);
		}
		return jFirstName;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.add(getJFirstNameTextField(), null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel2	
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
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJAge() {
		if (jAge == null) {
			jAge = new JPanel();
			jAge.setLayout(new BorderLayout());
			jAge.add(getJAgePanel(), BorderLayout.WEST);
			jAge.add(getJAgePanel1(), BorderLayout.EAST);
		}
		return jAge;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.add(getJAddressTextField(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.add(getJCityTextField(), null);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jPanel3 = new JPanel();
			jPanel3.add(getJNextKinTextField(), null);
		}
		return jPanel3;
	}

	/**
	 * This method initializes jPanel5	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel5() {
		if (jPanel5 == null) {
			jPanel5 = new JPanel();
			jPanel5.add(getJTelephoneTextField(), null);
		}
		return jPanel5;
	}

	/**
	 * This method initializes jAddress
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJAddress() {
		if (jAddress == null) {
			jAddress = new JPanel();
			jAddress.setLayout(new BorderLayout());
			jAddress.add(getJAddressPanel(), BorderLayout.WEST);
			jAddress.add(getJPanel(), BorderLayout.EAST);
			
		}
		return jAddress;
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
			jCity.add(getJCityPanel(), BorderLayout.WEST);
			jCity.add(getJPanel2(), BorderLayout.EAST);
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
			jNextKin.add(getJNextKinPanel(), BorderLayout.WEST);
			jNextKin.add(getJPanel3(), BorderLayout.EAST);
		}
		return jNextKin;
	}

	/**
	 * This method initializes jPanel6	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJTelephone() {
		if (jTelephone == null) {
			jTelephone = new JPanel();
			jTelephone.setLayout(new BorderLayout());
			jTelephone.add(getJTelPanel(), BorderLayout.WEST);
			jTelephone.add(getJPanel5(), BorderLayout.EAST);
		}
		return jTelephone;
	}

	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJDataContainPanel() {
		if (jDataContainPanel == null) {
			jDataContainPanel = new JPanel();
			if (!insert) {
				jDataContainPanel = setMyBorderCenter(jDataContainPanel, patient.getName());
			} else {
				jDataContainPanel = setMyBorderCenter(jDataContainPanel, MessageBundle.getMessage("angal.patient.insertdataofnewpatient"));
			}
			jDataContainPanel.setLayout(new BorderLayout());
			jDataContainPanel.add(getJLabelPanel(), BorderLayout.CENTER);
			jDataContainPanel.add(getJNotePanel(), BorderLayout.EAST);
		}
		return jDataContainPanel;
	}

	/**
	 * Set a specific border+title to a panel
	 */
	private JPanel setMyBorder(JPanel c, String title) {
		Border b1 = BorderFactory.createLineBorder(Color.lightGray);

		Border b2 = BorderFactory.createTitledBorder(b1, title,
				TitledBorder.LEFT, TitledBorder.TOP);

		c.setBorder(b2);
		return c;
	}

	private JPanel setMyBorderCenter(JPanel c, String title) {
		Border b1 = BorderFactory.createLineBorder(Color.lightGray);

		Border b2 = BorderFactory.createTitledBorder(b1, title,
				TitledBorder.CENTER, TitledBorder.TOP);

		c.setBorder(b2);
		return c;
	}

	/**
	 * This method initializes jTextPane	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
	private JTextArea getJTextArea() {
		if (jNoteTextArea == null) {
			jNoteTextArea = new JTextArea(40, 30);
			if (!insert) {
				jNoteTextArea.setText(patient.getNote());
			}
			jNoteTextArea.setLineWrap(true);
			jNoteTextArea.setPreferredSize(new Dimension(jNoteTextArea.getSize()));
			jNoteTextArea.setAutoscrolls(true);
		}
		return jNoteTextArea;
	}

	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJNotePanel() {
		if (jNoteScrollPane == null && jNotePanel == null) {
			jNoteScrollPane = new JScrollPane(getJTextArea());

			jNoteScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			jNoteScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			jNoteScrollPane.createVerticalScrollBar();
			jNoteScrollPane.setAutoscrolls(true);
			jNoteScrollPane.setPreferredSize(new Dimension(200, 350));
			jNoteScrollPane.validate();

			jNotePanel = new JPanel();
			jNotePanel = setMyBorder(jNotePanel, MessageBundle.getMessage("angal.patient.note"));
			jNotePanel.add(jNoteScrollPane);
		}
		return jNotePanel;
	}

}
