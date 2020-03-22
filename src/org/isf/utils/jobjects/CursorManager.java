package org.isf.utils.jobjects;

import java.awt.*;
import java.awt.event.InputEvent;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

class CursorManager {
  static EQPermit eqPermit = null;
  static {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
      ObjectName name = new ObjectName("org.isf.utils.jobjects:type=EQPermit"); 
      eqPermit = new EQPermit(); 
      mbs.registerMBean(eqPermit, name); 
    }catch(Exception exp){exp.printStackTrace();} 
    finally {}
  }
  private final DelayTimer waitTimer;
  private final Stack<DispatchedEvent> dispatchedEvents;
  private boolean needsCleanup;

  private EventQueue parentQueue = null;

  //private Lock clearLock = new ReentrantLock();

  public CursorManager(DelayTimer waitTimer) {
    this.dispatchedEvents = new Stack<DispatchedEvent>();
    this.waitTimer = waitTimer;
    this.parentQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
  }
  private void cleanUp() {
    if (((DispatchedEvent) dispatchedEvents.peek()).resetCursor()) {
      clearQueueOfInputEvents();
    }
  }
  private void acquirePermit() throws Exception{
    if(!eqPermit.getPermit()) 
          while(!eqPermit.getPermit()) 
          { 
            System.out.println("Cant get permit " + eqPermit.getPermit()); 
            Thread.sleep(2000);
          }
  }
  private void clearQueueOfInputEvents() {
    EventQueue q = Toolkit.getDefaultToolkit().getSystemEventQueue();
    try{
        synchronized(parentQueue){
          acquirePermit();
          synchronized(q) {
            ArrayList<AWTEvent> nonInputEvents = gatherNonInputEvents(q);
            for (Iterator<AWTEvent> it = nonInputEvents.iterator(); it.hasNext();)
              q.postEvent((AWTEvent)it.next());
          }
        }
    }catch(Exception exp){}
    finally{}
  }
  private ArrayList<AWTEvent> gatherNonInputEvents(EventQueue systemQueue) {
    ArrayList<AWTEvent> events = new ArrayList<AWTEvent>();
    while (systemQueue.peekEvent() != null) {
      try {
        AWTEvent nextEvent = systemQueue.getNextEvent();
        if (!(nextEvent instanceof InputEvent)) {
          events.add(nextEvent);
        }
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
    }
    return events;
  }
  public void push(Object source) {
    if (needsCleanup) {
      waitTimer.stopTimer();
      cleanUp(); 
      //this corrects the state when a modal dialog 
      //opened last time round
    }
    dispatchedEvents.push(new DispatchedEvent(source));
    needsCleanup = true;
  }
  public void pop() {
    cleanUp();
    dispatchedEvents.pop();
    if (!dispatchedEvents.isEmpty()) {
      //this will be stopped if getNextEvent() is called - 
      //used to watch for modal dialogs closing
      waitTimer.startTimer(); 
    } else {
      needsCleanup = false;
    }
  }
  public void setCursor() {
    ((DispatchedEvent) dispatchedEvents.peek()).setCursor();
  }
}
