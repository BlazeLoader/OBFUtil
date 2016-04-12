package net.acomputerdog.OBFUtil.table;

import net.acomputerdog.OBFUtil.util.ObfMap;
import net.acomputerdog.OBFUtil.util.TargetType;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A simple, direct implementation of OBFTable.  Uses HashMaps and ArrayLists to store data.
 */
public class DirectOBFTable<P extends ObfMap.Entry, T extends ObfMap<P>> implements OBFTable {
	protected final TargetTypeMap<T> tableMappings = new TargetTypeMap<T>();
	private int size = 0;
	
    public String deobf(String obfName, TargetType type) {
		return tableMappings.getChecked(type).byObf(obfName).deObf();
    }
    
    public String obf(String deobfName, TargetType type) {
		return tableMappings.getChecked(type).byDeobf(deobfName).obf();
    }
        
    public boolean hasObf(String obfName, TargetType type) {
    	return tableMappings.containsKey(type) && tableMappings.getChecked(type).hasObf(obfName);
    }
    
    public boolean hasDeobf(String deobfName, TargetType type) {
    	return tableMappings.containsKey(type) && tableMappings.getChecked(type).hasDeObf(deobfName);
    }
    
    public String[] getAllObf(TargetType type) {
    	return tableMappings.getChecked(type).getAllObf();
    }
    
    public String[] getAllDeobf(TargetType type) {
    	return tableMappings.getChecked(type).getAllDeObf();
    }
    
    protected void preAdd(TargetType type) {
    	size++;
    	if (!tableMappings.containsKey(type)) tableMappings.put(type, createMap());
    }
    
    protected T createMap() {
    	return (T)new Mapping();
    }
    
    public void addType(String obfName, String deObfName, TargetType type) {
    	preAdd(type);
    	tableMappings.get(type).add(obfName, deObfName);
    }
    
    public void writeToTable(OBFTable table, boolean overwrite) {
    	for (Entry<TargetType, T> i : tableMappings.entrySet()) {
    		i.getValue().write(table, overwrite, i.getKey());
    	}
    }
    
    public int size() {
    	return size;
    }
    
    public class Mapping implements ObfMap<P> {
    	
    	protected final Map<String, P> obfuscated = new HashMap<String, P>(); 
    	protected final Map<String, P> deobfuscated = new HashMap<String, P>();
    	
    	public String[] getAllObf() {
    		return obfuscated.keySet().toArray(new String[obfuscated.size()]);
    	}
    	
    	public String[] getAllDeObf() {
    		return deobfuscated.keySet().toArray(new String[deobfuscated.size()]);
    	}
    	
		public P byDeobf(String deobf) {
			return deobfuscated.get(deobf);
		}
		
		public P byObf(String obf) {
			return obfuscated.get(obf);
		}
    	
		public boolean hasObf(String obf) {
			return obfuscated.containsKey(obf);
		}
		
		public boolean hasDeObf(String deobf) {
			return deobfuscated.containsKey(deobf);
		}
		
		public void add(String obf, String deobf) {
			add((P)new ObfEntry(obf, deobf));
		}
		
		protected void add(P entry) {
			obfuscated.put(entry.obf(), entry);
			deobfuscated.put(entry.deObf(), entry);
		}
		
		public void write(OBFTable table, boolean overwrite, TargetType type) {
			for (Map.Entry<String, P> i : obfuscated.entrySet()) {
	            if (overwrite || !table.hasObf(i.getKey(), type)) {
	            	table.addType(i.getKey(), i.getValue().deObf(), type);
	            }
	        }
		}
    }
    
    public class ObfEntry implements ObfMap.Entry {
		
		public String obfuscated;
		public String deobfuscated;
		
		public ObfEntry(String obf, String deobf) {
			obfuscated = obf;
			deobfuscated = deobf;
		}
		
		public String obf() {
			return obfuscated;
		}
		
		public String deObf() {
			return deobfuscated;
		}
	}
}
