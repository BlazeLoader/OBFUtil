package net.acomputerdog.OBFUtil.parse.types;

import net.acomputerdog.OBFUtil.map.TargetType;
import net.acomputerdog.OBFUtil.parse.FileParser;
import net.acomputerdog.OBFUtil.parse.FormatException;
import net.acomputerdog.OBFUtil.parse.StreamParser;
import net.acomputerdog.OBFUtil.table.OBFTable;

import java.io.*;

import com.blazeloader.util.regex.Patterns;

/**
 * Reads and write obfuscation mappings to a .obf file.
 * General format is:
 *   # Comment type 1
 *   // Comment type 2
 *   <TYPE>:<OBF>=<DEOBF>
 */
public class OBFParser extends FileParser implements StreamParser {
	
    @Override
    public void storeEntries(File file, OBFTable table) throws IOException {
    	storeEntries(new FileOutputStream(file), table);
    }
    
    @Override
    public void loadEntries(InputStream stream, OBFTable table, boolean overwrite) throws IOException {
        if (stream == null) throw new NullPointerException("Stream cannot be null!");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(stream));
            parseFile(in, table, overwrite);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    @Override
    public void storeEntries(OutputStream stream, OBFTable table) throws IOException {
        if (stream == null) throw new NullPointerException("Stream cannot be null!");
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(stream));
            writeTable(out, table);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
	@Override
	protected void parseFile(BufferedReader reader, OBFTable table, boolean overwrite) throws IOException {
		int line = 0;
    	String str;
    	while ((str = reader.readLine()) != null) {
            line++;
            if (isCommentLine(str)) {
                continue;
            }
            String[] typeParts = str.split(Patterns.COLON);
            if (typeParts.length < 2) {
                throw new FormatException("Format error on line " + line + ": \"" + str + "\"");
            }
            TargetType type = TargetType.valueOf(typeParts[0]);
            if (type == null) {
                throw new FormatException("Illegal target type on line " + line + ": \"" + typeParts[0] + "\"");
            }
            String[] obfParts = typeParts[1].split(Patterns.EQUALS);
            if (obfParts.length < 2) {
                throw new FormatException("Format error on line " + line + ": \"" + str + "\"");
            }
            if (overwrite || !table.hasDeobf(obfParts[0], type)) {
                table.addType(obfParts[0], obfParts[1], type);
            }
        }
    }

    protected void writeTable(Writer out, OBFTable table) throws IOException {
        for (TargetType type : TargetType.values()) {
        	if (!table.supportsType(type)) continue;
            for (String obf : table.getAllObf(type)) {
                writeEntry(out, obf, type, table);
            }
        }
    }
    
    protected void writeEntry(Writer out, String obf, TargetType type, OBFTable table) throws IOException {
    	String deobf = table.deobf(obf, type);
        out.write(type.name());
        out.write(":");
        out.write(obf);
        out.write("=");
        out.write(deobf);
        out.write("\n");
    }
    
    protected boolean isCommentLine(String str) {
        String trimmed = str.trim();
        return (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//"));
    }
}
