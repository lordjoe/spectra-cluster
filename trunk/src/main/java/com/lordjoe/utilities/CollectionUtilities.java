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

    /**
      * because sometimes we find lists  containing nulls
      * this makes a copy of the list guaranteed not to contain nulls
      * @param inp  !null input list
      * @param <T>  type of the input list
      * @return !null output list as above
      */
     public static <T> List<T>  removeNulls(List<T> inp)
     {
         List<T> holder = new ArrayList<T>();
         for (T t : inp) {
              if(t != null)
                  holder.add(t);
         }

         return holder;
     }


    /**
      * because sometimes we find lists  containing duplicates
      * this makes a copy of the list guaranteed not to contain duplicates
     * but instead the first copy - i
      * @param inp  !null input list
      * @param <T>  type of the input list
      * @return !null output list as above
      */
     public static <T> List<T>  removeDuplicates(List<T> inp)
     {
         Set<T> theSet = new HashSet<T>();
         List<T> holder = new ArrayList<T>();
         for (T t : inp) {
              if(theSet.contains(t)) {
                  holder.add(t);
                  theSet.add(t); // no more copies
              }
         }

         return holder;
     }

}
