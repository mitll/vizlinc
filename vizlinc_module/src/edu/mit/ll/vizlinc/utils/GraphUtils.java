/*
 */
package edu.mit.ll.vizlinc.utils;

import org.gephi.graph.api.GraphFactory;

/**
 *
 */
public class GraphUtils {
    
    public static org.gephi.graph.api.Edge convertEdge(GraphFactory factory, com.tinkerpop.blueprints.Edge edgeFromBlueprints, com.tinkerpop.blueprints.Vertex nodeFromBlueprints1, com.tinkerpop.blueprints.Vertex nodeFromBlueprints2)
    {
        org.gephi.graph.api.Node node1 = convertNode(factory, nodeFromBlueprints1);
        org.gephi.graph.api.Node node2 = convertNode(factory, nodeFromBlueprints2);
        return factory.newEdge(node1, node2);
    }
    
    public static org.gephi.graph.api.Node convertNode(GraphFactory factory, com.tinkerpop.blueprints.Vertex nodeFromBlueprints)
    {
         return factory.newNode(nodeFromBlueprints.getId().toString());
    }
}
