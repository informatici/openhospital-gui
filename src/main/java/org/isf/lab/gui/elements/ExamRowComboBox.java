package org.isf.lab.gui.elements;

import org.isf.exa.model.ExamRow;

import javax.swing.*;
import java.util.List;

public class ExamRowComboBox extends JComboBox {
    public void addExamRowsWithDescriptionNotEqualTo(List<ExamRow> examRows, String notEqualTo) {
        if (null != examRows) {
            for (ExamRow r : examRows) {
                if (!r.getDescription().equals(notEqualTo))
                    addItem(r.getDescription());
            }
        }
        /*
		String finalResult = result;
		Optional.ofNullable(rows).ifPresent(
				examRows -> examRows.stream()
								.filter(examRow -> !examRow.getDescription().equals(finalResult))
								.forEach(examRow -> examComboBox.addItem(examRow.getDescription()))
		);

		 */
    }
}
