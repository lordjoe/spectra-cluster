package uk.ac.ebi.pride.spectracluster.cluster;

import junit.framework.Assert;
import org.junit.*;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.quality.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.tools.fast_spectra_clustering.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterComparatorTests {

    private List<ISpectrum> peptideSpectrumMatches;
    private ClusterComparator clusterComparator;
    private OriginalClusterComparator originalClusterComparator;

    @Before
    public void setUp() throws Exception {
        clusterComparator = new ClusterComparator();
        originalClusterComparator = new OriginalClusterComparator(new OriginalSignalToNoiseChecker());

        URL url = ClusterComparatorTests.class.getClassLoader().getResource("uk/ac/ebi/pride/spectracluster/util/two_spectra.mgf");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        peptideSpectrumMatches = ClusteringTestUtilities.readConsensusSpectralItems();
    }

    @Test
    public void testClusterComparator() throws Exception {
        final ISpectralCluster cluster1 = peptideSpectrumMatches.get(0).asCluster();
        int result = clusterComparator.compare(cluster1, peptideSpectrumMatches.get(1).asCluster());
        int originalResult = originalClusterComparator.compare(cluster1, peptideSpectrumMatches.get(1).asCluster());
        Assert.assertEquals(result, originalResult);
    }
}
