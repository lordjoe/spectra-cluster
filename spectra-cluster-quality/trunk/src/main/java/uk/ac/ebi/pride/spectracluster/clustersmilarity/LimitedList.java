package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.util.LimitedList
 * interface representing collection which is sorted and has limited size
 * User: Steve
 * Date: 9/25/13
 */
public interface LimitedList<T> {

    /**
     * return the maximum number of items
     *
     * @return
     */
    public int getMaxSize();

    /**
     * return the first item - not null if the list is not empty
     *
     * @return
     */
    public T first();

    /**
     * return the last item - not null if the list is not empty
     *
     * @return
     */
    public T last();

    /**
     * @return the contents as an array - must be sorted
     */
    public
    @Nonnull
    T[] toArray();

    /**
     * @return the contents as an array - must be sorted
     */
    public
    @Nonnull
    List<T> toList();

    /**
     * add to the list
     *
     * @param added
     */
    public boolean add(@Nonnull T added);

    /**
     * add to the list
     *
     * @param added
     */
    public void addAll(@Nonnull Iterable<T> added);

    /**
     * add to the list
     *
     * @param removed
     */
    public boolean remove(@Nonnull T removed);

    /**
     * like collection
     *
     * @return
     */
    public boolean isEmpty();

}
