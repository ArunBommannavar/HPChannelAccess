package edu.ciw.hpcat.epics.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.StringTokenizer;

public class OsInfo {
	 private String osArch = "";
	  private InetAddress ipAddr;
	  private InetAddress broadcastAddress;
	  private InetAddress subNetMask;
	  String infoLine = "";
	  String commandStr = "";
	  String linuxInetLine = "inet addr:";
	  String linuxBroadcastLine = "Bcast";
	  String linuxMaskLine = "Mask:";

	  String sparcInetLine = "inet";
	  String sparcBroadcastLine = "broadcast";
	  String sparcMaskLine = "netmask";

	  String macInetLine = "inet";
	  String macBroadcastLine = "broadcast";
	  String macMaskLine = "netmask";

	  String inetLine = "inet";
	  String broadcast = "cast";
	  String winLine = "";

	  public OsInfo() {

	  }

	  public void processInfo() {
	    String str;
	    String strInet;
	    String strMask="";

	    StringTokenizer st = new StringTokenizer(infoLine);

	    int index0;
	    int index1;

	    while (st.hasMoreTokens()) {
	      str = st.nextToken();
	      index0 = str.indexOf(":") + 1;
	      index1 = str.length();


//	      if(str.indexOf("inet") > -1){
	      if (str.contains("inet")) {
	        strInet = str.substring(index0, index1);
	  //      System.out.println(" Inet = " + strInet);
	        try {
	          ipAddr = InetAddress.getByName(strInet);
	        }
	        catch (IOException e) {

	        }
	      }


	      else if (str.contains("mask")) {
	        strMask = str.substring(index0, index1);
	        System.out.println(" Mask = " + strMask);

	        if (osArch.equals("solaris-sparc")) {
	          long sparcMask = Long.parseLong(strMask,16);
	          subNetMask = this.getSparcMask(sparcMask);
	        }
	        else {
	          try {
	            subNetMask = InetAddress.getByName(strMask);

	          }
	          catch (IOException e) {

	          }
	        }
	      }
	    }
	  }

	  public InetAddress getSparcMask(long nm) {
	    InetAddress sparcmask = null;
	    byte[] mm = new byte[4];

	    mm[3] = (byte) ( (0x000000FF & nm) >> 0);
	    mm[2] = (byte) ( (0x0000FF00 & nm) >> 8);
	    mm[1] = (byte) ( (0x00FF0000 & nm) >> 16);
	    mm[0] = (byte) ( (0xFF000000 & nm) >> 24);

	    try {
	      sparcmask = InetAddress.getByAddress(mm);
	    }
	    catch (IOException e) {

	    }

	    return sparcmask;
	  }

	  private String getOsType() {
	    String osname = System.getProperty("os.name", "");
	    float osversion = 0;

	    try {
	      osversion = NumberFormat.getInstance().parse(System.getProperty(
	          "os.version", "")).floatValue();
	    }
	    catch (ParseException pe) {
	    }
	    String osarch = System.getProperty("os.arch", "");

	    if (osarch.equals("i386") || osarch.equals("i486") || osarch.equals("i586")) {
	      osarch = "x86";
	    }

	    if (osname.equals("SunOS")) {
	      if (osversion >= 5) {
	        if (osarch.equals("sparc")) {
	          commandStr = "ifconfig eri0";
	          return "solaris-sparc";
	        }
	        else if (osarch.equals("x86")) {
	          commandStr = "ifconfig -a";
	          return "solaris-x86";
	        }
	      }
	    }
	    else if (osname.equals("Mac OS X")) {
	      if (osarch.equals("ppc")) {
	        commandStr = "ifconfig -a";
	        return "darwin-ppc";
	      }
	      else if (osarch.equals("x86")) {
	        commandStr = "ifconfig -a";
	        return "darwin-x86";
	      }
	    }
	    else if (osname.equals("Linux")) {
	      if (osarch.equals("x86")) {
	        commandStr = "ifconfig eth0";
	        return "linux-x86";
	      }
	      else if (osarch.equals("x86_64") || osarch.equals("amd64")) {
	        commandStr = "ifconfig eth0";
	        return "linux-x86_64";
	      }
	    }
	    else if (osname.startsWith("Win")) {
	      commandStr = "ipconfig";
	      return "win32-x86";
	    }
	    return "unknown";
	  }

	  private String getInfoLine() {

	    String line = "Grrr";
	    String oline;
	    BufferedReader outputReader = null;
	    Process proc;
	    int indexS;
	    int indexE;
	    if (!osArch.startsWith("win")) {

	      //    commandStr = "ifconfig -a";
	      try {
	        proc = Runtime.getRuntime().exec(commandStr);
	        outputReader = new BufferedReader(new InputStreamReader(proc.
	            getInputStream()));
	        while (true) {
	          try {
	            while (outputReader.ready()) {
	              line = outputReader.readLine(); // see if the netmask is in this line

	              if (line.contains(inetLine) && line.contains(broadcast)) {

	                if(osArch.startsWith("linux")){

	                  line = line.replace("inet addr", "inet");
	            line = line.replace("Mask", "netmask");


	                }else if(osArch.equals("solaris-sparc")){
	                  line = line.replace("inet ", "inet:");
	                  line = line.replace("netmask ", "netmask:");

	                }


	                winLine = line;
	                break;
	              }
	            }
	            // see if the process has exited
	            int exitValue = proc.exitValue();
	                 System.out.println("Process exited with code: " + exitValue);
	            break;
	          }
	          catch (IOException ioex) {
	            ioex.printStackTrace();
	          }
	          catch (IllegalThreadStateException itex) {
	            // program is still running!
	          }
	        }
	      }
	      catch (Exception e) {
	        e.printStackTrace();
	      }
	    }
	    else {
	      commandStr = "ipconfig";
	      try {
	        proc = Runtime.getRuntime().exec(commandStr);
	        outputReader = new BufferedReader(new InputStreamReader(proc.
	            getInputStream()));

	        while (true) {
	          try {
	            while (outputReader.ready()) {
	              line = outputReader.readLine(); // see if the netmask is in this line

	              if (line.contains("IP Address")) {

	                indexS = line.indexOf(":") + 1;
	                indexE = line.length();
	                oline = line.substring(indexS, indexE);
	                oline = "inet:" + oline.trim();
	                winLine = oline;
	              }
	              if (line.contains("Subnet Mask")) {

	                indexS = line.indexOf(":") + 1;
	                indexE = line.length();
	                oline = line.substring(indexS, indexE);
	                oline = " netmask:" + oline.trim();
	                winLine += oline;

	                break;
	              }
	            }
	            // see if the process has exited
	            int exitValue = proc.exitValue();
	                 System.out.println("Process exited with code: " + exitValue);
	            break;
	          }
	          catch (IOException ioex) {
	            ioex.printStackTrace();
	          }
	          catch (IllegalThreadStateException itex) {
	            // program is still running!
	          }
	        }

	      }
	      catch (IOException e) {

	      }
	    }

//	    System.out.println("Line =" + line+"   "+winLine);


	    getCleanedLineInfo();
	    return winLine;

	  }

	  public InetAddress getBroadcastMask() {


	    return broadcastAddress;
	  }

	  private void getCleanedLineInfo() {

	    StringTokenizer st = new StringTokenizer(infoLine);
	    String newLine = "";
	    while (st.hasMoreTokens()) {
	      String str = st.nextToken();
	      if (!str.contains("cast")) {
	   //     if (!(str.indexOf("cast") > -1)) {
	        newLine += " " + str;
	      }

	    }
	    infoLine = newLine;

	    // System.out.println(" New Info Line = "+infoLine);

	  }


	  public InetAddress getIpAdd() {

	    if (osArch.startsWith("win")) {
	      try {

	        ipAddr = InetAddress.getLocalHost();
	      }
	      catch (Exception e) {
	        e.printStackTrace();
	      }
	    }
	    return ipAddr;
	  }

	  private InetAddress getBroadcastMask(InetAddress ip, InetAddress msk) {
	    InetAddress addr = null;
	    // (localIPAddress & subnetMask) | (0xffffffff & ~subnetMask)


	    byte[] ipBytes = new byte[4];
	    byte[] maskBytes = new byte[4];
	    byte[] bMask = new byte[4];

	    ipBytes = ip.getAddress();
	    maskBytes = msk.getAddress();

	    for (int i = 0; i < 4; i++) {
	      bMask[i] = (byte) ( (ipBytes[i] & maskBytes[i]) | (0xFF & ~maskBytes[i]));
	    }
	    try {
	      addr = InetAddress.getByAddress(bMask);
	    }
	    catch (IOException e) {

	    }
	    return addr;
	  }


	public void init(){
	  osArch = getOsType();
	  infoLine = getInfoLine();


	  processInfo();
	  broadcastAddress = getBroadcastMask(ipAddr,subNetMask);

	}
	  public static void main(String[] args) {
	    OsInfo osinfo = new OsInfo();
	    osinfo.init();
	    System.out.println(" IPAddress = "+osinfo.getIpAdd()+"  Broadcast Mask = "+osinfo.getBroadcastMask());

	  }

}
