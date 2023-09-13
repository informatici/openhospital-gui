/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.admission.gui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.JComboBox;

import org.isf.disease.model.Disease;
import org.junit.jupiter.api.Test;

class DiseaseFinderTest {

	private DiseaseFinder diseaseFinder = new DiseaseFinder();

	@Test
	void shouldFindDiseaseByDescriptionContaining() {
		// given:
		List<Disease> diseases = new ArrayList<>(Arrays.asList(TestDisease.diseaseWithDescription("AIDS")));

		// when:
		List<Disease> result = diseaseFinder.getSearchDiagnosisResults("id", diseases);

		// then:
		assertThat(result).hasSize(1);
	}

	@Test
	void shouldFindAllByEmptyQuery() {
		// given:
		List<Disease> diseases = new ArrayList<>(Arrays.asList(TestDisease.diseaseWithDescription("AIDS")));

		// when:
		List<Disease> result = diseaseFinder.getSearchDiagnosisResults("", diseases);

		// then:
		assertThat(result).hasSize(1);
	}

	@Test
	void shouldNotFindByWrongDescription() {
		// given:
		List<Disease> diseases = new ArrayList<>(Arrays.asList(TestDisease.diseaseWithDescription("AIDS")));

		// when:
		List<Disease> result = diseaseFinder.getSearchDiagnosisResults("hiv", diseases);

		// then:
		assertThat(result).isEmpty();
	}

	@Test
	void shouldFindAndSelectAndAddAllFromDiseaseList() {
		// given:
		List<Disease> diseases = new ArrayList<>(
				Arrays.asList(
						TestDisease.diseaseWithCode("ebola"),
						TestDisease.diseaseWithCode("hiv")
				)
		);
		Disease diseaseToFind = TestDisease.diseaseWithCode("ebola");
		JComboBox<Disease> diseaseBox = new JComboBox<>();

		// when:
		Optional<Disease> result = diseaseFinder.findAndSelectDisease(diseaseToFind, diseases, diseaseBox);

		// then:
		assertThat(result).isPresent();
		assertThat(diseaseBox.getItemCount()).isEqualTo(2);
		assertThat(diseaseBox.getSelectedItem()).isEqualTo(diseaseToFind);
	}

	@Test
	void shouldReturnEmptyForNullDiseaseToFind() {
		// given:
		List<Disease> diseases = new ArrayList<>(
				Arrays.asList(
						TestDisease.diseaseWithCode("ebola"),
						TestDisease.diseaseWithCode("hiv")
				)
		);
		Disease diseaseToFind = null;
		JComboBox<Disease> diseaseBox = new JComboBox<>();

		// when:
		Optional<Disease> result = diseaseFinder.findAndSelectDisease(diseaseToFind, diseases, diseaseBox);

		// then:
		assertThat(result).isNotPresent();
		assertThat(diseaseBox.getItemCount()).isEqualTo(2);
	}

	@Test
	void shouldReturnEmptyForNotFoundDisease() {
		// given:
		List<Disease> diseases = new ArrayList<>(
				Arrays.asList(
						TestDisease.diseaseWithCode("ebola"),
						TestDisease.diseaseWithCode("hiv")
				)
		);
		Disease diseaseToFind = TestDisease.diseaseWithCode("mkbewe");
		JComboBox<Disease> diseaseBox = new JComboBox<>();

		// when:
		Optional<Disease> result = diseaseFinder.findAndSelectDisease(diseaseToFind, diseases, diseaseBox);

		// then:
		assertThat(result).isNotPresent();
		assertThat(diseaseBox.getItemCount()).isEqualTo(2);
	}

	@Test
	void shouldFindAndSelectAndAddSelectedFromAllDiseaseList() {
		// given:
		List<Disease> diseases = new ArrayList<>(
				Arrays.asList(
						TestDisease.diseaseWithCode("ebola"),
						TestDisease.diseaseWithCode("hiv")
				)
		);
		Disease diseaseToFind = TestDisease.diseaseWithCode("ebola");
		JComboBox<Disease> diseaseBox = new JComboBox<>();

		// when:
		Optional<Disease> result = diseaseFinder.findAndSelectFromAllDiseases(diseaseToFind, diseases, diseaseBox);

		// then:
		assertThat(result).isPresent();
		assertThat(diseaseBox.getItemCount()).isOne();
		assertThat(diseaseBox.getSelectedItem()).isEqualTo(diseaseToFind);
	}

	@Test
	void shouldReturnEmptyForNotFoundFromAllDiseaseList() {
		// given:
		List<Disease> diseases = new ArrayList<>(
				Arrays.asList(
						TestDisease.diseaseWithCode("ebola"),
						TestDisease.diseaseWithCode("hiv")
				)
		);
		Disease diseaseToFind = TestDisease.diseaseWithCode("mkbewe");
		JComboBox<Disease> diseaseBox = new JComboBox<>();

		// when:
		Optional<Disease> result = diseaseFinder.findAndSelectFromAllDiseases(diseaseToFind, diseases, diseaseBox);

		// then:
		assertThat(result).isNotPresent();
		assertThat(diseaseBox.getItemCount()).isZero();
	}

}
