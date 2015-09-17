package net.acomputerdog.OBFUtil.table;

import net.acomputerdog.OBFUtil.util.TargetType;

/**
 * Represents an object capable of managing obfuscation data.
 */
public interface OBFTable {
    
    /**
     * Gets the deobfuscated name of a TargetType.
     * @param obfName The obfuscated name.
     * @param type The type of obfuscation to get.
     * @return Return the deobfuscated name, or null if the mapping is not defined.
     */
    public String deobf(String obfName, TargetType type);

    /**
     * Gets the obfuscated name of a TargetType.
     *
     * @param deobfName The deobfuscated name.
     * @param type      The type of obfuscation to get.
     * @return Return the obfuscated name, or null if the mapping is not defined.
     */
    public String obf(String deobfName, TargetType type);

    /**
     * Adds an obfuscation mapping for a TargetType.
     * @param obfName The obfuscated name.
     * @param deObfName The deobfuscated name.
     * @param type The type to define.
     */
    public void addType(String obfName, String deObfName, TargetType type);
    
    /**
     * Checks if a TargetType mapping is defined.
     * @param obfName The obfuscated name.
     * @param type The type to check.
     * @return Return true if the mapping is defined, false otherwise.
     */
    public boolean hasObf(String obfName, TargetType type);
    
    /**
     * Checks if a TargetType mapping is defined.
     *
     * @param deobfName The deobfuscated name.
     * @param type      The type to check.
     * @return Return true if the mapping is defined, false otherwise.
     */
    public boolean hasDeobf(String deobfName, TargetType type);
    
    /**
     * Get an array of all obfuscated TargetType names.
     *
     * @param type The type to get.
     * @return Return an array of Strings representing all obfuscated names.
     */
    public String[] getAllObf(TargetType type);
    
    /**
     * Get an array of all deobfuscated TargetType names.
     * @param type The type to get.
     * @return Return an array of Strings representing all deobfuscated names.
     */
    public String[] getAllDeobf(TargetType type);

    /**
     * Write the contents of this table to another table.
     * @param table The table to write to.
     * @param overwrite If true, overwrite existing mappings.
     */
    public void writeToTable(OBFTable table, boolean overwrite);
}
