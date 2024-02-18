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
package org.isf.menu.gui;

import java.awt.AWTEvent;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.UserGroup;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.layout.SpringUtilities;

public class GroupEdit extends JDialog {

	private static final long serialVersionUID = 1L;
	private EventListenerList groupListeners = new EventListenerList();

	private UserBrowsingManager userBrowsingManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);

    public interface GroupListener extends EventListener {
        void groupUpdated(AWTEvent e);
        void groupInserted(AWTEvent e);
    }

    public void addGroupListener(GroupListener l) {
    	groupListeners.add(GroupListener.class, l);
    }

    public void removeGroupListener(GroupListener listener) {
    	groupListeners.remove(GroupListener.class, listener);
    }

	private void fireGroupInserted(UserGroup aGroup) {
		AWTEvent event = new AWTEvent(aGroup, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = groupListeners.getListeners(GroupListener.class);
		for (EventListener listener : listeners) {
			((GroupListener) listener).groupInserted(event);
		}
	}

	private void fireGroupUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = groupListeners.getListeners(GroupListener.class);
		for (EventListener listener : listeners) {
			((GroupListener) listener).groupUpdated(event);
		}
	}
    
	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JTextField descriptionTextField;
	private JTextField nameTextField;
    
	private UserGroup group;
	private boolean insert;
    
	/**
	 * This is the default constructor; we pass the arraylist and the selectedrow
     * because we need to update them
	 */
	public GroupEdit(UserGroupBrowsing parent, UserGroup old, boolean inserting) {
		super(parent, inserting
				? MessageBundle.getMessage("angal.groupsbrowser.newgroup.title")
				: MessageBundle.getMessage("angal.groupsbrowser.editgroup.title"), true);
		addGroupListener(parent);
		insert = inserting;
		group = old;
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {

		this.setBounds(300, 300, 450, 150);
		this.setContentPane(getJContentPane());

		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new SpringLayout());
			jContentPane.add(getDataPanel());
			jContentPane.add(getButtonPanel());
			SpringUtilities.makeCompactGrid(jContentPane, 2, 1, 5, 5, 5, 5);
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
			JLabel nameLabel = new JLabel(MessageBundle.getMessage("angal.groupsbrowser.groupname.label"));
			JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.groupsbrowser.description.label"));
			dataPanel = new JPanel(new SpringLayout());
			dataPanel.add(nameLabel);
			dataPanel.add(getNameTextField());
			  
			dataPanel.add(descLabel);
			dataPanel.add(getDescriptionTextField());
			SpringUtilities.makeCompactGrid(dataPanel, 2, 2, 5, 5, 5, 5);
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
				if (nameTextField.getText().isEmpty()) {
					MessageDialog.error(null, MessageBundle.getMessage("angal.groupsbrowser.pleaseinsertavalidusergroupname.msg"));
					return;
				}

				group.setCode(nameTextField.getText());

				group.setDesc(descriptionTextField.getText());
				if (insert) {      // inserting
					try {
						userBrowsingManager.newUserGroup(group);
						fireGroupInserted(group);
						dispose();
					} catch (OHServiceException e1) {
						MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
						OHServiceExceptionUtil.showMessages(e1);
					}
				} else {         // updating
					try {
						userBrowsingManager.updateUserGroup(group);
						fireGroupUpdated();
						dispose();
					} catch (OHServiceException e1) {
						MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
						OHServiceExceptionUtil.showMessages(e1);
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
			if (insert) {
				descriptionTextField = new JTextField();
			} else {
				descriptionTextField = new JTextField(group.getDesc());
			}
		}
		return descriptionTextField;
	}

	private JTextField getNameTextField() {
		if (nameTextField == null) {
			if (insert) {
				nameTextField = new JTextField();
			} else {
				nameTextField = new JTextField(group.getCode());
				nameTextField.setEditable(false);
			}
		}
		return nameTextField;
	}

}
