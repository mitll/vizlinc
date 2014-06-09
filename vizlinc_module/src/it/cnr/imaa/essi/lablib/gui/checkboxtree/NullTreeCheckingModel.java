/*
 * Copyright 2007-2010 Enrico Boldrini, Lorenzo Bigagli This file is part of
 * CheckboxTree. CheckboxTree is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version. CheckboxTree is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * General Public License along with CheckboxTree; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA
 */
package it.cnr.imaa.essi.lablib.gui.checkboxtree;

import javax.swing.tree.TreePath;

/**
 * Convenience class representing an empty tree checking model (cf. the Null
 * Object pattern), whose paths are always enabled, unchecked and ungreyed. This
 * class is a singleton.
 * 
 * @author Lorenzo Bigagli
 */
public class NullTreeCheckingModel implements TreeCheckingModel {

    private final static NullTreeCheckingModel singleton;

    static {
	singleton = new NullTreeCheckingModel();
    }

    private NullTreeCheckingModel() {
    }

    public static NullTreeCheckingModel getInstance() {
	return singleton;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#addCheckingPath
     * (javax.swing.tree.TreePath)
     */
    public void addCheckingPath(TreePath path) {
	// nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#addCheckingPaths
     * (javax.swing.tree.TreePath[])
     */
    public void addCheckingPaths(TreePath[] paths) {
	// nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     * 
     * @seeit.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#
     * addTreeCheckingListener
     * (it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener)
     */
    public void addTreeCheckingListener(TreeCheckingListener tcl) {
	// nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#clearChecking
     * ()
     */
    public void clearChecking() {
	// nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#getCheckingMode
     * ()
     */
    public CheckingMode getCheckingMode() {
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#getCheckingPaths
     * ()
     */
    public TreePath[] getCheckingPaths() {
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#getCheckingRoots
     * ()
     */
    public TreePath[] getCheckingRoots() {
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#getGreyingPaths
     * ()
     */
    public TreePath[] getGreyingPaths() {
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#isPathChecked
     * (javax.swing.tree.TreePath)
     */
    public boolean isPathChecked(TreePath path) {
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#isPathEnabled
     * (javax.swing.tree.TreePath)
     */
    public boolean isPathEnabled(TreePath path) {
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#isPathGreyed
     * (javax.swing.tree.TreePath)
     */
    public boolean isPathGreyed(TreePath path) {
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#removeCheckingPath
     * (javax.swing.tree.TreePath)
     */
    public void removeCheckingPath(TreePath path) {
	// nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     * 
     * @seeit.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#
     * removeCheckingPaths(javax.swing.tree.TreePath[])
     */
    public void removeCheckingPaths(TreePath[] paths) {
	// nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     * 
     * @seeit.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#
     * removeTreeCheckingListener
     * (it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingListener)
     */
    public void removeTreeCheckingListener(TreeCheckingListener tcl) {
	// nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#setCheckingMode
     * (it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel.CheckingMode)
     */
    public void setCheckingMode(CheckingMode mode) {
	// nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#setCheckingPath
     * (javax.swing.tree.TreePath)
     */
    public void setCheckingPath(TreePath path) {
	// nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#setCheckingPaths
     * (javax.swing.tree.TreePath[])
     */
    public void setCheckingPaths(TreePath[] paths) {
	// nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#setPathEnabled
     * (javax.swing.tree.TreePath, boolean)
     */
    public void setPathEnabled(TreePath path, boolean enable) {
	// nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#setPathsEnabled
     * (javax.swing.tree.TreePath[], boolean)
     */
    public void setPathsEnabled(TreePath[] paths, boolean enable) {
	// nothing to do (cf. the Null Object pattern)
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.cnr.imaa.essi.lablib.gui.checkboxtree.TreeCheckingModel#toggleCheckingPath
     * (javax.swing.tree.TreePath)
     */
    public void toggleCheckingPath(TreePath pathForRow) {
	// nothing to do (cf. the Null Object pattern)
    }

}
