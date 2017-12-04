package edu.ciw.hpcat.epics.data;

public class CubbyHole {

	private Object slot;

	public CubbyHole(){
		slot = null;
	}
	
	public synchronized void putIn(Object obj) throws InterruptedException {
		
		while(slot !=null){
		wait();	
		}
		slot = obj;
		notifyAll();
	}


public synchronized Object takeOut()throws InterruptedException{
	while(slot == null){
		wait();
	}
	
	Object obj = slot;
	slot = null;
	notifyAll();
	return obj;
	}
}