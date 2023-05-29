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

import java.util.List;
import java.util.Optional;

import javax.swing.JComboBox;

import org.isf.exa.model.Exam;
import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.Laboratory;

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
