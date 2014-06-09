package edu.mit.ll.vizlinc.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author pjmcswee
 */
public class NHops implements LongTask {

    private ProgressTicket progress;
    private boolean isCanceled;

    public NHops() {
    }

    public void execute(Graph graph, Collection<Node> startNodes, int hops) {
        Progress.start(progress, hops+1);
        
        Set<Node> closeNodes = new HashSet<Node>(startNodes);

        // Add nodes that are one hop away from the current set of qualifying nodes. Keep repeating this for n hops.
        for (int i = 0; i < hops; i++) {
            for (Node node : closeNodes.toArray(new Node[closeNodes.size()])) {
                for (Node neighbor : graph.getNeighbors(node)) {
                    closeNodes.add(neighbor);
                }
            }
            Progress.progress(progress);
        }

       if (isCanceled) return;
       
       // Now remove all nodes that are not in the qualifying set.
        for (Node graphNode : graph.getNodes().toArray()) {
            if (!closeNodes.contains(graphNode)) {
                graph.removeNode(graphNode);
            }
        }
        
        Progress.finish(progress);
    }

    @Override
    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;

    }
}
