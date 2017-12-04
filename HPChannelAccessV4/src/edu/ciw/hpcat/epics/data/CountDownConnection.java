package edu.ciw.hpcat.epics.data;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class CountDownConnection implements Runnable {

	private static CountDownConnection countDownConnection= new CountDownConnection();
	
	CountDownLatch latch = null;
	int numberOfLatches = 0;
	ArrayList<Object> edoList = new ArrayList<Object>();
	volatile boolean startAddPv = false;
	boolean running = false;
	
	private CountDownConnection (){
	}
	
	public static CountDownConnection getInstance(){
		return countDownConnection;
	}
	public void addObject(EpicsDataObject edo){
		edoList.add(edo);
		numberOfLatches++;
	}
	
	public int getNumberOfLatches(){
		return numberOfLatches;
	}
	public void removeObject(EpicsDataObject edo){
		edoList.remove(edo);
		numberOfLatches--;
		latch.countDown();

	}

	public boolean getStatus(){
		return running;
	}
	public void pendIO(){
		latch = new CountDownLatch(numberOfLatches);
		startAddPv = true;
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println(" In Pend IO " );
			e.printStackTrace();
		}		
	}
	
	
	public void startUdpSearch(){
		for (int i=0; i<edoList.size();i++){
			((EpicsDataObject)(edoList.get(i))).addPvToUdpSearch();
		}
	}
	
	public CountDownLatch getcountDownLatch(){
		return latch;
	}
	@Override
	public void run() {
		running = true;
		while(true){
			
			if(startAddPv){
				startUdpSearch();
			}
			startAddPv = false;
			try {
				Thread.sleep(15);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
}
