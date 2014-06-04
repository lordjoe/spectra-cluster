package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.IWideBinner;
import org.apache.hadoop.io.Text;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.cluster.ICluster;
import uk.ac.ebi.pride.spectracluster.engine.IIncrementalClusteringEngine;
import uk.ac.ebi.pride.spectracluster.engine.IncrementalClusteringEngine;
import uk.ac.ebi.pride.spectracluster.io.CGFClusterAppender;
import uk.ac.ebi.pride.spectracluster.io.MGFSpectrumAppender;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.keys.ChargeBinMZKey;
import uk.ac.ebi.pride.spectracluster.keys.ChargeMZKey;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Collection;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.SpectrumMergeReducer.
 * Form clusters from peaks
 */
public class SpectrumMergeReducer extends AbstractClusteringEngineReducer {

    private double spectrumMergeWindowSize;


    @SuppressWarnings("UnusedDeclaration")
    public double getMajorMZ() {
        return getMajorPeak();
    }


    public double getSpectrumMergeWindowSize() {
        return spectrumMergeWindowSize;
    }

    @Override
    protected void setup(final Context context) throws IOException, InterruptedException {
        super.setup(context);
        boolean offsetBins = context.getConfiguration().getBoolean("offsetBins", false);
        if (offsetBins)
            setBinner((IWideBinner) getBinner().offSetHalf());

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
                getEngine() == null) {
            boolean usedata = updateEngine(context, mzKey);
            if (!usedata)
                return;
        }

        IIncrementalClusteringEngine engine = getEngine();

        int numberProcessed = 0;
        int numberNoremove = 0;
        int numberRemove = 0;

        //noinspection LoopStatementThatDoesntLoop
        for (Text val : values) {
            String valStr = val.toString();

            LineNumberReader rdr = new LineNumberReader((new StringReader(valStr)));
            final ICluster cluster = ParserUtilities.readSpectralCluster(rdr, null);

            if (cluster != null) {  // todo why might this happen
                if (engine != null) {     // todo why might this happen
                    // look in great detail at a few cases
//                    if (isInterestingCluster(cluster)) {
//                        Collection<ICluster> clusters = engine.getClusters();
//                        ClusterSimilarityUtilities.testAddToClusters(cluster, clusters); // break here
//                    }


                    final Collection<ICluster> removedClusters = engine.addClusterIncremental(cluster);
                    if (!removedClusters.isEmpty()) {
                        writeClusters(context, removedClusters);
                        numberRemove++;
                    } else
                        numberNoremove++;

                }
            }
            if (numberProcessed > 0 && numberProcessed % 100 == 0)
                getBinTime().showElapsed("processed " + numberProcessed, System.err);
            //     System.err.println("processed " + numberProcessed);
            numberProcessed++;
        }
    }

//    private static String[] DUPLICATE_IDS = {
//            "VYYFQGGNNELGTAVGK", //   606.51,606.54
//            "YEEQTTNHPVAIVGAR",   //    595.7100228.2
//            "WAGNANELNAAYAADGYAR",  // 666.67797
//            "AKQPVKDGPLSTNVEAK"
//
//    };

//    private static Set<String> INTERESTING_IDS = new HashSet<String>(Arrays.asList(DUPLICATE_IDS));


//    protected static boolean isInterestingCluster(ICluster test) {
//        List<ISpectrum> clusteredSpectra = test.getClusteredSpectra();
//        for (ISpectrum spc : clusteredSpectra) {
//            if (spc instanceof ISpectrum) {
//                String peptide = ((ISpectrum) spc).getPeptide();
//                if (peptide != null && INTERESTING_IDS.contains(peptide))
//                    return true;
//            }
//        }
//        return false;
//    }


    /**
     * this version of writeCluster does all the real work
     *
     * @param context
     * @param cluster
     * @throws IOException
     * @throws InterruptedException
     */
    protected void writeOneVettedCluster(@Nonnull final Context context, @Nonnull final ICluster cluster) throws IOException, InterruptedException {
        if (cluster.getClusteredSpectraCount() == 0)
            return; // empty dont bother

//        if (isInterestingCluster(cluster))
//            System.out.println(cluster.toString());

        IWideBinner binner1 = getBinner();
        float precursorMz = cluster.getPrecursorMz();
        int bin = binner1.asBin(precursorMz);
        // you can merge clusters outside the current bin but not write them
        if (bin != getCurrentBin())
            return;
        ChargeMZKey key = new ChargeMZKey(cluster.getPrecursorCharge(), precursorMz);

        StringBuilder sb = new StringBuilder();
        final CGFClusterAppender clusterAppender = CGFClusterAppender.INSTANCE;
        clusterAppender.appendCluster(sb, cluster);
        String string = sb.toString();

        if (string.length() > SpectraHadoopUtilities.MIMIMUM_CLUSTER_LENGTH) {
            writeKeyValue(key.toString(), string, context);

        }
    }


    /**
     * make a new engine because  either we are in a new peak or at the end (pMZKey == null
     *
     * @param context !null context
     */
    protected <T> boolean updateEngine(final Context context, final T key) throws IOException, InterruptedException {
        ChargeBinMZKey pMzKey = (ChargeBinMZKey) key;
        if (getEngine() != null) {
            final Collection<ICluster> clusters = getEngine().getClusters();
            writeClusters(context, clusters);
            setEngine(null);
        }
        boolean ret = true;
        // if not at end make a new engine
        if (pMzKey != null) {
            setEngine(getFactory().getIncrementalClusteringEngine((float)getSpectrumMergeWindowSize()));
            setMajorPeak(pMzKey.getPrecursorMZ());
            ret = setCurrentBin(pMzKey.getBin());
            setCurrentCharge(pMzKey.getCharge());
        }
        return ret;
    }


    /**
     * Called once at the end of the task.
     */
    @Override
    protected void cleanup(final Context context) throws IOException, InterruptedException {
        //    writeParseParameters(context);
        super.cleanup(context);
        int value;

        value = IncrementalClusteringEngine.numberOverlap;
        System.err.println("numberOverlap " + value);

        value = IncrementalClusteringEngine.numberNotMerge;
        System.err.println("numberNotMerge " + value);

        value = IncrementalClusteringEngine.numberLessGoodMerge;
        System.err.println("numberLessGoodMerge " + value);

        value = IncrementalClusteringEngine.numberGoodMerge;
        System.err.println("numberGoodMerge " + value);

        value = IncrementalClusteringEngine.numberGoodMerge;
        System.err.println("numberGoodMerge " + value);

        value = IncrementalClusteringEngine.numberReAsssigned;
        System.err.println("numberReAsssigned " + value);

    }

    /**
     * remember we built the database
     *
     * @param context
     * @throws java.io.IOException
     */
    @SuppressWarnings({"UnusedParameters", "UnusedDeclaration"})
    protected void writeParseParameters(final Context context) throws IOException {
       throw new UnsupportedOperationException("Unimplemented June 2 This"); // ToDo
//        Configuration cfg = context.getConfiguration();
//        HadoopTandemMain application = getApplication();
//        TaskAttemptID tid = context.getTaskAttemptID();
//        //noinspection UnusedDeclaration
//        String taskStr = tid.getTaskID().toString();
//        String paramsFile = application.getDatabaseName() + ".params";
//        Path dd = HadoopUtilities.getRelativePath(paramsFile);
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
