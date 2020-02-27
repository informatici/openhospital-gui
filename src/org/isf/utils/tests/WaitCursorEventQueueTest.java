package org.isf.utils.tests;
import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.isf.utils.jobjects.WaitCursorEventQueue;

import junit.framework.TestCase;

public class WaitCursorEventQueueTest extends TestCase {
  private static final int TIMEOUT = 200;
  private static final int BUFFER = 30;
  private static final Cursor BASE_CURSOR = Cursor.getPredefinedCursor(
                                                Cursor.CROSSHAIR_CURSOR);
  private CursorReportingDialog dialog;
  private CursorReportingDialog dialog2;
  private CursorReportingFrame frame;
  private TestWaitCursorEventQueue eventQueue;

  public WaitCursorEventQueueTest(String name) {
    super(name);
  }

  public void setUp() {
    eventQueue = new TestWaitCursorEventQueue(TIMEOUT);
    Toolkit.getDefaultToolkit().getSystemEventQueue()
       .push(eventQueue);
    frame = new CursorReportingFrame();
    frame.pack();
    frame.setBounds(-1000, -1000, 100, 100);
    frame.setVisible(true);
    dialog = new CursorReportingDialog(frame);
    dialog.pack();
    dialog.setBounds(-1000, -1000, 100, 100);
    dialog2 = new CursorReportingDialog(dialog);
    dialog2.pack();
    dialog2.setBounds(-1000, -1000, 100, 100);
  }
  public void tearDown() throws InvocationTargetException, 
                                InterruptedException {
    flushQueue();
    eventQueue.close();
    eventQueue = null;
    flushQueue();
    frame.dispose();
    frame = null;
    dialog.dispose();
    dialog = null;
  }
  private void flushQueue() throws InvocationTargetException, 
                                   InterruptedException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
      }
    });
  }
  private void postEvent(Object source, Runnable event) {
    eventQueue.postEvent(new InvocationEvent(source, event));
  }
  private void hangOut(long timeout) {
    try {
      Thread.sleep(timeout);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  public void testNoCursor() throws InvocationTargetException, 
                                    InterruptedException {
    DelayEvent event = new DelayEvent(TIMEOUT - BUFFER);
    postEvent(frame, event);
    postEvent(frame, event);
    postEvent(frame, event);
    postEvent(frame, event);
    flushQueue();
    assertEquals("no cursor set", 0, frame.getCursorSetCount());
    assertEquals("no cursor reset", 0, 
                 frame.getCursorResetCount());
  }
  public void testCursor() throws InvocationTargetException, 
                                  InterruptedException {
    DelayEvent event = new DelayEvent(TIMEOUT + BUFFER);
    postEvent(frame, event);
    flushQueue();
    assertEquals("1 cursor set", 1, frame.getCursorSetCount());
    assertEquals("1 cursor reset", 1, 
                 frame.getCursorResetCount());
  }
  public void testDialog() throws InvocationTargetException, 
                                  InterruptedException {
    postEvent(frame, new DialogShowEvent(dialog, true, 0));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog never got cursor", 0, 
                 dialog.getCursorSetCount());
    assertEquals("dialog never reset cursor", 0, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got cursor", 0, 
                 frame.getCursorSetCount());
    assertEquals("frame never reset cursor", 0, 
                 frame.getCursorResetCount());
    postEvent(dialog, new DialogShowEvent(dialog, false, 0));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog never got cursor", 0, 
                 dialog.getCursorSetCount());
    assertEquals("dialog never reset cursor", 0, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got cursor", 0, 
                 frame.getCursorSetCount());
    assertEquals("frame never reset cursor", 0, 
                 frame.getCursorResetCount());
  }
  public void testCursorAndDialog()
                           throws InvocationTargetException, 
                                  InterruptedException {
    TestRunnable testAndShow = new TestRunnable() {
      public void run() {
        testsPassed &= (1 == frame.getCursorSetCount());
        testsPassed &= (0 == frame.getCursorResetCount());
        dialog.setVisible(true);
      }
    };
    postEvent(frame, 
              new DelayEvent(TIMEOUT + BUFFER, testAndShow));
    flushQueue();
    assertTrue("Delay worked", testAndShow.getTestsPassed());
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog never got cursor", 0, 
                 dialog.getCursorSetCount());
    assertEquals("dialog never reset cursor", 0, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got another cursor", 1, 
                 frame.getCursorSetCount());
    assertEquals("frame reset cursor", 1, 
                 frame.getCursorResetCount());
    postEvent(dialog, new DialogShowEvent(dialog, false, 0));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog never got cursor", 0, 
                 dialog.getCursorSetCount());
    assertEquals("dialog never reset cursor", 0, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got another cursor", 1, 
                 frame.getCursorSetCount());
    assertEquals("frame never reset another cursor", 1, 
                 frame.getCursorResetCount());
  }
  public void testCursorAndDialogAndCursor()
    throws InvocationTargetException, InterruptedException {
    TestRunnable testAndShow = new TestRunnable() {
      public void run() {
        testsPassed &= (1 == frame.getCursorSetCount());
        testsPassed &= (0 == frame.getCursorResetCount());
        dialog.setVisible(true);
        hangOut(TIMEOUT + BUFFER);
      }
    };
    postEvent(frame, 
              new DelayEvent(TIMEOUT + BUFFER, testAndShow));
    flushQueue();
    assertTrue("Delay worked", testAndShow.getTestsPassed());
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog never got cursor", 0, 
                 dialog.getCursorSetCount());
    assertEquals("dialog never reset cursor", 0, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got another cursor", 1, 
                 frame.getCursorSetCount());
    assertEquals("frame reset cursor", 1, 
                 frame.getCursorResetCount());
    postEvent(dialog, new DialogShowEvent(dialog, false, 0));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog never got cursor", 0, 
                 dialog.getCursorSetCount());
    assertEquals("dialog never reset cursor", 0, 
                 dialog.getCursorResetCount());
    assertEquals("frame got another cursor", 2, 
                 frame.getCursorSetCount());
    assertEquals("frame reset another cursor", 2, 
                 frame.getCursorResetCount());
  }
  /**
   * This test checks the condition where the EventDispatchThread
   * does not call EventQueue.getNextEvent() within TIMEOUT, 
   * (presumably) because of thread contention, even when there 
   * is not a dialog going down. Note that this case only actually 
   * matters if there is a dialog currently up.
   */
  public void testDelayedGetNextEvent()
                               throws InvocationTargetException, 
                                      InterruptedException {
    postEvent(frame, new DialogShowEvent(dialog, true, 0));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    eventQueue.setGetDelay(TIMEOUT + BUFFER);
    postEvent(frame, new DelayEvent(TIMEOUT - BUFFER));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    assertEquals("frame got a cursor", 1, 
                 frame.getCursorSetCount());
    assertEquals("frame reset a cursor", 1, 
                 frame.getCursorResetCount());
    postEvent(dialog, new DialogShowEvent(dialog, false, 0));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    assertEquals("frame did not get another cursor", 1, 
                 frame.getCursorSetCount());
    assertEquals("frame did not reset another cursor", 1, 
                 frame.getCursorResetCount());
  }
  public void testTwoDialogs() throws InvocationTargetException, 
                                      InterruptedException {
    TestRunnable testAndShow = new TestRunnable() {
      public void run() {
        testsPassed &= (1 == frame.getCursorSetCount());
        testsPassed &= (0 == frame.getCursorResetCount());
        dialog.setVisible(true);
      }
    };
    postEvent(frame, 
              new DelayEvent(TIMEOUT + BUFFER, testAndShow));
    flushQueue();
    assertTrue("Delay worked", testAndShow.getTestsPassed());
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog never got cursor", 0, 
                 dialog.getCursorSetCount());
    assertEquals("dialog never reset cursor", 0, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got another cursor", 1, 
                 frame.getCursorSetCount());
    assertEquals("frame reset cursor", 1, 
                 frame.getCursorResetCount());
    TestRunnable testAndShow2 = new TestRunnable() {
      public void run() {
        testsPassed &= (1 == dialog.getCursorSetCount());
        testsPassed &= (0 == dialog.getCursorResetCount());
        dialog2.setVisible(true);
      }
    };
    postEvent(dialog, 
              new DelayEvent(TIMEOUT + BUFFER, testAndShow2));
    flushQueue();
    assertTrue("Delay worked", testAndShow.getTestsPassed());
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog2 never got cursor", 0, 
                 dialog2.getCursorSetCount());
    assertEquals("dialog2 never reset cursor", 0, 
                 dialog2.getCursorResetCount());
    assertEquals("dialog never got another cursor", 1, 
                 dialog.getCursorSetCount());
    assertEquals("dialog reset cursor", 1, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got another cursor", 1, 
                 frame.getCursorSetCount());
    assertEquals("frame reset cursor", 1, 
                 frame.getCursorResetCount());
    postEvent(dialog2, new DelayEvent(TIMEOUT + BUFFER));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog2 got cursor", 1, 
                 dialog2.getCursorSetCount());
    assertEquals("dialog2 reset cursor", 1, 
                 dialog2.getCursorResetCount());
    assertEquals("dialog never got another cursor", 1, 
                 dialog.getCursorSetCount());
    assertEquals("dialog reset cursor", 1, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got another cursor", 1, 
                 frame.getCursorSetCount());
    assertEquals("frame reset cursor", 1, 
                 frame.getCursorResetCount());
    postEvent(dialog2, new DialogShowEvent(dialog2, false, 0));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog2 never got another cursor", 1, 
                 dialog2.getCursorSetCount());
    assertEquals("dialog2 never reset another cursor", 1, 
                 dialog2.getCursorResetCount());
    assertEquals("dialog never got another cursor", 1, 
                 dialog.getCursorSetCount());
    assertEquals("dialog never reset another cursor", 1, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got another cursor", 1, 
                 frame.getCursorSetCount());
    assertEquals("frame never reset another cursor", 1, 
                 frame.getCursorResetCount());
    postEvent(dialog, new DialogShowEvent(dialog, false, 0));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog2 never got another cursor", 1, 
                 dialog2.getCursorSetCount());
    assertEquals("dialog2 never reset another cursor", 1, 
                 dialog2.getCursorResetCount());
    assertEquals("dialog never got another cursor", 1, 
                 dialog.getCursorSetCount());
    assertEquals("dialog never reset another cursor", 1, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got another cursor", 1, 
                 frame.getCursorSetCount());
    assertEquals("frame never reset another cursor", 1, 
                 frame.getCursorResetCount());
  }
  public void testCursorAndTwoDialogsAndCursor()
    throws InvocationTargetException, InterruptedException {
    postEvent(frame, new DialogShowEvent(dialog, true, 0));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog never got cursor", 0, 
                 dialog.getCursorSetCount());
    assertEquals("dialog never reset cursor", 0, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got cursor", 0, 
                 frame.getCursorSetCount());
    assertEquals("frame never reset cursor", 0, 
                 frame.getCursorResetCount());
    TestRunnable testAndShow = new TestRunnable() {
      public void run() {
        testsPassed &= (1 == dialog.getCursorSetCount());
        testsPassed &= (0 == dialog.getCursorResetCount());
        dialog2.setVisible(true);
        hangOut(TIMEOUT + BUFFER);
      }
    };
    postEvent(dialog, 
              new DelayEvent(TIMEOUT + BUFFER, testAndShow));
    flushQueue();
    assertTrue("Delay worked", testAndShow.getTestsPassed());
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog2 never got cursor", 0, 
                 dialog2.getCursorSetCount());
    assertEquals("dialog2 never reset cursor", 0, 
                 dialog2.getCursorResetCount());
    assertEquals("dialog never got another cursor", 1, 
                 dialog.getCursorSetCount());
    assertEquals("dialog reset cursor", 1, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got cursor", 0, 
                 frame.getCursorSetCount());
    assertEquals("frame never reset cursor", 0, 
                 frame.getCursorResetCount());
    postEvent(dialog2, new DialogShowEvent(dialog2, false, 0));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog2 never got cursor", 0, 
                 dialog2.getCursorSetCount());
    assertEquals("dialog2 never reset cursor", 0, 
                 dialog2.getCursorResetCount());
    assertEquals("dialog got another cursor", 2, 
                 dialog.getCursorSetCount());
    assertEquals("dialog reset another cursor", 2, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got cursor", 0, 
                 frame.getCursorSetCount());
    assertEquals("frame never reset cursor", 0, 
                 frame.getCursorResetCount());
    postEvent(dialog, new DialogShowEvent(dialog, false, 0));
    flushQueue();
    hangOut(TIMEOUT + BUFFER);
    assertEquals("dialog2 never got cursor", 0, 
                 dialog2.getCursorSetCount());
    assertEquals("dialog2 never reset cursor", 0, 
                 dialog2.getCursorResetCount());
    assertEquals("dialog never got another cursor", 2, 
                 dialog.getCursorSetCount());
    assertEquals("dialog never reset another cursor", 2, 
                 dialog.getCursorResetCount());
    assertEquals("frame never got cursor", 0, 
                 frame.getCursorSetCount());
    assertEquals("frame never reset cursor", 0, 
                 frame.getCursorResetCount());
  }

  private class CursorReportingDialog extends Dialog {
    private int cursorResetCount;
    private int cursorSetCount;

    public CursorReportingDialog(Frame owner) {
      super(owner, true);
      init();
    }
    public CursorReportingDialog(Dialog owner) {
      super(owner, "", true);
      init();
    }

    private void init() {
      setCursor(BASE_CURSOR);
      this.cursorSetCount = 0;
      this.cursorResetCount = 0;
    }
    public int getCursorSetCount() {
      return cursorSetCount;
    }
    public int getCursorResetCount() {
      return cursorResetCount;
    }
    public void setCursor(Cursor cursor) {
      super.setCursor(cursor);
      if (BASE_CURSOR.equals(cursor)) {
        cursorResetCount++;
      } else {
        cursorSetCount++;
      }
    }
  }

  private class CursorReportingFrame extends Frame {
    private int cursorResetCount;
    private int cursorSetCount;

    public CursorReportingFrame() {
      super();
      setCursor(BASE_CURSOR);
      cursorSetCount = 0;
      cursorResetCount = 0;
    }

    public int getCursorSetCount() {
      return cursorSetCount;
    }
    public int getCursorResetCount() {
      return cursorResetCount;
    }
    public void setCursor(Cursor cursor) {
      super.setCursor(cursor);
      if (BASE_CURSOR.equals(cursor)) {
        cursorResetCount++;
      } else {
        cursorSetCount++;
      }
    }
  }

  private abstract class TestRunnable implements Runnable {
    protected boolean testsPassed = true;

    public boolean getTestsPassed() {
      return testsPassed;
    }
  }

  private class DelayEvent implements Runnable {
    private Runnable callback;
    private int delay;

    public DelayEvent(int delay) {
      this(delay, null);
    }
    public DelayEvent(int delay, Runnable callback) {
      this.delay = delay;
      this.callback = callback;
    }

    public void run() {
      try {
        Thread.sleep(delay);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (callback != null) {
        callback.run();
      }
    }
  }

  private class DialogShowEvent implements Runnable {
    private Dialog dialog;
    private boolean visible;
    private int delay;

    public DialogShowEvent(Dialog dialog, boolean visible, 
                           int delay) {
      this.dialog = dialog;
      this.visible = visible;
      this.delay = delay;
    }

    public void run() {
      dialog.setVisible(visible);
      if (delay > 0) {
        hangOut(delay);
      }
    }
  }

  private class TestWaitCursorEventQueue
      extends WaitCursorEventQueue {
    private int getDelay;

    public TestWaitCursorEventQueue(int delay) {
      super(delay);
    }

    public AWTEvent getNextEvent() throws InterruptedException {
      if (getDelay > 0) {
        hangOut(getDelay);
        getDelay = 0;
      }
      return super.getNextEvent();
    }
    public void setGetDelay(int getDelay) {
      this.getDelay = getDelay;
    }
  }
}	