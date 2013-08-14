package com.lordjoe.algorithms;

/**
 * com.lordjoe.algorithms.Equivalent
 *  like Comparable or equals but weaker - used for similarity comparisons
 *  it says an object can say it is equivalent to another object - not
 *  necessarily of the same Class - frequently implementing the same interface
 * @author Steve Lewis
 * @date 5/11/13
 */
public interface Equivalent<T> {
    public static Equivalent[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = Equivalent.class;

    /**
     * like equals but weaker - says other is equivalent to this
     * @param other   poiibly null other object
     * @return  true if other is "similar enough to this"
     */
    public boolean equivalent(T other) ;
}
