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
package org.isf.lab.gui.elements;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.isf.generaldata.MessageBundle;
import org.isf.lab.model.Laboratory;
import org.isf.patient.model.Patient;
import org.junit.jupiter.api.Test;

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
		assertThat(patientComboBox.getSelectedItem()).isEqualTo(MessageBundle.getMessage("angal.lab.selectapatient"));
	}

}
