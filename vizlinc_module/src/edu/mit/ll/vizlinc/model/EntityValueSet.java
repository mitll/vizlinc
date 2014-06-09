/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlincdb.entity.Entity;
import java.util.List;

/**
 * Container for lists of entities, one for each available class
 */
public class EntityValueSet 
{
    private List<Entity> dates;
    private List<Entity> orgs;
    private List<Entity> locs;
    private List<Entity> pers;
    
    public EntityValueSet(List<Entity> dates, List<Entity> orgs, List<Entity> locs, List<Entity> pers)
    {
        this.dates = dates;
        this.orgs = orgs;
        this.locs = locs;
        this.pers = pers;
    }

    public List<Entity> getDates() 
    {
        return dates;
    }

    public List<Entity> getOrgs() 
    {
        return orgs;
    }

    public List<Entity> getLocs() 
    {
        return locs;
    }

    public List<Entity> getPers() 
    {
        return pers;
    }
}
