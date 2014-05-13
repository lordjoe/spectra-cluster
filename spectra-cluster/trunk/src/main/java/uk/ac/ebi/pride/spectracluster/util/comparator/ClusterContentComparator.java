package uk.ac.ebi.pride.spectracluster.util.comparator;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.cluster.SpectrumInCluster;

/**
 * Compare spectrum ids in addition to ClusterComparator
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterContentComparator extends ClusterComparator {
    @Override
    public int compare(ISpectralCluster o1, ISpectralCluster o2) {
        String s1 = SpectrumInCluster.listClusterIds(o1);
        String s2 = SpectrumInCluster.listClusterIds(o2);
        if (!s1.equals(s2))
            return s1.compareTo(s2);  // differrent spectra
        // same spectra
        return super.compare(o1, o2);
    }
}
