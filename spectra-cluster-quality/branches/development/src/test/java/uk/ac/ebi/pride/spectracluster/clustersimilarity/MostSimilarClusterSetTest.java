package uk.ac.ebi.pride.spectracluster.clustersimilarity;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.ClusteringTestUtilities;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;

import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MostSimilarClusterSetTest {

    @Test
    public void testMostSimilarClusterSet() throws Exception {
        List<IPeptideSpectralCluster> spectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        List<IPeptideSpectralCluster> secondClusters = ClusteringTestUtilities.readSecondSpectraClustersFromResource();

        SimpleClusterSet clusterSet1 = new SimpleClusterSet(spectralClusters);
        SimpleClusterSet clusterSet2 = new SimpleClusterSet(secondClusters);

        MostSimilarClusterSet mostSimilarClusterSet = new MostSimilarClusterSet(clusterSet1, ConcensusSpectrumDistance.INSTANCE);
        mostSimilarClusterSet.addOtherSet(clusterSet2);

        MostSimilarClusters mostSimilarClusters = mostSimilarClusterSet.getMostSimilarClusters(spectralClusters.get(0));

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
