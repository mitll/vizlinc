/*
 */
package edu.mit.ll.vizlinc.graph;

import com.google.common.io.Files;
import edu.mit.ll.vizlinc.concurrency.VizLincLongTask;
import edu.mit.ll.vizlinc.concurrency.VizLincLongTaskListener;
import edu.mit.ll.vizlinc.model.GraphOperationListener;
import edu.mit.ll.vizlinc.model.LocationValue;
import edu.mit.ll.vizlinc.model.PersonValue;
import edu.mit.ll.vizlinc.model.VLQueryListener;
import edu.mit.ll.vizlinc.ui.options.VizLincPanel;
import static edu.mit.ll.vizlinc.ui.options.VizLincPanel.PREF_GRAPH_LOCATION;
import edu.mit.ll.vizlinc.utils.UIUtils;
import edu.mit.ll.vizlincdb.document.Document;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import edu.mit.ll.vizlinc.components.FacetedSearchTopComponent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.NodeData;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.openord.OpenOrdLayout;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionController;
import org.gephi.partition.plugin.NodeColorTransformer;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.Interpolator;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.PageRank;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.opengl.text.FixedSizeMode;
import org.gephi.visualization.opengl.text.ScaledSizeMode;
import org.gephi.visualization.opengl.text.TextManager;
import org.gephi.visualization.opengl.text.TextModel;
import org.gephi.visualization.opengl.text.SizeMode;
import edu.mit.ll.vizlinc.components.GraphToolsTopComponent;
import edu.mit.ll.vizlinc.components.PropertiesTopComponent;
import edu.mit.ll.vizlinc.components.VLQueryTopComponent;
import edu.mit.ll.vizlinc.model.DBManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Manages the graph view
 */
public class GraphManager implements VLQueryListener {

    private static GraphManager singleton;
    // Strings describing what the graph is showing. Used for GraphToolsTopComponent.setGraphScope(), .setNodeSizeInfo(), .setNodeColorInfo().
    public static final String SCOPE_QUERY = "Social network narrowed to";  // Must append names of people.
    public static final String SCOPE_WHOLE_GRAPH = "Entire social network";
    public static final String SCOPE_ONE_HOP = "One hop from highlighted";
    public static final String SCOPE_TWO_HOPS = "Two hops from highlighted";
    public static final String SCOPE_MULTIPLE_HOPS = "Multiple hops from highlighted";
    public static final String NODE_INFO_CLUSTERS = "Clusters";
    public static final String NODE_INFO_PAGERANK = "PageRank";
    public static final String NODE_INFO_CENTRALITY = "Eigenvector Centrality";
    public static final String NODE_INFO_NOTHING = "";
    // Change the name of the graph file to reflect that it has had a layout algorithm applied.
    public static final String GRAPH_FILE_EXTENSION = ".graphml";
    public static final String LAYOUT_GRAPH_FILE_EXTENSION = ".openord" + GRAPH_FILE_EXTENSION;
    
    private GraphView visibleView = null;
    private GraphModel graphModel;
    private AttributeModel attributeModel;
    private Graph visibleGraph;
    private VizController vizController;
    private GraphToolsTopComponent graphToolsWin;
    private FacetedSearchTopComponent searchWin;
    private VLQueryTopComponent queryWin;
    private PropertiesTopComponent propWin;
    private boolean queryInProgress = false;
    private boolean graphOperationInProgress = false;
    private Set<GraphOperationListener> graphOperationListeners = new HashSet<GraphOperationListener>();
    // The set of highlighted nodes.
    private HashMap<Integer, HighlightInfo> facetIdToHighlightInfo = new HashMap<Integer, HighlightInfo>();
    private Integer lastHighlightedNodeId = null;
    private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    private List<PersonValue> personValues = new ArrayList<PersonValue>();
    private boolean singletonNodesRemoved = false;

    /**
     * Subscribe to notifications that graph operations are starting and
     * finishing.
     *
     * @param listener
     */
    public void addGraphOperationListener(GraphOperationListener listener) {
        this.graphOperationListeners.add(listener);
    }

    private void triggerAboutToPerformOperation() {
        for (GraphOperationListener listener : graphOperationListeners) {
            listener.aboutToPerformGraphOperation();
        }
    }

    private void triggerOperationFinished() {
        for (GraphOperationListener listener : graphOperationListeners) {
            listener.graphOperationFinished();
        }
    }

    public void resetColors(Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        for (Node node : visibleGraph.getNodes()) {
            node.getNodeData().setColor(r, g, b);
            node.getNodeData().setAlpha(1f);
        }
        for (Edge edge : visibleGraph.getEdges()) {
            edge.getEdgeData().setColor(-1f, g, b);
            edge.getEdgeData().setAlpha(1f);
        }

        // Color now signifies nothing;
        graphToolsWin.setGraphNodeColorInfo(NODE_INFO_NOTHING);
    }

    public void resetSizes(float size) {
        for (Node node : visibleGraph.getNodes()) {
            node.getNodeData().setSize(size);
        }

        // Size now signifies nothing;
        graphToolsWin.setGraphNodeSizeInfo(NODE_INFO_NOTHING);
    }

    public boolean computationInProgress() {
        return queryInProgress || graphOperationInProgress;
    }

    private void startComputation() {
        triggerAboutToPerformOperation();
    }

    private void stopComputation() {
        triggerOperationFinished();
    }

    private void layoutGraph(VizLincLongTaskListener taskListener) {
        final VizLincLongTask layoutTask = new VizLincLongTask("Laying out graph") {
            OpenOrdLayout layout = new OpenOrdLayout(null);  // OpenOrdLayoutBuilder is not needed and is supplied as null.

            @Override
            public void execute() {
                ProgressTicket progressTicket = this.getProgressTicket();
                startComputation();
                try {
                    layout.resetPropertiesValues();
                    layout.setGraphModel(graphModel);
                    layout.setProgressTicket(progressTicket);
                    Progress.start(progressTicket);
                    layout.initAlgo();
                    while (layout.canAlgo()) {
                        layout.goAlgo();
                    }
                    layout.endAlgo();
                } finally {
                    stopComputation();    // Even if cancelled.
                    // VizLincLongTask does a .finish() by itself, via VizLincLongTaskListener.
                    // Progress.finish(progressTicket);
                }
            }
        };
        layoutTask.run(taskListener);
    }


    private void exportGraph(String layoutGraphFileLocation) throws IOException {
        ExportController exportController = Lookup.getDefault().lookup(ExportController.class);
        exportController.exportFile(new File(layoutGraphFileLocation));
    }

    class HighlightInfo {

        public float originalSize;
        public float originalR, originalG, originalB;

        public HighlightInfo(float size, float r, float g, float b) {
            originalSize = size;
            originalR = r;
            originalG = g;
            originalB = b;
        }
    }

    public synchronized static GraphManager getInstance() {
        if (singleton == null) {
            singleton = new GraphManager();
        }
        return singleton;
    }

    GraphManager() {
        if(!DBManager.getInstance().isReady())
        {
            return;
        }
        //Register me as faceted search listener, so I will know when the search changes.
        final GraphManager self = this;
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                graphToolsWin = UIUtils.getGraphToolsWindow();
                queryWin = UIUtils.getQueryWindow();
                searchWin = UIUtils.getFacetedSearchWindow();
                propWin = UIUtils.getPropertiesWindow();
                if(searchWin != null)
                {
                    searchWin.addQueryListener(self);
                    //Init variable to people present in facet tree
                    personValues = searchWin.getPeopleInFacetTree();
                }

                // Try to synchronize with changes made in the vizController controls as well.
                // We can't do this perfectly.
                // The models that might change are both the vizModel and the text model.

                vizController = Lookup.getDefault().lookup(VizController.class);
                vizController.getVizModel().addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        String propertyName = evt.getPropertyName();
                        if (propertyName.equals("init")) {
                            resetVizSettings();
                        } else if (propertyName.equals("showEdges")) {
                            firePropertyChange("showEdges", evt.getOldValue(), evt.getNewValue());
                        }
                    }
                });

                vizController.getTextManager().getModel().addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent evt) {
                        // If user has forced all labels off, uncheck the "Show All Labels" checkbox.
                        TextModel model = (TextModel) evt.getSource();
                        if (!model.isShowEdgeLabels()) {
                            firePropertyChange("showAllLabels", null, false);
                        }
                    }
                });
            }
        });


    }

    public int getNodeId(Node node) {
        return Integer.parseInt(node.getNodeData().getId());
    }

    public AttributeColumn addAttributeColumn(String name, String title, AttributeType type, AttributeOrigin origin, Object defaultValue) {
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn column = nodeTable.getColumn(name);
        if (nodeTable.getColumn(name) == null) {
            column = nodeTable.addColumn(name, title, type, origin, defaultValue);
        }
        return column;
    }

    /**
     * Add or remove the given people to/from the selection.
     * @param nodes
     * @param select true to add, false to remove
     */
    public void adjustSelectionOfPeople(List<Node> nodes, boolean select) {
        List<Integer> nodeIds = new ArrayList<Integer>(nodes.size());
        for (Node node: nodes) {
            nodeIds.add(getNodeId(node));
        }
        searchWin.adjustSelectionOfPeopleWithIds(nodeIds, select);
    }

    public void addNodeToQuery(Node node) {
        searchWin.addPersonWithIdToQuery(getNodeId(node));
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    public void firePropertyChange(String propertyName, Object oldvalue, Object newValue) {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldvalue, newValue);
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(evt);
        }
    }

    public void openGraph() throws FileNotFoundException, IOException {
        // Similar to http://forum.gephi.org/viewtopic.php?f=27&t=230#p803
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get models and controllers for this new workspace - will be useful later
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        //Import file
        Container container;
        final Preferences pref = NbPreferences.forModule(VizLincPanel.class);
        final String graphLocation = pref.get(VizLincPanel.PREF_GRAPH_LOCATION, "");
        container = importController.importFile(new File(graphLocation));
        
        container.getLoader().setEdgeDefault(EdgeDefault.UNDIRECTED);
        // Append imported data to GraphAPI.
        importController.process(container, new DefaultProcessor(), workspace);

        // Fetch the graph model for future use.
        attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        if (attributeModel == null) {
            System.out.println("attributeModel null !!");
        }
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        if (graphModel == null) {
            System.out.println("graphModel null !!");
        }

        displayWholeGraph();
        
        if (!graphLocation.endsWith(LAYOUT_GRAPH_FILE_EXTENSION)) {
            layoutGraph(new VizLincLongTaskListener() {
                @Override
                public void whenDone() {
                    String layoutGraphFileLocation = graphLocation.replace(GRAPH_FILE_EXTENSION, LAYOUT_GRAPH_FILE_EXTENSION);
                    try {
                        exportGraph(layoutGraphFileLocation);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    pref.put(PREF_GRAPH_LOCATION, layoutGraphFileLocation);
                    // Zoom to show whole graph.
                    vizController.getGraphIO().centerOnGraph();
                }
            });
        }
    }
    
    private void resetVizSettings() {
        setSimpleSelectionMode();
        setShowEdges(false);
        setShowAllLabels(false);
    }

    /**
     * Set a non-flashing, low-redisplay selection mode: no autoselection of
     * neighbors; no lightening of non-selected nodes
     */
    public void setSimpleSelectionMode() {
        VizModel vizModel = vizController.getVizModel();
        vizModel.setAutoSelectNeighbor(false);
        vizModel.setLightenNonSelectedAuto(false);
    }

    public void setFixedLabelSize(boolean fixed) {
        TextManager textManager = vizController.getTextManager();
        TextModel textModel = textManager.getModel();
        // Default is 0.5, but that's pretty big. We could change the font size instead,
        // but it doesn't really matter.
        textModel.setNodeSizeFactor(fixed ? 0.2f : 0.5f);
        // Find and use the FixedSizeMode setting, assuming it exists.
        for (SizeMode sizeMode : textManager.getSizeModes()) {
            if ((fixed && sizeMode instanceof FixedSizeMode)
                    || (!fixed && sizeMode instanceof ScaledSizeMode)) {
                textModel.setSizeMode(sizeMode);
                break;
            }
        }
        firePropertyChange("fixedLabelSize", null, fixed);
    }

    public void setShowEdges(boolean show) {
        boolean oldShow = getShowEdges();
        vizController.getVizModel().setShowEdges(show);
        firePropertyChange("showEdges", oldShow, show);
    }

    public boolean getShowEdges() {
        return vizController.getVizModel().isShowEdges();
    }

    public void setShowAllLabels(boolean show) {
        boolean oldShow = getShowAllLabels();
        TextManager textManager = vizController.getTextManager();
        TextModel textModel = textManager.getModel();
        textModel.setShowNodeLabels(true);
        textModel.setSelectedOnly(!show);
        // Don't use a fixed label size when showing all labels.
        setFixedLabelSize(!show);
        firePropertyChange("showAllLabels", oldShow, show);
    }

    public boolean getShowAllLabels() {
        TextModel textModel = vizController.getTextManager().getModel();
        return textModel.isShowNodeLabels() & !textModel.isSelectedOnly();
    }

    public void centerOnLastHighlighted() {
        if (lastHighlightedNodeId != null) {
            vizController.getSelectionManager().centerOnNode(visibleGraph.getNode(Integer.toString(lastHighlightedNodeId)));
        }
    }

    public void removeSingletonNodes() {
        for (Node node : visibleGraph.getNodes().toArray()) {
            if (visibleGraph.getDegree(node) == 0) {
                visibleGraph.removeNode(node);
            }
            singletonNodesRemoved = true;
        }
    }

    public boolean singletonNodesRemoved() {
        return singletonNodesRemoved;
    }

    /**
     * Get a new visible view for the graph and get its graph. Destroy any
     * previous view to free up its storage. We leave the main view alone.
     */
    public void displayWholeGraph() {
        if (visibleView != null) {
            graphModel.destroyView(visibleView);
        }
        visibleView = graphModel.newView();
        visibleGraph = graphModel.getGraph(visibleView);
        graphModel.setVisibleView(visibleView);
        singletonNodesRemoved = false;
        setGraphScope(SCOPE_WHOLE_GRAPH);
    }

    void setGraphScope(final String scope) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                UIUtils.getGraphToolsWindow().setGraphScope(scope, visibleGraph.getNodeCount());
            }
        });
    }

    /**
     * Reset visible view to last query.
     */
    public void displayQueryGraph() {
        displayQueryGraph(personValues);
    }

    /**
     * Displays the social network graph of all person entities that are
     * displayed in the faceted search. Two person nodes are connected by an
     * edge iff they co-occur in any document.
     *
     * @param personValues the people in all the documents selected in the
     * current faceted search
     */
     public void displayQueryGraph(List<PersonValue> personValuesIn) {
        // Remember the query so we can restore it later if requested.
        personValues = new ArrayList<PersonValue>(personValuesIn);
        // Get the vertex ids for the selected people.
        HashSet<Integer> personIds = new HashSet<Integer>();
        for (PersonValue pv : personValues) {
            personIds.add(pv.getId());
        }

        displayWholeGraph();

        // Filter visible subgraph.
        for (Node n : visibleGraph.getNodes().toArray()) {
            // Remove any nodes that are not in the narrowed set of people.
            if (!personIds.contains((Integer) n.getNodeData().getAttributes().getValue("intid"))) {
                visibleGraph.removeNode(n);
            }
        }

        // Update the graph status. If there's nothing in the query, return right away,
        // so that the graph status will be the whole graph.
        List<PersonValue> personValues = queryWin.getPersonFilters();
        if (personValues.isEmpty()) {
            return;
        }
        List<String> names = new ArrayList<String>();
        int count = 0;
        final int NAME_LIMIT = 3;

        for (PersonValue personValue : personValues) {
            count++;
            if (count > NAME_LIMIT) {
                names.add("...");
                break;
            } else {
                names.add(personValue.getText());
            }
        }

        setGraphScope(SCOPE_QUERY + " (" + StringUtils.join(names, " & ") + ")");

    }

    public boolean isHighlighted(PersonValue pv) {
        return isHighlighted(pv.getId());
    }

    public boolean isHighlighted(int nodeId) {
        return facetIdToHighlightInfo.containsKey(nodeId);
    }

    /**
     * Highlight the given people. Unhighlight anyone else.
     * @param people
     */
    public void highlightPeople(final Collection<PersonValue> people) {
        // Convert the set of people to a set of facet ids.
        Set<Integer> highlightTheseIds = new HashSet<Integer>();
        for (PersonValue pv : people) {
            highlightTheseIds.add(pv.getId());
        }
        
        // Unhighlight any nodes not in the new set.
        // Since we're changing the set on the fly, copy the keys before iterating.
        for (int facetId : facetIdToHighlightInfo.keySet().toArray(new Integer[facetIdToHighlightInfo.size()])) {
            if (!highlightTheseIds.contains(facetId)) {
                highlightNodeWithId(facetId, false);
            }
        }
        
        // Now highlight any nodes that are not yet highlighted.
        for (int facetId : highlightTheseIds) {
            highlightNodeWithId(facetId, true);
        }
        
        //Show properties of highlighted nodes in Properties window
        propWin.showPersonProperties(people);
    }
    
    
    public boolean highlightPerson(PersonValue pv, boolean highlight) {
        int facetId = pv.getId();
        return highlightNodeWithId(facetId, highlight);
    }

    private boolean highlightNodeWithId(int facetId, boolean highlight) {
        Node node = visibleGraph.getNode(Integer.toString(facetId));
        if (node == null) {
            return false;
        }

        if (highlight == facetIdToHighlightInfo.containsKey(facetId)) {
            // No change: already highlighted or unhighlighted.
            return true;
        }

        NodeData nodeData = node.getNodeData();
        if (highlight) {
            // Remember the current size and color of the node to be highlighted.
            float size = nodeData.getSize();
            facetIdToHighlightInfo.put(facetId, new HighlightInfo(size, nodeData.r(), nodeData.g(), nodeData.b()));
            lastHighlightedNodeId = facetId;
            nodeData.setSize(size * 10.0f);
            // System.out.format("x: %f, y: %f, z: %f\n", nodeData.x(), nodeData.y(), nodeData.z());
            nodeData.setColor(0.0f, 0.0f, 1.0f);
            // nodeData.setZ(-100.0f);
        } else {
            // Restore the size and color of the highlighted node.
            lastHighlightedNodeId = null;
            HighlightInfo highlightInfo = facetIdToHighlightInfo.get(facetId);
            nodeData.setSize(highlightInfo.originalSize);
            nodeData.setColor(highlightInfo.originalR, highlightInfo.originalG, highlightInfo.originalB);
            facetIdToHighlightInfo.remove(facetId);
        }
        return true;
    }

    public void unhighlightAll() {
        // Copy the key set since it will change as each node is unhighlighted.
        for (int id : facetIdToHighlightInfo.keySet().toArray(new Integer[facetIdToHighlightInfo.size()])) {
            highlightNodeWithId(id, false);
        }
    }

    private Set<Node> highlightedNodes() {
        Set<Node> nodes = new HashSet<Node>();
        for (int facetId : facetIdToHighlightInfo.keySet()) {
            Node node = visibleGraph.getNode(Integer.toString(facetId));
            if (node != null) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    @Override
    public void aboutToExecuteQuery() {
        queryInProgress = true;
    }

    @Override
    public void queryFinished(List<Document> documents, List<LocationValue> locationsInFacetTree, List<PersonValue> peopleInFacetTree) {
        queryInProgress = false;
        displayQueryGraph(peopleInFacetTree);
    }
    static public final String CLUSTER = "CLUSTER";

    /**
     * Clustering, invoked from GraphToolsTopComponent.
     *
     * @param lambda Infomap cluster parameter (as added by Bill and Stephen).
     */
    public void cluster(final int trials, final double lambda) {
        final File tempDir = Files.createTempDir();
        final File tempNetFile = new File(tempDir, "visiblegraph.net");
        final File tempCluFile = new File(tempDir, "visiblegraph.clu");

        final VizLincLongTask clusteringTask = new VizLincLongTask("Clustering") {
            boolean cancelled = false;

            boolean cancelled() {
                return cancelled;
            }

            @Override
            public boolean cancel() {
                cancelled = true;
                return cancelled;
            }

            String[] exportToPajek(File netFile) throws Exception {
                Progress.switchToIndeterminate(getProgressTicket());
                Writer writer = new BufferedWriter(new FileWriter(netFile));
                int nodeCount = visibleGraph.getNodeCount();
                int pajekNodeNum = 1;
                HashMap<String, Integer> nodeIdStringToIndex = new HashMap<String, Integer>(nodeCount);
                // index is 1-based.
                String[] indexToNodeIdString = new String[nodeCount + 1];

                visibleGraph.readLock();

                writer.append("*Vertices " + nodeCount + "\n");
                for (Node node : visibleGraph.getNodes()) {
                    writer.append(Integer.toString(pajekNodeNum));
                    // Label with blueprints node id, not text label
                    String nodeIdString = node.getNodeData().getId();
                    writer.append(" \"");
                    writer.append(nodeIdString);
                    writer.append("\"\n");
                    nodeIdStringToIndex.put(nodeIdString, pajekNodeNum); // assigns Ids from the interval [1..max]
                    indexToNodeIdString[pajekNodeNum] = nodeIdString;
                    pajekNodeNum++;
                }

                writer.append("*Edges\n");
                for (Edge edge : visibleGraph.getEdges()) {
                    if (edge != null) {
                        writer.append(Integer.toString(nodeIdStringToIndex.get(edge.getSource().getNodeData().getId())));
                        writer.append(" ");
                        writer.append(Integer.toString(nodeIdStringToIndex.get(edge.getTarget().getNodeData().getId())));
                        writer.append("\n");
                    }
                }

                writer.close();
                visibleGraph.readUnlockAll();
                return indexToNodeIdString;
            }

            void infomap(File netFile, int trials, double lambda) throws IOException {

                Progress.setDisplayName(getProgressTicket(), "Clustering: running Infomap ...");

                String os = System.getProperty("os.name");
                String infomapExecutablePath;
                if (os.startsWith("Windows")) {
                    infomapExecutablePath = "executables/win32/infomap.exe";
                    //} else if (os.startsWith("Linux")) {
                    //    infomapExecutablePath = "executables/linux/infomap";
                } else {
                    JOptionPane.showMessageDialog(graphToolsWin, "Unsupported OS for clustering: " + os);
                    return;
                }
                File infomapExecutableFile = InstalledFileLocator.getDefault().locate(infomapExecutablePath, "org-leads_demo", false);
                ProcessBuilder pb = new ProcessBuilder(
                        infomapExecutableFile.getAbsolutePath(), // infomap executable
                        "123456", // random seed to start
                        netFile.getAbsolutePath(), // input graph
                        Integer.toString(trials),
                        Double.toString(lambda));
                System.out.println(pb.command());
                pb.redirectErrorStream(true);
                Process proc = pb.start();
                // Read output produced by infoamp to be able to show progress.
                BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String line;
                Progress.switchToDeterminate(getProgressTicket(), trials);
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    // Check for cancel request as we read input from Infomap. Each line of output can take significant time.
                    if (cancelled()) {
                        reader.close();
                        return;
                    }
                    // Push the progress bar after the first attempt
                    if (line.startsWith("Attempt") && !line.startsWith("Attempt 1/")) {
                        Progress.progress(getProgressTicket());
                    }
                }
                Progress.progress(getProgressTicket());

            }

            void setAttributes(File cluFile, String[] indexToNodeIdString) throws FileNotFoundException, IOException {
                Progress.setDisplayName(this.getProgressTicket(), "Clustering: displaying results ...");
                Progress.switchToIndeterminate(getProgressTicket());

                // Add the CLUSTER attribute to the graph if it isn't already there.
                addAttributeColumn(CLUSTER, "cluster", AttributeType.INT, AttributeOrigin.COMPUTED, null);

                Map<String, Integer> nodeIdStringToCluster = new HashMap<String, Integer>(indexToNodeIdString.length);
                // For possible future use: TreeMap<Integer, Color> clusterToColor = new TreeMap<Integer, Color>();

                BufferedReader reader = new BufferedReader(new FileReader(cluFile));
                int nodeIndex = 1;
                String line;
                // Skip the first line, which is "*Vertices <number of vertices>"
                // Each following line is a cluster number for the node index.
                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    int cluster = Integer.parseInt(line);
                    String nodeIdString = indexToNodeIdString[nodeIndex];
                    // Remember the cluster number in the graph.
                    visibleGraph.getNode(nodeIdString).getAttributes().setValue(CLUSTER, cluster);
                    nodeIdStringToCluster.put(nodeIdString, cluster);

                    nodeIndex++;
                }
                reader.close();

                // Now color the nodes based on cluster numbers.
                PartitionController partitionController = Lookup.getDefault().lookup(PartitionController.class);
                Partition p = partitionController.buildPartition(attributeModel.getNodeTable().getColumn(CLUSTER), visibleGraph);
                NodeColorTransformer nodeColorTransformer = new NodeColorTransformer();
                // Choose a disparate set of colors that is not random.
                UIUtils.setPartitionColors(nodeColorTransformer, p);
                // nodeColorTransformer.randomizeColors(p);
                partitionController.transform(p, nodeColorTransformer);
            }

            @Override
            public void execute() {
                startComputation();
                try {
                    Progress.setDisplayName(this.getProgressTicket(), "Clustering: preparing graph file ...");
                    Progress.start(this.getProgressTicket());

                    // Don't include singleton nodes in the cluster finding.
                    removeSingletonNodes();

                    String[] indexToNodeIdString = exportToPajek(tempNetFile);
                    if (cancelled()) {
                        return;
                    }
                    infomap(tempNetFile, trials, lambda);
                    if (cancelled()) {
                        return;
                    }
                    setAttributes(tempCluFile, indexToNodeIdString);
                    graphToolsWin.setGraphNodeColorInfo(NODE_INFO_CLUSTERS);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(graphToolsWin, "Clustering failed: " + ex);
                } finally {
                    FileUtils.deleteQuietly(tempDir);  // Delete dir and contents.
                    stopComputation();    // Even if cancelled.
                }
            }
        };

        clusteringTask.run();
    }

    /**
     * PageRank, invoked from GraphToolsTopComponent.
     */
    public void pageRank(final boolean showBySize, final boolean showByColor) {
        final VizLincLongTask pageRankTask = new VizLincLongTask("Running PageRank") {
            PageRank pageRank = new PageRank();
            boolean cancelled = false;

            @Override
            public boolean cancel() {
                cancelled = pageRank.cancel();
                return cancelled;
            }

            @Override
            public void execute() {
                startComputation();
                try {
                    visibleGraph.readLock();

                    // Run PageRank, which works on the visible view.
                    // pageRank will take care of progress display.
                    pageRank.setProgressTicket(this.getProgressTicket());
                    pageRank.setDirected(false);
                    pageRank.execute(graphModel, attributeModel);
                    if (cancelled) {
                        return;
                    }

                    // Now visualize the rank.
                    RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
                    // Vary over the visible graph, not the whole graph.
                    rankingController.setUseLocalScale(true);
                    // S-shaped interpolator: exaggerate differences near extrema.
                    rankingController.setInterpolator(new Interpolator.BezierInterpolator(1.0f, 0.0f, 0.0f, 1.0f));
                    Ranking pageRankRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, PageRank.PAGERANK);

                    if (showBySize) {
                        AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
                        sizeTransformer.setMinSize(4.0f);
                        sizeTransformer.setMaxSize(20.0f);
                        rankingController.transform(pageRankRanking, sizeTransformer);
                        graphToolsWin.setGraphNodeSizeInfo(NODE_INFO_PAGERANK);

                    }

                    if (showByColor) {
                        AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
                        colorTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
                        rankingController.transform(pageRankRanking, colorTransformer);
                        graphToolsWin.setGraphNodeColorInfo(NODE_INFO_PAGERANK);

                    }

                    visibleGraph.readUnlockAll();
                } finally {
                    stopComputation();    // Even if cancelled.
                }
            }
        };

        pageRankTask.run();
    }

    /**
     * Centrality, invoked from GraphToolsTopComponent.
     */
    public void centrality(final boolean showBySize, final boolean showByColor) {
        final VizLincLongTask centralityTask = new VizLincLongTask("Running Centrality") {
            EigenvectorCentrality centrality = new EigenvectorCentrality();
            boolean cancelled = false;

            @Override
            public boolean cancel() {
                cancelled = centrality.cancel();
                return cancelled;
            }

            @Override
            public void execute() {
                startComputation();
                try {
                    visibleGraph.readLock();

                    // Centrality runs on the visible view.
                    // centrality will take care of displaying progress.
                    centrality.setProgressTicket(this.getProgressTicket());
                    centrality.setDirected(false);
                    centrality.execute(graphModel, attributeModel);
                    if (cancelled) {
                        return;
                    }

                    // Now visualize the centrality.
                    RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
                    // Vary over the visible graph, not the whole graph.
                    rankingController.setUseLocalScale(true);
                    // S-shaped interpolator: exaggerate differences near extrema.
                    rankingController.setInterpolator(new Interpolator.BezierInterpolator(1.0f, 0.0f, 0.0f, 1.0f));
                    Ranking centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, EigenvectorCentrality.EIGENVECTOR);

                    if (showBySize) {
                        AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
                        sizeTransformer.setMinSize(4.0f);
                        sizeTransformer.setMaxSize(20.0f);
                        rankingController.transform(centralityRanking, sizeTransformer);
                        graphToolsWin.setGraphNodeSizeInfo(NODE_INFO_CENTRALITY);
                    }

                    if (showByColor) {
                        AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
                        colorTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
                        rankingController.transform(centralityRanking, colorTransformer);
                        graphToolsWin.setGraphNodeColorInfo(NODE_INFO_CENTRALITY);
                    }

                    visibleGraph.readUnlockAll();
                } finally {
                    stopComputation();    // Even if cancelled.
                }
            }
        };
        centralityTask.run();
    }

    /**
     * limit display to n hops. Invoked from GraphToolsTopComponent.
     */
    public void displayNHops(final int hops) {
        // Reset to the whole query graph in case we have already narrowed the graph.
        displayQueryGraph();

        final VizLincLongTask oneHopTask = new VizLincLongTask("Running n hops") {
            NHops nHops = new NHops();
            boolean cancelled = false;

            @Override
            public boolean cancel() {
                cancelled = nHops.cancel();
                return cancelled;
            }

            @Override
            public void execute() {
                startComputation();
                try {
                    nHops.setProgressTicket(this.getProgressTicket());
                    Set<Node> highlightedNodes = highlightedNodes();
                    if (highlightedNodes.isEmpty()) {
                        JOptionPane.showMessageDialog(graphToolsWin, "Select one or more people in the social network graph.");
                        return;
                    }
                    nHops.execute(visibleGraph, highlightedNodes(), hops);
                    String scope = SCOPE_MULTIPLE_HOPS;
                    switch (hops) {
                        case 1:
                            scope = SCOPE_ONE_HOP;
                            break;
                        case 2:
                            scope = SCOPE_TWO_HOPS;
                            break;
                    }
                    setGraphScope(scope);
                } finally {
                    stopComputation();    // Even if cancelled.
                }
            }
        };
        oneHopTask.run();
    }
}
