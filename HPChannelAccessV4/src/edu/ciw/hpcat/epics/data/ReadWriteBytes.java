package edu.ciw.hpcat.epics.data;

public interface ReadWriteBytes {
	public byte[] getReadBytes();

	public byte[] getWriteBytes();

	public void putVal(String[] str);

	public void putVal(String str);
}
