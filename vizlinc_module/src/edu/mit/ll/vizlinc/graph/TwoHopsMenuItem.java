package edu.mit.ll.vizlinc.graph;

import java.util.Arrays;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class TwoHopsMenuItem extends VizLincGraphContextMenuItem {

    @Override
    public void execute() {
        // canExecute() will restrict nodes to be a single-element array.
        GraphManager.getInstance().adjustSelectionOfPeople(Arrays.asList(nodes), true);    // true = select
        GraphManager.getInstance().displayNHops(2);
    }

    @Override
    public String getName() {
        return "2 Hops from Node";
    }

    @Override
    public boolean canExecute() {
        // One do two hops from a single node
        return super.canExecute() && nodes.length == 1;
    }

    @Override
    public int getPosition() {
        return 40;
    }
}