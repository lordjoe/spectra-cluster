package uk.ac.ebi.pride.spectracluster.quality;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.spectracluster.util.*;

/**
 * Calculate the quality score of a given spectrum and
 * return the quality as a double
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface QualityScorer extends IAlgorithm {
    /**
     * return the quality of the spectrum
     * @param spectrum !null spectrum
     * @return quality >= 0
     */
    public double calculateQualityScore(ISpectrum spectrum);
}
