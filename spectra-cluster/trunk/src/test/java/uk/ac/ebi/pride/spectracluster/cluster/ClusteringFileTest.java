package uk.ac.ebi.pride.spectracluster.cluster;


import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.ISpectrumRetriever;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.LazyLoadedSpectralCluster;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;

import java.io.LineNumberReader;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringFileTest {

    @Test
    public void testParseClusteringFile() {
        LineNumberReader inp = ClusteringTestUtilities.getResourceClusteringReader();
        ISpectrumRetriever spectrumRetriever = null;

        ISpectralCluster[] clusters = ParserUtilities.readClustersFromClusteringFile(inp, spectrumRetriever);

        Assert.assertEquals(48, clusters.length);

        LazyLoadedSpectralCluster firstCluster = (LazyLoadedSpectralCluster) clusters[0];
        Assert.assertEquals(531.0, firstCluster.getPrecursorMz(), 0.001);
        Assert.assertEquals(84, firstCluster.getClusteredSpectraCount());
        List<String> peptides = firstCluster.getPeptides();
        Assert.assertEquals("GTIEIVSDVK", peptides.get(0));


        LazyLoadedSpectralCluster lastCluster = (LazyLoadedSpectralCluster) clusters[47];
        Assert.assertEquals(531.977, lastCluster.getPrecursorMz(), 0.001);
        Assert.assertEquals(19, lastCluster.getClusteredSpectraCount());
        List<String> peptides1 = lastCluster.getPeptides();
        Assert.assertEquals("AEQVFQNVK", peptides1.get(0));

    }
}
