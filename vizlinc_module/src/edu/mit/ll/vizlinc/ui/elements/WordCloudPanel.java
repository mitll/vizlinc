/*
 */
package edu.mit.ll.vizlinc.ui.elements;

import edu.mit.ll.vizlinc.model.FacetValue;
import edu.mit.ll.vizlinc.model.LocationValue;
import edu.mit.ll.vizlinc.model.OrganizationValue;
import edu.mit.ll.vizlinc.model.PersonValue;
import edu.mit.ll.vizlinc.utils.UIUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import edu.mit.ll.vizlinc.components.DocViewTopComponent;

/**
 * JPanel that displays a word cloud.
 */
public class WordCloudPanel extends JPanel
{
    public static final int PARAM_MENTIONS = 0;
    public static final int PARAM_DOCS = 1;
    
    private static final int MIN_FONT_SIZE = 9;
    private static final int MAX_FONT_SIZE = 48;
    public static int SORT_BY_ALPHA = 0;
    public static int SORT_BY_FREQ = 1;
    public static int SORT_BY_ENTITY_TYPE_A_Z = 2;
    
    public static final int TOPN_UNLIMITED = Integer.MAX_VALUE;
            
    private List<Tag> tagList;
    private int freqParameter;
    private int sortBy;
    private int topN;
    private TagMouseHandler mouseHandler;
    private static final Color HIGHLIGHTED_TAG_FOREGROUND = Color.white;

    public WordCloudPanel()
    {
        super();
        commonTasks();
    }

    public WordCloudPanel(LayoutManager manager)
    {
        super(manager);
        commonTasks();
    }

    private void commonTasks()
    {
        this.setBackground(Color.BLACK);
        this.setLayout(new WrapLayout(FlowLayout.LEFT));
        tagList = new LinkedList<Tag>();
        freqParameter = -1; //unassigned
        sortBy = WordCloudPanel.SORT_BY_ALPHA;
        topN = TOPN_UNLIMITED;
        this.mouseHandler = new TagMouseHandler(this);
    }

   /* @Override
    public Dimension getPreferredSize()
    {
        Container parent = getParent();
        if(parent != null)
        {
            Dimension parentSize = parent.getSize();
            return parentSize;
        }
        return super.getPreferredSize();
    }*/
    
    /**
     * 
     * @param facetValues - a list that can be modified by this method.
     * @param parameter either WordCloudPanel.PARAM_MENTIONS or WordCloudPanel.PARAM_DOCS
     * @param minFreq 
     */
    public void displayCloudOfEntities(List<FacetValue> facetValues)
    {
        //Remove all previous tags from cloud
        this.removeAll();
        this.tagList.clear();
        
        MinMax minMax = calcMinMaxFreq(facetValues);
        float min = minMax.getMin();
        float max = minMax.getMax();
        float range = max - min;
        
        float fontSizeDiff = MAX_FONT_SIZE - MIN_FONT_SIZE;
        
        //A;ways Sort facet values by frequency parameter first. Keep the topN (based on freq) and those are the 
        //ones that will be sorted based on the user-selected parameter (freq, a-z, etc)
        sortFacetValuesByFreq(facetValues);
        
        for(int i = 0; i < facetValues.size() && i < topN ; i++)
        {
            FacetValue f = facetValues.get(i);
            float freq = (float)getFreqFromEntity(f);
            //TODO: Min freq value must be calculated based on min of tags displayed. Not all tags in the list are displayed.
            //float multiplier = ((float) freq) / minFreqFloat;
            float score = (((freq - min)/range) * fontSizeDiff) + MIN_FONT_SIZE; 
            Tag tag = new Tag(f.getText(), score);
            tag.setColor(getColorForEntityType(f));
            tag.setFacetValue(f);
            this.tagList.add(tag);
        }
        
        //sort tag list if necessary
        //If not sorted by freq (order in which they are in the list), then re-order
        if(this.sortBy != SORT_BY_FREQ)
        {
            sortTagList();
        }
        displayTags();
    }
    
     private MinMax calcMinMaxFreq(List<FacetValue> fvs)
    {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        
        for(FacetValue f : fvs)
        {
           int freq =  getFreqFromEntity(f);
           if(freq < min)
           {
               min = freq;
           }
           if(freq > max)
           {
               max = freq;
           }
        }
        
        return new MinMax(min, max);
    }
     

    private int getFreqFromEntity(FacetValue f)
    {
        if(this.freqParameter == PARAM_MENTIONS)
        {
            
            return f.getNumMentionsShown();
        }
        else if(this.freqParameter == PARAM_DOCS)
        {
            
            return f.getNumDocumentsShown();
        }
        else
        {
            throw new IllegalArgumentException(freqParameter + " is not a valid frequency parameter");
        }
    }

    private void displayTags()
    {
        for(int i = 0; i < tagList.size(); i++) //TOP N tags have chosen previously and only those have been placed in the list
        {
            Tag t = this.tagList.get(i);
            int size = (int) Math.round(t.getScore());
            //int size = (int) t.getScore() * MIN_FONT_SIZE;
            //if(size > MAX_FONT_SIZE)
            //{
              //  size = MAX_FONT_SIZE;
            //}
            Font font = new Font("Tahoma", Font.PLAIN,size);
            //Collect tags first, sort and then display
            String text = t.getText().replaceAll(" ", "_").toLowerCase();
            TagLabel label = new TagLabel(text);
            label.setFont(font);
            label.setForeground(t.getColor());
            label.addMouseListener(this.mouseHandler);
            label.setCursor(new Cursor(Cursor.HAND_CURSOR));
            label.setTag(t);
            add(label);
        }
        
        invalidate();
        validate();
        repaint();
         
    }
    
    /**
     * The size of the tag will be calculated based on this parameter.
     * 
     * @param freqParameter  either {@link WordCloudPanel#PARAM_DOCS} or {@link WordCloudPanel#PARAM_MENTIONS}
     */ 
    public void setTagSizeBasedOn(int freqParameter)
    {
        if((freqParameter != PARAM_DOCS) && (freqParameter != PARAM_MENTIONS))
        {
            throw new IllegalArgumentException("Invalid frequency parameter: " + freqParameter);
        }
        
        this.freqParameter = freqParameter;
    }

    public void setTopN(int top)
    {
        this.topN = top;
    }

    private void sortTagList()
    {
        if (this.sortBy == WordCloudPanel.SORT_BY_ALPHA)
        {
            Collections.sort(this.tagList, new Comparator<Tag>()
            {
                @Override
                public int compare(Tag o1, Tag o2)
                {
                    return o1.getText().compareTo(o2.getText());
                }
            });
        }
        else if(this.sortBy == WordCloudPanel.SORT_BY_FREQ)
        {
            Collections.sort(this.tagList, new Comparator<Tag>()
            {
                @Override
                public int compare(Tag o1, Tag o2)
                {
                    Float f1 = new Float(o1.getScore());
                    Float f2 = new Float(o2.getScore());
                    
                    return f2.compareTo(f1);
                }
            });
        }
        else if(this.sortBy == WordCloudPanel.SORT_BY_ENTITY_TYPE_A_Z)
        {
            Collections.sort(this.tagList, new Comparator<Tag>()
            {
                @Override
                public int compare(Tag o1, Tag o2)
                {
                    //Compare type first
                    FacetValue fv1 = o1.getFacetValue();
                    FacetValue fv2 = o2.getFacetValue();
                    String t1 = fv1.getType();
                    String t2 = fv2.getType();
                    if(!t1.equals(t2))
                    {
                        return t1.compareTo(t2);
                    }
                    
                    //If types are equal compare text (alphabetically)
                    String text1 = fv1.getText();
                    String text2 = fv2.getText();
                    return text1.compareTo(text2);
                }
            });
        }
    }
    
    private void sortFacetValuesByFreq(List<FacetValue> facetValues)
    {
        Collections.sort(facetValues, new Comparator<FacetValue>()
            {
                @Override
                public int compare(FacetValue o1, FacetValue o2)
                {
                    Integer f1 = new Integer(getFreqFromEntity(o1));
                    Integer f2 = new Integer(getFreqFromEntity(o2));
                    return f2.compareTo(f1);
                }
            });
    }

    public void setSortBy(int sortBy)
    {
        this.sortBy = sortBy;
    }

    private Color getColorForEntityType(FacetValue f)
    {
        Map<String, String> colorMap = DocViewTopComponent.COLOR_MAP;
        if(f instanceof PersonValue)
        {
            return UIUtils.getAsColor(colorMap.get(DocViewTopComponent.PERSON_COLORMAP_KEY));
        }
        else if(f instanceof OrganizationValue)
        {
            return UIUtils.getAsColor(colorMap.get(DocViewTopComponent.ORGANIZATION_COLORMAP_KEY));
        }
        else if(f instanceof LocationValue)
        {
            return UIUtils.getAsColor(colorMap.get(DocViewTopComponent.LOCATION_COLORMAP_KEY));
        }
        return Color.WHITE;
    }
    
    
    private class Tag
    {
        private String text;
        private float score;
        private Color color;
        private FacetValue facetValue;
        
        Tag(String text, float score)
        {
            this.text = text;
            this.score = score;
        }
        
        public void setColor(Color color)
        {
            this.color = color;
        }
        
        public Color getColor()
        {
           return color;
        }

        public String getText()
        {
            return text;
        }

        public float getScore()
        {
            return score;
        }

        private void setFacetValue(FacetValue f)
        {
            this.facetValue = f;
        }

        private FacetValue getFacetValue()
        {
            return this.facetValue;
        }
    }
    
    private class MinMax
    {
        private int min;
        private int max; 
        
        public MinMax(int min, int max)
        {
            this.min = min;
            this.max = max;
        }

        public int getMin()
        {
            return min;
        }

        public int getMax()
        {
            return max;
        }
    }
    
    private class TagMouseHandler implements MouseListener
    {
        private Component parent;
        
        public TagMouseHandler(Component parent)
        {
            this.parent = parent;
        }
        
        @Override
        public void mouseClicked(MouseEvent e)
        {
            TagLabel tagLabel = (TagLabel) e.getSource();
            FacetValue fv = tagLabel.getTag().getFacetValue();
            addToQuery(fv);
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
            repaintLabel((TagLabel)e.getSource(), true);
            
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
            repaintLabel((TagLabel) e.getSource(), false);
        }

        private void repaintLabel(TagLabel source, boolean highlight)
        {
            Color color;
            //Border border;

            if (highlight) //Highlight label 
            {
                color = HIGHLIGHTED_TAG_FOREGROUND;
                //border = BorderFactory.createLineBorder(Color.WHITE, 1);
            } else
            {
                color = source.getTag().getColor();
                //border = BorderFactory.createEmptyBorder();
            }
            //source.setBorder(border);
            source.setForeground(color);
            source.repaint();
        }

        private void addToQuery(FacetValue fv)
        {
            //TODO: Center this message in the middle of the screen where the application is running
            int result = JOptionPane.showConfirmDialog(UIUtils.getFirstParentComponent(this.parent), "Add " + fv.getText() + " to query?");
            if(result == 0)
            {
                //addFacetValue to query
                UIUtils.getFacetedSearchWindow().addFacetValueToQuery(fv);
            }
        }
        
    }
    
    private class TagLabel extends JLabel
    {
        private Tag tag;

        public TagLabel()
        {
            super();
        }
        
        public TagLabel(Icon icon)
        {
            super(icon);
        }
        
        public TagLabel(Icon i, int hAlign)
        {
            super(i, hAlign);
        }
        public TagLabel(String s)
        {
            super(s);
        }
        public TagLabel(String s, Icon i, int hAlign)
        {
            super(s, i, hAlign);
        }
        public TagLabel(String s, int hAlign)
        {
            super(s, hAlign);
        }

        public Tag getTag()
        {
            return tag;
        }

        public void setTag(Tag tag)
        {
            this.tag = tag;
        }
    }
}
