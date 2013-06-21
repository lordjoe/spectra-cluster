package uk.ac.ebi.pride.spectracluster.clustersimilarity;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.tools.fast_spectra_clustering.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersimilarity.SimilarityTests
 * User: Steve
 * Date: 6/17/13
 */
public class SimilarityTests {

    private List<ISpectralCluster> originalSpectralClusters;
       private ClusterDistance distanceMeasure;

    @Before
    public void setUp() throws Exception {

        originalSpectralClusters =  ClusteringTestUtilities.readSpectraClustersFromResource();

        distanceMeasure = new ConcensusSpectrumDistance();
    }


    /**
     * sanity check distance cluster to itself must be 0
     *
     * @throws Exception
     */
    @Test
    public void testSelfSimilarity() throws Exception {

        for (ISpectralCluster sc1 : originalSpectralClusters) {
            final double distance = distanceMeasure.distance(sc1, sc1);
            Assert.assertEquals(0, distance, 0.0001);    // every cluster is similar to itself

        }

    }


    /**
     * sanity check compart list of clusters to itself
     *
     * @throws Exception
     */
    @Test
    public void testGroupSimilarity() throws Exception {
          List<ISpectralCluster> l1 = new ArrayList<ISpectralCluster>(originalSpectralClusters); // copy this will change
         List<ISpectralCluster> l2 = new ArrayList<ISpectralCluster>(originalSpectralClusters); // copy this will change;

        ClusterListSimilarity cd = new ClusterListSimilarity(distanceMeasure);
        final List<ISpectralCluster> identical = cd.identicalClusters(l1, l2);

        Assert.assertEquals(originalSpectralClusters.size(), identical.size());
        Assert.assertTrue(l1.isEmpty());
        Assert.assertTrue(l2.isEmpty());
    }
}
