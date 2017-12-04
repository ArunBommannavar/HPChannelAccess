package edu.ciw.hpcat.epics.net;

public class BootinIoc {
	  long beaconTime=0L;

	  public BootinIoc() {
	  }

	  public void setBeaconTime(long l){
	    beaconTime = l;
	  }

	  public long getBeaconTime(){
	    return beaconTime;
	  }
}
