/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.medicalinventory.gui;

import static org.isf.utils.Constants.DATE_TIME_FORMATTER;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicalinventory.manager.MedicalInventoryManager;
import org.isf.medicalinventory.manager.MedicalInventoryRowManager;
import org.isf.medicalinventory.model.MedicalInventory;
import org.isf.medicalinventory.model.MedicalInventoryRow;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.medicalstock.manager.MovStockInsertingManager;
import org.isf.medicalstock.model.Lot;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.utils.db.NormalizeString;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateChooser;
import org.isf.utils.jobjects.InventoryStatus;
import org.isf.utils.jobjects.InventoryType;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.jobjects.TextPrompt;
import org.isf.utils.jobjects.TextPrompt.Show;
import org.isf.utils.time.TimeTools;

public class InventoryEdit extends ModalJFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static EventListenerList InventoryListeners = new EventListenerList();

	public interface InventoryListener extends EventListener {

		public void InventoryInserted(AWTEvent e);

		public void InventoryUpdated(AWTEvent e);

		public void InventoryCancelled(AWTEvent e);
	}

	public static void addInventoryListener(InventoryListener l) {
		InventoryListeners.add(InventoryListener.class, l);
	}

	public static void removeInventoryListener(InventoryListener listener) {
		InventoryListeners.remove(InventoryListener.class, listener);
	}

	private void fireInventoryUpdated() {
		AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = InventoryListeners.getListeners(InventoryListener.class);
		for (int i = 0; i < listeners.length; i++)
			((InventoryListener) listeners[i]).InventoryUpdated(event);
	}

	private GoodDateChooser jCalendarInventory;
	private LocalDateTime dateInventory = TimeTools.getServerDateTime();
	private JPanel panelHeader;
	private JPanel panelFooter;
	private JPanel panelContent;
	private JButton closeButton;
	private JButton deleteButton;
	private JButton saveButton;
	private JScrollPane scrollPaneInventory;
	private JTable jTableInventoryRow;
	private List<MedicalInventoryRow> inventoryRowList;
	private List<MedicalInventoryRow> inventoryRowSearchList;
	private String[] pColums = { MessageBundle.getMessage("angal.common.code.txt").toUpperCase(),
			MessageBundle.getMessage("angal.inventory.product.col").toUpperCase(),
			MessageBundle.getMessage("angal.wardpharmacy.lotnumber.col").toUpperCase(),
			MessageBundle.getMessage("angal.medicalstock.duedate.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventoryrow.theorticqty.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventoryrow.realqty.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventoryrow.unitprice.col").toUpperCase(),
			MessageBundle.getMessage("angal.inventory.totalprice").toUpperCase() };
	private int[] pColumwidth = { 100, 200, 100, 100, 100, 80, 80, 80 };
	private boolean[] columnEditable = { false, false, false, false, false, true, true, false };
	private boolean[] columnEditableView = { false, false, false, false, false, false, false, false };
	private MedicalInventory inventory = null;
	private JRadioButton specificRadio;
	private JRadioButton allRadio;
	private JTextField searchTextField;
	private JLabel dateInventoryLabel;
	private JTextField codeTextField;
	private String code = null;
	private String mode = null;
	private JLabel referenceLabel;
	private JTextField referenceTextField;
	private JTextField jTetFieldEditor;
	private JButton moreData;
	private static boolean MORE_DATA = true;
	private MedicalInventoryManager medicalInventoryManager = Context.getApplicationContext().getBean(MedicalInventoryManager.class);
	private MedicalInventoryRowManager medicalInventoryRowManager = Context.getApplicationContext().getBean(MedicalInventoryRowManager.class);
	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);
	private MovStockInsertingManager movStockInsertingManager = Context.getApplicationContext().getBean(MovStockInsertingManager.class);

	public InventoryEdit() {
		initComponents();
		mode = "new";
	}

	public InventoryEdit(MedicalInventory inventory, String modee) {
		this.inventory = inventory;
		mode = modee;
		initComponents();
		if (mode.equals("view")) {
			saveButton.setVisible(false);
			columnEditable = columnEditableView;
		}

	}

	private void initComponents() {
		inventoryRowList = new ArrayList<>();
		inventoryRowSearchList = new ArrayList<>();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(950, 580));
		setLocationRelativeTo(null);
		setTitle(MessageBundle.getMessage("angal.inventory.neweditinventory.title"));

		getContentPane().setLayout(new BorderLayout());

		panelHeader = getPanelHeader();

		getContentPane().add(panelHeader, BorderLayout.NORTH);

		panelContent = getPanelContent();
		getContentPane().add(panelContent, BorderLayout.CENTER);

		panelFooter = getPanelFooter();
		getContentPane().add(panelFooter, BorderLayout.SOUTH);

		ajustWidth();

		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				if (inventoryRowList != null) {
					inventoryRowList.clear();
				}
				if (inventoryRowSearchList != null) {
					inventoryRowSearchList.clear();
				}
				dispose();
			}
		});
	}

	private JPanel getPanelHeader() {
		if (panelHeader == null) {
			panelHeader = new JPanel();
			panelHeader.setBorder(new EmptyBorder(5, 0, 5, 0));
			GridBagLayout gbl_panelHeader = new GridBagLayout();
			gbl_panelHeader.columnWidths = new int[] { 159, 191, 192, 218, 51, 0 };
			gbl_panelHeader.rowHeights = new int[] { 30, 30, 0 };
			gbl_panelHeader.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			gbl_panelHeader.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panelHeader.setLayout(gbl_panelHeader);
			GridBagConstraints gbc_dateInventoryLabel = new GridBagConstraints();
			gbc_dateInventoryLabel.insets = new Insets(0, 0, 5, 5);
			gbc_dateInventoryLabel.gridx = 0;
			gbc_dateInventoryLabel.gridy = 0;
			panelHeader.add(getDateInventoryLabel(), gbc_dateInventoryLabel);

			GridBagConstraints gbc_jCalendarInventory = new GridBagConstraints();
			gbc_jCalendarInventory.fill = GridBagConstraints.HORIZONTAL;
			gbc_jCalendarInventory.insets = new Insets(0, 0, 5, 5);
			gbc_jCalendarInventory.gridx = 1;
			gbc_jCalendarInventory.gridy = 0;
			panelHeader.add(getJCalendarFrom(), gbc_jCalendarInventory);
			GridBagConstraints gbc_referenceLabel = new GridBagConstraints();
			gbc_referenceLabel.anchor = GridBagConstraints.EAST;
			gbc_referenceLabel.insets = new Insets(0, 0, 5, 5);
			gbc_referenceLabel.gridx = 2;
			gbc_referenceLabel.gridy = 0;
			panelHeader.add(getReferenceLabel(), gbc_referenceLabel);
			GridBagConstraints gbc_referenceTextField = new GridBagConstraints();
			gbc_referenceTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_referenceTextField.insets = new Insets(0, 0, 5, 5);
			gbc_referenceTextField.gridx = 3;
			gbc_referenceTextField.gridy = 0;
			panelHeader.add(getReferenceTextField(), gbc_referenceTextField);
			GridBagConstraints gbc_specificRadio = new GridBagConstraints();
			gbc_specificRadio.anchor = GridBagConstraints.EAST;
			gbc_specificRadio.insets = new Insets(0, 0, 0, 5);
			gbc_specificRadio.gridx = 0;
			gbc_specificRadio.gridy = 1;
			panelHeader.add(getSpecificRadio(), gbc_specificRadio);
			GridBagConstraints gbc_codeTextField = new GridBagConstraints();
			gbc_codeTextField.insets = new Insets(0, 0, 0, 5);
			gbc_codeTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_codeTextField.gridx = 1;
			gbc_codeTextField.gridy = 1;
			panelHeader.add(getCodeTextField(), gbc_codeTextField);
			GridBagConstraints gbc_allRadio = new GridBagConstraints();
			gbc_allRadio.anchor = GridBagConstraints.EAST;
			gbc_allRadio.insets = new Insets(0, 0, 0, 5);
			gbc_allRadio.gridx = 2;
			gbc_allRadio.gridy = 1;
			panelHeader.add(getAllRadio(), gbc_allRadio);
			GridBagConstraints gbc_searchTextField = new GridBagConstraints();
			gbc_searchTextField.insets = new Insets(0, 0, 0, 5);
			gbc_searchTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_searchTextField.gridx = 3;
			gbc_searchTextField.gridy = 1;
			JPanel panSearchTextField = new JPanel();
			panSearchTextField.setLayout(new FlowLayout(FlowLayout.LEFT));
			panSearchTextField.add(getSearchTextField());
			panSearchTextField.add(getMoreDataBtn());
			panelHeader.add(panSearchTextField, gbc_searchTextField);
			ButtonGroup group = new ButtonGroup();
			group.add(specificRadio);
			group.add(allRadio);
		}
		return panelHeader;
	}

	private JPanel getPanelContent() {
		if (panelContent == null) {
			panelContent = new JPanel();
			GridBagLayout gbl_panelContent = new GridBagLayout();
			gbl_panelContent.columnWidths = new int[] { 452, 0 };
			gbl_panelContent.rowHeights = new int[] { 402, 0 };
			gbl_panelContent.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
			gbl_panelContent.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
			panelContent.setLayout(gbl_panelContent);
			GridBagConstraints gbc_scrollPaneInventory = new GridBagConstraints();
			gbc_scrollPaneInventory.fill = GridBagConstraints.BOTH;
			gbc_scrollPaneInventory.gridx = 0;
			gbc_scrollPaneInventory.gridy = 0;
			panelContent.add(getScrollPaneInventory(), gbc_scrollPaneInventory);
		}
		return panelContent;
	}

	private JPanel getPanelFooter() {
		if (panelFooter == null) {
			panelFooter = new JPanel();
			panelFooter.add(getNewButton());
			panelFooter.add(getDeleteButton());
			panelFooter.add(getCloseButton());
		}
		return panelFooter;
	}

	private GoodDateChooser getJCalendarFrom() {
		if (jCalendarInventory == null) {
			dateInventory = LocalDateTime.now();
			jCalendarInventory = new GoodDateChooser(LocalDate.now());
			if (inventory != null) {
				jCalendarInventory.setDate(inventory.getInventoryDate().toLocalDate());
			}
			jCalendarInventory.addPropertyChangeListener("date", new PropertyChangeListener() {

				public void propertyChange(PropertyChangeEvent evt) {
				}
			});
		}
		return jCalendarInventory;
	}

	private JButton getNewButton() {
		saveButton = new JButton(MessageBundle.getMessage("angal.common.save.btn"));
		saveButton.setMnemonic(MessageBundle.getMnemonic("angal.common.save.btn.key"));
		saveButton.addActionListener(actionEvent -> {
			String State = InventoryStatus.draft.toString();
			String user = UserBrowsingManager.getCurrentUser();
			int checkResults = 0;
			if (inventoryRowSearchList == null || inventoryRowSearchList.size() < 1) {
				MessageDialog.error(null, "angal.inventory.noproduct.msg");
				return;
			}
			LocalDateTime now = LocalDateTime.now();
			if (dateInventory.isAfter(now)) {
				MessageDialog.error(null, "angal.inventory.notdateinfuture.msg");
				return;
			}

			if ((inventory == null) && (mode.equals("new"))) {
				String reference = referenceTextField.getText().trim();
				if (reference.equals("")) {
					MessageDialog.error(null, "angal.inventory.mustenterareference.msg");
					return;
				}
				if (medicalInventoryManager.referenceExists(reference)) {
					MessageDialog.error(null, "angal.inventory.referencealreadyused.msg");
					return;
				}
				inventory = new MedicalInventory();
				inventory.setInventoryReference(reference);
				inventory.setInventoryDate(dateInventory);
				inventory.setStatus(State);
				inventory.setUser(user);
				inventory.setInventoryType(InventoryType.principal.toString());
				MedicalInventory meInventory;
				try {
					meInventory = medicalInventoryManager.newMedicalInventory(inventory);
					if (meInventory != null) {
						MedicalInventoryRow currentInventoryRow;
						for (Iterator<MedicalInventoryRow> iterator = inventoryRowSearchList.iterator(); iterator.hasNext();) {
							MedicalInventoryRow medicalInventoryRow = (MedicalInventoryRow) iterator.next();
							medicalInventoryRow.setInventory(meInventory);
							Lot lot = medicalInventoryRow.getLot();
							if (lot != null && lot.getCode().equals("")) {
								medicalInventoryRow.setLot(null);
							}
							currentInventoryRow = medicalInventoryRowManager.newMedicalInventoryRow(medicalInventoryRow);
							if (currentInventoryRow == null) {
								checkResults++;
							}
						}
						if (checkResults == 0) {
							// enable validation
							mode = "update";
							MessageDialog.info(this, "angal.inventory.savesucces.msg");
							fireInventoryUpdated();
							closeButton.doClick();
						} else {
							MessageDialog.error(null, "angal.inventory.error.msg");
						}
					} else {
						MessageDialog.error(null, "angal.inventory.error.msg");
					}
				} catch (OHServiceException e) {
					OHServiceExceptionUtil.showMessages(e);
				}
			}
		});
		return saveButton;
	}
	private JButton getDeleteButton() {
		deleteButton = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		deleteButton.addActionListener(actionEvent -> {
			if (jTableInventoryRow.getSelectedRowCount() > 1) {
				MessageDialog.error(this, "angal.inventoryrow.pleaseselectonlyoneinventoryrow.msg");
				return;
			}
			int selectedRow = jTableInventoryRow.getSelectedRow();
			if (selectedRow == -1) {
				MessageDialog.error(this, "angal.inventoryrow.pleaseselectonlyoneinventoryrow.msg");
				return;
			}
			MedicalInventoryRow selectedInventory = (MedicalInventoryRow) jTableInventoryRow.getValueAt(selectedRow, -1);
			int delete = MessageDialog.yesNo(null, "angal.inventoryrow.doyoureallywanttodeletethisinventoryrow.msg");
			if (delete == JOptionPane.YES_OPTION) {
				if (selectedInventory.getInventory() == null) {
					inventoryRowSearchList.remove(selectedRow);
				} else {

				}
			} else {
				return;
			}
			jTableInventoryRow.updateUI();
		});
		return deleteButton;
	}
	private JButton getCloseButton() {
		closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		closeButton.addActionListener(actionEvent -> {
			dispose();
		});
		return closeButton;
	}

	private JScrollPane getScrollPaneInventory() {
		if (scrollPaneInventory == null) {
			scrollPaneInventory = new JScrollPane();
			scrollPaneInventory.setViewportView(getJTableInventoryRow());
		}
		return scrollPaneInventory;
	}

	private JTable getJTableInventoryRow() {
		if (jTableInventoryRow == null) {
			jTableInventoryRow = new JTable();
			jTetFieldEditor = new JTextField();
			jTableInventoryRow.setFillsViewportHeight(true);
			jTableInventoryRow.setModel(new InventoryRowModel());
			jTableInventoryRow.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting()) {
						jTableInventoryRow.editCellAt(jTableInventoryRow.getSelectedRow(), 5);
						jTetFieldEditor.selectAll();
					}

				}
			});
			jTableInventoryRow.getModel().addTableModelListener(new TableModelListener() {

				@Override
				public void tableChanged(TableModelEvent e) {

					if (e.getType() == TableModelEvent.UPDATE) {
						int row = e.getFirstRow();
						int column = e.getColumn();
						TableModel model = (TableModel) e.getSource();
						Object data = model.getValueAt(row, column);

						if (column == 2) {
							Object data2 = model.getValueAt(row, 3);
							if (!data.toString().equals("") && data2.toString().equals("")) {
								jTableInventoryRow.setSurrendersFocusOnKeystroke(true);
								jTableInventoryRow.getEditorComponent().requestFocus();
								return;
							}
						}

						if (column == 3) {
							Object data2 = model.getValueAt(row, 2);
							if (!data.toString().equals("") && data2.toString().equals("")) {
								jTableInventoryRow.setSurrendersFocusOnKeystroke(true);
								jTableInventoryRow.getEditorComponent().requestFocus();
								return;
							}
						}
					}
				}
			});
			DefaultCellEditor cellEditor = new DefaultCellEditor(jTetFieldEditor);
			jTableInventoryRow.setDefaultEditor(Integer.class, cellEditor);
		}
		return jTableInventoryRow;
	}

	class InventoryRowModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public InventoryRowModel() {
			if (inventoryRowList != null) {
				inventoryRowSearchList = new ArrayList<>();
				inventoryRowSearchList.addAll(inventoryRowList);
			}
		}

		public Class< ? > getColumnClass(int c) {
			if (c == 0) {
				return String.class;
			} else if (c == 1) {
				return String.class;
			} else if (c == 2) {
				return String.class;
			} else if (c == 3) {
				return String.class;
			} else if (c == 4) {
				return Integer.class;
			} else if (c == 5) {
				return Integer.class;
			} else if (c == 6) {
				return Integer.class;
			} else if (c == 7) {
				return Integer.class;
			}
			return null;
		}

		public int getRowCount() {
			if (inventoryRowSearchList == null)
				return 0;
			return inventoryRowSearchList.size();
		}

		public String getColumnName(int c) {
			return pColums[c];
		}

		public int getColumnCount() {
			return pColums.length;
		}

		public Object getValueAt(int r, int c) {
			MedicalInventoryRow medInvtRow = inventoryRowSearchList.get(r);
			if (c == -1) {
				return medInvtRow;
			} else if (c == 0) {
				return medInvtRow.getMedical() == null ? "" : medInvtRow.getMedical().getProdCode();
			} else if (c == 1) {
				return medInvtRow.getMedical() == null ? "" : medInvtRow.getMedical().getDescription();
			} else if (c == 2) {
				if (medInvtRow.getLot() == null) {
					return "";
				}
				return medInvtRow.getLot().getCode();
			} else if (c == 3) {
				if (medInvtRow.getLot() != null) {
					if (medInvtRow.getLot().getDueDate() != null) {
						return medInvtRow.getLot().getDueDate().format(DATE_TIME_FORMATTER);
					}
				}
				return "";
			} else if (c == 4) {
				Double dblVal = medInvtRow.getTheoreticQty();
				return dblVal.intValue();
			} else if (c == 5) {
				Double dblValue = medInvtRow.getRealQty();
				return dblValue.intValue();
			} else if (c == 6) {
				if (medInvtRow.getLot() != null) {
					if (medInvtRow.getLot().getCost() != null) {
						return medInvtRow.getLot().getCost();
					}
				}
				return 0.0;
			} else if (c == 7) {
				if (medInvtRow.getLot() != null) {
					if (medInvtRow.getLot().getCost() != null) {
						return medInvtRow.getRealQty() * medInvtRow.getLot().getCost().doubleValue();
					}
				}
				return 0.0;
			}
			return null;
		}

		@Override
		public void setValueAt(Object value, int r, int c) {
			if (r < inventoryRowSearchList.size()) {
				MedicalInventoryRow invRow = inventoryRowSearchList.get(r);
				if (c == 5) {
					Integer intValue = 0;
					try {
						intValue = Integer.parseInt(value.toString());
					} catch (NumberFormatException e) {
						intValue = 0;
					}

					invRow.setRealqty(intValue);
					inventoryRowSearchList.set(r, invRow);
					jTableInventoryRow.updateUI();
				}
				if (c == 6) {
					Double doubleValue = 0.0;
					try {
						doubleValue = Double.parseDouble(value.toString());
					} catch (NumberFormatException e) {
						doubleValue = 0.0;
					}
					Lot lot = invRow.getLot();
					if (lot != null) {
						if (lot.getCode().equals("")) {
							MessageDialog.error(null, "angal.inventoryrow.cannotchangethepriceofproductwithoutlot.msg");
						} else {
							lot.setCost(new BigDecimal(doubleValue));
							try {
								Lot saveLot = movStockInsertingManager.updateLot(lot);
								invRow.setLot(saveLot);
							} catch (OHServiceException e) {
								OHServiceExceptionUtil.showMessages(e);
							}
						}
					}
					inventoryRowSearchList.set(r, invRow);
					jTableInventoryRow.updateUI();
				}
			}

		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnEditable[columnIndex];
		}

	}

	class DecimalFormatRenderer extends DefaultTableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final DecimalFormat formatter = new DecimalFormat("#,##0.00");

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.addFocusListener(new FocusListener() {

				@Override
				public void focusGained(FocusEvent e) {
				}

				@Override
				public void focusLost(FocusEvent e) {
				}
			});

			value = formatter.format((Number) value);
			if (!columnEditable[column]) {
				cell.setBackground(Color.LIGHT_GRAY);
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}

	public MedicalInventory getInventory() {
		return inventory;
	}

	public void setInventory(MedicalInventory inventory) {
		this.inventory = inventory;
	}

	private JRadioButton getSpecificRadio() {
		if (specificRadio == null) {
			specificRadio = new JRadioButton(MessageBundle.getMessage("angal.inventory.specificproduct.txt"));
			if (inventory != null) {
				specificRadio.setSelected(false);
			} else {
				specificRadio.setSelected(true);
			}
			specificRadio.addActionListener(actionEvent -> {
				if (specificRadio.isSelected()) {
					codeTextField.setEnabled(true);
					searchTextField.setEnabled(false);
					moreData.setEnabled(false);
					searchTextField.setText("");
					codeTextField.setText("");
					if (inventoryRowList != null) {
						inventoryRowList.clear();
					}
					if (inventoryRowSearchList != null) {
						inventoryRowSearchList.clear();
					}
					jTableInventoryRow.updateUI();
					ajustWidth();
				}
			});
		}
		return specificRadio;
	}

	private JRadioButton getAllRadio() {
		if (allRadio == null) {
			allRadio = new JRadioButton(MessageBundle.getMessage("angal.inventory.allproduct.txt"));
			if (inventory != null) {
				allRadio.setSelected(true);
			} else {
				allRadio.setSelected(false);
			}
			allRadio.addActionListener(actionEvent -> {
				if (allRadio.isSelected()) {
					codeTextField.setEnabled(false);
					searchTextField.setText("");
					codeTextField.setText("");
					searchTextField.setEnabled(true);
					if (inventory == null) {
						moreData.setEnabled(true);
					}
					if (inventoryRowList != null) {
						inventoryRowList.clear();
					}
					if (inventoryRowSearchList != null) {
						inventoryRowSearchList.clear();
					}
					jTableInventoryRow.setModel(new InventoryRowModel());
					jTableInventoryRow.updateUI();
					code = null;
					ajustWidth();
				}
			});
		}
		return allRadio;
	}

	private JTextField getSearchTextField() {
		if (searchTextField == null) {
			searchTextField = new JTextField();
			searchTextField.setColumns(16);
			TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.common.search.txt"), searchTextField, Show.FOCUS_LOST);
			suggestion.setFont(new Font("Tahoma", Font.PLAIN, 12));
			suggestion.setForeground(Color.GRAY);
			suggestion.setHorizontalAlignment(JLabel.CENTER);
			suggestion.changeAlpha(0.5f);
			suggestion.changeStyle(Font.BOLD + Font.ITALIC);
			searchTextField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void insertUpdate(DocumentEvent e) {
					filterInventoryRow();
					ajustWidth();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					filterInventoryRow();
					ajustWidth();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					filterInventoryRow();
					ajustWidth();
				}
			});
			searchTextField.setEnabled(false);
			if (inventory != null) {
				searchTextField.setEnabled(true);
			} else {
				searchTextField.setEnabled(false);
			}
		}
		return searchTextField;
	}

	private JLabel getDateInventoryLabel() {
		if (dateInventoryLabel == null) {
			dateInventoryLabel = new JLabel(MessageBundle.getMessage("angal.common.date.txt"));
		}
		return dateInventoryLabel;
	}

	private JButton getMoreDataBtn() {
		if (moreData == null) {
			moreData = new JButton("...");
			moreData.setPreferredSize(new Dimension(20, 20));
			moreData.setEnabled(false);
		}
		moreData.addActionListener(actionEvent -> {
			if (MORE_DATA && inventory == null) {
				List<Medical> medicalList = new ArrayList<Medical>();
				try {
					medicalList = medicalBrowsingManager.getMedicals();
				} catch (OHServiceException e) {
					e.printStackTrace();
				}
				List<Lot> lots = null;
				MedicalInventoryRow inventoryRowTemp = null;
				Iterator<Medical> medicalIterator = medicalList.iterator();
				Medical medical;
				while (medicalIterator.hasNext()) {
					medical = medicalIterator.next();
					try {
						lots = movStockInsertingManager.getLotByMedical(medical);
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
					}
					if (lots.size() == 0) {
						inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, medical, new Lot("", null, null));
						inventoryRowList.add(inventoryRowTemp);
					} else {
						for (Lot lot : lots) {
							inventoryRowTemp = new MedicalInventoryRow(0, lot.getOverallQuantity(), lot.getOverallQuantity(), null, medical, lot);
							inventoryRowList.add(inventoryRowTemp);
						}
					}
				}
				inventoryRowSearchList = new ArrayList<MedicalInventoryRow>();
				inventoryRowSearchList.addAll(inventoryRowList);
				if (medicalList.size() > 0) {
					jTableInventoryRow.updateUI();
				}
			}
		});
		return moreData;
	}
	
	private JTextField getCodeTextField() {
		if (codeTextField == null) {
			codeTextField = new JTextField();
			if (inventory != null) {
				codeTextField.setEnabled(false);
			} else {
				codeTextField.setEnabled(true);
			}
			codeTextField.setColumns(10);
			TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.common.code.txt"), codeTextField, Show.FOCUS_LOST);
			suggestion.setFont(new Font("Tahoma", Font.PLAIN, 12));
			suggestion.setForeground(Color.GRAY);
			suggestion.setHorizontalAlignment(JLabel.CENTER);
			suggestion.changeAlpha(0.5f);
			suggestion.changeStyle(Font.BOLD + Font.ITALIC);
			codeTextField.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						code = codeTextField.getText().trim();
						code = code.toLowerCase();
						try {
							addInventoryRow(code);
						} catch (OHServiceException e1) {
							OHServiceExceptionUtil.showMessages(e1);
						}
						codeTextField.setText("");
						ajustWidth();
					}
				}
			});
		}
		return codeTextField;
	}

	private void addInventoryRow(String code) throws OHServiceException {
		List<MedicalInventoryRow> inventoryRowsList = new ArrayList<MedicalInventoryRow>();
		List<Medical> medicalList = new ArrayList<Medical>();
		List<Lot> lots = null;
		Medical medical = null;
		MedicalInventoryRow inventoryRowTemp = null;
		if (code != null) {
			medical = medicalBrowsingManager.getMedicalByMedicalCode(code);
			if (medical != null) {
				boolean found = false;
				if (inventoryRowSearchList != null) {
					for (MedicalInventoryRow row : inventoryRowSearchList) {
						if (row.getMedical().getCode().equals(medical.getCode())) {
							found = true;
						}
					}
				}
				if (!found) {
					medicalList.add(medical);
				}
			} else {
				medical = chooseMedical(code);
				if (medical != null) {
					boolean found = false;
					if (inventoryRowSearchList != null) {
						for (MedicalInventoryRow row : inventoryRowSearchList) {
							if (row.getMedical().getCode().equals(medical.getCode())) {
								found = true;
							}
						}
					}
					if (!found) {
						medicalList.add(medical);
					}
				}
			}
		} else {
			medicalList = medicalBrowsingManager.getMedicals();
		}
		if (mode.equals("new")) {
			for (Iterator<Medical> iterator = medicalList.iterator(); iterator.hasNext();) {
				medical = (Medical) iterator.next();
				lots = movStockInsertingManager.getLotByMedical(medical);
				if (lots.size() == 0) {
					inventoryRowTemp = new MedicalInventoryRow(0, 0.0, 0.0, null, medical, new Lot("", null, null));
					inventoryRowsList.add(inventoryRowTemp);
				} else {
					for (Iterator<Lot> iterator2 = lots.iterator(); iterator2.hasNext();) {
						Lot lot = (Lot) iterator2.next();
						inventoryRowTemp = new MedicalInventoryRow(0, lot.getOverallQuantity(), lot.getOverallQuantity(), null, medical, lot);
						inventoryRowsList.add(inventoryRowTemp);
					}
				}
			}
		} else if (mode.equals("update")) {
			if (medical != null) {
				String medicalCode = medical.getProdCode();
				inventoryRowsList = medicalInventoryRowManager.getMedicalInventoryRowByInventoryId(inventory.getId()).stream()
								.filter(medRow -> medRow.getMedical().getProdCode().equals(medicalCode)).toList();
			}
		}
		for (MedicalInventoryRow inventoryRow : inventoryRowsList) {
			boolean found = false;
			for (MedicalInventoryRow row : inventoryRowSearchList) {
				if (row.getMedical().getCode().equals(inventoryRow.getMedical().getCode())
								&& (row.getLot().getCode().equals(inventoryRow.getLot().getCode()))) {
					found = true;
				}
			}
			if (!found) {
				inventoryRowSearchList.add(inventoryRow);
			}
		}
		jTableInventoryRow.updateUI();
	}

	private void filterInventoryRow() {
		String s = searchTextField.getText();
		s.trim();
		inventoryRowSearchList = new ArrayList<MedicalInventoryRow>();
		for (MedicalInventoryRow invRow : inventoryRowList) {
			if (!s.equals("")) {
				String name = invRow.getSearchString();
				if (name.contains(s.toLowerCase()))
					inventoryRowSearchList.add(invRow);
			} else {
				inventoryRowSearchList.add(invRow);
			}
		}
		jTableInventoryRow.updateUI();
		searchTextField.requestFocus();
	}

	private Medical chooseMedical(String text) throws OHServiceException {
		Map<String, Medical> medicalMap;
		List<Medical> medicals = medicalBrowsingManager.getMedicals();
		if (mode.equals("update")) {
			medicals.clear();
			List<MedicalInventoryRow> inventoryRowListTemp = medicalInventoryRowManager.getMedicalInventoryRowByInventoryId(inventory.getId());
			for (MedicalInventoryRow medicalInventoryRow : inventoryRowListTemp) {
				medicals.add(medicalInventoryRow.getMedical());
			}
		}
		medicalMap = new HashMap<String, Medical>();
		for (Medical med : medicals) {
			String key = med.getProdCode().toLowerCase();
			key = med.getCode().toString().toLowerCase();
			medicalMap.put(key, med);
		}
		ArrayList<Medical> medList = new ArrayList<Medical>();
		for (Medical aMed : medicalMap.values()) {
			if (NormalizeString.normalizeContains(aMed.getDescription().toLowerCase(), text)) {
				medList.add(aMed);
			}
		}
		Collections.sort(medList);
		Medical med = null;
		if (!medList.isEmpty()) {
			MedicalPicker framas = new MedicalPicker(new StockMedModel(medList), medList);
			framas.setSize(300, 400);
			JDialog dialog = new JDialog();
			dialog.setLocationRelativeTo(null);
			dialog.setSize(600, 350);
			dialog.setLocationRelativeTo(null);
			dialog.setModal(true);
			dialog.setTitle(MessageBundle.getMessage("angal.medicalstock.multiplecharging.selectmedical.title"));
			framas.setParentFrame(dialog);
			dialog.setContentPane(framas);
			dialog.setVisible(true);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			med = framas.getSelectedMedical();
			return med;
		}
		return null;
	}

	private void ajustWidth() {
		for (int i = 0; i < pColumwidth.length; i++) {
			jTableInventoryRow.getColumnModel().getColumn(i).setMinWidth(pColumwidth[i]);
		}
	}

	public EventListenerList getInventoryListeners() {
		return InventoryListeners;
	}

	public void setInventoryListeners(EventListenerList inventoryListeners) {
		InventoryListeners = inventoryListeners;
	}

	private JLabel getReferenceLabel() {
		if (referenceLabel == null) {
			referenceLabel = new JLabel(MessageBundle.getMessage("angal.common.reference.label"));
		}
		return referenceLabel;
	}

	private JTextField getReferenceTextField() {
		if (referenceTextField == null) {
			referenceTextField = new JTextField();
			referenceTextField.setColumns(10);
			if (inventory != null && !mode.equals("new")) {
				referenceTextField.setText(inventory.getInventoryReference());
				referenceTextField.setEnabled(false);
			}
		}
		return referenceTextField;
	}
}
