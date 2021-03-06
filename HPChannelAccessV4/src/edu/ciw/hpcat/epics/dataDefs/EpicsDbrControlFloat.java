package edu.ciw.hpcat.epics.dataDefs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EpicsDbrControlFloat extends EpicsDbrGRFloat implements EpicsControlData{


    protected float upperControlLimit;
    protected float lowerControlLimit;
    protected boolean pvMonitorSet = false;


    public EpicsDbrControlFloat() {
    }

    public void readControlData(byte[] in) {
        short sh;
        String st;
        float fl;

        byte[] b = new byte[8];

        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(in);
            DataInputStream dataIn = new DataInputStream(byteIn);
            // status
            sh = dataIn.readShort();
            setStatus(sh);
            // severity
            sh = dataIn.readShort();
            setSeverity(sh);
            // precision
            sh = dataIn.readShort();
            setPrecision(sh);
            // Risc
            sh = dataIn.readShort();
            setRisc(sh);
            // units
            dataIn.read(b, 0, 8);
            st = new String(b);
            st = st.trim();
            setUnits(st);

            // upper display limit
            fl = dataIn.readFloat();
            setUpperDisplayLimit(fl);

            // lower display limit
            fl = dataIn.readFloat();
            setLowerDisplayLimit(fl);

            // upper alarm limit
            fl = dataIn.readFloat();
            setUpperAlarmLimit(fl);

            // upper warning limit
            fl = dataIn.readFloat();
            setUpperWarningLimit(fl);

            //lower warning limit
            fl = dataIn.readFloat();
            setLowerWarningLimit(fl);

            // lower alarm limit
            fl = dataIn.readFloat();
            setLowerAlarmLimit(fl);

            // upper control limit
            fl = dataIn.readFloat();
            setUpperControlLimit(fl);

            // lower control limit
            fl = dataIn.readFloat();
            setLowerControlLimit(fl);


            for (int i = 0; i < numPoints; i++) {
                fl = dataIn.readFloat();
                setVal(fl, i);
            }
            int remBytes = dataIn.available();
            if (remBytes >0){
              b = new byte[remBytes];
              dataIn.read(b, 0, remBytes);

            }
			setEdoArrayVal();
			setInitialized(true);
  //          System.out.println(" In Control float ");

            if(cubbyHole !=null){
            	cubbyHole.putIn(new Object());
            }
          } catch (IOException | InterruptedException ie) {

        }
    }


    public void setUpperControlLimit(float fl) {
        upperControlLimit = fl;
    }

    public void setLowerControlLimit(float fl) {
        lowerControlLimit = fl;
    }

    public void setPvMonitorSet(boolean b) {
        pvMonitorSet = b;
    }
}
