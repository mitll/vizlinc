/*
 * 
*/
package edu.mit.ll.vizlinc.db;

import com.tinkerpop.blueprints.Vertex;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 */
public class Entity {

    private List<Vertex> mentions;
    private String name;
    
    public Entity(String name, Vertex v) 
    {
        this(name);
        mentions.add(v);
    }
    
    public Entity(String name)
    {
     mentions = new LinkedList<Vertex>();   
     this.name = name;
    }

   public void addMention(Vertex v) 
    {
        this.mentions.add(v);
    }

    public String getCanonicalName() {
        return this.name;
    }

    public Iterable<Vertex> getMentionNodes() {
        return this.mentions;
    }
    
    public int getNumMentions()
    {
        return this.mentions.size();
    }

    @Override
    public boolean equals(Object obj) 
    {
        if(!(obj instanceof Entity))
        {
            return false;
        }
        Entity e2 = (Entity) obj;
        return this.name.equals(e2.getCanonicalName());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    
    
    
    
    
    
    
    
}
