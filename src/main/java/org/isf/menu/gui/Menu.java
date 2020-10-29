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

	private final static float MIN_JAVA_VERSION = (float) 1.8;

	/**
	 * Create the GUI and show it.
	 */
	private static void createAndShowGUI() {
		logger = LoggerFactory.getLogger(Menu.class);
		logger.info("\n\n=====================\nStarting OpenHospital\n=====================\n");
		checkOHVersion();
		checkJavaVersion();
		JFrame.setDefaultLookAndFeelDecorated(false);
		new SplashWindow3("rsc" + File.separator + "images" + File.separator + "Splash.jpg", null, 3000);
		WaitCursorEventQueue waitQueue = new WaitCursorEventQueue(10, Toolkit.getDefaultToolkit().getSystemEventQueue());
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(waitQueue);
	}

	private static void checkOHVersion() {
		Version.getVersion();
		logger.info("OpenHospital version {}.{}.{}", Version.VER_MAJOR, Version.VER_MINOR, Version.VER_RELEASE);

	}

	public static void checkJavaVersion() {
		String version = System.getProperty("java.version");
		logger.info("Java version {}", version);
		Float f = Float.valueOf(version.substring(0, 3));
		if (f.floatValue() < MIN_JAVA_VERSION) {
			logger.error("Java version {} or higher is required.", MIN_JAVA_VERSION);
			logger.info("\n\n=====================\n OpenHospital closed \n=====================\n");
			System.exit(1);
		}
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
