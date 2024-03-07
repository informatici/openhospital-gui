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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicalinventory.manager.MedicalInventoryManager;
import org.isf.medicalinventory.manager.MedicalInventoryRowManager;
import org.isf.medicalinventory.model.MedicalInventory;
import org.isf.medicalinventory.model.MedicalInventoryRow;
import org.isf.menu.manager.Context;
import org.isf.utils.jobjects.GoodDateChooser;
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
		public void InventoryUpdated(AWTEvent e);

		public void InventoryInserted(AWTEvent e);

		public void InventoryValidated(AWTEvent e);

		public void InventoryCancelled(AWTEvent e);
	}

	public static void addInventoryListener(InventoryListener l) {
		InventoryListeners.add(InventoryListener.class, l);
	}

	private GoodDateChooser jCalendarTo;
	private GoodDateChooser jCalendarInventory;
	private LocalDateTime dateInventory = TimeTools.getServerDateTime();
	private JLabel jLabelTo;
	private JPanel panelHeader;
	private JPanel panelFooter;
	private JPanel panelContent;
	private JButton closeButton;
	private JScrollPane scrollPaneInventory;
	private JTable jTableInventoryRow;
	private List<MedicalInventoryRow> inventoryRowList;
	private List<MedicalInventoryRow> inventoryRowSearchList;
	private String[] pColums = { MessageBundle.getMessage("angal.common.code.txt"),
			MessageBundle.getMessage("angal.medicalstockward.patient.drug.col"),
			MessageBundle.getMessage("angal.wardpharmacy.lotnumber.col"),
			MessageBundle.getMessage("angal.medicalstock.duedate.col"),
			MessageBundle.getMessage("angal.inventoryrow.theorticqty.col"),
			MessageBundle.getMessage("angal.inventoryrow.realqty.col") };
	private int[] pColumwidth = { 100, 300, 100, 100, 100, 100 };
	private boolean[] columnEditable = { false, false, false, false, false, true };
	private boolean[] columnEditableView = { false, false, false, false, false, false };
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
	private JLabel loaderLabel;

	private JButton moreData;
	private int MAX_COUNT = 30;
	private int CURRENT_INDEX = 0;
	private boolean MORE_DATA = true;
	private MedicalInventoryManager medicalInventoryManager = Context.getApplicationContext().getBean(MedicalInventoryManager.class);
	private MedicalInventoryRowManager medicalInventoryRowManager = Context.getApplicationContext().getBean(MedicalInventoryRowManager.class);
	
	public InventoryEdit() {
		initComponents();
		mode = "new";
	}

	public InventoryEdit(MedicalInventory inventory, String modee) {
		this.inventory = inventory;
		mode = modee;
		initComponents();
		if (mode.equals("view")) {
			columnEditable = columnEditableView;
		}
		
	}

	private void initComponents() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(850, 580));
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
				if (inventoryRowList != null)
					inventoryRowList.clear();
				if (inventoryRowSearchList != null)
					inventoryRowSearchList.clear();
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
			GridBagConstraints gbc_loaderLabel = new GridBagConstraints();
			gbc_loaderLabel.insets = new Insets(0, 0, 5, 0);
			gbc_loaderLabel.gridx = 4;
			gbc_loaderLabel.gridy = 0;
			panelHeader.add(getLoaderLabel(), gbc_loaderLabel);
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

	private JLabel getJLabelTo() {
		if (jLabelTo == null) {
			jLabelTo = new JLabel();
			jLabelTo.setText(MessageBundle.getMessage("angal.common.to.txt")); //$NON-NLS-1$
		}
		return jLabelTo;
	}
	
	private JButton getCloseButton() {
		closeButton = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		closeButton.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}

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
					if(!e.getValueIsAdjusting()){
						jTableInventoryRow.editCellAt(jTableInventoryRow.getSelectedRow(), 5);
						jTetFieldEditor.selectAll();
					}
					
				}
			});
			jTableInventoryRow.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyPressed(KeyEvent e) {}
			});
			DefaultCellEditor cellEditor=new DefaultCellEditor(jTetFieldEditor);
			jTableInventoryRow.setDefaultEditor(Integer.class, cellEditor);
		}
		return jTableInventoryRow;
	}

	class InventoryRowModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public InventoryRowModel() {
			if(inventoryRowList != null) {
				inventoryRowSearchList = new ArrayList<MedicalInventoryRow>();
				inventoryRowSearchList.addAll(inventoryRowList);
			}
		}

		public Class<?> getColumnClass(int c) {
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
				return medInvtRow.getMedical() == null ? "" : medInvtRow.getMedical().getCode();
			} else if (c == 1) {
				return medInvtRow.getMedical() == null ? "" : medInvtRow.getMedical().getDescription();
			} else if (c == 2) {
				return medInvtRow.getLot() == null ? "" : medInvtRow.getLot().getCode();
			} else if (c == 3) {
				return medInvtRow.getLot() == null ? "" : medInvtRow.getLot().getDueDate().format(DATE_TIME_FORMATTER);
			} else if (c == 4) {
				Double dblVal = medInvtRow.getTheoreticQty();
				return dblVal.intValue();
			} else if (c == 5) {
				Double dblValue = medInvtRow.getRealQty();
				return dblValue.intValue();
			}
			return null;
		}

		@Override
		public void setValueAt(Object value, int r, int c) {
			if(r < inventoryRowSearchList.size()){
				MedicalInventoryRow invRow = inventoryRowSearchList.get(r);
				if (c == 5) {
					Integer intValue=0;
					try{
						intValue=Integer.parseInt(value.toString());
					}
					catch (NumberFormatException e) {
						intValue=0;
					}
					
					invRow.setRealqty(intValue);
					inventoryRowSearchList.set(r, invRow);
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
		private final DecimalFormat formatter = new DecimalFormat("#,##0.00"); //$NON-NLS-1$

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			cell.addFocusListener(new java.awt.event.FocusListener()
		      {
		       
				@Override
				public void focusGained(java.awt.event.FocusEvent e) {				
				}

				@Override
				public void focusLost(java.awt.event.FocusEvent e) {				
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
			specificRadio.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
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
			allRadio.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {					
					if (allRadio.isSelected()) {
						codeTextField.setEnabled(false);
						searchTextField.setText("");
						codeTextField.setText("");
						searchTextField.setEnabled(true);
						if(inventory == null) {
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
				}
			});
		}
		return allRadio;
	}

	private JTextField getSearchTextField() {
		if (searchTextField == null) {
			searchTextField = new JTextField();
			searchTextField.setColumns(16);
			TextPrompt suggestion = new TextPrompt(
					MessageBundle
							.getMessage("angal.common.search.txt"),
							searchTextField, Show.FOCUS_LOST);
			{
				suggestion.setFont(new Font("Tahoma", Font.PLAIN, 12));
				suggestion.setForeground(Color.GRAY);
				suggestion.setHorizontalAlignment(JLabel.CENTER);
				suggestion.changeAlpha(0.5f);
				suggestion.changeStyle(Font.BOLD + Font.ITALIC);
			}
			searchTextField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {
					ajustWidth();
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					ajustWidth();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
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
		if(moreData == null) {
			moreData = new JButton("...");
			moreData.setPreferredSize(new Dimension(20, 20));
			moreData.setEnabled(false);
		}
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
			TextPrompt suggestion = new TextPrompt(
							MessageBundle
							.getMessage("angal.common.code.txt"),
							codeTextField, Show.FOCUS_LOST);
			{
				suggestion.setFont(new Font("Tahoma", Font.PLAIN, 12));
				suggestion.setForeground(Color.GRAY);
				suggestion.setHorizontalAlignment(JLabel.CENTER);
				suggestion.changeAlpha(0.5f);
				suggestion.changeStyle(Font.BOLD + Font.ITALIC);
			}
		}
		return codeTextField;
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
	
	private JLabel getLoaderLabel() {
		if (loaderLabel == null) {
			ImageIcon icon = new ImageIcon("rsc/icons/oh_loader.GIF");
			loaderLabel = new JLabel("");
			loaderLabel.setIcon(icon);
			loaderLabel.setVisible(false);
		}
		return loaderLabel;
	}
}
