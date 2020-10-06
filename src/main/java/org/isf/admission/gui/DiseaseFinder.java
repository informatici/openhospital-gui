package org.isf.admission.gui;

import org.isf.disease.model.Disease;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DiseaseFinder {
    public List<Disease> getSearchDiagnosisResults(String query, ArrayList<Disease> diseaseList) {
        return diseaseList.stream()
                .filter(disease -> query.equals("") || diseaseMatchPatterns(query, disease))
                .collect(Collectors.toList());
    }

    public Optional<Disease> findAndSelectDisease(Disease found, Disease diseaseOut3, ArrayList<Disease> diseaseOutList, JComboBox diseaseBox) {
        for (Disease elem : diseaseOutList) {
            diseaseBox.addItem(elem);

            // Search for saved diseaseOut3
            if (
                    // TODO: editing && - move this outside if not editing don't run search
                    found == null && diseaseOut3 != null
                    && diseaseOut3.getCode().equalsIgnoreCase(elem.getCode())) {
                diseaseBox.setSelectedItem(elem); // TODO: if present set selected item in parent class
                return Optional.ofNullable(elem);
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
