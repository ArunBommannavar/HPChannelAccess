package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EpicsDbrControlChar extends EpicsDbrGRChar implements
		EpicsControlData {

//	protected String units;
	protected byte upperControlLimit;
	protected byte lowerControlLimit;

	protected boolean pvMonitorSet = false;

	public EpicsDbrControlChar() {
		
	}

	public void readControlData(byte[] in) {

		short sh;
		String st;
		byte by;

		byte[] b = new byte[8];

		try {
			ByteArrayInputStream byteIn = new ByteArrayInputStream(in);
			DataInputStream dataIn = new DataInputStream(byteIn);

			sh = dataIn.readShort();
			setStatus(sh);

			sh = dataIn.readShort();
			setSeverity(sh);

			dataIn.read(b, 0, 8);

			st = new String(b);
			st = st.trim();

			setUnits(st);

			by = dataIn.readByte();
			setUpperDisplayLimit(by);

			by = dataIn.readByte();
			setLowerDisplayLimit(by);

			by = dataIn.readByte();
			setUpperAlarmLimit(by);
			by = dataIn.readByte();
			setUpperWarningLimit(by);

			by = dataIn.readByte();
			setLowerWarningLimit(by);
			by = dataIn.readByte();
			setLowerAlarmLimit(by);

			by = dataIn.readByte();
			setUpperControlLimit(by);
			by = dataIn.readByte();
			setLowerControlLimit(by);
			
			by = dataIn.readByte();
			setPad1(by);

			for (int i = 0; i < numPoints; i++) {
				by = dataIn.readByte();
				setVal(by, i);
			}

			setEdoArrayVal();
			setInitialized(true);
//	        changes.firePropertyChange(edo.getMonitorString(), new Object(), new Object());
			if (cubbyHole != null) {
				cubbyHole.putIn(new Object());
			}
		} catch (IOException | InterruptedException ie) {

		}

	}
/*
	public void setUnits(String st) {
		units = st;
	}

	public void setUpperDisplayLimit(byte by) {
		upperDisplayLimit = by;
	}

	public void setLowerDisplayLimit(byte by) {
		lowerDisplayLimit = by;
	}

	public void setUpperAlarmLimit(byte by) {
		upperAlarmLimit = by;
	}

	public void setUpperWarningLimit(byte by) {
		upperWarningLimit = by;
	}

	public void setLowerWarningLimit(byte by) {
		lowerWarningLimit = by;
	}

	public void setLowerAlarmLimit(byte by) {
		lowerAlarmLimit = by;
	}
*/
	public void setUpperControlLimit(byte by) {
		upperControlLimit = by;
	}

	public void setLowerControlLimit(byte by) {
		lowerControlLimit = by;
	}

	public void setPvMonitorSet(boolean b) {
		pvMonitorSet = b;
	}

}
