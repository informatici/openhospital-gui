package org.isf.utils.jobjects;

import com.toedter.calendar.IDateEditor;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;
import org.isf.utils.time.Converters;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

public class LocalDateSupportingJDateChooser extends JDateChooser {
    public LocalDateSupportingJDateChooser(Date date, String dateFormatString) {
        super(date, dateFormatString);
    }

    public LocalDateSupportingJDateChooser(Date date) {
        super(date);
    }

    public LocalDateSupportingJDateChooser(IDateEditor dateEditor) {
        super(dateEditor);
    }

    public LocalDateSupportingJDateChooser() {
    }

    public LocalDateSupportingJDateChooser(Date date, String dateFormatString, IDateEditor dateEditor) {

    }

    public LocalDateSupportingJDateChooser(String datePattern, String maskPattern, char placeholder) {

    }

    public LocalDateSupportingJDateChooser(JCalendar jcal, Date date, String dateFormatString, IDateEditor dateEditor) {

    }

    public LocalDateSupportingJDateChooser(LocalDateTime date, String dateFormatString) {
        this(Converters.toDate(date), dateFormatString);
    }

    public LocalDateSupportingJDateChooser(LocalDateTime date) {
        this(Converters.toDate(date));
    }

    public void setDate(LocalDateTime localDate) {
        setDate(Converters.toDate(localDate));
    }

    public LocalDateTime getLocalDateTime() {
        return Optional.ofNullable(getDate())
                .map(Converters::convertToLocalDateTime)
                .orElse(null);
    }
}
