package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterDistanceSetTest {

    private ClusterDistanceSet clusterDistanceSet;
    private LazyLoadedSpectralCluster  baseCluster;

    @Before
    public void setUp() throws Exception {
        clusterDistanceSet = new ClusterDistanceSet();

        baseCluster = createLazyLoadedSpectralCluster("1234");



        ClusterDistanceItem item1 = new ClusterDistanceItem(baseCluster, createLazyLoadedSpectralCluster("2345"), 0.1);
        ClusterDistanceItem item2 = new ClusterDistanceItem(baseCluster, createLazyLoadedSpectralCluster("3456"), 0.2);
        ClusterDistanceItem item3 = new ClusterDistanceItem(baseCluster, createLazyLoadedSpectralCluster("4567"), 0.3);

        clusterDistanceSet.addDistance(item1);
        clusterDistanceSet.addDistance(item2);
        clusterDistanceSet.addDistance(item3);
    }

    private LazyLoadedSpectralCluster createLazyLoadedSpectralCluster(String id) {
        LazyLoadedSpectralCluster lazyLoadedSpectralCluster = new LazyLoadedSpectralCluster();
        lazyLoadedSpectralCluster.setId(id);

        return lazyLoadedSpectralCluster;
    }

    @Test
    public void testAddLongDistance() throws Exception {
        ClusterDistanceItem item = new ClusterDistanceItem(baseCluster, new LazyLoadedSpectralCluster(), 0.4);
        clusterDistanceSet.addDistance(item);

        List<ClusterDistanceItem> bestMatches = clusterDistanceSet.getBestMatches(baseCluster);
        Assert.assertEquals(3, bestMatches.size());
        Assert.assertEquals(0.1, bestMatches.get(0).getDistance(), 0.0001);
        Assert.assertEquals(0.2, bestMatches.get(1).getDistance(), 0.0001);
        Assert.assertEquals(0.3, bestMatches.get(2).getDistance(), 0.0001);
    }

    @Test
    public void testAddShortDistance() throws Exception {
        ClusterDistanceItem item = new ClusterDistanceItem(baseCluster, new LazyLoadedSpectralCluster(), 0.001);
        clusterDistanceSet.addDistance(item);

        List<ClusterDistanceItem> bestMatches = clusterDistanceSet.getBestMatches(baseCluster);
        Assert.assertEquals(3, bestMatches.size());
        Assert.assertEquals(0.001, bestMatches.get(0).getDistance(), 0.0001);
        Assert.assertEquals(0.1, bestMatches.get(1).getDistance(), 0.0001);
        Assert.assertEquals(0.2, bestMatches.get(2).getDistance(), 0.0001);

    }
}
