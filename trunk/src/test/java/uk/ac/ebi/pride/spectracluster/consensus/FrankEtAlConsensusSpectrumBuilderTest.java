package uk.ac.ebi.pride.spectracluster.consensus;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.normalizer.TotalIntensityNormalizer;
import uk.ac.ebi.pride.spectracluster.spectrum.ConsensusSpectraItems;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class FrankEtAlConsensusSpectrumBuilderTest {
    private ConsensusSpectraItems[] consensusSpectraItems;
    private ConsensusSpectrumBuilder consensusSpectrumBuilder;

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
        consensusSpectrumBuilder = new FrankEtAlConsensusSpectrumBuilder(new TotalIntensityNormalizer());
    }

    @Test
    public void testBuildConsensusSpectrum() throws Exception {
        // iterate over all clusters
        for (ConsensusSpectraItems cluster : consensusSpectraItems) {
            ISpectrum consensusSpectrum = cluster.getConcensus();
            List<ISpectrum> spectra = cluster.getSpectra();
            ISpectrum newConsensusSpectrum = consensusSpectrumBuilder.buildConsensusSpectrum(spectra);

            areConsensusSpectraSimilar(consensusSpectrum, newConsensusSpectrum);
        }
    }

    private void areConsensusSpectraSimilar(ISpectrum consensusSpectrum1, ISpectrum consensusSpectrum2) {
        // check average m/z
        double difference = consensusSpectrum1.getPrecursorMz() - consensusSpectrum2.getPrecursorMz();
        assertTrue("Average m/z are different",difference <= 0.0001);

        // check all the peaks
        List<IPeak> peaks1 = consensusSpectrum1.getPeaks();
        List<IPeak> peaks2 = consensusSpectrum2.getPeaks();

        // check the size of the peaks
        assertSame("The number of peaks are different", peaks1.size(), peaks2.size());

        for (int i = 0; i < peaks1.size(); i++) {
            IPeak peak1 = peaks1.get(i);
            IPeak peak2 = peaks2.get(i);

            Assert.assertEquals(peak1.getMz(), peak2.getMz(), IPeak.SMALL_MZ_DIFFERENCE);
     //       assertSame("Peaks have different m/z", peak1.getMz(), peak2.getMz());
            Assert.assertEquals( peak1.getIntensity(), peak2.getIntensity(),IPeak.SMALL_INTENSITY_DIFFERENCE);
            assertSame("Peaks have different count", peak1.getCount(), peak2.getCount());
        }
    }

}
