package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.*;
import com.lordjoe.utilities.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;

/**
 * Form clusters from peaks
 */
public class SpectrumMergeReducer extends AbstractParameterizedReducer {

    private double majorMZ;
    private int currentCharge;
    private int currentBin;
    private IWideBinner binner = SpectraHadoopUtilities.DEFAULT_WIDE_MZ_BINNER;
    private IIncrementalClusteringEngine.IIncrementalClusteringEngineFactory factory = IncrementalClusteringEngine.getClusteringEngineFactory();
    private IIncrementalClusteringEngine engine;
    private int numberProcessed;


    @SuppressWarnings("UnusedDeclaration")
    public double getMajorMZ() {
        return majorMZ;
    }

    public int getCurrentCharge() {
        return currentCharge;
    }

    public IIncrementalClusteringEngine getEngine() {
        return engine;
    }

    public int getCurrentBin() {
        return currentBin;
    }

    public IWideBinner getBinner() {
        return binner;
    }

    @Override
    public void reduceNormal(Text key, Iterable<Text> values,
                             Context context) throws IOException, InterruptedException {

        String keyStr = key.toString();
        //    System.err.println(keyStr);
        ChargeBinMZKey mzKey = new ChargeBinMZKey(keyStr);
        if (mzKey.getBin() < 0) {
            System.err.println("Bad bin " + keyStr);
            return;
        }

        // we only need to change engines for different charges
        if (mzKey.getCharge() != getCurrentCharge() ||
                mzKey.getBin() != getCurrentBin() ||
                engine == null) {
            boolean usedata = updateEngine(context, mzKey);
            if (!usedata)
                return;
        }

        IIncrementalClusteringEngine engine = getEngine();


        //noinspection LoopStatementThatDoesntLoop
        for (Text val : values) {
            String valStr = val.toString();

            LineNumberReader rdr = new LineNumberReader((new StringReader(valStr)));
            final ISpectralCluster cluster = ParserUtilities.readSpectralCluster(rdr, null);

            if (cluster != null) {  // todo why might this happen
                if (engine != null) {     // todo why might this happen
                    final List<ISpectralCluster> removedClusters = engine.addClusterIncremental(cluster);
                    writeClusters(context, removedClusters);

                } else {
                    Counter counter = context.getCounter("NoReduce", "NoEngine");
                    counter.increment(1);

                }
            } else {
                Counter counter = context.getCounter("NoReduce", "NoCluster");
                counter.increment(1);

            }
            if (numberProcessed++ % 400 == 0)
                System.err.println("processed " + numberProcessed);
            numberProcessed++;
        }
    }

    /**
     * write cluster and key
     *
     * @param context  !null context
     * @param clusters !null list of clusters
     */
    protected void writeClusters(final Context context, final List<ISpectralCluster> clusters) throws IOException, InterruptedException {
        if (clusters.isEmpty()) {
            Counter counter = context.getCounter("Reduce", "NoWrite");
            counter.increment(1);
            return;

        }
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
        if (cluster.getClusteredSpectraCount() == 0)
            return; // empty dont bother

        IWideBinner binner1 = getBinner();
        float precursorMz = cluster.getPrecursorMz();
        int bin = binner1.asBin(precursorMz);
        // you can merge clusters outside the current bin but not write them
        if (bin != currentBin)
            return;
        ChargeMZKey key = new ChargeMZKey(cluster.getPrecursorCharge(), precursorMz);

        final Text onlyKey = getOnlyKey();
        onlyKey.set(key.toString());

        final Text onlyValue = getOnlyValue();
        StringBuilder sb = new StringBuilder();
        cluster.append(sb);
        String string = sb.toString();

        if (string.length() > SpectraHadoopUtilities.MIMIMUM_CLUSTER_LENGTH) {
            onlyValue.set(string);
            context.write(onlyKey, onlyValue);

        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setMajorMZ(double majorMZ) {
        this.majorMZ = majorMZ;
    }

    public void setCurrentCharge(int currentCharge) {
        if (currentCharge == this.currentCharge)
            return;
        this.currentCharge = currentCharge;
        System.err.println("Setting charge   " + currentCharge);
    }

    public boolean setCurrentBin(int currentBin) {
        this.currentBin = currentBin;
        double mid = getBinner().fromBin(currentBin);
        String midStr = String.format("%10.1f", mid).trim();
        System.err.println("Handling bin " + currentBin + " " + midStr);
        //   if((currentBin != 149986))
        //       return false;
        return true; // use this
    }

    /**
     * make a new engine because  either we are in a new peak or at the end (pMZKey == null
     *
     * @param context !null context
     * @param pMzKey  !null unless done
     */
    protected boolean updateEngine(final Context context, final ChargeBinMZKey pMzKey) throws IOException, InterruptedException {
        if (engine != null) {
            final List<ISpectralCluster> clusters = engine.getClusters();
            writeClusters(context, clusters);
            engine = null;
        }
        numberProcessed = 0;
        boolean ret = true;
        // if not at end make a new engine
        if (pMzKey != null) {
            engine = factory.getIncrementalClusteringEngine();
            majorMZ = pMzKey.getPrecursorMZ();
            ret = setCurrentBin(pMzKey.getBin());
            setCurrentCharge(pMzKey.getCharge());

            // let the engine show hadoop some progress
            IProgressHandler handler = SpectraHadoopUtilities.buildProgressCounter("Clustering", "progress", context);
            engine.addProgressMonitor(handler);
        }
        return ret;
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
