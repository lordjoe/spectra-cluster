package uk.ac.ebi.pride.spectracluster;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.consensus.*;
import uk.ac.ebi.pride.spectracluster.normalizer.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.PeakBinningTests
 *
 * @author Steve Lewis
 * @date 19/05/13
 */
public class PeakBinningTests {

    private ConsensusSpectraItems[] consensusSpectraItems;

    @Before
    public void setUp() throws Exception {
        // load a file contains a list of clusters
        URL url = FrankEtAlConsensusSpectrumBuilderTest.class.getClassLoader().getResource("uk/ac/ebi/pride/spectracluster/util/spectra_400.0_4.0.cgf");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());

        consensusSpectraItems = ParserUtilities.readClusters(inputFile);

    }

    @Test
    public void testPeakBinning() throws Exception {
        for (int i = 0; i < consensusSpectraItems.length; i++) {
            ConsensusSpectraItems csi = consensusSpectraItems[i];
            for (ISpectrum sp : csi.getSpectra()) {
                testPeakBin(sp.getPeaks());
            }
        }

    }

    public void testPeakBin(List<IPeak> peaks) {
        int maxPerBin = 5;
        double minMZ = 0;
        double maxMZ = 5000;
        double binSize = 100;
        List<IPeak> binned = ClusterUtilities.getHighestInBins(peaks, minMZ, maxMZ, binSize, maxPerBin);
        for (IPeak iPeak : binned) {
            
        }
    }
}
