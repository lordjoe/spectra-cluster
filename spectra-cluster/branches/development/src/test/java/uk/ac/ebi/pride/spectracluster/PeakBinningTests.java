package uk.ac.ebi.pride.spectracluster;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.PeakBinningTests
 *
 * @author Steve Lewis
 * @date 19/05/13
 */
public class PeakBinningTests {



    @Test
    public void testPeakBinning() throws Exception {

        final List<ConsensusSpectraItems> consensusSpectraItems  = ClusteringTestUtilities.readConsensusSpectraItemsFromResource();
        for (ConsensusSpectraItems csi : consensusSpectraItems ) {
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
        //noinspection UnusedDeclaration
        List<IPeak> binned = ClusterUtilities.getHighestInBins(peaks, minMZ, maxMZ, binSize, maxPerBin);
//        for (IPeak iPeak : binned) {
//
//        }
    }
}
