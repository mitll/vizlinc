/*
 */
package edu.mit.ll.vizlinc.model;

/**
 * Stores a document's name and id
 */
public class DocNameId
{
    private String name;
    private Integer id;

    public DocNameId(String name, Integer id)
    {
        this.name = name;
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public Integer getId()
    {
        return id;
    }
    
    
}
