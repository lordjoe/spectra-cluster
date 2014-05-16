package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterSpectrumOverlapDistanceTest {

    private IPeptideSpectralCluster c1;
    private IPeptideSpectralCluster c2;
    private ClusterSpectrumOverlapDistance clusterSpectrumOverlapDistance;

    @Before
    public void setUp() throws Exception {
        c1 = new LazyLoadedSpectralCluster();
        c2 = new LazyLoadedSpectralCluster();
        clusterSpectrumOverlapDistance = ClusterSpectrumOverlapDistance.INSTANCE;
    }

    @Test
    public void testZeroDistance() throws Exception {
        LazyLoadedSpectrum lazyLoadedSpectrum = new LazyLoadedSpectrum("123", null);
        c1.addSpectra(lazyLoadedSpectrum);
        c2.addSpectra(lazyLoadedSpectrum);

        Assert.assertEquals(0, clusterSpectrumOverlapDistance.distance(c1, c2), 0.00001);
    }

    @Test
    public void testMaxDistance() throws Exception {
        LazyLoadedSpectrum one = new LazyLoadedSpectrum("123", null);
        LazyLoadedSpectrum another = new LazyLoadedSpectrum("234", null);
        c1.addSpectra(one);
        c2.addSpectra(another);

        Assert.assertEquals(1, clusterSpectrumOverlapDistance.distance(c1, c2), 0.00001);
    }

    /**
     * todo not sure what this is supposed to do
     *
     * @throws Exception
     */
    @Test
    public void testMediumDistance() throws Exception {

        LazyLoadedSpectrum one = new LazyLoadedSpectrum("123", null);
        LazyLoadedSpectrum another = new LazyLoadedSpectrum("234", null);
        LazyLoadedSpectrum yetAnother = new LazyLoadedSpectrum("345", null);
        LazyLoadedSpectrum last = new LazyLoadedSpectrum("456", null);
        c1.addSpectra(one);
        c2.addSpectra(one);
        c2.addSpectra(another);
        c2.addSpectra(yetAnother);
        c2.addSpectra(last);

        Assert.assertEquals(0.0, clusterSpectrumOverlapDistance.distance(c1, c2), 0.00001);
    }
}
