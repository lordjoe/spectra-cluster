package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.*;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterDistance
 *   Measure distance between clusters
 * User: Steve
 * Date: 6/17/13
 */
public interface ClusterDistance {
    /**
     * measure the distance between two clusters - a value of 0 says the clusters are the same or equivalent.
     * a cluster will always have a distance of 0 with itself
     * @param c1  !null cluster
     * @param c2   !null cluster
     * @return  distance >= 0
     */
    public double distance(ISpectralCluster c1,ISpectralCluster c2);

}
