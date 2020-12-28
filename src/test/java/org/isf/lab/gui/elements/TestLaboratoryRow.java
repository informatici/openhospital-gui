package org.isf.lab.gui.elements;

import org.isf.lab.model.LaboratoryRow;

public class TestLaboratoryRow {
    public static LaboratoryRow laboratoryRowWithDesc(String description) {
        LaboratoryRow laboratoryRow = new LaboratoryRow();
        laboratoryRow.setDescription(description);
        return laboratoryRow;
    }
}
