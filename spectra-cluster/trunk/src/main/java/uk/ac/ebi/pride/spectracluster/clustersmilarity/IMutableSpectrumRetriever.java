package uk.ac.ebi.pride.spectracluster.clustersmilarity;

import uk.ac.ebi.pride.spectracluster.spectrum.IPeptideSpectrumMatch;

import java.util.*;

/**
 * @author Rui Wang
 * @version $Id$
 */
public interface IMutableSpectrumRetriever extends ISpectrumRetriever {

    public void addSpectra(IPeptideSpectrumMatch... spectra);


}
