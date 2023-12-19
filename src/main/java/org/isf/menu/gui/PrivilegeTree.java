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
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.isf.generaldata.MessageBundle;
import org.isf.menu.manager.Context;
import org.isf.menu.manager.UserBrowsingManager;
import org.isf.menu.model.UserGroup;
import org.isf.menu.model.UserMenuItem;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.gui.OHServiceExceptionUtil;

class PrivilegeTree extends JDialog {

	private static final long serialVersionUID = 1L;

	private UserGroup aGroup;

	private UserBrowsingManager userBrowsingManager = Context.getApplicationContext().getBean(UserBrowsingManager.class);

	public PrivilegeTree(UserGroupBrowsing parent, UserGroup aGroup) {
		super(parent, MessageBundle.getMessage("angal.groupsbrowser.menuitembrowser.title"), true);
		this.aGroup = aGroup;

		Rectangle r = parent.getBounds();
		setBounds(new Rectangle(r.x + 50, r.y + 50, 280, 350));

		List<UserMenuItem> myMenu = null;
		try {
			myMenu = userBrowsingManager.getGroupMenu(aGroup);
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}
		List<UserMenuItem> rootMenu = null;
		try {
			rootMenu = userBrowsingManager.getGroupMenu(new UserGroup("admin", ""));
		} catch (OHServiceException e) {
			OHServiceExceptionUtil.showMessages(e);
		}

		UserMenuItem menuRoot = new UserMenuItem("main", "angal.groupsbrowser.mainmenu.txt", "angal.groupsbrowser.mainmenu.txt",
				"", 'M', "", "", true, 1, true);
		// the root 
		root = new DefaultMutableTreeNode(menuRoot);
		model = new DefaultTreeModel(root);
		tree = new JTree(model);

		// a supporting structure
		List<UserMenuItem> junkMenu = new ArrayList<>();

		// cycle to process the whole rootMenu
		while (!rootMenu.isEmpty()) {
			for (UserMenuItem umi : rootMenu) {
				// The only difference between groups and Admin Menus is that groups have some items set inactive (Admin
				// always active for all of them)
				//
				// So if myMenu does not contains umi it means that there is but is not active
				if (myMenu.contains(umi)) {
					if (addMenuItem(myMenu.get(myMenu.indexOf(umi))) != null) {
						junkMenu.add(umi);
					}
				} else {
					umi.setActive(false);
					if (addMenuItem(umi) != null) {
						junkMenu.add(umi);
					}
				}
			}
			// Cycle to remove already processed rootMenu items
			for (UserMenuItem umi : junkMenu) {
				rootMenu.remove(umi);
			}
			junkMenu = new ArrayList<>();
		}

		MouseListener mouseListener = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1 && e.getClickCount() == 2) {
					doubleClick(selPath);
				}
			}
		};
		tree.addMouseListener(mouseListener);

		// set up node icons
		UserItemNameTreeCellRenderer renderer = new UserItemNameTreeCellRenderer();

		// no icon on leaves
		renderer.setLeafIcon(new ImageIcon(""));
		tree.setCellRenderer(renderer);

		add(new JScrollPane(tree), BorderLayout.CENTER);

		addButton();

		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void doubleClick(TreePath selPath) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
		UserMenuItem umi = (UserMenuItem) node.getUserObject();
		
		// Also if node has leafs can be deactivated
		String user = UserBrowsingManager.getCurrentUser();
		String umiFile = umi.getCode();
		
		if (user.equals("admin") && (umiFile.equals("file") ||
				umiFile.equals("groups") || umiFile.equals("users") ||
				umiFile.equals("usersusers") || umiFile.equals("exit"))) {
			return;
		}

		umi.setActive(!umi.isActive());
		tree.expandPath(selPath);
	}

	public void addButton() {
		JPanel panel = new JPanel();

		ActionListener addListener = actionEvent -> {

			List<UserMenuItem> newUserMenu = new ArrayList<>();
			Enumeration<?> e = root.breadthFirstEnumeration();
			while (e.hasMoreElements()) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
				UserMenuItem umi = (UserMenuItem) node.getUserObject();
				if (!umi.getCode().equals("main")) {
					newUserMenu.add(umi);
				}
			}
			try {
				userBrowsingManager.setGroupMenu(aGroup, newUserMenu);
			} catch (OHServiceException e1) {
				OHServiceExceptionUtil.showMessages(e1);
			}
			dispose();
		};

		JButton addButton = new JButton(MessageBundle.getMessage("angal.common.update.btn"));
		addButton.setMnemonic(MessageBundle.getMnemonic("angal.common.update.btn.key"));
		addButton.addActionListener(addListener);
		panel.add(addButton);

		JButton buttonClose = new JButton(MessageBundle.getMessage("angal.common.close.btn"));
		buttonClose.setMnemonic(MessageBundle.getMnemonic("angal.common.close.btn.key"));
		buttonClose.addActionListener(actionEvent -> dispose());
		panel.add(buttonClose);

		add(panel, BorderLayout.SOUTH);
	}

	/**
	 * Finds an object in the tree.
	 * 
	 * @param obj
	 *            the object to find
	 * @return the node containing the object or null if the object is not
	 *         present in the tree
	 */
	public DefaultMutableTreeNode findUserObject(Object obj) {
		// find the node containing a user object
		Enumeration<?> e = root.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			if (node.getUserObject().equals(obj)) {
				return node;
			}
		}
		return null;
	}

	public DefaultMutableTreeNode findParent(Object obj) {
		// find the node containing a user object
		Enumeration<?> e = root.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			if (((UserMenuItem) node.getUserObject()).getCode().equals(((UserMenuItem) obj).getMySubmenu())) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Adds a new item
	 * 
	 * @param c
	 * 
	 * @return the newly added node.
	 */
	public DefaultMutableTreeNode addMenuItem(UserMenuItem c) {
		
		// if the class is already in the tree, return its node
		DefaultMutableTreeNode node = findUserObject(c);
		if (node != null) {
			return node;
		}

		DefaultMutableTreeNode parent = findParent(c);

		if (parent == null) {
			return null;
		}

		int pos = 0;
		for (int i = 0; i < parent.getChildCount(); i++) {
			UserMenuItem n = (UserMenuItem) ((DefaultMutableTreeNode) parent.getChildAt(i)).getUserObject();
			if (n.getPosition() < c.getPosition()) {
				pos++;
			}
		}

		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(c);
		
		model.insertNodeInto(newNode, parent, pos);
		// make node visible
		TreePath path = new TreePath(model.getPathToRoot(newNode));
		tree.makeVisible(path);

		return newNode;
	}

	private DefaultMutableTreeNode root;

	private DefaultTreeModel model;

	private JTree tree;

}

/**
 * This class renders a item name
 */
class UserItemNameTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 11L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		// get the user object
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		UserMenuItem c = (UserMenuItem) node.getUserObject();

		// the first time, derive italic font from plain font
		if (plainFont == null) {
			plainFont = new Font("Arial", Font.BOLD, 16);
		}
		
		if (c.isActive()) {
			if (c.isASubMenu()) {
				setForeground(Color.BLUE);
				setFont(plainFont);
			}
			else {
				setForeground(Color.BLACK);
				setFont(plainFont);
			}
		} else {
			setForeground(Color.LIGHT_GRAY);
			setFont(plainFont);
		}
		return this;
	}

	private Font plainFont;

}
