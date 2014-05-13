package uk.ac.ebi.pride.spectracluster.cluster;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.IClusterSet;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.MostSimilarClusterSet;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.SimpleClusterSet;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.SimpleSpectrumRetriever;
import uk.ac.ebi.pride.spectracluster.engine.*;
import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProductOld;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class IncrementalClusteringEngineTests {

    private static final boolean TEST_KNOWN_TO_FAIL = false; // todo take out when things work

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
        final IIncrementalClusteringEngine.IIncrementalClusteringEngineFactory cf = IncrementalClusteringEngine.getClusteringEngineFactory();
        final IIncrementalClusteringEngine ce = cf.getIncrementalClusteringEngine(WINDOW_SIZE);
        List<ISpectralCluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        List<ISpectrum> originalSpectra = ClusterUtilities.extractSpectra(originalSpectralClusters);
        final List<ISpectralCluster> clusters = getRunEngine(ce, originalSpectra);


        final int size = originalSpectralClusters.size();
        final int size1 = clusters.size();
        int index = 0;

        for (ISpectralCluster sc : originalSpectralClusters) {
            double mz1 = sc.getPrecursorMz();
            if (index >= clusters.size())
                break;
            ISpectralCluster newCluster = clusters.get(index);
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

    @Test
    public void testCompareIncrementalClusteringEngine() throws Exception {
        final IIncrementalClusteringEngine.IIncrementalClusteringEngineFactory cf = IncrementalClusteringEngine.getClusteringEngineFactory();
        final IIncrementalClusteringEngine ce = cf.getIncrementalClusteringEngine(WINDOW_SIZE);
        List<ISpectralCluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        List<ISpectrum> originalSpectra = ClusterUtilities.extractSpectra(originalSpectralClusters);
        final List<ISpectralCluster> clusters = getRunEngine(ce, originalSpectra);

        IClusterSet cs1 = new SimpleClusterSet(originalSpectralClusters);
        IClusterSet cs2 = new SimpleClusterSet(clusters);

        IPeptideSpectrumMatch[] sm = new IPeptideSpectrumMatch[originalSpectra.size()];
        for (int i = 0; i < sm.length; i++) {
            sm[i] = (IPeptideSpectrumMatch) originalSpectra.get(i);
        }
        SimpleSpectrumRetriever sr = new SimpleSpectrumRetriever(sm);
        MostSimilarClusterSet.compareClusterSets(sr, cs1, cs2);

    }

    protected List<ISpectralCluster> getRunEngine(IIncrementalClusteringEngine ce, List<ISpectrum> originalSpectra) {
        // these MUST be in ascending mz order
        Collections.sort(originalSpectra);
        final List<ISpectralCluster> clusters = new ArrayList<ISpectralCluster>();
        for (ISpectrum originalSpectrum : originalSpectra) {
            // only deal with one charge
            if (originalSpectrum.getPrecursorCharge() != 2)
                continue;
            final ISpectralCluster spectralCluster = originalSpectrum.asCluster();
            final Collection<ISpectralCluster> removed = ce.addClusterIncremental(spectralCluster);
            if (!removed.isEmpty())
                clusters.addAll(removed);
        }
        Collection<ISpectralCluster> clustersLeft = ce.getClusters();
        clusters.addAll(clustersLeft);

        // remove non-fitting
        final List<ISpectralCluster> holder = new ArrayList<ISpectralCluster>();
        for (ISpectralCluster spectralCluster : clustersLeft) {
            final List<ISpectralCluster> c = ClusteringUtilities.asWritttenSpectra(spectralCluster, ce);
            holder.addAll(c);
        }


        return holder;
    }

    @Test
    public void testClusteringEngine() throws Exception {

        if (TEST_KNOWN_TO_FAIL)
            return;
        List<ISpectralCluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        List<ISpectrum> originalSpectra = ClusterUtilities.extractSpectra(originalSpectralClusters);

        // these MUST be in ascending mz order
        Collections.sort(originalSpectra);

        IClusteringEngineFactory incrementalFactory = WrappedIncrementalClusteringEngine.getClusteringEngineFactory(new FrankEtAlDotProductOld(), Defaults.INSTANCE.getDefaultSpectrumComparator());
        IClusteringEngine incrementalEngine = incrementalFactory.getClusteringEngine(new Double(1000));
        IClusteringEngineFactory factory = ClusteringEngine.getClusteringEngineFactory(new FrankEtAlDotProductOld(), Defaults.INSTANCE.getDefaultSpectrumComparator());
        ClusteringEngine oldClusteringEngine = (ClusteringEngine) factory.getClusteringEngine();

        for (ISpectrum originalSpectrum : originalSpectra) {
            // only deal with one charge
            if (originalSpectrum.getPrecursorCharge() != 2)
                continue;
            final ISpectralCluster spectralCluster = originalSpectrum.asCluster();
            incrementalEngine.addClusters(spectralCluster);
            oldClusteringEngine.addClusters(spectralCluster);
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


        List<ISpectralCluster> newClusters = (List<ISpectralCluster>) incrementalEngine.getClusters();
        Collections.sort(newClusters);

        List<ISpectralCluster> oldClusters = oldClusteringEngine.getClusters();
        Collections.sort(oldClusters);

        Assert.assertEquals(oldClusters.size(), newClusters.size());

        if (TEST_KNOWN_TO_FAIL)
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
            Assert.assertTrue("No similar spectrum found: " + spectrum1.getId(), equivalentSpectrumFound);
        }
    }
}
