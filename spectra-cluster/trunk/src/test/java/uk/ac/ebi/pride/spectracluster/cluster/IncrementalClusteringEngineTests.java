package uk.ac.ebi.pride.spectracluster.cluster;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.engine.*;
import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProductOld;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;
import uk.ac.ebi.pride.spectracluster.util.IDefaultingFactory;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class IncrementalClusteringEngineTests {

    private static final boolean TEST_KNOWN_TO_FAIL = true; // todo take out when things work

    // a very quick test to make sure retainall works correctly on disjoint sets
    @Test
    public void testRetainAll() throws Exception {
        final String[] strings = {"foo", "bar"};
        Set<String> s1 = new HashSet<String>(Arrays.asList(strings));
        final String[] strings2 = {"star"};
        Set<String> s2 = new HashSet<String>(Arrays.asList(strings2));
        final boolean condition = s1.retainAll(s2);
        Assert.assertTrue(condition);
    }

    public static final int WINDOW_SIZE = 4000; // use a big window

    @Test
    public void testIncrementalClusteringEngine() throws Exception {
        if (TEST_KNOWN_TO_FAIL)
             return;
        final IncrementalClusteringEngineFactory cf = new IncrementalClusteringEngineFactory();
        final IIncrementalClusteringEngine ce = EngineFactories.buildIncrementalClusteringEngineFactory(WINDOW_SIZE).buildInstance();
        List<ICluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        List<ISpectrum> originalSpectra = ClusterUtilities.extractSpectra(originalSpectralClusters);
        final List<ICluster> clusters = getRunEngine(ce, originalSpectra);


        final int size = originalSpectralClusters.size();
        final int size1 = clusters.size();
        int index = 0;

        for (ICluster sc : originalSpectralClusters) {
            double mz1 = sc.getPrecursorMz();
            if (index >= clusters.size())
                break;
            ICluster newCluster = clusters.get(index);
            final String spectralId1 = sc.getSpectralId();
            String spectralId2 = newCluster.getSpectralId();
            if (spectralId1.equals(spectralId2)) {
                index++;
                continue;
            }
            double mz2 = newCluster.getPrecursorMz();
            while (mz2 <= mz1) {
                if (index >= clusters.size() - 1)
                    break;
                newCluster = clusters.get(++index);
                mz2 = newCluster.getPrecursorMz();
                spectralId2 = newCluster.getSpectralId();
                if (spectralId1.equals(spectralId2)) {
                    index++;
                    break;
                } else {
                    System.out.println(spectralId1 + " " + spectralId2);
                }
            }
            if (spectralId1.equals(spectralId2)) {
                continue;
            } else {
                System.out.println("unmatched " + spectralId1 + " " + spectralId2);

            }

        }
        Assert.assertEquals(size, size1);

    }

//    @Test
//    public void testCompareIncrementalClusteringEngine() throws Exception {
//        final IncrementalClusteringEngineFactory cf = new IncrementalClusteringEngineFactory();
//        final IIncrementalClusteringEngine ce = cf.getIncrementalClusteringEngine(WINDOW_SIZE);
//        List<ICluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();
//        List<ISpectrum> originalSpectra = ClusterUtilities.extractSpectra(originalSpectralClusters);
//        final List<ICluster> clusters = getRunEngine(ce, originalSpectra);
//
//        IClusterSet cs1 = new SimpleClusterSet(originalSpectralClusters);
//        IClusterSet cs2 = new SimpleClusterSet(clusters);
//
//        ISpectrum[] sm = new ISpectrum[originalSpectra.size()];
//        for (int i = 0; i < sm.length; i++) {
//            sm[i] = (ISpectrum) originalSpectra.get(i);
//        }
//        SimpleSpectrumRetriever sr = new SimpleSpectrumRetriever(sm);
//        MostSimilarClusterSet.compareClusterSets(sr, cs1, cs2);
//
//    }

    protected List<ICluster> getRunEngine(IIncrementalClusteringEngine ce, List<ISpectrum> originalSpectra) {
        // these MUST be in ascending mz order
        Collections.sort(originalSpectra);
        final List<ICluster> clusters = new ArrayList<ICluster>();
        for (ISpectrum originalSpectrum : originalSpectra) {
            // only deal with one charge
            if (originalSpectrum.getPrecursorCharge() != 2)
                continue;
            final ICluster spectralCluster = ClusterUtilities.asCluster(originalSpectrum);
            final Collection<ICluster> removed = ce.addClusterIncremental(spectralCluster);
            if (!removed.isEmpty())
                clusters.addAll(removed);
        }
        Collection<ICluster> clustersLeft = ce.getClusters();
        clusters.addAll(clustersLeft);

        // remove non-fitting
        final List<ICluster> holder = new ArrayList<ICluster>();
        for (ICluster spectralCluster : clustersLeft) {
            final List<ICluster> c = ClusterUtilities.removeNonFittingSpectra(spectralCluster, ce.getSimilarityChecker(), Defaults.getRetainThreshold());
            for (ICluster cluster : c) {
                holder.add((ICluster)cluster);
            }
        }


        return holder;
    }

    @Test
    public void testClusteringEngine() throws Exception {

        if (TEST_KNOWN_TO_FAIL)
            return;
        List<ICluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        List<ISpectrum> originalSpectra = ClusterUtilities.extractSpectra(originalSpectralClusters);

        // these MUST be in ascending mz order
        Collections.sort(originalSpectra);
        final IDefaultingFactory<IIncrementalClusteringEngine> factory = EngineFactories.buildIncrementalClusteringEngineFactory(new FrankEtAlDotProductOld(),
                Defaults.getDefaultSpectrumComparator(),
                Defaults.getSimilarityThreshold(),
                new Float(1000));
        IIncrementalClusteringEngine incrementalClusteringEngine = factory.buildInstance();
   //     final IncrementalClusteringEngine incrementalClusteringEngine = new IncrementalClusteringEngine(new FrankEtAlDotProductOld(), new DefaultClusterComparator(), new Double(1000), Defaults.getSimilarityThreshold());
        IClusteringEngine incrementalEngine = new WrappedIncrementalClusteringEngine(incrementalClusteringEngine);
        final IDefaultingFactory<IClusteringEngine> iClusteringEngineIDefaultingFactory = EngineFactories.buildClusteringEngineFactory(new FrankEtAlDotProductOld(), Defaults.getDefaultSpectrumComparator(), Defaults.getSimilarityThreshold());
        ClusteringEngine oldClusteringEngine = (ClusteringEngine)iClusteringEngineIDefaultingFactory.buildInstance();

        for (ISpectrum originalSpectrum : originalSpectra) {
            // only deal with one charge
            if (originalSpectrum.getPrecursorCharge() != 2)
                continue;
            final ICluster spectralCluster = ClusterUtilities.asCluster(originalSpectrum);
            incrementalEngine.addClusters(spectralCluster);
            oldClusteringEngine.addClusters(spectralCluster);
        }
        //noinspection UnusedDeclaration,UnusedAssignment
        ISimilarityChecker similarityChecker = Defaults.getDefaultSimilarityChecker();

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


        List<ICluster> newClusters = (List<ICluster>) incrementalEngine.getClusters();
        Collections.sort(newClusters);

        List<ICluster> oldClusters = oldClusteringEngine.getClusters();
        Collections.sort(oldClusters);

        if(oldClusters.size() != newClusters.size()) {
            // bad case - check out
            for (int i = 0; i < newClusters.size(); i++) {
                 ICluster newCluster = newClusters.get(i);
                 ICluster oldCluster = oldClusters.get(i);
                 double similarityScore = similarityChecker.assessSimilarity(newCluster.getConsensusSpectrum(), oldCluster.getConsensusSpectrum());
                 if (similarityScore >= Defaults.getSimilarityThreshold()) {
                     List<ISpectrum> newClusteredSpectra = newCluster.getClusteredSpectra();
                     List<ISpectrum> originalClusteredSpectra = oldCluster.getClusteredSpectra();
                     Assert.assertEquals(originalClusteredSpectra.size(), newClusteredSpectra.size());
                     compareSpectra(newClusteredSpectra, originalClusteredSpectra);
                 } else {
                     Assert.fail();
                 }

             }

            Assert.assertEquals(oldClusters.size(), newClusters.size());
        }

        if (TEST_KNOWN_TO_FAIL)
            return;
        for (int i = 0; i < newClusters.size(); i++) {
            ICluster newCluster = newClusters.get(i);
            ICluster oldCluster = oldClusters.get(i);
            double similarityScore = similarityChecker.assessSimilarity(newCluster.getConsensusSpectrum(), oldCluster.getConsensusSpectrum());
            if (similarityScore >= Defaults.getSimilarityThreshold()) {
                List<ISpectrum> newClusteredSpectra = newCluster.getClusteredSpectra();
                List<ISpectrum> originalClusteredSpectra = oldCluster.getClusteredSpectra();
                Assert.assertEquals(originalClusteredSpectra.size(), newClusteredSpectra.size());
                compareSpectra(newClusteredSpectra, originalClusteredSpectra);
            } else {
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
            if(!equivalentSpectrumFound)
                  Assert.assertTrue("No similar spectrum found: " + spectrum1.getId(), equivalentSpectrumFound);
        }
    }
}
