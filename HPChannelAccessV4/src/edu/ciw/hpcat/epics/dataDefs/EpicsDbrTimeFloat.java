package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import edu.ciw.hpcat.epics.data.DataType;


public class EpicsDbrTimeFloat extends BaseDataObject implements DataType, EpicsTimeData{
	  protected short status;
	  protected short severity;
	  protected short precision;
	  protected long secPastEpoch;
	  protected int dataType = 2;
	  protected int numPoints = 1;
	  protected float val; // = 0.0f;
	  protected float[] arrayVal;

	  public EpicsDbrTimeFloat() {

	  }

	  public int getDataType() {
	    return dataType;
	  }

	  public void readTimeData(byte[] in) {
	    short sh;
	    float fl;
	    long l;

	    try {
	      ByteArrayInputStream byteIn = new ByteArrayInputStream(in);
	      DataInputStream dataIn = new DataInputStream(byteIn);
	      sh = dataIn.readShort();
	      setStatus(sh);
	      sh = dataIn.readShort();
	      setSeverity(sh);
	      l = dataIn.readLong();
	      setSecPastEpoch(l);

	      for (int i = 0; i < numPoints; i++) {

	        fl = dataIn.readFloat();
	        setVal(fl, i);
	      }

	    }
	    catch (IOException ie) {

	    }
        setEdoArrayVal();
        changes.firePropertyChange(edo.getMonitorString(), new Object(), new Object());

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

	  public void setPrecision(short sh) {
	    precision = sh;
	  }

	  public void setVal(float f, int i) {
	    float fv = (float) Math.floor( (double) f * Math.pow(10, precision) + 0.5f);
	    fv = fv / (float) Math.pow(10, precision);
	    arrayVal[i]=f;
	    String str = String.valueOf(f);
	    strValues[i]=str;

	  }

	  public void setNumPoints(int n) {
	    numPoints = n;
	    arrayVal = new float[numPoints];
		edo.setNumPoints(numPoints);
	    strValues = new String[numPoints];
	    for (int ii = 0; ii < numPoints; ii++) {
	      arrayVal[ii] = Float.MAX_VALUE;
	    }
	  }
		public void setEdoArrayVal(){
			edo.setArrayValues(strValues);
		}

	  public int getNumPoints() {
	    return numPoints;
	  }
}
