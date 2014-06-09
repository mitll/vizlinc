/*
 */
package edu.mit.ll.vizlinc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the parameters that makes the text 'neighborhood' of a keyword or mention.
 */
public class NeighborhoodConfig
{
  private int distance;
  private List<Integer> nonKeywordFilterIds;
  
  public NeighborhoodConfig(List<FacetValue> nonKeywordFilters, int distance)
  {
      this.nonKeywordFilterIds = new ArrayList<Integer>(nonKeywordFilters.size());
      for(FacetValue fv : nonKeywordFilters)
      {
          this.nonKeywordFilterIds.add(fv.getId());
      }
      
      this.distance = distance;
  }

    public int getDistance()
    {
        return distance;
    }

    public List<Integer> getNonKeywordFilterIds()
    {
        return this.nonKeywordFilterIds;
    }
}
