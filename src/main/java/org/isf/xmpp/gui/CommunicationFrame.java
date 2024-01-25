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
package org.isf.xmpp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;
import org.isf.xmpp.gui.ChatTab.TabButton;
import org.isf.xmpp.manager.Interaction;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunicationFrame extends JFrame implements MessageListener, FileTransferListener, ChatManagerListener {

	private static final
	long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationFrame.class);

	private JPanel leftpanel;
	private JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
	private JList buddyList;
	private ChatTab tabs;
	public Object user;
	private ChatPanel newChat;
	private Interaction interaction;
	private static JFrame frame;
	private Roster roster;
	private JTextPane userInfo;
	private ChatMessages area;

	private UserBrowsingManager userBrowsingManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);

	public CommunicationFrame() {
		if (frame == null) {
			createFrame();
			frame = this;
			frame.validate();
			frame.repaint();
			frame.setVisible(false);
			frame.validate();
			frame.repaint();
			LOGGER.info("XMPP Server active and running"); //$NON-NLS-1$
		} else {
			frame = getFrame();
			frame.setVisible(true);
			frame.validate();
			frame.repaint();

		}
	}

	private void createFrame() {
		interaction = new Interaction();
		activateListeners();
		getContentPane().add(createLeftPanel(), BorderLayout.WEST);
		getContentPane().add(separator, BorderLayout.CENTER);

		tabs = new ChatTab();
		tabs.setPreferredSize(new Dimension(200, 400));
		tabs.setMaximumSize(new Dimension(200, 400));
		tabs.setMinimumSize(new Dimension(200, 400));
		tabs.setSize(new Dimension(200, 400));
		getContentPane().add(tabs, BorderLayout.CENTER);
		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
		setSize(600, 450);

		setTitle(MessageBundle.formatMessage("angal.xmpp.communication.fmt.title", UserBrowsingManager.getCurrentUser()));
		setResizable(false);
		setLocationRelativeTo(null);
	}

	public void activateListeners() {
		senseRoster();
		incomingChat();
		receiveFile();
	}

	public void senseRoster() {
		roster = interaction.getRoster();

		roster.addRosterListener(new RosterListener() {

			@Override
			public void presenceChanged(Presence presence) {
				LOGGER.debug("State changed -> {} - {}", presence.getFrom(), presence); //$NON-NLS-1$ //$NON-NLS-2$
				String userName = interaction.userFromAddress(presence.getFrom());
				StringBuilder sb = new StringBuilder();
				if (!presence.isAvailable()) {
					sb.append(userName).append(' ').append(MessageBundle.getMessage("angal.xmpp.isnowoffline.txt"));
				} else if (presence.isAvailable()) {
					sb.append(userName).append(' ').append(MessageBundle.getMessage("angal.xmpp.isnowonline.txt"));
				}
				int index = tabs.indexOfTab(userName);
				if (index != -1) {
					area = getArea(userName, true);
					try {
						area.printNotification(sb.toString());
					} catch (BadLocationException badLocationException) {
						LOGGER.error(badLocationException.getMessage(), badLocationException);
					}
				}
				refreshBuddyList();
			}

			@Override
			public void entriesUpdated(Collection<String> arg0) {
			}

			@Override
			public void entriesDeleted(Collection<String> arg0) {
			}

			@Override
			public void entriesAdded(Collection<String> arg0) {
			}
		});
	}

	public void refreshBuddyList() {
		getContentPane().remove(leftpanel);
		leftpanel = createLeftPanel();
		getContentPane().add(leftpanel, BorderLayout.WEST);
		validate();
		repaint();
	}

	private void incomingChat() {
		ChatManager chatmanager = interaction.getServer().getChatManager();
		chatmanager.addChatListener((chat, createLocally) -> {
			chat.addMessageListener((chat1, message) -> {
				if (message.getType() == Message.Type.chat) {
					LOGGER.debug("Incoming message from: {}", chat1.getThreadID());
					LOGGER.debug("GUI: {}", this);
					String user = chat1.getParticipant().substring(0, chat1.getParticipant().indexOf('@'));
					printMessage(getArea(user, true), interaction.userFromAddress(message.getFrom()), message.getBody(), false);
					if (!isVisible()) {
						setVisible(true);
						setState(Frame.NORMAL);
						toFront();
					} else {
						toFront();
					}
				}
			});
			if (!createLocally) {

			}
		});
	}

	public void receiveFile() {
		FileTransferNegotiator.setServiceEnabled(interaction.getConnection(), true);
	}

	private JScrollPane createBuddyList() {

		buddyList = getBuddyList();
		final JPopupMenu popUpMenu = new JPopupMenu();
		JMenuItem sendFile;
		popUpMenu.add(sendFile = new JMenuItem(MessageBundle.getMessage("angal.xmpp.sendfile.txt")));
		popUpMenu.add(new Separator());
		JMenuItem getInfo;
		popUpMenu.add(getInfo = new JMenuItem(MessageBundle.getMessage("angal.xmpp.getinfo.txt")));
		final JFileChooser fileChooser = new JFileChooser();
		sendFile.addActionListener(actionEvent -> {
			int returnVal = fileChooser.showOpenDialog(getParent());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				LOGGER.debug("Selected file: {}", file);
				String receiver = ((RosterEntry) buddyList.getSelectedValue()).getName();
				LOGGER.debug("Receiver: {}", receiver);
				interaction.sendFile(receiver, file, null);
			}
		});
		getInfo.addActionListener(actionEvent -> {
			String userName = ((RosterEntry) buddyList.getSelectedValue()).getName();
			String info = null;
			try {
				info = userBrowsingManager.getUsrInfo(userName);
			} catch (OHServiceException e) {
				OHServiceExceptionUtil.showMessages(e);
			}

			userInfo.setText(MessageBundle.formatMessage("angal.xmpp.userinfo.fmt.txt", userName, info));
			validate();
			repaint();
		});

		buddyList.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 1) {
					int index = buddyList.locationToIndex(e.getPoint());
					if (index >= 0) {
						user = ((RosterEntry) buddyList.getModel().getElementAt(index)).getName();
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e) && !buddyList.isSelectionEmpty()
						&& buddyList.locationToIndex(e.getPoint()) == buddyList.getSelectedIndex()) {
					popUpMenu.show(buddyList, e.getX(), e.getY());
				}

				if (e.getClickCount() == 2) {
					int index = buddyList.locationToIndex(e.getPoint());
					LOGGER.debug("Index : {}", index);
					if (index >= 0) {
						user = ((RosterEntry) buddyList.getModel().getElementAt(index)).getName();
						LOGGER.debug("User selected: {}", user); //$NON-NLS-1$
						newChat = new ChatPanel();
						roster = interaction.getRoster();
						Presence presence = roster.getPresence(((RosterEntry) buddyList.getModel().getElementAt(index)).getUser());
						if (presence.isAvailable()) {
							if (tabs.indexOfTab((String) user) == -1) {
								tabs.addTab((String) user, newChat);
								tabs.setSelectedIndex(tabs.indexOfTab((String) user));
							}
							tabs.setSelectedIndex(tabs.indexOfTab((String) user));
						} else {
							LOGGER.debug("User offline"); //$NON-NLS-1$
						}
					}
				}
			}
		});
		JScrollPane buddy = new JScrollPane(buddyList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Dimension size = new Dimension(150, 1000);
		buddy.setPreferredSize(size);

		return buddy;
	}

	private JTextPane userInfoArea() {
		Dimension size = new Dimension(150, 150);
		userInfo = new JTextPane();
		userInfo.setForeground(new Color(58, 95, 205));
		userInfo.setBackground(new Color(238, 238, 238));
		userInfo.setMinimumSize(size);
		userInfo.setMaximumSize(size);
		userInfo.setSize(size);
		userInfo.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.xmpp.usersinfo.border")));
		userInfo.setEditable(false);
		return userInfo;
	}

	private JPanel createLeftPanel() {  // contact list panel
		Dimension size = new Dimension(150, 200);

		leftpanel.setLayout(new BoxLayout(leftpanel, BoxLayout.Y_AXIS));
		JScrollPane buddy = createBuddyList();
		buddy.setBorder(BorderFactory.createTitledBorder(MessageBundle.getMessage("angal.xmpp.contacts.border")));
		buddy.setPreferredSize(size);
		buddy.setMaximumSize(size);
		leftpanel.setMaximumSize(size);
		leftpanel.add(buddy);
		leftpanel.add(userInfoArea());
		return leftpanel;
	}

	public ChatMessages getArea(String name, boolean incoming) {

		int index = tabs.indexOfTab(name);
		LOGGER.debug("Index_: {}", index); //$NON-NLS-1$
		if (index != -1) {
			if (incoming) {
				((TabButton) tabs.getTabComponentAt(index)).setColor(Color.red);
			} else {
				((TabButton) tabs.getTabComponentAt(index)).setColor(Color.black);
			}
		} else {
			LOGGER.debug("Index creation: {}", index); //$NON-NLS-1$
			newChat = new ChatPanel();
			tabs.addTab(name, newChat);
			tabs.setTabColor(new Color(176, 23, 31));
			validate();
			repaint();
			index = tabs.indexOfTab(name);
			LOGGER.debug("Index creation: {}", index); //$NON-NLS-1$
		}
		return ((ChatPanel) tabs.getComponentAt(index)).getChatMessages();
	}

	public String getSelectedUser() {
		int index = tabs.getSelectedIndex();
		LOGGER.debug("Title : {}", tabs.getTitleAt(index)); //$NON-NLS-1$
		LOGGER.debug("Index : {}", index); //$NON-NLS-1$
		return tabs.getTitleAt(index);
	}

	public void printMessage(ChatMessages area, String user, String text, boolean visualize) {
		try {

			if (text.startsWith("011100100110010101110000011011110111001001110100")) {//report jasper //$NON-NLS-1$
				area.printReport(user, text);
			} else if (text.startsWith("0101010001000001")) { //file transfer accepted 0101010001000001=TA //$NON-NLS-1$
				int index = text.indexOf('$'); //$NON-NLS-1$
				area.printNotification(text.substring(index + 1));
				LOGGER.debug("Transfer accepted."); //$NON-NLS-1$
			} else if (text.startsWith("0101010001010010")) {// file transfer refused 0101010001010010=TR //$NON-NLS-1$
				int index = text.indexOf('$'); //$NON-NLS-1$
				LOGGER.debug("Transfer rejected."); //$NON-NLS-1$
				area.printNotification(text.substring(index + 1));
			} else {
				area.printMessage(user, text, visualize);
			}
		} catch (BadLocationException badLocationException) {
			LOGGER.error(badLocationException.getMessage(), badLocationException);
		}
	}

	public void printNotification(ChatMessages area, String user, String fileTransfer, JButton accept, JButton reject) {
		area.printNotification(user, fileTransfer, accept, reject);
	}

	public void printNotification(ChatMessages area, String text) {
		try {
			area.printNotification(text);
		} catch (BadLocationException badLocationException) {
			LOGGER.error(badLocationException.getMessage(), badLocationException);
		}
	}

	public JList getBuddyList() {

		LOGGER.debug("==> roster : {}", roster);
		List<RosterEntry> entries = new ArrayList<>(roster.getEntries());
		entries.sort((r1, r2) -> {
			Presence presence1 = roster.getPresence(r1.getUser());
			Presence presence2 = roster.getPresence(r2.getUser());
			String r1Name = r1.getName();
			String r2Name = r2.getName();
			if (presence1.isAvailable() == presence2.isAvailable()) {
				return r1Name.toLowerCase().compareTo(r2Name.toLowerCase());
			}

			if (presence1.isAvailable() && (!presence2.isAvailable())) {
				return -1;
			} else {
				return 1;
			}

		});
		JList buddy = new JList(entries.toArray());

		ListCellRenderer render = new ComplexCellRender(interaction.getServer());

		buddy.setCellRenderer(render);

		return buddy;
	}

	public void sendMessage(String textMessage, String to, boolean visualize) {

		interaction.sendMessage(this, textMessage, to, visualize);
		if (visualize) {
			printMessage(getArea(getSelectedUser(), false), MessageBundle.getMessage("angal.xmpp.me.txt"), textMessage, visualize);
		}
	}

	public static JFrame getFrame() {
		return frame;
	}

	@Override
	public void processMessage(Chat arg0, Message arg1) {
		if (arg1.getType() == Message.Type.normal) {
			LOGGER.debug("Send message from: {}", arg0.getThreadID());
			String user = arg0.getParticipant().substring(0, arg0.getParticipant().indexOf('@'));
			printMessage((getArea(user, false)), user, arg1.getBody(), false);
			if (!this.isVisible()) {
				this.setVisible(true);
				this.setState(Frame.ICONIFIED);
				this.toFront();
			} else {
				this.toFront();
			}
		}
	}

	@Override
	public void fileTransferRequest(final FileTransferRequest request) {

		if (!this.isVisible()) {
			this.setVisible(true);
		}

		ImageIcon acceptIcon;
		ImageIcon rejectIcon;

		String fileTransfer = MessageBundle.formatMessage("angal.xmpp.wouldliketosend.fmt.msg",
				interaction.userFromAddress(request.getRequestor()), request.getFileName());
		acceptIcon = new ImageIcon("rsc/icons/ok_button.png");
		rejectIcon = new ImageIcon("rsc/icons/delete_button.png");
		final JButton accept = new JButton(acceptIcon);
		accept.setMargin(new Insets(1, 1, 1, 1));
		accept.setOpaque(false);
		accept.setBorderPainted(false);
		accept.setContentAreaFilled(false);
		final JButton reject = new JButton(rejectIcon);
		reject.setMargin(new Insets(1, 1, 1, 1));
		reject.setOpaque(false);
		reject.setBorderPainted(false);
		reject.setContentAreaFilled(false);
		final String user = interaction.userFromAddress(request.getRequestor());
		this.printNotification((this.getArea(user, false)), user, fileTransfer, accept, reject);

		accept.addActionListener(actionEvent -> {
			accept.setEnabled(false);
			reject.setEnabled(false);
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("."));
			chooser.setDialogTitle("Select the directory");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			chooser.setAcceptAllFileFilterUsed(false);

			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				LOGGER.debug("getCurrentDirectory(): {}", chooser.getCurrentDirectory());
				LOGGER.debug("getSelectedFile() : {}", chooser.getSelectedFile());
			} else {
				LOGGER.debug("No Selection.");
			}
			IncomingFileTransfer transfer = request.accept();
			String path = chooser.getSelectedFile() + "/" + request.getFileName();
			File file = new File(path);
			try {
				transfer.recieveFile(file);
			} catch (XMPPException xmppException) {
				LOGGER.error(xmppException.getMessage(), xmppException);
			}

			printNotification((getArea(user, true)), MessageBundle.formatMessage("angal.xmpp.thefiletransferofbetweenyouandendedsuccesfully.fmt.msg",
					request.getFileName(), user));
			sendMessage(MessageBundle.formatMessage("angal.xmpp.filetransferofhasbeenaccepted.fmt.msg", request.getFileName()),
					request.getRequestor(), false);
		});
		reject.addActionListener(actionEvent -> {
			accept.setEnabled(false);
			reject.setEnabled(false);
			request.reject();

			printNotification((getArea(user, false)), MessageBundle.getMessage("angal.xmpp.youhaverejectedthefiletransfer.txt"));
			sendMessage(MessageBundle.formatMessage("angal.xmpp.filetransferofhasbeenrejected.fmt.msg", request.getFileName()),
					request.getRequestor(), false);
		});
	}

	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		if (!createdLocally) {

			chat.addMessageListener((chat1, message) -> {
				if (message.getType() == Message.Type.chat) {
					LOGGER.debug("Incoming message from: {}", chat1.getThreadID());
					LOGGER.debug("GUI: {}", this);
					String user = chat1.getParticipant().substring(0, chat1.getParticipant().indexOf('@'));
					printMessage((getArea(user, false)), interaction.userFromAddress(message.getFrom()), message.getBody(), false);
					if (!isVisible()) {
						setVisible(true);
						setState(Frame.NORMAL);
						toFront();
					} else {
						toFront();
					}
				}
			});

		}
	}

}
