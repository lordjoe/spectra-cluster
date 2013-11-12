package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface ISpectrumRetriever {

    public IPeptideSpectrumMatch retrieve(String spectrumId);
}
