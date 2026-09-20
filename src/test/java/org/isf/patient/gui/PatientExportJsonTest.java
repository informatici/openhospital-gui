/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2025 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.patient.gui;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.isf.admission.model.Admission;
import org.isf.patconsensus.model.PatientConsensus;
import org.isf.patient.dto.PatientExport;
import org.isf.patient.model.Patient;
import org.isf.patient.model.PatientProfilePhoto;
import org.isf.patvac.model.PatientVaccine;
import org.isf.vaccine.model.Vaccine;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class PatientExportJsonTest {

	@Test
	void shouldSerializeAllGroupsToValidJson() throws Exception {
		// given:
		PatientExport export = buildPatientExport();

		// when:
		String json = PatientExportJson.toJson(export);
		JsonNode root = new ObjectMapper().readTree(json);

		// then:
		assertThat(root.get("patient")).isNotNull();
		assertThat(root.get("patient").get("firstName").asText()).isEqualTo("John");
		for (String group : new String[] { "admissions", "opds", "laboratories", "therapies", "operations", "vaccines", "examinations", "bills", "billItems",
						"billPayments" }) {
			assertThat(root.get(group)).as("group '%s' must be present", group).isNotNull();
			assertThat(root.get(group).isArray()).as("group '%s' must be an array", group).isTrue();
		}
		assertThat(root.get("vaccines")).isNotEmpty();
		assertThat(root.get("admissions")).isNotEmpty();
	}

	@Test
	void shouldSerializeDatesInIsoFormat() throws Exception {
		// given:
		PatientExport export = buildPatientExport();

		// when:
		String json = PatientExportJson.toJson(export);
		JsonNode root = new ObjectMapper().readTree(json);

		// then:
		assertThat(root.get("patient").get("birthDate").asText()).isEqualTo("1980-05-20");
		assertThat(root.get("vaccines").get(0).get("vaccineDate").asText()).startsWith("2023-05-10T09:30");
	}

	@Test
	void shouldNotSerializeProfilePhotoNorConsensus() throws Exception {
		// given:
		PatientExport export = buildPatientExport();

		// when:
		String json = PatientExportJson.toJson(export);
		JsonNode root = new ObjectMapper().readTree(json);

		// then:
		assertThat(root.get("patient").has("patientProfilePhoto")).isFalse();
		assertThat(root.get("patient").has("patientConsensus")).isFalse();
		assertThat(root.get("patient").has("searchString")).isFalse();
		assertThat(root.get("patient").has("informations")).isFalse();
		// the mix-in must also apply to patients nested in the connected records
		assertThat(root.get("vaccines").get(0).get("patient").has("patientProfilePhoto")).isFalse();
		assertThat(root.get("vaccines").get(0).get("patient").has("patientConsensus")).isFalse();
	}

	private PatientExport buildPatientExport() {
		Patient patient = new Patient();
		patient.setCode(1);
		patient.setFirstName("John");
		patient.setSecondName("Doe");
		patient.setBirthDate(LocalDate.of(1980, 5, 20));
		patient.setSex('M');
		patient.setPatientProfilePhoto(new PatientProfilePhoto());
		patient.setPatientConsensus(new PatientConsensus(true, false, patient));

		Admission admission = new Admission();
		admission.setId(10);
		admission.setPatient(patient);
		admission.setAdmDate(LocalDateTime.of(2023, 5, 9, 8, 0));
		List<Admission> admissions = new ArrayList<>();
		admissions.add(admission);

		Vaccine vaccine = new Vaccine();
		vaccine.setCode("1");
		vaccine.setDescription("Vaccine");
		PatientVaccine patientVaccine = new PatientVaccine(20, 1, LocalDateTime.of(2023, 5, 10, 9, 30), patient, vaccine, 0);
		List<PatientVaccine> vaccines = new ArrayList<>();
		vaccines.add(patientVaccine);

		PatientExport export = new PatientExport();
		export.setPatient(patient);
		export.setAdmissions(admissions);
		export.setOpds(Collections.emptyList());
		export.setLaboratories(Collections.emptyList());
		export.setTherapies(Collections.emptyList());
		export.setOperations(Collections.emptyList());
		export.setVaccines(vaccines);
		export.setExaminations(Collections.emptyList());
		export.setBills(Collections.emptyList());
		export.setBillItems(Collections.emptyList());
		export.setBillPayments(Collections.emptyList());
		return export;
	}
}
