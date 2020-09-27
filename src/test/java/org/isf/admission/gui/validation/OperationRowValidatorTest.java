package org.isf.admission.gui.validation;

import org.isf.admission.gui.TestAdmission;
import org.isf.admission.gui.TestOperationRow;
import org.isf.admission.gui.validation.OperationRowValidator;
import org.isf.generaldata.GeneralData;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.*;

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
        GregorianCalendar operationDate = dateTime(2020, 2, 2, 11, 00);
        GregorianCalendar admissionDate = dateTime(2020, 2, 1, 11, 00);
        GregorianCalendar dischargeDate = dateTime(2020, 2, 3, 11, 00);

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
        GregorianCalendar operationDate = dateTime(2020, 2, 2, 11, 00);
        GregorianCalendar admissionDate = null;
        GregorianCalendar dischargeDate = null;

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
        GregorianCalendar firstOperationDate = dateTime(2019, 2, 2, 11, 00);
        GregorianCalendar secondOperationDate = dateTime(2018, 2, 2, 11, 00);
        GregorianCalendar admissionDate = dateTime(2020, 2, 1, 11, 00);
        GregorianCalendar dischargeDate = dateTime(2020, 2, 3, 11, 00);

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
        GregorianCalendar firstOperationDate = dateTime(2019, 2, 2, 11, 00);
        GregorianCalendar secondOperationDate = dateTime(2018, 2, 2, 11, 00);
        GregorianCalendar admissionDate = dateTime(2020, 2, 1, 11, 00);
        GregorianCalendar dischargeDate = null;

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
        GregorianCalendar firstOperationDate = dateTime(2020, 2, 4, 11, 00);
        GregorianCalendar secondOperationDate = dateTime(2020, 2, 4, 11, 00);
        GregorianCalendar admissionDate = dateTime(2020, 2, 1, 11, 00);
        GregorianCalendar dischargeDate = dateTime(2020, 2, 3, 11, 00);

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
        GregorianCalendar firstOperationDate = dateTime(2020, 2, 1, 10, 00);
        GregorianCalendar secondOperationDate = dateTime(2020, 2, 3, 12, 00);
        GregorianCalendar admissionDate = dateTime(2020, 2, 1, 11, 00);
        GregorianCalendar dischargeDate = dateTime(2020, 2, 3, 11, 00);

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

    private GregorianCalendar dateTime(int year, int month, int day, int hour, int minute) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar;
    }

}