package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EpicsDbrControlLong extends EpicsDbrGRLong implements EpicsControlData {

	protected int upperControlLimit;
	protected int lowerControlLimit;
	protected boolean pvMonitorSet = false;

	public EpicsDbrControlLong() {
	}

	public void readControlData(byte[] in) {

		short sh;
		String st;
		int i;

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
			i = dataIn.readInt();
			setUpperDisplayLimit(i);
			i = dataIn.readInt();
			setLowerDisplayLimit(i);
			i = dataIn.readInt();
			setUpperAlarmLimit(i);
			i = dataIn.readInt();
			setUpperWarningLimit(i);
			i = dataIn.readInt();
			setLowerWarningLimit(i);
			i = dataIn.readInt();
			setLowerAlarmLimit(i);
			i = dataIn.readInt();
			setUpperControlLimit(i);
			i = dataIn.readInt();
			setLowerControlLimit(i);
			for (int j = 0; j < numPoints; j++) {

				i = dataIn.readInt();
				setVal(i, j);
			}
			setEdoArrayVal();
			setInitialized(true);
			// System.out.println(" In Control Long ");

			if (cubbyHole != null) {
				cubbyHole.putIn(new Object());
			}
		} catch (IOException | InterruptedException ie) {

		}
	}

	public void setUpperControlLimit(int i) {
		upperControlLimit = i;
	}

	public void setLowerControlLimit(int i) {
		lowerControlLimit = i;
	}

	public void setPvMonitorSet(boolean b) {
		pvMonitorSet = b;
	}
}
