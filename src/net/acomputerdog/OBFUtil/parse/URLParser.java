package net.acomputerdog.OBFUtil.parse;

import java.io.IOException;
import java.net.URL;

import net.acomputerdog.OBFUtil.table.OBFTable;

/**
 * Reads obfuscation mappings from a url.
 */
public interface URLParser extends FileParser {

    /**
     * Loads all entries from a resource at a given url into an OBFTable.
     *
     * @param url    The URL to load from.
     * @param table     The table to write to.
     * @param overwrite If true overwrite existing mappings.
     */
    public void loadEntries(URL url, OBFTable table, boolean overwrite) throws IOException;
}
