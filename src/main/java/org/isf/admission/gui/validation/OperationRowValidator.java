package org.isf.admission.gui.validation;

import org.isf.admission.model.Admission;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.operation.model.OperationRow;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.exception.model.OHSeverityLevel;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OperationRowValidator {

    public final DateTimeFormatter currentDateFormat;

    public OperationRowValidator() {
        currentDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss", new Locale(GeneralData.LANGUAGE));
    }

    @SuppressWarnings("deprecation")
    public List<OHExceptionMessage> checkAllOperationRowDate(List<OperationRow> operationRows, Admission admission) {
        Optional<LocalDateTime> beginDate = Optional.ofNullable(admission.getAdmDate());
        Optional<LocalDateTime> endDate = Optional.ofNullable(admission.getDisDate());
        Stream<LocalDateTime> updatedOperationDatesStream = getUpdatedOperationDates(operationRows);
        if ((beginDate.isPresent()) && (endDate.isPresent())) {
            return validateAgainstBeginAndEndDate(updatedOperationDatesStream, beginDate.get(), endDate.get());
        } else if ((beginDate.isPresent()) && (!endDate.isPresent())) {
            return validateAgainstBeginDate(updatedOperationDatesStream, beginDate.get());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private Stream<LocalDateTime> getUpdatedOperationDates(List<OperationRow> list) {
        return list.stream()
                .map(operationRow -> operationRow.getOpDate())
                .map(currentRowDate -> {
                    /**
                     * prevent for fails due to time
                     */
                    return currentRowDate.toLocalDate()
                            .atTime(23, 59, 59);
                });
    }

    private List<OHExceptionMessage> validateAgainstBeginDate(Stream<LocalDateTime> operationDatesStream, LocalDateTime beginDate) {
        return operationDatesStream
                .filter(currentRowDate -> (currentRowDate.isBefore(beginDate)))
                .map(currentRowDate -> new OHExceptionMessage(MessageBundle.getMessage("angal.hospital"),
                        MessageBundle.getMessage("angal.admission.invalidoperationdate") + " "
                                + MessageBundle.getMessage("angal.admission.theoperationdatenewerthan") + " "
                                + currentDateFormat.format(beginDate), OHSeverityLevel.ERROR))
                .collect(Collectors.toList());
    }

    private List<OHExceptionMessage> validateAgainstBeginAndEndDate(Stream<LocalDateTime> operationDatesStream, LocalDateTime beginDate, LocalDateTime endDate) {
        return operationDatesStream
                .filter(currentRowDate -> (currentRowDate.isBefore(beginDate) || currentRowDate.isAfter(endDate)))
                .map(currentRowDate -> new OHExceptionMessage(MessageBundle.getMessage("angal.hospital"),
                        MessageBundle.getMessage("angal.admission.invalidoperationdate") + " "
                                + MessageBundle.getMessage("angal.admission.theoperationdatebetween") + " "
                                + currentDateFormat.format(beginDate) + " " + MessageBundle.getMessage("angal.admission.and") + " "
                                + currentDateFormat.format(endDate), OHSeverityLevel.ERROR))
                .collect(Collectors.toList());
    }
}
