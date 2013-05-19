package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.Collection;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.ISpectralCluster
 *
 * @author Steve Lewis
 * @author Rui Wang
 * @date 10/05/13
 */
public interface ISpectralCluster {

    /**
     * Get cluster id
     */
    public String getId();

    /**
     * Get consensus spectrum
     */
    public ISpectrum getConsensusSpectrum();

    /**
     * all internally spectrum
     */
    public Collection<ISpectrum> getClusteredSpectra();

    /**
     * count of internal spectrum
     */
    public int getClusteredSpectraCount();

    /**
     * Add a array of spectrum to cluster
     */
    public void addSpectra(ISpectrum... merged);

    /**
     * Remove an array of spectrum from cluster
     */
    public void removeSpectra(ISpectrum... removed);

}
