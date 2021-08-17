package org.isf.utils.jobjects;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

public class PaginatedTableDecoratorFull<T> {
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
    private static final int MaxPagingCompToShow = 9;
    private static final String Ellipses = "...";

    private PaginatedTableDecoratorFull(JTable table, PaginationDataProvider<T> dataProvider,
                                    int[] pageSizes, int defaultPageSize) {
        this.table = table;
        this.dataProvider = dataProvider;
        this.pageSizes = pageSizes;
        this.currentPageSize = defaultPageSize;
    }

    public static <T> PaginatedTableDecoratorFull<T> decorate(JTable table,
                                                          PaginationDataProvider<T> dataProvider,
                                                          int[] pageSizes, int defaultPageSize) {
        PaginatedTableDecoratorFull<T> decorator = new PaginatedTableDecoratorFull<>(table, dataProvider,
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
            table.getRowSorter().addRowSorterListener(new RowSorterListener() {
                @Override
                public void sorterChanged(RowSorterEvent e) {
                    if(e.getType()== RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                        currentPage = 1;
                        paginate();
                    }
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
        paginationPanel.add(getJFirstPageButton());
        paginationPanel.add(getJPreviousPageButton());
        paginationPanel.add(getJLabelCurrentPage());
        paginationPanel.add(getJNextPageButton());
        paginationPanel.add(getJLastPageButton());
        paginationPanel.add(Box.createHorizontalStrut(15));
        
        pageLinkPanel = new JPanel(new GridLayout(1, MaxPagingCompToShow, 3, 3));
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
        pageLinkPanel.removeAll();
        int totalRows = dataProvider.getTotalRowCount();
        int pages = (int) Math.ceil((double) totalRows / currentPageSize);
        ButtonGroup buttonGroup = new ButtonGroup();
        if (pages > MaxPagingCompToShow) {
            addPageButton(pageLinkPanel, buttonGroup, 1);
            if (currentPage > (pages - ((MaxPagingCompToShow + 1) / 2))) {
                //case: 1 ... n->lastPage
                pageLinkPanel.add(createEllipsesComponent());
                addPageButtonRange(pageLinkPanel, buttonGroup, pages - MaxPagingCompToShow + 3, pages);
            } else if (currentPage <= (MaxPagingCompToShow + 1) / 2) {
                //case: 1->n ...lastPage
                addPageButtonRange(pageLinkPanel, buttonGroup, 2, MaxPagingCompToShow - 2);
                pageLinkPanel.add(createEllipsesComponent());
                addPageButton(pageLinkPanel, buttonGroup, pages);
            } else {//case: 1 .. x->n .. lastPage
                pageLinkPanel.add(createEllipsesComponent());//first ellipses
                //currentPage is approx mid point among total max-4 center links
                int start = currentPage - (MaxPagingCompToShow - 4) / 2;
                int end = start + MaxPagingCompToShow - 5;
                addPageButtonRange(pageLinkPanel, buttonGroup, start, end);
                pageLinkPanel.add(createEllipsesComponent());//last ellipsis
                addPageButton(pageLinkPanel, buttonGroup, pages);//last page link
            }
        } else {
            addPageButtonRange(pageLinkPanel, buttonGroup, 1, pages);
        }
        getJLabelCurrentPage();
		togglePreviousPageButton();
		toggleNextPageButton();
        pageLinkPanel.getParent().validate();
        pageLinkPanel.getParent().repaint();
    }

    private Component createEllipsesComponent() {
        return new JLabel(Ellipses, SwingConstants.CENTER);
    }

    private void addPageButtonRange(JPanel parentPanel, ButtonGroup buttonGroup, int start, int end) {
        for (; start <= end; start++) {
            addPageButton(parentPanel, buttonGroup, start);
        }
    }

    private void addPageButton(JPanel parentPanel, ButtonGroup buttonGroup, int pageNumber) {
        JToggleButton toggleButton = new JToggleButton(Integer.toString(pageNumber));
        toggleButton.setMargin(new Insets(1, 3, 1, 3));
        buttonGroup.add(toggleButton);
        parentPanel.add(toggleButton);
        if (pageNumber == currentPage) {
            toggleButton.setSelected(true);
        }
        toggleButton.addActionListener(ae -> {
            currentPage = Integer.parseInt(ae.getActionCommand());
            paginate();
        });
    }

    public void paginate() {
        List<T> rows = dataProvider.getRows(currentPage - 1, currentPageSize);
        lastPage =  (dataProvider.getTotalRowCount() / currentPageSize) + 1;
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
				if (currentPage > 1) currentPage--;
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
				if (currentPage < lastPage) currentPage++;
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