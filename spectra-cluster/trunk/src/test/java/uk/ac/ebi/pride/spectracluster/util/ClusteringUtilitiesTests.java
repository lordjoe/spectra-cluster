package uk.ac.ebi.pride.spectracluster.util;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.util.ClusteringUtilitiesTests
 * Tests of ClusteringUtilitiesFunctions
 * User: Steve
 * Date: 7/11/13
 */
public class ClusteringUtilitiesTests {


    @Test
    public void testMergeSpectra() throws Exception {
        List<ISpectrum> spectra = ClusteringTestUtilities.readConsensusSpectralItems();
        // read Spectra

        // make two identical lists
        List<ISpectralCluster> list1 = ClusterUtilities.asClusters(spectra);
        List<ISpectralCluster> list2 = ClusterUtilities.asClusters(spectra);

        // add the second list to the first making duplicates
        list1.addAll(list2); // dupicate clusters

        final SimilarityChecker similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
        // now merge - should get less or equal to original list
        final List<ISpectralCluster> newClusters = ClusterUtilities.mergeClusters(list1, similarityChecker, 1);
        // we merge at least as many as we had
        Assert.assertTrue(newClusters.size() <= list1.size());

        // now merge - should get less or equal to original list
        final List<ISpectralCluster> newClusters2 = ClusterUtilities.mergeClusters(newClusters, similarityChecker, 1);
        // we better get fewer
        Assert.assertTrue(newClusters2.size() <= newClusters.size());
        final List<ISpectralCluster> newClusters3 = ClusterUtilities.mergeClusters(newClusters2, similarityChecker, 1);
        // we better get fewer
        Assert.assertTrue(newClusters3.size() <= newClusters2.size());
        final List<ISpectralCluster> newClusters4 = ClusterUtilities.mergeClusters(newClusters3, similarityChecker, 1);
        // we better get fewer
        Assert.assertTrue(newClusters4.size() <= newClusters3.size());

        // no further mergers after convergance
        // Assert.assertEquals(newClusters4.size(),newClusters3.size());
    }


    /**
     * merge after solution - should be a noop singe clustering does this
     *
     * @throws Exception
     */
    @Test
    public void testMerge() throws Exception {
        List<ISpectrum> spectra = ClusteringTestUtilities.readConsensusSpectralItems();

        List<ISpectralCluster> list = ClusterUtilities.asClusters(spectra);
        IClusteringEngine engine = Defaults.INSTANCE.getDefaultClusteringEngine();
        for (ISpectralCluster sc : list) {
            engine.addClusters(sc);
        }
        for (int i = 0; i < Defaults.INSTANCE.getDefaultNumberReclusteringPasses(); i++) {
            if (!engine.processClusters())
                break;
        }
        // we have solved for these
        List<ISpectralCluster> found = (List<ISpectralCluster>) engine.getClusters();


        final SimilarityChecker similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
        final List<ISpectralCluster> newClusters = ClusterUtilities.mergeClusters(found, similarityChecker, 1);

        // because we just did this in the engine we expect little further merging
        Assert.assertEquals(newClusters.size(), found.size());


    }


}
