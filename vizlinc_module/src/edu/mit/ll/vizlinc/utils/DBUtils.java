package edu.mit.ll.vizlinc.utils;

import com.google.common.io.FileBackedOutputStream;
import com.tinkerpop.blueprints.Vertex;
import edu.mit.ll.vizlinc.model.DBManager;
import edu.mit.ll.vizlinc.model.DateValue;
import edu.mit.ll.vizlinc.model.DocNameId;
import edu.mit.ll.vizlinc.model.FacetValue;
import edu.mit.ll.vizlinc.model.FacetValuesByCategory;
import edu.mit.ll.vizlinc.model.LocationValue;
import edu.mit.ll.vizlinc.model.NeighborhoodConfig;
import edu.mit.ll.vizlinc.model.OrganizationValue;
import edu.mit.ll.vizlinc.model.PersonValue;
import edu.mit.ll.vizlincdb.entity.DateEntity;
import edu.mit.ll.vizlincdb.document.Document;
import edu.mit.ll.vizlincdb.entity.Entity;
import edu.mit.ll.vizlincdb.entity.EntityCounts;
import edu.mit.ll.vizlincdb.entity.EntitySet;
import edu.mit.ll.vizlincdb.geo.GeoPoint;
import edu.mit.ll.vizlincdb.util.VizLincProperties;
import edu.mit.ll.vizlincdb.relational.VizLincRDBMem;
import edu.mit.ll.vizlincdb.document.VizLincSearcher;
import edu.mit.ll.vizlincdb.entity.LocationEntity;
import edu.mit.ll.vizlincdb.entity.Mention;
import edu.mit.ll.vizlincdb.entity.MentionLocation;
import edu.mit.ll.vizlincdb.entity.OrganizationEntity;
import edu.mit.ll.vizlincdb.entity.PersonEntity;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.openide.util.Exceptions;
import org.openide.windows.OutputWriter;

/**
 * Manages all calls to the database and index.
 */
public class DBUtils
{
    //private static final String DB_LOCATION = "E:\\VizLinc\\database\\leads-2012-12-04.neo4j";

    private static final String PERSON_ENTITY_TYPE = "PERSON";
    private static final String DATE_ENTITY_TYPE = "DATE";
    private static final String LOCATION_ENTITY_TYPE = "LOCATION";
    private static final String ORGANIZATION_ENTITY_TYPE = "ORGANIZATION";
    
    private static final int NUM_DOCUMENTS = 20000;

    public static void main(String[] args)
    {
       /* VizLincDB db = DBManager.getInstance().getDB();
        Iterable<Vertex> docVertices = db.getDocuments();
        List<Document> docs = createDocumentListFromVertices(docVertices);
        EntityValueSet eVS = getAllEntitiesForDocs(docs);
        System.out.println("Dates : " + eVS.getDates().size());
        System.out.println("ORGS: " + eVS.getOrgs().size());
        System.out.println("LOCS: " + eVS.getLocs().size());
        System.out.println("PERSONS : " + eVS.getPers().size());*/

    }

    /*public static List<Entity> getEntitiesOfType(String type, Facet parent)
     {
     StringBuilder sb = new StringBuilder("Getting entities of type: " + type).append("\n");
        
     List<Entity> values = new LinkedList<Entity>();
     VizLincDB db = DBManager.getInstance().getDB();
     sb.append("Query: ");
     long start = System.currentTimeMillis();
     Iterable<Vertex> entities = db.getEntitiesOfType(type);
     long end = System.currentTimeMillis();
     sb.append((end - start)/1000).append("secs\n");
     sb.append("Iterating over results: ");
     start = System.currentTimeMillis();
     //TODO: Remove counter - amoutn limit
     int counter = 0;
     for(Vertex v: entities)
     {       
     if(counter >= 1000)
     {
     // break;
     }
     String text = v.getProperty(VizLincProperties.P_ENTITY_TEXT).toString();
     int mentionCount = (Integer) v.getProperty("num_mentions");
     //values.add(new FacetValue(parent, text, mentionCount, v));
     values.add(new Entity(parent, text, mentionCount, null));
     counter++;
     }
     end = System.currentTimeMillis();
     sb.append((end - start)/1000).append("secs\n");
        
     Log.appendLine(sb.toString());
     return values;
     }*/
    public static List<PersonValue> getPersons()
    {
        //return getEntitiesOfType("PERSON");
        VizLincRDBMem db = DBManager.getInstance().getDB();
        try
        {
            StringBuilder sb = new StringBuilder("Fetching all the people\n");
            long start = System.currentTimeMillis();
            List<PersonEntity> people = db.getPersonEntities();
            long end = System.currentTimeMillis();
            long duration = end - start;
            sb.append("Took" + duration + " ms\n");
            
            List<PersonValue> facetValues = new ArrayList<PersonValue>(people.size());
            for(PersonEntity p: people)
            {
                facetValues.add(new PersonValue(p));
            }
            Log.appendLine(sb.toString());
            return facetValues;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    public static List<DateValue> getDates()
    {
       // return getEntitiesOfType("DATE");
        VizLincRDBMem db = DBManager.getInstance().getDB();
        try
        {
            StringBuilder sb = new StringBuilder("Fetching all dates\n");
            long start = System.currentTimeMillis();

            List<DateEntity> dates = db.getDateEntities();
            long end = System.currentTimeMillis();
            long duration = end - start;
            sb.append("Took" + duration + " ms\n");
            List<DateValue> facetValues = new ArrayList<DateValue>(dates.size());
            for(DateEntity d: dates)
            {
                facetValues.add(new DateValue(d));
            }
            Log.appendLine(sb.toString());
            return facetValues;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static List<LocationValue> getLocations()
    {
       // return getEntitiesOfType("LOCATION");
        VizLincRDBMem db = DBManager.getInstance().getDB();
        try
        {
            System.out.println("Fetching all locations");
            StringBuilder sb = new StringBuilder("Fetching all locations\n");
            long start = System.currentTimeMillis();
            List<LocationEntity> locs = db.getLocationEntities();
            long end = System.currentTimeMillis();
            long duration = end - start;
            sb.append("Took ").append(duration).append(" ms\n");
            
            List<LocationValue> facetValues = new ArrayList<LocationValue>(locs.size());
            for(LocationEntity l: locs)
            {
                facetValues.add(new LocationValue(l));
            }
            
                
            Log.appendLine(sb.toString());
            printCountsToFile(facetValues);
            return facetValues;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static List<OrganizationValue> getOrganizations()
    {
      //  return getEntitiesOfType("ORGANIZATION");
       VizLincRDBMem db = DBManager.getInstance().getDB();
        try
        {
            StringBuilder sb = new StringBuilder("Fetching all orgs\n");
            long start = System.currentTimeMillis();
            List<OrganizationEntity> orgs =  db.getOrganizationEntities();
            long end = System.currentTimeMillis();
            long duration = end - start;
            sb.append("Took" + duration + " ms\n");
            
            List<OrganizationValue> facetValues = new ArrayList<OrganizationValue>(orgs.size());
            for(OrganizationEntity o: orgs)
            {
                facetValues.add(new OrganizationValue(o));
            }
            Log.appendLine(sb.toString());
            return facetValues;
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static List<Document> getAllDocuments() throws SQLException
    {
        VizLincRDBMem db = DBManager.getInstance().getDB();
        StringBuilder sb = new StringBuilder("Getting all document names\n");
        sb.append("\n");
        long now = System.currentTimeMillis();
        List<Document> docs = db.getDocuments();
        sb.append("Took " + (System.currentTimeMillis() - now) + " ms\n");

        Log.appendLine(sb.toString());
        return docs;
    }

    public static List<Document> getAllDocumentsForFacetValues(List<FacetValue> filterList) throws SQLException
    {
        //VizLincDB db = new VizLincDB(DB_LOCATION);
        StringBuilder sb = new StringBuilder("Getting all documents for entity values\n");
        if (filterList.isEmpty())
        {
            return getAllDocuments();
        }

        //Create entity id list
        sb.append("Creating parameters : ");
        long t1 = System.currentTimeMillis();
        List<Integer> entityIdList = new ArrayList<Integer>(filterList.size());
        for(FacetValue e: filterList)
        {
            entityIdList.add(e.getId());
        }
        long t2 = System.currentTimeMillis();
        sb.append((t2-t1) + " ms\n");
        VizLincRDBMem db = DBManager.getInstance().getDB();
        t1 = System.currentTimeMillis();
        List<Integer> docIds = db.getDocumentIdsWithAllOfTheseEntityIds(entityIdList);
        t2 = System.currentTimeMillis();
        sb.append("Query for doc ids: " + (t2 - t1));
        t1 = System.currentTimeMillis();
        List<Document> docs = db.getDocumentsWithIds(docIds);
        t2 = System.currentTimeMillis();
        sb.append("Query for doc objects: " + (t2-t1) + " ms\n");
        Log.appendLine(sb.toString());
        
        return docs;
    }


    public static List<String> getDistinctMentionTextsForEntityIdInDocument(int entityId, int docId) throws SQLException
    {
        VizLincRDBMem db = DBManager.getInstance().getDB();
        return db.getDistinctMentionTextsForEntityIdInDocument(entityId, docId);
    }
    
    public static FacetValuesByCategory getAllEntitiesForDocs(List<Document> docList) throws SQLException
    {
        List<Integer> docIdList = getDocIdList(docList);
        
        VizLincRDBMem db = DBManager.getInstance().getDB();
        StringBuilder sb = new StringBuilder("Getting all entities for docs: \n");
        long t1 = System.currentTimeMillis();
        
        List<Integer> entityIds =  db.getEntityIdsInAnyofTheseDocumentIds(docIdList);
        long t2 = System.currentTimeMillis();
        sb.append("Query to get entity ids: ").append((t2 - t1)).append("\n");
        t1 = System.currentTimeMillis();
        EntitySet entitySet = db.getEntitiesWithIds(entityIds);
        t2 = System.currentTimeMillis();
        sb.append("Query to get EntitySet").append((t2-t1)).append(" ms\n");
        
        //Get mention counts
        t1 = System.currentTimeMillis();
        Map<Integer, Integer> mentionMap =  db.getMentionCountsForEntitiesInDocuments(entityIds, docIdList);
        t2 = System.currentTimeMillis();
        sb.append("Query to get mention counts").append("Took: ").append((t2-t1)).append(" ms");
        //t1 = System.currentTimeMillis();
        //results.updateMentionCounts(mentionMap);
        //t2 = System.currentTimeMillis();
        
        //Get document counts
        t1 = System.currentTimeMillis();
        Map<Integer, Integer> docCountMap = db.getDocumentCountsForEntitiesInDocuments(entityIds, docIdList);
        t2 = System.currentTimeMillis();
        sb.append("Query to update fetch document counts took: " + (t2-t1) + " ms");
        //results.updateDocumentCounts(docCountMap);
        Log.appendLine(sb.toString());
        
        return new FacetValuesByCategory(entitySet, mentionMap, docCountMap, false);
    }
    
    public static FacetValuesByCategory getNeighboringEntities(List<Document> docList,NeighborhoodConfig neighConfig) throws SQLException
    {
        List<Integer> docIdList = getDocIdList(docList);
        
        VizLincRDBMem db = DBManager.getInstance().getDB();
        StringBuilder sb = new StringBuilder("Getting all entities for docs: \n");
        long t1 = System.currentTimeMillis();
        
        List<Integer> entityIds =  db.getEntityIdsInAnyofTheseDocumentIds(docIdList);
        long t2 = System.currentTimeMillis();
        sb.append("Query to get entity ids: ").append((t2 - t1)).append("\n");
        
        List<Integer> queryEntitiesId = neighConfig.getNonKeywordFilterIds();
        int nDistance = neighConfig.getDistance();
       // Map<Integer, EntityCounts> neighEntitiesAndCounts = db.getEntitiesMentionedNearEntitiesByIndex(queryEntitiesId, entityIds, docIdList, nDistance);
         Map<Integer, EntityCounts> neighEntitiesAndCounts = db.getEntitiesMentionedNearEntitiesByTextOffset(queryEntitiesId, entityIds, docIdList, nDistance);
        
        Set<Integer> neighEntityIds = neighEntitiesAndCounts.keySet();
        EntitySet neighEntitySet = db.getEntitiesWithIds(new ArrayList<Integer>(neighEntityIds));
        
        //Create neighborhood mention/doc count maps
        Map<Integer, Integer> mentionCountsMap = new HashMap<Integer, Integer> (neighEntityIds.size());
        Map<Integer, Integer> docCountsMap = new HashMap<Integer, Integer> (neighEntityIds.size());
        
        for(Integer id : neighEntityIds)
        {
            EntityCounts counts = neighEntitiesAndCounts.get(id);
            mentionCountsMap.put(id, counts.mentionCount);
            docCountsMap.put(id, counts.documentCount);
        }
           
        Log.appendLine(sb.toString());
        
        return new FacetValuesByCategory(neighEntitySet, mentionCountsMap, docCountsMap, false);
    }
    
    private static long getIdAsLong(Vertex v)
    {
        Long id = ((Long) v.getId());
        return id.longValue();
    }
    
    public static List<GeoPoint> getTopGeos(List<Integer> locIds) throws SQLException
    {
        VizLincRDBMem db = DBManager.getInstance().getDB();
        long t1 = System.currentTimeMillis();
        List<GeoPoint> result = db.getTopGeoPointsForLocationEntityIds(locIds);
        long t2 = System.currentTimeMillis();
        Log.appendLine("Getting top geos for " + locIds.size() + " locations took" + (t2-t1) + " ms\n");
        return result;
    }

    public static List<DocNameId> keywordSearch(Query q) throws CorruptIndexException, IOException, ParseException
    {   
        VizLincSearcher searcher = DBManager.getInstance().getSearcher();
       // List<Integer> ids = searcher.searchWithQuery(searchString, NUM_DOCUMENTS);
         List<Integer> ids = searcher.searchWithQuery(q, NUM_DOCUMENTS);
        List<DocNameId> docNamesIds = new ArrayList<DocNameId>(ids.size());
        for(int id: ids)
        {
            String docName = searcher.getDocumentName(id);
            docNamesIds.add(new DocNameId(docName, id));
        }
        
        return docNamesIds;
    }
    
    public static Map<String, Integer> getKeywordsMentionCountsForDocs(Query combinedQuery, List<Document> docs) throws IOException
    {
        VizLincSearcher searcher = DBManager.getInstance().getSearcher();
        return searcher.getTermPhraseFreqs(combinedQuery, docs);
    }

    public static List<String> searchDocByName(String keyword) throws IOException, ParseException
    {
        String queryString  = keyword;
        //JOptionPane.showMessageDialog(null, "Searching doc by name: " + queryString);
        VizLincSearcher searcher = DBManager.getInstance().getSearcher();
        QueryParser qp = new QueryParser(VizLincProperties.VIZLINCDB_LUCENE_VERSION, VizLincProperties.P_DOCUMENT_NAME, new KeywordAnalyzer());
        //String searchString = VizLincProperties.P_DOCUMENT_NAME + ":" + keyword;
        //List<Integer> ids = searcher.searchWithQuery(searchString, NUM_DOCUMENTS);
        Query q = qp.parse(queryString);
        List<Integer> ids = searcher.searchWithQuery(q, NUM_DOCUMENTS);
        List<String> docNames = new ArrayList<String>(ids.size());
        for(int id: ids)
        {
            String docName = searcher.getDocumentName(id);
            docNames.add(docName);
        }
        
        return docNames;
    }

    public static List<LocationValue> getLocationsInNeighborhood(List<FacetValue> nonKeywordFilters,List<LocationValue> locationsInResultSet, List<Document> docs, int distance) throws SQLException
    {
        if(nonKeywordFilters.isEmpty() || locationsInResultSet.isEmpty() || docs.isEmpty())
        {
            return new ArrayList<LocationValue>(0);
        }
        //Prepare data for query
        Set<Integer> queryEntityIds = new HashSet<Integer>(nonKeywordFilters.size());
        for(FacetValue fv: nonKeywordFilters)
        {
            queryEntityIds.add(fv.getId());
        }
        
        Set<Integer> wantedEntities = new HashSet<Integer>(locationsInResultSet.size());
        for(LocationValue lv: locationsInResultSet)
        {
            wantedEntities.add(lv.getId());
        }
        
        Set<Integer> docIds = new HashSet<Integer>(docs.size());
        for(Document d: docs)
        {
            docIds.add(d.getId());
        }
        
        VizLincRDBMem db = DBManager.getInstance().getDB();
        Map<Integer, EntityCounts> entCounts = db.getEntitiesMentionedNearEntitiesByIndex(queryEntityIds, wantedEntities, docIds, distance);
        //Extract locations only
        
       /* EntitySet allEntities = db.getEntitiesWithIds(new ArrayList(entCounts.keySet()));
        List<LocationEntity> allLocationEntities = allEntities.getLocationEntities();*/
        List<LocationValue> locationValues = new ArrayList<LocationValue>(entCounts.keySet().size());
        
        for(Integer id: entCounts.keySet())
        {
            EntityCounts count = entCounts.get(id);
            Entity e = db.getEntityWithId(id);
            //Check!!
            if(!(e.getType().equals(VizLincProperties.E_LOCATION)))
            {
                throw new RuntimeException("Unexpected entity type instance returned: " + e + ". Location expected.");
            }
            
            LocationValue locValue = new LocationValue(e);
            locValue.setCurrentNumMentions(count.mentionCount);
            locValue.setCurrentNumDocuments(count.documentCount);
            locValue.setShowFullDataSetCounts(false);
            locationValues.add(locValue);
        }
        
        return locationValues;
        
    }

    private static List<Integer> getDocIdList(List<Document> docList)
    {
        List<Integer> docIdList = new ArrayList<Integer>(docList.size());
        for(Document d: docList)
        {
            docIdList.add(d.getId());
        }
        
        return docIdList;
    }

    public static int getTotalMentionsForEntities(Document d, List<FacetValue> nonKeywordFilters) throws SQLException
    {
        VizLincRDBMem db = DBManager.getInstance().getDB();
        List<Integer> entityIds = new ArrayList(nonKeywordFilters.size());
        for(FacetValue fv: nonKeywordFilters)
        {
            entityIds.add(fv.getId());
        }
        
        List<Integer> docIds = new ArrayList<Integer>();
        docIds.add(d.getId());
        
        Map<Integer, Integer> result  = db.getMentionCountsForEntitiesInDocuments(entityIds, docIds);
        
        int sum = 0;
        for(int count: result.values())
        {
            sum += count;
        }
        
        return sum;
    }

    public static List<MentionLocation> getAllMentionsForThisDoc(int id)
    {
        VizLincRDBMem db = DBManager.getInstance().getDB();
        return db.getMentionLocationsForDocument(id);
    }

    public static Entity getEntityWithId(int eID)
    {
        VizLincRDBMem db = DBManager.getInstance().getDB();
        return db.getEntityWithId(eID);
    }

    public static List<MentionLocation> getMentionLocationsForEntityIdsInDocument(List<Integer> nonKeyWordEntityIds, int id)
    {
       VizLincRDBMem db = DBManager.getInstance().getDB();
       return db.getMentionLocationsForEntitiesIdInDocument(nonKeyWordEntityIds, id);
    }

    private static void printCountsToFile(List<LocationValue> facetValues) 
    {
        try (PrintWriter out = new PrintWriter("C:\\Users\\jo21372\\Desktop\\locCount.txt")) {
            for(LocationValue l: facetValues)
            {
                out.println(l.getNumMentionsShown());
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
}
