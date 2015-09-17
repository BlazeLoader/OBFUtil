package net.acomputerdog.OBFUtil.util;

public interface ObfMapSrg<T> extends ObfMap<T> {
	
	public String[] getAllSrg();
	
	public T bySrg(String srg);
	
	public boolean hasSrg(String srg);
	
	public void add(String obf, String deobf, String srg);
}
