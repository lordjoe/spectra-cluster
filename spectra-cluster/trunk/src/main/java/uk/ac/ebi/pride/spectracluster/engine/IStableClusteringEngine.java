package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.ICluster;

import java.util.Collection;
import java.util.List;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public interface IStableClusteringEngine extends IIncrementalClusteringEngine {

    public void addUnstableCluster(ICluster unstableCluster);

    public void processStableCluster(ICluster stableCluster);

    public Collection<ICluster> getClusters();

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<ICluster> findNoneFittingSpectra(ICluster cluster);
}
