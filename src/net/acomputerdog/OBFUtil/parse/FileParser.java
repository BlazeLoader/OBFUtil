package net.acomputerdog.OBFUtil.parse;

import net.acomputerdog.OBFUtil.table.OBFTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Reads and write obfuscation mappings to a file.
 */
public abstract class FileParser {

    /**
     * Loads all entries located in a File into an OBFTable.
     *
     * @param file      The file to load from.  Must exist.
     * @param table     The table to write to.
     * @param overwrite If true overwrite existing mappings.
     */
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
    
	/**
	 * Actually loads the file.
	 * 
	 * @param reader		BufferedReaderto provide file contents
     * @param table     	The table to write to.
     * @param overwrite 	If true overwrite existing mappings.
	 */
    protected abstract void parseFile(BufferedReader reader, OBFTable table, boolean overwrite) throws IOException;
    
    /**
     * Saves all entries located in an OBFTable into a file.
     *
     * @param file  The file to write to.  Must exist.
     * @param table The table to read from
     * @throws IOException
     */
    public abstract void storeEntries(File file, OBFTable table) throws IOException;
}
