/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlinc.utils.DBUtils;
import java.util.List;

/**
 * Organization facet.
 */
public class Organization extends Facet
{
    public Organization( int sortCriterion) 
    {
        super("Organization", sortCriterion);
    }
    
    public Organization(List<OrganizationValue> vals, int sortCriterion)
    {
        super("Organization", vals, sortCriterion);
    }

    @Override
    protected List<? extends FacetValue> initFacetValueList() 
    {
        //TODO: Sort
        //Collections.sort(vals, new AlphabeticalFacetValComp());
        //return Collections.unmodifiableList(vals);
        return DBUtils.getOrganizations();
    }
}
