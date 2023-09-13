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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.JComboBox;

import org.isf.disease.model.Disease;

public class DiseaseFinder {

	public List<Disease> getSearchDiagnosisResults(String query, List<Disease> diseaseList) {
		return diseaseList.stream()
				.filter(disease -> query.equals("") || diseaseMatchPatterns(query, disease))
				.collect(Collectors.toList());
	}

	public Optional<Disease> findAndSelectDisease(Disease diseaseToFind, List<Disease> diseaseOutList, JComboBox diseaseBox) {
		return diseaseOutList.stream()
				.peek(disease -> diseaseBox.addItem(disease))
				.filter(disease -> diseaseToFind != null)
				.filter(disease -> diseaseToFind.getCode().equalsIgnoreCase(disease.getCode()))
				.peek(disease -> diseaseBox.setSelectedItem(disease))
				.collect(Collectors.toList())
				.stream()
				.findFirst();
	}

	public Optional<Disease> findAndSelectFromAllDiseases(Disease diseaseIn, List<Disease> diseaseAllList, JComboBox diseaseInBox) {
		return diseaseAllList.stream()
				.filter(disease -> diseaseIn.getCode().equalsIgnoreCase(disease.getCode()))
				.peek(disease -> diseaseInBox.addItem(disease))
				.peek(disease -> diseaseInBox.setSelectedItem(disease))
				.findFirst();
	}

	private boolean diseaseMatchPatterns(String query, Disease disease) {
		String[] patterns = query.trim().split(" ");
		String description = disease.getDescription().toLowerCase();
		return Arrays.stream(patterns)
				.anyMatch(pattern -> description.contains(pattern.toLowerCase()));
	}

}
