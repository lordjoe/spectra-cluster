package com.lordjoe.hadoop;

import java.util.*;

/**
 * com.lordjoe.hadoop.ITextReducer
 *
 * @author Steve Lewis
 * @date 5/15/13
 */
public interface ITextReducer {
    public static ITextReducer[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ITextReducer.class;

    public TextKeyValue[] reduce(String key,List<String> values,Properties config);
}
