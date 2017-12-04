package edu.ciw.hpcat.epics.test;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CountDownLatch;

import edu.ciw.hpcat.epics.data.*;
public class ReadTest implements PropertyChangeListener{
//	ConnectionManager cm = ConnectionManager.getInstance();
	CountDownConnection countDownConnection = CountDownConnection.getInstance();
	CountDownLatch latch;
	EpicsDataObject obj;	
	EpicsDataObject obj1;	
	EpicsDataObject obj2;
	EpicsDataObject obj3;
	EpicsDataObject[] edo;
	EpicsDataObject[] edo1;
	EpicsDataObject[] edo2;
	EpicsDataObject[] edo3;
	int numberOfDetectors = 60;
	long stime;
	public ReadTest(){
		
	}
	
	public void createObject(){
//		boolean objConnected = false;

	
	
		obj = new EpicsDataObject("16TEST1:scan1.EXSC",true);
		obj.addPropertyChangeListener("val", this);
//		new Thread (countDownConnection).start();
		
		obj1 = new EpicsDataObject("16TEST1:scan1.VAL",true);
		obj1.addPropertyChangeListener("val", this);

		obj2 = new EpicsDataObject("16TEST1:scan1.DATA",true);
		obj2.addPropertyChangeListener("val", this);


		obj3 = new EpicsDataObject("16TEST1:scan1.DSTATE",true);
		obj3.addPropertyChangeListener("val", this);
		countDownConnection.pendIO();
/*		
		System.out.println(" After next 2");
		obj1 = new  EpicsDataObject("16TEST1:scan1.BUSY",true);
		obj1.addPropertyChangeListener("val", this);
		
		obj2 = new  EpicsDataObject("16TEST1:scan1.VAL",true);
		obj2.addPropertyChangeListener("val", this);

*/
		
	}
	
	public void dropObject(){
		obj.disconnectChannel();
	}
	public void crateObjectArray(){
		int j;
		String pvNameStr;
		String str = "16TEST1:scan1";
		boolean ret;
		edo = new EpicsDataObject[numberOfDetectors];

		stime = System.currentTimeMillis();
		for (int i=0; i< numberOfDetectors;i++){
			j=i+1;
		   pvNameStr = str + ".D" + String.format("%02d", j) + "NV";
			edo[i] = new EpicsDataObject(pvNameStr,true);
			edo[i].addPropertyChangeListener("val", this);
		}
		countDownConnection.pendIO();

	}
	public void readValue(){
//	String str = obj.getVal();
//	System.out.println(" Value = "+obj.getVal()+"   No. Of Points = "+obj.getNumPoints()+" Data Type = "+obj.getDataType());
	}
	
	public void readValueOnce(){
		System.out.println(" Value = "+obj.getVal());
		
	}
	public void readAllValues(){
		for(int i=0; i< numberOfDetectors;i++){
			System.out.println(" PV = "+edo[i].getPvName()+"  Index = "+edo[i].getEnumIndex());
		}
	}
	public void readArrayValue(){
		for (int k=0; k< numberOfDetectors;k++){
	String[] str = edo[k].getArrayVal();
	
	for(int i=0; i< str.length;i++)
	System.out.println("Array Value Read = "+i+"  pvName = "+edo[k].getPvName()+"   "  +str[i]);
		}
	}
	
	synchronized public void propertyChange(PropertyChangeEvent evt) {
	    Object source = evt.getSource();
	    String propertyName = evt.getPropertyName();
//		System.out.println(" Change propert Fired    "+" Data Type = "+obj.getDataType()+"   "+((EpicsDataObject)source).getVal()+"  "+((EpicsDataObject)source).getPvName());
//	    readValue();
	    System.out.println("Time = "+(System.currentTimeMillis()-stime)+" PV Name = "+ ((EpicsDataObject)source).getPvName() +"  Value=  "+((EpicsDataObject)source).getVal());
	}
	public static void main(String[] args) {
		ReadTest rt = new ReadTest();
//		rt.crateObjectArray();	

		rt.createObject();			
//		rt.readArrayValue();
//		rt.readValue();
//		rt.readValueOnce();
		/*
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
//		System.out.println(" Dropping Channel ");

//		rt.dropObject();

//		rt.readAllValues();
//		System.exit(0);
	}


}
