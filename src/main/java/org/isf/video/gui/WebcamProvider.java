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

import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

/**
 * Access to the capture devices, guarding the calls that can fail on a platform the capture driver
 * does not support.
 * <p>
 * Enabling the video module states that the user wants a webcam, not that the platform can provide
 * one: the driver loads a native library (BridJ) that ships for a limited set of architectures, and
 * on the others the very first lookup fails at run time. Left to escape, that failure takes down
 * whatever was being built at the time - the patient form could not be opened at all.
 * <p>
 * A failure of that kind is permanent for the life of the process: the native library will not
 * appear later, and once its class has failed to initialize every further call fails again. It is
 * therefore remembered, so the cost and the stack trace are paid once rather than on every patient.
 * An ordinary absence of devices is not remembered: {@code null} simply means none is connected
 * right now, and a webcam plugged in after startup is still found.
 */
public final class WebcamProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebcamProvider.class);

	/** Set when the capture driver has failed to load; never cleared. */
	private static volatile boolean driverUnavailable;

	private WebcamProvider() {
	}

	/**
	 * The default webcam, or {@code null} when the platform cannot provide one.
	 */
	public static Webcam getDefault() {
		return guarded(Webcam::getDefault, null);
	}

	/**
	 * The available webcams, or an empty list when the platform cannot provide any.
	 */
	public static List<Webcam> getWebcams() {
		return guarded(Webcam::getWebcams, List.of());
	}

	private static <T> T guarded(Supplier<T> lookup, T whenUnavailable) {
		if (driverUnavailable) {
			return whenUnavailable;
		}
		try {
			return lookup.get();
		} catch (RuntimeException | LinkageError e) {
			driverUnavailable = true;
			LOGGER.warn("No usable webcam on this platform: taking the photo from a file instead.", e);
			return whenUnavailable;
		}
	}

}
