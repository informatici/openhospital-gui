package org.isf.dicom.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.Selection;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.isf.dicom.model.FileDicom;
import org.isf.generaldata.MessageBundle;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.time.TimeTools;

import com.toedter.calendar.JDateChooser;

/**
 * 
 * @author Mwithi
 *
 */
class ShowPreLoadDialog extends JDialog {
		
		/**
		 * 
		 */
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
		
		/*
		 * Attributes
		 */
		private Date dicomDate;
		private String dicomDescription;
		private List<Date> dates;
		private boolean save = false;

		public ShowPreLoadDialog(JFrame owner, int numfiles, FileDicom fileDicom, List<Date> dates) {
			super(owner, true);
			if (numfiles > 0) 
				setTitle("Loading multiple images: " + numfiles);
			else
				setTitle("Load image");
			try {
				this.dicomDate = TimeTools.parseDate(fileDicom.getDicomSeriesDate(), "dd MMM yyyy", false).getTime();
			} catch (ParseException e) {
				System.out.println("Error parsing dicom date");
				this.dicomDate = new Date();
			}
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
			JLabel dateLabel = new JLabel("Date" + ":");
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
				gbc_dateListLabel.anchor = GridBagConstraints.WEST;
				gbc_dateListLabel.anchor = GridBagConstraints.NORTHWEST;
				JLabel dateListLabel = new JLabel("Other dates" + ":");
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
			JLabel categoryLabel = new JLabel("Category" + ":");
			centerPanel.add(categoryLabel, gbc_categoryLabel);
			
			GridBagConstraints gbc_categoryComboBox = new GridBagConstraints();
			gbc_categoryComboBox.insets = new Insets(5, 5, 5, 5);
			gbc_categoryComboBox.gridx = 1;
			gbc_categoryComboBox.gridy = 2;
			gbc_categoryComboBox.fill = GridBagConstraints.HORIZONTAL;
			JComboBox categoryComboBox = new JComboBox();
			centerPanel.add(categoryComboBox, gbc_categoryComboBox);
			
			GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
			gbc_descriptionLabel.insets = new Insets(5, 5, 5, 5);
			gbc_descriptionLabel.gridx = 0;
			gbc_descriptionLabel.gridy = 3;
			gbc_descriptionLabel.anchor = GridBagConstraints.WEST;
			JLabel descriptionLabel = new JLabel("Description" + ":");
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

		private JList getDatesList() {
			if (datesList == null) {
				datesList = new JList(dates.toArray());
				datesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				datesList.addListSelectionListener(new ListSelectionListener() {
					
					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (!e.getValueIsAdjusting()) {
							Date selectedDate = (Date) ((JList) e.getSource()).getSelectedValue();
							dateChooser.setDate(selectedDate);
						}
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
				buttonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel"));
				buttonCancel.setMnemonic(KeyEvent.VK_N);
				buttonCancel.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
			}
			return buttonCancel;
		}

		private JButton getButtonOK() {
			if (buttonOK == null) {
				buttonOK = new JButton(MessageBundle.getMessage("angal.common.ok"));
				buttonOK.setMnemonic(KeyEvent.VK_O);
				buttonOK.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						dicomDate = dateChooser.getDate();
						dicomDescription = descriptionTextField.getText().trim();
						save = true;
						dispose();
					}
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
		
		public boolean isSave() {
			return save;
		}
	}