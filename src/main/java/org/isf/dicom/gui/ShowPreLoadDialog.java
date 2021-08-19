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
package org.isf.dicom.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.isf.dicom.model.FileDicom;
import org.isf.dicomtype.manager.DicomTypeBrowserManager;
import org.isf.dicomtype.model.DicomType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.VoLimitedTextField;

import com.toedter.calendar.JDateChooser;

/**
 * @author Mwithi
 */
class ShowPreLoadDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	/*
	 * JComponents
	 */
	private JPanel buttonsPanel;
	private JList datesList;
	private JButton buttonCancel;
	private JButton buttonOK;
	private JDateChooser dateChooser;
	private VoLimitedTextField descriptionTextField;
	private JComboBox dicomTypeComboBox;

	/*
	 * Attributes
	 */
	private Date dicomDate;
	private DicomType dicomType;
	private String dicomDescription;
	private List<Date> dates;
	private boolean save = false;

	/*
	 * Managers
	 */
	private DicomTypeBrowserManager dicomTypeMan = Context.getApplicationContext().getBean(DicomTypeBrowserManager.class);

	public ShowPreLoadDialog(JFrame owner, int numfiles, FileDicom fileDicom, List<Date> dates) {
		super(owner, true);
		if (numfiles > 1) {
			setTitle(MessageBundle.formatMessage("angal.showpreload.loadingmultipleimages.fmt.title", numfiles));
		} else {
			setTitle(MessageBundle.getMessage("angal.showpreload.loadimage.title"));
		}
		this.dicomDate = fileDicom.getDicomSeriesDate();
		this.dicomDescription = fileDicom.getDicomSeriesDescription();
		this.dates = dates;
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getCenterPanel(), BorderLayout.CENTER);
		getContentPane().add(getButtonsPanel(), BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	private JPanel getCenterPanel() {
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc_dateLabel = new GridBagConstraints();
		gbc_dateLabel.insets = new Insets(5, 5, 5, 5);
		gbc_dateLabel.gridx = 0;
		gbc_dateLabel.gridy = 0;
		gbc_dateLabel.anchor = GridBagConstraints.WEST;
		JLabel dateLabel = new JLabel(MessageBundle.getMessage("angal.common.date.txt") + ":");
		centerPanel.add(dateLabel, gbc_dateLabel);

		GridBagConstraints gbc_date = new GridBagConstraints();
		gbc_date.insets = new Insets(5, 5, 5, 5);
		gbc_date.gridx = 1;
		gbc_date.gridy = 0;
		gbc_date.fill = GridBagConstraints.HORIZONTAL;
		dateChooser = new JDateChooser(this.dicomDate);
		centerPanel.add(dateChooser, gbc_date);

		if (!dates.isEmpty()) {
			GridBagConstraints gbc_dateListLabel = new GridBagConstraints();
			gbc_dateListLabel.insets = new Insets(5, 5, 5, 5);
			gbc_dateListLabel.gridx = 0;
			gbc_dateListLabel.gridy = 1;
			gbc_dateListLabel.anchor = GridBagConstraints.NORTHWEST;
			JLabel dateListLabel = new JLabel(MessageBundle.getMessage("angal.showpreload.otherdates.txt") + ":");
			centerPanel.add(dateListLabel, gbc_dateListLabel);

			GridBagConstraints gbc_dateList = new GridBagConstraints();
			gbc_dateList.insets = new Insets(5, 5, 5, 5);
			gbc_dateList.gridx = 1;
			gbc_dateList.gridy = 1;
			gbc_dateList.fill = GridBagConstraints.HORIZONTAL;
			centerPanel.add(getDatesList(), gbc_dateList);
		}

		GridBagConstraints gbc_categoryLabel = new GridBagConstraints();
		gbc_categoryLabel.insets = new Insets(5, 5, 5, 5);
		gbc_categoryLabel.gridx = 0;
		gbc_categoryLabel.gridy = 2;
		gbc_categoryLabel.anchor = GridBagConstraints.WEST;
		JLabel categoryLabel = new JLabel(MessageBundle.getMessage("angal.showpreload.category.txt") + ":");
		centerPanel.add(categoryLabel, gbc_categoryLabel);

		GridBagConstraints gbc_categoryComboBox = new GridBagConstraints();
		gbc_categoryComboBox.insets = new Insets(5, 5, 5, 5);
		gbc_categoryComboBox.gridx = 1;
		gbc_categoryComboBox.gridy = 2;
		gbc_categoryComboBox.fill = GridBagConstraints.HORIZONTAL;
		centerPanel.add(getDicomTypeComboBox(), gbc_categoryComboBox);

		GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
		gbc_descriptionLabel.insets = new Insets(5, 5, 5, 5);
		gbc_descriptionLabel.gridx = 0;
		gbc_descriptionLabel.gridy = 3;
		gbc_descriptionLabel.anchor = GridBagConstraints.WEST;
		JLabel descriptionLabel = new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ":");
		centerPanel.add(descriptionLabel, gbc_descriptionLabel);

		GridBagConstraints gbc_descriptionTextField = new GridBagConstraints();
		gbc_descriptionTextField.insets = new Insets(5, 5, 5, 5);
		gbc_descriptionTextField.gridx = 1;
		gbc_descriptionTextField.gridy = 3;
		gbc_descriptionTextField.fill = GridBagConstraints.HORIZONTAL;
		descriptionTextField = new VoLimitedTextField(255, 20);
		centerPanel.add(descriptionTextField, gbc_descriptionTextField);

		return centerPanel;
	}

	private JComboBox getDicomTypeComboBox() {
		if (dicomTypeComboBox == null) {
			dicomTypeComboBox = new JComboBox();
			dicomTypeComboBox.addItem("");
			try {
				ArrayList<DicomType> dicomTypeList = dicomTypeMan.getDicomType();
				for (DicomType dicomType : dicomTypeList) {
					dicomTypeComboBox.addItem(dicomType);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, ShowPreLoadDialog.this);
			}
			dicomTypeComboBox.addItemListener(e -> {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					try {
						dicomType = (DicomType) e.getItem();
					} catch (ClassCastException e1) {
						dicomType = null;
					}
				}
			});

		}
		return dicomTypeComboBox;
	}

	private JList getDatesList() {
		if (datesList == null) {
			datesList = new JList(dates.toArray());
			datesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			datesList.addListSelectionListener(e -> {
				if (!e.getValueIsAdjusting()) {
					Date selectedDate = (Date) ((JList) e.getSource()).getSelectedValue();
					dateChooser.setDate(selectedDate);
				}
			});
		}
		return datesList;
	}

	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new JPanel();
			buttonsPanel.add(getButtonOK());
			buttonsPanel.add(getButtonCancel());
		}
		return buttonsPanel;
	}

	private JButton getButtonCancel() {
		if (buttonCancel == null) {
			buttonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			buttonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			buttonCancel.addActionListener(arg0 -> dispose());
		}
		return buttonCancel;
	}

	private JButton getButtonOK() {
		if (buttonOK == null) {
			buttonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			buttonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			buttonOK.addActionListener(arg0 -> {
				dicomDate = dateChooser.getDate();
				dicomDescription = descriptionTextField.getText().trim();
				save = true;
				dispose();
			});
		}
		return buttonOK;
	}

	public Date getDicomDate() {
		return dicomDate;
	}

	public String getDicomDescription() {
		return dicomDescription;
	}

	public DicomType getDicomType() {
		return dicomType;
	}

	public boolean isSave() {
		return save;
	}
}
