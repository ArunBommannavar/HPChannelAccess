package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EpicsDbrGRLong extends EpicsDbrTimeLong implements EpicsGrData {
	  protected String units;
	  protected int upperDisplayLimit;
	  protected int lowerDisplayLimit;

	  protected int upperWarningLimit;
	  protected int lowerWarningLimit;

	  protected int upperAlarmLimit;
	  protected int lowerAlarmLimit;

	@Override
	public void readGRData(byte[] in) {
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
	
	
	  public void setUnits(String st) {
		    units = st;
		  }

		  public void setUpperDisplayLimit(int i) {
		    upperDisplayLimit = i;
		  }

		  public void setLowerDisplayLimit(int i) {
		    lowerDisplayLimit = i;
		  }

		  public void setUpperAlarmLimit(int i) {
		    upperAlarmLimit = i;
		  }

		  public void setUpperWarningLimit(int i) {
		    upperWarningLimit = i;
		  }

		  public void setLowerWarningLimit(int i) {
		    lowerWarningLimit = i;
		  }

		  public void setLowerAlarmLimit(int i) {
		    lowerAlarmLimit = i;
		  }


}
