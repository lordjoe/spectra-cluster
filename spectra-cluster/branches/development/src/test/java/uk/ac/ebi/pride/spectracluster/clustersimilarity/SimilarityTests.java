package uk.ac.ebi.pride.spectracluster.clustersimilarity;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersimilarity.SimilarityTests
 * User: Steve
 * Date: 6/17/13
 */
public class SimilarityTests {

     private ClusterDistance distanceMeasure = new ConcensusSpectrumDistance();


    /**
     * sanity check distance cluster to itself must be 0
     *
     * @throws Exception
     */
    @Test
    public void testSelfSimilarity() throws Exception {
        List<ISpectralCluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();

        for (ISpectralCluster sc1 : originalSpectralClusters) {
            double distance = distanceMeasure.distance(sc1, sc1);
            if (distance > 0.0001) {
                distance = distanceMeasure.distance(sc1, sc1); //  break here
                Assert.assertEquals(0, distance, 0.0001);    // every cluster is similar to itself
            }

        }

    }


    /**
     * sanity check compare list of clusters to itself
     *
     * @throws Exception
     */
    @Test
    public void testGroupSimilarity() throws Exception {
        final List<ISpectralCluster> originalCLuster = ClusteringTestUtilities.readSpectraClustersFromResource();
        Collections.sort(originalCLuster);
        List<ISpectralCluster> l1 = new ArrayList<ISpectralCluster>(originalCLuster);

        final List<ISpectralCluster> originalCLuster2 = ClusteringTestUtilities.readSpectraClustersFromResource();
        Collections.sort(originalCLuster2);
        List<ISpectralCluster> l2 = new ArrayList<ISpectralCluster>(originalCLuster2);

        ClusterListSimilarity cd = new ClusterListSimilarity(distanceMeasure);
        final List<ISpectralCluster> identical = cd.identicalClusters(l1, l2);

        Assert.assertEquals(originalCLuster.size(), identical.size());
        Assert.assertTrue(l1.isEmpty());
        Assert.assertTrue(l2.isEmpty());
    }

  }
