package org.isf.lab.gui.elements;

import org.isf.lab.model.Laboratory;
import org.isf.patient.model.Patient;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

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
        assertThat(patientComboBox.getItemCount()).isEqualTo(3);
        assertThat(selectedPatient.isPresent()).isTrue();
        assertThat(selectedPatient.get()).isEqualTo(patient2);
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
        assertThat(patientComboBox.getItemCount()).isEqualTo(3);
        assertThat(selectedPatient.isPresent()).isFalse();
        assertThat(patientComboBox.getSelectedItem()).isEqualTo("angal.lab.selectapatient");
    }
}