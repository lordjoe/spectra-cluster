package uk.ac.ebi.pride.spectracluster.util;

import javax.annotation.*;

/**
 * uk.ac.ebi.pride.spectracluster.util.TypedPredicate
 * interface representing a single boolean decision - frequently
 * use to implement a filter
 * User: Steve
 * Date: 9/25/13
 */
public interface  TypedPredicate<T> {

    /**
     *
     * @param pT
     * @param otherdata - implementation specific and usually blank
     * @return  what the implementation does
     */
   public boolean apply(@Nonnull T pT,Object... otherdata);
}
