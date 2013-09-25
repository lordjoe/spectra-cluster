package uk.ac.ebi.pride.spectracluster.util;

import javax.annotation.*;

/**
 * uk.ac.ebi.pride.spectracluster.util.OrPredicate
 * construct with n predicates return true if any true
 * User: Steve
 * Date: 9/25/13
 */
@SuppressWarnings("UnusedDeclaration")
public class OrPredicate<T> implements TypedPredicate<T> {

    private final TypedPredicate<T>[] m_Clauses;

    public OrPredicate(final TypedPredicate<T>... pClauses) {
        m_Clauses = pClauses;
    }

    /**
     * @param pT
     * @param otherdata - implementation specific and usually blank
     * @return what the implementation does
     */
    @Override
    public boolean apply(@Nonnull final T pT, final Object... otherdata) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < m_Clauses.length; i++) {
            if(m_Clauses[i].apply(pT,  otherdata))
                return true;
          }
        return false;
    }
}
