/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2025 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.medicalstock.gui;

import static org.isf.utils.Constants.DATE_FORMAT_DD_MM_YYYY_HH_MM;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicalstock.manager.MovStockDraftManager;
import org.isf.medicalstock.model.MovementDraft;
import org.isf.medicalstock.model.MovementDraftKind;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.time.TimeTools;

/**
 * Small modal dialog offering the {@link MovementDraft}s of one {@link MovementDraftKind}:
 * the user can resume one of them, delete them, or start a new movement. It is opened by
 * {@link MovStockMultipleCharging} and {@link MovStockMultipleDischarging} when at least
 * one draft of the matching kind exists.
 */
public class MovStockDraftChooser extends JDialog {

	private static final long serialVersionUID = 1L;

	private final MovementDraftKind kind;
	private final String[] columnNames;
	private List<MovementDraft> drafts;
	private List<Integer> rowCounts;
	private MovementDraft selectedDraft;
	private JTable jTableDrafts;
	private DraftTableModel model;

	private MovStockDraftManager movStockDraftManager = Context.getApplicationContext().getBean(MovStockDraftManager.class);

	public MovStockDraftChooser(JDialog owner, MovementDraftKind kind, List<MovementDraft> drafts) {
		super(owner, true);
		this.kind = kind;
		this.drafts = new ArrayList<>(drafts);
		this.columnNames = new String[] {
				MessageBundle.getMessage("angal.medicalstock.draft.lastmodified.col").toUpperCase(),
				MessageBundle.getMessage("angal.medicalstock.draft.createdby.col").toUpperCase(),
				MessageBundle.getMessage("angal.medicalstock.multiplecharging.referencenumberabb").toUpperCase(),
				MessageBundle.getMessage("angal.common.type.txt").toUpperCase(),
				kind == MovementDraftKind.charge
					? MessageBundle.getMessage("angal.medicalstock.multiplecharging.supplier").toUpperCase()
					: MessageBundle.getMessage("angal.medicalstock.multipledischarging.destination").toUpperCase(),
				MessageBundle.getMessage("angal.medicalstock.draft.rows.col").toUpperCase()
		};
		loadRowCounts();
		initComponents();
	}

	/**
	 * @return the draft chosen for resuming, or {@code null} to start a new movement.
	 */
	public MovementDraft getSelectedDraft() {
		return selectedDraft;
	}

	private void initComponents() {
		setTitle(MessageBundle.getMessage("angal.medicalstock.draft.title"));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JLabel jLabelMessage = new JLabel(MessageBundle.getMessage("angal.medicalstock.draft.resumeexistingdraftorstartnew.msg"));
		jLabelMessage.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(jLabelMessage, BorderLayout.NORTH);

		model = new DraftTableModel();
		jTableDrafts = new JTable(model);
		jTableDrafts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if (model.getRowCount() > 0) {
			jTableDrafts.setRowSelectionInterval(0, 0);
		}
		JScrollPane scrollPane = new JScrollPane(jTableDrafts);
		scrollPane.setPreferredSize(new Dimension(600, 200));
		add(scrollPane, BorderLayout.CENTER);

		add(getJButtonPanel(), BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(getOwner());
	}

	private JPanel getJButtonPanel() {
		JPanel buttonPanel = new JPanel();

		JButton resumeButton = new JButton(MessageBundle.getMessage("angal.medicalstock.draft.resumedraft.btn"));
		resumeButton.setMnemonic(MessageBundle.getMnemonic("angal.medicalstock.draft.resumedraft.btn.key"));
		resumeButton.addActionListener(actionEvent -> {
			int row = jTableDrafts.getSelectedRow();
			if (row < 0) {
				MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
				return;
			}
			selectedDraft = drafts.get(row);
			dispose();
		});
		buttonPanel.add(resumeButton);

		JButton deleteButton = new JButton(MessageBundle.getMessage("angal.medicalstock.draft.deletedraft.btn"));
		deleteButton.setMnemonic(MessageBundle.getMnemonic("angal.medicalstock.draft.deletedraft.btn.key"));
		deleteButton.addActionListener(actionEvent -> deleteSelectedDraft());
		buttonPanel.add(deleteButton);

		JButton startNewButton = new JButton(MessageBundle.getMessage("angal.medicalstock.draft.startnew.btn"));
		startNewButton.setMnemonic(MessageBundle.getMnemonic("angal.medicalstock.draft.startnew.btn.key"));
		startNewButton.addActionListener(actionEvent -> dispose());
		buttonPanel.add(startNewButton);

		return buttonPanel;
	}

	private void deleteSelectedDraft() {
		int row = jTableDrafts.getSelectedRow();
		if (row < 0) {
			MessageDialog.error(this, "angal.common.pleaseselectarow.msg");
			return;
		}
		int answer = MessageDialog.yesNo(this, "angal.medicalstock.draft.confirmdeletedraft.msg");
		if (answer != JOptionPane.YES_OPTION) {
			return;
		}
		try {
			movStockDraftManager.deleteMovementDraft(drafts.get(row));
			drafts = movStockDraftManager.getMovementDrafts(kind);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
			return;
		}
		loadRowCounts();
		model.fireTableDataChanged();
		if (drafts.isEmpty()) {
			dispose();
		} else {
			jTableDrafts.setRowSelectionInterval(0, 0);
		}
	}

	private void loadRowCounts() {
		rowCounts = new ArrayList<>();
		for (MovementDraft draft : drafts) {
			int count = 0;
			try {
				count = movStockDraftManager.countMovementDraftRows(draft.getId());
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}
			rowCounts.add(count);
		}
	}

	class DraftTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return drafts.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override
		public Object getValueAt(int r, int c) {
			MovementDraft draft = drafts.get(r);
			if (c == 0) {
				return draft.getLastModifiedDate() == null ? "" : TimeTools.formatDateTime(draft.getLastModifiedDate(), DATE_FORMAT_DD_MM_YYYY_HH_MM);
			} else if (c == 1) {
				return draft.getCreatedBy() == null ? "" : draft.getCreatedBy();
			} else if (c == 2) {
				return draft.getRefNo() == null ? "" : draft.getRefNo();
			} else if (c == 3) {
				return draft.getType() == null ? "" : draft.getType().getDescription();
			} else if (c == 4) {
				if (kind == MovementDraftKind.charge) {
					return draft.getSupplier() == null ? "" : draft.getSupplier().getSupName();
				}
				return draft.getWard() == null ? "" : draft.getWard().getDescription();
			} else if (c == 5) {
				return rowCounts.get(r);
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
	}
}
