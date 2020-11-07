package org.isf.admission.gui;

import org.isf.operation.model.OperationRow;

import java.time.LocalDateTime;
import java.util.GregorianCalendar;

public class TestOperationRow {
    public static OperationRow withOpDate(LocalDateTime dateTime) {
        OperationRow operationRow = new OperationRow();
        operationRow.setOpDate(dateTime);
        return operationRow;
    }
}
