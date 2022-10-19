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
package org.isf.supplier.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.supplier.manager.SupplierBrowserManager;
import org.isf.supplier.model.Supplier;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.JLabelRequired;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

/**
 * This class allows suppliers edits and inserts
 * 
 * @author Mwithi
 */
public class SupplierEdit extends JDialog {

	private static final long serialVersionUID = 1L;
	private EventListenerList supplierListeners = new EventListenerList();
	
	public interface SupplierListener extends EventListener {
		void supplierUpdated(AWTEvent e);
		void supplierInserted(AWTEvent e);
	}
	
	public void addSupplierListener(SupplierListener l) {
		supplierListeners.add(SupplierListener.class, l);
	}
	
	public void removeSupplierListener(SupplierListener listener) {
		supplierListeners.remove(SupplierListener.class, listener);
	}

	private void fireSupplierInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = supplierListeners.getListeners(SupplierListener.class);
		for (EventListener listener : listeners)
			((SupplierListener) listener).supplierInserted(event);
	}

	private void fireSupplierUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = supplierListeners.getListeners(SupplierListener.class);
		for (EventListener listener : listeners)
			((SupplierListener) listener).supplierUpdated(event);
	}
	
	private int pfrmBase = 16;
	private int pfrmWidth = 5;
	private int pfrmHeight = 9;
	private int pfrmBordX;
	private int pfrmBordY;
	private JPanel jContentPane = null;
	private JPanel dataPanel = null;
	private JPanel buttonPanel = null;
	private JButton cancelButton = null;
	private JButton okButton = null;
	private JLabel nameLabel = null;
	private JLabel addressLabel = null;
	private JLabel idLabel = null;
	private JLabel taxcodeLabel = null;
	private JLabel phoneLabel = null;
	private JLabel faxLabel = null;
	private JLabel emailLabel = null;
	private JLabel noteLabel = null;
	private JLabel isDeletedLabel = null;
	private JLabel requiredLabel = null;
	private JTextField nameTextField = null;
	private JTextField idTextField = null;
	private JTextField addressTexField = null;
	private JTextField taxcodeTestField = null;
	private JTextField phoneTextField = null;
	private JTextField faxTextField = null;
	private JTextField emailTextField = null;
	private JTextField noteTextField = null;
	private JCheckBox isDeletedCheck = null;
	private Supplier supplier;
	private boolean insert;

	private SupplierBrowserManager supplierBrowserManager = Context.getApplicationContext().getBean(SupplierBrowserManager.class);
	
	/**
	 * This is the default constructor; we pass the parent frame
	 * (because it is a jdialog), the arraylist and the selected
	 * row because we need to update them
	 */
	public SupplierEdit(JFrame parent, Supplier old, boolean inserting) {
		super(parent, true);
		insert = inserting;
		supplier = old;        // supplier will be used for every operation
		initialize();
	}
	
	/**
	 * This method initializes this
	 */
	private void initialize() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();
		pfrmBordX = (screensize.width - (screensize.width / pfrmBase * pfrmWidth)) / 2;
		pfrmBordY = (screensize.height - (screensize.height / pfrmBase * pfrmHeight)) / 2;
		this.setBounds(pfrmBordX, pfrmBordY, screensize.width / pfrmBase * pfrmWidth, screensize.height / pfrmBase * pfrmHeight);
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.supplier.newsupplier.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.supplier.editsupplier.title"));
		}
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getDataPanel(), java.awt.BorderLayout.NORTH);
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
			idLabel = new JLabel(MessageBundle.getMessage("angal.common.id.txt") + ':');
			nameLabel = new JLabelRequired(MessageBundle.getMessage("angal.common.name.txt") + ':');
			addressLabel = new JLabel(MessageBundle.getMessage("angal.common.address.txt") + ':');
			taxcodeLabel = new JLabel(MessageBundle.getMessage("angal.supplier.taxcode") + ':');
			phoneLabel = new JLabel(MessageBundle.getMessage("angal.common.telephone.txt") + ':');
			faxLabel = new JLabel(MessageBundle.getMessage("angal.supplier.faxnumber") + ':');
			emailLabel = new JLabel(MessageBundle.getMessage("angal.supplier.email") + ':');
			noteLabel = new JLabel(MessageBundle.getMessage("angal.supplier.note") + ':');
			isDeletedLabel = new JLabel(MessageBundle.getMessage("angal.supplier.deleted") + ':');
			requiredLabel= new JLabel(MessageBundle.getMessage("angal.supplier.requiredfields"));
			dataPanel = new JPanel(new SpringLayout());
			dataPanel.add(idLabel);
			dataPanel.add(getIdTextField());
			dataPanel.add(nameLabel);
			dataPanel.add(getNameTextField());
			dataPanel.add(addressLabel);
			dataPanel.add(getAddressTextField());
			dataPanel.add(taxcodeLabel);
			dataPanel.add(getTaxcodeTextField());
			dataPanel.add(phoneLabel);
			dataPanel.add(getPhoneTextField());
			dataPanel.add(faxLabel);
			dataPanel.add(getFaxTextField());
			dataPanel.add(emailLabel);
			dataPanel.add(getEmailTextField());
			dataPanel.add(noteLabel);
			dataPanel.add(getNoteTextField());
			if (!insert) {
				dataPanel.add(isDeletedLabel);
				dataPanel.add(getIsDeleted());
			}
			dataPanel.add(requiredLabel);
			dataPanel.add(new JLabel(""));
			SpringUtilities.makeCompactGrid(dataPanel, insert ? 9 : 10, 2, 5, 5, 5, 5);
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
				if (nameTextField.getText().trim().isEmpty()) {
					MessageDialog.error(null, "angal.supplier.pleaseinsertaname");
					return;
				}

				supplier.setSupName(nameTextField.getText());
				supplier.setSupAddress(addressTexField.getText().trim());
				supplier.setSupTaxcode(taxcodeTestField.getText());
				supplier.setSupPhone(phoneTextField.getText());
				supplier.setSupFax(faxTextField.getText());
				supplier.setSupEmail(emailTextField.getText());
				supplier.setSupNote(noteTextField.getText());
				if (!insert) {
					supplier.setActive(isDeletedCheck.isSelected() ? 0 : 1);
				} else {
					supplier.setActive(1);
				}
				boolean result = false;
				if (insert) { // inserting
					try {
						result = supplierBrowserManager.saveOrUpdate(supplier);
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}
					if (result) {
						fireSupplierInserted();
					}
				} else { // updating
					try {
						result = supplierBrowserManager.saveOrUpdate(supplier);
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}
					if (result) {
						fireSupplierUpdated();
					}
				}
				if (!result) {
					MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
				} else {
					dispose();
				}
			});
		}
		return okButton;
	}
	
	/**
	 * This method initializes idTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getIdTextField() {
		if (idTextField == null) {
			idTextField = new VoLimitedTextField(11, 50);
			if (!insert) {				
				idTextField.setText(String.valueOf(supplier.getSupId()));
			}
			idTextField.setEnabled(false);
		}
		return idTextField;
	}
	
	/**
	 * This method initializes nameTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getNameTextField() {
		if (nameTextField == null) {
			nameTextField = new VoLimitedTextField(100, 50);
			if (!insert) {				
				nameTextField.setText(supplier.getSupName());
			}
		}
		return nameTextField;
	}
	
	/**
	 * This method initializes addressTexField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getAddressTextField() {
		if (addressTexField == null) {
			addressTexField = new VoLimitedTextField(150, 50);			
			if (!insert) {
				addressTexField.setText(supplier.getSupAddress());
			}
		}
		return addressTexField;
	}
	
	/**
	 * This method initializes taxcodeTestField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTaxcodeTextField() {
		if (taxcodeTestField == null) {
			taxcodeTestField = new VoLimitedTextField(50, 50);
			if (!insert) {
				taxcodeTestField.setText(supplier.getSupTaxcode());
			}
		}
		return taxcodeTestField;
	}
	
	/**
	 * This method initializes phoneTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getPhoneTextField() {
		if (phoneTextField == null) {
			phoneTextField = new VoLimitedTextField(20, 50);
			if (!insert) {
				phoneTextField.setText(supplier.getSupPhone());
			}
		}
		return phoneTextField;
	}
	
	/**
	 * This method initializes faxTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getFaxTextField() {
		if (faxTextField == null) {
			faxTextField = new VoLimitedTextField(20, 50);
			if (!insert) {
				faxTextField.setText(supplier.getSupFax());
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
			emailTextField = new VoLimitedTextField(100, 50);
			if (!insert) {
				emailTextField.setText(supplier.getSupEmail());
			}
		}
		return emailTextField;
	}
	
	/**
	 * This method initializes noteTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getNoteTextField() {
		if (noteTextField == null) {
			noteTextField = new VoLimitedTextField(200, 50);
			if (!insert) {
				noteTextField.setText(supplier.getSupNote());
			}
		}
		return noteTextField;
	}
	
	/**
	 * This method initializes isDeletedCheck
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getIsDeleted() {
		if (isDeletedCheck == null) {
			isDeletedCheck = new JCheckBox();
			if (!insert) {
				isDeletedCheck.setSelected(supplier.getActive().equals(0));
			}
		}
		return isDeletedCheck;
	}

}
