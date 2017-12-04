package edu.ciw.hpcat.epics.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class CaAddrList {
	  private static CaAddrList caAddrList = new CaAddrList();
	  String tempString = "";
	  List<Object> caList = Collections.synchronizedList(new ArrayList<Object>());
	  boolean caAutoAddrList = true;

	  private CaAddrList() {
	    tempString = System.getenv("EPICS_CA_ADDR_LIST");
	    if (tempString != null) {
	      stuffCaAddrList();
	    }
	  }

	  public static CaAddrList getInstance() {
	    return caAddrList;
	  }

	  private void stuffCaAddrList() {
	    StringTokenizer st = new StringTokenizer(tempString, ";");
	    String str;

	    while (st.hasMoreTokens()) {
	      str = st.nextToken();
	      addCaAddr(str);

	    }
	  }
	  
/*	  
	private void stuffCaAutoAddrList(){
	  tempString = tempString.trim();
	  tempString = tempString.toUpperCase();
	  if (tempString.equals("NO")){
	    caAutoAddrList = false;
	  }

	}
	*/
	  public void addCaAddr(String str) {
	    str = str.trim();
	    if (str.length()>0){
	      if (!caList.contains(str)) {
	        caList.add(str);

	      }
	    }
	  }

	  synchronized public List<Object> getCaAddrList() {
	    return caList;
	  }

	public boolean getCaAutoAddrList(){
	  return caAutoAddrList;
	}
	  public void clearCaAddrList() {
	    caList.clear();
	  }
}
