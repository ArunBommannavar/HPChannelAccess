package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EpicsDbrGRDouble extends EpicsDbrTimeDouble implements EpicsGrData {
   	protected String units;

    protected short riscPad0;

    protected double upperDisplayLimit;
    protected double lowerDisplayLimit;

    protected double upperWarningLimit;
    protected double lowerWarningLimit;

    protected double upperAlarmLimit;
    protected double lowerAlarmLimit;

	@Override
	public void readGRData(byte[] in) {

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


            for (int i = 0; i < numPoints; i++) {
                d = dataIn.readDouble();
                setVal(d, i);
            }
			setEdoArrayVal();
			setInitialized(true);
//	        System.out.println(" dbr control = "+edo.getMonitorString());
//	        changes.firePropertyChange(edo.getMonitorString(), new Object(), new Object());

            if(cubbyHole !=null){
            	cubbyHole.putIn(new Object());
            }
          } catch (IOException | InterruptedException ie) {

        }

	}
    public void setRiscPad0(short sh) {
        riscPad0 = sh;
    }

    public void setUnits(String st) {
        units = st;
    }

    public void setUpperDisplayLimit(double d) {
        upperDisplayLimit = d;
    }

    public void setLowerDisplayLimit(double d) {
        lowerDisplayLimit = d;
    }

    public void setUpperAlarmLimit(double d) {
        upperAlarmLimit = d;
    }

    public void setUpperWarningLimit(double d) {
        upperWarningLimit = d;
    }

    public void setLowerWarningLimit(double d) {
        lowerWarningLimit = d;
    }

    public void setLowerAlarmLimit(double d) {
        lowerAlarmLimit = d;
    }

}
