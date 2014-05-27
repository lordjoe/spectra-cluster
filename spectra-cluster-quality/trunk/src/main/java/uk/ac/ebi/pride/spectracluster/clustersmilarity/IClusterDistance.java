package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;
import uk.ac.ebi.pride.spectracluster.util.IAlgorithm;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.ClusterDistance
 * Measure distance between clusters
 * User: Steve
 * Date: 6/17/13
 */
public interface IClusterDistance extends IAlgorithm {
    public static final double DEFAULT_MINIMAL_DISTANCE = 0.15;


    /**
     * measure the distance between two clusters - a value of 0 says the clusters are the same or equivalent.
     * a cluster will always have a distance of 0 with itself
     *
     * @param c1 !null cluster
     * @param c2 !null cluster
     * @return distance >= 0
     */
    public double distance(IPeptideSpectralCluster c1, IPeptideSpectralCluster c2);

    /**
     * if distance is further than the return value we consider the distance is
     * not worth considering
     *
     * @return
     */
    public double getMinimalMatchDistance();

}
