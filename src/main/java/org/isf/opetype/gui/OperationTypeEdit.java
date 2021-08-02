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
package org.isf.opetype.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.opetype.manager.OperationTypeBrowserManager;
import org.isf.opetype.model.OperationType;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

public class OperationTypeEdit extends JDialog{

	private static final long serialVersionUID = 1L;
	private EventListenerList operationTypeListeners = new EventListenerList();

    public interface OperationTypeListener extends EventListener {
        void operationTypeUpdated(AWTEvent e);
        void operationTypeInserted(AWTEvent e);
    }

    public void addOperationTypeListener(OperationTypeListener l) {
        operationTypeListeners.add(OperationTypeListener.class, l);
    }

    public void removeOperationTypeListener(OperationTypeListener listener) {
        operationTypeListeners.remove(OperationTypeListener.class, listener);
    }

    private void fireOperationInserted() {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;};

        EventListener[] listeners = operationTypeListeners.getListeners(OperationTypeListener.class);
	    for (EventListener listener : listeners) {
		    ((OperationTypeListener) listener).operationTypeInserted(event);
	    }
    }
    private void fireOperationUpdated() {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;};

        EventListener[] listeners = operationTypeListeners.getListeners(OperationTypeListener.class);
	    for (EventListener listener : listeners) {
		    ((OperationTypeListener) listener).operationTypeUpdated(event);
	    }
    }
    
	private JPanel jContentPane = null;
	private JPanel dataPanel = null;
	private JPanel buttonPanel = null;
	private JButton cancelButton = null;
	private JButton okButton = null;
	private JTextField descriptionTextField = null;
	private VoLimitedTextField codeTextField = null;
	private String lastdescription;
	private OperationType operationType;
	private boolean insert;
	private JPanel jDataPanel = null;

	/**
	 * This is the default constructor; we pass the arraylist and the selectedrow
     * because we need to update them
	 */
	public OperationTypeEdit(JFrame owner, OperationType old, boolean inserting) {
		super(owner,true);
		insert = inserting;
		operationType = old;//operation will be used for every operation
		lastdescription= operationType.getDescription();
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.opetype.newoperationtype.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.opetype.editoperationtype.title"));
		}
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
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
			dataPanel = new JPanel();
			dataPanel.add(getJDataPanel(), null);
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
			cancelButton.addActionListener(e -> dispose());
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
			okButton.addActionListener(e -> {
				OperationTypeBrowserManager manager = Context.getApplicationContext().getBean(OperationTypeBrowserManager.class);
				if (descriptionTextField.getText().equals(lastdescription)) {
					dispose();
				}
				operationType.setDescription(descriptionTextField.getText());
				operationType.setCode(codeTextField.getText());
				boolean result = false;
				if (insert) {      // inserting
					try {
						result = manager.newOperationType(operationType);
					} catch (OHServiceException e1) {
						result = false;
						OHServiceExceptionUtil.showMessages(e1);
					}
					if (result) {
						fireOperationInserted();
					}
					if (!result) {
						MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
					} else {
						dispose();
					}
				} else {                          // updating
					if (descriptionTextField.getText().equals(lastdescription)) {
						dispose();
					} else {
						try {
							result = manager.updateOperationType(operationType);
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1);
							result = false;
						}
						if (result) {
							fireOperationUpdated();
						}
						if (!result) {
							MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
						} else {
							dispose();
						}
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
			descriptionTextField = new JTextField(20);
			if (!insert) {
				descriptionTextField.setText(operationType.getDescription());
				lastdescription=operationType.getDescription();
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
			codeTextField = new VoLimitedTextField(2);
			if (!insert) {
				codeTextField.setText(operationType.getCode());
				codeTextField.setEnabled(false);
			}
		}
		return codeTextField;
	}

	/**
	 * This method initializes jDataPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJDataPanel() {
		if (jDataPanel == null) {
			jDataPanel = new JPanel(new SpringLayout());
			jDataPanel.add(new JLabel(MessageBundle.formatMessage("angal.common.codemaxchars.fmt.txt", 2) + ':'));
			jDataPanel.add(getCodeTextField());
			jDataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ':'));
			jDataPanel.add(getDescriptionTextField());
			SpringUtilities.makeCompactGrid(jDataPanel, 2, 2, 5, 5, 5, 5);
		}
		return jDataPanel;
	}
}
