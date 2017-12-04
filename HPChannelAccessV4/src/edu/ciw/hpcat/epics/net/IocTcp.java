package edu.ciw.hpcat.epics.net;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
//import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
//import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ciw.hpcat.epics.data.Cid;
import edu.ciw.hpcat.epics.data.Connected;
// import edu.ciw.hpcat.epics.data.ConnectionCounter;
import edu.ciw.hpcat.epics.data.DataType;
import edu.ciw.hpcat.epics.data.DropPv;
import edu.ciw.hpcat.epics.data.EpicsDataObject;
import edu.ciw.hpcat.epics.data.MinorVersion;
import edu.ciw.hpcat.epics.data.Monitor;
import edu.ciw.hpcat.epics.data.NumPoints;
import edu.ciw.hpcat.epics.data.PvName;
import edu.ciw.hpcat.epics.data.ReadWriteBytes;
import edu.ciw.hpcat.epics.data.TcpConnectTime;
import edu.ciw.hpcat.epics.dataDefs.EpicsControlData;
import edu.ciw.hpcat.epics.dataDefs.EpicsDbrControlChar;
import edu.ciw.hpcat.epics.dataDefs.EpicsDbrControlDouble;
import edu.ciw.hpcat.epics.dataDefs.EpicsDbrControlEnum;
import edu.ciw.hpcat.epics.dataDefs.EpicsDbrControlFloat;
import edu.ciw.hpcat.epics.dataDefs.EpicsDbrControlLong;
import edu.ciw.hpcat.epics.dataDefs.EpicsDbrControlShort;
import edu.ciw.hpcat.epics.dataDefs.EpicsDbrControlString;
import edu.ciw.hpcat.epics.dataDefs.EpicsTimeData;
import edu.ciw.hpcat.epics.dataDefs.BaseDataObject;
//import java.util.concurrent.*;

public class IocTcp implements Runnable, PropertyChangeListener, Ioc {
	private UdpSearch udpSearch;
	// private ConnectionCounter connectionCounter =
	// ConnectionCounter.getInstance();

	private short m_cmmd;
	private short m_type;
	private short m_count;
	private int m_cid;
	private int m_available;
	private String msg;
	private Socket tcpSocket = null;
	private String iocIpAddress;
	private int tcpPort;
	private InetSocketAddress isa;
	private PrintStream tcpOut;
	private ByteArrayOutputStream outBytes;
	private DataOutputStream outData;

	private volatile ByteArrayInputStream inBytes;
	private volatile DataInputStream inData;
	// private volatile ChannelAccessPushBackStream inData;
	private volatile byte[] bufSend;

	private String userName;
	private String hostName = "HostName";
	/* volatile */ private CaHdrOut caHdrOut;
	/* volatile */ private CaHdrIn caHdrIn;
	boolean connected = false;
	boolean initialized = false;
	long currentTime;
	private volatile Thread blinker;
	long delta = 0;
	volatile int ioId = 0;
	volatile int subId = 0;
	volatile int rnid = 0;
	volatile int auto = 0;
	volatile int monId = 0;
	java.sql.Timestamp timeNow;
	private long lastBeaconTime = 0;
	private boolean iocDisconnect = false;
	private int disconnectCounter = 0;
	private boolean connectionCheck = false;
	boolean test = false;
	File iocDropFile;

	volatile short cmmd;
	volatile byte[] runbuf;
	volatile int runbufSendLength = 0;
	volatile byte[] bufHeader = new byte[16];
	volatile short datatype;
	volatile short count;
	volatile int cid;
	volatile byte[] message;
	volatile short messageSize;
	volatile long runtimeNow;
	volatile int testAvailable;
	int bytesInDataStream = 0;

	// Map iocMap;

	// private PropertyChangeSupport changes = new PropertyChangeSupport(this);

	/**
	 * cidEpicsObject is a HashMap key=Client ID(CID), value = EpicsDataObject
	 */
	// Map cidEpicsObject = Collections.synchronizedMap(new HashMap());
	Map<String, Object> cidEpicsObject = new HashMap<String, Object>();
	// ConcurrentMap<String,Object> cidEpicsObject = new
	// ConcurrentHashMap<String,Object>();

	/**
	 * /** rnidEpicsObject is a HashMap key=rNd, value = EpicsDataObject
	 */
	// Map rnidEpicsObject = Collections.synchronizedMap(new HashMap());
	Map<String, Object> rnidEpicsObject = new HashMap<String, Object>();
	// ConcurrentMap<String,Object> rnidEpicsObject = new
	// ConcurrentHashMap<String,Object>();

	/**
	 * subIdEpicsObject is a HashMap key=rNd, value = EpicsDataObject
	 */
	/* volatile */

	// Map subIdEpicsObject = Collections.synchronizedMap(new HashMap());
//	Map<String, Object> subIdEpicsObject = new HashMap<String, Object>();
	Map<Integer, Object> subIdEpicsObject = new HashMap<Integer, Object>();
	// ConcurrentMap<String, Object> subIdEpicsObject = new
	// ConcurrentHashMap<String, Object>();

	/**
	 * epicsObjList is a List of all EpicsDataObjects
	 */
	// List epicsObjList = Collections.synchronizedList(new ArrayList());
	List<Object> epicsObjList = new ArrayList<Object>();

	/**
	 * claimResourceObjList is a List of all EpicsDataObjects which are yet to
	 * claim resource
	 */
	// List claimResourceObjList = Collections.synchronizedList(new
	// ArrayList());
	List<Object> claimResourceObjList = new ArrayList<Object>();

	/**
	 * readNotifyObjList is a List of all EpicsDataObjects which are yet to
	 * claim read Notify
	 */
	// List readNotifyObjList = Collections.synchronizedList(new ArrayList());
	List<Object> readNotifyObjList = new ArrayList<Object>();

	/**
	 * ReadOnceObjList is a List of all EpicsDataObjects which are yet to claim
	 * read Notify
	 */
	// List readOnceObjList = Collections.synchronizedList(new ArrayList());
	List<Object> readOnceObjList = new ArrayList<Object>();

	/**
	 * monitorObjList is a List of all EpicsDataObjects which are yet to claim
	 * read Notify
	 */
	// List monitorObjList = Collections.synchronizedList(new ArrayList());
	List<Object> monitorObjList = new ArrayList<Object>();

	/**
	 * dropObjList is a List of all EpicsDataObjects which need to be dropped
	 */
	// List dropObjList = Collections.synchronizedList(new ArrayList());
	List<Object> dropObjList = new ArrayList<Object>();
	/**
	 * To avoid concurrent access modification to dropObjList let's make a temp
	 * array. We can then cycle through temp array and remove objects from
	 * dropObjList.
	 */
	volatile List<Object> tempArray = new ArrayList<Object>();

	/**
	 * 
	 * 
	 *
	 * @param iocIpAddress
	 *            String
	 * @param tcpPort
	 *            int Let the constructor send the Username and Hostname So it
	 *            will never have to be sent again.
	 *
	 */

	public IocTcp(String iocIpAddress, int tcpPort) {

		udpSearch = UdpSearch.getInstance();
		caHdrOut = new CaHdrOut();
		this.iocIpAddress = iocIpAddress;
		this.tcpPort = tcpPort;
		userName = System.getProperty("user.name");

		if (userName.equals("") || userName == null) {
			userName = "user";
		}
	}

	synchronized public void propertyChange(PropertyChangeEvent pce) {

		Object source = pce.getSource();
		Object obj = pce.getNewValue();
		String propertyName = pce.getPropertyName();

		int bufSendLength = 0;
		byte[] writeBytes;
		// byte[] readBytes;
		if (propertyName.equals("read")) {
			// System.out.println("In EDO read "+((PvName)source).getPvName());
			readOnceObjList.add(obj);
			// System.out.println(" Blah Blah bytes = "+( (Connected)
			// obj).getReadFlag()+" "+((PvName)obj).getPvName());
			// System.out.println(" Read Once array size =
			// "+readOnceObjList.size());
			// readBytes = ( (ReadWriteBytes) source).getReadBytes();
			/*
			 * readBytes = getOneShotEventBytes(source); try {
			 * 
			 * outBytes = new ByteArrayOutputStream(); outData = new
			 * DataOutputStream(outBytes);
			 * 
			 * outBytes.flush(); outBytes.write(readBytes); bufSendLength =
			 * outData.size();
			 * 
			 * bufSend = new byte[bufSendLength]; bufSend =
			 * outBytes.toByteArray();
			 * 
			 * tcpOut.write(bufSend); } catch (IOException e) {
			 * 
			 * }
			 */
		}

		else if (propertyName.equals("write")) {

			writeBytes = ((ReadWriteBytes) source).getWriteBytes();

			try {

				outBytes = new ByteArrayOutputStream();
				outData = new DataOutputStream(outBytes);

				outBytes.flush();
				outBytes.write(writeBytes);
				bufSendLength = outData.size();

				bufSend = new byte[bufSendLength];
				bufSend = outBytes.toByteArray();

				tcpOut.write(bufSend);
			} catch (IOException e) {

			}
		}
	}

	public void addEpicsDataObject(Object obj) {
		synchronized (epicsObjList) {
			epicsObjList.add(obj);
		}
		synchronized (cidEpicsObject) {
			cidEpicsObject.put(Integer.toString(((Cid) obj).getChid()), obj);
		}
		synchronized (claimResourceObjList) {
			claimResourceObjList.add(obj);
		}
		((MinorVersion) obj).setMinorVersionNumber(0x0B);
	}

	public String getIp() {
		return iocIpAddress;
	}

	public void initialize() {
		if (!initialized) {
			sendUserNameHostName();

		}
	}

	public void initConnection() {

		while (!connected) {
			try {
				tcpSocket = new Socket(iocIpAddress, tcpPort);
				// tcpSocket = new Socket();
				// System.out.println(" Buffer size =
				// "+tcpSocket.getReceiveBufferSize());
				// isa = new InetSocketAddress(iocIpAddress, tcpPort);

				tcpSocket.setReceiveBufferSize(131072);
				tcpSocket.setSendBufferSize(131072);

				// tcpSocket.connect(isa, 100);
				// System.out.println(" after Buffer size =
				// "+tcpSocket.getReceiveBufferSize());

				// tcpSocket = new Socket();
				// tcpSocket.setReceiveBufferSize(131072);

				// tcpSocket.setTcpNoDelay(true);
				tcpSocket.setTcpNoDelay(false);

				inData = new DataInputStream(tcpSocket.getInputStream());
				tcpOut = new PrintStream(tcpSocket.getOutputStream());

				hostName = InetAddress.getLocalHost().getHostName();
				currentTime = System.currentTimeMillis();
				this.setLastBeaconTime(currentTime);

				try {
					setConnectInit(true);
				} catch (InterruptedException ie) {

				}

			} catch (UnknownHostException uhe) {

				try {
					setConnectInit(false);
				} catch (InterruptedException ie) {

				}
			} catch (SocketException se) {

				try {
					setConnectInit(false);
				} catch (InterruptedException ie) {

				}
			} catch (IOException ie) {
			}
		}
	}

	synchronized public void setConnectInit(boolean b) throws InterruptedException {
		connected = b;
		notifyAll();
	}

	private void sendVersionNumber() {

		byte[] buf;
		int bufSendLength = 0;

		try {

			outBytes = new ByteArrayOutputStream();
			outData = new DataOutputStream(outBytes);

			// outBytes.flush();
			buf = getVersionBytes();
			outBytes.write(buf);
			bufSendLength = outData.size();

			bufSend = new byte[bufSendLength];
			bufSend = outBytes.toByteArray();

			tcpOut.write(bufSend);
			outBytes.flush();

		} catch (IOException e) {

		}
	}

	private void sendUserNameHostName() {

		byte[] buf;
		int bufSendLength = 0;

		try {

			outBytes = new ByteArrayOutputStream();
			outData = new DataOutputStream(outBytes);

			// outBytes.flush();
			buf = getUserNameBytes();
			outBytes.write(buf);
			buf = getHostNameBytes();
			outBytes.write(buf);
			bufSendLength = outData.size();

			bufSend = new byte[bufSendLength];
			bufSend = outBytes.toByteArray();

			tcpOut.write(bufSend);
			outBytes.flush();

		} catch (IOException e) {
			// logger.error("sendUserNameHostName "+e.getMessage());
		}

		initialized = true;
	}

	private byte[] getUserNameBytes() {

		byte[] bytes;

		caHdrOut.clearBuffer();

		m_cmmd = (short) 0x14;
		m_type = (short) 0x00;
		m_count = (short) 0x00;
		m_cid = (int) 0x00;
		m_available = (int) 0x00;
		msg = userName;

		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_type);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_available);
		caHdrOut.setMessage(msg);
		bytes = caHdrOut.getByteArray();
		return bytes;
	}

	private byte[] getHostNameBytes() {

		byte[] bytes;
		caHdrOut.clearBuffer();

		m_cmmd = (short) 0x15;
		m_type = (short) 0x0000;
		m_count = (short) 0x0000;
		m_cid = (int) 0x00;
		m_available = (int) 0x00;

		msg = hostName;

		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_type);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_available);
		caHdrOut.setMessage(msg);
		bytes = caHdrOut.getByteArray();

		return bytes;
	}

	synchronized public boolean isIocConnected() {
		while (!connected) {
			try {
				wait();
			} catch (InterruptedException e) {

			}
		}
		notifyAll();

		return connected;
	}

	public byte[] getFlowOffBytes() {
		byte[] buf;

		m_cmmd = 0x08;
		m_type = 0;
		m_count = 0;
		m_cid = 0;
		m_available = 0;

		caHdrOut.clearBuffer();

		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_type);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_available);
		caHdrOut.setMessage("");
		buf = caHdrOut.getByteArray();
		return buf;

	}

	public byte[] getFlowOnBytes() {
		byte[] buf;

		m_cmmd = 0x09;
		m_type = 0;
		m_count = 0;
		m_cid = 0;
		m_available = 0;

		caHdrOut.clearBuffer();

		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_type);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_available);
		caHdrOut.setMessage("");
		buf = caHdrOut.getByteArray();
		return buf;

	}

	public void sendFlowOffBytes() {
		byte[] buf;
		int bufSendLength = 0;
		try {

			outBytes = new ByteArrayOutputStream();
			outData = new DataOutputStream(outBytes);

			// outBytes.flush();
			buf = getFlowOffBytes();
			outBytes.write(buf);
			bufSendLength = outData.size();

			bufSend = new byte[bufSendLength];
			bufSend = outBytes.toByteArray();

			tcpOut.write(bufSend);
			outBytes.flush();

		} catch (IOException e) {
			// logger.error("sendUserNameHostName "+e.getMessage());
		}

	}

	public void sendFlowOnBytes() {
		byte[] buf;
		int bufSendLength = 0;

		try {

			outBytes = new ByteArrayOutputStream();
			outData = new DataOutputStream(outBytes);

			// outBytes.flush();
			buf = getFlowOnBytes();
			outBytes.write(buf);
			bufSendLength = outData.size();

			bufSend = new byte[bufSendLength];
			bufSend = outBytes.toByteArray();

			tcpOut.write(bufSend);
			outBytes.flush();

		} catch (IOException e) {
			// logger.error("sendUserNameHostName "+e.getMessage());
		}

	}

	public boolean isInitialized() {
		return initialized;

	}

	public void addDropObj(Object obj) {
		dropObjList.add(obj);
	}

	public void initEdo() {
		Connected ciObj;
		synchronized (epicsObjList) {
			for (int i = 0; i < epicsObjList.size(); i++) {
				ciObj = (Connected) epicsObjList.get(i);
				udpSearch.addNotFoundPV(ciObj);
				ciObj.initParms();
			}
		}

	}

	synchronized private byte[] getConnectionCheckBytes() {
		byte[] buf;

		m_cmmd = 0x17;
		m_type = 0;
		m_count = 0;
		m_cid = 0;
		m_available = 0;

		caHdrOut.clearBuffer();

		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_type);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_available);
		caHdrOut.setMessage("");
		buf = caHdrOut.getByteArray();
		return buf;
	}

	/**
	 *
	 * @param i
	 *            int Local CID
	 * @param str
	 *            String PV Name
	 * @return byte[]
	 */
	synchronized private byte[] getClaimResourceBytes(int i, String str) {
		byte[] bytes;

		caHdrOut.clearBuffer();
		m_cmmd = 0x12;
		m_type = 0x00;
		m_count = 0x00;
		m_cid = i;
		m_available = 0x0b;
		msg = str;

		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_type);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_available);
		caHdrOut.setMessage(msg);
		bytes = caHdrOut.getByteArray();
		return bytes;
	}

	private byte[] getOneShotEventBytes(Object obj) {

		byte[] buf;
		short numPoints = (short) ((NumPoints) obj).getNumPoints();

		caHdrOut.clearBuffer();
		m_cmmd = (short) 0x0F;
		m_type = (short) (((DataType) obj).getDataType() + 28);
		m_count = numPoints;
		m_cid = (int) ((Cid) obj).getSid();
		m_available = (int) auto;

		msg = "";
		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_type);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_available);
		caHdrOut.setMessage(msg);
		buf = caHdrOut.getByteArray();
		((Connected) obj).setReadNotifyRequest(true);
		((Cid) obj).setRNId(auto);
		rnidEpicsObject.put(Integer.toString(auto), obj);
		auto++;
		return buf;
	}

	private synchronized byte[] getMonitorBytes(Object obj) {
		byte[] buf;
		short numPoints = (short) ((NumPoints) obj).getNumPoints();

		byte[] mask = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00,
				0x00 };

		caHdrOut.clearBuffer();
		m_cmmd = (short) 0x01;
		m_type = (short) (((DataType) obj).getDataType() + 14);
		m_count = numPoints;
		m_cid = (int) ((Cid) obj).getSid();
		m_available = (int) auto;
		((Monitor) obj).setMonitorRequest(true);
		((Monitor) obj).setPvMonitor(true);

		((Cid) obj).setSubId(auto);
		subIdEpicsObject.putIfAbsent(auto, obj);
		// subIdEpicsObject.put(Integer.toString(auto), obj);

		auto++;

		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_type);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_available);
		caHdrOut.setMessage(mask);
		buf = caHdrOut.getByteArray();

		return buf;
	}

	/*
	 * private void closeSocket() { try { inData.close(); outData.close();
	 * tcpSocket.close(); } catch (IOException iee) { //
	 * logger.error("closeSocket "+iee.getMessage()); } }
	 */
	public int getConnectCounter() {
		return disconnectCounter;
	}

	public long getLastBeaconTime() {
		return lastBeaconTime;
	}

	public void setLastBeaconTime(long l) {
		// System.out.println(" Setting Beacon Time = " + l + " Ip " + getIp());
		lastBeaconTime = l;
	}

	public List<Object> getEpicsObjectsList() {
		return this.epicsObjList;
	}

	public void setConnectionCheck(boolean b) {

		connectionCheck = b;
	}

	private byte[] getVersionBytes() {
		byte[] buf;

		m_cmmd = 0;
		m_type = 1;
		m_count = 0x0B;
		m_cid = 0;
		m_available = 0;
		caHdrOut.clearBuffer();
		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_type);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_available);
		caHdrOut.setMessage("");
		buf = caHdrOut.getByteArray();

		return buf;
	}

	public void start() {
		blinker = new Thread(this);
		blinker.start();
	}

	/**
	 * See the following article for more details.
	 * http://www.forward.com.au/javaProgramming/HowToStopAThread.html
	 */
	public void stop() {
		Thread tmpBlinker = blinker;
		blinker = null;
		if (tmpBlinker != null) {
			iocDisconnect = true;

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

	public void run() {
		/**
		 * Runnable thread.
		 */
		Object obj;
		Connected cObj;
		Monitor mObj;
		DropPv dObj;
		java.beans.PropertyChangeListener pObj;
		int localCid;
		String str;

		int sid;
		int avail;

		/**
		 * Start the whole run() in a while() loop.
		 */

		Thread thisThread = Thread.currentThread();
		while (blinker == thisThread) { // blinker Thread

			if (connectionCheck && (disconnectCounter < 5)) {

				try {
					outBytes = new ByteArrayOutputStream();
					outData = new DataOutputStream(outBytes);
					// outBytes.flush();
					runbuf = getConnectionCheckBytes();
					outBytes.write(runbuf);
					runbufSendLength = outData.size();
					bufSend = new byte[runbufSendLength];
					bufSend = outBytes.toByteArray();
					tcpOut.write(bufSend);
					disconnectCounter++;
					outData.flush();
					System.out.println("Disconnect counter =" + disconnectCounter);
				} catch (IOException e) {

				}
			}

			// Do some cleanup first.
			try {

				outBytes = new ByteArrayOutputStream();
				outData = new DataOutputStream(outBytes);
				/**
				 * First let us loop through the claimResourceObject list
				 */

				synchronized (claimResourceObjList) {

					for (int i = 0; i < claimResourceObjList.size(); i++) {

						obj = claimResourceObjList.get(i);
						if (!((Connected) obj).isClaimedResourceRequest()) {
							localCid = ((Cid) obj).getChid();
							str = ((PvName) obj).getPvName();
							((Connected) obj).setClaimedResourceRequest(true);
							runbuf = getClaimResourceBytes(localCid, str);
							outBytes.write(runbuf);
						}
					}

					runbufSendLength = outBytes.size();
					if (runbufSendLength > 0) {
						bufSend = new byte[runbufSendLength];
						bufSend = outBytes.toByteArray();
						// System.out.println(" Claim Resource Byte Size =
						// "+bufSend.length);
						tcpOut.write(bufSend);
					}
				}
				outBytes = new ByteArrayOutputStream();
				outData = new DataOutputStream(outBytes);

				/**
				 * Next let us loop through the readNotifyObject list
				 *
				 */

				synchronized (readNotifyObjList) {
					for (int i = 0; i < readNotifyObjList.size(); i++) {

						obj = readNotifyObjList.get(i);
						if (!((Connected) obj).isReadNotifyRequest()) {
							runbuf = getOneShotEventBytes(obj);
							outBytes.write(runbuf);
						}
					}

					runbufSendLength = outBytes.size();
					if (runbufSendLength > 0) {
						bufSend = new byte[runbufSendLength];
						bufSend = outBytes.toByteArray();
						tcpOut.write(bufSend);

					}
				}
				outBytes = new ByteArrayOutputStream();
				outData = new DataOutputStream(outBytes);

				synchronized (readOnceObjList) {
					for (int i = 0; i < readOnceObjList.size(); i++) {
						obj = readOnceObjList.get(i);
						if (((Connected) obj).getReadFlag()) {
							runbuf = getOneShotEventBytes(obj);
							outBytes.write(runbuf);
							((Connected) obj).setReadFlag(false);
						}
					}
					readOnceObjList.clear();
					runbufSendLength = outBytes.size();
					if (runbufSendLength > 0) {
						bufSend = new byte[runbufSendLength];
						bufSend = outBytes.toByteArray();
						tcpOut.write(bufSend);

					}
				}
				/**
				 * Next let us loop through the monitor list
				 *
				 */
				outBytes = new ByteArrayOutputStream();
				outData = new DataOutputStream(outBytes);

				synchronized (monitorObjList) {
					for (int i = 0; i < monitorObjList.size(); i++) {

						obj = monitorObjList.get(i);
						if (!((Monitor) obj).isMonitorRequested()) {
							runbuf = getMonitorBytes(obj);
							outBytes.write(runbuf);
						}
					}
					runbufSendLength = outBytes.size();
					if (runbufSendLength > 0) {
						bufSend = new byte[runbufSendLength];
						bufSend = outBytes.toByteArray();
						tcpOut.write(bufSend);
					}
				}

				/**
				 * Now lets loop through all EpicsDataObjects that are marked
				 * for being dropped.
				 */

				for (int i = 0; i < dropObjList.size(); i++) {
					dObj = (DropPv) dropObjList.get(i);
					tempArray.add(dObj);
				}
				outBytes = new ByteArrayOutputStream();
				outData = new DataOutputStream(outBytes);

				for (int i = 0; i < tempArray.size(); i++) {
					Object delObj = tempArray.get(i);
					runbuf = ((DropPv) delObj).getDropChannelBytes();
					outBytes.write(runbuf);
					dropObjList.remove(delObj);
				}
				tempArray.clear();

				runbufSendLength = outBytes.size();
				if (runbufSendLength > 0) {
					bufSend = new byte[runbufSendLength];
					bufSend = outBytes.toByteArray();
					tcpOut.write(bufSend);
					outData.flush();
					// System.out.println(" Sent Drop List Bytes
					// "+runbufSendLength);

				}
			} catch (IOException ie) {
			}
			sendFlowOffBytes();
			
			  try { 
				  Thread.sleep(10); 
				  } catch (InterruptedException e1) { 
			 
					  e1.printStackTrace(); }			 

			try {

				while (inData.available() > 0) {

					bytesInDataStream = inData.read(bufHeader, 0, 16);
					if (bytesInDataStream < 1) {
						System.out.println(" Nothing in stream ");
						break;
					}

					caHdrIn = new CaHdrIn(bufHeader);
					caHdrIn.readData();

					cmmd = caHdrIn.getCommand();
					messageSize = caHdrIn.getPostSize();
					datatype = caHdrIn.getType();
					count = caHdrIn.getCount();
					/*
					  if (caHdrIn.getCommand() == 0x01) {
					  System.out.print(" TCP Start " +
					  System.currentTimeMillis() + "   ");
					  System.out.println("Command = " +	Integer.toHexString(caHdrIn.getCommand()).toUpperCase());
					  System.out.println("Size    = " +Integer.toHexString(caHdrIn.getPostSize()).toUpperCase());
					  System.out.println("Type = "+Integer.toHexString(caHdrIn.getType()).toUpperCase());
					  System.out.println("Count   = "+Integer.toHexString(caHdrIn.getCount()).toUpperCase());
					  System.out.println("Cid     = "+Integer.toHexString(caHdrIn.getCid()).toUpperCase());
					  System.out.println("Avail   = "+Integer.toHexString(caHdrIn.getAvailable()).toUpperCase());
					  System.out.println("TCP End\n\n\n\n\n\n"); }
					 */
					if (messageSize > 0) {
						message = new byte[messageSize];
						inData.read(message, 0, messageSize);
					} else {
						message = new byte[0];
					}
					switch (cmmd) {
					case 0x00:

						// System.out.println(" Command zero message size =
						// "+message.length);
						break;

					case 0x01:
						avail = caHdrIn.getAvailable();//
						obj = subIdEpicsObject.get(avail);
						if (obj == null) {
							System.out.println(
									" Object is null " + avail + " Size of subId = " + subIdEpicsObject.size());
						}else{
							((Connected) obj).setCountDownConnectionDown();

						}

						if ((messageSize > 0) && (obj != null)) {

							runtimeNow = System.currentTimeMillis();
							((EpicsDataObject) obj).setTcpConnectTime(runtimeNow);

							((EpicsTimeData) (((EpicsDataObject) obj).getDataObject())).readTimeData(message);
							/*
							 * try { Thread.sleep(10); } catch
							 * (InterruptedException e) { // TODO Auto-generated
							 * catch block e.printStackTrace(); }
							 */

						} else {
							/*
							 * We have a drop Monitor packet.
							 *
							 */

							try {
//								obj = subIdEpicsObject.get(Integer.toString(avail));
								obj = subIdEpicsObject.get(avail);

								if (obj != null && ((EpicsDataObject) obj).getDropPv()) {

									((Monitor) obj).setMonitorRequest(false);
									monitorObjList.remove(obj);
									String rS = Integer.toString(avail);
//									Object bbb = subIdEpicsObject.get(Integer.toString(avail));
									Object bbb = subIdEpicsObject.get(avail);
									/*
									 * if (bbb != null) {
									 * System.out.println(" Removing Object " +
									 * ((EpicsDataObject) bbb).getPvName()); }
									 * else { System.out.
									 * println(" Removing Object was null");
									 * 
									 * }
									 */
//									subIdEpicsObject.remove(rS);
									subIdEpicsObject.remove(avail);
								}
							} catch (NullPointerException np) {
							}
						}

						break;

					case 0x02:

						 System.out.println(" Recieved command = 2");
						break;

					case 0x03:

						/*
						 * System.out.println(" Recieved command = 3");
						 * System.out.println("\n"); System.out.println(
						 * "Command = "
						 * +Integer.toHexString(caHdrIn.getCommand()).
						 * toUpperCase()); System.out.println("Size    = "
						 * +Integer.toHexString(caHdrIn.getPostSize()).
						 * toUpperCase()); System.out.println("Type    = "
						 * +Integer.toHexString(caHdrIn.getType()).toUpperCase()
						 * ); System.out.println("Count   = "
						 * +Integer.toHexString(caHdrIn.getCount()).toUpperCase(
						 * )); System.out.println("Cid     = "
						 * +Integer.toHexString(caHdrIn.getCid()).toUpperCase())
						 * ; System.out.println("Avail   = "
						 * +Integer.toHexString(caHdrIn.getAvailable()).
						 * toUpperCase()); System.out.println("TCP End\n\n\n");
						 * 
						 */
						// System.out.println(" Message byte size
						// ="+message.length);
						avail = caHdrIn.getAvailable();
//						obj = subIdEpicsObject.get(Integer.toString(avail));
						obj = subIdEpicsObject.get(avail);

						// ((EpicsTimeData) ( ( (EpicsDataObject)
						// obj).getDataObject())).readTimeData(message);
						((EpicsTimeData) obj).readTimeData(message);

						// System.out.println(" bah name
						// ="+((PvName)obj).getPvName());

						break;

					case 0x08:

						 System.out.println(" Recieved command = 0x08");
						break;

					case 0x09:

						 System.out.println(" Recieved command = 0x09");
						break;
					case 0x0A:

						 System.out.println(" Recieved command = 0x0A");
						break;

					case 0x0B:
						 System.out.println(" Recieved command = 0x0B");
						break;

					case 0x0C:
						avail = caHdrIn.getAvailable();

						// Get the EpicsObject
						obj = rnidEpicsObject.get(Integer.toString(avail));
						((EpicsDataObject) obj).dropPropertyChangeListener();

						String rS2 = Integer.toString(avail);
						rnidEpicsObject.remove(rS2);
						cidEpicsObject.remove(Integer.toString(((Cid) obj).getChid()));

						/*
						 * System.out.println("\n\n\n"); System.out.println(
						 * " removing C Pvname = "+((PvName)obj).getPvName() );
						 * System.out.println("\n\n"); System.out.println(
						 * " C After Size of cidEpicsObject = "
						 * +cidEpicsObject.size()); System.out.println(
						 * " C After Size of rnidEpicsObject = "
						 * +rnidEpicsObject.size()); System.out.println(
						 * " C After Size of subIdEpicsObject = "
						 * +subIdEpicsObject.size()); System.out.println(
						 * " C After Size of epicsObjList = "
						 * +epicsObjList.size()); System.out.println(
						 * " C After Size of claimResourceObjList = "
						 * +claimResourceObjList.size()); System.out.println(
						 * " C After Size of readNotifyObjList = "
						 * +readNotifyObjList.size()); System.out.println(
						 * " C After Size of monitorObjList = "
						 * +monitorObjList.size()); System.out.println(
						 * " C After Size of dropObjList = "
						 * +dropObjList.size()); System.out.println(
						 * " C After Size of tempArray = "+tempArray.size());
						 */
						((Connected) obj).setClaimedResourceRequest(false);
						((Connected) obj).setReadNotifyRequest(false);
						epicsObjList.remove(obj);
						((Connected) obj).setUdpConnected(false);

						break;

					case 0x0D:

						// System.out.println(" Command = 0x0D");
						break;
					case 0x0E:

						 System.out.println(" Command = 0x0E "+cmmd);
						break;

					case 0x0F:
						avail = caHdrIn.getAvailable();
						obj = rnidEpicsObject.get(Integer.toString(avail));
						readNotifyObjList.remove(obj);
						/*
						 * ((Connected)obj).setEDOInit(true); if (((Monitor)
						 * obj).isPvMonitor() && !((Monitor)
						 * obj).isMonitorRequested()) { String tempMonitorStr =
						 * ((EpicsDataObject)obj).getMonitorString();
						 * BaseDataObject aaa =
						 * (((EpicsDataObject)obj).getDataObject());
						 * aaa.addPropertyChangeListener(tempMonitorStr,
						 * (EpicsDataObject)obj); monitorObjList.add(obj); }
						 */

						// ((Connected)obj).setEDOInit(true);
						// if (((Monitor) obj).isPvMonitor() && !((Monitor)
						// obj).isMonitorRequested()) {
						String tempMonitorStr = ((EpicsDataObject) obj).getMonitorString();
						BaseDataObject aaa = (((EpicsDataObject) obj).getDataObject());
						aaa.addPropertyChangeListener(tempMonitorStr, (EpicsDataObject) obj);
						monitorObjList.add(obj);
						// }

						runtimeNow = System.currentTimeMillis();
						((TcpConnectTime) obj).setTcpConnectTime(runtimeNow);
						((EpicsDataObject) obj).setIocConnected(true);

						((EpicsControlData) (((EpicsDataObject) obj).getDataObject())).readControlData(message);
						break;

					case 0x10:
						
						System.out.println(" Recieved 0x10 "+cmmd);

						break;
					case 0x11:

						System.out.println(" Recieved 0x11 "+cmmd);

						break;
					case 0x12:

			//			 System.out.println(" Recieved 0x12");

						cid = caHdrIn.getCid();
						obj = cidEpicsObject.get(Integer.toString(cid));
						pObj = (java.beans.PropertyChangeListener) obj;

						avail = caHdrIn.getAvailable();
						claimResourceObjList.remove(obj); // Since we have
															// already claimed
															// resource in
															// server
						// readNotifyObjList.add(obj); // Next we need to set
						// command=0x0F
						// ( (EpicsDataObject) obj).setNumPoints(count); // Set
						// num points in EDO
						runtimeNow = System.currentTimeMillis();
						((TcpConnectTime) obj).setTcpConnectTime(runtimeNow);

						switch (datatype) {
						case 0:

							// String
							EpicsDbrControlString edcs = new EpicsDbrControlString();
							((EpicsDataObject) obj).setBaseDataObject(edcs);

							edcs.setEpicsDataObject((EpicsDataObject) obj);
							edcs.setNumPoints(count);

							((EpicsDataObject) obj).setIocTcp(this);

							((EpicsDataObject) obj).setPropertyChangeListener("write", this);
							((EpicsDataObject) obj).setPropertyChangeListener("read", this);

							break;

						case 1:

							// int, short
							EpicsDbrControlShort edci = new EpicsDbrControlShort();
							((EpicsDataObject) obj).setBaseDataObject(edci);
							edci.setEpicsDataObject((EpicsDataObject) obj);
							edci.setNumPoints(count);

							((EpicsDataObject) obj).setIocTcp(this);
							((EpicsDataObject) obj).setPropertyChangeListener("write", this);
							((EpicsDataObject) obj).setPropertyChangeListener("read", this);

							break;

						case 2:

							// float

							EpicsDbrControlFloat edcf = new EpicsDbrControlFloat();
							((EpicsDataObject) obj).setBaseDataObject(edcf);
							edcf.setEpicsDataObject((EpicsDataObject) obj);
							edcf.setNumPoints(count);

							((EpicsDataObject) obj).setIocTcp(this);
							((EpicsDataObject) obj).setPropertyChangeListener("write", this);
							((EpicsDataObject) obj).setPropertyChangeListener("read", this);

							break;

						case 3:

							// enum

							EpicsDbrControlEnum eden = new EpicsDbrControlEnum();
							((EpicsDataObject) obj).setBaseDataObject(eden);
							eden.setEpicsDataObject((EpicsDataObject) obj);
							eden.setNumPoints(count);

							((EpicsDataObject) obj).setIocTcp(this);
							((EpicsDataObject) obj).setPropertyChangeListener("write", this);
							((EpicsDataObject) obj).setPropertyChangeListener("read", this);

							break;

						case 4:

							// char
							EpicsDbrControlChar edcc = new EpicsDbrControlChar();
							((EpicsDataObject) obj).setBaseDataObject(edcc);
							edcc.setEpicsDataObject((EpicsDataObject) obj);
							edcc.setNumPoints(count);

							((EpicsDataObject) obj).setIocTcp(this);
							((EpicsDataObject) obj).setPropertyChangeListener("write", this);
							((EpicsDataObject) obj).setPropertyChangeListener("read", this);

							break;

						case 5:

							// long
							EpicsDbrControlLong edcl = new EpicsDbrControlLong();
							((EpicsDataObject) obj).setBaseDataObject(edcl);
							edcl.setEpicsDataObject((EpicsDataObject) obj);
							edcl.setNumPoints(count);

							((EpicsDataObject) obj).setIocTcp(this);
							((EpicsDataObject) obj).setPropertyChangeListener("write", this);
							((EpicsDataObject) obj).setPropertyChangeListener("read", this);

							break;

						case 6:

							// double
							EpicsDbrControlDouble edcd = new EpicsDbrControlDouble();
							((EpicsDataObject) obj).setBaseDataObject(edcd);
							edcd.setEpicsDataObject((EpicsDataObject) obj);
							edcd.setNumPoints(count);

							((EpicsDataObject) obj).setIocTcp(this);
							((EpicsDataObject) obj).setPropertyChangeListener("write", this);
							((EpicsDataObject) obj).setPropertyChangeListener("read", this);

							break;
						}
						((Cid) obj).setSid(avail); // set the Sid in EDO
						readNotifyObjList.add(obj); // Next we need to set

						break;

					case 0x13:

						 System.out.println(" Recieved 0x13");

						break;
					case 0x14:

						 System.out.println(" Recieved 0x14 "+cmmd);
						 sendUserNameHostName();

						break;
					case 0x15:

						 System.out.println(" Recieved 0x15 "+cmmd);

						break;

					case 0x16:

						// System.out.println(" \n Recieved 0x16 \n");
						cid = caHdrIn.getCid();
						obj = cidEpicsObject.get(Integer.toString(cid));
						avail = caHdrIn.getAvailable();
						((Cid) obj).setRw(avail);

						break;

					case 0x17:

						 System.out.println(" Beacon alive "+cmmd);
						disconnectCounter = 0;

						break;

					case 0x18:

						 System.out.println(" Command recieved = 0x18 "+cmmd);

						break;

					case 0x1A:

						 System.out.println(" Command recieved = 0x1A "+cmmd);
						break;

					case 0x1B:

						 System.out.println(" Server deletes the channel "+cmmd);
						break;

					}

				}
				// sendFlowOnBytes();
			} catch (IOException e) {
				System.out.println(" IO Exception " + e.getMessage());
			}

			sendFlowOnBytes();
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block e.printStackTrace();
			}

		} // blinker loop
			// Thread.currentThread().yield();
	}
}
