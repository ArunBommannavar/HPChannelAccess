package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EpicsDbrControlString extends EpicsDbrTimeString implements EpicsControlData {
    private boolean pvMonitorSet = false;
    EpicsString epicsStr;
    int strLen;

    public EpicsDbrControlString() {
    }

    public void readGRData(byte[] in){
    	
    }
    public void readControlData(byte[] in) {

        short st;
        short sev;
        int numBytes;// = 44;
 //       int numBytes = 28;
        int junk;

        String v = new String();
        byte[] b;

        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(in);
            DataInputStream dataIn = new DataInputStream(byteIn);

            st = dataIn.readShort();
            sev = dataIn.readShort();
            setStatus(st);
            setStatus(sev);

            numBytes = dataIn.available();
            for (int i = 0; i < numPoints; i++) {

                b = new byte[numBytes];
                dataIn.read(b);
                epicsStr = new EpicsString();
                strLen = epicsStr.getStringLength(b);
                v = epicsStr.getString(b,strLen);
                setVal(v, i);
            }
            
            numBytes = dataIn.available();
            if (numBytes > 0) {
                b = new byte[numBytes];
                dataIn.read(b);
            }
            setEdoArrayVal();
			setInitialized(true);

            if(cubbyHole !=null){
            	cubbyHole.putIn(new Object());
            }
          } catch (IOException | InterruptedException ie) {

        }
    }

    public void setPvMonitorSet(boolean b) {
        pvMonitorSet = b;
    }
}
