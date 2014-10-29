/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mit.ll.vizlinc.graph.layout.community;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import org.gephi.graph.api.Node;
import org.openide.util.*;
import org.gephi.graph.api.*;

/**
 *
 * @author ST23804
 */
public class Cluster {
    private ArrayList<Node> nodes;
    public UUID id;
    private float clusterRadius;
    
    public Cluster(){
        nodes = new ArrayList<Node>();
        id = UUID.randomUUID();
    }

    public void add(Node n){
        nodes.add(n);
    }

    public void addAll(Collection<? extends Node> c){
        nodes.addAll(c);
    }
            
    public ArrayList<Node> getNodes(){
        return nodes;
    }

    public UUID getID() {
        return id;
    }
    
    public boolean contains(Node n)
    {
        for(Node n2: this.nodes)
        {
            if(n2.getId() == n.getId())
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the average value of the Y components of the nodes in this cluster. 
     * This is treated as the center for some calculations
     * @return centerY
     */
    public float centerY(){
        float avY = 0;
        int count = 0;
        for (Node n : this.nodes){
            avY += n.getNodeData().y();
            count++;
        }
        avY = avY / count;
        return avY;
    }
    
    /**
     * Get the average value of the X components of the nodes in this cluster. 
     * This is treated as the center for some calculations
     * @return centerX
     */
    public float centerX(){
        float avX = 0;
        int count = 0;
        for (Node n : this.nodes){
            avX += n.getNodeData().x();
            count++;
        }
        avX = avX / count;
        return avX;
    }
    
    
    public float getRadius(){
        //return this.nodes.size()*4;
        return this.clusterRadius;
    }
    
    /**
     * Moves a cluster so that the average center of the cluster is at x, y 
     * This will preserve the prior layout of the nodes
     * @param x
     * @param y 
     */
    public void moveClusterRetain(float x, float y){
        // Get the average x and y values for the cluster
        float avX = this.centerX();
        float avY = this.centerY();
        
        float translateX = x - avX;
        float translateY = y - avY;
        
        for(Node n : this.nodes){
            n.getNodeData().setX(n.getNodeData().x() + translateX);
            n.getNodeData().setY(n.getNodeData().y() + translateY);
        }        
    }
    
    /**
     * Moves the cluster so that the average center of the cluster is at x,y 
     * and the nodes are drawn in a circle around the center
     * @param x
     * @param y 
     */
    public void moveCluster(float x, float y){
       int numNodes = this.nodes.size();
       double theta = 0;
       double inc = 2*Math.PI / numNodes;
       double radius = getRadius();
       
       for(Node n : this.nodes){
           float newX = (float) (radius * Math.cos(theta));
           float newY = (float) (radius * Math.sin(theta));
           n.getNodeData().setX(x + newX);
           n.getNodeData().setY(y + newY);
           
           theta += inc;       
       }
        
    }
    
    /**
     * Decide if this cluster is intersecting with another cluster
     * @param other
     * @return boolean, whether or not this cluster intersects with other
     */
    public boolean isIntersecting(Cluster other){
        // TODO: don't forget that nodes have radii also. 
        double distance = Math.sqrt(Math.pow(other.centerX() - this.centerX(), 2) + Math.pow(other.centerY() - this.centerY(), 2));
        return distance < (this.getRadius() + other.getRadius());
    }
    
    /**
     * Give this cluster the color c
     * Transparency is not supported
     * @param c - a color. 
     */
    public void colorCluster(java.awt.Color c){
        // Need to divide by 256 because java.awt.Color gives values in the range [0, 255]
        // and colorCluster takes values in the range [0, 1]
        this.colorCluster(c.getRed()/ (float)256, c.getGreen()/ (float)256, c.getBlue() / (float)256);
    }
    
    /**
     * Give this cluster the color given by (r, g, b)
     * Each argument is a value between 0 and 1
     * Transparency is not supported
     * @param r - red component
     * @param g - green component
     * @param b  - blue component
     */
    public void colorCluster(float r, float g, float b){
        for( Node n : this.nodes){
            n.getNodeData().setColor(r, g, b);
        }
    }
    
    /**
     * 
     * @param other
     * @param maxConnections maximum number of connections 'allowed'. This is used to speed up
     * the process in cases when we are not interested in the exact number of connections. Pass -1 for 
     * unlimited.
     * @return 
     */
    public ArrayList<Edge> getSharedConnections(Cluster other, int maxConnections)
    {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel model = graphController.getModel();
        Graph graph = model.getGraph();
     
        ArrayList<Edge> shared = new ArrayList<Edge>();
        
        // Only loop through this.nodes because looping through all nodes
        // causes a double count
        for(Node n : this.nodes){
            // get all connections
            Edge[] edges = graph.getEdges(n).toArray();

            // for each edge
            for(Edge e : edges){
                Node tgt = e.getTarget();
                Node src = e.getSource();
                if(this.contains(tgt) && other.contains(src) || this.contains(src) && other.contains(tgt)){
                    shared.add(e);
                    if(maxConnections != -1 && shared.size() >= maxConnections)
                    {
                        return shared;
                    }
                }
            }
        }
        return shared;
    }
    
    /**
     * Get the number of edges shared between this cluster and other.
     * For parameters see {@link #getSharedConnections(edu.mit.ll.vizlinc.graph.layout.community.Cluster, int) }
     */
    public int getNumConnections(Cluster other, int maxConnections)
    {
        ArrayList<Edge> shared = this.getSharedConnections(other, maxConnections);
        return shared.size();
    }
    
    /**
     * Get the smallest connection weight between two clusters
     * @param other
     * @return 
     */
    public float getSmallestConnectionWeightBetween(Cluster other){
        ArrayList<Edge> shared = this.getSharedConnections(other,-1);
        System.out.println(shared.size());
        float min = Float.MAX_VALUE;
        for(Edge e : shared){
            float weight = Float.parseFloat((String)e.getAttributes().getValue("label"));
            if(weight != 0){
                System.out.println(weight);
            }
            if(weight < min){
                min = weight;
            }
        }
        return min;
    }
    
    /**
     * Get all hypothesis edges for a given node
     * TODO: This might be nice to cache if things get too slow.
     * @param n
     * @return ArrayList of hypothesis edges
     */
//    ArrayList<Edge> getHypEdges(Node n) {
//        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
//        GraphModel model = graphController.getModel();
//        Graph graph = model.getGraph();
//
//        Edge[] allEdges = graph.getEdges(n).toArray();
//        ArrayList<Edge> justHyp = new ArrayList<Edge>();
//        
//        // Hypothesis edges have an ID that
//        // begins with CompareProcessor.default_ID
//        for(Edge e : allEdges){
//            if(e.getEdgeData().getId().startsWith(CompareProcessor.default_ID)){
//                justHyp.add(e);
//            }            
//        }
//        return justHyp;
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cluster other = (Cluster) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    public void layoutClusterCircleMultiLevel(float x, float y) {

        //place first node in the center
        final int margin = 5;
        NodeData nd = this.nodes.get(0).getNodeData();
        nd.setX(x);
        nd.setY(y);
        double nodeRadius = nd.getRadius();
        double r = 2 * nodeRadius;
        double theta = 0;
        double inc = (2 * nodeRadius) / r;
        for (int i = 1; i < nodes.size(); i++) {
            if (theta >= (2 * Math.PI)) {
                theta = 0;
                r = r + (2 * nodeRadius) + margin;
                inc = (2 * nodeRadius) / r;
            }
            Node n = nodes.get(i);
            float newX = (float) (r * Math.cos(theta));
            float newY = (float) (r * Math.sin(theta));
            nd = n.getNodeData();
            nd.setX(x + newX);
            nd.setY(y + newY);
            theta += inc;
        }
        this.clusterRadius = (float)(r + nodeRadius);
    }

    /**
     * This method assumes that clusters have been laid out with a reference 
     * center of (0,0)
     * @param x
     * @param y 
     */
    public void displaceCluster(float x, float y) 
    {
        for(Node n: this.nodes)
        {
            NodeData nd = n.getNodeData();
            float currX = nd.x();
            float currY = nd.y();
            
            nd.setX(currX + x);
            nd.setY(currY + y);
        }
    }
}
