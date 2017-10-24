package net.acomputerdog.OBFUtil.parse.types;

import net.acomputerdog.OBFUtil.map.TargetType;
import net.acomputerdog.OBFUtil.parse.FormatException;
import net.acomputerdog.OBFUtil.table.DirectOBFTableSRG;
import net.acomputerdog.OBFUtil.table.OBFTable;
import net.acomputerdog.OBFUtil.table.OBFTableSRG;

import java.io.*;

import com.blazeloader.util.regex.Patterns;

/**
 * BlazeLoader OBFuscation file.
 * Formatted "<TYPE>:<OBF>:<SEARGE>:<MCP>"  for packages, classes, and fields.
 * Formatted "METHOD:<OBF_NAME>:<OBF_DESC>:<SEARGE_NAME>:<SEARGE_DESC>:<MCP_NAME>:<MCP_DESC>"  for methods
 */
public class BLOBFParser extends OBFParser {

    private final boolean stripDescs;

    public BLOBFParser() {
        this(false);
    }

    public BLOBFParser(boolean stripMethodDescriptors) {
        this.stripDescs = stripMethodDescriptors;
    }
    
	@SuppressWarnings("rawtypes")
	@Override
	protected void parseFile(BufferedReader reader, OBFTable table, boolean overwrite) throws IOException {
		boolean handleSrg = table instanceof DirectOBFTableSRG;
		int line = 0;
        String str;
    	while ((str = reader.readLine()) != null) {
            line++;
            if (isCommentLine(str)) {
                continue;
            }
            String[] parts = str.split(Patterns.COLON);
            if (parts.length < 4) {
                throw new FormatException("Format error on line " + line + ": \"" + str + "\"");
            }
            TargetType type = TargetType.valueOf(parts[0]);
            if (type == null) {
                throw new FormatException("Illegal target type on line " + line + ": \"" + parts[0] + "\"");
            }
            if (handleSrg) {
            	parseStringArraySRG(line, str, parts, type, (DirectOBFTableSRG)table, overwrite);
            } else {
            	parseStringArrayNormal(line, str, parts, type, (DirectOBFTableSRG)table, overwrite);
            }
        }
	}
	
    @SuppressWarnings("rawtypes")
	protected void parseStringArraySRG(int line, String str, String[] parts, TargetType type, DirectOBFTableSRG table, boolean overwrite) throws FormatException, IOException {
    	if (type == TargetType.METHOD) {
            if (parts.length < 7) {
                throw new FormatException("Format error on line " + line + ": \"" + str + "\"");
            }
            if (overwrite || !table.hasObf(parts[1], type)) {
                if (stripDescs) {
                    table.addTypeSRG(parts[1], parts[3], parts[5], type);
                } else {
                    table.addTypeSRG(parts[1] + " " + parts[2], parts[3] + " " + parts[4], parts[5] + " " + parts[6], type);
                }
            }
        } else {
            if (overwrite || !table.hasObf(parts[1], type)) {
                table.addTypeSRG(parts[1], parts[2], parts[3], type);
            }
        }
    }

    protected void parseStringArrayNormal(int line, String str, String[] parts, TargetType type, OBFTable table, boolean overwrite) throws FormatException, IOException {
        if (type == TargetType.METHOD) {
            if (parts.length < 7) {
                throw new FormatException("Format error on line " + line + ": \"" + str + "\"");
            }
            if (overwrite || !table.hasObf(parts[1], type)) {
                table.addType(parts[1] + " " + parts[2], parts[5] + " " + parts[6], type);
            }
        } else {
            if (overwrite || !table.hasObf(parts[1], type)) {
                table.addType(parts[1], parts[3], type);
            }
        }
    }

    protected void writeTable(Writer out, OBFTable table) throws IOException {
        if (table instanceof OBFTableSRG) {
        	for (TargetType type : TargetType.values()) {
        		if (!table.supportsType(type)) continue;
                for (String obf : table.getAllObf(type)) {
            		writeEntry(out, obf, type, (OBFTableSRG) table);
                }
        	}
        } else {
            super.writeTable(out, table);
        }
    }
    
    protected void writeEntry(Writer out, String obf, TargetType type, OBFTable table) throws IOException {
    	out.write(type.name());
        out.write(":");
        String deobf = table.deobf(obf, type);
        if (type == TargetType.METHOD) {
            obf = separateDescriptor(obf);
            deobf = separateDescriptor(deobf);
        }
        out.write(obf);
        out.write(":");
        out.write(deobf);
        out.write("\n");
    }
    
    protected void writeEntry(Writer out, String obf, TargetType type, OBFTableSRG table) throws IOException {
        out.write(type.name());
        out.write(":");
        String srg = table.getSRGFromObf(obf, type);
        String deobf = table.deobf(obf, type);
        if (type == TargetType.METHOD) {
            obf = separateDescriptor(obf);
            srg = separateDescriptor(srg);
            deobf = separateDescriptor(deobf);
        }
        out.write(obf);
        out.write(":");
        out.write(srg);
        out.write(":");
        out.write(deobf);
        out.write("\n");
    }
    
    protected String separateDescriptor(String line) {
    	if (line.indexOf(' ') == -1) return line;
    	String[] parts = line.split(" ");
        return parts[0] + ":" + (parts.length > 1 ? packageToPath(parts[1]) : " ");
    }

    protected String packageToPath(String pkg) {
        return pkg == null ? null : pkg.replace('.', '/');
    }
}
