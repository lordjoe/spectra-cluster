package uk.ac.ebi.pride.spectracluster.cluster;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class StableClusteringEngineTest {
    private List<ISpectralCluster> unstableClusters;
    private List<ISpectralCluster> stableClusters;
    private StableClusteringEngine clusteringEngine;

    @Before
    public void setUp() throws Exception {
        clusteringEngine = new StableClusteringEngine();
        List<ISpectralCluster> originalSpectralClusters = ClusteringTestUtilities.readSpectraClustersFromResource();
        unstableClusters = new ArrayList<ISpectralCluster>();
        stableClusters = new ArrayList<ISpectralCluster>();

        for (ISpectralCluster originalSpectralCluster : originalSpectralClusters) {
            if (originalSpectralCluster.isStable()) {
                stableClusters.add(originalSpectralCluster);
            } else {
                unstableClusters.add(originalSpectralCluster);
            }
        }
    }


    @Test
    public void testEngine() throws Exception {
        Assert.assertTrue(!unstableClusters.isEmpty());
        Assert.assertTrue(!stableClusters.isEmpty());

        for (ISpectralCluster unstableCluster : unstableClusters) {
            clusteringEngine.addUnstableCluster(unstableCluster);
        }

        for (ISpectralCluster stableCluster : stableClusters) {
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

        Collection<ISpectralCluster> unstableClustersAfterProcess = clusteringEngine.getClusters();
        Assert.assertEquals(unstableClustersAfterProcess.size(),unstableClusters.size());
    }
}
