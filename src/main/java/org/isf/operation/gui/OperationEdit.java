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
package org.isf.operation.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.opetype.manager.OperationTypeBrowserManager;
import org.isf.opetype.model.OperationType;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

/**
 * This class allows operations edits and inserts
 *
 * @author Rick, Vero, Pupo
 * ----------------------------------------------------------
 * modification history
 * ====================
 * 13/02/09 - Alex - added Major/Minor control
 * -----------------------------------------------------------
 */
public class OperationEdit extends JDialog {

	private static final long serialVersionUID = 1L;
	private EventListenerList operationListeners = new EventListenerList();

	public interface OperationListener extends EventListener {
		void operationUpdated(AWTEvent e);

		void operationInserted(AWTEvent e);
	}

	public void addOperationListener(OperationListener l) {
		operationListeners.add(OperationListener.class, l);
	}

	public void removeOperationListener(OperationListener listener) {
		operationListeners.remove(OperationListener.class, listener);
	}

	private void fireOperationInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = operationListeners.getListeners(OperationListener.class);
		for (int i = 0; i < listeners.length; i++)
			((OperationListener) listeners[i]).operationInserted(event);
	}

	private void fireOperationUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = operationListeners.getListeners(OperationListener.class);
		for (int i = 0; i < listeners.length; i++)
			((OperationListener) listeners[i]).operationUpdated(event);
	}

	private JPanel jContentPane = null;
	private JPanel dataPanel = null;
	private JPanel buttonPanel = null;
	private JButton cancelButton = null;
	private JButton okButton = null;
	private JLabel descLabel = null;
	private JLabel codeLabel = null;
	private JTextField descriptionTextField = null;
	private JTextField codeTextField = null;
	private JLabel typeLabel = null;
	private JComboBox typeComboBox = null;
	private String lastdescription;
	private Operation operation = null;
	private JRadioButton major = null;
	private JRadioButton minor = null;
	private JPanel radioButtonPanel;
	private JLabel operForLabel=null;
	private boolean insert = false;
	private JComboBox operBox;

	/**
	 * This is the default constructor; we pass the arraylist and the selectedrow
	 * because we need to update them
	 */
	public OperationEdit(JFrame parent, Operation old, boolean inserting) {
		super(parent, true);
		insert = inserting;
		operation = old; // operation will be used for every operation
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {

		// Toolkit kit = Toolkit.getDefaultToolkit();
		// Dimension screensize = kit.getScreenSize();
		// pfrmBordX = (screensize.width - (screensize.width / pfrmBase * pfrmWidth)) /
		// 2;
		// pfrmBordY = (screensize.height - (screensize.height / pfrmBase * pfrmHeight))
		// / 2;
		// this.setBounds(pfrmBordX,pfrmBordY,screensize.width / pfrmBase *
		// pfrmWidth,screensize.height / pfrmBase * pfrmHeight);
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.operation.newoperation.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.operation.editoperation.title"));
		}
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getDataPanel(), java.awt.BorderLayout.NORTH); // Generated
			jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH); // Generated
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
			typeLabel = new JLabel();
			typeLabel.setText(MessageBundle.getMessage("angal.operation.type")); // Generated //$NON-NLS-1$
			typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			descLabel = new JLabel(MessageBundle.getMessage("angal.common.description.txt"));
			descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			codeLabel = new JLabel();
			codeLabel.setText(MessageBundle.getMessage("angal.common.code.txt"));
			codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			operForLabel = new JLabel();
			operForLabel.setText(MessageBundle.getMessage("angal.operation.operationcontext")); //$NON-NLS-1$
			operForLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			dataPanel = new JPanel();
			dataPanel.setLayout(new BoxLayout(getDataPanel(), BoxLayout.Y_AXIS)); // Generated
			dataPanel.add(typeLabel, null); // Generated
			dataPanel.add(getTypeComboBox(), null); // Generated
			dataPanel.add(codeLabel, null); // Generated
			dataPanel.add(getCodeTextField(), null); // Generated
			dataPanel.add(descLabel, null); // Generated
			dataPanel.add(getDescriptionTextField(), null); // Generated
			dataPanel.add(getRadioButtonPanel());
			dataPanel.add(operForLabel, null); // Generated
			dataPanel.add(getOperFor(), null); 
		}
		return dataPanel;
	}

	private JComboBox getOperFor() {
		
		operBox = new JComboBox();
		//TODO: replace integer values with mnemonic ones
		operBox.addItem(OperationBrowser.OPD_ADMISSION); 	// = "1"
		operBox.addItem(OperationBrowser.ADMISSION);		// = "2"
		operBox.addItem(OperationBrowser.OPD);				// = "3"
		
		if (!insert) {
			int index = operation.getOpeFor().equals("1") ? 0
							: operation.getOpeFor().equals("2") ? 1
											: operation.getOpeFor().equals("3") ? 2 
															: 0; // default
			operBox.setSelectedIndex(index);
		}

		return operBox;

	}

	/**
	 * This method initializes buttonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.add(getOkButton(), null); // Generated
			buttonPanel.add(getCancelButton(), null); // Generated
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
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
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
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						if (insert) {
							String key = codeTextField.getText().trim();
							if (key.equals("")) {
								MessageDialog.error(null, "angal.common.pleaseinsertacode.msg");
								return;
							}
							if (key.length() > 10) {
								MessageDialog.error(null, "angal.common.thecodeistoolongmaxchars.fmt.msg", 10);
								return;
							}
							OperationBrowserManager manager = Context.getApplicationContext().getBean(OperationBrowserManager.class);

							if (manager.isCodePresent(key)) {
								MessageDialog.error(null, "angal.common.thecodeisalreadyinuse.msg");
								return;
							}
						}
						if (descriptionTextField.getText().equals("")) {
							MessageDialog.error(null, "angal.common.pleaseinsertavaliddescription.msg");
							return;
						}
						OperationBrowserManager manager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
						if (descriptionTextField.getText().equals(lastdescription)) {
						} else {

							if (manager.descriptionControl(descriptionTextField.getText(),
									((OperationType) typeComboBox.getSelectedItem()).getCode())) {
								MessageDialog.error(null, "angal.operation.operationalreadypresent");
								return;
							}
						}
						String opeForSelection = String.valueOf(operBox.getSelectedIndex()+1);
						operation.setOpeFor(opeForSelection);
						operation.setType((OperationType) typeComboBox.getSelectedItem());
						operation.setDescription(descriptionTextField.getText());
						operation.setCode(codeTextField.getText().trim().toUpperCase());
						if (major.isSelected()) {
							operation.setMajor(1);
						} else {
							operation.setMajor(0);
						}

						boolean result = false;
						if (insert) { // inserting
							result = manager.newOperation(operation);
							if (result) {
								fireOperationInserted();
							}
						} else { // updating
							result = manager.updateOperation(operation);
							if (result) {
								fireOperationUpdated();
							}
						}
						if (!result) {
							MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
						}
						else {
							dispose();
						}
					} catch (OHServiceException ex) {
						OHServiceExceptionUtil.showMessages(ex);
					}
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
			descriptionTextField = new VoLimitedTextField(50, 50);
			if (!insert) {
				lastdescription = operation.getDescription();
				descriptionTextField.setText(lastdescription);
			}
		}
		return descriptionTextField;
	}

	/**
	 * This method initializes radioButtonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getRadioButtonPanel() {
		if (radioButtonPanel == null) {

			radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			if (major == null) {

				major = getRadioButton(MessageBundle.getMessage("angal.operation.major"), true);
				minor = getRadioButton(MessageBundle.getMessage("angal.operation.minor"), true);

				ButtonGroup radioGroup = new ButtonGroup();

				radioGroup.add(major);
				radioGroup.add(minor);

				radioButtonPanel.add(major);
				radioButtonPanel.add(minor);

				if (insert) {
					major.setSelected(true);
				} else {
					if (operation.getMajor() == 1) {
						major.setSelected(true);
					} else {
						minor.setSelected(true);
					}
				}
			}

		}
		return radioButtonPanel;
	}

	private JRadioButton getRadioButton(String label, boolean active) {
		JRadioButton rb = new JRadioButton(label);
		rb.setSelected(active);
		rb.setName(label);
		return rb;
	}

	/**
	 * This method initializes codeTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getCodeTextField() {
		if (codeTextField == null) {
			codeTextField = new VoLimitedTextField(10, 50);
			if (!insert) {
				codeTextField.setText(operation.getCode());
				codeTextField.setEnabled(false);
			}
		}
		return codeTextField;
	}

	/**
	 * This method initializes typeComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getTypeComboBox() {
		if (typeComboBox == null) {
			typeComboBox = new JComboBox();
			if (insert) {
				OperationTypeBrowserManager manager = Context.getApplicationContext().getBean(OperationTypeBrowserManager.class);
				ArrayList<OperationType> types;
				try {
					types = manager.getOperationType();

					for (OperationType elem : types) {
						typeComboBox.addItem(elem);
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
					types = null;
				}
			} else {
				typeComboBox.addItem(operation.getType());
				typeComboBox.setEnabled(false);
			}

		}
		return typeComboBox;
	}

}
