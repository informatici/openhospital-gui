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
package org.isf.ward.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.ward.manager.WardBrowserManager;
import org.isf.ward.model.Ward;

/**
 * This class allows wards edits and inserts
 * 
 * @author Rick
 */
public class WardEdit extends JDialog {

	private static final long serialVersionUID = 1L;
	private EventListenerList wardListeners = new EventListenerList();

	public interface WardListener extends EventListener {

		void wardUpdated(AWTEvent e);

		void wardInserted(AWTEvent e);
	}

	public void addWardListener(WardListener l) {
		wardListeners.add(WardListener.class, l);
	}

	public void removeWardListener(WardListener listener) {
		wardListeners.remove(WardListener.class, listener);
	}

	private void fireWardInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = wardListeners.getListeners(WardListener.class);
		for (EventListener listener : listeners) {
			((WardListener) listener).wardInserted(event);
		}
	}

	private void fireWardUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = wardListeners.getListeners(WardListener.class);
		for (EventListener listener : listeners) {
			((WardListener) listener).wardUpdated(event);
		}
	}

	private JPanel jContentPane = null;
	private JPanel dataPanel = null;
	private JPanel buttonPanel = null;
	private JButton cancelButton = null;
	private JButton okButton = null;
	private JTextField descriptionTextField = null;
	private JTextField codeTextField = null;
	private JTextField telTextField = null;
	private JTextField faxTextField = null;
	private JTextField emailTextField = null;
	private JTextField bedsTextField = null;
	private JTextField nursTextField = null;
	private JTextField docsTextField = null;
	private JTextField durationTextField = null;
	private JCheckBox isPharmacyCheck = null;
	private JCheckBox isMaleCheck = null;
	private JCheckBox isFemaleCheck = null;
	private Ward ward;
	private boolean insert;
	private int beds;
	private int nurs;
	private int docs;
	private int duration;

	/**
	 * This is the default constructor; we pass the parent frame
	 * (because it is a jdialog), the arraylist and the selected
	 * row because we need to update them
	 */
	public WardEdit(JFrame parent, Ward old, boolean inserting) {
		super(parent, true);
		insert = inserting;
		ward = old;        //operation will be used for every operation
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.ward.newward.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.ward.editward.title"));
		}
		pack();
		setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getDataPanel(), java.awt.BorderLayout.CENTER);
			jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes dataPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getDataPanel() {
		if (dataPanel == null) {
			dataPanel = new JPanel();
			GridBagLayout gblDataPanel = new GridBagLayout();
			gblDataPanel.columnWeights = new double[] { 0.0, 1.0 };
			dataPanel.setLayout(gblDataPanel);
			JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.common.codestar"));
			GridBagConstraints gbcCodeLabel = new GridBagConstraints();
			gbcCodeLabel.anchor = GridBagConstraints.WEST;
			gbcCodeLabel.insets = new Insets(0, 0, 5, 5);
			gbcCodeLabel.gridx = 0;
			gbcCodeLabel.gridy = 0;
			dataPanel.add(codeLabel, gbcCodeLabel);
			GridBagConstraints gbcCodeTextField = new GridBagConstraints();
			gbcCodeTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcCodeTextField.insets = new Insets(0, 0, 5, 0);
			gbcCodeTextField.gridx = 1;
			gbcCodeTextField.gridy = 0;
			dataPanel.add(getCodeTextField(), gbcCodeTextField);

			JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.ward.nameedit"));
			GridBagConstraints gbcDescLabel = new GridBagConstraints();
			gbcDescLabel.anchor = GridBagConstraints.WEST;
			gbcDescLabel.insets = new Insets(0, 0, 5, 5);
			gbcDescLabel.gridx = 0;
			gbcDescLabel.gridy = 1;
			dataPanel.add(descLabel, gbcDescLabel);

			GridBagConstraints gbcDescriptionTextField = new GridBagConstraints();
			gbcDescriptionTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcDescriptionTextField.insets = new Insets(0, 0, 5, 0);
			gbcDescriptionTextField.gridx = 1;
			gbcDescriptionTextField.gridy = 1;
			dataPanel.add(getDescriptionTextField(), gbcDescriptionTextField);

			GridBagConstraints gbcTelTextField = new GridBagConstraints();
			gbcTelTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcTelTextField.insets = new Insets(0, 0, 5, 0);
			gbcTelTextField.gridx = 1;
			gbcTelTextField.gridy = 2;
			dataPanel.add(getTelTextField(), gbcTelTextField);
			JLabel telephoneLabel = new JLabel(MessageBundle.getMessage("angal.common.telephone.txt"));
			GridBagConstraints gbcTelephoneLabel = new GridBagConstraints();
			gbcTelephoneLabel.anchor = GridBagConstraints.WEST;
			gbcTelephoneLabel.insets = new Insets(0, 0, 5, 5);
			gbcTelephoneLabel.gridx = 0;
			gbcTelephoneLabel.gridy = 2;
			dataPanel.add(telephoneLabel, gbcTelephoneLabel);

			JLabel faxLabel = new JLabel(MessageBundle.getMessage("angal.common.fax.txt"));
			GridBagConstraints gbcFaxLabel = new GridBagConstraints();
			gbcFaxLabel.anchor = GridBagConstraints.WEST;
			gbcFaxLabel.insets = new Insets(0, 0, 5, 5);
			gbcFaxLabel.gridx = 0;
			gbcFaxLabel.gridy = 3;
			dataPanel.add(faxLabel, gbcFaxLabel);
			GridBagConstraints gbcFaxTextField = new GridBagConstraints();
			gbcFaxTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcFaxTextField.insets = new Insets(0, 0, 5, 0);
			gbcFaxTextField.gridx = 1;
			gbcFaxTextField.gridy = 3;
			dataPanel.add(getFaxTextField(), gbcFaxTextField);
			GridBagConstraints gbcEmailTextField = new GridBagConstraints();
			gbcEmailTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcEmailTextField.insets = new Insets(0, 0, 5, 0);
			gbcEmailTextField.gridx = 1;
			gbcEmailTextField.gridy = 4;

			dataPanel.add(getEmailTextField(), gbcEmailTextField);
			JLabel emailLabel = new JLabel(MessageBundle.getMessage("angal.ward.emailedit"));
			GridBagConstraints gbcEmailLabel = new GridBagConstraints();
			gbcEmailLabel.anchor = GridBagConstraints.WEST;
			gbcEmailLabel.insets = new Insets(0, 0, 5, 5);
			gbcEmailLabel.gridx = 0;
			gbcEmailLabel.gridy = 4;
			dataPanel.add(emailLabel, gbcEmailLabel);

			JLabel bedsLabel = new JLabel(MessageBundle.getMessage("angal.ward.bedsedit"));
			GridBagConstraints gbcBedsLabel = new GridBagConstraints();
			gbcBedsLabel.anchor = GridBagConstraints.WEST;
			gbcBedsLabel.insets = new Insets(0, 0, 5, 5);
			gbcBedsLabel.gridx = 0;
			gbcBedsLabel.gridy = 5;
			dataPanel.add(bedsLabel, gbcBedsLabel);
			GridBagConstraints gbcBedsTextField = new GridBagConstraints();
			gbcBedsTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcBedsTextField.insets = new Insets(0, 0, 5, 0);
			gbcBedsTextField.gridx = 1;
			gbcBedsTextField.gridy = 5;
			dataPanel.add(getBedsTextField(), gbcBedsTextField);

			JLabel nurseLabel = new JLabel(MessageBundle.getMessage("angal.ward.nursesedit"));
			GridBagConstraints gbcNurseLabel = new GridBagConstraints();
			gbcNurseLabel.anchor = GridBagConstraints.WEST;
			gbcNurseLabel.insets = new Insets(0, 0, 5, 5);
			gbcNurseLabel.gridx = 0;
			gbcNurseLabel.gridy = 6;
			dataPanel.add(nurseLabel, gbcNurseLabel);
			GridBagConstraints gbcNursTextField = new GridBagConstraints();
			gbcNursTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcNursTextField.insets = new Insets(0, 0, 5, 0);
			gbcNursTextField.gridx = 1;
			gbcNursTextField.gridy = 6;
			dataPanel.add(getNursTextField(), gbcNursTextField);

			JLabel docsLabel = new JLabel(MessageBundle.getMessage("angal.ward.doctorsedit"));
			GridBagConstraints gbcDocsLabel = new GridBagConstraints();
			gbcDocsLabel.anchor = GridBagConstraints.WEST;
			gbcDocsLabel.insets = new Insets(0, 0, 5, 5);
			gbcDocsLabel.gridx = 0;
			gbcDocsLabel.gridy = 7;
			dataPanel.add(docsLabel, gbcDocsLabel);
			GridBagConstraints gbcDocsTextField = new GridBagConstraints();
			gbcDocsTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcDocsTextField.insets = new Insets(0, 0, 5, 0);
			gbcDocsTextField.gridx = 1;
			gbcDocsTextField.gridy = 7;
			dataPanel.add(getDocsTextField(), gbcDocsTextField);

			JLabel durationLabel = new JLabel(MessageBundle.getMessage("angal.ward.visitdurationedit"));
			GridBagConstraints gbcDurationLabel = new GridBagConstraints();
			gbcDurationLabel.anchor = GridBagConstraints.WEST;
			gbcDurationLabel.insets = new Insets(0, 0, 5, 5);
			gbcDurationLabel.gridx = 0;
			gbcDurationLabel.gridy = 8;
			dataPanel.add(durationLabel, gbcDurationLabel);
			GridBagConstraints gbcDurationTextField = new GridBagConstraints();
			gbcDurationTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcDurationTextField.insets = new Insets(0, 0, 5, 0);
			gbcDurationTextField.gridx = 1;
			gbcDurationTextField.gridy = 8;
			dataPanel.add(getDurationTextField(), gbcDurationTextField);

			GridBagConstraints gbcIsPharmacyCheck = new GridBagConstraints();
			gbcIsPharmacyCheck.anchor = GridBagConstraints.WEST;
			gbcIsPharmacyCheck.insets = new Insets(0, 0, 5, 0);
			gbcIsPharmacyCheck.gridwidth = 2;
			gbcIsPharmacyCheck.gridx = 0;
			gbcIsPharmacyCheck.gridy = 9;
			dataPanel.add(getIsPharmacyCheck(), gbcIsPharmacyCheck);

			GridBagConstraints gbcIsMaleCheck = new GridBagConstraints();
			gbcIsMaleCheck.anchor = GridBagConstraints.WEST;
			gbcIsMaleCheck.insets = new Insets(0, 0, 5, 0);
			gbcIsMaleCheck.gridwidth = 2;
			gbcIsMaleCheck.gridx = 0;
			gbcIsMaleCheck.gridy = 10;
			dataPanel.add(getIsMaleCheck(), gbcIsMaleCheck);

			GridBagConstraints gbcIsFemaleCheck = new GridBagConstraints();
			gbcIsFemaleCheck.anchor = GridBagConstraints.WEST;
			gbcIsFemaleCheck.insets = new Insets(0, 0, 5, 0);
			gbcIsFemaleCheck.gridwidth = 2;
			gbcIsFemaleCheck.gridx = 0;
			gbcIsFemaleCheck.gridy = 11;
			dataPanel.add(getIsFemaleCheck(), gbcIsFemaleCheck);

			JLabel requiredLabel = new JLabel(MessageBundle.getMessage("angal.ward.requiredfields"));
			GridBagConstraints gbcRequiredLabel = new GridBagConstraints();
			gbcRequiredLabel.gridwidth = 2;
			gbcRequiredLabel.anchor = GridBagConstraints.EAST;
			gbcRequiredLabel.gridx = 0;
			gbcRequiredLabel.gridy = 12;
			dataPanel.add(requiredLabel, gbcRequiredLabel);
		}
		return dataPanel;
	}

	/**
	 * This method initializes buttonPanel
	 *
	 * @return javax.swing.JPanel
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
	 * This method initializes okButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));

			okButton.addActionListener(actionEvent -> {
				WardBrowserManager manager = Context.getApplicationContext().getBean(WardBrowserManager.class);

				try {
					beds = Integer.parseInt(bedsTextField.getText());
				} catch (NumberFormatException f) {
					MessageDialog.error(WardEdit.this, "angal.ward.insertavalidbedsnumber");
					return;
				}
				if (beds < 0) {
					MessageDialog.error(WardEdit.this, "angal.ward.insertavalidbedsnumber");
					return;
				}
				try {
					nurs = Integer.parseInt(nursTextField.getText());
				} catch (NumberFormatException f) {
					MessageDialog.error(WardEdit.this, "angal.ward.insertavalidnursesnumber");
					return;
				}
				if (nurs < 0) {
					MessageDialog.error(WardEdit.this, "angal.ward.insertavalidnursesnumber");
					return;
				}
				try {
					docs = Integer.parseInt(docsTextField.getText());
				} catch (NumberFormatException f) {
					MessageDialog.error(WardEdit.this, "angal.ward.insertavaliddoctorsnumber");
					return;
				}
				if (docs < 0) {
					MessageDialog.error(WardEdit.this, "angal.ward.insertavaliddoctorsnumber");
					return;
				}
				try {
					duration = Integer.parseInt(durationTextField.getText());
				} catch (NumberFormatException f) {
					MessageDialog.error(WardEdit.this, "angal.ward.insertavaliddurationvalue.msg");
					return;
				}
				if (duration <= 0) {
					MessageDialog.error(WardEdit.this, "angal.ward.insertavaliddurationvalue.msg");
					return;
				}
				ward.setDescription(descriptionTextField.getText());
				ward.setCode(codeTextField.getText().toUpperCase().trim());
				ward.setTelephone(telTextField.getText());
				ward.setFax(faxTextField.getText());
				ward.setEmail(emailTextField.getText());
				ward.setBeds(beds);
				ward.setNurs(nurs);
				ward.setDocs(docs);
				ward.setPharmacy(isPharmacyCheck.isSelected());
				ward.setMale(isMaleCheck.isSelected());
				ward.setFemale(isFemaleCheck.isSelected());
				ward.setVisitDuration(duration);

				boolean result = false;
				Ward savedWard;
				if (insert) { // inserting
					try {
						savedWard = manager.newWard(ward);
						if (savedWard != null) {
							ward.setLock(savedWard.getLock());
							result = true;
						}
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}
					if (result) {
						fireWardInserted();
					}
				} else {
					try { // updating
						savedWard = manager.updateWard(ward);
						if (savedWard != null) {
							ward.setLock(savedWard.getLock());
							result = true;
						}
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}
					if (result) {
						fireWardUpdated();
					}
				}
				if (!result) {
					MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
				}
				else {
					dispose();
				}
			});
		}
		return okButton;
	}

	/**
	 * This method initializes descriptionTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getDescriptionTextField() {
		if (descriptionTextField == null) {
			descriptionTextField = new VoLimitedTextField(50);
			if (!insert) {
				descriptionTextField.setText(ward.getDescription());
			}
		}
		return descriptionTextField;
	}

	/**
	 * This method initializes codeTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getCodeTextField() {
		if (codeTextField == null) {
			codeTextField = new VoLimitedTextField(1, 20);
			if (!insert) {
				codeTextField.setText(ward.getCode());
				codeTextField.setEnabled(false);
			}
		}
		return codeTextField;
	}

	/**
	 * This method initializes telTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getTelTextField() {
		if (telTextField == null) {
			telTextField = new VoLimitedTextField(50, 20);
			if (!insert) {
				telTextField.setText(ward.getTelephone());
			}
		}
		return telTextField;
	}

	/**
	 * This method initializes faxTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getFaxTextField() {
		if (faxTextField == null) {
			faxTextField = new VoLimitedTextField(50, 20);
			if (!insert) {
				faxTextField.setText(ward.getFax());
			}
		}
		return faxTextField;
	}

	/**
	 * This method initializes emailTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getEmailTextField() {
		if (emailTextField == null) {
			emailTextField = new VoLimitedTextField(50, 20);
			if (!insert) {
				emailTextField.setText(ward.getEmail());
			}
		}
		return emailTextField;
	}

	/**
	 * This method initializes bedsTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getBedsTextField() {
		if (bedsTextField == null) {
			bedsTextField = new VoLimitedTextField(4, 20);
			if (!insert) {
				bedsTextField.setText(Integer.toString(ward.getBeds()));
			}
		}
		return bedsTextField;
	}

	/**
	 * This method initializes nursTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getNursTextField() {
		if (nursTextField == null) {
			nursTextField = new VoLimitedTextField(4, 20);
			if (!insert) {
				nursTextField.setText(Integer.toString(ward.getNurs()));
			}
		}
		return nursTextField;
	}

	/**
	 * This method initializes docsTextField
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getDocsTextField() {
		if (docsTextField == null) {
			docsTextField = new VoLimitedTextField(4, 20);
			if (!insert) {
				docsTextField.setText(Integer.toString(ward.getDocs()));
			}
		}
		return docsTextField;
	}

	private JTextField getDurationTextField() {
		if (durationTextField == null) {
			durationTextField = new VoLimitedTextField(4, 20);
			if (!insert) {
				durationTextField.setText(Integer.toString(ward.getVisitDuration()));
			}
		}
		return durationTextField;
	}

	/**
	 * This method initializes isPharmacyCheck
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getIsPharmacyCheck() {
		if (isPharmacyCheck == null) {
			isPharmacyCheck = new JCheckBox(MessageBundle.getMessage("angal.ward.wardwithpharmacy"));
			if (!insert) {
				isPharmacyCheck.setSelected(ward.isPharmacy());
			}
		}
		return isPharmacyCheck;
	}

	/**
	 * This method initializes isFemaleCheck
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getIsMaleCheck() {
		if (isMaleCheck == null) {
			isMaleCheck = new JCheckBox(MessageBundle.getMessage("angal.ward.maleward"));
			if (!insert) {
				isMaleCheck.setSelected(ward.isMale());
			}
		}
		return isMaleCheck;
	}

	/**
	 * This method initializes isFemaleCheck
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getIsFemaleCheck() {
		if (isFemaleCheck == null) {
			isFemaleCheck = new JCheckBox(MessageBundle.getMessage("angal.ward.femaleward"));
			if (!insert) {
				isFemaleCheck.setSelected(ward.isFemale());
			}
		}
		return isFemaleCheck;
	}
}
