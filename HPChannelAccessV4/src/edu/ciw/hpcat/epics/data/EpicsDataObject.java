package edu.ciw.hpcat.epics.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;

import edu.ciw.hpcat.epics.net.*;
import edu.ciw.hpcat.epics.dataDefs.BaseDataObject;

public class EpicsDataObject
		implements PropertyChangeListener, EpicsDataPropChangeListener, PvName, Cid, DataType, Connected, Monitor,
		DropPv, ReadWriteBytes, MinorVersion, IocTcpConnected, NumPoints, TcpConnectTime, UdpConnectTime {

	public EpicsDataObject() {

	}

	Ioc ioc = null;
	int rw;
	int chid = 0;
	int sid = 0;
	int subId = 0;
	int rnid = 0;

	String pvName;
	String value;

	String[] enumList;
	String[] arrayValues;
	int numStrings;
	int nativeDataType = -1;
	int enumValIndex = -1;
	int arrayPoints = 1;

	String putVal = "";
	String[] putValArray;
	int putValArrayLength = 0;

	boolean dropPv = false;
	String monitorString;

	/**
	 * userDropPv = true when the user drops it
	 */
	boolean userDropPv = false;
	boolean serverDroppedPv = false;
	int minorVersionNumber = 0;
	Object parentObj;
	public int numPoints = 1;
	int udpSearchCount = 0;

	String epicsVal = "Not Connected";
	long lastSearchTime = 0;
	List<String> parentListenerList = new ArrayList<>();
	long udpConnectTime = 0;
	long tcpConnectTime = 0;
	long lastValueChangedTime = 0;

	/**
	 * searchable = true if udpSearchCount < 10 This is to set a limit for
	 * searching the PV in the UDP search
	 */
	boolean searchable = true;

	/**
	 * chidStatus is true if the EpicsDataObject has been searched set
	 * chidStatus = false at initialization and also when IOC must be
	 * reconnected.
	 */
	boolean chidStatus = false;

	// static Logger logger = Logger.getLogger(EpicsDataObject.class);

	/**
	 * When the object has UDP connections then udpConnected = true else false
	 */
	boolean udpConnected = false;

	/**
	 * When the object has claimed resource in IOC then claimedResource = true
	 * else false
	 */

	boolean claimedResource = false;
	long claimResourceRequestTime = 0L;

	/**
	 * When the object has set ReadNotify (command = 0x0F) in IOC then
	 * readNotify = true else false
	 */

	boolean readNotify = false;

	/**
	 * If pvMonitor = true then TCP is instructed to tell the IOC to set monitor
	 * on this PV
	 */
	boolean pvMonitor = false;

	/**
	 * for wait and notify, lets define a lockable boolean
	 */

	volatile boolean valChanged = false;

	/**
	 * Lets define a default wait time in millisecs for wait and notify
	 */

	long waitToNotify = 0;

	/**
	 * monitor = true when pvMonitor = true and the IOC has set the monitor
	 */

	/**
	 * monitorRequest = true iocTcp when requested monitor connection
	 * 
	 */
	boolean monitorRequest = false;

	/**
	 * boolean iocConnected
	 */

	volatile boolean iocConnected = false;
	boolean readFlag = false;
	boolean edoInit = false;
	BaseDataObject baseDataObject;
	PropertyChangeSupport changes = new PropertyChangeSupport(this);
	UdpSearch udpSearch = UdpSearch.getInstance();
	Object waitObj;
	CountDownConnection countDownConnection = CountDownConnection.getInstance();
	boolean countDownReset = false;
	
	/*
	public EpicsDataObject(String str) {

		pvName = str.trim();

		lastSearchTime = 0;
		 addPvToUdpSearch();
	}
*/
	public EpicsDataObject(String str, boolean b) {

		pvName = str.trim();
		setPvMonitor(b);
		lastSearchTime = 0;
		if (!countDownConnection.getStatus()){
			new Thread(countDownConnection).start();
		}
		countDownConnection.addObject(this);
	}
	


	synchronized public void addPvToUdpSearch() {
		udpSearch.addRemovePv(this, "add");
		if (!udpSearch.isAlive()) {
			udpSearch.start();
		}
	}

	public boolean getStatus(long t) {
		boolean ret = false;

		return ret;

	}

	public void dropAllPropertyChangeListeners() {
		// Tell the data Object to remove this object from its listener list
		((EpicsDataPropChangeListener) baseDataObject).removePropertyChangeListener(this);
		// Remove all listeners who are listening to this object
		PropertyChangeListener[] li = changes.getPropertyChangeListeners();
		for (int i = 0; i < li.length; i++) {
			removePropertyChangeListener(li[i]);
		}
	}

	public void dropPropertyChangeListener() {
		changes.removePropertyChangeListener("write", (PropertyChangeListener) ioc);
		changes.removePropertyChangeListener("read", (PropertyChangeListener) ioc);
		((EpicsDataPropChangeListener) baseDataObject).removePropertyChangeListener(this);
	}

	public void setPvPropertyListener(String str, PropertyChangeListener l) {
		parentObj = l;
		parentListenerList.add(str);
		setPvPropertyListener(str, l);
	}

	public void removePropertyChangeListener(String str, PropertyChangeListener l) {
		changes.removePropertyChangeListener(str, l);
	}

	public void addPropertyChangeListener(String str, PropertyChangeListener l) {
		changes.addPropertyChangeListener(str, l);
		monitorString = str;
		// System.out.println(" Adding Monitor String = "+str);

	}

	public String getMonitorString() {
		return monitorString;
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return changes.getPropertyChangeListeners();
	}

	public int getNumberOfPropertyChangeListeners() {
		PropertyChangeListener[] li = changes.getPropertyChangeListeners();
		return li.length;
	}

	public void printListeners() {
		PropertyChangeListener[] li = changes.getPropertyChangeListeners();
		System.out.println(" Total number of listeners = " + li.length);
		System.out.println("EnumList " + changes.hasListeners("enumList"));
		System.out.println("EnumVal " + changes.hasListeners("enumVal"));
		System.out.println("EnumIndex " + changes.hasListeners("enumIndex"));
		System.out.println("Val    " + changes.hasListeners("val"));
		System.out.println("ArrayVal    " + changes.hasListeners("arrayVal"));
		System.out.println("dataType    " + changes.hasListeners("dataType"));
		System.out.println("Read/ Write    " + changes.hasListeners("rw"));
		System.out.println("Read    " + changes.hasListeners("read"));
		System.out.println("Write    " + changes.hasListeners("write"));

	}

	synchronized public void setNewVal(boolean b) {
		valChanged = b;
		notifyAll();
	}

	synchronized public void propertyChange(PropertyChangeEvent pce) {

		// Object source = pce.getSource();

		String propertyName = pce.getPropertyName();
		if(pvMonitor){
			changes.firePropertyChange(monitorString, new Object(), this);
		}

	}

	synchronized public void setIocConnected(boolean b) {
		iocConnected = b;
		// System.out.println(" Setting IOC "+b);

		notifyAll();
	}

	synchronized public boolean waitForIocConnection() {

		while (!iocConnected) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("Thread Interrupted");
			}
		}
		// System.out.println(" Out of Wait For IOC Connection");

		// System.out.println(" IOC Connection in EDO = "+iocConnected);
		return iocConnected;

	}

	public void setMinorVersionNumber(int i) {
		minorVersionNumber = i;
	}

	public int getMinorVersionNumber() {
		return minorVersionNumber;
	}

	public void setUdpSearchCount(int i) {
		udpSearchCount = 0;
	}

	public void updateUdpSearchCount() {
		udpSearchCount++;
		if (udpSearchCount > 20) {
			searchable = false;
			udpSearch.addRemovePv(this, "remove");
			udpSearch.addNotFoundPV(this);
		}
	}

	public void setLastSearchTime(long l) {
		lastSearchTime = l;
	}

	public long getLastSearchTime() {
		return lastSearchTime;
	}

	public void resetIoc() {
		ioc = null;
	}

	public void initUdpSearchCount() {
		udpSearchCount = 0;
	}

	public int getUdpSearchCount() {
		return udpSearchCount;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public void setSearchable(boolean b) {
		searchable = b;
	}

	public void setUdpConnected(boolean b) {
		udpConnected = b;
	}

	public boolean isUdpConnected() {
		return udpConnected;
	}

	public void setUdpConnectTime(long l) {
		udpConnectTime = l;
	}

	public long getUdpConnectTime() {
		return udpConnectTime;
	}

	public void setTcpConnectTime(long l) {
		tcpConnectTime = l;
	}

	public long getTcpConnectTime() {
		return tcpConnectTime;
	}

	public void setClaimedResourceRequest(boolean b) {
		claimedResource = b;
	}

	public boolean isClaimedResourceRequest() {
		return claimedResource;
	}

	public boolean isIocTcpConnected() {

		if (ioc != null) {
			return ioc.isIocConnected();
		} else {
			return false;
		}
	}

	public String getIocConnectionStatus() {
		String ret = "Connected";

		while (!iocConnected) {

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return ret;
	}

	public void setReadNotifyRequest(boolean b) {
		readNotify = b;
	}

	public boolean isReadNotifyRequest() {
		return readNotify;
	}
	public void setEDOInit(boolean b){
		edoInit = b;
	}
	
	public void setCountDownConnectionDown(){
		if(!countDownReset){
		countDownConnection.removeObject(this);			
		countDownReset = true;
		}
		
	}
	public void setPvName(String str) {
		pvName = str;
	}

	public String getPvName() {
		return pvName;
	}

	public void setRw(int i) {
		rw = i;
	}

	public int getRw() {
		return rw;
	}

	public void setChid(int i) {
		chid = i;
	}

	public int getChid() {
		return chid;
	}

	public void setChidStatus(boolean b) {
		chidStatus = b;
	}

	public boolean isChidStatus() {
		return chidStatus;
	}

	public void setSid(int i) {
		sid = i;
	}

	public int getSid() {
		return sid;
	}

	public void setSubId(int i) {
		subId = i;
	}

	public int getSubId() {
		return subId;
	}

	public void setRNId(int i) {
		rnid = i;
	}

	public int getRNId() {
		return rnid;
	}

	public void setPvMonitor(boolean b) {
		pvMonitor = b;
	}

	public boolean isPvMonitor() {
		return pvMonitor;
	}

	public void setMonitorRequest(boolean b) {
		monitorRequest = b;
	}

	public boolean isMonitorRequested() {
		return monitorRequest;
	}

	public void reConnectPropertyChangeListeners() {
		String str;
		this.removePropertyChangeListener((PropertyChangeListener) parentObj);
		for (int i = 0; i < parentListenerList.size(); i++) {
			str = parentListenerList.get(i).toString();
			this.addPropertyChangeListener(str, (PropertyChangeListener) parentObj);
		}
	}

	public void reConnect() {

		String str;
		udpSearchCount = 0;
		searchable = true;
		udpSearch.addRemovePv(this, "add");
		/**
		 * First Remove the listener from parent
		 */
		this.removePropertyChangeListener((PropertyChangeListener) parentObj);
		for (int i = 0; i < parentListenerList.size(); i++) {
			str = parentListenerList.get(i).toString();
			this.addPropertyChangeListener(str, (PropertyChangeListener) parentObj);

		}

		if (!udpSearch.isAlive()) {
			udpSearch.start();
		}

	}

	public void setBaseDataObject(BaseDataObject obj) {
		baseDataObject = obj;
		nativeDataType = ((DataType) obj).getDataType();

	}
/*
	public void setDataObject(BaseDataObject obj) {

		baseDataObject = obj;
		nativeDataType = ((DataType) obj).getDataType();
	}
*/
	public void setIocTcp(Ioc obj) {
		ioc = obj;
	}

	public void setPropertyChangeListener(String str) {
		((EpicsDataPropChangeListener) baseDataObject).addPropertyChangeListener(str,
				(java.beans.PropertyChangeListener) this);
	}

	public void setPropertyChangeListener(String str, Object obj) {

		changes.addPropertyChangeListener(str, (java.beans.PropertyChangeListener) obj);
	}

	public BaseDataObject getDataObject() {
		return baseDataObject;
	}

	public void initParms() {
		/**
		 * First lets set all initial parameters to their default init state.
		 */
		searchable = false;
		udpConnected = false;
		claimedResource = false;
		readNotify = false;
		pvMonitor = false;
		monitorRequest = false;
		/**
		 * Now lets drop all listeners.
		 */
		setIocConnected(false);
		dropPropertyChangeListener();

		/**
		 * Now lets set the ioc to null
		 */
		resetIoc();
	}

	public int getDataType() {
		return nativeDataType;
	}

	public void setNumPoints(int i) {
		numPoints = i;
		arrayValues = new String[numPoints];
	}

	public int getNumPoints() {
		return baseDataObject.getNumPoints();
	}

	public void setEnumIndex(int n) {
		enumValIndex = n;
	}

	public synchronized int getEnumIndex() {
		if (ioc == null) {

			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!baseDataObject.getInitialized()) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return enumValIndex;
	}

	public void setReadFlag(boolean b) {
		readFlag = b;
	}

	public boolean getReadFlag() {
		return readFlag;
	}

	public void putVal(String str) {
		String[] strArr = new String[1];
		strArr[0] = str;
		putVal(strArr);
	}

	public void putVal(String[] str) {
		if (isIocTcpConnected()) {

			putValArrayLength = str.length;
			putValArray = new String[putValArrayLength];

			for (int i = 0; i < putValArrayLength; i++) {
				putValArray[i] = str[i];
			}
			String oldObject = new String("");
			String newObject = new String("array");

			changes.firePropertyChange("write", oldObject, newObject);
		}
	}

	public void setArrayValues(String[] str) {
		for (int i = 0; i < numPoints; i++) {
			arrayValues[i] = str[i];
		}
	}

	public synchronized String getVal() {
		if (!iocConnected) {

			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (baseDataObject.getInitialized()) {
			if (isPvMonitor()) {
				// if (iocConnected) {

				if (nativeDataType == 3) {
					return arrayValues[enumValIndex];
				} else {
					return arrayValues[0];
				}
			}
			setReadFlag(true);
			CubbyHole ch = new CubbyHole();
			baseDataObject.setCubbyHole(ch);

			changes.firePropertyChange("read", new Object(), this);

			try {
				Object obj = ch.takeOut();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			baseDataObject.setCubbyHole(null);
		}
		if (nativeDataType == 3) {

			return arrayValues[enumValIndex];
		}
	
		return arrayValues[0];
	}

	public synchronized String[] getArrayVal() {
		if (ioc == null) {

			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (baseDataObject.getInitialized()) {
			if (iocConnected) {
				return arrayValues;
			}
			setReadFlag(true);
			CubbyHole ch = new CubbyHole();
			baseDataObject.setCubbyHole(ch);

			changes.firePropertyChange("read", new Object(), this);

			try {
				Object obj = ch.takeOut();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			baseDataObject.setCubbyHole(null);
		}
		return arrayValues;
	}

	public void disconnectChannel() {
		setDropPv(true);
	}

	public void setDropPv(boolean b) {
		dropPv = b;
		/**
		 * Since we are dropping this PV, lets set searchable to false
		 * 
		 */
		searchable = false;
		/**
		 * Add this object to a list in IocTcp. Objects in this list will be
		 * dropped.
		 */
		this.dropAllPropertyChangeListeners();
		ioc.addDropObj(this);
	}

	public boolean getDropPv() {
		return dropPv;
	}

	byte[] getMessageBytes(int dd) {
		byte[] messageBytes = new byte[1];
		double d;
		String s;
		float f;
		long l;
		int i;
		short sh;
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(byteOut);

		try {

			switch (dd) {

			case 0:
				for (int ik = 0; ik < putValArrayLength; ik++) {
					dataOut.writeBytes(putValArray[ik]);
				}
				break;

			case 1:
				i = Integer.parseInt(putVal);
				dataOut.writeInt(i);

				break;

			case 2:

				f = Float.parseFloat(putVal);
				dataOut.writeFloat(f);

				break;

			case 3:

				d = Double.parseDouble(putVal);
				dataOut.writeDouble(d);

				break;

			case 4:

				break;

			case 5:

				l = Long.parseLong(putVal);
				dataOut.writeLong(l);

				break;

			case 6:

				for (int ik = 0; ik < putValArrayLength; ik++) {
					d = Double.parseDouble(putValArray[ik]);
					dataOut.writeDouble(d);
				}

				break;
			}
			messageBytes = new byte[byteOut.toByteArray().length];
			messageBytes = byteOut.toByteArray();

		} catch (IOException ie) {
		}
		return messageBytes;
	}

	public byte[] getWriteBytes() {
		byte[] buf;
		short m_cmmd;
		short m_datatype;
		short m_count;
		int m_cid;
		int m_avail;
		CaHdrOut caHdrOut = new CaHdrOut();
		m_cmmd = (short) 0x04;
		if (nativeDataType == 0) {
			m_datatype = 0;
		} else {
			m_datatype = 0x06;
		}
		m_count = (short) putValArray.length;
		m_cid = sid;
		m_avail = (int) 0xFFFFFFFF;
		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_datatype);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_avail);
		caHdrOut.setMessage(getMessageBytes(m_datatype));

		buf = caHdrOut.getByteArray();

		return buf;
	}

	public byte[] getReadBytes() {
		byte[] buf;
		short m_cmmd;
		short m_datatype;
		short m_count;
		int m_cid;
		int m_avail;
		CaHdrOut caHdrOut = new CaHdrOut();

		m_cmmd = (short) 0x03;
		m_datatype = (short) 0x00;
		m_count = (short) 0x00;
		m_cid = sid;
		m_avail = rnid;

		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_datatype);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_avail);
		caHdrOut.setMessage("");

		buf = caHdrOut.getByteArray();
		return buf;
	}

	public byte[] getDropChannelBytes() {
		byte[] monBytes;
		byte[] buf;
		short m_cmmd;
		short m_datatype;
		short m_count;
		int m_cid;
		int m_avail;
		CaHdrOut caHdrOut = new CaHdrOut();
		;

		/**
		 * First check to see if there is monitor set
		 */

		if (pvMonitor) {

			m_cmmd = (short) 0x02;
			m_datatype = (short) nativeDataType;
			m_count = (short) 0x01;
			m_cid = sid;
			m_avail = subId;
			caHdrOut.setCommand(m_cmmd);
			caHdrOut.setDataType(m_datatype);
			caHdrOut.setCount(m_count);
			caHdrOut.setCid(m_cid);
			caHdrOut.setAvailable(m_avail);
			caHdrOut.setMessage("");
			monBytes = caHdrOut.getByteArray();
		}
		m_cmmd = (short) 0x0C;
		m_datatype = (short) 0x00;
		m_count = (short) 0x00;
		m_cid = sid;
		m_avail = rnid;

		caHdrOut.setCommand(m_cmmd);
		caHdrOut.setDataType(m_datatype);
		caHdrOut.setCount(m_count);
		caHdrOut.setCid(m_cid);
		caHdrOut.setAvailable(m_avail);
		caHdrOut.setMessage("");

		buf = caHdrOut.getByteArray();
		return buf;
	}

	public synchronized void runSafe(Runnable task) {
		if (SwingUtilities.isEventDispatchThread()) {
			task.run();
		} else {
			SwingUtilities.invokeLater(task);
		}
	}

}
