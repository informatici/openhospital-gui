package org.isf.lab.gui.elements;

import org.isf.patient.model.Patient;

public class TestPatient {
    public static Patient patientWithCode(int code) {
        Patient patient = new Patient();
        patient.setCode(code);
        return patient;
    }
}
