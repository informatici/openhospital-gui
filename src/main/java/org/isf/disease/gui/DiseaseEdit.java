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
package org.isf.disease.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.EventListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;

import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.distype.manager.DiseaseTypeBrowserManager;
import org.isf.distype.model.DiseaseType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;

/**
 * DiseaseEdit - Add/edit a Disease
 */
public class DiseaseEdit extends JDialog {
	
	private static final long serialVersionUID = 1L;

	private EventListenerList diseaseListeners = new EventListenerList();
	
	public interface DiseaseListener extends EventListener {
		void diseaseUpdated(AWTEvent e);
		void diseaseInserted(AWTEvent e);
	}
	
	public void addDiseaseListener(DiseaseListener l) {
		diseaseListeners.add(DiseaseListener.class, l);
	}
	
	public void removeDiseaseListener(DiseaseListener listener) {
		diseaseListeners.remove(DiseaseListener.class, listener);
	}

	private void fireDiseaseInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = diseaseListeners.getListeners(DiseaseListener.class);
		for (EventListener listener : listeners) {
			((DiseaseListener) listener).diseaseInserted(event);
		}
	}

	private void fireDiseaseUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = diseaseListeners.getListeners(DiseaseListener.class);
		for (EventListener listener : listeners) {
			((DiseaseListener) listener).diseaseUpdated(event);
		}
	}
	
	private JPanel jContentPane;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton okButton;
	private JTextField descriptionTextField;
	private JTextField codeTextField;
	private JComboBox<DiseaseType> diseaseTypeComboBox;
	private Disease disease;
	private boolean insert;
	private JPanel jNewPatientPanel;
	private JCheckBox includeOpdCheckBox;
	private JCheckBox includeIpdInCheckBox;
	private JCheckBox includeIpdOutCheckBox;

	private DiseaseTypeBrowserManager diseaseTypeBrowserManager = Context.getApplicationContext().getBean(DiseaseTypeBrowserManager.class);
	private DiseaseBrowserManager diseaseBrowserManager = Context.getApplicationContext().getBean(DiseaseBrowserManager.class);

	/**
	 * This is the default constructor; we pass the arraylist and the selectedrow
	 * because we need to update them
	 */
	public DiseaseEdit(JFrame parent, Disease old, boolean inserting) {
		super(parent, true);
		insert = inserting;
		disease = old;        //disease will be used for every operation
		initialize();
	}
	
	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.disease.newdisease.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.disease.editdisease.title"));
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
			jContentPane.add(getDataPanel(), BorderLayout.CENTER);
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
			GridBagLayout gblDataPanel = new GridBagLayout();
			gblDataPanel.columnWidths = new int[]{0, 0, 0};
			gblDataPanel.rowHeights = new int[]{31, 31, 31, 31, 0};
			gblDataPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gblDataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			dataPanel.setLayout(gblDataPanel);
			JLabel typeLabel = new JLabel(MessageBundle.getMessage("angal.disease.type"));
			GridBagConstraints gbcTypeLabel = new GridBagConstraints();
			gbcTypeLabel.fill = GridBagConstraints.BOTH;
			gbcTypeLabel.insets = new Insets(5, 5, 5, 5);
			gbcTypeLabel.gridx = 0;
			gbcTypeLabel.gridy = 0;
			dataPanel.add(typeLabel, gbcTypeLabel);
			GridBagConstraints gbctypeComboBox = new GridBagConstraints();
			gbctypeComboBox.fill = GridBagConstraints.BOTH;
			gbctypeComboBox.insets = new Insets(5, 5, 5, 5);
			gbctypeComboBox.gridx = 1;
			gbctypeComboBox.gridy = 0;
			dataPanel.add(getDiseaseTypeComboBox(), gbctypeComboBox);
			JLabel codeLabel = new JLabel(MessageBundle.getMessage("angal.common.code.txt"));
			GridBagConstraints gbcCodeLabel = new GridBagConstraints();
			gbcCodeLabel.insets = new Insets(5, 5, 5, 5);
			gbcCodeLabel.fill = GridBagConstraints.BOTH;
			gbcCodeLabel.gridx = 0;
			gbcCodeLabel.gridy = 1;
			dataPanel.add(codeLabel, gbcCodeLabel);
			JLabel descLabel = new JLabel(MessageBundle.getMessage("angal.common.description.txt"));
			GridBagConstraints gbcCescLabel = new GridBagConstraints();
			gbcCescLabel.fill = GridBagConstraints.BOTH;
			gbcCescLabel.insets = new Insets(5, 5, 5, 5);
			gbcCescLabel.gridx = 0;
			gbcCescLabel.gridy = 2;
			dataPanel.add(descLabel, gbcCescLabel);
			GridBagConstraints gbcDescriptionTextField = new GridBagConstraints();
			gbcDescriptionTextField.fill = GridBagConstraints.HORIZONTAL;
			gbcDescriptionTextField.insets = new Insets(5, 5, 5, 5);
			gbcDescriptionTextField.gridy = 2;
			gbcDescriptionTextField.gridx = 1;
			gbcCescLabel.fill = GridBagConstraints.BOTH;
			gbcCescLabel.insets = new Insets(0, 0, 5, 5);
			gbcCescLabel.gridx = 1;
			gbcCescLabel.gridy = 3;
			dataPanel.add(getDescriptionTextField(), gbcDescriptionTextField);
			GridBagConstraints gbcCodeTextField = new GridBagConstraints();
			gbcCodeTextField.fill = GridBagConstraints.BOTH;
			gbcCodeTextField.insets = new Insets(5, 5, 5, 5);
			gbcCodeTextField.gridx = 1;
			gbcCodeTextField.gridy = 1;
			dataPanel.add(getCodeTextField(), gbcCodeTextField);
			GridBagConstraints gbcNewPatientPanel = new GridBagConstraints();
			gbcNewPatientPanel.fill = GridBagConstraints.BOTH;
			gbcNewPatientPanel.gridx = 1;
			gbcNewPatientPanel.gridy = 3;
			dataPanel.add(getJFlagsPanel(), gbcNewPatientPanel);
			
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

				disease.setType((DiseaseType) diseaseTypeComboBox.getSelectedItem());
				disease.setDescription(descriptionTextField.getText());
				disease.setCode(codeTextField.getText().trim().toUpperCase());
				disease.setOpdInclude(includeOpdCheckBox.isSelected());
				disease.setIpdInInclude(includeIpdInCheckBox.isSelected());
				disease.setIpdOutInclude(includeIpdOutCheckBox.isSelected());

				boolean result = false;
				Disease savedDisease;
				try {
					if (insert) { // inserting
						savedDisease = diseaseBrowserManager.newDisease(disease);
						if (savedDisease != null) {
							disease.setLock(savedDisease.getLock());
							result = true;
						}

						if (result) {
							fireDiseaseInserted();
						}
					} else { // updating
						savedDisease = diseaseBrowserManager.updateDisease(disease);
						if (savedDisease != null) {
							disease.setLock(savedDisease.getLock());
							result = true;
						}

						if (result) {
							fireDiseaseUpdated();
						}
					}
					if (!result) {
						MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
					} else {
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
			if (insert) {
				descriptionTextField = new JTextField();
			} else {
				descriptionTextField = new JTextField(disease.getDescription());
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
				codeTextField.setText(disease.getCode());
				codeTextField.setEnabled(false);
			}
		}
		return codeTextField;
	}

	private JPanel getJFlagsPanel() {
		if (jNewPatientPanel == null) {
			jNewPatientPanel = new JPanel();
			includeOpdCheckBox = new JCheckBox(MessageBundle.getMessage("angal.disease.opd"));
			includeIpdInCheckBox = new JCheckBox(MessageBundle.getMessage("angal.disease.ipdin"));
			includeIpdOutCheckBox = new JCheckBox(MessageBundle.getMessage("angal.disease.ipdout"));
			jNewPatientPanel.add(includeOpdCheckBox);
			jNewPatientPanel.add(includeIpdInCheckBox);
			jNewPatientPanel.add(includeIpdOutCheckBox);
			if (!insert) {
				if (disease.getOpdInclude()) {
					includeOpdCheckBox.setSelected(true);
				}
				if (disease.getIpdInInclude()) {
					includeIpdInCheckBox.setSelected(true);
				}
				if (disease.getIpdOutInclude()) {
					includeIpdOutCheckBox.setSelected(true);
				}
			}
		}
		return jNewPatientPanel;
	}
	
	/**
	 * This method initializes diseaseTypeComboBox
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox<DiseaseType> getDiseaseTypeComboBox() {
		if (diseaseTypeComboBox == null) {
			diseaseTypeComboBox = new JComboBox<>();
			diseaseTypeComboBox.setBorder(new EmptyBorder(5, 5, 5, 5));
			try {
				List<DiseaseType> types = diseaseTypeBrowserManager.getDiseaseType();
				if (insert) {
					if (types != null) {
						for (DiseaseType elem : types) {
							diseaseTypeComboBox.addItem(elem);
						}
					}
				} else {
					DiseaseType selectedDiseaseType = null;
					if (types != null) {
						for (DiseaseType elem : types) {
							diseaseTypeComboBox.addItem(elem);
							if (disease.getType().equals(elem)) {
								selectedDiseaseType = elem;
							}
						}
					}
					if (selectedDiseaseType != null) {
						diseaseTypeComboBox.setSelectedItem(selectedDiseaseType);
					}
				}
			} catch (OHServiceException ohServiceException) {
				MessageDialog.showExceptions(ohServiceException);
			}
		}
		return diseaseTypeComboBox;
	}
	
}
