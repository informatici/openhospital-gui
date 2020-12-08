package org.isf.lab.gui.elements;

import org.isf.exa.model.Exam;
import org.isf.lab.model.Laboratory;
import org.isf.patient.model.Patient;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

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

        // then:
        assertEquals(3, patientComboBox.getItemCount());
        assertEquals(patient2, patientComboBox.getSelectedItem());
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

        // then:
        assertEquals(3, patientComboBox.getItemCount());
        assertEquals("angal.lab.selectapatient", patientComboBox.getSelectedItem());
    }
}