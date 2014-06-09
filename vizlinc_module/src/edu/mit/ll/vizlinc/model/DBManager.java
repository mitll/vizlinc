 /*
 */
package edu.mit.ll.vizlinc.model;

import edu.mit.ll.vizlinc.ui.options.VizLincPanel;
import edu.mit.ll.vizlincdb.document.VizLincSearcher;
import edu.mit.ll.vizlincdb.relational.VizLincRDBMem;
import java.awt.Frame;
import java.io.IOException;
import java.sql.SQLException;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 * Singleton class that controls communication with the database and the index.
 */
public class DBManager
{

    private VizLincRDBMem db;
    private VizLincSearcher searcher;
    boolean dbShutdown;
    private static DBManager singleton;
    private boolean ready = false;

    public boolean isReady()
    {
        return this.ready;
    }

    private DBManager()
    {
        System.err.println("In DBManager()");
        Preferences pref = NbPreferences.forModule(VizLincPanel.class);
        String dbLocation = pref.get(VizLincPanel.PREF_DB_LOCATION, "");
        String indexLocation = pref.get(VizLincPanel.PREF_INDEX_LOCATION, "");
        try
        {
            this.db = new VizLincRDBMem(dbLocation);
            this.searcher = new VizLincSearcher(indexLocation);
            this.ready = true;
        } catch (Exception e)
        {
            e.printStackTrace();
            this.ready = false;
            String title = "";
            if (db == null)
            {
                title = "Problem opening database. ";
            } else if (searcher == null)
            {
                title = "Problem opening search index. ";
            }

            String msg = title + ": " + e.getMessage() + "\nCheck path specified under Tools > Options > VizLinc and restart the application.";

            Frame frame = WindowManager.getDefault().getMainWindow();
            JOptionPane.showMessageDialog(frame, msg, title, JOptionPane.ERROR_MESSAGE);
        }

        dbShutdown = false;
    }

    public synchronized static DBManager getInstance()
    {
        if (singleton == null)
        {
            singleton = new DBManager();
        }

        return singleton;
    }

    public void shutdownDB() throws SQLException, IOException
    {
        dbShutdown = true;
        if (db != null)
        {
            db.shutdown();
        }

        if (searcher != null)
        {
            searcher.close();
        }
    }

    public VizLincRDBMem getDB()
    {
        if (dbShutdown)
        {
            return null;
        }

        return this.db;
    }

    public VizLincSearcher getSearcher()
    {
        if (dbShutdown)
        {
            return null;
        }
        return this.searcher;
    }
}