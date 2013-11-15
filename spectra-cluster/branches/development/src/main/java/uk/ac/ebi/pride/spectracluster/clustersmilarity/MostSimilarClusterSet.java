package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.ElapsedTimer;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
        } catch (Exception e) {
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

        File ooriginalFile = new File(args[1]);

        IClusterSet originalClusterSet = readClusterSet(simpleSpectrumRetriever, ooriginalFile, "SemiStableOriginal.clustering");
        timer.showElapsed("Read Original set");
        timer.reset(); // back to 0

        File newFile = new File(args[2]);
        IClusterSet newClusterSet = readClusterSet(simpleSpectrumRetriever, newFile, "StableNew.clustering");
        timer.showElapsed("Read New set");
        timer.reset(); // back to 0

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


    protected static void compareSameCluster(String[] args) throws IOException {

        SimpleSpectrumRetriever simpleSpectrumRetriever = new SimpleSpectrumRetriever();

        ElapsedTimer timer = new ElapsedTimer();

        File tsvFile = new File(args[0]);
        ClusterSimilarityUtilities.buildFromTSVFile(tsvFile, simpleSpectrumRetriever);
        timer.showElapsed("Read TSV");
        timer.reset(); // back to 0

        File ooriginalFile = new File(args[1]);

        IClusterSet originalClusterSet = readClusterSet(simpleSpectrumRetriever, ooriginalFile, "SemiStableNew.clustering");
        timer.showElapsed("Read   set");
        timer.reset(); // back to 0


        System.out.println("Number Clusters " + originalClusterSet.getClusterCount());
        List<ISpectralCluster> semiStableClusters = originalClusterSet.getMatchingClusters(ISpectralCluster.SEMI_STABLE_PREDICATE);
        System.out.println("Number SemiStable Clusters " + originalClusterSet.getClusterCount());
        originalClusterSet = new SimpleClusterSet(semiStableClusters);

        originalClusterSet = SimpleClusterSet.removeDuplicates(originalClusterSet);
        System.out.println("Number NonDuplicate SemiStable Clusters " + originalClusterSet.getClusterCount());

        List<ISpectralCluster> stableClusters = originalClusterSet.getMatchingClusters(ISpectralCluster.STABLE_PREDICATE);
        IClusterSet newClusterSet = new SimpleClusterSet(stableClusters);
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


    private static IClusterSet readClusterSet(SimpleSpectrumRetriever simpleSpectrumRetriever, File newFile, String saveName) {
        IClusterSet newClusterSet = ClusterSimilarityUtilities.buildFromClusteringFile(newFile, simpleSpectrumRetriever);
        if (newFile.isDirectory())
            ClusterSimilarityUtilities.saveSemiStableClusters(newClusterSet, new File(saveName));
        return newClusterSet;
    }


    /**
     * This is an example on how to use it
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
       // compareSameCluster(args);
         compareDifferentClusters(args);

    }


}
