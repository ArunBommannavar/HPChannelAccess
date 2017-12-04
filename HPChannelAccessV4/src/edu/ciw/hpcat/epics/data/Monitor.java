package edu.ciw.hpcat.epics.data;

public interface Monitor {
    public void setPvMonitor(boolean b);
    public boolean isPvMonitor();
    public void setMonitorRequest(boolean b);
    public boolean isMonitorRequested();
}
