package uk.ac.ebi.pride.spectracluster.cluster;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.IClusteringEngine
 *
 * This object does the clusters
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface IClusteringEngine {

       /**
     * Get clustered clusters sorted by MZ is useful
     * @return !null list this will be sorted by mz a include clusters of all sizes
     */
    public List<ISpectralCluster> getClusters();


    /**
     * add some clusters
     */
    public void addClusters(ISpectralCluster... cluster);

    /**
     * clusters are merged in the internal collection
     * @return true is  anything happened
     */
    public boolean processClusters();

    /**
     * nice for debugging to name an engine
     * @return  possibly null name
     */
    public String getName();

    /**
     * nice for debugging to name an engine
     * @param pName   possibly null name
     */
    public void setName(final String pName);

    /**
     * total number of clusters including queued clustersToAdd
     * @return
     */
    public int size();


}
