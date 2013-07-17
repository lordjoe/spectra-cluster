package com.lordjoe.utilities;

import java.util.*;

/**
 * com.lordjoe.utilities.CollectionUtilities
 * useful general purpose functions on collections
 * User: Steve
 * Date: 7/17/13
 */
public class CollectionUtilities {

    /**
     * return the elements of the original list from 0 to end - 1
     *
     * @param orig !null list
     * @param end  last element copied - may be truncated to list size
     * @param <T>  type of list
     * @return elements 0 .. end - 1
     */
    public static <T> List<T> subList(List<T> orig, int end) {
        return subList(orig, 0, end);
    }

    /**
     * return the elements of the original list from 0 to end - 1
     *
     * @param orig  !null list
     * @param start firat element copied - must be > 0 and < end
     * @param end   last element copied - may be truncated to list size
     * @param <T>   type of list
     * @return elements 0 .. end - 1
     */
    public static <T> List<T> subList(List<T> orig, int start, int end) {
        end = Math.min(end, orig.size());
        if (start < 0 || start >= end)
            throw new IllegalArgumentException("bad start value " + start);
        List<T> holder = new ArrayList<T>();
        for (int i = start; i < end; i++) {
            holder.add(orig.get(i));
        }
        return holder;
    }
}
