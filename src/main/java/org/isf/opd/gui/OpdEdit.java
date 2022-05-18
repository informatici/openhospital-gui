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
package org.isf.opd.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.distype.manager.DiseaseTypeBrowserManager;
import org.isf.distype.model.DiseaseType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.opd.manager.OpdBrowserManager;
import org.isf.opd.model.Opd;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.RememberDates;

/**
 * ------------------------------------------
 * OpdEdit - add/edit an OPD registration
 * -----------------------------------------
 * modification history
 * 11/12/2005 - Vero, Rick  - first beta version
 * 07/11/2006 - ross - renamed from Surgery
 *                   - added visit date, disease 2, disease 3
 *                   - disease is not mandatory if re-attendance
 * 			         - version is now 1.0
 * 28/05/2008 - ross - added referral to / referral from check boxes
 * 			         - version is now 1.1
 * 09/01/2009 - fabrizio - Removed unuseful control on variable dateIn.
 *                         Cosmetic changes to code style.
 * ------------------------------------------
 * */
public class OpdEdit extends JDialog {
	
	private static final long serialVersionUID = -7369841416710920082L;

	private EventListenerList surgeryListeners = new EventListenerList();
	
	public interface SurgeryListener extends EventListener {
		void surgeryUpdated(AWTEvent e, Opd opd);
		void surgeryInserted(AWTEvent e, Opd opd);
	}
	
	public void addSurgeryListener(SurgeryListener l) {
		surgeryListeners.add(SurgeryListener.class, l);
	}
	
	public void removeSurgeryListener(SurgeryListener listener) {
		surgeryListeners.remove(SurgeryListener.class, listener);
	}
	
	private void fireSurgeryInserted(Opd opd) {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = -2831804524718368850L;
		};
		
		EventListener[] listeners = surgeryListeners.getListeners(SurgeryListener.class);
		for (EventListener listener : listeners) {
			((SurgeryListener) listener).surgeryInserted(event, opd);
		}
	}

	private void fireSurgeryUpdated(Opd opd) {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = -1073238832996429931L;
		};
		
		EventListener[] listeners = surgeryListeners.getListeners(SurgeryListener.class);
		for (EventListener listener : listeners) {
			((SurgeryListener) listener).surgeryUpdated(event, opd);
		}
	}
	
	private JPanel insertPanel = null;
	private JPanel principalPanel = null;
	private JPanel jAgePanel = null;
	private JPanel jSexPanel = null;
	private JPanel jNewPatientPanel= null;
	private JPanel jDatePanel= null;
	private JPanel jDiseasePanel = null;
	private JPanel jDiseasePanel2 = null;
	private JPanel jDiseasePanel3 = null;
	private JPanel jDiseaseTypePanel = null;
	private JComboBox<DiseaseType> diseaseTypeBox = null;
	private JComboBox diseaseBox = null;
	private JComboBox diseaseBox2 = null;
	private JComboBox diseaseBox3 = null;
	private GoodDateTimeChooser opdDateField = null;
	private JPanel jPanel2 = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private JCheckBox newPatientCheckBox = null;
	private JCheckBox referralToCheckBox = null;
	private JCheckBox referralFromCheckBox = null;
	private VoLimitedTextField ageField = null;
	private JPanel sexPanel = null;
	private JRadioButton radiof;
	private Integer age = null;
	private Opd opd;
	private boolean insert;
	private char sex;
	private int oldAge;
	private DiseaseType allType = new DiseaseType(
			MessageBundle.getMessage("angal.common.alltypes.txt"),
			MessageBundle.getMessage("angal.common.alltypes.txt")
	);
	
	/*
	 * Managers and Arrays
	 */
	private DiseaseTypeBrowserManager typeManager = Context.getApplicationContext().getBean(DiseaseTypeBrowserManager.class);
	private DiseaseBrowserManager diseaseManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);
	private OpdBrowserManager opdManager = Context.getApplicationContext().getBean(OpdBrowserManager.class);
	private List<DiseaseType> types;
	private List<Disease> diseasesAll;
	
    /*
     * Adds: Textfields and buttons to enable search in diagnosis
     */
    private JTextField searchDiseaseTextField;
    private JTextField searchDiseaseTextField2;
    private JTextField searchDiseaseTextField3;
    private JButton searchDiseaseButton;
    private JButton searchDiseaseButton2;
    private JButton searchDiseaseButton3;
        
	/**
	 * This method initializes
	 */
	public OpdEdit(JFrame owner, Opd old, boolean inserting) {
		super(owner, true);
		opd = old;
		insert = inserting;
		try {
			types = typeManager.getDiseaseType();
			diseasesAll = diseaseManager.getDiseaseAll();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (!insert) {
			oldAge = opd.getAge();
		}
		initialize();
		pack();
	}
	
	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setBounds(100, 50, 450, 500);
		this.setContentPane(getPrincipalPanel());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.opd.newopdregistration.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.opd.editopdregistration.title"));
		}
	}

	/**
	 * This method initializes principalPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPrincipalPanel() {
		if (principalPanel == null) {
			principalPanel = new JPanel();
			principalPanel.setLayout(new BorderLayout());
			principalPanel.add(getInsertPanel(), java.awt.BorderLayout.NORTH);
			principalPanel.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
		}
		return principalPanel;
	}

	
	/**
	 * This method initializes insertPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getInsertPanel() {
		if (insertPanel == null) {
			insertPanel = new JPanel();
			insertPanel.setLayout(new BoxLayout(insertPanel, BoxLayout.Y_AXIS));
			insertPanel.add(getJNewPatientPanel(), null);
			insertPanel.add(getJDatePanel(), null);
			insertPanel.add(getJDiseaseTypePanel(), null);
			insertPanel.add(getDiseaseTypeBox(), null);
			insertPanel.add(getJDiseasePanel(), null);
			insertPanel.add(getDiseaseBox1(), null);
			insertPanel.add(getJDiseasePanel2(), null);
			insertPanel.add(getDiseaseBox2(), null);
			insertPanel.add(getJDiseasePanel3(), null);
			insertPanel.add(getDiseaseBox3(), null);
			insertPanel.add(getJAgePanel(), null);
			insertPanel.add(getJSexPanel(), null);
		}
		return insertPanel;
	}

	/**
	 * This method initializes diseaseTypeBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox<DiseaseType> getDiseaseTypeBox() {
		if (diseaseTypeBox == null) {
			diseaseTypeBox = new JComboBox<>();
			DiseaseType elem2 = null;
			diseaseTypeBox.setMaximumSize(new Dimension(400, 50));
			diseaseTypeBox.addItem(allType);
			if (types != null) {
				for (DiseaseType elem : types) {
					if (!insert && opd.getDisease().getType() != null) {
						if (opd.getDisease().getType().getCode().equals(elem.getCode())) {
							elem2 = elem;
						}
					}
					diseaseTypeBox.addItem(elem);
				}
			}
			if (elem2 != null) {
				diseaseTypeBox.setSelectedItem(elem2);
			} else {
				diseaseTypeBox.setSelectedIndex(0);
			}
			diseaseTypeBox.addActionListener(actionEvent -> {
				diseaseBox.removeAllItems();
				getDiseaseBox1();
			});
		}
		return diseaseTypeBox;
	}
	
	/**
	 * This method initializes diseaseBox
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDiseaseBox1() {
		if (diseaseBox == null) {
			diseaseBox = new JComboBox();
			diseaseBox.setMaximumSize(new Dimension(400, 50));
		}
		Disease thisDiseaseEdit = null;
		List<Disease> diseases = null;
		try {
			if (diseaseTypeBox.getSelectedIndex() == 0) {
				diseases = diseaseManager.getDiseaseOpd();
			} else {
				String code = ((DiseaseType)diseaseTypeBox.getSelectedItem()).getCode();
				diseases = diseaseManager.getDiseaseOpd(code);
			}
		} catch(OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		diseaseBox.addItem("");
		if (diseases != null) {
			for (Disease elem : diseases) {
				diseaseBox.addItem(elem);
				if (!insert && opd.getDisease() != null) {
					if (opd.getDisease().getCode().equals(elem.getCode())) {
						thisDiseaseEdit = elem;}
				}
			}
		}
		if (!insert) {
			if (thisDiseaseEdit != null) {
				diseaseBox.setSelectedItem(thisDiseaseEdit);
			} else { //try in the cancelled diseases
				if (opd.getDisease() != null) {
					for (Disease elem : diseasesAll) {
						if (opd.getDisease().getCode().equals(elem.getCode())) {
							MessageDialog.warning(OpdEdit.this,"angal.opd.disease1mayhavebeencancelled.msg");
							diseaseBox.addItem(elem);
							diseaseBox.setSelectedItem(elem);
						}
					}
				}
			}
		}
		return diseaseBox;
	}

	private JComboBox getDiseaseBox2() {
		if (diseaseBox2 == null) {
			diseaseBox2 = new JComboBox();
			diseaseBox2.setMaximumSize(new Dimension(400, 50));
		}
		Disease elem2=null;
		List<Disease> diseases = null;
		try {
			diseases = diseaseManager.getDiseaseOpd();
		} catch(OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		diseaseBox2.addItem("");
		if (diseases != null) {
			for (Disease elem : diseases) {
				diseaseBox2.addItem(elem);
				if (!insert && opd.getDisease2()!=null) {
					if (opd.getDisease2().getCode().equals(elem.getCode())) {
						elem2 = elem;}
				}
			}
		}
		if (elem2!= null) {
			diseaseBox2.setSelectedItem(elem2);
		} else { //try in the cancelled diseases
			if (opd.getDisease2() != null) {
				if (diseasesAll != null) {
					for (Disease elem : diseasesAll) {
						if (opd.getDisease2().getCode().equals(elem.getCode())) {
							MessageDialog.warning(OpdEdit.this, "angal.opd.disease2mayhavebeencancelled.msg");
							diseaseBox2.addItem(elem);
							diseaseBox2.setSelectedItem(elem);
						}
					}
				}
			}
		}
		return diseaseBox2;
	}

	private JComboBox getDiseaseBox3() {
		if (diseaseBox3 == null) {
			diseaseBox3 = new JComboBox();
			diseaseBox3.setMaximumSize(new Dimension(400, 50));
		}
		Disease elem2=null;
		List<Disease> diseases = null;
		try {
			diseases = diseaseManager.getDiseaseOpd();
		} catch(OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		diseaseBox3.addItem("");
		if (diseases != null) {
			for (Disease elem : diseases) {
				diseaseBox3.addItem(elem);
				if (!insert && opd.getDisease3() != null) {
					if (opd.getDisease3().getCode().equals(elem.getCode())) {
						elem2 = elem;}
				}
			}
		}
		if (elem2 != null) {
			diseaseBox3.setSelectedItem(elem2);
		} else { //try in the cancelled diseases
			if (opd.getDisease3() != null) {
				if (diseasesAll != null) {
					for (Disease elem : diseasesAll) {
						if (opd.getDisease3().getCode().equals(elem.getCode())) {
							MessageDialog.warning(OpdEdit.this, "angal.opd.disease3mayhavebeencancelled.msg");
							diseaseBox3.addItem(elem);
							diseaseBox3.setSelectedItem(elem);
						}
					}
				}
			}
		}
		return diseaseBox3;
	}
	
	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJButtonPanel() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.add(getOkButton(), null);
			jPanel2.add(getCancelButton(), null);
		}
		return jPanel2;
	}
	
	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			okButton.addActionListener(actionEvent -> {
						boolean result;
						LocalDateTime visitDate = LocalDateTime.now();
						char newPatient;
						String referralTo;
						String referralFrom;
						Disease disease = null;
						Disease disease2 = null;
						Disease disease3 = null;

						if (newPatientCheckBox.isSelected()) {
							newPatient = 'N';
						} else {
							newPatient = 'R';
						}

						if (referralToCheckBox.isSelected()) {
							referralTo = "R";
						} else {
							referralTo = "";
						}

						if (referralFromCheckBox.isSelected()) {
							referralFrom = "R";
						} else {
							referralFrom = "";
						}

						// disease
						if (diseaseBox.getSelectedIndex() > 0) {
							disease = ((Disease) diseaseBox.getSelectedItem());
						}
						// disease2
						if (diseaseBox2.getSelectedIndex() > 0) {
							disease2 = ((Disease) diseaseBox2.getSelectedItem());
						}
						// disease3
						if (diseaseBox3.getSelectedIndex() > 0) {
							disease3 = ((Disease) diseaseBox3.getSelectedItem());
						}
						// visit date
						LocalDateTime localDateTime = opdDateField.getLocalDateTime();
						if (localDateTime == null) {
							MessageDialog.error(OpdEdit.this, "angal.opd.pleaseinsertattendancedate.msg");
							return;
						}
						visitDate = localDateTime;

						if (radiof.isSelected()) {
							sex = 'F';
						} else {
							sex = 'M';
						}

						opd.setNewPatient(newPatient);
						opd.setReferralFrom(referralFrom);
						opd.setReferralTo(referralTo);
						opd.setAge(age);
						opd.setSex(sex);
						opd.setDisease(disease);
						opd.setDisease2(disease2);
						opd.setDisease3(disease3);
						opd.setDate(visitDate);
						opd.setNote("");
						opd.setUserID(UserBrowsingManager.getCurrentUser());

						try {
							if (insert) {    // Insert
								opd.setProgYear(getOpdProgYear(visitDate));
								// remember for later use
								RememberDates.setLastOpdVisitDate(visitDate);

								result = opdManager.newOpd(opd);
								if (result) {
									fireSurgeryInserted(opd);
									dispose();
								} else {
									MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
								}
							} else {    // Update
								Opd updatedOpd = opdManager.updateOpd(opd);
								if (updatedOpd != null) {
									fireSurgeryUpdated(updatedOpd);
									dispose();
								} else {
									MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
								}
							}
						} catch (OHServiceException ex) {
							OHServiceExceptionUtil.showMessages(ex);
						}
					}
			);
		}
		return okButton;
	}

	private int getOpdProgYear(LocalDateTime date) {
		int opdNum = 0;
		if (date == null) {
			date = LocalDateTime.now();
		}
		try {
			opdNum = opdManager.getProgYear(date.getYear()) + 1;
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		return opdNum;
	}
	
	/**
	 * This method initializes cancelButton
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
            cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			cancelButton.addActionListener(actionEvent -> dispose());
		}
		return cancelButton;
	}
	
	/**
	 * This method initializes ageField
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getAgeField() {
		if (ageField == null) {
			ageField = new VoLimitedTextField(3, 2);
			ageField.setText("0");
			ageField.setMaximumSize(new Dimension(50, 50));
			if (insert) {
				age = -1;
				ageField.setText("");
			} else {
				int oldage = opd.getAge();
				ageField.setText(String.valueOf(oldage));
				age = oldAge;
			}
			ageField.setMinimumSize(new Dimension(100, 50));
		}
		ageField.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				try {
					age = Integer.parseInt(ageField.getText());
					if (age < 0 || age > 200) {
						ageField.setText("");
						MessageDialog.error(OpdEdit.this, "angal.opd.insertavalidage.msg");
					}
				} catch (NumberFormatException ex) {
					ageField.setText("");
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		return ageField;
	}
	
	/**
	 * This method initializes sexPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSexPanel() {
		if (sexPanel == null) {
			sexPanel = new JPanel();
			ButtonGroup group = new ButtonGroup();
			JRadioButton radiom = new JRadioButton(MessageBundle.getMessage("angal.common.male.btn"));
			radiof = new JRadioButton(MessageBundle.getMessage("angal.common.female.btn"));
			if (insert) {
				radiom.setSelected(true);
			}
			else {
				if (opd.getSex() == 'F') {
					radiof.setSelected(true);
				} else {
					radiom.setSelected(true);
				}
			}			
			group.add(radiom);
			group.add(radiof);
			sexPanel.add(radiom);
			sexPanel.add(radiof);
		}
		return sexPanel;
	}

	private JPanel getJAgePanel() {
		if (jAgePanel == null) {
			jAgePanel = new JPanel();
			jAgePanel.add(new JLabel(MessageBundle.getMessage("angal.common.age.label")));
			jAgePanel.add(getAgeField());
		}
		return jAgePanel;
	}

	private JPanel getJDiseasePanel() {
		if (jDiseasePanel == null) {
			jDiseasePanel = new JPanel();
			jDiseasePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			jDiseasePanel.add(new JLabel(MessageBundle.getMessage("angal.opd.diagnosis.txt")));

			searchDiseaseTextField = new JTextField();
			jDiseasePanel.add(searchDiseaseTextField);
			searchDiseaseTextField.setColumns(10);
			searchDiseaseTextField.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						searchDiseaseButton.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});

			searchDiseaseButton = new JButton("");
			jDiseasePanel.add(searchDiseaseButton);
			searchDiseaseButton.setPreferredSize(new Dimension(20, 20));
			searchDiseaseButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			searchDiseaseButton.addActionListener(new ActionListener() {

				List<Disease> diseasesOPD = null;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						diseasesOPD = diseaseManager.getDiseaseOpd();
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}
					diseaseBox.removeAllItems();
					diseaseBox.addItem("");
					for (Disease disease : getSearchDiagnosisResults(searchDiseaseTextField.getText(),
							diseasesOPD == null ? diseasesAll : diseasesOPD)) {
						diseaseBox.addItem(disease);
					}

					if (diseaseBox.getItemCount() >= 2) {
						diseaseBox.setSelectedIndex(1);
					}
					diseaseBox.requestFocus();
					if (diseaseBox.getItemCount() > 2) {
						diseaseBox.showPopup();
					}
				}
			});

		}
		return jDiseasePanel;
	}

	private JPanel getJDiseasePanel2() {
		if (jDiseasePanel2 == null) {
			jDiseasePanel2 = new JPanel();
			jDiseasePanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
			jDiseasePanel2.add(new JLabel(MessageBundle.getMessage("angal.opd.diagnosisnfulllist2.txt")));

			searchDiseaseTextField2 = new JTextField();
			jDiseasePanel2.add(searchDiseaseTextField2);
			searchDiseaseTextField2.setColumns(10);
			searchDiseaseTextField2.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						searchDiseaseButton2.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});

			searchDiseaseButton2 = new JButton("");
			jDiseasePanel2.add(searchDiseaseButton2);
			searchDiseaseButton2.setPreferredSize(new Dimension(20, 20));
			searchDiseaseButton2.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			searchDiseaseButton2.addActionListener(new ActionListener() {

				List<Disease> diseasesOPD = null;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						diseasesOPD = diseaseManager.getDiseaseOpd();
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}
					diseaseBox2.removeAllItems();
					diseaseBox2.addItem("");
					for (Disease disease : getSearchDiagnosisResults(searchDiseaseTextField2.getText(),
									diseasesOPD == null ? diseasesAll : diseasesOPD)) {
						diseaseBox2.addItem(disease);
					}

					if (diseaseBox2.getItemCount() >= 2) {
						diseaseBox2.setSelectedIndex(1);
					}
					diseaseBox2.requestFocus();
					if (diseaseBox2.getItemCount() > 2) {
						diseaseBox2.showPopup();
					}
				}
			});
		}
		return jDiseasePanel2;
	}

	private JPanel getJDiseasePanel3() {
		if (jDiseasePanel3 == null) {
			jDiseasePanel3 = new JPanel();
			jDiseasePanel3.setLayout(new FlowLayout(FlowLayout.LEFT));
			jDiseasePanel3.add(new JLabel(MessageBundle.getMessage("angal.opd.diagnosisnfulllist3.txt")));

			searchDiseaseTextField3 = new JTextField();
			jDiseasePanel3.add(searchDiseaseTextField3);
			searchDiseaseTextField3.setColumns(10);
			searchDiseaseTextField3.addKeyListener(new KeyListener() {

				@Override
				public void keyPressed(KeyEvent e) {
					int key = e.getKeyCode();
					if (key == KeyEvent.VK_ENTER) {
						searchDiseaseButton3.doClick();
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});

			searchDiseaseButton3 = new JButton("");
			jDiseasePanel3.add(searchDiseaseButton3);
			searchDiseaseButton3.setPreferredSize(new Dimension(20, 20));
			searchDiseaseButton3.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
			searchDiseaseButton3.addActionListener(new ActionListener() {

				List<Disease> diseasesOPD = null;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						diseasesOPD = diseaseManager.getDiseaseOpd();
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}
					diseaseBox3.removeAllItems();
					diseaseBox3.addItem("");
					for (Disease disease : getSearchDiagnosisResults(searchDiseaseTextField3.getText(),
							diseasesOPD == null ? diseasesAll : diseasesOPD)) {
						diseaseBox3.addItem(disease);
					}

					if (diseaseBox3.getItemCount() >= 2) {
						diseaseBox3.setSelectedIndex(1);
					}
					diseaseBox3.requestFocus();
					if (diseaseBox3.getItemCount() > 2) {
						diseaseBox3.showPopup();
					}
				}
			});
		}
		return jDiseasePanel3;
	}

	private JPanel getJDiseaseTypePanel() {
		if (jDiseaseTypePanel == null) {
			jDiseaseTypePanel = new JPanel();
			jDiseaseTypePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			jDiseaseTypePanel.add(new JLabel(MessageBundle.getMessage("angal.opd.diseasetype.txt")));
		}
		return jDiseaseTypePanel;
	}

	private JPanel getJSexPanel() {
		if (jSexPanel == null) {
			jSexPanel = new JPanel();
			jSexPanel.add(new JLabel(MessageBundle.getMessage("angal.common.sex.label")));
			jSexPanel.add(getSexPanel());
		}
		return jSexPanel;
	}

	private JPanel getJNewPatientPanel() {
		String referralTo;
		String referralFrom;

		if (jNewPatientPanel == null) {
			jNewPatientPanel = new JPanel();
			newPatientCheckBox = new JCheckBox(MessageBundle.getMessage("angal.opd.newattendance.txt"));
			jNewPatientPanel.add(newPatientCheckBox);
			if (!insert) {
				if (opd.getNewPatient() == 'N') {
					newPatientCheckBox.setSelected(true);
				}
			}
			referralFromCheckBox = new JCheckBox(MessageBundle.getMessage("angal.opd.referral.txt"));
			jNewPatientPanel.add(referralFromCheckBox);
			if (!insert) {
				referralFrom = opd.getReferralFrom();
				if (referralFrom == null) {
					referralFrom="";
				}
				if (referralFrom.equals("R")) {
					referralFromCheckBox.setSelected(true);
				}
			}
			referralToCheckBox = new JCheckBox(MessageBundle.getMessage("angal.opd.referralto.txt"));
			jNewPatientPanel.add(referralToCheckBox);
			if (!insert) {
				referralTo = opd.getReferralTo();
				if (referralTo == null) {
					referralTo="";
				}
				if (referralTo.equals("R")) {
					referralToCheckBox.setSelected(true);
				}
			}
		}
		return jNewPatientPanel;
	}

	private JPanel getJDatePanel() {
		if (jDatePanel == null) {
			jDatePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
			LocalDateTime dateIn;
			if (insert) {
				if (RememberDates.getLastOpdVisitDate() == null) {
					dateIn = LocalDateTime.now();
				} else {
					dateIn = RememberDates.getLastOpdVisitDate();
				}
			} else {
				dateIn = opd.getDate();
			}

			opdDateField = new GoodDateTimeChooser(dateIn);
			jDatePanel.add(opdDateField);
			jDatePanel = setMyBorder(jDatePanel, MessageBundle.getMessage("angal.opd.attendancedate.txt"));
		}
		return jDatePanel;
	}
	
	/*
	 * Set a specific border+title to a panel
	 */
	private JPanel setMyBorder(JPanel c, String title) {
		javax.swing.border.Border b2 = BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title), BorderFactory
						.createEmptyBorder(0, 0, 0, 0));
		c.setBorder(b2);
		return c;
	}

	private List<Disease> getSearchDiagnosisResults(String s, List<Disease> diseaseList) {
		String query = s.trim();
		List<Disease> results = new ArrayList<>();
		for (Disease disease : diseaseList) {
			if (!query.equals("")) {
				String[] patterns = query.split(" ");
				String name = disease.getDescription().toLowerCase();
				boolean patternFound = false;
				for (String pattern : patterns) {
					if (name.contains(pattern.toLowerCase())) {
						patternFound = true;
						//It is sufficient that only one pattern matches the query
						break;
					}
				}
				if (patternFound) {
					results.add(disease);
				}
			} else {
				results.add(disease);
			}
		}
		return results;
	}
	
}
