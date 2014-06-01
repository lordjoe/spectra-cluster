package uk.ac.ebi.pride.spectracluster.clustersimilarity;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.ClusteringTestUtilities;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterDistanceItem;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.SimpleClusterSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.clustersimilarity.ClisterDistanceTests
 *
 * @author Steve Lewis
 * @date 12/11/13
 */
public class ClusterDistanceTests {

    public static final Random RND = new Random();

    public List<ClusterDistanceItem> buildDistances(ICluster me, List<ICluster> other, int size) {
        List<ClusterDistanceItem> holder = new ArrayList<ClusterDistanceItem>();
        for (int i = 0; i < size; i++) {
            ICluster otherCluster = chooseCluster(other, me);
            ClusterDistanceItem clusterDistanceItem = new ClusterDistanceItem(me, otherCluster, RND.nextDouble());
            holder.add(clusterDistanceItem);
        }

        return holder;
    }


    private ICluster chooseCluster(List<ICluster> clusters, ICluster me) {

        ICluster iPeptideSpectralCluster = clusters.get(RND.nextInt(clusters.size()));
        while (iPeptideSpectralCluster == me)
            iPeptideSpectralCluster = clusters.get(RND.nextInt(clusters.size()));
        return iPeptideSpectralCluster;
    }

    @Test
    public void testSort() throws Exception {
        List<ICluster> clusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        List<ICluster> clusters2 =
                ClusteringTestUtilities.readSpectraClustersFromResource(ClusteringTestUtilities.SAMPLE_SECOND_CGF_FILE);

        ICluster me = chooseCluster(clusters, null);
        List<ClusterDistanceItem> distances = buildDistances(me, clusters2, 6);
        Collections.sort(distances);
        for (int i = 1; i < distances.size(); i++) {
            Assert.assertTrue(distances.get(i - 1).getDistance() < distances.get(i).getDistance());

        }

        SimpleClusterSet sc1 = new SimpleClusterSet(clusters);
        SimpleClusterSet sc2 = new SimpleClusterSet(clusters2);

        List<ICluster> bestMatchingClusters = sc2.getBestMatchingClusters(me, 3);
        for (ICluster bestMatchingCluster : bestMatchingClusters) {
            //           System.out.println(bestMatchingCluster);
        }

    }
}

