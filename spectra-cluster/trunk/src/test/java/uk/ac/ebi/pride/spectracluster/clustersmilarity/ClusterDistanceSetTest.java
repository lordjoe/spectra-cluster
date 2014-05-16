package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import java.util.List;
import java.util.Random;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterDistanceSetTest {

    private ClusterDistanceSet clusterDistanceSetx;
    private IPeptideSpectralCluster baseCluster;

    public static final Random RND = new Random();

    @Before
    public void setUp() throws Exception {
        clusterDistanceSetx = new ClusterDistanceSet();

        baseCluster = createLazyLoadedSpectralCluster("1234");


        ClusterDistanceItem item1 = new ClusterDistanceItem(baseCluster, createLazyLoadedSpectralCluster("2345"), 0.1);
        ClusterDistanceItem item2 = new ClusterDistanceItem(baseCluster, createLazyLoadedSpectralCluster("3456"), 0.2);
        ClusterDistanceItem item3 = new ClusterDistanceItem(baseCluster, createLazyLoadedSpectralCluster("4567"), 0.3);

        clusterDistanceSetx.addDistance(item1);
        clusterDistanceSetx.addDistance(item2);
        clusterDistanceSetx.addDistance(item3);
    }

    private LazyLoadedSpectralCluster createLazyLoadedSpectralCluster(String id) {
        LazyLoadedSpectralCluster lazyLoadedSpectralCluster = new LazyLoadedSpectralCluster();
        lazyLoadedSpectralCluster.setId(id);

        return lazyLoadedSpectralCluster;
    }

    @Test
    public void testAddLongDistance() throws Exception {
        ClusterDistanceItem item = new ClusterDistanceItem(baseCluster, new LazyLoadedSpectralCluster(), 0.4);
        clusterDistanceSetx.addDistance(item);

        List<ClusterDistanceItem> bestMatches = clusterDistanceSetx.getBestMatches(baseCluster);
        Assert.assertEquals(3, bestMatches.size());
        Assert.assertEquals(0.1, bestMatches.get(0).getDistance(), 0.0001);
        Assert.assertEquals(0.2, bestMatches.get(1).getDistance(), 0.0001);
        Assert.assertEquals(0.3, bestMatches.get(2).getDistance(), 0.0001);
    }

    @Test
    public void testAddShortDistance() throws Exception {
        ClusterDistanceItem item = new ClusterDistanceItem(baseCluster, new LazyLoadedSpectralCluster(), 0.001);
        clusterDistanceSetx.addDistance(item);

        List<ClusterDistanceItem> bestMatches = clusterDistanceSetx.getBestMatches(baseCluster);
        Assert.assertEquals(3, bestMatches.size());
        Assert.assertEquals(0.001, bestMatches.get(0).getDistance(), 0.0001);
        Assert.assertEquals(0.1, bestMatches.get(1).getDistance(), 0.0001);
        Assert.assertEquals(0.2, bestMatches.get(2).getDistance(), 0.0001);

    }

    @Test
    public void testManyAdds() throws Exception {
        ClusterDistanceSet cds = buildTestSet(baseCluster, 1000);
        List<ClusterDistanceItem> bestMatches = clusterDistanceSetx.getBestMatches(baseCluster);
        Assert.assertEquals(3, bestMatches.size());
    }


    public ClusterDistanceSet buildTestSet(IPeptideSpectralCluster base, int size) {
        ClusterDistanceSet ret = new ClusterDistanceSet();


        for (int i = 0; i < size; i++) {

            LazyLoadedSpectralCluster lazyLoadedSpectralCluster = createLazyLoadedSpectralCluster("foo" + i);
            ClusterDistanceItem item1 = new ClusterDistanceItem(base, lazyLoadedSpectralCluster, RND.nextDouble());

            ret.addDistance(item1);

        }
        return ret;
    }

}
