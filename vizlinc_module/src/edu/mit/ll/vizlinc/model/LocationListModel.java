/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlinc.utils.DBUtils;
import java.util.List;

/**
 * List model for location list. 
 */
public class LocationListModel extends FacetListModel<LocationValue> 
{
    public LocationListModel(int sort)
    {
        super(DBUtils.getLocations(), sort);
    }

    LocationListModel(List<LocationValue> locationValues, int sort)
    {
        super(locationValues, sort);
    }
}