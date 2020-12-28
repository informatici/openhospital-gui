package org.isf.lab.gui.elements;

import org.assertj.core.api.Assertions;
import org.isf.exa.model.ExamRow;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.LaboratoryRow;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ExamRowSubPanelTest {
    @Before
    public void setUp() {
        GeneralData.LANGUAGE = "en";
        MessageBundle.initialize();
    }

    @Test
    public void shouldCreateWithSelectedNResultWhenOnlyExamRowProvided() {
        // given:
        ExamRow examRow = new ExamRow();

        // when:
        ExamRowSubPanel result = ExamRowSubPanel.forExamRow(examRow);

        // then:
        Assertions.assertThat(result.getSelectedResult()).isEqualTo("N");
    }

    @Test
    public void shouldCreateWithSelectedPResultWhenMatchesLaboratoryRow() {
        // given:
        ExamRow examRow = new ExamRow();
        examRow.setDescription("test2");
        List<LaboratoryRow> laboratoryRows = Arrays.asList(
            TestLaboratoryRow.laboratoryRowWithDesc("test"),
            TestLaboratoryRow.laboratoryRowWithDesc("test2")
        );

        // when:
        ExamRowSubPanel result = ExamRowSubPanel.forExamRowAndLaboratoryRows(examRow, laboratoryRows);

        // then:
        Assertions.assertThat(result.getSelectedResult()).isEqualTo("P");
    }

    @Test
    public void shouldCreateWithSelectedNResultWhenNotMatchesLaboratoryRow() {
        // given:
        ExamRow examRow = new ExamRow();
        examRow.setDescription("test2");
        List<LaboratoryRow> laboratoryRows = Arrays.asList(
                TestLaboratoryRow.laboratoryRowWithDesc("test3"),
                TestLaboratoryRow.laboratoryRowWithDesc("test4")
        );

        // when:
        ExamRowSubPanel result = ExamRowSubPanel.forExamRowAndLaboratoryRows(examRow, laboratoryRows);

        // then:
        Assertions.assertThat(result.getSelectedResult()).isEqualTo("N");
    }
}