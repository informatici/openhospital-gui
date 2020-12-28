package org.isf.lab.gui.elements;

import org.isf.exa.model.ExamRow;
import org.isf.lab.model.LaboratoryRow;

public class TestExamRow {
    public static ExamRow examRowWithDesc(String description) {
        ExamRow examRow = new ExamRow();
        examRow.setDescription(description);
        return examRow;
    }
}
