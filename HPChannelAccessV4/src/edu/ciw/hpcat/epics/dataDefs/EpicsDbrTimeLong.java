package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import edu.ciw.hpcat.epics.data.DataType;



public class EpicsDbrTimeLong extends BaseDataObject implements DataType,EpicsTimeData {
	protected short status;
	protected short severity;
	protected long secPastEpoch;
	protected int dataType = 5;
	protected int numPoints = 1;
	protected int val;
	protected int[] arrayVal;

	public EpicsDbrTimeLong() {
	}

	public void readTimeData(byte[] in) {

		short sh;
		long l;
		int i;

		try {
			ByteArrayInputStream byteIn = new ByteArrayInputStream(in);
			DataInputStream dataIn = new DataInputStream(byteIn);

			sh = dataIn.readShort();
			setStatus(sh);

			sh = dataIn.readShort();
			setSeverity(sh);

			l = dataIn.readLong();
			setSecPastEpoch(l);

			for (int j = 0; j < numPoints; j++) {
				i = dataIn.readInt();
				setVal(i, j);
			}

		} catch (IOException ie) {

		}
        setEdoArrayVal();
        changes.firePropertyChange(edo.getMonitorString(), new Object(), new Object());

	}

	public int getDataType() {
		return dataType;
	}

	public void setSecPastEpoch(long l) {
		secPastEpoch = l;
	}

	public void setStatus(short sh) {
		status = sh;
	}

	public void setSeverity(short sh) {
		severity = sh;
	}

	public void setVal(int n, int j) {
		String str = Integer.toString(n);
		arrayVal[j] = n;
		strValues[j] = str;
	}

	public void setNumPoints(int i) {
		numPoints = i;
		arrayVal = new int[numPoints];
		strValues = new String[numPoints];
		edo.setNumPoints(i);
		for (int j = 0; j < numPoints; j++) {
			arrayVal[j] = 0;
		}
	}

	public void setEdoArrayVal() {
		edo.setArrayValues(strValues);
	}

	public int getNumPoints() {
		return numPoints;
	}
}
