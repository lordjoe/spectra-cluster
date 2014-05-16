package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectralCluster;

import java.util.Collection;
import java.util.List;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public interface IStableClusteringEngine extends IIncrementalClusteringEngine {

    public void addUnstableCluster(IPeptideSpectralCluster unstableCluster);

    public void processStableCluster(IPeptideSpectralCluster stableCluster);

    public Collection<IPeptideSpectralCluster> getClusters();

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<IPeptideSpectralCluster> findNoneFittingSpectra(IPeptideSpectralCluster cluster);
}
