package uk.ac.ebi.pride.spectracluster.consensus;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MergeIdenticalPeaksTests {

    @SuppressWarnings("FieldCanBeLocal")
    private List<IPeak> peaks;
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private  ConsensusSpectrum consensusSpectrumBuilder;

    @Before
    public void setUp() throws Exception {
        peaks = new ArrayList<IPeak>();

        peaks.add(new Peak(123.45F, 10, 1));
        peaks.add(new Peak(123.47F, 20, 1));
        peaks.add(new Peak(123.48F, 30, 1));

        peaks.add(new Peak(223.0F, 10, 1));
        peaks.add(new Peak(223.1F, 20, 1));
        peaks.add(new Peak(223.4F, 30, 1));

        consensusSpectrumBuilder = new ConsensusSpectrum();
    }

    @Test
    public void testIdenticalPeaksMerged() throws Exception {
//        List<IPeak> mergedPeaks = consensusSpectrumBuilder.mergeIdenticalPeaksInternal(peaks);
//
//        Assert.assertEquals(4, mergedPeaks.size());
//        Assert.assertEquals(60.0F, mergedPeaks.get(0).getIntensity());
//        Assert.assertEquals(60.0F, mergedPeaks.get(1).getIntensity());
    }
}
