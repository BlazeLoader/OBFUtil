package net.acomputerdog.OBFUtil.parse.types;

import net.acomputerdog.OBFUtil.parse.FileFormatException;
import net.acomputerdog.OBFUtil.parse.FileParser;
import net.acomputerdog.OBFUtil.table.OBFTable;
import net.acomputerdog.OBFUtil.util.TargetType;
import net.acomputerdog.core.file.TextFileReader;

import java.io.*;
import java.util.regex.Pattern;

/**
 * Reads and write obfuscation mappings to a .obf file.
 * General format is:
 *   # Comment type 1
 *   // Comment type 2
 *   <TYPE>:<OBF>=<DEOBF>
 */
public class OBFFileParser implements FileParser {
    @Override
    public void loadEntries(File file, OBFTable table, boolean overwrite) throws IOException {
        TextFileReader reader = null;
        try {
            reader = new TextFileReader(file);
            int line = 0;
            for (String str : reader.readAllLines()) {
                line++;
                if (isCommentLine(str)) {
                    continue;
                }
                String[] typeParts = str.split(Pattern.quote(":"));
                if (typeParts.length < 2) {
                    throw new FileFormatException("Format error on line " + line + ": \"" + str + "\"");
                }
                TargetType type = TargetType.valueOf(typeParts[0]);
                if (type == null) {
                    throw new FileFormatException("Illegal target type on line " + line + ": \"" + typeParts[0] + "\"");
                }
                String[] obfParts = typeParts[1].split(Pattern.quote("="));
                if (obfParts.length < 2) {
                    throw new FileFormatException("Format error on line " + line + ": \"" + str + "\"");
                }
                if (overwrite || !table.hasType(type, obfParts[0])) {
                    table.addType(type, obfParts[0], obfParts[1]);
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Override
    public void storeEntries(File file, OBFTable table) throws IOException {
        Writer out = null;
        try {
            out = new BufferedWriter(new FileWriter(file));
            for (TargetType type : TargetType.values()) {
                for (String obf : table.getAllType(type)) {
                    String deobf = table.getType(type, obf);
                    out.write(type.name());
                    out.write(":");
                    out.write(obf);
                    out.write("=");
                    out.write(deobf);
                    out.write("\n");
                }
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private boolean isCommentLine(String str) {
        String trimmed = str.trim();
        return (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//"));
    }
}
