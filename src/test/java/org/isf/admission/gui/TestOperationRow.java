package org.isf.admission.gui;

import org.isf.operation.model.OperationRow;

import java.util.GregorianCalendar;

public class TestOperationRow {
    public static OperationRow withOpDate(GregorianCalendar date) {
        OperationRow operationRow = new OperationRow();
        operationRow.setOpDate(date);
        return operationRow;
    }
}
