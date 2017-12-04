package edu.ciw.hpcat.epics.net;

import java.net.InetAddress;

public class NetParms {
	 String netMaskStr = "Not defined";
	  InetAddress netMaskAddr = null;
	  InetAddress ipAdd;
	  private static NetParms netParms = new NetParms();
	  private NetParms() {

	    netMaskStr = System.getenv("EPICS_NETMASK");
	    if (netMaskStr == null) {
	      netMaskStr = "255.255.255.0";
	    }
	  }

	  public static NetParms getInstance() {
	    return netParms;
	  }


	  public void setNetMask(String str) {
	    netMaskStr = str;
	  }

	  public InetAddress getNetMaskAddr() {
	    try {
	      netMaskAddr = InetAddress.getByName(netMaskStr);
	    }
	    catch (Exception e) {

	    }

	    return netMaskAddr;
	  }


}
