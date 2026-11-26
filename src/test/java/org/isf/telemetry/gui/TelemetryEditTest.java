/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
package org.isf.telemetry.gui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.isf.telemetry.envdatacollector.AbstractDataCollector;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

class TelemetryEditTest {

	@Test
	void initialConfigurationSelectsAllCollectorsAndAllowsOptionalDeselection() {
		List<CheckBoxWrapper> checkboxes = buildCheckboxes(Map.of(), true);
		for (CheckBoxWrapper wrapper : checkboxes) {
			assertTrue(wrapper.getCheckbox().isSelected());
			if ("TEL_ID".equals(wrapper.getId())) {
				assertFalse(wrapper.getCheckbox().isEnabled());
			} else {
				assertTrue(wrapper.getCheckbox().isEnabled());
				wrapper.getCheckbox().doClick();
				assertFalse(wrapper.getCheckbox().isSelected());
			}
		}
	}

	@Test
	void savedChoicesAreRestoredAndNewCategoriesAreNotSelected() {
		Map<String, Boolean> consents = Map.of("TEL_ID", true, "TEL_SW", true, "TEL_HW", false);
		List<CheckBoxWrapper> checkboxes = buildCheckboxes(consents, false);
		for (CheckBoxWrapper wrapper : checkboxes) {
			if ("TEL_ID".equals(wrapper.getId()) || "TEL_SW".equals(wrapper.getId())) {
				assertTrue(wrapper.getCheckbox().isSelected());
			} else {
				assertFalse(wrapper.getCheckbox().isSelected());
			}
		}
	}

	@Test
	void existingEmptyConfigurationDoesNotSelectOptionalCollectors() {
		for (CheckBoxWrapper wrapper : buildCheckboxes(Map.of(), false)) {
			if ("TEL_ID".equals(wrapper.getId())) {
				assertTrue(wrapper.getCheckbox().isSelected());
				assertFalse(wrapper.getCheckbox().isEnabled());
			} else {
				assertFalse(wrapper.getCheckbox().isSelected());
			}
		}
	}

	private List<CheckBoxWrapper> buildCheckboxes(Map<String, Boolean> consents, boolean initialConfiguration) {
		ApplicationContext context = mock(ApplicationContext.class);
		when(context.getBeansOfType(AbstractDataCollector.class)).thenReturn(Map.of(
				"id", collector("TEL_ID"), "software", collector("TEL_SW"),
				"hardware", collector("TEL_HW"), "hospital", collector("TEL_OH")));
		return TelemetryEdit.buildPermissionCheckboxes(context, consents, initialConfiguration);
	}

	private AbstractDataCollector collector(String id) {
		return new AbstractDataCollector() {
			@Override
			public String getId() {
				return id;
			}

			@Override
			public String getDescription() {
				return id;
			}

			@Override
			public Map<String, String> retrieveData() {
				throw new AssertionError("Checkbox initialization must not collect telemetry data");
			}
		};
	}
}