package net.acomputerdog.OBFUtil.map;

public interface ObfMapSrg<T extends ObfMapSrg.Entry> extends ObfMap<T> {
	
	public String[] getAllSrg();
	
	public T bySrg(String srg);
	
	public boolean hasSrg(String srg);
	
	public void add(String obf, String deobf, String srg);
	
    public static interface Entry extends ObfMap.Entry {
    	String srg();
    }
}
