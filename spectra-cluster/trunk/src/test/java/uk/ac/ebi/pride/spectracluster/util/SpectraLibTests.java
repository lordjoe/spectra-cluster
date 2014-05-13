package uk.ac.ebi.pride.spectracluster.util;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeak;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * uk.ac.ebi.pride.spectracluster.util.SpectraLibTests
 * NOTE!!! add Setting/Compiler/ResourcePatterns *.msp;*.sptxt
 * User: Steve
 * Date: 12/20/13
 */
public class SpectraLibTests {
    public static final String SPTEXT_RESOURCE = "res://NIS_EColi.sptxt";
    public static final String MSP_RESOURCE = "res://Human_sample.msp";


    @Test
    public void testSPtextead() {
        LineNumberReader inp = ParserUtilities.getDescribedReader(SPTEXT_RESOURCE);
        List<ISpectrum> scs = readMSPCluster(inp);
        int number_scans = scs.size();
        Assert.assertEquals(1527, number_scans);
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (ISpectrum sc : scs) {
            String id = sc.getId();
            if (id == null)
                Assert.assertNotNull(id);  // make sure it exists
            Assert.assertEquals(index++, Integer.parseInt(id));
            String label = ((IPeptideSpectrumMatch) sc).getAnnotation();
            Assert.assertNotNull(label);  // make sure it exists

            final List<IPeak> peaks1 = sc.getPeaks();
            IPeak[] peaks = peaks1.toArray(new IPeak[peaks1.size()]);
            if (peaks.length < 2)
                Assert.assertTrue(peaks.length > 2);  // make there are some peaks
            sc.appendSPText(sb);

        }

        LineNumberReader inp2 = new LineNumberReader(new StringReader(sb.toString()));
        List<ISpectrum> scs2 = readMSPCluster(inp2);
        Assert.assertEquals(scs.size(), scs2.size());

        for (int i = 0; i < scs2.size(); i++) {
            ISpectrum spc = scs.get(i);
            ISpectrum spc2 = scs2.get(i);
            Assert.assertTrue(spc.equivalent(spc2));

        }


    }

    @Test
    public void testMSPRead() {
        LineNumberReader inp = ParserUtilities.getDescribedReader(MSP_RESOURCE);
        List<ISpectrum> scs = readMSPCluster(inp);
        int number_scans = scs.size();
        Assert.assertEquals(2599, number_scans);
        StringBuilder sb = new StringBuilder();
        for (ISpectrum sc : scs) {
            String label = ((IPeptideSpectrumMatch) sc).getAnnotation();
            Assert.assertNotNull(label);  // make sure it exists

            final List<IPeak> peaks1 = sc.getPeaks();
            IPeak[] peaks = peaks1.toArray(new IPeak[peaks1.size()]);
            if (peaks.length < 2)
                Assert.assertTrue(peaks.length > 2);  // make there are some peaks

            sc.appendMSF(sb);
        }

        String string = sb.toString();
        LineNumberReader inp2 = new LineNumberReader(new StringReader(string));
        List<ISpectrum> scs2 = readMSPCluster(inp2);
        Assert.assertEquals(scs.size(), scs2.size());

        for (int i = 0; i < scs2.size(); i++) {
            ISpectrum spc = scs.get(i);
            ISpectrum spc2 = scs2.get(i);
            Assert.assertTrue(spc.equivalent(spc2));

        }


    }


    public List<ISpectrum> readMSPCluster(final LineNumberReader pInp) {
        Assert.assertNotNull(pInp);  // make sure it exists
        List<ISpectrum> ret = new ArrayList<ISpectrum>();
        ISpectrum spectralCluster = ParserUtilities.readSPTextScan(pInp);
        int number_scans = 1;

        while (spectralCluster != null) {
            ret.add(spectralCluster);
            spectralCluster = ParserUtilities.readSPTextScan(pInp);
            number_scans++;
        }
        return ret;
    }

}
