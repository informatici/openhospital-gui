package org.isf.admission.gui.validation;

import org.isf.admission.gui.TestAdmission;
import org.isf.admission.gui.TestOperationRow;
import org.isf.generaldata.GeneralData;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperationRowValidatorTest {
    OperationRowValidator operationRowValidator;

    @Before
    public void setUp() {
        GeneralData.LANGUAGE = "en-EN";
        operationRowValidator = new OperationRowValidator();
    }

    @Test
    public void shouldNotAddErrorsWhenDateRangeIsCorrect() {
        // given:
        LocalDateTime operationDate = LocalDateTime.of(2020, 2, 2, 11, 00);
        LocalDateTime admissionDate = LocalDateTime.of(2020, 2, 1, 11, 00);
        LocalDateTime dischargeDate = LocalDateTime.of(2020, 2, 3, 11, 00);

        // when:
        List<OHExceptionMessage> result = operationRowValidator.checkAllOperationRowDate(
                Arrays.asList(TestOperationRow.withOpDate(operationDate)),
                TestAdmission.withAdmAndDisDate(admissionDate, dischargeDate)
        );

        // then:
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldNotAddErrorsWhenAdmissionAndDischargeDatesAreNull() {
        // given:
        LocalDateTime operationDate = LocalDateTime.of(2020, 2, 2, 11, 00);
        LocalDateTime admissionDate = null;
        LocalDateTime dischargeDate = null;

        // when:
        List<OHExceptionMessage> result = operationRowValidator.checkAllOperationRowDate(
                Arrays.asList(TestOperationRow.withOpDate(operationDate)),
                TestAdmission.withAdmAndDisDate(admissionDate, dischargeDate)
        );

        // then:
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldAddErrorsForOperationsBeforeAdmissionDate() {
        // given:
        LocalDateTime firstOperationDate = LocalDateTime.of(2019, 2, 2, 11, 00);
        LocalDateTime secondOperationDate = LocalDateTime.of(2018, 2, 2, 11, 00);
        LocalDateTime admissionDate = LocalDateTime.of(2020, 2, 1, 11, 00);
        LocalDateTime dischargeDate = LocalDateTime.of(2020, 2, 3, 11, 00);

        // when:
        List<OHExceptionMessage> result = operationRowValidator.checkAllOperationRowDate(
                Arrays.asList(
                        TestOperationRow.withOpDate(firstOperationDate),
                        TestOperationRow.withOpDate(secondOperationDate)
                ),
                TestAdmission.withAdmAndDisDate(admissionDate, dischargeDate)
        );

        // then:
        assertEquals(2, result.size());
    }

    @Test
    public void shouldAddErrorsForOperationsBeforeAdmissionDateWhenDischargeDateNotProvided() {
        // given:
        LocalDateTime firstOperationDate = LocalDateTime.of(2019, 2, 2, 11, 00);
        LocalDateTime secondOperationDate = LocalDateTime.of(2018, 2, 2, 11, 00);
        LocalDateTime admissionDate = LocalDateTime.of(2020, 2, 1, 11, 00);
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
        assertEquals(2, result.size());
    }

    @Test
    public void shouldAddErrorsForOperationsAfterDischargeDate() {
        // given:
        LocalDateTime firstOperationDate = LocalDateTime.of(2020, 2, 4, 11, 00);
        LocalDateTime secondOperationDate = LocalDateTime.of(2020, 2, 4, 11, 00);
        LocalDateTime admissionDate = LocalDateTime.of(2020, 2, 1, 11, 00);
        LocalDateTime dischargeDate = LocalDateTime.of(2020, 2, 3, 11, 00);

        // when:
        List<OHExceptionMessage> result = operationRowValidator.checkAllOperationRowDate(
                Arrays.asList(
                        TestOperationRow.withOpDate(firstOperationDate),
                        TestOperationRow.withOpDate(secondOperationDate)
                ),
                TestAdmission.withAdmAndDisDate(admissionDate, dischargeDate)
        );

        // then:
        assertEquals(2, result.size());
    }

    @Test
    public void shouldIgnoreHourWhenComparingDischargeDates() {
        // given:
        LocalDateTime firstOperationDate = LocalDateTime.of(2020, 2, 1, 10, 00);
        LocalDateTime secondOperationDate = LocalDateTime.of(2020, 2, 3, 12, 00);
        LocalDateTime admissionDate = LocalDateTime.of(2020, 2, 1, 11, 00);
        LocalDateTime dischargeDate = LocalDateTime.of(2020, 2, 3, 11, 00);

        // when:
        List<OHExceptionMessage> result = operationRowValidator.checkAllOperationRowDate(
                Arrays.asList(
                        TestOperationRow.withOpDate(firstOperationDate),
                        TestOperationRow.withOpDate(secondOperationDate)
                ),
                TestAdmission.withAdmAndDisDate(admissionDate, dischargeDate)
        );

        // then:
        assertEquals(1, result.size());
    }
}