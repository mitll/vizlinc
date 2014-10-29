/*
 */
package edu.mit.ll.vizlinc.map;

import edu.mit.ll.vizlinc.utils.UIUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultWaypointRenderer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import edu.mit.ll.vizlinc.components.MapTopComponent;
import edu.mit.ll.vizlinc.components.PropertiesTopComponent;

/**
 * Renders waypoints as circles on the map
 */
public class CircleWayPointRenderer implements WaypointRenderer
{

    private int markerWidth = 10;
    private int markerHeight = markerWidth;
    private BufferedImage img = null;
    private Map<Integer, Color> colorMap;
    private PropertiesTopComponent topComponent;
    private Map<Integer, Integer> clusterCounters;
    private int totalPoints;
    private boolean clusteringOn;
    private static final Color DEFAULT_LOCATION_COLOR = new Color(100, 149, 237, 125);
    private ColorByConfig colorByConfig;
    private double maxNumMentions;
    private double maxNumDocs;

    public CircleWayPointRenderer(PropertiesTopComponent comp)
    {
        colorMap = new HashMap<Integer, Color>();
        this.topComponent = comp;
        clusteringOn = false;
        colorByConfig = new ColorByConfig(false, null, null, null);
        maxNumMentions = 0;
        maxNumDocs = 0;
 
        try
        {
            img = ImageIO.read(DefaultWaypointRenderer.class.getResource("resources/standard_waypoint.png"));
        } catch (Exception ex)
        {
            throw new RuntimeException("Couldn't read standard_waypoint.png", ex);
        }
    }
    
    public void setMarkerSize(int px)
    {
        this.markerHeight = px;
        this.markerWidth = px;
    }

    /**
     * {@inheritDoc}
     *
     * @param g
     * @param map
     * @param waypoint
     * @return
     */
    public boolean paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint waypoint)
    {
        if (this.clusteringOn)
        {
            paintByClusters(g, map, waypoint);
        } else
        {
            paintNormal(g, waypoint);
        }

        return false;
    }

    //TODO: Do I have to explicitely get rid of the Color objects I allocate?
    private Color getRandomColor()
    {
        Random random = new Random();

        //***************************************/
        //Bright pastel colors
      /*  final float hue = random.nextFloat();
         // Saturation between 0.1 and 0.3
         final float saturation = (random.nextInt(2000) + 1000) / 10000f;
         final float luminance = 0.9f;
         final Color color = Color.getHSBColor(hue, saturation, luminance);*/
        //**********************************************

        //**********************//
        // Truly random colors
        // Java 'Color' class takes 3 floats, from 0 to 1.
        float r = random.nextFloat();
        float g = random.nextFloat();
        float b = random.nextFloat();
//Then to finally create the colour, pass the primary colours into the constructor:

        final Color color = new Color(r, g, b, 0.3f);
        return color;
    }

    private Color getWaypointClusterColor(Waypoint waypoint)
    {
        //See if a color has been assigned to this waypoint's clutter
        if (waypoint instanceof ClusterWaypoint)
        {
            ClusterWaypoint myWaypoint = (ClusterWaypoint) waypoint;
            int clusterId = myWaypoint.getClusterId();



            Color clColor = colorMap.get(clusterId);
            if (clColor == null)
            {
                if (clusterId == ClusterWaypoint.NOISE)
                {
                    clColor = new Color(0f, 0f, 0f, 0.5f);
                } else
                {
                    //clColor = getRandomColor();
                    //Get color in color map according to count under this cluster
                    int count = this.clusterCounters.get(clusterId);
                    float fraction = ((float) count) / ((float) this.totalPoints);
                    clColor = new Color(1.0f, 1.0f - fraction, 0f, 0.4f);
                }
                colorMap.put(clusterId, clColor);
            }
            return clColor;
        } else
        {
            return getRandomColor();
        }
    }

    void setClusterCountersAndTotal(Map<Integer, Integer> clusterCounters, int totalPoints)
    {
        this.clusterCounters = clusterCounters;
        this.totalPoints = totalPoints;
    }

    void setClusteringOn(boolean clusterOn)
    {
        this.clusteringOn = clusterOn;
    }
    
    protected void setColorBy(ColorByConfig config)
    {
        this.colorByConfig = config;
    }

    private void paintByClusters(Graphics2D g, JXMapViewer map, Waypoint waypoint)
    {
        ClusterWaypoint cw = (ClusterWaypoint) waypoint;
        if (cw.getClusterId() == ClusterWaypoint.NOISE)
        {
            /*if(img != null) 
             {
             g.drawImage(img,-img.getWidth()/2,-img.getHeight(),null);
             }
             else 
             {*/
            if (cw.getSelected())
            {
                g.setColor(Color.WHITE);
                g.fillOval(-5, -5, this.markerWidth, this.markerHeight);
            } else
            {
                g.setColor(getWaypointClusterColor(waypoint));
                g.fillOval(-5, -5, this.markerWidth, this.markerHeight);
            }

            g.setStroke(new BasicStroke(1f));
            g.setColor(Color.BLACK);
            g.drawOval(-5, -5, this.markerWidth, this.markerHeight);
            //}
        } else
        {
            //println("Rendering NOT Noise");
            // g.setColor(Color.BLUE);
            g.setStroke(new BasicStroke(3f));
            g.setColor(getWaypointClusterColor(waypoint));
            //g.drawOval(-10,-10,20,20);
            g.fillOval(-5, -5, this.markerWidth, this.markerHeight);
            // g.setStroke(new BasicStroke(1f));
            // g.drawLine(-10,0,10,0);
            // g.drawLine(0,-10,0,10);
            //}
        }
    }

    private void paintNormal(Graphics2D g, Waypoint waypoint)
    {
        ClusterWaypoint cWaypoint = (ClusterWaypoint) waypoint;
        int halfWidthLeft = -(markerWidth / 2);
        if (cWaypoint.getSelected())
        {
            g.setColor(Color.WHITE);
            g.fillOval(halfWidthLeft, halfWidthLeft, markerWidth, markerHeight);
            g.setStroke(new BasicStroke(1f));
            g.setColor(Color.BLACK);
            g.drawOval(halfWidthLeft, halfWidthLeft, markerWidth, markerHeight);
        } 
        else
        {
            paintRegularCircleMarker(g, waypoint);
        }
        
        if(cWaypoint.isHighlighted())
        {
            if (img != null)
            {
                g.drawImage(img, -img.getWidth() / 2, -img.getHeight(), null);
            } 
            else
            {
                g.setColor(Color.BLACK);
                g.fillRect(halfWidthLeft, halfWidthLeft, markerWidth, markerHeight);
                g.setStroke(new BasicStroke(1f));
                g.setColor(Color.BLACK);
                g.drawRect(halfWidthLeft, halfWidthLeft, markerWidth, markerHeight);
            }
        }
    }
    
    private void paintRegularCircleMarker(Graphics2D g, Waypoint waypoint)
    {
        int halfWidthLeft = -(markerWidth / 2);
        g.setStroke(new BasicStroke(3f));
            if (this.colorByConfig.isActive())
            {
                g.setColor(getColorByMentions((ClusterWaypoint)waypoint));
            } 
            else
            {
                g.setColor(DEFAULT_LOCATION_COLOR);
            }
            g.fillOval(halfWidthLeft, halfWidthLeft, this.markerWidth, this.markerHeight);
    }

    protected void setMaxNumMentions(long total)
    {
        this.maxNumMentions = total;
    }
    
    
    private Color getColorByMentions(ClusterWaypoint waypoint)
    {   
        MapTopComponent.ScaleType scale = this.colorByConfig.getScaleType();
        MapTopComponent.ColorVariable var = this.colorByConfig.getColorVariable();
        MapTopComponent.ColorByCriterion criterion = this.colorByConfig.getColorByCriterion();
        
        double count;
        double maxCount;
        
        switch(criterion)
        {
            case MENTION:
                count = (double)waypoint.getLocationValue().getNumMentionsShown();
                maxCount = this.maxNumMentions;
                break;
            case DOCUMENT:
                count = (double) waypoint.getLocationValue().getNumDocumentsShown();
                maxCount = this.maxNumDocs;
                break;
            default:
                //Make sure exception is reported to the user:
                IllegalArgumentException e = new IllegalArgumentException("Invalid value for map scale type: " + scale); 
                UIUtils.reportException(e);
                throw e;
        }
        
        double fraction;
        switch (scale)
        {
            case LOG:
                //fraction = Math.log(count + 1) / (Math.log(maxCount + 1));
                fraction = Math.sqrt(Math.log(count)) / Math.sqrt(Math.log(maxCount));
                break;
            case LINEAR:
                fraction = count / (maxCount);
                break;
            default:
                //Make sure exception is reported to the user:
                IllegalArgumentException e = new IllegalArgumentException("Invalid value for map scale type: " + scale); 
                UIUtils.reportException(e);
                throw e;
        }
        

        switch(var)
        {
            case ALPHA:
                return new Color(1.0f, 0.0f, 0f, (float)fraction);
            case COLOR:
               // return new Color(1.0f, 1.0f - (float)fraction, 0f, 1.0f);
                return getHotColdColor((float) fraction);
            default:
                IllegalArgumentException e = new IllegalArgumentException("Invalid value for map color variable type: " + var); 
                UIUtils.reportException(e);
                throw e;
        }
        
    }

    private Color getHotColdColor(float val)
    {
        float r = 1.0f;
        float g= 1.0f;
        float b= 1.0f;
        
        if(val < 0.25)
        {
            r = 0;
            g = 4*val;
        }
        else if(val < 0.5)
        {
            r = 0;
            b = 1f + 4f*(0.25f - val); 
        }
        else if (val < 0.75)
        {
            r = 4f * (val - 0.5f);
            b = 0;
        }
        else
        {
            g = 1.0f + 4.0f * (0.75f - val);
            b = 0;
        }
        
        return new Color(r,g,b,0.6f);
    }

    void setMaxNumDocs(int maxDocs)
    {
        this.maxNumDocs = maxDocs;
    }

    ColorByConfig getColorByConfig()
    {
        return this.colorByConfig;
    }

    public int getMarkerWidth()
    {
        return this.markerWidth; 
    }
    
    public int getMarkerHeight()
    {
        return this.markerHeight;
    }
}
