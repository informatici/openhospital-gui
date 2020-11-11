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
package org.isf.admission.gui.validation;

import org.isf.admission.model.Admission;
import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.operation.model.OperationRow;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.utils.exception.model.OHSeverityLevel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
