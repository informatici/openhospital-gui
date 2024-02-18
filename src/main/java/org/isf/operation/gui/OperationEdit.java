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
package org.isf.operation.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.EventListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
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
import org.isf.utils.layout.SpringUtilities;

/**
 * This class allows operations edits and inserts
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
		for (EventListener listener : listeners) {
			((OperationListener) listener).operationInserted(event);
		}
	}

	private void fireOperationUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = operationListeners.getListeners(OperationListener.class);
		for (EventListener listener : listeners) {
			((OperationListener) listener).operationUpdated(event);
		}
	}

	private OperationBrowserManager operationBrowserManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
	private OperationTypeBrowserManager operationTypeBrowserManager = Context.getApplicationContext().getBean(OperationTypeBrowserManager.class);

	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JTextField descriptionTextField;
	private JTextField codeTextField;
	private JComboBox<OperationType> operationTypeComboBox;
	private String lastdescription;
	private Operation operation;
	private JRadioButton major;
	private JPanel radioButtonPanel;
	private boolean insert;
	private JComboBox<String> operBox;

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
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.operation.newoperation.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.operation.editoperation.title"));
		}
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
			jContentPane.add(getDataPanel(), BorderLayout.NORTH);
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
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
			JLabel typeLabel = new JLabel(MessageBundle.getMessage("angal.operation.type") + ':');
			JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ':');
			JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ':');
			JLabel operForLabel = new JLabel(MessageBundle.getMessage("angal.operation.operationcontext") + ':');

			dataPanel = new JPanel(new SpringLayout());
			dataPanel.add(typeLabel);
			dataPanel.add(getOperationTypeComboBox());
			dataPanel.add(codeLabel);
			dataPanel.add(getCodeTextField());
			dataPanel.add(descLabel);
			dataPanel.add(getDescriptionTextField());
			dataPanel.add(new JLabel(""));
			dataPanel.add(getRadioButtonPanel());
			dataPanel.add(operForLabel);
			dataPanel.add(getOperFor());
			SpringUtilities.makeCompactGrid(dataPanel, 5, 2, 5, 5, 5, 5);
		}
		return dataPanel;
	}

	private JComboBox<String> getOperFor() {
		
		operBox = new JComboBox<>();
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

						if (operationBrowserManager.isCodePresent(key)) {
							MessageDialog.error(null, "angal.common.thecodeisalreadyinuse.msg");
							return;
						}
					}
					if (descriptionTextField.getText().equals("")) {
						MessageDialog.error(null, "angal.common.pleaseinsertavaliddescription.msg");
						return;
					}
					if (descriptionTextField.getText().equals(lastdescription)) {
					} else {

						if (operationBrowserManager.descriptionControl(descriptionTextField.getText(),
								((OperationType) operationTypeComboBox.getSelectedItem()).getCode())) {
							MessageDialog.error(null, "angal.operation.operationalreadypresent");
							return;
						}
					}
					String opeForSelection = String.valueOf(operBox.getSelectedIndex()+1);
					operation.setOpeFor(opeForSelection);
					operation.setType((OperationType) operationTypeComboBox.getSelectedItem());
					operation.setDescription(descriptionTextField.getText());
					operation.setCode(codeTextField.getText().trim().toUpperCase());
					if (major.isSelected()) {
						operation.setMajor(1);
					} else {
						operation.setMajor(0);
					}

					boolean result = false;
					if (insert) { // inserting
						Operation insertedOperation = operationBrowserManager.newOperation(operation);
						if (insertedOperation != null) {
							result = true;
							fireOperationInserted();
						}
					} else { // updating
						Operation updatedOperation = operationBrowserManager.updateOperation(operation);
						if (updatedOperation != null) {
							result = true;
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
				JRadioButton minor = getRadioButton(MessageBundle.getMessage("angal.operation.minor"), true);

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
	 * This method initializes operationTypeComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox<OperationType> getOperationTypeComboBox() {
		if (operationTypeComboBox == null) {
			operationTypeComboBox = new JComboBox<>();
			try {
				List<OperationType> types = operationTypeBrowserManager.getOperationType();
				if (insert) {
					if (types != null) {
						for (OperationType elem : types) {
							operationTypeComboBox.addItem(elem);
						}
					}
				} else {
					OperationType selectedOperationType = null;
					if (types != null) {
						for (OperationType elem : types) {
							operationTypeComboBox.addItem(elem);
							if (operation.getType().equals(elem)) {
								selectedOperationType = elem;
							}
						}
					} if (selectedOperationType != null) {
						operationTypeComboBox.setSelectedItem(selectedOperationType);
					}
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);

			}
		}
		return operationTypeComboBox;
	}

}
