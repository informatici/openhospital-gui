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
package org.isf.session;

import java.awt.MouseInfo;
import java.awt.Point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionRefreshTimerRunnable implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SessionRefreshTimerRunnable.class);
	private static final long THREAD_SLEEP_TIME = 5000;

	private double x;
	private double y;

	@Override
	public void run() {
		try {
			while (true) {

				Thread.sleep(THREAD_SLEEP_TIME);

				Point point = MouseInfo.getPointerInfo().getLocation();

				double x = point.getX();
				double y = point.getY();

				if (x != this.x || y != this.y) {
					if (RestartUserSession.getTimer() != null) {
						RestartUserSession.getTimer().startTimer();
						LOGGER.trace("Mouse moved. Session refreshed.");
					}
					this.x = x;
					this.y = y;
				}
			}
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}

	}

}
