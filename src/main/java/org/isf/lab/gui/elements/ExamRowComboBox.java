package org.isf.lab.gui.elements;

import org.isf.exa.model.ExamRow;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public class ExamRowComboBox extends JComboBox {
    public void addExamRowsWithDescriptionNotEqualTo(List<ExamRow> examRows, final String notEqualTo) {
        Optional.ofNullable(examRows).ifPresent(
				rows -> rows.stream()
								.filter(examRow -> !examRow.getDescription().equals(notEqualTo))
								.forEach(examRow -> addItem(examRow.getDescription()))
		);
    }
}
