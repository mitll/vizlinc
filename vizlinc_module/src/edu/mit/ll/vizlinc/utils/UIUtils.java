/*
 */
package edu.mit.ll.vizlinc.utils;

import edu.mit.ll.vizlinc.model.Location;
import edu.mit.ll.vizlinc.model.LocationValue;
import edu.mit.ll.vizlinc.model.Organization;
import edu.mit.ll.vizlinc.model.OrganizationValue;
import edu.mit.ll.vizlinc.model.Person;
import edu.mit.ll.vizlinc.model.PersonValue;
import edu.mit.ll.vizlincdb.entity.PersonEntity;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.gephi.partition.api.Part;
import org.gephi.partition.api.Partition;
import org.gephi.partition.plugin.NodeColorTransformer;
import edu.mit.ll.vizlinc.components.FacetedSearchTopComponent;
import edu.mit.ll.vizlinc.components.GraphToolsTopComponent;
import edu.mit.ll.vizlinc.components.PropertiesTopComponent;
import edu.mit.ll.vizlinc.components.VLQueryTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 */
public class UIUtils
{

    private static Map<String, ImageIcon> iconRegistry = initIconRegistry();
    private static final String LOCATIONS_KEY = "LOCATIONS";
    private static final String ORGANIZATIONS_KEY = "ORGANIZATIONS";
    private static final String PEOPLE_KEY = "PEOPLE";
    private static final String DATE_KEY = "DATE";
    /**
     *
     * @param hexRGB hex rgb value in the format: #hhhhhh
     * @return
     */
    public static Color getAsColor(String hexRGB)
    {
        int r = Integer.valueOf(hexRGB.substring(1, 3), 16);
        int g = Integer.valueOf(hexRGB.substring(3, 5), 16);
        int b = Integer.valueOf(hexRGB.substring(5, 7), 16);
        return new Color(r, g, b);
    }
    
    
    /**
     * Adapted from org.gephi.utils.PaletteUtils.getSequenceColors(). Generate a set of visually disparate colors.
     * The PaletteUtils version generates random colors which change each time. This generates a fixed set of colors.
     * @param transformer
     * @param partition 
     */
    public static void setPartitionColors(NodeColorTransformer transformer, Partition partition) {
        int numParts = partition.getPartsCount();
        Map<Object, Color> transformerColorMap = transformer.getMap();
        
        float B = 0.7f;		// 0.6 <= B < 1 (criteria used by PaletteUtils.getSequenceColors().
        float S = 0.7f;		// 0.6 <= S < 1

        int i = 1;
        
        List<Part> parts = Arrays.asList(partition.getParts());
        // Shuffle the partitions before assigning colors, but use a constant random seed.
        Collections.shuffle(parts, new Random(0));
        for (Part p : parts) {
            float H = i / (float) numParts;
            transformerColorMap.put(p.getValue(), Color.getHSBColor(H, S, B));
            i++;
        }
    }
    
    
     /**
     * Note: Callers should make sure this method is called from the AWT
     * dispatch thread.
     *
     * @return
     */
    public static FacetedSearchTopComponent getFacetedSearchWindow()
    {
        return (FacetedSearchTopComponent) WindowManager.getDefault().findTopComponent("FacetedSearchTopComponent");
    }
    
     /**
     * Note: Callers should make sure this method is called from the AWT
     * dispatch thread.
     *
     * @return
     */
    public static PropertiesTopComponent getPropertiesWindow()
    {
        return (PropertiesTopComponent) WindowManager.getDefault().findTopComponent("PropertiesTopComponent");
    }

    /**
     * Should be called from the UI thread
     *
     * @param e
     */
    public static void reportException(Exception e)
    {
        reportException(null, e);
    }

    public static void reportException(Component parentComponent, Exception e)
    {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, e.getMessage() + "\n" + e.toString(), "Exception", JOptionPane.ERROR_MESSAGE);
    }

    public static Component getFirstParentComponent(Component component)
    {
        Component parent = component;
        Component parent2;
        while ((parent2 = parent.getParent()) != null)
        {
            parent = parent2;
        }

        return parent;
    }

    public static VLQueryTopComponent getQueryWindow()
    {
        return (VLQueryTopComponent) WindowManager.getDefault().findTopComponent("VLQueryTopComponent");
    }

    public static GraphToolsTopComponent getGraphToolsWindow()
    {
        return (GraphToolsTopComponent) WindowManager.getDefault().findTopComponent("GraphToolsTopComponent");
    }

    public static TopComponent getGraphWindow()
    {
        return WindowManager.getDefault().findTopComponent("GraphTopComponent");
    }
    
    public static String getIconPath(Object value)
    {
        String imgPath = null;
        if (value instanceof PersonValue)
        {
            PersonEntity entity = ((PersonValue) value).getPersonEntity();
            if (entity != null)
            {
                String createdBy = entity.getCreatedBy();

                if (createdBy.equals("weak_across_doc_person_coref"))
                {
                    imgPath = "icons/Person_3_16x16px.png";
                } else
                {
                    imgPath = "icons/Person_1_16x16px.png";
                }
            } else
            {
                imgPath = "icons/Person_1_16x16px.png";
            }
        } 
        else if (value instanceof LocationValue)
        {
            imgPath = "icons/Location_1_16x16px.png";
        } 
        else if (value instanceof OrganizationValue)
        {
            imgPath = "icons/org_value.png";
        }
        else if (value instanceof Person)
        {
            imgPath = "icons/Person_2_16x16px.png";
        } 
        else if (value instanceof Location)
        {
            imgPath = "icons/Location_2_16x16px.png";
        } 
        else if (value instanceof Organization)
        {
            imgPath = "icons/org.png";
        } 
        else
        {
            System.out.println("getIconPath() Class:  " + value.getClass());
        }

        return imgPath;
    }

    public static String getIconDir()
    {
        return "icons";
    }
    
    /**
     * Do a depth-first search for a Component with the given class name.
     * We use string class names because we may not have access to the class otherwise.
     * This is for unauthorized access into unexposed parts of Gephi.
     * @param top start with this Component
     * @param className full dotted name of the class
     * @return the matching Component
     */
    public static Component findFirstSubComponentWithClassName(Component top, String className)
    {
        if (top.getClass().getName().equals(className))
        {
            return top;
        }
        
        if (top instanceof Container) {
            for (Component component : ((Container) top).getComponents())
            {
                Component subcomponent = findFirstSubComponentWithClassName(component, className);
                if (subcomponent != null) return subcomponent;
            }
        }
        
        return null;
    }

    public static void setEnabledForAllLeafComponents(Container container, boolean enabled) {
        for (Component component : container.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof Container) {
                setEnabledForAllLeafComponents((Container) component, enabled);
            } else {
            }
        }
    }
    
    private static Map<String, ImageIcon> initIconRegistry()
    {
        Map<String, ImageIcon> map = new HashMap<String, ImageIcon>(4);
        map.put(LOCATIONS_KEY, new ImageIcon(UIUtils.class.getResource("/edu/mit/ll/vizlinc/ui/icons/location.png")));
        map.put(ORGANIZATIONS_KEY, new ImageIcon(UIUtils.class.getResource("/edu/mit/ll/vizlinc/ui/icons/org.png")));
        map.put(PEOPLE_KEY, new ImageIcon(UIUtils.class.getResource("/edu/mit/ll/vizlinc/ui/icons/person.png")));
        map.put(DATE_KEY, new ImageIcon(UIUtils.class.getResource("/edu/mit/ll/vizlinc/ui/icons/date.png")));
        return map;
    }

    
    public static ImageIcon getLocationsIcon()
    {
        return iconRegistry.get(LOCATIONS_KEY);
    }
    
    public static ImageIcon getPeopleIcon()
    {
        return iconRegistry.get(PEOPLE_KEY);
    }
    
    public static ImageIcon getOrganizationsIcon()
    {
        return iconRegistry.get(ORGANIZATIONS_KEY);
    }
    
    public static ImageIcon getDatesIcon()
    {
        return iconRegistry.get(DATE_KEY);
    }
}
