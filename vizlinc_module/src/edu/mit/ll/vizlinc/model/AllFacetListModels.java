package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlinc.utils.DBUtils;
import edu.mit.ll.vizlincdb.document.Document;
import java.sql.SQLException;
import java.util.List;

/**
 * Container for list models of all entity types
 */
public class AllFacetListModels
{

    private LocationListModel lm;
    private OrganizationListModel om;
    private PersonListModel pm;

    public AllFacetListModels(int sort, boolean showWeakAcrossDocPeople)
    {
        lm = new LocationListModel(sort);
        om = new OrganizationListModel(sort);
        pm = new PersonListModel(sort, showWeakAcrossDocPeople);
    }

    public AllFacetListModels(List<Document> resultDocList, int sort, boolean showWeakAcrossDocPeople) throws SQLException
    {
        this(resultDocList, sort, null, showWeakAcrossDocPeople);
    }

    public AllFacetListModels(List<Document> resultDocList, int sort, NeighborhoodConfig config, boolean showWeakAcrossDocPeople) throws SQLException
    {
        FacetValuesByCategory fVByCat;
        if (config != null)
        {
            //TODO: add support for neighborhood
            //fVByCat = DBUtils.getNeighboringEntities(docList, neighConfig);
            fVByCat = null;
        } 
        else
        {
            fVByCat = DBUtils.getAllEntitiesForDocs(resultDocList);
        }
        lm = new LocationListModel(fVByCat.getLocationValues(), sort);
        om = new OrganizationListModel(fVByCat.getOrganizationValues(), sort);
        pm = new PersonListModel(fVByCat.getPersonValues(), sort, showWeakAcrossDocPeople);
    }

    public LocationListModel getLocationListModel()
    {
        return lm;
    }

    public OrganizationListModel getOrganizationListModel()
    {
        return om;
    }

    public PersonListModel getPersonListModel()
    {
        return pm;
    }
}
