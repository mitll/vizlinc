/*
 */
package edu.mit.ll.vizlinc.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.search.Query;

/**
 * A value of the KeywordEntity. Used to represent keywords used in string-based search.
 */
public class KeywordValue extends FacetValue
{
    public static Query joinKeywordsInOneQuery(List<KeywordValue> keywords)
    {
        List<KeywordEntity> entities = new ArrayList<KeywordEntity>(keywords.size());
        for(KeywordValue kv : keywords)
        {
            entities.add((KeywordEntity)kv.entity);
        }
       
        return KeywordEntity.joinKeywordsInOneQuery(entities);
    }
    
    public KeywordValue(KeywordEntity e)
    {
        super(e);
    }

    public Query getQuery()
    {
        return ((KeywordEntity)entity).getQuery();
    }

    public int getField()
    {
        return ((KeywordEntity)this.entity).getField();
    }
}
