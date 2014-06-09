/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlinc.utils.DBUtils;
import edu.mit.ll.vizlinc.utils.UIUtils;
import edu.mit.ll.vizlincdb.document.Document;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.apache.lucene.search.Query;
import edu.mit.ll.vizlinc.components.VLQueryTopComponent;

/**
 * Table model that serves the 'working document set' table.
 */
public class ResultDocSetTableModel extends AbstractTableModel
{

    private List<Document> docs;
    private Map<String, Integer> docPathToTotalMentionCount;
    private static final String[] COLUMN_NAMES = new String[]
    {
        "Name", "Total Mentions"
    };

    public ResultDocSetTableModel(List<Document> documents)
    {
        this.docs = documents;
        this.docPathToTotalMentionCount = null;
    }

    @Override
    public int getRowCount()
    {
        return docs.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        
        if(columnIndex == 1)
        {
            return Integer.class;
        }
        else
        {
            return super.getColumnClass(columnIndex);
        }
    }
    
    @Override
    public int getColumnCount()
    {
        return 2;
    }

    @Override
    public String getColumnName(int column)
    {
        return COLUMN_NAMES[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (columnIndex == 0)
        {
            return docs.get(rowIndex);
        } else if (columnIndex == 1)
        {
            //return 0;
            if(docPathToTotalMentionCount == null)
            {
                return 0;
            }
            else
            {
                Document d = docs.get(rowIndex);
                return docPathToTotalMentionCount.get(d.getPath());
            }
        } else
        {
            return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }

    public void setList(List<Document> documents) throws SQLException, IOException
    {
        this.docs = documents;
        this.docPathToTotalMentionCount = createMentionCountMap();
        fireTableDataChanged();
    }

    public void sortByAZ()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sortByHits()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Document getDocumentByRow(int selectedRow)
    {
        return this.docs.get(selectedRow) ;
    }
    
    private Map<String, Integer> createMentionCountMap() throws SQLException, IOException
    {
        VLQueryTopComponent queryWin = UIUtils.getQueryWindow();
        Map<String, Integer> nonKeywordMentionCounts = null;
        Map<String, Integer> keywordMentionCounts = null;
        
        List<FacetValue> nonKeyword = queryWin.getNonKeywordFilters();
        if(!nonKeyword.isEmpty())
        {
            nonKeywordMentionCounts = getNonKeywordMentionCounts(nonKeyword);
        }
        
        List<KeywordValue> keywordFilters = queryWin.getKeywordFilters();
        if(!keywordFilters.isEmpty())
        {
            keywordMentionCounts = getKeywordMentionCounts(keywordFilters);
        }
        
        if(nonKeywordMentionCounts != null && keywordMentionCounts != null)
        {
            return combineMentionCountMaps(nonKeywordMentionCounts, keywordMentionCounts);
        }
        else if( nonKeywordMentionCounts != null)
        {
            return nonKeywordMentionCounts;
        }
        else if( keywordMentionCounts != null)
        {
            return keywordMentionCounts;
        }
        else
        {
            return null;
        }
    }
    
    private Map<String, Integer> getNonKeywordMentionCounts(List<FacetValue> nonKeywordFilters) throws SQLException
    {      
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (Document d : this.docs)
        {
            int mentions = DBUtils.getTotalMentionsForEntities(d, nonKeywordFilters);
            result.put(d.getPath(), mentions);
        }
        return result;
    }

    private Map<String, Integer> getKeywordMentionCounts(List<KeywordValue> keywordFilters) throws IOException
    {
        Query q;
        if(keywordFilters.size() == 1)
        {
            q = keywordFilters.get(0).getQuery();
        }
        else
        {
            q = KeywordValue.joinKeywordsInOneQuery(keywordFilters);
        }
        return DBUtils.getKeywordsMentionCountsForDocs(q, this.docs);
    }
    
    private Map<String,Integer> combineMentionCountMaps(Map<String,Integer> nonKeywordMentionCounts, Map<String,Integer> keywordMentionCounts)
    {
        for(String doc: nonKeywordMentionCounts.keySet())
        {
            int count1 = nonKeywordMentionCounts.get(doc);
            int count2 = keywordMentionCounts.get(doc);
            int sum = count1 + count2;
            keywordMentionCounts.put(doc, sum);
        }
        
        return keywordMentionCounts;
    }
}