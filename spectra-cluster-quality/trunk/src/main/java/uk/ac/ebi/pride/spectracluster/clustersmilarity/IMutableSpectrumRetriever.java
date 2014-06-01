package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.spectrum.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IMutableSpectrumRetriever extends ISpectrumRetriever {

    public void addSpectra(ISpectrum... spectra);


}
