package org.isf.lab.gui.elements;

import org.isf.lab.model.Laboratory;
import org.isf.patient.model.Patient;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PatientComboBoxTest {
    @Test
    public void shouldCreateComboBoxWithPatientsAndPatientFromLaboratorySelected() {
        // given:
        Patient patient = TestPatient.patientWithCode(1);
        Patient patient2 = TestPatient.patientWithCode(2);
        List<Patient> patients = Arrays.asList(patient, patient2);
        Laboratory laboratory = new Laboratory();
        laboratory.setPatient(patient2);

        // when:
        PatientComboBox patientComboBox = PatientComboBox.withPatientsAndPatientFromLaboratorySelected(patients, laboratory, false);
        Optional<Patient> selectedPatient = patientComboBox.getSelectedPatient();

        // then:
        assertEquals(3, patientComboBox.getItemCount());
        assertTrue(selectedPatient.isPresent());
        assertEquals(patient2, selectedPatient.get());
    }

    @Test
    public void shouldCreateComboBoxWithPatientsAndNotSelectWhenInsertMode() {
        // given:
        Patient patient = TestPatient.patientWithCode(1);
        Patient patient2 = TestPatient.patientWithCode(2);
        List<Patient> patients = Arrays.asList(patient, patient2);
        Laboratory laboratory = new Laboratory();
        laboratory.setPatient(patient2);

        // when:
        PatientComboBox patientComboBox = PatientComboBox.withPatientsAndPatientFromLaboratorySelected(patients, laboratory, true);
        Optional<Patient> selectedPatient = patientComboBox.getSelectedPatient();

        // then:
        assertEquals(3, patientComboBox.getItemCount());
        assertEquals("angal.lab.selectapatient", patientComboBox.getSelectedItem());
        assertFalse(selectedPatient.isPresent());
    }
}