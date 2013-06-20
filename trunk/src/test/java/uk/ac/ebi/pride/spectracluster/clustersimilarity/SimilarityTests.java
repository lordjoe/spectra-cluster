package uk.ac.ebi.pride.spectracluster.clustersimilarity;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersimilarity.SimilarityTests
 * User: Steve
 * Date: 6/17/13
 */
public class SimilarityTests {

    private ISpectralCluster[] originalSpectralClusters;
    private List<ISpectrum> originalSpectra;
    private ClusterDistance distanceMeasure;

    @Before
    public void setUp() throws Exception {
        // load a file contains a list of clusters
        URL url = ClusteringEngineTests.class.getClassLoader().getResource("uk/ac/ebi/pride/spectracluster/util/spectra_400.0_4.0.cgf");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());

        originalSpectralClusters = ParserUtilities.readSpectralCluster(inputFile);
        Arrays.sort(originalSpectralClusters);
        originalSpectra = ClusterUtilities.extractSpectra(Arrays.asList(originalSpectralClusters));

        distanceMeasure = new ConcensusSpectrumDistance();
    }


    /**
     * sanity check distance cluster to itself must be 0
     *
     * @throws Exception
     */
    @Test
    public void testSelfSimilarity() throws Exception {

        for (int i = 0; i < originalSpectralClusters.length; i++) {
            ISpectralCluster sc1 = originalSpectralClusters[i];
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
        List<ISpectralCluster> l1 = new ArrayList<ISpectralCluster>(Arrays.asList(originalSpectralClusters));
        List<ISpectralCluster> l2 = new ArrayList<ISpectralCluster>(Arrays.asList(originalSpectralClusters));

        ClusterListSimilarity cd = new ClusterListSimilarity(distanceMeasure);
        final List<ISpectralCluster> identical = cd.identicalClusters(l1, l2);

        Assert.assertEquals(originalSpectralClusters.length, identical.size());
        Assert.assertTrue(l1.isEmpty());
        Assert.assertTrue(l2.isEmpty());
    }
}
