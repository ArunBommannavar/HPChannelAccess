package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EpicsDbrGRShort extends EpicsDbrTimeShort implements EpicsGrData {
	
    protected String units;

    protected short upperDisplayLimit;
    protected short lowerDisplayLimit;

    protected short upperWarningLimit;
    protected short lowerWarningLimit;

    protected short upperAlarmLimit;
    protected short lowerAlarmLimit;
    
    protected short value; 

	@Override
	public void readGRData(byte[] in) {
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

}
