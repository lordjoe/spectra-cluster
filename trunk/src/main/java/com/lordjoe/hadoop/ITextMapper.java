package com.lordjoe.hadoop;

import java.util.*;
/**
 * com.lordjoe.hadoop.ITextMapper
 *
 * @author Steve Lewis
 * @date 5/15/13
 */
public interface ITextMapper {
    public static ITextMapper[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ITextMapper.class;

    public TextKeyValue[]  map(String key,String value,Properties config);
}
