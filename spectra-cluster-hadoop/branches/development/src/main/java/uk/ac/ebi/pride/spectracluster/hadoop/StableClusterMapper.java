package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.StableClusterMapper
 * Handle stable and unstable clusters differently
 * User: Steve
 * Date: 8/14/13
 */
public class StableClusterMapper extends AbstractParameterizedMapper<Text> {



    public static final Random RND = new Random();
    public static final int WIDE_BIN_WIDTH = 16;
    public static final int WIDE_BIN_OVERLAP = 1;
    private static IWideBinner BINNER = new SizedWideBinner(
            IPeak.HIGHEST_USABLE_MZ,
            WIDE_BIN_WIDTH,
            IPeak.LOWEST_USABLE_MZ,
            WIDE_BIN_OVERLAP);

    private Map<Integer, String[]> binToAllKeys = new HashMap<Integer, String[]>();

    /**
     * return all posssible keys for a bin - this may be subfragmanets
     * @param bin
     * @param context
     * @return
     */
     public String[] getBinKeys(int bin, int charge,   final double mz, Context context) {
         // look up
        String[] ret = binToAllKeys.get(bin);
        if (ret != null)
            return ret;
        ret = buildBinKeys(bin,charge,mz,context);
        binToAllKeys.put(bin, ret);
        return ret;
    }

    private String[] buildBinKeys(int bin,   int charge,   final double mz,Context context) {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<String> holder = new ArrayList<String>();
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        int numberSpectraWithMZ = DaltonBinSize.getNumberSpectraWithMZ(bin,context.getConfiguration());
        int ng = getNumberGroups(numberSpectraWithMZ);
        for (int i = 0; i < ng; i++) {
            StableChargeBinMZKey key = new StableChargeBinMZKey(charge,bin,i,mz);
            holder.add(key.toString() );

        }

        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    protected int getNumberGroups(int totalSpectra)    {
        return 0;
    }

    @Override
    public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        String label = key.toString();
        String text = value.toString();
        if (label == null || text == null)
            return;
        if (label.length() == 0 || text.length() == 0)
            return;

        IWideBinner binner = BINNER;

        Text onlyKey = getOnlyKey();
        Text onlyValue = getOnlyValue();


        LineNumberReader rdr = new LineNumberReader((new StringReader(text)));
        ISpectralCluster[] clusters = ParserUtilities.readSpectralCluster(rdr);
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < clusters.length; i++) {
            ISpectralCluster cluster = clusters[i];
            int precursorCharge = cluster.getPrecursorCharge();
            double precursorMZ = cluster.getPrecursorMz();
            int[] bins = binner.asBins(precursorMZ);
            //noinspection ForLoopReplaceableByForEach
            for (int j = 0; j < bins.length; j++) {
                int bin = bins[j];
                String[] keys = getBinKeys(bin, precursorCharge,precursorMZ, context);
                if (cluster.isStable()) {
                 //noinspection ForLoopReplaceableByForEach
                    for (int k = 0; k < keys.length; k++) {
                        String akey = keys[k];
                          onlyKey.set(akey);
                        onlyValue.set(text);   // send on the MGF
                        context.write(onlyKey, onlyValue);

                    }

                } else {
                    // choose a random key for unstable clusters
                    String akey = keys[RND.nextInt(keys.length)];

                    akey = akey.replace(StableChargeBinMZKey.SORT_PREFIX,UnStableChargeBinMZKey.SORT_PREFIX);

                    onlyKey.set(akey);
                    onlyValue.set(text);   // send on the MGF
                    context.write(onlyKey, onlyValue);
                }

                ChargeBinMZKey mzKey = new ChargeBinMZKey(precursorCharge, bin, precursorMZ);

                SpectraHadoopUtilities.incrementPartitionCounter(context, mzKey);   // debug to make sure partitioning is balanced

                // check partitioning
                countHashValues(mzKey, context);

                final String keyStr = mzKey.toString();
                onlyKey.set(keyStr);
                onlyValue.set(text);   // send on the MGF
                context.write(onlyKey, onlyValue);
            }
        }

    }


    public static final int NUMBER_REDUCERS = 300;

    // for debugging add a partitioning counter
    @SuppressWarnings("UnusedDeclaration")
    public void countHashValues(ChargeBinMZKey mzKey, Context context) {
        //       incrementPartitionCounters(mzKey, context);    //the reducer handle
        //      incrementDaltonCounters((int)mzKey.getPrecursorMZ(),context);
    }

    public void incrementDaltonCounters(int precursorMZ, Context context) {
        Counter counter = context.getCounter("Binning", "MZ" + String.format("%03d", precursorMZ));
        counter.increment(1);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void incrementPartitionCounters(ChargeBinMZKey mzKey, Context context) {
        int partition = mzKey.getPartitionHash() % NUMBER_REDUCERS;

        Counter counter = context.getCounter("Partitioning", "Partition" + String.format("%03d", partition));
        counter.increment(1);
    }


}
