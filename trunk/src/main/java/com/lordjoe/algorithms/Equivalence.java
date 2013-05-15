package com.lordjoe.algorithms;

/**
 * com.lordjoe.algorithms.Equivalence
 *  like Comparator but weaker - used for similarity comparons
 * @author Steve Lewis
 * @date 5/11/13
 */
public interface Equivalence<T> {
    public static Equivalence[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = Equivalence.class;

    /**
     * like equals but weaker - says other is equivalent to this
     * @param other   poiibly null other object
     * @return  true if other is "similar enough to this"
     */
    public boolean equivalent(T o1,T o2) ;
}
