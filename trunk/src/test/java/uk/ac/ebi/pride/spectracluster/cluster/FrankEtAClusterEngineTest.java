package uk.ac.ebi.pride.spectracluster.cluster;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.consensus.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;
import uk.ac.ebi.pride.tools.fast_spectra_clustering.*;

import java.util.*;

import static org.junit.Assert.assertEquals;


/**
 * @author Rui Wang
 * @version $Id$
 */
public class FrankEtAClusterEngineTest {
     private FrankEtAlConsensusSpectrumBuilder consensusSpectrumBuilder;

    @Before
    public void setUp() throws Exception {
//
//        // create an instance of consensus spectrum builder
        consensusSpectrumBuilder = new FrankEtAlConsensusSpectrumBuilder(Defaults.INSTANCE.getDefaultIntensityNormalizer());
    }

    @Test
    public void testBuildConsensusSpectrum() throws Exception {
        final List<ConsensusSpectraItems> consensusSpectraItems = ClusteringTestUtilities.readConsensusSpectraItemsFromResource();
        // iterate over all clusters
        int index = 0;
        for (ConsensusSpectraItems cluster : consensusSpectraItems) {
            ISpectrum consensusSpectrum = cluster.getConcensus();
            List<ISpectrum> spectra = cluster.getSpectra();


            // make a concensus in bulk
//            List<IPeak> jpeaks;
//            List<List<IPeak>> spectra1;
//            spectra1 = asListOfLists(spectra);
//            jpeaks = jSpectrumBuilder.buildConsensusSpectrum(spectra1);
//            Collections.sort(jpeaks, PeakMzComparator.getInstance());
//             jpeaks = totalIntensityNormalizer.normalizePeaks(jpeaks);


            ISpectrum newConsensusSpectrum =  consensusSpectrumBuilder.buildConsensusSpectrum(spectra);

            if (!areConsensusSpectraSimilar(consensusSpectrum, newConsensusSpectrum)) {
                // repeat and debug failures - if you are here it will fail
                boolean failure = areConsensusSpectraSimilar(consensusSpectrum, newConsensusSpectrum);
                Assert.assertTrue(failure);
            }

            index++; // track where we are
        }
    }


    private static List<List<IPeak>> asListOfLists(List<ISpectrum> spectra) {
        List<List<IPeak>> ret = new ArrayList<List<IPeak>>();
        for (ISpectrum sp : spectra) {
            ret.add(sp.getPeaks());
        }
        return ret;
    }

    private boolean areConsensusSpectraSimilar(ISpectrum originalConcensus, ISpectrum newConcensus) {
        // check average m/z
        assertEquals(originalConcensus.getPrecursorMz(), newConcensus.getPrecursorMz(), 0.1);


        // check all the peaks
        List<IPeak> originalConcensusPeaks = originalConcensus.getPeaks();
        List<IPeak> newConcensusPeaks = newConcensus.getPeaks();
        // compare concensus spectra
        if (!arePeaksSimilar(originalConcensusPeaks, newConcensusPeaks)) {
            boolean failure = arePeaksSimilar(originalConcensusPeaks, newConcensusPeaks); // try again so we can walk through the code
            Assert.assertTrue(failure);
        }


        return true; // all ok
    }

    private boolean arePeaksSimilar(List<IPeak> peaks1, List<IPeak> peaks2) {
          // check the size of the peaks
        if (peaks1.size() != peaks2.size())
            return false;
        double total1 = 0;
        double total2 = 0;
        for (int i = 0; i < peaks1.size(); i++) {
            IPeak peak1 = peaks1.get(i);
            total1 += peak1.getIntensity();
            IPeak peak2 = peaks2.get(i);
            total2 += peak2.getIntensity();
        }

        // Note -
        // We need to compare all three spectra
        // 2 we should compare without failing so we can look hard at the differenece


        double del = 0;
        // only look at MZ
        for (int i = 0; i < peaks1.size(); i++) {
            IPeak peak1 = peaks1.get(i);
            IPeak peak2 = peaks2.get(i);

            del = Math.abs(peak1.getMz() - peak2.getMz());
            if (del > IPeak.SMALL_MZ_DIFFERENCE)
                return false; // fail
        }

        // repeat for intensity
        for (int i = 0; i < peaks1.size(); i++) {
            IPeak peak1 = peaks1.get(i);
            IPeak peak2 = peaks2.get(i);

            del = Math.abs(peak1.getIntensity() - peak2.getIntensity());
            if (del > IPeak.SMALL_INTENSITY_DIFFERENCE)
                return false; // fail
            if (peak1.getCount() != peak2.getCount())
                return false;   // fail
        }

        return true;
    }

}
