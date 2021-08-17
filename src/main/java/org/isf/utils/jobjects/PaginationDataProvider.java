package org.isf.utils.jobjects;

import java.util.List;

public interface PaginationDataProvider<T> {
    int getTotalRowCount();
	List<T> getRows(int startPage, int pageSize, int sortColumn, boolean sortDescending);
}