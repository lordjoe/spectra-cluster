package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.ChargeMZNarrowBinMapper
 * Map using the narrow bins
 * User: Steve
 * Date: 8/14/13
 */
public class ChargeMZNarrowBinMapper extends AbstractParameterizedMapper<Text> {


    @Override
    public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        String label = key.toString();
        String text = value.toString();
        if (label == null || text == null)
            return;
        if (label.length() == 0 || text.length() == 0)
            return;

        IWideBinner binner = SpectraHadoopUtilities.DEFAULT_WIDE_MZ_BINNER;


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
                ChargeBinMZKey mzKey = new ChargeBinMZKey(precursorCharge, bin, precursorMZ);

                SpectraHadoopUtilities.incrementPartitionCounter(context, mzKey);   // debug to make sure partitioning is balanced

                // check partitioning
                countHashValues(mzKey, context);

                final String keyStr = mzKey.toString();
                writeKeyValue(keyStr, text, context);
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

    @SuppressWarnings("UnusedDeclaration")
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
