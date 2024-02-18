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
import java.awt.Dimension;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.pricesothers.gui.PricesOthersEdit.PricesOthersListener;
import org.isf.pricesothers.manager.PricesOthersManager;
import org.isf.pricesothers.model.PricesOthers;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;

public class PricesOthersBrowser extends ModalJFrame implements PricesOthersListener {

	@Override
	public void pricesOthersInserted(AWTEvent e) {
		jTablePricesOthers.setModel(new PricesOthersBrowserModel());
	}

	@Override
	public void pricesOthersUpdated(AWTEvent e) {
		jTablePricesOthers.setModel(new PricesOthersBrowserModel());
	}
	
	private static final long serialVersionUID = 1L;
	private JTable jTablePricesOthers;
	private JScrollPane jScrollPaneTable;
	private JPanel jPanelButtons;
	private JButton jButtonNew;
	private JButton jButtonEdit;
	private JButton jButtonDelete;
	private JButton jButtonClose;
	private String[] columnNames = {
			MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.description.txt").toUpperCase(),
			MessageBundle.getMessage("angal.pricesothers.opd.col").toUpperCase(),
			MessageBundle.getMessage("angal.pricesothers.ipd.col").toUpperCase(),
			MessageBundle.getMessage("angal.pricesothers.daily.col").toUpperCase(),
			MessageBundle.getMessage("angal.common.discharge.txt").toUpperCase(),
			MessageBundle.getMessage("angal.common.undefined.txt").toUpperCase()
	};
	private int[] columnWidth = {100, 100, 50, 50, 50, 100, 100};
	private boolean[] columnResizable = {false, true, false, false, false, false, false};
	
	protected static Class<?>[] cTypes = {String.class, String.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class};
	
	private PricesOthers pOthers;
	private PricesOthersManager pricesOthersManager = Context.getApplicationContext().getBean(PricesOthersManager.class);
	private List<PricesOthers> pOthersArray;
	private JFrame myFrame;
	
	public PricesOthersBrowser() {
		myFrame = this;
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	private void initComponents() {
		setTitle(MessageBundle.getMessage("angal.pricesothers.otherpricesbrowser.title"));
		add(getJScrollPaneTable(), BorderLayout.CENTER);
		add(getJPanelButtons(), BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(null);
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
				if (jTablePricesOthers.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.pricesothers.pleaseselectanitemtodelete");
				} else {
					int selectedRow = jTablePricesOthers.getSelectedRow();
					pOthers = (PricesOthers) jTablePricesOthers.getModel().getValueAt(selectedRow, -1);
					if (pOthers.getId() == 1) {
						MessageDialog.error(null, "angal.sql.operationnotpermittedprotectedelement");
						return;
					}
					int answer = MessageDialog.yesNo(null, "angal.pricesothers.deletethisitem.fmt.msg", pOthers.getDescription());
					if (answer == JOptionPane.OK_OPTION) {
						try {
							pricesOthersManager.deleteOther(pOthers);
							jTablePricesOthers.setModel(new PricesOthersBrowserModel());
						} catch (OHServiceException e) {
							MessageDialog.error(null, "angal.pricesothers.thedatacouldnotbedeleted");
							OHServiceExceptionUtil.showMessages(e);
						}
					}
				}
			});
		}
		return jButtonDelete;
	}

	private JButton getJButtonEdit() {
		if (jButtonEdit == null) {
			jButtonEdit = new JButton(MessageBundle.getMessage("angal.common.edit.btn"));
			jButtonEdit.setMnemonic(MessageBundle.getMnemonic("angal.common.edit.btn.key"));
			jButtonEdit.addActionListener(actionEvent -> {
				if (jTablePricesOthers.getSelectedRow() < 0) {
					MessageDialog.error(null, "angal.pricesothers.pleaseselectanitemtoedit");
				} else {
					int selectedRow = jTablePricesOthers.getSelectedRow();
					PricesOthers pOther = (PricesOthers)jTablePricesOthers.getModel().getValueAt(selectedRow, -1);
					PricesOthersEdit editOther = new PricesOthersEdit(myFrame, pOther, false);
					editOther.addOtherListener(this);
					editOther.setVisible(true);
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
				PricesOthers pOther = new PricesOthers("", "", true, true, false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				PricesOthersEdit editOther = new PricesOthersEdit(myFrame, pOther, true);
				editOther.addOtherListener(this);
				editOther.setVisible(true);
			});
		}
		return jButtonNew;
	}

	private JPanel getJPanelButtons() {
		if (jPanelButtons == null) {
			jPanelButtons = new JPanel();
			jPanelButtons.add(getJButtonNew());
			jPanelButtons.add(getJButtonEdit());
			jPanelButtons.add(getJButtonDelete());
			jPanelButtons.add(getJButtonClose());
		}
		return jPanelButtons;
	}

	private JScrollPane getJScrollPaneTable() {
		if (jScrollPaneTable == null) {
			jScrollPaneTable = new JScrollPane();
			jScrollPaneTable.setViewportView(getJTablePricesOthers());
			jScrollPaneTable.setSize(jTablePricesOthers.getPreferredSize());
		}
		return jScrollPaneTable;
	}

	private JTable getJTablePricesOthers() {
		if (jTablePricesOthers == null) {
			jTablePricesOthers = new JTable() {

				private static final long serialVersionUID = 1L;

				// Override this method so that it returns the preferred
				// size of the JTable instead of the default fixed size
				@Override
				public Dimension getPreferredScrollableViewportSize() {
					return new Dimension((int) getPreferredSize().getWidth(), 200);
				}
			};
			jTablePricesOthers.setModel(new PricesOthersBrowserModel());
			for (int i = 0; i < columnWidth.length; i++) {
				jTablePricesOthers.getColumnModel().getColumn(i).setMinWidth(columnWidth[i]);
				if (!columnResizable[i]) {
					jTablePricesOthers.getColumnModel().getColumn(i).setMaxWidth(columnWidth[i]);
				}
			}
			jTablePricesOthers.setAutoCreateColumnsFromModel(false);
		}
		return jTablePricesOthers;
	}

	class PricesOthersBrowserModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public PricesOthersBrowserModel() {
			try {
				pOthersArray = pricesOthersManager.getOthers();
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
		}

		@Override
		public int getRowCount() {
			if (pOthersArray == null) {
				return 0;
			}
			return pOthersArray.size();
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

			PricesOthers price = pOthersArray.get(r);
			if (c == -1) {
				return price;
			} else if (c == 0) {
				return price.getCode();
			} else if (c == 1) {
				return price.getDescription();
			} else if (c == 2) {
				return price.isOpdInclude();
			} else if (c == 3) {
				return price.isIpdInclude();
			} else if (c == 4) {
				return price.isDaily();
			} else if (c == 5) {
				return price.isDischarge();
			} else if (c == 6) {
				return price.isUndefined();
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return cTypes[column];
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

}
