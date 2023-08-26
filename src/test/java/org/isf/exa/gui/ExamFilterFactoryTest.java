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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.swing.RowFilter;

import org.junit.jupiter.api.Test;

class ExamFilterFactoryTest {

	private ExamFilterFactory examFilterFactory = new ExamFilterFactory();

	@Test
	void shouldBuildFiltersForAllWordsProvided() {
		// given:
		String text = "mkbewe mokebe testo";

		// when:
		List<RowFilter<Object, Object>> rowFilters = examFilterFactory.buildFilters(text);

		// then
		assertThat(rowFilters).hasSize(3);
	}

	@Test
	void shouldReturnEmptyForEmptyText() {
		// given:
		String text = "";

		// when:
		List<RowFilter<Object, Object>> rowFilters = examFilterFactory.buildFilters(text);

		// then
		assertThat(rowFilters).isEmpty();
	}

	@Test
	void shouldReturnEmptyForBadPattern() {
		// given:
		String text = "*hgf*";

		// when:
		List<RowFilter<Object, Object>> rowFilters = examFilterFactory.buildFilters(text);

		// then
		assertThat(rowFilters).isEmpty();
	}

}