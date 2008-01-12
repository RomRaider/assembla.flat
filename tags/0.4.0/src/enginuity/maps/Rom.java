package enginuity.maps;

import enginuity.ECUEditor;
import enginuity.swing.JProgressPane;
import enginuity.xml.TableNotFoundException;

import javax.swing.*;
import java.io.File;
import java.io.Serializable;
import java.util.Vector;

public class Rom implements Serializable {

    private RomID romID = new RomID();
    private String fileName = "";
    private File fullFileName = new File(".");
    private Vector<Table> tables = new Vector<Table>();
    private ECUEditor container;
    private byte[] binData;

    public Rom() {
    }

    public void addTable(Table table) {
        boolean found = false;
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).getName().equalsIgnoreCase(table.getName())) {
                tables.remove(i);
                tables.add(i, table);
                found = true;
                break;
            }
        }
        if (!found) tables.add(table);
    }

    public Table getTable(String tableName) throws TableNotFoundException {
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).getName().equalsIgnoreCase(tableName)) {
                return tables.get(i);
            }
        }
        throw new TableNotFoundException();
    }

    public void removeTable(String tableName) {
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).getName().equalsIgnoreCase(tableName)) {
                tables.remove(i);
            }
        }
    }

    public void populateTables(byte[] binData, JProgressPane progress) {
        this.binData = binData;
        for (int i = 0; i < getTables().size(); i++) {

            // update progress
            int currProgress = (int) ((double) i / (double) getTables().size() * 40);
            progress.update("Populating tables...", 40 + currProgress);

            try {
                // if storageaddress has not been set (or is set to 0) omit table
                if (tables.get(i).getStorageAddress() != 0) {
                    try {
                        tables.get(i).populateTable(binData);

                    } catch (ArrayIndexOutOfBoundsException ex) {

                        System.out.println(tables.get(i).getName() +
                                " type " + tables.get(i).getType() + " start " +
                                tables.get(i).getStorageAddress() + " " + binData.length + " filesize");

                        // table storage address extends beyond end of file
                        JOptionPane.showMessageDialog(container, "Storage address for table \"" + tables.get(i).getName() +
                                "\" is out of bounds.\nPlease check ECU definition file.", "ECU Definition Error", JOptionPane.ERROR_MESSAGE);
                        tables.removeElementAt(i);
                        i--;

                    }

                } else {
                    tables.remove(i);
                    // decrement i because length of vector has changed
                    i--;
                }

            } catch (NullPointerException ex) {
                JOptionPane.showMessageDialog(container, "There was an error loading table " + tables.get(i).getName(), "ECU Definition Error", JOptionPane.ERROR_MESSAGE);
                tables.removeElementAt(i);
            }
        }
    }

    public void setRomID(RomID romID) {
        this.romID = romID;
    }

    public RomID getRomID() {
        return romID;
    }

    public String getRomIDString() {
        return romID.getXmlid();
    }

    public String toString() {
        String output = "";
        output = output + "\n---- Rom ----" + romID.toString();
        for (int i = 0; i < getTables().size(); i++) {
            output = output + getTables().get(i);
        }
        output = output + "\n---- End Rom ----";

        return output;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Vector<Table> getTables() {
        return tables;
    }

    public ECUEditor getContainer() {
        return container;
    }

    public void setContainer(ECUEditor container) {
        this.container = container;
        // apply settings to tables
        for (int i = 0; i < tables.size(); i++) {
            tables.get(i).applyColorSettings(container.getSettings());
            tables.get(i).resize();
        }
    }

    public byte[] saveFile() {
        for (int i = 0; i < tables.size(); i++) {
            tables.get(i).saveFile(binData);
        }
        return binData;
    }

    public int getRealFileSize() {
        return binData.length;
    }

    public File getFullFileName() {
        return fullFileName;
    }

    public void setFullFileName(File fullFileName) {
        this.fullFileName = fullFileName;
        this.setFileName(fullFileName.getName());
    }
}