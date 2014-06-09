package edu.mit.ll.vizlinc.graph;

import edu.mit.ll.vizlinc.utils.UIUtils;
import javax.swing.JOptionPane;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class AddToQueryMenuItem extends VizLincGraphContextMenuItem {

        @Override
    public void execute() {
        if (nodes.length > 1) {
            // This should not happen, based on canExecute(), but check anyway.
            JOptionPane.showMessageDialog(UIUtils.getGraphWindow(), "Please select only one node to add to the query.");
            
        }
        GraphManager.getInstance().addNodeToQuery(nodes[0]);
    }

    @Override
    public String getName() {
        return "Add Node to Query";
    }

    @Override
    public boolean canExecute() {
        // Add exactly one node.
        return  super.canExecute() && nodes.length == 1;
    }

    @Override
    public int getPosition() {
        return 20;
    }
}
