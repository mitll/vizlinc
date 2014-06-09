package edu.mit.ll.vizlinc.graph;

import edu.mit.ll.vizlinc.model.GraphOperationListener;
import edu.mit.ll.vizlinc.model.LocationValue;
import edu.mit.ll.vizlinc.model.PersonValue;
import edu.mit.ll.vizlinc.model.VLQueryListener;
import edu.mit.ll.vizlinc.utils.UIUtils;
import edu.mit.ll.vizlincdb.document.Document;
import java.util.List;
import javax.swing.Icon;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.windows.WindowManager;


public abstract class VizLincGraphContextMenuItem implements GraphContextMenuItem, GraphOperationListener, VLQueryListener {

    protected Node[] nodes;
    protected HierarchicalGraph graph;
    // Turn off when other operations are happening.
    protected boolean disabled = false;

    @Override
    public void setup(HierarchicalGraph graph, Node[] nodes) {
        this.nodes = nodes;
        this.graph = graph;
        
        final VizLincGraphContextMenuItem self = this;
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                GraphManager.getInstance().addGraphOperationListener(self);
                UIUtils.getFacetedSearchWindow().addQueryListener(self);
            }
        });
    }

    @Override
    public boolean canExecute() {
        return !disabled;
    }

    @Override
    public int getType() {
        // -1 makes these items be above all the rest.
        return -1;
    }

    @Override
    public ContextMenuItemManipulator[] getSubItems() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Integer getMnemonicKey() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public ManipulatorUI getUI() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public void aboutToPerformGraphOperation() {
        disabled = true;
    }

    @Override
    public void graphOperationFinished() {
        disabled = false;
    }

    @Override
    public void aboutToExecuteQuery() {
        disabled = true;
    }

    @Override
    public void queryFinished(List<Document> documents, List<LocationValue> locationsInFacetTree, List<PersonValue> peopleInFacetTree) {
        disabled = false;
    }

}
