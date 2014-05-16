package uk.ac.ebi.pride.spectracluster.util.comparator;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
import uk.ac.ebi.pride.spectracluster.hadoop.SpectrumInCluster;

import java.util.Comparator;

/**
 * Compare spectrum ids in addition to ClusterComparator
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterContentComparator extends ClusterComparator {
    public static final Comparator<IPeptideSpectrumCluster> INSTANCE = new ClusterContentComparator();

    private ClusterContentComparator() {
    }

    @Override
    public int compare(IPeptideSpectrumCluster o1, IPeptideSpectrumCluster o2) {
        String s1 = SpectrumInCluster.listClusterIds(o1);
        String s2 = SpectrumInCluster.listClusterIds(o2);
        if (!s1.equals(s2))
            return s1.compareTo(s2);  // differrent spectra
        // same spectra
        return super.compare(o1, o2);
    }
}
