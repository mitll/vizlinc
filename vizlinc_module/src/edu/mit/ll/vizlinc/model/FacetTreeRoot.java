package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlinc.utils.DBUtils;
import edu.mit.ll.vizlinc.utils.UIUtils;
import edu.mit.ll.vizlincdb.document.Document;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class FacetTreeRoot 
{
    private List<Facet> facets;
    

    public FacetTreeRoot(String message)
    {
        this.facets = new ArrayList<Facet>(1);
        facets.add(new MessageFacet(message));
    }
    public FacetTreeRoot(int sort) 
    {   
        this.facets = new ArrayList<Facet>(5);
       // facets.add(new Date(sort));
        facets.add(new Location(sort));
        facets.add(new Organization(sort));
        facets.add(new Person(sort));
       // facets.add(new Topic(sort));
        
        //Create a read-only list
        facets = Collections.unmodifiableList(facets);
    }

    FacetTreeRoot(List<Document> docList, int sort, NeighborhoodConfig neighConfig) 
    {
        this.facets = new ArrayList<Facet>(5);
        try
        {
            FacetValuesByCategory fVByCat;
            if(neighConfig != null)
            {
                fVByCat = DBUtils.getNeighboringEntities(docList, neighConfig);
            }
            else
            {
                fVByCat = DBUtils.getAllEntitiesForDocs(docList);
            }
           // facets.add(new Date(fVByCat.getDateValues(), sort));
            facets.add(new Location(fVByCat.getLocationValues(), sort));
            facets.add(new Organization(fVByCat.getOrganizationValues(), sort));
            facets.add(new Person(fVByCat.getPersonValues(), sort));
            //TODO: change this when topics are implemented
            //facets.add(new Topic(sort));

            //Create a read-only list
            facets = Collections.unmodifiableList(facets);
        } catch (SQLException e)
        {
            UIUtils.reportException(e);
        }
    }

    @Override
    public String toString() 
    {
        return "Facets";
    }
    
    public List<Facet> getFacets()
    {
        return this.facets;
    }
    
    public int getFacetCount()
    {
        return this.facets.size();
    }

    void changeSortCriterion(int sort) 
    {
        for(Facet f: this.facets)
        {
            f.sortValues(sort);
        }
    }
    
    public Facet getFacetWithName(String name)
    {
        for(Facet f: this.facets)
        {
            // Entities use uppercase; facets use capitalized words: LOCATION vs Location.
            if(f.getName().equalsIgnoreCase(name))
            {
                return f;
            }
        }
        return null;
    }
}
