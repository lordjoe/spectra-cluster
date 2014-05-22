package uk.ac.ebi.pride.spectracluster.clustersimilarity;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.ClusteringTestUtilities;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterDistanceItem;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterSpectrumOverlapDistance;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.ConcensusSpectrumDistance;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.MostSimilarClusters;

import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MostSimilarClustersTest {
    /**
     * This will not work - no overlap
     *
     * @throws Exception
     */
    //  @Test
    public void testMostSimilarClusters() throws Exception {

        List<IPeptideSpectralCluster> spectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();

        MostSimilarClusters mostSimilarClusters = new MostSimilarClusters(spectralClusters.get(0), ClusterSpectrumOverlapDistance.INSTANCE);

        mostSimilarClusters.addClusters(spectralClusters);

        IPeptideSpectralCluster bestMatch = mostSimilarClusters.getBestMatchingCluster();
        IPeptideSpectralCluster baseCluster = mostSimilarClusters.getBaseCluster();
        if (!baseCluster.equals(bestMatch)) {
            bestMatch = mostSimilarClusters.getBestMatchingCluster();// repeat test
            Assert.assertEquals(baseCluster, bestMatch);   // allow us to look at the bad case
        }
    }

    @Test
    public void testAlternativeClusters() throws Exception {

        List<IPeptideSpectralCluster> spectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        List<IPeptideSpectralCluster> secondClusters = ClusteringTestUtilities.readSecondSpectraClustersFromResource();

        MostSimilarClusters mostSimilarClusters = new MostSimilarClusters(spectralClusters.get(0), ConcensusSpectrumDistance.INSTANCE);

        mostSimilarClusters.addClusters(secondClusters);

        ClusterDistanceItem match = mostSimilarClusters.getBestMatch();
        Assert.assertEquals(0, match.getDistance(), 0.00001);

        List<ClusterDistanceItem> bestMatches = mostSimilarClusters.getBestMatches();
        double distance = -1000;
        for (ClusterDistanceItem bestMatch : bestMatches) {
            double bestMatchDistance = bestMatch.getDistance();
            Assert.assertTrue(distance <= bestMatchDistance);
            distance = bestMatchDistance;
        }

    }
}
