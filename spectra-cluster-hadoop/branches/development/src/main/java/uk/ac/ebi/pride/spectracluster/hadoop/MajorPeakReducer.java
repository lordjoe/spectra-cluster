package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.*;
import org.apache.hadoop.io.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.MajorPeakReducer
 * Form clusters from peaks
 */
public class MajorPeakReducer extends AbstractClusteringEngineReducer {



    private double majorPeakWindowSize = Defaults.getMajorPeakMZWindowSize();


    public double getMajorPeakWindowSize() {
        return majorPeakWindowSize;
    }



    @Override
    public void reduceNormal(Text key, Iterable<Text> values,
                             Context context) throws IOException, InterruptedException {

        String keyStr = key.toString();
        ChargePeakMZKey mzKey = new ChargePeakMZKey(keyStr);

        // if we are in a different bin - different charge or peak
        if (mzKey.getCharge() != getCurrentCharge() || mzKey.getPeakMZ() != getMajorPeak()) {
            updateEngine(context, mzKey);
        }

        IIncrementalClusteringEngine engine = getEngine();
        if (engine == null)
            return; // very occasionally  we get null - not sure why

        //noinspection LoopStatementThatDoesntLoop
        for (Text val : values) {
            String valStr = val.toString();


            LineNumberReader rdr = new LineNumberReader((new StringReader(valStr)));
            final IPeptideSpectrumMatch match = ParserUtilities.readMGFScan(rdr);
            if (match == null)
                continue; // not sure why this happens but nothing seems like the thing to do
            final ISpectralCluster cluster = match.asCluster();


            final List<ISpectralCluster> removedClusters = engine.addClusterIncremental(cluster);

            writeClusters(context, removedClusters);


        }
    }

   
    protected void writeOneVettedCluster(final Context context, final ISpectralCluster cluster) throws IOException, InterruptedException {
        ChargeMZKey key = new ChargeMZKey(cluster.getPrecursorCharge(), cluster.getPrecursorMz());

        StringBuilder sb = new StringBuilder();
        cluster.append(sb);
        String string = sb.toString();

        if (string.length() > SpectraHadoopUtilities.MIMIMUM_CLUSTER_LENGTH) {
            writeKeyValue(key.toString(), string, context);
            incrementBinCounters(key, context); // how big are the bins - used in next job
        }
    }

    protected void incrementBinCounters(ChargeMZKey mzKey, Context context) {
         IWideBinner binner = Defaults.DEFAULT_WIDE_MZ_BINNER;
         int[] bins = binner.asBins(mzKey.getPrecursorMZ());
         //noinspection ForLoopReplaceableByForEach
         for (int i = 0; i < bins.length; i++) {
             int bin = bins[i];
             SpectraHadoopUtilities.incrementPartitionCounter(context, "Bin", bin);

         }

     }



    protected <T> boolean updateEngine(final Context context, final T key) throws IOException, InterruptedException
      {
          ChargePeakMZKey pMzKey = (ChargePeakMZKey)key;

          if (getEngine() != null) {
              final List<ISpectralCluster> clusters = getEngine().getClusters();
              writeClusters(context, clusters);
              setEngine(null);
          }
          // if not at end make a new engine
          if (pMzKey != null) {
              setEngine(getFactory().getIncrementalClusteringEngine( getMajorPeakWindowSize()));
              setMajorPeak(pMzKey.getPeakMZ());
              setCurrentCharge(pMzKey.getCharge());
          }
         return true;
     }



}
