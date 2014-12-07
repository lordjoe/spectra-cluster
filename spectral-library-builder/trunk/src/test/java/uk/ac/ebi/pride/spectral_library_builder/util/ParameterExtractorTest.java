package uk.ac.ebi.pride.spectral_library_builder.util;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jg on 07.12.14.
 */
public class ParameterExtractorTest {
    private static final String TEST_FILE_PATH = ParameterExtractorTest.class.getClassLoader().getResource("test_2.mgf").getFile();
    private List<ISpectrum> testSpectra;


    @Before
    public void setUp() throws Exception {
        MgfFile mgfFile = new MgfFile(new File(TEST_FILE_PATH));

        testSpectra = new ArrayList<ISpectrum>(mgfFile.getMs2QueryCount());
        Iterator<Spectrum> it = mgfFile.getSpectrumIterator();

        while(it.hasNext()) {
            testSpectra.add(SpectrumConverter.asClusteringSpectrum(it.next()));
        }
    }

    @Test
    public void testParameterExtractor() throws Exception {
        ParameterExtractor parameterExtractor = new ParameterExtractor(testSpectra);

        Assert.assertEquals(46, parameterExtractor.getNumberOfProjects());
        Assert.assertEquals(1381, parameterExtractor.getNumberOfSpectra());
        Assert.assertEquals("RIQLVEEELDR", parameterExtractor.getMaximumSequence());
        Assert.assertEquals(1083, parameterExtractor.getMaximumSequenceCount());
        Assert.assertEquals(1381, parameterExtractor.getNumberOfPSMs());
        Assert.assertEquals(0.784214337F, parameterExtractor.getMaxPsmRatio());
        Assert.assertEquals(3.640808F, parameterExtractor.getPrecursorMzRange());

        Map<String, Integer> countPerIlIndependentSequence = parameterExtractor.getCountPerILIndependentSequence();

        for (String sequence : countPerIlIndependentSequence.keySet()) {
            if (sequence.equals("RIQLVEEELDR")) {
                Assert.assertEquals(1083, countPerIlIndependentSequence.get(sequence).intValue());
            }

            if (sequence.equals("QAGWSSQARPPDPLCLCR")) {
                Assert.assertEquals(1, countPerIlIndependentSequence.get(sequence).intValue());
                PSM psm = parameterExtractor.getPsmForIlIndependentSequence(sequence);
                Assert.assertEquals(2, psm.getPtms().size());
            }

            if (sequence.equals("GICVAFKNVVPLSAQAGDPR")) {
                Assert.assertEquals(3, countPerIlIndependentSequence.get(sequence).intValue());
                PSM psm = parameterExtractor.getPsmForIlIndependentSequence(sequence);
                Assert.assertEquals(1, psm.getPtms().size());
            }
        }
    }
}
