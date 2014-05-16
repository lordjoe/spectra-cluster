package uk.ac.ebi.pride.spectracluster.engine;

import uk.ac.ebi.pride.spectracluster.cluster.IPeptideSpectrumCluster;

import java.util.Collection;
import java.util.List;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public interface IUnStableClusteringEngine {

    public void addStableCluster(IPeptideSpectrumCluster unstableCluster);

    /**
     * try to move spectra to the stable cluster
     *
     * @param unstableCluster
     * @return true if changed
     */
    public boolean processUnStableCluster(IPeptideSpectrumCluster unstableCluster);

    public Collection<IPeptideSpectrumCluster> getClusters();

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<IPeptideSpectrumCluster> findNoneFittingSpectra(IPeptideSpectrumCluster cluster);
}
