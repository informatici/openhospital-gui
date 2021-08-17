package org.isf.utils.jobjects;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public abstract class PageableTableModel<T> extends AbstractTableModel {
    private List<T> objectRows = new ArrayList<>();

    public List<T> getObjectRows() {
        return objectRows;
    }

    public void setObjectRows(List<T> objectRows) {
        this.objectRows = objectRows;
    }

    @Override
    public int getRowCount() {
        return objectRows.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T t = objectRows.get(rowIndex);
        return getValueAt(t, columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (objectRows.isEmpty()) {
            return Object.class;
        }
        Object valueAt = getValueAt(0, columnIndex);
        return valueAt!=null? valueAt.getClass(): Object.class;
    }

    public abstract Object getValueAt(T t, int columnIndex);

    @Override
    public abstract String getColumnName(int column);

    public abstract String getFieldName(int column);
}