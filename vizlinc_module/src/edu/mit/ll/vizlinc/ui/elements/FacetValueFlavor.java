/*
 */
package edu.mit.ll.vizlinc.ui.elements;

import edu.mit.ll.vizlinc.model.FacetValue;
import java.awt.datatransfer.DataFlavor;

/**
 ** DataFlavor for facet values. This class is used to support drag and drop operations where
 * facet values are transferred.
 */
public class FacetValueFlavor extends DataFlavor
{
    /**
     * 
     * @throws ClassNotFoundException  if the class is not loaded see {@link DataFlavor} for details.
     */
    public FacetValueFlavor() throws ClassNotFoundException
    {
        super(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + FacetValue.class.getName());
    }
}