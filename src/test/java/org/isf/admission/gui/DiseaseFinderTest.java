package org.isf.admission.gui;

import org.isf.disease.model.Disease;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DiseaseFinderTest {
    private DiseaseFinder diseaseFinder = new DiseaseFinder();

    @Test
    public void shouldFindDiseaseByDescriptionContaining() {
        // given:
        ArrayList<Disease> diseases = new ArrayList<>(Arrays.asList(TestDisease.diseaseWithDescription("AIDS")));

        // when:
        List<Disease> result = diseaseFinder.getSearchDiagnosisResults("id", diseases);

        // then:
        assertEquals(1, result.size());
    }

    @Test
    public void shouldFindAllByEmptyQuery() {
        // given:
        ArrayList<Disease> diseases = new ArrayList<>(Arrays.asList(TestDisease.diseaseWithDescription("AIDS")));

        // when:
        List<Disease> result = diseaseFinder.getSearchDiagnosisResults("", diseases);

        // then:
        assertEquals(1, result.size());
    }

    @Test
    public void shouldNotFindByWrongDescription() {
        // given:
        ArrayList<Disease> diseases = new ArrayList<>(Arrays.asList(TestDisease.diseaseWithDescription("AIDS")));

        // when:
        List<Disease> result = diseaseFinder.getSearchDiagnosisResults("hiv", diseases);

        // then:
        assertTrue(result.isEmpty());
    }

}