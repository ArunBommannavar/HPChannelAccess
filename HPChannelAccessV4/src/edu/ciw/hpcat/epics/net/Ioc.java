package edu.ciw.hpcat.epics.net;

import java.util.List;

public interface Ioc {
	public void addEpicsDataObject(Object obj);

	public boolean isIocConnected();

	public void initConnection();

	public void addDropObj(Object obj);

	public void initialize();

	public boolean isInitialized();

	public void stop();

	public void start();

	public boolean isAlive();

	public long getLastBeaconTime();

	public void setLastBeaconTime(long l);

	public String getIp();

	public void setConnectionCheck(boolean b);

	public int getConnectCounter();

	public void initEdo();

	public List<Object> getEpicsObjectsList();
}
