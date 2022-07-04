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
package org.isf.utils;

import java.time.format.DateTimeFormatter;

public interface Constants {

	public static final String DATE_FORMAT_DD_MM_YY = "dd/MM/yy";
	public static final String DATE_FORMAT_DD_MM_YYYY = "dd/MM/yyyy";

	public static final String DATE_FORMAT_MM_DD_YY = "MM/dd/yy";
	public static final String DATE_FORMAT_MM_DD_YYYY = "MM/dd/yyyy";

	public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
	public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";

	public static final String DATE_FORMAT_DD_MM_YY_HH_MM = "dd/MM/yy HH:mm";
	public static final String DATE_FORMAT_DD_MM_YY_HH_MM_SS = "dd/MM/yy - HH:mm:ss";

	public static final String DATE_FORMAT_DD_MM_YYYY_HH_MM = "dd/MM/yyyy HH:mm";

	public static final String DATE_FORMAT_DD_MM_YYYY_HH_MM_SS = "dd/MM/yyyy HH:mm:ss";

	public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
	public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

	public static final String TIME_FORMAT_HH_MM = "HH:mm";
	public static final String TIME_FORMAT_HH_MM_SS = "HH:mm:ss";

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY);
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY_HH_MM_SS);

}
