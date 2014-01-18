package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import javax.annotation.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.FractionInClustersOfSize
 * User: Steve
 * Date: 1/16/14
 */
public class FractionInClustersOfSize {
    public static final int[] TEST_SIZES = { 0,2,8,30};

    private final List<ISpectralCluster>  clusters = new ArrayList<ISpectralCluster>();

    public  int getNumberInClusterOfSize(int clusterSize)  {
        Set<String> clusterIds = new HashSet<String>();
        for (ISpectralCluster cluster : clusters) {
            List<ISpectrum> clusteredSpectra = cluster.getClusteredSpectra();
            if(clusteredSpectra.size() < clusterSize)
                 continue;
             for (ISpectrum spc : clusteredSpectra) {
                 clusterIds.add(spc.getId());
             }

        }
        return clusterIds.size();
    }


    public void addCluster(@Nonnull ISpectralCluster pT) {
        clusters.add(pT) ;
    }


}

