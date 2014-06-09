/*
 */
package edu.mit.ll.vizlinc.model;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a facet: a category that can be used in a query to filter out irrelevant documents.
 * 
 * Make sure to call the (String) constructor from sub classes with the
 * appropriate name.
 * 
 */
public abstract class Facet 
{
    private String facetName;
    private List<? extends FacetValue> facetVals;
    private int order;
    
    protected Facet(String name, int sortCriterion) 
    {
       this.facetVals = initFacetValueList();   
        initClassMembers(name, sortCriterion);
    }

    protected Facet(String name, List<? extends FacetValue> values, int sortCriterion)
    {
        facetVals = values;
        initClassMembers(name,sortCriterion);
        
    }
    private void initClassMembers(String name, int sortCriterion)
    {
        this.facetName = name;
        sortValues(sortCriterion);
    }
    
    public String getName() 
    {
        return this.facetName;
    }
    
    protected abstract List<? extends FacetValue> initFacetValueList();
    
    public List<? extends FacetValue> getFacetValues()
    {
        return this.facetVals;
    }

    /**
     * Return the FacetValue whose node id is equal to Id. Returns null if none found.
     * This does a linear search through the FacetValues. If more efficiency is needed, the FacetValues should be indexed.
     * @param id
     * @return 
     */
    public FacetValue findFacetValueWithId(int id)
    {
        for (FacetValue facetValue : facetVals)
        {
            if (facetValue.getId() == id) return facetValue;
        }
        return null;
    }
    
    
    @Override
    public String toString() {
        long count = (long) getFacetValues().size();
        DecimalFormat format = new DecimalFormat("###,###");
        String fCount = format.format(count);
        
        return this.facetName + " (" + fCount+")";
    }

    @Override
    public int hashCode() 
    {
        return facetName.hashCode();
    }

    @Override
    public boolean equals(Object obj) 
    {
        if(!(obj instanceof Facet))
        {
            return false;
        }
        Facet f2 = (Facet) obj;
        return this.facetName.equals(f2.getName());
    }

    protected void sortValues(int sort) 
    {
        this.order = sort;
        sortValues();
    }
    
    private void sortValues()
    {
        Comparator comparator = null;
        if(this.order == FacetTreeModel.SORT_ALPHA)
        {
            comparator = new AlphabeticalFacetValComp();
        }
        else if(this.order == FacetTreeModel.SORT_MENTIONS)
        {
            comparator = new FreqComparator();
        }
        else if(this.order == FacetTreeModel.SORT_DOC)
        {
            comparator = new DocCountComparator();
        }
        else
        {
            throw new IllegalArgumentException("Unknown sorting criterion: " + this.order);
        }
        
        Collections.sort(facetVals, comparator);
    }
}
