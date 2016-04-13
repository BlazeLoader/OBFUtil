package net.acomputerdog.OBFUtil.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.acomputerdog.OBFUtil.map.TargetType;
import net.acomputerdog.OBFUtil.table.OBFTable;
import net.acomputerdog.core.java.Patterns;

public class Obfuscator {
	private static final Pattern DESCRIPTOR_MATCHER = Pattern.compile(Patterns.DESCRIPTOR_PARAMETER);
	
	public String getMemberName(String item) {
		item = item.split(" ")[0];
		int lastDot = item.lastIndexOf('.');
		return item.substring(lastDot + 1, item.length());
	}
	
	public String getMemberClass(String item) {
		item = item.split(" ")[0];
		int lastDot = item.lastIndexOf('.');
		return item.substring(0, lastDot);
	}
	
	public String obfuscateDescriptor(String descriptor, OBFTable table) {
    	String obfuscatedDescriptor = "";
    	String[] split = descriptor.split("\\)");
    	List<String> classes = splitDescriptor(split[0]);
    	if (split.length < 2) throw new IllegalArgumentException("Missing return type for \"" + descriptor + "\"");
    	for (int j = 0; j < classes.size(); j++) {
    		obfuscatedDescriptor += obfParameter(classes.get(j), table);
    	}
    	return "(" + obfuscatedDescriptor + ")" + obfParameter(split[1], table);
    }
	
	public String deObfuscateDescriptor(String descriptor, OBFTable table) {
    	String obfuscatedDescriptor = "";
    	String[] split = descriptor.split("\\)");
    	List<String> classes = splitDescriptor(split[0]);
    	if (split.length < 2) throw new IllegalArgumentException("Missing return type for \"" + descriptor + "\"");
    	for (int j = 0; j < classes.size(); j++) {
    		obfuscatedDescriptor += deObfParameter(classes.get(j), table);
    	}
    	return "(" + obfuscatedDescriptor + ")" + deObfParameter(split[1], table);
    }
	
	public String obfMemberClass(String item, OBFTable table) {
		String className = getMemberClass(item);
		if (table.hasDeobf(className, TargetType.CLASS)) {
			return item.replace(className, table.obf(className, TargetType.CLASS));
		}
		return item;
	}
	
    public String obfParameter(String item, OBFTable table) {
    	if (item.endsWith(";")) {
			String type = extractClass(item);
			String dotted = type.replace('/','.');
			if (table.hasDeobf(dotted, TargetType.CLASS)) {
				return item.replace(type, table.obf(dotted, TargetType.CLASS).replace('.', '/'));
			}
		}
    	return item;
    }
    
    public String deObfParameter(String item, OBFTable table) {
    	if (item.endsWith(";")) {
			String type = extractClass(item);
			String dotted = type.replace('/','.');
			if (table.hasObf(dotted, TargetType.CLASS)) {
				return item.replace(type, table.deobf(dotted, TargetType.CLASS).replace('.', '/'));
			}
		}
    	return item;
    }
    
    private String extractClass(String descriptedClass) {
    	String[] split = descriptedClass.split("\\[");
    	String result = split[split.length - 1];
    	return result.substring(1, result.length() - 1);
    }
    
    public static List<String> splitDescriptor(String descriptor) {
    	List<String> params = new ArrayList<String>();
    	Matcher matcher = DESCRIPTOR_MATCHER.matcher(descriptor.trim());
    	while (matcher.find()) params.add(matcher.group());
    	return params;
    }
}
