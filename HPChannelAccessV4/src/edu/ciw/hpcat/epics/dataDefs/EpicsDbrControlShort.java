package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EpicsDbrControlShort extends EpicsDbrGRShort implements EpicsControlData{
//    protected String units;

 
    protected short upperControlLimit;
    protected short lowerControlLimit;
    protected boolean pvMonitorSet = false;

    public EpicsDbrControlShort() {
    }

    public void readControlData(byte[] in){

        short sh;
        String st;

        byte[] b = new byte[8];

        try {
            ByteArrayInputStream    byteIn= new ByteArrayInputStream(in);
            DataInputStream         dataIn= new DataInputStream(byteIn);

            sh =    dataIn.readShort ();
            setStatus(sh);

            sh =    dataIn.readShort ();
            setSeverity(sh);

            dataIn.read (b,0,8);

            st = new String(b);
            st = st.trim ();

            setUnits(st);

            sh =    dataIn.readShort ();
            setUpperDisplayLimit(sh);
            sh =    dataIn.readShort ();
            setLowerDisplayLimit(sh);

            sh =    dataIn.readShort ();
            setUpperAlarmLimit(sh);
            sh =    dataIn.readShort ();
            setUpperWarningLimit(sh);

            sh =    dataIn.readShort ();
            setLowerWarningLimit(sh);
            sh =    dataIn.readShort ();
            setLowerAlarmLimit(sh);

            sh =    dataIn.readShort ();
            setUpperControlLimit(sh);
            sh =    dataIn.readShort ();
            setLowerControlLimit(sh);
            for (int i=0; i< numPoints; i++){

                sh = dataIn.readShort();
                setVal(sh,i);
            }
            setEdoArrayVal();
			setInitialized(true);

            if(cubbyHole !=null){
            	cubbyHole.putIn(new Object());
            }
          } catch (IOException | InterruptedException ie) {

        }
    }
/*
    public void setUnits(String st){
        units = st;
    }

    public void setUpperDisplayLimit (short sh){
        upperDisplayLimit = sh;
    }

    public void setLowerDisplayLimit (short sh){
        lowerDisplayLimit = sh;
    }

    public void setUpperAlarmLimit (short sh){
        upperAlarmLimit = sh;
    }

    public void setUpperWarningLimit (short sh){
        upperWarningLimit = sh;
    }

    public void setLowerWarningLimit (short sh){
        lowerWarningLimit = sh;
    }

    public void setLowerAlarmLimit (short sh){
        lowerAlarmLimit = sh;
    }
*/
    public void setUpperControlLimit (short sh){
        upperControlLimit = sh;
    }

    public void setLowerControlLimit (short sh){
        lowerControlLimit = sh;
    }
    public void setPvMonitorSet(boolean b){
      pvMonitorSet = b;
 }
}
