/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.admtype.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.EventListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

import org.isf.admtype.manager.AdmissionTypeBrowserManager;
import org.isf.admtype.model.AdmissionType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

public class AdmissionTypeBrowserEdit extends JDialog{

	private static final long serialVersionUID = 1L;
	private EventListenerList admissionTypeListeners = new EventListenerList();

    public interface LaboratoryTypeListener extends EventListener {
        void admissionTypeUpdated(AWTEvent e);
        void admissionTypeInserted(AWTEvent e);
    }

    public void addAdmissionTypeListener(LaboratoryTypeListener l) {
    	admissionTypeListeners.add(LaboratoryTypeListener.class, l);
    }

    public void removeAdmissionTypeListener(LaboratoryTypeListener listener) {
    	admissionTypeListeners.remove(LaboratoryTypeListener.class, listener);
    }

    private void fireAdmissionInserted(AdmissionType anAdmissionType) {
        AWTEvent event = new AWTEvent(anAdmissionType, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;};

        EventListener[] listeners = admissionTypeListeners.getListeners(LaboratoryTypeListener.class);
        for (int i = 0; i < listeners.length; i++)
            ((LaboratoryTypeListener)listeners[i]).admissionTypeInserted(event);
    }
    private void fireAdmissionUpdated() {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;};

        EventListener[] listeners = admissionTypeListeners.getListeners(LaboratoryTypeListener.class);
        for (int i = 0; i < listeners.length; i++)
            ((LaboratoryTypeListener)listeners[i]).admissionTypeUpdated(event);
    }
    
	private JPanel jContentPane = null;
	private JPanel dataPanel = null;
	private JPanel buttonPanel = null;
	private JButton cancelButton = null;
	private JButton okButton = null;
	private JTextField descriptionTextField = null;
	private VoLimitedTextField codeTextField = null;
	//private JTextField classTextField = null;
	private String lastdescription;
	private AdmissionType admissionType = null;
	private boolean insert;
	private JPanel jDataPanel = null;
	private JLabel jCodeLabel = null;
	private JPanel jCodeLabelPanel = null;
	private JPanel jDescriptionLabelPanel = null;
	private JLabel jDescriptionLabel = null;

	/**
	 * This is the default constructor; we pass the arraylist and the selectedrow
     * because we need to update them
	 */
	public AdmissionTypeBrowserEdit(JFrame owner,AdmissionType old,boolean inserting) {
		super(owner,true);
		insert = inserting;
		admissionType = old;//disease will be used for every operation
		lastdescription= admissionType.getDescription();
		initialize();
	}


	/**
	 * This method initializes this
	 */
	private void initialize() {
		
		//this.setBounds(400,400,350,170);
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.admtype.newadmissiontype.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.admtype.editadmissiontype.title"));
		}
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
			jContentPane.add(getDataPanel(), java.awt.BorderLayout.NORTH);  // Generated
			jContentPane.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);  // Generated
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
			//dataPanel.setLayout(new BoxLayout(getDataPanel(), BoxLayout.Y_AXIS));  // Generated
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
			buttonPanel.add(getOkButton(), null);  // Generated
			buttonPanel.add(getCancelButton(), null);  // Generated
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
					AdmissionTypeBrowserManager manager = Context.getApplicationContext().getBean(AdmissionTypeBrowserManager.class);

					if (descriptionTextField.getText().equals(lastdescription)){
						dispose();	
					}
				
					admissionType.setDescription(descriptionTextField.getText());
					admissionType.setCode(codeTextField.getText());
					boolean result = false;
					if (insert) {      // inserting
						try {
							result = manager.newAdmissionType(admissionType);
                            if (result) {
                                fireAdmissionInserted(admissionType);
                            }
                            if (!result) {
	                            MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
                            } else {
                            	dispose();
                            }
						} catch(OHServiceException ex){
							OHServiceExceptionUtil.showMessages(ex);
						}
                    }
                    else {                          // updating
                    	if (descriptionTextField.getText().equals(lastdescription)){
    						dispose();	
    					} else {
    						try {
								result = manager.updateAdmissionType(admissionType);
                                if (result) {
                                    fireAdmissionUpdated();
                                }
                                if (!result) {
	                                MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
                                } else {
                                	dispose();
                                }
    						} catch(OHServiceException ex){
    							OHServiceExceptionUtil.showMessages(ex);
    						}
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
				descriptionTextField.setText(admissionType.getDescription());
				lastdescription=admissionType.getDescription();
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
			codeTextField = new VoLimitedTextField(10);
			if (!insert) {
				codeTextField.setText(admissionType.getCode());
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
			jDataPanel = new JPanel();
			jDataPanel.setLayout(new BoxLayout(getJDataPanel(),BoxLayout.Y_AXIS));
			jDataPanel.add(getJCodeLabelPanel(), null);
			jDataPanel.add(getCodeTextField(), null);
			jDataPanel.add(getJDescriptionLabelPanel(), null);
			jDataPanel.add(getDescriptionTextField(), null);
		}
		return jDataPanel;
	}

	/**
	 * This method initializes jCodeLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getJCodeLabel() {
		if (jCodeLabel == null) {
			jCodeLabel = new JLabel();
			jCodeLabel.setText(MessageBundle.formatMessage("angal.common.codemaxchars.fmt.txt", 10));
		}
		return jCodeLabel;
	}

	/**
	 * This method initializes jCodeLabelPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJCodeLabelPanel() {
		if (jCodeLabelPanel == null) {
			jCodeLabelPanel = new JPanel();
			//jCodeLabelPanel.setLayout(new BorderLayout());
			jCodeLabelPanel.add(getJCodeLabel(), BorderLayout.CENTER);
		}
		return jCodeLabelPanel;
	}

	/**
	 * This method initializes jDescriptionLabelPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJDescriptionLabelPanel() {
		if (jDescriptionLabelPanel == null) {
			jDescriptionLabel = new JLabel();
			jDescriptionLabel.setText(MessageBundle.getMessage("angal.common.description.txt"));
			jDescriptionLabelPanel = new JPanel();
			jDescriptionLabelPanel.add(jDescriptionLabel, null);
		}
		return jDescriptionLabelPanel;
	}

}


