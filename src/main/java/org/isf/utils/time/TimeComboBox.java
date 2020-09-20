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
package org.isf.utils.time;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.GregorianCalendar;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

public class TimeComboBox extends JPanel{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EventListenerList timeComboListeners = new EventListenerList();

    public interface TimeComboListener extends EventListener {
        void dateSet(AWTEvent e);
    }

    public void addTimeComboListener(TimeComboListener l) {
    	timeComboListeners.add(TimeComboListener.class, l);
    }

    public void removeTimeComboListener(TimeComboListener listener) {
    	timeComboListeners.remove(TimeComboListener.class, listener);
    }

    private void fireDateSet() {
        AWTEvent event = new AWTEvent(new Object(), AWTEvent.RESERVED_ID_MAX + 1) {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;};
        EventListener[] listeners = timeComboListeners.getListeners(TimeComboListener.class);
        for (int i = 0; i < listeners.length; i++)
            ((TimeComboListener)listeners[i]).dateSet(event);
    }
	
	private JComboBox days=null;
	private JComboBox months=null;
	private JComboBox years=null;
	private ActionListener action;
	
	public TimeComboBox(GregorianCalendar time){
		action=new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(((Integer)years.getSelectedItem())+" "+((Integer)months.getSelectedItem())+" "+((Integer)days.getSelectedItem()));
				fireDateSet();
			}
		};
		initialize(time);
		add(days);
		add(months);
		add(years);
	}
	private void initialize(GregorianCalendar time){
		years=new JComboBox();
		for(int y=2006;y<2020;y++){
			years.addItem(y);
		}
		years.setSelectedItem(time.get(GregorianCalendar.YEAR));
		years.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(((Integer)years.getSelectedItem())+" "+((Integer)months.getSelectedItem())+" "+((Integer)days.getSelectedItem()));
				setDays(new GregorianCalendar(((Integer)years.getSelectedItem()),((Integer)months.getSelectedItem())-1,((Integer)days.getSelectedItem())));
				fireDateSet();
			}
		});
		
		months=new JComboBox();
		for(int m=1;m<=12;m++){
			months.addItem(m);
		}
		months.setSelectedItem(time.get(GregorianCalendar.MONTH)+1);
		months.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(((Integer)years.getSelectedItem())+" "+((Integer)months.getSelectedItem())+" "+((Integer)days.getSelectedItem()));
				setDays(new GregorianCalendar(((Integer)years.getSelectedItem()),((Integer)months.getSelectedItem())-1,((Integer)days.getSelectedItem())));
				System.out.println("Passo2");
				fireDateSet();
			}
		});
		setDays(time);
	}
	public void setDays(GregorianCalendar time){
		if(days!=null)days.removeActionListener(action);
		else days=new JComboBox();
		days.removeAllItems();

		int numDays;
		int month=time.get(GregorianCalendar.MONTH)+1;
		if(month==1||month==3||month==5||month==7||month==8||month==10||month==12){
			numDays=31;
		}
		else{
			if(month==2)
				if(time.get(GregorianCalendar.YEAR)%4==0)numDays=29;
				else numDays=28;
			else {
				numDays=30;
			}
		}
		for(int i=1;i<=numDays;i++){
			days.addItem(i);
		}
		days.setSelectedItem(time.get(GregorianCalendar.DAY_OF_MONTH));
		days.addActionListener(action);
	}
	public GregorianCalendar getDate(){
		return new GregorianCalendar(((Integer)years.getSelectedItem()),((Integer)months.getSelectedItem())-1,((Integer)days.getSelectedItem()));
	}

}
