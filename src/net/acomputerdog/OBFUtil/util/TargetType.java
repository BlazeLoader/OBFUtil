package net.acomputerdog.OBFUtil.util;

import java.util.ArrayList;

/**
 * Represents the part of a class to target.
 */
public enum TargetType {
    /**
     * A package name/structure
     */
    PACKAGE("PACKAGE", "PK"),
    /**
     * A class
     */
    CLASS("CLASS", "CL"),
    /**
     * A method or constructor
     */
    METHOD("METHOD", "MD"),
    /**
     * A field
     */
    FIELD("FIELD", "FD"),
    /**
     * A class constructor
     */
    CONSTRUCTOR("CONSTRUCTOR", "CSTR", "CS");
    
    private final String[] aliases;

    TargetType(String... aliases) {
        this.aliases = aliases;
    }
    
    /**
     * Gets the simplest form of this TargetType.
     */
    public TargetType getBaseType() {
    	return this == CONSTRUCTOR ? METHOD : this;
    }

    /**
     * Gets a TargetType from it's name or an alias
     *
     * @param type The name to identify with
     * @return Return the TargetType identified by type
     */
    public static TargetType getType(String type) {
        if (type == null) {
            return null;
        }
        for (TargetType tt : values()) {
            for (String str : tt.aliases) {
                if (type.equalsIgnoreCase(str)) {
                    return tt;
                }
            }
        }
        return null;
    }
    
    private final static TargetType[] parsable;
    
    /**
     * Gets the set of TargetTypes that can be stored to an .obf file.
     */
    public static TargetType[] parsable() {
    	return parsable;
    }
    
    static {
    	ArrayList<TargetType> temp = new ArrayList();
    	for (TargetType i : values()) {
    		if (i != CONSTRUCTOR) {
    			temp.add(i);
    		}
    	}
    	parsable = temp.toArray(new TargetType[temp.size()]);
    }
}
