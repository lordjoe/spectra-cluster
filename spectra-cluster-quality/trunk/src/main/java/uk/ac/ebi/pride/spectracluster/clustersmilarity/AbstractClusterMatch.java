package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

/**
 * uk.ac.ebi.pride.spectracluster.clustersmilarity.AbstractClusterMatch
 * base class for Cluster matches concrete subclasses need to supply
 * getDistance fpr comparison
 *
 * @author Steve Lewis
 * @date 05/11/13
 */
public abstract class AbstractClusterMatch implements IClusterMatch {

    private final IPeptideSpectralCluster source;
    private final IPeptideSpectralCluster target;

    public AbstractClusterMatch(IPeptideSpectralCluster source, IPeptideSpectralCluster target) {
        this.source = source;
        this.target = target;
    }

    public IPeptideSpectralCluster getSource() {
        return source;
    }

    public IPeptideSpectralCluster getTarget() {
        return target;
    }

    /**
     * return some measure of the quality - base comparator use3s thia to
     * sort
     *
     * @param addedData - usually ignored but allows some implementations to bess other data
     * @return
     */
    @Override
    public abstract double getDistance();

    @Override
    public int compareTo(IClusterMatch o) {
        if (o == this)
            return 0;
        double mine = getDistance();
        double his = o.getDistance();
        if (mine != his)
            return mine > his ? -1 : 1; // sort decreasing match
        // just be consistent
        return System.identityHashCode(this) < System.identityHashCode(o) ? -1 : 1;

    }
}
