package org.isf.lab.gui.elements;

import org.assertj.core.api.Assertions;
import org.isf.exa.model.ExamRow;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ExamRowComboBoxTest {
    @Test
    public void shouldAddExamRowsWithDescriptionDifferentThanGivenString() {
        // given:
        List<ExamRow> examRows = Arrays.asList(
                TestExamRow.examRowWithDesc("test"),
                TestExamRow.examRowWithDesc("test2"),
                TestExamRow.examRowWithDesc("test3")
        );
        String descriptionToSkip = "test2";
        ExamRowComboBox examRowComboBox = new ExamRowComboBox();

        // when:
        examRowComboBox.addExamRowsWithDescriptionNotEqualTo(examRows, descriptionToSkip);

        // then:
        assertThat(examRowComboBox.getItemCount()).isEqualTo(2);
    }
}