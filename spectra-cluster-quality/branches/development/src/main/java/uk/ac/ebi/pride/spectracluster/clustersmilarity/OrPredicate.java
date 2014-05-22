package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.TypedPredicate;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * uk.ac.ebi.pride.spectracluster.util.OrPredicate
 * construct with n predicates return true if any true
 * User: Steve
 * Date: 9/25/13
 */
@SuppressWarnings("UnusedDeclaration")
public class OrPredicate<T> implements TypedPredicate<T> {

    private final Set<TypedPredicate<T>> m_Clauses = new HashSet<TypedPredicate<T>>();

    public OrPredicate(final TypedPredicate<T>... pClauses) {
        addPredicates(pClauses);
    }

    public void addPredicates(TypedPredicate<T>... pClauses) {
        m_Clauses.addAll(Arrays.asList(pClauses));

    }

    /**
     * @param pT
     * @param otherdata - implementation specific and usually blank
     * @return what the implementation does
     */
    @Override
    public boolean apply(@Nonnull final T pT, final Object... otherdata) {
        for (TypedPredicate clause : m_Clauses) {
            //noinspection unchecked
            if (clause.apply(pT, otherdata))
                return true;
        }
        return false;
    }
}