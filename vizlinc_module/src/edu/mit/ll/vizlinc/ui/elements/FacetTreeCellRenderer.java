/*
 */
package edu.mit.ll.vizlinc.ui.elements;

import edu.mit.ll.vizlinc.model.PersonValue;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Custom cell renderer for facet tree
 */
public class FacetTreeCellRenderer extends DefaultTreeCellRenderer
{
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
        if(value instanceof PersonValue)
        {
        String createdBy = ((PersonValue) value).getPersonEntity().getCreatedBy();
        if(createdBy.equals("weak_across_doc_person_coref"))
        {
            boolean x = true;
        }
        }
       return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    }
}