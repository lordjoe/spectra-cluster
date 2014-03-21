package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.*;
import com.lordjoe.utilities.*;
import org.apache.hadoop.io.*;
import org.systemsbiology.hadoop.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.clustersmilarity.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;
import uk.ac.ebi.pride.spectracluster.util.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.SpectrumMergeReducer.
 * Form clusters from peaks
 */
public class SpectrumMergeReducer extends AbstractParameterizedReducer {

    private double majorMZ;
    private int currentCharge;
    private int currentBin;
    private IWideBinner binner = Defaults.DEFAULT_WIDE_MZ_BINNER;
    private IIncrementalClusteringEngine.IIncrementalClusteringEngineFactory factory = IncrementalClusteringEngine.getClusteringEngineFactory();
    private IIncrementalClusteringEngine engine;
    private ElapsedTimer binTime = new ElapsedTimer();
    private ElapsedTimer jobTime = new ElapsedTimer();
    private double spectrumMergeWindowSize;


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

    public double getSpectrumMergeWindowSize() {
        return spectrumMergeWindowSize;
    }

    @Override
    protected void setup(final Context context) throws IOException, InterruptedException {
        super.setup(context);
        boolean offsetBins = context.getConfiguration().getBoolean("offsetBins", false);
        if(offsetBins)
            binner = (IWideBinner)binner.offSetHalf();

          Defaults.configureAnalysisParameters(getApplication());
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

        int numberProcessed = 0;
        int numberNoremove = 0;
        int numberRemove = 0;

        //noinspection LoopStatementThatDoesntLoop
        for (Text val : values) {
            String valStr = val.toString();

            LineNumberReader rdr = new LineNumberReader((new StringReader(valStr)));
            final ISpectralCluster cluster = ParserUtilities.readSpectralCluster(rdr, null);

            if (cluster != null) {  // todo why might this happen
                if (engine != null) {     // todo why might this happen
                    // look in great detail at a few cases
                    if(isInterestingCluster(cluster)) {
                        List<ISpectralCluster> clusters = engine.getClusters();
                        ClusterSimilarityUtilities.testAddToClusters(cluster, clusters); // break here
                    }


                    final List<ISpectralCluster> removedClusters = engine.addClusterIncremental(cluster);
                    if(!removedClusters.isEmpty()) {
                        writeClusters(context, removedClusters);
                        numberRemove++;
                    }
                    else
                        numberNoremove++;

                }
            }
            if (numberProcessed > 0 && numberProcessed % 100 == 0)
                binTime.showElapsed("processed " + numberProcessed, System.err);
            //     System.err.println("processed " + numberProcessed);
            numberProcessed++;
        }
    }

    private static String[] DUPLICATE_IDS = {
            "VYYFQGGNNELGTAVGK", //   606.51,606.54
            "YEEQTTNHPVAIVGAR",   //    595.7100228.2
            "WAGNANELNAAYAADGYAR",  // 666.67797
            "AKQPVKDGPLSTNVEAK"

    };

    private static Set<String> INTERESTING_IDS = new HashSet<String>(Arrays.asList(DUPLICATE_IDS));


    protected static boolean isInterestingCluster(ISpectralCluster test)
    {
        List<ISpectrum> clusteredSpectra = test.getClusteredSpectra();
        for (ISpectrum spc : clusteredSpectra) {
            if(spc instanceof IPeptideSpectrumMatch)  {
                String peptide = ((IPeptideSpectrumMatch)spc).getPeptide();
                if(peptide != null && INTERESTING_IDS.contains(peptide))
                    return true;
            }
        }
        return false;
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
        if (cluster.getClusteredSpectraCount() == 0)
            return; // empty dont bother

        if(isInterestingCluster(cluster))
            System.out.println(cluster.toString());

        IWideBinner binner1 = getBinner();
        float precursorMz = cluster.getPrecursorMz();
        int bin = binner1.asBin(precursorMz);
        // you can merge clusters outside the current bin but not write them
        if (bin != currentBin)
            return;
        ChargeMZKey key = new ChargeMZKey(cluster.getPrecursorCharge(), precursorMz);

        StringBuilder sb = new StringBuilder();
        cluster.append(sb);
        String string = sb.toString();

        if (string.length() > SpectraHadoopUtilities.MIMIMUM_CLUSTER_LENGTH) {
            writeKeyValue(key.toString(), string, context);

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
        binTime.reset();
        jobTime.showElapsed("Handling bin " + currentBin + " " + midStr, System.err);
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
        boolean ret = true;
        // if not at end make a new engine
        if (pMzKey != null) {
            engine = factory.getIncrementalClusteringEngine(getSpectrumMergeWindowSize() );
            majorMZ = pMzKey.getPrecursorMZ();
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
        updateEngine(context, null); // write any left over clusters
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
