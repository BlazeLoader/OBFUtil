package net.acomputerdog.OBFUtil.util;

import net.acomputerdog.OBFUtil.table.OBFTable;

public interface ObfMap<T> {
	
	public String[] getAllObf();
	
	public String[] getAllDeObf();
	
	public T byDeobf(String deobf);
	
	public T byObf(String obf);
	
	public boolean hasObf(String obf);
	
	public boolean hasDeObf(String deobf);
	
	public void add(String obf, String deobf);
	
	public void write(OBFTable table, boolean overwrite, TargetType type);
}
