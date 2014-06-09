/*
 */
package edu.mit.ll.vizlinc.model;

import java.util.Comparator;

    
/**
 *
 * Compares {@link FacetValue} objects alphabetically
 */
class AlphabeticalFacetValComp implements Comparator<FacetValue>
{

    public AlphabeticalFacetValComp() {
    }

    @Override
    public int compare(FacetValue o1, FacetValue o2)
    {
        return o1.getText().compareTo(o2.getText());
    }

    
    
}
