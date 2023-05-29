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
package org.isf.priceslist.gui;

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
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.priceslist.manager.PriceListManager;
import org.isf.priceslist.model.PriceList;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.VoLimitedTextField;
import org.isf.utils.layout.SpringUtilities;

public class ListEdit extends JDialog {

	private EventListenerList listListeners = new EventListenerList();
	
	public interface ListListener extends EventListener {
		void listUpdated(AWTEvent e);
		void listInserted(AWTEvent e);
	}
	
	public void addListListener(ListListener l) {
		listListeners.add(ListListener.class, l);
	}

	public void removeListListener(ListListener listener) {
		listListeners.remove(ListListener.class, listener);
	}

	private void fireListInserted() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = listListeners.getListeners(ListListener.class);
		for (EventListener listener : listeners) {
			((ListListener) listener).listInserted(event);
		}
	}

	private void fireListUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = listListeners.getListeners(ListListener.class);
		for (EventListener listener : listeners) {
			((ListListener) listener).listUpdated(event);
		}
	}

	private PriceListManager priceListManager = Context.getApplicationContext().getBean(PriceListManager.class);

	private static final long serialVersionUID = 1L;
	private JPanel jPanelData;
	private JPanel jPanelButtons;
	private JLabel jLabelCode;
	private JTextField jTextFieldCode;
	private JLabel jLabelName;
	private JTextField jTextFieldName;
	private JLabel jLabelDescription;
	private JTextField jTextFieldDescription;
	private JLabel jLabelCurrency;
	private JTextField jTextFieldCurrency;
	private JButton jButtonOK;
	private JButton jButtonCancel;
	private boolean insert;
	private PriceList list;
	
	public ListEdit(JFrame parent, PriceList list2, boolean inserting) {
		super(parent, true);
		insert = inserting;
		list = list2;
		initComponents();
		pack();
		setLocationRelativeTo(null);
	}

	private void initComponents() {
		add(getJPanelData(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setSize(400, 200);
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.priceslist.newlist.title"));
		} else {
			this.setTitle(MessageBundle.getMessage("angal.priceslist.editlist.title"));
		}
	}

	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton(MessageBundle.getMessage("angal.common.ok.btn"));
			jButtonOK.setMnemonic(MessageBundle.getMnemonic("angal.common.ok.btn.key"));
			jButtonOK.addActionListener(actionEvent -> {

				list.setCode(jTextFieldCode.getText());
				list.setName(jTextFieldName.getText());
				list.setDescription(jTextFieldDescription.getText());
				list.setCurrency(jTextFieldCurrency.getText());

				boolean result = false;
				try {
					if (insert) {	// inserting
						PriceList insertedPriceList = priceListManager.newList(list);
						if (insertedPriceList != null) {
							fireListInserted();
							result = true;
						}
					}
					else {	// updating
						PriceList updatedPriceList = priceListManager.updateList(list);
						if (updatedPriceList != null) {
							fireListUpdated();
							result = true;
						}
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
				if (!result) {
					MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
				}
				else {
					dispose();
				}
			});
		}
		return jButtonOK;
	}
	
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton(MessageBundle.getMessage("angal.common.cancel.btn"));
			jButtonCancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));
			jButtonCancel.addActionListener(actionEvent -> dispose());
		}
		return jButtonCancel;
	}

	private JTextField getJTextFieldDescription() {
		if (jTextFieldDescription == null) {
			jTextFieldDescription = new VoLimitedTextField(100, 20);
			if (!insert) {
				jTextFieldDescription.setText(list.getDescription());
			} else {
				jTextFieldDescription = new VoLimitedTextField(100);
			}
		}
		return jTextFieldDescription;
	}

	private JLabel getJLabelDescription() {
		if (jLabelDescription == null) {
			jLabelDescription = new JLabel(MessageBundle.getMessage("angal.priceslist.descriptionstar")); //$NON-NLS-1$
		}
		return jLabelDescription;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonOK());
			jPanelButtons.add(getJButtonCancel());
		}
		return jPanelButtons;
	}

	private JTextField getJTextFieldName() {
		if (jTextFieldName == null) {
			jTextFieldName = new VoLimitedTextField(50, 20);
			if (!insert) {
				jTextFieldName.setText(list.getName());
			} else {
				jTextFieldName.setText(""); //$NON-NLS-1$
			}
		}
		return jTextFieldName;
	}

	private JLabel getJLabelName() {
		if (jLabelName == null) {
			jLabelName = new JLabel(MessageBundle.getMessage("angal.priceslist.namestar")); //$NON-NLS-1$
		}
		return jLabelName;
	}

	private JTextField getJTextFieldCode() {
		if (jTextFieldCode == null) {
			jTextFieldCode = new VoLimitedTextField(7, 20);
			if (!insert) {
				jTextFieldCode.setText(list.getCode());
			} else {
				jTextFieldCode.setText(MessageBundle.getMessage("angal.priceslist.listm")); //$NON-NLS-1$
			}
		}
		return jTextFieldCode;
	}

	private JLabel getJLabelCode() {
		if (jLabelCode == null) {
			jLabelCode = new JLabel(MessageBundle.getMessage("angal.common.codestar")); //$NON-NLS-1$
		}
		return jLabelCode;
	}

	private JTextField getJTextFieldCurrency() {
		if (jTextFieldCurrency == null) {
			jTextFieldCurrency = new VoLimitedTextField(10, 20);
			if (!insert) {
				jTextFieldCurrency.setText(list.getCurrency());
			} else {
				jTextFieldCurrency.setText(""); //$NON-NLS-1$
			}
		}
		return jTextFieldCurrency;
	}

	private JLabel getJLabelCurrency() {
		if (jLabelCurrency == null) {
			jLabelCurrency = new JLabel(MessageBundle.getMessage("angal.priceslist.currencystar")); //$NON-NLS-1$
		}
		return jLabelCurrency;
	}

	private JPanel getJPanelData() {
		if (jPanelData == null) {
			jPanelData = new JPanel(new SpringLayout());
			jPanelData.add(getJLabelCode());
			jPanelData.add(getJTextFieldCode());
			jPanelData.add(getJLabelName());
			jPanelData.add(getJTextFieldName());
			jPanelData.add(getJLabelDescription());
			jPanelData.add(getJTextFieldDescription());
			jPanelData.add(getJLabelCurrency());
			jPanelData.add(getJTextFieldCurrency());
			jPanelData.add(new JLabel(MessageBundle.getMessage("angal.vaccine.requiredfields")));
			jPanelData.add(new JLabel(""));
			SpringUtilities.makeCompactGrid(jPanelData, 5, 2, 5, 5, 5, 5);
		}
		return jPanelData;
	}
}
