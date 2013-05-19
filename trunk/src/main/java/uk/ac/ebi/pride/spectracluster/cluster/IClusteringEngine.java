package uk.ac.ebi.pride.spectracluster.cluster;

import java.util.Collection;

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
     * Get clustered clusters
     */
    public Collection<ISpectralCluster> getClusters();

    /**
     * add some clusters
     */
    public void addClusters(ISpectralCluster... cluster);

    /**
     * clusters are merged in the internal collection
     * @return true is  anything happened
     */
    public boolean mergeClusters();

}
