package org.isf.admission.gui.validation;

import org.isf.admission.model.Admission;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.operation.model.OperationRow;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.exception.model.OHSeverityLevel;

import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OperationRowValidator {

    public final DateFormat currentDateFormat;

    public OperationRowValidator() {
        currentDateFormat = DateFormat.getDateInstance(DateFormat.SHORT, new Locale(GeneralData.LANGUAGE));
    }

    @SuppressWarnings("deprecation")
    public List<OHExceptionMessage> checkAllOperationRowDate(List<OperationRow> operationRows, Admission admission) {
        Optional<Date> beginDate = Optional.ofNullable(admission.getAdmDate())
                .map(Calendar::getTime);
        Optional<Date> endDate = Optional.ofNullable(admission.getDisDate())
                .map(Calendar::getTime);
        Stream<Date> updatedOperationDatesStream = getUpdatedOperationDates(operationRows);
        if ((beginDate.isPresent()) && (endDate.isPresent())) {
            return validateAgainstBeginAndEndDate(updatedOperationDatesStream, beginDate.get(), endDate.get());
        } else if ((beginDate.isPresent()) && (!endDate.isPresent())) {
            return validateAgainstBeginDate(updatedOperationDatesStream, beginDate.get());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private Stream<Date> getUpdatedOperationDates(List<OperationRow> list) {
        return list.stream()
                .map(operationRow -> operationRow.getOpDate().getTime())
                .map(currentRowDate -> {
                    Date updatedDate = new Date(currentRowDate.getTime());
                    /**
                     * prevent for fails due to time
                     */
                    updatedDate.setHours(23);
                    updatedDate.setMinutes(59);
                    updatedDate.setSeconds(59);
                    return updatedDate;
                });
    }

    private List<OHExceptionMessage> validateAgainstBeginDate(Stream<Date> operationDatesStream, Date beginDate) {
        return operationDatesStream
                .filter(currentRowDate -> (currentRowDate.before(beginDate)))
                .map(currentRowDate -> new OHExceptionMessage(MessageBundle.getMessage("angal.hospital"),
                        MessageBundle.getMessage("angal.admission.invalidoperationdate") + " "
                                + MessageBundle.getMessage("angal.admission.theoperationdatenewerthan") + " "
                                + currentDateFormat.format(beginDate), OHSeverityLevel.ERROR))
                .collect(Collectors.toList());
    }

    private List<OHExceptionMessage> validateAgainstBeginAndEndDate(Stream<Date> operationDatesStream, Date beginDate, Date endDate) {
        return operationDatesStream
                .filter(currentRowDate -> (currentRowDate.before(beginDate) || currentRowDate.after(endDate)))
                .map(currentRowDate -> new OHExceptionMessage(MessageBundle.getMessage("angal.hospital"),
                        MessageBundle.getMessage("angal.admission.invalidoperationdate") + " "
                                + MessageBundle.getMessage("angal.admission.theoperationdatebetween") + " "
                                + currentDateFormat.format(beginDate) + " " + MessageBundle.getMessage("angal.admission.and") + " "
                                + currentDateFormat.format(endDate), OHSeverityLevel.ERROR))
                .collect(Collectors.toList());
    }
}
