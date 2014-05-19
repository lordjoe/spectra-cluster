package uk.ac.ebi.pride.spectracluster.cluster;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.engine.ClusteringEngine;
import uk.ac.ebi.pride.spectracluster.engine.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.engine.StableClusteringEngine;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class StableClusteringEngineTest {

    public static final int STABLE_CLUSTER_SIZE = 4;

    private List<IPeptideSpectralCluster> unstableClusters;
    private List<IPeptideSpectralCluster> stableClusters;
    private StableClusteringEngine clusteringEngine;

    @Before
    public void setUp() throws Exception {
        ClusterUtilities.setStableClusterSize(STABLE_CLUSTER_SIZE);   // drop cluster size foe a small sample
        IClusteringEngine ce = ClusteringEngine.getClusteringEngineFactory().getClusteringEngine();
        List<IPeptideSpectralCluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();

        for (IPeptideSpectralCluster sc : originalSpectralClusters) {
            ce.addClusters(sc);
        }
        originalSpectralClusters = (List<IPeptideSpectralCluster>) ce.getClusters();

        clusteringEngine = new StableClusteringEngine();
        unstableClusters = new ArrayList<IPeptideSpectralCluster>();
        stableClusters = new ArrayList<IPeptideSpectralCluster>();

        final CountBasedClusterStabilityAssessor clusterStabilityAssessor = new CountBasedClusterStabilityAssessor();
        for (IPeptideSpectralCluster originalSpectralCluster : originalSpectralClusters) {
            if (clusterStabilityAssessor.isStable(originalSpectralCluster)) {
                stableClusters.add(originalSpectralCluster);
            } else {
                unstableClusters.add(originalSpectralCluster);
            }
        }
    }


    public static final int NUMBER_MERGED_CLUSTERS = 0;

    @Test
    public void testEngine() throws Exception {
        Assert.assertTrue(!unstableClusters.isEmpty());
        Assert.assertTrue(!stableClusters.isEmpty());

        for (IPeptideSpectralCluster unstableCluster : unstableClusters) {
            clusteringEngine.addUnstableCluster(unstableCluster);
        }

        for (IPeptideSpectralCluster stableCluster : stableClusters) {
            int clusteredSpectraCount = stableCluster.getClusteredSpectraCount();
            int numberOfUnstableSpectra = clusteringEngine.getNumberOfUnstableSpectra();
            clusteringEngine.processStableCluster(stableCluster);
            int processedNumberOfUnstableSpectra = clusteringEngine.getNumberOfUnstableSpectra();
            int processedClusteredSpectrumCount = stableCluster.getClusteredSpectraCount();
//            Assert.assertEquals(clusteredSpectraCount,processedClusteredSpectrumCount);

            if (clusteredSpectraCount != processedClusteredSpectrumCount) {
                int actual = numberOfUnstableSpectra - processedNumberOfUnstableSpectra;
                int expected = processedClusteredSpectrumCount - clusteredSpectraCount;
                Assert.assertEquals(expected, actual);
            }
        }

        Collection<IPeptideSpectralCluster> unstableClustersAfterProcess = clusteringEngine.getClusters();
        // we did get some merging and I think that is good
        int actual = unstableClusters.size() - NUMBER_MERGED_CLUSTERS;
        Assert.assertEquals(unstableClustersAfterProcess.size(), actual);
    }
}
