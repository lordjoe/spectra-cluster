package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface ISpectrumRetriever {

    public ISpectrum retrieve(String spectrumId);


    public List<ISpectrum> retrieveAll();

}
