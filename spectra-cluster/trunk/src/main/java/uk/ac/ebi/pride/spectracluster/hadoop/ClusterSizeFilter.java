package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.utilities.TypedPredicate;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import javax.annotation.Nonnull;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.ClusterSizeFilter
 * filter clustera on size supporting either  minimum only or minumum and maximum size
 * User: Steve
 * Date: 9/25/13
 */
@SuppressWarnings("UnusedDeclaration")
public class ClusterSizeFilter implements TypedPredicate<IPeptideSpectralCluster> {

    private final int m_MinimumSize;
    private final int m_MaximumSize;

    public ClusterSizeFilter(final int pMinimumSize) {
        this(pMinimumSize, Integer.MAX_VALUE); // no upper limit
    }

    public ClusterSizeFilter(final int pMinimumSize, final int maxSize) {
        m_MinimumSize = pMinimumSize;
        m_MaximumSize = maxSize;
    }

    public int getMinimumSize() {
        return m_MinimumSize;
    }

    public int getMaximumSize() {
        return m_MaximumSize;
    }

    /**
     * @param pT
     * @param otherdata - implementation specific and usually blank
     * @return what the implementation does
     */
    @Override
    public boolean apply(@Nonnull final IPeptideSpectralCluster pIPeptideSpectralCluster, final Object... otherdata) {
        int clusteredSpectraCount = pIPeptideSpectralCluster.getClusteredSpectraCount();
        if (clusteredSpectraCount < getMinimumSize())
            return false;
        //noinspection RedundantIfStatement
        if (clusteredSpectraCount > getMaximumSize())
            return false;
        return true;
    }
}
