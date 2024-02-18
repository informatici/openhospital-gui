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
package org.isf.malnutrition.gui;

import java.util.EventListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.malnutrition.manager.MalnutritionManager;
import org.isf.malnutrition.model.Malnutrition;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.layout.SpringUtilities;

public class InsertMalnutrition extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private EventListenerList malnutritionListeners = new EventListenerList();

    public interface MalnutritionListener extends EventListener {
        void malnutritionUpdated(Malnutrition maln);
        void malnutritionInserted();
    }

    public void addMalnutritionListener(MalnutritionListener l) {
    	malnutritionListeners.add(MalnutritionListener.class, l);
    }

    public void removeMalnutritionListener(MalnutritionListener listener) {
    	malnutritionListeners.remove(MalnutritionListener.class, listener);
    }

    private void fireMalnutritionInserted() {

        EventListener[] listeners = malnutritionListeners.getListeners(MalnutritionListener.class);
	    for (EventListener listener : listeners) {
		    ((MalnutritionListener) listener).malnutritionInserted();
	    }
    }
    private void fireMalnutritionUpdated(Malnutrition maln) {

        EventListener[] listeners = malnutritionListeners.getListeners(MalnutritionListener.class);
	    for (EventListener listener : listeners) {
		    ((MalnutritionListener) listener).malnutritionUpdated(maln);
	    }
    }

	private GoodDateChooser confDate;

	private GoodDateChooser suppDate;
	
	private JTextField weightField;

	private JTextField heightField;

	private Malnutrition maln;

	private boolean inserting;
	
	private MalnutritionManager malnutritionManager = Context.getApplicationContext().getBean(MalnutritionManager.class);

	InsertMalnutrition(JDialog owner, Malnutrition malnutrition, boolean insert) {
		super(owner, true);
		maln = malnutrition;
		inserting = insert;
		if (inserting) {
			setTitle(MessageBundle.getMessage("angal.malnutrition.newmalnutrition.title"));
		} else {
			setTitle(MessageBundle.getMessage("angal.malnutrition.editmalnutrition.title"));
		}
		add(getJContentPane());
		pack();
		setLocationRelativeTo(null);
	}
	
	private JPanel getJContentPane() {
		JPanel jContentPane = new JPanel();
		jContentPane.setLayout(new BoxLayout(jContentPane, BoxLayout.Y_AXIS));
		jContentPane.add(getMainPanel());
		jContentPane.add(getButtonPanel());
		validate();
		return jContentPane;
	}

	private JPanel getMainPanel() {
		if (inserting) {
			suppDate = new GoodDateChooser();
			confDate = new GoodDateChooser(null);
		} else {
			suppDate = new GoodDateChooser(maln.getDateSupp().toLocalDate());
			confDate = new GoodDateChooser(maln.getDateConf().toLocalDate());
		}
		JLabel suppDateLabel = new JLabel(MessageBundle.getMessage("angal.malnutrition.dateofthiscontrol") + ':');
		JLabel confDateLabel = new JLabel(MessageBundle.getMessage("angal.malnutrition.dateofthenextcontrol") + ':');

		weightField = new JTextField();
		weightField.setColumns(6);
		heightField = new JTextField();
		heightField.setColumns(6);
		if (!inserting) {
			weightField.setText(String.valueOf(maln.getWeight()));
			heightField.setText(String.valueOf(maln.getHeight()));
		}
		JLabel weightLabel = new JLabel(MessageBundle.getMessage("angal.common.weight.txt") + ':');
		JLabel heightLabel = new JLabel(MessageBundle.getMessage("angal.common.height.txt") + ':');

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new SpringLayout());
		mainPanel.add(suppDateLabel);
		mainPanel.add(suppDate);
		mainPanel.add(confDateLabel);
		mainPanel.add(confDate);
		mainPanel.add(weightLabel);
		mainPanel.add(weightField);
		mainPanel.add(heightLabel);
		mainPanel.add(heightField);
		SpringUtilities.makeCompactGrid(mainPanel, 4, 2, 5, 5, 5, 5);

		return mainPanel;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(getOkButton());
		buttonPanel.add(getCancelButton());
		return buttonPanel;
	}

	private JButton getOkButton() {
		JButton okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
		okButton.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
		okButton.addActionListener(actionEvent -> {
			try {
				maln.setHeight(Float.parseFloat(heightField.getText()));
			} catch (NumberFormatException e) {
				maln.setHeight(0);
			}
			try {
				maln.setWeight(Float.parseFloat(weightField.getText()));
			} catch (NumberFormatException e) {
				maln.setWeight(0);
			}
			maln.setDateSupp(suppDate.getDateStartOfDay());
			maln.setDateConf(confDate.getDateStartOfDay());

			if (inserting) {	//inserting
				Malnutrition insertedMalnutrition = null;
				try {
					insertedMalnutrition = malnutritionManager.newMalnutrition(maln);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				if (insertedMalnutrition != null) {
					fireMalnutritionInserted();
					dispose();
				}

			} else {	//updating
				Malnutrition updatedMaln = null;
				try {
					updatedMaln = malnutritionManager.updateMalnutrition(maln);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				if (updatedMaln != null) {
					fireMalnutritionUpdated(updatedMaln);
					dispose();
				}
			}
		});
		return okButton;
	}

	private JButton getCancelButton() {
		JButton cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		cancelButton.addActionListener(actionEvent -> dispose());
		return cancelButton;
	}
}
