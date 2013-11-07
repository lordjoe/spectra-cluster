package uk.ac.ebi.pride.spectracluster.cluster;


import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.ISpectrumRetriever;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.LazyLoadedSpectralCluster;
import uk.ac.ebi.pride.spectracluster.util.ClusteringTestUtilities;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import java.io.LineNumberReader;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusteringFileTest {

    @Test
    public void testParseClusteringFile()
    {
        LineNumberReader inp = ClusteringTestUtilities.getResourceClusteringReader();
        ISpectrumRetriever spectrumRetriever = null;

        ISpectralCluster[] clusters = ParserUtilities.readClustersFromClusteringFile(inp, spectrumRetriever);

        Assert.assertEquals(48,clusters.length);

        LazyLoadedSpectralCluster firstCluster = (LazyLoadedSpectralCluster)clusters[0];
        Assert.assertEquals(531.0,firstCluster.getPrecursorMz(), 0.001);
        Assert.assertEquals(84,firstCluster.getClusteredSpectraCount());
        Assert.assertEquals("[GTIEIVSDVK]", firstCluster.getPeptideSequence());


        LazyLoadedSpectralCluster lastCluster = (LazyLoadedSpectralCluster)clusters[47];
        Assert.assertEquals(531.977, lastCluster.getPrecursorMz(), 0.001);
        Assert.assertEquals(19,lastCluster.getClusteredSpectraCount());
        Assert.assertEquals("[AEQVFQNVK]", lastCluster.getPeptideSequence());

    }
}
