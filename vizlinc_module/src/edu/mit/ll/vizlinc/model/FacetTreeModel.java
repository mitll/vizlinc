/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlincdb.document.Document;
import java.util.List;
import java.util.Vector;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Tree model for facet trees
 */
public class FacetTreeModel implements TreeModel
{

    private FacetTreeRoot root;
    private Vector<TreeModelListener> treeModelListeners;
    public static final int SORT_ALPHA = 0;
    public static final int SORT_MENTIONS = 1;
    public static int SORT_DOC = 2;
    private boolean showWeakAcrossDocPeople;

    public FacetTreeModel(String s)
    {
        root = new FacetTreeRoot(s);
        initMembers();
    }

    public FacetTreeModel(int sort)
    {
        root = new FacetTreeRoot(sort);
        initMembers();
    }

    public FacetTreeModel(List<Document> docList, int sort, NeighborhoodConfig neighConfig)
    {
        root = new FacetTreeRoot(docList, sort, neighConfig);
        initMembers();
    }

    private void initMembers()
    {
        treeModelListeners = new Vector<TreeModelListener>();
        showWeakAcrossDocPeople = true;
    }

    @Override
    public Object getRoot()
    {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index)
    {
        if (parent instanceof FacetTreeRoot)
        {
            FacetTreeRoot root = (FacetTreeRoot) parent;
            return root.getFacets().get(index);
        }

        if (parent instanceof Facet)
        {
            if (parent instanceof Person && !showWeakAcrossDocPeople)
            {
                Person p = (Person) parent;
                return p.getStrongAcrossDocPeople().get(index);
            } 
            else
            {
                Facet facet = (Facet) parent;
                //TODO for now this is a read-only list but should be a list that  
                //can be sorted alphabetically or by mention count depending on user 
                //choice.
                return facet.getFacetValues().get(index);
            }
        }

        return null;
    }

    @Override
    public int getChildCount(Object parent)
    {
        if (parent instanceof FacetTreeRoot)
        {
            FacetTreeRoot root = (FacetTreeRoot) parent;
            return root.getFacetCount();
        }

        if (parent instanceof Facet)
        {
            if (parent instanceof Person && !showWeakAcrossDocPeople)
            {
                return ((Person) parent).getNumStrongAcrossDocPeople();
            } else
            {
                Facet f = (Facet) parent;
                return f.getFacetValues().size();
            }
        }

        return 0;
    }

    @Override
    public boolean isLeaf(Object node)
    {
        return (getChildCount(node) == 0);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getIndexOfChild(Object parent, Object child)
    {
        if (parent == null || child == null)
        {
            return -1;
        }

        if (parent instanceof FacetTreeRoot)
        {
            return ((FacetTreeRoot) parent).getFacets().indexOf(child);
        }

        if (parent instanceof Facet)
        {
            if (parent instanceof Person && !showWeakAcrossDocPeople)
            {
                return ((Person) parent).getStrongAcrossDocPeople().indexOf(child);
            } else
            {
                return ((Facet) parent).getFacetValues().indexOf(child);
            }
        }

        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l)
    {
        treeModelListeners.addElement(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l)
    {
        treeModelListeners.removeElement(l);
    }

    public void setFacetValueOrder(int sort)
    {
        if (sort == SORT_ALPHA || sort == SORT_MENTIONS || sort == SORT_DOC)
        {
            this.root.changeSortCriterion(sort);
            fireTreeStructureChanged(this.root);
        }
    }
    
    /**
     * 
     * @param showWeak if true, weakly disambiguated  person entities will be shown
     *                 if false, weakly disambiguated  person entities will be hidden in facet tree
     */
    public void setShowWeakAcrossDocPeople(boolean showWeak)
    {
        this.showWeakAcrossDocPeople = showWeak;
        Person person = (Person)root.getFacetWithName(Person.PERSON_FACET_NAME);
        person.setShowingWeakAcrossDoc(showWeak); //TODO: Duplicate flag is redundant
        fireTreeStructureChanged(this.root);
    }

    /**
     * The only event raised by this model is TreeStructureChanged with the root
     * as path, i.e. the whole tree has changed.
     */
    protected void fireTreeStructureChanged(FacetTreeRoot root)
    {
        String threadName = Thread.currentThread().getName();
        String s = ("Notifying " + treeModelListeners.size() + " listeners from thread: " + threadName);
        //JOptionPane.showMessageDialog(null, s);
        TreeModelEvent e = new TreeModelEvent(this, new Object[]
                {
                    root
                });
        for (TreeModelListener tml : treeModelListeners)
        {
            tml.treeStructureChanged(e);
        }
    }
}