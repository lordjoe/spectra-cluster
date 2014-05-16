package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.ElapsedTimer;
import com.lordjoe.utilities.FileUtilities;
import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import java.io.*;
import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MostSimilarClusterSet {

    private final Map<IPeptideSpectralCluster, MostSimilarClusters> clusterToSimilarity =
            new HashMap<IPeptideSpectralCluster, MostSimilarClusters>();
    private final Map<IPeptideSpectralCluster, Integer> timesClusterIsBestMatch =
            new HashMap<IPeptideSpectralCluster, Integer>();
    private final IClusterSet baseSet;
    private final IClusterDistance clusterDistance;
    private final List<RatedClusterSimilarity> byRating = new ArrayList<RatedClusterSimilarity>();
    private final int[] countByRating = new int[ClusterSimilarityEnum.values().length];

    public MostSimilarClusterSet(IClusterSet baseSet, IClusterDistance clusterDistance) {
        this.baseSet = baseSet;
        this.clusterDistance = clusterDistance;
        buildSimilaritySets();
    }

    /**
     * private final because this is called in the constructor
     */
    private final void buildSimilaritySets() {
        List<IPeptideSpectralCluster> clusters = baseSet.getClusters();
        for (IPeptideSpectralCluster cluster : clusters) {
            clusterToSimilarity.put(cluster, new MostSimilarClusters(cluster, clusterDistance));
        }
    }

    /**
     * private final because this is called in the constructor
     */
    public void addOtherSet(IClusterSet otherSet) {
//        int count = 0 ;
        List<IPeptideSpectralCluster> clusters = otherSet.getClusters();
        for (MostSimilarClusters similarClusters : clusterToSimilarity.values()) {
            similarClusters.addClusters(clusters);
//            if(count++ %1000 == 0)
//                System.out.println(count);
        }
    }

    public IClusterSet getBaseSet() {
        return baseSet;
    }

    public IClusterDistance getClusterDistance() {
        return clusterDistance;
    }

    public MostSimilarClusters getMostSimilarClusters(IPeptideSpectralCluster cluster) {
        return clusterToSimilarity.get(cluster);
    }

    public void clear() {
        clusterToSimilarity.clear();
        timesClusterIsBestMatch.clear();
        buildSimilaritySets();
    }

    public String getReport() {
        StringBuilder sb = new StringBuilder();

        appendReport(sb);

        return sb.toString();
    }

    public void buildQualityGroups() {
        Arrays.fill(countByRating, 0);
        byRating.clear();
        for (IPeptideSpectralCluster cluster : baseSet.getClusters()) {
            MostSimilarClusters mostSimilarClusters = getMostSimilarClusters(cluster);
            RatedClusterSimilarity rateed = new RatedClusterSimilarity(mostSimilarClusters);
            byRating.add(rateed);
        }
        Collections.sort(byRating);

        for (RatedClusterSimilarity rating : byRating) {
            ClusterSimilarityEnum rating1 = rating.getRating();
            countByRating[rating1.getValue()]++;
        }
        for (ClusterSimilarityEnum val : ClusterSimilarityEnum.values()) {
            System.out.println(val + " " + countByRating[val.getValue()]);
        }
    }


    public static final boolean USE_TSV = true;
    public static final boolean REPORT_STATISTICS_ONLY = true;

    public void appendReport(Appendable appendable) {
        try {
            buildQualityGroups();
            List<ClusterDistanceItem> goodMatches = new ArrayList<ClusterDistanceItem>();
            ClusterSimilarityEnum[] values = ClusterSimilarityEnum.values();
            int[] counts = new int[values.length];
            int[] firstIsSubsetCounts = new int[values.length];
            if (USE_TSV)
                RatedClusterSimilarity.appendHeaderTSV(appendable);

            for (RatedClusterSimilarity cluster : byRating) {
                ClusterSimilarityEnum rating = cluster.getRating();
                int value = rating.getValue();
                if (rating.isSubset()) {
                    MostSimilarClusters clusters = cluster.getClusters();
                    int sizeA = clusters.getBaseCluster().getClusteredSpectraCount();
                    int sizeB = clusters.getBestMatchingCluster().getClusteredSpectraCount();
                    if (sizeA > sizeB)
                        firstIsSubsetCounts[value]++;

                }
                counts[value]++;
                if (!REPORT_STATISTICS_ONLY) {
                    if (USE_TSV)
                        cluster.appendReportTSV(appendable);
                    else
                        cluster.appendReport(appendable);
                }
            }

            //        if (!USE_TSV) {
            appendable.append("Total " + byRating.size() + "\n");
            for (int i = 0; i < values.length; i++) {
                ClusterSimilarityEnum rating = values[i];
                int value = rating.getValue();
                int count = counts[value];
                appendable.append(rating.toString() + " " + count);
                if (rating.isSubset()) {
                    int firstIsSubsetCount = firstIsSubsetCounts[value];
                    appendable.append("\tA:" + firstIsSubsetCount + "\tB:" +
                            (count - firstIsSubsetCount));

                }
                appendable.append("\n");

                //            }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SimpleSpectrumRetriever getSimpleSpectrumRetriever(final String propertyFileName) {
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(propertyFileName));

            SimpleSpectrumRetriever simpleSpectrumRetriever = new SimpleSpectrumRetriever();


            String tsvFileName = properties.getProperty("SpectraFile");
            File tsvFile = new File(tsvFileName);
            ClusterSimilarityUtilities.buildFromTSVFile(tsvFile, simpleSpectrumRetriever);
            return simpleSpectrumRetriever;
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    protected static void compareDifferentClusters(SimpleSpectrumRetriever simpleSpectrumRetriever, String[] args1) throws IOException {

        ElapsedTimer timer = new ElapsedTimer();


        File originalFile = new File(args1[0]);

        IClusterSet originalClusterSet = readClusterSet(simpleSpectrumRetriever, originalFile);
        timer.showElapsed("Read Original set");
        timer.reset(); // back to 0

        ClusterStatistics stat = new ClusterStatistics(simpleSpectrumRetriever, originalClusterSet, new FractionInClustersOfSizeStatistics(simpleSpectrumRetriever));
        stat.gatherData();
        String report = stat.generateReport();
        System.out.println(report);

        File newFile = new File(args1[1]);
        IClusterSet newClusterSet = readClusterSet(simpleSpectrumRetriever, newFile);
        timer.showElapsed("Read New set");
        timer.reset(); // back to 0

        stat = new ClusterStatistics(simpleSpectrumRetriever, newClusterSet, new FractionInClustersOfSizeStatistics(simpleSpectrumRetriever));
        stat.gatherData();
        report = stat.generateReport();
        System.out.println(report);


        List<IPeptideSpectralCluster> stableClusters = newClusterSet.getMatchingClusters(new StableClusterPredicate());
        newClusterSet = new SimpleClusterSet(stableClusters);

        List<IPeptideSpectralCluster> semiStableClusters = originalClusterSet.getMatchingClusters(new SemiStableClusterPredicate());
        originalClusterSet = new SimpleClusterSet(semiStableClusters);

        Appendable out = System.out;
        if (args1.length > 2)
            out = new PrintWriter(new FileWriter(new File(args1[2])));
        if (!USE_TSV)
            out.append("Algorithm " + ClusterContentDistance.INSTANCE.getName()).append("\n");
        showClusterComparison(originalClusterSet, newClusterSet, out);
        timer.showElapsed("Build comparison");
        timer.reset(); // back to 0

        showClusterComparison(newClusterSet, originalClusterSet, out);
        timer.showElapsed("Build comparison");
        timer.reset(); // back to 0
        // writer.close();  // bad idea to close system .out
    }

    protected static void showClusterComparison(final IClusterSet pOriginalClusterSet, final IClusterSet pNewClusterSet, Appendable out) throws IOException {
        MostSimilarClusterSet mostSimilarClusterSet = new MostSimilarClusterSet(pNewClusterSet, ConcensusSpectrumDistance.INSTANCE);
        mostSimilarClusterSet.addOtherSet(pOriginalClusterSet);


        mostSimilarClusterSet.appendReport(out);
    }


    public static void compareClusterSets(SimpleSpectrumRetriever simpleSpectrumRetriever, IClusterSet originalClusterSet, IClusterSet newClusterSet) {
        compareClusterSets(simpleSpectrumRetriever, originalClusterSet, newClusterSet, System.out);
    }

    public static void compareClusterSets(SimpleSpectrumRetriever simpleSpectrumRetriever, IClusterSet originalClusterSet, IClusterSet newClusterSet, Appendable out) {

        try {
            ClusterStatistics stat = new ClusterStatistics(simpleSpectrumRetriever, originalClusterSet, new FractionInClustersOfSizeStatistics(simpleSpectrumRetriever));
            stat.gatherData();
            String report = stat.generateReport();
            System.out.println(report);


            stat = new ClusterStatistics(simpleSpectrumRetriever, newClusterSet, new FractionInClustersOfSizeStatistics(simpleSpectrumRetriever));
            stat.gatherData();
            report = stat.generateReport();
            out.append(report);
            out.append("\n");


            if (true)
                return; // todo add later
            out.append("=======New ste duplicates =======================================");
            out.append("\n");
            //  newClusterSet = SimpleClusterSet.removeDuplicates(newClusterSet);


            System.out.println("==============================================================");

            List<IPeptideSpectralCluster> stableClusters = newClusterSet.getMatchingClusters(new StableClusterPredicate());
            newClusterSet = new SimpleClusterSet(stableClusters);

            List<IPeptideSpectralCluster> semiStableClusters = originalClusterSet.getMatchingClusters(new SemiStableClusterPredicate());
            originalClusterSet = new SimpleClusterSet(semiStableClusters);

            MostSimilarClusterSet mostSimilarClusterSet = new MostSimilarClusterSet(newClusterSet, ConcensusSpectrumDistance.INSTANCE);
            mostSimilarClusterSet.addOtherSet(originalClusterSet);


            mostSimilarClusterSet.appendReport(out);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    public static final double[] REPORT_FRACTIONS = {1.0, 0.98, 0.9, 0.8, 0.0};


    /**
     * report number clusters with 100,98,95,80 pct one peptide
     *
     * @param clusters
     * @param out
     */
    public static void makePurityReport(List<IPeptideSpectralCluster> clusters, Appendable out) {
        List<ClusterPeptidePurity> purities = ClusterPeptidePurity.getPurities(clusters);
        double total = purities.size();
        int nextIndex = 0;
        int count = 0;
        try {
            out.append("size " + purities.size() + "\n");
            for (ClusterPeptidePurity purity : purities) {
                double fractionMostCommon = purity.getFractionMostCommon();
                if (fractionMostCommon < REPORT_FRACTIONS[nextIndex]) {
                    double pct = count * 100.0 / total;
                    out.append(String.format("%4.2f", REPORT_FRACTIONS[nextIndex]).trim() + " " + String.format("%6.1f", pct).trim() + "\n");
                    nextIndex++;
                }
                count++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    protected static void compareSameCluster(SimpleSpectrumRetriever spectra, String[] args1) throws IOException {


        ElapsedTimer timer = new ElapsedTimer();


        File originalFile = new File(args1[0]);

        IClusterSet originalClusterSet = readClusterSet(spectra, originalFile);
        timer.showElapsed("Read   set");
        timer.reset(); // back to 0


        System.out.println("Number Clusters " + originalClusterSet.getClusterCount());
        List<IPeptideSpectralCluster> semiStableClusters = originalClusterSet.getMatchingClusters(new SemiStableClusterPredicate());
        System.out.println("Number SemiStable Clusters " + originalClusterSet.getClusterCount());
        originalClusterSet = new SimpleClusterSet(semiStableClusters);

        System.out.println("=======Original Set duplicates =======================================");
        originalClusterSet = SimpleClusterSet.removeDuplicates(originalClusterSet);

        System.out.println("=======Semi Stable =======================================");
        makePurityReport(semiStableClusters, System.out);


        System.out.println("==============================================================");

        System.out.println("Number NonDuplicate SemiStable Clusters " + originalClusterSet.getClusterCount());

        List<IPeptideSpectralCluster> stableClusters = originalClusterSet.getMatchingClusters(new StableClusterPredicate());
        IClusterSet newClusterSet = new SimpleClusterSet(stableClusters);


        System.out.println("=======  Stable =======================================");
        makePurityReport(stableClusters, System.out);


        System.out.println("=======New Set duplicates =======================================");
        newClusterSet = SimpleClusterSet.removeDuplicates(newClusterSet);


        System.out.println("==============================================================");

        System.out.println("Number Stable Clusters " + newClusterSet.getClusterCount());

        MostSimilarClusterSet mostSimilarClusterSet = new MostSimilarClusterSet(newClusterSet, ConcensusSpectrumDistance.INSTANCE);
        mostSimilarClusterSet.addOtherSet(originalClusterSet);

        timer.showElapsed("Build comparison");
        timer.reset(); // back to 0

        PrintWriter writer = new PrintWriter(new FileWriter(new File(args1[1])));
        if (!USE_TSV)
            writer.append("Algorithm " + ClusterContentDistance.INSTANCE.getName()).append("\n");
        mostSimilarClusterSet.appendReport(writer);
        writer.close();
    }


    public static IClusterSet readClusterSet(SimpleSpectrumRetriever simpleSpectrumRetriever, File newFile) {
        IClusterSet newClusterSet = ClusterSimilarityUtilities.buildFromClusteringFile(newFile, simpleSpectrumRetriever);
        newClusterSet.setName(newFile.getName());
        //      if (newFile.isDirectory())
        //         ClusterSimilarityUtilities.saveSemiStableClusters(newClusterSet, new File(saveName));
        return newClusterSet;
    }


    public static void mergeClustersing(File directory, Appendable out) {
        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                String name = pathname.getName();
                if (!name.endsWith(".clustering"))
                    return false;
                if (name.startsWith("Big"))
                    return false;
                return true;
            }
        });
        for (File file : files) {
            FileUtilities.appendToAppendable(file, out);
        }
    }


    public static void mergeClustering(SimpleSpectrumRetriever sr, final String[] args) throws IOException {
        File directory = new File(args[0]);
        PrintWriter out = new PrintWriter(new FileWriter(args[1]));
        mergeClustersing(directory, out);
        out.close();
    }

    /**
     * This is an example on how to use it
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {

        SimpleSpectrumRetriever sr = getSimpleSpectrumRetriever(args[0]);
        args = Arrays.copyOfRange(args, 1, args.length);

        //         compareSameCluster(sr,args);
//          mergeClustering(sr,args);
        compareDifferentClusters(sr, args);

    }


}
