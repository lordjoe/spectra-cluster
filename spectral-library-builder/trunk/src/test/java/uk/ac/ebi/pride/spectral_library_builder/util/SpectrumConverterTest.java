package uk.ac.ebi.pride.spectral_library_builder.util;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by jg on 05.12.14.
 */
public class SpectrumConverterTest {
    private static final String TEST_FILE_PATH = SpectrumConverterTest.class.getClassLoader().getResource("test_2.mgf").getFile();
    private List<Spectrum> testSpectra;

    @Before
    public void setUp() throws Exception {
        MgfFile mgfFile = new MgfFile(new File(TEST_FILE_PATH));

        testSpectra = new ArrayList<Spectrum>(mgfFile.getMs2QueryCount());
        Iterator<Spectrum> it = mgfFile.getSpectrumIterator();

        while(it.hasNext()) {
            testSpectra.add(it.next());
        }
    }

    @Test
    public void testAsPeaks() throws Exception {
        for (Spectrum spectrum : testSpectra) {
            List<IPeak> peaks = SpectrumConverter.asPeaks(spectrum);

            for (IPeak peak : peaks) {
                // "convert" peak's float to double (rounding issue in Java)
                Double mz = Double.parseDouble(new Float(peak.getMz()).toString());
                Double intensity = Double.parseDouble(new Float(peak.getIntensity()).toString());

                Double spectrumIntensity = spectrum.getPeakList().get(mz);

                Assert.assertNotNull("No peak available for " + peak.getMz() + " : " + peak.getIntensity(),
                        spectrumIntensity);

                if (spectrumIntensity == 0 && intensity == 0)
                    continue;

                Assert.assertTrue("Different spectrum intensity: spectrum intens = " + spectrumIntensity + ", intenstiy = " + intensity,
                        Math.abs(spectrumIntensity - intensity) / (spectrumIntensity + intensity) * 2 < 0.0000001);
            }
        }
    }

    @Test
    public void testExtractSpectrumPsms() throws Exception {
        int index = 0;

        for (Spectrum spectrum : testSpectra) {
            uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum clusteringSpectrum = SpectrumConverter.asClusteringSpectrum(spectrum);
            Set<PSM> psms = SpectrumConverter.extractSpectrumPsms(clusteringSpectrum);

            for (PSM psm : psms) {
                if (index == 0) {
                    Assert.assertEquals(1, psms.size());
                    Assert.assertEquals("RIQLVEEELDR", psm.getSequence());
                }

                if (index == 96) {
                    Assert.assertEquals("QAGWSSQARPPDPLCLCR", psm.getSequence());

                    for (PSM.PTM ptm : psm.getPtms()) {
                        Assert.assertEquals("MOD:01214", ptm.getAccession());
                        Assert.assertTrue(ptm.getPosition() == 15 || ptm.getPosition() == 17);
                    }
                }

                if (index == 122) {
                    Assert.assertEquals("GICVAFKNVVPLSAQAGDPR", psm.getSequence());
                    Assert.assertEquals(1, psm.getPtms().size());

                    PSM.PTM ptm = psm.getPtms().iterator().next();
                    Assert.assertEquals("MOD:01214", ptm.getAccession());
                    Assert.assertEquals(3, ptm.getPosition());
                }
            }

            if (index == 78) {
                Assert.assertEquals("9606", clusteringSpectrum.getProperty(SpectrumConverter.PROPERTY_TAXID));
                Assert.assertEquals("PRD000154", clusteringSpectrum.getProperty(SpectrumConverter.PROPERTY_PROJECT));
            }
            if (index == 111) {
                Assert.assertEquals("10116", clusteringSpectrum.getProperty(SpectrumConverter.PROPERTY_TAXID));
                Assert.assertEquals("PRD000165", clusteringSpectrum.getProperty(SpectrumConverter.PROPERTY_PROJECT));
            }

            index++;
        }
    }
}
