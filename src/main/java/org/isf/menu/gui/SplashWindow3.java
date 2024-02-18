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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.isf.generaldata.GeneralData;
import org.isf.session.LogoutEventListener;
import org.isf.session.RestartUserSession;
import org.isf.session.SessionRefreshTimerRunnable;
import org.isf.utils.time.DelayTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SplashWindow3 extends JWindow {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SplashWindow3.class);

	public SplashWindow3(String filename, Frame f, int waitTime) {
		super(f);

		JLabel l = new JLabel(new ImageIcon(filename));

		getContentPane().add(l, BorderLayout.CENTER);
		pack();

		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();

		Dimension labelSize = l.getPreferredSize();
		setLocation(screenSize.width / 2 - (labelSize.width / 2), screenSize.height / 2 - (labelSize.height / 2));

		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				setVisible(false);
				dispose();
			}
		});
		final int pause = waitTime;
		final Runnable closerRunner = () -> {
			setVisible(false);
			dispose();
			MainMenu mainMenu = new MainMenu(null);
			startLogoutTimer(mainMenu);
		};
		Runnable waitRunner = () -> {
			try {
				Thread.sleep(pause);
				SwingUtilities.invokeAndWait(closerRunner);
			} catch (Exception exception) {
				LOGGER.error(exception.getMessage(), exception);
				// can catch InvocationTargetException
				// can catch InterruptedException
			}
		};
		setVisible(true);
		Thread splashThread = new Thread(waitRunner, "SplashThread");
		splashThread.start();
	}

	private void startLogoutTimer(MainMenu mainMenu) {
		new Thread(new SessionRefreshTimerRunnable()).start();
		if (RestartUserSession.getTimer() != null) {
			RestartUserSession.getTimer().quit();
		}
		RestartUserSession.setTimer(new DelayTimer(new LogoutEventListener(), GeneralData.SESSIONTIMEOUT * 1000 * 60));
		RestartUserSession.getTimer().startTimer();
	}

}