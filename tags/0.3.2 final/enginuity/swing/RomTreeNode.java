package enginuity.swing;

import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import enginuity.maps.Rom;
import enginuity.maps.Table;
import java.util.Enumeration;

public class RomTreeNode extends DefaultMutableTreeNode {
    
    private Rom rom = new Rom();
    
    public RomTreeNode(Rom rom, int userLevel, boolean isDisplayHighTables) {
        setRom(rom);
        refresh(userLevel, isDisplayHighTables);
        updateFileName();
    }
        
    public void refresh(int userLevel, boolean isDisplayHighTables) {
        
        removeAllChildren();
        Vector<Table> tables = rom.getTables();
        
        for (int i = 0; i < tables.size(); i++) {
            Table table = tables.get(i);
            add(table);
            
            if (isDisplayHighTables || userLevel >= table.getUserLevel()) {
                    
                boolean categoryExists = false;

                for (int j = 0; j < getChildCount(); j++) {
                    if (getChildAt(j).toString().equals(table.getCategory())) {

                        // add to appropriate category
                        TableTreeNode tableNode = new TableTreeNode(table);
                        getChildAt(j).add(tableNode);
                        categoryExists = true;
                        break;
                    }
                }         

                if (!categoryExists) { // if category does not already exist, create it
                    add(new CategoryTreeNode(table.getCategory(), table.getRom()));            
                    TableTreeNode tableNode = new TableTreeNode(table);

                    getLastChild().add(tableNode);           
                }  
            }
        }
    }
    
    public void removeAllChildren() {
        
        // close all table windows
        // loop through categories first
        for (int i = 0; i < getChildCount(); i++) { 
            DefaultMutableTreeNode category = getChildAt(i);
            
            // loop through tables in each category
            for (Enumeration j = category.children(); j.hasMoreElements();) {                 
                ((TableTreeNode)j.nextElement()).getFrame().dispose();

            }
        }
        
        // removeAllChildren
        super.removeAllChildren();
    }
    
    public void updateFileName() {
        setUserObject(rom);
    }
    
    public void add(Table table) {
        TableFrame frame = new TableFrame(table);
        table.setFrame(frame); 
    }
    
    public DefaultMutableTreeNode getChildAt(int i) {
        return (DefaultMutableTreeNode)super.getChildAt(i);
    }
    
    public DefaultMutableTreeNode getLastChild() {
        return (DefaultMutableTreeNode)super.getLastChild();
    }    

    public Rom getRom() {
        return rom;
    }
    
    public void setRom(Rom rom) {
        this.rom = rom;
    }
    
}