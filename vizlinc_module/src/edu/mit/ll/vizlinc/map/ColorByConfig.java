/*
 */
package edu.mit.ll.vizlinc.map;

import edu.mit.ll.vizlinc.components.MapTopComponent;
import edu.mit.ll.vizlinc.components.MapTopComponent.ColorByCriterion;
import edu.mit.ll.vizlinc.components.MapTopComponent.ColorVariable;
import edu.mit.ll.vizlinc.components.MapTopComponent.ScaleType;

/**
 * Holds the user-selected configuration about how to color waypoints in the map.
 */
public class ColorByConfig
{
    private boolean active;
    private MapTopComponent.ScaleType scaleType;
    private MapTopComponent.ColorByCriterion colorByCriterion;
    private MapTopComponent.ColorVariable colorVariable;
    
    public ColorByConfig(boolean b, MapTopComponent.ScaleType s, MapTopComponent.ColorByCriterion colorBy, MapTopComponent.ColorVariable var)
    {
        active = b;
        scaleType = s;
        colorByCriterion = colorBy;
        colorVariable = var; 
    }

    public boolean isActive()
    {
        return active;
    }

    public ScaleType getScaleType()
    {
        return scaleType;
    }

    public ColorByCriterion getColorByCriterion()
    {
        return colorByCriterion;
    }

    public ColorVariable getColorVariable()
    {
        return colorVariable;
    }
}
