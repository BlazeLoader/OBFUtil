package net.acomputerdog.OBFUtil.parse.types;

import net.acomputerdog.OBFUtil.parse.FileParser;
import net.acomputerdog.OBFUtil.parse.FormatException;
import net.acomputerdog.OBFUtil.table.OBFTable;
import net.acomputerdog.OBFUtil.util.TargetType;
import net.acomputerdog.core.java.Patterns;

import java.io.*;

/**
 * Reads and writes obfuscation data to an MCP .srg file.
 */
public class SRGFileParser implements FileParser {

    private final String side;
    private final boolean stripDescs;

    /**
     * Creates a new SRGFileParser
     *
     * @param side                   The side to read.  Should be "C" or "S" (client/server).  Unsided entries are always read, but will be saved as as this.
     * @param stripMethodDescriptors If true, method descriptors will not be read.
     */
    public SRGFileParser(String side, boolean stripMethodDescriptors) {
        this.side = side;
        this.stripDescs = stripMethodDescriptors;
    }

    /**
     * Loads all entries located in a File into an OBFTable.
     *
     * @param file      The file to load from.  Must exist.
     * @param table     The table to write to.
     * @param overwrite If true overwrite existing mappings.
     */
    @Override
    public void loadEntries(File file, OBFTable table, boolean overwrite) throws IOException {
        if (file == null) throw new IllegalArgumentException("File must not be null!");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            parseFile(in, table, overwrite);
        } catch (IOException e) {
        	throw new IOException("Exception whilst reading file", e);
        } catch (IllegalArgumentException e) {
        	throw new IOException("Exception whilst reading file", e);
        } finally {
            if (in != null) in.close();
        }
    }
    
    private void parseFile(BufferedReader reader, OBFTable table, boolean overwrite) throws IOException {
    	int line = 0;
    	String str;
    	while ((str = reader.readLine()) != null) {
            line++;
            String[] sections = str.split(Patterns.SPACE);
            if (sections.length < 3) {
                throw new FormatException("Not enough sections on line " + line + ": \"" + str + "\"");
            }
            TargetType type = TargetType.getType(sections[0].replace(":", ""));
            if (type == null) {
                throw new FormatException("Illegal target type on line " + line + ": \"" + sections[0] + "\"");
            }
            String obf;
            String deobf;
            String side;
            if (type == TargetType.METHOD) {
                if (sections.length < 5) {
                    throw new FormatException("Not enough sections on line " + line + ": \"" + str + "\"");
                }
                if (stripDescs) {
                    obf = sections[1].replace('/', '.');
                    deobf = sections[3].replace('/', '.');
                } else {
                    obf = sections[1].replace('/', '.').concat(" ").concat(sections[2].replace('.', '/'));
                    deobf = sections[3].replace('/', '.').concat(" ").concat(sections[4].replace('.', '/'));
                }
                side = (sections.length >= 6) ? sections[5].replace("#", "") : "";
            } else {
                obf = sections[1].replace('/', '.');
                deobf = sections[2].replace('/', '.');
                side = (sections.length >= 4) ? sections[3].replace("#", "") : "";


            }
            if ((overwrite || !table.hasDeobf(obf, type)) && (side.isEmpty() || this.side.isEmpty() || side.equals(this.side))) {
                table.addType(obf, deobf, type);
            }
        }
    }

    /**
     * Saves all entries located in an OBFTable into a file.
     *
     * @param file  The file to write to.  Must exist.
     * @param table The table to read from
     * @throws java.io.IOException
     */
    @Override
    public void storeEntries(File file, OBFTable table) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }
        Writer out = null;
        try {
            out = new BufferedWriter(new FileWriter(file));
            for (TargetType type : TargetType.parsable()) {
                for (String obf : table.getAllObf(type)) {
                    String deobf = table.deobf(obf, type);
                    out.write(getPrefix(type));
                    out.write(": ");
                    out.write(type == TargetType.PACKAGE && obf.length() == 1 ? obf : slash(obf));
                    out.write(" ");
                    out.write(type == TargetType.PACKAGE && deobf.length() == 1 ? deobf : slash(deobf));
                    if (side.length() > 0) {
	                    out.write(" #");
	                    out.write(side);
                    }
                    out.write(Patterns.LINE_SEPARATOR);
                }
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

	private String slash(String s) {
		return s.replace('.', '/');
	}
	
    private String getPrefix(TargetType type) {
        switch (type) {
            case PACKAGE:
                return "PK";
            case CLASS:
                return "CL";
            case METHOD:
                return "MD";
            case FIELD:
                return "FD";
            default:
                throw new IllegalArgumentException("Invalid TargetType: " + type.name());
        }
    }
}
