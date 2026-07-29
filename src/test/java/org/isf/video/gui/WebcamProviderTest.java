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
package org.isf.video.gui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

/**
 * The point of the provider is that a platform without a usable capture driver is answered rather
 * than thrown at: the lookup escaping is what stopped the patient form from opening at all.
 * <p>
 * These run on any platform. Where the driver loads they exercise the ordinary path, where it does
 * not - Apple Silicon, or any architecture BridJ does not ship a native for - they exercise the
 * failure path, and neither is allowed to raise.
 */
class WebcamProviderTest {

	@Test
	void theLookupNeverEscapes() {
		assertThatCode(WebcamProvider::getDefault).doesNotThrowAnyException();
		assertThatCode(WebcamProvider::getWebcams).doesNotThrowAnyException();
	}

	@Test
	void repeatingTheLookupKeepsAnswering() {
		WebcamProvider.getDefault();

		// the second call is the one a second patient form makes: once a driver failure has been
		// remembered it must short-circuit, and it must still answer rather than raise
		assertThatCode(WebcamProvider::getDefault).doesNotThrowAnyException();
		assertThat(WebcamProvider.getWebcams()).isNotNull();
	}
}
