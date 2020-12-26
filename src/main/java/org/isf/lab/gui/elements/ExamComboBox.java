package org.isf.lab.gui.elements;

import org.isf.exa.model.Exam;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.Laboratory;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public class ExamComboBox extends JComboBox {
    private ExamComboBox() {
    }

    public static ExamComboBox withExamsAndExamFromLaboratorySelected(List<Exam> exams, Laboratory lab, boolean insert) {
        ExamComboBox examComboBox = new ExamComboBox();
        examComboBox.addItem(MessageBundle.getMessage("angal.lab.selectanexam"));
        Optional.ofNullable(exams).ifPresent(examList ->
                examList.stream()
                        .peek(exam -> examComboBox.addItem(exam))
                        .filter(exam -> (lab.getExam() != null && !insert) && exam.getCode().equals(lab.getExam().getCode()))
                        .forEach(exam -> examComboBox.setSelectedItem(exam))
        );

        return examComboBox;
    }

    public Optional<Exam> getSelectedExam() {
        return Optional.ofNullable(getSelectedItem())
                .filter(o -> o instanceof Exam)
                .map(o -> (Exam) o);
    }
}
