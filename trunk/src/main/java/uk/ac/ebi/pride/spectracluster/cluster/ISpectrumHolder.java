package uk.ac.ebi.pride.spectracluster.cluster;

import uk.ac.ebi.pride.spectracluster.spectrum.*;

/**
 * uk.ac.ebi.pride.spectracluster.cluster.ISpectrumHolder
 *  generalize the concept holding spectra - ISpectralCluster can do
 *   this but also new concensusSpectrumBuilder
 * User: Steve
 * Date: 7/10/13
 */
public interface ISpectrumHolder {

    /**
     * Add a array of spectrum to cluster
     */
    public void addSpectra(ISpectrum... merged);

    /**
     * Remove an array of spectrum from cluster
     */
    public void removeSpectra(ISpectrum... removed);


}
