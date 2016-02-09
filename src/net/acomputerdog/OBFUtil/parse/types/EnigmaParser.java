package net.acomputerdog.OBFUtil.parse.types;

import java.util.List;

import com.google.common.collect.Lists;

import net.acomputerdog.OBFUtil.parse.FormatException;
import net.acomputerdog.OBFUtil.table.DirectOBFTableSRG;
import net.acomputerdog.OBFUtil.table.OBFTable;
import net.acomputerdog.OBFUtil.util.TargetType;
import net.acomputerdog.core.java.Patterns;

public class EnigmaParser extends BLOBFParser {
	
    protected void parseStringArraySRG(String[] lines, DirectOBFTableSRG table, boolean overwrite) throws FormatException {
        int line = 0;
        String activeClass = null;
        String activeClassObf = null;
        List<String[]> retroActiveMethods = Lists.newArrayList();
        for (String str : lines) {
            line++;
            if (isCommentLine(str)) continue;
            
            String[] parts = str.trim().split(Patterns.SPACE);
            
            TargetType type = TargetType.getType(parts[0]);
            switch (type) {
	            case CLASS:
	            	activeClassObf = parts[1].replace("none/", "");
	            	activeClass = parts[2];
	            	table.addTypeSRG(activeClassObf, activeClassObf, activeClass, type);
	            case FIELD:
	            	table.addTypeSRG(activeClassObf + "." + parts[1], activeClassObf + "." + parts[1], activeClass + "." + parts[2], type);
	            case METHOD:
	            	if (parts.length == 4) {
	            		retroActiveMethods.add(new String[] {activeClassObf + "." + parts[1], activeClass + "." + parts[2], parts[3]});
	            	} else if (parts.length == 3 && "<init>".equals(parts[1])) {
	            		retroActiveMethods.add(new String[] {activeClassObf + "." + parts[1], activeClass + "." + parts[1], parts[2], ""});
	            	}
            	default: continue;
            }
        }
        for (String[] s : retroActiveMethods) {
        	String completed = "";
        	String[] descriptor = s[2].split("none/");
        	for (int i = 0; i < descriptor.length; i++) {
        		if (descriptor[i].contains(";")) {
        			String deobf = table.deobf(descriptor[i].split(";")[0], TargetType.CLASS);
        			if (deobf != null) descriptor[i] = deobf + descriptor[i].split(";")[1];
        		}
        		completed += descriptor[i];
        	}
        	table.addTypeSRG(s[0] + " " + s[2].replace("none/", ""), s[0] + " " + s[2].replace("none/", ""), s[1] + " " + completed, s.length == 3 ? TargetType.METHOD : TargetType.CONSTRUCTOR);
        }
    }

    protected void parseStringArrayNormal(String[] lines, OBFTable table, boolean overwrite) throws FormatException {
        int line = 0;
        String activeClass = null;
        String activeClassObf = null;
        List<String[]> retroActiveMethods = Lists.newArrayList();
        for (String str : lines) {
            line++;
            if (isCommentLine(str)) continue;
            
            String[] parts = str.trim().split(Patterns.SPACE);
            
            TargetType type = TargetType.getType(parts[0]);
            switch (type) {
	            case CLASS:
	            	activeClassObf = parts[1].replace("none/", "");
	            	activeClass = parts[2];
	            	table.addType(activeClassObf, activeClass, type);
	            case FIELD:
	            	table.addType(activeClassObf + "." + parts[1], activeClass + "." + parts[2], type);
	            case METHOD:
	            	if (parts.length == 4) {
	            		retroActiveMethods.add(new String[] {activeClassObf + "." + parts[1], activeClass + "." + parts[2], parts[3]});
	            	} else if (parts.length == 3 && "<init>".equals(parts[1])) {
	            		retroActiveMethods.add(new String[] {activeClassObf + "." + parts[1], activeClass + "." + parts[1], parts[2], ""});
	            	}
            	default: continue;
            }
        }
        for (String[] s : retroActiveMethods) {
        	String completed = "";
        	String[] descriptor = s[2].split("none/");
        	for (int i = 0; i < descriptor.length; i++) {
        		if (descriptor[i].contains(";")) {
        			String deobf = table.deobf(descriptor[i].split(";")[0], TargetType.CLASS);
        			if (deobf != null) descriptor[i] = deobf + descriptor[i].split(";")[1];
        		}
        		completed += descriptor[i];
        	}
        	table.addType(s[0] + " " + s[2].replace("none/", ""), s[1] + " " + completed, s.length == 3 ? TargetType.METHOD : TargetType.CONSTRUCTOR);
        }
    }
}
