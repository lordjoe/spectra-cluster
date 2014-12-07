package uk.ac.ebi.pride.spectral_library_builder.cli;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by jg on 07.12.14.
 */
public class TestSpectralLibraryBuilderCli {
    private static final String[] fileFormats = new String[] {"msp"};
    private static final String testFilePath = TestSpectralLibraryBuilderCli.class.getClassLoader().getResource("test_2.mgf").getFile();

    @Test
    public void testProcessFile() throws Exception {
        String[] convertedSpectrum = SpectralLibraryBuilderCli.processInputFile(testFilePath, fileFormats);

        Assert.assertEquals(1, convertedSpectrum.length);
        String[] lines = convertedSpectrum[0].split("\n");

        Assert.assertEquals(98, lines.length);
        Assert.assertEquals("Name: RIQLVEEELDR/2", lines[0]);
        Assert.assertEquals("Comment: Spec=Consensus Mods=0 Parent=2 Nreps=1381 Naa=11 MaxRatio=0.7842143 PrecursorMzRange=3.640808", lines[1]);
        Assert.assertEquals("Num peaks: 95", lines[2]);
    }
}
