package uk.ac.ebi.pride.spectracluster.hadoop.clustersimilarity;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.clustersimilarity.ClisterDistanceTests
 *
 * @author Steve Lewis
 * @date 12/11/13
 */
public class ClusterDistanceTests {

    public static final Random RND = new Random();

    public List<ClusterDistanceItem> buildDistances(ISpectralCluster me,  List<ISpectralCluster> other, int size) {
        List<ClusterDistanceItem> holder = new ArrayList<ClusterDistanceItem>();
        for (int i = 0; i < size; i++) {
            ISpectralCluster otherCluster = chooseCluster(other,me);
            ClusterDistanceItem clusterDistanceItem = new ClusterDistanceItem(me, otherCluster, RND.nextDouble());
            holder.add(clusterDistanceItem);
        }

        return holder;
    }



    private ISpectralCluster chooseCluster( List<ISpectralCluster> clusters,ISpectralCluster me) {

        ISpectralCluster iSpectralCluster = clusters.get(RND.nextInt(clusters.size()));
        while(iSpectralCluster == me)
            iSpectralCluster = clusters.get(RND.nextInt(clusters.size()));
        return iSpectralCluster;
    }

    @Test
    public void testSort() throws Exception {
        List<ISpectralCluster> clusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        ISpectralCluster me = chooseCluster(clusters,null);
        List<ClusterDistanceItem> distances =  buildDistances( me,clusters  , 6) ;
        Collections.sort(distances);
        for (int i = 1; i < distances.size; i++) {
              Assert.assertTrue(distances.get(i - 1).getDistance() < distances.get(i ).getDistance());

        }

    }
}

