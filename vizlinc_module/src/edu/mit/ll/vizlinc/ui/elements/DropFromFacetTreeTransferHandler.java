/*
 */
package edu.mit.ll.vizlinc.ui.elements;

import edu.mit.ll.vizlinc.model.FacetValue;
import edu.mit.ll.vizlinc.utils.UIUtils;
import javax.swing.TransferHandler;
import org.openide.util.Exceptions;

/**
 * Supports drag and drop of facet values from the search window to the query window.
 */
public class DropFromFacetTreeTransferHandler extends TransferHandler
    {

        public DropFromFacetTreeTransferHandler()
        {
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support)
        {
            System.out.println("VLQuery: CanImport called");
            //Drops on this window only suppor FacetValueFlavor
            if(!support.isDataFlavorSupported(FacetValueTransferable.facetValueFlavor))
            {
                return false;
            }
            
            return true;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support)
        {
            System.out.println("VLQuery: importData called()");
            if(!support.isDrop())
            {
                return false;
            }
            
            FacetValue fv;
            try
            {
                fv = (FacetValue)support.getTransferable().getTransferData(FacetValueTransferable.facetValueFlavor);
            } catch (Exception ex)
            {
                Exceptions.printStackTrace(ex);
                UIUtils.reportException(UIUtils.getFirstParentComponent(support.getComponent()), ex);
                return false;
            }
            
            System.out.println("About to add facet value to query...");
            //UIUtils.getFacetedSearchWindow().addFacetValueToQuery(fv);
            
            return true;
        }
    }