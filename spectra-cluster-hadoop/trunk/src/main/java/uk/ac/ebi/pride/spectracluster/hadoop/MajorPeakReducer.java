package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.IWideBinner;
import org.apache.hadoop.io.Text;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.engine.IIncrementalClusteringEngine;
import uk.ac.ebi.pride.spectracluster.io.CGFClusterAppender;
import uk.ac.ebi.pride.spectracluster.io.MGFSpectrumAppender;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.keys.ChargeMZKey;
import uk.ac.ebi.pride.spectracluster.keys.ChargePeakMZKey;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;
import uk.ac.ebi.pride.spectracluster.util.ClusterUtilities;
import uk.ac.ebi.pride.spectracluster.util.Defaults;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Collection;

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
            final IPeptideSpectralCluster cluster = ClusterUtilities.asCluster(match);


            final Collection<IPeptideSpectralCluster> removedClusters = engine.addClusterIncremental(cluster);

            writeClusters(context, removedClusters);


        }
    }

    /**
     * this version of writeCluster does all the real work
     * @param context
     * @param cluster
     * @throws IOException
     * @throws InterruptedException
     */
    protected void writeOneVettedCluster(@Nonnull final Context context,@Nonnull  final IPeptideSpectralCluster cluster) throws IOException, InterruptedException {
        ChargeMZKey key = new ChargeMZKey(cluster.getPrecursorCharge(), cluster.getPrecursorMz());

        StringBuilder sb = new StringBuilder();
        final CGFClusterAppender clusterAppender = new CGFClusterAppender(new MGFSpectrumAppender());
        clusterAppender.appendCluster(sb, cluster);
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
              final Collection<IPeptideSpectralCluster> clusters = getEngine().getClusters();
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
