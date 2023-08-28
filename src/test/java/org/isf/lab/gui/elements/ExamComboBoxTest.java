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
package org.isf.lab.gui.elements;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.isf.exa.model.Exam;
import org.isf.lab.model.Laboratory;
import org.junit.jupiter.api.Test;

class ExamComboBoxTest {

	@Test
	void shouldCreateComboBoxWithPersistedExamsAndExamFromLaboratorySelected() {
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
		assertThat(examComboBox.getItemCount()).isEqualTo(3);
		assertThat(selectedExam).isPresent().contains(exam2);
	}

	@Test
	void shouldCreateComboBoxWithPersistedExamsAndNotSelectWhenInsertMode() {
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
		assertThat(examComboBox.getItemCount()).isEqualTo(3);
		assertThat(selectedExam).isNotPresent();
	}

}
