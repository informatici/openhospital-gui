package org.isf.utils.jobjects;

import java.awt.*;
import java.util.Stack;

class CursorManager {
  private final DelayTimer waitTimer;
  private final Stack<DispatchedEvent> dispatchedEvents;
  private boolean needsCleanup;

  public CursorManager(DelayTimer waitTimer) {
    this.dispatchedEvents = new Stack<DispatchedEvent>();
    this.waitTimer = waitTimer;
  }
  private void cleanUp() {
    if (dispatchedEvents.peek().resetCursor()) {
      clearQueueOfInputEvents();
    }
  }
  private void clearQueueOfInputEvents() {
    final EventQueue systemEventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
    if (systemEventQueue instanceof WaitCursorEventQueue) {
      final WaitCursorEventQueue waitCursorEventQueue = (WaitCursorEventQueue) systemEventQueue;
      for (final AWTEvent nonInputEvent : waitCursorEventQueue.getNonInputEvents()) {
        waitCursorEventQueue.postEvent(nonInputEvent);
      }
    }
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
