package edu.ciw.hpcat.epics.dataDefs;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import edu.ciw.hpcat.epics.data.CubbyHole;
import edu.ciw.hpcat.epics.data.EpicsDataObject;
import edu.ciw.hpcat.epics.data.EpicsDataPropChangeListener;
import edu.ciw.hpcat.epics.data.NumPoints;

public class BaseDataObject implements EpicsDataPropChangeListener,NumPoints {

	EpicsDataObject edo;
	CubbyHole cubbyHole = null;

	public int numPoints = 1;
	public String[] strValues;
	String basePvName = "";
	
	boolean initialized = false;
	
	protected PropertyChangeSupport changes = new PropertyChangeSupport(this);

	public BaseDataObject() {
	}

	public void dropAllPropertyChangeListeners() {
		PropertyChangeListener[] li = changes.getPropertyChangeListeners();
		for (int i = 0; i < li.length; i++) {
			removePropertyChangeListener(li[i]);
		}
	}

	public void addPropertyChangeListener(String str, PropertyChangeListener l) {
		changes.addPropertyChangeListener(str, l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}

	public void removePropertyChangeListener(String str,
			PropertyChangeListener l) {
		changes.removePropertyChangeListener(str, l);
	}

	public void setEpicsDataObject(EpicsDataObject edo) {
		this.edo = edo;
	}
	public void setBasePvName(String str){
		basePvName = str;
	}

	public String getBasePvName(){
		return basePvName;
	}
	public void setCubbyHole(CubbyHole ch){
		this.cubbyHole = ch;
	}
	public void printListeners() {
		PropertyChangeListener[] li = changes.getPropertyChangeListeners();
		for (int i = 0; i < li.length; i++) {
			// removePropertyChangeListener(li[i]);
		}

	}

	@Override
	public void setNumPoints(int i) {
		numPoints = i;
		
	}

	@Override
	public int getNumPoints() {
		
		return numPoints;
	}
	
	public void setInitialized(boolean b){
		initialized = b;
	}
	
	public boolean getInitialized(){
		return initialized;
	}
}
