package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlinc.utils.DBUtils;
import edu.mit.ll.vizlincdb.entity.Entity;
import edu.mit.ll.vizlincdb.util.VizLincProperties;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Person facet.
 */
public class Person extends Facet 
{
    public static final String PERSON_FACET_NAME = "Person";
    
    private boolean showingWeakAcrossDoc = true;
    private List<PersonValue> filteredList; 
    
    public Person(int sortCriterion) 
    {
        super("Person", sortCriterion);
        setFilteredList();
    }
    
    public Person(List<PersonValue> vals, int sortCriterion)
    {
        super("Person", vals,  sortCriterion);
        setFilteredList();
    }

    @Override
    protected List<? extends FacetValue> initFacetValueList() 
    {        
        return  DBUtils.getPersons();
    }

    private void printToFile(List<Entity> facetVals) 
    {
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream("C:\\TEMP\\persons.txt"), "UTF-8"));
            for(Entity f: facetVals)
            {
                //writer.println("**" + f.getText() + "** (" + f.getMentionCount() + ") length: " + f.getText().length());
                writer.println("**" + f.getText() + "** (" + ") length: " + f.getText().length());
            }
        }
        catch(FileNotFoundException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        catch(UnsupportedEncodingException e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        finally
        {
            writer.close();
        }
    }
    
    public List<PersonValue> getValuesAsPersonValues()
    {
        List<? extends FacetValue> fvs = getFacetValues();
        List<PersonValue> persons = new ArrayList<PersonValue>(fvs.size());
        for(int i = 0; i < fvs.size(); i++)
        {
            PersonValue p = (PersonValue) fvs.get(i);
            persons.add(p);
        }
        
        return persons;
    }

    int getNumStrongAcrossDocPeople()
    {
        return this.filteredList.size();
    }

    List<PersonValue> getStrongAcrossDocPeople()
    {
        return this.filteredList;
    }

    private void setFilteredList()
    {
        filteredList = new ArrayList();
        for(FacetValue fv: getFacetValues())
        {
            PersonValue pv = (PersonValue) fv;
            if(!pv.getPersonEntity().getCreatedBy().equals(VizLincProperties.P_CREATED_BY_WEAK_ACROSS_DOC))
            {
               filteredList.add(pv); 
            }
        }
    }

    @Override
    protected void sortValues(int sort)
    {
        super.sortValues(sort);
        setFilteredList();
    }
    
    public void setShowingWeakAcrossDoc(boolean show)
    {
        this.showingWeakAcrossDoc = show;
    }
    
    @Override
    public String toString() 
    {
        if (!showingWeakAcrossDoc)
        {
            System.out.println("Not showing weak across doc.");
            long count = (long) filteredList.size();
            DecimalFormat format = new DecimalFormat("###,###");
            String fCount = format.format(count);

            return PERSON_FACET_NAME + " (" + fCount + ")";
        }
        else
        {
            System.out.println("Showing weak across doc.");
            return super.toString();
        }
    }
}