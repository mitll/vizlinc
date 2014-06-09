package edu.mit.ll.vizlinc.graph;

import java.util.Arrays;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.lookup.ServiceProvider;

/**
 * Menu item to remove the effect of highlighting nodes in the graph.
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class UnhighlightNodeMenuItem extends  VizLincGraphContextMenuItem {

    @Override
    public void execute() {
        GraphManager.getInstance().adjustSelectionOfPeople(Arrays.asList(nodes), false);  // false = unselect
    }

    @Override
    public String getName() {
        return nodes.length > 1 ? "Unhighlight All Selected" : "Unhighlight";
    }

    @Override
    public boolean canExecute() {
        if (!super.canExecute()) {
            return false;
        }
        boolean noneHighlighted = true;
        for (Node node: nodes) {
            if (GraphManager.getInstance().isHighlighted(Integer.parseInt(node.getNodeData().getId()))) {
                noneHighlighted = false;
                break;
            }
        }
        return !noneHighlighted && nodes.length > 0;
    }

    @Override
    public int getPosition() {
        return 10;
    }

}
