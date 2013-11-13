package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class MostSimilarClusterSet {

    private final Map<ISpectralCluster, MostSimilarClusters> clusterToSimilarity =
            new HashMap<ISpectralCluster, MostSimilarClusters>();
    private final IClusterSet baseSet;
    private final IClusterSet otherSet;
    private final IClusterDistance clusterDistance;

    public MostSimilarClusterSet(IClusterSet baseSet, IClusterSet otherSet, IClusterDistance clusterDistance) {
        this.baseSet = baseSet;
        this.otherSet = otherSet;
        this.clusterDistance = clusterDistance;
        buildSimilaritySets();
        addOtherSet();
    }

    /**
     * private final because this is called in the constructor
     */
    private final void buildSimilaritySets() {
        List<ISpectralCluster> clusters = baseSet.getClusters();
        for (ISpectralCluster cluster : clusters) {
            clusterToSimilarity.put(cluster, new MostSimilarClusters(cluster, clusterDistance));
        }
    }

    /**
     * private final because this is called in the constructor
     */
    private final void addOtherSet() {
        List<ISpectralCluster> clusters = otherSet.getClusters();
        for (MostSimilarClusters similarClusters : clusterToSimilarity.values()) {
            similarClusters.addClusters(clusters);
        }
    }

    public IClusterSet getBaseSet() {
        return baseSet;
    }

    public IClusterSet getOtherSet() {
        return otherSet;
    }

    public MostSimilarClusters getMostSimilarClusters(ISpectralCluster cluster) {
        return clusterToSimilarity.get(cluster);
    }
}
