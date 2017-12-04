package edu.ciw.hpcat.epics.dataDefs;

public class EpicsString {
	   int len = 0;
	    public EpicsString() {
	  }


	  public int getStringLength(byte[] b){


	    for (int i=0;i<b.length; i++){
	      if(b[i] ==0x00){
	        len = i;
	        break;
	      }
	    }


	    return len;

	  }

	  public String getString(byte[] b,int length){
	    String str="";
	    str = new String(b,0,length);
//	    System.out.println(" String in new class = "+str);
	    return str;
	  }
}
