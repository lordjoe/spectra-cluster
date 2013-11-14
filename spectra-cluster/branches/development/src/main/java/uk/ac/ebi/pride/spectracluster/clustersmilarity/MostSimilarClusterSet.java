package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import com.lordjoe.utilities.ElapsedTimer;
import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void appendReport(Appendable appendable) {
        try {
            List<ClusterDistanceItem> goodMatches = new ArrayList<ClusterDistanceItem>();
            int numberBadMatches = 0;


              for (ISpectralCluster cluster : baseSet.getClusters()) {
                MostSimilarClusters mostSimilarClusters = getMostSimilarClusters(cluster);
                List<ClusterDistanceItem> bestMatches = mostSimilarClusters.getBestMatches();

                ClusterDistanceItem bestMatch = bestMatches.get(0);
                if (bestMatch.getDistance() < clusterDistance.getMinimalMatchDistance()) {
                    goodMatches.add(bestMatch);
                    bestMatch.appendReport(appendable);
                    appendable.append("\n");


                    for (int i = 1; i < bestMatches.size(); i++) {
                        ClusterDistanceItem clusterDistanceItem = bestMatches.get(i);
                        if (clusterDistanceItem.getDistance() < clusterDistance.getMinimalMatchDistance()) {
                            clusterDistanceItem.appendReport(appendable);
                            appendable.append("\n");
                        }
                    }
                    appendable.append("============================================\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is an example on how to use it
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        SimpleSpectrumRetriever simpleSpectrumRetriever = new SimpleSpectrumRetriever();

        ElapsedTimer timer = new ElapsedTimer();

        ClusterSimilarityUtilities.buildFromTSVFile(new File(args[0]), simpleSpectrumRetriever);
        timer.showElapsed("Read TSV");
        timer.reset(); // back to 0


        IClusterSet originalClusterSet = ClusterSimilarityUtilities.buildFromClusteringFile(new File(args[1]), simpleSpectrumRetriever);
        timer.showElapsed("Read Original Set");
        timer.reset(); // back to 0

        IClusterSet newClusterSet = ClusterSimilarityUtilities.buildFromClusteringFile(new File(args[2]), simpleSpectrumRetriever);
        timer.showElapsed("Read new set");
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
        writer.append("Algorithm " + ClusterContentDistance.INSTANCE.getName()).append("\n");
        mostSimilarClusterSet.appendReport(writer);
//        for (ISpectralCluster cluster : stableClusters) {
//            MostSimilarClusters mostSimilarClusters = mostSimilarClusterSet.getMostSimilarClusters(cluster);
//            List<ClusterDistanceItem> bestMatches = mostSimilarClusters.getBestMatches();
//            String csq = bestMatches.get(0).toString();
//            System.out.println(csq);
//            writer.println(csq);
//
//            for (int i = 1; i < bestMatches.size(); i++) {
//                ClusterDistanceItem clusterDistanceItem = bestMatches.get(i);
//                if (clusterDistanceItem.getDistance() < 0.2) {
//                    System.out.println(clusterDistanceItem);
//                    writer.println(clusterDistanceItem);
//                }
//            }
//            System.out.println("==============================");
//            writer.println("==============================");
//        }
        writer.close();

    }
}
