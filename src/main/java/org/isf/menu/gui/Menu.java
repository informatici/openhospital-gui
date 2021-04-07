/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

import java.awt.Toolkit;
import java.io.File;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.apache.log4j.PropertyConfigurator;
import org.isf.generaldata.Version;
import org.isf.menu.manager.Context;
import org.isf.utils.jobjects.WaitCursorEventQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Menu {

	private static Logger logger = LoggerFactory.getLogger(Menu.class);

	private static final Pattern DELIMITER = Pattern.compile("[._\\-+]");
	private static final String MIN_JAVA_VERSION = "1.8";

	/**
	 * Create the GUI and show it.
	 */
	private static void createAndShowGUI() {
		logger = LoggerFactory.getLogger(Menu.class);
		logger.info("\n\n=====================\nStarting Open Hospital\n=====================\n");
		checkOHVersion();
		checkJavaVersion();
		JFrame.setDefaultLookAndFeelDecorated(false);
		new SplashWindow3("rsc" + File.separator + "images" + File.separator + "Splash.jpg", null, 3000);
		WaitCursorEventQueue waitQueue = new WaitCursorEventQueue(10, Toolkit.getDefaultToolkit().getSystemEventQueue());
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(waitQueue);
	}

	private static void checkOHVersion() {
		Version.getVersion();
		logger.info("Open Hospital version {}.{}.{}", Version.VER_MAJOR, Version.VER_MINOR, Version.VER_RELEASE);
	}

	public static void checkJavaVersion() {
		String version = System.getProperty("java.version");
		logger.info("Java version {}", version);
		if (!isAtLeastVersion(MIN_JAVA_VERSION)) {
			logger.error("Java version {} or higher is required.", MIN_JAVA_VERSION);
			logger.info("\n\n=====================\n Open Hospital closed \n=====================\n");
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
		Scanner scannerRunningVersion = new Scanner(runningVersion);
		Scanner scannerRequiredVersion = new Scanner(requiredVersion);
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

	public static void main(String[] args) {
		PropertyConfigurator.configure(new File("./rsc/log4j.properties").getAbsolutePath());
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		Context.setApplicationContext(context);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				createAndShowGUI();
			}
		});

	}
}
