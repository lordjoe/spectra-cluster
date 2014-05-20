package uk.ac.ebi.pride.spectracluster.util;

import org.systemsbiology.hadoop.ISetableParameterHolder;

import java.util.UUID;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class StableClusterUtilities {
    public static final String STABLE_CLUSTER_SIZE_PROPERTY = "uk.ac.ebi.pride.spectracluster.util.ClusterUtilities.StableClusterSize";
    public static final String SEMI_STABLE_CLUSTER_SIZE_PROPERTY = "uk.ac.ebi.pride.spectracluster.util.ClusterUtilities.SemiStableClusterSize";

    public static final String PLACE_SPECTRUM_IN_BEST_CLUSTER = "uk.ac.ebi.pride.spectracluster.cluster.SpectrumInCluster.PlaceSpectrumInBestCluster";
    public static final String BREAK_UP_CLUSTERS_LESS_THAN = "uk.ac.ebi.pride.spectracluster.cluster.SpectrumInCluster.BreakUpClustersLessThan";

    public static final int DEFAULT_STABLE_CLUSTER_SIZE = 20;
    public static final int DEFAULT_SEMI_STABLE_CLUSTER_SIZE = 10;


    public static void setStableClusterSizeFromProperties(final ISetableParameterHolder pApplication) {
        int stableClusterSize = pApplication.getIntParameter(STABLE_CLUSTER_SIZE_PROPERTY, DEFAULT_STABLE_CLUSTER_SIZE);
        setStableClusterSize(stableClusterSize);
        int semiStableClusterSize = pApplication.getIntParameter(SEMI_STABLE_CLUSTER_SIZE_PROPERTY, DEFAULT_SEMI_STABLE_CLUSTER_SIZE);
        setSemiStableClusterSize(semiStableClusterSize);
    }


    public static final String STABLE_CLUSTER_PREFIX = "SC";
    @SuppressWarnings("UnusedDeclaration")
    public static final String SEMI_STABLE_CLUSTER_PREFIX = "SSC";

    private static int stableClusterSize = DEFAULT_STABLE_CLUSTER_SIZE;
    private static int semiStableClusterSize = DEFAULT_SEMI_STABLE_CLUSTER_SIZE;

    public static int getStableClusterSize() {
        return stableClusterSize;
    }

    public static void setStableClusterSize(final int pStableClusterSize) {
        stableClusterSize = pStableClusterSize;
    }

    public static int getSemiStableClusterSize() {
        return semiStableClusterSize;
    }

    public static void setSemiStableClusterSize(final int pSemiStableClusterSize) {
        semiStableClusterSize = pSemiStableClusterSize;
    }

    public static String getStableClusterId() {
        return STABLE_CLUSTER_PREFIX + UUID.randomUUID().toString();
    }

    public static String getSemiStableClusterId() {
        return STABLE_CLUSTER_PREFIX + UUID.randomUUID().toString();
    }
}
