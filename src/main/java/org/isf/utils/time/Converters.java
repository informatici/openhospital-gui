/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2021 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.utils.time;

import static org.isf.utils.Constants.DATE_FORMAT_YYYY_MM_DD;
import static org.isf.utils.Constants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import org.joda.time.DateTime;

/**
 * @author nicosalvato on 2016-08-25.
 * Contact: nicosalvato@gmail.com
 */
public class Converters {

    public static LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(ldt -> ldt.atZone(ZoneId.systemDefault()).toInstant())
                .map(Date::from)
                .orElse(null);
    }

    /**
     * Returns a {@link String} representing the date in format {@code yyyy-MM-dd HH:mm:ss}.
     * @param datetime {@link GregorianCalendar} object.
     * @return the date in format {@code yyyy-MM-dd HH:mm:ss}.
     */
    public static String convertToSQLDate(GregorianCalendar datetime) {
        if (datetime == null) {
            return null;
        }
        return convertToSQLDate(datetime.getTime());
    }

    /**
     * Returns a {@link String} representing the date in format {@code yyyy-MM-dd HH:mm:ss}.
     * @param datetime {@link Date} input.
     * @return the date in format {@code yyyy-MM-dd HH:mm:ss}.
     */
    public static String convertToSQLDate(Date datetime) {
        if (datetime == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
        return simpleDateFormat.format(datetime);
    }

    /**
     * Returns a {@link String} representing the date in format {@code yyyy-MM-dd}.
     * @param date {@link Date} object.
     * @return the date in format {@code yyyy-MM-dd}.
     */
    public static String convertToSQLDateLimited(GregorianCalendar date) {
        if (date == null) {
            return null;
        }
        return convertToSQLDateLimited(date.getTime());
    }

    /**
     * Returns a {@link String} representing the date in format {@code yyyy-MM-dd}.
     * @param date {@link Date} object.
     * @return the date in format {@code yyyy-MM-dd}.
     */
    public static String convertToSQLDateLimited(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_YYYY_MM_DD);
        return simpleDateFormat.format(date);
    }

    /**
     * Converts a {@link GregorianCalendar} to a {@link Date}.
     * @param calendar the calendar to convert.
     * @return the converted value or {@code null} if the passed value is {@code null}.
     */
    public static Date toDate(GregorianCalendar calendar) {
        if (calendar == null) {
            return null;
        }
        return new Date(calendar.getTimeInMillis());
    }
    
    public static DateTime toDateTime(GregorianCalendar calendar) {
        if (calendar == null) {
            return null;
        }
        return new DateTime(calendar.getTimeInMillis());
    }

    /**
     * Converts the specified {@link java.sql.Date} to a {@link GregorianCalendar}.
     * @param date the date to convert.
     * @return the converted date.
     */
    public static GregorianCalendar toCalendar(Date date) {
        if (date == null) {
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar;
    }

    public static GregorianCalendar toCalendar(LocalDateTime localDateTime) {
        return toCalendar(toDate(localDateTime));
    }

    public static LocalDateTime convertToLocalDateTime(Calendar gregorianCalendar) {
        return convertToLocalDateTime(toDate((GregorianCalendar) gregorianCalendar));
    }

    public static LocalDate parseStringToLocalDate(String dateStr, String dateFormat) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(dateFormat));
    }

    public static LocalDateTime parseStringToLocalDateTime(String dateStr, String dateFormat) {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(dateFormat));
    }

}
