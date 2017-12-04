package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import edu.ciw.hpcat.epics.data.DataType;

public class EpicsDbrTimeEnum extends BaseDataObject implements DataType,EpicsTimeData {
	protected short status;
	protected short severity;
	protected short index = -1;
	protected long secPastEpoch;
	protected short risc;
	protected int dataType = 3;
	protected int numPoints = 1;
	protected String[] labels;

	public EpicsDbrTimeEnum() {
	}

	public void readTimeData(byte[] in) {

		short sh;
		long l;
//		int i;
		long tmp;

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
			sh = dataIn.readShort();
			setEnumIndex(sh);

			tmp = dataIn.readLong();
			dataIn.skipBytes(dataIn.available());

//			System.out.println(" Enum Time firing "+edo.getMonitorString());

	        changes.firePropertyChange(edo.getMonitorString(), new Object(), this);


		} catch (IOException ie) {
		}
//        setEdoArrayVal();

	}

	public int getDataType() {
		return dataType;
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

	public void setRisc(short sh) {
		risc = sh;
	}

	public void setStringVal(String str, int n) {
		labels[n] = str;
	}

	public String[] getEnumList() {
		return labels;
	}

	public short getEnumIndex() {
		return index;
	}

	public String getEnumVal() {
		return labels[index];
	}

	public void setEnumIndex(short sh) {
		index = sh;
		edo.setEnumIndex(index);
	}

	public void setNumPoints(int n) {
		numPoints = n;
		labels = new String[numPoints];
		edo.setNumPoints(numPoints);
	}
	public void setEdoArrayVal(){
		edo.setArrayValues(labels);

	}

	public int getNumPoints() {
		return numPoints;
	}
}
