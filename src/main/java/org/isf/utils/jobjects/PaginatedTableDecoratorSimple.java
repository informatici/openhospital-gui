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
package org.isf.utils.jobjects;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

public class PaginatedTableDecoratorSimple<T> {

	private JTable table;
	private PaginationDataProvider<T> dataProvider;
	private int[] pageSizes;
	private JPanel contentPanel;
	private int currentPageSize;
	private int currentPage = 1;
	private int lastPage = 1;
	private JPanel pageLinkPanel;
	private PageableTableModel objectTableModel;
	private JButton jFirstPageButton;
	private JButton jPreviousPageButton;
	private JButton jNextPageButton;
	private JButton jLastPageButton;
	private JLabel currentPageLabel;

	private PaginatedTableDecoratorSimple(JTable table, PaginationDataProvider<T> dataProvider,
			int[] pageSizes, int defaultPageSize) {
		this.table = table;
		this.dataProvider = dataProvider;
		this.pageSizes = pageSizes;
		this.currentPageSize = defaultPageSize;
	}

	public static <T> PaginatedTableDecoratorSimple<T> decorate(JTable table,
			PaginationDataProvider<T> dataProvider,
			int[] pageSizes, int defaultPageSize) {
		PaginatedTableDecoratorSimple<T> decorator = new PaginatedTableDecoratorSimple<>(table, dataProvider,
				pageSizes, defaultPageSize);
		decorator.init();
		return decorator;
	}

	public JPanel getContentPanel() {
		return contentPanel;
	}

	private void init() {
		initDataModel();
		initPaginationComponents();
		initListeners();
		paginate();
	}

	private void initListeners() {
		objectTableModel.addTableModelListener(this::refreshPageButtonPanel);
		if (table.getRowSorter() != null) {
			table.getRowSorter().addRowSorterListener(e -> {
				if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
					currentPage = 1;
					paginate();
				}
			});
		}
	}

	private void initDataModel() {
		TableModel model = table.getModel();
		if (!(model instanceof PageableTableModel)) {
			throw new IllegalArgumentException("TableModel must be a subclass of ObjectTableModel");
		}
		objectTableModel = (PageableTableModel) model;
	}

	private void initPaginationComponents() {
		contentPanel = new JPanel(new BorderLayout());
		JPanel paginationPanel = createPaginationPanel();
		contentPanel.add(paginationPanel, BorderLayout.NORTH);
		contentPanel.add(new JScrollPane(table));
	}

	private JPanel createPaginationPanel() {
		JPanel paginationPanel = new JPanel();
		pageLinkPanel = new JPanel(new GridLayout(1, 5, 5, 5));
		pageLinkPanel.add(getJFirstPageButton());
		pageLinkPanel.add(getJPreviousPageButton());
		pageLinkPanel.add(getJLabelCurrentPage());
		pageLinkPanel.add(getJNextPageButton());
		pageLinkPanel.add(getJLastPageButton());
		paginationPanel.add(pageLinkPanel);

		if (pageSizes != null) {
			JComboBox<Integer> pageComboBox = new JComboBox<>(
					Arrays.stream(pageSizes).boxed()
							.toArray(Integer[]::new));
			pageComboBox.addActionListener((ActionEvent e) -> {
				//to preserve current rows position
				int currentPageStartRow = ((currentPage - 1) * currentPageSize) + 1;
				currentPageSize = (Integer) pageComboBox.getSelectedItem();
				currentPage = ((currentPageStartRow - 1) / currentPageSize) + 1;
				paginate();
			});
			paginationPanel.add(Box.createHorizontalStrut(15));
			paginationPanel.add(new JLabel("Page Size: "));
			paginationPanel.add(pageComboBox);
			pageComboBox.setSelectedItem(currentPageSize);
		}
		return paginationPanel;
	}

	private void refreshPageButtonPanel(TableModelEvent tme) {
		getJLabelCurrentPage();
		togglePreviousPageButton();
		toggleNextPageButton();
		pageLinkPanel.getParent().validate();
		pageLinkPanel.getParent().repaint();
	}

	public void paginate() {
		List<T> rows = dataProvider.getRows(currentPage - 1, currentPageSize);
		lastPage = (dataProvider.getTotalRowCount() / currentPageSize) + 1;
		objectTableModel.setObjectRows(rows);
		objectTableModel.fireTableDataChanged();
	}

	private JLabel getJLabelCurrentPage() {
		if (currentPageLabel == null) {
			currentPageLabel = new JLabel();
		}
		currentPageLabel.setText("Page " + currentPage + " / " + lastPage);
		return currentPageLabel;
	}

	private JButton getJFirstPageButton() {
		if (jFirstPageButton == null) {
			jFirstPageButton = new JButton();
			jFirstPageButton.setText("<<");
			jFirstPageButton.setMnemonic(KeyEvent.VK_HOME);
			jFirstPageButton.addActionListener(event -> {
				currentPage = 1;
				paginate();
			});
			jFirstPageButton.setEnabled(false);
		}
		return jFirstPageButton;
	}

	private JButton getJPreviousPageButton() {
		if (jPreviousPageButton == null) {
			jPreviousPageButton = new JButton();
			jPreviousPageButton.setText("<");
			jPreviousPageButton.setMnemonic(KeyEvent.VK_PAGE_UP);
			jPreviousPageButton.addActionListener(event -> {
				if (currentPage > 1) {
					currentPage--;
				}
				paginate();
			});
			jPreviousPageButton.setEnabled(false);
		}
		return jPreviousPageButton;
	}

	private JButton getJNextPageButton() {
		if (jNextPageButton == null) {
			jNextPageButton = new JButton();
			jNextPageButton.setText(">");
			jNextPageButton.setMnemonic(KeyEvent.VK_PAGE_DOWN);
			jNextPageButton.addActionListener(event -> {
				if (currentPage < lastPage) {
					currentPage++;
				}
				paginate();
			});
		}
		return jNextPageButton;
	}

	private JButton getJLastPageButton() {
		if (jLastPageButton == null) {
			jLastPageButton = new JButton();
			jLastPageButton.setText(">>");
			jLastPageButton.setMnemonic(KeyEvent.VK_END);
			jLastPageButton.addActionListener(event -> {
				currentPage = lastPage;
				paginate();
			});
			jLastPageButton.setEnabled(false);
		}
		return jLastPageButton;
	}

	private void togglePreviousPageButton() {
		if (currentPage == 1) {
			jPreviousPageButton.setEnabled(false);
			jFirstPageButton.setEnabled(false);
		} else {
			jPreviousPageButton.setEnabled(true);
			jFirstPageButton.setEnabled(true);
		}
	}

	private void toggleNextPageButton() {
		if (dataProvider.getTotalRowCount() < currentPageSize || currentPage == lastPage) {
			jNextPageButton.setEnabled(false);
			jLastPageButton.setEnabled(false);
		} else {
			jNextPageButton.setEnabled(true);
			jLastPageButton.setEnabled(true);
		}
	}
}
