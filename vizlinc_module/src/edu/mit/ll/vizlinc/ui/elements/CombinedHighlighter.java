/*
 * 
 */
package edu.mit.ll.vizlinc.ui.elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.mit.ll.vizlinc.components.DocViewTopComponent;

/**
 * Merges the entity-highlighted text with the keyword-highlighted text into one string.
 */
public class CombinedHighlighter
{
    private int ePoint;
    private int kPoint;
    private String entityText;
    private String keywordText;
    private boolean combinedAlready;
    private static final Pattern SEARCH_TERM_TAG_PATTERN = Pattern.compile("(\\Q" + DocViewTopComponent.KEYWORD_START_TAG + "\\E)|(" +
            DocViewTopComponent.KEYWORD_END_TAG + ")");
    private Pattern ENTITY_TAG_PATTERN = Pattern.compile("(<SPAN style=\"BACKGROUND-COLOR: (#[0-9a-fA-F]{6})\">)|(</SPAN>)");

    public CombinedHighlighter(String entityText, String keywordText)
    {
        this.entityText = entityText;
        this.keywordText = keywordText;
        combinedAlready = false;
        ePoint = 0;
        kPoint = 0;
    }
    
    public String combineHighlightings()
    {
        if(combinedAlready)
        {
            return this.entityText;
        }
        else
        {
            return doCombineHighlightings();
        }
    }
    
    /**
     * Inserts highlightings in keyword search highlighted text into the entity highlighted one.
     * @param entityHighlightedText
     * @param searchTermsHighlightedText
     * @return 
     */
    private String doCombineHighlightings()
    {
        int lastEPoint = -1;
        while(ePoint < entityText.length() && kPoint < keywordText.length())
        {
            lastEPoint = ePoint;
            char cE = entityText.charAt(ePoint);
            
            char cK = keywordText.charAt(kPoint);
            if(cE == '<') //Seems to be an entity tag
            {
                int tagSizeE = peakAtTagEntity(entityText, ePoint);
                if(tagSizeE != -1) //It is definetly one of the expected tags
                {
                    ePoint = ePoint + tagSizeE; //skip it!
                }
                else //seemed to be a tag but it isn't
                {
                    if(cK == '<') //if keyword text also has this character
                    {
                        //Check if it is because this a keyword tag
                        int tagSizeK = peakAtTagKeyword(keywordText, kPoint);
                        if(tagSizeK == -1) //It is not a tag, just an isolated character
                        {
                            ePoint++;
                            kPoint++;
                        }
                        else //It is a keyword tag, insert it. 
                        {
                            insertKeywordTag(tagSizeK);
                        }
                    }
                    else //mismatch between the two texts. Can't explain this; throw an exception
                    {
                        throw new RuntimeException("Error occurred when trying to highlight both search terms and entities. Character mismatch: keyword:" + 
                                keywordText.charAt(kPoint) + " entity:" + entityText.charAt(ePoint));
                    }
                }
            }
            else
            {
                if(cK == cE) //Two characters match and they are not '<' (becuase cE wasn't). skip current character in both texts
                {
                    ePoint++;
                    kPoint++;
                }
                else // cK could be the beginning of a tag
                {
                    if(cK == '<') //could be a tag
                    {
                        int tagSizeK = peakAtTagKeyword(keywordText, kPoint);
                        if(tagSizeK != -1) //it is indeed a tag, insert it
                        {
                            insertKeywordTag(tagSizeK);
                        }
                        else
                        {
                            throw new RuntimeException("Error occurred when trying to highlight both search terms and entities. Character mismatch: keyword:" + 
                                keywordText.charAt(kPoint) + " entity:" + entityText.charAt(ePoint));
                        }
                    }
                    else //it is not a tag and it is not equal to its corresponding character in the othe text. Mismatch!!
                    {
                        throw new RuntimeException("Error occurred when trying to highlight both search terms and entities. Character mismatch: keyword:" + 
                                keywordText.charAt(kPoint) + " entity:" + entityText.charAt(ePoint));
                    }
                }
            }
        }
        combinedAlready = true;
        return entityText;
    }

    private int peakAtTagKeyword(String keywordText, int kPoint)
    {
      return peakAtTag(keywordText, kPoint, SEARCH_TERM_TAG_PATTERN);
    }
    
    private int peakAtTag(String text, int startPoint, Pattern testPattern)
    {
        boolean sawEndOfTag = false;
        StringBuilder tag = new StringBuilder(10);

        for (int i = startPoint; i < text.length(); i++)
        {
            char c = text.charAt(i);
            tag.append(c);
            if (c == '>')
            {
                sawEndOfTag = true;
                break;
            }
        }

        if (sawEndOfTag)
        {
            //Checks that tag has the expected form
            Matcher m = testPattern.matcher(tag.toString());
            if(m.matches())
            {
                return tag.length();
            }
        }
        return -1;
    }

    private int peakAtTagEntity(String entityText, int ePoint)
    {
        return peakAtTag(entityText, ePoint, ENTITY_TAG_PATTERN);
    }

    private void insertKeywordTag(int tagSizeK)
    {
        //Update entity text to reflect the insertion
        String newTag = keywordText.substring(kPoint, kPoint + tagSizeK);
        this.entityText = entityText.substring(0, ePoint) +  
                newTag + entityText.substring(ePoint);
        
        //update pointers before continuing 
        ePoint += tagSizeK;
        kPoint += tagSizeK;
    }
}