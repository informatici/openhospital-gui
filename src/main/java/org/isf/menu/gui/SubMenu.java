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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import org.isf.menu.model.UserMenuItem;



public class SubMenu extends JDialog implements ActionListener{
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

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;};

        EventListener[] listeners = commandListeners.getListeners(CommandListener.class);
        for (int i = 0; i < listeners.length; i++)
            ((CommandListener)listeners[i]).commandInserted(event);
    }
		
	
	private ArrayList<UserMenuItem> myMenu;
	private MainMenu mainMenu;
	
	private int prfButtonSize=0;
	public int getMinButtonSize(){
		return prfButtonSize;
	}
	
	
	public SubMenu(SubMenu parent, String code, ArrayList<UserMenuItem> menu, MainMenu mainMenu) {
		super(parent, "     ", true);
		this.prfButtonSize=parent.getMinButtonSize();
		initialize(mainMenu, code, menu, parent.getBounds());
	}
	
	
	public SubMenu(MainMenu parent, String code, ArrayList<UserMenuItem> menu) {
		super(parent, "     ", true);
		this.prfButtonSize=parent.getMinButtonSize();
		initialize(parent, code, menu, parent.getBounds());
	}
	
	
	private void initialize(MainMenu mainMenu, String code, ArrayList<UserMenuItem> menu, Rectangle parentBounds){

		final int displacement = 50;
		
		this.mainMenu = mainMenu;
		
		addCommandListener(mainMenu);
		
		myMenu = menu;
		
		// add panel to frame
		SubPanel panel = new SubPanel(this, code);
		add(panel);

		// submenu leggermente spostato rispetto a menu
		Rectangle r = parentBounds;
		r.width = getBounds().width;
		r.height = getBounds().height;
		r.x += displacement;
		r.y -= displacement;

		setBounds(r);

		setResizable(false);
		pack();
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		for(UserMenuItem u : myMenu){
			if (u.getCode().equals(command)){
				if (u.isASubMenu()){
					dispose();
					new SubMenu(this, u.getCode(), myMenu, mainMenu );					
					break;
				}
				else {
					dispose();
					fireCommandInserted(u.getCode());					
					break;
				}
			}
		}
		
	}
	
	private class SubPanel extends JPanel {

		private static final long serialVersionUID = 4338749100837551874L;

		private JButton button[];
		private String title;
		
		public SubPanel(SubMenu dialogFrame, String subName) {

			int numItems = 0;
			
			for(UserMenuItem u : myMenu){
				if (u.getMySubmenu().equals(subName)) numItems++;
				if (u.getCode().equalsIgnoreCase(subName)) title=u.getButtonLabel();
			}	
				
			//System.out.println(numItems);
			
			button = new JButton[numItems];

			int k=1;
			
			for(UserMenuItem u : myMenu)
				if (u.getMySubmenu().equals(subName)){
					button[k-1]= new JButton(u.getButtonLabel());
					button[k-1].setMnemonic(KeyEvent.VK_A	+ (int)(u.getShortcut() - 'A'));
					button[k-1].setActionCommand(u.getCode());
					if (!u.isActive())
						button[k-1].setEnabled(false);
					else 
						button[k-1].addActionListener(dialogFrame); 
					k++;
				} 

			setButtonsSize(button);

			//setBackground(Color.WHITE);
			GridBagLayout layout = new GridBagLayout();
			setLayout(layout);
			JPanel p = new JPanel();
			JLabel l = new JLabel(title);
			l.setFont(new Font("SansSerif",Font.BOLD,12));
			p.add(l);
			p.setPreferredSize(new Dimension(prfButtonSize,p.getPreferredSize().height));
			//p.setBackground(Color.WHITE);
			p.setBorder(new javax.swing.border.LineBorder(Color.lightGray));
			
			final int insetsValue = 5;

			add(p,new GBC(0, 0).setInsets(insetsValue));
						
			for (int i = 0; i < button.length; i++) {
				add(button[i], new GBC(0, i+1).setInsets(insetsValue));
			}
			
		}
		
		private void setButtonsSize(JButton button[]) {
			int maxH = 0;
			int maxMax = 0;
			int maxMin = 0;
			int maxPrf = 0;

			for (int i = 0; i < button.length; i++) {
				maxH = Math.max(maxH, button[i].getMaximumSize().height);
				maxMax = Math.max(maxMax, button[i].getMaximumSize().width);
				maxMin = Math.max(maxMin, button[i].getMinimumSize().width);
				maxPrf = Math.max(maxPrf, button[i].getPreferredSize().width);
			}
			maxPrf = Math.max(maxPrf, prfButtonSize);
			
			for (int i = 0; i < button.length; i++) {
				button[i].setMaximumSize(new Dimension(maxMax, maxH));
				button[i].setMinimumSize(new Dimension(maxMin, maxH));
				button[i].setPreferredSize(new Dimension(maxPrf, maxH));
			}
		}
	}
}
