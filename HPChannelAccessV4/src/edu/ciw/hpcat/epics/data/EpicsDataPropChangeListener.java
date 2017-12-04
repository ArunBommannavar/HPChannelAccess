package edu.ciw.hpcat.epics.data;

public interface EpicsDataPropChangeListener {

	public void addPropertyChangeListener(String str,
			java.beans.PropertyChangeListener l);

	public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

	public void removePropertyChangeListener(String str,
			java.beans.PropertyChangeListener l);

	public void dropAllPropertyChangeListeners();

}
