package org.isf.utils.jobjects;

public class EQPermit implements EQPermitMXBean {
    private volatile boolean permit = true;
    @Override
    public void flipPermit() {
      // TODO Auto-generated method stub
      permit=!permit;
    }
  
    @Override
    public boolean getPermit() {
      // TODO Auto-generated method stub
      return permit;
    }
    
  }