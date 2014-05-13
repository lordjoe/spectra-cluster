package uk.ac.ebi.pride.spectracluster.spectrum;

/**
 * @author Rui Wang
 * @version $Id$
 */
@Deprecated
public interface ISpectrumQuality extends IPeaksSpectrum {

    public static final int BAD_QUALITY_MEASURE = -1;

    /**
     * Get the quality measure of a spectrum
     */
    public double getQualityScore();


}
