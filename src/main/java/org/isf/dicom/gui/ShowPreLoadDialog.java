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
package org.isf.dicom.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.time.LocalDateTime;
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
import javax.swing.WindowConstants;

import org.isf.dicom.model.FileDicom;
import org.isf.dicomtype.manager.DicomTypeBrowserManager;
import org.isf.dicomtype.model.DicomType;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.Converters;

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
	private GoodDateChooser dateChooser;
	private VoLimitedTextField descriptionTextField;
	private JComboBox dicomTypeComboBox;

	/*
	 * Attributes
	 */
	private LocalDateTime dicomDate;
	private DicomType dicomType;
	private String dicomDescription;
	private List<Date> dates;
	private boolean save;

	/*
	 * Managers
	 */
	private DicomTypeBrowserManager dicomTypeBrowserManager = Context.getApplicationContext().getBean(DicomTypeBrowserManager.class);

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
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private JPanel getCenterPanel() {
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbcDateLabel = new GridBagConstraints();
		gbcDateLabel.insets = new Insets(5, 5, 5, 5);
		gbcDateLabel.gridx = 0;
		gbcDateLabel.gridy = 0;
		gbcDateLabel.anchor = GridBagConstraints.WEST;
		JLabel dateLabel = new JLabel(MessageBundle.getMessage("angal.common.date.txt") + ':');
		centerPanel.add(dateLabel, gbcDateLabel);

		GridBagConstraints gbcDateChooser = new GridBagConstraints();
		gbcDateChooser.insets = new Insets(5, 5, 5, 5);
		gbcDateChooser.gridx = 1;
		gbcDateChooser.gridy = 0;
		gbcDateChooser.fill = GridBagConstraints.HORIZONTAL;
		if (dicomDate != null) {
			dateChooser = new GoodDateChooser(this.dicomDate.toLocalDate());
		} else {
			dateChooser = new GoodDateChooser();
		}
		centerPanel.add(dateChooser, gbcDateChooser);

		if (!dates.isEmpty()) {
			GridBagConstraints gbcDateListLabel = new GridBagConstraints();
			gbcDateListLabel.insets = new Insets(5, 5, 5, 5);
			gbcDateListLabel.gridx = 0;
			gbcDateListLabel.gridy = 1;
			gbcDateListLabel.anchor = GridBagConstraints.NORTHWEST;
			JLabel dateListLabel = new JLabel(MessageBundle.getMessage("angal.showpreload.otherdates.txt") + ':');
			centerPanel.add(dateListLabel, gbcDateListLabel);

			GridBagConstraints gbcDateList = new GridBagConstraints();
			gbcDateList.insets = new Insets(5, 5, 5, 5);
			gbcDateList.gridx = 1;
			gbcDateList.gridy = 1;
			gbcDateList.fill = GridBagConstraints.HORIZONTAL;
			centerPanel.add(getDatesList(), gbcDateList);
		}

		GridBagConstraints gbcCategoryLabel = new GridBagConstraints();
		gbcCategoryLabel.insets = new Insets(5, 5, 5, 5);
		gbcCategoryLabel.gridx = 0;
		gbcCategoryLabel.gridy = 2;
		gbcCategoryLabel.anchor = GridBagConstraints.WEST;
		JLabel categoryLabel = new JLabel(MessageBundle.getMessage("angal.showpreload.category.txt") + ':');
		centerPanel.add(categoryLabel, gbcCategoryLabel);

		GridBagConstraints gbcCategoryComboBox = new GridBagConstraints();
		gbcCategoryComboBox.insets = new Insets(5, 5, 5, 5);
		gbcCategoryComboBox.gridx = 1;
		gbcCategoryComboBox.gridy = 2;
		gbcCategoryComboBox.fill = GridBagConstraints.HORIZONTAL;
		centerPanel.add(getDicomTypeComboBox(), gbcCategoryComboBox);

		GridBagConstraints gbcDescriptionLabel = new GridBagConstraints();
		gbcDescriptionLabel.insets = new Insets(5, 5, 5, 5);
		gbcDescriptionLabel.gridx = 0;
		gbcDescriptionLabel.gridy = 3;
		gbcDescriptionLabel.anchor = GridBagConstraints.WEST;
		JLabel descriptionLabel = new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ':');
		centerPanel.add(descriptionLabel, gbcDescriptionLabel);

		GridBagConstraints gbcDescriptionTextField = new GridBagConstraints();
		gbcDescriptionTextField.insets = new Insets(5, 5, 5, 5);
		gbcDescriptionTextField.gridx = 1;
		gbcDescriptionTextField.gridy = 3;
		gbcDescriptionTextField.fill = GridBagConstraints.HORIZONTAL;
		descriptionTextField = new VoLimitedTextField(255, 20);
		centerPanel.add(descriptionTextField, gbcDescriptionTextField);

		return centerPanel;
	}

	private JComboBox getDicomTypeComboBox() {
		if (dicomTypeComboBox == null) {
			dicomTypeComboBox = new JComboBox();
			dicomTypeComboBox.addItem("");
			try {
				List<DicomType> dicomTypeList = dicomTypeBrowserManager.getDicomType();
				for (DicomType dicomType : dicomTypeList) {
					dicomTypeComboBox.addItem(dicomType);
				}
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e, this);
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
			datesList.addListSelectionListener(selectionEvent -> {
				if (!selectionEvent.getValueIsAdjusting()) {
					Date selectedDate = (Date) ((JList) selectionEvent.getSource()).getSelectedValue();
					dateChooser.setDate(Converters.convertToLocalDate(selectedDate));
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
			buttonCancel.addActionListener(actionEvent -> dispose());
		}
		return buttonCancel;
	}

	private JButton getButtonOK() {
		if (buttonOK == null) {
			buttonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			buttonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			buttonOK.addActionListener(actionEvent -> {
				dicomDate = dateChooser.getDateEndOfDay();
				dicomDescription = descriptionTextField.getText().trim();
				save = true;
				dispose();
			});
		}
		return buttonOK;
	}

	public LocalDateTime getDicomDate() {
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
