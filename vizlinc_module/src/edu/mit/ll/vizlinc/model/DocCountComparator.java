/*
 * 
 */
package edu.mit.ll.vizlinc.model;

import java.util.Comparator;

/**
 * Compares two {@link FacetValue} objects by document count.
 */
class DocCountComparator implements Comparator<FacetValue>
{
    @Override
    public int compare(FacetValue o1, FacetValue o2)
    {
        int dc1 = o1.getNumDocumentsShown();
        int dc2 = o2.getNumDocumentsShown();
        
        if( dc1 > dc2)
        {
            return -1;
        }
        else if (dc1 < dc2)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
}
