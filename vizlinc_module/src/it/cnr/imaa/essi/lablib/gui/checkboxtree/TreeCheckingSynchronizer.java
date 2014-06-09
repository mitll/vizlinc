package it.cnr.imaa.essi.lablib.gui.checkboxtree;

import javax.swing.tree.TreePath;

public class TreeCheckingSynchronizer implements TreeCheckingListener {

    protected TreeCheckingModel model1;

    protected TreeCheckingModel model2;

    public TreeCheckingSynchronizer(CheckboxTree tree1, CheckboxTree tree2) {

	this.model1 = tree1.getCheckingModel();
	this.model2 = tree2.getCheckingModel();

	tree1.addTreeCheckingListener(this);
	tree2.addTreeCheckingListener(this);
    }

    public void valueChanged(TreeCheckingEvent e) {

	Object source = e.getSource();
	TreePath leadingPath = e.getPath();

	boolean checked = e.isCheckedPath();

	TreeCheckingModel dest = source.equals(model1) ? model2 : model1;

	if (checked) {
	    if (!dest.isPathChecked(leadingPath)) {
		dest.addCheckingPath(leadingPath);
	    }
	} else {
	    if (dest.isPathChecked(leadingPath)) {
		dest.removeCheckingPath(leadingPath);
	    }
	}
    }
}
