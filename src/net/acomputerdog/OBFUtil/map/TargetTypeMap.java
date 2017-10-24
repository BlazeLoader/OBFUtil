package net.acomputerdog.OBFUtil.map;

import java.util.HashMap;

public class TargetTypeMap<T> extends HashMap<TargetType, T> {
	private static final long serialVersionUID = 2485577485561601623L;

	public T getChecked(TargetType type) {
		if (!containsKey(type)) throw new IllegalArgumentException("Unknown target type: " + type.name());
		return get(type);
	}
}
