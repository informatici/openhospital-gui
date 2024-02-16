/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2024 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.time.Duration;
import java.util.EventListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.EventListenerList;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.session.RestartUserSession;
import org.isf.utils.db.BCrypt;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.file.FileTools;
import org.isf.utils.jobjects.JLabelInfo;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.layout.SpringUtilities;
import org.isf.utils.time.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Login extends JDialog implements ActionListener, KeyListener {

	private static final long serialVersionUID = 76205822226035164L;

	private static final Logger LOGGER = LoggerFactory.getLogger(Login.class);
	private static final String CANCEL_BTN = MessageBundle.getMessage("angal.common.cancel.btn");
	private static final String SUBMIT_BTN = MessageBundle.getMessage("angal.common.submit.btn");

	private EventListenerList loginListeners = new EventListenerList();

	private UserBrowsingManager userBrowsingManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);

	public interface LoginListener extends EventListener {

		void loginInserted(AWTEvent e);
	}

	public void addLoginListener(LoginListener listener) {
		loginListeners.add(LoginListener.class, listener);
	}

	public void removeLoginListener(LoginListener listener) {
		loginListeners.remove(LoginListener.class, listener);
	}

	private void fireLoginInserted(User aUser) {
		AWTEvent event = new AWTEvent(aUser, AWTEvent.RESERVED_ID_MAX + 1) {

			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = loginListeners.getListeners(LoginListener.class);
		for (EventListener listener : listeners) {
			((LoginListener) listener).loginInserted(event);
		}
	}

	@Override
	public void keyPressed(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			String source = event.getComponent().getName();
			if ("pwd".equalsIgnoreCase(source)) {
				acceptPwd();
			} else if ("submit".equalsIgnoreCase(source)) {
				acceptPwd();
			} else if ("cancel".equalsIgnoreCase(source)) {
				clearText();
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent event) {
	}

	@Override
	public void keyReleased(KeyEvent event) {
	}

	private List<User> users;
	private JComboBox<String> usersList;
	protected JTextField login;
	private JPasswordField pwd;
	private MainMenu parent;
	private boolean usersListLogin;

	public Login(JFrame parent) {
		super(parent, MessageBundle.getMessage("angal.login.title"), true);

		usersListLogin = GeneralData.getGeneralData().getUSERSLISTLOGIN();

		// add panel to frame
		LoginPanel panel = new LoginPanel(this);
		add(panel);
		pack();

		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	public Login(JFrame hiddenFrame, MainMenu parent) {
		super(hiddenFrame, MessageBundle.getMessage("angal.login.title"), true);

		usersListLogin = GeneralData.getGeneralData().getUSERSLISTLOGIN();

		this.parent = parent;

		addLoginListener(parent);

		// add panel to frame
		LoginPanel panel = new LoginPanel(this);
		add(panel);
		pack();

		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	private void clearText() {
		pwd.setText("");
	}

	private void acceptPwd() {
		String userName = usersListLogin ? (String) usersList.getSelectedItem() : login.getText();
		String passwd = new String(pwd.getPassword());
		boolean found = false;
		User user = null;
		try {
			for (User u : users) {
				if (u.getUserName().equals(userName)) {
					user = userBrowsingManager.getUserByName(u.getUserName());
					// is this login within the idle time set (if any)?
					if (GeneralData.PASSWORDIDLE > 0 && user.getLastLogin() != null) {
						if (user.getLastLogin().plusDays(GeneralData.PASSWORDIDLE).isBefore(TimeTools.getNow())) {
							userBrowsingManager.lockUser(user);
							MessageDialog.error(this, "angal.login.accounthasnotbeenusedindayscontacttheadministrator.fmt.msg", GeneralData.PASSWORDIDLE);
							pwd.setText("");
							pwd.grabFocus();
							return;
						}
					}
					if (user.isAccountLocked()) {
						boolean isUnlocked = userBrowsingManager.unlockWhenTimeExpired(user);
						if (!isUnlocked) {
							Duration duration = Duration.between(TimeTools.getNow(), user.getLockedTime().plusMinutes(GeneralData.PASSWORDLOCKTIME));
							MessageDialog.error(this, "angal.login.accountisstilllockedformoreminutes.fmt.msg", duration.toMinutes());
							pwd.setText("");
							pwd.grabFocus();
							return;
						}
					}
					if (BCrypt.checkpw(passwd, u.getPasswd())) {
						found = true;
					}
					break;
				}
			}
			if (found) {
				userBrowsingManager.setLastLogin(user);
				// good PW, so reset failed attempts if there are any
				if (user.getFailedAttempts() > 0) {
					userBrowsingManager.resetFailedAttempts(user);
				}
				fireLoginInserted(user);
				removeLoginListener(parent);
				RestartUserSession.setUser(user);
				dispose();
				return;
			}
			LOGGER.warn("Login failed: {}", MessageBundle.getMessage("angal.login.passwordisincorrectpleaseretry.msg"));
			MessageDialog.error(this, "angal.login.passwordisincorrectpleaseretry.msg");
			pwd.setText("");
			pwd.grabFocus();
			// Can't lock an account that doesn't exist
			if (user == null) {
				return;
			}
			if (user.isAccountLocked()) {
				MessageDialog.error(this, "angal.login.accountislocked.msg");
				return;
			}
			userBrowsingManager.increaseFailedAttempts(user);
			if (GeneralData.PASSWORDTRIES != 0) {
				user.setFailedAttempts(user.getFailedAttempts() + 1);
				if (user.getFailedAttempts() >= GeneralData.PASSWORDTRIES) {
					userBrowsingManager.lockUser(user);
					MessageDialog.error(this, "angal.login.accountisnowlockedforminutes.fmt.msg", GeneralData.PASSWORDLOCKTIME);
				}
			}
		} catch (OHServiceException e1) {
			LOGGER.error("Error while logging in user: {}. Exiting.", userName);
			OHServiceExceptionUtil.showMessages(e1);
			System.exit(1);
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String command = actionEvent.getActionCommand();
		if (command.equals(CANCEL_BTN)) {
			LOGGER.warn("Login cancelled.");
			dispose();
		} else if (command.equals(SUBMIT_BTN)) {
			acceptPwd();
		}
	}

	private class LoginPanel extends JPanel {

		private static final String DEFAULT_CREDENTIALS_PROPERTIES = "default_credentials.properties";
		private static final long serialVersionUID = 4338749100444551874L;

		public LoginPanel(Login myFrame) {

			try {
				users = userBrowsingManager.getUser();
			} catch (OHServiceException e1) {
				LOGGER.error("Exiting.");
				OHServiceExceptionUtil.showMessages(e1);
				System.exit(1);
			}

			if (usersListLogin) {
				usersList = new JComboBox<>();
				for (User u : users) {
					usersList.addItem(u.getUserName());
				}

				Dimension preferredSize = usersList.getPreferredSize();
				usersList.setPreferredSize(new Dimension(120, preferredSize.height));
			} else {
				login = new JTextField();
				Dimension preferredSize = login.getPreferredSize();
				login.setPreferredSize(new Dimension(120, preferredSize.height));
			}

			pwd = new JPasswordField(25);
			pwd.setName("pwd");
			pwd.addKeyListener(myFrame);

			JButton submit = new JButton(SUBMIT_BTN);
			submit.setMnemonic(MessageBundle.getMnemonic("angal.common.submit.btn.key"));
			JButton cancel = new JButton(CANCEL_BTN);
			cancel.setMnemonic(MessageBundle.getMnemonic("angal.common.cancel.btn.key"));

			JPanel body = new JPanel(new SpringLayout());
			body.add(new JLabel(MessageBundle.getMessage("angal.common.userid.label")));
			body.add(usersListLogin ? usersList : login);
			body.add(new JLabel(MessageBundle.getMessage("angal.login.password.label")));
			body.add(pwd);
			SpringUtilities.makeCompactGrid(body,
							2, 2,
							5, 5,
							5, 5);

			JPanel buttons = new JPanel();
			buttons.setLayout(new FlowLayout());
			buttons.add(submit);
			buttons.add(cancel);

			setLayout(new BorderLayout(10, 10));
			add(body, BorderLayout.NORTH);
			add(buttons, BorderLayout.SOUTH);

			File credentialProperties = FileTools.getFile(DEFAULT_CREDENTIALS_PROPERTIES);
			if (credentialProperties != null) {
				String tooltip = FileTools.readFileToStringLineByLine(credentialProperties.getAbsolutePath(), true);
				JLabel infoButton = new JLabelInfo(new ImageIcon("rsc/icons/info_button.png"), tooltip, Color.white);

				JPanel infoPanel = new JPanel();
				infoPanel.setLayout(new FlowLayout());
				infoPanel.add(infoButton, BorderLayout.CENTER);
				add(infoPanel, BorderLayout.WEST);
			}

			submit.addActionListener(myFrame);
			submit.setName("submit");
			submit.addKeyListener(myFrame);
			cancel.addActionListener(myFrame);
			cancel.setName("cancel");
			cancel.addKeyListener(myFrame);
		}
	}
}
