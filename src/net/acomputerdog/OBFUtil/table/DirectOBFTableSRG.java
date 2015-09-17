package net.acomputerdog.OBFUtil.table;

import java.util.HashMap;
import java.util.Map;

import net.acomputerdog.OBFUtil.util.ObfMapSrg;
import net.acomputerdog.OBFUtil.util.TargetType;

/**
 * OBFTable that adds support for a third "searge" obfuscation name.  Based on DirectOBFTable.
 */
public class DirectOBFTableSRG<P extends DirectOBFTableSRG.ObfEntrySrg, T extends ObfMapSrg<P>> extends DirectOBFTable<P, T> implements OBFTableSRG {
	
    public String getObfFromSRG(String searge, TargetType type) {
    	return tableMappings.getChecked(type).bySrg(searge).obfuscated;
    }
    
    public String getDeObfFromSRG(String searge, TargetType type) {
    	return tableMappings.getChecked(type).bySrg(searge).deobfuscated;
    }
    
    public String getSRGFromObf(String obf, TargetType type) {
    	return tableMappings.getChecked(type).byObf(obf).searge;
    }
    
    public String getSRGFromDeObf(String deobf, TargetType type) {
    	return tableMappings.getChecked(type).byDeobf(deobf).searge;
    }
    
    public boolean hasSRG(String srgName, TargetType type) {
    	return tableMappings.getChecked(type).hasSrg(srgName);
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
    	
    	private final Map<String, P> searge = new HashMap<String, P>();
    	
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
			searge.put(entry.searge, entry);
		}
    }
    
    public class ObfEntrySrg extends DirectOBFTable.ObfEntry {
		public String searge;
		
		public ObfEntrySrg(String obf, String deobf, String srg) {
			super(obf, deobf);
			searge = srg;
		}
	}
}
