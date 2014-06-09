/*

*/
package edu.mit.ll.vizlinc.db;

import edu.mit.ll.vizlinc.model.DBManager;
import edu.mit.ll.vizlinc.ui.elements.DataConfigDialog;
import edu.mit.ll.vizlinc.ui.options.VizLincPanel;
import edu.mit.ll.vizlinc.utils.UIUtils;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.openide.modules.ModuleInstall;
import org.openide.modules.OnStart;
import org.openide.util.NbPreferences;

/**
 *  Monitors the application's life cycle and executes at key points during this cycle (e.g. start of application, when application is closed)
 */
@OnStart
public class Installer extends ModuleInstall implements Runnable
{
    @Override
    public void close()
    {
        try
        {
            DBManager.getInstance().shutdownDB();
        } 
        catch (Exception e)
        {
            UIUtils.reportException(e);
        }
        super.close();
    }

    @Override
    public void restored()
    {
        super.restored();
      //  UIManager.put("ViewTabDisplayerUI", "edu.mit.ll.leads.ui.elements.NoTabsTabDisplayerUI");
        
    }

    /**
     * Gets executed on start thanks to onStart annotation.
     */
    @Override
    public void run()
    {
        System.err.println("Installer OnStart: Verifying Vizlinc Configuration");
        //Determine whether configuration dialog should be displayed and the reason
        //Check if some parameters are unspecified
        boolean unspecified = allParamsSpecified();
        if(!unspecified)
        {
            JOptionPane.showMessageDialog(null, "The data can't be found because their paths haven't been specified. Click \"Ok\" to configure VizLinc in the next screen.", "VizLinc Configuration", JOptionPane.WARNING_MESSAGE);
            DataConfigDialog dialog = new DataConfigDialog(null);
            dialog.pack();
            //dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    }

    private boolean allParamsSpecified()
    {
        Preferences pref = NbPreferences.forModule(VizLincPanel.class);
        String dbLocation = pref.get(VizLincPanel.PREF_DB_LOCATION,"");
        if(dbLocation.isEmpty())
        {
            return false;
        }
        
        String indexLocation = pref.get(VizLincPanel.PREF_INDEX_LOCATION,"");
        if(indexLocation.isEmpty())
        {
            return false;
        }
        
        String graphLocation = pref.get(VizLincPanel.PREF_GRAPH_LOCATION,"");
        if(graphLocation.isEmpty())
        {
            return false;
        }
        
        String tilesLocation = pref.get(VizLincPanel.PREF_TILES_LOCATION_FILE,"");
        if(tilesLocation.isEmpty())
        {
            tilesLocation = pref.get(VizLincPanel.PREF_TILES_LOCATION_URL,"");
            if(tilesLocation.isEmpty())
            {
                return false;
            }
        }
        
        return true;
    }
}