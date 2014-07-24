package uk.ac.ebi.pride.spectracluster.hadoop;

import com.lordjoe.algorithms.*;
import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;
import uk.ac.ebi.pride.spectracluster.engine.*;
import uk.ac.ebi.pride.spectracluster.io.*;
import uk.ac.ebi.pride.spectracluster.keys.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.hadoop.SampleRunner
 * User: Steve
 * Date: 7/23/2014
 */
public class SampleRunner {
    private double spectrumMergeWindowSize; // todo huh???

    private final Map<String, List<ICluster>> keysToClusters = new HashMap<String, List<ICluster>>();
    private final List<ICluster> inputClusters = new ArrayList<ICluster>();
    private final List<ICluster> foundClusters = new ArrayList<ICluster>();
    private final CountedMap<String> inputCounts = new CountedMap<String>();
    private final CountedMap<String> outputCounts = new CountedMap<String>();
    private final Set<String>  highDuplicates = new HashSet<String>();

    protected int currentCharge;
    private int currentBin;

    private IWideBinner binner = HadoopDefaults.DEFAULT_WIDE_MZ_BINNER;
    private IncrementalClusteringEngineFactory factory = new IncrementalClusteringEngineFactory();


    public SampleRunner(List<ICluster> clusters) {
        inputClusters.addAll(clusters);
        performMapping(clusters);
        List<String> itemsMoreThanN = inputCounts.getItemsMoreThanN(2);
        highDuplicates.addAll(itemsMoreThanN);
    }

    public int totalMapped()
    {
        int ret = 0;
        for (String s : keysToClusters.keySet()) {
           ret += keysToClusters.get(s).size();
        }
        return ret;
    }

    public int getCurrentCharge() {
        return currentCharge;
    }

    public void setCurrentCharge(final int pCurrentCharge) {
        currentCharge = pCurrentCharge;
    }

    public int getCurrentBin() {
        return currentBin;
    }

    public void setCurrentBin(final int pCurrentBin) {
        currentBin = pCurrentBin;
    }

    public double getSpectrumMergeWindowSize() {
        return spectrumMergeWindowSize;
    }

    public void setSpectrumMergeWindowSize(final double pSpectrumMergeWindowSize) {
        spectrumMergeWindowSize = pSpectrumMergeWindowSize;
    }

    public void analyze() {
        String[] keys = getSortedKeys();
        System.out.println("Number bins " + keys.length);


        ChargeBinMZKey key = new ChargeBinMZKey(keys[0]);
        setCurrentCharge(key.getCharge());
        setCurrentBin(key.getBin());
        int currentPartition = key.getPartitionHash();
        IIncrementalClusteringEngine engine = factory.getIncrementalClusteringEngine((float) getSpectrumMergeWindowSize());
        System.out.println("new Engine " + key);
        for (int i = 0; i < keys.length; i++) {
            String s = keys[i];
            key = new ChargeBinMZKey(s);
            if (key.getPartitionHash() != currentPartition) {
                for (ICluster iCluster : engine.getClusters()) {
                    saveCluster(key,iCluster);
                }
                engine = factory.getIncrementalClusteringEngine((float) getSpectrumMergeWindowSize());
                currentPartition = key.getPartitionHash();
                System.out.println("new Engine " + key);
            }
            reduceBin(key, engine);
        }
        for (ICluster iCluster : engine.getClusters()) {
            saveCluster(key,iCluster);
        }
    }

    protected void reduceBin(ChargeBinMZKey key, IIncrementalClusteringEngine engine) {
        List<ICluster> clusters = keysToClusters.get(key.toString());
        reduceBin(engine,key, clusters);
    }

    protected void reduceBin(IIncrementalClusteringEngine engine, ChargeBinMZKey key, List<ICluster> clusters) {
        for (ICluster cluster : clusters) {
            Collection<ICluster> toSaveClusters = engine.addClusterIncremental(cluster);
            for (ICluster toSaveCluster : toSaveClusters) {
                saveCluster(key,toSaveCluster);
            }
        }
    }

    protected void saveCluster( ChargeBinMZKey key,final ICluster pToSaveCluster) {
        int bin = binner.asBin(pToSaveCluster.getPrecursorMz());
        if(key.getBin() != bin)
            return;
        foundClusters.add(pToSaveCluster);
        addCluster(pToSaveCluster, outputCounts);
    }

    protected String[] getSortedKeys() {
        List<String> ret = new ArrayList<String>(keysToClusters.keySet());
        Collections.sort(ret);
        return ret.toArray(new String[ret.size()]);
    }

    public static void addCluster(ICluster cluster, CountedMap<String> counts) {
        for (ISpectrum spc : cluster.getClusteredSpectra()) {
            counts.add(spc.getId());
        }
    }


    public void reportCounts(final Appendable pOut) {


        System.out.println("Input Clusters " + inputClusters.size());
        System.out.println("Mapped Clusters " + totalMapped());
        System.out.println("Output Clusters " + foundClusters.size());


        System.out.println("Input counts " + inputCounts.getTotal());
        double[] countDistribution = inputCounts.getCountDistribution();
        for (int i = 1; i < countDistribution.length; i++) {
            System.out.println(i + " " + String.format("%8.5f", countDistribution[i]));

        }
        System.out.println("Output counts " + outputCounts.getTotal());
        countDistribution = outputCounts.getCountDistribution();
        for (int i = 1; i < countDistribution.length; i++) {
            System.out.println(i + " " + String.format("%8.5f", countDistribution[i]));

        }
    }

    public List<ICluster> getFoundClusters() {
        return foundClusters;
    }

    protected void performMapping(List<ICluster> clusters) {
        /**
         * what uk.ac.ebi.pride.spectracluster.hadoop.ChargeMZNarrowBinMapper
         * does
         *
         */
        for (ICluster cluster : clusters) {
            addCluster(cluster, inputCounts);
            int precursorCharge = cluster.getPrecursorCharge();
            double precursorMZ = cluster.getPrecursorMz();
            int[] bins = binner.asBins(precursorMZ);
            //noinspection ForLoopReplaceableByForEach
            for (int j = 0; j < bins.length; j++) {
                int bin = bins[j];
                ChargeBinMZKey mzKey = new ChargeBinMZKey(precursorCharge, bin, precursorMZ);
                String keyStr = mzKey.toString();
                if (keysToClusters.containsKey(keyStr)) {
                    keysToClusters.get(keyStr).add(cluster);
                }
                else {
                    List<ICluster> list = new ArrayList<ICluster>();
                    list.add(cluster);
                    keysToClusters.put(keyStr, list);
                }
            }

        }

    }

    /**
     * little piece of code to sample every nth cluster in a cgf file
     * @param args
     * @param every
     * @throws IOException
     */
    public static void writeClusters(ICluster[] args, int every) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter("Saved" + every + ".cgf"));
        for (int i = 0; i < args.length; i += 3 * every) {
            // try sequences
            ICluster arg = args[i];
            CGFClusterAppender.INSTANCE.appendCluster(pw, arg);
              arg = args[i + 1];
             CGFClusterAppender.INSTANCE.appendCluster(pw, arg);
              arg = args[i + 2];
             CGFClusterAppender.INSTANCE.appendCluster(pw, arg);
         }
        pw.close();
    }


    /**
     * little piece of code to sample every the first nth
     * @param args
     * @param every
     * @throws IOException
     */
    public static void writeFirstClusters(ICluster[] args, int every) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter("First" + every + "th.cgf"));
        for (int i = 0; i < args.length / every; i++) {
            // try sequences
            ICluster arg = args[i];
            CGFClusterAppender.INSTANCE.appendCluster(pw, arg);
          }
        pw.close();
    }


    /**
     * remerge clusters - this should cause things to get better
     * @param runner
     * @return
     */
    public static SampleRunner reanalyze(SampleRunner runner) {
        SampleRunner runner2 = new SampleRunner(runner.foundClusters);
        runner2.analyze();
        runner2.reportCounts(System.out);
        return runner2;
    }

    public static void main(String[] args) throws Exception {
        ElapsedTimer timer = new ElapsedTimer();
        ICluster[] clusters = ParserUtilities.readSpectralCluster(new File(args[0]));
        // Save a small sample for development
       // writeClusters(clusters,8);
        writeFirstClusters(clusters,5);

        SampleRunner runner = new SampleRunner(Arrays.asList(clusters));
        timer.showElapsed("read cgf");
        timer.reset();
        runner.analyze();
        timer.showElapsed("analyzed");
        runner.reportCounts(System.out);

        for (int i = 0; i < 3; i++) {
            System.out.println("===========================");
            timer.reset();
            runner = reanalyze(runner);
            timer.showElapsed("analyzed");
        }


        System.out.println("Done");
    }

}
