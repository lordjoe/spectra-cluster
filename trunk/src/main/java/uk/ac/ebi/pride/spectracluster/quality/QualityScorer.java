package uk.ac.ebi.pride.spectracluster.quality;

import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;

/**
 * Calculate the quality score of a given spectrum and
 * return the quality as a double
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface QualityScorer {

    public double calculateQualityScore(ISpectrum spectrum);
}
