package uk.ac.ebi.pride.spectracluster.quality;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.ParserUtilities;

import java.io.File;
import java.net.URL;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SignalToNoiseQualityCheckerTests {
    private ISpectrum[] peptideSpectrumMatches;
    private QualityScorer originalQualityScorer;
    private QualityScorer qualityScorer;


    @Before
    public void setUp() throws Exception {
        originalQualityScorer = new OriginalSignalToNoiseChecker();
        qualityScorer = new SignalToNoiseChecker();

        URL url = SignalToNoiseQualityCheckerTests.class.getClassLoader().getResource("uk/ac/ebi/pride/spectracluster/util/two_spectra.mgf");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        File inputFile = new File(url.toURI());
        peptideSpectrumMatches = ParserUtilities.readMGFScans(inputFile);
    }

    @Test
    public void testSignalToNoiseQualityChecker() throws Exception {
        for (ISpectrum peptideSpectrumMatch : peptideSpectrumMatches) {

            double originalScore = originalQualityScorer.calculateQualityScore(peptideSpectrumMatch);
            double score = qualityScorer.calculateQualityScore(peptideSpectrumMatch);
            Assert.assertEquals(score, originalScore, 0.1);
        }
    }
}
