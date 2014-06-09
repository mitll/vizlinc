/*
 */
package edu.mit.ll.vizlinc.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import javax.swing.JOptionPane;

/**
 * Text utility functions.
 */
public class TextUtils 
{
    /**
     * 
     * @param s
     * @return a string where every character after space(s) is upper case and every other character is lowercase
     */
    public static String toCamelCase(String s)
    {
        StringBuilder result = new StringBuilder(s.length());
        boolean up = true;
        
        for(int i = 0 ; i < s.length(); i++)
        {
            StringBuilder c = new StringBuilder(1);
            c.append(s.charAt(i));
            String charAsStr = c.toString();
            if(charAsStr.matches("\\s"))
            {
                up = true;
            }
            else
            {
                if(up)
                {
                    charAsStr = charAsStr.toUpperCase();
                    up = false;
                }
                else
                {
                    charAsStr = charAsStr.toLowerCase();
                }
            }
            
            result.append(charAsStr);
        }
        return result.toString();
    }
    
    /**
     * 
     * @param s string to print to file
     * @param filePath output file path 
     * @param encoding a valid character encoding
     */
    public static void printStringToFile(String s, String filePath, String encoding)
    {
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filePath), encoding));
            writer.println(s);
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
    
    /**
     *  Find the occurrence <code>occurrenceNum</code> of <code>target</code> in string <code>s</code>.
     * 
     * @param target target substring
     * @param occurrenceNum occurrence number
     * @param s input string
     * @return index of specified occurrence
     */
    public static int findIndexOfOccurrence(String target, int occurrenceNum, String s)
    {
        if(occurrenceNum < 1)
        {
            return -1;
        }
        
        int fromIndex = 0;
        int targetLength = target.length();
        for(int n = 1; n<=occurrenceNum; n++)
        {
            int newOcc = s.indexOf(target, fromIndex);
            if(newOcc == -1)
            {
                return -1;
            }
            fromIndex = newOcc  + targetLength;
        }
        
        int occIndex =  fromIndex - targetLength;
        if(occIndex >= s.length() || occIndex < 0)
        {
            return -1;
        }
        return occIndex;
    }
    
    /**
     * Returns input string with leading and trailing quotation marks
     * @param s
     * @return 
     */
    public static String surroundWithQuotationMarks(String s)
    {
        String result = s;
        if(!result.startsWith("\""))
        {
            result = "\"" + result;
        }
        if(!result.endsWith("\""))
        {
            result = result + "\"";
        }
        
        return result;
    }
}