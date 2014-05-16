package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.FractionInClustersOfSize
 * User: Steve
 * Date: 1/16/14
 */
public class FractionInClustersOfSize {
    public static final int[] TEST_SIZES = {0, 2, 8, 30};

    private final List<IPeptideSpectrumCluster> clusters = new ArrayList<IPeptideSpectrumCluster>();

    public int getNumberInClusterOfSize(int clusterSize) {
        Set<String> clusterIds = new HashSet<String>();
        for (IPeptideSpectrumCluster cluster : clusters) {
            List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();
            if (clusteredSpectra.size() < clusterSize)
                continue;
            for (ISpectrum spc : clusteredSpectra) {
                clusterIds.add(spc.getId());
            }

        }
        return clusterIds.size();
    }


    public void addCluster(@Nonnull IPeptideSpectrumCluster pT) {
        clusters.add(pT);
    }


}

