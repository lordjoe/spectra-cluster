package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.io.IClusterAppender;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.ChainedDotClusterAppender
 * append a cluster as all appenders suggest
 * User: Steve
 * Date: 9/25/13
 */
//todo: delete
@Deprecated
@SuppressWarnings("UnusedDeclaration")
public class ChainedClusterAppender implements IClusterAppender {
    private final IClusterAppender[] m_Clauses;

    public ChainedClusterAppender(final IClusterAppender... pClauses) {
        m_Clauses = pClauses;
    }

    /**
     * @param out       !null open appendale
     * @param data      !null cluster
     * @param OtherData any other data - implementation specific and usually blank
     * @return true if anything was appended otherwise false
     */
    @Override
    public boolean appendCluster(final Appendable out, final ISpectralCluster data, final Object... otherdata) {
        boolean anyThingDone = false;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < m_Clauses.length; i++) {
            if (!m_Clauses[i].appendCluster(out, data, otherdata))
                return false;
        }
        return anyThingDone;
    }


    /**
     * add whatever happens at the start
     *
     * @param out       !null open appendale
     * @param data      !null cluster
     * @param OtherData any other data - implementation specific and usually blank
     * @return true if anything was appended otherwise false
     */
    @Override
    public boolean appendStart(final Appendable out, final Object... otherdata) {
        boolean anyThingDone = false;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < m_Clauses.length; i++) {
            if (!m_Clauses[i].appendStart(out, otherdata))
                return false;
        }
        return anyThingDone;
    }

    /**
     * add whatever happens at the end
     *
     * @param out       !null open appendale
     * @param data      !null cluster
     * @param OtherData any other data - implementation specific and usually blank
     * @return true if anything was appended otherwise false
     */
    @Override
    public boolean appendEnd(final Appendable out, final Object... otherdata) {
        boolean anyThingDone = false;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < m_Clauses.length; i++) {
            if (!m_Clauses[i].appendEnd(out, otherdata))
                return false;
        }
        return anyThingDone;
    }

}
