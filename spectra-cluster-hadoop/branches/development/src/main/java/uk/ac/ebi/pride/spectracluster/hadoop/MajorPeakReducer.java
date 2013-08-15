package uk.ac.ebi.pride.spectracluster.hadoop;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;

/**
 * Form clusters from peaks
 */
public class MajorPeakReducer extends Reducer<Text, Text, Text, Text> {

    private int majorPeak;
    private int currentCharge;
    private IIncrementalClusteringEngine.IIncrementalClusteringEngineFactory factory = IncrementalClusteringEngine.getClusteringEngineFactory();
    private IIncrementalClusteringEngine engine;

    private final Text onlyKey = new Text();
    private final Text onlyValue = new Text();

    public Text getOnlyValue() {
        return onlyValue;
    }

    public Text getOnlyKey() {
        return onlyKey;
    }


    public int getMajorPeak() {
        return majorPeak;
    }

    public int getCurrentCharge() {
        return currentCharge;
    }

    public IIncrementalClusteringEngine getEngine() {
        return engine;
    }

    @Override
    public void reduce(Text key, Iterable<Text> values,
                       Context context) throws IOException, InterruptedException {

        String keyStr = key.toString();
        ChargePeakMZKey mzKey = new ChargePeakMZKey(keyStr);

        if (mzKey.getCharge() != getCurrentCharge() || mzKey.getPeakMZ() != getMajorPeak()) {
            updateEngine(context, mzKey);
        }

        IIncrementalClusteringEngine engine = getEngine();

        //noinspection LoopStatementThatDoesntLoop
        for (Text val : values) {
            String valStr = val.toString();


            LineNumberReader rdr = new LineNumberReader((new StringReader(valStr)));
            final IPeptideSpectrumMatch match = ParserUtilities.readMGFScan(rdr);
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
        final Text onlyKey = getOnlyKey();
        onlyKey.set(key.toString());

        final Text onlyValue = getOnlyValue();
        StringBuilder sb = new StringBuilder();
        cluster.append(sb);
        onlyValue.set(sb.toString());

        context.write(onlyKey, onlyValue);
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
            engine = factory.getIncrementalClusteringEngine();
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
