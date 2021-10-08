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
package org.isf.malnutrition.gui;

import java.util.EventListener;
import java.util.GregorianCalendar;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.malnutrition.manager.MalnutritionManager;
import org.isf.malnutrition.model.Malnutrition;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.time.DateTextField;

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

	private JPanel jContentPane;

	private JPanel timePanel;

	private JPanel fieldPanel;
	
	private JPanel buttonPanel;

	private DateTextField confDate;

	private DateTextField suppDate;
	
	private JTextField weightField;

	private JTextField heightField;

	private Malnutrition maln;
	
	private JButton okButton;

	private JButton cancelButton;

	private boolean inserting;
	
	private MalnutritionManager manager = Context.getApplicationContext().getBean(MalnutritionManager.class);

	InsertMalnutrition(JDialog owner, Malnutrition malnutrition, boolean insert) {
		super(owner, true);
		maln = malnutrition;
		inserting = insert;
		if (inserting) {
			setTitle(MessageBundle.getMessage("angal.malnutrition.newmalnutrition.title"));
		}
		else {
			setTitle(MessageBundle.getMessage("angal.malnutrition.editmalnutrition.title"));
		}
		add(getJContentPane());
		pack();
		setLocationRelativeTo(null);
	}
	
	private JPanel getJContentPane() {
		jContentPane = new JPanel();
		jContentPane.setLayout(new BoxLayout(jContentPane, BoxLayout.Y_AXIS));
		jContentPane.add(getTimePanel());
		jContentPane.add(getFieldPanel());
		jContentPane.add(getButtonPanel());
		validate();
		return jContentPane;
	}

	private JPanel getTimePanel() {
		timePanel = new JPanel();
		timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
		
		if (inserting) {
			suppDate = new DateTextField(new GregorianCalendar());
			confDate = new DateTextField();
		} else {
			suppDate = new DateTextField(maln.getDateSupp());
			confDate = new DateTextField(maln.getDateConf());
		}
				
		JLabel suppDateLabel = new JLabel(MessageBundle.getMessage("angal.malnutrition.dateofthiscontrol"));
		suppDateLabel.setAlignmentX(CENTER_ALIGNMENT);
		timePanel.add(suppDateLabel);
		timePanel.add(suppDate);
		JLabel confDateLabel = new JLabel(MessageBundle.getMessage("angal.malnutrition.dateofthenextcontrol"));
		confDateLabel.setAlignmentX(CENTER_ALIGNMENT);
		timePanel.add(confDateLabel);
		timePanel.add(confDate);
		return timePanel;
	}

	private JPanel getFieldPanel() {
		fieldPanel = new JPanel();
		// fieldPanel.setLayout(new BoxLayout(fieldPanel,BoxLayout.Y_AXIS));
		weightField = new JTextField();
		weightField.setColumns(6);
		heightField = new JTextField();
		heightField.setColumns(6);
		if (!inserting) {
			weightField.setText(String.valueOf(maln.getWeight()));
			heightField.setText(String.valueOf(maln.getHeight()));
		}
		JLabel weightLabel = new JLabel(MessageBundle.getMessage("angal.common.weight.txt"));
		JLabel heightLabel = new JLabel(MessageBundle.getMessage("angal.common.height.txt"));
		fieldPanel.add(weightLabel);
		fieldPanel.add(weightField);
		fieldPanel.add(heightLabel);
		fieldPanel.add(heightField);
		return fieldPanel;
	}

	private JPanel getButtonPanel() {
		buttonPanel = new JPanel();
		buttonPanel.add(getOkButton());
		buttonPanel.add(getCancelButton());
		return buttonPanel;
	}

	private JButton getOkButton() {
		okButton = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
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
			maln.setDateSupp(suppDate.getCompleteLocalDateTime());
			maln.setDateConf(confDate.getCompleteLocalDateTime());

			if (inserting) {
				boolean inserted = false;
				try {
					inserted = manager.newMalnutrition(maln);
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				if (inserted) {
					fireMalnutritionInserted();
					dispose();
				}

			} else {
				Malnutrition updatedMaln = null;
				try {
					updatedMaln = manager.updateMalnutrition(maln);
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
		cancelButton = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
		cancelButton.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
		cancelButton.addActionListener(actionEvent -> dispose());
		return cancelButton;
	}
}
