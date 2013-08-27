package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.*;
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
        List<ChargePeakMZKey> keyholder = new ArrayList<ChargePeakMZKey>();
        final List<? extends ISpectrum> specs = ClusteringDataUtilities.readISpectraFromResource();
        for (ISpectrum spec : specs) {
            keyholder.addAll(validateSpectrum(spec));
        }
        validatePartitioner(keyholder)  ;
    }

    public static final int NUMBER_BUCKETS = 20;
    protected void validatePartitioner(List<ChargePeakMZKey> keyholder) {
        int[] counts = new int[NUMBER_BUCKETS];
        for (ChargePeakMZKey key : keyholder) {
             int hash = key.getPartitionHash();
             counts[hash % NUMBER_BUCKETS]++;
        }
        int maxCounts = 0;
        int minCounts = Integer.MAX_VALUE;
        //noinspection ForLoopReplaceableByForEach
       for (int i = 0; i < counts.length; i++) {
            int count = counts[i];
           maxCounts = Math.max(maxCounts,count);
           minCounts = Math.min(minCounts, count);

        }
        Assert.assertTrue( 1.3 * minCounts > maxCounts); // 30% of each other
    }

    protected List<ChargePeakMZKey> validateSpectrum(ISpectrum spec) {
        final double precursorMz = spec.getPrecursorMz();
        final int charge = spec.getPrecursorCharge();

        MZKey mzKey1 = new MZKey(precursorMz);
        ChargeMZKey k2 = new ChargeMZKey(charge, precursorMz);
        // better not crash

        String s1 = mzKey1.toString();
        String s2 = k2.toString();
        Assert.assertEquals(s2, String.format("%02d", charge) + ":" + s1);

        MZKey mzKey2 = new MZKey(s1);
        Assert.assertEquals(mzKey1,mzKey2);


        ChargeMZKey m3 = new ChargeMZKey(s2);
        Assert.assertEquals(k2,m3);

        ChargePeakMZKey chargePeakKey1 = new ChargePeakMZKey(charge, precursorMz,precursorMz);
        ChargePeakMZKey chargePeakKey2 = new ChargePeakMZKey(chargePeakKey1.toString());
        if(!chargePeakKey1.equals(chargePeakKey2))
          Assert.assertEquals(chargePeakKey1,chargePeakKey2);

        IWideBinner binner = SpectraHadoopUtilities.NARROW_MZ_BINNER;

        List<String> holder = new ArrayList<String>();
        List<ChargePeakMZKey> keyholder = new ArrayList<ChargePeakMZKey>();
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<ChargeBinMZKey> binKeyholder = new ArrayList<ChargeBinMZKey>();
        for (IPeak pk : spec.getPeaks()) {
            final float mz = pk.getMz();
            ChargePeakMZKey key = new ChargePeakMZKey(charge, mz, precursorMz);
            int[] bins = binner.asBins(precursorMz);
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < bins.length; i++) {
                int bin = bins[i];
                ChargeBinMZKey binkey = new ChargeBinMZKey(charge, bin, precursorMz);
                binKeyholder.add(binkey) ;
                String binKeyStr = binkey.toString();
                ChargeBinMZKey binky2 = new ChargeBinMZKey(binKeyStr);
                 Assert.assertEquals(binkey,binky2);
            }
            String keyStr = key.toString();
            ChargePeakMZKey ky2 = new ChargePeakMZKey(keyStr);
            Assert.assertEquals(key,ky2);
            holder.add(keyStr);
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

        return keyholder;
    }
}
