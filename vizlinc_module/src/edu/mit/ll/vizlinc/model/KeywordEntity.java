/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlincdb.entity.Entity;
import edu.mit.ll.vizlincdb.document.FoldingSpanishAnalyzer;
import edu.mit.ll.vizlincdb.util.VizLincProperties;
import java.util.List;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * Represents the keyword entity (facet).
 */
public class KeywordEntity extends Entity
{
    public static final int FIELD_DOC_NAME = 0;
    public static final int FIELD_DOC_TEXT = 1;

    public static Query joinKeywordsInOneQuery(List<KeywordEntity> keywords)
    {
        BooleanQuery combinedQ = new BooleanQuery();
        for(KeywordEntity k: keywords)
        {
            combinedQ.add(k.getQuery(), BooleanClause.Occur.MUST);
        }
        
        return combinedQ;
    }
    
    private int field;
    private Query query;
    
    public KeywordEntity(String text, int numDocuments, int numMentions,
            String createdBy, int id, int field) throws ParseException
    {
        super(text, numDocuments, numMentions, createdBy, id);
        this.field = field;
        
        QueryParser qp = null;
        
        if(field == FIELD_DOC_TEXT)
        {
            qp = new QueryParser(VizLincProperties.VIZLINCDB_LUCENE_VERSION, VizLincProperties.P_DOCUMENT_TEXT, new FoldingSpanishAnalyzer(VizLincProperties.VIZLINCDB_LUCENE_VERSION));
        }
        else if(field == FIELD_DOC_NAME)
        {
            qp = new QueryParser(VizLincProperties.VIZLINCDB_LUCENE_VERSION, VizLincProperties.P_DOCUMENT_NAME, new KeywordAnalyzer());
        }
        else
        {
            throw new IllegalArgumentException("Unknown field value: " + field);
        }
        this.query = qp.parse(text);
    }

    
    
    @Override
    public String getType()
    {
        return "KEYWORD";
    }

    @Override
    public int hashCode()
    {
        return getText().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof KeywordEntity) || obj == null)
        {
            return false;
        }
        
        KeywordEntity k = (KeywordEntity) obj;
        return k.getText().equals(getText());
    }
    
    public int getField()
    {
        return this.field;
    }

    public Query getQuery()
    {
        return this.query;
    }
}
