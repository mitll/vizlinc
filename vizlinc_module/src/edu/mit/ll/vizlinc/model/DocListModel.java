/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlinc.utils.DBUtils;
import edu.mit.ll.vizlinc.utils.UIUtils;
import edu.mit.ll.vizlincdb.document.Document;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.apache.lucene.search.Query;
import edu.mit.ll.vizlinc.components.VLQueryTopComponent;

/**
 * ListModel for document list
 */
public class DocListModel implements ListModel
{

    private List<ListDataListener> listeners;
    private List<Document> docs;

    /**
     * Stores a pointer to the original lists. Changes to the GUI List affect
     * the original list and vice versa.
     */
    public DocListModel(List<Document> docs)
    {
        listeners = new LinkedList<ListDataListener>();
        this.docs = docs;
    }

    public void setList(List<Document> newDocs)
    {
        this.docs = newDocs;
        fireListContentChangedEvent(0, newDocs.size());
    }

    public List<Document> getList()
    {
        return this.docs;
    }

    @Override
    public int getSize()
    {
        return this.docs.size();
    }

    @Override
    public Object getElementAt(int index)
    {
        return this.docs.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l)
    {
        this.listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l)
    {
        this.listeners.remove(l);
    }

    private void fireListContentChangedEvent(int index0, int index1)
    {
        final int i0 = index0;
        final int i1 = index1;
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                for (ListDataListener l : listeners)
                {
                    l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, i0, i1));
                }
            }
        });
    }

    public void sortByAZ()
    {
        Comparator<Document> comparator = new Comparator<Document>()
        {
            @Override
            public int compare(Document o1, Document o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        };
        Collections.sort(docs, comparator);
        fireListContentChangedEvent(0, this.docs.size());
    }

    public void sortByHits() throws SQLException, IOException
    {
        final Map<String, Integer> docToMentionsMap = createMentionCountMap();

        Collections.sort(docs, new Comparator<Document>()
        {
            @Override
            public int compare(Document o1, Document o2)
            {
                Integer m1 = docToMentionsMap.get(o1.getPath());
                Integer m2 = docToMentionsMap.get(o2.getPath());

                if (m1 == null || m2 == null)
                {
                    JOptionPane.showMessageDialog(null, o1.getName() + ":" + m1 + " " + o2.getName() + "->" + m2);
                }
                return m2.compareTo(m1);
            }
        });

        fireListContentChangedEvent(0, this.docs.size());

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
            throw new RuntimeException("Illegal state: can't compute frequency counts");
        }
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
        Query q = null;
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
}
