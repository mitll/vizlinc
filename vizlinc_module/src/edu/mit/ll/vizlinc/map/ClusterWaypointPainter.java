/*
 */
package edu.mit.ll.vizlinc.map;

import edu.mit.ll.vizlinc.components.MapTopComponent;
import edu.mit.ll.vizlinc.model.LocationValue;
import edu.mit.ll.vizlinc.utils.Log;
import java.awt.BasicStroke;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultWaypointRenderer;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.painter.AbstractPainter;
import edu.mit.ll.vizlinc.components.PropertiesTopComponent;
import java.util.Collections;
import java.util.Comparator;

/**
 * Paints waypoints on the JXMapViewer. This is an instance of Painter that only
 * can draw on to JXMapViewers.
 *
 */
public class ClusterWaypointPainter<T extends JXMapViewer> extends AbstractPainter<T>
{

    private WaypointRenderer renderer = new DefaultWaypointRenderer();
    private List<Waypoint> waypoints;
    private MapTopComponent.ColorByCriterion waypointsSortedBy;
    private int prevZoom;
    private int minPoints;
    private Map<Integer, Integer> clusterCounters;
    private boolean clusteringOn;
    private List<ClusterWaypoint> clickablePoints;
    private Rectangle selectionRectangle;
    private LocationValue nodeToHighligh;
    //Comparators
    private Comparator mentionComparator = new Comparator<Waypoint>()
    {
        @Override
        public int compare(Waypoint o1, Waypoint o2)
        {
            ClusterWaypoint cw1 = (ClusterWaypoint) o1;
            ClusterWaypoint cw2 = (ClusterWaypoint) o2;
            int c1 = cw1.getLocationValue().getNumMentionsShown();
            int c2 = cw2.getLocationValue().getNumMentionsShown();
            if (c1 > c2)
            {
                return 1;
            }
            if (c1 < c2)
            {
                return -1;
            }
            return 0;
        }
    };
    private Comparator docComparator = new Comparator<Waypoint>()
    {
        @Override
        public int compare(Waypoint o1, Waypoint o2)
        {
            ClusterWaypoint cw1 = (ClusterWaypoint) o1;
            ClusterWaypoint cw2 = (ClusterWaypoint) o2;
            int c1 = cw1.getLocationValue().getNumDocumentsShown();
            int c2 = cw2.getLocationValue().getNumDocumentsShown();
            if (c1 > c2)
            {
                return 1;
            }
            if (c1 < c2)
            {
                return -1;
            }
            return 0;
        }
    };

    /**
     * Creates a new instance of WaypointPainter
     */
    public ClusterWaypointPainter()
    {
        setAntialiasing(true);
        setCacheable(false);
        waypoints = new LinkedList<Waypoint>();
        prevZoom = -1;
        this.minPoints = 2;
        clusteringOn = false;
        this.clickablePoints = new LinkedList<ClusterWaypoint>();
        nodeToHighligh = null;
        waypointsSortedBy = null;
    }

    /**
     * Sets the waypoint renderer to use when painting waypoints
     *
     * @param r the new WaypointRenderer to use
     */
    public void setRenderer(WaypointRenderer r)
    {
        this.renderer = r;
    }

    /**
     * Gets the current set of waypoints to paint
     *
     * @return a typed Set of Waypoints
     */
    public List<Waypoint> getWaypoints()
    {
        return waypoints;
    }

    /**
     * Sets the current list of waypoints to paint
     *
     * @param waypoints the new list of Waypoints to use
     */
    public void setWaypoints(List<Waypoint> waypoints)
    {
        this.waypoints = waypoints;
        sortWaypoints();
        int maxMentions = Integer.MIN_VALUE;
        int maxDocs = Integer.MIN_VALUE;

        //Calculate max # of mentions
        for (Waypoint w : waypoints)
        {
            if (w instanceof ClusterWaypoint)
            {
                ClusterWaypoint cw = (ClusterWaypoint) w;
                int mentions = cw.getLocationValue().getNumMentionsShown();
                int docCount = cw.getLocationValue().getNumDocumentsShown();
                if (mentions > maxMentions)
                {
                    maxMentions = mentions;
                }
                if (docCount > maxDocs)
                {
                    maxDocs = docCount;
                }

            }
        }
        //save in renderer
        CircleWayPointRenderer cwRenderer = getRendererAsCWRenderer();
        cwRenderer.setMaxNumMentions(maxMentions);
        cwRenderer.setMaxNumDocs(maxDocs);

    }

    public void setColorBy(ColorByConfig config)
    {
        //If waypoints are not sorted in decreasing frequency according to the parameter
        //specified in this configuration, sort appropriately.
        getRendererAsCWRenderer().setColorBy(config);
        sortWaypoints();
    }
    
    /**
     * Sets the size of the circle marker in map
     * @param px - radius of circle
     */
    public void setMarkerSize(int px)
    {
        getRendererAsCWRenderer().setMarkerSize(px);
    }

    public ColorByConfig getColorBy()
    {
        return getRendererAsCWRenderer().getColorByConfig();
    }

    /**
     * {@inheritDoc}
     *
     * @param g
     * @param map
     * @param width
     * @param height
     */
    @Override
    protected void doPaint(Graphics2D g, T map, int width, int height)
    {
        if (renderer == null)
        {
            return;
        }

        //paintSelectionRectangle(g);

        int zoom = map.getZoom();
        if (zoom != this.prevZoom && clusteringOn)
        {
            // JOptionPane.showMessageDialog(null, "Re-clustering");
            //clusterWaypoints(map);
            dbScan(map);
            prevZoom = zoom;
            if (renderer instanceof CircleWayPointRenderer)
            {
                ((CircleWayPointRenderer) renderer).setClusterCountersAndTotal(this.clusterCounters, this.waypoints.size());
            }
        }
        //figure out which waypoints are within this map viewport
        //so, get the bounds
        Rectangle viewportBounds = map.getViewportBounds();
        Dimension sizeInTiles = map.getTileFactory().getMapSize(zoom);
        int tileSize = map.getTileFactory().getTileSize(zoom);
        Dimension sizeInPixels = new Dimension(sizeInTiles.width * tileSize, sizeInTiles.height * tileSize);

        double vpx = viewportBounds.getX();
        // normalize the left edge of the viewport to be positive
        while (vpx < 0)
        {
            vpx += sizeInPixels.getWidth();
        }
        // normalize the left edge of the viewport to no wrap around the world
        while (vpx > sizeInPixels.getWidth())
        {
            vpx -= sizeInPixels.getWidth();
        }

        // create two new viewports next to eachother
        Rectangle2D vp2 = new Rectangle2D.Double(vpx,
                viewportBounds.getY(), viewportBounds.getWidth(), viewportBounds.getHeight());
        Rectangle2D vp3 = new Rectangle2D.Double(vpx - sizeInPixels.getWidth(),
                viewportBounds.getY(), viewportBounds.getWidth(), viewportBounds.getHeight());

        //for each waypoint within these bounds
        StringBuilder sb = new StringBuilder("Painting all waypoints\n");
        long start = System.currentTimeMillis();
        List<Waypoint> highlightedWaypoints = new LinkedList<Waypoint>();
        for (Waypoint w : getWaypoints())
        {
            if (((ClusterWaypoint) w).isHighlighted())
            {
                //Leave it for the end
                highlightedWaypoints.add(w);
            } else
            {
                doPaintWaypoint(map, w, vp2, vp3, g);
            }
        }
        //Paint highlighed waypoints last so that they appear on top of everything else
        for (Waypoint w : highlightedWaypoints)
        {
            doPaintWaypoint(map, w, vp2, vp3, g);
        }
        long end = System.currentTimeMillis();
        sb.append("painting all waypoints took: " + (end - start) + " ms\n");
        Log.appendLine(sb.toString());
    }

    private void doPaintWaypoint(T map, Waypoint w, Rectangle2D vp2, Rectangle2D vp3, Graphics2D g)
    {
        Point2D point = map.getTileFactory().geoToPixel(w.getPosition(), map.getZoom());
        if (vp2.contains(point))
        {
            int x = (int) (point.getX() - vp2.getX());
            int y = (int) (point.getY() - vp2.getY());
            setWaypointBounds(w, x, y);
            g.translate(x, y);
            paintWaypoint(w, map, g);
            g.translate(-x, -y);
        }
        if (vp3.contains(point))
        {
            int x = (int) (point.getX() - vp3.getX());
            int y = (int) (point.getY() - vp3.getY());
            setWaypointBounds(w, x, y);
            g.translate(x, y);
            paintWaypoint(w, map, g);
            g.translate(-x, -y);
        }
    }

    /**
     * <p>Override this method if you want more control over how a waypoint is
     * painted than what you can get by just plugging in a custom waypoint
     * renderer. Most developers should not need to override this method and can
     * use a WaypointRenderer instead.</p>
     *
     *
     * <p>This method will be called to each waypoint with the graphics object
     * pre-translated so that 0,0 is at the center of the waypoint. This saves
     * the developer from having to deal with lat/long => screen coordinate
     * transformations.</p>
     *
     * @param w the current waypoint
     * @param map the current map
     * @param g the current graphics context
     * @see setRenderer(WaypointRenderer)
     * @see WaypointRenderer
     */
    protected void paintWaypoint(final Waypoint w, final T map, final Graphics2D g)
    {
        renderer.paintWaypoint(g, map, w);
    }

    private static void p(String str)
    {
        System.out.println(str);
    }

    private void clusterWaypoints(T map)
    {
        //calculate distance matrix N^2 for now
        final float eps = 25; //in pixels
        Map<ClusterWaypoint, Map<ClusterWaypoint, Float>> distanceMatrix = calcDistanceMatrix(map);
        int clusterId = 1;

        for (Waypoint w : waypoints)
        {
            ClusterWaypoint cw = (ClusterWaypoint) w;
            List<ClusterWaypoint> epsNeigh = getEpsilonNeigh(cw, distanceMatrix, eps);
            int useThisId = clusterId;
            if (cw.getClusterId() != -1)
            {
                useThisId = cw.getClusterId();
                clusterId++;
            }
            for (ClusterWaypoint w2 : epsNeigh)
            {
                w2.setClusterId(useThisId);
            }
        }
    }

    private void dbScan(T map)
    {
        resetDBSCAN();

        //Calculate distance matrix; it will be used to find the eps Neighborhood
        Map<ClusterWaypoint, Map<ClusterWaypoint, Float>> distanceMatrix = calcDistanceMatrix(map);
        // printDistanceMatrix(distanceMatrix);

        int clusterId = 1;
        for (Waypoint w : this.waypoints)
        {
            ClusterWaypoint p = (ClusterWaypoint) w;
            if (p.getClusterId() == ClusterWaypoint.UNCLASSIFIED)
            {
                if (expandCluster(p, clusterId, 25, distanceMatrix))
                {
                    clusterId++;
                }
            }
        }

        printClusterCounts();
    }

    private Map<ClusterWaypoint, Map<ClusterWaypoint, Float>> calcDistanceMatrix(T map)
    {
        Map<ClusterWaypoint, Map<ClusterWaypoint, Float>> distanceMatrix = new HashMap<ClusterWaypoint, Map<ClusterWaypoint, Float>>();
        for (Waypoint w : waypoints)
        {
            Map<ClusterWaypoint, Float> thisRow = distanceMatrix.get((ClusterWaypoint) w);
            if (thisRow == null)
            {
                thisRow = new HashMap<ClusterWaypoint, Float>();
                distanceMatrix.put((ClusterWaypoint) w, thisRow);
            }
            for (Waypoint w2 : waypoints)
            {
                float dist = euclDistance(w, w2, map);
                thisRow.put((ClusterWaypoint) w2, dist);
            }
        }
        return distanceMatrix;
    }

    private float euclDistance(Waypoint w1, Waypoint w2, T map)
    {
        TileFactory factory = map.getTileFactory();
        int zoom = map.getZoom();
        Point2D p1 = factory.geoToPixel(w1.getPosition(), zoom);
        Point2D p2 = factory.geoToPixel(w2.getPosition(), zoom);

        return (float) p1.distance(p2);
    }

    private List<ClusterWaypoint> getEpsilonNeigh(ClusterWaypoint w, Map<ClusterWaypoint, Map<ClusterWaypoint, Float>> distanceMatrix, float eps)
    {
        Map<ClusterWaypoint, Float> allNeighbors = distanceMatrix.get(w);
        List<ClusterWaypoint> epsN = new LinkedList<ClusterWaypoint>();

        for (ClusterWaypoint n : allNeighbors.keySet())
        {
            float dist = allNeighbors.get(n);
            if (dist <= eps)
            {
                epsN.add(n);
            }
        }
        return epsN;
    }

    private boolean expandCluster(ClusterWaypoint p, int clusterId, int eps, Map<ClusterWaypoint, Map<ClusterWaypoint, Float>> distanceMatrix)
    {
        List<ClusterWaypoint> seeds = getEpsilonNeigh(p, distanceMatrix, eps);
        if (seeds.size() < this.minPoints) // no core point
        {
            //println("Assigning NOISE to: " + p.getPosition().toString());
            p.setClusterId(ClusterWaypoint.NOISE);
            this.clickablePoints.add(p);
            //increaseCounter(ClusterWaypoint.NOISE,1);

            return false;
        } else //all points in seeds are density reachable from p
        {
            int pointsInCluster = 0;
            for (ClusterWaypoint cw : seeds)
            {
                cw.setClusterId(clusterId);
                pointsInCluster++;
            }

            seeds.remove(p);
            while (!seeds.isEmpty())
            {
                ClusterWaypoint currentP = seeds.get(0);
                List<ClusterWaypoint> result = getEpsilonNeigh(currentP, distanceMatrix, eps);
                if (result.size() >= this.minPoints)
                {
                    for (ClusterWaypoint resultP : result)
                    {
                        if (resultP.getClusterId() == ClusterWaypoint.NOISE
                                || resultP.getClusterId() == ClusterWaypoint.UNCLASSIFIED)
                        {
                            if (resultP.getClusterId() == ClusterWaypoint.UNCLASSIFIED)
                            {
                                seeds.add(resultP);
                            } else //cluster id == NOISE
                            {
                                this.clickablePoints.remove(resultP);
                            }
                            resultP.setClusterId(clusterId);
                            pointsInCluster++;
                        }
                    }
                }
                seeds.remove(currentP);

            }
            increaseCounter(clusterId, pointsInCluster);
            return true;
        }
    }

    private void increaseCounter(int clusterId, int byAmount)
    {
        if (!this.clusterCounters.containsKey(clusterId))
        {
            this.clusterCounters.put(clusterId, byAmount);
        } else
        {
            int currCount = this.clusterCounters.get(clusterId);
            this.clusterCounters.put(clusterId, currCount + byAmount);
        }
    }

    private void printDistanceMatrix(Map<ClusterWaypoint, Map<ClusterWaypoint, Float>> distanceMatrix)
    {
        for (ClusterWaypoint point : distanceMatrix.keySet())
        {
            Map<ClusterWaypoint, Float> neighs = distanceMatrix.get(point);
            StringBuilder sb = new StringBuilder();
            for (ClusterWaypoint n : neighs.keySet())
            {
                sb.append(n.getPosition().toString()).append(":");
                float dist = neighs.get(n);
                sb.append(dist).append(", ");
            }
        }
    }

    private void resetDBSCAN()
    {
        //Mark all points as unclassified
        for (Waypoint p : this.waypoints)
        {
            ((ClusterWaypoint) p).setClusterId(ClusterWaypoint.UNCLASSIFIED);
        }

        //Reset cluster counters
        this.clusterCounters = new HashMap<Integer, Integer>();
        this.clickablePoints.clear();
    }

    private void printClusterCounts()
    {
        int total = 0;
        for (Integer id : this.clusterCounters.keySet())
        {
            int count = this.clusterCounters.get(id);
            total += count;
        }

    }

    public void setClusteringOn(boolean clusterOn)
    {
        this.clusteringOn = clusterOn;
        if (this.renderer != null && this.renderer instanceof CircleWayPointRenderer)
        {
            ((CircleWayPointRenderer) this.renderer).setClusteringOn(clusterOn);
        }
    }

    public boolean isClusteringOn()
    {
        return this.clusteringOn;
    }

    public List<ClusterWaypoint> getClickablePoints()
    {
        if (this.clusteringOn)
        {
            return clickablePoints;
        }

        return new ArrayList<ClusterWaypoint>(0);
    }

    private void setWaypointBounds(Waypoint w, int centerX, int centerY)
    {
        if (!(w instanceof ClusterWaypoint))
        {
            return;
        }

        ClusterWaypoint cw = (ClusterWaypoint) w;
        if (this.clusteringOn && cw.getClusterId() != ClusterWaypoint.NOISE)
        {
            return;
        }

        int width = getRendererAsCWRenderer().getMarkerWidth();
        int height = getRendererAsCWRenderer().getMarkerHeight();
        int x = centerX - (width / 2);
        int y = centerY - (height / 2);

        cw.setBounds(x, y, width, height);
        //println("Setting bounds: " + cw.getBounds().toString());
    }

    private CircleWayPointRenderer getRenderer()
    {
        if (this.renderer != null)
        {
            return (CircleWayPointRenderer) this.renderer;
        }

        return null;
    }

    private void paintSelectionRectangle(Graphics2D g)
    {
        if (selectionRectangle != null && selectionRectangle.width > 0 && selectionRectangle.height > 0)
        {
            ((Graphics2D) g).setStroke(new BasicStroke(1f));
            g.drawRect(selectionRectangle.x, selectionRectangle.y, selectionRectangle.width, selectionRectangle.height);
        }
    }

    public void setSelectionRectangle(Rectangle selectionRectangle)
    {
        this.selectionRectangle = selectionRectangle;
    }

    private CircleWayPointRenderer getRendererAsCWRenderer()
    {
        return ((CircleWayPointRenderer) this.renderer);
    }

    public void setAsHighlightedLocations(Set<LocationValue> locationsToHighlight)
    {
        //Iterate over waypoints and mark them as highlighted if appropriate
        for (Waypoint w : this.waypoints)
        {
            ClusterWaypoint cw = (ClusterWaypoint) w;
            LocationValue thisLocation = cw.getLocationValue();
            if (locationsToHighlight.contains(thisLocation))
            {
                cw.setHighlighted(true);
            } else
            {
                cw.setHighlighted(false);
            }
        }
    }

    private void sortWaypoints()
    {
        ColorByConfig config = getColorBy();
        MapTopComponent.ColorByCriterion criterion = config.getColorByCriterion();
        if (config.isActive() && !(waypointsSortedBy == config.getColorByCriterion()))
        {
            System.out.println("Sorting waypoints");
            Comparator comp = null;
            if (criterion == MapTopComponent.ColorByCriterion.MENTION)
            {
                comp = mentionComparator;
            }
            if (criterion == MapTopComponent.ColorByCriterion.DOCUMENT)
            {
                comp = docComparator;
            }

            Collections.sort(this.waypoints, comp);
            this.waypointsSortedBy = criterion;
        }
    }
}
