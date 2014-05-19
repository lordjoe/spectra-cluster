package uk.ac.ebi.pride.spectracluster.util.comparator;

import com.lordjoe.algorithms.CompareTo;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;

import java.util.Comparator;

/**
 * Compare cluster by a number of parameters
 *
 * Precursor m/z
 * Precursor charge
 * Spectra count
 * Hash Code
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterComparator implements Comparator<ICluster> {
    public static final Comparator<ICluster> INSTANCE = new ClusterComparator();

    @Override
    public int compare(ICluster o1, ICluster o2) {
        int ret = CompareTo.compare(o1.getPrecursorMz(), o2.getPrecursorMz());
        if (ret != 0) {
            return ret;
        }
        if (o1.getPrecursorCharge() != o2.getPrecursorCharge()) {
            return o1.getPrecursorCharge() < o2.getPrecursorCharge() ? -1 : 1;
        }
        if (o1.getClusteredSpectraCount() != o2.getClusteredSpectraCount()) {
            return o1.getClusteredSpectraCount() < o2.getClusteredSpectraCount() ? -1 : 1;
        }

        int hash1 = o1.hashCode();
        int hash2 = o2.hashCode();
        if (hash1 != hash2)
            return hash1 < hash2 ? -1 : 1;

        return 0;
    }
}
