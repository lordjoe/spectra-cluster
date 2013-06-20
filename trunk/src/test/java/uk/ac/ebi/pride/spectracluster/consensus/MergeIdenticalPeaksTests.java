package uk.ac.ebi.pride.spectracluster.consensus;

import junit.framework.Assert;
import org.junit.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MergeIdenticalPeaksTests {

    private List<IPeak> peaks;
    private  FrankEtAlConsensusSpectrumBuilder consensusSpectrumBuilder;

    @Before
    public void setUp() throws Exception {
        peaks = new ArrayList<IPeak>();

        peaks.add(new Peak(123.45, 10, 1));
        peaks.add(new Peak(123.47, 20, 1));
        peaks.add(new Peak(123.48, 30, 1));

        peaks.add(new Peak(223.0, 10, 1));
        peaks.add(new Peak(223.1, 20, 1));
        peaks.add(new Peak(223.4, 30, 1));

        consensusSpectrumBuilder = new  FrankEtAlConsensusSpectrumBuilder(Defaults.INSTANCE.getDefaultIntensityNormalizer());
    }

    @Test
    public void testIdenticalPeaksMerged() throws Exception {
        List<IPeak> mergedPeaks = consensusSpectrumBuilder.mergeIdenticalPeaks(peaks);

        Assert.assertEquals(2, mergedPeaks.size());
        Assert.assertEquals(60.0, mergedPeaks.get(0).getIntensity());
        Assert.assertEquals(60.0, mergedPeaks.get(1).getIntensity());
    }
}
