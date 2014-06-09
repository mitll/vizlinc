/*
 */
package edu.mit.ll.vizlinc.ui.elements;

import edu.mit.ll.vizlinc.utils.UIUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.mit.ll.vizlinc.components.DocViewTopComponent;
import edu.mit.ll.vizlincdb.entity.MentionLocation;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

/**
 * GUI component that, for a text document, displays: 
 * 1. a re-sizable column proportional to the document's length
 * 2. markers for each highlighted line at a height proportional to where they occur in the text
 */
public class HighlightSummaryPanel extends javax.swing.JPanel
{

    private static final int MARKER_HEIGHT = 3; // in pixels. this is the height of the marker
    //TODO: find out how calculate or access these properties. For now, hardcoded (determined empirically).
    private static final int TRACK_TOP = 19;    //Size in pixels of top arrow in scrollbar
    private static final int TRACK_BOTTOM = 17; //Size in pixels of bottom arrow in scrollbar
    private static final String HIGHLIGHT_REGEX = "<SPAN style=\"BACKGROUND-COLOR: (#[0-9a-fA-F]{6})";
    private static final Pattern HIGHLIGHT_PATTERN = Pattern.compile(HIGHLIGHT_REGEX);
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\Q" + DocViewTopComponent.KEYWORD_START_TAG + "\\E");
    private int numTextLines;
    private List<LineMark> lineNumbersToMark;
    private DocViewTopComponent parentDocViewComponent;
    
    //Pointer to current mention considered. Aux var used when updating display.
    private int currentMention;

    public HighlightSummaryPanel()
    {
        numTextLines = 0;
        lineNumbersToMark = new ArrayList<LineMark>(0);
    }

    public void setParentTopComponent(DocViewTopComponent parentDocViewTopComponent)
    {
        this.parentDocViewComponent = parentDocViewTopComponent;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        int numBins = getMaxNumMarkers();
        float linesPerBin = (float) numTextLines / (float) numBins;

        for (LineMark lineMark : this.lineNumbersToMark)
        {
            //Find where ther mark for that line should be
            //int width = this.getSize().width;
            double lineNum = (double) lineMark.getLineNum();
            int binNumber = (int) Math.floor(lineNum / (double) linesPerBin);
            int markY = binNumber * MARKER_HEIGHT;

            //Paint
            paintMarker(g, lineMark, markY);
            // int numTypes = lineMark.getNumberOfMarkTypes();
        }
    }

    public void summarizeNewDocument(StyledDocument document, List<MentionLocation> highlighted) throws BadLocationException
    {
        String text = document.getText(0, document.getLength());
        System.out.println("HTML String in HIghlighSummaryPanel: *********\n"
                + text + "\n***************************************");
        //String[] linesOfText = splitInLines(null);
        
        this.lineNumbersToMark = findLinesToMark(text, highlighted);

        repaint();
    }

    private int getMaxNumMarkers()
    {
        int height = this.getSize().height - TRACK_TOP - TRACK_BOTTOM;
        return height / MARKER_HEIGHT;
    }

    private String[] splitInLines(String htmlString)
    {
        return htmlString.split("<br/>");
    }

    /**
     * 
     * @param text
     * @param highlighted mention locations sorted by start offset.
     * @return 
     */
    private List<LineMark> findLinesToMark(String text, List<MentionLocation> highlighted)
    {
        List<LineMark> linesToMarkGUI = new LinkedList<LineMark>();
        int lineNum = 0;
        int lineStart = 0;
        currentMention = 0;
        
        for(int i = 0; i < text.length(); i++)
        {
            //Iterate until we find a newline character
            if(text.charAt(i) == '\n')
            {
                //Create a new line
                LineMark lm = new LineMark(lineNum, lineStart);
                boolean marked = markLine(lm, highlighted, i);
                if(marked)
                {
                    linesToMarkGUI.add(lm);
                }
                lineStart = i + 1;
                lineNum++;
            }
        }
        
        
        if(lineStart < text.length()) //Text doesn't end with a new line character
        {
            //consider last line
            LineMark lm = new LineMark(lineNum, lineStart);
            if(markLine(lm,highlighted, text.length() -1))
            {
                linesToMarkGUI.add(lm);
            }
            lineNum++;
        }
        
        //Update number of lines in current text
        numTextLines = lineNum;
        
        return linesToMarkGUI;
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        /****************************************************************/
//        List<LineMark> result = new LinkedList<LineMark>();
//        int charOffset = 0;
//        for (int i = 0; i < lines.length; i++)
//        {
//            String line = lines[i];
//            Set<Integer> highlightedTypes = extractHighlightedTypes(line);
//            if (!highlightedTypes.isEmpty())
//            {
//                LineMark lineMark = new LineMark(i, charOffset);
//                for (int t : highlightedTypes)
//                {
//                    lineMark.addType(t);
//                }
//                result.add(lineMark);
//            }
//            charOffset = charOffset + line.length() + 1;
//        }
//
//        return result;
    }

    private boolean containsHighlightedEntity(String line)
    {
        Matcher match = HIGHLIGHT_PATTERN.matcher(line);
        return match.find();
    }

    private void paintOne(Graphics g, LineMark lineMark, int markY, int componentWidth)
    {
        int type = -1;
        for (int t : lineMark.getMarkTypes())
        {
            type = t;
        }
        g.setColor(getTypeColor(type));
        g.fillRect(0, TRACK_TOP + markY, componentWidth, MARKER_HEIGHT);
    }

    private Color getTypeColor(int type)
    {
        Map<String, String> colorMap = DocViewTopComponent.COLOR_MAP;
        switch (type)
        {
            case LineMark.DATE_MARK:
                return UIUtils.getAsColor(colorMap.get(DocViewTopComponent.DATE_COLORMAP_KEY));

            case LineMark.LOC_MARK:
                return UIUtils.getAsColor(colorMap.get(DocViewTopComponent.LOCATION_COLORMAP_KEY));

            case LineMark.ORG_MARK:
                return UIUtils.getAsColor(colorMap.get(DocViewTopComponent.ORGANIZATION_COLORMAP_KEY));

            case LineMark.PERSON_MARK:
                return UIUtils.getAsColor(colorMap.get(DocViewTopComponent.PERSON_COLORMAP_KEY));
            default:
                return Color.WHITE;

        }
    }

    private void paintMarker(Graphics g, LineMark lineMark, int markY)
    {
        int numTypes = lineMark.getNumberOfMarkTypes();
        int width = getSize().width;
        if (numTypes == 1)
        {
            int type = lineMark.getMarkTypes().iterator().next();
            g.setColor(getTypeColor(type));

        } else
        {
            g.setColor(Color.BLACK);
        }

        int y = TRACK_TOP + markY;
        g.fillRect(0, y, width, MARKER_HEIGHT);
        if (g.getColor() == Color.WHITE) //needs an outline to be seen
        {
            g.setColor(Color.BLACK);
            ((Graphics2D) g).setStroke(new BasicStroke(1));
            g.drawRect(0, y, width - 1, MARKER_HEIGHT);
        }


        //Save info about where the obj was drawn (bounds)
        lineMark.setBounds(0, y, width - 2, MARKER_HEIGHT);
    }

    private int getRandomType()
    {
        Random rand = new Random();
        return rand.nextInt(4);
    }

    private Set<Integer> extractHighlightedTypes(String line)
    {
        Set<Integer> typesInThisLine = new HashSet<Integer>(LineMark.NUM_PREDEFINED_TYPES);
        Matcher match = HIGHLIGHT_PATTERN.matcher(line);
        Map<String, String> colorMap = DocViewTopComponent.COLOR_MAP;

        while (match.find())
        {
            String colorCode = match.group(1);
            if (colorCode.equals(colorMap.get(DocViewTopComponent.PERSON_COLORMAP_KEY)))
            {
                typesInThisLine.add(LineMark.PERSON_MARK);
            } else if (colorCode.equals(colorMap.get(DocViewTopComponent.ORGANIZATION_COLORMAP_KEY)))
            {
                typesInThisLine.add(LineMark.ORG_MARK);
            } else if (colorCode.equals(colorMap.get(DocViewTopComponent.LOCATION_COLORMAP_KEY)))
            {
                typesInThisLine.add(LineMark.LOC_MARK);
            } else if (colorCode.equals(colorMap.get(DocViewTopComponent.DATE_COLORMAP_KEY)))
            {
                typesInThisLine.add(LineMark.DATE_MARK);
            } else
            {
                throw new RuntimeException("Unknown color code.");
            }

            //Check to leave early
            if (typesInThisLine.size() == LineMark.NUM_PREDEFINED_TYPES)
            {
                break;
            }
        }

        //Check if there is a keyword in this line
        match = KEYWORD_PATTERN.matcher(line);
        if (match.find())
        {
            typesInThisLine.add(LineMark.KEYWORD_MARK);
        }

        return typesInThisLine;
    }

    /**
     * 
     * @param lm
     * @param highlighted
     * @return true if line contains a highlighted mention or keyword.
     *              false otherwise.
     */
    private boolean markLine(LineMark lm, List<MentionLocation> highlighted, int lineEnd)
    {
        //Check if current mention, and the ones following, fall on the line being considered
        MentionLocation m;
        int start = lm.docOffset;
        boolean marked = false;
        int mentionStart;
        while (currentMention < highlighted.size())
        {
            m = highlighted.get(currentMention);
            mentionStart = m.getTextStart();

            if (mentionStart > lineEnd)
            {
                break;
            }

            if (start <= mentionStart && mentionStart <= lineEnd)
            {
                //Mark line with appropriate type
                markLineWithType(lm, m.getMentionType());
                marked = true;
            }

            currentMention++;
        }
        
        return marked;
    }

    private void markLineWithType(LineMark lm, String mentionType)
    {
        if(mentionType.equals(DocViewTopComponent.PERSON_COLORMAP_KEY))
        {
            lm.addType(LineMark.PERSON_MARK);
        }
        else if(mentionType.equals(DocViewTopComponent.LOCATION_COLORMAP_KEY))
        {
            lm.addType(LineMark.LOC_MARK);
        }
        else if(mentionType.equals(DocViewTopComponent.ORGANIZATION_COLORMAP_KEY))
        {
            lm.addType(LineMark.ORG_MARK);
        }
        else if(mentionType.equals(DocViewTopComponent.DATE_COLORMAP_KEY))
        {
            lm.addType(LineMark.DATE_MARK);
        }
        else if (mentionType.equals("KEYWORD"))
        {
            lm.addType(LineMark.KEYWORD_MARK);
        }
        else
        {
            throw new RuntimeException("Unkown type: " + mentionType);
        }
    }

    private class LineMark
    {

        public static final int PERSON_MARK = 0;
        public static final int ORG_MARK = 1;
        public static final int DATE_MARK = 2;
        public static final int LOC_MARK = 3;
        public static final int KEYWORD_MARK = 4;
        private static final int NUM_PREDEFINED_TYPES = 5;
        private Set<Integer> types;
        /**
         * Zero-based line number.
         */
        private int lineNum;
        //zero-based offset of beginning of line in document
        private int docOffset;
        private Rectangle bounds;

        public LineMark(int lineNum, int docOffset)
        {
            this.lineNum = lineNum;
            this.docOffset = docOffset;
            types = new HashSet<Integer>(NUM_PREDEFINED_TYPES);
            bounds = new Rectangle(0, 0);
        }

        public int getNumberOfMarkTypes()
        {
            return types.size();
        }

        /**
         *
         * @param type one of the constants defined in this class
         */
        public void addType(int type)
        {
            this.types.add(type);
        }

        public Set<Integer> getMarkTypes()
        {
            return this.types;
        }

        public int getLineNum()
        {
            return this.lineNum;
        }

        public void setBounds(int x, int y, int width, int height)
        {
            this.bounds = new Rectangle(x, y, width, height);
        }

        public boolean contains(Point p)
        {
            return this.bounds.contains(p);
        }
    }
}