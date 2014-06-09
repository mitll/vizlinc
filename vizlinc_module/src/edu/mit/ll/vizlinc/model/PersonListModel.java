/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlinc.utils.DBUtils;
import edu.mit.ll.vizlincdb.util.VizLincProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * List model for a person list.
 */
public class PersonListModel extends FacetListModel<PersonValue>
{
    private List<PersonValue> strongAcrossDocOnly;
    private boolean useStrongAcrossDocOnly;

    public PersonListModel(int sort, boolean showingWeakAcrossDoc)
    {
        super(DBUtils.getPersons(), sort);
        setFilteredList();
        useStrongAcrossDocOnly = !showingWeakAcrossDoc;
    }

    PersonListModel(List<PersonValue> personValues, int sort, boolean showingWeakAcrossDoc)
    {
        super(personValues, sort);
        setFilteredList();
        useStrongAcrossDocOnly = !showingWeakAcrossDoc;
    }

    private void setFilteredList()
    {
        strongAcrossDocOnly = new ArrayList();
        for (PersonValue pv : super.getList())
        {
            if (!pv.getPersonEntity().getCreatedBy().equals(VizLincProperties.P_CREATED_BY_WEAK_ACROSS_DOC))
            {
                strongAcrossDocOnly.add(pv);
            }
        }
    }

    public void showWeakAcrossDocPeople(boolean b)
    {
        this.useStrongAcrossDocOnly = !b;
        fireContentsChanged(this, 0, super.getSize());
    }

    @Override
    public int getSize()
    {
        if (useStrongAcrossDocOnly)
        {
            return strongAcrossDocOnly.size();
        } else
        {
            return super.getSize();
        }
    }

    @Override
    public PersonValue getElementAt(int index)
    {
        if (useStrongAcrossDocOnly)
        {
            return strongAcrossDocOnly.get(index);
        } else
        {
            return super.getElementAt(index);
        }
    }

    @Override
    public List<PersonValue> getList()
    {
        if (useStrongAcrossDocOnly)
        {
            return strongAcrossDocOnly;
        } else
        {
            return super.getList();
        }
    }

    @Override
    public void sort(int val)
    {
        //Do not fire the contentsChanged event yet. We want to do it after both 
        //lists are updated.
        super.sort(val,false); 
        setFilteredList();
        fireContentsChanged(this, 0, super.getSize());
    }

    @Override
    public int getIndexOfFacetValueWithId(int facetId)
    {
        if(!useStrongAcrossDocOnly)
        {
            return super.getIndexOfFacetValueWithId(facetId);
        }
        
        for(int i= 0; i< this.strongAcrossDocOnly.size();i++)
        {
            PersonValue v = strongAcrossDocOnly.get(i);
            if(v.getId() == facetId)
            {
                return i;
            }
        }
        return -1;
    }
}