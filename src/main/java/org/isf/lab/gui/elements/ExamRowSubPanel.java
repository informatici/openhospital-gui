package org.isf.lab.gui.elements;

import org.isf.exa.model.ExamRow;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.LaboratoryRow;

import javax.swing.*;
import java.util.List;

public class ExamRowSubPanel extends JPanel {

    private static final long serialVersionUID = -8847689740511562992L;

    private JLabel label = null;

    private JRadioButton radioPos = null;

    private JRadioButton radioNeg = null;

    private ButtonGroup group = null;

    public static ExamRowSubPanel forExamRow(ExamRow r) {
        return new ExamRowSubPanel(r, "N");
    }

    public static ExamRowSubPanel forExamRowAndLaboratoryRows(ExamRow r, List<LaboratoryRow> lRows) {
        boolean find = false;
        for (LaboratoryRow lR : lRows) {
            if (r.getDescription()
                    .equalsIgnoreCase(lR.getDescription()))
                find = true;
        }
        if (find) {
            return new ExamRowSubPanel(r, "P");
        } else {
            return new ExamRowSubPanel(r, "N");
        }
    }

    private ExamRowSubPanel(ExamRow row, String result) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        label = new JLabel(row.getDescription());
        this.add(label);

        group = new ButtonGroup();
        radioPos = new JRadioButton(MessageBundle.getMessage("angal.lab.p"));
        radioNeg = new JRadioButton(MessageBundle.getMessage("angal.lab.n"));
        group.add(radioPos);
        group.add(radioNeg);

        this.add(radioPos);
        this.add(radioNeg);
        if (result.equals(MessageBundle.getMessage("angal.lab.p")))
            radioPos.setSelected(true);
        else
            radioNeg.setSelected(true);
    }

    public String getSelectedResult() {
        if (radioPos.isSelected())
            return MessageBundle.getMessage("angal.lab.p");
        else
            return MessageBundle.getMessage("angal.lab.n");
    }

}
