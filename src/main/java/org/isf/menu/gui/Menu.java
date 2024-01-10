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
package org.isf.menu.gui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.generaldata.Version;
import org.isf.menu.manager.Context;
import org.isf.utils.jobjects.WaitCursorEventQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Menu {

	private static final Logger LOGGER = LoggerFactory.getLogger(Menu.class);

	private static final Pattern DELIMITER = Pattern.compile("[._\\-+]");
	private static final String MIN_JAVA_VERSION = "11";

	/**
	 * Create the GUI and show it.
	 */
	private static void createAndShowGUI() {
		String newLine = System.lineSeparator();
		LOGGER.info("{}{}====================={}Starting Open Hospital{}====================={}", newLine, newLine, newLine, newLine, newLine);
		checkOHVersion();
		checkJavaVersion();
		fontChecker();
		JFrame.setDefaultLookAndFeelDecorated(false);
		new SplashWindow3("rsc" + File.separator + "images" + File.separator + "splash.png", null, 3000);
		WaitCursorEventQueue waitQueue = new WaitCursorEventQueue(10, Toolkit.getDefaultToolkit().getSystemEventQueue());
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(waitQueue);
	}

	private static void checkOHVersion() {
		Version.initialize();
		LOGGER.info("Open Hospital version {}.{}.{}", Version.VER_MAJOR, Version.VER_MINOR, Version.VER_RELEASE);
	}

	public static void checkJavaVersion() {
		String version = System.getProperty("java.version");
		LOGGER.info("Java version {}", version);
		if (!isAtLeastVersion(MIN_JAVA_VERSION)) {
			LOGGER.error("Java version {} or higher is required.", MIN_JAVA_VERSION);
			String newLine = System.lineSeparator();
			LOGGER.info("{}{}====================={} Open Hospital closed {}====================={}", newLine, newLine, newLine, newLine, newLine);
			System.exit(1);
		}
	}

	/**
	 * A very optimistic test for ensuring we at least have a minimal required Java version. It will not fail when we
	 * cannot determine the result. In essence, this method splits a version string using {@link
	 * Menu#DELIMITER} and compares two version number by number.
	 *
	 * @param requiredVersion Should be in the form X.X.X_XXX where X are integers.
	 * @return true if the numbers in version available for comparison are all greater-equals the currently running Java
	 * version.
	 */
	public static boolean isAtLeastVersion(String requiredVersion) {
		String runningVersion = System.getProperty("java.version");
		if (runningVersion == null || requiredVersion == null) {
			return true;
		}
		try (Scanner scannerRunningVersion = new Scanner(runningVersion); Scanner scannerRequiredVersion = new Scanner(requiredVersion)) {
			scannerRunningVersion.useDelimiter(DELIMITER);
			scannerRequiredVersion.useDelimiter(DELIMITER);
			while (scannerRunningVersion.hasNextInt() && scannerRequiredVersion.hasNextInt()) {
				int running = scannerRunningVersion.nextInt();
				int required = scannerRequiredVersion.nextInt();
				if (running == required) {
					continue;
				}
				return running >= required;
			}
			return true;
		}
	}

	public static void setUIFont(FontUIResource f) {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				UIManager.put(key, f);
			}
		}
	}

	public static void fontChecker() {

		GeneralData.initialize();
		MessageBundle.getBundle();
		String textToCheck = MessageBundle.getMessage("angal.common.address.txt"); // Any string from bundle to check
		LOGGER.debug("textToCheck: {}", textToCheck);

		Font currentFont = UIManager.getFont("Label.font");

		if (currentFont != null) {
			LOGGER.debug("Current Font: {} {}", currentFont.getFontName(), currentFont.getSize());
		} else {
			LOGGER.debug("Unable to retrieve the current font from the L&F.");
			return;
		}

		// Check if the currentFont can display the textToCheck
		if (currentFont.canDisplayUpTo(textToCheck) == -1) {
			LOGGER.debug("The current font supports the selected language.");
		} else {
			LOGGER.debug("The current font does not support the selected language.");

			// Find a font that supports the provided textToCheck
			Font[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
			for (Font font : availableFonts) {
				if (font.canDisplayUpTo(textToCheck) == -1) {
					LOGGER.debug("Found a font that supports the selected language: {}", font.getFontName());
					setUIFont(new FontUIResource(font.getFontName(), Font.PLAIN, 12));
					return;
				}
			}
			LOGGER.error("Unable to find a font that supports the selected language.");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		ApplicationContext context = null;
		try {
			context = new ClassPathXmlApplicationContext("applicationContext.xml");
		} catch (Exception e) {
			LOGGER.error("Fatal: fail to load application context. {}", e.getMessage(), e);
			System.exit(1);
		}
		Context.setApplicationContext(context);
		SwingUtilities.invokeLater(() -> createAndShowGUI());
	}
}
