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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.menu.model.UserGroup;
import org.isf.menu.model.UserMenuItem;
import org.isf.sms.service.SmsSender;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.utils.jobjects.ModalJFrame;
import org.isf.utils.layout.SpringUtilities;
import org.isf.xmpp.gui.CommunicationFrame;
import org.isf.xmpp.service.Server;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class MainMenu extends JFrame implements ActionListener, Login.LoginListener, SubMenu.CommandListener {

	private static final long serialVersionUID = 7620582079916035164L;
	public static final String ADMIN_STR = "admin";
	private boolean flag_Xmpp;
	private boolean flag_Sms;

	private static final Logger LOGGER = LoggerFactory.getLogger(MainMenu.class);

	@Override
	public void loginInserted(AWTEvent e) {
		if (e.getSource() instanceof User) {
			myUser = (User) e.getSource();
			MDC.put("OHUser", myUser.getUserName());
			MDC.put("OHUserGroup", myUser.getUserGroupName().getCode());
			LOGGER.info("Logging: \"{}\" user has logged into the system.", myUser.getUserName());
		}
	}

	@Override
	public void commandInserted(AWTEvent e) {
		if (e.getSource() instanceof String) {
			launchApp((String) e.getSource());
		}
	}

	public static boolean checkUserGrants(String code) {

		for (UserMenuItem umi : myMenu) {
			if (umi.getCode().equalsIgnoreCase(code)) {
				return true;
			}
		}
		return false;
	}

	private int minButtonSize;

	public void setMinButtonSize(int value) {
		minButtonSize = value;
	}

	public int getMinButtonSize() {
		return minButtonSize;
	}

	private static User myUser;
	private static ArrayList<UserMenuItem> myMenu;

	final int menuXPosition = 10;
	final int menuYDisplacement = 75;

	// singleUser=true : one user
	private boolean singleUser;
	// internalPharmacies=false : no internalPharmacies
	private boolean internalPharmacies;
	// debug mode
	private boolean debug;
	private MainMenu myFrame;

	private UserBrowsingManager manager = Context.getApplicationContext().getBean(UserBrowsingManager.class);

	public MainMenu() {
		myFrame = this;
		GeneralData.initialize();
		Locale.setDefault(new Locale(GeneralData.LANGUAGE)); //for all fixed options YES_NO_CANCEL in dialogs
		singleUser = GeneralData.getGeneralData().getSINGLEUSER();
		MessageBundle.getBundle();
		try {
			internalPharmacies = GeneralData.INTERNALPHARMACIES;
			debug = GeneralData.DEBUG;
			if (debug) {
				LOGGER.info("Debug: OpenHospital in debug mode.");
			}
			flag_Xmpp = GeneralData.XMPPMODULEENABLED;
			flag_Sms = GeneralData.SMSENABLED;
			// start connection with SMS service
			if (flag_Sms) {
				Thread thread = new Thread(new SmsSender());
				thread.start();
			}
		} catch (Exception e) {
			singleUser = true; // default for property not found
			internalPharmacies = false; // default for property not found
			debug = false; // default for property not found
		}

		if (singleUser) {
			LOGGER.info("Logging: Single User mode.");
			myUser = new User(ADMIN_STR, new UserGroup(ADMIN_STR, ""), ADMIN_STR, "");
			MDC.put("OHUser", myUser.getUserName());
			MDC.put("OHUserGroup", myUser.getUserGroupName().getCode());
		} else {
			// get an user
			LOGGER.info("Logging: Multi User mode.");
			new Login(this);

			if (null == myUser) {
				// Login failed
				actionExit(2);
			}
		}

		// get menu items
		try {
			myMenu = manager.getMenu(myUser);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}

		// start connection with xmpp server if is enabled
		if (flag_Xmpp) {
			try {
				Server.getInstance().login(myUser.getUserName(), myUser.getPasswd());
				try {
					Thread.sleep(500);
				} catch (InterruptedException interruptedException) {
					LOGGER.error(interruptedException.getMessage(), interruptedException);
				}
				new CommunicationFrame();
				/*
				 * Interaction communication= new Interaction();
				 * communication.incomingChat(); communication.receiveFile();
				 */
			} catch (XMPPException e) {
				String message = e.getMessage();
				if (message.contains("SASL authentication DIGEST-MD5 failed")) {
					if (ADMIN_STR.equals(myUser.getUserName())) {
						LOGGER.error("Cannot use \"admin\" user, please consider creating another user under the admin group.");
					} else {
						LOGGER.error("Passwords do not match, please drop the XMPP user and login to OH again with the same user.");
					}
				} else if (message.contains("XMPPError connecting")) {
					LOGGER.error("No XMPP Server seems to be running: set XMPPMODULEENABLED = false");
				} else {
					LOGGER.error("An error occurs: {}", e.getMessage());
				}
				flag_Xmpp = GeneralData.XMPPMODULEENABLED = false;
			}

		}

		// if in singleUser mode remove "users" and "communication" menu
		if (singleUser) {
			ArrayList<UserMenuItem> junkMenu = new ArrayList<>();
			for (UserMenuItem umi : myMenu) {
				if ("USERS".equalsIgnoreCase(umi.getCode()) || "USERS".equalsIgnoreCase(umi.getMySubmenu())) {
					junkMenu.add(umi);
				}
				if ("communication".equalsIgnoreCase(umi.getCode())) {
					if (flag_Xmpp) {
						LOGGER.info("Single user mode: set XMPPMODULEENABLED = false");
						flag_Xmpp = GeneralData.XMPPMODULEENABLED = false;
					}
					junkMenu.add(umi);
				}
			}
			for (UserMenuItem umi : junkMenu) {
				myMenu.remove(umi);
			}
		} else { // remove only "communication" if flag_Xmpp = false
			if (!flag_Xmpp) {
				ArrayList<UserMenuItem> junkMenu = new ArrayList<>();
				for (UserMenuItem umi : myMenu) {
					if ("communication".equalsIgnoreCase(umi.getCode())) {
						junkMenu.add(umi);
					}
				}
				for (UserMenuItem umi : junkMenu) {
					myMenu.remove(umi);
				}
			}
		}

		// if not internalPharmacies mode remove "medicalsward" menu
		if (!internalPharmacies) {
			ArrayList<UserMenuItem> junkMenu = new ArrayList<>();
			for (UserMenuItem umi : myMenu) {
				if ("MEDICALSWARD".equalsIgnoreCase(umi.getCode()) || "MEDICALSWARD".equalsIgnoreCase(umi.getMySubmenu())) {
					junkMenu.add(umi);
				}
			}
			for (UserMenuItem umi : junkMenu) {
				myMenu.remove(umi);
			}
		}

		// remove disabled buttons
		ArrayList<UserMenuItem> junkMenu = new ArrayList<>();
		for (UserMenuItem umi : myMenu) {
			if (!umi.isActive()) {
				junkMenu.add(umi);
			}
		}
		for (UserMenuItem umi : junkMenu) {
			myMenu.remove(umi);
		}

		setTitle(MessageBundle.formatMessage("angal.mainmenu.fmt.title", myUser.getUserName()));
		ImageIcon img = new ImageIcon("./rsc/icons/oh.png");
		setIconImage(img.getImage());
		// add panel with buttons to frame
		MainPanel panel = new MainPanel(this);
		add(panel);
		pack();

		// compute menu position
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int screenHeight = screenSize.height;
		int frameHeight = getSize().height;
		setLocation(menuXPosition, screenHeight - frameHeight - menuYDisplacement);

		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myFrame.setAlwaysOnTop(GeneralData.MAINMENUALWAYSONTOP);
		myFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				actionExit(0);
			}
		});

		setResizable(false);
		setVisible(true);
	}

	private void actionExit(int status) {
		if (2 == status) {
			LOGGER.info("Login failed.");
		}
		String newLine = System.lineSeparator();
		LOGGER.info("{}{}====================={} Open Hospital closed {}====================={}", newLine, newLine, newLine, newLine, newLine);
		System.exit(status);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		launchApp(command);
	}

	/**
	 * @param itemMenuCode
	 */
	private void launchApp(String itemMenuCode) {

		for (UserMenuItem u : myMenu) {
			if (u.getCode().equals(itemMenuCode)) {
				if ("EXIT".equalsIgnoreCase(u.getCode())) {
					actionExit(0);
				} else if (u.isASubMenu()) {
					new SubMenu(this, u.getCode(), u.getButtonLabel(), myMenu);
					break;
				} else {
					String app = u.getMyClass();
					// an empty menu item
					if ("none".equalsIgnoreCase(app)) {
						return;
					}
					try {
						Object target = Class.forName(app).newInstance();
						try {
							((ModalJFrame) target).showAsModal(this);
						} catch (ClassCastException noModalJFrame) {
							try {
								((JFrame) target).setEnabled(true);
							} catch (ClassCastException noJFrame) {
								((JDialog) target).setEnabled(true);
							}
						}
					} catch (InstantiationException | ClassNotFoundException | IllegalAccessException ie) {
						LOGGER.error("Error instantiating menu item: '{}' with class '{}'.", u.getCode(), u.getMyClass());
					}
					break;
				}
			}
		}
	}

	private class MainPanel extends JPanel {

		private static final long serialVersionUID = 4338749100837551874L;

		private JButton[] button;
		private MainMenu parentFrame;

		public MainPanel(MainMenu parentFrame) {
			this.parentFrame = parentFrame;
			int numItems = 0;

			for (UserMenuItem u : myMenu) {
				if (u.getMySubmenu().equals("main")) {
					numItems++;
				}
			}
			button = new JButton[numItems];

			int k = 0;
			for (UserMenuItem u : myMenu) {
				if (u.getMySubmenu().equals("main")) {
					button[k] = new JButton(u.getButtonLabel());
					button[k].setMnemonic(KeyEvent.VK_A + (u.getShortcut() - 'A'));

					button[k].addActionListener(parentFrame);
					button[k].setActionCommand(u.getCode());
					k++;
				}
			}

			JLabel fig = new JLabel(new ImageIcon("rsc" + File.separator + "images" + File.separator + "LogoMenu.jpg"));
			add(fig, BorderLayout.WEST);

			JPanel buttons = new JPanel();
			buttons.setLayout(new SpringLayout());
			for (JButton jButton : button) {
				buttons.add(jButton);
			}
			SpringUtilities.makeCompactGrid(buttons, button.length, 1, 6, 6, 8, 10);
			add(buttons, BorderLayout.EAST);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension dimension = super.getPreferredSize();
		String title = this.getTitle();
		if (title != null) {
			Font defaultFont = UIManager.getDefaults().getFont("Label.font");
			int titleStringWidth = SwingUtilities.computeStringWidth(new JLabel().getFontMetrics(defaultFont), title);

			// account for titlebar button widths. (estimated)
			titleStringWidth += 120;

			// +10 accounts for the three dots that are appended when the title is too long
			if (dimension.getWidth() + 10 <= titleStringWidth) {
				dimension = new Dimension(titleStringWidth, (int) dimension.getHeight());
			}
		}
		return dimension;
	}

	public static User getUser() {
		return myUser;
	}
}
