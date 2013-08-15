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

    public static interface IIncrementalClusteringEngineFactory {
        /**
         * build a new version
         * @return
         */
           public IIncrementalClusteringEngine getIncrementalClusteringEngine();
    }

    /**
     * add one cluster and return any clusters which are too far in mz from further consideration
     * NOTE clusters MUST be added in ascending MZ order
     * @param added  !null cluster to add
     * @return !null list of clusters not far enough away they will no longer change
     */
    public List<ISpectralCluster> addClusterIncremental(ISpectralCluster added);


}
