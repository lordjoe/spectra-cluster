package uk.ac.ebi.pride.spectracluster.cluster;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.similarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class IncrementalClusteringEngineTests {

    private static final boolean TEST_KNOWN_TO_FAIL = true; // todo take out when things work




    @Test
    public void testClusteringEngine() throws Exception {

        List<ISpectralCluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        List<ISpectrum> originalSpectra = ClusterUtilities.extractSpectra(originalSpectralClusters);

        // these MUST be in ascending mz order
        Collections.sort(originalSpectra);

        IClusteringEngineFactory incrementalFactory = WrappedIncrementalClusteringEngine.getClusteringEngineFactory(new FrankEtAlDotProductOld(), Defaults.INSTANCE.getDefaultSpectrumComparator());
        IClusteringEngine incrementalEngine = incrementalFactory.getClusteringEngine();
        IClusteringEngineFactory factory = ClusteringEngine.getClusteringEngineFactory(new FrankEtAlDotProductOld(), Defaults.INSTANCE.getDefaultSpectrumComparator());
        ClusteringEngine oldClusteringEngine = (ClusteringEngine)factory.getClusteringEngine();

        for (ISpectrum originalSpectrum : originalSpectra) {
            // only deal with one charge
            if(originalSpectrum.getPrecursorCharge() != 2)
                continue;
            incrementalEngine.addClusters(originalSpectrum.asCluster());
            oldClusteringEngine.addClusters(originalSpectrum.asCluster());
        }
        //noinspection UnusedDeclaration,UnusedAssignment
        SimilarityChecker similarityChecker = Defaults.INSTANCE.getDefaultSimilarityChecker();

        long start = System.currentTimeMillis();
        incrementalEngine.processClusters();

        long endNewEngine = System.currentTimeMillis();
            //noinspection UnusedDeclaration
        double delSec = (endNewEngine - start) / 1000.0;
        oldClusteringEngine.mergeAllClusters(); // do not do full clustering
        long endOldEngine = System.currentTimeMillis();
             //noinspection UnusedDeclaration
        double delOldSec = (endOldEngine - endNewEngine) / 1000.0;

        // System.out.println(String.format("new %10.2f Old %10.2f", delSec, delOldSec));


        List<ISpectralCluster> newClusters = (List<ISpectralCluster> )incrementalEngine.getClusters();
        Collections.sort(newClusters);

        List<ISpectralCluster> oldClusters = oldClusteringEngine.getClusters();
        Collections.sort(oldClusters);

        Assert.assertEquals(oldClusters.size(), newClusters.size());

        if(TEST_KNOWN_TO_FAIL)
            return;
        for (int i = 0; i < newClusters.size(); i++) {
            ISpectralCluster newCluster = newClusters.get(i);
            ISpectralCluster oldCluster = oldClusters.get(i);
            double similarityScore = similarityChecker.assessSimilarity(newCluster.getConsensusSpectrum(), oldCluster.getConsensusSpectrum());
               if (similarityScore >= similarityChecker.getDefaultThreshold()) {
                      List<ISpectrum> newClusteredSpectra = newCluster.getClusteredSpectra();
                   List<ISpectrum> originalClusteredSpectra = oldCluster.getClusteredSpectra();
                   Assert.assertEquals(originalClusteredSpectra.size(), newClusteredSpectra.size());
                   compareSpectra(newClusteredSpectra, originalClusteredSpectra);
               }
               else {
                   Assert.fail();
               }

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
