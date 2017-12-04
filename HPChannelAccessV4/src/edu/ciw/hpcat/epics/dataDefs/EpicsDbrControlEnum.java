package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EpicsDbrControlEnum extends EpicsDbrGREnum implements EpicsControlData  {
	  private boolean pvMonitorSet = false;

	  public EpicsDbrControlEnum() {
	  }

	  public void readControlData(byte[] in) {

	    short sh;
	    String st;
	    byte[] b = new byte[26];
	
	    try {

	      ByteArrayInputStream byteIn = new ByteArrayInputStream(in);
	      DataInputStream dataIn = new DataInputStream(byteIn);

	      sh = dataIn.readShort();
	      setStatus(sh);
	      sh = dataIn.readShort();
	      setSeverity(sh);
	      sh = dataIn.readShort();
	      setNumPoints(sh);
	      for (short j = 0; j < 16; j++) {
	        dataIn.read(b, 0, 26);
	        st = new String(b);
	        if (j < sh) {
	          setStringVal(st.trim(), j);
	        }
	      }
	      sh = dataIn.readShort();
	      edo.setEnumIndex(sh);
	      setEnumIndex(sh);

	      setEdoArrayVal();
			setInitialized(true);
//		      changes.firePropertyChange(edo.getMonitorString(), new Object(), new Object());

			if (cubbyHole != null) {
//				System.out.println(" In Control Enum Setting object in CubbyHole Index ="+sh);

				cubbyHole.putIn(new Object());
			}
		} catch (IOException | InterruptedException ie) {

		}
	    
	  }

	  public void setPvMonitorSet(boolean b) {
	    pvMonitorSet = b;
	  }
}
