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
package org.isf.dicomtype.gui;

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

import org.isf.dicomtype.manager.DicomTypeBrowserManager;
import org.isf.dicomtype.model.DicomType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

public class DicomTypeEdit extends JDialog {

	private static final long serialVersionUID = 1L;
	private EventListenerList dicomTypeListeners = new EventListenerList();

    public interface DicomTypeListener extends EventListener {
        void dicomTypeUpdated(AWTEvent e);
        void dicomTypeInserted(AWTEvent e);
    }

    public void addDicomTypeListener(DicomTypeListener l) {
    	dicomTypeListeners.add(DicomTypeListener.class, l);
    }

    public void removeDicomTypeListener(DicomTypeListener listener) {
    	dicomTypeListeners.remove(DicomTypeListener.class, listener);
    }

	private void fireDicomTypeInserted(DicomType anDicomType) {
		AWTEvent event = new AWTEvent(anDicomType, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = dicomTypeListeners.getListeners(DicomTypeListener.class);
		for (EventListener listener : listeners) {
			((DicomTypeListener) listener).dicomTypeInserted(event);
		}
	}

	private void fireDicomUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = dicomTypeListeners.getListeners(DicomTypeListener.class);
		for (EventListener listener : listeners) {
			((DicomTypeListener) listener).dicomTypeUpdated(event);
		}
	}

	private DicomTypeBrowserManager dicomTypeBrowserManager = Context.getApplicationContext().getBean(DicomTypeBrowserManager.class);

	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JTextField descriptionTextField;
	private VoLimitedTextField codeTextField;
	private String lastdescription;
	private DicomType dicomType;
	private boolean insert;
	private JPanel jDataPanel;

	/**
	 * This is the default constructor; we pass the arraylist and the selectedrow
     * because we need to update them
	 */
	public DicomTypeEdit(JFrame owner, DicomType old, boolean inserting) {
		super(owner, true);
		insert = inserting;
		dicomType = old;
		lastdescription = dicomType.getDicomTypeDescription();
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		
		setContentPane(getJContentPane());
		if (insert) {
			setTitle(MessageBundle.getMessage("angal.dicomtype.newdicomtype.title"));
		} else {
			setTitle(MessageBundle.getMessage("angal.dicomtype.editdicomtype.title"));
		}
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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

				dicomType.setDicomTypeDescription(descriptionTextField.getText());
				dicomType.setDicomTypeID(codeTextField.getText());
				if (insert) {    // inserting
					try {
						dicomTypeBrowserManager.newDicomType(dicomType);
						fireDicomTypeInserted(dicomType);
						dispose();
					} catch (OHServiceException e1) {
						OHServiceExceptionUtil.showMessages(e1, this);
					}
				} else {                // updating
					if (descriptionTextField.getText().equals(lastdescription)) {
						dispose();
					} else {
						try {
							dicomTypeBrowserManager.updateDicomType(dicomType);
							fireDicomUpdated();
							dispose();
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1, this);
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
				descriptionTextField.setText(dicomType.getDicomTypeDescription());
				lastdescription=dicomType.getDicomTypeDescription();
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
				codeTextField.setText(dicomType.getDicomTypeID());
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
			jDataPanel.add(new JLabel(MessageBundle.formatMessage("angal.common.codemaxchars.fmt.txt", 3) + ':'));
			jDataPanel.add(getCodeTextField());
			jDataPanel.add(new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ':'));
			jDataPanel.add(getDescriptionTextField());
			SpringUtilities.makeCompactGrid(jDataPanel, 2, 2, 5, 5, 5, 5);
		}
		return jDataPanel;
	}
}
