package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;
import uk.ac.ebi.pride.spectracluster.util.ClusterCreateListener;

/**
 * uk.ac.ebi.pride.spectracluster.ClusterSetListener
 * User: Steve
 * Date: 4/14/2014
 */
public class ClusterSetListener implements ClusterCreateListener {
    private final IClusterSet set;

    public ClusterSetListener(final IClusterSet pSet) {
        set = pSet;
    }

    /**
     * initialize reading - if reading happens once - sayt from
     * one file all this may happen in the constructor
     *
     * @param otherData
     */
    @Override
    public void onClusterStarted(final Object... otherData) {
    }

    /**
     * do something when a cluster is created or read
     *
     * @param cluster
     * @param otherData
     */

    public static final int DOT_PER = 50;
    public static final int DOT_PER_LINE = 40;

    @Override
    public void onClusterCreate(final ISpectralCluster cluster, final Object... otherData) {
        set.addCluster(cluster);
        // show progress
        int size = set.getClusterCount();
        if (size > 0 && (size % DOT_PER) == 0) {
            System.out.print(".");
            if ((size % (DOT_PER_LINE * DOT_PER)) == 0)
                System.out.println();
        }
    }

    /**
     * do something when a cluster when the last cluster is read -
     * this may be after a file read is finished
     *
     * @param otherData
     */
    @Override
    public void onClusterCreateFinished(final Object... otherData) {
    }
}