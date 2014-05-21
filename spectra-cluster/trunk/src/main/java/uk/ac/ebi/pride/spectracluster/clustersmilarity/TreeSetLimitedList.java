package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.util.TreeSetLimitedList
 *
 * @author Steve Lewis
 * @date 13/11/13
 */
public class TreeSetLimitedList<T> implements LimitedList<T> {

    private final Class<? extends T> requiresClass;
    private final int maxSize;
    private final SortedSet<T> realSet;
    private final Comparator<? super T> sorter;

    /**
     * in this version requiresClass must implement comparable
     *
     * @param requiresClass !null class
     * @param maxSize       maxsize
     */
    public TreeSetLimitedList(Class<? extends T> requiresClass, int maxSize) {
        this.requiresClass = requiresClass;
        this.maxSize = maxSize;
        if (requiresClass.isAssignableFrom(Comparable.class))
            throw new IllegalArgumentException("class must be comparable");
        realSet = new TreeSet<T>();
        this.sorter = null;
    }

    /**
     * in this version a comparator is supplied
     *
     * @param requiresClass
     * @param maxSize
     * @param sorter
     */
    @SuppressWarnings("UnusedDeclaration")
    public TreeSetLimitedList(Class<? extends T> requiresClass, int maxSize, Comparator<? super T> sorter) {
        this.requiresClass = requiresClass;
        this.maxSize = maxSize;
        this.sorter = sorter;
        realSet = new TreeSet<T>(this.sorter);
    }

    /**
     * return the maximum number of items
     *
     * @return
     */
    @Override
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * return the first item - not null if the list is not empty
     *
     * @return
     */
    @Override
    public T first() {
        return realSet.first();
    }

    public boolean isEmpty() {
        return realSet.isEmpty();
    }

    /**
     * return the last item - not null if the list is not empty
     *
     * @return
     */
    @Override
    public T last() {
        return realSet.last();
    }

    /**
     * @return the contents as an array - must be sorted
     */
    @Nonnull
    @Override
    public T[] toArray() {

        //noinspection unchecked
        T[] ret = (T[]) Array.newInstance(requiresClass, realSet.size());
        return realSet.toArray(ret);
    }

    /**
     * @return the contents as an array - must be sorted
     */
    @Nonnull
    @Override
    public List<T> toList() {
        List<T> ret = new ArrayList<T>(realSet);
        if (sorter != null)
            Collections.sort(ret, sorter);
        else {
            //noinspection UnnecessaryLocalVariable
            List decast = ret;   // $%&%$# need to cast away generic to sort
            Collections.sort(decast);
        }
        return ret;

    }

    /**
     * add to list
     *
     * @param added
     * @return true of added is now in the list
     */
    @Override
    public boolean add(@Nonnull T added) {
        if (!requiresClass.isInstance(added))
            throw new IllegalArgumentException("requires type " + requiresClass);
        boolean ret = realSet.add(added);
        while (realSet.size() > getMaxSize()) {
            T last = realSet.last();
            if (last == added)
                ret = false;
            realSet.remove(last);
        }
        return ret;
    }

    /**
     * add to the list
     *
     * @param added
     */
    @Override
    public void addAll(@Nonnull Iterable<T> added) {
        for (T t : added) {
            add(t);
        }

    }

    /**
     * add to the list
     *
     * @param removed
     */
    @Override
    public boolean remove(@Nonnull T removed) {
        return realSet.remove(removed);

    }
}
