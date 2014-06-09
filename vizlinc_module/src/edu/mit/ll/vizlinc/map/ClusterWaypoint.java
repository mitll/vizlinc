/*
 */
package edu.mit.ll.vizlinc.map;

import edu.mit.ll.vizlinc.model.LocationValue;
import java.awt.Point;
import java.awt.Rectangle;
import org.jdesktop.swingx.mapviewer.Waypoint;

/**
 * Waypoint that holds cluster information
 */
public class ClusterWaypoint extends Waypoint
{
    private int clusterId;
    public final static int UNCLASSIFIED = -1;
    public final static int NOISE = 0;
    private Rectangle bounds;
    private boolean isSelected;
    private final LocationValue locationValue;
    private boolean highlighted;

    public ClusterWaypoint(double latitude, double longitude, LocationValue location) 
    {
        super(latitude, longitude);
        clusterId = UNCLASSIFIED;
        bounds = new Rectangle(0, 0, 0, 0);
        isSelected = false;
        this.locationValue = location;
        this.highlighted = false;
    }
    
    public void setBounds(int x, int y , int width, int height)
    {
        this.bounds = new Rectangle(x,y, width, height);
    }
    
    public void setClusterId(int id)
    {
        this.clusterId = id;
    }
    
    public int getClusterId()
    {
        return this.clusterId;
    }

    @Override
    public int hashCode() 
    {
        return this.getPosition().toString().hashCode();
    }

    //TODO: I'm eliminating duplicates which might be useful to depict. Deal with 
    //locations that belong to different mentions but map to the same lat/long.
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ClusterWaypoint))
        {
            return false;
        }
        ClusterWaypoint w2 = (ClusterWaypoint) obj;
        
        return w2.getPosition().equals(this.getPosition());
    }

    public boolean contains(Point pt)
    {
        return this.bounds.contains(pt);
    }

    public void setSelected(boolean b)
    {
        this.isSelected = b;
    }
    
    public boolean getSelected()
    {
        return this.isSelected; 
    }

    public Rectangle getBounds()
    {
        return this.bounds;
    }

    
    public LocationValue getLocationValue()
    {
        return this.locationValue;
    }

    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }
    
    public boolean isHighlighted()
    {
        return this.highlighted;
    }
}
