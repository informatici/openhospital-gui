package org.isf.operation.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.MainMenu;
import org.isf.menu.manager.Context;
import org.isf.opd.gui.OpdEditExtended;
import org.isf.opd.model.Opd;
import org.isf.operation.manager.OperationBrowserManager;
import org.isf.operation.manager.OperationRowBrowserManager;
import org.isf.operation.model.Operation;
import org.isf.operation.model.OperationRow;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.OhDefaultCellRenderer;
import org.isf.utils.jobjects.OhTableOperationModel;
import org.isf.utils.jobjects.VoFloatTextField;
import org.joda.time.DateTime;

import com.toedter.calendar.JDateChooser;

public class OperationRowOpd extends JPanel implements OpdEditExtended.SurgeryListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel labelDate;
	private JTextField textFieldUnit;
	private JDateChooser textDate;
	private JComboBox comboOperation;
	private JComboBox comboResult;
	private JTextArea textAreaRemark;

	private OperationBrowserManager opeManager = Context.getApplicationContext().getBean(OperationBrowserManager.class);
	private OperationRowBrowserManager opeRowManager = Context.getApplicationContext().getBean(OperationRowBrowserManager.class);
	private OhTableOperationModel<OperationRow> modelOhOpeRow;
	private List<OperationRow> oprowData = new ArrayList<OperationRow>();
	private Opd myOpd;
	
	private ArrayList<String> operationResults = opeManager.getResultsList();

	OhDefaultCellRenderer cellRenderer = new OhDefaultCellRenderer();

	private JDateChooser jCalendarDate;
	private JTable tableData;

	public OperationRowOpd(Opd opd) {
		setLayout(new BorderLayout(0, 0));
		myOpd = opd;
		JPanel panelForm = new JPanel();
		panelForm.setBorder(new EmptyBorder(10, 10, 10, 10));
		panelForm.setSize(new Dimension(200, 200));

		add(panelForm, BorderLayout.NORTH);
		GridBagLayout gbl_panelForm = new GridBagLayout();
		gbl_panelForm.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelForm.rowHeights = new int[] { 0, 0, 30, 0, 0 };
		gbl_panelForm.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelForm.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		panelForm.setLayout(gbl_panelForm);

		JLabel labelOperation = new JLabel(MessageBundle.getMessage("angal.operationrowedit.operation")); //$NON-NLS-1$
		GridBagConstraints gbc_labelOperation = new GridBagConstraints();
		gbc_labelOperation.anchor = GridBagConstraints.EAST;
		gbc_labelOperation.insets = new Insets(0, 0, 5, 5);
		gbc_labelOperation.gridx = 0;
		gbc_labelOperation.gridy = 0;
		panelForm.add(labelOperation, gbc_labelOperation);

		// JComboBox comboOperation = new JComboBox();
		comboOperation = getOperationsBox();

		GridBagConstraints gbc_comboOperation = new GridBagConstraints();
		gbc_comboOperation.insets = new Insets(0, 0, 5, 5);
		gbc_comboOperation.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboOperation.gridx = 1;
		gbc_comboOperation.gridy = 0;
		panelForm.add(comboOperation, gbc_comboOperation);

		labelDate = new JLabel(MessageBundle.getMessage("angal.operationrowlist.date")); //$NON-NLS-1$
		GridBagConstraints gbc_labelDate = new GridBagConstraints();
		gbc_labelDate.anchor = GridBagConstraints.EAST;
		gbc_labelDate.insets = new Insets(0, 0, 5, 5);
		gbc_labelDate.gridx = 2;
		gbc_labelDate.gridy = 0;
		panelForm.add(labelDate, gbc_labelDate);

		// textDate = new JTextField();
		textDate = getJCalendarDate();
		GridBagConstraints gbc_textDate = new GridBagConstraints();
		gbc_textDate.insets = new Insets(0, 0, 5, 0);
		gbc_textDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_textDate.gridx = 3;
		gbc_textDate.gridy = 0;
		panelForm.add(textDate, gbc_textDate);
		// textDate.setColumns(10);

		JLabel labelResultat = new JLabel(MessageBundle.getMessage("angal.operationrowedit.result")); //$NON-NLS-1$
		GridBagConstraints gbc_labelResultat = new GridBagConstraints();
		gbc_labelResultat.anchor = GridBagConstraints.EAST;
		gbc_labelResultat.insets = new Insets(0, 0, 5, 5);
		gbc_labelResultat.gridx = 0;
		gbc_labelResultat.gridy = 1;
		panelForm.add(labelResultat, gbc_labelResultat);

		comboResult = new JComboBox();
		GridBagConstraints gbc_comboResult = new GridBagConstraints();
		gbc_comboResult.insets = new Insets(0, 0, 5, 5);
		gbc_comboResult.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboResult.gridx = 1;
		gbc_comboResult.gridy = 1;
		panelForm.add(comboResult, gbc_comboResult);
		comboResult.addItem(null);
		for (int i = 0; i < operationResults.size(); i++) {
			comboResult.addItem(operationResults.get(i));
		}

		JLabel lblUniteTrans = new JLabel(MessageBundle.getMessage("angal.operationrowedit.unitetrans")); //$NON-NLS-1$
		GridBagConstraints gbc_lblUniteTrans = new GridBagConstraints();
		gbc_lblUniteTrans.anchor = GridBagConstraints.EAST;
		gbc_lblUniteTrans.insets = new Insets(0, 0, 5, 5);
		gbc_lblUniteTrans.gridx = 2;
		gbc_lblUniteTrans.gridy = 1;
		panelForm.add(lblUniteTrans, gbc_lblUniteTrans);

		// textFieldUnit = new JTextField();
		textFieldUnit = new VoFloatTextField(0, 100);
		GridBagConstraints gbc_textFieldUnit = new GridBagConstraints();
		gbc_textFieldUnit.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldUnit.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldUnit.gridx = 3;
		gbc_textFieldUnit.gridy = 1;
		panelForm.add(textFieldUnit, gbc_textFieldUnit);
		textFieldUnit.setColumns(10);

		JLabel lblRemarques = new JLabel(MessageBundle.getMessage("angal.operationrowedit.remark")); //$NON-NLS-1$
		GridBagConstraints gbc_lblRemarques = new GridBagConstraints();
		gbc_lblRemarques.insets = new Insets(0, 0, 5, 5);
		gbc_lblRemarques.gridx = 0;
		gbc_lblRemarques.gridy = 2;
		panelForm.add(lblRemarques, gbc_lblRemarques);

		textAreaRemark = new JTextArea();
		textAreaRemark.setLineWrap(true);
		GridBagConstraints gbc_textAreaRemark = new GridBagConstraints();
		gbc_textAreaRemark.insets = new Insets(0, 0, 5, 0);
		gbc_textAreaRemark.gridwidth = 3;
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

		JButton btnSave = new JButton(MessageBundle.getMessage("angal.operationrowedit.save")); //$NON-NLS-1$
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addToGrid();
			}
		});

		JButton btnNew = new JButton(MessageBundle.getMessage("angal.operationrowedit.new")); //$NON-NLS-1$
		btnNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clearForm();
			}
		});
		panelActions.add(btnNew);
		panelActions.add(btnSave);

		JButton btnDelete = new JButton(MessageBundle.getMessage("angal.operationrowlist.delete")); //$NON-NLS-1$
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = tableData.getSelectedRow();
				deleteOpeRow(index);
			}
		});
		panelActions.add(btnDelete);

		JPanel panelGridData = new JPanel();
		panelListData.add(panelGridData, BorderLayout.CENTER);
		panelGridData.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPaneData = new JScrollPane();

		panelGridData.add(scrollPaneData);

		tableData = new JTable();
		/*** apply default oh cellRender *****/
		tableData.setDefaultRenderer(Object.class, cellRenderer);
		tableData.setDefaultRenderer(Double.class, cellRenderer);

		tableData.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
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
				// TODO Auto-generated method stub

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

		if (myOpd != null) {
			List<OperationRow> res = new ArrayList<OperationRow>();
			try {
				res = opeRowManager.getOperationRowByOpd(myOpd);
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
			oprowData.addAll(res);
		}
		modelOhOpeRow = new OhTableOperationModel<OperationRow>(oprowData);
		tableData.setModel(modelOhOpeRow);

	}

	private JDateChooser getJCalendarDate() {
		if (jCalendarDate == null) {
			jCalendarDate = new JDateChooser();
			jCalendarDate.setLocale(new Locale(GeneralData.LANGUAGE));
			jCalendarDate.setDateFormatString("dd/MM/yy"); //$NON-NLS-1$
			jCalendarDate.setDate(DateTime.now().toDate());
		}
		return jCalendarDate;
	}

	private JComboBox getOperationsBox() {
		JComboBox comboOpe = new JComboBox();
		ArrayList<Operation> opeList = new ArrayList<Operation>();
		try {
			opeList.addAll(opeManager.getOperationOpd());
		} catch (OHServiceException ex) {
			Logger.getLogger(OperationRowAdm.class.getName()).log(Level.SEVERE, null, ex);
		}
		comboOpe.addItem(null);
		for (org.isf.operation.model.Operation elem : opeList) {
			comboOpe.addItem(elem);
		}
		comboOpe.setEnabled(true);
		return comboOpe;
	}

	public void addToGrid() {
		if ((this.textDate.getDate() == null) || (this.comboOperation.getSelectedItem() == null)) {
			JOptionPane.showMessageDialog(OperationRowOpd.this,
					MessageBundle.getMessage("angal.operationrowedit.warningdateope"), //$NON-NLS-1$
					MessageBundle.getMessage("angal.hospital"), JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
			return;
		}
		

		OperationRow operationRow = new OperationRow();
		GregorianCalendar dateop = new GregorianCalendar();
		dateop.setTime(this.textDate.getDate());
		operationRow.setOpDate(dateop);
		if (this.comboResult.getSelectedItem() != null)
			operationRow.setOpResult(this.comboResult.getSelectedItem().toString());
		else
			operationRow.setOpResult(""); //$NON-NLS-1$
		try {
			operationRow.setTransUnit(Float.parseFloat(this.textFieldUnit.getText()));
		} catch (NumberFormatException e) {
			operationRow.setTransUnit(new Float(0));
		}
		Operation op = (Operation) this.comboOperation.getSelectedItem();
		operationRow.setOperation(op);
		if (myOpd != null)
			operationRow.setOpd(myOpd);
		operationRow.setPrescriber(MainMenu.getUser().getUserName());
		operationRow.setRemarks(textAreaRemark.getText());
		int index = tableData.getSelectedRow();
		if (index < 0) {
			oprowData.add(operationRow);
			modelOhOpeRow = new OhTableOperationModel<OperationRow>(oprowData);
			tableData.setModel(modelOhOpeRow);
		} else {
			OperationRow opeInter = oprowData.get(index);
			dateop.setTime(this.textDate.getDate());
			opeInter.setOpDate(dateop);
			opeInter.setOpResult(this.comboResult.getSelectedItem().toString());
			opeInter.setTransUnit(Float.parseFloat(this.textFieldUnit.getText()));
			op = (Operation) this.comboOperation.getSelectedItem();
			opeInter.setOperation(op);
			opeInter.setPrescriber(MainMenu.getUser().getUserName());
			opeInter.setRemarks(textAreaRemark.getText());
			oprowData.set(index, opeInter);
			modelOhOpeRow = new OhTableOperationModel<OperationRow>(oprowData);
			tableData.setModel(modelOhOpeRow);
		}
		clearForm();
	}

	public void addToForm() {
		OperationRow opeRow = (OperationRow) oprowData.get(tableData.getSelectedRow());
		/*** for combo operation *****/
		ArrayList<Operation> opeList = new ArrayList<Operation>();
		try {
			opeList.addAll(opeManager.getOperationOpd());
		} catch (OHServiceException ex) {
			//
		}
		if (opeRow != null) {
			boolean found = false;
			for (org.isf.operation.model.Operation elem : opeList) {
				if (opeRow.getOperation().getCode().equals(elem.getCode())) {
					found = true;
					comboOperation.setSelectedItem(elem);
					comboOperation.setEditable(false);
					comboOperation.setEnabled(false);
					break;
				}
			}
			if (!found) {
				comboOperation.addItem(null);
			}
		}

		if (opeRow != null) {
			textDate.setDate(opeRow.getOpDate().getTime());
			textAreaRemark.setText(opeRow.getRemarks());
			textFieldUnit.setText(opeRow.getTransUnit() + ""); //$NON-NLS-1$
		}

		/****** resultat *****/
		int index = -1;
		for (int i = 0; i < operationResults.size(); i++) {
			if (opeRow.getOpResult() != null && (operationResults.get(i) + "").equals(opeRow.getOpResult())) { //$NON-NLS-1$
				index = i;
			}
		}
		comboResult.setSelectedIndex(index + 1);
		/*************/

	}

	public void deleteOpeRow(int idRow) {
		// int idRow = this.tableData.getSelectedRow();
		OperationRow operationRow = null;
		if (idRow < 0) {
			JOptionPane.showMessageDialog(OperationRowOpd.this,
					MessageBundle.getMessage("angal.common.pleaseselectarow"), //$NON-NLS-1$
					MessageBundle.getMessage("angal.hospital"), JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
			return;
		} else {
			operationRow = oprowData.get(idRow);
			int yesOrNo = JOptionPane.showConfirmDialog(OperationRowOpd.this,
					MessageBundle.getMessage("angal.operationrowlist.confirmdelete"), null, JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
			if (yesOrNo == JOptionPane.YES_OPTION) {
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
						JOptionPane.showMessageDialog(OperationRowOpd.this,
								MessageBundle.getMessage("angal.operationrowlist.successdel"), //$NON-NLS-1$
								MessageBundle.getMessage("angal.hospital"), JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
						oprowData.remove(idRow);
						modelOhOpeRow = new OhTableOperationModel<OperationRow>(oprowData);
						tableData.setModel(modelOhOpeRow);
						tableData.repaint();
						clearForm();
					} else {
						JOptionPane.showMessageDialog(OperationRowOpd.this,
								MessageBundle.getMessage("angal.operationrowlist.errosdel"), //$NON-NLS-1$
								MessageBundle.getMessage("angal.hospital"), JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
						return;
					}
				} else {
					JOptionPane.showMessageDialog(OperationRowOpd.this,
							MessageBundle.getMessage("angal.operationrowlist.successdel"), //$NON-NLS-1$
							MessageBundle.getMessage("angal.hospital"), JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
					oprowData.remove(idOpe);
					modelOhOpeRow = new OhTableOperationModel<OperationRow>(oprowData);
					tableData.setModel(modelOhOpeRow);
					tableData.repaint();
					clearForm();
				}
			} else {
				return;
			}
		}
	}

	/**
	 * TODO: Remove Create or Update operations from this method
	 * it should only update the view at this point
	 */
	@Override
	public void surgeryUpdated(AWTEvent e, Opd opd) {
		try {
			saveAllOpeRow(oprowData, opeRowManager, e);
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}
	}

	/**
	 * TODO: Remove Create or Update operations from this method
	 * it should only update the view at this point
	 */
	@Override
	public void surgeryInserted(AWTEvent e, Opd opd) {
		try {
			saveAllOpeRow(oprowData, opeRowManager, e);
		} catch (OHServiceException e1) {
			OHServiceExceptionUtil.showMessages(e1);
		}
	}

	public void saveAllOpeRow(List<OperationRow> listOpe, OperationRowBrowserManager RowManager, AWTEvent e) throws OHServiceException {
		for (org.isf.operation.model.OperationRow opRow : listOpe) {
			if ((opRow.getId() > 0) && (opRow.getOpd().getCode() > 0)) {
				RowManager.updateOperationRow(opRow);

			}
			if ((opRow.getId() <= 0) && (opRow.getOpd().getCode() > 0)) {
				RowManager.newOperationRow(opRow);

			}
			if ((opRow.getId() <= 0) && (opRow.getOpd().getCode() <= 0)) {
				Opd op = (Opd) e.getSource();
				opRow.setOpd(op);
				RowManager.newOperationRow(opRow);
			}
		}
	}

	public void clearForm() {
		comboOperation.setSelectedItem(null);
		textDate.setDate(null);
		textAreaRemark.setText(""); //$NON-NLS-1$
		comboResult.setSelectedIndex(-1);
		textFieldUnit.setText(""); //$NON-NLS-1$
		tableData.clearSelection();
		comboOperation.setEditable(true);
		comboOperation.setEnabled(true);
	}

	public List<OperationRow> getOprowData() {
		return oprowData;
	}

	public void setOprowData(List<OperationRow> oprowData) {
		this.oprowData = oprowData;
	}




}