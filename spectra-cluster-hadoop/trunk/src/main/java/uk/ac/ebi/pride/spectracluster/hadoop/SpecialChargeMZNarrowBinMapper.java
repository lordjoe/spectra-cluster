package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.io.*;
import uk.ac.ebi.pride.spectracluster.keys.*;

import java.io.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.TextIdentityMapper
 * Not sure whay I cannot find in 0.2 but uses the key and value unchanged
 * User: Steve
 * Date: 8/14/13
 */
@SuppressWarnings("UnusedDeclaration")
public class SpecialChargeMZNarrowBinMapper extends AbstractParameterizedMapper<Text> {

    private IWideBinner mapBinner;
     @Override
     protected void setup(final Context context) throws IOException, InterruptedException {
         super.setup(context);
         setMapBinner(HadoopDefaults.DEFAULT_WIDE_MZ_BINNER);
     }

     public IWideBinner getMapBinner() {
         return mapBinner;
     }

     public void setMapBinner(final IWideBinner pMapBinner) {
         mapBinner = pMapBinner;
         AbstractBinnedAPrioriPartitioner.setBinner(pMapBinner);
      }

    @Override
    public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        String label = key.toString();
        String text = value.toString();
        if (label == null || text == null)
            return;
        if (label.length() == 0 || text.length() == 0)
            return;

        IWideBinner binner = getMapBinner();


        LineNumberReader rdr = new LineNumberReader((new StringReader(text)));
        ICluster[] clusters = ParserUtilities.readSpectralCluster(rdr);
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < clusters.length; i++) {
            ICluster cluster = clusters[i];
            int precursorCharge = cluster.getPrecursorChargeX();
            double precursorMZ = cluster.getPrecursorMz();
            int[] bins = binner.asBins(precursorMZ);
            //noinspection ForLoopReplaceableByForEach
            for (int j = 0; j < bins.length; j++) {
                int bin = bins[j];
                ChargeBinMZKey mzKey = new ChargeBinMZKey(precursorCharge, bin, precursorMZ);


                MZKey mzkey = new MZKey(cluster.getPrecursorMz());


                SpectraHadoopUtilities.incrementPartitionCounter(context, mzKey);   // debug to make sure partitioning is balanced

                //  if(bin != 149986)
                //         continue; // todo remove this is to debug one case

                // check partitioning
                countHashValues(mzKey, context);

                final String keyStr = mzkey.toString();
                writeKeyValue(keyStr, text, context);
            }
        }

    }


    // for debugging add a partitioning counter
    @SuppressWarnings("UnusedDeclaration")
    public void countHashValues(ChargeBinMZKey mzKey, Context context) {
        //      incrementPartitionCounters(mzKey, context);    //the reducer handle
    }

    @SuppressWarnings("UnusedDeclaration")
    public void incrementPartitionCounters(ChargeBinMZKey mzKey, Context context) {
        //noinspection ConstantIfStatement
        if (true)
            return;
        int partition = mzKey.getPartitionHash() % HadoopUtilities.DEFAULT_TEST_NUMBER_REDUCERS;

        Counter counter = context.getCounter("Partitioning", "Partition" + String.format("%03d", partition));
        counter.increment(1);
    }


}
