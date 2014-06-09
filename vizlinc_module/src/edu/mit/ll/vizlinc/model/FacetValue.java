/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlincdb.entity.Entity;
import java.text.DecimalFormat;

/**
 * Represents a possible value for a Facet
 */
public abstract class FacetValue
{
    private boolean showFullDataSetCounts;
    protected Entity entity;
    private int currentNumDocuments;
    private int currentNumMentions;
    
    protected FacetValue(Entity e)
    {
        showFullDataSetCounts = true;
        this.entity = e;
        currentNumDocuments = -1;
        currentNumMentions = -1;
    }
    
    public void setCurrentNumDocuments(int n)
    {
        currentNumDocuments = n;
    }
    
    public void setCurrentNumMentions(int n)
    {
        currentNumMentions = n;
    }

    public int getCurrentNumDocuments()
    {
        return currentNumDocuments;
    }

    public int getCurrentNumMentions()
    {
        return currentNumMentions;
    }
    
    public void setShowFullDataSetCounts(boolean b)
    {
        this.showFullDataSetCounts = b;
    }

    @Override
    public String toString()
    {
        int docCount;
        int mentionCount;
        
        if(showFullDataSetCounts)
        {
            docCount = entity.getNumDocuments();
            mentionCount = entity.getNumMentions();
        }
        else
        {
            docCount = currentNumDocuments;
            mentionCount = currentNumMentions;
        }
        
        DecimalFormat format = new DecimalFormat("###,###");
        String fCount = format.format((long) mentionCount);
        String dCount = format.format((long) docCount);
        return entity.getText() + "(M:" + fCount + ", D:" + dCount + ")";
    }

    public Integer getId()
    {
        return this.entity.getId();
    }

    public String getText()
    {
        return this.entity.getText();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(! (obj instanceof FacetValue))
        {
            return false;
        }
        
        return this.entity.equals(((FacetValue)obj).entity);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.entity != null ? this.entity.hashCode() : 0);
        return hash;
    }

    public String getType()
    {
       return this.entity.getType();
    }

   public  int getFullDataSetNumMentions()
    {
        return this.entity.getNumMentions();
    }

    public int getNumMentionsShown()
    {
        if(showFullDataSetCounts)
        {
            return this.entity.getNumMentions();
        }
        return currentNumMentions;
    }

    public int getNumDocumentsShown()
    {
        if(showFullDataSetCounts)
        {
            return this.entity.getNumDocuments();
        }
        return currentNumDocuments;
    }
}
