package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EpicsDbrGRChar extends EpicsDbrTimeChar implements EpicsGrData {
	protected String units;

	protected byte upperDisplayLimit;
	protected byte lowerDisplayLimit;

	protected byte upperAlarmLimit;
	protected byte lowerAlarmLimit;

	protected byte upperWarningLimit;
	protected byte lowerWarningLimit;
	

	@Override
	public void readGRData(byte[] in) {
		short sh;
		byte by;
		byte[] b = new byte[8];
		String st;


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

			
			sh = dataIn.readShort();
			setRisc(sh);

			for (int i = 0; i < numPoints; i++) {

				by = dataIn.readByte();
				setVal(by, i);
			}

		} catch (IOException ie) {

		}

	}
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

}
