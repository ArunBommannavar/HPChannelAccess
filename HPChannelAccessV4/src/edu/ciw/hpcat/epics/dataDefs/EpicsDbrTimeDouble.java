package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import edu.ciw.hpcat.epics.data.DataType;
//import edu.ciw.hpcat.epics.data.NumPoints;


public class EpicsDbrTimeDouble extends BaseDataObject implements DataType, EpicsTimeData {
	  	protected short status;
	    protected short severity;
	    protected short precision = 0;
        protected double val = Double.MAX_VALUE;
	    protected int riscPad;
	    protected long secPastEpoch;
	    protected int dataType = 6;
	    protected int numPoints = 1;
	    protected double[] arrayVal;

	    public EpicsDbrTimeDouble() {

	    }

	    public int getDataType() {
	        return dataType;
	    }

	    synchronized public void readTimeData(byte[] in) {

	        short sh;
	        long l;
	        double d;
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
	            i = dataIn.readInt();
	            setRiscPad(i);
	            arrayVal = new double[numPoints];
	            for (i = 0; i < numPoints; i++) {
	                d = dataIn.readDouble();
	                setVal(d, i);
	            }
	            
	        } catch (IOException ie) {

	        }

	        setEdoArrayVal();
//	        System.out.println(" dbr time = "+edo.getMonitorString());

	        changes.firePropertyChange(edo.getMonitorString(), new Object(), new Object());

	    }

	    public void setStatus(short sh) {
	        status = sh;
	    }

	    public void setSeverity(short sh) {
	        severity = sh;
	    }

	    public void setPrecision(short sh) {
	        precision = sh;
	    }

	    public void setRiscPad(int i) {
	        riscPad = i;
	    }

	    public void setSecPastEpoch(long l) {
	        secPastEpoch = l;
	    }

	    public void setVal(double v, int i) {
	        double d = Math.floor(v * Math.pow(10, precision) + 0.5);
	        d = d / Math.pow(10, precision);
	        String str = Double.toString(d);
	        
	       	arrayVal[i] = v;
	       	strValues[i]= str;
	    }

	    public void setNumPoints(int n) {
	        numPoints = n;
			edo.setNumPoints(numPoints);

	        arrayVal = new double[numPoints];
	        strValues = new String[numPoints];
	    }
		public void setEdoArrayVal(){
			edo.setArrayValues(strValues);
		}
	    public int getNumPoints() {
	        return numPoints;
	    }
}
