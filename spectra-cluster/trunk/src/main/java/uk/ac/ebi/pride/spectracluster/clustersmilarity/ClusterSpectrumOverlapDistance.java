package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.google.common.collect.Sets;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterSpectrumOverlapDistance implements IClusterDistance {

    public static final String VERSION = "1.0";

    public static final ClusterSpectrumOverlapDistance INSTANCE = new ClusterSpectrumOverlapDistance();

    private ClusterSpectrumOverlapDistance() {
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public String getCurrentVersion() {
        return VERSION;
    }

    @Override
    public double distance(IPeptideSpectrumCluster c1, IPeptideSpectrumCluster c2) {
        Set<String> c1SpectralIds = c1.getSpectralIds();
        Set<String> c2SpectralIds = c2.getSpectralIds();

        Sets.SetView<String> intersection = Sets.intersection(c1SpectralIds, c2SpectralIds);
        double size = intersection.size();
        HashSet<String> c1Andc2 = new HashSet<String>(c1SpectralIds);
        c1Andc2.addAll(c2SpectralIds);


        int together = c1Andc2.size();
        together = Math.min(c1SpectralIds.size(), c2SpectralIds.size());
        if (together == 0)
            return 1;
        return 1 - (size / together);
    }

    @Override
    public double getMinimalMatchDistance() {
        return DEFAULT_MINIMAL_DISTANCE;
    }
}
