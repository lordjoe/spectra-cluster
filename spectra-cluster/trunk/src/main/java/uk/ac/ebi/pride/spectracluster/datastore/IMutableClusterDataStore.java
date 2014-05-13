package uk.ac.ebi.pride.spectracluster.datastore;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import java.util.Collection;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.IMutableClusterDataStore
 * Expose methods for changing the datastore - most implementations can to these
 * but we may not wish to expost the capability
 * User: Steve
 * Date: 7/15/13
 *
 * @author Steve Lewis
 * @author Rui Wang
 */
public interface IMutableClusterDataStore extends IClusterDataStore {

    /**
     * Delete all data - use with caution
     */
    public void clearAllData();

    /**
     * store one cluster in the database
     *
     * @param clusterToStore
     */
    public void storeCluster(ISpectralCluster clusterToStore);


    /**
     * Store a set of clusters
     *
     * @param clustersToStore
     */
    public void storeClusters(Collection<ISpectralCluster> clustersToStore);

    /**
     * Remove a cluster
     *
     * @param clusterToRemove !null added
     */
    public void removeCluster(ISpectralCluster clusterToRemove);

}
