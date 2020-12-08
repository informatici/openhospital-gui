package org.isf.lab.gui.elements;

import org.isf.exa.model.Exam;
import org.isf.exatype.model.ExamType;

public class TestExam {
    public static Exam examWithCode(String code) {
        Exam exam = new Exam();
        exam.setCode(code);
        exam.setDescription("test");
        exam.setExamtype(type());
        return exam;
    }

    private static ExamType type() {
        ExamType examType = new ExamType();
        examType.setCode("jhg");
        examType.setDescription("hgf");
        return examType;
    }
}
