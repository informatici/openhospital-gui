package org.isf.lab.gui.elements;

import org.isf.exa.model.Exam;
import org.isf.lab.model.Laboratory;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ExamComboBoxTest {
    @Test
    public void shouldCreateComboBoxWithPersistedExamsAndExamFromLaboratorySelected() {
        // given:
        Exam exam = TestExam.examWithCode("test");
        Exam exam2 = TestExam.examWithCode("test2");
        List<Exam> exams = Arrays.asList(exam, exam2);
        Laboratory laboratory = new Laboratory();
        laboratory.setExam(exam2);

        // when:
        ExamComboBox examComboBox = ExamComboBox.withExamsAndExamFromLaboratorySelected(exams, laboratory, false);

        // then:
        assertEquals(3, examComboBox.getItemCount());
        assertEquals(exam2, examComboBox.getSelectedItem());
    }

    @Test
    public void shouldCreateComboBoxWithPersistedExamsAndNotSelectWhenInsertMode() {
        // given:
        Exam exam = TestExam.examWithCode("test");
        Exam exam2 = TestExam.examWithCode("test2");
        List<Exam> exams = Arrays.asList(exam, exam2);
        Laboratory laboratory = new Laboratory();
        laboratory.setExam(exam2);

        // when:
        ExamComboBox examComboBox = ExamComboBox.withExamsAndExamFromLaboratorySelected(exams, laboratory, true);

        // then:
        assertEquals(3, examComboBox.getItemCount());
        assertNull(examComboBox.getSelectedItem());
    }

}