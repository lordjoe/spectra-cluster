package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.TextIdentityMapper
 * Not sure whay I cannot find in 0.2 but uses the key and value unchanged
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
                ChargeBinMZKey mzKey = new ChargeBinMZKey(precursorCharge, bin, precursorMZ);

                  SpectraHadoopUtilities.incrementPartitionCounter(context,mzKey);   // debug to make sure partitioning is balanced

                // check partitioning
                countHashValues(mzKey, context);

                final String keyStr = mzKey.toString();
                onlyKey.set(keyStr);
                onlyValue.set(text);   // send on the MGF
                context.write(onlyKey, onlyValue);
            }
        }

    }



    public static final int NUMBER_REDUCERS = 600;

    // for debugging add a partitioning counter
    @SuppressWarnings("UnusedDeclaration")
    public void countHashValues(ChargeBinMZKey mzKey, Context context) {
  //      incrementPartitionCounters(mzKey, context);    //the reducer handle
     }

    @SuppressWarnings("UnusedDeclaration")
    public void incrementPartitionCounters(ChargeBinMZKey mzKey, Context context) {
        //noinspection SimplifiableIfStatement,PointlessBooleanExpression,ConstantConditions,RedundantIfStatement,ConstantIfStatement
        if(true)
            return; // dont do this
        int partition = mzKey.getPartitionHash() % NUMBER_REDUCERS;

        Counter counter = context.getCounter("Partitioning", "Partition" + String.format("%03d", partition));
        counter.increment(1);
    }




}
