package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlincdb.document.Document;
import java.util.List;

/**
 * Interface for all components that would like to be notified when a query is executed.
 * 
 */
public interface VLQueryListener
{
    public void aboutToExecuteQuery();
    public void queryFinished(List<Document> documents, List<LocationValue> locationsInFacetTree, List<PersonValue> peopleInFacetTree);
}
