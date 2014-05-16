package uk.ac.ebi.pride.spectracluster.util.comparator;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import java.util.Comparator;

/**
 * Compare clusters by the number of spectra within
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterSizeComparator implements Comparator<IPeptideSpectralCluster> {
    public static final Comparator<IPeptideSpectralCluster> INSTANCE = new ClusterSizeComparator();

    private ClusterSizeComparator() {
    }

    @Override
    public int compare(IPeptideSpectralCluster o1, IPeptideSpectralCluster o2) {
        int diff = o1.getClusteredSpectraCount() - o2.getClusteredSpectraCount();
        if (diff != 0)
            return diff > 0 ? -1 : 1;
        return o1.compareTo(o2);
    }
}
