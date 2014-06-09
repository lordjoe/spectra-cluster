package uk.ac.ebi.pride.spectracluster.engine;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.similarity.AllPeaksDotProduct;
import uk.ac.ebi.pride.spectracluster.similarity.ISimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Unit tests on ClusteringEngine to compare the clusters produced by AllPeaksDotProduct and
 * FrankAtElDotProduct.
 *
 *
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringEngineComparisonTests {
    public static final String SMALL_CLUSTER_FILE = "uk/ac/ebi/pride/spectracluster/engine/cluster_spec_39546.mgf";

    @Test
    public void testConsensusSpectrumSimilarity() throws Exception {
        final ISimilarityChecker defaultSimilarityChecker = Defaults.getDefaultSimilarityChecker();

        final ClusteringEngine clusteringEngine = new ClusteringEngine(defaultSimilarityChecker, Defaults.getDefaultSpectrumComparator(), Defaults.getSimilarityThreshold());
        final ClusteringEngine allPeaksClusteringEngine = new ClusteringEngine(new AllPeaksDotProduct(Defaults.getSimilarityMZRange()), Defaults.getDefaultSpectrumComparator(), Defaults.getSimilarityThreshold());

        Collection<ICluster> spectraFromLargeClusterFile = readSpectraFromClusterFile(SMALL_CLUSTER_FILE);
        clusteringEngine.addClusters(spectraFromLargeClusterFile.toArray(new ICluster[spectraFromLargeClusterFile.size()]));
        boolean changed = true;
        int count = 4;
        while(changed && count > 0) {
            changed = clusteringEngine.processClusters();
            count--;
        }


        spectraFromLargeClusterFile = readSpectraFromClusterFile(SMALL_CLUSTER_FILE);
        allPeaksClusteringEngine.addClusters(spectraFromLargeClusterFile.toArray(new ICluster[spectraFromLargeClusterFile.size()]));
        changed = true;
        count = 4;
        while(changed && count > 0) {
            changed = allPeaksClusteringEngine.processClusters();
            count--;
        }

        final List<ICluster> clusters = clusteringEngine.getClusters();
        final List<ICluster> allPeakClusters = allPeaksClusteringEngine.getClusters();
        final double similarity = defaultSimilarityChecker.assessSimilarity(clusters.get(0).getConsensusSpectrum(), allPeakClusters.get(0).getConsensusSpectrum());
        Assert.assertTrue("Consensus spectrum must be similar", similarity > 0.85);

    }


    private Collection<ICluster> readSpectraFromClusterFile(String file) {
        final List<ISpectrum> spectra= ClusteringTestUtilities.readISpectraFromResource(file);
        Collection<ICluster> spectraFromFile = new ArrayList<ICluster>();
        for (ISpectrum spectrum : spectra) {
            spectraFromFile.add(ClusterUtilities.asCluster(spectrum));
        }
        return spectraFromFile;
    }
}
