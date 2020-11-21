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

import org.isf.disease.manager.DiseaseBrowserManager;
import org.isf.disease.model.Disease;
import org.isf.generaldata.MessageBundle;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;

import javax.swing.JComboBox;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DiseaseFinder {
    public List<Disease> getSearchDiagnosisResults(String query, List<Disease> diseaseList) {
        return diseaseList.stream()
                .filter(disease -> query.equals("") || diseaseMatchPatterns(query, disease))
                .collect(Collectors.toList());
    }

    public Optional<Disease> findAndSelectDisease(Disease disease, List<Disease> diseaseOutList, JComboBox diseaseBox) {
        for (Disease elem : diseaseOutList) {
            diseaseBox.addItem(elem);
            // Search for saved diseaseOut3
            if (disease != null && disease.getCode().equalsIgnoreCase(elem.getCode())) {
                diseaseBox.setSelectedItem(elem); // TODO: if present set selected item in parent class
                return Optional.ofNullable(elem);
            }
        }
        return Optional.empty();
    }

    public Optional<Disease> findAndSelectFromAllDiseases(Disease diseaseIn, List<Disease> diseaseAllList, JComboBox diseaseInBox) {
        //Not found: search among all diseases
        for (Disease elem : diseaseAllList) {
            if (diseaseIn.getCode().equalsIgnoreCase(elem.getCode())) {
                diseaseInBox.addItem(elem);
                diseaseInBox.setSelectedItem(elem);
                return Optional.of(elem);
            }
        }
        return Optional.empty();
    }

    private boolean diseaseMatchPatterns(String query, Disease disease) {
        String[] patterns = query.trim().split(" ");
        String description = disease.getDescription().toLowerCase();
        return Arrays.stream(patterns)
                .filter(pattern -> description.contains(pattern.toLowerCase()))
                .findAny()
                .isPresent();
    }
}
