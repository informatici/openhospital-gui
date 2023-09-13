/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.admission.gui.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.isf.admission.gui.TestAdmission;
import org.isf.admission.gui.TestOperationRow;
import org.isf.generaldata.GeneralData;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OperationRowValidatorTest {

	OperationRowValidator operationRowValidator;

	@BeforeEach
	void setUp() {
		GeneralData.LANGUAGE = "en-EN";
		operationRowValidator = new OperationRowValidator();
	}

	@Test
	void shouldNotAddErrorsWhenDateRangeIsCorrect() {
		// given:
		LocalDateTime operationDate = LocalDateTime.of(2020, 2, 2, 11, 0);
		LocalDateTime admissionDate = LocalDateTime.of(2020, 2, 1, 11, 0);
		LocalDateTime dischargeDate = LocalDateTime.of(2020, 2, 3, 11, 0);

		// when:
		List<OHExceptionMessage> result = operationRowValidator.checkAllOperationRowDate(
				Arrays.asList(TestOperationRow.withOpDate(operationDate)),
				TestAdmission.withAdmAndDisDate(admissionDate, dischargeDate)
		);

		// then:
		assertThat(result).isEmpty();
	}

	@Test
	void shouldNotAddErrorsWhenAdmissionAndDischargeDatesAreNull() {
		// given:
		LocalDateTime operationDate = LocalDateTime.of(2020, 2, 2, 11, 0);
		LocalDateTime admissionDate = null;
		LocalDateTime dischargeDate = null;

		// when:
		List<OHExceptionMessage> result = operationRowValidator.checkAllOperationRowDate(
				Arrays.asList(TestOperationRow.withOpDate(operationDate)),
				TestAdmission.withAdmAndDisDate(admissionDate, dischargeDate)
		);

		// then:
		assertThat(result).isEmpty();
	}

	@Test
	void shouldAddErrorsForOperationsBeforeAdmissionDate() {
		// given:
		LocalDateTime firstOperationDate = LocalDateTime.of(2019, 2, 2, 11, 0);
		LocalDateTime secondOperationDate = LocalDateTime.of(2018, 2, 2, 11, 0);
		LocalDateTime admissionDate = LocalDateTime.of(2020, 2, 1, 11, 0);
		LocalDateTime dischargeDate = LocalDateTime.of(2020, 2, 3, 11, 0);

		// when:
		List<OHExceptionMessage> result = operationRowValidator.checkAllOperationRowDate(
				Arrays.asList(
						TestOperationRow.withOpDate(firstOperationDate),
						TestOperationRow.withOpDate(secondOperationDate)
				),
				TestAdmission.withAdmAndDisDate(admissionDate, dischargeDate)
		);

		// then:
		assertThat(result).hasSize(2);
	}

	@Test
	void shouldAddErrorsForOperationsBeforeAdmissionDateWhenDischargeDateNotProvided() {
		// given:
		LocalDateTime firstOperationDate = LocalDateTime.of(2019, 2, 2, 11, 0);
		LocalDateTime secondOperationDate = LocalDateTime.of(2018, 2, 2, 11, 0);
		LocalDateTime admissionDate = LocalDateTime.of(2020, 2, 1, 11, 0);
		LocalDateTime dischargeDate = null;

		// when:
		List<OHExceptionMessage> result = operationRowValidator.checkAllOperationRowDate(
				Arrays.asList(
						TestOperationRow.withOpDate(firstOperationDate),
						TestOperationRow.withOpDate(secondOperationDate)
				),
				TestAdmission.withAdmAndDisDate(admissionDate, dischargeDate)
		);

		// then:
		assertThat(result).hasSize(2);
	}

	@Test
	void shouldAddErrorsForOperationsAfterDischargeDate() {
		// given:
		LocalDateTime firstOperationDate = LocalDateTime.of(2020, 2, 4, 11, 0);
		LocalDateTime secondOperationDate = LocalDateTime.of(2020, 2, 4, 11, 0);
		LocalDateTime admissionDate = LocalDateTime.of(2020, 2, 1, 11, 0);
		LocalDateTime dischargeDate = LocalDateTime.of(2020, 2, 3, 11, 0);

		// when:
		List<OHExceptionMessage> result = operationRowValidator.checkAllOperationRowDate(
				Arrays.asList(
						TestOperationRow.withOpDate(firstOperationDate),
						TestOperationRow.withOpDate(secondOperationDate)
				),
				TestAdmission.withAdmAndDisDate(admissionDate, dischargeDate)
		);

		// then:
		assertThat(result).hasSize(2);
	}

}
