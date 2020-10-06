package org.isf.admission.gui;

import org.isf.disease.model.Disease;

public class TestDisease {
    public static Disease diseaseWithDescription(String description) {
        Disease disease = new Disease();
        disease.setDescription(description);
        return disease;
    }
}
