package uk.ac.ebi.pride.spectracluster;

import com.sun.corba.se.impl.orb.*;

import java.util.*;

import org.junit.*;

/**
 * uk.ac.ebi.pride.spectracluster.SpectrumPeakTests
 *
 * @author Steve Lewis
 * @date 5/10/13
 */
public class SpectrumPeakTests {
    public static SpectrumPeakTests[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = SpectrumPeakTests.class;
    public static final Random RND = new Random();
    public static final int MAX_PEAKS = 100;


    public static ISpecClusterPeak[] buildPeaks() {
        List<ISpecClusterPeak> holder = new ArrayList<ISpecClusterPeak>();
        for (int i = 0; i < MAX_PEAKS; i++) {
            double mz = 1000 - i;
            double intensity = 100 * RND.nextDouble();
            holder.add(new SpecClusterPeak(mz, intensity));

        }

        ISpecClusterPeak[] ret = new ISpecClusterPeak[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    @Test
    public void sortTest() {
        ISpecClusterPeak[] pks = buildPeaks();
        Assert.assertEquals(MAX_PEAKS, pks.length);
        double last =  Double.MAX_VALUE;
        // we built them in inverted order
        for (int i = 0; i < pks.length; i++) {
            ISpecClusterPeak pk = pks[i];
            Assert.assertTrue(pk.getMassChargeRatio() < last);
            last =   pk.getMassChargeRatio();
        }
        Arrays.sort(pks);
        last = Double.MIN_VALUE;
        // they sort in ascendint order
        for (int i = 0; i < pks.length; i++) {
            ISpecClusterPeak pk = pks[i];
            Assert.assertTrue(pk.getMassChargeRatio() > last);
            last =   pk.getMassChargeRatio();
        }
    }

}
