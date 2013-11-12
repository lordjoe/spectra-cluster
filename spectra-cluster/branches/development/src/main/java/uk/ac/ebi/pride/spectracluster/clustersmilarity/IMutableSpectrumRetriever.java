package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IMutableSpectrumRetriever extends ISpectrumRetriever {

    public void addSpectrum(IPeptideSpectrumMatch... spectra);
}
