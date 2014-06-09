/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlinc.utils.DBUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Location facet.
 */
public class Location extends Facet
{
    public static final String LOCATION_FACET_NAME = "Location";
    
    public Location(int sortCriterion) 
    {
        super(LOCATION_FACET_NAME, sortCriterion);
    }
    
    public Location(List<LocationValue> vals, int sortCriterion)
    {
        super(LOCATION_FACET_NAME , vals, sortCriterion);
        //setParentOfAllValues(this);
    }

    @Override
    protected List<? extends FacetValue> initFacetValueList() 
    {
        //TODO: Sort list
       // Collections.sort(vals, new AlphabeticalFacetValComp());
        //return Collections.unmodifiableList(vals);
        return DBUtils.getLocations();
    }
    
    public List<LocationValue> getValuesAsLocationValues()
    {
        List<? extends FacetValue> fvs = getFacetValues();
        List<LocationValue> locations = new ArrayList<LocationValue>(fvs.size());
        for(int i = 0; i < fvs.size(); i++)
        {
            LocationValue l = (LocationValue) fvs.get(i);
            locations.add(l);
        }
        
        return locations;
    }
}