package enginuity.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.File;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import enginuity.Settings;

public class DOMSettingsUnmarshaller {
    
    public Settings unmarshallSettings (Node rootNode) {
        Settings settings = new Settings();        
        Node n;
        NodeList nodes = rootNode.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) { 
            n = nodes.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("window")) {  
                settings = unmarshallWindow(n, settings);
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("options")) {  
                settings = unmarshallOptions(n, settings);
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("files")) {  
                settings = unmarshallFiles(n, settings);
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("tabledisplay")) {  
                settings = unmarshallTableDisplay(n, settings);
                
            }
        }        
        return settings;
    }
    

    public Settings unmarshallWindow (Node windowNode, Settings settings) {         
        Node n;
        NodeList nodes = windowNode.getChildNodes();
            
        for (int i = 0; i < nodes.getLength(); i++) { 
            n = nodes.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("size")) {  
                settings.setWindowSize(new Dimension(unmarshallAttribute(n, "y", 600), 
                                                     unmarshallAttribute(n, "x", 800)));
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("location")) {  
                // set default location in top left screen if no settings file found
                settings.setWindowLocation(new Point(unmarshallAttribute(n, "x", 0), 
                                                     unmarshallAttribute(n, "y", 0)));                
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("splitpane")) {  
                settings.setSplitPaneLocation(unmarshallAttribute(n, "location", 150));
                
            } 
        }
        return settings;
    }     
        
    public Settings unmarshallFiles (Node urlNode, Settings settings) {   
        Node n;
        NodeList nodes = urlNode.getChildNodes();
            
        for (int i = 0; i < nodes.getLength(); i++) { 
            n = nodes.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("ecudefinitionfile")) {  
                settings.addEcuDefinitionFile(new File(unmarshallAttribute(n, "name", "ecu_defs.xml")));
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("image_dir")) {  
                settings.setLastImageDir(new File(unmarshallAttribute(n, "path", "ecu_defs.xml")));
                
            } 
        }
        return settings;
    }
        
    public Settings unmarshallOptions (Node optionNode, Settings settings) {
        Node n;
        NodeList nodes = optionNode.getChildNodes();
            
        for (int i = 0; i < nodes.getLength(); i++) { 
            n = nodes.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("obsoletewarning")) {  
                settings.setObsoleteWarning(Boolean.parseBoolean(unmarshallAttribute(n, "value", "true")));
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("debug")) {  
                settings.setDebug(Boolean.parseBoolean(unmarshallAttribute(n, "value", "true")));
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("calcconflictwarning")) {  
                settings.setCalcConflictWarning(Boolean.parseBoolean(unmarshallAttribute(n, "value", "true")));
                
            }  else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("userlevel")) {  
                settings.setUserLevel(unmarshallAttribute(n, "value", 1));
                
            }  else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("tableclickcount")) {  
                settings.setTableClickCount(unmarshallAttribute(n, "value", 2));
                
            }   else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("version")) {  
                settings.setRecentVersion(unmarshallAttribute(n, "value", ""));
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("savedebugtables")) {  
                settings.setSaveDebugTables(Boolean.parseBoolean(unmarshallAttribute(n, "value", "false")));
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("displayhightables")) {  
                settings.setDisplayHighTables(Boolean.parseBoolean(unmarshallAttribute(n, "value", "false")));
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("valuelimitwarning")) {  
                settings.setValueLimitWarning(Boolean.parseBoolean(unmarshallAttribute(n, "value", "true")));
                
            } 
        }        
        return settings;
    }
        
    public Settings unmarshallTableDisplay (Node tableDisplayNode, Settings settings) {        
        Node n;
        NodeList nodes = tableDisplayNode.getChildNodes();
            
        for (int i = 0; i < nodes.getLength(); i++) { 
            n = nodes.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("font")) {  
                settings.setTableFont(new Font(unmarshallAttribute(n, "face", "Arial"),
                                               unmarshallAttribute(n, "decoration", Font.BOLD),
                                               unmarshallAttribute(n, "size", 12)));
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("cellsize")) {  
                settings.setCellSize(new Dimension(unmarshallAttribute(n, "x", 42),
                                                   unmarshallAttribute(n, "y", 18)));
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("colors")) {  
                settings = unmarshallColors(n, settings);  
                
            }
        }        
        return settings;
    }
    
    public Settings unmarshallColors(Node colorNode, Settings settings) {
        Node n;
        NodeList nodes = colorNode.getChildNodes();
            
        for (int i = 0; i < nodes.getLength(); i++) { 
            n = nodes.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("max")) {  
                settings.setMaxColor(unmarshallColor(n));            
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("min")) {  
                settings.setMinColor(unmarshallColor(n));    
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("highlight")) {  
                settings.setHighlightColor(unmarshallColor(n)); 
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("increaseborder")) {  
                settings.setIncreaseBorder(unmarshallColor(n));  
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("decreaseborder")) {  
                settings.setDecreaseBorder(unmarshallColor(n));   
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("axis")) {  
                settings.setAxisColor(unmarshallColor(n));   
                
            } else if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("warning")) {  
                settings.setWarningColor(unmarshallColor(n));   
                
            }
        }        
        return settings;        
    }
    
    public Color unmarshallColor(Node colorNode) {
        return new Color(unmarshallAttribute(colorNode, "r", 155),
                         unmarshallAttribute(colorNode, "g", 155),
                         unmarshallAttribute(colorNode, "b", 155));
    }
        
    private String unmarshallText(Node textNode) {
		StringBuffer buf = new StringBuffer();

		Node n;
		NodeList nodes = textNode.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			n = nodes.item(i);

			if (n.getNodeType() == Node.TEXT_NODE) {
				buf.append(n.getNodeValue());
			}
			else {
				// expected a text-only node (skip)
			}
		}
		return buf.toString();
	}    
    
    private String unmarshallAttribute(Node node, String name, String defaultValue) {
		Node n = node.getAttributes().getNamedItem(name);
		return (n != null) ? (n.getNodeValue()) : (defaultValue);
	}  
    
    private Double unmarshallAttribute(Node node, String name, double defaultValue) {
        return Double.parseDouble(unmarshallAttribute(node, name, defaultValue+""));
    }   
    
    private int unmarshallAttribute(Node node, String name, int defaultValue) {
        return Integer.parseInt(unmarshallAttribute(node, name, defaultValue+""));
    }   
    
    private boolean unmarshallAttribute(Node node, String name, boolean defaultValue) {
        return Boolean.parseBoolean(unmarshallAttribute(node, name, defaultValue+""));
    }    
 }