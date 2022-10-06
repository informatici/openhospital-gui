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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import org.isf.utils.db.BCrypt;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.MessageDialog;
import org.isf.utils.layout.SpringUtilities;
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
	private User returnUser;
	private boolean usersListLogin;

	public Login(MainMenu parent) {
		super(parent, MessageBundle.getMessage("angal.login.title"), true);

		usersListLogin = GeneralData.getGeneralData().getUSERSLISTLOGIN();
		
		this.parent = parent;

		addLoginListener(parent);

		// add panel to frame
		LoginPanel panel = new LoginPanel(this);
		add(panel);
		pack();

		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screensize = kit.getScreenSize();

		Dimension mySize = getSize();

		setLocation((screensize.width - mySize.width) / 2,
				(screensize.height - mySize.height) / 2);

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
		for (User u : users) {
			if (u.getUserName().equals(userName) && BCrypt.checkpw(passwd, u.getPasswd())) {
				returnUser = u;
				found = true;
			}
		}
		if (!found) {
			LOGGER.warn("Login failed: {}", MessageBundle.getMessage("angal.login.passwordisincorrectpleaseretry.msg"));
			MessageDialog.error(this, "angal.login.passwordisincorrectpleaseretry.msg");
			pwd.setText("");
			pwd.grabFocus();
		} else {
			fireLoginInserted(returnUser);
			removeLoginListener(parent);
			dispose();
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

			submit.addActionListener(myFrame);
			submit.setName("submit");
			submit.addKeyListener(myFrame);
			cancel.addActionListener(myFrame);
			cancel.setName("cancel");
			cancel.addKeyListener(myFrame);
		}
	}
}
