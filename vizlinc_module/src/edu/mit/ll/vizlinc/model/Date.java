/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlinc.utils.DBUtils;
import java.util.List;

/**
 * Class representing the 'Date' facet
 */
public class Date extends Facet
{
    public Date(int sortCriterion) 
    {
        super("Date", sortCriterion);
    }

    Date(List<DateValue> dates, int sortCriterion) 
    {
        super("Date", dates, sortCriterion);
       // setParentOfAllValues(this);
    }

    @Override
    protected List<? extends FacetValue> initFacetValueList() 
    {  
        //TODO: Sort List
        //Collections.sort(vals, new AlphabeticalFacetValComp());
        //return Collections.unmodifiableList(vals);
        
        return DBUtils.getDates();
    }
}
