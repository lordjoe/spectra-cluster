package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MostSimilarClusters {

    public static final int MAX_SIMILAR_CLUSTERS = 3;

    private final IPeptideSpectralCluster baseCluster;
    private final LimitedList<ClusterDistanceItem> mostSimilarClusters;
    private final IClusterDistance clusterDistance;
    private double bestDistance;


    public MostSimilarClusters(IPeptideSpectralCluster baseCluster, IClusterDistance clusterDistance) {
        this.baseCluster = baseCluster;
        this.clusterDistance = clusterDistance;
        this.mostSimilarClusters = new TreeSetLimitedList<ClusterDistanceItem>(ClusterDistanceItem.class, MAX_SIMILAR_CLUSTERS);
        bestDistance = Double.MAX_VALUE;
    }

    public IPeptideSpectralCluster getBaseCluster() {
        return baseCluster;
    }

    public IPeptideSpectralCluster getBestMatchingCluster() {

        ClusterDistanceItem bestMatch = getBestMatch();
        return bestMatch.getTarget();
    }

    public ClusterDistanceItem getBestMatch() {
        // List<ClusterDistanceItem> clusters = mostSimilarClusters.toList();
        //   Collections.sort(clusters);
        if (mostSimilarClusters.isEmpty())
            return null;
        return mostSimilarClusters.first();
    }

    public List<ClusterDistanceItem> getBestMatches() {
        return mostSimilarClusters.toList();
    }

    public double getBestDistance() {
        return bestDistance;
    }

    public List<ClusterDistanceItem> getOtherMatches() {
        List<ClusterDistanceItem> clusterDistanceItems = mostSimilarClusters.toList();
        clusterDistanceItems.remove(getBestMatch());
        Collections.sort(clusterDistanceItems);
        return clusterDistanceItems;
    }

    public ClusterDistanceItem getNextBestMatches() {
        List<ClusterDistanceItem> clusterDistanceItems = mostSimilarClusters.toList();
        if (clusterDistanceItems.size() < 2)
            return null;
        clusterDistanceItems.remove(getBestMatch());
        Collections.sort(clusterDistanceItems);
        return clusterDistanceItems.get(0);
    }

    public void addCluster(IPeptideSpectralCluster cluster) {
        double distance = clusterDistance.distance(baseCluster, cluster);
        if (distance < bestDistance) {
            bestDistance = distance;
            if (bestDistance < 0.2)
                distance = clusterDistance.distance(baseCluster, cluster); // break here
        }
        mostSimilarClusters.add(new ClusterDistanceItem(baseCluster, cluster, distance));
    }

    public void addClusters(Iterable<IPeptideSpectralCluster> clusters) {
        for (IPeptideSpectralCluster cluster : clusters) {
            if (baseCluster == cluster)
                continue; // do not compare a cluster to itself
            addCluster(cluster);
        }
    }

    public class ClusterDistanceComparator implements Comparator<IPeptideSpectralCluster> {

        @Override
        public int compare(IPeptideSpectralCluster o1, IPeptideSpectralCluster o2) {
            double distance1 = clusterDistance.distance(baseCluster, o1);
            double distance2 = clusterDistance.distance(baseCluster, o2);

            if (distance1 != distance2) {
                return distance1 < distance2 ? -1 : 1;
            } else {
                int hashCode1 = System.identityHashCode(o1);
                int hashCode2 = System.identityHashCode(o2);

                if (hashCode1 != hashCode2) {
                    return hashCode1 < hashCode2 ? -1 : 1;
                } else {
                    return 0;
                }
            }
        }
    }
}
