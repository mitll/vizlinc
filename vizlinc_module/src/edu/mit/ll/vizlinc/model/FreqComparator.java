/*
 */
package edu.mit.ll.vizlinc.model;

import java.util.Comparator;

/**
 * Compares facet values by mention frequency
 */
class FreqComparator implements Comparator<FacetValue> {

    public FreqComparator() {
    }

    @Override
    public int compare(FacetValue o1, FacetValue o2) {
        int f1 = o1.getNumMentionsShown();
        int f2 = o2.getNumMentionsShown();
        
        if(f1 > f2)
        {
            return -1;
        }
        else if(f1 == f2)
        {
            return 0;
        }
        else 
        {
            return 1;
        }  
        
    }
    
}
