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
package org.isf.medtype.gui;

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

import org.isf.generaldata.MessageBundle;
import org.isf.medtype.manager.MedicalTypeBrowserManager;
import org.isf.medtype.model.MedicalType;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

public class MedicalTypeBrowserEdit extends JDialog{

	private static final long serialVersionUID = 1L;
	private EventListenerList medicalTypeListeners = new EventListenerList();

    public interface MedicalTypeListener extends EventListener {
        void medicalTypeUpdated(AWTEvent e);
        void medicalTypeInserted(AWTEvent e);
    }

    public void addMedicalTypeListener(MedicalTypeListener l) {
        medicalTypeListeners.add(MedicalTypeListener.class, l);
    }

    public void removeMedicalTypeListener(MedicalTypeListener listener) {
        medicalTypeListeners.remove(MedicalTypeListener.class, listener);
    }

    private void fireMedicalInserted() {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;};

        EventListener[] listeners = medicalTypeListeners.getListeners(MedicalTypeListener.class);
        for (int i = 0; i < listeners.length; i++)
            ((MedicalTypeListener)listeners[i]).medicalTypeInserted(event);
    }
    private void fireMedicalUpdated() {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;};

        EventListener[] listeners = medicalTypeListeners.getListeners(MedicalTypeListener.class);
        for (int i = 0; i < listeners.length; i++)
            ((MedicalTypeListener)listeners[i]).medicalTypeUpdated(event);
    }
    
	private JPanel jContentPane = null;
	private JPanel dataPanel = null;
	private JPanel buttonPanel = null;
	private JButton cancelButton = null;
	private JButton okButton = null;
	private JTextField descriptionTextField = null;
	private VoLimitedTextField codeTextField = null;
	private String lastdescription;
	private MedicalType medicalType = null;
	private boolean insert;
	private JPanel jDataPanel = null;
	private JLabel jCodeLabel = null;
	private JPanel jCodeLabelPanel = null;
	private JPanel jDescriptionLabelPanel = null;
	private JLabel jDescriptionLabel = null;

	private MedicalTypeBrowserManager manager = Context.getApplicationContext().getBean(MedicalTypeBrowserManager.class);

	/**
	 * This is the default constructor; we pass the arraylist and the selectedrow
     * because we need to update them
	 */
	public MedicalTypeBrowserEdit(JFrame owner,MedicalType old,boolean inserting) {
		super(owner,true);
		insert = inserting;
		medicalType = old;//disease will be used for every operation
		lastdescription= medicalType.getDescription();
		initialize();
	}


	/**
	 * This method initializes this
	 */
	private void initialize() {
		
//		this.setBounds(300,300,350,180);
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.medtype.newmedicaltype.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.medtype.editmedicaltype.title"));
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

					medicalType.setDescription(descriptionTextField.getText());
					medicalType.setCode(codeTextField.getText());

					boolean result;
					if (insert) { // inserting
						try {
							result = manager.newMedicalType(medicalType);
							if (result) {
								fireMedicalInserted();
								dispose();
							} else {
								MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
							}
						} catch (OHServiceException e1) {
							result = false;
							OHServiceExceptionUtil.showMessages(e1);
						}
					} else { // updating
						if (descriptionTextField.getText().equals(lastdescription)) {
							dispose();
						} else {
							try {
								result = manager.updateMedicalType(medicalType);
								if (result) {
									fireMedicalUpdated();
									dispose();
								} else {
									MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
								}
							} catch (OHServiceException e1) {
								result = false;
								OHServiceExceptionUtil.showMessages(e1);
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
				descriptionTextField.setText(medicalType.getDescription());
				lastdescription=medicalType.getDescription();
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
			codeTextField = new VoLimitedTextField(1);
			if (!insert) {
				codeTextField.setText(medicalType.getCode());
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
			jCodeLabel = new JLabel(MessageBundle.getMessage("angal.common.codemax1char.txt"));
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
			jDescriptionLabel = new JLabel(MessageBundle.getMessage("angal.common.description.txt"));
			jDescriptionLabelPanel = new JPanel();
			jDescriptionLabelPanel.add(jDescriptionLabel, null);
		}
		return jDescriptionLabelPanel;
	}

}
