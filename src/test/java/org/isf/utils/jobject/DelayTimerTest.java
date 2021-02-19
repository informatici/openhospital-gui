package org.isf.utils.jobject;

import org.isf.utils.jobjects.DelayTimer;
import org.isf.utils.jobjects.DelayTimerCallback;

import junit.framework.TestCase;

public class DelayTimerTest extends TestCase {
  private static final int TIMEOUT = 100;
  private static final int BUFFER = 20;
  private static final int MORE_THAN_HALF = 60;
  private DelayTimer timer;
  private TestDelayTimerCallback callback;

  public DelayTimerTest(String name) {
    super(name);
  }

  public void setUp() {
    callback = new TestDelayTimerCallback();
    timer = new DelayTimer(callback, TIMEOUT);
  }

  public void tearDown() {
    timer.quit();
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void testNotStarted() {
    sleep(TIMEOUT + BUFFER);
    assertEquals("no trigger without start()", 0, 
                 callback.getTriggerCount());
  }

  public void testNoTriggerIfTooShort() {
    timer.startTimer();
    assertEquals("no trigger if too fast", 0, 
                 callback.getTriggerCount());
    timer.stopTimer();
    sleep(TIMEOUT + BUFFER);
    assertEquals("no trigger after stop", 0, 
                 callback.getTriggerCount());
  }

  public void testTimerRestarts() {
    timer.startTimer();
    sleep(MORE_THAN_HALF);
    timer.startTimer();
    sleep(MORE_THAN_HALF);
    timer.stopTimer();
    assertEquals("timer is restarted on calls to start()", 0, 
                 callback.getTriggerCount());
  }

  public void testTimerTriggersThenStops() {
    timer.startTimer();
    sleep(TIMEOUT + BUFFER);
    timer.stopTimer();
    sleep(TIMEOUT + BUFFER);
    assertEquals("timer triggered event", 1, 
                 callback.getTriggerCount());
  }

  public void testTimerOnlyTriggersOneEvent() {
    timer.startTimer();
    sleep(TIMEOUT + BUFFER);
    assertEquals("timer triggered event", 1, 
                 callback.getTriggerCount());
    sleep(TIMEOUT + BUFFER);
    assertEquals("timer did not trigger another event", 1, 
                 callback.getTriggerCount());
    timer.stopTimer();
  }

  public void testTimerStopsTwice() {
    timer.startTimer();
    timer.stopTimer();
    timer.stopTimer();
    sleep(TIMEOUT + BUFFER);
    assertEquals("timer did not trigger event", 0, 
                 callback.getTriggerCount());
  }

  public void testTriggers() {
    timer.startTimer();
    sleep(TIMEOUT + BUFFER);
    timer.stopTimer();
    timer.stopTimer();
    assertEquals("timer triggered only 1 event", 1, 
                 callback.getTriggerCount());
  }

  public void testTriggerTrigger() {
    timer.startTimer();
    sleep(TIMEOUT + BUFFER);
    assertEquals("timer triggered first event", 1, 
                 callback.getTriggerCount());
    timer.startTimer();
    sleep(TIMEOUT + BUFFER);
    assertEquals("timer triggered second event", 2, 
                 callback.getTriggerCount());
    sleep(TIMEOUT + BUFFER);
    timer.stopTimer();
    assertEquals("timer did not trigger another event", 2, 
                 callback.getTriggerCount());
  }

  public void testStopTimerHappensAfterTrigger() {
    FancyTestDelayTimerCallback callback = new FancyTestDelayTimerCallback();
    timer = new DelayTimer(callback, TIMEOUT);

    timer.startTimer();
    sleep(TIMEOUT + BUFFER);
    assertTrue("timer is in trigger", callback.inTrigger);
    assertTrue("timer has not thrown exception", 
               !callback.exception);
    assertTrue("timer is not out of trigger", 
               !callback.outOfTrigger);

    Runnable runnable = new Runnable() {
      public void run() {
        timer.stopTimer();
      }
    };

    Thread testThread = new Thread(runnable, "test thread");
    testThread.start();

    sleep(TIMEOUT + BUFFER);

    assertTrue("stopTimer() has not returned", 
               testThread.isAlive());
    synchronized (callback) {
      callback.notify();
    }

    sleep(TIMEOUT + BUFFER);

    assertTrue("timer has not thrown exception", 
               !callback.exception);
    assertTrue("timer is out of trigger", callback.outOfTrigger);
    assertTrue("stopTimer() has returned", !testThread.isAlive());
  }

  public void testMishMash() {
    timer.startTimer();
    sleep(MORE_THAN_HALF);
    timer.startTimer();
    sleep(MORE_THAN_HALF);
    timer.stopTimer();
    sleep(MORE_THAN_HALF);
    timer.startTimer();
    timer.stopTimer();
    timer.stopTimer();
    sleep(MORE_THAN_HALF);
    timer.startTimer();
    timer.startTimer();
    assertEquals("no event yet", 0, callback.getTriggerCount());
    sleep(TIMEOUT + BUFFER);
    timer.stopTimer();
    assertEquals("got event yet", 1, callback.getTriggerCount());
  }

  private class FancyTestDelayTimerCallback
      implements DelayTimerCallback {
    public boolean exception;
    public boolean inTrigger;
    public boolean outOfTrigger;

    public synchronized void trigger() {
      inTrigger = true;

      try {
        wait();
      } catch (InterruptedException e) {
        exception = true;
        e.printStackTrace();
      } finally {
        outOfTrigger = true;
      }
    }
  }

  private class TestDelayTimerCallback
      implements DelayTimerCallback {
    private int triggerCount;

    public int getTriggerCount() {
      return triggerCount;
    }

    public void trigger() {
      triggerCount++;
    }
  }
}