package org.isf.session;

import java.awt.AWTEvent;

import org.isf.menu.gui.Login;
import org.isf.menu.model.User;

public class LoginEventListener implements Login.LoginListener {

	@Override
	public void loginInserted(AWTEvent e) {
		if (e.getSource() instanceof User) {
			User myUser = (User) e.getSource();
			UserSession.setUser(myUser);
			UserSession.getTimer().startTimer();
		}

	}

}
