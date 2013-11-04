package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface ISpectrumRetriever {

    public ISpectrum retrieve(String spectrumId);
}
