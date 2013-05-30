package uk.ac.ebi.pride.spectracluster.cluster;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringEngineTests {

    private ISpectralCluster[] originalSpectralClusters;
    private List<ISpectrum> originalSpectra;
    private IClusteringEngine clusteringEngine;
    private IClusteringEngine oldClusteringEngine;
     private SimilarityChecker similarityChecker;

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

        oldClusteringEngine = new ClusteringEngine(new FrankEtAlDotProductOld(),Defaults.INSTANCE.getDefaultSpectrumComparator());
        clusteringEngine = Defaults.INSTANCE.getDefaultClusteringEngine();
         for (ISpectrum originalSpectrum : originalSpectra) {
             clusteringEngine.addClusters(originalSpectrum.asCluster());
             oldClusteringEngine.addClusters(originalSpectrum.asCluster());
         }

        similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
    }

    @Test
    public void testClusteringEngine() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 2; i++) {
            if (!clusteringEngine.mergeClusters()) {
                break;
            }
        }
        long endNewEngine = System.currentTimeMillis();
        double delSec = (endNewEngine - start)  /1000.0;
        for (int i = 0; i < 2; i++) {
             if (!oldClusteringEngine.mergeClusters()) {
                 break;
             }
         }
        long endOldEngine = System.currentTimeMillis();
        double delOldSec = (endOldEngine - endNewEngine )  /1000.0;

        System.out.println(String.format("new %10.2f Old %10.2f", delSec, delOldSec));


        List<ISpectralCluster> newClusters = clusteringEngine.getClusters();
        Collections.sort(newClusters);

        List<ISpectralCluster> oldClusters = oldClusteringEngine.getClusters();
        Collections.sort(oldClusters);


        Assert.assertEquals(newClusters.size(), originalSpectralClusters.length);

        for (ISpectralCluster newCluster : newClusters) {
            boolean foundSimilarCluster = false;

            for (ISpectralCluster originalSpectralCluster : originalSpectralClusters) {
                double similarityScore = similarityChecker.assessSimilarity(newCluster.getConsensusSpectrum(), originalSpectralCluster.getConsensusSpectrum());
                if (similarityScore >= similarityChecker.getDefaultThreshold()) {
                    foundSimilarCluster = true;
                    List<ISpectrum> newClusteredSpectra = newCluster.getClusteredSpectra();
                    List<ISpectrum> originalClusteredSpectra = originalSpectralCluster.getClusteredSpectra();
                    Assert.assertEquals(originalClusteredSpectra.size(), newClusteredSpectra.size());
                    compareSpectra(newClusteredSpectra, originalClusteredSpectra);
                }
            }

            Assert.assertTrue("No similar cluster found", foundSimilarCluster);
        }
    }

    private void compareSpectra(List<ISpectrum> spectra1, List<ISpectrum> spectra2) {
        for (ISpectrum spectrum1 : spectra1) {
            boolean equivalentSpectrumFound = false;
            for (ISpectrum spectrum2 : spectra2) {
                if (spectrum1.equivalent(spectrum2)) {
                    equivalentSpectrumFound = true;
                    break;
                }
            }
            Assert.assertTrue("No similar spectrum found: " + spectrum1.getId(), equivalentSpectrumFound);
        }
    }
}
