package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterDistanceItem
 *
 * @author Steve Lewis
 * @date 12/11/13
 */
public class ClusterDistanceItem implements Comparable<ClusterDistanceItem> {
    private final ISpectralCluster baseCluster;
    private final ISpectralCluster otherCluster;
    private final double distance;

    public ClusterDistanceItem(ISpectralCluster baseCluster, ISpectralCluster otherCluster, double distance) {
        this.baseCluster = baseCluster;
        this.otherCluster = otherCluster;
        this.distance = distance;
    }


    public ISpectralCluster getOtherCluster() {
        return otherCluster;
    }

    public ISpectralCluster getBaseCluster() {
        return baseCluster;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(ClusterDistanceItem o) {
        double delDist = getDistance() - o.getDistance();
        if (delDist != 0)
            return delDist < 0 ? -1 : 1;
        if (getOtherCluster() != o.getOtherCluster()) {
            return System.identityHashCode(getOtherCluster()) >
                    System.identityHashCode(o.getOtherCluster()) ? -1 : 1;

        }
        if (getBaseCluster() != o.getBaseCluster()) {
            return System.identityHashCode(getBaseCluster()) >
                    System.identityHashCode(o.getBaseCluster()) ? -1 : 1;

        }
        return 0;
    }
}
