package uk.ac.ebi.pride.spectracluster.util;

import javax.annotation.*;

/**
 * uk.ac.ebi.pride.spectracluster.util.TypedPredicate
 * interface representing a single boolean decision - frequently
 * use to implement a filter
 * User: Steve
 * Date: 9/25/13
 */
public interface TypedVisitor<T> {

    /**
     *
     * @param pT interface implemented by the visitor pattern
        */
   public void visit(@Nonnull T pT);
}
