package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.*;
import org.apache.hadoop.io.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;

/**
 * Form clusters from peaks
 */
public class MajorPeakReducer extends AbstractParameterizedReducer {

    private double majorPeak;
    private int currentCharge;
    private IIncrementalClusteringEngine.IIncrementalClusteringEngineFactory factory = IncrementalClusteringEngine.getClusteringEngineFactory();
    private IIncrementalClusteringEngine engine;


    public double getMajorPeak() {
        return majorPeak;
    }

    public int getCurrentCharge() {
        return currentCharge;
    }

    public IIncrementalClusteringEngine getEngine() {
        return engine;
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

    /**
     * write cluster and key
     *
     * @param context  !null context
     * @param clusters !null list of clusters
     */
    protected void writeClusters(final Context context, final List<ISpectralCluster> clusters) throws IOException, InterruptedException {
        for (ISpectralCluster cluster : clusters) {
            writeCluster(context, cluster);
        }
    }

    /**
     * write one cluster and key
     *
     * @param context !null context
     * @param cluster !null cluster
     */
    protected void writeCluster(final Context context, final ISpectralCluster cluster) throws IOException, InterruptedException {
        final List<ISpectralCluster> allClusters = getEngine().findNoneFittingSpectra(cluster);
        if (!allClusters.isEmpty()) {
            for (ISpectralCluster removedCluster : allClusters) {

                // drop all spectra
                final List<ISpectrum> clusteredSpectra = removedCluster.getClusteredSpectra();
                ISpectrum[] allRemoved = clusteredSpectra.toArray(new ISpectrum[clusteredSpectra.size()]);
                cluster.removeSpectra(allRemoved);

                // and write as stand alone
                writeOneCluster(context, removedCluster);
            }

        }
        // now write the original
        if (cluster.getClusteredSpectraCount() > 0)
            writeOneCluster(context, cluster);     // nothing removed
    }

    protected void writeOneCluster(final Context context, final ISpectralCluster cluster) throws IOException, InterruptedException {
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


    /**
     * make a new engine because  either we are in a new peak or at the end (pMZKey == null
     *
     * @param context !null context
     * @param pMzKey  !null unless done
     */
    protected void updateEngine(final Context context, final ChargePeakMZKey pMzKey) throws IOException, InterruptedException {
        if (engine != null) {
            final List<ISpectralCluster> clusters = engine.getClusters();
            writeClusters(context, clusters);
            engine = null;
        }
        // if not at end make a new engine
        if (pMzKey != null) {
            engine = factory.getIncrementalClusteringEngine(Defaults.DEFAULT_MAJOR_PEAK_MZ_WINDOW);
            majorPeak = pMzKey.getPeakMZ();
            currentCharge = pMzKey.getCharge();
        }
    }


    /**
     * Called once at the end of the task.
     */
    @Override
    protected void cleanup(final Context context) throws IOException, InterruptedException {
        //    writeParseParameters(context);
        updateEngine(context, null); // write any left over clusters
        super.cleanup(context);

    }

    /**
     * remember we built the database
     *
     * @param context
     * @throws java.io.IOException
     */
    @SuppressWarnings({"UnusedParameters", "UnusedDeclaration"})
    protected void writeParseParameters(final Context context) throws IOException {
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        Configuration cfg = context.getConfiguration();
//        HadoopTandemMain application = getApplication();
//        TaskAttemptID tid = context.getTaskAttemptID();
//        //noinspection UnusedDeclaration
//        String taskStr = tid.getTaskID().toString();
//        String paramsFile = application.getDatabaseName() + ".params";
//        Path dd = XTandemHadoopUtilities.getRelativePath(paramsFile);
//
//        FileSystem fs = FileSystem.get(cfg);
//
//        if (!fs.exists(dd)) {
//            try {
//                FastaHadoopLoader ldr = new FastaHadoopLoader(application);
//                String x = ldr.asXMLString();
//                FSDataOutputStream fsout = fs.create(dd);
//                PrintWriter out = new PrintWriter(fsout);
//                out.println(x);
//                out.close();
//            }
//            catch (IOException e) {
//                try {
//                    fs.delete(dd, false);
//                }
//                catch (IOException e1) {
//                    throw new RuntimeException(e1);
//                }
//                throw new RuntimeException(e);
//            }
//
//        }

    }


}
