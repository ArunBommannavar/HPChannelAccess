package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import edu.ciw.hpcat.epics.data.DataType;

public class EpicsDbrTimeShort extends BaseDataObject implements DataType,EpicsTimeData {
	protected short status;
	protected short severity;
	protected long secPastEpoch;
	protected short risc;
	protected int dataType = 1;
	protected int numPoints = 1;
	protected short val;
	protected short[] arrayVal;

	public EpicsDbrTimeShort() {

	}

	public int getDataType() {

		return dataType;
	}

	public void readTimeData(byte[] in) {

		short sh;
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
			for (int i = 0; i < numPoints; i++) {
				sh = dataIn.readShort();
				setVal(sh, i);
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

	public void setStatus(short sh) {
		status = sh;
	}

	public void setSeverity(short sh) {
		severity = sh;
	}

	public void setVal(short v, int i) {
		String str = String.valueOf(v);
		arrayVal[i] = v;
		strValues[i] = str;
	}

	public void setNumPoints(int i) {
		numPoints = i;
		arrayVal = new short[numPoints];
		strValues = new String[numPoints];
		edo.setNumPoints(i);
		for (int ii = 0; ii < numPoints; ii++) {
			arrayVal[ii] = Short.MAX_VALUE;
		}
	}
	public void setEdoArrayVal() {
		edo.setArrayValues(strValues);
	}
	public int getNumPoints() {
		return numPoints;
	}
}
