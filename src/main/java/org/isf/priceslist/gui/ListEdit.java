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
package org.isf.priceslist.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.EventListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.priceslist.manager.PriceListManager;
import org.isf.priceslist.model.PriceList;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.VoLimitedTextField;

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

			private static final long serialVersionUID = 1L;};

		EventListener[] listeners = listListeners.getListeners(ListListener.class);
		for (int i = 0; i < listeners.length; i++)
			((ListListener)listeners[i]).listInserted(event);
	}
	private void fireListUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;};
		
		EventListener[] listeners = listListeners.getListeners(ListListener.class);
		for (int i = 0; i < listeners.length; i++)
			((ListListener)listeners[i]).listUpdated(event);
	}
	
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
		//this.setLayout(new BorderLayout());
		add(getJPanelData(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setSize(400, 200);
		if (insert) {
			this.setTitle(MessageBundle.getMessage("angal.priceslist.newlist")); //$NON-NLS-1$
		} else {
			this.setTitle(MessageBundle.getMessage("angal.priceslist.editlist")); //$NON-NLS-1$
		}
	}

	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton();
			jButtonOK.setText(MessageBundle.getMessage("angal.common.ok")); //$NON-NLS-1$
			jButtonOK.setMnemonic(KeyEvent.VK_O);
			jButtonOK.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent event) {
						
					list.setCode(jTextFieldCode.getText());
					list.setName(jTextFieldName.getText());
					list.setDescription(jTextFieldDescription.getText());
					list.setCurrency(jTextFieldCurrency.getText());
					
					PriceListManager listManager = Context.getApplicationContext().getBean(PriceListManager.class);
					boolean result = false;
					try{
						if (insert) {      // inserting
							result = listManager.newList(list);
							if (result) {
								fireListInserted();
							}
						}
						else {             // updating
							result = listManager.updateList(list);
							if (result) {
								fireListUpdated();
							}
						}
					}catch(OHServiceException e){
						OHServiceExceptionUtil.showMessages(e);
					}
					if (!result) JOptionPane.showMessageDialog(
											null,
											MessageBundle.getMessage("angal.sql.thedatacouldnotbesaved")); //$NON-NLS-1$
					else  dispose();
					
				}
			});
		}
		return jButtonOK;
	}
	
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton();
			jButtonCancel.setText(MessageBundle.getMessage("angal.common.cancel")); //$NON-NLS-1$
			jButtonCancel.setMnemonic(KeyEvent.VK_C);
			jButtonCancel.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent event) {
						dispose();
				}
			});
		}
		return jButtonCancel;
	}

	private JTextField getJTextFieldDescription() {
		if (jTextFieldDescription == null) {
			jTextFieldDescription = new VoLimitedTextField(100, 20);
			if (!insert) {
				jTextFieldDescription.setText(list.getDescription());
			} else jTextFieldDescription = new VoLimitedTextField(100);
		}
		return jTextFieldDescription;
	}

	private JLabel getJLabelDescription() {
		if (jLabelDescription == null) {
			jLabelDescription = new JLabel();
			jLabelDescription.setText(MessageBundle.getMessage("angal.priceslist.descriptionstar")); //$NON-NLS-1$
			
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
			} else jTextFieldName.setText(""); //$NON-NLS-1$
		}
		return jTextFieldName;
	}

	private JLabel getJLabelName() {
		if (jLabelName == null) {
			jLabelName = new JLabel();
			jLabelName.setText(MessageBundle.getMessage("angal.priceslist.namestar")); //$NON-NLS-1$
		}
		return jLabelName;
	}

	private JTextField getJTextFieldCode() {
		if (jTextFieldCode == null) {
			jTextFieldCode = new VoLimitedTextField(7, 20);
			if (!insert) {
				jTextFieldCode.setText(list.getCode());
			} else jTextFieldCode.setText(MessageBundle.getMessage("angal.priceslist.listm")); //$NON-NLS-1$
		}
		return jTextFieldCode;
	}

	private JLabel getJLabelCode() {
		if (jLabelCode == null) {
			jLabelCode = new JLabel();
			jLabelCode.setText(MessageBundle.getMessage("angal.common.codestar")); //$NON-NLS-1$
		}
		return jLabelCode;
	}
	
	private JTextField getJTextFieldCurrency() {
		if (jTextFieldCurrency == null) {
			jTextFieldCurrency = new VoLimitedTextField(10, 20);
			if (!insert) {
				jTextFieldCurrency.setText(list.getCurrency());
			} else jTextFieldCurrency.setText(""); //$NON-NLS-1$
		}
		return jTextFieldCurrency;
	}

	private JLabel getJLabelCurrency() {
		if (jLabelCurrency == null) {
			jLabelCurrency = new JLabel();
			jLabelCurrency.setText(MessageBundle.getMessage("angal.priceslist.currencystar")); //$NON-NLS-1$
		}
		return jLabelCurrency;
	}

	private JPanel getJPanelData() {
		if (jPanelData == null) {
			jPanelData = new JPanel();
			jPanelData.setLayout(new BoxLayout(jPanelData, BoxLayout.Y_AXIS));
			jPanelData.add(getJLabelCode());
			jPanelData.add(getJTextFieldCode());
			jPanelData.add(getJLabelName());
			jPanelData.add(getJTextFieldName());
			jPanelData.add(getJLabelDescription());
			jPanelData.add(getJTextFieldDescription());
			jPanelData.add(getJLabelCurrency());
			jPanelData.add(getJTextFieldCurrency());
		}
		return jPanelData;
	}
}
