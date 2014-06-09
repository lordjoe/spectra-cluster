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
import java.util.Collections;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class IncrementalClusteringEngineComparisonTest {
    public static final String SMALL_CLUSTER_FILE = "uk/ac/ebi/pride/spectracluster/engine/cluster_spec_39546.mgf";

    @Test
    public void testConsensusSpectrumSimilarity() throws Exception {
        final ISimilarityChecker defaultSimilarityChecker = Defaults.getDefaultSimilarityChecker();

        final IncrementalClusteringEngine clusteringEngine = new IncrementalClusteringEngine(defaultSimilarityChecker, Defaults.getDefaultSpectrumComparator(), (float)Defaults.getSimilarityMZRange(), Defaults.getSimilarityThreshold());
        final IncrementalClusteringEngine allPeaksClusteringEngine = new IncrementalClusteringEngine(new AllPeaksDotProduct(Defaults.getSimilarityMZRange()), Defaults.getDefaultSpectrumComparator(), (float)Defaults.getSimilarityMZRange(), Defaults.getSimilarityThreshold());

        List<ICluster> spectraFromLargeClusterFile = readSpectraFromClusterFile(SMALL_CLUSTER_FILE);
        Collections.sort(spectraFromLargeClusterFile);

        for (ICluster cluster : spectraFromLargeClusterFile) {
            clusteringEngine.addClusterIncremental(cluster);
        }


        spectraFromLargeClusterFile = readSpectraFromClusterFile(SMALL_CLUSTER_FILE);
        Collections.sort(spectraFromLargeClusterFile);
        for (ICluster cluster : spectraFromLargeClusterFile) {
            allPeaksClusteringEngine.addClusterIncremental(cluster);
        }

        final List<ICluster> clusters = clusteringEngine.getClusters();
        final List<ICluster> allPeakClusters = allPeaksClusteringEngine.getClusters();
        final double similarity = defaultSimilarityChecker.assessSimilarity(clusters.get(0).getConsensusSpectrum(), allPeakClusters.get(0).getConsensusSpectrum());
        Assert.assertTrue("Consensus spectrum must be similar", similarity > 0.85);

    }


    private List<ICluster> readSpectraFromClusterFile(String file) {
        final List<ISpectrum> spectra= ClusteringTestUtilities.readISpectraFromResource(file);
        List<ICluster> spectraFromFile = new ArrayList<ICluster>();
        for (ISpectrum spectrum : spectra) {
            spectraFromFile.add(ClusterUtilities.asCluster(spectrum));
        }
        return spectraFromFile;
    }
}
