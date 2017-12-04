package edu.ciw.hpcat.epics.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ciw.hpcat.epics.data.CountDownConnection;

public class HpRepeaterClient implements Runnable{

	  private short m_cmmd;
	  private short m_type;
	  private short m_count;
	  private int m_cid;
	  private DatagramSocket socket = null;
	  private DatagramPacket dPacket;
	  private InetAddress address;
	  private String localLoopback = "127.0.0.1";

	  private CaHdrIn caHdrIn;
	  private CaHdrOut caHdrOut = new CaHdrOut();
	  private volatile Thread blinker;

	  private byte[] bufSend;
	  private byte[] bufGet = new byte[1024];
	  private int totalBytesInPacket;
	  private ByteArrayInputStream dpByteIoc;
	  private DataInputStream dpDataIoc;
	  private boolean joined = false;
	  private String beaconIpAddress;
	  private Set<String> beaconIocSet = new HashSet<String>();
	  private Map<String,Object> bootingIocMap = new HashMap<String,Object>();
	  private Set<String> disconnectedIocIpAddress = new HashSet<String>();
	  long testBeat;
	  long beaconTime;
	  BootinIoc bootingIoc = null;
	  long bootingDelta = 0;

	  CountDownConnection countDownConnection = CountDownConnection.getInstance();

	  private boolean joining = false;
	  /**
	   * deadEpicsDataObjectList contains EpicsDataObjects that were
	   * disconnected due to an IOC being dead.
	   */
	  private List<Object> deadEpicsDataObjectList = Collections.synchronizedList(new ArrayList<Object>());

	  /**
	   * Lets make sure that access to HashMap is synchronized
	   */
	  private Map<String,Object> iocMap = Collections.synchronizedMap(new HashMap<String,Object>());
	  /**
	   * handle to UdpSearch instance
	   */

	  UdpSearch udpSearch;

	  public HpRepeaterClient() {
	    try {

	      socket = new DatagramSocket();
	    }
	    catch (IOException ie) {
//	    System.out.println(" IOException " + ie.getMessage());
	    }
	  }

	  public boolean getJoined() {
	    return joined;
	  }

	  private void sendJoin() {
	    try {
	      address = InetAddress.getByName(localLoopback);
	      bufSend = getJoinBytes();
	      joined = true;

	      dPacket = new DatagramPacket(bufSend, bufSend.length, address, 5065);
	      socket.send(dPacket);

	    }
	    catch (IOException e) {
	      joined = false;
	    }
	  }

	  private byte[] getJoinBytes() {
	    byte[] bytes;

	    caHdrOut.clearBuffer();

	    m_cmmd = (short) 0x18;
	    m_type = (short) 0x00;
	    m_count = (short) 0x00;
	    m_cid = (int) 0x00;

	    caHdrOut.setCommand(m_cmmd);
	    caHdrOut.setDataType(m_type);
	    caHdrOut.setCount(m_count);
	    caHdrOut.setCid(m_cid);
	    caHdrOut.setAvailable(address);
	    bytes = caHdrOut.getByteArray();
	    return bytes;

	  }

	  public void start() {
	    blinker = new Thread(this);
	    blinker.start();
	    sendJoin();
	  }

	  public void stop() {
	    Thread tmpBlinker = blinker;
	    blinker = null;
	    if (tmpBlinker != null) {
	      tmpBlinker.interrupt();
	    }
	  }

	  public boolean isAlive() {
	    boolean returnVal = false;
	    if (blinker != null) {
	      return blinker.isAlive();
	    }
	    else {
	      return returnVal;
	    }
	  }

	  public void addIoc(String str, Ioc ioc) {
//	    System.out.println(" Adding IOC to set in repeater client");
	    if (!iocMap.containsKey(str)) {

	      iocMap.put(str, ioc);
	    }
	  }

	  public void setUdpSearch(UdpSearch u) {
	    udpSearch = u;
	  }

	  private void checkOnIoc() {
	    Set<String> iocKeys = iocMap.keySet();

	    long delta;
	    long iocLastBeaconTime = 0;
	    Ioc ioc;
	    int connectCounter;

	    for (Iterator<String> I = iocKeys.iterator(); I.hasNext(); ) {

	      ioc = (Ioc) iocMap.get(I.next());
	      iocLastBeaconTime = ioc.getLastBeaconTime();
	      delta = (System.currentTimeMillis() - iocLastBeaconTime) / 1000L;
	      connectCounter = ioc.getConnectCounter();

	      /**
	       * connectCounter is a counter
	       * It keeps a count of IocTcp checking on the
	       * connection to the IOC
	       * If it is more than 3 then obviously the
	       * IOC is dead.
	       */
	      if (delta > 16.0) {
	        ioc.setConnectionCheck(true);
	      }
	      if (connectCounter > 3 && ioc.isAlive()) {

	        /**
	         * First let us stop the ioc
	         */

	        ioc.stop();

	        /**
	         * Next we remove the key corresponding to this ioc from the
	         * Set beaconSet.
	         */
	        beaconIocSet.remove(ioc.getIp());
	        disconnectedIocIpAddress.add(ioc.getIp());

	        /**
	         * Next let the IocTcp init the EDO
	         */
	        ioc.initEdo();
	        udpSearch.dropIoc(ioc.getIp());
	      }
	      /**
	       * Lets ask the iocTcp to check on the connection to the IOC
	       */
	      if (delta > 28 && delta < 1000 && (connectCounter == 0)) {
	        ioc.setConnectionCheck(true);
	      }
	    }

	    for (Iterator<String> I=disconnectedIocIpAddress.iterator(); I.hasNext();){

	     String ii =  I.next().toString();
	      iocMap.remove(ii);
	    }



	  }

	  /**
	   * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread causes
	   * the object's <code>run</code> method to be called in that separately executing thread.
	   *
	   * @todo Implement this java.lang.Runnable method
	   */
	  public void run() {
	    Thread thisThread = Thread.currentThread();
	    while (blinker == thisThread) {
	      int offset = 0;
	      Ioc ioc = null;

	      try {
	        /**
	         * First of all, lets check the health of each
	         * IOC in the Map.
	         */
	        checkOnIoc();

	        dPacket = new DatagramPacket(bufGet, bufGet.length);
	        socket.receive(dPacket);
	        totalBytesInPacket = dPacket.getLength();

	        while (totalBytesInPacket > 0) {

	          dpByteIoc = new ByteArrayInputStream(dPacket.getData(), offset, 16);
	          dpDataIoc = new DataInputStream(dpByteIoc);

	          offset = offset + 16;

	          caHdrIn = new CaHdrIn(dpDataIoc);
	          caHdrIn.readData();

	          m_cmmd = caHdrIn.getCommand();
	          m_cid = caHdrIn.getCid();

	          switch (m_cmmd) {
	            case 0x0D:
	              beaconIpAddress = caHdrIn.getAddress().getHostAddress();
//	              System.out.println(" beacon Address = "+beaconIpAddress);

	              /**
	               * Is this is a new beacon?
	               *    If so, is this a rebooting IOC or a stable one?
	               *
	               */

	              if (this.disconnectedIocIpAddress.contains(beaconIpAddress)) {
	                /**
	                 * We are here because this is an ioc that was there in the list
	                 * before and was disconnected for some reason
	                 * and is booting.
	                 * Lets check if the booting is complete
	                 *
	                 */
	                //                udpSearch.addToSearchList();
	                beaconTime = System.currentTimeMillis() / 1000L;
	                bootingIoc = new BootinIoc();
	                bootingIoc.setBeaconTime(beaconTime);
	                bootingIocMap.put(beaconIpAddress, bootingIoc);
	                disconnectedIocIpAddress.remove(beaconIpAddress);
	              }

	              if (bootingIocMap.containsKey(beaconIpAddress)){
	                bootingIoc = (BootinIoc)bootingIocMap.get(beaconIpAddress);
	                beaconTime = System.currentTimeMillis() / 1000L;
	                  bootingDelta = beaconTime - bootingIoc.getBeaconTime();
	                  if (bootingDelta > 4){
	                    udpSearch.addToSearchList();
	                     bootingIocMap.remove(beaconIpAddress);
	                  }
	              }

	              if (!beaconIocSet.contains(beaconIpAddress) && !bootingIocMap.containsKey(beaconIpAddress) ) {
	                beaconIocSet.add(beaconIpAddress);
	              }

	              beaconTime = System.currentTimeMillis() / 1000L;

	              ioc = (Ioc) iocMap.get(beaconIpAddress);
	              if (ioc != null) {
	                ioc.setLastBeaconTime(System.currentTimeMillis());
	              }
	              break;
	          }
	          totalBytesInPacket = totalBytesInPacket - 16;
	        }
	      }
	      catch (IOException e) {
	        System.out.println(" Recieve exception " + e.getMessage());
	      }
	    }
	  }
}
