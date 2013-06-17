package uk.ac.ebi.pride.spectracluster.cluster;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.quality.OriginalSignalToNoiseChecker;
import uk.ac.ebi.pride.spectracluster.quality.SignalToNoiseChecker;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import java.io.File;
import java.net.URL;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class ClusterComparatorTests {

    private ISpectrum[] peptideSpectrumMatches;
    private ClusterComparator clusterComparator;
    private OriginalClusterComparator originalClusterComparator;

    @Before
    public void setUp() throws Exception {
        clusterComparator = new ClusterComparator(new SignalToNoiseChecker());
        originalClusterComparator = new OriginalClusterComparator(new OriginalSignalToNoiseChecker());

        URL url = ClusterComparatorTests.class.getClassLoader().getResource("uk/ac/ebi/pride/spectracluster/util/two_spectra.mgf");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        peptideSpectrumMatches = ParserUtilities.readMGFScans(inputFile);
    }

    @Test
    public void testClusterComparator() throws Exception {
        int result = clusterComparator.compare(peptideSpectrumMatches[0].asCluster(), peptideSpectrumMatches[1].asCluster());
        int originalResult = originalClusterComparator.compare(peptideSpectrumMatches[0].asCluster(), peptideSpectrumMatches[1].asCluster());
        Assert.assertEquals(result, originalResult);
    }
}
