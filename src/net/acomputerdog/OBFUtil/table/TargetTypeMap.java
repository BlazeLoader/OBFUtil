package net.acomputerdog.OBFUtil.table;

import java.util.HashMap;

import net.acomputerdog.OBFUtil.util.TargetType;

public class TargetTypeMap<T> extends HashMap<TargetType, T> {
	
	public T getChecked(TargetType type) {
		if (!containsKey(type)) throw new IllegalArgumentException("Unknown target type: " + type.name());
		return get(type);
	}
}
