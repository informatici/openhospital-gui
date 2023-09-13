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

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.EventListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

import org.isf.menu.model.UserMenuItem;

public class SubMenu extends JDialog implements ActionListener {

	private static final long serialVersionUID = 7620582079916035164L;

	private EventListenerList commandListeners = new EventListenerList();

	public interface CommandListener extends EventListener {

		void commandInserted(AWTEvent e);
	}

	public void addCommandListener(CommandListener listener) {
		commandListeners.add(CommandListener.class, listener);
	}

	public void removeCommandListener(CommandListener listener) {
		commandListeners.remove(CommandListener.class, listener);
	}

	private void fireCommandInserted(String aCommand) {
		AWTEvent event = new AWTEvent(aCommand, AWTEvent.RESERVED_ID_MAX + 1) {
			private static final long serialVersionUID = 1L;
		};

		EventListener[] listeners = commandListeners.getListeners(CommandListener.class);
		for (EventListener listener : listeners) {
			((CommandListener) listener).commandInserted(event);
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

	private List<UserMenuItem> myMenu;
	private MainMenu mainMenu;

	private int prfButtonSize;

	public int getMinButtonSize() {
		return prfButtonSize;
	}

	public SubMenu(SubMenu parent, String code, String title, List<UserMenuItem> menu, MainMenu mainMenu) {
		super(parent, title, true);
		this.prfButtonSize = parent.getMinButtonSize();
		initialize(mainMenu, code, menu, parent.getBounds());
	}

	public SubMenu(MainMenu parent, String code, String title, List<UserMenuItem> menu) {
		super(parent, title, true);
		this.prfButtonSize = parent.getMinButtonSize();
		initialize(parent, code, menu, parent.getBounds());
	}

	private void initialize(MainMenu mainMenu, String code, List<UserMenuItem> menu, Rectangle parentBounds) {

		final int displacement = 5;

		this.mainMenu = mainMenu;

		addCommandListener(mainMenu);

		myMenu = menu;

		// add panel to frame
		SubPanel panel = new SubPanel(this, code);
		add(panel);
		
		// positioning submenu, slightly shifted from MainMenu
		Rectangle r = mainMenu.getBounds();
		r.width = getBounds().width;
		r.height = getBounds().height;
		r.x += displacement + mainMenu.getBounds().width;
		int subMenuHeight = panel.calculatedHeight;
		int mainMenuHeight = mainMenu.getRootPane().getHeight();
		if (subMenuHeight > mainMenuHeight) {
			r.y -= (subMenuHeight - mainMenuHeight);
		}

		setBounds(r);

		setResizable(false);
		pack();
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {

		String command = actionEvent.getActionCommand();
		for (UserMenuItem u : myMenu) {
			if (u.getCode().equals(command)) {
				if (u.isASubMenu()) {
					dispose();
					new SubMenu(this, u.getCode(), u.getButtonLabel(), myMenu, mainMenu);
					break;
				} else {
					dispose();
					fireCommandInserted(u.getCode());
					break;
				}
			}
		}
	}

	private class SubPanel extends JPanel {

		private static final long serialVersionUID = 4338749100837551874L;

		int calculatedHeight;

		public SubPanel(SubMenu dialogFrame, String subName) {

			int numItems = 0;
			for (UserMenuItem u : myMenu) {
				if (u.getMySubmenu().equals(subName)) {
					numItems++;
				}
			}

			JButton[] button = new JButton[numItems];

			int k = 0;
			for (UserMenuItem u : myMenu) {
				if (u.getMySubmenu().equals(subName)) {
					button[k] = new JButton(u.getButtonLabel());
					button[k].setMnemonic(KeyEvent.VK_A + u.getShortcut() - 'A');
					button[k].setActionCommand(u.getCode());
					if (!u.isActive()) {
						button[k].setEnabled(false);
					} else {
						button[k].addActionListener(dialogFrame);
					}
					k++;
				}
			}

			setButtonsSize(button);

			GridBagLayout layout = new GridBagLayout();
			setLayout(layout);

			final int insetsValue = 5;
			for (int i = 0; i < button.length; i++) {
				add(button[i], new GBC(0, i + 1).setInsets(insetsValue));
				calculatedHeight += insetsValue * 2;
			}
		}

		private void setButtonsSize(JButton[] button) {
			int maxH = 0;
			int maxMax = 0;
			int maxMin = 0;
			int maxPrf = 0;		

			for (JButton value : button) {
				maxH = Math.max(maxH, value.getMaximumSize().height);
				maxMax = Math.max(maxMax, value.getMaximumSize().width);
				maxMin = Math.max(maxMin, value.getMinimumSize().width);
				maxPrf = Math.max(maxPrf, value.getPreferredSize().width);
				calculatedHeight += maxH;
			}
			maxPrf = Math.max(maxPrf, prfButtonSize);

			for (JButton jButton : button) {
				jButton.setMaximumSize(new Dimension(maxMax, maxH));
				jButton.setMinimumSize(new Dimension(maxMin, maxH));
				jButton.setPreferredSize(new Dimension(maxPrf, maxH));
			}
		}
	}
}
