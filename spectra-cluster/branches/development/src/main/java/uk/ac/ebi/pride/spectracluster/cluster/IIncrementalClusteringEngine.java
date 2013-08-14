package uk.ac.ebi.pride.spectracluster.cluster;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.IClusteringEngine
 * <p/>
 * This object does the clusters
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface IIncrementalClusteringEngine extends IClusteringEngine {

    /**
     * add one cluster and return any clusters which are too far in mz from further consideration
     * @return !null Cluster
     */
    public List<ISpectralCluster> addClusterIncremental(ISpectralCluster added);


}
