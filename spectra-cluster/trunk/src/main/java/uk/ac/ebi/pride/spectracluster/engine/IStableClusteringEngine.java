package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;

import java.util.Collection;
import java.util.List;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public interface IStableClusteringEngine extends IIncrementalClusteringEngine {

    public void addUnstableCluster(IPeptideSpectrumCluster unstableCluster);

    public void processStableCluster(IPeptideSpectrumCluster stableCluster);

    public Collection<IPeptideSpectrumCluster> getClusters();

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<IPeptideSpectrumCluster> findNoneFittingSpectra(IPeptideSpectrumCluster cluster);
}
