package edu.mit.ll.vizlinc.graph;

import java.util.Arrays;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class HighlightNodeMenuItem extends VizLincGraphContextMenuItem {

    @Override
    public void execute() {
        GraphManager.getInstance().adjustSelectionOfPeople(Arrays.asList(nodes), true);    // true = select
    }

    @Override
    public String getName() {
        return (nodes.length > 1 ? "Highlight All Selected" : "Highlight");
    }

    @Override
    public boolean canExecute() {
        if (!super.canExecute()) {
            return false;
        }
        boolean allHighlighted = true;
        for (Node node: nodes) {
            if (!GraphManager.getInstance().isHighlighted(Integer.parseInt(node.getNodeData().getId()))) {
                allHighlighted = false;
                break;
            }
        }
        return !allHighlighted && nodes.length > 0;
    }

    @Override
    public int getPosition() {
        return 0;
    }
}