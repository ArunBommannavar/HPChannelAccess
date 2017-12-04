package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import edu.ciw.hpcat.epics.data.DataType;



public class EpicsDbrTimeChar extends BaseDataObject implements DataType,EpicsTimeData {
	protected short status;
	protected short severity;
	protected long secPastEpoch;
	protected short risc;
	protected byte pad1;

//	protected byte val;
	protected byte[] arrayVal;
	protected String units;

	protected int dataType = 4;

	public EpicsDbrTimeChar() {

	}

	public int getDataType() {

		return dataType;
	}

	public void readTimeData(byte[] in) {

		short sh;
		byte by;
		long l;

		try {
			ByteArrayInputStream byteIn = new ByteArrayInputStream(in);
			DataInputStream dataIn = new DataInputStream(byteIn);
			sh = dataIn.readShort();
			setStatus(sh);
			sh = dataIn.readShort();
			setSeverity(sh);
			l = dataIn.readLong();
			setSecPastEpoch(l);
			sh = dataIn.readShort();
			setRisc(sh);
			by = dataIn.readByte();
			setPad1(by);

			for (int i = 0; i < numPoints; i++) {

				by = dataIn.readByte();
				setVal(by, i);
			}

		} catch (IOException ie) {

		}
        setEdoArrayVal();
        changes.firePropertyChange(edo.getMonitorString(), new Object(), new Object());

	}

	public void setSecPastEpoch(long l) {
		secPastEpoch = l;
	}

	public void setRisc(short sh) {
		risc = sh;
	}

	public void setPad1(byte by) {
		pad1 = by;
	}

	public void setStatus(short sh) {
		status = sh;
	}

	public void setSeverity(short sh) {
		severity = sh;
	}

	public void setVal(byte b, int i) {
		arrayVal[i]= b;
		strValues[i]=Byte.toString(b);
	}
	
	public void setNumPoints(int n) {
		numPoints = n;
		edo.setNumPoints(n);
		arrayVal = new byte[numPoints];
		strValues = new String[numPoints];
		for (int ii = 0; ii < numPoints; ii++) {
			arrayVal[ii] = Byte.MAX_VALUE;
		}
	}

	public void setEdoArrayVal(){
		edo.setArrayValues(strValues);
	}
	public int getNumPoints() {
		return numPoints;
	}
}
