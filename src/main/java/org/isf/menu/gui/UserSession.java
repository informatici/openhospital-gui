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
package org.isf.menu.gui;

import java.awt.Window;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.isf.menu.model.User;
import org.isf.utils.jobjects.DelayTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSession {

	private static final String MAIN_MENU = "mainMenu";
	private static final String LOGOUT_TIMER = "logoutTimer";
	private static final String USER = "user";

	private static Map<String, Object> map = new HashMap<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(UserSession.class);

	public static MainMenu getMainMenu() {
		return (MainMenu) map.get(MAIN_MENU);
	}

	public static void setMainMenu(JFrame mainMenu) {
		map.put(MAIN_MENU, mainMenu);
	}

	public static DelayTimer getTimer() {
		return (DelayTimer) map.get(LOGOUT_TIMER);
	}

	public static void setTimer(DelayTimer logoutTimer) {
		map.put(LOGOUT_TIMER, logoutTimer);
	}

	public static void setUser(User myUser) {
		map.put(USER, myUser);

	}

	public static boolean isLoggedIn() {
		return map.get(USER) != null;
	}

	public static void removeUser() {
		map.remove(USER);
	}

	public static void restartSession() {
		JFrame tmp = getMainMenu();
		removeUser();
		getMainMenu().clearUser();
		List<Window> windows = Arrays.asList(Window.getWindows());
		Runnable waitRunner = () -> {
			try {
				SwingUtilities.invokeAndWait(() -> {
					MainMenu mainMenu = new MainMenu();
					setMainMenu(mainMenu);
					windows.forEach(win -> win.dispose());
				});
			} catch (Exception exception) {
				LOGGER.error(exception.getMessage(), exception);
				// can catch InvocationTargetException
				// can catch InterruptedException
			}
		};

		Thread newThread = new Thread(waitRunner, "Logout");
		newThread.start();
	}

}
