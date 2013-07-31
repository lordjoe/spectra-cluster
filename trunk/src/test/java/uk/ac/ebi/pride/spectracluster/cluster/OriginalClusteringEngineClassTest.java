package uk.ac.ebi.pride.spectracluster.cluster;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.similarity.FrankEtAlDotProductOld;
import uk.ac.ebi.pride.spectracluster.similarity.SimilarityChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.PeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.tools.fast_spectra_clustering.ClusteringTestUtilities;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jg
 * Date: 7/31/13
 * Time: 9:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class OriginalClusteringEngineClassTest {
    private final OriginalClusteringEngine originalClusteringEngine = new OriginalClusteringEngine();
    private List<IPeptideSpectrumMatch> spectra;

    @Before
    public void setUp() {
        spectra = ClusteringTestUtilities.readISpectraFromResource();
    }

    private ISpectralCluster getClusterWithSpectrum(List<ISpectralCluster> cluster, String spectrumId) {
        for (ISpectralCluster c : cluster) {
            for (ISpectrum s : c.getClusteredSpectra()) {
                if (s.getId().equals(spectrumId)) {
                    return c;
                }
            }
        }

        return null;
    }

    private void printCluster(ISpectralCluster cluster, String header) {
        if (header != null && !header.isEmpty())
            System.out.println("--" + header + "--");

        for (ISpectrum s : cluster.getClusteredSpectra())
            System.out.println(s.getId());
    }

    @Test
    public void testClustering() {
        for (IPeptideSpectrumMatch psm : spectra)
            originalClusteringEngine.addClusters(psm.asCluster());

        boolean resultChanged = true;

        for (int round = 1; round <= 4 && resultChanged; round++)
            resultChanged = originalClusteringEngine.processClusters();

        List<ISpectralCluster> cluster = originalClusteringEngine.getClusters();

        // make sure no empty or null cluster exist
        for (ISpectralCluster c : cluster) {
            Assert.assertNotNull(c);
            Assert.assertTrue("Empty cluster found", c.getClusteredSpectraCount() > 0);
        }

        // print the 10 largest cluster
        Collections.sort(cluster, new ClusterSizeComparator());
        for (int i = cluster.size() -1; i > cluster.size() - 10; i--) {
            printCluster(cluster.get(i), Integer.toString(i));
        }

        Assert.assertEquals(122, cluster.size()); // original code had 142 spectra

        // find the cluster with spectrum 1248200
        ISpectralCluster cluster1 = getClusterWithSpectrum(cluster, "1248200");
        Assert.assertNotNull(cluster1);
        Assert.assertTrue(cluster1.getPrecursorMz() > 0);

        // cluster 1 is the same as in the original result, only missing spectrum 18834
        ISpectralCluster cluster2 = getClusterWithSpectrum(cluster, "18834");
        Assert.assertNotNull(cluster2);

        /**
         * 18834 is in a cluster with 4 other spectra. in the original result
         * these other 4 spectra are in two clusters: one with 1 and one with 3 spectra
         * Therefore, this difference can be ignored.
         */

        SimilarityChecker similariyChecker = new FrankEtAlDotProductOld();
        double simC1C2 = similariyChecker.assessSimilarity(cluster1.getConsensusSpectrum(), cluster2.getConsensusSpectrum());
        System.out.println("Similariy cluster1, cluster2 = " + simC1C2);
        Assert.assertEquals(24, cluster1.getClusteredSpectraCount());
        Assert.assertEquals(5, cluster2.getClusteredSpectraCount());

        /**
         * In the original result the spectra found in cluster3-5 are found in
         * one 8 spec cluster, one 2 spec cluster and one 1 spec cluster. Here,
         * the original result seems more favourable.
         */
        ISpectralCluster cluster3 = getClusterWithSpectrum(cluster, "81749");
        Assert.assertNotNull(cluster3);

        ISpectralCluster cluster4 = getClusterWithSpectrum(cluster, "16650");
        Assert.assertNotNull(cluster4);;

        ISpectralCluster cluster5 = getClusterWithSpectrum(cluster, "77573");
        Assert.assertNotNull(cluster5);

        Assert.assertEquals(3, cluster3.getClusteredSpectraCount());
        Assert.assertEquals(4, cluster4.getClusteredSpectraCount());
        Assert.assertEquals(2, cluster5.getClusteredSpectraCount());
    }

    public class ClusterSizeComparator implements Comparator<ISpectralCluster> {
        @Override
        public int compare(ISpectralCluster o1, ISpectralCluster o2) {
            return Integer.compare(o1.getClusteredSpectraCount(), o2.getClusteredSpectraCount());
        }
    }
}
