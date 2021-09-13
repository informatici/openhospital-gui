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
package org.isf.priceslist.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.priceslist.gui.ListEdit.ListListener;
import org.isf.priceslist.manager.PriceListManager;
import org.isf.priceslist.model.PriceList;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class ListBrowser extends ModalJFrame  implements ListListener{

	@Override
	public void listInserted(AWTEvent e) {
		try {
			listArray = listManager.getLists();
		} catch(OHServiceException ex) {
			OHServiceExceptionUtil.showMessages(ex);
		}
		jTablePriceLists.setModel(new ListBrowserModel());
	}

	@Override
	public void listUpdated(AWTEvent e) {
		((ListBrowserModel)jTablePriceLists.getModel()).fireTableDataChanged();
		jTablePriceLists.updateUI();
	}
	
	private static final long serialVersionUID = 1L;
	private JTable jTablePriceLists;
	private JScrollPane jScrollPaneTable;
	private JButton jButtonNew;
	private JPanel jPanelButtons;
	private JButton jButtonEdit;
	private JButton jButtonCopy;
	private JButton jButtonClose;
	private JButton jButtonDelete;
	private String[] columnNames = {
			MessageBundle.getMessage("angal.common.id.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.name.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
			MessageBundle.getMessage("angal.priceslist.currency.col").toUpperCase()
	};
	private int[] columnWidth = {100, 100, 200, 100};
	private boolean[] columnResizable = {false, false, true, false};
	
	private PriceList list;
	PriceListManager listManager = Context.getApplicationContext().getBean(PriceListManager.class);
	private ArrayList<PriceList> listArray;
	private JFrame myFrame;
			
	public ListBrowser() {
		myFrame = this;
		initComponents();
		pack();
		setLocationRelativeTo(null);
	}

	private void initComponents() {
		add(getJScrollPaneTable(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		setTitle(MessageBundle.getMessage("angal.priceslist.listbrowser.title"));
	}

	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
			jButtonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
			jButtonClose.addActionListener(actionEvent -> dispose());
		}
		return jButtonClose;
	}
	
	private JButton getJButtonDelete() {
		if (jButtonDelete == null) {
			jButtonDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
			jButtonDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
			jButtonDelete.addActionListener(actionEvent -> {
				if (jTablePriceLists.getSelectedRow() < 0) {
					JOptionPane.showMessageDialog(null,MessageBundle.getMessage("angal.priceslist.pleaseselectalisttodelete"));
				} else {
					if (jTablePriceLists.getRowCount() == 1) {
						MessageDialog.error(null, "angal.priceslist.sorryatleastonelist");
						return;
					}
					int selectedRow = jTablePriceLists.getSelectedRow();
					list = (PriceList)jTablePriceLists.getModel().getValueAt(selectedRow, -1);

					int answer = MessageDialog.yesNo(null, "angal.priceslist.deletethislistandallitsprices.fmt.msg", list.getName());
					try {
						if (answer == JOptionPane.OK_OPTION) {

							boolean result;
							result = listManager.deleteList(list);

							if (result) {
								listArray = listManager.getLists();
								jTablePriceLists.setModel(new ListBrowserModel());
							} else {
								MessageDialog.error(null, "angal.priceslist.thedatacouldnotbedeleted");
							}
						}
					} catch(OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
				}
			});
		}
		return jButtonDelete;
	}

	private JButton getJButtonCopy() {
		if (jButtonCopy == null) {
			jButtonCopy = new JButton(MessageBundle.getMessage("angal.priceslist.copy.btn"));
			jButtonCopy.setMnemonic(MessageBundle.getMnemonic("angal.priceslist.copy.btn.key"));
			jButtonCopy.addActionListener(actionEvent -> {
				if (jTablePriceLists.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.priceslist.pleaseselectalisttocopy");
				} else {
					int selectedRow = jTablePriceLists.getSelectedRow();
					list = (PriceList)jTablePriceLists.getModel().getValueAt(selectedRow, -1);

					String newName = JOptionPane.showInputDialog(MessageBundle.getMessage("angal.priceslist.enterthenameforthenewlist"));

					if (newName != null) {

						Double qty;
						Double step;
						Double startQty = 1.;
						Double minQty = 0.;
						Double maxQty = 100.;
						Double stepQty = 0.01;
						JSpinner jSpinnerQty = new JSpinner(new SpinnerNumberModel(startQty,minQty,maxQty,stepQty));

						int r = JOptionPane.showConfirmDialog(ListBrowser.this,
							new Object[] { MessageBundle.getMessage("angal.priceslist.multiplier"), jSpinnerQty },
							MessageBundle.getMessage("angal.priceslist.multiplier"),
					        JOptionPane.OK_CANCEL_OPTION,
					        JOptionPane.PLAIN_MESSAGE);

						if (r == JOptionPane.OK_OPTION) {
							try {
								qty = (Double) jSpinnerQty.getValue();
								if (qty == 0.) {
									MessageDialog.error(ListBrowser.this, "angal.priceslist.invalidmultiplierpleasetryagain");
									return;
								}
							} catch (Exception eee) {
								MessageDialog.error(ListBrowser.this, "angal.priceslist.invalidmultiplierpleasetryagain");
								return;
							}

							startQty = 0.25;
							minQty = 0.;
							maxQty = 1.;
							stepQty = 0.01;
							jSpinnerQty = new JSpinner(new SpinnerNumberModel(startQty,minQty,maxQty,stepQty));

							r = JOptionPane.showConfirmDialog(ListBrowser.this,
								new Object[] { MessageBundle.getMessage("angal.priceslist.rounduptothenearest"), jSpinnerQty },
								MessageBundle.getMessage("angal.priceslist.roundingfactor"),
						        JOptionPane.OK_CANCEL_OPTION,
						        JOptionPane.PLAIN_MESSAGE);

							if (r == JOptionPane.OK_OPTION) {
								try {
									step = (Double) jSpinnerQty.getValue();
									if (step == 0.) {
										MessageDialog.error(ListBrowser.this, "angal.priceslist.invalidfactorpleasetryagain");
										return;
									}
								} catch (Exception eee) {
									MessageDialog.error(ListBrowser.this, "angal.priceslist.invalidfactorpleasetryagain");
									return;
								}
							} else {
								return;
							}
						} else {
							return;
						}

						// Save new list
						if (newName.equals("")) {
							newName = MessageBundle.getMessage("angal.priceslist.copyof").concat(" ").concat(list.getName());
						}
						PriceList copiedList = new PriceList(list.getId(),MessageBundle.getMessage("angal.priceslist.acode"),newName,MessageBundle.getMessage("angal.priceslist.adescription"),list.getCurrency());

						boolean result;
						try {
							result = listManager.copyList(copiedList, qty, step);

							if (result) {
								MessageDialog.info(null, "angal.priceslist.listcopiedremembertoeditinformations");

								listArray = listManager.getLists();
								jTablePriceLists.setModel(new ListBrowserModel());

							} else {
								MessageDialog.error(null, "angal.common.datacouldnotbesaved.msg");
							}
						} catch(OHServiceException e) {
							OHServiceExceptionUtil.showMessages(e);
						}
					}
				}
			});
		}
		return jButtonCopy;
	}

	private JButton getJButtonEdit() {
		if (jButtonEdit == null) {
			jButtonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jButtonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jButtonEdit.addActionListener(actionEvent -> {

				if (jTablePriceLists.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.priceslist.pleaseselectalisttoedit");
				} else {
					int selectedRow = jTablePriceLists.getSelectedRow();
					list = (PriceList)jTablePriceLists.getModel().getValueAt(selectedRow, -1);
					ListEdit editList = new ListEdit(myFrame, list, false);
					editList.addListListener(ListBrowser.this);
					editList.setVisible(true);
				}
			});
		}
		return jButtonEdit;
	}
	
	private JButton getJButtonNew() {
		if (jButtonNew == null) {
			jButtonNew = new JButton(MessageBundle.getMessage("angal.common.new.btn"));
			jButtonNew.setMnemonic(MessageBundle.getMnemonic("angal.common.new.btn.key"));
			jButtonNew.addActionListener(actionEvent -> {

				PriceList newList = new PriceList(0, "", "", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				ListEdit editList = new ListEdit(myFrame, newList, true);
				editList.addListListener(ListBrowser.this);
				editList.setVisible(true);

			});

		}
		return jButtonNew;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonNew());
			jPanelButtons.add(getJButtonCopy());
			jPanelButtons.add(getJButtonEdit());
			jPanelButtons.add(getJButtonDelete());
			jPanelButtons.add(getJButtonClose());
		}
		return jPanelButtons;
	}

	private JScrollPane getJScrollPaneTable() {
		if (jScrollPaneTable == null) {
			jScrollPaneTable = new JScrollPane();
			jScrollPaneTable.setViewportView(getJTablePriceLists());
		}
		return jScrollPaneTable;
	}

	private JTable getJTablePriceLists() {
		if (jTablePriceLists == null) {
			jTablePriceLists = new JTable();
			jTablePriceLists.setModel(new ListBrowserModel());

			for (int i = 0; i < columnWidth.length; i++) {
				jTablePriceLists.getColumnModel().getColumn(i).setPreferredWidth(columnWidth[i]);

		    	if (!columnResizable[i]) {
				    jTablePriceLists.getColumnModel().getColumn(i).setMaxWidth(columnWidth[i]);
			    }
			}
			jTablePriceLists.setAutoCreateColumnsFromModel(false);
		}
		return jTablePriceLists;
	}
	
	class ListBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public ListBrowserModel() {
			listManager = Context.getApplicationContext().getBean(PriceListManager.class);
			try {
				listArray = listManager.getLists();
			} catch(OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}
		@Override
		public int getRowCount() {
			if (listArray == null) {
				return 0;
			}
			return listArray.size();
		}
		
		@Override
		public String getColumnName(int c) {
			return columnNames[c];
		}
		
		@Override
		public int getColumnCount() {
			return columnNames.length;
		}
		
		@Override
		public Object getValueAt(int r, int c) {
			if (c == -1) {
				return listArray.get(r);
			} else if (c == 0) {
				return listArray.get(r).getCode();
			} else if (c == 1) {
				return listArray.get(r).getName();
			} else if (c == 2) {
				return listArray.get(r).getDescription();
			} else if (c == 3) {
				return listArray.get(r).getCurrency();
			} 
			return null;
		}
		
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}
}
