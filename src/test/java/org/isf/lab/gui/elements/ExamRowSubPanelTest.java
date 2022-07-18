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
package org.isf.lab.gui.elements;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.isf.exa.model.ExamRow;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.LaboratoryRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExamRowSubPanelTest {

	@BeforeEach
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
		assertThat(result.getSelectedResult()).isEqualTo(MessageBundle.getMessage("angal.lab.negativeabbr.btn"));
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
		assertThat(result.getSelectedResult()).isEqualTo(MessageBundle.getMessage("angal.lab.positiveabbr.btn"));
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
		assertThat(result.getSelectedResult()).isEqualTo(MessageBundle.getMessage("angal.lab.negativeabbr.btn"));
	}

}
