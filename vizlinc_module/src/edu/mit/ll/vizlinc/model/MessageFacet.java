/*
 */
package edu.mit.ll.vizlinc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Facet with a configurable name.
 */
public class MessageFacet extends Facet
{
    public MessageFacet(String message)
    {
        super(message, FacetTreeModel.SORT_ALPHA);
    }

    @Override
    protected List<? extends FacetValue> initFacetValueList()
    {
        return new ArrayList<FacetValue>(0);
    }
}
