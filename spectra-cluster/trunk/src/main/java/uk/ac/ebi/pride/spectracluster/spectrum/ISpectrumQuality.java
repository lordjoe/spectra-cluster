package uk.ac.ebi.pride.spectracluster.spectrum;

import uk.ac.ebi.pride.spectracluster.quality.IQualityScorer;

/**
 * Spectrum quality interface
 *
 * @author Rui Wang
 * @version $Id$
 */
public interface ISpectrumQuality {

    /**
     * Get the quality measure of a spectrum
     */
    public double getQualityScore();


    /**
     * Get the quality scorer used
     * @return  quality scorer
     */
    public IQualityScorer getQualityScorer();
}
