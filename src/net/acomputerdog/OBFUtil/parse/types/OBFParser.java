package net.acomputerdog.OBFUtil.parse.types;

import net.acomputerdog.OBFUtil.parse.FileParser;
import net.acomputerdog.OBFUtil.parse.FormatException;
import net.acomputerdog.OBFUtil.parse.StreamParser;
import net.acomputerdog.OBFUtil.table.OBFTable;
import net.acomputerdog.OBFUtil.util.TargetType;
import net.acomputerdog.core.java.Patterns;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * Reads and write obfuscation mappings to a .obf file.
 * General format is:
 *   # Comment type 1
 *   // Comment type 2
 *   <TYPE>:<OBF>=<DEOBF>
 */
public class OBFParser implements FileParser, StreamParser {

    @Override
    public void loadEntries(File file, OBFTable table, boolean overwrite) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }
        parseStringList(FileUtils.readLines(file), table, overwrite);
    }

    @Override
    public void storeEntries(File file, OBFTable table) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }
        Writer out = null;
        try {
            out = new BufferedWriter(new FileWriter(file));
            writeTable(out, table);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Loads all entries located in a stream into an OBFTable.
     *
     * @param stream      The stream to load from.
     * @param table     The table to write to.
     * @param overwrite If true overwrite existing mappings.
     */
    @Override
    public void loadEntries(InputStream stream, OBFTable table, boolean overwrite) throws IOException {
        if (stream == null) {
            throw new NullPointerException("Stream cannot be null!");
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(stream));
            List<String> data = new ArrayList<String>();
            String line;
            while ((line = in.readLine()) != null) {
                data.add(line);
            }
            parseStringList(data, table, overwrite);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Saves all entries located in an OBFTable into a stream.
     *
     * @param stream  The stream to write to.
     * @param table The table to read from
     * @throws IOException
     */
    @Override
    public void storeEntries(OutputStream stream, OBFTable table) throws IOException {
        if (stream == null) {
            throw new NullPointerException("Stream cannot be null!");
        }
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

    private boolean isCommentLine(String str) {
        String trimmed = str.trim();
        return (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//"));
    }

    private int parseStringList(List<String> lines, OBFTable table, boolean overwrite) throws FormatException {
        int line = 0;
        for (String str : lines) {
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
        return line;
    }

    private void writeTable(Writer out, OBFTable table) throws IOException {
        for (TargetType type : TargetType.values()) {
            for (String obf : table.getAllObf(type)) {
                String deobf = table.deobf(obf, type);
                out.write(type.name());
                out.write(":");
                out.write(obf);
                out.write("=");
                out.write(deobf);
                out.write("\n");
            }
        }
    }
}
