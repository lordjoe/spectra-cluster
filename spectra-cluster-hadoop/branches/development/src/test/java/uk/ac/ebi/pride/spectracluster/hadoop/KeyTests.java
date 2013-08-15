package uk.ac.ebi.pride.spectracluster.hadoop;

import org.junit.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.KeyTests
 * User: Steve
 * Date: 8/13/13
 */
public class KeyTests {

    @Test
    public void testKeys() throws Exception {
        final List<? extends ISpectrum> specs = ClusteringDataUtilities.readISpectraFromResource();
        for (ISpectrum spec : specs) {
            validateSpectrum(spec);
        }

    }

    protected void validateSpectrum(ISpectrum spec) {
        final double precursorMz = spec.getPrecursorMz();
        final int charge = spec.getPrecursorCharge();
        MZKey k1 = new MZKey(precursorMz);
        ChargeMZKey k2 = new ChargeMZKey(charge, precursorMz);
        // better not crash

        String s1 = k1.toString();
        String s2 = k2.toString();
        Assert.assertEquals(s2, String.format("%02d", charge) + ":" + s1);

        List<String> holder = new ArrayList<String>();
        List<ChargePeakMZKey> keyholder = new ArrayList<ChargePeakMZKey>();
        for (IPeak pk : spec.getPeaks()) {
            final float mz = pk.getMz();
            ChargePeakMZKey key = new ChargePeakMZKey(charge, mz, precursorMz);
            holder.add(key.toString());
            keyholder.add(key);
        }
        // make a sorted collection
        List<String> holderSort = new ArrayList<String>(holder);
        Collections.sort(holderSort);
        // because peaks ar MZ sorted keys should be
        Assert.assertArrayEquals(holderSort.toArray(),holder.toArray());

        // make a sorted collection
        List<ChargePeakMZKey> keyholderSort = new ArrayList<ChargePeakMZKey>(keyholder);
        Collections.sort(keyholderSort);
       // because peaks ar MZ sorted keys should be
        Assert.assertArrayEquals(keyholderSort.toArray(),keyholder.toArray());


    }
}
