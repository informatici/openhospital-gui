package org.isf.menu.gui;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import org.isf.generaldata.GeneralData;
import org.isf.menu.model.User;
import org.isf.utils.jobjects.DelayTimerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutEventListener implements AWTEventListener, Login.LoginListener, DelayTimerCallback {

	@Override
	public void eventDispatched(AWTEvent e) {
		if (!GeneralData.getGeneralData().getSINGLEUSER() && e instanceof MouseEvent) {
			MouseEvent me = (MouseEvent) e;
			if (Arrays.asList(MouseEvent.MOUSE_MOVED).contains(me.getID())) {
				if (UserSession.getTimer() != null) {
					UserSession.getTimer().startTimer();
				}
			}
		} else if (!GeneralData.getGeneralData().getSINGLEUSER() && e instanceof KeyEvent) {
			KeyEvent key = (KeyEvent) e;
			if (Arrays.asList(KeyEvent.KEY_PRESSED).contains(key.getID())) {
				if (UserSession.getTimer() != null) {
					UserSession.getTimer().startTimer();
				}
			}
		}

	}

	@Override
	public void loginInserted(AWTEvent e) {
		if (e.getSource() instanceof User) {
			User myUser = (User) e.getSource();
			UserSession.setUser(myUser);
			UserSession.getTimer().startTimer();
		}

	}

	@Override
	public void trigger() {
		if (!GeneralData.getGeneralData().getSINGLEUSER() && UserSession.isLoggedIn()) {
				UserSession.restartSession();
				UserSession.getTimer().startTimer();
				System.out.println("RESTARTED...");
		}

	}

}
