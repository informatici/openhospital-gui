package org.isf.lab.gui.elements;

import org.isf.exa.model.Exam;
import org.isf.lab.model.Laboratory;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        Optional<Exam> selectedExam = examComboBox.getSelectedExam();

        // then:
        assertEquals(3, examComboBox.getItemCount());
        assertTrue(selectedExam.isPresent());
        assertEquals(exam2, selectedExam.get());
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
        Optional<Exam> selectedExam = examComboBox.getSelectedExam();

        // then:
        assertEquals(3, examComboBox.getItemCount());
        assertFalse(selectedExam.isPresent());
    }

}