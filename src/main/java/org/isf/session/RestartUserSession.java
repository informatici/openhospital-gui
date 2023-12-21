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

import java.awt.Window;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.isf.menu.gui.Login;
import org.isf.menu.gui.MainMenu;
import org.isf.sessionaudit.model.UserSession;

public class RestartUserSession extends UserSession {
	
	private static final int LOGIN_FAILED = 2;
	
	public static void restartSession() {
		List<Window> windows = Arrays.asList(Window.getWindows());
		Runnable waitRunner = () -> {
			try {
				SwingUtilities.invokeAndWait(() -> {

					UserSession.removeUser();

					JFrame tmpJFrame = new JFrame();

					windows.forEach(win -> {
						win.dispose();
					});

					new Login(tmpJFrame);

					if (!UserSession.isLoggedIn()) {
						System.exit(LOGIN_FAILED);
					}

					tmpJFrame.dispose();
					
					new MainMenu(getUser());
					getTimer().startTimer();
					LOGGER.debug("Session refreshed...");

				});
			} catch (Exception exception) {
				LOGGER.error(exception.getMessage(), exception);
				// can catch InvocationTargetException
				// can catch InterruptedException
			}
		};

		Thread newThread = new Thread(waitRunner, "newSession");
		newThread.start();
	}

}