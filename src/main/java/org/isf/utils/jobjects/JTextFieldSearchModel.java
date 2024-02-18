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
package org.isf.utils.jobjects;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.isf.generaldata.MessageBundle;
import org.isf.medicals.manager.MedicalBrowsingManager;
import org.isf.medicals.model.Medical;
import org.isf.menu.manager.Context;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.TextPrompt.Show;

/**
 * @author Nanni
 */
public class JTextFieldSearchModel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int CODE_COLUMN_WIDTH = 100;

	private JTextField jTextFieldSearch;
	private Object selectedObject;
	private HashMap<String, Medical> medicalMap;
	private JDialog owner;

	private MedicalBrowsingManager medicalBrowsingManager = Context.getApplicationContext().getBean(MedicalBrowsingManager.class);

	/**
	 * Creates a Dialog containing a JTextField
	 * with search capabilities over a certain model class
	 *
	 * @param owner - the JFrame owner for Dialog modality
	 * @param model - the class to search
	 */
	public JTextFieldSearchModel(JDialog owner, Object model) {
		super();
		this.owner = owner;
		if (model == Medical.class || model instanceof Medical) {
			initializeMedical();
			if (model == Medical.class) {
				add(getJTextFieldSearch(null), BorderLayout.CENTER);
			}
			if (model instanceof Medical) {
				add(getJTextFieldSearch((Medical) model), BorderLayout.CENTER);
			}
		}
	}

	private void initializeMedical() {
		List<Medical> medicals = null;
		medicalMap = new HashMap<>();
		try {
			medicals = medicalBrowsingManager.getMedicalsSortedByCode();
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		if (medicals != null) {
			for (Medical med : medicals) {
				String key = med.getProdCode();
				if (key == null || key.equals("")) {
					key = med.getType().getCode() + med.getDescription();
				}
				medicalMap.put(key, med);
			}
		}
	}

	protected Medical chooseMedical(String text) {
		List<Medical> medList = new ArrayList<>();
		for (Medical aMed : medicalMap.values()) {
			if (aMed.getProdCode().toLowerCase().contains(text)
					|| aMed.getDescription().toLowerCase().contains(text)) {
				medList.add(aMed);
			}
		}
		Collections.sort(medList);
		Medical med = null;

		if (!medList.isEmpty()) {
			JTable medTable = new JTable(new StockMedModel(medList));
			medTable.getColumnModel().getColumn(0).setMaxWidth(CODE_COLUMN_WIDTH);
			medTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JPanel panel = new JPanel();
			panel.add(new JScrollPane(medTable));

			int ok = JOptionPane.showConfirmDialog(owner,
					panel,
					MessageBundle.getMessage("angal.medicalstock.multiplecharging.chooseamedical"),
					JOptionPane.YES_NO_OPTION);

			if (ok == JOptionPane.OK_OPTION) {
				int row = medTable.getSelectedRow();
				med = medList.get(row);
			}
			return med;
		}
		return null;
	}

	private JTextField getJTextFieldSearch(Medical medical) {
		if (jTextFieldSearch == null) {
			jTextFieldSearch = new JTextField(50);
			jTextFieldSearch.setPreferredSize(new Dimension(300, 30));
			jTextFieldSearch.setHorizontalAlignment(SwingConstants.LEFT);

			TextPrompt suggestion = new TextPrompt(MessageBundle.getMessage("angal.medicalstock.typeacodeoradescriptionandpressenter"),
					jTextFieldSearch,
					Show.FOCUS_GAINED);
			suggestion.setFont(new Font("Tahoma", Font.PLAIN, 14));
			suggestion.setForeground(Color.GRAY);
			suggestion.setHorizontalAlignment(SwingConstants.CENTER);
			suggestion.changeAlpha(0.5f);
			suggestion.changeStyle(Font.BOLD + Font.ITALIC);
			if (medical != null) {
				selectedObject = medical;
				jTextFieldSearch.setText(medical.toString());
			}
			jTextFieldSearch.addActionListener(actionEvent -> {
				String text = jTextFieldSearch.getText();
				Medical med;
				if (medicalMap.containsKey(text)) {
					// Medical found
					med = medicalMap.get(text);
				} else {
					med = chooseMedical(text.toLowerCase());
				}
				if (med != null) {
					selectedObject = med;
					jTextFieldSearch.setText(med.toString());
				}
			});
		}
		return jTextFieldSearch;
	}

	class StockMedModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;
		private List<Medical> medList;

		public StockMedModel(List<Medical> meds) {
			medList = meds;
		}

		@Override
		public int getRowCount() {
			if (medList == null) {
				return 0;
			}
			return medList.size();
		}

		@Override
		public String getColumnName(int c) {
			if (c == 0) {
				return MessageBundle.getMessage("angal.common.code.txt").toUpperCase();
			}
			if (c == 1) {
				return MessageBundle.getMessage("angal.common.description.txt").toUpperCase();
			}
			return "";
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int r, int c) {
			Medical med = medList.get(r);
			if (c == -1) {
				return med;
			} else if (c == 0) {
				return med.getProdCode();
			} else if (c == 1) {
				return med.getDescription();
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			return false;
		}
	}

	/**
	 * @return the selectedObject
	 */
	public Object getSelectedObject() {
		return selectedObject;
	}

}
