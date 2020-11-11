/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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