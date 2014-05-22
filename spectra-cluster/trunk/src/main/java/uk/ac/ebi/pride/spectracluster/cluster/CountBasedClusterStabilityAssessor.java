package uk.ac.ebi.pride.spectracluster.cluster;

/**
 * Cluster stability assessor based on the spectra count
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public class CountBasedClusterStabilityAssessor implements IClusterStabilityAssessor {

    public static final int DEFAULT_STABLE_CLUSTER_SIZE = 20;
    public static final int DEFAULT_SEMI_STABLE_CLUSTER_SIZE = 10;

    private final int stableClusterSize;
    private final int semiStableClusterSize;

    public CountBasedClusterStabilityAssessor() {
        this.stableClusterSize = DEFAULT_STABLE_CLUSTER_SIZE;
        this.semiStableClusterSize = DEFAULT_SEMI_STABLE_CLUSTER_SIZE;
    }

    public CountBasedClusterStabilityAssessor(int stableClusterSize, int semiStableClusterSize) {
        this.stableClusterSize = stableClusterSize;
        this.semiStableClusterSize = semiStableClusterSize;
    }

    @Override
    public boolean isStable(ICluster cluster) {
        int count = cluster.getClusteredSpectraCount();
        if (count == 1)
            return false; // Duh but saves other tests
        if (count >= stableClusterSize)
            return true;
        // some tests for debugging
        if (count < 5)
            return false;
        if (count < 10)
            return false;
        if (count < 15)
            return false;

        return false;
    }

    @Override
    public boolean isSemiStable(ICluster cluster) {
        int count = cluster.getClusteredSpectraCount();
        if (count == 1)
            return false; // Duh but saves other tests
        if (count >= semiStableClusterSize)
            return true;
        // some tests for debugging
        if (count < 5)
            return false;
        return false;
    }
}
