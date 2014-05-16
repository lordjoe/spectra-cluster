package uk.ac.ebi.pride.spectracluster.hadoop.clustersimilarity;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterDistanceItem;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.SimpleClusterSet;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;

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

    public List<ClusterDistanceItem> buildDistances(IPeptideSpectrumCluster me, List<IPeptideSpectrumCluster> other, int size) {
        List<ClusterDistanceItem> holder = new ArrayList<ClusterDistanceItem>();
        for (int i = 0; i < size; i++) {
            IPeptideSpectrumCluster otherCluster = chooseCluster(other, me);
            ClusterDistanceItem clusterDistanceItem = new ClusterDistanceItem(me, otherCluster, RND.nextDouble());
            holder.add(clusterDistanceItem);
        }

        return holder;
    }


    private IPeptideSpectrumCluster chooseCluster(List<IPeptideSpectrumCluster> clusters, IPeptideSpectrumCluster me) {

        IPeptideSpectrumCluster iPeptideSpectrumCluster = clusters.get(RND.nextInt(clusters.size()));
        while (iPeptideSpectrumCluster == me)
            iPeptideSpectrumCluster = clusters.get(RND.nextInt(clusters.size()));
        return iPeptideSpectrumCluster;
    }

    @Test
    public void testSort() throws Exception {
        List<IPeptideSpectrumCluster> clusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        List<IPeptideSpectrumCluster> clusters2 =
                ClusteringTestUtilities.readSpectraClustersFromResource(ClusteringTestUtilities.SAMPLE_SECOND_CGF_FILE);

        IPeptideSpectrumCluster me = chooseCluster(clusters, null);
        List<ClusterDistanceItem> distances = buildDistances(me, clusters2, 6);
        Collections.sort(distances);
        for (int i = 1; i < distances.size(); i++) {
            Assert.assertTrue(distances.get(i - 1).getDistance() < distances.get(i).getDistance());

        }

        SimpleClusterSet sc1 = new SimpleClusterSet(clusters);
        SimpleClusterSet sc2 = new SimpleClusterSet(clusters2);

        List<IPeptideSpectrumCluster> bestMatchingClusters = sc2.getBestMatchingClusters(me, 3);
        for (IPeptideSpectrumCluster bestMatchingCluster : bestMatchingClusters) {
            //           System.out.println(bestMatchingCluster);
        }

    }
}

