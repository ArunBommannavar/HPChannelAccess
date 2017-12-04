package edu.ciw.hpcat.epics.data;

public interface Connected {
    public void setUdpConnected(boolean b);
    public boolean isUdpConnected();
    public void setClaimedResourceRequest(boolean b);
    public boolean isClaimedResourceRequest();
    public void setReadNotifyRequest(boolean b);
    public boolean isReadNotifyRequest();
    public void initUdpSearchCount();
    public void setUdpSearchCount(int i);
    public void updateUdpSearchCount();
    public int getUdpSearchCount();
    public boolean isSearchable();
    public void setSearchable(boolean b);
    public void reConnect();
    public void initParms();
    public void reConnectPropertyChangeListeners();
    public boolean getReadFlag();
    public void setReadFlag(boolean b);
    public void setEDOInit(boolean b);
    public void setCountDownConnectionDown();
}
