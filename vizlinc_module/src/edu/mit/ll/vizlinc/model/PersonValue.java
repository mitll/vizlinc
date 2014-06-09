/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlincdb.entity.Entity;
import edu.mit.ll.vizlincdb.entity.PersonEntity;

/**
 * Represents a value for facet {@link Person}
 */
public class PersonValue extends FacetValue
{
    public PersonValue(Entity e)
    {
        super(e);
    }
    
    public PersonEntity getPersonEntity()
    {
        return (PersonEntity) this.entity;
    }
}
