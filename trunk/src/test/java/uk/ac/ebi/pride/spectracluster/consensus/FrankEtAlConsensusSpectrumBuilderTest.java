package uk.ac.ebi.pride.spectracluster.consensus;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.normalizer.IntensityNormalizer;
import uk.ac.ebi.pride.spectracluster.normalizer.TotalIntensityNormalizer;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import java.io.File;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class FrankEtAlConsensusSpectrumBuilderTest {
    private ConsensusSpectraItems[] consensusSpectraItems;
    private IntensityNormalizer intensityNormalizer;
    private ConsensusSpectrumBuilder consensusSpectrumBuilder;
    private JohannesSpectrumBuilder jSpectrumBuilder;

    @Before
    public void setUp() throws Exception {
        // load a file contains a list of clusters
        URL url = FrankEtAlConsensusSpectrumBuilderTest.class.getClassLoader().getResource("uk/ac/ebi/pride/spectracluster/util/spectra_400.0_4.0.cgf");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());

        consensusSpectraItems = ParserUtilities.readClusters(inputFile);

        // create an instance of consensus spectrum builder
        intensityNormalizer = new TotalIntensityNormalizer();
        consensusSpectrumBuilder = new FrankEtAlConsensusSpectrumBuilder(intensityNormalizer);
        jSpectrumBuilder = new JohannesSpectrumBuilder();
    }

    @Test
    public void testBuildConsensusSpectrum() throws Exception {
        // iterate over all clusters
        for (ConsensusSpectraItems cluster : consensusSpectraItems) {
            ISpectrum consensusSpectrum = cluster.getConcensus();
            List<ISpectrum> spectra = cluster.getSpectra();
            List<IPeak> jpeaks;
            List<List<IPeak>> spectra1;

            ISpectrum newConsensusSpectrum = null;
            switch (spectra.size()) {
                case 1:
                    newConsensusSpectrum = consensusSpectrumBuilder.buildConsensusSpectrum(spectra);
                    areConsensusSpectraSimilar(consensusSpectrum, newConsensusSpectrum);
                    break;
                case 2:
                    newConsensusSpectrum = consensusSpectrumBuilder.buildConsensusSpectrum(spectra);
                    spectra1 = asListOfLists(spectra);
                    jpeaks = jSpectrumBuilder.buildConsensusSpectrum(spectra1);
                    Collections.sort(jpeaks, PeakMzComparator.getInstance());
                    areConsensusSpectraSimilar(consensusSpectrum, newConsensusSpectrum);
                    break;
                case 5:
                    newConsensusSpectrum = consensusSpectrumBuilder.buildConsensusSpectrum(spectra);
                    spectra1 = asListOfLists(spectra);
                    jpeaks = jSpectrumBuilder.buildConsensusSpectrum(spectra1);
                    Collections.sort(jpeaks, PeakMzComparator.getInstance());
                    areConsensusSpectraSimilar(consensusSpectrum, newConsensusSpectrum);
                    break;
                default:
                    newConsensusSpectrum = consensusSpectrumBuilder.buildConsensusSpectrum(spectra);
                    areConsensusSpectraSimilar(consensusSpectrum, newConsensusSpectrum);
                    break;
            }
        }
    }

    private static List<List<IPeak>> asListOfLists(List<ISpectrum> spectra) {
        List<List<IPeak>> ret = new ArrayList<List<IPeak>>();
        for (ISpectrum sp : spectra) {
            ret.add(sp.getPeaks());
        }
        return ret;
    }

    private void areConsensusSpectraSimilar(ISpectrum consensusSpectrum1, ISpectrum consensusSpectrum2) {
        // check average m/z
        assertEquals(consensusSpectrum1.getPrecursorMz(), consensusSpectrum2.getPrecursorMz(), 0.1);

        // check all the peaks
        List<IPeak> peaks1 = consensusSpectrum1.getPeaks();
        List<IPeak> peaks2 = consensusSpectrum2.getPeaks();


        // check the size of the peaks
        Assert.assertEquals(peaks1.size(), peaks2.size());

        for (int i = 0; i < peaks1.size(); i++) {
            IPeak peak1 = peaks1.get(i);
            IPeak peak2 = peaks2.get(i);

            assertEquals(peak1.getMz(), peak2.getMz(), IPeak.SMALL_MZ_DIFFERENCE);
            assertEquals(peak1.getIntensity(), peak2.getIntensity(), IPeak.SMALL_INTENSITY_DIFFERENCE);
            assertEquals(peak1.getCount(), peak2.getCount());
        }
    }

}
