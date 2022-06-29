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
package org.isf.operation.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.manager.OperationRowBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.GoodDateTimeChooser;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.jobjects.OhTableOperationModel;
import org.isf.utils.jobjects.VoFloatTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class OperationRowBase extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationRowBase.class);

	protected JLabel labelDate;
	protected JTextField textFieldUnit;
	protected GoodDateTimeChooser textDate;
	protected JComboBox<Operation> comboOperation;
	protected JTextField searchOperationTextField;
	protected JButton searchOperationButton;
	protected JComboBox<String> comboResult;
	protected JTextArea textAreaRemark;
	
	protected List<Operation> operationsOPD;
	protected List<Operation> operationsAll;

	protected OperationBrowserManager opeManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
	protected OperationRowBrowserManager opeRowManager = Context.getApplicationContext().getBean(OperationRowBrowserManager.class);
	protected OhTableOperationModel<org.isf.operation.model.OperationRow> modelOhOpeRow;
	protected List<org.isf.operation.model.OperationRow> oprowData = new ArrayList<>();

	protected List<String> operationResults = opeManager.getResultDescriptionList();
	protected OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();
	protected JTable tableData;


	protected OperationRowBase() {
		setLayout(new BorderLayout(0, 0));
		JPanel panelForm = new JPanel();
		panelForm.setBorder(new EmptyBorder(10, 10, 10, 10));
		panelForm.setSize(new Dimension(200, 200));

		add(panelForm, BorderLayout.NORTH);
		GridBagLayout gbl_panelForm = new GridBagLayout();
		gbl_panelForm.columnWidths = new int[] { 60, 75, 0, 300, 0, 0 };
		gbl_panelForm.rowHeights = new int[] { 0, 0, 30, 0, 0 };
		gbl_panelForm.columnWeights = new double[] { 0.0, 1.0, 0.25, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panelForm.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		panelForm.setLayout(gbl_panelForm);

		JLabel labelOperation = new JLabel(MessageBundle.getMessage("angal.operationrowedit.operation")); //$NON-NLS-1$
		GridBagConstraints gbc_labelOperation = new GridBagConstraints();
		gbc_labelOperation.anchor = GridBagConstraints.WEST;
		gbc_labelOperation.insets = new Insets(0, 0, 5, 5);
		gbc_labelOperation.gridx = 0;
		gbc_labelOperation.gridy = 0;
		panelForm.add(labelOperation, gbc_labelOperation);

		searchOperationTextField = new JTextField();
		GridBagConstraints gbc_searchOperation = new GridBagConstraints();
		gbc_searchOperation.insets = new Insets(0, 0, 5, 5);
		gbc_searchOperation.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelOperation.gridx = 1;
		gbc_labelOperation.gridy = 0;
		searchOperationTextField.setColumns(20);
		searchOperationTextField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER) {
					searchOperationButton.doClick();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
		panelForm.add(searchOperationTextField, gbc_searchOperation);
		
		searchOperationButton = new JButton("");
		GridBagConstraints gbc_searchOperationButton = new GridBagConstraints();
		gbc_searchOperationButton.insets = new Insets(0, 5, 5, 5);
		gbc_searchOperationButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelOperation.gridx = 2;
		gbc_labelOperation.gridy = 0;
		searchOperationButton.setPreferredSize(new Dimension(20, 20));
		searchOperationButton.setIcon(new ImageIcon("rsc/icons/zoom_r_button.png"));
		searchOperationButton.addActionListener(new ActionListener() {

			List<Operation> operationsOPD = null;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					operationsOPD = opeManager.getOperation();
				} catch (OHServiceException ex) {
					OHServiceExceptionUtil.showMessages(ex);
				}
				comboOperation.removeAllItems();
				comboOperation.addItem(null);
				for (Operation ope : getSearchOperationsResults(searchOperationTextField.getText(),
								operationsOPD == null ? operationsAll : operationsOPD)) {
					comboOperation.addItem(ope);
				}

				if (comboOperation.getItemCount() >= 2) {
					comboOperation.setSelectedIndex(1);
				}
				comboOperation.requestFocus();
				if (comboOperation.getItemCount() > 2) {
					comboOperation.showPopup();
				}
			}
		});
		panelForm.add(searchOperationButton, gbc_searchOperationButton);

		comboOperation = getOperationsBox();
		GridBagConstraints gbcOperationBox = new GridBagConstraints();
		gbcOperationBox.insets = new Insets(0, 5, 5, 5);
		gbcOperationBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelOperation.gridx = 3;
		gbc_labelOperation.gridy = 0;
		panelForm.add(comboOperation, gbcOperationBox);
		
		labelDate = new JLabel(MessageBundle.getMessage("angal.operationrowlist.date")); //$NON-NLS-1$
		GridBagConstraints gbc_labelDate = new GridBagConstraints();
		gbc_labelDate.anchor = GridBagConstraints.WEST;
		gbc_labelDate.insets = new Insets(0, 0, 5, 5);
		gbc_labelDate.gridx = 4;
		gbc_labelDate.gridy = 0;
		panelForm.add(labelDate, gbc_labelDate);

		textDate = getDateTimeChooser();
		GridBagConstraints gbc_textDate = new GridBagConstraints();
		gbc_textDate.insets = new Insets(0, 0, 5, 0);
		gbc_textDate.anchor = GridBagConstraints.WEST;
		gbc_textDate.fill = GridBagConstraints.NONE;
		gbc_textDate.gridx = 5;
		gbc_textDate.gridy = 0;
		panelForm.add(textDate, gbc_textDate);

		JLabel labelResultat = new JLabel(MessageBundle.getMessage("angal.common.result.txt"));
		GridBagConstraints gbc_labelResultat = new GridBagConstraints();
		gbc_labelResultat.anchor = GridBagConstraints.WEST;
		gbc_labelResultat.insets = new Insets(0, 0, 5, 5);
		gbc_labelResultat.gridx = 0;
		gbc_labelResultat.gridy = 1;
		panelForm.add(labelResultat, gbc_labelResultat);

		comboResult = getComboResultBox();
		GridBagConstraints gbc_comboResult = new GridBagConstraints();
		gbc_comboResult.insets = new Insets(0, 0, 5, 5);
		gbc_comboResult.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboResult.gridwidth = 3;
		gbc_comboResult.gridx = 1;
		gbc_comboResult.gridy = 1;
		panelForm.add(comboResult, gbc_comboResult);

		JLabel lblUniteTrans = new JLabel(MessageBundle.getMessage("angal.operationrowedit.unitetrans")); //$NON-NLS-1$
		GridBagConstraints gbc_lblUniteTrans = new GridBagConstraints();
		gbc_lblUniteTrans.anchor = GridBagConstraints.WEST;
		gbc_lblUniteTrans.insets = new Insets(0, 0, 5, 5);
		gbc_lblUniteTrans.gridx = 4;
		gbc_lblUniteTrans.gridy = 1;
		panelForm.add(lblUniteTrans, gbc_lblUniteTrans);

		textFieldUnit = new VoFloatTextField(0, 100);
		GridBagConstraints gbc_textFieldUnit = new GridBagConstraints();
		gbc_textFieldUnit.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldUnit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldUnit.gridx = 5;
		gbc_textFieldUnit.gridy = 1;
		panelForm.add(textFieldUnit, gbc_textFieldUnit);
		textFieldUnit.setColumns(10);

		JLabel lblRemarques = new JLabel(MessageBundle.getMessage("angal.operationrowedit.remark")); //$NON-NLS-1$
		GridBagConstraints gbc_lblRemarques = new GridBagConstraints();
		gbc_lblRemarques.anchor = GridBagConstraints.WEST;
		gbc_lblRemarques.insets = new Insets(0, 0, 5, 5);
		gbc_lblRemarques.gridx = 0;
		gbc_lblRemarques.gridy = 2;
		panelForm.add(lblRemarques, gbc_lblRemarques);

		textAreaRemark = new JTextArea();
		textAreaRemark.setLineWrap(true);
		GridBagConstraints gbc_textAreaRemark = new GridBagConstraints();
		gbc_textAreaRemark.insets = new Insets(0, 0, 5, 0);
		gbc_textAreaRemark.gridwidth = 5;
		gbc_textAreaRemark.fill = GridBagConstraints.BOTH;
		gbc_textAreaRemark.gridx = 1;
		gbc_textAreaRemark.gridy = 2;
		panelForm.add(textAreaRemark, gbc_textAreaRemark);

		JPanel panelListData = new JPanel();
		add(panelListData, BorderLayout.CENTER);
		panelListData.setLayout(new BorderLayout(0, 0));

		JPanel panelActions = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panelActions.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panelListData.add(panelActions, BorderLayout.NORTH);

		JButton btnAdd = new JButton(MessageBundle.getMessage("angal.operationrowlist.add.btn"));
		btnAdd.setMnemonic(MessageBundle.getMnemonic("angal.operationrowlist.add.btn.key"));
		btnAdd.addActionListener(actionEvent -> addToGrid());
		panelActions.add(btnAdd);

		JButton btnClear = new JButton(MessageBundle.getMessage("angal.operationrow.clear.btn"));
		btnClear.setMnemonic(MessageBundle.getMnemonic("angal.operationrow.clear.btn.key"));
		btnClear.addActionListener(actionEvent -> clearForm());
		panelActions.add(btnClear);

		JButton btnDelete = new JButton(MessageBundle.getMessage("angal.common.delete.btn"));
		btnDelete.setMnemonic(MessageBundle.getMnemonic("angal.common.delete.btn.key"));
		btnDelete.addActionListener(actionEvent -> {
			int index = tableData.getSelectedRow();
			deleteOpeRow(this, index);
		});
		panelActions.add(btnDelete);

		JPanel panelGridData = new JPanel();
		panelListData.add(panelGridData, BorderLayout.CENTER);
		panelGridData.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPaneData = new JScrollPane();

		panelGridData.add(scrollPaneData);

		tableData = new JTable();
		/* ** apply default oh cellRender **** */
		tableData.setDefaultRenderer(Object.class, cellRenderer);
		tableData.setDefaultRenderer(Double.class, cellRenderer);

		tableData.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				JTable aTable = (JTable) e.getSource();
				int itsRow = aTable.rowAtPoint(e.getPoint());
				if (itsRow >= 0) {
					cellRenderer.setHoveredRow(itsRow);
				} else {
					cellRenderer.setHoveredRow(-1);
				}
				aTable.repaint();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});
		tableData.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				cellRenderer.setHoveredRow(-1);
			}
		});

		tableData.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				addToForm();
			}
		});
		scrollPaneData.setViewportView(tableData);
	}

	abstract void addToGrid();

	protected GoodDateTimeChooser getDateTimeChooser() {
		if (textDate == null) {
			textDate = new GoodDateTimeChooser(LocalDateTime.now());
		}
		return textDate;
	}
	
	private List<Operation> getSearchOperationsResults(String s, List<Operation> opeList) {
		String query = s.trim();
		List<Operation> results = new ArrayList<>();
		for (Operation ope : opeList) {
			if (!query.equals("")) {
				String[] patterns = query.split(" ");
				String name = ope.getDescription().toLowerCase();
				boolean patternFound = false;
				for (String pattern : patterns) {
					if (name.contains(pattern.toLowerCase())) {
						patternFound = true;
						//It is sufficient that only one pattern matches the query
						break;
					}
				}
				if (patternFound) {
					results.add(ope);
				}
			} else {
				results.add(ope);
			}
		}
		return results;
	}
	

	protected JComboBox<Operation> getOperationsBox() {
		JComboBox<Operation> comboOpe = new JComboBox<>();
		ArrayList<Operation> opeList = new ArrayList<>();
		try {
			opeList.addAll(getOperationCollection());
		} catch (OHServiceException ex) {
			LOGGER.error(ex.getMessage(), ex);
		}
		comboOpe.addItem(null);
		for (Operation elem : opeList) {
			comboOpe.addItem(elem);
		}
		comboOpe.setEnabled(true);
		return comboOpe;
	}

	protected JComboBox<String> getComboResultBox() {
		JComboBox<String> comboResult = new JComboBox<>();
		for (String description : operationResults) {
			comboResult.addItem(description);
		}
		return comboResult;
	}

	// Either return opeManager.getOperationOpd() or opeManager.getOperationAdm()
	abstract List<Operation> getOperationCollection() throws OHServiceException;

	public void addToForm() {
		org.isf.operation.model.OperationRow opeRow = oprowData.get(tableData.getSelectedRow());
		/* ** for combo operation **** */
		ArrayList<Operation> opeList = new ArrayList<>();
		try {
			opeList.addAll(getOperationCollection());
		} catch (OHServiceException ex) {
			//
		}
		if (opeRow != null) {
			boolean found = false;
			for (Operation elem : opeList) {
				if (opeRow.getOperation().getCode().equals(elem.getCode())) {
					found = true;
					comboOperation.setSelectedItem(elem);
					comboOperation.setEnabled(false);
					break;
				}
			}
			if (!found) {
				comboOperation.addItem(null);
			}
		}

		if (opeRow != null) {
			textDate.setDateTime(opeRow.getOpDate());

			textAreaRemark.setText(opeRow.getRemarks());
			textFieldUnit.setText(opeRow.getTransUnit() + ""); //$NON-NLS-1$
		}

		/* ***** resultat **** */
		int index = 0;
		for (int i = 0; i < operationResults.size(); i++) {
			if (opeRow.getOpResult() != null && (opeManager.getResultDescriptionKey(operationResults.get(i) + "")).equals(opeRow.getOpResult())) { //$NON-NLS-1$
				index = i;
			}
		}
		comboResult.setSelectedIndex(index);
		/* *********** */
	}

	public void deleteOpeRow(Component parentComponent, int idRow) {
		org.isf.operation.model.OperationRow operationRow;
		if (idRow < 0) {
			MessageDialog.error(parentComponent, "angal.common.pleaseselectarow.msg");
		} else {
			operationRow = oprowData.get(idRow);
			int answer = MessageDialog.yesNo(parentComponent, "angal.operationrowlist.delete.operation.msg");
			if (answer == JOptionPane.YES_OPTION) {
				int idOpe = operationRow.getId();
				if (idOpe > 0) {
					boolean result;
					try {
						result = opeRowManager.deleteOperationRow(operationRow);
					} catch (OHServiceException e) {
						OHServiceExceptionUtil.showMessages(e);
						return;
					}
					if (result) {
						MessageDialog.info(parentComponent, "angal.operationrowlist.successdel");
						oprowData.remove(idRow);
						modelOhOpeRow = new OhTableOperationModel<>(oprowData);
						tableData.setModel(modelOhOpeRow);
						tableData.repaint();
						clearForm();
					} else {
						MessageDialog.error(parentComponent, "angal.operationrowlist.errosdel");
					}
				} else {
					MessageDialog.info(parentComponent, "angal.operationrowlist.successdel");
					oprowData.remove(idOpe);
					modelOhOpeRow = new OhTableOperationModel<>(oprowData);
					tableData.setModel(modelOhOpeRow);
					tableData.repaint();
					clearForm();
				}
			}
		}
	}

	public void clearForm() {
		comboOperation.setSelectedItem(null);
		textDate.setDateTime(null);
		textAreaRemark.setText(""); //$NON-NLS-1$
		comboResult.setSelectedIndex(-1);
		textFieldUnit.setText(""); //$NON-NLS-1$
		tableData.clearSelection();
		comboOperation.setEnabled(true);
	}

	public List<org.isf.operation.model.OperationRow> getOprowData() {
		return oprowData;
	}

	public void setOprowData(List<org.isf.operation.model.OperationRow> oprowData) {
		this.oprowData = oprowData;
	}

}
