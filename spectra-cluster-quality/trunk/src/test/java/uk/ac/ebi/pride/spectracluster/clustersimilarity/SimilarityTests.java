package uk.ac.ebi.pride.spectracluster.clustersimilarity;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.ClusteringTestUtilities;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * uk.ac.ebi.pride.spectracluster.clustersimilarity.SimilarityTests
 * User: Steve
 * Date: 6/17/13
 */
public class SimilarityTests {

    public static final Random RND = new Random();

    private IClusterDistance distanceMeasure = ConcensusSpectrumDistance.INSTANCE;
    private IClusterDistance distanceMeasure2 = ClusterSpectrumOverlapDistance.INSTANCE;
    private IClusterDistance distanceMeasure3 = ClusterContentDistance.INSTANCE;
    private IClusterDistance distanceMeasure4 = ConsensusSimilarityDistance.INSTANCE;

    private IClusterDistance[] measures = {
            distanceMeasure, distanceMeasure2, distanceMeasure3, distanceMeasure4
    };


    private ICluster chooseOtherCluster(List<ICluster> originalSpectralClusters, ICluster notMe) {
        if (originalSpectralClusters.size() < 2)
            throw new IllegalArgumentException("Only works of at least 2 clusters");
        ICluster choice = originalSpectralClusters.get(RND.nextInt(originalSpectralClusters.size()));
        while (choice == notMe)
            choice = originalSpectralClusters.get(RND.nextInt(originalSpectralClusters.size()));
        return choice;
    }

    /**
     * sanity check distance cluster to itself must be 0
     *
     * @throws Exception
     */

    public static final double MINIMUM_NON_SELF_DISTANCE = 0.0000001;
    public static final double MAXIMUM_SELF_DISTANCE = 100 * MINIMUM_NON_SELF_DISTANCE;

    @Test
    public void testSelfSimilarity() throws Exception {
        List<ICluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();

            for (ICluster sc1 : originalSpectralClusters) {
                for (int i = 0; i < measures.length; i++) {
                    IClusterDistance measure = measures[i];
                    double distance = measure.distance(sc1, sc1);
                    if (distance > MAXIMUM_SELF_DISTANCE) {
                        distance = measure.distance(sc1, sc1); //  break here
                        Assert.assertEquals(0, distance, MAXIMUM_SELF_DISTANCE);    // every cluster is similar to itself
                    }
                    ICluster other = chooseOtherCluster(originalSpectralClusters, sc1);
                    distance = measure.distance(sc1, other);
                    if (distance < MINIMUM_NON_SELF_DISTANCE) {
                        distance = measure.distance(sc1, sc1); //  break here
                        distance = measure.distance(sc1, other); //  break here
                        Assert.assertTrue(distance > MINIMUM_NON_SELF_DISTANCE);
                    }
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
        final List<ICluster> originalCLuster = ClusteringTestUtilities.readSpectraClustersFromResource();
        Collections.sort(originalCLuster);
        List<ICluster> l1 = new ArrayList<ICluster>(originalCLuster);

        final List<ICluster> originalCLuster2 = ClusteringTestUtilities.readSpectraClustersFromResource();
        Collections.sort(originalCLuster2);
        List<ICluster> l2 = new ArrayList<ICluster>(originalCLuster2);

        ClusterListSimilarity cd = new ClusterListSimilarity(distanceMeasure);
        final List<ICluster> identical = cd.identicalClusters(l1, l2);

        Assert.assertEquals(originalCLuster.size(), identical.size());
        Assert.assertTrue(l1.isEmpty());
        Assert.assertTrue(l2.isEmpty());
    }

}
