package edu.mit.ll.vizlinc.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 *  Abstract class for facet lists.
 */
public abstract class FacetListModel <T extends FacetValue> extends AbstractListModel<T>
{
    private List<T> values;
    
    public FacetListModel(List<T> typedValues, int sort)
    {
        values = typedValues;
        sort(sort, false);
    }
    
    @Override
    public int getSize()
    {
        return values.size();
    }
    
    @Override
    public T getElementAt(int index)
    {
        return values.get(index);
    }
    
    public List<T> getList()
    {
        return this.values;
    }
    
    public void sort(int val)
    {
        sort(val, true);
    }
    
    protected void sort(int val, boolean fireContentsChangedEvent)
    {
        Comparator comparator = null;
        if(val == FacetTreeModel.SORT_ALPHA)
        {
            comparator = new AlphabeticalFacetValComp();
        }
        else if(val == FacetTreeModel.SORT_MENTIONS)
        {
            comparator = new FreqComparator();
        }
        else if(val == FacetTreeModel.SORT_DOC)
        {
            comparator = new DocCountComparator();
        }
        else
        {
            throw new IllegalArgumentException("Unknown sorting criterion: " + val);
        }
        Collections.sort(values, comparator);
        if(fireContentsChangedEvent)
        {
            fireContentsChanged(this, 0, values.size());
        }
    }
    
    public int getIndexOfFacetValueWithId(int facetId)
    {
        for(int i= 0; i< this.values.size();i++)
        {
            T v = values.get(i);
            if(v.getId() == facetId)
            {
                return i;
            }
        }
        return -1;
    }
} 