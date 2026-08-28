/*
 * Open Hospital (www.open-hospital.org)
 * Copyright (C) 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.utils.table;

import java.util.Locale;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Utility methods for browser tables using {@link TableRowSorter}.
 */
public final class OHTableSortFilter {

	private OHTableSortFilter() {
	}

	public static <T extends TableModel> TableRowSorter<T> installSorter(JTable table, T model) {
		TableRowSorter<T> sorter = new TableRowSorter<>(model);
		table.setModel(model);
		table.setRowSorter(sorter);
		return sorter;
	}

	public static <T extends TableModel> void applyNaturalTextFilter(JTable table, TableRowSorter<T> sorter, String text) {
		if (sorter == null) {
			return;
		}

		String filterText = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
		if (filterText.isEmpty()) {
			sorter.setRowFilter(null);
			return;
		}

		String[] chunks = filterText.split("\\s+");
		sorter.setRowFilter(new RowFilter<T, Integer>() {

			@Override
			public boolean include(Entry<? extends T, ? extends Integer> entry) {
				for (String chunk : chunks) {
					boolean found = false;
					for (int viewColumn = 0; viewColumn < table.getColumnCount(); viewColumn++) {
						if (!isColumnVisible(table, viewColumn)) {
							continue;
						}
						String value = entry.getStringValue(table.convertColumnIndexToModel(viewColumn));
						if (value != null && value.toLowerCase(Locale.ROOT).contains(chunk)) {
							found = true;
							break;
						}
					}
					if (!found) {
						return false;
					}
				}
				return true;
			}
		});
	}

	public static int getSelectedModelRow(JTable table) {
		int selectedRow = table.getSelectedRow();
		if (selectedRow < 0) {
			return -1;
		}
		return table.convertRowIndexToModel(selectedRow);
	}

	public static int[] getSelectedModelRows(JTable table) {
		int[] selectedRows = table.getSelectedRows();
		for (int i = 0; i < selectedRows.length; i++) {
			selectedRows[i] = table.convertRowIndexToModel(selectedRows[i]);
		}
		return selectedRows;
	}

	private static boolean isColumnVisible(JTable table, int viewColumn) {
		TableColumn column = table.getColumnModel().getColumn(viewColumn);
		return column.getWidth() > 0 || column.getMinWidth() > 0 || column.getMaxWidth() > 0 || column.getPreferredWidth() > 0;
	}
}
