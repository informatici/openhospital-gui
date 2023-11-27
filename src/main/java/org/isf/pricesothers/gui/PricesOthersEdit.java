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
package org.isf.pricesothers.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.EventListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.pricesothers.manager.PricesOthersManager;
import org.isf.pricesothers.model.PricesOthers;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

public class PricesOthersEdit extends JDialog {
	
	private EventListenerList pricesOthersListeners = new EventListenerList();
	
	public interface PricesOthersListener extends EventListener {
		void pricesOthersUpdated(AWTEvent e);
		void pricesOthersInserted(AWTEvent e);
	}
	
	public void addOtherListener(PricesOthersListener l) {
		pricesOthersListeners.add(PricesOthersListener.class, l);
	}
	
	public void removeOtherListener(PricesOthersListener listener) {
		pricesOthersListeners.remove(PricesOthersListener.class, listener);
	}

	private void fireOtherInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = pricesOthersListeners.getListeners(PricesOthersListener.class);
		for (EventListener listener : listeners) {
			((PricesOthersListener) listener).pricesOthersInserted(event);
		}
	}

	private void fireOtherUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = pricesOthersListeners.getListeners(PricesOthersListener.class);
		for (EventListener listener : listeners) {
			((PricesOthersListener) listener).pricesOthersUpdated(event);
		}
	}

	private PricesOthersManager pricesOthersManager = Context.getApplicationContext().getBean(PricesOthersManager.class);

	private static final long serialVersionUID = 1L;
	private JPanel jPanelData;
	private JPanel jPanelCodeDescription;
	private JTextField jTextFieldCode;
	private JTextField jTextFieldDescription;
	private JPanel jPanelParameters;
	private JCheckBox jCheckBoxOPD;
	private JCheckBox jCheckBoxIPD;
	private JCheckBox jCheckBoxDaily;
	private JCheckBox jCheckBoxDischarge;
	private JCheckBox jCheckBoxUndefined;
	private JPanel jPanelButtons;
	private JButton jButtonOK;
	private JButton jButtonCancel;
	private boolean insert;
	private PricesOthers pOther;
	
	public PricesOthersEdit(JFrame parent, PricesOthers other, boolean inserting) {
		super(parent, true);
		pOther = other;
		insert = inserting;
		initComponents();
		setLocationRelativeTo(null);
	}

	private void initComponents() {
		add(getJPanelData(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setSize(400, 180);
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.pricesothers.newprice.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.pricesothers.editprice.title"));
		}
	}

	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jButtonCancel.addActionListener(actionEvent -> dispose());
		}
		return jButtonCancel;
	}

	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			jButtonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			jButtonOK.addActionListener(actionEvent -> {

				pOther.setCode(jTextFieldCode.getText());
				pOther.setDescription(jTextFieldDescription.getText());
				pOther.setOpdInclude(jCheckBoxOPD.isSelected());
				pOther.setIpdInclude(jCheckBoxIPD.isSelected());
				pOther.setDaily(jCheckBoxDaily.isSelected());
				pOther.setDischarge(jCheckBoxDischarge.isSelected());
				pOther.setUndefined(jCheckBoxUndefined.isSelected());

				try {
					if (insert) {	// inserting
						pricesOthersManager.newOther(pOther);
						fireOtherInserted();
					} else {	// updating
						pricesOthersManager.updateOther(pOther);
						fireOtherUpdated();
					}
				} catch (OHServiceException e) {
					MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
					OHServiceExceptionUtil.showMessages(e);
				}
				dispose();
			});
		}
		return jButtonOK;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonOK());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
	}

	private JCheckBox getJCheckBoxUndefined() {
		if (jCheckBoxUndefined == null) {
			jCheckBoxUndefined = new JCheckBox(MessageBundle.getMessage("angal.common.undefined.txt"));
			jCheckBoxUndefined.setSelected(!insert && pOther.isUndefined());
		}
		return jCheckBoxUndefined;
	}
	
	private JCheckBox getJCheckBoxDischarge() {
		if (jCheckBoxDischarge == null) {
			jCheckBoxDischarge = new JCheckBox(MessageBundle.getMessage("angal.common.discharge.txt"));
			jCheckBoxDischarge.setSelected(!insert && pOther.isDischarge());
		}
		return jCheckBoxDischarge;
	}
	
	private JCheckBox getJCheckBoxDaily() {
		if (jCheckBoxDaily == null) {
			jCheckBoxDaily = new JCheckBox(MessageBundle.getMessage("angal.pricesothers.daily.txt"));
			jCheckBoxDaily.setSelected(!insert && pOther.isDaily());
		}
		return jCheckBoxDaily;
	}

	private JCheckBox getJCheckBoxIPD() {
		if (jCheckBoxIPD == null) {
			jCheckBoxIPD = new JCheckBox();
			jCheckBoxIPD.setText("IPD");
			jCheckBoxIPD.setSelected(insert || pOther.isIpdInclude());

		}
		return jCheckBoxIPD;
	}

	private JCheckBox getJCheckBoxOPD() {
		if (jCheckBoxOPD == null) {
			jCheckBoxOPD = new JCheckBox();
			jCheckBoxOPD.setText("OPD");
			jCheckBoxOPD.setSelected(insert || pOther.isOpdInclude());

		}
		return jCheckBoxOPD;
	}

	private JPanel getJPanelParameters() {
		if (jPanelParameters == null) {
			jPanelParameters = new JPanel();
			jPanelParameters.add(getJCheckBoxOPD());
			jPanelParameters.add(getJCheckBoxIPD());
			jPanelParameters.add(getJCheckBoxDaily());
			jPanelParameters.add(getJCheckBoxDischarge());
			jPanelParameters.add(getJCheckBoxUndefined());
		}
		return jPanelParameters;
	}

	private JTextField getJTextFieldDescription() {
		if (jTextFieldDescription == null) {
			jTextFieldDescription = new VoLimitedTextField(100);
			jTextFieldDescription.setText(insert? "" : pOther.getDescription()); //$NON-NLS-1$
		}
		return jTextFieldDescription;
	}

	private JTextField getJTextFieldCode() {
		if (jTextFieldCode == null) {
			jTextFieldCode = new VoLimitedTextField(10);
			jTextFieldCode.setText(insert ? MessageBundle.getMessage("angal.pricesothers.othm") : pOther.getCode()); //$NON-NLS-1$
		}
		return jTextFieldCode;
	}

	private JPanel getJPanelCodeDescription() {
		if (jPanelCodeDescription == null) {
			jPanelCodeDescription = new JPanel();
			jPanelCodeDescription.setLayout(new SpringLayout());
			jPanelCodeDescription.add(new JLabel(MessageBundle.getMessage("angal.common.code.txt") + ':'));
			jPanelCodeDescription.add(getJTextFieldCode());
			jPanelCodeDescription.add(new JLabel(MessageBundle.getMessage("angal.common.description.txt") + ':'));
			jPanelCodeDescription.add(getJTextFieldDescription());
			SpringUtilities.makeCompactGrid(jPanelCodeDescription, 2, 2, 5, 5, 5, 5);
		}
		return jPanelCodeDescription;
	}

	private JPanel getJPanelData() {
		if (jPanelData == null) {
			jPanelData = new JPanel();
			jPanelData.setLayout(new BoxLayout(jPanelData, BoxLayout.Y_AXIS));
			jPanelData.add(getJPanelCodeDescription());
			jPanelData.add(getJPanelParameters());
		}
		return jPanelData;
	}
}
