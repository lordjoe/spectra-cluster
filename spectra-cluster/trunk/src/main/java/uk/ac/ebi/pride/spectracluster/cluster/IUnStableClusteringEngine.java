package uk.ac.ebi.pride.spectracluster.cluster;

import java.util.Collection;
import java.util.List;

/**
 * @author Steve Lewis
 * @author Rui Wang
 * @version $Id$
 */
public interface IUnStableClusteringEngine {

    public void addStableCluster(ISpectralCluster unstableCluster);

    /**
     * try to move spectra to the stable cluster
     *
     * @param unstableCluster
     * @return true if changed
     */
    public boolean processUnStableCluster(ISpectralCluster unstableCluster);

    public Collection<ISpectralCluster> getClusters();

    /**
     * expose critical code for demerge - THIS NEVER CHANGES INTERNAL STATE and
     * usually is called on removed clusters
     *
     * @return !null Cluster
     */
    public List<ISpectralCluster> findNoneFittingSpectra(ISpectralCluster cluster);
}
