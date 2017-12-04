package edu.ciw.hpcat.epics.net;

import java.util.concurrent.locks.ReentrantLock;

public class CALock {

	private static CALock caLock = new CALock();
	ReentrantLock lock = new ReentrantLock ();
	
	private CALock(){
		
	}

	  public static CALock getInstance() {
		    return caLock;
		  }	  
	  
	  synchronized public boolean lockIt(String str){
		  boolean status = false;
		  if (!lock.isLocked()){
		  lock.lock();
		  status = true;
	//	  System.out.println(" Attempt by "+str+" to lock success");

		  }else {
	//		  System.out.println(" Attempt by "+str+" to lock no success");

		  }
		  
		  return status;
	  }
	  
	  synchronized public boolean  unLockIt(String str){
		  boolean status = false;
		  if (lock.isLocked()){
		  lock.unlock();
	//	  System.out.println(" Attempt by "+str+" to unlock success ");

		  status = true;
		  } else {
	//		  System.out.println(" Attempt by "+str+" to unlock no success ");
  
		  }
		  
		  return status;
	  }

}
