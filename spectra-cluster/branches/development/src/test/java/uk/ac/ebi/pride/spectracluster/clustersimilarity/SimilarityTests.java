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

    public static final Random RND = new Random();

    private IClusterDistance distanceMeasure  = ConcensusSpectrumDistance.INSTANCE;
    private IClusterDistance distanceMeasure2 = ClusterSpectrumOverlapDistance.INSTANCE;
    private IClusterDistance distanceMeasure3 = ClusterContentDistance.INSTANCE;
    private IClusterDistance distanceMeasure4 = ConsensusSimilarityDistance.INSTANCE;

    private IClusterDistance[] measures = {
            distanceMeasure, distanceMeasure2, distanceMeasure3, distanceMeasure4
    };


    private ISpectralCluster chooseOtherCluster(List<ISpectralCluster> originalSpectralClusters, ISpectralCluster notMe) {
        if (originalSpectralClusters.size() < 2)
            throw new IllegalArgumentException("Only works of at least 2 clusters");
        ISpectralCluster choice = originalSpectralClusters.get(RND.nextInt(originalSpectralClusters.size()));
        while (choice == notMe)
            choice = originalSpectralClusters.get(RND.nextInt(originalSpectralClusters.size()));
        return choice;
    }

    /**
     * sanity check distance cluster to itself must be 0
     *
     * @throws Exception
     */

    public static final double MINIMUM_NON_SELF_DISTANCE = 0.01;
    public static final double MAXIMUM_SELF_DISTANCE = 0.0001;
     @Test
    public void testSelfSimilarity() throws Exception {
        List<ISpectralCluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();

        for (ISpectralCluster sc1 : originalSpectralClusters) {
            for (int i = 0; i < measures.length; i++) {
                IClusterDistance measure = measures[i];
                double distance = measure.distance(sc1, sc1);
                if (distance > MAXIMUM_SELF_DISTANCE) {
                    distance = measure.distance(sc1, sc1); //  break here
                    Assert.assertEquals(0, distance, MAXIMUM_SELF_DISTANCE);    // every cluster is similar to itself
                }
                ISpectralCluster other = chooseOtherCluster(originalSpectralClusters, sc1);
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
