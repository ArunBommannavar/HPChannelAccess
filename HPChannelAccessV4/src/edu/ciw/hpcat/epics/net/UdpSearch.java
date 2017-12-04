package edu.ciw.hpcat.epics.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ciw.hpcat.epics.data.Cid;
import edu.ciw.hpcat.epics.data.Connected;
import edu.ciw.hpcat.epics.data.CountDownConnection;
import edu.ciw.hpcat.epics.data.EpicsDataObject;

public class UdpSearch implements Runnable {
	private static UdpSearch udpSearch = new UdpSearch();
	private DatagramSocket so = null;
	private int udpPort = 5064;
	volatile private Thread blinker;
	private InetAddress localAddress;
	private InetAddress broadcastAddress;
	private InetAddress caIpAddress;
	private String localHostName;
	private InetAddress localLoopback;
	private String localLoopbackString = "127.0.0.1";

	private DatagramPacket dpSend;
	private DatagramPacket dpGet;

	private CaHdrOut caHdrOut = new CaHdrOut();
	private volatile int pvNum = 0;
	private InetAddress netMask = null;
	private short m_cmmd;
	private short m_type;
	private short m_count;
	private int m_cid;
	private int m_available;
	private boolean hpCaRepeaterInit = false;
//	private List<Object> searchList = Collections.synchronizedList(new ArrayList<Object>());
	private List<Object> searchList = new ArrayList<Object>();

	private ByteArrayInputStream dpByteLocal;
	private DataInputStream dpDataLocal;

	private ByteArrayInputStream dpByteIoc;
	private DataInputStream dpDataIoc;

	private byte[] bufSend;
//	private byte[] bufGet = new byte[32768];
	private byte[] bufGet = new byte[1024];

	private Map<String,Object> cidEpicsObject = new HashMap<String,Object>();
	private NetParms netParms = NetParms.getInstance();

	/**
	 * mapIoc: key = IPAddress, value = reference to the IocTcp instance
	 */
	private Map<String,Object> mapIoc = new HashMap<String,Object>();
	private Ioc ioc;
	private Ioc dropIocTcp;
	private boolean waitForDp = false;
	private int sentCount;
	private int queryTimeInterval;
	private int totalBytesInPacket;
	boolean caAutoAddrList = true;
	private CaAddrList caAddrList = CaAddrList.getInstance();
	private HpRepeaterClient hpRepeaterClient = null;
	private Process proc;

	Set<Object> notFoundSet = new HashSet<Object>();
	String diagnosticStr;
	volatile boolean lockStatus;
//	volatile boolean searchEnable = false;
	volatile boolean searchEnable = true;
	CountDownConnection countDownConnection = CountDownConnection.getInstance();
	volatile int numberOfLatches = 0;

	private UdpSearch() {

		netMask = netParms.getNetMaskAddr();
		caAutoAddrList = caAddrList.getCaAutoAddrList();
		try {
			localAddress = InetAddress.getLocalHost();
			broadcastAddress = getBroadcastMask(localAddress, netMask);
			localHostName = localAddress.getHostAddress();
			localLoopback = InetAddress.getByName(localLoopbackString);

		} catch (UnknownHostException ue) {
			System.out.println(ue.getMessage());
			System.exit(1);
		}

		catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static UdpSearch getInstance() {
		return udpSearch;
	}

	private InetAddress getBroadcastMask(InetAddress ip, InetAddress msk) {
		InetAddress addr = null;

		byte[] ipBytes = new byte[4];
		byte[] maskBytes = new byte[4];
		byte[] bMask = new byte[4];

		ipBytes = ip.getAddress();
		maskBytes = msk.getAddress();

		for (int i = 0; i < 4; i++) {
			bMask[i] = (byte) ((ipBytes[i] & maskBytes[i]) | (0xFF & ~maskBytes[i]));
		}
		try {
			addr = InetAddress.getByAddress(bMask);
		} catch (IOException e) {

		}
		return addr;
	}

	synchronized public void dropIoc(String str) {
		IocTcp ioc = (IocTcp) mapIoc.get(str);
		ioc = null;
		mapIoc.remove(str);
	}
/*
	public boolean getReadyStatus(){
		boolean ret = false;
		
		while(searchList.size()>0){
			try {
				wait(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
		///		e.printStackTrace();
				System.out.println(" Waited too long");
				return ret;
			}
		}
		ret= true;
		
		return ret;
	}
	*/
	
	/*synchronized */public byte[] makeSearchPacket(Object obj) {

		byte[] searchBytes;
		caHdrOut.clearBuffer();

		String pv = ((EpicsDataObject) obj).getPvName();
		m_cmmd = 6;
		m_type = 5;
		m_count = 8;
		m_cid = ((Cid) obj).getChid();
		m_available = m_cid;

		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_type);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_available);
		caHdrOut.setMessage(pv);
		searchBytes = caHdrOut.getByteArray();
		return searchBytes;
	}

	public void printBytes(byte[] b, String str) {

		for (int i = 0; i < b.length; i++) {
			System.out.println(Integer.toHexString(b[i]));
		}
	}

	synchronized public void addNotFoundPV(Object obj) {

		notFoundSet.add(obj);
		addRemovePv(obj, "remove");
	}

	synchronized public boolean isSearchListEmpty() {
		boolean empty = false;

		if (searchList.size() == 0) {
			empty = true;
		} else {
			empty = false;
		}
		return empty;

	}

	synchronized public void addToSearchList() {
		Object obj;
		Connected cObj;
		for (Iterator<Object> I = notFoundSet.iterator(); I.hasNext();) {
			obj = I.next();
			cObj = (Connected) obj;
			cObj.setUdpSearchCount(0);
			cObj.setSearchable(true);
			cObj.reConnectPropertyChangeListeners();
			addRemovePv(obj, "add");
		}

		if (!isAlive() && searchList.size() > 0) {
			start();
		}
	}

	synchronized public void addRemovePv(Object obj, String str) {

		ArrayList<Object> rrr = new ArrayList<Object>();

		if (str.equals("add") && !searchList.contains(obj)) {
			if (((Connected) obj).getUdpSearchCount() == 0) {
				Cid chidObj = (Cid) obj;
				chidObj.setChid(pvNum);
				cidEpicsObject.put(Integer.toString(pvNum), obj);
				searchList.add(obj);
//				 System.out.println(" UDP Adding "+((PvName)obj).getPvName()+" Chid = "+pvNum);

				pvNum++;

			}
		} else if (str.equals("remove")) {

			if (searchList.contains(obj)) {

				Set<String> keySet = cidEpicsObject.keySet();
				for (Iterator<String> I = keySet.iterator(); I.hasNext();) {
					Object keyObj = I.next();
					if (cidEpicsObject.get(keyObj).equals(obj)) {
						rrr.add(keyObj);
					}
				}

				for (int i = 0; i < rrr.size(); i++) {
					cidEpicsObject.remove(rrr.get(i));
				}
				searchList.remove(obj);
			}
		}
	}

	public void start() {

		blinker = new Thread(this);
		blinker.start();

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
		} else {
			return returnVal;
		}
	}
/*
	public void setSearchEnable(boolean b){
		searchEnable = b;
//		System.out.println(" Enable = "+searchEnable);

	}
	
	public boolean getSearchEnable(){
		return searchEnable;
	}
*/	
	public void run() {
		boolean socketFree = true;
		Connected cObj = null;
		UdpConnectTime uObj = null;
		Thread thisThread = Thread.currentThread();
		long searchTime;
		long searchTimeSince = 0;
		List<Object> caList = caAddrList.getCaAddrList();
		String sst;

		String iocIpAddress;
		int iocPort;
		int foundCid;
		String foundCidStr;
		Object foundObj = null;

		if (hpRepeaterClient == null) {
			try {
				proc = Runtime.getRuntime().exec("caRepeater");
			} catch (Exception e) {

				e.printStackTrace();
			}
			hpRepeaterClient = new HpRepeaterClient();
			hpRepeaterClient.setUdpSearch(this);
			hpRepeaterClient.start();
		}
		while (blinker == thisThread) { //blinker thread
			
			// System.out.println(" UDP search started");
			/*
			 try { Thread.sleep(50); 
			 } 	 
			 catch (InterruptedException e1) 
			 {	 
				 e1.printStackTrace(); 
			 }
			 
	*/
				


			int offset;
			ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			DataOutputStream bufArrayOut = new DataOutputStream(byteArrayOut);

			try {
				if (so == null) {
					// so = new DatagramSocket(udpPort);
					so = new DatagramSocket();
					so.setSoTimeout(100);
				} else {
					if (!so.isClosed()) {
						so.close();
					}
					so = new DatagramSocket();
					so.setSoTimeout(100);
				}
			} catch (SocketException se) {
				if (so != null && so.isBound()) {
					so.disconnect();
					so.close();

				}
				socketFree = false;
			}

			if (socketFree) {/*** Here 1 ***/
				/**
				 * If the EpicsDataObject has been sent for searching more than
				 * 120 mSec, then send it again.
				 */
				numberOfLatches = countDownConnection.getNumberOfLatches();
				if (numberOfLatches ==0 ) continue;
//					System.out.println(" Number of Latches "+numberOfLatches);
				for (int i = 0; i < searchList.size(); i++) {
					if (i > 5)
						continue;
					Object obj = searchList.get(i);
					cObj = (Connected) obj;
					uObj = (UdpConnectTime) obj;

					if (!cObj.isUdpConnected() && cObj.isSearchable()) {

						searchTime = System.currentTimeMillis();
						searchTimeSince = searchTime - uObj.getLastSearchTime();
						
//		Lets send the bytes only if 120 mSec have passed since last time 
						if (searchTimeSince > 120) {
//							 System.out.println(" SearchTimeSince = "+searchTimeSince+"  i = "+i);
							uObj.setLastSearchTime(searchTime);
							byte[] bytes = makeSearchPacket(obj);

							/**
							 * Let the EpicsObject know that a search has been
							 * done and it should up the search count
							 */
							((Connected) obj).updateUdpSearchCount();
							try {
								byteArrayOut.write(bytes);
							} catch (IOException e) {
							}
						}
					}
				}
//			}
				bufSend = byteArrayOut.toByteArray();

				if (bufSend.length > 0) {
					try {
						if (caAutoAddrList) {
							dpSend = new DatagramPacket(bufSend, bufSend.length, broadcastAddress, udpPort);
							so.send(dpSend);
						}
						for (int hi = 0; hi < caList.size(); hi++) {
							sst = caList.get(hi).toString();
							caIpAddress = InetAddress.getByName(sst);
							dpSend = new DatagramPacket(bufSend, bufSend.length, caIpAddress, udpPort);

							so.send(dpSend);
						}
						waitForDp = true;
					} catch (IOException e) {
					}
				}
			} /*** Here 2 ***/
			/**
			 * Now lets check if there are any bytes in the UDP recieve
			 */
			while (waitForDp) { /*** Here 3 ***/

				dpGet = new DatagramPacket(bufGet, bufGet.length);
				try {
					so.receive(dpGet);
				} catch (IOException e) {
				}

				offset = 0;
				totalBytesInPacket = dpGet.getLength();

				if (dpGet.getAddress() != null) {

					if (dpGet.getAddress().getHostAddress().equals(localHostName)) {
						dpByteLocal = new ByteArrayInputStream(dpGet.getData(), offset, dpGet.getLength());
						dpDataLocal = new DataInputStream(dpByteLocal);
						byte[] temp = new byte[dpGet.getLength()];
						try {
							dpDataLocal.read(temp);
						} catch (IOException e) {

						}
					}

					else {
						int totalBytesCounter = 0;
						while (totalBytesInPacket > 0) {

							/**
							 * So the bytes are recived from another host. (Not
							 * from Localhost) So, till all the bytes are read,
							 * put the routine in a while loop and read them.
							 */

							/* Get the IP Address of the IOC that responded */
							iocIpAddress = dpGet.getAddress().getHostAddress();
							dpByteIoc = new ByteArrayInputStream(dpGet.getData(), offset, 16);
							dpDataIoc = new DataInputStream(dpByteIoc);

							offset = offset + 16;

							CaHdrIn caHdrIn = new CaHdrIn(dpDataIoc);
							caHdrIn.readData();
							int comand = caHdrIn.getCommand();
							iocPort = caHdrIn.getType();
							foundCid = caHdrIn.getAvailable();
							foundCidStr = Integer.toString(foundCid);

							// conditionalize for debug
							/*
							 * System.out.println(" Udp Search start\n");
							 * System.out.println("Command = " +
							 * caHdrIn.getCommand()); System.out.println(
							 * "Size    = " + caHdrIn.getPostSize());
							 * System.out.println("Type    = " +
							 * caHdrIn.getType()); System.out.println(
							 * "Count   = " + caHdrIn.getCount());
							 * System.out.println("Cid     = " +
							 * Integer.toHexString(caHdrIn.getCid()));
							 * System.out.println("Avail   = " + foundCid);
							 * System.out.println(" IP Address = " +
							 * iocIpAddress+" iocPort   "+iocPort);
							 * System.out.println(" Local Host Name = "
							 * +localHostName);
							 * 
							 * System.out.println("Udp Search End\n");
							 * System.out.println("\n");
							 */
							
							if (iocPort > 1024 && comand == 6) {
								foundObj = cidEpicsObject.get(foundCidStr);
								if (cidEpicsObject.get(foundCidStr) == null) {
									break;
								}

								((Connected) foundObj).setUdpConnected(true);
								/*
								 * Put the ioc ipAddress in a HashMap if it
								 * isn't there already
								 */

								if (!mapIoc.containsKey(iocIpAddress)) {
									mapIoc.put(iocIpAddress, new IocTcp(iocIpAddress, iocPort));
								}

								/* Get reference for the IOC */
								ioc = (IocTcp) mapIoc.get(iocIpAddress);
								ioc.addEpicsDataObject(foundObj);
								ioc.initConnection();
								ioc.initialize();
								if (!ioc.isAlive()) {
									ioc.start();
									hpRepeaterClient.addIoc(iocIpAddress, ioc);
								}
								addRemovePv(foundObj, "remove");
							}

							/**
							 * Now pick off remaining, if any, bytes. These are
							 * useless
							 */
							totalBytesInPacket = totalBytesInPacket - 16;
							if (caHdrIn.getPostSize() > 0) {

								dpByteIoc = new ByteArrayInputStream(dpGet.getData(), offset, caHdrIn.getPostSize());
								dpDataIoc = new DataInputStream(dpByteIoc);
								offset = offset + caHdrIn.getPostSize();
								totalBytesInPacket = totalBytesInPacket - caHdrIn.getPostSize();
							}
							totalBytesCounter++;
							if (totalBytesInPacket == 0) {
								waitForDp = false;
								so.close();
							}
						} // while (totalBytesInPacket > 0)
					}
				} else {
					waitForDp = false;
				}
			} /*** Here 4 ***/
			if (!so.isClosed()) {
				so.close();
			}
		
		}// end of blinker thread
	}
}
