/*
 * 
 */
package edu.mit.ll.vizlinc.components;

import com.google.common.primitives.Ints;
import edu.mit.ll.vizlinc.concurrency.VizLincLongTask;
import edu.mit.ll.vizlinc.model.AllFacetListModels;
import edu.mit.ll.vizlinc.model.DocNameId;
import edu.mit.ll.vizlinc.model.FacetListModel;
import edu.mit.ll.vizlinc.model.FacetTreeModel;
import edu.mit.ll.vizlinc.model.FacetValue;
import edu.mit.ll.vizlinc.graph.GraphManager;
import edu.mit.ll.vizlinc.model.DBManager;
import edu.mit.ll.vizlinc.model.GraphOperationListener;
import edu.mit.ll.vizlinc.model.KeywordEntity;
import edu.mit.ll.vizlinc.model.KeywordValue;
import edu.mit.ll.vizlinc.model.LocationListModel;
import edu.mit.ll.vizlinc.model.LocationValue;
import edu.mit.ll.vizlinc.model.OrganizationListModel;
import edu.mit.ll.vizlinc.model.OrganizationValue;
import edu.mit.ll.vizlinc.model.PersonListModel;
import edu.mit.ll.vizlinc.model.PersonValue;
import edu.mit.ll.vizlinc.model.VLQueryListener;
import edu.mit.ll.vizlinc.ui.elements.FacetValueTransferable;
import edu.mit.ll.vizlinc.utils.DBUtils;
import edu.mit.ll.vizlinc.utils.TextUtils;
import edu.mit.ll.vizlinc.utils.UIUtils;
import edu.mit.ll.vizlinc.utils.Utils;
import edu.mit.ll.vizlincdb.document.Document;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;
import edu.mit.ll.vizlinc.ui.elements.PersonListCellRenderer;
import java.awt.Frame;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Top component that displays the faceted search controls and entities extracted.
 */
@ConvertAsProperties(
        dtd = "-//edu.mit.ll.vizlinc.components//FacetedSearch//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "FacetedSearchTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "rankingmode", openAtStartup = true)
@ActionID(category = "Window", id = "edu.mit.ll.vizlinc.components.FacetedSearchTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_FacetedSearchAction",
        preferredID = "FacetedSearchTopComponent")
@Messages(
        {
    "CTL_FacetedSearchAction=Faceted Search",
    "CTL_FacetedSearchTopComponent=Search",
    "HINT_FacetedSearchTopComponent=This is a FacetedSearch window"
})
public final class FacetedSearchTopComponent extends TopComponent implements GraphOperationListener
{
    //User created components
    private JPopupMenu popup;
    private FacetValue popUpOnNode;
    private MapTopComponent mapWin;
    private VLQueryTopComponent queryWin;
    private Set<VLQueryListener> queryListeners;
    
    public FacetedSearchTopComponent()
    {
        if(!DBManager.getInstance().isReady())
        {
            return;
        }
        
        initComponents();
        setName(Bundle.CTL_FacetedSearchTopComponent());
        setToolTipText(Bundle.HINT_FacetedSearchTopComponent());
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);

        //Custom code
        sortByMentionButton.setSelected(true);
        
        popup = new JPopupMenu();
        JMenuItem addToQueryItem = new JMenuItem("Add Filter");
        addToQueryItem.setIcon(new ImageIcon(getClass().getResource("/edu/mit/ll/vizlinc/ui/icons/add_filter.png")));
        addToQueryItem.addActionListener(new AddToQueryPopupListener());
        popup.add(addToQueryItem);

        //Selection Listener
        locationList.getSelectionModel().addListSelectionListener(new HighlightInMapSelectionListener());
        peopleList.getSelectionModel().addListSelectionListener(new HighlightInGraphSelectionListener());
        
        //Set TransferHandlers on the facet lists so that entities can be dragged to the 
        //query area.
        locationList.setTransferHandler(new FacetListTransferHandler());
        orgList.setTransferHandler(new FacetListTransferHandler());
        peopleList.setTransferHandler(new FacetListTransferHandler());
        
        queryListeners = new HashSet<VLQueryListener>();
        //Set tab icons
        jTabbedPane1.setIconAt(0, UIUtils.getLocationsIcon());
        jTabbedPane1.setIconAt(1, UIUtils.getOrganizationsIcon());
        jTabbedPane1.setIconAt(2, UIUtils.getPeopleIcon());
        setFacetValueCountOnTabs();
        //Find map window and save a pointer to it. This should happen when the UI is ready
        final FacetedSearchTopComponent self = this;
        WindowManager.getDefault().invokeWhenUIReady(new Runnable()
        {
            @Override
            public void run()
            {
                mapWin = (MapTopComponent) WindowManager.getDefault().findTopComponent("MapTopComponent");
                queryWin = UIUtils.getQueryWindow();
                try {
                    GraphManager.getInstance().openGraph();
                    GraphManager.getInstance().addGraphOperationListener(self);
                } catch (Exception ex) 
                {
                    ex.printStackTrace();
                    String title = "Problem Accessing Graph File";
                    String msg = "Verify that the correct path to the graph file was specified in Tools > Options > VizLinc and restart the application. Error: "
                            + ex.getMessage();
                    Frame frame = WindowManager.getDefault().getMainWindow();
                    JOptionPane.showMessageDialog(frame, msg, title, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    
    public void addQueryListener(VLQueryListener listener)
    {
        this.queryListeners.add(listener);
    }
    
    private ListModel createLocationListModel()
    {
        LocationListModel lM = new LocationListModel(getSortCriterion());
        return lM;
    }
    
    private ListModel createOrganizationListModel()
    {
        OrganizationListModel oM = new OrganizationListModel(getSortCriterion());
        return oM;
    }
    
    private ListModel createPersonListModel()
    {
        PersonListModel pM = new PersonListModel(getSortCriterion(), showWeakAcrossDocPeopleBtn.isSelected());
       return pM;
    }
    
    
    private void maybePopUp(MouseEvent evt)
    {
        Object source = evt.getSource();
        if (isOneOfTheFacetLists(source))
        {
            JList selectedList = (JList) source;
            if (evt.isPopupTrigger())
            {
                //Select element closest to the click first
                int selectedIndex = selectedList.locationToIndex(evt.getPoint());
                if(selectedIndex == -1)
                {
                    //Do nothing as click has been outside the list.
                    return;
                }
                selectedList.setSelectedIndex(selectedIndex);
                //Get the selected value
                Object selectedVal = selectedList.getModel().getElementAt(selectedIndex);
                if (selectedVal != null && selectedVal instanceof FacetValue)
                {
                    popUpOnNode = (FacetValue) selectedVal;
                    popup.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        }
    }

    /**
     *
     * @return null if no entity value selected in tree.
     */
    private FacetValue getSelectedEntity()
    {
        Object selectedValue = getSelectedFacetList().getSelectedValue();
        if (selectedValue != null && selectedValue instanceof FacetValue)
        {
            return (FacetValue) selectedValue;
        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sortByButtonGroup = new javax.swing.ButtonGroup();
        jButton1 = new javax.swing.JButton();
        keywordTextField = new javax.swing.JTextField();
        keywordSearchBtn = new javax.swing.JButton();
        eraseLastFilterBtn = new javax.swing.JButton();
        sortByMentionButton = new javax.swing.JRadioButton();
        sortByAlphaButton = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        addFilterBtn = new javax.swing.JButton();
        clearFiltersBtn = new javax.swing.JButton();
        keywordFieldDropDown = new javax.swing.JComboBox();
        sortByDocButton = new javax.swing.JRadioButton();
        neighborhoodCheckbox = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        findInFacetTreeTextField = new javax.swing.JTextField();
        findNextInFacetTreeBtn = new javax.swing.JButton();
        findPreviousInFacetTreeBtn = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        locationList = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        orgList = new javax.swing.JList();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        peopleList = new javax.swing.JList();
        showWeakAcrossDocPeopleBtn = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.jButton1.text")); // NOI18N

        keywordTextField.setText(org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.keywordTextField.text")); // NOI18N
        keywordTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keywordTextFieldActionPerformed(evt);
            }
        });
        keywordTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                keyReleasedOnKeywordTextField(evt);
            }
        });

        keywordSearchBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/mit/ll/vizlinc/ui/icons/keyword_search.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(keywordSearchBtn, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.keywordSearchBtn.text")); // NOI18N
        keywordSearchBtn.setToolTipText(org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.keywordSearchBtn.toolTipText")); // NOI18N
        keywordSearchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keywordSearchBtnActionPerformed(evt);
            }
        });

        eraseLastFilterBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/mit/ll/vizlinc/ui/icons/remove_last_filter.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eraseLastFilterBtn, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.eraseLastFilterBtn.text")); // NOI18N
        eraseLastFilterBtn.setToolTipText(org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.eraseLastFilterBtn.toolTipText")); // NOI18N
        eraseLastFilterBtn.setEnabled(false);
        eraseLastFilterBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eraseLastFilterBtnActionPerformed(evt);
            }
        });

        sortByButtonGroup.add(sortByMentionButton);
        org.openide.awt.Mnemonics.setLocalizedText(sortByMentionButton, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.sortByMentionButton.text")); // NOI18N
        sortByMentionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByAlphaButtonActionPerformed(evt);
            }
        });

        sortByButtonGroup.add(sortByAlphaButton);
        org.openide.awt.Mnemonics.setLocalizedText(sortByAlphaButton, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.sortByAlphaButton.text")); // NOI18N
        sortByAlphaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByAlphaButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.jLabel1.text")); // NOI18N

        addFilterBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/mit/ll/vizlinc/ui/icons/add_filter.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addFilterBtn, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.addFilterBtn.text")); // NOI18N
        addFilterBtn.setToolTipText(org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.addFilterBtn.toolTipText")); // NOI18N
        addFilterBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFilterBtnActionPerformed(evt);
            }
        });

        clearFiltersBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/mit/ll/vizlinc/ui/icons/clear_filters.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(clearFiltersBtn, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.clearFiltersBtn.text")); // NOI18N
        clearFiltersBtn.setEnabled(false);
        clearFiltersBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFiltersBtnActionPerformed(evt);
            }
        });

        keywordFieldDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Document Name:", "Document Text:" }));
        keywordFieldDropDown.setSelectedIndex(1);
        keywordFieldDropDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keywordFieldDropDownActionPerformed(evt);
            }
        });

        sortByButtonGroup.add(sortByDocButton);
        org.openide.awt.Mnemonics.setLocalizedText(sortByDocButton, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.sortByDocButton.text")); // NOI18N
        sortByDocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByAlphaButtonPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(neighborhoodCheckbox, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.neighborhoodCheckbox.text")); // NOI18N
        neighborhoodCheckbox.setEnabled(false);
        neighborhoodCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                neighborhoodCheckboxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.jLabel3.text")); // NOI18N

        findInFacetTreeTextField.setText(org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.findInFacetTreeTextField.text")); // NOI18N
        findInFacetTreeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                keyReleasedOnFindTextBox(evt);
            }
        });

        findNextInFacetTreeBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/mit/ll/vizlinc/ui/icons/find_next.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(findNextInFacetTreeBtn, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.findNextInFacetTreeBtn.text")); // NOI18N
        findNextInFacetTreeBtn.setToolTipText(org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.findNextInFacetTreeBtn.toolTipText")); // NOI18N
        findNextInFacetTreeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findNextInFacetTreeBtnActionPerformed(evt);
            }
        });

        findPreviousInFacetTreeBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/mit/ll/vizlinc/ui/icons/find_previous.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(findPreviousInFacetTreeBtn, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.findPreviousInFacetTreeBtn.text")); // NOI18N
        findPreviousInFacetTreeBtn.setToolTipText(org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.findPreviousInFacetTreeBtn.toolTipText")); // NOI18N
        findPreviousInFacetTreeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findPreviousInFacetTreeBtnActionPerformed(evt);
            }
        });

        jTabbedPane1.setToolTipText(org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.jTabbedPane1.toolTipText")); // NOI18N

        locationList.setModel(createLocationListModel());
        locationList.setDragEnabled(true);
        locationList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                locationListMouseReleased(evt);
            }
        });
        jScrollPane2.setViewportView(locationList);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        orgList.setModel(createOrganizationListModel());
        orgList.setDragEnabled(true);
        orgList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                organizationListMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(orgList);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        peopleList.setModel(createPersonListModel());
        peopleList.setCellRenderer(new PersonListCellRenderer());
        peopleList.setDragEnabled(true);
        peopleList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                personListMouseReleased(evt);
            }
        });
        jScrollPane3.setViewportView(peopleList);

        org.openide.awt.Mnemonics.setLocalizedText(showWeakAcrossDocPeopleBtn, org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.showWeakAcrossDocPeopleBtn.text")); // NOI18N
        showWeakAcrossDocPeopleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showWeakAcrossDocPeopleBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(showWeakAcrossDocPeopleBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(showWeakAcrossDocPeopleBtn)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(FacetedSearchTopComponent.class, "FacetedSearchTopComponent.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addFilterBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(eraseLastFilterBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(clearFiltersBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(neighborhoodCheckbox))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(findInFacetTreeTextField)
                        .addGap(0, 0, 0)
                        .addComponent(findNextInFacetTreeBtn)
                        .addGap(3, 3, 3)
                        .addComponent(findPreviousInFacetTreeBtn))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(keywordFieldDropDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(keywordTextField)
                        .addGap(1, 1, 1)
                        .addComponent(keywordSearchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByAlphaButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sortByMentionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByDocButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(addFilterBtn)
                    .addComponent(eraseLastFilterBtn)
                    .addComponent(clearFiltersBtn)
                    .addComponent(neighborhoodCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(keywordFieldDropDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keywordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keywordSearchBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sortByMentionButton)
                    .addComponent(sortByAlphaButton)
                    .addComponent(jLabel1)
                    .addComponent(sortByDocButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(findInFacetTreeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(findNextInFacetTreeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(findPreviousInFacetTreeBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void eraseLastFilterBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eraseLastFilterBtnActionPerformed
        queryWin.pop();
        if (queryWin.isEmpty())
        {
            eraseLastFilterBtn.setEnabled(false);
        }
        performQueryAndUpdateUI();
    }//GEN-LAST:event_eraseLastFilterBtnActionPerformed
    
    private void sortByAlphaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByAlphaButtonActionPerformed
        if (evt.getSource() == sortByAlphaButton)
        {
            // model.setFacetValueOrder(FacetTreeModel.SORT_ALPHA);
            sortAllLists(FacetTreeModel.SORT_ALPHA);
        } else if (evt.getSource() == sortByMentionButton)
        {
            //model.setFacetValueOrder(FacetTreeModel.SORT_MENTIONS);
            sortAllLists(FacetTreeModel.SORT_MENTIONS);
        } else if (evt.getSource() == sortByDocButton)
        {
            //model.setFacetValueOrder(FacetTreeModel.SORT_DOC);
            sortAllLists(FacetTreeModel.SORT_DOC);
        }
    }//GEN-LAST:event_sortByAlphaButtonActionPerformed
    
    private void sortAllLists(int sortCriterion)
    {
        getLocationListModel().sort(sortCriterion);
        getOrganizationListModel().sort(sortCriterion);
        getPersonListModel().sort(sortCriterion);
    }
    
    private void addFilterBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addFilterBtnActionPerformed
    {//GEN-HEADEREND:event_addFilterBtnActionPerformed
        //Get selected facet value
        FacetValue selected = getSelectedEntity();
        //If nothing selected, prompt the user
        if (selected == null)
        {
            //TODO: Do this better. Only enable this button if something is selected.
            JOptionPane.showMessageDialog(UIUtils.getFirstParentComponent(this), "No entity selected.\nSelect an entity and try again.", "Nothing Selected",
                    JOptionPane.INFORMATION_MESSAGE);
            
        } else
        {
            addFacetValueToQuery(selected);
        }
    }//GEN-LAST:event_addFilterBtnActionPerformed
    
    private void clearFiltersBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearFiltersBtnActionPerformed
    {//GEN-HEADEREND:event_clearFiltersBtnActionPerformed
        queryWin.clear();
        performQueryAndUpdateUI();
    }//GEN-LAST:event_clearFiltersBtnActionPerformed
    
    private void keywordSearchBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_keywordSearchBtnActionPerformed
    {//GEN-HEADEREND:event_keywordSearchBtnActionPerformed
        retrieveKeywordAndPerformFullQuery();
    }//GEN-LAST:event_keywordSearchBtnActionPerformed
    
    private void retrieveKeywordAndPerformFullQuery()
    {
        try
        {
            //Get Text
            String keyword = this.keywordTextField.getText();
            String fieldText = this.keywordFieldDropDown.getSelectedItem().toString();
            int field = KeywordEntity.FIELD_DOC_TEXT;
            //TODO: create constants for these
            if (fieldText.equals("Document Name:"))
            {
                field = KeywordEntity.FIELD_DOC_NAME;
            }
            
            if (keyword != null && !keyword.isEmpty())
            {
                //update filter stack
                String searchString = TextUtils.surroundWithQuotationMarks(keyword);
                KeywordValue keywordEntity = new KeywordValue(new KeywordEntity(searchString, -1, -1, "Joel", -1, field));
                boolean pushed = queryWin.pushFilter(keywordEntity);
                //Clear search textbox
                this.keywordTextField.setText("");
                if (pushed)
                {
                    performQueryAndUpdateUI();
                }
            }
        } catch (ParseException e)
        {
            UIUtils.reportException(e);
        }
    }
    
    private void keywordTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_keywordTextFieldActionPerformed
    {//GEN-HEADEREND:event_keywordTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_keywordTextFieldActionPerformed
    
    private void keywordFieldDropDownActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_keywordFieldDropDownActionPerformed
    {//GEN-HEADEREND:event_keywordFieldDropDownActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_keywordFieldDropDownActionPerformed
    
    private void sortByAlphaButtonPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sortByAlphaButtonPerformed
    {//GEN-HEADEREND:event_sortByAlphaButtonPerformed
        sortByAlphaButtonActionPerformed(evt);
    }//GEN-LAST:event_sortByAlphaButtonPerformed
    
    private void neighborhoodCheckboxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_neighborhoodCheckboxActionPerformed
    {//GEN-HEADEREND:event_neighborhoodCheckboxActionPerformed
        performQueryAndUpdateUI(neighborhoodCheckbox.isSelected());
    }//GEN-LAST:event_neighborhoodCheckboxActionPerformed
    
    private void findNextInFacetTreeBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_findNextInFacetTreeBtnActionPerformed
    {//GEN-HEADEREND:event_findNextInFacetTreeBtnActionPerformed
        findInFacetTree(true);
    }//GEN-LAST:event_findNextInFacetTreeBtnActionPerformed
    
    private void findPreviousInFacetTreeBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_findPreviousInFacetTreeBtnActionPerformed
    {//GEN-HEADEREND:event_findPreviousInFacetTreeBtnActionPerformed
        findInFacetTree(false);
    }//GEN-LAST:event_findPreviousInFacetTreeBtnActionPerformed
    
    private void keyReleasedOnFindTextBox(java.awt.event.KeyEvent evt)//GEN-FIRST:event_keyReleasedOnFindTextBox
    {//GEN-HEADEREND:event_keyReleasedOnFindTextBox
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
            findInFacetTree(true);
        }
    }//GEN-LAST:event_keyReleasedOnFindTextBox
        
    private void keyReleasedOnKeywordTextField(java.awt.event.KeyEvent evt)//GEN-FIRST:event_keyReleasedOnKeywordTextField
    {//GEN-HEADEREND:event_keyReleasedOnKeywordTextField
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
            retrieveKeywordAndPerformFullQuery();
        }
    }//GEN-LAST:event_keyReleasedOnKeywordTextField
    
    private void locationListMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_locationListMouseReleased
    {//GEN-HEADEREND:event_locationListMouseReleased
        maybePopUp(evt);
    }//GEN-LAST:event_locationListMouseReleased
    
    private void organizationListMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_organizationListMouseReleased
    {//GEN-HEADEREND:event_organizationListMouseReleased
        maybePopUp(evt);
    }//GEN-LAST:event_organizationListMouseReleased

    private void personListMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_personListMouseReleased
    {//GEN-HEADEREND:event_personListMouseReleased
        maybePopUp(evt);
    }//GEN-LAST:event_personListMouseReleased

    private void showWeakAcrossDocPeopleBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_showWeakAcrossDocPeopleBtnActionPerformed
    {//GEN-HEADEREND:event_showWeakAcrossDocPeopleBtnActionPerformed
       boolean selected = showWeakAcrossDocPeopleBtn.isSelected();
       getPersonListModel().showWeakAcrossDocPeople(selected);
       setFacetValueCountOnTabs();
    }//GEN-LAST:event_showWeakAcrossDocPeopleBtnActionPerformed

    /**
     * Finds a value in a selected facet of the facet tree.
     *
     * @param forward if true, tries to find value down starting from the
     * current selection. false - searches up the tree (backwards) from current
     * selection.
     */
    private void findInFacetTree(boolean forward)
    {
        String searchString = findInFacetTreeTextField.getText();
         if (searchString.isEmpty())
         {
         //Do nothing
         return;
         }

         //get selected facet list
         JList selectedList = getSelectedFacetList();

         if (selectedList == null)
         {
            JOptionPane.showMessageDialog(UIUtils.getFirstParentComponent(this), "Select the facet you would like to search on.", "Select a Facet", JOptionPane.INFORMATION_MESSAGE);
            return;
         }
         
         //Get item currently selected
         Object selectedValue = selectedList.getSelectedValue();
         int indexFound = findValueStartingWith(selectedList, selectedValue, searchString, forward);

         //highlight intem found (if any) and scroll so that it is visible
         if(indexFound != -1)
         {
             selectedList.setSelectedIndex(indexFound);
             selectedList.ensureIndexIsVisible(indexFound);
         }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFilterBtn;
    private javax.swing.JButton clearFiltersBtn;
    private javax.swing.JButton eraseLastFilterBtn;
    private javax.swing.JTextField findInFacetTreeTextField;
    private javax.swing.JButton findNextInFacetTreeBtn;
    private javax.swing.JButton findPreviousInFacetTreeBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox keywordFieldDropDown;
    private javax.swing.JButton keywordSearchBtn;
    private javax.swing.JTextField keywordTextField;
    private javax.swing.JList locationList;
    private javax.swing.JCheckBox neighborhoodCheckbox;
    private javax.swing.JList orgList;
    private javax.swing.JList peopleList;
    private javax.swing.JCheckBox showWeakAcrossDocPeopleBtn;
    private javax.swing.JRadioButton sortByAlphaButton;
    private javax.swing.ButtonGroup sortByButtonGroup;
    private javax.swing.JRadioButton sortByDocButton;
    private javax.swing.JRadioButton sortByMentionButton;
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
    
    private int getSortCriterion()
    {
        if (sortByAlphaButton.isSelected())
        {
            return FacetTreeModel.SORT_ALPHA;
        } else if (sortByDocButton.isSelected())
        {
            //Default
            return FacetTreeModel.SORT_DOC;
        } else
        {
            return FacetTreeModel.SORT_MENTIONS;
        }
    }
    
    public List<LocationValue> getLocationsInFacetTree()
    {
        return getLocationListModel().getList();
    }
    
    private LocationListModel getLocationListModel()
    {
        return (LocationListModel) locationList.getModel();
    }
    
    private OrganizationListModel getOrganizationListModel()
    {
        return (OrganizationListModel) orgList.getModel();
    }
    
    private PersonListModel getPersonListModel()
    {
        return (PersonListModel) peopleList.getModel();
    }
    
    public List<PersonValue> getPeopleInFacetTree()
    {
        return getPersonListModel().getList();
    }

    public void addPersonWithIdToQuery(Integer facetId)
    {
        List<PersonValue> people = getPeopleInFacetTree();
        PersonValue person = null;
        for(PersonValue p : people)
        {
            if(p.getId().equals(facetId))
            {
                person = p;
                break;
            }
        }
        if(person == null)
        {
            throw new RuntimeException("Couldn't find person with id: " + facetId);
        }
        List<FacetValue> onePersonList = new ArrayList<FacetValue>(1);
        onePersonList.add(person);
        pushToStackAndPerformQuery(onePersonList);
    }

    /**
     * Select or deselect the facet values corresponding to the given facetIds.
     *
     * @param facetIds
     * @param select
     */
    public void adjustSelectionOfPeopleWithIds(List<Integer> facetIds, boolean select)
    {
        List<Integer> facetIndices = new ArrayList<Integer>();
        PersonListModel personListModel = getPersonListModel();
        for (Integer facetId : facetIds) {
            int index = personListModel.getIndexOfFacetValueWithId(facetId);
            // Ignore people not in the list: not sure if this is an error or just a possibility.
            if (index != -1) {
                facetIndices.add(index);
            }
        }

        // Use a sorted set so we can make the highest one visible.
       SortedSet<Integer> selectedIndices = new TreeSet<Integer>();
       for (int index: peopleList.getSelectedIndices())
       {
           selectedIndices.add(index);
       }
       
       if (select) //If adding a new selection
       {
           selectedIndices.addAll(facetIndices);
       }
       else
       {
           selectedIndices.removeAll(facetIndices);
       }
       
        peopleList.setSelectedIndices(Ints.toArray(selectedIndices));
        if (selectedIndices.size() > 0)
        {
            peopleList.ensureIndexIsVisible(selectedIndices.last());
        }
    }
    
    /**
     * Select the given facet values. Clear any existing selection. Scroll to
     * the first selection.
     *
     * @param facetValues
     */
    public void selectFacetValues(List<FacetValue> facetValues)
    {
         clearAllSelections();
         int firstLoc = -1;
         int firstOrg = -1;
         int firstPer = -1;
         List<Integer> selLocs = new LinkedList<Integer>();
         List<Integer> selOrgs = new LinkedList<Integer>();
         List<Integer> selPers = new LinkedList<Integer>();
         LocationListModel lm = getLocationListModel();
         OrganizationListModel om = getOrganizationListModel();
         PersonListModel pm = getPersonListModel();
         
         for (FacetValue fV : facetValues) 
         {
             int i = -1;
             if(fV instanceof LocationValue)
             {
                  i = selectFacetValuesAux(fV, lm, firstLoc, selLocs);
                 if(firstLoc == -1)
                 {
                     firstLoc = i;
                 }
             }
             else if (fV instanceof OrganizationValue)
             {
                 i = selectFacetValuesAux(fV, om, firstOrg, selOrgs);
                 if(firstOrg == -1)
                 {
                     firstOrg = i;
                 }
             }
             else if (fV instanceof PersonValue)
             {
                 i = selectFacetValuesAux(fV, pm, firstPer, selPers);
                 if(firstPer == -1)
                 {
                     firstPer = i;
                 }
             }
         }
         int[] locIndices = Utils.convertIntListToArray(selLocs);
         int[] orgIndices = Utils.convertIntListToArray(selOrgs);
         int[] perIndices = Utils.convertIntListToArray(selPers);
         locationList.setSelectedIndices(locIndices);
         if(firstLoc != -1)
         {
         locationList.ensureIndexIsVisible(firstLoc);
         }
         
         orgList.setSelectedIndices(orgIndices);
         if(firstOrg != -1)
         {
             orgList.ensureIndexIsVisible(firstOrg);
         }
         
         peopleList.setSelectedIndices(perIndices);
         if(firstPer != -1)
         {
            peopleList.ensureIndexIsVisible(firstPer);
         }
    }
    
    /**
     * Search list model for facet value, if found add corresponding index to selList and return this
     * index.
     * @param fV
     * @param lm
     * @param first
     * @param selList
     * @return 
     */
    private int selectFacetValuesAux(FacetValue fV, FacetListModel lm, int first, List<Integer> selList)
    {
        int i = lm.getIndexOfFacetValueWithId(fV.getId());
        if(i != -1)
        {
            selList.add(i);
        }
        return i;
    }
    
    private void enableAppropriateControls(final boolean queryFinished)
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    if (queryFinished)
                    {
                        if (queryWin.isEmpty())
                        {
                            eraseLastFilterBtn.setEnabled(false);
                            clearFiltersBtn.setEnabled(false);
                            //neighborhoodCheckbox.setEnabled(false);
                            //neighborhoodCheckbox.setSelected(false);
                        } else
                        {
                            //Enable filter eraser button
                            eraseLastFilterBtn.setEnabled(true);
                            clearFiltersBtn.setEnabled(true);
                            //neighborhoodCheckbox.setEnabled(true);
                        }
                        addFilterBtn.setEnabled(true);
                        setEnableFacetLists(true);
                        keywordSearchBtn.setEnabled(true);
                        keywordTextField.setEnabled(true);
                        showWeakAcrossDocPeopleBtn.setEnabled(true);
                        findNextInFacetTreeBtn.setEnabled(true);
                        findPreviousInFacetTreeBtn.setEnabled(true);
                        findInFacetTreeTextField.setEnabled(true);
                        sortByAlphaButton.setEnabled(true);
                        sortByDocButton.setEnabled(true);
                        sortByMentionButton.setEnabled(true);
                    } else
                    {
                        eraseLastFilterBtn.setEnabled(false);
                        clearFiltersBtn.setEnabled(false);
                        //neighborhoodCheckbox.setEnabled(false);
                        setEnableFacetLists(false);
                        addFilterBtn.setEnabled(false);
                        keywordSearchBtn.setEnabled(false);
                        keywordTextField.setEnabled(false);
                        showWeakAcrossDocPeopleBtn.setEnabled(false);
                        findNextInFacetTreeBtn.setEnabled(false);
                        findPreviousInFacetTreeBtn.setEnabled(false);
                        findInFacetTreeTextField.setEnabled(false);
                        sortByAlphaButton.setEnabled(false);
                        sortByDocButton.setEnabled(false);
                        sortByMentionButton.setEnabled(false);
                        
                    }
                }
                
                private void setEnableFacetLists(boolean b)
                {
                    jTabbedPane1.setEnabled(b);
                    locationList.setEnabled(b);
                    orgList.setEnabled(b);
                    peopleList.setEnabled(b);
                }
            });
        } catch (InterruptedException ex)
        {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private List<Document> performKeywordQuery(List<KeywordValue> keywords, List<Document> resultDocList)
    {
        if (keywords.isEmpty())
        {
            return resultDocList;
        }
        
        Query q = keywords.get(0).getQuery();
        if (keywords.size() > 1)
        {
            q = KeywordValue.joinKeywordsInOneQuery(keywords);
        }
        
        System.out.println(q.toString());
        return performKeywordQueryAux(q, resultDocList);
    }
    
    private List<Document> performKeywordQueryAux(Query combinedQuery, List<Document> resultDocList)
    {
        List<DocNameId> docNames = null;
        try
        {  //TODO: New thread here.
            //perform query
            docNames = DBUtils.keywordSearch(combinedQuery);
        } catch (Exception e)
        {
            UIUtils.reportException(e);
            return resultDocList;
        }
        
        if (docNames != null)
        {
            //subset doc list
            return subsetDocList(docNames, resultDocList);
        }
        
        return resultDocList;
    }
    
    private List<Document> subsetDocList(List<DocNameId> docNames, List<Document> currentDocs)
    {
        //Iterate over current list and keep only those that appear in this docNames list
        //Make map, indexing docNames
        Map<String, DocNameId> nameMap = new HashMap<String, DocNameId>();
        for (DocNameId ni : docNames)
        {
            nameMap.put(ni.getName(), ni);
        }
        
        List<Document> result = new LinkedList<Document>();
        
        for (Document d : currentDocs)
        {
            String currDocName = d.getName();
            if (nameMap.containsKey(currDocName))
            {
                DocNameId nameId = nameMap.get(currDocName);
                d.setLuceneId(nameId.getId());
                result.add(d);
            }
        }
        
        return result;
    }
    
    void pushToStackAndPerformQuery(List<FacetValue> entities)
    {
        boolean atLeastOnePushed = false;
        
        for (FacetValue e : entities)
        {
            atLeastOnePushed = atLeastOnePushed || queryWin.pushFilter(e);
        }
        if (atLeastOnePushed)
        {
            performQueryAndUpdateUI();
        }
    }
    
    private AllFacetListModels createNewFacetListModels(List<Document> resultDocList, boolean showNeighborhoodOnly)
    {
        AllFacetListModels allModels = null;
        int sort = getSortCriterion();
        
        if (queryWin.isEmpty())
        {
            allModels = new AllFacetListModels(sort, showWeakAcrossDocPeopleBtn.isSelected());
        } else
        {
            /**
             * * Disabled for now. TODO: Add text neighborhood functionality.              *
             * if(showNeighborhoodOnly) {
             *
             * }else {*
             */
            try
            {
                allModels = new AllFacetListModels(resultDocList, sort, showWeakAcrossDocPeopleBtn.isSelected());
            } catch (Exception e)
            {
                UIUtils.reportException(this, e);
            }
        }
        return allModels;
    }

    /*private void updateDocumentList(final List<Document> resultDocList)
     {
        
     //Update doc list
     SwingUtilities.invokeLater(new Runnable()
     {
     @Override
     public void run()
     {
     DocListTopComponent docListWin = (DocListTopComponent) WindowManager.getDefault().findTopComponent("DocListTopComponent");
     docListWin.setDocumentList(resultDocList);
     }
     });
     }*/

    //TODO: This method is inneficient since it is doing a linear search from the beginning of the list every time. Get the index of the selected value and 
    //search from there.
    private int findValueStartingWith(JList selectedList, Object selectedListItem, String searchString, boolean searchForward)
    {
        String lowerString = searchString.toLowerCase();
        int start = 0;
        List<? extends FacetValue> values = ((FacetListModel)selectedList.getModel()).getList();
        int firstInList = -1;
        int lastBeforeSelection = -1;
        boolean firstFound = false;

        //Find the highlighted item in the tree so that the search starts from there.
        if (selectedListItem != null && selectedListItem instanceof FacetValue)
        {
            for (; start < values.size(); start++)
            {
                FacetValue fv = values.get(start);
                if (fv.getText().toLowerCase().startsWith(lowerString))
                {
                    if (!fv.equals(selectedListItem))
                    {
                        lastBeforeSelection = start;
                    }
                    if (!firstFound)
                    {
                        firstInList = start;
                        firstFound = true;
                    }
                }
                
                if (fv.equals(selectedListItem))
                {
                    break;
                }
            }
            start++;
        }
        
        if (searchForward)
        {
            for (int i = start; i < values.size(); i++)
            {
                FacetValue fv = values.get(i);
                String lowerText = fv.getText().toLowerCase();
                if (lowerText.startsWith(lowerString))
                {
                    return i;
                }
            }

            //None found after current selection return the first found (before
            //the selection) or null.
            return firstInList;
        } 
        else
        {
            if (lastBeforeSelection != -1)
            {
                return lastBeforeSelection;
            } else
            {
                //Search from the end to the selected index
                for (int i = values.size() - 1; i >= start; i--)
                {
                    FacetValue fv = values.get(i);
                    String lowerText = fv.getText().toLowerCase();
                    if (lowerText.startsWith(lowerString))
                    {
                        return i;
                    }
                }
                return -1;
            }
        }
    }
    
    public List<FacetValue> getFacetValuesInFacetTree()
    {
        LocationListModel lm = getLocationListModel();
        OrganizationListModel om = getOrganizationListModel();
        PersonListModel pm = getPersonListModel();
        
        int totalSize = lm.getSize() + om.getSize() + pm.getSize();
        List<FacetValue> result = new ArrayList<FacetValue>(totalSize);
        result.addAll(lm.getList());
        result.addAll(om.getList());
        result.addAll(pm.getList());
        
        return result;
    }
    
    private void triggerAboutToExecuteQueryEvent()
    {
        for (VLQueryListener listener : this.queryListeners)
        {
            listener.aboutToExecuteQuery();
        }
    }
    
    private void triggerQueryFinishedEvent(List<Document> resultDocList)
    {
        List<LocationValue> locationsInFacetTree = getLocationsInFacetTree();
        List<PersonValue> peopleInFacetTree = getPeopleInFacetTree();
        for (VLQueryListener listener : this.queryListeners)
        {
            listener.queryFinished(resultDocList, locationsInFacetTree, peopleInFacetTree);
        }
    }

    @Override
    public void aboutToPerformGraphOperation() {
        enableAppropriateControls(false);
    }

    @Override
    public void graphOperationFinished() {
        enableAppropriateControls(true);
    }
    
    private void updateAllFacetLists(final AllFacetListModels newModels)
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    //Update this facet tree
                    //TODO: Update mention count
                    locationList.setModel(newModels.getLocationListModel());
                    orgList.setModel(newModels.getOrganizationListModel());
                    peopleList.setModel(newModels.getPersonListModel());
                    setFacetValueCountOnTabs();
                }
            });
        } catch (Exception e)
        {
            UIUtils.reportException(this, e);
        }
    }
    
    private boolean isOneOfTheFacetLists(Object source)
    {
        return source == locationList || source == orgList || source == peopleList;
    }

    private JList getSelectedFacetList()
    {
       int selectedTab = jTabbedPane1.getSelectedIndex();
        if (selectedTab == -1)
        {
            return null;
        }
        switch (selectedTab)
        {
            case 0:
                return locationList;
            case 1:
               return orgList;
            case 2:
                return peopleList;
            default:
                return null;
        } 
    }

    private void setFacetValueCountOnTabs()
    {
        //TODO: tab indices are hardcoded
        long numLocations = locationList.getModel().getSize();
        long numOrgs = orgList.getModel().getSize();
        long numPer = peopleList.getModel().getSize();
        DecimalFormat format = new DecimalFormat("###,###");
        String lCount = format.format(numLocations);
        String oCount = format.format(numOrgs);
        String pCount = format.format(numPer);
        jTabbedPane1.setTitleAt(0, "Locations " + lCount);
        jTabbedPane1.setTitleAt(1, "Organizations " + oCount);
        jTabbedPane1.setTitleAt(2, "People " + pCount);
    }

    private void clearAllSelections()
    {
        locationList.clearSelection();
        peopleList.clearSelection();
        orgList.clearSelection();
    }

    private class FacetListTransferHandler extends TransferHandler
    {
        public FacetListTransferHandler()
        {
        }
        
        @Override
        public int getSourceActions(JComponent c)
        {
            return COPY_OR_MOVE;
        }
        
        @Override
        protected Transferable createTransferable(JComponent c)
        {
            System.out.println("CreateTransferable()");
            System.out.println("Component class" + c.getClass().getName());
            JList selectedList = (JList) c;
            Object selectedValue = selectedList.getSelectedValue();
            System.out.println("Last component: " + selectedValue.getClass().getName());
            
            if (selectedValue instanceof FacetValue)
            {
                FacetValue fv = (FacetValue) selectedValue;
                return new FacetValueTransferable(fv);
            }
            
            return null;
        }
        
        @Override
        protected void exportDone(JComponent source, Transferable data, int action)
        {
            if (queryWin.wasNewFilterPushed())
            {
                performQueryAndUpdateUI();
            }
        }
    }
    
    private class AddToQueryPopupListener implements ActionListener
    {
        
        public AddToQueryPopupListener()
        {
        }
        
        @Override
        public void actionPerformed(ActionEvent e)
        {
            addFacetValueToQuery(popUpOnNode);            
        }
    }
    
    private class HighlightInGraphSelectionListener implements ListSelectionListener
    {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            List<Object> selectedValues = peopleList.getSelectedValuesList();
            List<PersonValue> peopleToHighlight = new ArrayList<PersonValue>();
            for (Object v : selectedValues)
            {
                if (v instanceof PersonValue)
                {
                    peopleToHighlight.add((PersonValue) v);
                }
            }
            highlightInGraph(peopleToHighlight);
        }
    }
    
    private class HighlightInMapSelectionListener implements ListSelectionListener
    {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            List<Object> selectedValues = locationList.getSelectedValuesList();
            Set<LocationValue> locationsToHighlight = new HashSet<LocationValue>();
            for (Object v : selectedValues)
            {
                if (v instanceof LocationValue)
                {
                    locationsToHighlight.add((LocationValue) v);
                }
            }
            mapWin.highlightInMap(locationsToHighlight);
        }
    }
    
    public void addFacetValueToQuery(FacetValue fv)
    {
        //Add filter to stack
        boolean pushed = queryWin.pushFilter(fv);
        if (pushed)
        {
            performQueryAndUpdateUI();
        }
    }
    
    public void highlightInGraph(Collection<PersonValue> people)
    {
        GraphManager.getInstance().highlightPeople(people);
    }
    
    private void performQueryAndUpdateUI()
    {
        performQueryAndUpdateUI(neighborhoodCheckbox.isSelected());
    }
    
    private void performQueryAndUpdateUI(final boolean showNeighborhoodOnly)
    {
        //Perform query in a different thread
        final VizLincLongTask task = new VizLincLongTask("Executing query...")
        {
            @Override
            public void execute()
            {
                ProgressTicket pt = this.getProgressTicket();
                Progress.setDisplayName(pt, "Executing query...");
                Progress.start(pt);
                performQueryAndUpdateUIInThread(showNeighborhoodOnly);
            }
        };
        task.run();
    }
    
    private void performQueryAndUpdateUIInThread(boolean showNeighborhoodOnly)
    {
        //Prepare this window's UI for query processing
        enableAppropriateControls(false);
        //Notify listeners
        triggerAboutToExecuteQueryEvent();
        //Perform query
        //1. Perform query - entities first
        List<FacetValue> filters = queryWin.getNonKeywordFilters();
        List<KeywordValue> keywords = queryWin.getKeywordFilters();
        
        List<Document> resultDocList = new ArrayList<Document>(0);
        try
        {
            resultDocList = DBUtils.getAllDocumentsForFacetValues(filters);
        } catch (SQLException ex)
        {
            UIUtils.reportException(ex);
        }

        //2. Perform keyword query
        //  JOptionPane.showMessageDialog(null, "Perform keyword query");
        resultDocList = performKeywordQuery(keywords, resultDocList);
        //Create new facet tree model based on query results
        //FacetTreeModel newModel = createNewFacetTreeModel(resultDocList, showNeighborhoodOnly);
        AllFacetListModels newModels = createNewFacetListModels(resultDocList, showNeighborhoodOnly);
        //Update this UI when all other components have been notified.
        //updateFacetTree(newModel);
        updateAllFacetLists(newModels);
        //Notify listeners
        triggerQueryFinishedEvent(resultDocList);
        //Enable query now that all components have been notified.
        enableAppropriateControls(true);
    }
}