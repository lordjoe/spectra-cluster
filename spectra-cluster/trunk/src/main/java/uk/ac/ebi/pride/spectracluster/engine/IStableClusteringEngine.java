package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster;

import java.util.Collection;
import java.util.List;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public interface IStableClusteringEngine extends IIncrementalClusteringEngine {

    public void addUnstableCluster(ISpectralCluster unstableCluster);

    public void processStableCluster(ISpectralCluster stableCluster);

    public Collection<ISpectralCluster> getClusters();

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<ISpectralCluster> findNoneFittingSpectra(ISpectralCluster cluster);
}
