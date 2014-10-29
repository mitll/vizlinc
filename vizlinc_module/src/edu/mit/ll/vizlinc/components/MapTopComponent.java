/*
 * 
 */
package edu.mit.ll.vizlinc.components;

import edu.mit.ll.vizlinc.map.CircleWayPointRenderer;
import edu.mit.ll.vizlinc.map.ClusterWaypoint;
import edu.mit.ll.vizlinc.map.ClusterWaypointPainter;
import edu.mit.ll.vizlinc.map.ColorByConfig;
import edu.mit.ll.vizlinc.model.DBManager;
import edu.mit.ll.vizlinc.model.FacetValue;
import edu.mit.ll.vizlinc.model.LocationValue;
import edu.mit.ll.vizlinc.model.PersonValue;
import edu.mit.ll.vizlinc.model.VLQueryListener;
import edu.mit.ll.vizlinc.ui.options.VizLincPanel;
import edu.mit.ll.vizlinc.utils.DBUtils;
import edu.mit.ll.vizlinc.utils.Log;
import edu.mit.ll.vizlinc.utils.UIUtils;
import edu.mit.ll.vizlincdb.document.Document;
import edu.mit.ll.vizlincdb.geo.GeoPoint;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component that displays map and geo-coded locations
 */
@ConvertAsProperties(
        dtd = "-//edu.mit.ll.vizlinc.components//Map//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "MapTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = true)
@ActionID(category = "Window", id = "edu.mit.ll.vizlinc.components.MapTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MapAction",
        preferredID = "MapTopComponent")
@Messages(
        {
    "CTL_MapAction=Map",
    "CTL_MapTopComponent=Map",
    "HINT_MapTopComponent=This is a Map window"
})
public final class MapTopComponent extends TopComponent implements VLQueryListener
{

    private PropertiesTopComponent propWin;
    private JPopupMenu popup;
    private List<ClusterWaypoint> selectedWayPoints;
    private Rectangle selectionRectangle;

    public enum ColorByCriterion
    {

        DOCUMENT, MENTION
    }

    public enum ScaleType
    {

        LINEAR, LOG
    }

    public enum ColorVariable
    {

        COLOR, ALPHA
    };
    private boolean queryInProgress;

    public MapTopComponent()
    {
        if(!DBManager.getInstance().isReady())
        {
            return;
        }
        //setProxySettings();
        initComponents();

        setName(Bundle.CTL_MapTopComponent());
        setToolTipText(Bundle.HINT_MapTopComponent());
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        //Set local tile server
        MouseHandler mh = new MouseHandler();
        jXMapKit1.getMainMap().addMouseListener(mh);
        jXMapKit1.getMainMap().addMouseMotionListener(mh);


        //Define tile server for this mapkit
        Preferences pref = NbPreferences.forModule(VizLincPanel.class);
        String tilesLoc = pref.get(VizLincPanel.PREF_TILES_LOCATION_FILE, "").replaceAll("\\\\", "/");
        boolean usingServerTemp = false;
        if(tilesLoc.isEmpty())
        {
            //URL should be specified
            tilesLoc = pref.get(VizLincPanel.PREF_TILES_LOCATION_URL, "");
            usingServerTemp = true;
        }
        final String dirPath = tilesLoc;
        final boolean usingServer = usingServerTemp;
        
        //final String dirPath = "E:/LEADS/open_street_maps/tiles";
        if (dirPath != null && !dirPath.isEmpty())
        {
            String baseDir = dirPath;
            if(!usingServer)
            {
                baseDir = "file:///" + dirPath;
            }
            TileFactoryInfo info = new TileFactoryInfo(1, 15, 17, 256, true, true,
                    baseDir, "x", "y", "z")
            {
                public String getTileUrl(int x, int y, int zoom)
                {
                    zoom = 17 - zoom;
                    String filePath = zoom + "/" + x + "/" + y + ".png";
                    String url = this.baseURL + "/" + filePath;
                    if(usingServer)
                    {
                        System.out.println("Returning URL: " + url);
                        return url;
                    }
                    
                    File f = new File(dirPath, filePath);
                    if (f.exists())
                    {
                        return url;
                    }
                    else
                    {
                        url = this.baseURL + "/dummy.png";
                        File dummyImgFile = new File(dirPath, "dummy.png");
                        if(!dummyImgFile.exists())
                        {
                            throw new RuntimeException("Directory: " + dirPath + " does not contain map tiles. "
                                    + "Please set the appropriate path in Tools > Options > VizLinc and restart the application.");
                        }
                        else
                        {
                            return url;
                        }
                    }
                    
                }
            };

            try
            {
                jXMapKit1.setTileFactory(new DefaultTileFactory(info));

                // Set initial zoom level and re-center; must be done after setTileFactory().
                jXMapKit1.setZoom(14);
                jXMapKit1.setCenterPosition(new GeoPosition(4.713303, -74.087128));
            } catch (Exception e)
            {
                e.printStackTrace();
                String msg = "Problem when trying to access map tiles. Please verify the map tiles are accessible and the the associated options"
                        + " are correctly specified under Tools > Options > VizLinc. Any changes will require you to restart VizLinc";
                Frame mainFrame =  WindowManager.getDefault().getMainWindow();
                JOptionPane.showMessageDialog(mainFrame, msg, "Map Tiles Access", JOptionPane.ERROR_MESSAGE);
            }
        }
        //TODO; the properties window might not have been initialized at this point. Make this call
        //only when you are sure the window has been initialized.
        this.propWin = (PropertiesTopComponent) WindowManager.getDefault().findTopComponent("PropertiesTopComponent");
        ClusterWaypointPainter painter = new ClusterWaypointPainter();
        painter.setRenderer(new CircleWayPointRenderer(propWin));
        jXMapKit1.getMainMap().setOverlayPainter(painter);

        //no location is selected initially
        selectedWayPoints = new LinkedList<ClusterWaypoint>();

        //no query in progress on startup
        queryInProgress = false;

        //Define actions
        //Map pop up menu
        popup = new JPopupMenu();
        JMenuItem item = new JMenuItem("Add Filter");
        item.setIcon(new ImageIcon(getClass().getResource("/edu/mit/ll/vizlinc/ui/icons/add_filter.png")));
        item.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                List<FacetValue> entities = new ArrayList<FacetValue>(selectedWayPoints.size());
                for (ClusterWaypoint cw : selectedWayPoints)
                {
                    entities.add(cw.getLocationValue());
                }

                FacetedSearchTopComponent fS = UIUtils.getFacetedSearchWindow();
                fS.pushToStackAndPerformQuery(entities);
            }
        });
        popup.add(item);

        this.selectionRectangle = new Rectangle();
        final VLQueryListener thisAsAListener = this;

        WindowManager.getDefault().invokeWhenUIReady(new Runnable()
        {
            @Override
            public void run()
            {
                FacetedSearchTopComponent searchWindow = UIUtils.getFacetedSearchWindow();

                if (searchWindow != null)
                {
                    //Register as a VLQueryListener 
                    searchWindow.addQueryListener(thisAsAListener);
                    //Load all locations to map on startup
                    mapLocationsAux(searchWindow.getLocationsInFacetTree(), false);
                }
            }
        });
    }

    @Override
    public void aboutToExecuteQuery()
    {
        System.err.println("In MAP: about to execute query");
        queryInProgress = true;
        System.err.println("DONE");
    }

    @Override
    public void queryFinished(List<Document> documents, List<LocationValue> locationsInFacetTree, List<PersonValue> peopleInFacetTree)
    {
        System.err.println("In MAP: queryFinished()...");
        queryInProgress = false;
        mapLocations(locationsInFacetTree);
        System.err.println("In MAP: queryFinished()...DONE");
    }

    private DocListTopComponent getDocListWindow()
    {
        return (DocListTopComponent) WindowManager.getDefault().findTopComponent("DocListTopComponent");
    }

    // TODO: make proxy settings configurable
    private void setProxySettings()
    {
        //Properties systemSettings = System.getProperties();
        //systemSettings.put("http.proxyHost", "155.34.234.20");
        // systemSettings.put("http.proxyPort", "8080");
        //System.setProperties(systemSettings);
        JOptionPane.showMessageDialog(null, "Setting proxy settings");
        System.setProperty("http.proxyHost", "155.34.234.20");
        System.setProperty("http.proxyPort", "8080");
    }

    public void mapLocations(List<LocationValue> locations)
    {
        mapLocationsAux(locations, false);
    }

    private void mapLocationsAux(List<LocationValue> locations, boolean notifyLocsInMapWin)
    {
        System.err.println("mapping locations " + Thread.currentThread() + " ....");
        //Beware of duplicate waypoints
        List<Waypoint> waypoints = new LinkedList<Waypoint>();

        //TODO: I don't want a marker on the center of the map
         /*int numWayPoints = 10000;
         
         for(int i = 0; i < numWayPoints; i++)
         {
         //Create a random wayPoint
         waypoints.add(createRandomWayPoint());
         }*/

        //  int id = 0;
        StringBuilder sb = new StringBuilder("Fetching geos for all locations and creating waypoints");
        sb.append("\n");
        Log.appendLine(sb.toString());
        List<Integer> locIds = new ArrayList<Integer>(locations.size());
        for (LocationValue loc : locations)
        {
            locIds.add(loc.getId());
        }

        //geos = DBUtils.getNFirstGeolocations(locEntity, 1);

        List<GeoPoint> geoPoints = null;
        try
        {
            geoPoints = DBUtils.getTopGeos(locIds);
        } catch (SQLException e)
        {
            UIUtils.reportException(e);
        }
        if (geoPoints != null)
        {
            for (int i = 0; i < geoPoints.size(); i++)
            {
                GeoPoint g = geoPoints.get(i);
                if (g != null)
                {
                    LocationValue location = locations.get(i);
                    ClusterWaypoint myWaypoint = new ClusterWaypoint(g.latitude, g.longitude, location);
                    // myWaypoint.setClusterId(id);
                    //TODO: I might be losing locations by using a set given the current equals method. Revisit this!!
                    waypoints.add(myWaypoint);
                }
            }
        }

        mapWaypoints(waypoints);
        System.err.println("mapping locations " + Thread.currentThread() + " .... DONE");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        ColorByRadioBtn = new javax.swing.ButtonGroup();
        scaleRadioBtn = new javax.swing.ButtonGroup();
        variableBtnGroup = new javax.swing.ButtonGroup();
        jXMapKit1 = new org.jdesktop.swingx.JXMapKit();
        colorByMentionsCheckbox = new javax.swing.JCheckBox();
        mentionRadioBtn = new javax.swing.JRadioButton();
        docRadioBtn = new javax.swing.JRadioButton();
        byLabel = new javax.swing.JLabel();
        scaleLabel = new javax.swing.JLabel();
        linearRadioBtn = new javax.swing.JRadioButton();
        logRadioBtn = new javax.swing.JRadioButton();
        varyLabel = new javax.swing.JLabel();
        colorRadioBtn = new javax.swing.JRadioButton();
        alphaRadioBtn = new javax.swing.JRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        markerSizeSpinner = new javax.swing.JSpinner();

        jXMapKit1.setDefaultProvider(null);

        org.openide.awt.Mnemonics.setLocalizedText(colorByMentionsCheckbox, org.openide.util.NbBundle.getMessage(MapTopComponent.class, "MapTopComponent.colorByMentionsCheckbox.text")); // NOI18N
        colorByMentionsCheckbox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                colorByMentionsCheckboxActionPerformed(evt);
            }
        });

        ColorByRadioBtn.add(mentionRadioBtn);
        mentionRadioBtn.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(mentionRadioBtn, org.openide.util.NbBundle.getMessage(MapTopComponent.class, "MapTopComponent.mentionRadioBtn.text")); // NOI18N
        mentionRadioBtn.setEnabled(false);
        mentionRadioBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mentionRadioBtnActionPerformed(evt);
            }
        });

        ColorByRadioBtn.add(docRadioBtn);
        org.openide.awt.Mnemonics.setLocalizedText(docRadioBtn, org.openide.util.NbBundle.getMessage(MapTopComponent.class, "MapTopComponent.docRadioBtn.text")); // NOI18N
        docRadioBtn.setEnabled(false);
        docRadioBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mentionRadioBtnActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(byLabel, org.openide.util.NbBundle.getMessage(MapTopComponent.class, "MapTopComponent.byLabel.text")); // NOI18N
        byLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(scaleLabel, org.openide.util.NbBundle.getMessage(MapTopComponent.class, "MapTopComponent.scaleLabel.text")); // NOI18N
        scaleLabel.setEnabled(false);

        scaleRadioBtn.add(linearRadioBtn);
        linearRadioBtn.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(linearRadioBtn, org.openide.util.NbBundle.getMessage(MapTopComponent.class, "MapTopComponent.linearRadioBtn.text")); // NOI18N
        linearRadioBtn.setEnabled(false);
        linearRadioBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mentionRadioBtnActionPerformed(evt);
            }
        });

        scaleRadioBtn.add(logRadioBtn);
        org.openide.awt.Mnemonics.setLocalizedText(logRadioBtn, org.openide.util.NbBundle.getMessage(MapTopComponent.class, "MapTopComponent.logRadioBtn.text")); // NOI18N
        logRadioBtn.setEnabled(false);
        logRadioBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mentionRadioBtnActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(varyLabel, org.openide.util.NbBundle.getMessage(MapTopComponent.class, "MapTopComponent.varyLabel.text")); // NOI18N
        varyLabel.setEnabled(false);

        variableBtnGroup.add(colorRadioBtn);
        colorRadioBtn.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(colorRadioBtn, org.openide.util.NbBundle.getMessage(MapTopComponent.class, "MapTopComponent.colorRadioBtn.text")); // NOI18N
        colorRadioBtn.setEnabled(false);
        colorRadioBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mentionRadioBtnActionPerformed(evt);
            }
        });

        variableBtnGroup.add(alphaRadioBtn);
        org.openide.awt.Mnemonics.setLocalizedText(alphaRadioBtn, org.openide.util.NbBundle.getMessage(MapTopComponent.class, "MapTopComponent.alphaRadioBtn.text")); // NOI18N
        alphaRadioBtn.setEnabled(false);
        alphaRadioBtn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mentionRadioBtnActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        markerSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(10, 0, 15, 1));
        markerSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                markerSizeSpinnerChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jXMapKit1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(colorByMentionsCheckbox)
                .addGap(2, 2, 2)
                .addComponent(byLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mentionRadioBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(docRadioBtn)
                .addGap(18, 18, 18)
                .addComponent(scaleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(linearRadioBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logRadioBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(varyLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorRadioBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(alphaRadioBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(markerSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colorByMentionsCheckbox)
                        .addComponent(mentionRadioBtn)
                        .addComponent(docRadioBtn)
                        .addComponent(byLabel)
                        .addComponent(scaleLabel)
                        .addComponent(linearRadioBtn)
                        .addComponent(logRadioBtn)
                        .addComponent(varyLabel)
                        .addComponent(colorRadioBtn)
                        .addComponent(alphaRadioBtn))
                    .addComponent(markerSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jXMapKit1, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void colorByMentionsCheckboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_colorByMentionsCheckboxActionPerformed
    {//GEN-HEADEREND:event_colorByMentionsCheckboxActionPerformed
        boolean val;
        if (this.colorByMentionsCheckbox.isSelected())
        {
            val = true;
            //Activate related controls
            setEnableColorControls(true);
        } else
        {
            val = false;
            setEnableColorControls(false);
        }
        ColorByCriterion colorBy = null;
        if (docRadioBtn.isSelected())
        {
            colorBy = ColorByCriterion.DOCUMENT;
        } else if (mentionRadioBtn.isSelected())
        {
            colorBy = ColorByCriterion.MENTION;
        }

        ScaleType scale = null;
        if (linearRadioBtn.isSelected())
        {
            scale = ScaleType.LINEAR;
        } else if (logRadioBtn.isSelected())
        {
            scale = ScaleType.LOG;
        }

        ColorVariable var = null;
        if (colorRadioBtn.isSelected())
        {
            var = ColorVariable.COLOR;
        } else if (alphaRadioBtn.isSelected())
        {
            var = ColorVariable.ALPHA;
        }

        getMapPainter().setColorBy(new ColorByConfig(val, scale, colorBy, var));
        //repaint map
        getMainMap().repaint();
    }//GEN-LAST:event_colorByMentionsCheckboxActionPerformed

    private void mentionRadioBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mentionRadioBtnActionPerformed
    {//GEN-HEADEREND:event_mentionRadioBtnActionPerformed
        //TODO: Don't color if user is just pressing the button that was selected already.
        colorByMentionsCheckboxActionPerformed(evt);
    }//GEN-LAST:event_mentionRadioBtnActionPerformed

    private void markerSizeSpinnerChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_markerSizeSpinnerChanged
    {//GEN-HEADEREND:event_markerSizeSpinnerChanged
        System.out.println("Size changed");
        JSpinner spinner = (JSpinner)evt.getSource();
        int radius = Integer.parseInt(spinner.getValue().toString());
        final JXMapViewer map = jXMapKit1.getMainMap();
        ClusterWaypointPainter painter = (ClusterWaypointPainter) (map.getOverlayPainter());
        painter.setMarkerSize(radius);
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // propWin.displayLine("Mapping " + waypoints.size() + " locations");
                map.repaint();
            }
        });
    }//GEN-LAST:event_markerSizeSpinnerChanged

    private void displayMemoryUsage()
    {
        //**************** DEBUGGING ****************************//
        Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        runtime.gc();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        sb.append("free memory: " + format.format(freeMemory) + "\n");
        sb.append("allocated memory: " + format.format(allocatedMemory) + "\n");
        sb.append("max memory: " + format.format(maxMemory) + "\n");
        sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory))) + "\n");

        String javaVersion = System.getProperty("java.version");
        sb.append("Java version: " + javaVersion);
        propWin.displayLine(sb.toString());
        //*****************************************************************
    }

    protected void highlightInMap(Set<LocationValue> locations)
    {
        getPainter().setAsHighlightedLocations(locations);
        //Select these points only
        selectOnly(locations);
        getMainMap().repaint();
        propWin.showLocationProperties(selectedWayPoints);
    }

    private void selectOnly(Set<LocationValue> locations)
    {
        selectedWayPoints.clear();
        List<Waypoint> waypoints = getPainter().getWaypoints();
        for (Waypoint w : waypoints)
        {
            ClusterWaypoint cw = (ClusterWaypoint) w;
            if (locations.contains(cw.getLocationValue()))
            {
                cw.setSelected(true);
                selectedWayPoints.add(cw);
            } else
            {
                cw.setSelected(false);
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup ColorByRadioBtn;
    private javax.swing.JRadioButton alphaRadioBtn;
    private javax.swing.JLabel byLabel;
    private javax.swing.JCheckBox colorByMentionsCheckbox;
    private javax.swing.JRadioButton colorRadioBtn;
    private javax.swing.JRadioButton docRadioBtn;
    private javax.swing.JSeparator jSeparator1;
    private org.jdesktop.swingx.JXMapKit jXMapKit1;
    private javax.swing.JRadioButton linearRadioBtn;
    private javax.swing.JRadioButton logRadioBtn;
    private javax.swing.JSpinner markerSizeSpinner;
    private javax.swing.JRadioButton mentionRadioBtn;
    private javax.swing.JLabel scaleLabel;
    private javax.swing.ButtonGroup scaleRadioBtn;
    private javax.swing.ButtonGroup variableBtnGroup;
    private javax.swing.JLabel varyLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened()
    {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed()
    {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p)
    {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p)
    {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private Waypoint createRandomWayPoint()
    {
        //Long between -70 and -78
        Random rand = new Random();
        float longi = rand.nextFloat();
        longi = -1 * ((longi * 8) + 70);

        //Lat between 0 - 6.3
        float lat = 6.3f * rand.nextFloat();

        return new Waypoint(lat, longi);
    }

    private void clusterWaypoints(Set<ClusterWaypoint> waypoints)
    {
        //calculate distance matrix N^2 for now
        final float eps = 25; //in pixels
        Map<ClusterWaypoint, Map<ClusterWaypoint, Float>> distanceMatrix = calcDistanceMatrix(waypoints);
        int clusterId = 1;

        for (ClusterWaypoint w : waypoints)
        {
            List<ClusterWaypoint> epsNeigh = getEpsilonNeigh(w, distanceMatrix, eps);
            int useThisId = clusterId;
            if (w.getClusterId() != -1)
            {
                useThisId = w.getClusterId();
                clusterId++;
            }
            for (ClusterWaypoint w2 : epsNeigh)
            {
                w2.setClusterId(useThisId);
            }
        }
    }

    private Map<ClusterWaypoint, Map<ClusterWaypoint, Float>> calcDistanceMatrix(Set<ClusterWaypoint> waypoints)
    {
        Map<ClusterWaypoint, Map<ClusterWaypoint, Float>> distanceMatrix = new HashMap<ClusterWaypoint, Map<ClusterWaypoint, Float>>();
        for (ClusterWaypoint w : waypoints)
        {
            Map<ClusterWaypoint, Float> thisRow = distanceMatrix.get(w);
            if (thisRow == null)
            {
                thisRow = new HashMap<ClusterWaypoint, Float>();
                distanceMatrix.put(w, thisRow);
            }
            for (ClusterWaypoint w2 : waypoints)
            {
                float dist = euclDistance(w, w2);
                thisRow.put(w2, dist);
            }
        }
        return distanceMatrix;
    }

    private float euclDistance(ClusterWaypoint w1, ClusterWaypoint w2)
    {
        JXMapViewer map = jXMapKit1.getMainMap();
        TileFactory factory = map.getTileFactory();
        int zoom = map.getZoom();
        Point2D p1 = factory.geoToPixel(w1.getPosition(), zoom);
        Point2D p2 = factory.geoToPixel(w2.getPosition(), zoom);

        return (float) p1.distance(p2);
    }

    private List<ClusterWaypoint> getEpsilonNeigh(ClusterWaypoint w, Map<ClusterWaypoint, Map<ClusterWaypoint, Float>> distanceMatrix, float eps)
    {
        Map<ClusterWaypoint, Float> allNeighbors = distanceMatrix.get(w);
        List<ClusterWaypoint> epsN = new LinkedList<ClusterWaypoint>();

        for (ClusterWaypoint n : allNeighbors.keySet())
        {
            float dist = allNeighbors.get(n);
            if (dist <= eps)
            {
                epsN.add(n);
            }
        }
        return epsN;
    }

    void mapWaypoints(final List<Waypoint> waypoints)
    {
        final JXMapViewer map = jXMapKit1.getMainMap();
        ClusterWaypointPainter painter = (ClusterWaypointPainter) (map.getOverlayPainter());
        painter.setWaypoints(waypoints);
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                // propWin.displayLine("Mapping " + waypoints.size() + " locations");
                map.repaint();
            }
        });
    }

    List<Waypoint> getWaypoints()
    {
        return ((ClusterWaypointPainter) this.jXMapKit1.getMainMap().getOverlayPainter()).getWaypoints();
    }

    private ClusterWaypointPainter getMapPainter()
    {
        return (ClusterWaypointPainter) this.jXMapKit1.getMainMap().getOverlayPainter();
    }

    private void setEnableColorControls(boolean b)
    {
        byLabel.setEnabled(b);
        scaleLabel.setEnabled(b);
        linearRadioBtn.setEnabled(b);
        logRadioBtn.setEnabled(b);
        docRadioBtn.setEnabled(b);
        mentionRadioBtn.setEnabled(b);
        varyLabel.setEnabled(b);
        colorRadioBtn.setEnabled(b);
        alphaRadioBtn.setEnabled(b);
    }

    private class MouseHandler extends MouseAdapter
    {

        @Override
        public void mousePressed(MouseEvent e)
        {
            Point mousePt = e.getPoint();
            //Select clicked node (if any)
            boolean newLocationSelected = false;
            //Painter contains information about those waypoints that are clickable
            ClusterWaypointPainter painter = (ClusterWaypointPainter) jXMapKit1.getMainMap().getOverlayPainter();
            /* ++++++++++++ For clustering only +++++++++++++++++++++++/
             if (jCheckBox1.isSelected())
             {
             List<ClusterWaypoint> clickablePoints = painter.getClickablePoints();
             propWin.displayLine("Found " + clickablePoints.size() + " clickable points");
             newLocationSelected = selectOne(clickablePoints, mousePt);
             } else
             {++++++++++++++++++++++++++++++++++++++++++++++++++++++++  */
            List<Waypoint> waypoints = painter.getWaypoints();
            //propWin.displayLine("Found " + waypoints.size() + " clickable points");
            newLocationSelected = selectAllInClick(waypoints, mousePt);
            //}
            /*if(locationSelected && e.isPopupTrigger())
             {
             showPopup(e);
             }*/

            if (newLocationSelected)
            {
                propWin.showLocationProperties(selectedWayPoints);
            }

            //TODO: Consider not having to paint the whole map but only the section that was selected
            e.getComponent().repaint();
            if (SwingUtilities.isRightMouseButton(e) && !selectedWayPoints.isEmpty() && !queryInProgress)
            {
                //Disable panning to avoid NullPointerException due to supression of mousePressed events by the Popup menu
                jXMapKit1.getMainMap().setPanEnabled(false);
                showPopup(e);
            } else
            {
                jXMapKit1.getMainMap().setPanEnabled(true);
            }
            /* else if (SwingUtilities.isLeftMouseButton(e) && e.isControlDown())
             {
             //Save mouse pressed location
             Point p = e.getPoint();
             selectionRectangle.x = e.getX();
             selectionRectangle.y = e.getY();
             } */
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            //  jXMapKit1.getMainMap().setPanEnabled(true);
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            //Do nothing
        }

        private void showPopup(MouseEvent e)
        {
            popup.show(e.getComponent(), e.getX(), e.getY());
        }

        /**
         *
         * @param waypoints
         * @param mousePt
         * @return true if a new selection was made. false otherwise
         */
        private boolean selectAllInClick(List<Waypoint> waypoints, Point mousePt)
        {
            //The selected nodes are going to change regardless...
            selectedWayPoints.clear();
            boolean newFound = false;

            for (Waypoint w : waypoints)
            {
                ClusterWaypoint cw = (ClusterWaypoint) w;
                if (cw.contains(mousePt))
                {
                    if (!cw.getSelected())
                    {
                        newFound = true;
                    }
                    selectedWayPoints.add(cw);
                    cw.setSelected(true);
                } else
                {
                    if (cw.getSelected())
                    {
                        newFound = true;
                    }
                    cw.setSelected(false);

                }
                cw.setHighlighted(false);
            }
            return newFound;
        }
    }

    private ClusterWaypointPainter getPainter()
    {
        return (ClusterWaypointPainter) this.jXMapKit1.getMainMap().getOverlayPainter();
    }

    private JXMapViewer getMainMap()
    {
        return jXMapKit1.getMainMap();
    }
    /* private class ShowInProWinAction extends AbstractAction {

     public ShowInProWinAction(String name) 
     {
     super(name);
     }

     public void actionPerformed(ActionEvent e) 
     {
     Node.selectNone(nodes);
     Point p = mousePt.getLocation();
     Color color = control.hueIcon.getColor();
     Node n = new Node(p, radius, color, kind);
     n.setSelected(true);
     nodes.add(n);
     repaint();
     }
     }*/
}
