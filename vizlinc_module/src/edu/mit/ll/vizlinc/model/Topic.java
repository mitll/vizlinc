/*
 */
package edu.mit.ll.vizlinc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the topic facet.
 */
public class Topic extends Facet
{
    public Topic( int sortCriterion) 
    {
        super("Topic",  sortCriterion);
    }

    @Override
    protected List<? extends FacetValue> initFacetValueList() 
    {
        List<FacetValue> vals = new ArrayList<FacetValue>();
       // vals.add(new Entity("Topic 1", 10, 1));
        //vals.add(new Entity("Topic 2", 20,2));
        
        //TODO: sort
        
        //return Collections.unmodifiableList(vals);
        return vals;
    }
}