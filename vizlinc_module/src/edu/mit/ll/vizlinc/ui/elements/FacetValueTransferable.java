/*
 */
package edu.mit.ll.vizlinc.ui.elements;

import edu.mit.ll.vizlinc.model.FacetValue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Drag and drop support from search window to query window
 */
public class FacetValueTransferable implements Transferable
{
    public static DataFlavor facetValueFlavor = getFacetValueFlavor();
   private static final DataFlavor[] flavors = { facetValueFlavor, DataFlavor.stringFlavor};
   private static final int FACET_VALUE = 0;
   private static final int STRING = 1;
   
   private FacetValue data;
   
   private static FacetValueFlavor getFacetValueFlavor()
   {
       FacetValueFlavor flavor = null;
       try
       {
           flavor = new FacetValueFlavor();
       }
       catch (Exception e)
       {
           return null;
       }
       return flavor;
   }
   
   public FacetValueTransferable(FacetValue data)
   {
       this.data = data;
   }

    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        return (DataFlavor[])flavors.clone();
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        for (int i = 0; i < flavors.length; i++) 
        {
	    if (flavor.equals(flavors[i])) 
            {
	        return true;
	    }
	}
	return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
    {
        if(flavor.equals(flavors[FACET_VALUE]))
        {
            return (Object) data;
        }
        else if(flavor.equals(flavors[STRING]))
        {
            return data.getType() + ":" + data.getText();
        }
        else
        {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}