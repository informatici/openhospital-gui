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

import org.isf.patconsensus.model.PatientConsensus;
import org.isf.patient.dto.PatientExport;
import org.isf.patient.model.Patient;
import org.isf.patient.model.PatientProfilePhoto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Serializes a {@link PatientExport} aggregate to pretty-printed JSON in an open format,
 * for GDPR Art. 20 (right to data portability) exports.
 */
public final class PatientExportJson {

	private static final ObjectMapper MAPPER = new ObjectMapper()
					.registerModule(new JavaTimeModule())
					.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
					.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
					.addMixIn(Patient.class, PatientMixIn.class);

	private PatientExportJson() {
	}

	/**
	 * Serializes the given export aggregate to pretty-printed JSON. Dates are rendered in ISO-8601 format.
	 * The patient profile photo is excluded (binary data, loaded lazily) as well as the patient consensus
	 * (bidirectional association, not part of the export scope) and the derived search/information strings.
	 *
	 * @param export the aggregate to serialize
	 * @return the JSON representation of the export
	 * @throws JsonProcessingException if the aggregate cannot be serialized
	 */
	public static String toJson(PatientExport export) throws JsonProcessingException {
		return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(export);
	}

	private abstract static class PatientMixIn {

		@JsonIgnore
		abstract PatientProfilePhoto getPatientProfilePhoto();

		@JsonIgnore
		abstract PatientConsensus getPatientConsensus();

		@JsonIgnore
		abstract String getSearchString();

		@JsonIgnore
		abstract String getInformations();
	}
}
