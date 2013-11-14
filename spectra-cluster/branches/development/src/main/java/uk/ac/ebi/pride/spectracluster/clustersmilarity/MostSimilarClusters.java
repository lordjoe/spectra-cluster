package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.util.LimitedList;
import uk.ac.ebi.pride.spectracluster.util.TreeSetLimitedList;

import java.util.Comparator;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MostSimilarClusters {

    public static final int MAX_SIMILAR_CLUSTERS = 3;

    private final ISpectralCluster baseCluster;
    private final LimitedList<ClusterDistanceItem> mostSimilarClusters;
    private final IClusterDistance clusterDistance;


    public MostSimilarClusters(ISpectralCluster baseCluster, IClusterDistance clusterDistance) {
        this.baseCluster = baseCluster;
        this.clusterDistance = clusterDistance;
        this.mostSimilarClusters = new TreeSetLimitedList<ClusterDistanceItem>(ClusterDistanceItem.class, MAX_SIMILAR_CLUSTERS);
    }

    public ISpectralCluster getBaseCluster() {
        return baseCluster;
    }

    public ISpectralCluster getBestMatchingCluster() {
        return getBestMatch().getTarget();
    }

    public ClusterDistanceItem getBestMatch() {
        return mostSimilarClusters.first();
    }

    public List<ClusterDistanceItem> getBestMatches() {
        return mostSimilarClusters.toList();
    }

    public void addCluster(ISpectralCluster cluster) {
        double distance = clusterDistance.distance(baseCluster, cluster);
        mostSimilarClusters.add(new ClusterDistanceItem(baseCluster, cluster, distance));
    }

    public void addClusters(Iterable<ISpectralCluster> clusters) {
        for (ISpectralCluster cluster : clusters) {
            addCluster(cluster);
        }
    }

    public class ClusterDistanceComparator implements Comparator<ISpectralCluster> {

        @Override
        public int compare(ISpectralCluster o1, ISpectralCluster o2) {
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
