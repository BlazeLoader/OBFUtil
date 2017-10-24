package net.acomputerdog.OBFUtil.parse.types;

import net.acomputerdog.OBFUtil.map.TargetType;
import net.acomputerdog.OBFUtil.parse.FormatException;
import net.acomputerdog.OBFUtil.table.OBFTable;

import java.io.*;

import com.blazeloader.util.regex.Patterns;

/**
 * Reads and write obfuscation data to an SOBF (Sided OBFuscation) file.  This format is an adaption of the OBF format to support sides defined in MCP files.
 * General format is:
 * # Comment type 1
 * // Comment type 2
 * <TYPE>.<SIDE>:<OBF>=<DEOBF>
 */
public class SOBFFileParser extends OBFParser {

    private final int side;

    /**
     * Creates a new SOBFFileParser.
     *
     * @param side The side to use.
     */
    public SOBFFileParser(int side) {
        this.side = side;
    }
    
    @Override
    protected void parseFile(BufferedReader reader, OBFTable table, boolean overwrite) throws IOException {
    	int line = 0;
    	String str;
    	while ((str = reader.readLine()) != null) {
            line++;
            if (isCommentLine(str)) continue;
            String[] typeParts = str.split(Patterns.COLON);
            if (typeParts.length < 2) {
                throw new FormatException("Format error on line " + line + ": \"" + str + "\"");
            }
            String[] sideParts = typeParts[0].split(Patterns.PERIOD);
            if (sideParts.length < 2) {
                throw new FormatException("Format error on line " + line + ": \"" + str + "\"");
            }
            TargetType type = TargetType.valueOf(sideParts[0]);
            if (type == null) {
                throw new FormatException("Illegal target type on line " + line + ": \"" + typeParts[0] + "\"");
            }
            String[] obfParts = typeParts[1].split(Patterns.EQUALS);
            if (obfParts.length < 2) {
                throw new FormatException("Format error on line " + line + ": \"" + str + "\"");
            }
            int side = Integer.parseInt(sideParts[1]);
            if ((overwrite || !table.hasDeobf(obfParts[0], type)) && (side == this.side)) {
                table.addType(obfParts[0], obfParts[1], type);
            }
        }
    }
    
    @Override
    protected void writeEntry(Writer out, String obf, TargetType type, OBFTable table) throws IOException {
    	String deobf = table.deobf(obf, type);
        out.write(type.name());
        out.write(".");
        out.write(side);
        out.write(":");
        out.write(obf);
        out.write("=");
        out.write(deobf);
        out.write("\n");
    }
}
