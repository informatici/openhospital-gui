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
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;

import org.isf.generaldata.GeneralData;
import org.isf.generaldata.MessageBundle;
import org.isf.menu.gui.Login.LoginListener;
import org.isf.menu.gui.SubMenu.CommandListener;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.User;
import org.isf.menu.model.UserGroup;
import org.isf.menu.model.UserMenuItem;
import org.isf.session.RestartUserSession;
import org.isf.sessionaudit.manager.SessionAuditManager;
import org.isf.sessionaudit.model.SessionAudit;
import org.isf.sms.service.SmsSender;
import org.isf.telemetry.constants.TelemetryConstants;
import org.isf.telemetry.daemon.TelemetryDaemon;
import org.isf.telemetry.gui.TelemetryEdit;
import org.isf.telemetry.manager.TelemetryManager;
import org.isf.telemetry.model.Telemetry;
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

public class MainMenu extends JFrame implements ActionListener, LoginListener, CommandListener {

	private static final long serialVersionUID = 7620582079916035164L;
	public static final String ADMIN_STR = "admin";
	private boolean flag_Xmpp;
	private boolean flag_Sms;
	private boolean flag_Telemetry;
	private TelemetryDaemon telemetryDaemon;
	// used to understand if a module is enabled
	private Map<String, Boolean> activableModules;

	private SessionAuditManager sessionAuditManager = Context.getApplicationContext().getBean(SessionAuditManager.class);
	private static final Logger LOGGER = LoggerFactory.getLogger(MainMenu.class);
	private Integer sessionAuditId;

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
	private static List<UserMenuItem> myMenu;

	static final int menuXPosition = 10;
	static final int menuYDisplacement = 75;
	private static final String OH_TITLE = "OH";

	// singleUser=true : one user
	private boolean singleUser;

	private UserBrowsingManager userBrowsingManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);

	public MainMenu(User myUserIn) {
		setTitle(OH_TITLE);
		myUser = myUserIn;
		MainMenu myFrame = this;
		GeneralData.initialize();
		this.activableModules = retrieveActivatedModulesMap();
		Locale.setDefault(new Locale(GeneralData.LANGUAGE)); // for all fixed options YES_NO_CANCEL in dialogs
		singleUser = GeneralData.getGeneralData().getSINGLEUSER();
		MessageBundle.getBundle();
		// internalPharmacies=false : no internalPharmacies
		boolean internalPharmacies;
		// debug mode
		boolean debug;
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
		}

		if (singleUser) {
			LOGGER.info("Logging: Single User mode.");
			myUser = new User(ADMIN_STR, new UserGroup(ADMIN_STR, ""), ADMIN_STR, "");
		} else {
			// get an user
			LOGGER.info("Logging: Multi User mode.");

			if (null == myUser) {
				JFrame hiddenOwner = new JFrame(MessageBundle.getMessage("angal.login.title"));
				ImageIcon img = new ImageIcon("./rsc/icons/oh.png");
				hiddenOwner.setIconImage(img.getImage());
				hiddenOwner.setLocation(-10000, -1000);
				hiddenOwner.setSize(new Dimension(1, 1));
				hiddenOwner.setVisible(true);
				new Login(hiddenOwner, this);
				hiddenOwner.dispose();
			}

			if (null == myUser) {
				// Login failed
				actionExit(2);
			}
		}

		flag_Telemetry = GeneralData.TELEMETRYENABLED;
		if (flag_Telemetry) {
			runTelemetry();
		}

		MDC.put("OHUser", myUser.getUserName());
		MDC.put("OHUserGroup", myUser.getUserGroupName().getCode());
		try {
			this.sessionAuditId = sessionAuditManager.newSessionAudit(new SessionAudit(myUser.getUserName(), LocalDateTime.now(), null));
		} catch (OHServiceException e1) {
			LOGGER.error("Unable to log user login in the session_audit table");
		}
		// get menu items
		try {
			myMenu = userBrowsingManager.getMenu(myUser);
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
				 * Interaction communication = new Interaction(); communication.incomingChat(); communication.receiveFile();
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
			List<UserMenuItem> junkMenu = new ArrayList<>();
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
				List<UserMenuItem> junkMenu = new ArrayList<>();
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
		if (!flag_Sms) { // remove SMS Manager if not enabled
			List<UserMenuItem> junkMenu = new ArrayList<>();
			for (UserMenuItem umi : myMenu) {
				if ("smsmanager".equalsIgnoreCase(umi.getCode())) {
					junkMenu.add(umi);
				}
			}
			for (UserMenuItem umi : junkMenu) {
				myMenu.remove(umi);
			}
		}
		if (!flag_Telemetry) { // remove Telemetry Manager if not enabled
			List<UserMenuItem> junkMenu = new ArrayList<>();
			for (UserMenuItem umi : myMenu) {
				if ("telemetry".equalsIgnoreCase(umi.getCode())) {
					junkMenu.add(umi);
				}
			}
			for (UserMenuItem umi : junkMenu) {
				myMenu.remove(umi);
			}
		}

		// if not internalPharmacies mode remove "medicalsward" menu
		if (!internalPharmacies) {
			List<UserMenuItem> junkMenu = new ArrayList<>();
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
		List<UserMenuItem> junkMenu = new ArrayList<>();
		for (UserMenuItem umi : myMenu) {
			// if is not active or it is a module that is not enabled (there is no point in
			// showing a menu item)
			if (!umi.isActive() || isMenuItemNotEnabled(umi.getCode())) {
				junkMenu.add(umi);
			}
		}
		for (UserMenuItem umi : junkMenu) {
			myMenu.remove(umi);
		}

		ImageIcon img = new ImageIcon("./rsc/icons/oh.png");
		setIconImage(img.getImage());
		// add panel with buttons to frame
		MainPanel panel = new MainPanel(this);
		add(panel);
		setResizable(false);
		pack();

		// compute menu position
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int screenHeight = screenSize.height;
		int frameHeight = getSize().height;
		setLocation(menuXPosition, screenHeight - frameHeight - menuYDisplacement);

		myFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		myFrame.setAlwaysOnTop(GeneralData.MAINMENUALWAYSONTOP);
		myFrame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				actionExit(0);
			}
		});

		setVisible(true);
	}

	private Map<String, Boolean> retrieveActivatedModulesMap() {
		return new HashMap<>() {

			private static final long serialVersionUID = 1L;
			{
				put(TelemetryConstants.MENU_ID, Boolean.valueOf(GeneralData.TELEMETRYENABLED));
			}
		};
	}

	private boolean isMenuItemNotEnabled(String menuCode) {
		return this.activableModules.containsKey(menuCode) && !activableModules.get(menuCode).booleanValue();
	}

	private void runTelemetry() {
		TelemetryManager telemetryManager = Context.getApplicationContext().getBean(TelemetryManager.class);
		Telemetry settings = telemetryManager.retrieveSettings();
		// active = null => show popup
		// active = true => start daemon
		// active = false => do nothing
		if (settings == null || settings.getActive() == null) {
			// show telemetry popup
			new TelemetryEdit(this, true);
		}
		// start telemetry daemon
		this.telemetryDaemon = TelemetryDaemon.getTelemetryDaemon();
		if (telemetryDaemon.isInitialized()) {
			telemetryDaemon.start();
		} else {
			flag_Telemetry = false;
		}

	}

	private void actionExit(int status) {
		if (2 == status) {
			LOGGER.info("Login failed.");
		}
		updateSessionAudit();
		String newLine = System.lineSeparator();
		LOGGER.info("{}{}====================={} Open Hospital closed {}====================={}", newLine, newLine, newLine, newLine, newLine);
		System.exit(status);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String command = actionEvent.getActionCommand();
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
						Object target;
						try {
							target = Class.forName(app).getDeclaredConstructor().newInstance();
						} catch (InvocationTargetException | NoSuchMethodException e) {
							throw new RuntimeException(e);
						}
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

		private static final String BACKGROUND_COLOR_HEX = "#90b6b9";

		private static final long serialVersionUID = 4338749100837551874L;

		public MainPanel(MainMenu parentFrame) {
			int numItems = 1;

			setLayout(new BorderLayout());

			for (UserMenuItem u : myMenu) {
				if (u.getMySubmenu().equals("main")) {
					numItems++;
				}
			}
			JButton[] button = new JButton[numItems];

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

			addLogoutButton(button, k);

			add(getLogoPanel(), BorderLayout.WEST);

			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // top, left, bottom, right
			buttonsPanel.setLayout(new SpringLayout());
			for (JButton jButton : button) {
				buttonsPanel.add(jButton);
			}
			SpringUtilities.makeCompactGrid(buttonsPanel, button.length, 1, 0, 0, 0, 10);

			JPanel centerPanel = new JPanel();
			centerPanel.setLayout(new BorderLayout());

			JLabel userName = new JLabel(MessageBundle.formatMessage("angal.mainmenu.username.fmt", myUser.getUserName()));
			userName.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			userName.setToolTipText(myUser.getUserName());
			int nameWidth = buttonsPanel.getLayout().minimumLayoutSize(buttonsPanel).getSize().width;
			userName.setMaximumSize(new Dimension(nameWidth, 20));
			userName.setPreferredSize(new Dimension(nameWidth, 20));

			centerPanel.add(userName, BorderLayout.NORTH);
			centerPanel.add(buttonsPanel, BorderLayout.CENTER); // to center anyway, regardless the window's size

			add(centerPanel, BorderLayout.CENTER);
		}

		public void addLogoutButton(JButton[] button, int k) {
			button[k] = new JButton(MessageBundle.getMessage("angal.menu.logout.btn"));
			button[k].setMnemonic(MessageBundle.getMnemonic("angal.menu.logout.btn.key"));
			if (!singleUser) {
				button[k].addActionListener(actionEvent -> {
					updateSessionAudit();
					RestartUserSession.restartSession();
				});
			} else {
				button[k].addActionListener(actionEvent -> actionExit(0));
			}
			button[k].setActionCommand("logout");
		}

		private JPanel getLogoPanel() {
			JLabel logo_appl = new JLabel(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("logo_menu_vert.png"))
							.getImage().getScaledInstance(28, 180, Image.SCALE_SMOOTH)));
			Object checkLogoHospital = getClass().getClassLoader().getResource("logo_hospital.png");

			JPanel logoPanel = new JPanel();
			logoPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); // top, left, bottom, right
			BoxLayout layout = new BoxLayout(logoPanel, BoxLayout.Y_AXIS);
			logoPanel.setLayout(layout);
			logoPanel.setBackground(Color.decode(BACKGROUND_COLOR_HEX));
			if (checkLogoHospital != null) {
				JLabel logo_hosp = new JLabel(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("logo_hospital.png"))
								.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
				logoPanel.add(logo_hosp);
				logo_appl.setIcon(new ImageIcon(
								new ImageIcon(getClass().getClassLoader().getResource("logo_menu.png"))
												.getImage().getScaledInstance(90, 57, Image.SCALE_SMOOTH)));
			} else {
				logoPanel.add(Box.createVerticalStrut(100)); // for short menu
			}
			logoPanel.add(Box.createVerticalGlue());
			logoPanel.add(logo_appl);
			return logoPanel;
		}
	}

	public static User getUser() {
		return myUser;
	}

	public static void clearUser() {
		myUser = null;
	}

	private void updateSessionAudit() {
		try {
			if (sessionAuditId == null) {
				return;
			}
			Optional<SessionAudit> sa = sessionAuditManager.getSessionAudit(this.sessionAuditId);
			if (sa.isPresent()) {
				SessionAudit sessionAudit = sa.get();
				sessionAudit.setLogoutDate(LocalDateTime.now());
				sessionAuditManager.updateSessionAudit(sessionAudit);
			}
		} catch (OHServiceException e) {
			LOGGER.error("Unable to log user login in the session_audit table");
		}
	}

}