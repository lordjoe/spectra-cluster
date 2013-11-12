package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.google.common.collect.Sets;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterSpectrumOverlapDistance implements IClusterDistance {

    public static final String VERSION = "1.0";

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public String getCurrentVersion() {
        return VERSION;
    }

    @Override
    public double distance(ISpectralCluster c1, ISpectralCluster c2) {
        Set<String> c1SpectralIds = c1.getSpectralIds();
        Set<String> c2SpectralIds = c2.getSpectralIds();

        Sets.SetView<String> intersection = Sets.intersection(c1SpectralIds, c2SpectralIds);
        double size = intersection.size();
        HashSet<String> c1Andc2 = new HashSet<String>(c1SpectralIds);
        c1Andc2.addAll(c2SpectralIds);

        return 1 - (size / c1Andc2.size());
    }
}
