/*
 */
package edu.mit.ll.vizlinc.utils;

import java.util.List;

/**
 *
 */
public class Utils
{
    public static int[] convertIntListToArray(List<Integer> intList)
    {
        int[] result = new int[intList.size()];
        for(int i = 0; i< intList.size(); i++)
        {
            result[i] = intList.get(i);
        }
        return result;
    }
}