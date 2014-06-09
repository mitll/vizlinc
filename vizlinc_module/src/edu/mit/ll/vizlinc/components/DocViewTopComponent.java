package edu.mit.ll.vizlinc.components;

import edu.mit.ll.vizlinc.concurrency.VizLincLongTask;
import edu.mit.ll.vizlinc.model.DBManager;
import edu.mit.ll.vizlinc.model.FacetValue;
import edu.mit.ll.vizlinc.model.KeywordEntity;
import edu.mit.ll.vizlinc.model.KeywordValue;
import edu.mit.ll.vizlinc.model.LocationValue;
import edu.mit.ll.vizlinc.model.PersonValue;
import edu.mit.ll.vizlinc.model.VLQueryListener;
import edu.mit.ll.vizlinc.ui.elements.CombinedHighlighter;
import edu.mit.ll.vizlinc.utils.DBUtils;
import edu.mit.ll.vizlinc.utils.UIUtils;
import edu.mit.ll.vizlincdb.document.Document;
import edu.mit.ll.vizlincdb.document.FoldingSpanishAnalyzer;
import edu.mit.ll.vizlincdb.util.VizLincProperties;
import edu.mit.ll.vizlinc.highlight.Formatter;
import edu.mit.ll.vizlinc.highlight.Fragmenter;
import edu.mit.ll.vizlinc.highlight.Highlighter;
import edu.mit.ll.vizlinc.highlight.InvalidTokenOffsetsException;
import edu.mit.ll.vizlinc.highlight.NullFragmenter;
import edu.mit.ll.vizlinc.highlight.QueryScorer;
import edu.mit.ll.vizlinc.highlight.SimpleHTMLFormatter;
import edu.mit.ll.vizlincdb.entity.Entity;
import edu.mit.ll.vizlincdb.entity.Mention;
import edu.mit.ll.vizlincdb.entity.MentionLocation;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
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

/**
 * Display document's content and highlights entities automatically extracted and/or present in query.
 */
@ConvertAsProperties(
    dtd = "-//edu.mit.ll.vizlinc.components//DocView//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "DocViewTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "contextmode", openAtStartup = true)
@ActionID(category = "Window", id = "edu.mit.ll.vizlinc.components.DocViewTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_DocViewAction",
preferredID = "DocViewTopComponent")
@Messages(
{
    "CTL_DocViewAction=DocView",
    "CTL_DocViewTopComponent=Document Viewer",
    "HINT_DocViewTopComponent=Shows document contents"
})
public final class DocViewTopComponent extends TopComponent implements VLQueryListener
{
    //Custom members
    public static final Map<String, String> COLOR_MAP = new HashMap<String, String>();
    public static final String PERSON_COLORMAP_KEY = "PERSON";
    public static final String LOCATION_COLORMAP_KEY = "LOCATION";
    public static final String ORGANIZATION_COLORMAP_KEY = "ORGANIZATION";
    public static final String DATE_COLORMAP_KEY = "DATE";
    public static String KEYWORD_START_TAG = "<SPAN style=\"color:blue;font-weight:bold\">";
    public static String KEYWORD_END_TAG = "</SPAN>";
    private static String TYPE_NONE_ATT = " TYPE=\"NONE\"";
    private static String PERSON_TAG_START = "<PERSON" + TYPE_NONE_ATT + ">";
    private static String PERSON_TAG_END = "</PERSON>";
    private static String LOC_TAG_START = "<LOCATION" + TYPE_NONE_ATT + ">";
    private static String LOC_TAG_END = "</LOCATION>";
    private static String DATE_TAG_START = "<DATE" + TYPE_NONE_ATT + ">";
    private static String DATE_TAG_END = "</DATE>";
    private static String ORG_TAG_START = "<ORGANIZATION" + TYPE_NONE_ATT + ">";
    private static String ORG_TAG_END = "</ORGANIZATION>";
    private static String STYLE_REGULAR = "regular";
    private static String STYLE_KEYWORD = "KEYWORD";
    
    private Document documentDisplayed;
    private Map<String, Color> colorForEntity;

    public DocViewTopComponent()
    {
        if(!DBManager.getInstance().isReady())
        {
            return;
        }
        
        initComponents();
        setName(Bundle.CTL_DocViewTopComponent());
        setToolTipText(Bundle.HINT_DocViewTopComponent());

        initCustomMembers();
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        //DefaultCaret caret = (DefaultCaret)this.documentEditorPane.getCaret();
        //caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        
        //Register as search listener
        final VLQueryListener thisAsAListener = this;
        WindowManager.getDefault().invokeWhenUIReady(new Runnable()
        {
            @Override
            public void run()
            {
                FacetedSearchTopComponent searchWin = UIUtils.getFacetedSearchWindow();
                if(searchWin != null) //if null, search window didn't instantiate therefore we have 
                                    //bigger problems.
                {
                    searchWin.addQueryListener(thisAsAListener);
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        docNameLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        highlightAllCheckbox = new javax.swing.JCheckBox();
        highlightSummaryPanel2 = new edu.mit.ll.vizlinc.ui.elements.HighlightSummaryPanel();
        locLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        documentEditorPane = new javax.swing.JEditorPane();

        docNameLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        docNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(docNameLabel, org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.docNameLabel.text")); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setIcon(UIUtils.getPeopleIcon());
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.jLabel1.toolTipText")); // NOI18N
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        jLabel2.setBackground(new java.awt.Color(255, 255, 0));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.jLabel2.text")); // NOI18N
        jLabel2.setOpaque(true);

        jLabel3.setBackground(new java.awt.Color(102, 255, 102));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.jLabel3.text")); // NOI18N
        jLabel3.setOpaque(true);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edu/mit/ll/vizlinc/ui/icons/date.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.jLabel4.text")); // NOI18N
        jLabel4.setToolTipText(org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.jLabel4.toolTipText")); // NOI18N
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabel5.setBackground(new java.awt.Color(153, 204, 255));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.jLabel5.text")); // NOI18N
        jLabel5.setOpaque(true);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel7.setIcon(UIUtils.getOrganizationsIcon());
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.jLabel7.text")); // NOI18N
        jLabel7.setToolTipText(org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.jLabel7.toolTipText")); // NOI18N
        jLabel7.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabel8.setBackground(new java.awt.Color(255, 153, 153));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.jLabel8.text")); // NOI18N
        jLabel8.setOpaque(true);

        highlightAllCheckbox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(highlightAllCheckbox, org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.highlightAllCheckbox.text")); // NOI18N
        highlightAllCheckbox.setEnabled(false);
        highlightAllCheckbox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                highlightAllCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout highlightSummaryPanel2Layout = new javax.swing.GroupLayout(highlightSummaryPanel2);
        highlightSummaryPanel2.setLayout(highlightSummaryPanel2Layout);
        highlightSummaryPanel2Layout.setHorizontalGroup(
            highlightSummaryPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        highlightSummaryPanel2Layout.setVerticalGroup(
            highlightSummaryPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 250, Short.MAX_VALUE)
        );

        locLabel.setIcon(UIUtils.getLocationsIcon());
        org.openide.awt.Mnemonics.setLocalizedText(locLabel, org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.locLabel.text")); // NOI18N
        locLabel.setToolTipText(org.openide.util.NbBundle.getMessage(DocViewTopComponent.class, "DocViewTopComponent.locLabel.toolTipText")); // NOI18N
        locLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jPanel1.setLayout(new java.awt.BorderLayout());

        documentEditorPane.setEditable(false);
        documentEditorPane.setContentType("text/rtf"); // NOI18N
        jScrollPane2.setViewportView(documentEditorPane);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(docNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(highlightSummaryPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(locLabel)
                .addGap(18, 18, 18)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 308, Short.MAX_VALUE)
                .addComponent(highlightAllCheckbox))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(docNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                        .addComponent(locLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(highlightAllCheckbox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(highlightSummaryPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void highlightAllCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highlightAllCheckboxActionPerformed
        displayDocument(false);
    }//GEN-LAST:event_highlightAllCheckboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel docNameLabel;
    private javax.swing.JEditorPane documentEditorPane;
    private javax.swing.JCheckBox highlightAllCheckbox;
    private edu.mit.ll.vizlinc.ui.elements.HighlightSummaryPanel highlightSummaryPanel2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel locLabel;
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

    void setDoc(Document doc)
    {
        this.documentDisplayed = doc;
        displayDocument(true);
    }

    /**
     * 
     * @param newDocument true if displaying a new document (different from the one being currently displayed
     *                    false - otherwise. This applies to the case when a document is re-displayed with different highlighting.
     */
    private void displayDocument(boolean isNewDocument)
    {
        final int verticalScrollbarVal;
        if(isNewDocument)
        {
            //Scroll to the top
            verticalScrollbarVal = 0;
        }
        else
        {
            //Remember scroll bar position
             verticalScrollbarVal = jScrollPane2.getVerticalScrollBar().getValue();
        }
        
        if(isNewDocument)
        {
            //Let the user now that the text shown will change soon
            documentEditorPane.setText("Opening document...");
        }
        
        setEnableViewComponents(false);
         //Get Filters applied
        VLQueryTopComponent queryWin = UIUtils.getQueryWindow();
        final List<FacetValue> filters = queryWin.getFilters();
        //Open Document in a different thread
        final VizLincLongTask task = new VizLincLongTask("Opening Document")
        {
            @Override
            public void execute()
            {
                ProgressTicket pt = this.getProgressTicket();
                Progress.setDisplayName(pt, "Opening Document...");
                Progress.start(pt);
                retrieveHighlightAndDisplay(filters, verticalScrollbarVal);
            }
        };
        task.run();
    }
    
    private void retrieveHighlightAndDisplay(List<FacetValue> filters, int verticalScrollbarVal)
    {
        String s = null;
        
        try
        {
            s = documentDisplayed.getText(DBManager.getInstance().getDB());
            //System.out.println("Text from DB:");
            //System.out.println(s);
        } catch (final Exception e)
        {
            e.printStackTrace();
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    UIUtils.reportException(e);
                }
            });
        }

        if (s == null || s.isEmpty())
        {
            final Component thisRef = this;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                    JOptionPane.showMessageDialog(UIUtils.getFirstParentComponent(thisRef), "Can't retrieve text for this document", "Error", JOptionPane.ERROR_MESSAGE);
                    documentEditorPane.setText("");
                }
            });
        }
        else
        {
            List<MentionLocation> highlighted = null;
            
            DocMentionsPair dm = null;
            if (highlightAllCheckbox.isSelected())
            {
                //s = highlightAllEntities(s);
                dm = highlightAllEntitiesStyle(s);
            } 
            else
            {
                try
                {
                    //s = highlightFilterMentionsOnly(filters, s);
                    dm = highlightFilterMentionsOnly(filters, s);
                } catch (final Exception e)
                {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            UIUtils.reportException(e);
                        }
                    });
                }
            }

            //Convert text to html
            //s = addHtmlElementAndNewLines(s);
            
            
            //Update UI
            displayHighlightedText(verticalScrollbarVal, dm.getDocument(), dm.getMentionLocations());
        }
    }

    private String highlightAllEntities(String s)
    { 
        List<MentionLocation> mentions = DBUtils.getAllMentionsForThisDoc(this.documentDisplayed.getId());
        return highlightMentionsInText(s, mentions);
    }
    
    private String highlightMentionsInText(String s, List<MentionLocation> mentions)
    {
        sortMentionsByStart(mentions);
        StringBuilder sb = new StringBuilder();
        int currChar = 0;
        for(MentionLocation m: mentions)
        {
            int start = m.getTextStart();
            int stop = m.getTextStop();
            //Append text between current character and start of mention
            sb.append(s.substring(currChar, start));
            
            //Insert appropriate tag
            String mentionType = m.getMentionType();
            String mentionText = s.substring(start, stop);
            System.out.println("Mention text: " + mentionText + "Mention type: " + mentionType + "doc id: " + documentDisplayed.getId());
            insertAppropriateTag(sb, mentionType, mentionText);
            currChar = stop;
        }
        sb.append(s.substring(currChar));
        
        return sb.toString(); 
    }

    private String addHtmlElementAndNewLines(String s)
    {
        return "<html>" + s.replaceAll("\n", "<br/>") + "</html>";
    }

    private String highlightFilterMentionsOnlyOld(List<FacetValue> filters, String docText) throws IOException, InvalidTokenOffsetsException
    {
        //Get all strings to highlight
        boolean foundNonKeywordEntities = false;
        
        try
        {
            List<KeywordValue> keywords = new LinkedList<KeywordValue>(); //groups all keywords before highlighting
            List<Integer> nonKeyWordEntityIds = new LinkedList<>();
            String entityHighlightedText = docText; //text copy that will be highlighted with entities (PERSON, LOCATION ...)
            for (FacetValue entity : filters)
            {
                if (entity instanceof KeywordValue) //find a more elegant way of doing this
                {
                    KeywordValue keywordEntity = (KeywordValue) entity;
                    if (keywordEntity.getField() == KeywordEntity.FIELD_DOC_TEXT)
                    {
                        keywords.add(keywordEntity);
                    }
                } 
                else
                {
                    foundNonKeywordEntities = true;
                    nonKeyWordEntityIds.add(entity.getId());
                }
            }
            List<MentionLocation> mentionsToHighlight;
            mentionsToHighlight = DBUtils.getMentionLocationsForEntityIdsInDocument(nonKeyWordEntityIds, this.documentDisplayed.getId());
            entityHighlightedText = highlightMentionsInText(docText, mentionsToHighlight);

            //1. Keyword highlighting
            String searchTermsHighlightedText = "";
                if(!keywords.isEmpty())
                {
                     searchTermsHighlightedText = highlightKeywordSearchTerms(docText, KeywordValue.joinKeywordsInOneQuery(keywords));
                }
                
            //Entities are already highlighted in their corresponding copy of the document text; clean up this copy.
                if(foundNonKeywordEntities)
                {
                    entityHighlightedText = removeAllEntityTags(entityHighlightedText);
                }
                
                
                //check what changes has been made so far.
                if(foundNonKeywordEntities && !searchTermsHighlightedText.isEmpty())
                {
                    //Combine the two highlightings (keyword and entities)
                    CombinedHighlighter cH = new CombinedHighlighter(entityHighlightedText, searchTermsHighlightedText);
                    String ret = cH.combineHighlightings();
                    return ret;
                }
                else if(foundNonKeywordEntities)
                {
                    return entityHighlightedText;
                }
                else if(!searchTermsHighlightedText.isEmpty())
                {
                    return searchTermsHighlightedText;
                } 
                else
                {
                    //Nothing done to original text, return original
                    return docText;
                }
        } catch (Exception e)
        {
            UIUtils.reportException(e);
            //If an exception occurs, return the document text unaltered
            return docText;
        }
    }
    
    private DocMentionsPair highlightFilterMentionsOnly(List<FacetValue> filters, String docText) throws IOException, InvalidTokenOffsetsException, BadLocationException
    {
        //Get all strings to highlight
        try
        {
            List<KeywordValue> keywords = new LinkedList<KeywordValue>(); //groups all keywords before highlighting
            List<Integer> nonKeyWordEntityIds = new LinkedList<>();
            for (FacetValue entity : filters)
            {
                if (entity instanceof KeywordValue) //find a more elegant way of doing this
                {
                    KeywordValue keywordEntity = (KeywordValue) entity;
                    if (keywordEntity.getField() == KeywordEntity.FIELD_DOC_TEXT)
                    {
                        keywords.add(keywordEntity);
                    }
                } 
                else
                {
                    nonKeyWordEntityIds.add(entity.getId());
                }
            }
            List<MentionLocation> mentionsToHighlight;
            mentionsToHighlight = DBUtils.getMentionLocationsForEntityIdsInDocument(nonKeyWordEntityIds, this.documentDisplayed.getId());
            
            //Keyword highlighting
            String searchTermsHighlightedText = "";
            if (!keywords.isEmpty())
            {
                searchTermsHighlightedText = highlightKeywordSearchTerms(docText, KeywordValue.joinKeywordsInOneQuery(keywords));
                if (!searchTermsHighlightedText.isEmpty())
                {
                    //Add keyword highlighting to doc
                    addKeywordsToMentionLocList(searchTermsHighlightedText, mentionsToHighlight);
                }
            }
            //Doc will contain either: 
            // 1) original text if query is empty
            // 2) entity and/or keyword-highlighted text: if query is non-empty
            StyledDocument doc = highlightMentionsInTextStyle(docText, mentionsToHighlight);
            return new DocMentionsPair(doc, mentionsToHighlight);
        } catch (Exception e)
        {
            UIUtils.reportException(e);
            //If an exception occurs, return the document text unaltered
            StyledDocument d = new DefaultStyledDocument();
            d.insertString(0, docText, null);
            return new DocMentionsPair(d, new ArrayList<MentionLocation>(0));
        }
    }
    
    private String getHexColorForEntity(String entity)
    {
        if(COLOR_MAP.containsKey(entity)) 
        {
            return COLOR_MAP.get(entity);
        }
        //Return default color
        throw new RuntimeException("Cannot find highlighting color for entity type: " + entity);
        
    }

    private void initCustomMembers()
    {
        COLOR_MAP.put(PERSON_COLORMAP_KEY, "#ffff00");
        COLOR_MAP.put(DATE_COLORMAP_KEY, "#66ff66");
        COLOR_MAP.put(LOCATION_COLORMAP_KEY, "#99ccff");
        COLOR_MAP.put(ORGANIZATION_COLORMAP_KEY, "#ff9999");

        documentDisplayed = null;

        //set parent component for accompanying highligthing panel
        this.highlightSummaryPanel2.setParentTopComponent(this);
        
        colorForEntity = new HashMap<String,Color>(4);
        colorForEntity.put(PERSON_COLORMAP_KEY, new Color(255, 255, 0));
        colorForEntity.put(DATE_COLORMAP_KEY, new Color(102, 255, 102));
        colorForEntity.put(LOCATION_COLORMAP_KEY, new Color(153,204, 255));
        colorForEntity.put(ORGANIZATION_COLORMAP_KEY, new Color(255, 153, 153));
    }

    private String removeAllEntityTags(String result)
    {
        result = result.replaceAll(PERSON_TAG_START, "");
        result = result.replaceAll(PERSON_TAG_END, "");
        result = result.replaceAll(LOC_TAG_START, "");
        result = result.replaceAll(LOC_TAG_END, "");
        result = result.replaceAll(ORG_TAG_START, "");
        result = result.replaceAll(ORG_TAG_END, "");
        result = result.replaceAll(DATE_TAG_START, "");
        result = result.replaceAll(DATE_TAG_END, "");
        result = result.replaceAll("<PAGE>", "");
        result = result.replaceAll("</PAGE>", "");

        return result;
    }

    
    public void scrollToLine(int documentPos)
    {
        try
        {
            documentEditorPane.scrollRectToVisible(documentEditorPane.modelToView(documentPos));
        } catch (BadLocationException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    private String highlightKeywordSearchTerms(String docText, Query query) throws IOException, InvalidTokenOffsetsException
    {
        //strip out all entity tags
        //TODO: stripping tags prevents highlighting of any entities
        String textWOAnnotations = removeAllEntityTags(docText);
        //***********************
        System.out.println("QUERY: " + query);
        System.out.println("**TEXT WITHOUT ANNOTATIONS ****\n");
        System.out.println("Number of characters: " + textWOAnnotations.length());
        String field = VizLincProperties.P_DOCUMENT_TEXT;
        Analyzer analyzer =  new FoldingSpanishAnalyzer(VizLincProperties.VIZLINCDB_LUCENE_VERSION);
        TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(textWOAnnotations));
        QueryScorer scorer = new QueryScorer(query, field);
        Fragmenter fragmenter = new NullFragmenter();
        Formatter formatter = new SimpleHTMLFormatter(KEYWORD_START_TAG, KEYWORD_END_TAG);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        highlighter.setMaxDocCharsToAnalyze(textWOAnnotations.length() + 1);
        highlighter.setTextFragmenter(fragmenter);
        String result = highlighter.getBestFragment(tokenStream, textWOAnnotations);
        
        return result;
    }

    @Override
    public void aboutToExecuteQuery()
    {
        System.err.println("In Doc View: aboutToExecuteQuery()");
        //Disable controls
        setEnableViewComponents(false);
        System.err.println("In Doc View: aboutToExecuteQuery()... DONE");
    }

    @Override
    public void queryFinished(final List<Document> newDocuments, List<LocationValue> locationsInFacetTree, List<PersonValue> peopleInFacetTree)
    {
        System.err.println("In Doc View: queryFinished()");
        if (this.documentDisplayed != null)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    System.err.println("Displaying document. " + Thread.currentThread());
                    String docName = documentDisplayed.getName();
                    if (!docName.isEmpty()) //If displaying a doc title:
                    {
                        //Check whether it is part of the new document set
                        if (!existsInDocList(docName, newDocuments))
                        {
                            //Change font to show that document is not part of current working set
                            Font newFont = docNameLabel.getFont().deriveFont(Font.ITALIC | Font.BOLD);
                            docNameLabel.setFont(newFont);
                            docNameLabel.setText("*" + docName + " (not in current results)");
                            //Do not enable the components in this window. They can't be used if doc is not part of the 
                            //working set
                        } else
                        {
                            //Display document properly
                            //TODO: call to this method fetches document content again and then highlights appropriately
                            //Consider doing this without accessing the db for document content.
                            displayDocument(false);
                        }
                    }
                    System.err.println("Displaying document. " + Thread.currentThread() + "... DONE");
                }
            });
        }
    }

    private void setEnableViewComponents(boolean b)
    {
        this.highlightAllCheckbox.setEnabled(b);
        this.documentEditorPane.setEnabled(b);
    }

    private boolean existsInDocList(String docName, List<Document> newDocuments)
    {
        //Linear search, for now...
        //TODO: explore improving speed
        for(Document doc: newDocuments)
        {
            if(doc.getName().equals(docName))
            {
                return true;
            }
        }
        return false;
        
    }

    private void displayHighlightedText(final int verticalScrollbarVal, final StyledDocument doc, final List<MentionLocation> mentionsHighlighted)
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                @Override
                public void run()
                {
                    long start = System.currentTimeMillis();
                    System.err.println("Displaying text in component...");
                    
                    //documentEditorPane.setText(s);
                    //Reader stringReader = new StringReader(s);
                    //HTMLEditorKit htmlKit = new HTMLEditorKit();
                    //HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
                    try
                    {
                      //  htmlKit.read(stringReader, htmlDoc, 0);
                        documentEditorPane.setDocument(doc);
                    } catch(Exception e)
                    {
                        e.printStackTrace();
                        UIUtils.reportException(e);
                    }
                    docNameLabel.setText(documentDisplayed.getName());
                    docNameLabel.setFont(docNameLabel.getFont().deriveFont(Font.PLAIN));
                    long end = System.currentTimeMillis();
                    System.err.println("Took: " + (end - start));
                    //UPdate markers next to scrollbar
                    System.err.println("Summarizing highlighting");
                    start = System.currentTimeMillis();
                    try
                    {
                        highlightSummaryPanel2.summarizeNewDocument(doc, mentionsHighlighted);
                    } catch(Exception e)
                    {
                        e.printStackTrace();
                        UIUtils.reportException(e);
                    }
                    
                    //enable components
                    setEnableViewComponents(true);
                   // Log.appendLine("\n\n" + s + "\n\n");
                    end = System.currentTimeMillis();
                    System.err.println("Took " + (end - start));
                }
            });
            
            SwingUtilities.invokeLater(new Runnable()
            {

                @Override
                public void run()
                {
                    jScrollPane2.getVerticalScrollBar().setValue(verticalScrollbarVal);
                }
            });
        } catch (final Exception ex)
        {
            SwingUtilities.invokeLater( new Runnable() 
            {

                @Override
                public void run()
                {
                    UIUtils.reportException(ex);
                }
            });
        } 
    }

    private void sortMentionsByStart(List<MentionLocation> mentions)
    {
        Collections.sort(mentions, new Comparator<MentionLocation>(){

            @Override
            public int compare(MentionLocation o1, MentionLocation o2)
            {
                int s1 = o1.getTextStart();
                int s2 = o2.getTextStart();
                return Integer.compare(s1, s2);
            }
    });
    }

    private void insertAppropriateTag(StringBuilder sb, String type, String mentionText)
    {
        sb.append("<SPAN style=\"BACKGROUND-COLOR: ").append(getHexColorForEntity(type)).append("\">").append(mentionText);
        sb.append("</SPAN>");
    }

    private DocMentionsPair highlightAllEntitiesStyle(String s)
    {
        List<MentionLocation> mentions = DBUtils.getAllMentionsForThisDoc(this.documentDisplayed.getId());
        StyledDocument doc = highlightMentionsInTextStyle(s, mentions);
        return new DocMentionsPair(doc, mentions);
    }
    
    private StyledDocument highlightMentionsInTextStyle(String s, List<MentionLocation> mentions)
    {
        sortMentionsByStart(mentions);
        //StringBuilder sb = new StringBuilder();
        DefaultStyledDocument doc = new DefaultStyledDocument();
        addStylesToDoc(doc);
        
        int currChar = 0;
        for (MentionLocation m : mentions)
        {
            int start = m.getTextStart();
            int stop = m.getTextStop();
            //Append text between current character and start of mention
            // sb.append(s.substring(currChar, start));
            try
            {
                doc.insertString(doc.getLength(), s.substring(currChar, start), doc.getStyle(STYLE_REGULAR));
                //Insert appropriate tag
                String mentionType = m.getMentionType();
                String mentionText = s.substring(start, stop);
                //System.out.println("Mention text: " + mentionText + "Mention type: " + mentionType + "doc id: " + documentDisplayed.getId());
                //insertAppropriateTag(sb, mentionType, mentionText);
                doc.insertString(doc.getLength(), mentionText, doc.getStyle(mentionType));
                currChar = stop;
            } catch (Exception e)
            {
                e.printStackTrace();
                UIUtils.reportException(e);
            }
        }
        try
        {
            doc.insertString(doc.getLength(), s.substring(currChar), doc.getStyle(STYLE_REGULAR));
            //System.out.println("Before returning: \n++++" + doc.getText(0, doc.getLength()) + "+++");
        } catch (Exception e) //TODO: Handle Exceptions...
        {
            System.err.println("EXCEPTION!!");
            e.printStackTrace();
            UIUtils.reportException(e);
        }
        return doc;
    }

    private void addStylesToDoc(DefaultStyledDocument doc)
    {
        Style def = StyleContext.getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle(STYLE_REGULAR, def);
        //StyleConstants.setFontFamily(def, "SansSerif");

        for(String k : colorForEntity.keySet())
        {
            Style style = doc.addStyle(k, regular);
            StyleConstants.setBackground(style, colorForEntity.get(k));
        }
        
        Style keywordStyle = doc.addStyle(STYLE_KEYWORD, regular);
        StyleConstants.setBold(keywordStyle, true);
        StyleConstants.setForeground(keywordStyle, Color.BLUE);
        
        
//        StyleConstants.setBold(style, true);
//        StyleConstants.setBackground(style, Color.YELLOW);
    }

    private void addKeywordsToMentionLocList(String searchTermsHighlightedText, List<MentionLocation> mentionsToHighlight)
    {
        Pattern p = Pattern.compile(KEYWORD_START_TAG + "(.+?)" + KEYWORD_END_TAG, Pattern.DOTALL);
        int tagsLength = KEYWORD_END_TAG.length() + KEYWORD_START_TAG.length();
        int tagPairsFound = 0;
        Matcher m = p.matcher(searchTermsHighlightedText);
        while(m.find())
        {
            String keyword = m.group(1);
            int start = m.start() - (tagsLength * tagPairsFound);
            int stop = start + keyword.length();
            mentionsToHighlight.add(new MentionLocation(-1, -1, -1, start, stop, "KEYWORD"));
            tagPairsFound ++;
        }
    }

    
    private class DocMentionsPair
    {
        private StyledDocument doc;
        private List<MentionLocation> mentions;
        
        public DocMentionsPair(StyledDocument d, List<MentionLocation> mls)
        {
            doc = d;
            mentions = mls;
        }
        
        public StyledDocument getDocument()
        {
            return doc;
        }
        
        public List<MentionLocation> getMentionLocations()
        {
            return mentions;
        }
        
    }
}