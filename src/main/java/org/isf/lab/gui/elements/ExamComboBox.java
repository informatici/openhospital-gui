package org.isf.lab.gui.elements;

import org.isf.exa.model.Exam;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.Laboratory;

import javax.swing.*;
import java.util.List;

public class ExamComboBox extends JComboBox {
    private ExamComboBox() {
    }

    public static ExamComboBox withExamsAndExamFromLaboratorySelected(List<Exam> exams, Laboratory lab, boolean insert) {
        ExamComboBox examComboBox = new ExamComboBox();
        Exam examSel = null;
        examComboBox.addItem(MessageBundle.getMessage("angal.lab.selectanexam"));

        if (null != exams) {
            for (Exam elem : exams) {
                if (!insert && elem.getCode()!=null) {
                    if (elem.getCode().equalsIgnoreCase((lab.getExam().getCode()))) {
                        examSel=elem;
                    }
                }
                examComboBox.addItem(elem);
            }
        }
        examComboBox.setSelectedItem(examSel);

        return examComboBox;
    }
}
