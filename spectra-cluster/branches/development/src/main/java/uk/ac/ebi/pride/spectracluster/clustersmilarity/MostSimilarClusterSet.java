package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.*;
import uk.ac.ebi.pride.spectracluster.cluster.*;

import java.io.*;
import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MostSimilarClusterSet {

    private final Map<ISpectralCluster, MostSimilarClusters> clusterToSimilarity =
            new HashMap<ISpectralCluster, MostSimilarClusters>();
    private final Map<ISpectralCluster, Integer> timesClusterIsBestMatch =
            new HashMap<ISpectralCluster, Integer>();
    private final IClusterSet baseSet;
    private final IClusterDistance clusterDistance;
    private final List<RatedClusterSimilarity> byRating = new ArrayList<RatedClusterSimilarity>();


    public MostSimilarClusterSet(IClusterSet baseSet, IClusterDistance clusterDistance) {
        this.baseSet = baseSet;
        this.clusterDistance = clusterDistance;
        buildSimilaritySets();
    }

    /**
     * private final because this is called in the constructor
     */
    private final void buildSimilaritySets() {
        List<ISpectralCluster> clusters = baseSet.getClusters();
        for (ISpectralCluster cluster : clusters) {
            clusterToSimilarity.put(cluster, new MostSimilarClusters(cluster, clusterDistance));
        }
    }

    /**
     * private final because this is called in the constructor
     */
    public void addOtherSet(IClusterSet otherSet) {
//        int count = 0 ;
        List<ISpectralCluster> clusters = otherSet.getClusters();
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

    public MostSimilarClusters getMostSimilarClusters(ISpectralCluster cluster) {
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
        byRating.clear();
        for (ISpectralCluster cluster : baseSet.getClusters()) {
            MostSimilarClusters mostSimilarClusters = getMostSimilarClusters(cluster);
            RatedClusterSimilarity rateed = new RatedClusterSimilarity(mostSimilarClusters);
            byRating.add(rateed);
        }
        Collections.sort(byRating);
    }


    public static final boolean USE_TSV = true;

    public void appendReport(Appendable appendable) {
        try {
            buildQualityGroups();
            List<ClusterDistanceItem> goodMatches = new ArrayList<ClusterDistanceItem>();
            int numberBadMatches = 0;

            int numberGood = 0;
            int numberMedium = 0;
            int numberBad = 0;

            if (USE_TSV)
                RatedClusterSimilarity.appendHeaderTSV(appendable);

            for (RatedClusterSimilarity cluster : byRating) {
                ClusterSimilarityEnum rating = cluster.getRating();
                switch (rating) {
                    case Good:
                        numberGood++;
                        break;
                    case Medium:
                        numberMedium++;
                        break;
                    default:
                        numberBad++;
                        break;
                }

                if (USE_TSV)
                    cluster.appendReportTSV(appendable);
                else
                    cluster.appendReport(appendable);


            }

            if (!USE_TSV) {
                appendable.append("Total " + byRating.size() + "\n");
                appendable.append("Number Good " + numberGood + "\n");
                appendable.append("Number Medium " + numberMedium + "\n");
                appendable.append("Number Bad " + numberBad + "\n");
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static void compareDifferentClusters(String[] args) throws IOException {


        SimpleSpectrumRetriever simpleSpectrumRetriever = new SimpleSpectrumRetriever();

        ElapsedTimer timer = new ElapsedTimer();

        File tsvFile = new File(args[0]);
        ClusterSimilarityUtilities.buildFromTSVFile(tsvFile, simpleSpectrumRetriever);
        timer.showElapsed("Read TSV");
        timer.reset(); // back to 0

        File originalFile = new File(args[1]);

        IClusterSet originalClusterSet = readClusterSet(simpleSpectrumRetriever, originalFile, "SemiStableOriginal.clustering");
        timer.showElapsed("Read Original set");
        timer.reset(); // back to 0

        ClusterStatistics stat = new ClusterStatistics(simpleSpectrumRetriever, originalClusterSet, new FractionInClustersOfSizeStatistics(simpleSpectrumRetriever));
        stat.gatherData();
        String report = stat.generateReport();
        System.out.println(report);

        File newFile = new File(args[2]);
        IClusterSet newClusterSet = readClusterSet(simpleSpectrumRetriever, newFile, "StableNew.clustering");
        timer.showElapsed("Read New set");
        timer.reset(); // back to 0

        stat = new ClusterStatistics(simpleSpectrumRetriever, newClusterSet, new FractionInClustersOfSizeStatistics(simpleSpectrumRetriever));
        stat.gatherData();
        report = stat.generateReport();
        System.out.println(report);

        System.out.println("=======New ste duplicates =======================================");
        //  newClusterSet = SimpleClusterSet.removeDuplicates(newClusterSet);


        System.out.println("==============================================================");

        List<ISpectralCluster> stableClusters = newClusterSet.getMatchingClusters(ISpectralCluster.STABLE_PREDICATE);
        newClusterSet = new SimpleClusterSet(stableClusters);

        List<ISpectralCluster> semiStableClusters = originalClusterSet.getMatchingClusters(ISpectralCluster.SEMI_STABLE_PREDICATE);
        originalClusterSet = new SimpleClusterSet(semiStableClusters);

        MostSimilarClusterSet mostSimilarClusterSet = new MostSimilarClusterSet(newClusterSet, ConcensusSpectrumDistance.INSTANCE);
        mostSimilarClusterSet.addOtherSet(originalClusterSet);

        timer.showElapsed("Build comparison");
        timer.reset(); // back to 0

        PrintWriter writer = new PrintWriter(new FileWriter(new File(args[3])));
        if (!USE_TSV)
            writer.append("Algorithm " + ClusterContentDistance.INSTANCE.getName()).append("\n");
        mostSimilarClusterSet.appendReport(writer);
        writer.close();
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

            out.append("=======New ste duplicates =======================================");
            out.append("\n");
            //  newClusterSet = SimpleClusterSet.removeDuplicates(newClusterSet);


            System.out.println("==============================================================");

            List<ISpectralCluster> stableClusters = newClusterSet.getMatchingClusters(ISpectralCluster.STABLE_PREDICATE);
            newClusterSet = new SimpleClusterSet(stableClusters);

            List<ISpectralCluster> semiStableClusters = originalClusterSet.getMatchingClusters(ISpectralCluster.SEMI_STABLE_PREDICATE);
            originalClusterSet = new SimpleClusterSet(semiStableClusters);

            MostSimilarClusterSet mostSimilarClusterSet = new MostSimilarClusterSet(newClusterSet, ConcensusSpectrumDistance.INSTANCE);
            mostSimilarClusterSet.addOtherSet(originalClusterSet);


            mostSimilarClusterSet.appendReport(out);
        }
        catch (IOException e) {
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
    public static void makePurityReport(List<ISpectralCluster> clusters, Appendable out) {
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
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    protected static void compareSameCluster(String[] args) throws IOException {

        SimpleSpectrumRetriever spectra = new SimpleSpectrumRetriever();

        ElapsedTimer timer = new ElapsedTimer();

        File tsvFile = new File(args[0]);
        ClusterSimilarityUtilities.buildFromTSVFile(tsvFile, spectra);
        timer.showElapsed("Read TSV");
        timer.reset(); // back to 0

        File originalFile = new File(args[1]);

        IClusterSet originalClusterSet = readClusterSet(spectra, originalFile, "SemiStableNew.clustering");
        timer.showElapsed("Read   set");
        timer.reset(); // back to 0


        System.out.println("Number Clusters " + originalClusterSet.getClusterCount());
        List<ISpectralCluster> semiStableClusters = originalClusterSet.getMatchingClusters(ISpectralCluster.SEMI_STABLE_PREDICATE);
        System.out.println("Number SemiStable Clusters " + originalClusterSet.getClusterCount());
        originalClusterSet = new SimpleClusterSet(semiStableClusters);

        System.out.println("=======Original Set duplicates =======================================");
        originalClusterSet = SimpleClusterSet.removeDuplicates(originalClusterSet);

        System.out.println("=======Semi Stable =======================================");
        makePurityReport(semiStableClusters, System.out);


        System.out.println("==============================================================");

        System.out.println("Number NonDuplicate SemiStable Clusters " + originalClusterSet.getClusterCount());

        List<ISpectralCluster> stableClusters = originalClusterSet.getMatchingClusters(ISpectralCluster.STABLE_PREDICATE);
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

        PrintWriter writer = new PrintWriter(new FileWriter(new File(args[2])));
        if (!USE_TSV)
            writer.append("Algorithm " + ClusterContentDistance.INSTANCE.getName()).append("\n");
        mostSimilarClusterSet.appendReport(writer);
        writer.close();
    }


    public static IClusterSet readClusterSet(SimpleSpectrumRetriever simpleSpectrumRetriever, File newFile, String saveName) {
        IClusterSet newClusterSet = ClusterSimilarityUtilities.buildFromClusteringFile(newFile, simpleSpectrumRetriever);
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


    public static void mergeClustering(final String[] args) throws IOException {
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
        //  compareSameCluster(args);
        //  mergeClustering(args);
        compareDifferentClusters(args);

    }


}
