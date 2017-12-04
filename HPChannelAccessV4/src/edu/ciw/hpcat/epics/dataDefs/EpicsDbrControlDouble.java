package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EpicsDbrControlDouble extends EpicsDbrGRDouble implements
EpicsControlData, Runnable{

	    protected double upperControlLimit;
	    protected double lowerControlLimit;
	    protected boolean pvMonitorSet = false;
//	    private boolean doneReading = false;
	    public EpicsDbrControlDouble() {

	    }

	    synchronized public void readControlData(byte[] in) {

	        short sh;
	        String st;
	        double d;
	        byte[] b = new byte[8];

	        try {
	            ByteArrayInputStream byteIn = new ByteArrayInputStream(in);
	            DataInputStream dataIn = new DataInputStream(byteIn);

	            sh = dataIn.readShort();
	            setStatus(sh);
	            sh = dataIn.readShort();
	            setSeverity(sh);
	            sh = dataIn.readShort();
	            setPrecision(sh);
	            sh = dataIn.readShort();
	            setRiscPad0(sh);

	            dataIn.read(b, 0, 8);
	            st = new String(b);
	            st = st.trim();
	            setUnits(st);
	            d = dataIn.readDouble();
	            setUpperDisplayLimit(d);
	            d = dataIn.readDouble();
	            setLowerDisplayLimit(d);
	            d = dataIn.readDouble();
	            setUpperAlarmLimit(d);
	            d = dataIn.readDouble();
	            setUpperWarningLimit(d);
	            d = dataIn.readDouble();
	            setLowerWarningLimit(d);
	            d = dataIn.readDouble();
	            setLowerAlarmLimit(d);
	            d = dataIn.readDouble();
	            setUpperControlLimit(d);
	            d = dataIn.readDouble();
	            setLowerControlLimit(d);

	            for (int i = 0; i < numPoints; i++) {
	                d = dataIn.readDouble();
	                setVal(d, i);
	            }
				setEdoArrayVal();
				setInitialized(true);
//		        System.out.println(" dbr control = "+edo.getMonitorString());
//		        changes.firePropertyChange(edo.getMonitorString(), new Object(), new Object());

	            if(cubbyHole !=null){
	            	cubbyHole.putIn(new Object());
	            }
	          } catch (IOException | InterruptedException ie) {

	        }
	    }


	    public void setUpperControlLimit(double d) {
	        upperControlLimit = d;
	    }

	    public void setLowerControlLimit(double d) {
	        lowerControlLimit = d;
	    }

	    public void setPvMonitorSet(boolean b) {
	        pvMonitorSet = b;
	    }

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
}
