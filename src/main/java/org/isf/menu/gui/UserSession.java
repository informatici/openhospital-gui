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

	public static final int SESSION_TIME = 15000;

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
