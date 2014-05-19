package uk.ac.ebi.pride.spectracluster.util;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.ArrayList;
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
        List<IPeptideSpectralCluster> list1 = ClusterUtilities.asClusters(spectra);
        List<IPeptideSpectralCluster> list2 = ClusterUtilities.asClusters(spectra);

        // add the second list to the first making duplicates
        list1.addAll(list2); // dupicate clusters

        final SimilarityChecker similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
        // now merge - should get less or equal to original list
        final List<ICluster> newClusters = ClusterUtilities.mergeClusters(new ArrayList<ICluster>(list1), similarityChecker, 1);
        // we merge at least as many as we had
        Assert.assertTrue(newClusters.size() <= list1.size());

        // now merge - should get less or equal to original list
        final List<ICluster> newClusters2 = ClusterUtilities.mergeClusters(newClusters, similarityChecker, 1);
        // we better get fewer
        Assert.assertTrue(newClusters2.size() <= newClusters.size());
        final List<ICluster> newClusters3 = ClusterUtilities.mergeClusters(newClusters2, similarityChecker, 1);
        // we better get fewer
        Assert.assertTrue(newClusters3.size() <= newClusters2.size());
        final List<ICluster> newClusters4 = ClusterUtilities.mergeClusters(newClusters3, similarityChecker, 1);
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

        List<IPeptideSpectralCluster> list = ClusterUtilities.asClusters(spectra);
        IClusteringEngine engine = Defaults.INSTANCE.getDefaultClusteringEngine();
        for (ICluster sc : list) {
            engine.addClusters(sc);
        }
        for (int i = 0; i < Defaults.INSTANCE.getDefaultNumberReclusteringPasses(); i++) {
            if (!engine.processClusters())
                break;
        }
        // we have solved for these
        List<ICluster> found = (List<ICluster>) engine.getClusters();


        final SimilarityChecker similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();
        final List<ICluster> newClusters = ClusterUtilities.mergeClusters(found, similarityChecker, 1);

        // because we just did this in the engine we expect little further merging
        Assert.assertEquals(newClusters.size(), found.size());


    }


}
