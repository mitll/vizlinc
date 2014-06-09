/*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlinc.utils.DBUtils;
import java.util.List;

/**
 * List model for an organization list.
 */
public class OrganizationListModel extends FacetListModel<OrganizationValue>
{
    public OrganizationListModel(int sort)
    {
        super(DBUtils.getOrganizations(), sort);
    }

    OrganizationListModel(List<OrganizationValue> organizationValues, int sort)
    {
        super(organizationValues, sort);
    }
}
