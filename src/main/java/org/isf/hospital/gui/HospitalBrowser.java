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
package org.isf.hospital.gui;

import java.awt.BorderLayout;
import java.sql.Time;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.isf.generaldata.MessageBundle;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.hospital.model.Hospital;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodTimeChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoIntegerTextField;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

/**
 * Shows information about the hospital
 *
 * @author Fin8, Furla, Thoia
 */
public class HospitalBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;

	private JPanel jContainPanel = null;
	private JPanel jButtonPanel = null;
	private JPanel jDataPanel = null;
	private GoodTimeChooser visitStartField;
	private GoodTimeChooser visitEndField;
	private VoIntegerTextField durationField;
	private JTextField nameJTextField;
	private JTextField addressJTextField;
	private JTextField cityJTextField;
	private JTextField teleJTextField;
	private JTextField faxJTextField;
	private JTextField emailJTextField;
	private JTextField currencyCodeJTextField;
	private HospitalBrowsingManager manager;
	private Hospital hospital;
	private JButton editButton;
	private JButton updateButton;

	public HospitalBrowser() {
		super();
		manager = Context.getApplicationContext().getBean(HospitalBrowsingManager.class);
		try {
			hospital = manager.getHospital();
		} catch (OHServiceException e) {
			this.hospital = null;
			OHServiceExceptionUtil.showMessages(e);
		}
		initialize();
		setVisible(true);
		pack();
	}

	private void initialize() {
		this.setTitle(MessageBundle.getMessage("angal.hospital.hospitalinformation.title"));
		setContentPane(getJContainPanel());
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
	}

	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel(new BorderLayout());
			jContainPanel.add(getJDataPanel(), java.awt.BorderLayout.CENTER);
			jContainPanel.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
		}
		return jContainPanel;
	}

	private JPanel getJDataPanel() {
		if (jDataPanel == null) {
			jDataPanel = new JPanel(new SpringLayout());

			JLabel nameJLabel = new JLabel(MessageBundle.getMessage("angal.common.name.txt") + ": ");
			nameJTextField = new JTextField(25);
			nameJTextField.setEditable(false);
			nameJTextField.setText(hospital.getDescription());

			JLabel addressJLabel = new JLabel(MessageBundle.getMessage("angal.common.address.txt") + ": ");
			addressJTextField = new JTextField(25);
			addressJTextField.setEditable(false);
			addressJTextField.setText(hospital.getAddress());

			JLabel cityJLabel = new JLabel(MessageBundle.getMessage("angal.common.city.txt") + ": ");
			cityJTextField = new JTextField(25);
			cityJTextField.setEditable(false);
			cityJTextField.setText(hospital.getCity());

			JLabel teleJLabel = new JLabel(MessageBundle.getMessage("angal.common.telephone.txt") + ": ");
			teleJTextField = new JTextField(25);
			teleJTextField.setEditable(false);
			teleJTextField.setText(hospital.getTelephone());

			JLabel faxJLabel = new JLabel(MessageBundle.getMessage("angal.hospital.faxnumber") + ": ");
			faxJTextField = new JTextField(25);
			faxJTextField.setEditable(false);
			faxJTextField.setText(hospital.getFax());

			JLabel emailJLabel = new JLabel(MessageBundle.getMessage("angal.hospital.emailaddress") + ": ");
			emailJTextField = new JTextField(25);
			emailJTextField.setEditable(false);
			emailJTextField.setText(hospital.getEmail());

			JLabel currencyCodeJLabel = new JLabel(MessageBundle.getMessage("angal.hospital.currencycod") + ": ");
			currencyCodeJTextField = new VoLimitedTextField(3, 25);
			currencyCodeJTextField.setEditable(false);
			currencyCodeJTextField.setText(hospital.getCurrencyCod());

			JLabel startHourJLabel = new JLabel(MessageBundle.getMessage("angal.hospital.visitstarthour.txt") + ": ");
			visitStartField = new GoodTimeChooser(hospital.getVisitStartTime().toLocalTime());
			visitStartField.setEditable(false);

			JLabel endHourJLabel = new JLabel(MessageBundle.getMessage("angal.hospital.visitendhour.txt") + ": ");
			visitEndField = new GoodTimeChooser(hospital.getVisitEndTime().toLocalTime());
			visitEndField.setEditable(false);

			JLabel durationLabel = new JLabel(MessageBundle.getMessage("angal.hospital.visitduration.txt") + ": ");
			durationField = new VoIntegerTextField(hospital.getVisitDuration(), 2);
			durationField.setEditable(false);

			jDataPanel.add(nameJLabel);
			jDataPanel.add(nameJTextField);
			jDataPanel.add(addressJLabel);
			jDataPanel.add(addressJTextField);
			jDataPanel.add(cityJLabel);
			jDataPanel.add(cityJTextField);
			jDataPanel.add(teleJLabel);
			jDataPanel.add(teleJTextField);
			jDataPanel.add(faxJLabel);
			jDataPanel.add(faxJTextField);
			jDataPanel.add(emailJLabel);
			jDataPanel.add(emailJTextField);
			jDataPanel.add(currencyCodeJLabel);
			jDataPanel.add(currencyCodeJTextField);
			jDataPanel.add(startHourJLabel);
			jDataPanel.add(visitStartField);
			jDataPanel.add(endHourJLabel);
			jDataPanel.add(visitEndField);
			jDataPanel.add(durationLabel);
			jDataPanel.add(durationField);

			SpringUtilities.makeCompactGrid(jDataPanel, 10, 2, 5, 5, 5, 5);
		}
		return jDataPanel;
	}

	private boolean isModified() {
		LocalTime startTime = visitStartField.getLocalTime();
		LocalTime endTime = visitEndField.getLocalTime();
		if (!nameJTextField.getText().equalsIgnoreCase(hospital.getDescription())
				|| !addressJTextField.getText().equalsIgnoreCase(hospital.getAddress())
				|| !cityJTextField.getText().equalsIgnoreCase(hospital.getCity())
				|| !teleJTextField.getText().equalsIgnoreCase(hospital.getTelephone() == null ? "" : hospital.getTelephone())
				|| !faxJTextField.getText().equalsIgnoreCase(hospital.getFax() == null ? "" : hospital.getFax())
				|| !emailJTextField.getText().equalsIgnoreCase(hospital.getEmail() == null ? "" : hospital.getEmail())
				|| !currencyCodeJTextField.getText().equalsIgnoreCase(hospital.getCurrencyCod() == null ? "" : hospital.getCurrencyCod())
				|| !startTime.equals(hospital.getVisitStartTime().toLocalTime())
				|| !endTime.equals(hospital.getVisitEndTime().toLocalTime())
		        || durationField.getValue() != hospital.getVisitDuration()) {
			return true;
		}
		return false;
	}

	private void saveConfirm() {
		int response = MessageDialog.yesNo(null, "angal.hospital.savethechanges.msg");
		if (response == JOptionPane.YES_OPTION) {
			if (validationErrors()) {
				return;
			}
			updateHospitalEntity();
		}
	}

	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel();
			editButton = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			editButton.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			updateButton = new JButton(MessageBundle.getMessage("angal.common.update.btn"));
			updateButton.setMnemonic(MessageBundle.getMnemonic("angal.common.update.btn.key"));
			JButton closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			updateButton.setEnabled(false);

			closeButton.addActionListener(actionEvent -> {
				if (validationErrors()) {
					return;
				}
				if (isModified()) {
					//open confirm save window
					saveConfirm();
				}
				dispose();
			});
			editButton.addActionListener(actionEvent -> setFieldsForEditing(true));
			updateButton.addActionListener(actionEvent -> {
				if (validationErrors()) {
					return;
				}
				setFieldsForEditing(false);
				updateHospitalEntity();
			});
			jButtonPanel.add(editButton);
			jButtonPanel.add(updateButton);
			jButtonPanel.add(closeButton);
		}
		return jButtonPanel;
	}

	private void setFieldsForEditing(boolean enabled) {
		nameJTextField.setEditable(enabled);
		addressJTextField.setEditable(enabled);
		cityJTextField.setEditable(enabled);
		teleJTextField.setEditable(enabled);
		faxJTextField.setEditable(enabled);
		emailJTextField.setEditable(enabled);
		currencyCodeJTextField.setEditable(enabled);
		visitStartField.setEditable(enabled);
		visitEndField.setEditable(enabled);
		durationField.setEditable(enabled);
		updateButton.setEnabled(enabled);
		editButton.setEnabled(!enabled);
		nameJTextField.requestFocus();
	}

	private boolean validationErrors() {
		boolean inError = false;
		if (nameJTextField.getText().isEmpty()) {
			MessageDialog.error(null, "angal.hopsital.thehospitalnamecannotbeblank.msg");
			inError = true;
		}
		LocalTime startTime = visitStartField.getLocalTime();
		LocalTime endTime = visitEndField.getLocalTime();
		if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
			MessageDialog.error(null, "angal.hospital.thestartofvisitinghoursislaterthantheendhour.msg");
			inError = true;
		}
		if (startTime.getHour() < 0 || endTime.getHour() > 24) {
			MessageDialog.error(null, "angal.hospital.thevisitinghourvaluesmustbeintherange0to24.msg");
			inError = true;
		}
		if (durationField.getText().isEmpty()) {
			MessageDialog.error(null, "angal.hospital.thevisitdurationcannotbeblank.msg");
			inError = true;
		} else {
			long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
			if (durationField.getValue() <= 0 || durationField.getValue() >= minutes) {
				MessageDialog.error(null, "angal.hospital.thevisitdurationmustbepositiveandlessthanthelengthofthevisitinghours.msg");
				inError = true;

			}
		}
		return inError;
	}

	private void updateHospitalEntity() {
		hospital.setDescription(nameJTextField.getText());
		hospital.setAddress(addressJTextField.getText());
		hospital.setCity(cityJTextField.getText());
		hospital.setTelephone(teleJTextField.getText().isEmpty() ? null : teleJTextField.getText());
		hospital.setFax(faxJTextField.getText().isEmpty() ? null : faxJTextField.getText());
		hospital.setEmail(emailJTextField.getText().isEmpty() ? null : emailJTextField.getText());
		hospital.setCurrencyCod(currencyCodeJTextField.getText().isEmpty() ? null : currencyCodeJTextField.getText());
		hospital.setVisitStartTime(Time.valueOf(visitStartField.getLocalTime()));
		hospital.setVisitEndTime(Time.valueOf(visitEndField.getLocalTime()));
		hospital.setVisitDuration(durationField.getValue());

		try {
			this.hospital = manager.updateHospital(hospital);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}

}
