package net.acomputerdog.OBFUtil.parse.types;

import net.acomputerdog.OBFUtil.parse.FileParser;
import net.acomputerdog.OBFUtil.table.DirectOBFTable;
import net.acomputerdog.OBFUtil.table.OBFTable;
import net.acomputerdog.OBFUtil.util.TargetType;

import java.io.File;
import java.io.IOException;

/**
 * Reads and writes obfuscation mappings to an MCP format .csv file.  MCP format is searge,name,side,desc as categories.
 */
public class MCPCSVFileParser extends CSVFileParser {
    private static final int OBFNAME_INDEX = 0;
    private static final int DEOBFNAME_INDEX = 1;
    private static final int SIDE_INDEX = 2;
    //private static final int DESC_INDEX = 3;

    private final TargetType type;
    private final int side;
    private final boolean ignoreSides;

    public MCPCSVFileParser(TargetType type) {
        this(type, true, -1); //-1 in case of bugs
    }

    public MCPCSVFileParser(TargetType type, int side) {
        this(type, false, side);
    }

    public MCPCSVFileParser(TargetType type, boolean ignoreSides, int side) {
        this.type = type;
        this.side = side;
        this.ignoreSides = ignoreSides;
    }

    /**
     * Writes the data in a CSVFile to an OBFTable
     *
     * @param source The file where the data originated from.
     * @param csv    The CSVFile to read from
     * @param table  The OBFTable to write to.
     */
    @Override
    public void writeCSVToTable(File source, CSVFile csv, OBFTable table) {
        for (int rowNum = 0; rowNum < csv.size(); rowNum++) {
            String[] row = csv.getRow(rowNum);
            if (ignoreSides || Integer.parseInt(row[SIDE_INDEX]) == side) {
                table.addType(row[OBFNAME_INDEX], row[DEOBFNAME_INDEX], type);
            }
        }
    }

    /**
     * Creates a CSVFile representing the data in an OBFTable
     *
     * @param source The file where the data originated from.
     * @param table  The table containing the data.
     * @return Return a CSVFile representing the data in the OBFTable
     */
    @Override
    public CSVFile readCSVFromTable(File source, OBFTable table) {
        CSVFile csv = new CSVFile();

        String[] obfs = table.getAllObf(type);
        for (String obf : obfs) {
            csv.addItem("searge", obf);
            csv.addItem("name", table.deobf(obf, type));
            csv.addItem("side", Integer.toString(ignoreSides ? 0 : side));
            csv.addItem("desc", "");
        }
        return csv;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("No file specified!  Use \"test <path_to_csv>\"");
            System.exit(0);
        }
        File csvFile = new File(args[0]);
        if (!csvFile.exists() || !csvFile.isFile()) {
            System.out.println("Path does not represent a file!  Use \"test <path_to_csv>\"");
            System.exit(0);
        }
        FileParser parser = new MCPCSVFileParser(getTypeOfFile(csvFile), false, 0);
        OBFTable table = new DirectOBFTable();
        try {
            parser.loadEntries(csvFile, table, true);
        } catch (IOException e) {
            System.out.println("Exception parsing CSV!");
            e.printStackTrace();
            System.exit(-1);
        }
        for (TargetType i : TargetType.parsable()) {
        	System.out.println(i.name() + ":");
        	for (String str : table.getAllObf(i)) {
                System.out.println("   " + str + " = " + table.deobf(str, i));
            }
        }
    }

    private static TargetType getTypeOfFile(File f) {
        if (f.getName().startsWith("fields")) return TargetType.FIELD;
        else if (f.getName().startsWith("methods")) return TargetType.METHOD;
        else throw new IllegalArgumentException("File is not a valid MCP CSV file!");
    }
}
