package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import edu.ciw.hpcat.epics.data.DataType;


public class EpicsDbrTimeString extends BaseDataObject implements DataType,
		EpicsTimeData {
	protected short status;
	protected short severity;
	protected String val;
//	protected String[] arrayVal;
	protected long secPastEpoch;
	protected int dataType = 0;
	protected int numPoints = 1;
	java.sql.Timestamp abc;

	public EpicsDbrTimeString() {
	}

	public int getDataType() {

		return dataType;
	}

	synchronized public void readTimeData(byte[] in) {

		byte[] b;
		String v;
		short sh;
		short sev;
//		int i;
		long ll;
		int numBytes;// = 22;
		EpicsString epicsStr;
		int strLen;
		try {
			ByteArrayInputStream byteIn = new ByteArrayInputStream(in);
			DataInputStream dataIn = new DataInputStream(byteIn);
			sh = dataIn.readShort();
			sev = dataIn.readShort();

			setStatus(sh);
			setSeverity(sev);

			ll = dataIn.readLong();
			setSecPastEpoch(ll);
			abc = new java.sql.Timestamp(ll);
			numBytes = dataIn.available();
			for (int j = 0; j < numPoints; j++) {
				b = new byte[numBytes];
				dataIn.read(b);
				epicsStr = new EpicsString();
				strLen = epicsStr.getStringLength(b);
				v = epicsStr.getString(b, strLen);
				setVal(v, j);

			}
			numBytes = dataIn.available();

			if (numBytes > 0) {
				b = new byte[numBytes];
				dataIn.read(b);
			}

		} catch (IOException ie) {

		}
        setEdoArrayVal();
 //       System.out.println(" String Time DBR firing "+edo.getPvName());
        changes.firePropertyChange(edo.getMonitorString(), new Object(), new Object());

	}

	public void setStatus(short sh) {
		status = sh;
	}

	public void setSeverity(short sh) {
		severity = sh;
	}

	public void setSecPastEpoch(long l) {
		secPastEpoch = l;
	}

	public void setVal(String str, int i) {	
		strValues[i]=str.trim();	
	}

	public void setNumPoints(int i) {
		numPoints = i;
		strValues = new String[numPoints];
		edo.setNumPoints(i);
	}
	public void setEdoArrayVal() {
		edo.setArrayValues(strValues);
	}
	public int getNumPoints() {
		return numPoints;
	}
}
