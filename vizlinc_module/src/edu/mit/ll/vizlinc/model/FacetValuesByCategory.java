/*
 * 
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlincdb.entity.DateEntity;
import edu.mit.ll.vizlincdb.entity.EntitySet;
import edu.mit.ll.vizlincdb.entity.LocationEntity;
import edu.mit.ll.vizlincdb.entity.OrganizationEntity;
import edu.mit.ll.vizlincdb.entity.PersonEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Container class that categorizes and stores facet values 
 */
public class FacetValuesByCategory
{
    private List<DateValue> dateValues;
    private List<LocationValue> locationValues;
    private List<OrganizationValue> organizationValues;
    private List<PersonValue> personValues;
    
    public FacetValuesByCategory(EntitySet e, Map<Integer, Integer> mentionCountMap, Map<Integer,Integer> docCountMap, boolean showFullDataCounts)
    {
  
        locationValues = new ArrayList<LocationValue>(e.getLocationEntities().size());
        organizationValues = new ArrayList<OrganizationValue>(e.getOrganizationEntities().size());
        personValues = new ArrayList<PersonValue>(e.getPersonEntities().size());
                
        dateValues = createDateFacetValueList( e.getDateEntities(), mentionCountMap, docCountMap, showFullDataCounts);
        locationValues = createLocFacetValueList(e.getLocationEntities(), mentionCountMap, docCountMap, showFullDataCounts);
        organizationValues = createOrgFacetValueList(e.getOrganizationEntities(), mentionCountMap, docCountMap, showFullDataCounts);
        personValues =  createPerFacetValueList(e.getPersonEntities(), mentionCountMap, docCountMap, showFullDataCounts);
    }

    private List<DateValue> createDateFacetValueList( List<DateEntity> entities, Map<Integer, Integer> mentionCountMap, Map<Integer, Integer> docCountMap, boolean showFullDataCounts)
    {
        List<DateValue> result = new ArrayList<DateValue>(entities.size());
        for(DateEntity e: entities)
        {
            DateValue val = new DateValue(e);
            setFacetValueFields(val, mentionCountMap, docCountMap, showFullDataCounts, e.getId());
            result.add(val);
        }
        
        return result;
    }
    
    private List<LocationValue> createLocFacetValueList(List<LocationEntity> entities, Map<Integer, Integer> mentionCountMap, Map<Integer, Integer> docCountMap, boolean showFullDataCounts)
    {
       List<LocationValue> result = new ArrayList<LocationValue>(entities.size());
        for(LocationEntity e: entities)
        {
            LocationValue val = new LocationValue(e);
            setFacetValueFields(val, mentionCountMap, docCountMap, showFullDataCounts, e.getId());
            result.add(val);
        }
        
        return result;
    }
    
    private List<OrganizationValue> createOrgFacetValueList(List<OrganizationEntity> entities, Map<Integer, Integer> mentionCountMap, Map<Integer, Integer> docCountMap, boolean showFullDataCounts)
    {
        List<OrganizationValue> result = new ArrayList<OrganizationValue>(entities.size());
        for(OrganizationEntity e: entities)
        {
            OrganizationValue val = new OrganizationValue(e);
            setFacetValueFields(val, mentionCountMap, docCountMap, showFullDataCounts, e.getId());
            result.add(val);
        }
        
        return result;
    }
    
    private List<PersonValue> createPerFacetValueList(List<PersonEntity> entities, Map<Integer, Integer> mentionCountMap, Map<Integer, Integer> docCountMap, boolean showFullDataCounts)
    {
        List<PersonValue> result = new ArrayList<PersonValue>(entities.size());
        for(PersonEntity e: entities)
        {
            PersonValue val = new PersonValue(e);
            setFacetValueFields(val, mentionCountMap, docCountMap, showFullDataCounts, e.getId());
            result.add(val);
        }
        
        return result;
    }

    private void setFacetValueFields(FacetValue val, Map<Integer, Integer> mentionCountMap, Map<Integer, Integer> docCountMap, boolean showFullDataCounts, int eId)
    {
        val.setCurrentNumDocuments(docCountMap.get(eId));
        val.setCurrentNumMentions(mentionCountMap.get(eId));
        val.setShowFullDataSetCounts(showFullDataCounts);
    }

    List<DateValue> getDateValues()
    {
        return this.dateValues;
    }

    List<LocationValue> getLocationValues()
    {
       return this.locationValues;
    }

    List<OrganizationValue> getOrganizationValues()
    {
        return this.organizationValues;
    }

    List<PersonValue> getPersonValues()
    {
        return this.personValues;
    }
}