/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

import java.awt.Component;
import java.util.Optional;

import org.isf.menu.manager.Context;
import org.isf.patconsensus.manager.PatientConsensusBrowserManager;
import org.isf.patconsensus.model.PatientConsensus;
import org.isf.patient.model.Patient;
import org.isf.utils.jobjects.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Signals that a patient has been flagged by the administration, so that the staff can check with them before providing
 * further services. The warning is informational only: it never prevents the operation the user is starting.
 */
public final class PatientAdministrativeFlagWarning {

	private static final Logger LOGGER = LoggerFactory.getLogger(PatientAdministrativeFlagWarning.class);

	private PatientAdministrativeFlagWarning() {
	}

	/**
	 * Shows a dismissable warning when the given patient has been flagged by the administration.
	 *
	 * @param parentComponent the component the dialog is shown over
	 * @param patient the patient being selected, may be {@code null}
	 */
	public static void showIfFlagged(Component parentComponent, Patient patient) {
		if (patient == null || patient.getCode() == null) {
			return;
		}
		Optional<PatientConsensus> consensus;
		try {
			consensus = Context.getApplicationContext().getBean(PatientConsensusBrowserManager.class)
							.getPatientConsensusByUserId(patient.getCode());
		} catch (Exception e) {
			// a warning must never get in the way of the operation the user is starting
			LOGGER.error("Unable to read the consensus of patient {}", patient.getCode(), e);
			return;
		}
		if (consensus.isEmpty() || !consensus.get().isAdministrativeFlag()) {
			return;
		}
		String reason = consensus.get().getAdministrativeReason();
		if (reason == null || reason.isBlank()) {
			MessageDialog.warning(parentComponent, "angal.patient.consensus.administrative.warning.msg");
		} else {
			MessageDialog.warning(parentComponent, "angal.patient.consensus.administrative.warning.reason.fmt.msg", reason);
		}
	}
}
