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
package org.isf.exa.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.swing.RowFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExamFilterFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExamFilterFactory.class);

	public List<RowFilter<Object, Object>> buildFilters(String s) {
		try {
			return Arrays.stream(s.split(" "))
					.filter(token -> !token.isEmpty())
					.map(String::toLowerCase)
					.map(token -> RowFilter.regexFilter("(?i)" + token))
					.collect(Collectors.toList());
		} catch (PatternSyntaxException pse) {
			LOGGER.info("Bad regex pattern");
			return Collections.emptyList();
		}
	}

}
