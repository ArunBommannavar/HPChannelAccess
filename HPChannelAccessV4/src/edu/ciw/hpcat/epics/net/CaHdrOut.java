package edu.ciw.hpcat.epics.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

public class CaHdrOut {
	private String message;
	  private short m_cmmd;
	  private short m_postsize;
	  private short m_type;
	  private short m_count;
	  private int m_cid;
	  private int m_available;
	  private byte[] msgBytes;

	  private int numberOfBytes;
//	  private int eight = 8;
	  private byte zero = 0x00;

	  private byte[] totalBytes;
	  private byte[] messageBytes;
	  private int msgByteLength;
	  private int temp0;

	  ByteArrayOutputStream byteArrayOut;
	  DataOutputStream bufArrayOut;

	  public CaHdrOut() {
	    byteArrayOut = new ByteArrayOutputStream();
	    bufArrayOut = new DataOutputStream(byteArrayOut);
	    numberOfBytes = 0;
	    message = "";
	  }

	  public void setCommand(short i) {
	    this.m_cmmd = (short) (i & 0xFFFF);
	  }

	  public void setPostSize(short i) {
	    this.m_postsize = (short) (i & 0xFFFF);
	  }

	  public void setDataType(short i) {
	    this.m_type = (short) (i & 0xFFFF);
	  }

	  public void setCount(short i) {
	    this.m_count = (short) (i & 0xFFFF);
	  }

	  public void setCid(int i) {
	    this.m_cid = i & 0xFFFFFFFF;
	  }

	  public void setAvailable(int i) {
	    this.m_available = i & 0xFFFFFFFF;

	  }
	  public void setAvailable(InetAddress addr) {
	     this.m_available =ipAddressToInt(addr) & 0xFFFFFFFF;

	   }

	  public int ipAddressToInt(InetAddress addr) {

	    byte[] buf = addr.getAddress();

	    int result = ( (buf[0] & 0xFF) << 24)
	        | ( (buf[1] & 0xFF) << 16)
	        | ( (buf[2] & 0xFF) << 8)
	        | (buf[3] & 0xFF);

	    return result;
	  }

	  public void setMessage(String msg) {
	    int mLength;
	    byte[] b = msg.getBytes();
	    mLength = b.length;

	    int mo = mLength % 8;
	    if (mo == 0) {

	      byte[] b1 = new byte[b.length + 1];
	      for (int i = 0; i < b.length; i++) {
	        b1[i] = b[i];
	      }
	      b1[b.length] = 0;
	      setMessage(b1);
	    }
	    else {
	      setMessage(b);
	    }
	  }

	  public void setMessage(byte[] b) {
	    int x;
	    msgBytes = b;
	    msgByteLength = msgBytes.length;

	    if (msgByteLength > 0) {
	      int mo = msgByteLength % 8;
	      if (mo > 0) {
	        temp0 = 8 - mo;
	      }
	      else {
	        temp0 = 0;
	      }
	    }
	    else {
	      temp0 = 0;
	    }
	    m_postsize = (short) (msgByteLength + temp0);
	  }

	  public void clearBuffer() {

	    byteArrayOut.reset();
	    msgBytes = new byte[0];

	  }

	  public byte[] getByteArray() {

	    try {

	      bufArrayOut.writeShort(m_cmmd);
	      bufArrayOut.writeShort(m_postsize);
	      bufArrayOut.writeShort(m_type);
	      bufArrayOut.writeShort(m_count);
	      bufArrayOut.writeInt(m_cid);
	      bufArrayOut.writeInt(m_available);

	      if (msgBytes.length > 0) {

	        bufArrayOut.write(msgBytes);
	        for (int i = 0; i < temp0; i++) {
	          bufArrayOut.writeByte(zero);
	        }
	      }

	    }
	    catch (IOException e) {

	      // catch IOException and post a message
	    }

	    return byteArrayOut.toByteArray();

	  }

}
