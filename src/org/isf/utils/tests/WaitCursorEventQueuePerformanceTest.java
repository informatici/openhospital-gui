package org.isf.utils.tests;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;
import java.util.Random;

import javax.swing.SwingUtilities;

import org.isf.utils.jobjects.WaitCursorEventQueue;

import junit.framework.TestCase;

public class WaitCursorEventQueuePerformanceTest
    extends TestCase {
  private static final long FAST = 5;
  private static final long SLOW = 50;
  private static final long MIXED = -1;
  private static final long TIMEOUT = 15;
  private Dialog dialog;
  private Frame frame;

  public WaitCursorEventQueuePerformanceTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    frame = new Frame();
    frame.pack();
    frame.setBounds(-1000, -1000, 100, 100);
    frame.setVisible(true);
    dialog = new Dialog(frame, true);
    dialog.pack();
    dialog.setBounds(-1000, -1000, 100, 100);
  }
  protected void tearDown() throws Exception {
    frame.dispose();
    dialog.dispose();
  }
  private long postEvents(long time) throws InterruptedException {
    InvocationEvent repeatEvent = new InvocationEvent(
        frame, new TimedEvent(time));
    InvocationEvent finalEvent = new InvocationEvent(
        frame, new TimedEvent(time), this, false);
    EventQueue q = Toolkit.getDefaultToolkit().getSystemEventQueue();
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < 1000; i++)
      q.postEvent(repeatEvent);
    synchronized (this) {
      q.postEvent(finalEvent);
      wait(); //we will be notified by finalEvent when it gets posted
    }
    long endTime = System.currentTimeMillis();
    return (endTime - startTime);
  }
  public void testNormalPerformanceWithFastEvents()
    throws InterruptedException {
    System.out.println("\nnormal with fast: " + postEvents(FAST));
  }
  public void testNormalPerformanceWithSlowEvents()
    throws InterruptedException {
    System.out.println("\nnormal with slow: " + postEvents(SLOW));
  }
  public void testNormalPerformanceWithMixedEvents()
    throws InterruptedException {
    System.out.println("\nnormal with random: " + postEvents(MIXED));
  }
  public void testWaitQueuePerformanceWithFastEvents()
    throws InterruptedException {
    WaitCursorEventQueue waitQueue = new WaitCursorEventQueue(
                                         (int) TIMEOUT);
    Toolkit.getDefaultToolkit().getSystemEventQueue()
       .push(waitQueue);
    System.out.println("\nwait with fast: " + postEvents(FAST));
    waitQueue.close();
  }
  public void testWaitQueuePerformanceWithSlowEvents()
    throws InterruptedException {
    WaitCursorEventQueue waitQueue = new WaitCursorEventQueue(
                                         (int) TIMEOUT);
    Toolkit.getDefaultToolkit().getSystemEventQueue()
       .push(waitQueue);
    System.out.println("\nwait with slow: " + postEvents(SLOW));
    waitQueue.close();
  }
  public void testWaitQueuePerformanceWithMixedEvents()
      throws InterruptedException {
    WaitCursorEventQueue waitQueue = new WaitCursorEventQueue(
                                         (int) TIMEOUT);
    Toolkit.getDefaultToolkit().getSystemEventQueue()
       .push(waitQueue);
    System.out.println("\nwait with random: " + postEvents(MIXED));
    waitQueue.close();
  }
  public void testWaitQueuePerformanceWithDialogWithFastEvents()
    throws InterruptedException {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        dialog.setVisible(true);
      }
    });
    WaitCursorEventQueue waitQueue = new WaitCursorEventQueue(
                                         (int) TIMEOUT);
    Toolkit.getDefaultToolkit().getSystemEventQueue()
       .push(waitQueue);
    System.out.println("\nwait with dialog with fast: " + 
                       postEvents(FAST));
    waitQueue.close();
  }
  public void testWaitQueuePerformanceWithDialogWithSlowEvents()
    throws InterruptedException {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        dialog.setVisible(true);
      }
    });
    WaitCursorEventQueue waitQueue = new WaitCursorEventQueue(
                                         (int) TIMEOUT);
    Toolkit.getDefaultToolkit().getSystemEventQueue()
       .push(waitQueue);
    System.out.println("\nwait with dialog with slow: " + 
                       postEvents(SLOW));
    waitQueue.close();
  }
  public void testWaitQueuePerformanceWithDialogWithMixedEvents()
    throws InterruptedException {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        dialog.setVisible(true);
      }
    });
    WaitCursorEventQueue waitQueue = new WaitCursorEventQueue(
                                         (int) TIMEOUT);
    Toolkit.getDefaultToolkit().getSystemEventQueue()
       .push(waitQueue);
    System.out.println("\nwait with dialog with random: " + 
                       postEvents(MIXED));
    waitQueue.close();
  }

  private class TimedEvent implements Runnable {
    private Random random;
    private long time;

    public TimedEvent(long time) {
      this.time = time;
      if (time == MIXED) {
        random = new Random(1000);
      }
    }

    public void run() {
      try {
        if (time == MIXED) {
          Thread.sleep(
              (long) (random.nextDouble() * (SLOW - FAST) + 
                FAST));
        } else {
          Thread.sleep(time);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}