/*
 */
package edu.mit.ll.vizlinc.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Simple log utility.
 */
public class Log
{
    public static final String LOG_FILE = getLogFile();
    
    public static void appendLine(String s)
    {
        PrintWriter pw = null;
        try
        {
            Date now = new Date();
            pw = new PrintWriter(new BufferedWriter(new FileWriter(LOG_FILE, true)));
            pw.println(now + ":" + s);
        }
        catch (Exception e)
        {
            //Do nothing... we tried.
        }
        finally
        {
            if(pw != null)
            {
                pw.close();
            }
        }
        
        
    }

    private static String getLogFile()
    {
        File f = new File(System.getProperty("java.io.tmpdir"),"vizlinc_log.log");
        String s = f.getAbsolutePath();
        System.out.println("Writing log file to: " + s);
        return s;
    }

}
