/*
 * 
 */
package edu.mit.ll.vizlinc.db;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses string and returns a NormalizedDate object corresponding to the input
 * string.
 */
public class DateNormalizer {

    private static final String MONTH_GROUP = "(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)";
    private static final String MONTH_SHORT_GROUP = "(ene|feb|mar|abr|may|jun|jul|ago|sep|oct|nov|dic)";
    private static final String DAY_EXP = "([0-3]?[0-9])";
    private static final String DAY_WORDS = "uno|primero|segundo|dos|tres|cuatro|cinco|seis|siete|ocho|nuevo|diez|once|doce|trece|catorce|quince|dieciseis|diecisiete|" +
            "dieciocho|diecinueve|veinte|veintiuno|veintidos|veintitres|veinticuatro|veinticinco|veintiseis|veintisiete|veintiocho|veintinueve|treinta|treinta y uno";
    //Years in YYYY format from 1900 to 2012
    private static final String VALID_YEAR = "1\\.?9[0-9][0-9]|2\\.?0[01][0-9]";
    private static final String VALID_YEAR_SHORT = "[0-9][0-9]";
    
    

    private static String normalizeString(String date) {
        String result = date.toLowerCase();
        result = result.replaceAll("\\s+", " ");
        return result;
    }

    public DateNormalizer() {
    }

    public static NormalizedDate normalize(String date) {
        String nString = normalizeString(date);
        NormalizedDate nDate = null;

        String regex1 = DAY_EXP + " (de )?" + MONTH_GROUP + " (de|del( año)?) (" + VALID_YEAR + ")";
        String day = null;
        String month = null;
        String year = null;

        Pattern p1 = Pattern.compile(regex1);
        Matcher m1 = p1.matcher(nString);
        if (m1.find()) {
            System.out.println("Using regex1: " + regex1);
            day = m1.group(1);
            month = m1.group(3);

            year = m1.group(m1.groupCount());

            nDate = new NormalizedDate(day, month, year);
            System.out.println(date + " -> " + nDate);
            return nDate;

        }
        
        String regex2 = DAY_EXP + "[ ]?([/-])[ ]?" + MONTH_SHORT_GROUP + "[ ]?\\2[ ]?"
                + "(" + VALID_YEAR + "|" + VALID_YEAR_SHORT + ")";
        System.out.println("Using regex2: " + regex2);
        Pattern p2 = Pattern.compile(regex2);
        Matcher m2 = p2.matcher(nString);
        if (m2.find()) {
            day = m2.group(1);
            month = m2.group(3);
            year = m2.group(4);

            nDate = new NormalizedDate(day, month, year);
            System.out.println(date + " -> " + nDate);
            return nDate;
        }

//Month first
        String regex3 = MONTH_GROUP + " " + DAY_EXP + " del? (" + VALID_YEAR + ")";
        System.out.println("Regex3: " + regex3);
        Pattern p3 = Pattern.compile(regex3);
        Matcher m3 = p3.matcher(nString);
        if(m3.find())
        {
            day = m3.group(2);
            month = m3.group(1);
            year = m3.group(3);
            nDate = new NormalizedDate(day, month, year);
            System.out.println(date + " -> " + nDate);
            return nDate;
        }
        
        System.out.println(date + " -> " + nDate);
        return nDate;


    }
}
