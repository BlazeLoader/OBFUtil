package net.acomputerdog.OBFUtil.table;

import java.util.HashMap;
import java.util.Map;

import net.acomputerdog.OBFUtil.util.ObfMapSrg;
import net.acomputerdog.OBFUtil.util.TargetType;

/**
 * OBFTable that adds support for a third "searge" obfuscation name.  Based on DirectOBFTable.
 */
public class DirectOBFTableSRG<P extends ObfMapSrg.Entry, T extends ObfMapSrg<P>> extends DirectOBFTable<P, T> implements OBFTableSRG {
	
    public String getObfFromSRG(String searge, TargetType type) {
    	return tableMappings.getChecked(type).bySrg(searge).obf();
    }
    
    public String getDeObfFromSRG(String searge, TargetType type) {
    	return tableMappings.getChecked(type).bySrg(searge).deObf();
    }
    
    public String getSRGFromObf(String obf, TargetType type) {
    	return tableMappings.getChecked(type).byObf(obf).srg();
    }
    
    public String getSRGFromDeObf(String deobf, TargetType type) {
    	return tableMappings.getChecked(type).byDeobf(deobf).srg();
    }
    
    public boolean hasSRG(String srgName, TargetType type) {
    	return tableMappings.containsKey(type) && tableMappings.getChecked(type).hasSrg(srgName);
    }
    
    public String[] getAllSRG(TargetType type) {
    	return tableMappings.getChecked(type).getAllSrg();
    }
    
    protected T createMap() {
    	return (T)new MappingSrg();
    }
    
    public void addType(String obfName, String deObfName, TargetType type) {
        addTypeSRG(obfName, deObfName, deObfName, type);
    }
    
    public void addTypeSRG(String obfName, String seargeName, String deObfName, TargetType type) {
    	preAdd(type);
    	tableMappings.get(type).add(obfName, deObfName, seargeName);
    }
    
    public class MappingSrg extends Mapping implements ObfMapSrg<P> {
    	
    	protected final Map<String, P> searge = new HashMap<String, P>();
    	
		public String[] getAllSrg() {
			return searge.keySet().toArray(new String[searge.size()]);
		}
		
		public P bySrg(String srg) {
			return searge.get(srg);
		}
		
		public boolean hasSrg(String srg) {
			return searge.containsKey(srg);
		}
		
		public void add(String obf, String deobf, String srg) {
			add((P)new ObfEntrySrg(obf, deobf, srg));
		}
		
		public void add(String obf, String deobf) {
			add(obf, deobf, deobf);
		}
    	
		protected void add(P entry) {
			super.add(entry);
			searge.put(entry.srg(), entry);
		}
    }
    
    public class ObfEntrySrg extends DirectOBFTable.ObfEntry implements ObfMapSrg.Entry {
		public String searge;
		
		public ObfEntrySrg(String obf, String deobf, String srg) {
			super(obf, deobf);
			searge = srg;
		}
		
		public String srg() {
			return searge;
		}
	}
}
