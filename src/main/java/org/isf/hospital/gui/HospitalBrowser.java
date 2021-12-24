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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.isf.generaldata.MessageBundle;
import org.isf.hospital.manager.HospitalBrowsingManager;
import org.isf.hospital.model.Hospital;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.VoLimitedTextField;

/**
 * Shows information about the hospital
 *
 * @author Fin8, Furla, Thoia
 */
public class HospitalBrowser extends ModalJFrame {

	private static final long serialVersionUID = 1L;
	private int pfrmBase = 96;
	private int pfrmWidth = 24;
	private int pfrmHeight = 24;
	private int pfrmBordX;
	private int pfrmBordY;
	private JPanel jContainPanel = null;
	private JPanel jButtonPanel = null;
	private JPanel jDataPanel = null;
	private JPanel jNamePanel = null;
	private JPanel jAddressPanel = null;
	private JPanel jCityPanel = null;
	private JPanel jTelePanel = null;
	private JPanel jFaxPanel = null;
	private JPanel jEmailPanel = null;
	private JPanel jCurrencyCodPanel = null;
	private JTextField nameJTextField;
	private JTextField addressJTextField;
	private JTextField cityJTextField;
	private JTextField teleJTextField;
	private JTextField faxJTextField;
	private JTextField emailJTextField;
	private JTextField currencyCodJTextField;
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
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		pfrmBordX = (screensize.width - (screensize.width / pfrmBase * pfrmWidth)) / 2;
		pfrmBordY = (screensize.height - (screensize.height / pfrmBase * pfrmHeight)) / 2;
		this.setBounds(pfrmBordX, pfrmBordY, screensize.width / pfrmBase * pfrmWidth, screensize.height / pfrmBase * pfrmHeight);
		this.setContentPane(getJContainPanel());
	}

	private JPanel getJContainPanel() {
		if (jContainPanel == null) {
			jContainPanel = new JPanel();
			jContainPanel.setLayout(new BorderLayout());
			jContainPanel.add(getJDataPanel(), java.awt.BorderLayout.CENTER);
			jContainPanel.add(getJButtonPanel(), java.awt.BorderLayout.SOUTH);
		}
		return jContainPanel;
	}

	private JPanel getJDataPanel() {
		if (jDataPanel == null) {
			jDataPanel = new JPanel();
			jDataPanel.setLayout(new BoxLayout(getJDataPanel(), BoxLayout.Y_AXIS));
			jDataPanel.add(getJNamePanel());
			jDataPanel.add(getJAddressPanel());
			jDataPanel.add(getJCityPanel(), null);
			jDataPanel.add(getJTelePanel(), null);
			jDataPanel.add(getJFaxPanel(), null);
			jDataPanel.add(getJEmailPanel(), null);
			jDataPanel.add(getJCurrencyCodPanel(), null);
		}
		return jDataPanel;
	}

	private JPanel getJNamePanel() {
		if (jNamePanel == null) {
			jNamePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JLabel nameJLabel = new JLabel(MessageBundle.getMessage("angal.common.name.txt") + ": ");
			jNamePanel.add(nameJLabel);
			nameJTextField = new JTextField(25);
			nameJTextField.setEditable(false);
			jNamePanel.add(nameJTextField);
			nameJTextField.setText(hospital.getDescription());
		}
		return jNamePanel;
	}

	private JPanel getJAddressPanel() {
		if (jAddressPanel == null) {
			jAddressPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JLabel addJLabel = new JLabel(MessageBundle.getMessage("angal.common.address.txt") + ": ");
			jAddressPanel.add(addJLabel);
			addressJTextField = new JTextField(25);
			addressJTextField.setEditable(false);
			jAddressPanel.add(addressJTextField);
			addressJTextField.setText(hospital.getAddress());
		}
		return jAddressPanel;
	}

	private JPanel getJCityPanel() {
		if (jCityPanel == null) {
			jCityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JLabel cityJLabel = new JLabel(MessageBundle.getMessage("angal.common.city.txt") + ": ");
			jCityPanel.add(cityJLabel);
			cityJTextField = new JTextField(25);
			cityJTextField.setEditable(false);
			cityJTextField.setText(hospital.getCity());
			jCityPanel.add(cityJTextField);
		}
		return jCityPanel;
	}

	private JPanel getJTelePanel() {
		if (jTelePanel == null) {
			jTelePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JLabel teleJLabel = new JLabel(MessageBundle.getMessage("angal.common.telephone.txt") + ": ");
			jTelePanel.add(teleJLabel);
			teleJTextField = new JTextField(25);
			teleJTextField.setEditable(false);
			teleJTextField.setText(hospital.getTelephone());
			jTelePanel.add(teleJTextField);
		}
		return jTelePanel;
	}

	private JPanel getJFaxPanel() {
		if (jFaxPanel == null) {
			jFaxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JLabel faxJLabel = new JLabel(MessageBundle.getMessage("angal.hospital.faxnumber") + ": ");
			jFaxPanel.add(faxJLabel);
			faxJTextField = new JTextField(25);
			faxJTextField.setEditable(false);
			faxJTextField.setText(hospital.getFax());
			jFaxPanel.add(faxJTextField);
		}
		return jFaxPanel;
	}

	private JPanel getJEmailPanel() {
		if (jEmailPanel == null) {
			jEmailPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JLabel emailJLabel = new JLabel(MessageBundle.getMessage("angal.hospital.emailaddress") + ": ");
			jEmailPanel.add(emailJLabel);
			emailJTextField = new JTextField(25);
			emailJTextField.setEditable(false);
			emailJTextField.setText(hospital.getEmail());
			jEmailPanel.add(emailJTextField);
		}
		return jEmailPanel;
	}

	private JPanel getJCurrencyCodPanel() {
		if (jCurrencyCodPanel == null) {
			jCurrencyCodPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JLabel currencyCodJLabel = new JLabel(MessageBundle.getMessage("angal.hospital.currencycod") + ": ");
			jCurrencyCodPanel.add(currencyCodJLabel);
			currencyCodJTextField = new VoLimitedTextField(3, 25);
			currencyCodJTextField.setEditable(false);
			currencyCodJTextField.setText(hospital.getCurrencyCod());
			jCurrencyCodPanel.add(currencyCodJTextField);
		}
		return jCurrencyCodPanel;
	}
	
	private boolean isModified() {

		boolean change = false;

		if (!nameJTextField.getText().equalsIgnoreCase(hospital.getDescription()) 
						|| !addressJTextField.getText().equalsIgnoreCase(hospital.getAddress())
						|| !cityJTextField.getText().equalsIgnoreCase(hospital.getCity())
						|| !teleJTextField.getText().equalsIgnoreCase(hospital.getTelephone() == null ? "" : hospital.getTelephone())
						|| !faxJTextField.getText().equalsIgnoreCase(hospital.getFax() == null ? "" : hospital.getFax())
						|| !emailJTextField.getText().equalsIgnoreCase(hospital.getEmail() == null ? "" : hospital.getEmail())
						|| !currencyCodJTextField.getText().equalsIgnoreCase(hospital.getCurrencyCod() == null ? "" : hospital.getCurrencyCod())) {
			change = true;
		}

		return change;
	}

	private void saveConfirm() {
		int response = MessageDialog.yesNo(null, "angal.hospital.savethechanges.msg");
		if (response == JOptionPane.YES_OPTION) {
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
				if (isModified()) {
					//open confirm save window
					saveConfirm();
				}
				dispose();
			});
			editButton.addActionListener(actionEvent -> setFieldsForEditing(true));
			updateButton.addActionListener(actionEvent -> {
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
		currencyCodJTextField.setEditable(enabled);
		updateButton.setEnabled(enabled);
		editButton.setEnabled(!enabled);
		nameJTextField.requestFocus(); 
	}
	
	private void setHospitalEntity() {
		hospital.setDescription(nameJTextField.getText());
		hospital.setAddress(addressJTextField.getText());
		hospital.setCity(cityJTextField.getText());
		hospital.setTelephone(teleJTextField.getText().isEmpty() ? null : teleJTextField.getText());
		hospital.setFax(faxJTextField.getText().isEmpty() ? null : faxJTextField.getText());
		hospital.setEmail(emailJTextField.getText().isEmpty() ? null : emailJTextField.getText());
		hospital.setCurrencyCod(currencyCodJTextField.getText().isEmpty() ? null : currencyCodJTextField.getText());
	}
	
	private void updateHospitalEntity() {
		setHospitalEntity();
		try {
			this.hospital = manager.updateHospital(hospital);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
	}
}
