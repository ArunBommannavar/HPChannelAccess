package edu.ciw.hpcat.epics.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CaHdrIn {
	  private short m_cmmd;
	  private short m_postSize;
	  private short m_type;
	  private short m_count;
	  private int m_cid;
	  private int m_available;
//	  private String message;

	  private int zero = 0;

//	  private byte[] totalDataBuf;
//	  private byte[] toalHeaderBuf;

	  private ByteArrayInputStream byteArrayIn;
	  private DataInputStream dataArrayIn;

	  public CaHdrIn(byte[] data, int i) {

	    byteArrayIn = new ByteArrayInputStream(data, zero, i);
	    dataArrayIn = new DataInputStream(byteArrayIn);
	  }

	  public CaHdrIn(DataInputStream buf) {
	    dataArrayIn = buf;
	  }

	  public CaHdrIn(byte[] data) {
	    byteArrayIn = new ByteArrayInputStream(data);
	    dataArrayIn = new DataInputStream(byteArrayIn);
	  }

	  public void readData() {
	    try {
	      m_cmmd = (short) (dataArrayIn.readShort() & 0xFFFF);
	      m_postSize = (short) (dataArrayIn.readShort() & 0xFFFF);
	      m_type = (short) (dataArrayIn.readShort() & 0xFFFF);
	      m_count = (short) (dataArrayIn.readShort() & 0xFFFF);
	      m_cid = (int) (dataArrayIn.readInt() & 0xFFFFFFFF);
	      m_available = (int) (dataArrayIn.readInt() & 0xFFFFFFFF);

	    }
	    catch (IOException e) {
	      System.out.println(" CaHdrIn IO Exception");
	    }
	  }

	  public short getCommand() {
	    return (short) (m_cmmd & 0xFFFF);
	  }

	  public short getPostSize() {
	    return (short) (m_postSize & 0xFFFF);
	  }

	  public short getType() {
	    return (short) (m_type & 0xFFFF);
	  }

	  public short getCount() {
	    return (short) (m_count & 0xFFFF);
	  }

	  public int getCid() {
	    return (int) (m_cid & 0xFFFFFFFF);
	  }

	  public int getAvailable() {
	    return (int) (m_available & 0xFFFFFFFF);
	  }

	  public InetAddress getAddress() {
	    InetAddress result = null;

	    byte[] buf = new byte[4];

	    buf[0] = (byte) ( (m_available >> 24) & 0xFF);
	    buf[1] = (byte) ( (m_available >> 16) & 0xFF);
	    buf[2] = (byte) ( (m_available >> 8) & 0xFF);
	    buf[3] = (byte) ( (m_available & 0xFF));

	    try {
	      result = InetAddress.getByAddress(buf);
	    }
	    catch (UnknownHostException e) {
	    }

	    return result;
	  }

	  public String getMessage(byte[] b) {
	    String str = b.toString();
	    return str;
	  }

	  public void flushData() {

	  }

	  public int dataAvailableForRead() {
	    int avail = 0;
	    try {
	      avail = dataArrayIn.available();
	    }
	    catch (IOException e) {
	    }
	    return avail;
	  }
}
