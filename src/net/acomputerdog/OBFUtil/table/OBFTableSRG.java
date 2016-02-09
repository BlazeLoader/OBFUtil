package net.acomputerdog.OBFUtil.table;

import net.acomputerdog.OBFUtil.util.TargetType;

/**
 * Extension to OBFTable that adds support for a third "searge" obfuscation name.
 */
public interface OBFTableSRG extends OBFTable {
	
    public void addTypeSRG(String obfName, String seargeName, String deObfName, TargetType type);
    
    public String getObfFromSRG(String searge, TargetType type);
    
    public String getDeObfFromSRG(String searge, TargetType type);
    
    public String getSRGFromObf(String obf, TargetType type);
    
    public String getSRGFromDeObf(String deobf, TargetType type);
    
    public boolean hasSRG(String srgName, TargetType type);
    
    public String[] getAllSRG(TargetType type);
}
