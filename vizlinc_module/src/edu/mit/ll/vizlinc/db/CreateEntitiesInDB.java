/*
 */
package edu.mit.ll.vizlinc.db;

import edu.mit.ll.vizlincdb.graph.VizLincDB;
import com.tinkerpop.blueprints.Vertex;
import edu.mit.ll.vizlinc.utils.TextUtils;
import edu.mit.ll.vizlincdb.util.VizLincProperties;
import edu.mit.ll.vizlincdb.graph.VizLincDB;
import edu.mit.ll.vizlincdb.util.VizLincProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for debugging and testing purposes.
 */
public class CreateEntitiesInDB {

    private VizLincDB db;

    public static void main(String[] args) {
        System.out.println("Starting app");
        CreateEntitiesInDB creator = new CreateEntitiesInDB("E:\\VizLinc\\database\\sn-doc100-2012-12-19.neo4j");
       // creator.removeAllEntities();
        creator.removeAllEntitiesOfType("DATE");
        creator.createNewEntities();
       // creator.printEntities();
        creator.shutdown();
    }

    public CreateEntitiesInDB(String dbPath) {
        db = new VizLincDB(dbPath);
    }

    private void removeAllEntities() {
        System.out.println("Removing all entities from db");

        for (Vertex v : db.getEntities()) {
            db.deleteNode(v);
            db.commit();
        }

    }

    private void createNewEntities() {
        //createNewEntitiesAux("PERSON");
        createNewEntitiesAux("DATE");
        //createNewEntitiesAux("LOCATION");
        //createNewEntitiesAux("ORGANIZATION");
    }

    private void createNewEntitiesAux(String type) {
        List<String> unresolvedDates = new LinkedList<String> ();
        System.out.println("Creating entities of type " + type);
        // Set<String> mentions = new HashSet<String>();
        System.out.println("Getting mentions");
        Iterable<Vertex> entNodes = db.getMentionsOfType(type);
        System.out.println("Resolving...");
      //  StringDisambiguator sd = new StringDisambiguator();
        //List<Entity> entities = sd.resolve(entNodes);
       

//**********************************************************
        Map<String,Entity> entitiesMap = new HashMap<String, Entity> (); //using mention as key
        
        System.out.println("Before iterator");
        System.out.println("Has next?? " + entNodes.iterator().hasNext());
        System.out.println("After iterator");
        for (Vertex v : entNodes) {
            
            System.out.println("Iterating..");
            String mentionStr = v.getProperty(VizLincProperties.P_MENTION_TEXT).toString();
            System.out.println("Mention : " + mentionStr);
            //Normalize mention
            String normalizedMention = null;
            if(type.equals("DATE"))
            {
                NormalizedDate date = null;
                try
                {
                 date = DateNormalizer.normalize(mentionStr);
                }catch(IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
                if(date == null) 
                {
                    unresolvedDates.add(mentionStr);
                    continue;
                }
                normalizedMention = date.toString();
                if(normalizedMention.equals("2002/mayo/16"))
                {
                    System.out.println("Found it");
                }
            }else
            {
                normalizedMention = TextUtils.toCamelCase(mentionStr);
            }
            
            if(entitiesMap.containsKey(normalizedMention))
            {
                System.out.println("Existing.");
                Entity e = entitiesMap.get(normalizedMention);
                e.addMention(v);
            }
            else
            {
                System.out.println("NEW!");
                entitiesMap.put(normalizedMention, new Entity(normalizedMention, v));
            }
           
        }
        
        List<Entity> entities =  new ArrayList<Entity>(entitiesMap.values());
        //**********************************************************

        
        //Create entities, one per distinct mention
        int count = 0;
        for (Entity e : entities) {
            System.out.println("Creating vertex for mention: " + e.getCanonicalName());
            int numMentions = e.getNumMentions();
            if(numMentions > 1)
            {
                System.out.println("MORE THAN ONE MENTION ASSOCIATED!! " + numMentions);
            }
            
            Vertex eNode = db.newEntity(type, e.getCanonicalName(), "joel_simple");
            db.connectEntityToMentionsAndDocuments(eNode, e.getMentionNodes());
            eNode.setProperty("num_mentions", new Integer(numMentions));
            db.commit();
            count++;
        }
        System.out.println("**** CREATED " + count + " ENTITIES *********");
        System.out.println("** UNRESOLVED **");
        for(String s: unresolvedDates)
        {
            System.out.println("**" + s + "**");
        }
    }

    private void printEntities() {
        System.out.println("*********** ENTITIES **********************");
        for (Vertex v : db.getEntities()) {
            String text = v.getProperty(VizLincProperties.P_ENTITY_TEXT).toString();
            String type = v.getProperty(VizLincProperties.P_ENTITY_TYPE).toString();
            System.out.println(text + "(" + type + ")");
        }
        System.out.println("**********************************************");
    }

    private void shutdown() {
        db.shutdown();
    }

    private void removeAllEntitiesOfType(String type) {
        System.out.println("Removing all nodes of type " + type);
           int count = 0;     
       for(Vertex v: db.getEntitiesOfType(type))
       {
           db.deleteNode(v);
           db.commit();
           count++;
       }
       
       System.out.println("Removed " + count + " nodes.");
    }
}
